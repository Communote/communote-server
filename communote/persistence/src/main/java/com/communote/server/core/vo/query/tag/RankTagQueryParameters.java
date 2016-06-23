package com.communote.server.core.vo.query.tag;

import org.apache.commons.lang.StringUtils;

/**
 * Query instance to find tags
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class RankTagQueryParameters extends TagQueryParameters {

    /**
     * Construct me with the right query definition
     * 
     * @param queryDefinition
     *            the valid definition
     */
    public RankTagQueryParameters(AbstractTagQuery<?> queryDefinition) {
        super(queryDefinition);
    }

    /**
     * Sort by the tag name ascending
     */
    public void sortByTagCountDesc() {
        addSortField(StringUtils.EMPTY, "count(*)", SORT_DESCENDING);
    }
}
