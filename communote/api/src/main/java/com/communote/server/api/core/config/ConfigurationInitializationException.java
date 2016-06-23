package com.communote.server.api.core.config;

/**
 * Exception to be thrown if the initialization of the configuration settings failed.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class ConfigurationInitializationException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new exception.
     * 
     * @param message
     *            the detail message
     */
    public ConfigurationInitializationException(String message) {
        super(message);
    }

    /**
     * Constructs a new exception wrapping the cause.
     * 
     * @param message
     *            the detail message
     * @param t
     *            the cause
     */
    public ConfigurationInitializationException(String message, Throwable t) {
        super(message, t);
    }

}