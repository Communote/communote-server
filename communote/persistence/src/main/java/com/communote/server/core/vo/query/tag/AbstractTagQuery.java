package com.communote.server.core.vo.query.tag;

import org.apache.commons.lang.StringUtils;

import com.communote.server.api.core.tag.TagData;
import com.communote.server.core.vo.query.TagConstraintConnectorEnum;
import com.communote.server.core.vo.query.TaggingCoreItemQueryDefinition;
import com.communote.server.core.vo.query.logical.LogicalTagFormula;
import com.communote.server.model.tag.TagConstants;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 * @param <R>
 *            Type of the result item.
 */
public abstract class AbstractTagQuery<R extends TagData> extends
        TaggingCoreItemQueryDefinition<R, TagQueryParameters> {

    /**
     * the parameters of the list item constructor
     */
    private final static String[] CONSTRUCTOR_PARAMETERS = new String[] {
            "tag." + TagConstants.ID,
            " tag." + TagConstants.DEFAULTNAME
    };

    /**
     * {@inheritDoc}
     */
    @Override
    public String buildQuery(TagQueryParameters queryParameters) {
        StringBuilder mainQuery = new StringBuilder();
        StringBuilder whereQuery = new StringBuilder();
        renderSelectClause(queryParameters, mainQuery);
        subQueryFindNoteWithTags(mainQuery, whereQuery, queryParameters,
                TagConstraintConnectorEnum.OR);

        if (whereQuery.length() > 0) {
            mainQuery.append(" where ");
            mainQuery.append(whereQuery);
            // exclude tags of query
            LogicalTagFormula formula = queryParameters.getLogicalTags();
            if (formula != null && queryParameters.isHideSelectedTags()) {
                mainQuery.append("and tag." + TagConstants.TAGSTORETAGID + " not in (");
                mainQuery.append(queryParameters.createParameterNames(formula));
                mainQuery.append(")");
            }
            if (queryParameters.isHideSelectedTags() && !queryParameters.getTagIds().isEmpty()) {
                mainQuery.append("and tag." + TagConstants.ID + " not in (");
                mainQuery.append(StringUtils.join(queryParameters.getTagIds(), ","));
                mainQuery.append(")");
            }
            if (queryParameters.getTagIdsToExclude().size() > 0) {
                mainQuery.append("and tag." + TagConstants.ID + " not in (");
                mainQuery.append(StringUtils.join(queryParameters.getTagIdsToExclude(), ","));
                mainQuery.append(")");
            }
        }
        mainQuery.append(" group by tag." + TagConstants.ID + ", tag." + TagConstants.DEFAULTNAME
                + " ");
        renderOrderbyClause(mainQuery, queryParameters);
        return mainQuery.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TagQueryParameters createInstance() {
        return new TagQueryParameters(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String[] getConstructorParameters() {
        return CONSTRUCTOR_PARAMETERS;
    }

    /**
     * {@inheritDoc}
     * 
     * @return
     */
    protected String getResultObjectPrefix() {
        return "tag.";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean needUserInQuery(TagQueryParameters queryInstance) {
        return false;
    }

    /**
     * {@inheritDoc}
     * 
     * @return
     */
    @Override
    protected boolean queryReturnsTags() {
        return true;
    }

}
