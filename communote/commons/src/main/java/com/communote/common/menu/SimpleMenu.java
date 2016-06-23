package com.communote.common.menu;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple menu consisting of two levels. The items of the first level are called categories and
 * can have sub-items. Those sub-items form the second level and can only contain items which do not
 * have sub-items.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * @param <C>
 *            type of the categories
 * @param <S>
 *            type of the sub-items
 */
public class SimpleMenu<C extends MenuEntry, S extends MenuEntry> {
    private List<CategoryEntry<C, S>> items;

    /**
     * Create an menu with an empty set of categories
     */
    public SimpleMenu() {
        this.items = new ArrayList<>();
    }

    /**
     * Create a menu with the given set of categories
     *
     * @param categories
     *            the categories of the menu
     */
    public SimpleMenu(List<CategoryEntry<C, S>> categories) {
        this.items = categories;
    }

    /**
     * @return category items of the menu
     */
    public List<CategoryEntry<C, S>> getCategories() {
        return this.items;
    }

    /**
     * @return whether there are category items in the menu
     */
    public boolean hasCategories() {
        return this.items != null && this.items.size() > 0;
    }

    /**
     * Set the category items of the menu
     *
     * @param categories
     *            the category items
     */
    public void setItems(List<CategoryEntry<C, S>> categories) {
        this.items = categories;
    }
}
