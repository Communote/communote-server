package com.communote.common.i18n;

import java.util.Locale;

/**
 * Interface to be used for specifying more complex messages.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public interface LocalizedMessage {

    /**
     * Returns the localized message.
     *
     * @param locale
     *            The locale for the message.
     * @param arguments
     *            Can be used, if some arguments might be set or to overwrite already given
     *            arguments.
     * @return The message.
     */
    String toString(Locale locale, Object... arguments);
}
