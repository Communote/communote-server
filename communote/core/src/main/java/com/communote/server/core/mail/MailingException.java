package com.communote.server.core.mail;


/**
 * Thrown to indicate that there was a problem while sending or trying to send an email.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class MailingException extends RuntimeException {
    /**
     * Constructs a new instance of MailingException
     *
     * @param message
     *            the throwable message.
     */
    public MailingException(String message) {
        super(message);
    }

    /**
     * Constructs a new instance of MailingException
     *
     * @param message
     *            the throwable message.
     * @param throwable
     *            the parent of this Throwable.
     */
    public MailingException(String message, Throwable throwable) {
        super(message, throwable);
    }

}
