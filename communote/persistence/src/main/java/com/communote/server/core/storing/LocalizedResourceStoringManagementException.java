package com.communote.server.core.storing;

import java.util.Locale;

import com.communote.common.i18n.LocalizedMessage;
import com.communote.server.persistence.common.messages.MessageKeyLocalizedMessage;


/**
 * Exception containing the error as {@link LocalizedMessage}.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class LocalizedResourceStoringManagementException extends ResourceStoringManagementException {

    private static final long serialVersionUID = -2936897729881501400L;
    private final LocalizedMessage message;

    /**
     * Constructor.
     * 
     * @param message
     *            The message.
     */
    public LocalizedResourceStoringManagementException(LocalizedMessage message) {
        this(message, null);
    }

    /**
     * Constructor.
     * 
     * @param message
     *            The message.
     * @param cause
     *            The cause of the exception.
     */
    public LocalizedResourceStoringManagementException(LocalizedMessage message, Throwable cause) {
        super(message.toString(Locale.ENGLISH), cause);
        this.message = message;
    }

    /**
     * Constructor.
     * 
     * @param messageKey
     *            Message key of the message.
     * @param arguments
     *            Arguments of the message.
     */
    public LocalizedResourceStoringManagementException(String messageKey, Object... arguments) {
        this(new MessageKeyLocalizedMessage(messageKey, arguments));
    }

    /**
     * Returns a localized message of this exception.
     * 
     * @param locale
     *            The locale.
     * @param arguments
     *            The arguments.
     * @return The message as string.
     */
    public String getLocalizedMessage(Locale locale, Object... arguments) {
        return message.toString(locale, arguments);
    }

}
