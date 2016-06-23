package com.communote.server.persistence.common.security;

import java.util.Locale;

import com.communote.server.persistence.common.messages.ResourceBundleManager;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class CommunoteRuntimeException extends RuntimeException {

    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = 4456849579811622153L;

    private final String messageKey;

    private final Object[] arguments;

    /**
     * Constructs a new instance of CommunoteException
     *
     * @param message
     *            of the exception
     */
    public CommunoteRuntimeException(String message) {
        this(message, null);
    }

    /**
     * Constructs a new instance of CommunoteException
     *
     * @param message
     *            of the exception
     * @param throwable
     *            the cause of the exception
     */
    public CommunoteRuntimeException(String message, Throwable throwable) {
        this(message, throwable, "error.unknown");
    }

    /**
     * Constructs a new instance of CommunoteException
     *
     * @param message
     *            of the exception
     * @param throwable
     *            the cause of the exception
     *
     * @param messageKey
     *            A message key to be used for a specific error message.
     * @param arguments
     *            Arguments to be used for the specific error message.
     */
    public CommunoteRuntimeException(String message, Throwable throwable, String messageKey,
            Object... arguments) {
        super(message, throwable);
        this.messageKey = messageKey;
        if (arguments == null) {
            arguments = new Object[0];
        }
        this.arguments = arguments;
    }

    /**
     * @return the arguments
     */
    public Object[] getArguments() {
        return arguments;
    }

    /**
     * Returns the localized message of this exception.
     *
     * @param locale
     *            The locale.
     * @return The localized message of this exception
     */
    public String getLocalizedMessage(Locale locale) {
        return ResourceBundleManager.instance().getText(getMessageKey(), locale, getArguments());
    }

    /**
     * @return the messageKey
     */
    public String getMessageKey() {
        return messageKey;
    }

}
