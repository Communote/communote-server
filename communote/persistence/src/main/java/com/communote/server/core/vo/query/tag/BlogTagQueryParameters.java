package com.communote.server.core.vo.query.tag;

import java.util.Locale;
import java.util.Map;

import com.communote.server.api.core.common.IdentifiableEntityData;
import com.communote.server.api.core.tag.TagData;
import com.communote.server.core.filter.ResultSpecification;
import com.communote.server.core.vo.query.blog.BlogQuery;
import com.communote.server.core.vo.query.blog.BlogQueryParameters;
import com.communote.server.model.tag.TagConstants;


/**
 * QueryInstance for getting tags of blogs.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class BlogTagQueryParameters extends BlogQueryParameters {

    /**
     * the parameter name for the selected tags if this tags should be hidden
     */
    private final static String PARAM_BLOG_TAG_HIDDEN_PREFIX = "blogTagHide";

    /** to hide selected tags */
    private boolean hideSelectedTags = true;

    /**
     * Constructor.
     */
    public BlogTagQueryParameters() {
        setResultSpecification(new ResultSpecification(0, 50));
    }

    /**
     * Get the parameter name for the selected tag of a given index (if its a parameter list). Used
     * if {@code isHideSelectedTags=true}.
     * 
     * @param index
     *            the index of the selected tag
     * @return the parameter name to the index
     */
    public String getBlogTagHideConstant(int index) {
        return PARAM_BLOG_TAG_HIDDEN_PREFIX + index;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Object> getParameters() {
        Map<String, Object> parameter = super.getParameters();

        if (getTags() != null && isHideSelectedTags()) {
            for (int i = 0; i < getTags().length; i++) {
                parameter.put(getBlogTagHideConstant(i), getTags()[i].toLowerCase(Locale.ENGLISH));
            }
        }

        return parameter;
    }

    /**
     * @return the hideSelectedTags
     */
    public boolean isHideSelectedTags() {
        return hideSelectedTags;
    }

    /**
     * @param hideSelectedTags
     *            the hideSelectedTags to set
     */
    public void setHideSelectedTags(boolean hideSelectedTags) {
        this.hideSelectedTags = hideSelectedTags;
    }

    /**
     * sort by the name of the blogs
     */
    @Override
    public void sortByNameAsc() {
        this.addSortField(BlogQuery.ALIAS_TAGS, TagConstants.DEFAULTNAME,
                SORT_ASCENDING);
    }

    /**
     * sort by the count of tags
     */
    public void sortByTagCount() {
        this.addSortField("", "*", SORT_DESCENDING, "count");
    }

    /**
     * Transforms the BlogData. Sets the description for BlogData because the description is
     * not allow in query (clob in a oracle db environment)
     * 
     * @param resultItem
     *            The resultItem to transform
     * @return The transformed BlogData
     */
    @Override
    public IdentifiableEntityData transformResultItem(Object resultItem) {
        return (TagData) resultItem;
    }

}
