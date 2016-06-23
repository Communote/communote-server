package com.communote.server.core.mail;

import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Transport;
import javax.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import com.communote.server.core.mail.messages.TextMailMessage;

/**
 * MailSender that creates {@link MessageIdPreservingMimeMessage} mime messages
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class KenmeiJavaMailSender extends JavaMailSenderImpl {

    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(KenmeiJavaMailSender.class);

    /**
     * Constructor.
     */
    public KenmeiJavaMailSender() {
        super();
    }

    /**
     * @param host
     *            The host.
     * @param port
     *            The port.
     * @param username
     *            The username, might be null.
     * @param password
     *            The password, might be null.
     * @param javaMailProperties
     *            Mailing properties.
     */
    public KenmeiJavaMailSender(String host, int port, String username, String password,
            Properties javaMailProperties) {
        super();
        setHost(host);
        setPort(port);
        setJavaMailProperties(javaMailProperties);
        setUsername(username);
        setPassword(password);
    }

    /**
     * @return True, if this sender can connect, else false.
     */
    public boolean canConnect() {
        try {
            Transport transport = getSession().getTransport("smtp");
            transport.connect(getHost(), getPort(), getUsername(), getPassword());
            if (!transport.isConnected()) {
                throw new MessagingException("The service is not connected.");
            }
            transport.close();
            return true;
        } catch (NoSuchProviderException e) {
            LOGGER.debug(e.getMessage());
        } catch (MessagingException e) {
            LOGGER.debug(e.getMessage());
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MimeMessage createMimeMessage() {
        // create a Message-ID preserving mime message
        return new MessageIdPreservingMimeMessage(getSession());
    }

    /**
     * @param subject
     *            The subject.
     * @param content
     *            The content
     * @param fromEmailAddress
     *            The senders email address. Might be null.
     * @param fromName
     *            The senders name. Might be null.
     * @param to
     *            The receiver.
     * @throws MailException
     *             Exception.
     */
    public void send(String subject, String content, String fromEmailAddress, String fromName,
            String to)
            throws MailException {
        send(new TextMailMessage(subject, content, to, fromEmailAddress, fromName));
    }
}
