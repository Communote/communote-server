package com.communote.server.core.vo.query.blog;

import java.util.Map;

import com.communote.server.core.vo.query.user.UserQueryParameters;


/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class BlogMemberQueryParameters extends UserQueryParameters {

    /** Parameter name for the blog id filter. */
    public static final String PARAM_BLOG_ID_FILTER = "blogIdFilter";

    private Long blogId;

    /**
     * @return the blogId
     */
    public Long getBlogId() {
        return blogId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Object> getParameters() {
        Map<String, Object> params = super.getParameters();
        if (blogId != null) {
            params.put(PARAM_BLOG_ID_FILTER, blogId);
        }
        return params;
    }

    /**
     * 
     * @param blogId
     *            the blogId to set
     */
    public void setBlogId(Long blogId) {
        this.blogId = blogId;
    }
}
