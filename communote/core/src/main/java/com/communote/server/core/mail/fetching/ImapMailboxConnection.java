package com.communote.server.core.mail.fetching;

import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.FolderClosedException;
import javax.mail.Message;
import javax.mail.MessagingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.config.ApplicationConfigurationProperties;
import com.communote.server.api.core.config.type.ApplicationPropertyMailfetching;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPMessage;

/**
 * MailboxConnection implementation using the IMAP protocol.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ImapMailboxConnection extends MailboxConnection {

    /**
     * Thread that tries to avoid that the IMAP IDLE connection is dropped because of inactivity
     * (e.g. by the server or a firewall). There are cases where a mailbox connection might not
     * recognize these drops which means that the mail fetcher stops working without notice. To
     * compensate this this thread restarts the IMAP IDLE connection after a configurable timeout.
     *
     * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
     */
    private class ImapIdleKeepAliveSender extends Thread {
        private final long timeout;
        private final AtomicBoolean shouldStop = new AtomicBoolean(false);
        private final AtomicBoolean running = new AtomicBoolean(false);

        /**
         * Creates the thread with the given timeout
         *
         * @param timeout
         *            the timeout to use
         */
        public ImapIdleKeepAliveSender(long timeout) {
            this.timeout = timeout;
            setName("ImapIdleKeepAliveSender");
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void run() {
            running.set(true);
            LOGGER.debug("IMAP IDLE keep alive packet sender started");
            try {
                // only run if supporting idle
                if (supportsIdle) {
                    while (true) {
                        if (shouldStop.get()) {
                            break;
                        }
                        try {
                            // sleep the configured timeout and check the availability of the
                            // connection afterwards, this will interrupt the IMAP IDLE connection
                            Thread.sleep(timeout);
                            LOGGER.debug("Sending IMAP IDLE keep alive packet");
                            folder.getMessageCount();
                        } catch (InterruptedException e) {
                            // ignore
                        } catch (MessagingException e) {
                            // can be ignored because the IMAP IDLE connection is interrupted too.
                            // As soon as IMAP IDLE is reconnecting it will receive the same
                            // exception and stop this thread.
                        }
                    }
                }
            } finally {
                LOGGER.debug("IMAP IDLE keep alive packet sender stopped");
                running.set(false);
            }
        }

        /**
         * Stop the thread. Returns after the thread stopped
         */
        public void stopSending() {
            shouldStop.set(true);
            if (running.get()) {
                this.interrupt();
                LOGGER.debug("Stopping IMAP IDLE keep alive thread");
                while (running.get()) {
                    try {
                        Thread.sleep(250L);
                    } catch (InterruptedException e) {
                        // ignore
                    }
                }
            }
        }
    }

    /**
     * the log.
     */
    private final static Logger LOGGER = LoggerFactory.getLogger(ImapMailboxConnection.class);

    private IMAPFolder folder;

    private Thread fetchingThread;
    private ImapIdleKeepAliveSender imapIdleKeepAliveThread;

    private String protocolName;
    private long imapIdleKeepAliveTimeout;

    /**
     * Whether the mail server supports the IMAP idle extension.
     */
    private boolean supportsIdle;

    /**
     *
     * @param secure
     *            whether to use a secure connection exists or not
     */
    public ImapMailboxConnection(boolean secure) {
        super();

        if (secure) {
            this.protocolName = MailInProtocolType.IMAPS.getName();
        } else {
            this.protocolName = MailInProtocolType.IMAP.getName();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean expungeMessages() {
        boolean success = false;
        if (folder != null) {
            try {
                folder.expunge();
                success = true;
            } catch (MessagingException e) {
                LOGGER.error("Expunge messages failed.", e);
            }
        }
        return success;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Folder getFolder() {
        return this.folder;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getProtocolName() {
        return this.protocolName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getUniqueMessageId(Message message) {
        try {
            return String.valueOf(folder.getUID(message));
        } catch (MessagingException e) {
            LOGGER.error("Getting UID from message failed.", e);
        } catch (NoSuchElementException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized boolean initialize() {
        if (super.initialize()) {
            ApplicationConfigurationProperties applicationConfigProperties = CommunoteRuntime
                    .getInstance().getConfigurationManager()
                    .getApplicationConfigurationProperties();
            // RFC recommends 29 seconds, but we use 25 as default
            imapIdleKeepAliveTimeout = applicationConfigProperties.getProperty(
                    ApplicationPropertyMailfetching.IMAP_IDLE_KEEP_ALIVE_TIMEOUT, 25L * 60000L);
            return true;
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void prepareFetching(String mailbox) throws MailboxConnectionException {
        try {
            this.folder = (IMAPFolder) getMessageStore().getFolder(mailbox);
            if (this.folder != null && this.folder.exists()) {
                if (this.folder.isOpen() && this.folder.getMode() != Folder.READ_WRITE) {
                    this.folder.close(false);
                    this.folder.open(Folder.READ_WRITE);
                } else if (!folder.isOpen()) {
                    this.folder.open(Folder.READ_WRITE);
                }
            } else {
                String errMsg = "Folder " + mailbox + " not found";
                LOGGER.error(errMsg);
                throw new MailboxConnectionException(errMsg, false);
            }
        } catch (MessagingException e) {
            LOGGER.error("Opening IMAP folder failed.", e);
            throw new MailboxConnectionException("Opening IMAP folder failed.", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void processUndeletedMessages(MailMessageWorker worker) {
        if (folder != null) {
            try {
                Message[] messages = folder.getMessages();
                for (Message message : messages) {
                    if (!message.isExpunged() && !message.isSet(Flags.Flag.DELETED)) {
                        worker.processMailMessage(message);
                    }
                    if (message instanceof IMAPMessage) {
                        // kind of ugly, but Java Mail keeps references to the headers until the
                        // folder is closed which can lead to an OOM exception. By invalidating the
                        // headers these references are removed.

                        // TODO maybe we should close the folder as soon as we handled the
                        // unprocessed messages and reopen and attach the AddedListener afterwards?
                        // But would be "blind" for a short while. Could also try to work with
                        // HIGHESTMODSEQ if the server supports it.
                        ((IMAPMessage) message).invalidateHeaders();
                    }
                }

            } catch (MessagingException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void startCheckMessages() throws MailboxConnectionException {
        try {
            supportsIdle = true;
            imapIdleKeepAliveThread = new ImapIdleKeepAliveSender(imapIdleKeepAliveTimeout);
            imapIdleKeepAliveThread.start();
            folder.idle();
        } catch (FolderClosedException e) {
            LOGGER.error("Message fetch loop was unexpectedly interrupted.", e);
            stopImapIdleKeepAliveThread();
            throw new MailboxConnectionException(
                    "Message fetch loop was unexpectedly interrupted.", e, true);
        } catch (MessagingException mex) {
            LOGGER.info("Mail server does not support IMAP IDLE");
            supportsIdle = false;
            stopImapIdleKeepAliveThread();
            fetchingThread = Thread.currentThread();
        }
        while (true) {
            if (isShouldStop()) {
                break;
            }
            try {
                if (supportsIdle) {
                    folder.idle();
                } else {
                    try {
                        Thread.sleep(getFetchTimeout());
                    } catch (InterruptedException e) {
                        if (isShouldStop()) {
                            break;
                        }
                    }
                    if (!folder.isOpen()) {
                        LOGGER.info("IMAP Folder was closed unexpectedly.");
                        // don't just reopen the folder because we would be
                        // missing all messages sent in the meantime
                        throw new MailboxConnectionException(
                                "IMAP Folder was closed unexpectedly.", true);
                    } else {
                        // force the IMAP server to send EXISTS notifications
                        folder.getMessageCount();
                    }
                }
            } catch (MessagingException e) {
                LOGGER.error("Message fetch loop was unexpectedly interrupted.", e);
                stopImapIdleKeepAliveThread();
                // we got this far, thus a reconnect is likely to succeed
                throw new MailboxConnectionException("IMAP Folder was closed unexpectedly.", e,
                        true);
            }
        }
        stopImapIdleKeepAliveThread();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void stopCheckMessages() {
        stopImapIdleKeepAliveThread();
        if (supportsIdle) {
            // interrupt the idle method with a call to an IMAP command
            try {
                // Note: method can block indefinitely if someone installed a firewall that drops
                // all messages sent from the server, because stopping IMAP IDLE requires the server
                // to send some bytes to the client. By now, mail API doesn't provide a way to force
                // this.
                LOGGER.debug("Aborting IMAP IDLE");
                folder.getMessageCount();
            } catch (MessagingException e) {
                LOGGER.error("Exception while stopping IMAP IDLE", e);
            }
        } else {
            if (fetchingThread != null) {
                fetchingThread.interrupt();
            }
        }
    }

    /**
     * Stop the IMAP IDLE keep alive thread
     */
    private synchronized void stopImapIdleKeepAliveThread() {
        if (imapIdleKeepAliveThread != null) {
            imapIdleKeepAliveThread.stopSending();
            imapIdleKeepAliveThread = null;
        }
    }
}
