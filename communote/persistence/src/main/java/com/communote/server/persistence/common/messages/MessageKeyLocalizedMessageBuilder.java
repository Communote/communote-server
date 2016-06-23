package com.communote.server.persistence.common.messages;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

/**
 * Helper to build a {@link MessageKeyLocalizedMessage} from message key pre- and suffixes.
 * <p>
 * Note: this implementation is not thread-safe.
 * </p>
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class MessageKeyLocalizedMessageBuilder {

    private final String messageKeyBase;
    private String messageKeyPrefix;
    private String messageKeySuffix;
    private List<Object> arguments;

    /**
     * Create a new builder.
     *
     * @param messageKeyBase
     *            the base message key string that can be extended with a prefix and a suffix
     * @param arguments
     *            the message arguments
     */
    public MessageKeyLocalizedMessageBuilder(String messageKeyBase, Object... arguments) {
        this.messageKeyBase = messageKeyBase;
        if (arguments != null && arguments.length > 0) {
            this.arguments = Arrays.asList(arguments);
        } else {
            // not using Collections.emptyList because the returned list cannot be modified
            this.arguments = new ArrayList<>();
        }
    }

    /**
     * Append the provided arguments to those passed to the constructor or previous calls to this
     * method.
     *
     * @param arguments
     *            the message arguments to append
     */
    public void appendArguments(Object... arguments) {
        if (arguments != null && arguments.length > 0) {
            Collections.addAll(this.arguments, arguments);
        }
    }

    public MessageKeyLocalizedMessage build() {
        String messageKey = messageKeyPrefix != null ? messageKeyPrefix : StringUtils.EMPTY;
        if (messageKeyBase != null) {
            messageKey += messageKeyBase;
        }
        if (messageKeySuffix != null) {
            messageKey += messageKeySuffix;
        }
        return new MessageKeyLocalizedMessage(messageKey, arguments.toArray());
    }

    /**
     * Prepend the provided arguments to those passed to the constructor or previous calls to this
     * method.
     *
     * @param arguments
     *            the message arguments to prepend
     */
    public void prependArguments(Object... arguments) {
        if (arguments != null && arguments.length > 0) {
            this.arguments.addAll(0, Arrays.asList(arguments));
        }
    }

    /**
     * Prepend the provided string to the messageKeyBase that was passed to the constructor.
     *
     * @param messageKeyPrefix
     *            the prefix to prepend
     */
    public void setPrefix(String messageKeyPrefix) {
        this.messageKeyPrefix = messageKeyPrefix;
    }

    /**
     * Append the provided string to the messageKeyBase that was passed to the constructor.
     *
     * @param messageKeySuffix
     *            the suffix to append
     */
    public void setSuffix(String messageKeySuffix) {
        this.messageKeySuffix = messageKeySuffix;
    }
}
