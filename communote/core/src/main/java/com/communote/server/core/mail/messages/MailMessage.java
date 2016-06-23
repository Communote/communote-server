package com.communote.server.core.mail.messages;

import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.apache.commons.lang.StringUtils;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.config.ApplicationConfigurationProperties;
import com.communote.server.api.core.config.ClientConfigurationProperties;
import com.communote.server.api.core.config.type.ApplicationPropertyMailing;
import com.communote.server.api.core.config.type.ClientProperty;
import com.communote.server.core.common.i18n.LocalizationManagement;
import com.communote.server.core.mail.MailMessageHelper;
import com.communote.server.core.user.helper.UserNameHelper;
import com.communote.server.model.user.User;
import com.communote.server.persistence.common.messages.ResourceBundleManager;
import com.communote.server.persistence.user.client.ClientHelper;
import com.communote.server.persistence.user.client.ClientUrlHelper;

/**
 * An abstract base class for all mail messages
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class MailMessage implements MimeMessagePreparator {

    private static final String ENCODING = "UTF-8";

    /** The Constant CONFIRMATION_LINK_PREFIX. */
    public final static String ACTIVATION_LINK_PREFIX = "/admin/client/usermanagementview";

    private static final String HEADER_MESSAGE_ID = "Message-ID";

    /** The configuration to use for constants. */
    private final ClientConfigurationProperties clientConfiguration = CommunoteRuntime
            .getInstance().getConfigurationManager().getClientConfigurationProperties();

    private final ApplicationConfigurationProperties applicationConfiguration = CommunoteRuntime
            .getInstance().getConfigurationManager().getApplicationConfigurationProperties();

    /** the name of the template path to use. */

    private final Locale locale;

    private final User[] receivers;

    private String subject;

    private final String messageKey;

    private String messageTemplate;
    private String subjectTemplate;

    private String[] receiversAsString;

    /**
     * Construct a new message with the given message key and locale.
     *
     * @param messageKey
     *            Key of the message template.
     * @param locale
     *            the locale to use
     * @param receivers
     *            A list of receivers for this message.
     */
    public MailMessage(String messageKey, Locale locale, User... receivers) {
        this(messageKey, null, locale, receivers);
    }

    /**
     * Construct a new message with the given message key and locale.
     *
     * @param messageKey
     *            Key of the message template.
     * @param templatePlaceholderMessageKeys
     *            A mapping of placeholders to message keys, which will be replaced within the
     *            template with the loaded message. Use it in the template @@placeholder@@.
     * @param locale
     *            the locale to use
     * @param receivers
     *            A list of receivers for this message.
     */
    public MailMessage(String messageKey, Map<String, String> templatePlaceholderMessageKeys,
            Locale locale, User... receivers) {
        this.messageKey = messageKey;
        this.locale = locale;
        this.receivers = receivers;
        if (locale == null) {
            throw new IllegalArgumentException("Locale cannot be null!");
        }
        ResourceBundleManager resourceBundleManager = ResourceBundleManager.instance();
        this.messageTemplate = resourceBundleManager.getText(messageKey, locale);
        this.subjectTemplate = resourceBundleManager.getText(messageKey + ".subject", locale);
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
            throw new ResourceNotFoundException(
                    "Resource not found for either locale and/or fallback locale for key:  "
                            + messageKey);
        }
    }

    /**
     * Add a receiver by setting also the personal saluation if availablke.
     *
     * @param message
     *            the mime message
     * @param email
     *            the email of the receiver
     * @param receiver
     *            the receivers addtional information (hint: email is NOT used for sending)
     * @throws MessagingException
     *             in case of an error
     * @throws UnsupportedEncodingException
     *             in case of an error
     */
    protected void addReceiver(MimeMessageHelper message, String email, User receiver)
            throws MessagingException, UnsupportedEncodingException {
        String signature = UserNameHelper.getCompleteSignature(receiver);
        if (signature == null) {
            message.addTo(email);
        } else {
            message.addTo(email, signature);
        }
    }

    /**
     * Adapt all receiving addresses to the test address and put the original recipients into the
     * mail text.
     *
     * @param message
     *            The message with the addresses
     * @param text
     *            The text
     * @throws MessagingException
     *             In case of an error
     */
    private void checkTestMode(MimeMessageHelper message, String text) throws MessagingException {
        if (!CommunoteRuntime.getInstance().getConfigurationManager().getDevelopmentProperties()
                .isMailingTestMode()) {
            return;
        }
        StringBuilder testOutput = new StringBuilder();
        testOutput.append(text);
        testOutput.append("\n\n--------------------------------------------------\n"
                + "TEST OUTPUT\n\nOriginal addresses:\n\n");
        MailMessageHelper.changeAddresses(testOutput, message, CommunoteRuntime.getInstance()
                .getConfigurationManager().getDevelopmentProperties().getMailingTestAddress());
        message.setText(testOutput.toString(), isHtmlMail());
    }

    /**
     * Gets the email address of the sender.<br>
     * Default implementation returns a value defined within a configuration file.<br>
     * One should be careful when overriding this method because the domain part of the address
     * should normally not leave the domain of the outgoing email server.
     *
     * @return the email address
     */
    public String getFromAddress() {
        return applicationConfiguration.getAssertProperty(ApplicationPropertyMailing.FROM_ADDRESS);
    }

    /**
     * Gets the from sender name.
     *
     * @return the sender name
     */
    public String getFromAddressName() {
        return applicationConfiguration
                .getAssertProperty(ApplicationPropertyMailing.FROM_ADDRESS_NAME);
    }

    /**
     * get data that should be accessible for every mail message
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
    protected String getMessageIdentifier() {
        return null;
    }

    /**
     * Returns a string holding the email address to be used in the "reply-to" header. The default
     * implementation returns an address defined for the current client. If null is returned, the
     * "reply-to" header will not be set.
     *
     * @return the email address
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
     * States if the underlying mail template is an html mail or not.
     *
     * @return True if the mail template is an html email
     */
    protected boolean isHtmlMail() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void prepare(MimeMessage mimeMessage) throws Exception {
        MimeMessageHelper message = new MimeMessageHelper(mimeMessage, ENCODING);
        // set some headers to avoid automatic OutOfOffice replies
        mimeMessage.setHeader("Precedence", "list");
        mimeMessage.setHeader("X-Auto-Response-Suppress", "OOF");
        String identifier = getMessageIdentifier();
        if (identifier != null) {
            mimeMessage.setHeader(HEADER_MESSAGE_ID,
                    MailMessageHelper.createMessageIdHeaderValue(identifier));
        }
        // set before as default, the velocity engine may overwrite this
        message.setFrom(getFromAddress(), getFromAddressName());
        message.setSentDate(new Date());

        String replyTo = getReplyToAddress();
        String replyToName = getReplyToAddressName();
        if (!StringUtils.isBlank(replyTo)) {
            message.setReplyTo(replyTo, replyToName);
        }

        // get global model
        Map<String, Object> model = getGlobalModel();
        prepareModel(model);
        model.put(MailModelPlaceholderConstants.MESSAGE, message);

        StringWriter messageWriter = new StringWriter();
        StringWriter subjectWriter = new StringWriter();
        try {
            VelocityEngine velocityEngine = ServiceLocator.findService(VelocityEngine.class);
            velocityEngine.evaluate(new VelocityContext(model), messageWriter, messageKey,
                    messageTemplate);
            velocityEngine.evaluate(new VelocityContext(model), subjectWriter, messageKey
                    + ".subject", subjectTemplate);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        String text = messageWriter.toString().trim();
        message.setText(text, isHtmlMail());
        subject = subjectWriter.toString().trim();
        message.setSubject(subject);
        setReceivers(message);
        MailMessageHelper.assertNonAnonymousRecipients(mimeMessage);
        receiversAsString = MailMessageHelper.getRecipients(mimeMessage, Message.RecipientType.TO);
        checkTestMode(message, text);
    }

    /**
     * Use this to set the needed objects for applying the template. If restrict the map to a size
     * add +1 for the message to be added.
     *
     * @param model
     *            The model to add elements to.
     */
    protected abstract void prepareModel(Map<String, Object> model);

    /**
     * Set the receivers for this message.
     *
     * @param message
     *            The message to use
     * @throws MessagingException
     *             In case of an error
     * @throws UnsupportedEncodingException
     *             In case of an error
     */
    public void setReceivers(MimeMessageHelper message) throws MessagingException,
            UnsupportedEncodingException {
        if (receivers == null) {
            return;
        }
        for (User receiver : receivers) {
            message.addTo(receiver.getEmail());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuilder message = new StringBuilder();
        message.append("Template: ");
        message.append(messageKey);
        message.append(";Locale: ");
        message.append(getLocale().toString());
        message.append(";From: ");
        message.append(getFromAddress());
        message.append(";To: ");
        if (receiversAsString != null) {
            message.append(StringUtils.join(receiversAsString, ","));
        } else {
            message.append("No Receivers");
        }
        message.append(";Subject: ");
        message.append(subject);
        return message.toString();
    }
}
