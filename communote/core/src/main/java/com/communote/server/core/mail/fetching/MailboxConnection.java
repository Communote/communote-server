package com.communote.server.core.mail.fetching;

import java.util.Properties;

import javax.mail.AuthenticationFailedException;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.communote.common.encryption.EncryptionException;
import com.communote.common.encryption.EncryptionUtils;
import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.config.ApplicationConfigurationProperties;
import com.communote.server.api.core.config.ConfigurationManager;
import com.communote.server.api.core.config.type.ApplicationProperty;
import com.communote.server.api.core.config.type.ApplicationPropertyMailfetching;

/**
 * Used to connect to a mailbox using a specific protocol for retrieving messages. Possible
 * implementations of this abstract class are IMAP and POP3 based mailbox connections.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class MailboxConnection {

    /**
     * The log.
     */
    private final static Logger LOG = LoggerFactory.getLogger(MailboxConnection.class);

    /**
     * The default time to wait between checks for new messages in milliseconds.
     */
    private static final long DEFAULT_FETCH_TIMEOUT = 30000;

    /** default name of the mailbox */
    private static final String DEFAULT_MAILBOX = "INBOX";
    /** default password is empty string */
    private static final String DEFAULT_PASSWORD = "";

    private MessagesAddedListener messagesAddedListener;

    private String host;
    private String mailbox;
    private String login;
    private String password;

    /**
     * Flag indicating whether the connection is in the check messages loop.
     */
    private boolean checkingMessages;

    /**
     * A reference to the message store.
     */
    private Store messageStore;

    /**
     * The time to wait between checks for new messages, if applicable.
     */
    private long fetchTimeout;
    /**
     * Indicates that another thread initiates the close command for the connection.
     */
    private boolean shouldStop;

    /**
     * Attaches the listener which gets informed about a new message.
     *
     * @param worker
     *            the worker which will process the new message
     * @throws MailboxConnectionException
     *             if connecting the listener failed
     */
    private synchronized void attachChangeListener(MailMessageWorker worker)
            throws MailboxConnectionException {
        if (getFolder() == null) {
            LOG.error("The mailbox folder is not available.");
            throw new MailboxConnectionException("The mailbox folder is not available.", false);
        }
        if (messagesAddedListener == null) {
            messagesAddedListener = new MessagesAddedListener(worker);
        }
        getFolder().addMessageCountListener(messagesAddedListener);
    }

    /**
     * Closes the connection to the mail server
     */
    private void closeConnection() {
        try {
            messageStore.close();
        } catch (MessagingException e) {
            LOG.error("Exception while closing the connection.", e);
        }
    }

    /**
     * Opens a connection to the mailbox using the {@link #host}, {@link #mailbox},
     * {@link #password} and {@link #login} members.
     *
     * @throws MailboxConnectionException
     *             if the connection to the mailbox could not be established
     */
    private synchronized void connect() throws MailboxConnectionException {
        if (!messageStore.isConnected()) {
            try {
                // connect
                messageStore.connect(host, login, password);
                prepareFetching(mailbox);
            } catch (AuthenticationFailedException e) {
                LOG.error("Cannot access mailbox. Authentication failed");
                throw new MailboxConnectionException(
                        "Cannot access mailbox. Authentication failed", e);
            } catch (MessagingException e) {
                LOG.error("Cannot access mailbox.", e);
                throw new MailboxConnectionException("Cannot access mailbox.", e);
            }
        }
    }

    /**
     * Detaches the listener which gets informed about a new message.
     *
     */
    private synchronized void detachChangeListener() {
        if (messagesAddedListener != null) {
            messagesAddedListener.stop();
            if (getFolder() != null) {
                getFolder().removeMessageCountListener(messagesAddedListener);
            }
            messagesAddedListener = null;
        }
    }

    /**
     * Closes the connection to the mail server. This method will block until the connection is
     * closed.
     */
    public void disconnect() {
        synchronized (this) {
            shouldStop = true;
        }
        detachChangeListener();
        if (isCheckingMessages()) {
            // stop the check messages loop and wait for end of that loop
            stopCheckMessages();
            while (isCheckingMessages()) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {

                }
            }
        }
        closeConnection();
    }

    /**
     * Expunge messages with the deleted flag. See java mail API for protocol specific
     * considerations when implementing this method.
     *
     * @return true if the operation succeeded
     */
    public abstract boolean expungeMessages();

    /**
     * @return the fetchTimeout
     */
    protected long getFetchTimeout() {
        return fetchTimeout;
    }

    /**
     * Returns the mailbox folder or null if not initialized or available.
     *
     * @return the folder
     */
    protected abstract Folder getFolder();

    /**
     * @return the messageStore
     */
    protected Store getMessageStore() {
        return messageStore;
    }

    /**
     * Returns the name of the protocol used by this connection (e.g. "imap" or "pop3").
     *
     * @return the protocol name
     */
    public abstract String getProtocolName();

    /**
     * Returns a unique id for an email message.
     *
     * @param message
     *            the message
     * @return the unique id
     */
    public abstract String getUniqueMessageId(Message message);

    /**
     * Reads the configuration parameters.
     *
     * @return false if the initialization failed
     */
    private boolean initFromConfiguration() {
        // TODO add another security provider via Security.addProvider() or is Sun's provider ok?
        ApplicationConfigurationProperties applicationConfigProperties = CommunoteRuntime
                .getInstance().getConfigurationManager().getApplicationConfigurationProperties();
        host = ApplicationPropertyMailfetching.HOST.getValue();
        login = ApplicationPropertyMailfetching.USER_LOGIN.getValue();
        if (StringUtils.isBlank(login) || StringUtils.isBlank(host)) {
            LOG.error("Cannot access mailbox. " + (StringUtils.isBlank(host) ? "Host" : "Login")
                    + " is not defined.");
            return false;
        }

        try {
            password = EncryptionUtils.decrypt(
                    ApplicationPropertyMailfetching.USER_PASSWORD.getValue(),
                    ApplicationProperty.INSTALLATION_UNIQUE_ID.getValue());
        } catch (EncryptionException e) {
            password = DEFAULT_PASSWORD;
            LOG.error("Was not able to decrypt the property '"
                    + ApplicationPropertyMailfetching.USER_PASSWORD.getKeyString()
                    + "'. Using empty string as default.", e);
        }

        mailbox = applicationConfigProperties.getProperty(ApplicationPropertyMailfetching.MAILBOX,
                DEFAULT_MAILBOX);
        fetchTimeout = applicationConfigProperties.getProperty(
                ApplicationPropertyMailfetching.FETCH_TIMEOUT, DEFAULT_FETCH_TIMEOUT);
        String protocolName = getProtocolName();
        String propertiesProtocolPrefix = "mail." + protocolName + ".";
        Properties properties = new Properties();
        properties.putAll(System.getProperties());
        // StartTLS will only be used if the server supports it.
        boolean startTls = applicationConfigProperties.getProperty(
                ApplicationPropertyMailfetching.USE_STARTTLS, false);
        properties.put(propertiesProtocolPrefix + "starttls.enable", Boolean.toString(startTls));
        int port = applicationConfigProperties
                .getProperty(ApplicationPropertyMailfetching.PORT, -1);
        if (port > 0) {
            properties.put(propertiesProtocolPrefix + "port", port);
        }
        Session session = Session.getInstance(properties);
        boolean success = false;
        try {
            messageStore = session.getStore(protocolName);
            success = true;
        } catch (NoSuchProviderException e) {
            LOG.error("No provider for protocol " + protocolName + " found.", e);
        }
        return success;
    }

    /**
     * Initializes the connection from configuration properties using the
     * {@link ConfigurationManager}. This does not include opening the connection to the mail
     * server.
     *
     * @return true on success or false if the initialization fails for some reason like a
     *         configuration problem
     */
    public synchronized boolean initialize() {
        shouldStop = false;
        if (!initFromConfiguration()) {
            LOG.error("Attaching the mail listener failed.");
            return false;
        }
        return true;
    }

    /**
     * @return the checkingMessages
     */
    private synchronized boolean isCheckingMessages() {
        return checkingMessages;
    }

    /**
     * @return the shouldStop
     */
    protected synchronized boolean isShouldStop() {
        return shouldStop;
    }

    /**
     * Subclasses should use this method for protocol specific initialization. This includes for
     * instance testing if the mailbox exists and can be opened with read and write access rights.
     *
     * @param mailbox
     *            the mailbox to use. Subclasses should ignore this parameter if they only support a
     *            specific mailbox
     * @throws MailboxConnectionException
     *             if the connection to the mailbox (folder) failed or was lost
     */
    protected abstract void prepareFetching(String mailbox) throws MailboxConnectionException;

    /**
     * Processes all undeleted messages in a mailbox. Because there are differences when working
     * with imap and pop3 folders (regarding opening and keeping open) subclasses must implement
     * this method.
     *
     * @param worker
     *            the worker processing the messages
     */
    protected abstract void processUndeletedMessages(MailMessageWorker worker);

    /**
     * Tries to set the new checkingMessages state. Setting the state to true can fail if another
     * thread invoked the close method.
     *
     * @param state
     *            the checkingMessages to set
     * @return false if the the close command was called and the state was true
     */
    private synchronized boolean setCheckingMessages(boolean state) {
        if (shouldStop && state) {
            return false;
        }
        this.checkingMessages = state;
        return true;
    }

    /**
     * Starts checking for messages and blocks until another thread calls the {@link #disconnect()}
     * method or an error or exception (e.g. connection closed, server unreachable) occurs.
     *
     * @throws MailboxConnectionException
     *             in case the message check loop was interrupted by an error or exception
     */
    protected abstract void startCheckMessages() throws MailboxConnectionException;

    /**
     * First tries to open the connection to the mail server. If this succeeded a listener is
     * attached to the mailbox and all messages missed while not listening will be processed.
     * Afterwards the check messages loop is entered, which blocks until an exception occurs or
     * another thread stops the loop by calling {@link #disconnect()}.
     *
     * @param worker
     *            the worker that will be invoked to process messages
     *
     * @throws MailboxConnectionException
     *             if an error or exception occurs while connecting or listening
     */
    public void startListening(MailMessageWorker worker) throws MailboxConnectionException {
        connect();
        attachChangeListener(worker);

        // process all mails in mailbox that were retrieved while we were not
        // listening, do this in parallel working mode; also set the checkingMessages state to force
        // wait of close method
        if (setCheckingMessages(true)) {
            worker.startParallelWorkingMode();
            LOG.debug("Start processing undeleted messages.");
            processUndeletedMessages(worker);
            LOG.debug("Processing undeleted messages done.");
            worker.stopParallelWorkingMode();

            if (!isShouldStop()) {
                LOG.debug("Entering check-messages loop.");
                try {
                    startCheckMessages();
                } catch (MailboxConnectionException e) {
                    setCheckingMessages(false);
                    // close everything because MessageException does not result in
                    // disconnected store
                    detachChangeListener();
                    closeConnection();
                    throw e;
                }
            }
            setCheckingMessages(false);
        }
    }

    /**
     * Interrupt the check messages loop that was started in {@link #startCheckMessages()}. This
     * method should only be run from another thread than the one that called
     * {@link #startCheckMessages()}.
     */
    protected abstract void stopCheckMessages();
}
