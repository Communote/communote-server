package com.communote.plugins.mq.service.exception;

import com.communote.common.i18n.LocalizedMessage;
import com.communote.server.core.exception.Reason;


/**
 * In case the message contains inconsistent or missing values this exception is used.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class MessageValidationException extends Exception {

    private final LocalizedMessage validationMessage;
    private final Reason[] reasons;

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /**
     * 
     * 
     * @param exceptionMessage
     *            the exception message (for logging, internal purposes)
     * @param validationMessage
     *            the validation message to be send back
     * @param reasons
     *            a list of specific reasons
     */
    public MessageValidationException(String exceptionMessage, LocalizedMessage validationMessage,
            Reason... reasons) {
        this(exceptionMessage, validationMessage, null, reasons);
    }

    /**
     * 
     * @param exceptionMessage
     *            the exception message (for logging, internal purposes)
     * @param validationMessage
     *            the validation message to be send back
     * @param cause
     *            a causing error
     * @param reasons
     *            a list of specific reasons
     */
    public MessageValidationException(String exceptionMessage, LocalizedMessage validationMessage,
            Throwable cause, Reason... reasons) {
        super(exceptionMessage, cause);
        this.validationMessage = validationMessage;
        this.reasons = reasons;

    }

    /**
     * 
     * @return specific reasons, can be null or empty
     */
    public Reason[] getReasons() {
        return reasons;
    }

    /**
     * 
     * @return the general validation message
     */
    public LocalizedMessage getValidationMessage() {
        return validationMessage;
    }
}
