package com.communote.plugins.mq.provider.activemq.security;

import java.security.Principal;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;

import org.apache.activemq.broker.Broker;
import org.apache.activemq.broker.BrokerFilter;
import org.apache.activemq.broker.ConnectionContext;
import org.apache.activemq.broker.TransportConnector;
import org.apache.activemq.command.ConnectionInfo;
import org.apache.activemq.security.JaasAuthenticationBroker;
import org.apache.activemq.security.SecurityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Authentication broker filter implementation
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class CommunoteUsernamePasswordJaasAuthenticationBroker extends BrokerFilter {

    /**
     * jaas security context
     */
    static class JaasSecurityContext extends SecurityContext {

        private final Subject subject;

        /**
         * @param userName
         *            user name
         * @param subject
         *            subject
         */
        public JaasSecurityContext(String userName, Subject subject) {
            super(userName);
            this.subject = subject;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.apache.activemq.security.SecurityContext#getPrincipals()
         */
        @Override
        public Set<Principal> getPrincipals() {
            return subject.getPrincipals();
        }

    }

    /** Logger. */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(CommunoteUsernamePasswordJaasAuthenticationBroker.class);

    private final String jaasConfiguration;

    private final CopyOnWriteArrayList<SecurityContext> securityContexts = new CopyOnWriteArrayList<SecurityContext>();

    /**
     * @param next
     *            next broker filter in the chain
     * @param jaasConfiguration
     *            jaas configuration name
     */
    public CommunoteUsernamePasswordJaasAuthenticationBroker(Broker next,
            String jaasConfiguration) {
        super(next);
        this.jaasConfiguration = jaasConfiguration;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.activemq.broker.BrokerFilter#addConnection(org.apache.activemq
     * .broker.ConnectionContext, org.apache.activemq.command.ConnectionInfo)
     */
    @Override
    public void addConnection(ConnectionContext context, ConnectionInfo info)
            throws Exception {
        if (context.getSecurityContext() == null) {
            // Set the TCCL since it seems JAAS needs it to find the login
            // module classes.
            ClassLoader original = Thread.currentThread()
                    .getContextClassLoader();
            Thread.currentThread().setContextClassLoader(
                    JaasAuthenticationBroker.class.getClassLoader());

            try {
                CommunoteMQCallbackHandler callback = new CommunoteMQCallbackHandler(
                        info.getUserName(), info.getPassword(), context
                                .getConnection().getRemoteAddress(),
                        ((TransportConnector) context.getConnector()).getUri()
                                .toString());
                LoginContext lc = new LoginContext(jaasConfiguration, callback);
                lc.login();
                Subject subject = lc.getSubject();
                SecurityContext s = new JaasSecurityContext(info.getUserName(),
                        subject);
                context.setSecurityContext(s);
                securityContexts.add(s);
            } catch (Exception e) {
                LOGGER.debug(e.getMessage(), e);
                throw new SecurityException(e.getMessage(), e);
            } finally {
                Thread.currentThread().setContextClassLoader(original);
            }
        }
        super.addConnection(context, info);
    }

    /**
     * Previously logged in users may no longer have the same access anymore. Refresh all the logged
     * into users.
     */
    public void refresh() {
        for (Iterator<SecurityContext> iter = securityContexts.iterator(); iter
                .hasNext();) {
            SecurityContext sc = iter.next();
            sc.getAuthorizedReadDests().clear();
            sc.getAuthorizedWriteDests().clear();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.activemq.broker.BrokerFilter#removeConnection(org.apache.activemq
     * .broker.ConnectionContext, org.apache.activemq.command.ConnectionInfo, Throwable)
     */
    @Override
    public void removeConnection(ConnectionContext context,
            ConnectionInfo info, Throwable error) throws Exception {
        super.removeConnection(context, info, error);
        if (securityContexts.remove(context.getSecurityContext())) {
            context.setSecurityContext(null);
        }
    }
}
