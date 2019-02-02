package com.communote.server.core.mail;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.apache.velocity.app.VelocityEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.communote.common.encryption.EncryptionException;
import com.communote.common.encryption.EncryptionUtils;
import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.config.ApplicationConfigurationPropertyConstant;
import com.communote.server.api.core.config.ConfigurationManager;
import com.communote.server.api.core.config.ConfigurationUpdateException;
import com.communote.server.api.core.config.type.ApplicationProperty;
import com.communote.server.api.core.config.type.ApplicationPropertyMailing;
import com.communote.server.core.mail.messages.MailMessage;
import com.communote.server.model.user.User;

/**
 * 
 * @author Communote team - <a href="http://communote.github.io/">http://communote.github.io/</a>
 * @since 3.5
 */
@Service("mailSender")
public class MailSenderImpl implements MailSender {

    private static Logger LOGGER = LoggerFactory.getLogger("MailStatus");

    private MimeMessageSender mimeMessageSender;
    private final VelocityEngine velocityEngine;

    @Autowired
    public MailSenderImpl(VelocityEngine velocityEngine) {
        this.velocityEngine = velocityEngine;
    }

    private synchronized MimeMessageSender createMimeMessageSender() throws MailingException {
        if (this.mimeMessageSender != null) {
            return this.mimeMessageSender;
        }
        try {
            return new MimeMessageSenderFactory().createInstance();
        } catch (Exception e) {
            throw new MailingException("Creation of mime message sender failed", e);
        }
    }

    private String getMessageDetails(MailMessage message) {
        StringBuilder messageDetails = new StringBuilder();
        messageDetails.append("Template ID: ");
        messageDetails.append(message.getContentTemplateId());
        messageDetails.append(";Locale: ");
        messageDetails.append(message.getLocale().toString());
        messageDetails.append(";From: ");
        messageDetails.append(message.getFromAddress());
        messageDetails.append(";To: ");
        boolean hasRecipients = false;
        for (User user : message.getTo()) {
            if (hasRecipients) {
                messageDetails.append(',');
            }
            messageDetails.append(user.getEmail());
            hasRecipients = true;
        }
        for (String toAddress : message.getToAddresses()) {
            if (hasRecipients) {
                messageDetails.append(',');
            }
            messageDetails.append(toAddress);
        }
        return messageDetails.toString();
    }

    private MimeMessageSender getMimeMessageSender() throws MailingException {
        if (mimeMessageSender == null) {
            // lazy initialization since factory prepares the sender and needs to access database which has
            // to be updated before
            mimeMessageSender = createMimeMessageSender();
        }
        return mimeMessageSender;
    }

    private MimeMessage prepareMimeMessage(MailMessage message)
            throws MessagingException, UnsupportedEncodingException {
        VelocityMimeMessagePreparator preparator = new VelocityMimeMessagePreparator(message,
                velocityEngine);
        MimeMessage mimeMessage = getMimeMessageSender().createMimeMessage();
        preparator.prepare(mimeMessage);
        MailMessageHelper.assertNonAnonymousRecipients(mimeMessage);
        return mimeMessage;
    }

    @Override
    public void send(MailMessage message) throws MailingException {
        try {
            getMimeMessageSender().send(prepareMimeMessage(message));
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Mail message sent successfully. {}", getMessageDetails(message));
            }
        } catch (UnsupportedEncodingException | InvalidRecipientMailAddressException
                | MessagingException e) {
            throw new MailingException("Sending mail message for template "
                    + message.getContentTemplateId() + " failed", e);
        }
    }

    @Override
    public boolean testSettings(Map<ApplicationPropertyMailing, String> settings) {
        try {
            return getMimeMessageSender().testSettings(settings);
        } catch (MailingException e) {
            LOGGER.error("Testing new mail settings failed", e);
            return false;
        }
    }

    @Override
    public boolean testSettings(Map<ApplicationPropertyMailing, String> settings,
            MailMessage testMessage) {
        try {
            return getMimeMessageSender().testSettings(settings, prepareMimeMessage(testMessage));
        } catch (UnsupportedEncodingException | MessagingException
                | MailingException e) {
            LOGGER.error("Testing new mail settings failed", e);
            return false;
        }
    }

    @Override
    public void updateSettings(Map<ApplicationPropertyMailing, String> settings)
            throws ConfigurationUpdateException {
        ConfigurationManager manager = CommunoteRuntime.getInstance().getConfigurationManager();
        HashMap<ApplicationConfigurationPropertyConstant, String> props = new HashMap<>(
                settings.size());
        props.putAll(settings);
        // TODO the configuration manager should take care of encryption. The property could have a
        // flag to mark it as secure / "should be encrypted"
        String password = settings.get(ApplicationPropertyMailing.PASSWORD);
        try {
            if (password != null) {
                password = EncryptionUtils.encrypt(password,
                        ApplicationProperty.INSTALLATION_UNIQUE_ID.getValue());
                props.put(ApplicationPropertyMailing.PASSWORD, password);
            }
        } catch (EncryptionException e) {
            throw new ConfigurationUpdateException("Encrypting mail account password failed", null,
                    e);
        }
        manager.updateApplicationConfigurationProperties(props);
        try {
            getMimeMessageSender().updateSettings(settings);
        } catch (MailingException e) {
            throw new ConfigurationUpdateException(
                    "Updating configuration of mime message sender failed", null, e);
        }
    }

}
