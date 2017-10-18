package com.communote.server.core.mail;

import java.util.Map;

import com.communote.server.api.core.config.ConfigurationUpdateException;
import com.communote.server.api.core.config.type.ApplicationPropertyMailing;
import com.communote.server.core.mail.messages.MailMessage;

/**
 * Component for sending mails. The mail sender converts {@link MailMessage}s into MimeMessages and
 * uses a {@link MimeMessageSender} to transport them.
 * 
 * @author Communote team - <a href="http://communote.github.io/">http://communote.github.io/</a>
 */
public interface MailSender {

    /**
     * Send the given message.
     * 
     * @param message
     * @throws MailingException
     *             in case there was an error sending the message
     */
    public void send(MailMessage message) throws MailingException;

    /**
     * Test the given mail-out configuration. Depending on the used {@link MimeMessageSender} this
     * might include a connection test with the server.
     * 
     * @param settings
     *            the settings to test
     * @return true if the settings were tested successfully, false otherwise. Occurred exceptions
     *         will be logged.
     */
    public boolean testSettings(Map<ApplicationPropertyMailing, String> settings);

    /**
     * Test the given mail-out configuration and send a test message.
     * 
     * @param settings
     *            the settings to test
     * @param testMessage
     *            the test message to send
     * @return true if the settings were tested successfully and the test message was sent without
     *         error, false otherwise. Occurred exceptions will be logged.
     */
    public boolean testSettings(Map<ApplicationPropertyMailing, String> settings,
            MailMessage testMessage);

    /**
     * Update the mail-out configuration so that subsequent calls to {@link #send(MailMessage)} will
     * use the new settings. This method won't test the settings for validity.
     * 
     * @param settings
     *            the new configuration
     * @throws ConfigurationUpdateException
     *             in case the configuration could not be updated
     */
    public void updateSettings(Map<ApplicationPropertyMailing, String> settings)
            throws ConfigurationUpdateException;
}
