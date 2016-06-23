package com.communote.server.core.filter.listitems;

/**
 * <p>
 * An interface to mark list items which contain a rank.
 * </p>
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public interface RankListItem {

    /**
     * <p>
     * The rank of the item
     * </p>
     */
    public Number getRank();

}