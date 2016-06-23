package com.communote.common.i18n;

import java.util.Locale;

/**
 * Static message with always the same text.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class StaticLocalizedMessage implements LocalizedMessage {

    private final String message;

    /**
     * Constructor.
     *
     * @param message
     *            The message.
     */
    public StaticLocalizedMessage(String message) {
        this.message = message;
    }

    /**
     * Always returns the value of message.
     *
     * @param locale
     *            Ignored.
     * @param arguments
     *            Ignored.
     * @return The value of message.
     */
    @Override
    public String toString(Locale locale, Object... arguments) {
        return message;
    }

}
