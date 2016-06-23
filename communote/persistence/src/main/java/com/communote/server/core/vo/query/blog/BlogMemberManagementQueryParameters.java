package com.communote.server.core.vo.query.blog;

import java.util.HashMap;
import java.util.Map;

import com.communote.server.api.core.common.IdentifiableEntityData;
import com.communote.server.core.vo.query.user.UserQueryParameters;
import com.communote.server.model.blog.BlogRole;

/**
 * Query instance to retrieve the blog member roles for a blog
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class BlogMemberManagementQueryParameters extends UserQueryParameters {

    /**
     * Parameter name of the blog id
     */
    public static final String PARAM_BLOG_ID = "blogId";

    public static final String PARAM_EXTENRAL_SYSTEM_ID = "externalSystemId";

    private Long blogId;

    private BlogRole[] includeBlogRoles;

    private String externalSystemId;

    /**
     * @return the blogId
     */
    public Long getBlogId() {
        return blogId;
    }

    public String getExternalSystemId() {
        return externalSystemId;
    }

    /**
     * @return the includeBlogRoles
     */
    public BlogRole[] getIncludeBlogRoles() {
        return includeBlogRoles;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Object> getParameters() {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(PARAM_BLOG_ID, blogId);
        params.put(PARAM_EXTENRAL_SYSTEM_ID, externalSystemId);
        return params;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean needTransformListItem() {
        return false;
    }

    /**
     * @param blogId
     *            the blogId to set
     */
    public void setBlogId(Long blogId) {
        this.blogId = blogId;
    }

    public void setExternalSystemId(String externalSystemId) {
        this.externalSystemId = externalSystemId;
    }

    /**
     * @param includeBlogRoles
     *            the includeBlogRoles to set
     */
    public void setIncludeBlogRoles(BlogRole[] includeBlogRoles) {
        this.includeBlogRoles = includeBlogRoles;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IdentifiableEntityData transformResultItem(Object resultItem) {
        return (IdentifiableEntityData) resultItem;
    }
}
