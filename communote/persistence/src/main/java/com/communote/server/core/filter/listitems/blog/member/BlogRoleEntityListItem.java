package com.communote.server.core.filter.listitems.blog.member;

import java.util.List;

import com.communote.server.api.core.common.IdentifiableEntityData;
import com.communote.server.model.blog.BlogRole;

/**
 * List items for the role of a entity to the blog. The role reflects the aggregated role of all
 * blog members of the entity.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class BlogRoleEntityListItem extends IdentifiableEntityData {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private BlogRole grantedBlogRole;
    private List<BlogMemberListItem> roles;
    private CommunoteEntityData entity;

    /**
     * @return the entity
     */
    public CommunoteEntityData getEntity() {
        return entity;
    }

    /**
     * @return the grantedBlogRole
     */
    public BlogRole getGrantedBlogRole() {
        return grantedBlogRole;
    }

    /**
     * @return the roles
     */
    public List<BlogMemberListItem> getRoles() {
        return roles;
    }

    /**
     * @param entity
     *            the entity to set
     */
    public void setEntity(CommunoteEntityData entity) {
        this.entity = entity;
    }

    /**
     * @param grantedBlogRole
     *            the grantedBlogRole to set
     */
    public void setGrantedBlogRole(BlogRole grantedBlogRole) {
        this.grantedBlogRole = grantedBlogRole;
    }

    /**
     * @param roles
     *            the roles to set
     */
    public void setRoles(List<BlogMemberListItem> roles) {
        this.roles = roles;
    }

    @Override
    public String toString() {
        return "BlogRoleEntityListItem [grantedBlogRole=" + grantedBlogRole + ", roles=" + roles
                + ", entity=" + entity + ", getEntity()=" + getEntity() + ", getGrantedBlogRole()="
                + getGrantedBlogRole() + ", getRoles()=" + getRoles() + ", getId()=" + getId()
                + ", getProperties()=" + getProperties() + "]";
    }

}
