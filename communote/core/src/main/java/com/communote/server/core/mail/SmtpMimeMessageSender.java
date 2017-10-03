package com.communote.server.core.mail;

import java.util.Map;
import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.Transport;
import javax.mail.internet.MimeMessage;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import com.communote.server.api.core.config.type.ApplicationPropertyMailing;

/**
 * MimeMessageSender which uses SMTP to send the message.
 * 
 * @author Communote team - <a href="http://communote.github.io/">http://communote.github.io/</a>
 *
 */
public class SmtpMimeMessageSender implements MimeMessageSender {

    private static final String PROTOCOL = "smtp";
    private static final Logger LOGGER = LoggerFactory.getLogger(SmtpMimeMessageSender.class);

    private JavaMailSenderImpl sender;

    public SmtpMimeMessageSender(Map<ApplicationPropertyMailing, String> settings) {
        sender = createSender(settings);
    }

    @Override
    public MimeMessage createMimeMessage() {
        return new MessageIdPreservingMimeMessage(sender.getSession());
    }

    protected JavaMailSenderImpl createSender(Map<ApplicationPropertyMailing, String> settings) {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setProtocol(PROTOCOL);
        sender.setHost(settings.get(ApplicationPropertyMailing.HOST));
        sender.setPort(getPort(settings));
        Properties mailingProperties = new Properties();
        String login = settings.get(ApplicationPropertyMailing.LOGIN);
        if (StringUtils.isNotEmpty(login)) {
            sender.setUsername(login);
            mailingProperties.setProperty("mail.smtp.auth", "true");
            String password = settings.get(ApplicationPropertyMailing.PASSWORD);
            if (StringUtils.isNotEmpty(password)) {
                // expect non-encrypted password!
                sender.setPassword(password);
            }
        }
        mailingProperties.setProperty("mail.smtp.starttls.enable", getStartTls(settings));
        sender.setJavaMailProperties(mailingProperties);
        return sender;
    }

    private int getPort(Map<ApplicationPropertyMailing, String> settings) {
        String port = settings.get(ApplicationPropertyMailing.PORT);
        if (port != null) {
            try {
                return Integer.parseInt(port);
            } catch (NumberFormatException e) {
            }
        }
        return 25;
    }

    private String getStartTls(Map<ApplicationPropertyMailing, String> settings) {
        String startTls = settings.get(ApplicationPropertyMailing.USE_STARTTLS);
        return String.valueOf(Boolean.parseBoolean(startTls));
    }

    @Override
    public void send(MimeMessage message) throws MailingException {
        try {
            sender.send(message);
        } catch (MailException e) {
            throw new MailingException("Sending mime message failed", e);
        }
    }

    protected void testConnection(JavaMailSenderImpl sender) throws MessagingException {
        Transport transport = null;
        try {
            // TODO is there a better way to enable debugging?
            sender.getSession().setDebug(LOGGER.isDebugEnabled());
            transport = sender.getSession().getTransport(PROTOCOL);
            transport.connect(sender.getHost(), sender.getPort(), sender.getUsername(),
                    sender.getPassword());
        } finally {
            if (transport != null) {
                transport.close();
            }
        }

    }

    @Override
    public boolean testSettings(Map<ApplicationPropertyMailing, String> settings) {
        return testSettings(settings, null);
    }

    @Override
    public boolean testSettings(Map<ApplicationPropertyMailing, String> settings,
            MimeMessage testMessage) {
        JavaMailSenderImpl tmpSender = createSender(settings);
        try {
            testConnection(tmpSender);
        } catch (MessagingException e) {
            LOGGER.error("Connection test to SMTP server failed", e);
            return false;
        }
        if (testMessage != null) {
            try {
                tmpSender.send(testMessage);
            } catch (MailException e) {
                LOGGER.error("Sending test message failed", e);
                return false;
            }
        }
        return true;
    }

    @Override
    public void updateSettings(Map<ApplicationPropertyMailing, String> settings) {
        // TODO test connection before updating?
        sender = createSender(settings);
    }

}
