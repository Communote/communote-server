package com.communote.server.core.property;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

import com.communote.common.converter.Converter;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.blog.BlogRightsManagement;
import com.communote.server.api.core.common.NotFoundException;
import com.communote.server.api.core.event.EventDispatcher;
import com.communote.server.api.core.property.PropertyManagement;
import com.communote.server.api.core.property.PropertyType;
import com.communote.server.api.core.property.StringPropertyTO;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.core.common.caching.CacheManager;
import com.communote.server.core.security.SecurityHelper;
import com.communote.server.model.note.Note;
import com.communote.server.model.user.User;
import com.communote.server.model.user.UserStatus;
import com.communote.server.model.user.UserNoteProperty;
import com.communote.server.persistence.blog.NoteDao;
import com.communote.server.persistence.property.PropertyDao;
import com.communote.server.persistence.user.UserDao;
import com.communote.server.persistence.user.UserNotePropertyDao;

/***
 * Property accessor for users, don't construct it, use it from the {@link PropertyManagement}
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class UserNotePropertyAccessor extends StringPropertyAccessor<Note, UserNoteProperty> {

    private final UserNotePropertyCacheElementProvider elementProvider;
    private CacheManager cacheManager;

    /**
     * Don't construct it from the outside packages.
     *
     * @param eventDispatcher
     *            the event dispatcher to use for firing events
     */
    protected UserNotePropertyAccessor(EventDispatcher eventDispatcher) {
        super(eventDispatcher);
        elementProvider = new UserNotePropertyCacheElementProvider();
    }

    /**
     * Checks, that the current user is allowed to read the topic the note is in.
     *
     * {@inheritDoc}
     */
    @Override
    protected void assertReadAccess(Note note) throws AuthorizationException {
        // TODO should check DM access
        if (SecurityHelper.isInternalSystem()) {
            return;
        }
        if (!ServiceLocator
                .instance()
                .getService(BlogRightsManagement.class)
                .currentUserHasReadAccess(note.getBlog().getId(), false)) {
            throw new AuthorizationException(
                    "The user has no access to this note.");
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Does it by calling {@link #assertReadAccess(Note)}.
     * </p>
     *
     */
    @Override
    protected void assertWriteAccess(Note note) throws AuthorizationException {
        assertReadAccess(note);
    }

    /**
     * @return the lazily initialized cache manager
     */
    private CacheManager getCacheManager() {
        if (cacheManager == null) {
            cacheManager = ServiceLocator.findService(CacheManager.class);
        }
        return cacheManager;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Long getObjectId(Note object) {
        return object.getId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PropertyType getPropertyType() {
        return PropertyType.UserNoteProperty;
    }

    /**
     * Return the users that a given property for a note. The found users are converted into the
     * target type of the provided converter. Deleted users won't be included.
     *
     * @param <T>
     *            the target type of the conversion
     * @param noteId
     *            the ID of the note for which the users that have the property should be returned
     * @param keyGroup
     *            the group key of the searched property
     * @param key
     *            the group of the searched property
     * @param value
     *            the value of the searched property
     * @param converter
     *            the converter to convert the found users
     * @return the found users or an empty list
     * @throws NotFoundException
     *             in case the note does not exist
     * @throws AuthorizationException
     *             in case the current user is not allowed to read the property
     */
    public <T> Collection<T> getUsersOfProperty(Long noteId, String keyGroup, String key,
            String value, Converter<User, T> converter) throws NotFoundException,
            AuthorizationException {
        assertLoadObject(noteId, false);
        UserNotePropertyCacheKey cacheKey = new UserNotePropertyCacheKey(noteId, keyGroup, key);
        HashMap<String, Set<Long>> valueToUsers = getCacheManager().getCache().get(cacheKey,
                elementProvider);
        ArrayList<T> result = new ArrayList<T>();
        if (valueToUsers != null) {
            Set<Long> userIds = valueToUsers.get(value);
            if (userIds != null) {
                UserDao userDao = ServiceLocator.findService(UserDao.class);
                for (Long userId : userIds) {
                    User user = userDao.load(userId);
                    if (user != null && !user.getStatus().equals(UserStatus.DELETED)) {
                        result.add(converter.convert(user));
                    }
                }
            }
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected UserNoteProperty handleCreateNewProperty(Note note) {
        UserNoteProperty property = UserNoteProperty.Factory.newInstance();
        property.setNote(note);
        property.setUser(SecurityHelper.assertCurrentKenmeiUser());
        return property;
    }

    @Override
    protected Set<StringPropertyTO> handleGetAllObjectProperties(Note note)
            throws AuthorizationException {
        // TODO what should we return all properties added to the note? only those of the current
        // user? maybe we should refactor the object hierarchy or at least the methods in
        // PropertiesManagement that do object properties stuff to only work with
        // ObjectPropertyAccessor types
        throw new UnsupportedOperationException("Not supported!");
    }

    @Override
    protected UserNoteProperty handleGetObjectPropertyUnfiltered(Note note, String keyGroup,
            String propertyKey) throws AuthorizationException {
        return ServiceLocator.findService(UserNotePropertyDao.class)
                .findProperty(note.getId(), keyGroup, propertyKey);
    }

    @Override
    protected void handleRemoveObjectProperty(Note note, String keyGroup, String key)
            throws AuthorizationException {
        UserNoteProperty property = handleGetObjectPropertyUnfiltered(note, keyGroup, key);
        if (property != null) {
            PropertyDao propertyDao = ServiceLocator.findService(PropertyDao.class);
            property.setNote(null);
            property.setUser(null);
            propertyDao.remove(property);
            UserNotePropertyCacheKey cacheKey = new UserNotePropertyCacheKey(note.getId(),
                    keyGroup, key);
            getCacheManager().getCache().invalidate(cacheKey, elementProvider);
        }
    }

    /**
     * Test whether the current user has a given property for a note
     *
     * @param noteId
     *            the ID of the note for which the existence of the property should be checked
     * @param keyGroup
     *            the group key of the searched property
     * @param key
     *            the group of the searched property
     * @param value
     *            the value of the searched property
     * @return true if the user has the property otherwise false
     * @throws AuthorizationException
     *             in case the current user is not allowed to read the property
     * @throws NotFoundException
     *             in case the note does not exist
     */
    public boolean hasProperty(Long noteId, String keyGroup, String key, String value)
            throws NotFoundException, AuthorizationException {
        if (!getFilterDefintion().isPropertyAllowedToGet(keyGroup, key)
                || SecurityHelper.isInternalSystem() || SecurityHelper.isPublicUser()) {
            return false;
        }
        assertLoadObject(noteId, false);
        UserNotePropertyCacheKey cacheKey = new UserNotePropertyCacheKey(noteId,
                keyGroup, key);
        HashMap<String, Set<Long>> valueToUsers = getCacheManager().getCache().get(cacheKey,
                elementProvider);
        if (valueToUsers != null) {
            Set<Long> userIds = valueToUsers.get(value);
            if (userIds != null) {
                return userIds.contains(SecurityHelper.getCurrentUserId());
            }
        }

        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Note load(Long id) {
        return ServiceLocator.findService(NoteDao.class).load(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setPropertyValue(UserNoteProperty property, String value) {
        super.setPropertyValue(property, value);
        UserNotePropertyCacheKey cacheKey = new UserNotePropertyCacheKey(
                property.getNote().getId(),
                property.getKeyGroup(), property.getPropertyKey());
        getCacheManager().getCache().invalidate(cacheKey, elementProvider);
    }
}
