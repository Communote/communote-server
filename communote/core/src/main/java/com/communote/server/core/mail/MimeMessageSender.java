package com.communote.server.core.mail;

import java.util.Map;

import javax.mail.internet.MimeMessage;

import com.communote.server.api.core.config.type.ApplicationPropertyMailing;

/**
 * Send a mime message.
 * 
 * @author Communote team - <a href="http://communote.github.io/">http://communote.github.io/</a>
 */
public interface MimeMessageSender {

    /**
     * @return an empty mime message to be filled by the caller
     */
    public MimeMessage createMimeMessage();

    /**
     * Send the mime message.
     * 
     * @param message
     *            the message to send
     * @throws MailingException
     *             in case sending failed
     */
    public void send(MimeMessage message) throws MailingException;

    /**
     * Test the new configuration for sending mime messages. Implementations can for example do a
     * connection test. Exceptions should be caught and logged.
     * 
     * @param settings
     *            the configuration to test
     * @return true if the configuration is valid for this mime message sender implementation, false
     *         otherwise.
     */
    public boolean testSettings(Map<ApplicationPropertyMailing, String> settings);

    /**
     * Test the new configuration for sending mime messages and send a test message. Exceptions
     * should be caught and logged.
     * 
     * @param settings
     *            the configuration to test
     * @param testMessage
     *            a test message to send. Can be null. In this case the method should behave like
     *            {@link #testSettings(Map)}.
     * @return true if the configuration is valid for this mime message sender implementation, false
     *         otherwise.
     */
    public boolean testSettings(Map<ApplicationPropertyMailing, String> settings,
            MimeMessage testMessage);

    /**
     * Update the configuration of this mime message sender so that it will be used in subsequent
     * invocations of {@link #send(MimeMessage)}. Implementations don't need to validate the
     * settings.
     * 
     * @param settings
     *            the new settings
     */
    public void updateSettings(Map<ApplicationPropertyMailing, String> settings);
}
