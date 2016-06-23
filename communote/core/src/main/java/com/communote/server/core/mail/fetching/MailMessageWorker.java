package com.communote.server.core.mail.fetching;

import java.util.HashSet;
import java.util.Set;

import javax.mail.Message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A worker which is used to process a mail message. To process mail messages of the same mailbox
 * from different threads in parallel one should enable the parallel working mode which takes care
 * of not processing the same message twice. This mode should be disabled when its no longer needed.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public abstract class MailMessageWorker {

    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(MailMessageWorker.class);

    private boolean parallelWorkingMode;
    private Set<String> processedMessages;

    /**
     * Subclasses should override this method to implement their specific message processing.
     * 
     * @param message
     *            The message.
     */
    protected abstract void handleProcessMailMessage(Message message);

    /**
     * Returns whether a mail message was already or is currently processed. Returns always false if
     * the worker is not in parallel working mode.
     * 
     * @param message
     *            the message
     * @return true if the worker is in parallel working mode and a mail is or was processed, false
     *         otherwise
     */
    private synchronized boolean mailMessageProcessed(Message message) {
        if (!parallelWorkingMode) {
            return false;
        }
        String messageId = MailFetcher.instance().getUniqueMessageId(message);
        return !processedMessages.add(messageId);
    }

    /**
     * Starts processing of a mail message.
     * 
     * @param message
     *            The message.
     */
    public void processMailMessage(Message message) {
        if (!mailMessageProcessed(message)) {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Start processing message with id "
                        + MailFetcher.instance().getUniqueMessageId(message));
            }
            handleProcessMailMessage(message);
        }
    }

    /**
     * Starts the parallel working mode.
     */
    public synchronized void startParallelWorkingMode() {
        if (!parallelWorkingMode) {
            parallelWorkingMode = true;
            if (processedMessages == null) {
                processedMessages = new HashSet<String>();
            }
        }
    }

    /**
     * Stops the parallel working mode.
     */
    public synchronized void stopParallelWorkingMode() {
        if (parallelWorkingMode) {
            parallelWorkingMode = false;
            processedMessages.clear();
        }

    }
}
