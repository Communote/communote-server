package com.communote.server.core.vo.query.converters;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.blog.BlogRightsManagement;
import com.communote.server.api.core.blog.UserBlogData;
import com.communote.server.model.blog.Blog;
import com.communote.server.model.blog.BlogRole;

/**
 * Converter that converts a blog entity into a list item
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class BlogToBlogListItemQueryResultConverter extends
        DirectQueryResultConverter<Blog, UserBlogData> {

    @Override
    public boolean convert(Blog source, UserBlogData target) {
        target.setId(source.getId());
        target.setDescription(source.getDescription());
        target.setLastModificationDate(source.getLastModificationDate());
        target.setCreationDate(source.getCreationDate());
        target.setNameIdentifier(source.getNameIdentifier());
        target.setTitle(source.getTitle());
        BlogRole role = ServiceLocator.instance().getService(BlogRightsManagement.class)
                .getRoleOfCurrentUser(source.getId(), false);
        target.setUserRole(role);
        return true;
    }

    @Override
    public UserBlogData create() {
        return new UserBlogData();
    }

}
