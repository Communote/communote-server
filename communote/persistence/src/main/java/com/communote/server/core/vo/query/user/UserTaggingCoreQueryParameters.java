package com.communote.server.core.vo.query.user;

import org.apache.commons.lang3.StringUtils;

import com.communote.server.core.vo.query.TimelineQueryParameters;


/**
 * Query instance to find user tagged resources
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class UserTaggingCoreQueryParameters extends TimelineQueryParameters {

    private final AbstractUserTaggingCoreQuery<?> query;

    /**
     * Construct me with the right query definition
     * 
     * @param abstractUserTaggingCoreQuery
     *            the valid definition
     */
    public UserTaggingCoreQueryParameters(
            AbstractUserTaggingCoreQuery<?> abstractUserTaggingCoreQuery) {
        this.query = abstractUserTaggingCoreQuery;
    }

    /**
     * Sort by the user rank descending
     */
    public void sortByUserRankDesc() {
        addSortField(StringUtils.EMPTY, "count(distinct " + query.getNoteAlias() + "id)",
                SORT_DESCENDING);
    }

}
