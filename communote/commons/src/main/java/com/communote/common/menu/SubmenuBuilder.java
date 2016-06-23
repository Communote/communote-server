package com.communote.common.menu;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.communote.common.menu.PositionDescriptor.PositionType;

/**
 * Builder for a sub-menu with entries whose positions are described declaratively by a
 * {@link PositionDescriptor}.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 * @param <T>
 *            type of the menu entries of the sub menu
 */
public class SubmenuBuilder<T extends MenuEntry> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SubmenuBuilder.class);

    private List<String> topPositionEntries;
    private List<String> bottomPositionEntries;
    // mapping from entry ID to entry IDs that are positioned after this entry
    private HashMap<String, List<String>> entryIdToAfterPositionEntries;
    // mapping from entry ID the ID of the entry this on is positioned after
    private HashMap<String, String> afterPositionEntryToEntryId;
    private HashMap<String, T> entries;
    private List<T> cachedOrderedEntries;

    /**
     * Add all of the given entries with the same position descriptor.
     *
     * @param entries
     *            the entries to add to the sub-menu
     * @param positionDescriptor
     *            descriptor of the position of each entry within the sub-menu
     */
    public void addAll(List<T> entries, PositionDescriptor positionDescriptor) {
        if (entries != null) {
            synchronized (this) {
                for (T entry : entries) {
                    addEntry(entry, positionDescriptor);
                }
            }
        }
    }

    /**
     * Add all entries of the given list including those that are positioned after these entries.
     *
     * @param entriesToAdd
     *            the IDs of the entries to add
     * @param orderedEntries
     *            the ordered collection of entries to append to
     */
    private void addAllEntries(List<String> entriesToAdd, ArrayList<T> orderedEntries) {
        if (entriesToAdd != null) {
            for (String itemId : entriesToAdd) {
                orderedEntries.add(entries.get(itemId));
                addEntriesPositionedAfterId(itemId, orderedEntries);
            }
        }
    }

    /**
     * Add all entries which are positioned after an entry with a given ID. This will recursively
     * also add all entries which are positioned after the entries positioned after the one with the
     * given ID.
     *
     * @param entryId
     *            the ID of the entry for which all entries that are positioned after this entry
     *            should be added
     * @param orderedEntries
     *            the ordered collection of entries to append to
     */
    private void addEntriesPositionedAfterId(String entryId, ArrayList<T> orderedEntries) {
        if (entryIdToAfterPositionEntries != null
                && entryIdToAfterPositionEntries.containsKey(entryId)) {
            addAllEntries(entryIdToAfterPositionEntries.get(entryId), orderedEntries);
        }
    }

    /**
     * Add an entry to the sub-menu.
     * <p>
     * If called several times with position type <code>TOP</code> the entries which were added
     * later will be positioned before those added earlier. If called several times with position
     * type <code>BOTTOM</code> or <code>AFTER</code> (and the same menu entry ID to position
     * after), the entries which were added later will be positioned after those added earlier.
     * Entries with position type <code>TOP</code> are always before the <code>BOTTOM</code>
     * positioned entries. Entries with position type <code>AFTER</code> will not appear in the
     * resulting menu if the referenced menu entry does not exist. However, if that referenced entry
     * is added later on, the entries to position after that entry will than be contained in the
     * sub-menu.
     * </p>
     * <p>
     * To get the resulting sub-menu the build method needs to be called.
     * </p>
     *
     * @param entry
     *            the entry to add
     * @param positionDescriptor
     *            descriptor of the position of the entry within the sub-menu, can be null for
     *            default positioning at bottom
     * @return whether the entry was added or there was already an entry with the same ID
     */
    public synchronized boolean addEntry(T entry, PositionDescriptor positionDescriptor) {
        if (entries == null) {
            entries = new HashMap<String, T>();
        } else if (entries.containsKey(entry.getId())) {
            LOGGER.debug("Ignoring menu entry with ID {} because it already exists.", entry.getId());
            return false;
        }
        if (positionDescriptor == null || positionDescriptor.getPositionType() == null
                || PositionType.BOTTOM.equals(positionDescriptor.getPositionType())) {
            if (bottomPositionEntries == null) {
                bottomPositionEntries = new ArrayList<>();
            }
            bottomPositionEntries.add(entry.getId());
        } else if (PositionType.TOP.equals(positionDescriptor.getPositionType())) {
            if (topPositionEntries == null) {
                topPositionEntries = new ArrayList<>();
            }
            topPositionEntries.add(entry.getId());
        } else {
            if (entryIdToAfterPositionEntries == null) {
                entryIdToAfterPositionEntries = new HashMap<>();
                afterPositionEntryToEntryId = new HashMap<>();
            }
            if (positionDescriptor.getMenuEntryId() == null) {
                throw new IllegalArgumentException(
                        "The menuEntryId of the descriptor with PositionType "
                                + positionDescriptor.getPositionType() + " must not be null.");
            }
            afterPositionEntryToEntryId.put(entry.getId(), positionDescriptor.getMenuEntryId());
            List<String> entriesAfterEntry = entryIdToAfterPositionEntries.get(positionDescriptor
                    .getMenuEntryId());
            if (entriesAfterEntry == null) {
                entriesAfterEntry = new ArrayList<String>();
                entryIdToAfterPositionEntries.put(positionDescriptor.getMenuEntryId(),
                        entriesAfterEntry);
            }
            entriesAfterEntry.add(entry.getId());
        }

        entries.put(entry.getId(), entry);
        // reset cache
        cachedOrderedEntries = null;
        return true;
    }

    private synchronized List<T> build() {
        if (cachedOrderedEntries != null) {
            return cachedOrderedEntries;
        }
        ArrayList<T> orderedEntries = new ArrayList<>();
        // add top items first, but in inverse order
        if (topPositionEntries != null) {
            for (int i = topPositionEntries.size() - 1; i >= 0; i--) {
                String entryId = topPositionEntries.get(i);
                orderedEntries.add(entries.get(entryId));
                // if there are any children positioned after this item, add them now
                addEntriesPositionedAfterId(entryId, orderedEntries);
            }
        }
        // add the bottom positioned children with all children that are positioned after one of
        // those
        addAllEntries(bottomPositionEntries, orderedEntries);
        cachedOrderedEntries = orderedEntries;
        return orderedEntries;
    }

    /**
     * Build the resulting sub-menu
     *
     * @return ordered list of entries of this sub-menu
     */
    public List<T> buildSubmenu() {
        if (cachedOrderedEntries != null) {
            return cachedOrderedEntries;
        }
        return build();
    }

    /**
     * Return an entry by its ID.
     *
     * @param entryId
     *            the ID of the entry to return
     * @return the entry or null if the entry does not exist
     */
    public T getEntry(String entryId) {
        if (this.entries != null) {
            return entries.get(entryId);
        }
        return null;
    }

    /**
     * Remove an entry from the sub-menu. After successfully removing an entry the sub-menu has to
     * be rebuild explicitly.
     *
     * @param entryId
     *            the ID of the entry to remove
     * @return the removed entry if the entry existed, otherwise null
     */
    public synchronized T removeEntry(String entryId) {
        if (entries != null) {
            T entry = entries.remove(entryId);
            if (entry != null) {
                boolean removed = false;
                if (topPositionEntries != null) {
                    removed = topPositionEntries.remove(entryId);
                }
                if (!removed && bottomPositionEntries != null) {
                    removed = bottomPositionEntries.remove(entryId);
                }
                if (!removed && entryIdToAfterPositionEntries != null) {
                    String afterMenuEntryId = afterPositionEntryToEntryId.remove(entryId);
                    entryIdToAfterPositionEntries.get(afterMenuEntryId).remove(entryId);
                }
                cachedOrderedEntries = null;
                return entry;
            }
        }
        return null;
    }

    /**
     * Remove an entry from the sub-menu. After successfully removing an entry the sub-menu has to
     * be rebuild explicitly.
     *
     * @param entry
     *            the entry to remove
     * @return the removed entry if the entry existed, otherwise null
     */
    public synchronized T removeEntry(T entry) {
        return removeEntry(entry.getId());
    }
}
