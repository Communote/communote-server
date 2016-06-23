package com.communote.server.core.exception;

import com.communote.common.i18n.LocalizedMessage;
import com.communote.server.persistence.common.messages.MessageKeyLocalizedMessage;

/**
 * Status which describes the result of an exception mapper.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class Status {

    private final LocalizedMessage message;
    private final String errorCode;
    private final Reason[] errors;

    /**
     * Constructor.
     * 
     * @param message
     *            The message for this status.
     * @param errorCode
     *            The error code.
     * @param errors
     *            Possible errors.
     */
    public Status(LocalizedMessage message, String errorCode, Reason... errors) {
        this.message = message;
        this.errorCode = errorCode;
        this.errors = errors == null ? new Reason[0] : errors;
    }

    /**
     * Constructor.
     * 
     * @param messageKey
     *            Message key for the status.
     * @param arguments
     *            Optional arguments for message. Can be null.
     * @param errorCode
     *            The error code.
     * @param errors
     *            Possible errors.
     */
    public Status(String messageKey, Object[] arguments, String errorCode, Reason... errors) {
        this(new MessageKeyLocalizedMessage(messageKey, arguments), errorCode, errors);
    }

    /**
     * Constructor.
     * 
     * @param messageKey
     *            Message key for the status.
     * @param errorCode
     *            The error code.
     * @param errors
     *            Possible errors.
     */
    public Status(String messageKey, String errorCode, Reason... errors) {
        this(messageKey, new Object[0], errorCode, errors);
    }

    /**
     * @return the errorCode
     */
    public String getErrorCode() {
        return errorCode;
    }

    /**
     * @return the errors. Returns never null.
     */
    public Reason[] getErrors() {
        return errors;
    }

    /**
     * @return the message
     */
    public LocalizedMessage getMessage() {
        return message;
    }
}
