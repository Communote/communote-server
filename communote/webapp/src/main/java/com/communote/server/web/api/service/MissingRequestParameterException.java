package com.communote.server.web.api.service;

import org.apache.commons.lang.StringUtils;

/**
 * Thrown to indicate that a required request parameter is missing.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class MissingRequestParameterException extends ApiException {

    /**
     * default serial version UID
     */
    private static final long serialVersionUID = 1L;
    private final String[] parameters;

    /**
     * Creates a new exception.
     * 
     * @param parameter
     *            the required parameter
     * @param message
     *            a message with details
     */
    public MissingRequestParameterException(String parameter, String message) {
        this(new String[] { parameter }, message);
    }

    /**
     * Creates a new exception.
     * 
     * @param parameters
     *            an array of parameters of which at least one is required
     * @param message
     *            a message with details
     */
    public MissingRequestParameterException(String[] parameters, String message) {
        super(message);
        this.parameters = parameters;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getMessage() {
        String msg;
        if (parameters != null || parameters.length == 0) {
            if (parameters.length == 1) {
                msg = "Parameter " + parameters[0] + " is required! ";
            } else {
                msg = "At least one of the parameters " + StringUtils.join(parameters, ", ")
                        + " must be provided! ";
            }
        } else {
            msg = StringUtils.EMPTY;
        }
        return msg + (super.getMessage() != null ? super.getMessage() : StringUtils.EMPTY);
    }
}
