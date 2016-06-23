package com.communote.server.core.property;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.common.NotFoundException;
import com.communote.server.api.core.event.EventDispatcher;
import com.communote.server.api.core.property.PropertyEvent;
import com.communote.server.api.core.property.PropertyEvent.PropertyEventType;
import com.communote.server.api.core.property.PropertyType;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.core.security.SecurityHelper;
import com.communote.server.model.property.Property;
import com.communote.server.model.property.Propertyable;
import com.communote.server.model.property.StringProperty;
import com.communote.server.model.user.UserNoteProperty;
import com.communote.server.persistence.property.PropertyDao;

/**
 * The Property Accessor provides means for setting and retrieving properties. The accessor
 * implementations shouldn't be used directly. They are intended to be registered at the
 * PropertyManagement instead.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 * @param <O>
 *            The type that got the properties
 * @param <P>
 *            The type of property
 * @param <V>
 *            The type of the value the property will handle
 */
// TODO Refactor object hierarchy: only ObjectPropertyAccessor work with properties that are bound
// to an object, thus all objectProperties methods should be there. The BinaryPropertyAccessor and
// UserNotePropertyAccessor are special. This class should only hold the common features which are
// the filters.
public abstract class PropertyAccessor<O extends Propertyable, P extends Property, V extends Object> {

    private static final Logger LOGGER = LoggerFactory.getLogger(PropertyAccessor.class);
    private final PropertyAccessorFilterDefinition filterDefinition = new PropertyAccessorFilterDefinition();

    private final EventDispatcher eventDispatcher;

    /**
     *
     * @param eventDispatcher
     *            the event dispatcher for dispatching event on property changes
     */
    public PropertyAccessor(EventDispatcher eventDispatcher) {
        this.eventDispatcher = eventDispatcher;
    }

    /**
     * Add a combination of a property key group and property key to the accessible properties.
     *
     * @param keyGroup
     *            the key group of the property that should be read and writable
     * @param key
     *            the key of the property that should be read and writable
     */
    public void addToFilterDefinition(String keyGroup, String key) {
        if (keyGroup == null) {
            throw new IllegalArgumentException("keyGroup cannot be null!");
        }
        if (key == null) {
            throw new IllegalArgumentException("key cannot be null!");
        }
        // add it to read and write filter
        getFilterDefintion().addPropertyToReadFilterDefinition(keyGroup, key);
        getFilterDefintion().addPropertyToWriteFilterDefinition(keyGroup, key);
    }

    /**
     * Load and check the existence of the object. Also checks for valid read or write access.
     *
     * @param id
     *            the ID of the object
     * @param writeAccess
     *            <code>true</code> if the current user needs to have write access to the object and
     *            its properties, <code>false</code> if only read access is required
     * @return the object
     * @throws NotFoundException
     *             in case the object has not been found
     * @throws AuthorizationException
     *             in case the current user has not the requested access level to the object
     */
    protected O assertLoadObject(Long id, boolean writeAccess) throws NotFoundException,
            AuthorizationException {
        O object = load(id);
        if (object == null) {
            throw new NotFoundException("Object for id='" + id + "' not found! clazz="
                    + this.getClass().getName());
        }
        if (writeAccess) {
            assertWriteAccess(object);
        } else {
            assertReadAccess(object);
        }
        return object;
    }

    /**
     * Asserts valid read access for the given object.
     *
     * @param object
     *            The object.
     * @throws AuthorizationException
     *             Thrown, when the read access to the object was denied.
     */
    protected abstract void assertReadAccess(O object) throws AuthorizationException,
            NotFoundException;

    /**
     * Check that the group and key are both set and that the filters allow setting the property
     *
     * @param keyGroup
     *            the key group of the property
     * @param key
     *            the key of the property
     * @return true if group and key are valid, false otherwise
     */
    protected boolean assertValidForCreateAndUpdate(String keyGroup, String key) {
        if (keyGroup == null) {
            throw new IllegalArgumentException("keyGroup cannot be null!");
        }
        if (key == null) {
            throw new IllegalArgumentException("key cannot be null!");
        }
        if (!getFilterDefintion().isPropertyAllowedToSet(keyGroup, key)) {
            LOGGER.warn("Setting the object property with key group {} and key {} is not allowed",
                    keyGroup, key);
            // TODO throw an appropriate exception. Be aware that calling XyzManagement classes are
            // currently not prepared for this. When throwing a checked exception transactions might
            // need to be rolled back manually.
            return false;
        }
        return true;
    }

    /**
     * Method to assert, that the current user has write access to the given object.
     *
     * @param object
     *            The object to assert write access for.
     * @throws AuthorizationException
     *             Thrown, when the current user is not allowed to write to the given object.
     */
    protected abstract void assertWriteAccess(O object) throws AuthorizationException,
            NotFoundException;

    /**
     * @return the filter definition of this accessor
     */
    protected PropertyAccessorFilterDefinition getFilterDefintion() {
        return filterDefinition;
    }

    /**
     * Get an global property, that is one with no key group (internal the key group will be global
     * however).
     *
     * @param objectId
     *            the id of the object to get the property of
     * @param key
     *            the key of the property
     * @return the property or null if the property does not exists or cannot be read because of a
     *         filter definition
     * @throws NotFoundException
     *             in case there is not object to the given id.
     * @throws AuthorizationException
     *             Thrown, when the user is not allowed to access.
     */
    public final P getGlobalObjectProperty(Long objectId, String key) throws NotFoundException,
            AuthorizationException {
        return handleGetObjectPropertyUnfiltered(assertLoadObject(objectId, false),
                Property.KEY_GROUP_GLOBAL, key);
    }

    /**
     *
     * @param object
     *            the object the property is / will be assigned to
     * @return the id of this object
     */
    protected abstract Long getObjectId(O object);

    /**
     * Get an object property for the given group and key
     *
     * @param objectId
     *            the id of the object to get the property of
     * @param keyGroup
     *            the key group of the property to get
     * @param key
     *            the key of the property
     * @return the property or null if no such property exists or cannot be read because of a filter
     *         definition
     * @throws NotFoundException
     *             in case there is not object to the given id.
     * @throws AuthorizationException
     *             Thrown, when the user is not allowed to access.
     */
    public final P getObjectProperty(Long objectId, String keyGroup, String key)
            throws NotFoundException, AuthorizationException {
        if (keyGroup == null || key == null
                || !getFilterDefintion().isPropertyAllowedToGet(keyGroup, key)) {
            // TODO throw an exception?
            return null;
        }
        return handleGetObjectPropertyUnfiltered(assertLoadObject(objectId, false), keyGroup, key);
    }

    /**
     * Get an object property for the given group and key. The filter definition will not be
     * checked.
     *
     * @param objectId
     *            the id of the object to get the property of
     * @param keyGroup
     *            the key group of the property to get
     * @param key
     *            the key of the property
     * @return the property or null if no such property exists
     * @throws NotFoundException
     *             in case there is not object to the given id.
     * @throws AuthorizationException
     *             Thrown, when the user is not allowed to access.
     */
    public final P getObjectPropertyUnfiltered(Long objectId, String keyGroup, String key)
            throws NotFoundException, AuthorizationException {
        if (keyGroup == null || key == null) {
            return null;
        }
        return handleGetObjectPropertyUnfiltered(assertLoadObject(objectId, false), keyGroup, key);
    }

    /**
     *
     * @return the property type this accessor is handling
     */
    public abstract PropertyType getPropertyType();

    /**
     * Create a new property and associate it with the object
     *
     * @param object
     *            the object to add a new property to
     * @return the property created
     */
    protected abstract P handleCreateNewProperty(O object);

    /**
     * Create the property in the databse using the given values.
     *
     * @param object
     *            the object to add the property to
     * @param keyGroup
     *            the key group identifier
     * @param key
     *            the key
     * @param value
     *            the value
     * @return the property
     */
    private P handleCreateProperty(O object, String keyGroup, String key, V value) {
        P property = handleCreateNewProperty(object);

        property.setKeyGroup(keyGroup);
        property.setPropertyKey(key);
        property.setLastModificationDate(new Date());
        setPropertyValue(property, value);

        PropertyDao propertyDao = ServiceLocator.findService(PropertyDao.class);
        propertyDao.create(property);

        return property;
    }

    /**
     * Get an object property for the given group and key
     *
     * @param object
     *            the object to get the property of
     * @param keyGroup
     *            the key group of the property to get
     * @param key
     *            the key of the propert no such property exists
     * @return the property or null if no such property exists
     * @throws AuthorizationException
     *             Thrown, when the user is not allowed to access the property.
     */
    protected abstract P handleGetObjectPropertyUnfiltered(O object, String keyGroup, String key)
            throws AuthorizationException;

    /**
     * Removes the property of an object
     *
     * @param object
     *            object to get the property of
     * @param keyGroup
     *            the key group of the property to get
     * @param key
     *            the key of the property
     * @throws AuthorizationException
     *             Thrown, when the user is not allowed to access.
     */
    protected void handleRemoveObjectProperty(O object, String keyGroup, String key)
            throws AuthorizationException {
        P property = handleGetObjectPropertyUnfiltered(object, keyGroup, key);
        if (property != null) {
            PropertyDao propertyDao = ServiceLocator.findService(PropertyDao.class);
            propertyDao.remove(property);
        }
    }

    /**
     * Adds a new, modifies or deletes an existing property.
     *
     * @param object
     *            the object for which the properties should be modified
     * @param keyGroup
     *            the key group of the property to set
     * @param key
     *            the key of the property
     * @param value
     *            the value of the property, can be null to delete the property
     * @return the changed or created property. Null will be returned if the property did not exist
     *         and the provided value was null.
     * @throws AuthorizationException
     *             Thrown, when the user is not allowed to access the object.
     */
    protected final P handleSetObjectProperty(O object, String keyGroup, String key, V value)
            throws AuthorizationException {
        if (!assertValidForCreateAndUpdate(keyGroup, key)) {
            return null;
        }
        return handleSetObjectPropertyUnfiltered(object, keyGroup, key, value);
    }

    /**
     * Adds a new, modifies or deletes an existing property. The filter definition will not be
     * checked.
     *
     * @param object
     *            the object for which the properties should be modified
     * @param keyGroup
     *            the key group of the property to set
     * @param key
     *            the key of the property
     * @param value
     *            the value of the property, can be null to delete the property
     * @return the changed or created property. Null will be returned if the property did not exist
     *         and the provided value was null.
     * @throws AuthorizationException
     *             Thrown, when the user is not allowed to access the object.
     */
    protected P handleSetObjectPropertyUnfiltered(O object, String keyGroup, String key, V value)
            throws AuthorizationException {
        PropertyEvent event = new PropertyEvent(getObjectId(object), getPropertyType(), keyGroup,
                key);

        // TODO with a binary property this can lead to out of memory although the value is not
        // needed
        P property = handleGetObjectPropertyUnfiltered(object, keyGroup, key);

        if (PropertyType.UserNoteProperty.equals(event.getPropertyType())) {
            Long userId;
            if (property == null) {
                userId = SecurityHelper.getCurrentUserId();
            } else {
                userId = ((UserNoteProperty) property).getUser().getId();
            }
            event.setUserId(userId);
        }
        // get value of existing property if it is a StringProperty
        if (property instanceof StringProperty) {
            StringProperty stringProperty = (StringProperty) property;
            event.setOldValue(stringProperty.getPropertyValue());
        }
        if (value instanceof String) {
            event.setNewValue((String) value);
        }

        if (property == null) {
            if (value != null) {
                property = handleCreateProperty(object, keyGroup, key, value);
                event.setPropertyEventType(PropertyEventType.CREATE);
            }
        } else if (value == null) {
            handleRemoveObjectProperty(object, keyGroup, key);
            event.setPropertyEventType(PropertyEventType.DELETE);
        } else {
            property.setLastModificationDate(new Date());
            setPropertyValue(property, value);
            event.setPropertyEventType(PropertyEventType.UPDATE);

        }

        eventDispatcher.fire(event);
        return property;
    }

    /**
     *
     * @param id
     *            the id of the object
     * @return the loaded object or null if it does not exist
     */
    protected abstract O load(Long id);

    /**
     * Remove a combination of a property key group and property key from the accessible properties.
     *
     * @param keyGroup
     *            the key group of the property
     * @param key
     *            the key of the property
     */
    public void removeFromFilterDefinition(String keyGroup, String key) {
        // remove from read and write
        getFilterDefintion().removePropertyFromReadFilterDefinition(keyGroup, key);
        getFilterDefintion().removePropertyFromWriteFilterDefinition(keyGroup, key);
    }

    /**
     * Removes the property of an object
     *
     * @param objectId
     *            the id of the object to get the property of
     * @param keyGroup
     *            the key group of the property to get
     * @param key
     *            the key of the property
     * @throws NotFoundException
     *             in case there is not object to the given id.
     * @throws AuthorizationException
     *             Thrown, when the user is not allowed to access.
     */
    public final void removeObjectProperty(Long objectId, String keyGroup, String key)
            throws NotFoundException, AuthorizationException {
        if (assertValidForCreateAndUpdate(keyGroup, key)) {
            handleRemoveObjectProperty(assertLoadObject(objectId, true), keyGroup, key);
        }
    }

    /**
     * Add a new, modify or delete an existing global property
     *
     * @param objectId
     *            the object to set the property on
     * @param key
     *            the key of the property
     * @param value
     *            the value of the property
     * @return the changed or created property. Null will be returned if the property did not exist
     *         and the provided value was null.
     * @throws NotFoundException
     *             in case there is not object to the given id.
     * @throws AuthorizationException
     *             Thrown, when the user is not allowed to access.
     */
    public final P setGlobalObjectProperty(Long objectId, String key, V value)
            throws NotFoundException, AuthorizationException {
        return handleSetObjectProperty(assertLoadObject(objectId, true), Property.KEY_GROUP_GLOBAL,
                key, value);
    }

    /**
     * Add a new, modify or delete an existing property. If the key group and key refer to a
     * property that is not in the filter definition the property won't be stored.
     *
     * @param objectId
     *            the id of the object for which the properties should be modified
     * @param keyGroup
     *            the key group of the property to set
     * @param key
     *            the key of the property
     * @param value
     *            the value of the property, can be null to delete the property
     * @return the changed or created property. Null will be returned if the property did not exist
     *         and the provided value was null.
     * @throws NotFoundException
     *             in case there is no object for the given ID
     * @throws AuthorizationException
     *             Thrown, when the user is not allowed to modify the object.
     */
    public final P setObjectProperty(Long objectId, String keyGroup, String key, V value)
            throws NotFoundException, AuthorizationException {
        return handleSetObjectProperty(assertLoadObject(objectId, true), keyGroup, key, value);
    }

    /**
     * Adds a new, modifies or deletes an existing property. The filter definition will not be
     * checked.
     *
     * @param objectId
     *            the id of the object for which the properties should be modified
     * @param keyGroup
     *            the key group of the property to set
     * @param key
     *            the key of the property
     * @param value
     *            the value of the property, can be null to delete the property
     * @return the changed or created property. Null will be returned if the property did not exist
     *         and the provided value was null.
     * @throws NotFoundException
     *             in case there is no object for the given ID
     * @throws AuthorizationException
     *             Thrown, when the user is not allowed to modify the object.
     */
    public final P setObjectPropertyUnfiltered(Long objectId, String keyGroup, String key, V value)
            throws NotFoundException, AuthorizationException {
        return handleSetObjectPropertyUnfiltered(assertLoadObject(objectId, true), keyGroup, key,
                value);
    }

    /**
     * @param property
     *            The property.
     * @param value
     *            The value.
     */
    protected abstract void setPropertyValue(P property, V value);
}
