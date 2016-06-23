package com.communote.server.core.converter.blog;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.blog.BlogRightsManagement;
import com.communote.server.core.filter.listitems.blog.UserDetailBlogListItem;
import com.communote.server.model.blog.Blog;
import com.communote.server.model.blog.BlogRole;

/**
 * Converter for converting a blog entity into a list item.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class BlogToUserDetailBlogListItemConverter extends
        BlogToDetailBlogListItemConverter<UserDetailBlogListItem> {

    /**
     * Create a new converter
     * 
     * @param clazz
     *            the class of the target type
     * @param includeProperties
     *            whether to add the blog properties to the result. The property key will be
     *            constructed as follows &lt;groupKey&gt;.&lt;propertyKey&gt;
     * @param includeTags
     *            whether to add the tags as a comma separated string
     * @param includeMembers
     *            whether to include the members
     * @param includeChildren
     *            If true, the children of this topic, the user can access will be converted too.
     * @param topicRightsManagement
     *            This is only used when includeChildren is true.
     * */
    public BlogToUserDetailBlogListItemConverter(Class<UserDetailBlogListItem> clazz,
            boolean includeProperties, boolean includeTags, boolean includeMembers,
            boolean includeChildren, boolean includeParents,
            BlogRightsManagement topicRightsManagement) {
        super(clazz, includeProperties, includeTags, includeMembers, includeChildren,
                includeParents,
                topicRightsManagement);
    }

    @Override
    public void convert(Blog source, UserDetailBlogListItem target) {
        super.convert(source, target);
        BlogRole role = ServiceLocator.findService(BlogRightsManagement.class)
                .getRoleOfCurrentUser(source.getId(), false);
        target.setUserRole(role);
    }
}
