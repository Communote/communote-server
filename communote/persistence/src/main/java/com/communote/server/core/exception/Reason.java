package com.communote.server.core.exception;

import com.communote.common.i18n.LocalizedMessage;
import com.communote.server.persistence.common.messages.MessageKeyLocalizedMessage;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class Reason {

    private final LocalizedMessage errorMessage;
    private final String errorField;
    private final String errorCause;

    /**
     * Constructor.
     * 
     * @param errorMessage
     *            Message of the error.
     * @param errorField
     *            Field the error occurred.
     * @param errorCause
     *            The cause of the error.
     */
    public Reason(LocalizedMessage errorMessage, String errorField, String errorCause) {
        this.errorMessage = errorMessage;
        this.errorField = errorField;
        this.errorCause = errorCause;
    }

    /**
     * Constructor.
     * 
     * @param messageKey
     *            Message key of the error.
     * @param errorField
     *            Field the error occurred.
     * @param errorCause
     *            The cause of the error.
     * @param messageArguments
     *            Optional arguments for the message key.
     */
    public Reason(String messageKey, String errorField, String errorCause,
            Object... messageArguments) {
        this(new MessageKeyLocalizedMessage(messageKey, messageArguments), errorField, errorCause);
    }

    /**
     * @return the errorCause
     */
    public String getErrorCause() {
        return errorCause;
    }

    /**
     * @return the errorField
     */
    public String getErrorField() {
        return errorField;
    }

    /**
     * @return the errorMessage
     */
    public LocalizedMessage getErrorMessage() {
        return errorMessage;
    }
}
