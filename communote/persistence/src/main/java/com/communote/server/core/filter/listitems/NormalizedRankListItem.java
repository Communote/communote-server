package com.communote.server.core.filter.listitems;

import com.communote.server.api.core.common.IdentifiableEntityData;

/**
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 * @param <R>
 *            Type of the list item this refers to.
 */
public class NormalizedRankListItem<R extends RankListItem> extends IdentifiableEntityData {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private final int normalizedRank;

    private final R item;

    /**
     * @param item
     *            The item
     * @param normalizedRank
     *            The normalized rank of the item.
     */
    public NormalizedRankListItem(R item, int normalizedRank) {
        if (item == null) {
            throw new IllegalArgumentException("item cannot be null!");
        }
        this.normalizedRank = normalizedRank;
        this.item = item;
    }

    /**
     * @return The item.
     */
    public R getItem() {
        return item;
    }

    /**
     * @return The normalized rank for the given item.
     */
    public int getNormalizedRank() {
        return normalizedRank;
    }
}
