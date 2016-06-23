package com.communote.server.core.vo.query.tag;

import org.apache.commons.lang.StringUtils;

import com.communote.server.core.filter.listitems.RankTagListItem;
import com.communote.server.core.vo.query.TagConstraintConnectorEnum;
import com.communote.server.core.vo.query.logical.LogicalTagFormula;
import com.communote.server.model.i18n.MessageConstants;
import com.communote.server.model.note.NoteConstants;
import com.communote.server.model.tag.TagConstants;

/**
 * Find related tags
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class RelatedRankTagQuery extends AbstractTagQuery<RankTagListItem> {

    /**
     * the parameters of the list item constructor
     */
    private final static String[] CONSTRUCTOR_PARAMETERS = new String[] {
            "myTag." + TagConstants.ID,
            "count(*)",
            "myTag." + TagConstants.DEFAULTNAME
    };

    /**
     * {@inheritDoc}
     */
    @Override
    public String buildQuery(TagQueryParameters queryInstance) {
        StringBuilder subMainQuery = new StringBuilder();
        StringBuilder subWhereQuery = new StringBuilder();

        StringBuilder mainQuery = new StringBuilder();
        renderSelectClause(queryInstance, mainQuery);

        mainQuery.append(" from ");
        mainQuery.append(NoteConstants.CLASS_NAME);
        mainQuery.append(" myUtr left join myUtr.");
        mainQuery.append(NoteConstants.TAGS);
        mainQuery.append(" myTag ");
        if (StringUtils.isNotBlank(queryInstance.getTagPrefix())
                && queryInstance.isMultilingualTagPrefixSearch()) {
            mainQuery.append("LEFT JOIN myTag."
                    + TagConstants.NAMES + " tagName LEFT JOIN tagName."
                    + MessageConstants.LANGUAGE
                    + " language");
        }
        mainQuery.append(" where ");

        mainQuery.append(" myUtr.id in (");

        String tagPrefix = queryInstance.getTagPrefix();
        // don't render tag prefix constraint in subQuery
        if (tagPrefix != null) {
            queryInstance.setTagPrefix(null);
        }

        subMainQuery.append(" select utr.id ");

        subQueryFindNoteWithTags(subMainQuery, subWhereQuery, queryInstance,
                TagConstraintConnectorEnum.OR);

        if (subWhereQuery.length() > 0) {
            subMainQuery.append(" where ");
            subMainQuery.append(subWhereQuery);
        }

        mainQuery.append(subMainQuery);

        mainQuery.append(" ) ");

        // restore tagPrefix to ensure that a correct parameter map is created
        queryInstance.setTagPrefix(tagPrefix);
        // render tag prefix constraint
        renderTagPrefixConstraint(tagPrefix, mainQuery, "myTag.", AND, queryInstance);
        // exclude tags of query
        LogicalTagFormula formula = queryInstance.getLogicalTags();
        if (formula != null && queryInstance.isHideSelectedTags()) {
            mainQuery.append("and myTag." + TagConstants.TAGSTORETAGID + " not in (");
            mainQuery.append(queryInstance.createParameterNames(formula));
            mainQuery.append(")");
        }
        if (queryInstance.isHideSelectedTags() && !queryInstance.getTagIds().isEmpty()) {
            mainQuery.append("and myTag." + TagConstants.ID + " not in (");
            mainQuery.append(StringUtils.join(queryInstance.getTagIds(), ","));
            mainQuery.append(")");
        }
        mainQuery.append(" group by myTag." + TagConstants.ID + ", myTag."
                + TagConstants.DEFAULTNAME
                + " ");

        renderOrderbyClause(mainQuery, queryInstance);

        return mainQuery.toString();

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
     */
    @Override
    public Class<RankTagListItem> getResultListItem() {
        return RankTagListItem.class;
    }

    /**
     * {@inheritDoc}
     * 
     * @return
     */
    @Override
    protected String getResultObjectPrefix() {
        return "myTag.";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setupQueries() {
        super.setupQueries();
    }

}
