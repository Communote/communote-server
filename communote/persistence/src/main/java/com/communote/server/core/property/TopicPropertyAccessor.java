package com.communote.server.core.property;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.blog.BlogRightsManagement;
import com.communote.server.api.core.event.EventDispatcher;
import com.communote.server.api.core.property.PropertyManagement;
import com.communote.server.api.core.property.PropertyType;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.core.security.SecurityHelper;
import com.communote.server.model.blog.Blog;
import com.communote.server.model.blog.BlogProperty;
import com.communote.server.persistence.blog.BlogDao;

/**
 * Property accessor for topics, don't construct it, use it from the {@link PropertyManagement}
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class TopicPropertyAccessor extends ObjectPropertyAccessor<Blog, BlogProperty> {

    /**
     * Don't construct it from the outside packages
     *
     * @param eventDispatcher
     *            the event dispatcher to use for sending events
     */
    protected TopicPropertyAccessor(EventDispatcher eventDispatcher) {
        super(eventDispatcher);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void assertReadAccess(Blog blog) throws AuthorizationException {
        if (!ServiceLocator.instance().getService(BlogRightsManagement.class)
                .userHasReadAccess(blog.getId(), SecurityHelper
                        .assertCurrentUserId(), false)) {
            throw new AuthorizationException(
                    "The note does not exist or the user has no access to this blog.");
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Asserts manager right of the current user on the topic.
     * </p>
     */
    @Override
    protected void assertWriteAccess(Blog topic) throws AuthorizationException {
        if (!ServiceLocator.instance().getService(BlogRightsManagement.class)
                .userHasManagementAccess(topic.getId(), SecurityHelper
                        .assertCurrentUserId())) {
            throw new AuthorizationException(
                    "The note does not exist or the user has no access to this blog.");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Long getObjectId(Blog object) {
        return object.getId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PropertyType getPropertyType() {
        return PropertyType.BlogProperty;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected BlogProperty handleCreateNewProperty(Blog blog) {
        BlogProperty property = BlogProperty.Factory.newInstance();
        blog.getProperties().add(property);
        return property;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Blog load(Long id) {
        return ServiceLocator.findService(BlogDao.class).load(id);
    }

}
