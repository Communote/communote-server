package com.communote.plugins.mq.service.exception;

/**
 * In case the message contains invalid or incomplete json content or invalid or missing attributes.
 * The associated mapper will map the this exception has a bad request one.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class MessageParsingException extends Exception {

    private boolean beSilent;

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /**
     * 
     * @param message
     *            the exception message (for logging, internal purposes)
     * @param beSilent
     *            instead send a general error message.
     */
    public MessageParsingException(String message, boolean beSilent) {
        super(message);
        this.beSilent = beSilent;
    }

    /**
     * 
     * @param message
     *            the exception message (for logging, internal purposes)
     * @param cause
     *            a causing error
     * @param beSilent
     *            instead send a general error message.
     */
    public MessageParsingException(String message,
            Throwable cause, boolean beSilent) {
        super(message, cause);
        this.beSilent = beSilent;

    }

    /**
     * 
     * @return if be silent the error message should not be exposed to external clients. instead
     *         send a general error message.
     */
    public boolean isBeSilent() {
        return beSilent;
    }

}
