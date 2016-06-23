package com.communote.server.core.vo.query.tag;

import com.communote.server.core.filter.ResultSpecification;
import com.communote.server.core.vo.query.user.UserQueryParameters;

/**
 * QueryInstance for getting tags of blogs.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class UserTagQueryParameters extends UserQueryParameters {

    /**
     * the parameter name for the selected tags if this tags should be hidden
     */
    private final static String PARAM_BLOG_TAG_HIDDEN_PREFIX = "blogTagHide";

    /**
     * Constructor.
     */
    public UserTagQueryParameters() {
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
     * @return <code>false</code>
     */
    @Override
    public boolean needTransformListItem() {
        return false;
    }

    /**
     * sort by the count of tags
     */
    public void sortByTagCount() {
        this.addSortField("", "*", SORT_DESCENDING, "count");
    }

}
