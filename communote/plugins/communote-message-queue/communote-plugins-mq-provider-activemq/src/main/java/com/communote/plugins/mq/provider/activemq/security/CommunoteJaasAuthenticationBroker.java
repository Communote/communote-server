package com.communote.plugins.mq.provider.activemq.security;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.cert.X509Certificate;

import javax.security.auth.Subject;

import org.apache.activemq.broker.Broker;
import org.apache.activemq.broker.BrokerFilter;
import org.apache.activemq.broker.ConnectionContext;
import org.apache.activemq.broker.Connector;
import org.apache.activemq.broker.EmptyBroker;
import org.apache.activemq.broker.ProducerBrokerExchange;
import org.apache.activemq.broker.TransportConnector;
import org.apache.activemq.command.ConnectionInfo;
import org.apache.activemq.command.Message;
import org.apache.activemq.command.MessageDispatch;
import org.apache.activemq.jaas.GroupPrincipal;
import org.apache.activemq.jaas.UserPrincipal;
import org.apache.activemq.security.JaasCertificateAuthenticationBroker;
import org.apache.activemq.security.JaasCertificateSecurityContext;
import org.apache.activemq.security.SecurityContext;
import org.apache.activemq.transport.tcp.SslTransportServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.communote.plugins.mq.adapter.activemq.configuration.ActiveMQEmbeddedConfiguration;
import com.communote.plugins.mq.provider.activemq.security.CommunoteUsernamePasswordJaasAuthenticationBroker.JaasSecurityContext;
import com.communote.plugins.mq.provider.activemq.settings.MQSettingsDAO;
import com.communote.plugins.mq.provider.jms.JMSAdapter;
import com.communote.server.persistence.common.security.CommunoteUserCertificate;

/**
 * A Jaas Authenticator that will either use a certificate or a username password authentication
 * based on the settings and the connection.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class CommunoteJaasAuthenticationBroker extends BrokerFilter {

    private final JaasCertificateAuthenticationBroker certificateBroker;

    private final CommunoteUsernamePasswordJaasAuthenticationBroker usernamePasswordBroker;
    private MQSettingsDAO settingsDao;
    private ActiveMQEmbeddedConfiguration embeddedConfiguration;
    /** The LOG. */
    private static Logger LOGGER = LoggerFactory.getLogger(CommunoteJaasAuthenticationBroker.class);

    /***
     * Simple constructor. Leaves everything to superclass.
     * 
     * @param settingsDao
     *            the settings dao
     * @param embeddedConfiguration
     *            the embedded configuration
     * @param next
     *            The Broker that does the actual work for this Filter.
     * @param jaasConfiguration
     *            The JAAS domain configuration name for non-SSL connections (refer to JAAS
     *            documentation).
     * @param jaasSslConfiguration
     *            The JAAS domain configuration name for SSL connections (refer to JAAS
     *            documentation).
     */
    public CommunoteJaasAuthenticationBroker(MQSettingsDAO settingsDao,
            ActiveMQEmbeddedConfiguration embeddedConfiguration, Broker next,
            String jaasConfiguration,
            String jaasSslConfiguration) {
        super(next);

        this.settingsDao = settingsDao;
        this.embeddedConfiguration = embeddedConfiguration;
        this.usernamePasswordBroker = new CommunoteUsernamePasswordJaasAuthenticationBroker(next,
                jaasConfiguration);
        this.certificateBroker = new JaasCertificateAuthenticationBroker(new EmptyBroker(),
                jaasSslConfiguration);

    }

    /**
     * Overridden to allow for authentication using different Jaas configurations depending on if
     * the connection is SSL or not.
     * 
     * @param context
     *            The context for the incoming Connection.
     * @param info
     *            The ConnectionInfo Command representing the incoming connection.
     * @throws Exception
     *             in case of an error
     */
    @Override
    public void addConnection(ConnectionContext context, ConnectionInfo info) throws Exception {
        try {
            if (context.getSecurityContext() == null) {

                Connector connector = context.getConnector();

                if (isEmbeddedConnection(context, info)) {

                    handleEmbeddedConnection(context, info);
                    super.addConnection(context, info);
                } else if (useCertificateAuthentication(connector, info)) {

                    this.handleCertificateConnection(context, info);
                    super.addConnection(context, info);
                } else if (assertForceSsl(context, info)) {
                    this.usernamePasswordBroker.addConnection(context, info);
                } else {
                    throw new SecurityException(
                            "Cannot add connection since no authentication could be appled. Check the configurations");
                }
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw new SecurityException("Error in authentication " + e.getMessage(), e);
        }
    }

    /**
     * If force ssl is enabled it checks if the connection is a ssl connection. If it is not a ssl
     * connection it then throws a security exception. if force ssl is deactivated nothing happens
     * here.
     * 
     * @param context
     *            the context
     * @param info
     *            the connection info
     * @return true if everything is cool (hence force ssl is not on or it is an ssl connection)
     * @throws IOException
     *             in case of an error
     * @throws URISyntaxException
     *             in case of an error
     */
    private boolean assertForceSsl(ConnectionContext context, ConnectionInfo info)
            throws IOException, URISyntaxException {
        if (this.settingsDao.isForceSSL() && !isSslConnection(context.getConnector())) {
            throw new SecurityException(
                    "ForceSSL is on but connection is not SSL. Will reject connection.");
        }
        return true;
    }

    /**
     * We expect a client certificate so check it if it is a communote user one
     * 
     * TODO do the extraction of the ou and cn and dn in a common place in the core
     * 
     * @param context
     *            the context
     * @param info
     *            the info
     * @throws Exception
     *             in case of an error
     */
    private void handleCertificateConnection(ConnectionContext context, ConnectionInfo info)
            throws Exception {

        if (!(info.getTransportContext() instanceof X509Certificate[])) {
            throw new SecurityException(
                    "Unable to authenticate transport without SSL certificate.");
        }

        X509Certificate[] certificates = (X509Certificate[]) info.getTransportContext();

        CommunoteUserCertificate cert = CommunoteUserCertificate.pickValid(certificates);
        if (cert == null) {
            throw new SecurityException(
                    "Unable to authenticate transport. No valid SSL certificate found.");
        }

        String dnName = cert.getSubjectName();

        UserPrincipal principal = new UserPrincipal(dnName);
        GroupPrincipal groupPrincipal = new GroupPrincipal(CommunoteJaasLoginModule.ROLE_USERS);

        Subject subject = new Subject();
        subject.getPrincipals().add(principal);
        subject.getPrincipals().add(groupPrincipal);

        SecurityContext s = new JaasCertificateSecurityContext(dnName, subject,
                (X509Certificate[]) info.getTransportContext());
        context.setSecurityContext(s);

    }

    /**
     * It is in an internal connection so register a user
     * 
     * @param context
     *            the context
     * @param info
     *            the connection info
     */
    private void handleEmbeddedConnection(ConnectionContext context, ConnectionInfo info) {
        UserPrincipal principal = new UserPrincipal(
                CommunoteJaasLoginModule.LOCAL_COMMUNOTE_USER);
        GroupPrincipal groupPrincipal = new GroupPrincipal("users");

        Subject subject = new Subject();
        subject.getPrincipals().add(principal);
        subject.getPrincipals().add(groupPrincipal);

        SecurityContext s = new JaasSecurityContext(info.getUserName(),
                subject);
        context.setSecurityContext(s);
    }

    /**
     * Check if the connection is internal
     * 
     * @param context
     *            the context
     * @param info
     *            the connection info
     * @return true if the connection is embedded
     */
    private boolean isEmbeddedConnection(ConnectionContext context, ConnectionInfo info) {
        if (embeddedConfiguration != null) {
            // check the username and password of the connection
            if (embeddedConfiguration.getUsername().equals(info.getUserName())
                    && embeddedConfiguration.getPassword()
                            .equals(info.getPassword())) {

                // check if its local
                if (CommunoteJaasLoginModule.isAddressLocal(context
                        .getConnection().getRemoteAddress())) {

                    // it is as an embedded connection
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Determin if it is a SSL connection
     * 
     * @param connector
     *            the connector
     * @return true if the connection is ssl
     * @throws IOException
     *             in case of an error
     * @throws URISyntaxException
     *             in case of an error
     */
    private boolean isSslConnection(Connector connector) throws IOException, URISyntaxException {
        boolean isSSL;
        if (connector instanceof TransportConnector) {
            TransportConnector transportConnector = (TransportConnector) connector;
            isSSL = transportConnector.getServer() instanceof SslTransportServer;
        } else {
            isSSL = false;
        }
        return isSSL;
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public void removeConnection(ConnectionContext context, ConnectionInfo info, Throwable error)
            throws Exception {

        Connector connector = context.getConnector();

        super.removeConnection(context, info, error);
        if (useCertificateAuthentication(connector, info)) {
            this.certificateBroker.removeConnection(context, info, error);
        } else {
            this.usernamePasswordBroker.removeConnection(context, info, error);
        }
    }

    @Override
    public void send(ProducerBrokerExchange producerExchange, Message messageSend) throws Exception {

		//remote address starts with vm (is embedded and within same VM): lets trust
        boolean trust = producerExchange.getConnectionContext().getConnection().getRemoteAddress()
                .startsWith("vm");

        messageSend.setProperty(JMSAdapter.MESSAGE_PROPERTY_TRUST_USER, trust);
        messageSend.setMarshalledProperties(null);

        super.send(producerExchange, messageSend);
    }

    /**
     * Determine if a client certificate authentication should be tried.
     * 
     * @param connector
     *            the connector
     * @param info
     *            the connection info
     * @return true if the certificate authentication should be used.
     * @throws IOException
     *             in case of an error
     * @throws URISyntaxException
     *             in case of an error
     */
    private boolean useCertificateAuthentication(Connector connector, ConnectionInfo info)
            throws IOException,
            URISyntaxException {
        boolean isSSL = isSslConnection(connector);

        boolean haveUsernamePassword = info.getUserName() != null && info.getPassword() != null;

        // if its as ssl request and we force a client certificate we will do the certificate
        // authentication
        if (isSSL && this.settingsDao.isForceSSLClientAuthentication()) {
            return true;
        }

        // if we have a password, use it
        if (haveUsernamePassword) {
            return false;
        }

        // finally depend on the SSL flag
        return isSSL;
    }
}
