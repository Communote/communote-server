package com.communote.common.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Thread-safe manager for orderable objects which provides convenience methods for accessing the
 * sorted orderables.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 * @param <T>
 *            the type of the orderables
 */
public class OrderableManager<T extends Orderable> {

    private final Comparator<Orderable> comparator;
    private final boolean allowDuplicates;
    private List<T> items;

    /**
     * Create a manager for orderable items.
     *
     * @param comparator
     *            the comparator to use for sorting the added items.
     * @param allowDuplicates
     *            whether duplicates should be allowed when adding new items. If false a new item
     *            will not be added if it already exists.
     */
    public OrderableManager(Comparator<Orderable> comparator, boolean allowDuplicates) {
        this.comparator = comparator;
        this.allowDuplicates = allowDuplicates;
        this.items = Collections.emptyList();
    }

    /**
     * Add a new item. If duplicates are not allowed the new item will not be added if it already
     * exists.
     *
     * @param orderable
     *            the item to add
     * @return true if the item was added or false if the item was null or already exists and
     *         duplicates are not allowed.
     */
    public synchronized boolean add(T orderable) {
        if (orderable == null) {
            return false;
        }
        if (!allowDuplicates && this.items.contains(orderable)) {
            return false;
        }
        ArrayList<T> newItems = new ArrayList<>(items);
        newItems.add(orderable);
        Collections.sort(newItems, comparator);
        items = Collections.unmodifiableList(newItems);
        return true;
    }

    /**
     * @return an unmodifiable list of all added items. The list is sorted with the comparator
     *         passed to the constructor.
     */
    public List<T> getAll() {
        return items;
    }

    /**
     * @return the first item of the sorted list of all added items or null if no items were added
     */
    public T getFirst() {
        List<T> curItems = items;
        if (curItems.size() == 0) {
            return null;
        }
        return curItems.get(0);
    }

    /**
     * @return the last item of the sorted list of all added items or null if no items were added
     */
    public T getLast() {
        List<T> curItems = items;
        if (curItems.size() == 0) {
            return null;
        }
        return curItems.get(curItems.size() - 1);
    }

    /**
     * @return whether duplicates are allowed
     */
    public boolean isAllowDuplicates() {
        return allowDuplicates;
    }

    /**
     * Remove a previously added item. If duplicates are allowed the first matching will be removed.
     *
     * @param orderable
     *            the item to remove
     * @return true if the item was contained, false otherwise
     */
    public synchronized boolean remove(T orderable) {
        ArrayList<T> newItems = new ArrayList<>(items);
        if (newItems.remove(orderable)) {
            // no need to sort because remove doesn't change order
            items = Collections.unmodifiableList(newItems);
            return true;
        }
        return false;
    }
}
