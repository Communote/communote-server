package com.communote.server.core.vo.query;

import com.communote.server.core.filter.listitems.TimeRangeListItem;
import com.communote.server.model.note.NoteConstants;


/**
 * Query definition for time constraints.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class TaggingCoreItemTimeLimitsQuery extends
        TaggingCoreItemQueryDefinition<TimeRangeListItem, TaggingCoreItemTimeLimitsQueryParameters> {

    /**
     * the parameters of the list item constructor
     */
    private String[] constructorParameters;

    /**
     * {@inheritDoc}
     */
    @Override
    public String buildQuery(TaggingCoreItemTimeLimitsQueryParameters queryInstance) {
        StringBuilder mainQuery = new StringBuilder();
        StringBuilder whereQuery = new StringBuilder();

        renderSelectClause(queryInstance, mainQuery);

        subQueryFindNoteWithTags(mainQuery, whereQuery, queryInstance,
                TagConstraintConnectorEnum.OR);

        if (whereQuery.length() > 0) {
            mainQuery.append(" where ");
            mainQuery.append(whereQuery);
        }

        return mainQuery.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TaggingCoreItemTimeLimitsQueryParameters createInstance() {
        return new TaggingCoreItemTimeLimitsQueryParameters();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String[] getConstructorParameters() {
        return constructorParameters;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<TimeRangeListItem> getResultListItem() {
        return TimeRangeListItem.class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean needUserInQuery(TaggingCoreItemTimeLimitsQueryParameters queryInstance) {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setupQueries() {
        constructorParameters = new String[] {
                "min(" + getNoteAlias() + NoteConstants.CREATIONDATE + ")",
                "max(" + getNoteAlias() + NoteConstants.CREATIONDATE + ")"
        };
        super.setupQueries();
    }

}
