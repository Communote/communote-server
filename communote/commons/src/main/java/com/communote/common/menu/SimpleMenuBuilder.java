package com.communote.common.menu;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Builder to create a {@link SimpleMenu} with entries that are sorted according to a declarative
 * defined position. This builder also supports a filtering mechanism to dynamically decide whether
 * a certain category or category entry should be included in the menu.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 * @param <C>
 *            type of the categories
 * @param <S>
 *            type of the sub-items
 */
public class SimpleMenuBuilder<C extends MenuEntry, S extends MenuEntry> {

    private final SubmenuBuilder<C> categoriesBuilder = new SubmenuBuilder<C>();
    private final HashMap<String, SubmenuBuilder<S>> categoryContentBuilders = new HashMap<>();
    private List<MenuEntryFilter<C>> categoryFilters = new ArrayList<>();
    private List<MenuEntryFilter<S>> categoryEntryFilters = new ArrayList<>();
    private SimpleMenu<C, S> cachedMenu;

    /**
     * Add a new category to the first level of the menu. If there is already a category with that
     * ID the new one is ignored.
     * <p>
     * If called several times with position type <code>TOP</code> the categories which were added
     * later will be positioned before those added earlier. If called several times with position
     * type <code>BOTTOM</code> or <code>AFTER</code> (and the same menu entry ID to position
     * after), the categories which were added later will be positioned after those added earlier.
     * Entries with position type <code>TOP</code> are always before the <code>BOTTOM</code>
     * positioned categories. Categories with position type <code>AFTER</code> will not appear in
     * the resulting menu if the referenced category does not exist. However, if that referenced
     * category is added later on, the categories to position after that category will than be
     * contained in the menu.
     * </p>
     *
     * @param item
     *            the item to add
     * @param positionDescriptor
     *            descriptor defining where the item should be added. If null the category will be
     *            added to the bottom.
     * @return whether the category was added or there was already a category with the same ID
     */
    public boolean addCategory(C item, PositionDescriptor positionDescriptor) {
        if (categoriesBuilder.addEntry(item, positionDescriptor)) {
            menuContentChanged();
            return true;
        }
        return false;
    }

    private void addCategoryContent(CategoryEntry<C, S> categoryEntry) {
        SubmenuBuilder<S> categoryContentBuilder = categoryContentBuilders.get(categoryEntry
                .getId());
        if (categoryContentBuilder != null) {
            List<S> submenu = categoryContentBuilder.buildSubmenu();
            for (S menuEntry : submenu) {
                if (filterEntry(menuEntry)) {
                    categoryEntry.getChildren().add(menuEntry);
                }
            }
        }
    }

    /**
     * Add a filter that will be called when the menu is built to decide whether a menu entry should
     * be added to its category.
     *
     * @param filter
     *            the filter to add
     */
    public void addCategoryEntryFilter(MenuEntryFilter<S> filter) {
        List<MenuEntryFilter<S>> newFilters = new ArrayList<>(categoryEntryFilters);
        newFilters.add(filter);
        categoryEntryFilters = newFilters;
    }

    /**
     * Add a filter that will be called when the menu is built to decide whether a category should
     * be included in the menu.
     *
     * @param filter
     *            the filter to add
     */
    public void addCategoryFilter(MenuEntryFilter<C> filter) {
        List<MenuEntryFilter<C>> newFilters = new ArrayList<>(categoryFilters);
        newFilters.add(filter);
        categoryFilters = newFilters;
    }

    /**
     * Add a menu item to a category. If the category does not exist the item won't be contained in
     * the menu. But if the category is added later on this item will be added automatically to the
     * category.
     * <p>
     * If called several times with position type <code>TOP</code> the entries which were added
     * later will be positioned before those added earlier. If called several times with position
     * type <code>BOTTOM</code> or <code>AFTER</code> (and the same menu entry ID to position
     * after), the entries which were added later will be positioned after those added earlier.
     * Entries with position type <code>TOP</code> are always before the <code>BOTTOM</code>
     * positioned entries. Entries with position type <code>AFTER</code> will not appear in the
     * resulting menu if the referenced menu entry does not exist. However, if that referenced entry
     * is added later on, the entries to position after that entry will than be contained in the
     * category.
     * </p>
     *
     *
     * @param categoryId
     *            the ID of the category
     * @param item
     *            the item to add
     * @param positionDescriptor
     *            descriptor defining where the item should be added. If null the item will be added
     *            to the bottom
     * @return whether the entry was added or there was already an entry with the same ID
     */
    public boolean addToCategory(String categoryId, S item, PositionDescriptor positionDescriptor) {
        if (getCategoryContentBuilder(categoryId).addEntry(item, positionDescriptor)) {
            menuContentChanged();
            return true;
        }
        return false;
    }

    /**
     * Create the final menu. Should always be called after adding/removing categories or menu items
     * because the children of a category are not directly updated.
     *
     * @return the created menu
     */
    public SimpleMenu<C, S> buildMenu() {
        SimpleMenu<C, S> resultingMenu = cachedMenu;
        boolean cachable = isCachable();
        if (resultingMenu == null || !cachable) {
            resultingMenu = new SimpleMenu<>();
            List<C> orderedCategories = this.categoriesBuilder.buildSubmenu();
            for (C category : orderedCategories) {
                if (filterCategory(category)) {
                    CategoryEntry<C, S> categoryEntry = new CategoryEntry<>(category);
                    addCategoryContent(categoryEntry);
                    resultingMenu.getCategories().add(categoryEntry);
                }
            }
            if (cachable) {
                cachedMenu = resultingMenu;
            } else {
                cachedMenu = null;
            }
        }
        return resultingMenu;
    }

    /**
     * Apply the registered category filters to the category and return whether it should be added
     * to the menu
     *
     * @param category
     *            the category to test
     * @return true if the category should be included false otherwise
     */
    protected boolean filterCategory(C category) {
        for (MenuEntryFilter<C> filter : categoryFilters) {
            if (!filter.include(category)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Apply the registered filters to the entry and return whether it should be added to the
     * category.
     *
     * @param entry
     *            the entry to test
     * @return true if the entry should be included false otherwise
     */
    protected boolean filterEntry(S entry) {
        for (MenuEntryFilter<S> filter : categoryEntryFilters) {
            if (!filter.include(entry)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Get a category
     *
     * @param categoryId
     *            the ID of the category
     * @return the category or null if there is no category for the given ID
     */
    public C getCategory(String categoryId) {
        return categoriesBuilder.getEntry(categoryId);
    }

    private synchronized SubmenuBuilder<S> getCategoryContentBuilder(String categoryId) {
        SubmenuBuilder<S> submenuBuilder = categoryContentBuilders.get(categoryId);
        if (submenuBuilder == null) {
            submenuBuilder = new SubmenuBuilder<S>();
            categoryContentBuilders.put(categoryId, submenuBuilder);
        }
        return submenuBuilder;
    }

    /**
     * Get a category entry. This will even return an entry if the category of the entry was
     * removed.
     *
     * @param categoryId
     *            the ID of the category
     * @param entryId
     *            the ID of the entry in the category
     * @return the entry or null if the entry was not added to a category with the given ID
     */
    public S getCategoryEntry(String categoryId, String entryId) {
        SubmenuBuilder<S> submenuBuilder = categoryContentBuilders.get(categoryId);
        if (submenuBuilder != null) {
            return submenuBuilder.getEntry(entryId);
        }
        return null;
    }

    /**
     * Denotes whether the built menu can be cached and should only be recreated if the menu content
     * changed.
     * <p>
     * This implementation assumes that registered filters lead to a menu that is not cacheable.
     * </p>
     *
     * @return true if the menu can be cached, false otherwise
     */
    protected boolean isCachable() {
        return this.categoryFilters.size() == 0 && this.categoryEntryFilters.size() == 0;
    }

    /**
     * Is invoked if a category or category entry was added or removed.
     * <p>
     * This implementation discards the cached menu.
     * </p>
     */
    protected void menuContentChanged() {
        cachedMenu = null;
    }

    /**
     * Remove a previously added category. The items that were added to that category are not
     * removed but will instead just not be in the resulting menu. If the category is added again
     * the items will also be contained in the menu again.
     *
     * @param item
     *            the category to remove
     * @return whether the category existed and was removed
     */
    public boolean removeCategory(C item) {
        return removeCategory(item.getId());
    }

    /**
     * Same as {@link #removeCategory(MenuEntry) removeCategory(C)} but removes the category by its
     * ID.
     *
     * @param categoryId
     *            the ID of the category to remove
     * @return whether the category existed and was removed
     */
    public boolean removeCategory(String categoryId) {
        if (categoriesBuilder.removeEntry(categoryId) != null) {
            menuContentChanged();
            return true;
        }
        return true;
    }

    /**
     * Remove a previously added filter
     *
     * @param filter
     *            the filter to remove
     */
    public void removeCategoryEntryFilter(MenuEntryFilter<S> filter) {
        List<MenuEntryFilter<S>> newFilters = new ArrayList<>(categoryEntryFilters);
        newFilters.remove(filter);
        categoryEntryFilters = newFilters;
    }

    /**
     * Remove a previously added filter
     *
     * @param filter
     *            the filter to remove
     */
    public void removeCategoryFilter(MenuEntryFilter<C> filter) {
        List<MenuEntryFilter<C>> newFilters = new ArrayList<>(categoryFilters);
        newFilters.remove(filter);
        categoryFilters = newFilters;
    }

    /**
     * Remove a previously added item from a category.
     *
     * @param categoryId
     *            the ID of the category from which the entry should be removed
     * @param item
     *            the item to remove
     * @return the removed entry if the entry existed in the category, otherwise null
     */
    public S removeFromCategory(String categoryId, S item) {
        return removeFromCategory(categoryId, item.getId());
    }

    /**
     * Same as {@link #removeFromCategory(String, MenuEntry) removeFromCategory(String, S)} but
     * searches the item by its ID.
     *
     * @param categoryId
     *            the ID of the category from which the entry should be removed
     * @param itemId
     *            the ID of the item to remove
     * @return the removed entry if the entry existed in the category, otherwise null
     */
    public S removeFromCategory(String categoryId, String itemId) {
        SubmenuBuilder<S> categoryContentBuilder = categoryContentBuilders.get(categoryId);
        S removedEntry = null;
        if (categoryContentBuilder != null) {
            removedEntry = categoryContentBuilder.removeEntry(itemId);
            if (removedEntry != null) {
                menuContentChanged();
            }
        }
        return removedEntry;
    }

}
