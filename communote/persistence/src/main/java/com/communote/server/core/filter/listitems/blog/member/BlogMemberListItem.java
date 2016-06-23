package com.communote.server.core.filter.listitems.blog.member;

import com.communote.server.api.core.common.IdentifiableEntityData;
import com.communote.server.model.blog.BlogRole;


/**
 * List item reflecting a blog member
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class BlogMemberListItem extends IdentifiableEntityData {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private String externalSystemId;
    private BlogRole blogRole;

    /**
     * @return the blogRole
     */
    public BlogRole getBlogRole() {
        return blogRole;
    }

    /**
     * @return the externalSystemId
     */
    public String getExternalSystemId() {
        return externalSystemId;
    }

    /**
     * @param blogRole
     *            the blogRole to set
     */
    public void setBlogRole(BlogRole blogRole) {
        this.blogRole = blogRole;
    }

    /**
     * @param externalSystemId
     *            the externalSystemId to set
     */
    public void setExternalSystemId(String externalSystemId) {
        this.externalSystemId = externalSystemId;
    }

}
