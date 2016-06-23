package com.communote.plugins.api.rest.v30.resource.validation;

/**
 * Represents a validation error
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class ParameterValidationError {

    private String source;

    private String messageKey;

    private Object[] parameters;

    /**
     * gets the error source (name of the parameter, that did not pass validation)
     * 
     * @return source of the error.
     */
    public String getSource() {
        return source;
    }

    /**
     * Sets the error source (name of the parameter, that did not pass validation)
     * 
     * @param source
     *            error source
     */
    public void setSource(String source) {
        this.source = source;
    }

    /**
     * @return message key (I18n), that describes the error
     */
    public String getMessageKey() {
        return messageKey;
    }

    /**
     * sets message key (I18n), that describes the error
     * 
     * @param messageKey
     *            message key
     */
    public void setMessageKey(String messageKey) {
        this.messageKey = messageKey;
    }

    /**
     * @return parameters, that might be used in the message
     */
    public Object[] getParameters() {
        return parameters;
    }

    /**
     * @param parameters
     *            that might be used in the message
     */
    public void setParameters(Object... parameters) {
        this.parameters = parameters;
    }

}
