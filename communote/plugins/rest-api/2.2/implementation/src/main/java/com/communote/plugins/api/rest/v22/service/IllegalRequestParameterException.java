package com.communote.plugins.api.rest.v22.service;

import org.apache.commons.lang3.StringUtils;

/**
 * Exception for an invalid request parameter value
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class IllegalRequestParameterException extends ApiException {
    private static final long serialVersionUID = 1L;

    private final String parameter;
    private final String value;

    /**
     * Construct the exception
     * 
     * @param parameter
     *            the concerned parameter
     * @param value
     *            the invalid value
     * @param message
     *            An error message
     */
    public IllegalRequestParameterException(String parameter, String value, String message) {
        super(message);
        this.parameter = parameter;
        this.value = value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getMessage() {
        return "Invalid parameter value for " + parameter + " = '" + value + "'! "
                + (super.getMessage() != null ? super.getMessage() : StringUtils.EMPTY);
    }

    /**
     * @return the concerning parameter
     */
    public String getParameter() {
        return parameter;
    }

    /**
     * @return the invalid value causing this exception
     */
    public String getValue() {
        return value;
    }
}
