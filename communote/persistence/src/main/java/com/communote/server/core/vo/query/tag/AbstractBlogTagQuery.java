package com.communote.server.core.vo.query.tag;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.communote.server.api.core.tag.TagData;
import com.communote.server.core.vo.query.Query;
import com.communote.server.core.vo.query.blog.BlogQuery;
import com.communote.server.model.tag.TagConstants;


/**
 * Query definition for retrieving the tags of blogs.
 * 
 * @param <R>
 *            Type of the result for this query.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class AbstractBlogTagQuery<R extends TagData> extends
        BlogQuery<R, BlogTagQueryParameters> {
    private ArrayList<String> constructorParameter;

    /**
     * Constructor.
     * 
     * @param resultListItemType
     *            Type of the result item.
     */
    public AbstractBlogTagQuery(Class<R> resultListItemType) {
        super(resultListItemType);
    }

    /**
     * @return BlogTagQueryInstance
     */
    @Override
    public BlogTagQueryParameters createInstance() {
        return new BlogTagQueryParameters();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<String> getConstructorParameter() {
        return this.constructorParameter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void renderGroupBy(StringBuilder query) {
        query.append(" GROUP BY " + BlogQuery.ALIAS_TAGS + "."
                + TagConstants.ID + ", " + BlogQuery.ALIAS_TAGS + "."
                + TagConstants.DEFAULTNAME);
    }

    /**
     * This method is overridden to handle
     * 
     * {@inheritDoc}
     */
    @Override
    protected boolean renderTagsQuery(BlogTagQueryParameters instance, StringBuilder mainQuery,
            String prefix) {
        if (!super.renderTagsQuery(instance, mainQuery, prefix) && instance.getTagIds().isEmpty()) {
            return false;
        }

        if (instance.isHideSelectedTags()) {
            if (instance.getTags() != null && instance.getTags().length != 0) {
                mainQuery.append(Query.AND + BlogQuery.ALIAS_TAGS + "."
                        + TagConstants.TAGSTORETAGID + " not in (");
                for (int i = 0; i < instance.getTags().length; i++) {
                    if (i > 0) {
                        mainQuery.append(", ");
                    }
                    mainQuery.append(":" + instance.getBlogTagHideConstant(i));
                }
                mainQuery.append(")");
            }
            if (!instance.getTagIds().isEmpty()) {
                mainQuery.append(Query.AND + BlogQuery.ALIAS_TAGS + "."
                        + TagConstants.ID + " not in (");
                mainQuery.append(StringUtils.join(instance.getTagIds(), ","));
                mainQuery.append(")");
            }
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setupQueries() {
        constructorParameter = new ArrayList<String>();
        constructorParameter.add(BlogQuery.ALIAS_TAGS + "." + TagConstants.ID
                + ", " + BlogQuery.ALIAS_TAGS + "." + TagConstants.DEFAULTNAME);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean tagEntityRequiredInQuery() {
        return true;
    }
}
