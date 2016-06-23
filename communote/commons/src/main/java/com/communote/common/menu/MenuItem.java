package com.communote.common.menu;

import java.util.List;

/**
 * An item of a menu which can have sub-items.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 * @param <T>
 *            the type of the sub-items
 */
public interface MenuItem<T extends MenuEntry> extends MenuEntry {

    /**
     * @return the sub-items of the menu item or null if {@link #hasChildren()} returns false
     */
    List<T> getChildren();

    /**
     * @return whether the menu item contains other (sub-) menu items
     */
    boolean hasChildren();

    /**
     * set the children of this menu item
     *
     * @param children
     *            the sub-items to set
     */
    void setChildren(List<T> children);
}