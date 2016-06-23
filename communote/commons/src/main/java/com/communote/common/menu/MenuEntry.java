package com.communote.common.menu;

import java.util.Locale;

/**
 * A generic entry of a menu.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public interface MenuEntry {

    /**
     * @return a unique ID of the item within the menu
     */
    String getId();

    /**
     * @return the label of a menu item. Implementors can decide what to return here, this could for
     *         instance be a static string, a localized string in a default language or maybe null
     *         if unlocalized strings are not supported.
     */
    String getLabel();

    /**
     * Return the localized label of the menu item. The return value should be the same as that of
     * {@link #getLabel()} if localization is not supported.
     *
     * @param locale
     *            the locale to use
     * @param arguments
     *            any arguments to pass to the i18n function
     * @return the localized label
     */
    String getLocalizedLabel(Locale locale, Object... arguments);

}