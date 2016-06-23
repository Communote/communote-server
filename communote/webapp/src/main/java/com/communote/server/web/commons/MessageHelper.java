package com.communote.server.web.commons;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import com.communote.common.i18n.LocalizedMessage;
import com.communote.server.core.common.session.SessionHandler;
import com.communote.server.persistence.common.messages.ResourceBundleManager;

/**
 * The {@link MessageHelper} provides functions to access messages resources and to save messages in
 * the request as either error or success messages.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class MessageHelper {
    /**
     * The request key to be used for messages
     */
    public static final String MESSAGES_KEY = "successMessages";

    /** The request key to be used for info messages. */
    public static final String INFO_MESSAGES_KEY = "infoMessages";
    /**
     * The request key to be used for error messages.
     */
    public static final String ERROR_MESSAGES_KEY = "errorMessages";
    /**
     * The request key to be used for warnings.
     */
    public static final String WARNING_MESSAGES_KEY = "warningMessages";

    /** the request key to be sued for the message target. */
    public static final String MESSAGES_TARGET = "messagesTarget";

    private static SessionHandler SESSION_HANDLER = SessionHandler.instance();

    /**
     * Request attribute name for storing the current client ID. This attribute should be set if the
     * ThreadLocal that holds the client is not available anymore or cannot be set (e.g. if the
     * client is not active).
     */
    public static final String CLIENT_ID_REQUEST_ATTRIBUTE = "currentClientId";

    /**
     * Get the localized value of the LocalizedMessage
     *
     * @param request
     *            the request to determine the locale
     * @param localizedMessage
     *            the message providing the transaltion
     * @param arguments
     *            if provided overrides the arguments the LocalizedMessage was created with
     * @return the translated message
     */
    public static String getText(HttpServletRequest request, LocalizedMessage localizedMessage,
            Object... arguments) {
        Locale locale = SESSION_HANDLER.getCurrentLocale(request);
        return localizedMessage.toString(locale, arguments);
    }

    /**
     * Convenience method for getting a i18n key's value.
     *
     * @param request
     *            the request to determine the locale
     * @param msgKey
     *            The message key
     * @return The localized text
     */
    public static String getText(HttpServletRequest request, String msgKey) {
        Locale locale = SESSION_HANDLER.getCurrentLocale(request);
        return ResourceBundleManager.instance().getText(msgKey, locale);
    }

    /**
     * Method for getting a i18n key's value.
     *
     * @param request
     *            the request to determine the locale
     * @param msgKey
     *            The message key
     * @param args
     *            message arguments
     * @return The localized text
     */
    public static String getText(HttpServletRequest request, String msgKey, Object[] args) {
        Locale locale = SESSION_HANDLER.getCurrentLocale(request);
        return ResourceBundleManager.instance().getText(msgKey, locale, args);
    }

    /**
     * @param request
     *            The request.
     * @param msgKey
     *            The message key.
     * @param arguments
     *            Possible arguments.
     * @return True, if there is such a message key, else false.
     */
    public static boolean hasText(HttpServletRequest request, String msgKey, Object[] arguments) {
        Locale locale = SESSION_HANDLER.getCurrentLocale(request);
        return ResourceBundleManager.instance().knowsMessageKey(msgKey, arguments, locale);
    }

    /**
     * Saves a new message in the request under the key {@link MessageHelper#ERROR_MESSAGES_KEY} as
     * error message
     *
     * @param request
     *            The request to save the messages to
     * @param message
     *            the {@link LocalizedMessage} containing the translated message
     */
    public static void saveErrorMessage(HttpServletRequest request, LocalizedMessage message) {
        saveMessage(request, getText(request, message), ERROR_MESSAGES_KEY);
    }

    /**
     * Saves a new message in the request under the key {@link MessageHelper#ERROR_MESSAGES_KEY} as
     * error message
     *
     * @param request
     *            The request to save the messages to
     * @param message
     *            the message text to save
     */
    public static void saveErrorMessage(HttpServletRequest request, String message) {
        saveMessage(request, message, ERROR_MESSAGES_KEY);
    }

    /**
     * Saves a new message in the request under the key {@link MessageHelper#ERROR_MESSAGES_KEY} as
     * error message
     *
     * @param request
     *            The request to save the messages to
     * @param messageKey
     *            The key of the message.
     */
    public static void saveErrorMessageFromKey(HttpServletRequest request, String messageKey) {
        saveErrorMessage(request, getText(request, messageKey));
    }

    /**
     * Saves a new message in the request under the key {@link MessageHelper#ERROR_MESSAGES_KEY} as
     * error message
     *
     * @param request
     *            The request to save the messages to
     * @param messageKey
     *            The key of the message.
     * @param arguments
     *            Message arguments.
     */
    public static void saveErrorMessageFromKey(HttpServletRequest request, String messageKey,
            Object... arguments) {
        saveErrorMessage(request, getText(request, messageKey, arguments));
    }

    /**
     * Saves a new message in the request under the key {@link MessageHelper#MESSAGES_KEY} as
     * success message
     *
     * @param request
     *            The request to save the messages to
     * @param message
     *            the message text to save
     */
    public static void saveMessage(HttpServletRequest request, LocalizedMessage message) {
        saveMessage(request, getText(request, message), MESSAGES_KEY);
    }

    /**
     * Saves a new message in the request under the key {@link MessageHelper#MESSAGES_KEY} as
     * success message
     *
     * @param request
     *            The request to save the messages to
     * @param message
     *            the message text to save
     */
    public static void saveMessage(HttpServletRequest request, String message) {
        saveMessage(request, message, MESSAGES_KEY);
    }

    /**
     * Saves a message with the specified key.
     *
     * @param request
     *            The request.
     * @param message
     *            The message.
     * @param type
     *            The message type (Error, Success, ...).
     */
    // TODO Replace type through an enumeration.
    public static void saveMessage(HttpServletRequest request, String message, String type) {
        List<String> messages = (List<String>) request.getAttribute(type);

        if (messages == null) {
            messages = new ArrayList<String>(2);
        }
        messages.add(message);
        request.setAttribute(type, messages);
    }

    /**
     * Saves a new message in the request under the key {@link MessageHelper#MESSAGES_KEY} as
     * success message
     *
     * @param request
     *            The request to save the messages to
     * @param messageKey
     *            The key of the message.
     */

    public static void saveMessageFromKey(HttpServletRequest request, String messageKey) {
        saveMessage(request, getText(request, messageKey));
    }

    /**
     * Saves a new message in the request under the key {@link MessageHelper#MESSAGES_KEY} as
     * success message
     *
     * @param request
     *            The request to save the messages to
     * @param messageKey
     *            The key of the message.
     * @param type
     *            The messages type.
     */

    public static void saveMessageFromKey(HttpServletRequest request, String messageKey, String type) {
        saveMessage(request, getText(request, messageKey), type);
    }

    /**
     * Saves a new message in the request under the key {@link MessageHelper#MESSAGES_KEY} as
     * success message
     *
     * @param request
     *            The request to save the messages to
     * @param messageKey
     *            The key of the message.
     * @param type
     *            The messages type.
     * @param arguments
     *            Arguments.
     */

    public static void saveMessageFromKey(HttpServletRequest request, String messageKey,
            String type, Object... arguments) {
        saveMessage(request, getText(request, messageKey, arguments), type);
    }

    /**
     * Sets the message view target.
     *
     * @param request
     *            the request
     * @param url
     *            the url
     */
    public static void setMessageTarget(HttpServletRequest request, String url) {
        request.getSession().setAttribute(MESSAGES_TARGET, url);
    }

    /**
     * Do not use me this way
     */
    private MessageHelper() {

    }
}
