package com.communote.server.api.core.config;

/**
 * Exception to be thrown if the update of configuration settings failed.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class ConfigurationUpdateException extends Exception {

    private static final long serialVersionUID = 1L;
    private final String messageKey;

    /**
     * Constructs a new exception.
     * 
     * @param message
     *            the detail message
     * @param messageKey
     *            a message key referencing a localized message
     */
    public ConfigurationUpdateException(String message, String messageKey) {
        super(message);
        this.messageKey = messageKey;
    }

    /**
     * Key referencing a localized error message.
     * 
     * @return the key of the localized message
     */
    public String getMessageKey() {
        return messageKey;
    }
}