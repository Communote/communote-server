package com.communote.server.core.filter.listitems;

/**
 * IdentifiableEntityData for the count.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class CountListItem extends SimpleNoteListItem {
    private static final long serialVersionUID = -3474923752502799209L;
    private final long count;

    /**
     * @param count
     *            The count.
     */
    public CountListItem(Long count) {
        this.count = count;
    }

    /**
     * @return the count
     */
    public long getCount() {
        return count;
    }
}