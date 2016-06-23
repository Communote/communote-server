package com.communote.server.core.vo.query;

import java.util.Date;

/**
 * The instance for {@link TaggingCoreItemTimeLimitsQuery}. It is a simple implementation of
 * abstract parent class.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class TaggingCoreItemTimeLimitsQueryParameters extends TimelineQueryParameters {

    /**
     * @return null because date restrictions should not be in the query to get actual time limits.
     */
    @Override
    public Date getLowerTagDate() {
        return null;
    }

    /**
     * @return null because date restrictions should not be in the query to get actual time limits.
     */
    @Override
    public Date getUpperTagDate() {
        return null;
    }

    /**
     * Initiates the query instance with data copied from another query instance object.
     * 
     * @param queryInstance
     *            the query instance
     */
    public void populateFromQueryInstance(TimelineQueryParameters queryInstance) {
        setTypeSpecificExtension(queryInstance.getTypeSpecificExtension());
        setResourceId(queryInstance.getResourceId());
        setTagPrefix(queryInstance.getTagPrefix());
        setLogicalTags(queryInstance.getLogicalTags());
        setUserIds(queryInstance.getUserIds());
        setUserIdsToIgnore(queryInstance.getUserIdsToIgnore());
        setNoteId(queryInstance.getNoteId());
        setUserSearchFilters(queryInstance.getUserSearchFilters(),
                !queryInstance.isIgnoreEmailField());
    }
}
