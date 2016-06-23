package com.communote.common.menu;

/**
 * A filter to hide menu entries which should not be shown in certain situations.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 * @param <T>
 *            the type of the menu entry this filter handles
 */
public interface MenuEntryFilter<T extends MenuEntry> {

    /**
     * Whether the menu entry should be included in the menu
     *
     * @param entry
     *            the entry to test
     * @return true if the entry should be included, false otherwise
     */
    boolean include(T entry);
}
