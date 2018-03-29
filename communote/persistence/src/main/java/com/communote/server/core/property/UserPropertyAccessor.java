package com.communote.server.core.property;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.event.EventDispatcher;
import com.communote.server.api.core.property.PropertyManagement;
import com.communote.server.api.core.property.PropertyType;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.core.security.SecurityHelper;
import com.communote.server.model.user.User;
import com.communote.server.model.user.UserProperty;
import com.communote.server.persistence.user.UserDao;

/***
 * Property accessor for users, don't construct it, use it from the {@link PropertyManagement}
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class UserPropertyAccessor extends ObjectPropertyAccessor<User, UserProperty> {

    /**
     *
     * @param eventDispatcher
     *            the event dispatcher for dispatching event on property changes
     */
    public UserPropertyAccessor(EventDispatcher eventDispatcher) {
        super(eventDispatcher);
    }

    @Override
    protected void assertReadAccess(User user) throws AuthorizationException {
        if (SecurityHelper.isClientManager() || SecurityHelper.isInternalSystem()) {
            return;
        }
        // TODO we should probably have a way to restrict read access to the owner of the property
        if (SecurityHelper.getCurrentUserId() == null) {
            throw new AuthorizationException("Anonymous access to user properties is not allowed");
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Checks if the user to edit is the user itself or a client manager.
     * </p>
     */
    @Override
    protected void assertWriteAccess(User user) throws AuthorizationException {
        if (SecurityHelper.isClientManager() || SecurityHelper.isInternalSystem()) {
            return;
        }
        if (!user.getId().equals(SecurityHelper.getCurrentUserId())) {
            throw new AuthorizationException(
                    "The current user (" + SecurityHelper.getCurrentUserId()
                            + ") has to be a client manager, internal system user or the user to be edited ("
                            + user.getId() + ").");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Long getObjectId(User object) {
        return object.getId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PropertyType getPropertyType() {
        return PropertyType.UserProperty;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected UserProperty handleCreateNewProperty(User user) {
        UserProperty property = UserProperty.Factory.newInstance();
        user.getProperties().add(property);
        return property;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected User load(Long id) {
        return ServiceLocator.findService(UserDao.class).load(id);
    }

}
