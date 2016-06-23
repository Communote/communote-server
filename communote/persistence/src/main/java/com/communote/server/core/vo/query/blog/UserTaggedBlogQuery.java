package com.communote.server.core.vo.query.blog;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.communote.server.api.core.blog.BlogData;
import com.communote.server.core.vo.query.TagConstraintConnectorEnum;
import com.communote.server.core.vo.query.TaggingCoreItemQueryDefinition;
import com.communote.server.core.vo.query.TaggingCoreItemUTPExtension;
import com.communote.server.model.blog.BlogConstants;

/**
 * Query definition to retrieve blogs.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class UserTaggedBlogQuery extends
        TaggingCoreItemQueryDefinition<BlogData, TopicTimelineParameters> {

    /** The constructor parameters. */
    private String[] constructorParameters;

    @Override
    public String buildQuery(TopicTimelineParameters queryInstance) {
        StringBuilder mainQuery = new StringBuilder();
        StringBuilder whereQuery = new StringBuilder();
        renderSelectClause(queryInstance, mainQuery);
        subQueryFindNoteWithTags(mainQuery, whereQuery, queryInstance,
                TagConstraintConnectorEnum.OR);

        if (whereQuery.length() > 0) {
            mainQuery.append(" where ");
            mainQuery.append(whereQuery);
        }
        renderGroupByClause(mainQuery, queryInstance);
        renderOrderbyClause(mainQuery, queryInstance);

        return mainQuery.toString();
    }

    @Override
    public TopicTimelineParameters createInstance() {
        return new TopicTimelineParameters();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String[] getConstructorParameters() {
        return this.constructorParameters;
    }

    @Override
    public Class<BlogData> getResultListItem() {
        return BlogData.class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean needDistinct(TopicTimelineParameters queryInstance) {
        return false;
    }

    @Override
    protected boolean needUserInQuery(TopicTimelineParameters queryInstance) {
        return false;
    }

    @Override
    public void renderGroupByClause(StringBuilder query, TopicTimelineParameters queryInstance) {
        query.append(" group by ");
        query.append(StringUtils.join(this.constructorParameters, ","));
    }

    @Override
    protected void setupQueries() {
        List<String> parameters = new ArrayList<String>();
        parameters.add(TaggingCoreItemUTPExtension.ALIAS_BLOG + "." + BlogConstants.NAMEIDENTIFIER);
        /** no DESCRIPTION here, because oracle. DESCRIPTION is load later */
        // parameters.add(TaggingCoreItemUTPExtension.ALIAS_BLOG + "." + BlogConstants.DESCRIPTION);
        parameters.add(TaggingCoreItemUTPExtension.ALIAS_BLOG + "." + BlogConstants.ID);
        parameters.add(TaggingCoreItemUTPExtension.ALIAS_BLOG + "." + BlogConstants.TITLE);
        parameters.add(TaggingCoreItemUTPExtension.ALIAS_BLOG + "." + BlogConstants.CREATIONDATE);
        parameters.add(TaggingCoreItemUTPExtension.ALIAS_BLOG + "."
                + BlogConstants.LASTMODIFICATIONDATE);

        this.constructorParameters = parameters.toArray(new String[] { });
        super.setupQueries();
    }
}
