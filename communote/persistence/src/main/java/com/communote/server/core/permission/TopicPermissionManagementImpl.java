package com.communote.server.core.permission;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.communote.server.api.core.blog.BlogAccessException;
import com.communote.server.api.core.blog.BlogRightsManagement;
import com.communote.server.api.core.blog.CreationBlogTO;
import com.communote.server.api.core.blog.TopicPermissionManagement;
import com.communote.server.api.core.security.permission.BasePermissionManagement;
import com.communote.server.api.core.security.permission.Permission;
import com.communote.server.api.core.security.permission.PermissionManagement;
import com.communote.server.core.permission.filters.TopicCreateOptionPermissionFilter;
import com.communote.server.core.permission.filters.TopicRolePermissionFilter;
import com.communote.server.model.blog.Blog;
import com.communote.server.persistence.blog.BlogDao;

/**
 * {@link PermissionManagement}
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
@Service("topicPermissionManagement")
@Transactional(readOnly = true)
public class TopicPermissionManagementImpl extends
BasePermissionManagement<Blog, CreationBlogTO, BlogAccessException> implements
TopicPermissionManagement {

    @Autowired
    private BlogRightsManagement blogRightsManagement;

    @Autowired
    private BlogDao blogDao;

    @Override
    protected BlogAccessException createPermissonViolationException(Blog entity,
            Permission<Blog> permission) {
        return new BlogAccessException("The current user hasn't the required permission",
                entity.getId(), permission);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Blog getEntity(Long entityId) {
        Blog blog = blogDao.load(entityId);
        return blog;
    }

    /**
     * Initializer.
     */
    @PostConstruct
    public void init() {
        addPermissionFilter(new TopicRolePermissionFilter(blogRightsManagement));
        addPermissionFilter(new TopicCreateOptionPermissionFilter());

    }

}
