package com.communote.server.core.user.validation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.communote.common.i18n.LocalizedMessage;
import com.communote.common.i18n.StaticLocalizedMessage;
import com.communote.server.persistence.common.messages.MessageKeyLocalizedMessageBuilder;

/**
 * Exception to be thrown if, for some reason, the status of a user cannot be set to
 * <code>ACTIVE</code>.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
@SuppressWarnings("serial")
public class UserActivationValidationException extends Exception {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(UserActivationValidationException.class);
    private final MessageKeyLocalizedMessageBuilder reasonMessageBuilder;
    private final LocalizedMessage reasonMessage;

    /**
     * Create a new exception with details message and a localizable reason.
     *
     * @param message
     *            a message with details
     * @param reasonMessage
     *            a message describing the reason why the user could not be activated
     */
    public UserActivationValidationException(String message, LocalizedMessage reasonMessage) {
        super(message);
        this.reasonMessageBuilder = null;
        this.reasonMessage = reasonMessage;
    }

    /**
     * Create a new exception with details message and a message key suffix for the reason. This
     * constructor allows creating a more context sensitive user feedback when showing the reason.
     *
     * @param message
     *            a message with details
     * @param messageKeySuffix
     *            suffix to be added to the message key that is passed to {@link #getReason(String)}
     *            to build the actual message key
     * @param messageArguments
     *            the message arguments to be used when localizing the reason
     */
    public UserActivationValidationException(String message, String messageKeySuffix,
            Object... messageArguments) {
        super(message);
        if (messageKeySuffix == null) {
            messageKeySuffix = "unknown";
        }
        this.reasonMessageBuilder = new MessageKeyLocalizedMessageBuilder(messageKeySuffix,
                messageArguments);
        this.reasonMessage = null;
    }

    /**
     * Create a new exception with details message, cause and a localizable reason.
     *
     * @param message
     *            a message with details
     * @param cause
     *            the cause of the exception
     * @param reasonMessage
     *            a message describing the reason why the user could not be activated
     */
    public UserActivationValidationException(String message, Throwable cause,
            LocalizedMessage reasonMessage) {
        super(message, cause);
        this.reasonMessageBuilder = null;
        this.reasonMessage = reasonMessage;
    }

    /**
     * Create a new exception with details message, a cause and a message key suffix for the reason.
     * This constructor allows creating a more context sensitive user feedback when showing the
     * reason.
     *
     * @param message
     *            a message with details
     * @param cause
     *            the cause of the exception
     * @param messageKeySuffix
     *            suffix to be added to the message key that is passed to {@link #getReason(String)}
     *            to build the actual message key
     * @param messageArguments
     *            the message arguments to be used when localizing the reason
     */
    public UserActivationValidationException(String message, Throwable cause,
            String messageKeySuffix, Object... messageArguments) {
        super(message, cause);
        this.reasonMessageBuilder = new MessageKeyLocalizedMessageBuilder(messageKeySuffix,
                messageArguments);
        this.reasonMessage = null;
    }

    /**
     * Get the localizable reason.
     *
     * @param messageKeyPrefix
     *            a string to prepend to the messageKeySuffix that was passed to the constructor to
     *            build the actual message key for the reason. If this exception was constructed
     *            with a {@link LocalizedMessage} this parameter is ignored.
     * @param arguments
     *            additional message arguments that will be prepended to those passed the
     *            constructor. If this exception was constructed with a {@link LocalizedMessage}
     *            this parameter is ignored.
     * @return the localizable message describing the reason of the exception
     */
    public LocalizedMessage getReason(String messageKeyPrefix, Object... arguments) {
        if (reasonMessage != null) {
            return reasonMessage;
        }
        if (reasonMessageBuilder == null) {
            LOGGER.warn("No loclized reason message available");
            return new StaticLocalizedMessage("User activation failed.");
        }
        reasonMessageBuilder.setPrefix(messageKeyPrefix);
        reasonMessageBuilder.prependArguments(arguments);
        return reasonMessageBuilder.build();
    }

}
