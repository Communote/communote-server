package com.communote.plugins.api.rest.v30.resource.validation;

import java.util.ArrayList;
import java.util.List;

/**
 * Defines exception to be thrown in the case of validation problems. Includes a list of errors,
 * describing each concrete error, that occurred
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class ParameterValidationException extends Exception {

    private static final long serialVersionUID = -7054522734145034685L;

    private String messageKey;

    private List<ParameterValidationError> errors;

    /**
     * @param error
     *            adds an error the the list of errors, that caused this exception
     */
    public void addError(ParameterValidationError error) {
        if (this.errors == null) {
            this.errors = new ArrayList<ParameterValidationError>();
        }
        this.errors.add(error);
    }

    /**
     * @return list of errors, that caused this exception
     */
    public List<ParameterValidationError> getErrors() {
        return errors;
    }

    /**
     * @return message key (I18n) for the occurred exception
     */
    public String getMessageKey() {
        if (messageKey != null) {
            return messageKey;
        } else {
            return "error.validation";
        }
    }

    /**
     * @param errors
     *            list of errors, that caused this exception
     */
    public void setErrors(List<ParameterValidationError> errors) {
        this.errors = errors;
    }

    /**
     * @param messageKey
     *            message key (I18n) for the occurred exception
     */
    public void setMessageKey(String messageKey) {
        this.messageKey = messageKey;
    }

}
