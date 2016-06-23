package com.communote.server.core.vo.query.tag;

import org.apache.commons.lang.StringUtils;

import com.communote.server.core.filter.listitems.RankTagListItem;
import com.communote.server.core.vo.query.user.AbstractUserQuery;
import com.communote.server.core.vo.query.user.UserQuery;
import com.communote.server.model.i18n.MessageConstants;
import com.communote.server.model.tag.TagConstants;
import com.communote.server.model.user.CommunoteEntityConstants;
import com.communote.server.model.user.UserConstants;


/**
 * Query definition for retrieving the tags of blogs.
 * 
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class UserTagQuery extends AbstractUserQuery<RankTagListItem, UserTagQueryParameters> {
    /**
     * {@inheritDoc}
     */
    @Override
    public void buildSelectQuery(StringBuilder query, UserTagQueryParameters queryInstance) {
        String userAlias = getUserAlias();
        query.append("Select new " + RankTagListItem.class.getName() + "(min(tags."
                + TagConstants.ID + "), count(*) , min(tags." + TagConstants.DEFAULTNAME + "))");
        query.append(" from " + UserConstants.CLASS_NAME + " " + getUserAlias());
        query.append(" inner join " + userAlias + "." + CommunoteEntityConstants.TAGS + " "
                + UserQuery.ALIAS_TAGS);
        if (StringUtils.isNotBlank(queryInstance.getTagPrefix())
                && queryInstance.isMultilingualTagPrefixSearch()) {
            query.append(" LEFT JOIN " + UserQuery.ALIAS_TAGS + "."
                    + TagConstants.NAMES + " tagName LEFT JOIN tagName."
                    + MessageConstants.LANGUAGE
                    + " language");
        }
    }

    /**
     * @return BlogTagQueryInstance
     */
    @Override
    public UserTagQueryParameters createInstance() {
        return new UserTagQueryParameters();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void renderGroupByClause(StringBuilder query, UserTagQueryParameters queryInstance) {
        query.append(" GROUP BY tags." + TagConstants.TAGSTORETAGID);
        // KENMEI-5725 We need the ordering after the grouping.
        query.append(" ORDER BY count(*) desc");
    }

    /**
     * Does nothing.
     * 
     * {@inheritDoc}
     */
    @Override
    protected void renderOrderbyClause(StringBuilder mainQuery, UserTagQueryParameters queryInstance) {
        // Do nothing.
    }
}
