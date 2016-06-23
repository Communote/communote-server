package com.communote.server.core.messaging;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.velocity.app.VelocityEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.communote.common.converter.IdentityConverter;
import com.communote.common.util.HTMLHelper;
import com.communote.common.util.UrlHelper;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.common.ClientAndChannelContextHolder;
import com.communote.server.api.core.config.type.ApplicationPropertyXmpp;
import com.communote.server.api.core.config.type.ClientProperty;
import com.communote.server.api.core.property.PropertyManagement;
import com.communote.server.core.blog.NoteManagement;
import com.communote.server.core.blog.export.PermalinkGenerationManagement;
import com.communote.server.core.mail.MailManagement;
import com.communote.server.core.mail.MailMessageHelper;
import com.communote.server.core.mail.messages.MailModelPlaceholderConstants;
import com.communote.server.core.mail.messages.user.NotifyAboutNoteMailMessage;
import com.communote.server.core.messaging.NotificationDefinition.NotificationTypes;
import com.communote.server.core.messaging.connector.MessagerConnector;
import com.communote.server.core.messaging.connectors.xmpp.XMPPConnector;
import com.communote.server.core.messaging.vo.Message;
import com.communote.server.core.retrieval.helper.AttachmentHelper;
import com.communote.server.core.security.SecurityHelper;
import com.communote.server.core.user.UserManagement;
import com.communote.server.core.user.UserManagementHelper;
import com.communote.server.model.attachment.Attachment;
import com.communote.server.model.messaging.MessagerConnectorConfig;
import com.communote.server.model.messaging.MessagerConnectorType;
import com.communote.server.model.note.Note;
import com.communote.server.model.tag.Tag;
import com.communote.server.model.user.NotificationConfig;
import com.communote.server.model.user.User;
import com.communote.server.model.user.UserRole;
import com.communote.server.persistence.common.messages.ResourceBundleManager;
import com.communote.server.persistence.user.client.ClientHelper;

/**
 * @see com.communote.server.core.messaging.NotificationManagement
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
@Service("notificationManagement")
public class NotificationManagementImpl extends NotificationManagementBase {

    /** Logger. */
    private final static Logger LOGGER = LoggerFactory.getLogger(NotificationManagementImpl.class);

    /** Default priority for xmpp. */
    public static final int XMPP_PRIORITY = 2;

    @Autowired
    private VelocityEngine velocityEngine;

    @Autowired
    private MailManagement mailManagement;

    @Autowired
    private PermalinkGenerationManagement permalinkGenerationManagement;
    @Autowired
    private PropertyManagement propertyManagement;

    @Autowired
    private UserManagement userManagement;

    @Autowired
    private NoteManagement noteManagement;

    private final Map<String, MessagerConnector> connectors = new HashMap<String, MessagerConnector>();

    /**
     * Get the connector for the id, throws an exception if it goes wrong
     *
     * @param connectorId
     *            the connector id
     * @return the connector for the given id
     */
    private MessagerConnector getConnector(String connectorId) {
        MessagerConnector connector = connectors.get(connectorId);
        if (connector == null) {
            throw new NotificationManagementException("Connector does not exist! connectorId="
                    + connectorId);
        }
        return connector;
    }

    /**
     * This method returns a notification {@link Message} in the specific XMPP format. User which
     * aren't online are ignored.
     *
     * @param connector
     *            The current connector. Needed to check presence status.
     * @param note
     *            The original posting.
     * @param userToNotify
     *            A user to be notified about the post
     * @param locale
     *            the locale to use when creating the message
     * @param usersNotAbleToSendTo
     *            This is an in out list, containing the users which wanted to be informed via XMPP,
     *            but aren't available.
     * @param definition
     *            The notification
     * @param model
     *            Additional elements used for the velocity context.
     * @return A {@link Message} with XMPP specific settings, like correct receivers and sender.
     * @throws IOException
     *             Eception.
     */
    private Message getNotificationMessageForXMPPClients(MessagerConnector connector, Note note,
            User userToNotify, Locale locale, Collection<User> usersNotAbleToSendTo,
            NotificationDefinition definition, Map<String, Object> model) throws IOException {
        String clientId = ClientAndChannelContextHolder.getClient().getClientId();
        String topicId = note.getBlog().getNameIdentifier();
        Message message = new Message();
        Collection<String> receiversToNotify = new ArrayList<String>();

        message.setMessage(setNotificationMessageContentForXMPPClients(note, userToNotify, locale,
                definition, model));

        message.setSender(topicId + "." + clientId);

        if (userWantsXMPPMessage(userToNotify)) {
            String receiver = userToNotify.getAlias() + "." + clientId;
            if (connector.isAvailable(receiver)) {
                receiversToNotify.add(receiver);
            } else if (userWantsXMPPFallback(userToNotify)) {
                usersNotAbleToSendTo.add(userToNotify);
            }
        }

        message.setReceivers(receiversToNotify.toArray(new String[receiversToNotify.size()]));
        return message;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(propagation = Propagation.SUPPORTS)
    public String getXMPPId() {
        String jabberId = SecurityHelper.getCurrentUserAlias()
                + "."
                + ClientHelper.getCurrentClientId()
                + CommunoteRuntime.getInstance().getConfigurationManager()
                        .getApplicationConfigurationProperties()
                .getProperty(ApplicationPropertyXmpp.USER_SUFFIX);

        return jabberId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void handleDisableUser(long userId, String connectorId) {
        MessagerConnector connector = getConnector(connectorId);
        String username = connector.getUserMessagerId(userId);
        connector.disableUser(username);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void handleEnableUser(long userId, String connectorId) {
        MessagerConnector connector = getConnector(connectorId);
        String username = connector.getUserMessagerId(userId);
        connector.enableUser(username);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String handleGetClientId(String client, String connectorId) {
        MessagerConnector connector = getConnector(connectorId);
        return connector.getClientId(client);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String handleGetUserAlias(String username, String connectorId) {
        MessagerConnector connector = getConnector(connectorId);
        return connector.getUserAlias(username);
    }

    /**
     * This method sends out the mails.
     *
     * @param note
     *            The posting to send.
     * @param usersToNotify
     *            contains all users to be notified as a collection from User
     * @param fallbackUsers
     *            The users which falled back and want to be notified via mail.
     * @param definition
     *            The definition to use.
     * @param model
     *            Additional elements used for the velocity context.
     */
    private void handleMailNotification(Note note, Collection<User> usersToNotify,
            Collection<User> fallbackUsers, NotificationDefinition definition,
            Map<String, Object> model) {
        Map<String, String> definitionKeys = new HashMap<String, String>();
        definitionKeys.put("content", definition.getMessageKeyForMessage(NotificationTypes.PLAIN));
        definitionKeys.put("subject", definition.getMessageKeyForSubject(NotificationTypes.PLAIN));
        // Because of salutation and localization we have to send a separate mail to each user.
        for (User user : usersToNotify) {
            if (user.getEmail().endsWith(MailMessageHelper.ANONYMOUS_EMAIL_ADDRESS_SUFFIX)) {
                LOGGER.warn("Skipping sending mail to user with anonymous email address: {}",
                        user.getAlias());
                continue;
            }
            if ((userWantsMailMessage(user) || fallbackUsers.contains(user))) {
                Locale locale = user.getLanguageLocale();
                Collection<User> users = new HashSet<User>();
                users.add(user);
                NotifyAboutNoteMailMessage message = new NotifyAboutNoteMailMessage(users,
                        note.getUser(), locale, note, note.getBlog(), definitionKeys, model);
                mailManagement.sendMail(message);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void handleRemoveMessagerConnector(String connectorId) {
        MessagerConnector oldConnector = connectors.remove(connectorId);
        if (oldConnector != null) {
            oldConnector.stop();
        }
    }

    /**
     * Handles the notification via XMPP.
     *
     * @param connector
     *            the XMPP connector
     * @param note
     *            the original post
     * @param usersToNotify
     *            contains all users to be notified as a collection from User
     * @param fallbackUsers
     *            is used to store the users who could not be informed via XMPP but defined a
     *            notification fallback for that case
     * @param definition
     *            The notification
     * @param model
     *            Additional elements used for the velocity context.
     * @throws Exception
     *             if sending the message via the connector failed
     */
    private void handleXMPPNotification(MessagerConnector connector, Note note,
            Collection<User> usersToNotify, Collection<User> fallbackUsers,
            NotificationDefinition definition, Map<String, Object> model) throws Exception {
        LOGGER.debug("Try sending message with id {} via XMPP to {} users", note.getId(),
                usersToNotify.size());
        for (User user : usersToNotify) {
            connector.sendMessage(getNotificationMessageForXMPPClients(connector, note, user,
                    user.getLanguageLocale(), fallbackUsers, definition, model));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void registerMessagerConnector(MessagerConnector connector) {
        MessagerConnector oldConnector = connectors.put(connector.getId(), connector);
        if (oldConnector != null) {
            oldConnector.stop();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sendMessage(Long noteId, Long userId,
            NotificationDefinition notificationDefinition, Map<String, Object> model) {
        ArrayList<User> usersToNotify = new ArrayList<User>();
        usersToNotify.add(userManagement.getUserById(userId, new IdentityConverter<User>()));
        sendMessage(noteManagement.getNote(noteId, new IdentityConverter<Note>()), usersToNotify,
                notificationDefinition, model);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sendMessage(Note note, Collection<User> usersToNotify,
            NotificationDefinition definition) {
        sendMessage(note, usersToNotify, definition, new HashMap<String, Object>());
    }

    /**
     * Send notifications to users to inform about created or edited notes.
     *
     * @param note
     *            Note, which should be send. the note to inform about.
     * @param usersToNotify
     *            List of users to notify.
     * @param definition
     *            The current definition of the notification.
     * @param model
     *            Additional elements used for the velocity context.
     */
    private void sendMessage(Note note, Collection<User> usersToNotify,
            NotificationDefinition definition, Map<String, Object> model) {
        Collection<User> fallbackUsers = new HashSet<User>();
        for (String connectorId : connectors.keySet()) {
            MessagerConnector connector = connectors.get(connectorId);
            if (connector instanceof XMPPConnector) {
                try {
                    handleXMPPNotification(connector, note, usersToNotify, fallbackUsers,
                            definition, model);
                } catch (Exception e) {
                    LOGGER.error("Error in messager integration (" + connector.getName() + "): ", e);
                }
            }
        }
        handleMailNotification(note, usersToNotify, fallbackUsers, definition, model);
    }

    /**
     * This method sets the message content for XMPP clients.
     *
     * @param note
     *            The original note
     * @param userToNotify
     *            A user to be notified about the post
     * @param locale
     *            the locale for the message
     * @param definition
     *            The notification
     * @param model
     *            Additional elements used for the velocity context.
     * @return The message as String.
     */
    private String setNotificationMessageContentForXMPPClients(Note note, User userToNotify,
            Locale locale, NotificationDefinition definition, Map<String, Object> model) {
        String htmlContent = (note.getContent()).getContent();
        htmlContent = UrlHelper.convertAnchorsToString(htmlContent);
        User user = note.getUser();

        DateFormat format = DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.SHORT,
                locale);
        format.setTimeZone(UserManagementHelper.getEffectiveUserTimeZone(userToNotify.getId()));
        if (model == null) {
            model = new HashMap<String, Object>();
        }
        model.put(MailModelPlaceholderConstants.IS_MODIFIED,
                note.getCreationDate().before(note.getLastModificationDate()));
        model.put(MailModelPlaceholderConstants.USER, note.getUser());
        boolean renderAttachmentLinks = ClientProperty.NOTIFICATION_RENDER_ATTACHMENTLINKS
                .getValue(ClientProperty.DEFAULT_NOTIFICATION_RENDER_ATTACHMENTLINKS);

        if (renderAttachmentLinks && !note.getAttachments().isEmpty()) {
            String[] attachments = new String[note.getAttachments().size()];
            int i = 0;
            for (Attachment attachment : note.getAttachments()) {
                attachments[i] = "[" + (i + 1) + "] " + attachment.getName() + ": "
                        + AttachmentHelper.determineAbsoluteAttachmentUrl(attachment);
                i++;
            }
            model.put(MailModelPlaceholderConstants.ATTACHMENTS, attachments);
        }
        model.put(MailModelPlaceholderConstants.UTI_FORMATED_DATE_CREATE,
                UserManagementHelper.getDateFormat(userToNotify.getId(), user.getLanguageLocale())
                .format(note.getCreationDate()));
        model.put(MailModelPlaceholderConstants.UTI_FORMATED_DATE_MODIFY,
                UserManagementHelper.getDateFormat(userToNotify.getId(), user.getLanguageLocale())
                .format(note.getLastModificationDate()));

        Set<Tag> tags = note.getTags();
        List<String> tagList = new ArrayList<String>();
        if (tags != null) {
            for (Tag tag : tags) {
                tagList.add(tag.getName());
            }
        }
        model.put(MailModelPlaceholderConstants.UTI_TAGS, StringUtils.join(tagList, ", "));
        model.put(MailModelPlaceholderConstants.BLOG_TITLE, note.getBlog().getTitle());
        boolean renderLink = ClientProperty.NOTIFICATION_RENDER_PERMALINKS
                .getValue(ClientProperty.DEFAULT_NOTIFICATION_RENDER_PERMALINKS);
        model.put(MailModelPlaceholderConstants.RENDER_PERMA_LINK, renderLink);
        model.put(
                MailModelPlaceholderConstants.PERMA_LINK_NOTE,
                ServiceLocator.findService(PermalinkGenerationManagement.class).getNoteLink(
                        note.getBlog().getNameIdentifier(), note.getId()));
        model.put(MailModelPlaceholderConstants.UTP_CONTENT,
                HTMLHelper.htmlToPlaintextExt(htmlContent, true));
        model.put(MailModelPlaceholderConstants.IS_DIRECT, note.isDirect());
        return ResourceBundleManager.instance().evaluateText(
                definition.getMessageKeyForMessage(NotificationTypes.PLAIN), locale, model);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(propagation = Propagation.SUPPORTS)
    public void start() {
        // Do nothing
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(propagation = Propagation.SUPPORTS)
    public void stop() {
        connectors.clear();
    }

    /**
     * This method checks if the users wants to be informed via mail.
     *
     * @param user
     *            The user to check again.
     * @return True if the users wants to be informed via mail.
     */
    private boolean userWantsMailMessage(User user) {
        for (UserRole role : user.getRoles()) {
            if (UserRole.ROLE_SYSTEM_USER.equals(role)) {
                LOGGER.debug("System users can't be notified via email: {}", user.getAlias());
                return false;
            }
        }
        NotificationConfig config = user.getProfile().getNotificationConfig();
        boolean wantsMail = false;
        if (config == null) {
            LOGGER.error("NotificationConfig is null for user=" + user.attributesToString());
            wantsMail = true;
        } else {
            for (MessagerConnectorConfig messagerConfig : config.getConfigs()) {
                if (messagerConfig.getType().equals(MessagerConnectorType.MAIL)) {
                    wantsMail = true;
                    break;
                }
            }
        }
        return wantsMail;
    }

    /**
     * @param user
     *            The user.
     * @return True, if the users wants a fallback if XMPP is not available.
     * @throws IOException
     *             Exception.
     */
    private boolean userWantsXMPPFallback(User user) throws IOException {
        NotificationConfig config = user.getProfile().getNotificationConfig();
        if (config != null) {
            for (MessagerConnectorConfig messagerConfig : config.getConfigs()) {
                if (messagerConfig.getType().equals(MessagerConnectorType.XMPP)) {
                    if (StringUtils.isEmpty(messagerConfig.getProperties())) {
                        break;
                    }
                    Properties properties = new Properties();
                    properties.load(new ByteArrayInputStream(messagerConfig.getProperties()
                            .getBytes()));
                    return Boolean.parseBoolean(properties
                            .getProperty(NotificationManagementConstants.FALLBACK_ON_FAIL_LITERAL));
                }
            }
        }
        return false;
    }

    /**
     * This method checks if the users wants to be informed via XMPP.
     *
     * @param user
     *            The user to check again.
     * @return True if the users wants to be informed via XMPP. This method also returns false if no
     *         XMPP connector is available.
     */
    private boolean userWantsXMPPMessage(User user) {
        NotificationConfig config = user.getProfile().getNotificationConfig();
        if (config != null) {
            for (MessagerConnectorConfig messagerConfig : config.getConfigs()) {
                if (messagerConfig.getType().equals(MessagerConnectorType.XMPP)) {
                    return true;
                }
            }
        }
        return false;
    }
}
