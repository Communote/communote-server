package com.communote.plugins.api.rest.v30.resource.topic.role;

import com.communote.server.core.filter.listitems.blog.member.BlogRoleEntityListItem;
import com.communote.server.core.vo.query.QueryResultConverter;

/**
 * Converts a {@link BlogRoleEntityListItem} to a {@link RoleResource}
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class RoleResourceConverter extends
        QueryResultConverter<BlogRoleEntityListItem, RoleResource> {

    @Override
    public boolean convert(BlogRoleEntityListItem source, RoleResource target) {

        target.setEntityId(source.getEntity().getEntityId());
        target.setIsGroup(source.getEntity().isGroup());
        if (source.getEntity().isGroup()) {
            target.setGroupAlias(source.getEntity().getAlias());
        } else {
            target.setUserAlias(source.getEntity().getAlias());
        }
        target.setRole(RoleResourceHelper.getERole(source.getGrantedBlogRole()));
        target.setDisplayName(source.getEntity().getDisplayName());
        target.setShortDisplayName(source.getEntity().getShortDisplayName());

        return true;
    }

    @Override
    public RoleResource create() {
        return new RoleResource();
    }
}