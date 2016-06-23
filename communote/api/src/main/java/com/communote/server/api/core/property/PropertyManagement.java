package com.communote.server.api.core.property;

import java.util.Collection;
import java.util.Date;
import java.util.Set;

import com.communote.common.converter.Converter;
import com.communote.server.api.core.common.NotFoundException;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.model.property.BinaryProperty;
import com.communote.server.model.property.StringProperty;
import com.communote.server.model.user.User;

/**
 * This service provides means to create, read, update and remove properties including the generic
 * binary property and the properties which are associated with an object like a note or a user.
 * <p>
 * When accessing properties a filter is applied on the group and key to decide whether the property
 * is accessible. Only properties whose group and key were registered with
 * {@link #addObjectPropertyFilter(PropertyType, String, String)} will be accessible.
 * </p>
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public interface PropertyManagement {
    /** Common key group for internal properties. */
    public static final String KEY_GROUP = "com.communote";

    /**
     * Add a combination of a property key group and property key to the accessible properties of
     * the given property type.
     *
     * @param propertyType
     *            the property type
     * @param keyGroup
     *            the key group of the property
     * @param propertyKey
     *            the key of the property
     */
    void addObjectPropertyFilter(PropertyType propertyType, String keyGroup, String propertyKey);

    /**
     * Get all filtered properties from a given object for the given property type
     *
     * @param propertyType
     *            the property type
     * @param objectId
     *            the object id
     * @return all filtered properties from the object
     * @throws AuthorizationException
     *             in case the user is not allowed to access the properties of the object
     * @throws NotFoundException
     *             in case the object does not exist
     */
    Set<StringPropertyTO> getAllObjectProperties(PropertyType propertyType, Long objectId)
            throws NotFoundException, AuthorizationException;

    /**
     * Get a binary property not bound to any context
     *
     * @param keyGroup
     *            the key group
     * @param key
     *            the key of the property
     * @return the property (or null)
     */
    BinaryProperty getBinaryProperty(String keyGroup, String key);

    /**
     * Get last modification date of a binary property.
     *
     * @param keyGroup
     *            group of the property key
     * @param key
     *            the key of the property
     * @return the last modification date or null if the property does not exist
     */
    Date getBinaryPropertyLastModificationDate(String keyGroup, String key);

    /**
     * Get a global property which is a property with the predefined key group "global"
     *
     * @param propertyType
     *            the type of property
     * @param objectId
     *            the id of the object to get the property for
     * @param key
     *            the key of the property
     * @return the property (or null)
     * @throws NotFoundException
     *             in case there is no object of the given property type and id
     * @throws AuthorizationException
     *             in case the user is not allowed to access the properties of the object
     */
    StringProperty getGlobalObjectProperty(PropertyType propertyType, Long objectId, String key)
            throws NotFoundException, AuthorizationException;

    /**
     * Get an object property
     *
     * @param propertyType
     *            the type of property
     * @param objectId
     *            the ID of the object whose property should be retrieved
     * @param keyGroup
     *            the key group of the property
     * @param key
     *            the key of the property
     * @return the property or null if there is no such property or the key and group combination is
     *         not in the allowed filters
     * @throws NotFoundException
     *             in case there is no object of the given property type and id
     * @throws AuthorizationException
     *             in case the user is not allowed to access the properties of the object
     */
    StringProperty getObjectProperty(PropertyType propertyType, Long objectId, String keyGroup,
            String key) throws NotFoundException, AuthorizationException;

    /**
     * Get an object property. The filter of will not be applied.
     *
     * @param propertyType
     *            the type of property
     * @param objectId
     *            the ID of the object whose property should be retrieved
     * @param keyGroup
     *            the key group of the property
     * @param key
     *            the key of the property
     * @return the property or null if there is no such property
     * @throws NotFoundException
     *             in case there is no object of the given property type and id
     * @throws AuthorizationException
     *             in case the user is not allowed to access the properties of the object
     */
    StringProperty getObjectPropertyUnfiltered(PropertyType propertyType, Long objectId,
            String keyGroup, String key) throws NotFoundException, AuthorizationException;

    /**
     * Return the users that have a specific note property set for a note. The found users are
     * converted into the target type of the provided converter. Deleted users won't be included.
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
    <T> Collection<T> getUsersOfProperty(Long noteId, String keyGroup, String key, String value,
            Converter<User, T> converter) throws NotFoundException, AuthorizationException;

    /**
     * Test whether a binary property exists.
     *
     * @param keyGroup
     *            the key group
     * @param key
     *            the key of the property
     * @return True, if this property exists.
     */
    boolean hasBinaryProperty(String keyGroup, String key);

    /**
     * Test whether the current user has a given property for a note.
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
    boolean hasUserNoteProperty(Long noteId, String keyGroup, String key, String value)
            throws NotFoundException, AuthorizationException;

    /**
     * Remove a binary property.
     *
     * @param keyGroup
     *            The group of the property key.
     * @param propertyKey
     *            The properties key.
     *
     */
    void removeBinaryProperty(String keyGroup, String propertyKey);

    /**
     * Remove a combination of a property key group and property key from the accessible properties
     * of the given type.
     *
     * @param propertyType
     *            the property type
     * @param keyGroup
     *            the key group of the property
     * @param propertyKey
     *            the key of the property
     */
    void removeObjectPropertyFilter(PropertyType propertyType, String keyGroup, String propertyKey);

    /**
     * Set a binary property.
     *
     * @param keyGroup
     *            the key group
     * @param key
     *            the key of the property
     * @param value
     *            the value to set
     * @return the property
     * @throws AuthorizationException
     *             in case the user is not allowed to access the properties of the object
     */
    BinaryProperty setBinaryProperty(String keyGroup, String key, byte[] value)
            throws AuthorizationException;

    /**
     * Set a global property which is a property with the predefined key group will be "global".
     *
     * @param propertyType
     *            the type of property
     * @param objectId
     *            the id of the object to set the property for
     * @param key
     *            the key of the property
     * @param value
     *            the value to set
     * @return the property
     * @throws NotFoundException
     *             in case there is no object of the given property type and id
     * @throws AuthorizationException
     *             in case the user is not allowed to access the properties of the object
     */
    StringProperty setGlobalObjectProperty(PropertyType propertyType, Long objectId, String key,
            String value) throws NotFoundException, AuthorizationException;

    /**
     * Set several properties of an object.
     *
     * @param propertyType
     *            the type of property
     * @param objectId
     *            the id of the object to set the property for
     * @param properties
     *            Set of transfer objects describing the new properties or property values
     * @throws NotFoundException
     *             in case there is no object of the given property type and id
     * @throws AuthorizationException
     *             in case the user is not allowed to access the properties of the object
     */
    void setObjectProperties(PropertyType propertyType, Long objectId,
            Set<StringPropertyTO> properties) throws NotFoundException, AuthorizationException;

    /**
     * Set a new value of a property. If the property does not exist it will be created and assigned
     * to the object. If the value to set is null, an existing property will be removed. In case the
     * property does not exist nothing will happen.
     *
     * @param propertyType
     *            the type of property
     * @param objectId
     *            the id of the object to set the property for
     * @param keyGroup
     *            the key group of the property
     * @param key
     *            the key of the property
     * @param value
     *            the value to set. Can be null to remove an existing property.
     * @return the created or modified property. Can be null if the value was null and the property
     *         did not exist.
     * @throws NotFoundException
     *             in case there is no object of the given property type and id
     * @throws AuthorizationException
     *             in case the user is not allowed to access the properties of the object
     *
     */
    // TODO use generics to return the correct property subtype - but why return the property
    // anyway?
    StringProperty setObjectProperty(PropertyType propertyType, Long objectId, String keyGroup,
            String key, String value) throws NotFoundException, AuthorizationException;

}