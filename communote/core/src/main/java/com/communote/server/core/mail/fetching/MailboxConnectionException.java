package com.communote.server.core.mail.fetching;

import java.net.ConnectException;

import javax.mail.MessagingException;

/**
 * An exception that indicates that the connection to the mailbox failed or was lost.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class MailboxConnectionException extends Exception {

    /**
     * default serialVersionUID
     */
    private static final long serialVersionUID = 1L;
    private final boolean reconnectMaySucceed;

    /**
     * Creates a new MailboxConnectionException.
     * 
     * @param details
     *            string describing the details of the exception
     * @param reconnectMaySucceed
     *            whether a reconnect at a later time may succeed. A useful case for setting this to
     *            true is when the server is not reachable because it is temporarily down. If for
     *            instance the authentication failed it is better to pass false because this is
     *            usually a configuration problem.
     */
    public MailboxConnectionException(String details, boolean reconnectMaySucceed) {
        super(details);
        this.reconnectMaySucceed = reconnectMaySucceed;
    }

    /**
     * Creates a new MailboxConnectionException. Whether a reconnect may succeed will be determined
     * by evaluating the provided cause.
     * 
     * @param details
     *            string describing the details of the exception
     * @param cause
     *            the exception that caused this exception
     */
    public MailboxConnectionException(String details, Throwable cause) {
        super(details, cause);
        if (cause instanceof MessagingException) {
            Throwable innerCause = cause.getCause();
            if (innerCause instanceof ConnectException) {
                // usually happens when the mail-server is down (this can be temporary so a
                // reconnect may succeed)
                this.reconnectMaySucceed = true;
            } else {
                this.reconnectMaySucceed = false;
            }
        } else {
            this.reconnectMaySucceed = false;
        }
    }

    /**
     * Creates a new MailboxConnectionException.
     * 
     * @param details
     *            string describing the details of the exception
     * @param cause
     *            the exception that caused this exception
     * @param reconnectMaySucceed
     *            whether a reconnect at a later time may succeed. A useful case for setting this to
     *            true is when the server is not reachable because it is temporarily down. If for
     *            instance the authentication failed it is better to pass false because this is
     *            usually a configuration problem.
     */
    public MailboxConnectionException(String details, Throwable cause, boolean reconnectMaySucceed) {
        super(details, cause);
        this.reconnectMaySucceed = reconnectMaySucceed;
    }

    /**
     * Indicates whether a reconnect may succeed at a later time.
     * 
     * @return true if a reconnect may succeed
     */
    public boolean getReconnectMaySucceed() {
        return reconnectMaySucceed;
    }
}
