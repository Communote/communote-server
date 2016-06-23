package com.communote.server.core.permission.filters;

import com.communote.server.api.core.blog.CreationBlogTO;
import com.communote.server.api.core.security.permission.PermissionFilter;
import com.communote.server.model.blog.Blog;

/**
 * Permission filter for topics.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public interface TopicPermissionFilter extends PermissionFilter<Blog, CreationBlogTO> {

}
