package com.communote.server.core.messaging.connectors.xmpp;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.PacketExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.communote.server.api.core.client.ClientDelegate;
import com.communote.server.api.core.client.ClientNotFoundException;
import com.communote.server.api.core.client.ClientTO;
import com.communote.server.core.delegate.client.DelegateCallbackException;
import com.communote.server.core.vo.blog.NoteModificationResult;
import com.communote.server.core.vo.blog.NoteModificationStatus;
import com.communote.server.model.user.User;
import com.communote.server.model.user.UserStatus;
import com.communote.server.persistence.blog.CreateBlogPostHelper;
import com.communote.server.persistence.common.messages.ResourceBundleManager;
import com.communote.server.service.NoteService;

/**
 * The {@link XMPPMessagePacketListener} listens for incoming XMPP message packets and handles them.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class XMPPMessagePacketListener implements PacketListener {

    private final static Logger LOG = LoggerFactory.getLogger(XMPPMessagePacketListener.class);
    private final Map<String, Long> timeMap;
    private final XMPPConnection connection;

    /**
     * Constructor for the {@link XMPPMessagePacketListener}.
     *
     * @param connection
     *            Specific connection this Listener can use to send messages.
     * @param management
     *            Management to retrieve user information.
     */
    public XMPPMessagePacketListener(XMPPConnection connection, NoteService management) {
        this.connection = connection;
        timeMap = new HashMap<String, Long>();
    }

    /**
     * Tests whether a user is allowed to write a post, i.e. must not be null and must have status
     * ACTIVE. In case the user is not allowed feedback messages will be sent.
     *
     * @param user
     *            the user to test
     * @param message
     *            the original message
     * @param aliasExtension
     *            the alias for sending the feedback to
     * @return true if conditions are met, false otherwise
     * @throws NotConnectedException
     */
    private boolean canUserCreateUTP(User user, Message message, AliasPacketExtension aliasExtension)
            throws NotConnectedException {
        if (user != null) {
            if (UserStatus.ACTIVE.equals(user.getStatus())) {
                return true;
            } else if (UserStatus.TEMPORARILY_DISABLED.equals(user.getStatus())) {
                sendMessage(
                        message.getFrom(),
                        message.getTo(),
                        aliasExtension.getValue(),
                        ResourceBundleManager.instance().getText("xmpp.message.user.disabled",
                                user.getLanguageLocale()));
            } else {
                sendMessage(
                        message.getFrom(),
                        message.getTo(),
                        aliasExtension.getValue(),
                        ResourceBundleManager.instance().getText("xmpp.message.user.unresolvable",
                                user.getLanguageLocale()));
            }
        } else {
            LOG.error("User for JID " + message.getFrom() + " not found.");
            sendMessage(
                    message.getFrom(),
                    message.getTo(),
                    aliasExtension.getValue(),
                    ResourceBundleManager.instance().getText("xmpp.message.user.unresolvable",
                            Locale.ENGLISH));
        }
        return false;
    }

    /**
     * This method checks if the user is out of the flooding interval. If the user is in the
     * interval he will get a message.
     *
     * @param message
     *            The message to extract the user from.
     * @param alias
     *            Alias to send an information message if user is within his time border.
     * @return true if the user is able to send a message.
     * @throws NotConnectedException
     */
    private boolean checkUserTimeBorder(Message message, AliasPacketExtension alias)
            throws NotConnectedException {
        String key = getUserTimeMapKey(message);
        if (timeMap.containsKey(key)) {
            if (!((System.currentTimeMillis() - timeMap.get(key)) > XMPPConnector.getTimeToWait())) {
                sendMessage(
                        message.getFrom(),
                        message.getTo(),
                        alias.getValue(),
                        ResourceBundleManager.instance().getText(
                                "xmpp.message.wait",
                                XMPPPatternUtils.extractKenmeiUser(message.getFrom())
                                        .getLanguageLocale(),
                                Math.round(XMPPConnector.getTimeToWait() / 1000)));
                return false;
            }
        }
        return true;
    }

    /**
     * Evaluates the result of the UTP creation and sends necessary feedback messages to the user.
     *
     * @param result
     *            the UTP creation result
     * @param message
     *            the original message
     * @param aliasExtension
     *            the alias (aka blog) to retrieve the blog
     * @param user
     *            the user who sent the message
     * @throws NotConnectedException
     */
    private void evalPostCreationResult(NoteModificationResult result, Message message,
            AliasPacketExtension aliasExtension, User user) throws NotConnectedException {
        if (result.getStatus().equals(NoteModificationStatus.SUCCESS)) {
            String warningMsg = CreateBlogPostHelper.getFeedbackMessageAfterModification(result,
                    user.getLanguageLocale());
            if (warningMsg != null) {
                sendMessage(message.getFrom(), message.getTo(), aliasExtension.getValue(),
                        warningMsg);
            } else {
                sendMessage(
                        message.getFrom(),
                        message.getTo(),
                        aliasExtension.getValue(),
                        ResourceBundleManager.instance().getText("xmpp.message.success",
                                user.getLanguageLocale()));
            }
        } else {
            // error case
            String errorMsg = CreateBlogPostHelper.getFeedbackMessageAfterModification(result,
                    user.getLanguageLocale());
            sendMessage(message.getFrom(), message.getTo(), aliasExtension.getValue(), errorMsg);

        }
    }

    /**
     * Extract the key for the timeMap map from a message.
     *
     * @param message
     *            The message to extract the user.
     * @return The specific user key to access the map.
     */
    private String getUserTimeMapKey(Message message) {
        return XMPPPatternUtils.extractKenmeiUser(message.getFrom())
                + XMPPPatternUtils.extractClientIdFromUser(message.getFrom());
    }

    /**
     * This method handles incoming packets.
     *
     * @param packet
     *            The incoming packet.
     * @throws NotConnectedException
     */
    @Override
    public void processPacket(Packet packet) throws NotConnectedException {
        if (!(packet instanceof Message)) {
            return;
        }
        Message message = (Message) packet;
        if (message.getBody() == null) {
            return;
        }
        PacketExtension extension = packet.getExtension(AliasPacketExtension.ALIAS_ELEMENT_NAME,
                AliasPacketExtension.ALIAS_NAMESPACE);
        if (extension == null) {
            sendMessage(
                    packet.getFrom(),
                    packet.getTo(),
                    ResourceBundleManager.instance().getText(
                            "xmpp.message.error.bot",
                            XMPPPatternUtils.extractKenmeiUser(packet.getFrom())
                            .getLanguageLocale(),
                            XMPPPatternUtils.extractClientIdFromUser(packet.getFrom()),
                            XMPPPatternUtils.getBlogSuffix()));
        } else {
            sendMessageToCommunote(message, (AliasPacketExtension) extension);
        }
    }

    /**
     * This method sends a message via XMPP.
     *
     * @param to
     *            The receiver.
     * @param from
     *            The sender.
     * @param message
     *            The message.
     * @throws NotConnectedException
     */
    private void sendMessage(String to, String from, String message) throws NotConnectedException {
        Message messagePacket = new Message();
        messagePacket.setTo(to);
        messagePacket.setFrom(from);
        messagePacket.setBody(message);
        connection.sendPacket(messagePacket);
    }

    /**
     * Method to send a message over XMPP to a user via a specific alias.
     *
     * @param to
     *            The receiver.
     * @param from
     *            The sender.
     * @param alias
     *            The alias as pseudo sender.
     * @param message
     *            The message.
     * @throws NotConnectedException
     */
    private void sendMessage(String to, String from, String alias, String message)
            throws NotConnectedException {
        Message messagePacket = new Message();
        messagePacket.setTo(alias);
        messagePacket.setFrom(from);
        messagePacket.setBody(message);
        AliasPacketExtension extension = new AliasPacketExtension();
        extension.setValue(to);
        messagePacket.addExtension(extension);
        connection.sendPacket(messagePacket);
    }

    /**
     * This method posts messages into Kenmei.
     *
     * @param message
     *            The message.
     * @param aliasExtension
     *            The alias (aka blog) to retrieve the blog.
     * @throws NotConnectedException
     */
    private void sendMessageToCommunote(Message message, AliasPacketExtension aliasExtension)
            throws NotConnectedException {
        if (!checkUserTimeBorder(message, aliasExtension)) {
            return;
        }
        User user = XMPPPatternUtils.extractKenmeiUser(message.getFrom());
        if (canUserCreateUTP(user, message, aliasExtension)) {

            try {
                ClientTO client = XMPPPatternUtils.extractClient(aliasExtension.getValue());
                String extractedClientId = XMPPPatternUtils.extractClientIdFromUser(message
                        .getFrom());
                if (client != null && !client.getClientId().equals(extractedClientId)) {
                    LOG.warn("Received packet where client ID in alias extension ("
                            + client.getClientId() + ") does not match client ID in from ("
                            + extractedClientId + ").");
                    sendMessage(
                            message.getFrom(),
                            message.getTo(),
                            aliasExtension.getValue(),
                            ResourceBundleManager.instance().getText(
                                    "xmpp.message.client.prohibited", user.getLanguageLocale()));
                    return;
                }
                NoteModificationResult result = (NoteModificationResult) new ClientDelegate(client)
                        .execute(new XMPPDatabaseCallback(message, aliasExtension));
                timeMap.put(getUserTimeMapKey(message), System.currentTimeMillis());
                evalPostCreationResult(result, message, aliasExtension, user);
            } catch (DelegateCallbackException e) {
                sendMessage(message.getFrom(), message.getTo(), aliasExtension.getValue(),
                        e.getMessage());
                LOG.warn("User tried to do something prohibited.", e);
            } catch (ClientNotFoundException e) {
                sendMessage(
                        message.getFrom(),
                        message.getTo(),
                        aliasExtension.getValue(),
                        ResourceBundleManager.instance().getText("xmpp.message.wrong.client",
                                user.getLanguageLocale()));
                LOG.warn("User tried to do something prohibited.", e);
            } catch (IllegalArgumentException e) {
                sendMessage(
                        message.getFrom(),
                        message.getTo(),
                        aliasExtension.getValue(),
                        ResourceBundleManager.instance().getText("xmpp.message.error.other",
                                user.getLanguageLocale()));
                LOG.warn("User tried to do something prohibited.", e);
            } catch (Exception e) {
                sendMessage(
                        message.getFrom(),
                        message.getTo(),
                        aliasExtension.getValue(),
                        ResourceBundleManager.instance().getText("xmpp.message.error.unknown",
                                user.getLanguageLocale()));
                LOG.debug("Unknown exception (Maybe the user tried to access another client).", e);
            }
        }
    }
}
