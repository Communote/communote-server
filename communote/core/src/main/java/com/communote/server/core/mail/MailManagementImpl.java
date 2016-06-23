package com.communote.server.core.mail;

import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Service;

import com.communote.common.encryption.EncryptionException;
import com.communote.common.encryption.EncryptionUtils;
import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.config.ApplicationConfigurationProperties;
import com.communote.server.api.core.config.type.ApplicationProperty;
import com.communote.server.api.core.config.type.ApplicationPropertyMailing;

/**
 * The implementation of the mail management
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
@Service("mailManagement")
public class MailManagementImpl extends MailManagementBase {

    /**
     * logger instance
     */
    private static Logger LOG = LoggerFactory.getLogger("MailStatus");

    private static AtomicBoolean IS_INITIALIZED = new AtomicBoolean(false);

    private JavaMailSender mailSender;

    /**
     * Default constructor
     */
    public MailManagementImpl() {
        // Do nothing.
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void handleResetSettings() {
        IS_INITIALIZED.set(false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void handleSendMail(MimeMessagePreparator mailMessage) {
        initalizeMailSender();
        try {
            if (mailSender != null) {
                mailSender.send(mailMessage);
            }
            LOG.info(mailMessage.toString());
        } catch (InvalidRecipientMailAddressException e) {
            LOG.error("Recipients contain deleted user", e);
            throw e;
        } catch (Exception e) {
            LOG.error("Error sending email!", e);
            throw new MailingException("Error sending email!", e);
        }
    }

    /**
     * Initialize the mail sender and apply the properties
     */
    private synchronized void initalizeMailSender() {
        try {
            if (IS_INITIALIZED.compareAndSet(false, true)) {
                Properties mailingProperties = new Properties();
                KenmeiJavaMailSender tempMailSenderImpl = new KenmeiJavaMailSender();

                ApplicationConfigurationProperties props = CommunoteRuntime.getInstance()
                        .getConfigurationManager().getApplicationConfigurationProperties();
                tempMailSenderImpl.setPort(props.getProperty(ApplicationPropertyMailing.PORT, 25));
                tempMailSenderImpl.setHost(props.getProperty(ApplicationPropertyMailing.HOST));
                if (StringUtils.isNotEmpty(props.getProperty(ApplicationPropertyMailing.LOGIN))) {
                    tempMailSenderImpl.setUsername(props
                            .getProperty(ApplicationPropertyMailing.LOGIN));
                    mailingProperties.setProperty("mail.smtp.auth", "true");

                    if (StringUtils.isNotEmpty(props
                            .getProperty(ApplicationPropertyMailing.PASSWORD))) {
                        String decryptedPassword;
                        decryptedPassword = EncryptionUtils.decrypt(
                                props.getProperty(ApplicationPropertyMailing.PASSWORD),
                                ApplicationProperty.INSTALLATION_UNIQUE_ID.getValue());
                        tempMailSenderImpl.setPassword(decryptedPassword);
                    }
                }
                mailingProperties.setProperty(
                        "mail.smtp.starttls.enable",
                        props.getProperty(ApplicationPropertyMailing.USE_STARTTLS,
                                Boolean.FALSE.toString()));
                tempMailSenderImpl.setJavaMailProperties(mailingProperties);
                this.mailSender = tempMailSenderImpl;
            }
        } catch (EncryptionException e) {
            LOG.error("Error decrypting mail password.");
            throw new RuntimeException(e);
        }
    }
}
