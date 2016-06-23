package com.communote.common.menu;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Generic category menu entry which can have sub-items and arbitrary details describing the
 * category.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 * @param <C>
 *            the type of the category details
 * @param <S>
 *            the type of the sub-items
 */
public class CategoryEntry<C extends MenuEntry, S extends MenuEntry> implements MenuItem<S> {

    private List<S> children = new ArrayList<S>();
    private final C categoryDetails;

    /**
     * Create a new category entry with an empty collection of children
     *
     * @param categoryDetails
     *            the details describing the category
     */
    public CategoryEntry(C categoryDetails) {
        this.categoryDetails = categoryDetails;
    }

    /**
     * @return the details describing this category
     */
    public C getCategoryDetails() {
        return categoryDetails;
    }

    @Override
    public List<S> getChildren() {
        return children;
    }

    @Override
    public String getId() {
        return categoryDetails.getId();
    }

    @Override
    public String getLabel() {
        return categoryDetails.getLabel();
    }

    @Override
    public String getLocalizedLabel(Locale locale, Object... arguments) {
        return categoryDetails.getLocalizedLabel(locale, arguments);
    }

    @Override
    public boolean hasChildren() {
        return children != null && children.size() > 0;
    }

    @Override
    public void setChildren(List<S> children) {
        this.children = children;
    }
}
