package com.communote.server.core.mail.messages;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.config.ApplicationConfigurationProperties;
import com.communote.server.api.core.config.ClientConfigurationProperties;
import com.communote.server.api.core.config.type.ApplicationPropertyMailing;
import com.communote.server.api.core.config.type.ClientProperty;
import com.communote.server.core.common.i18n.LocalizationManagement;
import com.communote.server.model.user.User;
import com.communote.server.persistence.common.messages.ResourceBundleManager;
import com.communote.server.persistence.user.client.ClientHelper;
import com.communote.server.persistence.user.client.ClientUrlHelper;

/**
 * An abstract base class for all mail messages. Subclasses can define templates for the content and
 * subject of the mail and can provide data to be available when the templates are rendered.
 *
 * @author Communote team - <a href="http://communote.github.io/">http://communote.github.io/</a>
 */
public abstract class MailMessage {

    /** Holds the relative path to the user management section of administration area */
    public final static String ACTIVATION_LINK_PREFIX = "/admin/client/usermanagementview";

    /** The configuration to use for constants. */
    private final ClientConfigurationProperties clientConfiguration = CommunoteRuntime
            .getInstance().getConfigurationManager().getClientConfigurationProperties();

    private final ApplicationConfigurationProperties applicationConfiguration = CommunoteRuntime
            .getInstance().getConfigurationManager().getApplicationConfigurationProperties();

    private final Locale locale;

    private String fromAddress;
    private String fromAddressName;
    
    private final List<User> toRecipients;
    private final List<String> toRecipientEmailAddresses;
    private final Map<String, String> toRecipientPersonalNames;
    private final List<User> ccRecipients;
    private final List<User> bccRecipients;

    // TODO why is this false by default??
    private boolean insertRecipientPersonalName = false;

    private final String messageKey;
    private final String subjectKey;

    private String messageTemplate;
    private String subjectTemplate;

    /**
     * Construct a new message. Content and subject templates will be retrieved from the
     * {@link ResourceBundleManager}.
     *
     * @param messageKey
     *            Key of the message to get from the {@link ResourceBundleManager} and use as
     *            content template. The key will be returned by
     *            {@link MailMessage#getContentTemplateId()}. The key of the message to use as the
     *            subject template is derived from the messageKey by appending <code>.subject</code>
     *            to it.
     * @param locale
     *            the locale to use when getting the templates from the
     *            {@link ResourceBundleManager}
     * @param recipients
     *            A list of recipients of this message. The email addresses will be added to the TO
     *            header.
     */
    public MailMessage(String messageKey, Locale locale, User... recipients) {
        this(messageKey, null, locale, recipients);
    }

    /**
     * Construct a new message. Content and subject templates will be retrieved from the
     * {@link ResourceBundleManager}.
     *
     * @param messageKey
     *            Key of the message to get from the {@link ResourceBundleManager} and use as
     *            content template. The key will be returned by
     *            {@link MailMessage#getContentTemplateId()}. The key of the message to use as the
     *            subject template is derived from the messageKey by appending <code>.subject</code>
     *            to it.
     * @param locale
     *            the locale to use when getting the templates from the
     *            {@link ResourceBundleManager}
     * @param recipients
     *            A list of recipients of this message. The email addresses will be added to the TO
     *            header.
     */
    public MailMessage(String messageKey, Locale locale, Collection<User> recipients) {
        this(messageKey, null, locale, recipients);
    }

    /**
     * Construct a new message. Content and subject templates will be retrieved from the
     * {@link ResourceBundleManager}.
     *
     * @param messageKey
     *            Key of the message to get from the {@link ResourceBundleManager} and use as
     *            content template. The key will be returned by
     *            {@link MailMessage#getContentTemplateId()}. The key of the message to use as the
     *            subject template is derived from the messageKey by appending <code>.subject</code>
     *            to it.
     * @param templatePlaceholderMessageKeys
     *            A mapping of placeholders to message keys, which will be replaced within the
     *            template with the loaded message. Use it in the template @@placeholder@@.
     * @param locale
     *            the locale to use when getting the templates from the
     *            {@link ResourceBundleManager}
     * @param recipients
     *            A list of recipients of this message. The email addresses will be added to the TO
     *            header.
     * @since 3.5
     */
    public MailMessage(String messageKey, Map<String, String> templatePlaceholderMessageKeys,
            Locale locale, User... recipients) {
        this(messageKey, templatePlaceholderMessageKeys, locale,
                recipients == null ? null : Arrays.asList(recipients));
    }

    /**
     * Construct a new message. Content and subject templates will be retrieved from the
     * {@link ResourceBundleManager}.
     *
     * @param messageKey
     *            Key of the message to get from the {@link ResourceBundleManager} and use as
     *            content template. The key will be returned by
     *            {@link MailMessage#getContentTemplateId()}. The key of the message to use as the
     *            subject template is derived from the messageKey by appending <code>.subject</code>
     *            to it.
     * @param templatePlaceholderMessageKeys
     *            A mapping of placeholders to message keys, which will be replaced within the
     *            template with the loaded message. Use it in the template @@placeholder@@.
     * @param locale
     *            the locale to use when getting the templates from the
     *            {@link ResourceBundleManager}
     * @param recipients
     *            A list of recipients of this message. The email addresses will be added to the TO
     *            header.
     * @since 3.5
     */
    public MailMessage(String messageKey, Map<String, String> templatePlaceholderMessageKeys,
            Locale locale, Collection<User> recipients) {
        this.messageKey = messageKey;
        this.subjectKey = messageKey + ".subject";
        if (locale == null) {
            throw new IllegalArgumentException("Locale cannot be null!");
        }
        this.locale = locale;
        this.toRecipients = new ArrayList<>();
        if (recipients != null) {
            this.toRecipients.addAll(recipients);
        }
        this.toRecipientEmailAddresses = new ArrayList<>();
        this.toRecipientPersonalNames = new HashMap<>();
        this.ccRecipients = new ArrayList<>();
        this.bccRecipients = new ArrayList<>();
        ResourceBundleManager resourceBundleManager = ResourceBundleManager.instance();
        this.messageTemplate = resourceBundleManager.getText(messageKey, locale);
        this.subjectTemplate = resourceBundleManager.getText(subjectKey, locale);
        if (templatePlaceholderMessageKeys != null) {
            for (Entry<String, String> placeholderMessageKey : templatePlaceholderMessageKeys
                    .entrySet()) {
                String replacement = resourceBundleManager.getText(
                        placeholderMessageKey.getValue(), locale);
                messageTemplate = messageTemplate.replace("@@" + placeholderMessageKey.getKey()
                        + "@@", replacement);
                subjectTemplate = subjectTemplate.replace("@@" + placeholderMessageKey.getKey()
                        + "@@", replacement);
            }
        }
        if (StringUtils.isBlank(messageTemplate)) {
            throw new RuntimeException(
                    "Resource not found for either locale and/or fallback locale for key:  "
                            + messageKey);
        }
    }

    /**
     * Add a recipient of the email. The email address of the user will be added to the BCC header.
     * 
     * @param user
     *            the user to send the mail to
     * @since 3.5
     */
    public void addBcc(User user) {
        this.bccRecipients.add(user);
    }

    /**
     * Add a recipient of the email. The email address of the user will be added to the CC header.
     * 
     * @param user
     *            the user to send the mail to
     * @since 3.5
     */
    public void addCc(User user) {
        this.ccRecipients.add(user);
    }

    /**
     * Add a recipient of the email. The email address of the user will be added to the TO header.
     * 
     * @param user
     *            the user to send the mail to
     * @since 3.5
     */
    public void addTo(User user) {
        this.toRecipients.add(user);
    }

    /**
     * Add a recipient of the email. The email address will be added to the TO header.
     * 
     * @param emailAddress
     *            the email address of the recipient
     * @since 3.5
     */
    public void addTo(String emailAddress) {
        this.toRecipientEmailAddresses.add(emailAddress);
    }

    /**
     * Add a recipient of the email. The email address will be added to the TO header.
     * 
     * @param emailAddress
     *            the email address of the recipient
     * @param personalName
     *            the personal name to add to the TO header.
     * 
     * @since 3.5
     */
    public void addTo(String emailAddress, String personalName) {
        this.addTo(emailAddress);
        if (personalName != null) {
            this.toRecipientPersonalNames.put(emailAddress, personalName);
        }
    }

    /**
     * @return a possibly empty collection of all users whose email addresses should be added to the
     *         BCC header
     * @since 3.5
     */
    public Collection<User> getBcc() {
        return bccRecipients;
    }

    /**
     * @return a possibly empty collection of all users whose email addresses should be added to the
     *         CC header
     * @since 3.5
     */
    public Collection<User> getCc() {
        return ccRecipients;
    }

    /**
     * @return the template for rendering the content of the mail message
     * @since 3.5
     */
    public String getContentTemplate() {
        return messageTemplate;
    }

    /**
     * @return an ID of the content template
     * @since 3.5
     */
    public String getContentTemplateId() {
        return messageKey;
    }

    /**
     * Get the email address of the sender.<br>
     * If no address has been set the configured address of the Communote installation will be returned.<br>
     *
     * @return the email address to be used in the From header
     */
    public String getFromAddress() {
        if (fromAddress == null) {
            return applicationConfiguration.getAssertProperty(ApplicationPropertyMailing.FROM_ADDRESS);
        }
        return fromAddress;
    }

    /**
     * Get the personal name of the sender. <br>
     * If no name has been set the configured name of the Communote installation will be
     * returned.<br>
     * 
     * @return the sender name to be used in the From header
     */
    public String getFromAddressName() {
        if (fromAddressName == null) {
            return applicationConfiguration
                    .getAssertProperty(ApplicationPropertyMailing.FROM_ADDRESS_NAME);
        }
        return fromAddressName;
    }

    /**
     * Model of key-value pairs with common data useful for all mail messages. The model will be
     * passed to {@link #prepareModel(Map)} where it can be extended with specific data before
     * handing it over to the template rendering engine.
     *
     * @return the model
     */
    public Map<String, Object> getGlobalModel() {
        Map<String, Object> globalModel = new HashMap<String, Object>();
        globalModel.put(MailModelPlaceholderConstants.Client.HOMEPAGE,
                ClientUrlHelper.renderConfiguredAbsoluteUrl(null, true));

        globalModel.put(MailModelPlaceholderConstants.Client.CLIENT,
                ClientHelper.getCurrentClient());
        String clientSignature = ServiceLocator.instance().getService(LocalizationManagement.class)
                .getCustomMessage("custom.message.client.email.signature", locale);
        if (StringUtils.isEmpty(clientSignature)) {
            clientSignature = " ";
        }
        globalModel.put(MailModelPlaceholderConstants.Client.SIGNATURE, clientSignature);
        String replyTo = getReplyToAddress();
        String replyToName;
        if (StringUtils.isEmpty(replyTo)) {
            replyTo = getFromAddress();
            replyToName = getFromAddressName();
        } else {
            replyToName = getReplyToAddressName();
        }
        globalModel.put(MailModelPlaceholderConstants.Client.REPLY_TO_ADDRESS, replyTo);
        globalModel.put(MailModelPlaceholderConstants.Client.REPLY_TO_NAME, replyToName);
        return globalModel;
    }

    /**
     * @return the locale
     */
    public Locale getLocale() {
        return locale;
    }

    /**
     * Returns an identifier of the message which can be used in the Message-ID header. The
     * identifier must be unique and must fulfill the dot-atom-text pattern of rfc2822. The
     * identifier will extended to become a syntactically correct Message-ID header field value.
     *
     * @return the message identifier or null if no specific Message-ID header should be set
     */
    public String getMessageIdentifier() {
        return null;
    }

    /**
     * Returns a string holding the email address to be used in the "reply-to" header. The default
     * implementation returns an address defined for the current client. If null is returned, the
     * "reply-to" header should not be set.
     *
     * @return the email address to add to the reply-to header, can be null
     */
    public String getReplyToAddress() {
        return clientConfiguration.getProperty(ClientProperty.REPLY_TO_ADDRESS);
    }

    /**
     * Returns the personal name to be used in the "reply-to" header field.
     *
     * @return the personal name
     */
    public String getReplyToAddressName() {
        return clientConfiguration.getProperty(ClientProperty.REPLY_TO_ADDRESS_NAME);
    }

    /**
     * @return the template for rendering the subject of the mail message
     * @since 3.5
     */
    public String getSubjectTemplate() {
        return subjectTemplate;
    }

    /**
     * @return an ID of the subject template
     * @since 3.5
     */
    public String getSubjectTemplateId() {
        return subjectKey;
    }

    /**
     * @return a possibly empty collection of all users whose email addresses should be added to the
     *         TO header
     * @since 3.5
     */
    public Collection<User> getTo() {
        return toRecipients;
    }

    /**
     * @return a possibly empty collection of email addresses which should be added to the TO
     *         header. This doesn't include the email addresses of the users returned by
     *         {@link #getTo()}, instead only the addresses added with {@link #addTo(String)} or
     *         {@link #addTo(String, String)} are returned.
     * @since 3.5
     */
    public Collection<String> getToAddresses() {
        return toRecipientEmailAddresses;
    }

    /**
     * Return a personal name which was added with an email address by invoking
     * {@link #addTo(String, String)}.
     * 
     * @param emailAddress
     *            the email address for which the personal name should be returned
     * @return the personal name added with the email address or null if no personal name was added
     *         or the email address wasn't added
     * @since 3.5
     */
    public String getToAddressPersonalName(String emailAddress) {
        return toRecipientPersonalNames.get(emailAddress);
    }

    /**
     * States whether the underlying mail content template produces an HTML mail or not.
     *
     * @return True if the mail template produces HTML
     */
    public boolean isHtmlMail() {
        return false;
    }

    /**
     * @return whether to insert the personal name (first name, last name and salutation if
     *         available) of a recipient to the TO, CC or BCC header. By default the personal name
     *         is not added.
     * @since 3.5
     */
    public boolean isInsertRecipientPersonalName() {
        return insertRecipientPersonalName;
    }

    /**
     * Prepare a model of key-value pairs which will be passed to the template engine when the
     * content and subject templates should be rendered.
     *
     * @param model
     *            The model to prepare. It will be initialized with the settings returned by
     *            {@link #getGlobalModel()}.
     */
    public abstract void prepareModel(Map<String, Object> model);

    /**
     * Set the email address of sender.<br>
     * 
     * Note: be careful when setting an address because the domain part of the address should
     * normally not leave the domain of the outgoing email server.
     * 
     * @param fromAddress
     *            the email address to be used in From header
     * @since 3.5
     */
    public void setFromAddress(String fromAddress) {
        this.fromAddress = fromAddress;
    }
    
    /**
     * Set the personal name of the sender.
     * 
     * @param fromAddressName
     *            the name of the sender to be used in the From header
     * @since 3.5
     */
    public void setFromAddressName(String fromAddressName) {
        this.fromAddressName = fromAddressName;
    }
    
    /**
     * Define whether the personal name (first name, last name and salutation if available) of a
     * recipient should be added to the TO, CC or BCC headers.
     * 
     * @param insertRecipientPersonalName
     *            true to insert the personal name, false otherwise
     * @since 3.5
     */
    public void setInsertRecipientPersonalName(boolean insertRecipientPersonalName) {
        this.insertRecipientPersonalName = insertRecipientPersonalName;
    }

}
