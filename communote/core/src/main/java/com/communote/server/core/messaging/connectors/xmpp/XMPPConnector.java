package com.communote.server.core.messaging.connectors.xmpp;

import java.io.IOException;
import java.util.Locale;
import java.util.Properties;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.Roster.SubscriptionMode;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.blog.BlogAccessException;
import com.communote.server.api.core.blog.BlogManagement;
import com.communote.server.api.core.blog.BlogNotFoundException;
import com.communote.server.api.core.common.ClientAndChannelContextHolder;
import com.communote.server.api.core.config.ApplicationConfigurationProperties;
import com.communote.server.api.core.config.type.ApplicationPropertyXmpp;
import com.communote.server.core.messaging.connector.MessagerConnector;
import com.communote.server.core.messaging.connector.MessagerException;
import com.communote.server.core.messaging.vo.Message;
import com.communote.server.core.user.UserManagement;
import com.communote.server.model.messaging.MessagerConnectorType;
import com.communote.server.persistence.common.messages.ResourceBundleManager;
import com.communote.server.service.NoteService;

/**
 * Implementation of {@link MessagerConnector} for XMPP integration using Smack API.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class XMPPConnector implements MessagerConnector {

    private static final Logger LOG = LoggerFactory.getLogger(XMPPConnector.class);

    private static final int DEFAULT_XMPP_PORT = 5222;

    /** Property key for the bots host. */
    public final static String PROPERTIES_HOST = "host";
    /** Property key for the bots port. */
    public final static String PROPERTIES_PORT = "port";
    /** Property key for the bots login. */
    public final static String PROPERTIES_LOGIN = "login";
    /** Property key for the bots password. */
    public final static String PROPERTIES_PASSWORD = "password";
    /** Property key for the bots resource. */
    public final static String PROPERTIES_RESOURCE = "resource";

    /** Time the user has to wait until he is able to post again. */

    /**
     * @return Time to wait before new messages can be posted in milliseconds.
     */
    public static int getTimeToWait() {
        return CommunoteRuntime.getInstance().getConfigurationManager()
                .getApplicationConfigurationProperties()
                .getProperty(ApplicationPropertyXmpp.TIME_TO_WAIT, 30000);
    }

    private XMPPConnection connection;

    private final String sender;

    /**
     * Constructor for using properties to load a connector.
     *
     * @param properties
     *            Settings for this connection as {@link Properties}.
     */
    public XMPPConnector(Properties properties) {
        this(properties.getProperty(PROPERTIES_HOST), properties.getProperty(PROPERTIES_PORT),
                properties.getProperty(PROPERTIES_LOGIN), properties
                        .getProperty(PROPERTIES_PASSWORD), properties
                        .getProperty(PROPERTIES_RESOURCE));
    }

    /**
     * The constructor for the XMPP bot.
     *
     * @param host
     *            The server the bot should login.
     * @param port
     *            The hosts port.
     * @param login
     *            The bots login name.
     * @param password
     *            The bots login password.
     * @param resource
     *            The bots resource (i.e. Work or Home). Can be null.
     * @throws IllegalArgumentException
     *             Throws an {@link IllegalArgumentException} if some of the parameters are in an
     *             invalid format.
     */
    public XMPPConnector(String host, String port, String login, String password, String resource)
            throws IllegalArgumentException {
        checkParameters(host, port, login, password, resource);
        int numericalPort = port == null ? DEFAULT_XMPP_PORT : Integer.parseInt(port);
        this.sender = login + "@" + host + "/" + resource;
        SmackConfiguration.DEBUG_ENABLED = Boolean.parseBoolean(ApplicationPropertyXmpp.DEBUG
                .getValue());
        ProviderManager.addExtensionProvider(AliasPacketExtension.ALIAS_ELEMENT_NAME,
                AliasPacketExtension.ALIAS_NAMESPACE, AliasPacketExtension.class);
        ConnectionConfiguration config = new ConnectionConfiguration(host, numericalPort);
        connection = new XMPPTCPConnection(config);
        connect(login, password, resource);
        sendPriorityPresence();
        if (connection.isConnected()) {
            LOG.info(ResourceBundleManager.instance().getText("xmpp.connection.started",
                    Locale.ENGLISH));
            connection.addConnectionListener(new XMPPConnectionStatusLogger());
        } else {
            LOG.info("The XMPP connection wasn't established.");
        }
    }

    /**
     * This method checks if the parameters are syntactically correct.
     *
     * @param host
     *            the host.
     * @param port
     *            the port.
     * @param login
     *            the login.
     * @param password
     *            the password.
     * @param resource
     *            resource.
     */
    private void checkParameters(String host, String port, String login, String password,
            String resource) {

        if (host == null) {
            throw new IllegalArgumentException("XMPP Connector: Arguments may not be null: host");
        }
        if (login == null) {
            throw new IllegalArgumentException("XMPP Connector: Arguments may not be null: login");
        }
        if (password == null) {
            throw new IllegalArgumentException(
                    "XMPP Connector: Arguments may not be null: password");
        }
        if (resource == null) {
            throw new IllegalArgumentException(
                    "XMPP Connector: Arguments may not be null: resource");
        }

        if (port != null) {
            try {
                int numericalPort = Integer.parseInt(port);
                if (numericalPort < 0 || numericalPort > 65535) {
                    throw new IllegalArgumentException(
                            "XMPP Connector: Port has to be within 0 and 65535 (actual: " + port
                                    + ").");
                }
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(
                        "XMPP Connector: Port has to be a number between 0 and 65535 (actual: "
                                + port + ").");
            }
        }

    }

    /**
     * This method tries to establish the connection to the server. If the
     * {@link ApplicationPropertyXmpp#IGNORE_INCOMING_MESSAGES} is not set to true, this connector
     * won't listen to incoming messages.
     *
     * @param login
     *            The login.
     * @param password
     *            The Password.
     * @param resource
     *            The resource.
     */
    private void connect(String login, String password, String resource) {
        try {
            connection.connect();
            ApplicationConfigurationProperties props = CommunoteRuntime.getInstance()
                    .getConfigurationManager().getApplicationConfigurationProperties();
            boolean ignoreIncomingMessages = new Boolean(
                    props.getProperty(ApplicationPropertyXmpp.IGNORE_INCOMING_MESSAGES));
            if (!ignoreIncomingMessages) {
                connection.addPacketListener(new XMPPMessagePacketListener(connection,
                        ServiceLocator.instance().getService(NoteService.class)),
                        new XMPPMessagePacketFilter());
            }
            boolean handleSubscriptionRequests = new Boolean(
                    props.getProperty(ApplicationPropertyXmpp.HANDLE_SUBSCRIPTION_REQUESTS));
            if (handleSubscriptionRequests) {
                connection.addPacketListener(new XMPPPresencePacketListener(connection),
                        new PacketTypeFilter(Presence.class));
            }
            connection.login(login, password, resource);
            connection.getRoster().setSubscriptionMode(SubscriptionMode.manual);
        } catch (XMPPException | IOException | SmackException e) {
            LOG.info("There were problems with the Jabber/XMPP connection:" + e.getMessage());
        } catch (RuntimeException e) {
            LOG.info("There were problems with the Jabber/XMPP connection:" + e.getMessage());
        }
    }

    /**
     * This method does nothing. {@inheritDoc}
     */
    @Override
    public void disableUser(String username) {
        // Do nothing.
    }

    /**
     * This method sends a friend request to the user if he isn't already in the bots {@link Roster}
     * . {@inheritDoc}
     */
    @Override
    public void enableUser(String username) {
        if (!connection.isConnected()) {
            return;
        }
        Roster roster = connection.getRoster();
        if (roster != null && roster.getEntry(username) != null) {
            return;
        }
        try {
            String clientId = ClientAndChannelContextHolder.getClient().getClientId();
            Presence subscribe = new Presence(Presence.Type.subscribe);
            subscribe.setFrom(sender);
            subscribe.setTo(username + "." + clientId + XMPPPatternUtils.getUserSuffix());
            connection.sendPacket(subscribe);
            Presence subscribed = new Presence(Presence.Type.subscribed);
            subscribed.setFrom(sender);
            subscribed.setTo(username + "." + clientId + XMPPPatternUtils.getUserSuffix());
            connection.sendPacket(subscribed);
        } catch (NotConnectedException e) {
            LOG.debug("Could not send friendship request because XMPP connector is disconnected");
        }
    }

    /**
     * {@inheritDoc}
     *
     * @throws BlogAccessException
     * @throws BlogNotFoundException
     */
    @Override
    public String getBlogMessagerId(long blogId) throws BlogNotFoundException, BlogAccessException {
        return ServiceLocator.instance().getService(BlogManagement.class)
                .getBlogById(blogId, false).getNameIdentifier();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getClientId(String client) {
        String normalisedUsername = client.split("/")[0].replace(XMPPPatternUtils.getUserSuffix(),
                "");
        int idx = normalisedUsername.lastIndexOf('.');
        if (idx == -1) {
            return normalisedUsername;
        }
        return normalisedUsername.substring(idx + 1);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getClientMessagerId(String clientId) {
        return clientId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDescription() {
        return "This connector provides XMPP/Jabber intergration.";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getId() {
        return MessagerConnectorType.XMPP.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return "Jabber Connector";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getUserAlias(String username) {
        String normalisedUsername = username.split("/")[0].replace(
                XMPPPatternUtils.getUserSuffix(), "");
        int idx = normalisedUsername.lastIndexOf('.');
        if (idx == -1) {
            return normalisedUsername;
        }
        return normalisedUsername.substring(0, idx);
    }

    /**
     * This method returns the clients alias.
     *
     * @param userId
     *            The user id.
     * @return The specific user id for this messager.
     */
    @Override
    public String getUserMessagerId(long userId) {
        UserManagement management = ServiceLocator.instance().getService(UserManagement.class);
        return management.findUserByUserId(userId).getAlias();
    }

    /**
     * {@inheritDoc}
     *
     * @param username
     *            Has to be in the messagers format.
     */
    @Override
    public boolean isAvailable(String username) {
        if (connection.isConnected()) {
            Roster roster = connection.getRoster();
            if (roster != null) {
                Presence presence = roster.getPresence(username + XMPPPatternUtils.getUserSuffix());
                return presence.isAvailable();
            } else {
                LOG.debug("XMPP connection did not return a roster for communote bot");
            }
        } else {
            LOG.debug("Cannot check availability of user because XMPP connector is disconnected");
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isConnected() {
        return connection.isConnected();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sendMessage(Message message) throws MessagerException {
        if (!connection.isConnected()) {
            LOG.debug("Ignoring sendMessage request because XMPP connector is disconnected");
            return;
        }
        String to = message.getSender() + ApplicationPropertyXmpp.BLOG_SUFFIX.getValue();
        try {
            for (String receiver : message.getReceivers()) {
                // always create a new packet otherwise the messages are not delivered correctly
                org.jivesoftware.smack.packet.Message xMPPMessage = new org.jivesoftware.smack.packet.Message();
                xMPPMessage.setBody(message.getMessage());
                xMPPMessage.setFrom(sender);
                xMPPMessage.setTo(to);
                AliasPacketExtension extension = new AliasPacketExtension();
                xMPPMessage.addExtension(extension);
                extension.setValue(receiver + XMPPPatternUtils.getUserSuffix());
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Sending xmppMessage. From=" + sender + " To=" + to + " Extension="
                            + extension.getValue());
                }
                connection.sendPacket(xMPPMessage);
            }
        } catch (NotConnectedException e) {
            LOG.info("Could not send message to all receivers because XMPP connector is disconnected");
        }
    }

    /**
     * This method sends a {@link Presence} packet with the configured priority.
     */
    private void sendPriorityPresence() {
        if (!connection.isConnected()) {
            return;
        }
        int priority = CommunoteRuntime.getInstance().getConfigurationManager()
                .getApplicationConfigurationProperties()
                .getProperty(ApplicationPropertyXmpp.PRIORITY, 100);
        Presence presence = new Presence(Presence.Type.available);
        presence.setPriority(priority);
        try {
            connection.sendPacket(presence);
        } catch (NotConnectedException e) {
            LOG.info("Could not send presence packet because XMPP connector is disconnected");
        }
    }

    /**
     * This method sets the XMPP Connection of this connector.
     *
     * @param xMPPConnection
     *            The connection to use.
     */
    public void setXMPPConnection(XMPPConnection xMPPConnection) {
        this.connection = xMPPConnection;
    }

    /**
     * Does nothing.
     */
    @Override
    public void start() {
        // Do nothing.
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stop() {
        try {
            connection.disconnect();
        } catch (NotConnectedException e) {
            // ignore
        }
    }
}
