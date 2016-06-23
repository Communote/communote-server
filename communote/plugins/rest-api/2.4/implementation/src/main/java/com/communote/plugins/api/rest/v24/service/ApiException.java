package com.communote.plugins.api.rest.v24.service;

/**
 * Base exception for all api exceptions
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ApiException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /**
     * @param message
     *            the message to use
     */
    public ApiException(String message) {
        super(message);
    }
}
