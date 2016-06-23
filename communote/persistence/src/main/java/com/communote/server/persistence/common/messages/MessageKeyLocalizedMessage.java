package com.communote.server.persistence.common.messages;

import java.util.Locale;

import org.apache.commons.lang3.ArrayUtils;

import com.communote.common.i18n.LocalizedMessage;
import com.communote.server.api.core.i18n.LocalizationChangeObservable;

/**
 * Message that uses the {@link ResourceBundleManager} for localization.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class MessageKeyLocalizedMessage implements LocalizedMessage, LocalizationChangeObservable {

    private String messageKey;
    private Object[] arguments;
    private boolean combineArguments;

    /**
     * Constructor.
     *
     * @param messageKey
     *            The message key to use.
     * @param arguments
     *            Optional arguments for the message.
     */
    public MessageKeyLocalizedMessage(String messageKey, Object... arguments) {
        this.messageKey = messageKey;
        this.arguments = arguments;
    }

    /**
     * @return the arguments
     */
    public Object[] getArguments() {
        return arguments;
    }

    @Override
    public Class<ResourceBundleChangedEvent> getChangeNotificationEvent() {
        return ResourceBundleChangedEvent.class;
    }

    /**
     * @return the messageKey
     */
    public String getMessageKey() {
        return messageKey;
    }

    public boolean isCombineArguments() {
        return combineArguments;
    }

    /**
     * @param arguments
     *            the arguments to set
     */
    public void setArguments(Object[] arguments) {
        this.arguments = arguments;
    }

    /**
     * Whether to combine the the arguments passed to toString with those set via setArguments.
     *
     * @param combineArguments
     *            true to append the arguments, which were passed to toString, to the arguments set
     *            with setArguments.
     */
    public void setCombineArguments(boolean combineArguments) {
        this.combineArguments = combineArguments;
    }

    /**
     * @param messageKey
     *            the messageKey to set
     */
    public void setMessageKey(String messageKey) {
        this.messageKey = messageKey;
    }

    /**
     * @return messageKey + " " + arguments
     */
    @Override
    public String toString() {
        return messageKey + " " + arguments;
    }

    /**
     * @param locale
     *            The locale to be used.
     * @param arguments
     *            Set this to overwrite possible default arguments.
     * @return The localized message from the message key.
     */
    @Override
    public String toString(Locale locale, Object... arguments) {
        // if no arguments are passed it is an empty array: Object[0]
        if (arguments != null && arguments.length > 0) {
            if (combineArguments) {
                return ResourceBundleManager.instance().getText(messageKey, locale,
                        ArrayUtils.add(this.arguments, arguments));
            }
            return ResourceBundleManager.instance().getText(messageKey, locale, arguments);
        }
        return ResourceBundleManager.instance().getText(messageKey, locale, this.arguments);
    }
}
