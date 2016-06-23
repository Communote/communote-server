package com.communote.server.test.mail;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;
import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessagePreparator;

import com.communote.common.encryption.EncryptionException;
import com.communote.common.encryption.EncryptionUtils;
import com.communote.common.io.IOHelper;
import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.config.ApplicationConfigurationProperties;
import com.communote.server.api.core.config.type.ApplicationProperty;
import com.communote.server.api.core.config.type.ApplicationPropertyMailing;
import com.communote.server.core.mail.MailManagement;
import com.communote.server.core.mail.MailingException;

/**
 * MailManagemet which does not send the e-mails but validates that from-field and recipients are
 * set. Also checks that the configuration is valid.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class MockMailManagement implements MailManagement {

    /** Logger. */
    private final static Logger LOGGER = Logger.getLogger(MockMailManagement.class);
    private boolean initialized = false;
    private JavaMailSender mailSender;

    private final String mailStoragePath;

    /**
     * Constructor.
     */
    public MockMailManagement() {
        mailStoragePath = CommunoteRuntime.getInstance().getConfigurationManager()
                .getStartupProperties().getDataDirectory().getAbsolutePath()
                + File.separator + "development" + File.separator + "mails" + File.separator;
        File mailStorage = new File(mailStoragePath);
        mailStorage.mkdirs();
        LOGGER.info("Mock Mailing initialized with storing path: " + mailStoragePath);
    }

    /**
     * initializes the mail sender
     */
    private synchronized void initializeSender() {
        try {
            if (!initialized) {
                JavaMailSenderImpl tempMailSenderImpl = new JavaMailSenderImpl();
                ApplicationConfigurationProperties props = CommunoteRuntime.getInstance()
                        .getConfigurationManager().getApplicationConfigurationProperties();
                tempMailSenderImpl.setPort(props.getProperty(ApplicationPropertyMailing.PORT, 25));
                tempMailSenderImpl.setHost(props.getProperty(ApplicationPropertyMailing.HOST));
                if (StringUtils.isNotBlank(props.getProperty(ApplicationPropertyMailing.LOGIN))) {
                    tempMailSenderImpl.setUsername(props
                            .getProperty(ApplicationPropertyMailing.LOGIN));
                }
                if (StringUtils.isNotBlank(props.getProperty(ApplicationPropertyMailing.PASSWORD))) {
                    String decryptedPassword;
                    decryptedPassword = EncryptionUtils.decrypt(
                            props.getProperty(ApplicationPropertyMailing.PASSWORD),
                            ApplicationProperty.INSTALLATION_UNIQUE_ID.getValue());
                    tempMailSenderImpl.setPassword(decryptedPassword);
                }
                Properties mailingProperties = new Properties();
                mailingProperties.setProperty("mail.smtp.starttls.enable", "true");
                tempMailSenderImpl.setJavaMailProperties(mailingProperties);
                this.mailSender = tempMailSenderImpl;
                initialized = true;
            }
        } catch (EncryptionException e) {
            throw new RuntimeException("Error decrypting mail password.", e);
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void resetSettings() {
        synchronized (this) {
            initialized = false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sendMail(MimeMessagePreparator mailMessage) throws MailingException {
        if (!initialized) {
            initializeSender();
        }
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        try {
            mailMessage.prepare(mimeMessage);
        } catch (Exception e) {
            throw new MailingException("MimeMessage preparation failed", e);
        }
        try {
            // validate the mime message
            if (mimeMessage.getFrom() == null || mimeMessage.getFrom().length == 0) {
                throw new MailingException("Sender must be specified.");
            }
            if (mimeMessage.getAllRecipients() == null
                    || mimeMessage.getAllRecipients().length == 0) {
                throw new MailingException("Recipients must be specified");
            }
            String mailsFileName = new Date() + "-" + mimeMessage.getSubject() + ".txt";
            mailsFileName = mailsFileName.replace("$", "_");
            mailsFileName = mailsFileName.replace("{", "_");
            mailsFileName = mailsFileName.replace("}", "_");
            mailsFileName = mailsFileName.replace("..", ".");
            mailsFileName = mailsFileName.replace(" ", "_");
            mailsFileName = mailsFileName.replace(":", "_");
            FileOutputStream mailOutputStream = new FileOutputStream(mailStoragePath
                    + mailsFileName);
            mimeMessage.writeTo(mailOutputStream);
            IOHelper.close(mailOutputStream);
        } catch (MessagingException e) {
            throw new MailingException("Error sending email", e);
        } catch (Exception e) {
            LOGGER.debug("There was an error storing a message.");
        }
    }
}
