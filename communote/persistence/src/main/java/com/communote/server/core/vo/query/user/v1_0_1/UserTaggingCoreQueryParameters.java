package com.communote.server.core.vo.query.user.v1_0_1;

import org.apache.commons.lang.StringUtils;

import com.communote.server.core.vo.query.TimelineQueryParameters;


/**
 * Query instance to find user tagged resources
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class UserTaggingCoreQueryParameters extends TimelineQueryParameters {

    private final UserTaggingCoreQuery query;

    /**
     * Construct me with the right query definition
     * 
     * @param query
     *            the valid definition
     */
    public UserTaggingCoreQueryParameters(UserTaggingCoreQuery query) {
        this.query = query;
    }

    /**
     * Sort by the user rank descending
     */
    public void sortByUserRankDesc() {
        addSortField(StringUtils.EMPTY, "count(distinct " + query.getNoteAlias() + "id)",
                SORT_DESCENDING);
    }

}
