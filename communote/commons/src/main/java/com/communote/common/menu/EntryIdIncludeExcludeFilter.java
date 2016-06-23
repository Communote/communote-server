package com.communote.common.menu;

import java.util.HashSet;

/**
 * Base class to implement a filter which includes or excludes an entry if a condition is met. The
 * entries are identified by the ID.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 * @param <T>
 *            type of the menu entry this filter handles
 */
public abstract class EntryIdIncludeExcludeFilter<T extends MenuEntry> implements
        MenuEntryFilter<T> {

    private final HashSet<String> entryIdsToInclude = new HashSet<>();
    private final HashSet<String> entryIdsToExclude = new HashSet<>();

    /**
     * Add the ID of an entry that should only be included if the condition is not fulfilled If the
     * same ID was also added via {@link #addEntryToInclude(String)} the inclusion takes precedence.
     *
     * @param entryId
     *            the ID of the entry to check
     */
    public void addEntryToExclude(String entryId) {
        this.entryIdsToExclude.add(entryId);
    }

    /**
     * Add the ID of an entry that should only be included if the condition is fulfilled
     *
     * @param entryId
     *            the ID of the entry to check
     */
    public void addEntryToInclude(String entryId) {
        this.entryIdsToInclude.add(entryId);
    }

    /**
     * @return true if the condition is fulfilled and the ID of the entry was added to the includes
     *         or the condition is fulfilled and the ID of the entry was added to the includes. Also
     *         returns true if the ID of the entry was not added as inclusion or exclusion.
     */
    @Override
    public boolean include(T entry) {
        if (entryIdsToInclude.contains(entry.getId())) {
            return testCondition(entry);
        } else if (entryIdsToExclude.contains(entry.getId())) {
            return !testCondition(entry);
        }
        return true;
    }

    /**
     * Remove a previously added entry ID from those that should be excluded.
     *
     * @param entryId
     *            the ID of the entry
     */
    public void removeEntryToExclude(String entryId) {
        this.entryIdsToExclude.remove(entryId);
    }

    /**
     * Remove a previously added entry ID from those that should be included.
     *
     * @param entryId
     *            the ID of the entry
     */
    public void removeEntryToInclude(String entryId) {
        this.entryIdsToInclude.remove(entryId);
    }

    /**
     * Test the condition and return true if the condition is fulfilled.
     *
     * @param entry
     *            the entry to test
     * @return true if the condition is fulfilled
     */
    protected abstract boolean testCondition(T entry);

}