package com.communote.server.core.mail.fetching;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.IllegalWriteException;
import javax.mail.Message;
import javax.mail.MessagingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.config.type.ApplicationPropertyMailfetching;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class MailFetcher {
    private final static String PROTOCOL_IMAP = "imap";

    private final static Logger LOGGER = LoggerFactory.getLogger(MailFetcher.class);

    /** singleton instance of the mail fetcher */
    private static MailFetcher INSTANCE;

    /**
     * Returns the singleton instance of the MailFetcher.
     *
     * @return the instance of the MailFetcher or null if the initialization of the fetcher failed
     *         (due to configuration problems).
     */
    public static synchronized MailFetcher instance() {
        if (INSTANCE == null) {
            INSTANCE = new MailFetcher();
            if (!INSTANCE.initialize()) {
                INSTANCE = null;
            }
        }
        return INSTANCE;
    }

    private MailboxConnection connection;

    /**
     * private constructor
     */
    protected MailFetcher() {
    }

    /**
     * Deletes an email message.
     *
     * @param message
     *            the message
     * @return true if the email message was deleted successfully (which means the deleted flag was
     *         set), false otherwise
     */
    public boolean deleteMessage(Message message) {
        try {
            try {
                message.setFlag(Flags.Flag.DELETED, true);
            } catch (IllegalWriteException e) {
                // try reopening the folder and delete the message
                Folder folder = message.getFolder();
                if (folder != null && !folder.isOpen()) {
                    folder.open(Folder.READ_WRITE);
                    message.setFlag(Flags.Flag.DELETED, true);
                }
            }
        } catch (MessagingException e) {
            LOGGER.error("Exception occured while deleting a message.", e);
            return false;
        }
        return true;
    }

    /**
     * Expunge (permanently remove) messages having the deleted flag.
     *
     * @return true if the operation succeeded, false otherwise
     */
    public boolean expungeMessages() {
        // just delegate to connection because operation is protocol specific
        return connection.expungeMessages();
    }

    /**
     * Returns a unique id for an email message.
     *
     * @param message
     *            the message
     * @return the unique id or null if not found
     */
    public String getUniqueMessageId(Message message) {
        // delegate to connection because retrieving a unique message ID is
        // protocol specific
        return connection.getUniqueMessageId(message);
    }

    /**
     * Initializes the fetcher from configuration.
     *
     * @return true if the initialization succeeded, false otherwise. Retrying will not succeed.
     */
    private boolean initialize() {
        String protocol = CommunoteRuntime.getInstance().getConfigurationManager()
                .getApplicationConfigurationProperties()
                .getProperty(ApplicationPropertyMailfetching.PROTOCOL);
        if (protocol == null) {
            LOGGER.error("MailFetcher initialization failed. No protocol specified.");
            return false;
        }
        if (protocol.equalsIgnoreCase(MailInProtocolType.IMAP.getName())) {
            connection = new ImapMailboxConnection(false);
        } else if (protocol.equalsIgnoreCase(MailInProtocolType.IMAPS.getName())) {
            connection = new ImapMailboxConnection(true);
        } else {
            LOGGER.error("MailFetcher initialization failed. The protocol {} is not supported.",
                    protocol);
            return false;
        }
        return connection.initialize();
    }

    /**
     * Stops the mail-fetcher.
     */
    public void shutdown() {
        connection.disconnect();
        INSTANCE = null;
    }

    /**
     * Blocking method which starts fetching mails. All mails found which do not have the deleted
     * flag will be passed to the mail message worker. The method only returns in case of an error
     * (e.g. connection to the mail server was lost) or when another thread called the
     * {@link #shutdown()} method.
     *
     * @param worker
     *            used for processing mails
     * @throws MailboxConnectionException
     *             if an error or exception occurs while connecting or listening. The exception
     *             holds information whether a restart at a later time may succeed.
     */
    public void startFetching(MailMessageWorker worker) throws MailboxConnectionException {
        try {
            connection.startListening(worker);
        } catch (MailboxConnectionException e) {
            // cleanup
            if (!e.getReconnectMaySucceed()) {
                shutdown();
            }
            throw e;
        }
    }
}
