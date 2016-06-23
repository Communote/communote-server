package com.communote.server.core.mail.fetching;

import javax.mail.Flags;
import javax.mail.Message;
import javax.mail.MessageRemovedException;
import javax.mail.MessagingException;
import javax.mail.event.MessageCountAdapter;
import javax.mail.event.MessageCountEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.mail.imap.IMAPMessage;

/**
 * A listener which listens for message count events and processes the mails added.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class MessagesAddedListener extends MessageCountAdapter {

    /**
     * the log.
     */
    private final static Logger LOG = LoggerFactory.getLogger(MessagesAddedListener.class);

    private final MailMessageWorker worker;
    private boolean active;
    private boolean shouldStop;

    public MessagesAddedListener(MailMessageWorker worker) {
        this.worker = worker;
        this.active = false;
        this.shouldStop = false;
    }

    private synchronized boolean isActive() {
        return active;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void messagesAdded(MessageCountEvent ev) {
        Message[] messages = ev.getMessages();
        for (Message message : messages) {
            if (setActive(true)) {
                // ignore all expunged messages, because in idle mode these
                // messages are reported from time to time (bug of mailer or
                // java mail?)
                // the isExpunged check does not seem to work properly, thus we
                // test for the deleted flag which will throw a
                // MessageRemovedException if the message is expunged
                try {
                    if (!message.isExpunged() && !message.isSet(Flags.Flag.DELETED)) {
                        worker.processMailMessage(message);
                    }
                    if (message instanceof IMAPMessage) {
                        // kind of ugly, but required to avoid memory leak, see
                        // ImapMailboxConnection for details
                        ((IMAPMessage) message).invalidateHeaders();
                    }
                } catch (MessageRemovedException e) {
                    LOG.debug("Received an expunged message");
                } catch (MessagingException e) {
                    LOG.error("Error occured in check for deleted flag.", e);
                }
                setActive(false);
            }
        }
    }

    private synchronized boolean setActive(boolean state) {
        if (!state) {
            this.active = false;
            return true;
        } else {
            if (shouldStop) {
                return false;
            }
            this.active = true;
            return true;
        }
    }

    /**
     * Stops the listener and its worker. Returns as soon as the worker has finished processing the
     * message it is currently working on.
     */
    public void stop() {
        synchronized (this) {
            shouldStop = true;
        }
        while (isActive()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {

            }
        }
    }
}
