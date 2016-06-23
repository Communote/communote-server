package com.communote.server.core.external;

import java.util.Collection;

import com.communote.common.converter.Converter;
import com.communote.common.util.Pair;
import com.communote.server.api.core.blog.BlogAccessException;
import com.communote.server.api.core.blog.BlogNotFoundException;
import com.communote.server.api.core.common.NotFoundException;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.model.blog.Blog;
import com.communote.server.model.external.ExternalObject;

/**
 * Management functions for the external objects
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public interface ExternalObjectManagement {

    /**
     * Assign an external object to a topic. If the external object is already assigned to the topic
     * nothing will happen. If it is assigned to another topic an exception will be thrown. If it is
     * not assigned it is created, the provided properties are added and it is assigned to the
     * topic.
     *
     * @param blogId
     *            the ID of the blog to assign the external object to
     * @param externalObject
     *            object describing the external object to assign
     * @return the assigned external object
     * @throws BlogNotFoundException
     *             in case the blog was not found
     * @throws BlogAccessException
     *             in case the user is not a manager of the blog
     * @throws ExternalObjectAlreadyAssignedException
     *             in case the external object is assigned to another blog than the provided one
     * @throws ExternalSystemNotConfiguredException
     *             in case the externalSystemId of the external object does not belong to a
     *             registered or active external object source. A source is considered active if it
     *             provides a valid configuration.
     * @throws TooManyExternalObjectsPerTopicException
     *             in case there are already assignments for the external source and topic and the
     *             configuration does not allow more assignments
     */
    public ExternalObject assignExternalObject(Long blogId, ExternalObject externalObject)
            throws BlogNotFoundException, BlogAccessException,
            ExternalObjectAlreadyAssignedException, TooManyExternalObjectsPerTopicException,
            ExternalSystemNotConfiguredException;

    /**
     * Assign or update a collection of external objects. If one of the provided external objects is
     * already assigned to the blog it will be updated. If it is not assigned it will be added. The
     * update works as {@link #updateExternalObject(Long, ExternalObject)} and the assign like
     * {@link #assignExternalObject(Long, ExternalObject)}
     *
     * @param blogId
     *            the ID of the blog to assign the external objects to
     * @param externalObjects
     *            objects describing the external objects to assign or update. To find an existing
     *            external object the external ID and the external system ID members are used if set
     *            otherwise the ID member is used.
     * @throws BlogNotFoundException
     *             in case the blog was not found
     * @throws BlogAccessException
     *             in case the user is not a manager of the blog
     * @throws ExternalObjectAlreadyAssignedException
     *             in case one of the external objects is assigned to another blog than the provided
     *             one
     * @throws ExternalSystemNotConfiguredException
     *             in case the externalSystemId of the external object does not belong to a
     *             registered or active external object source. A source is considered active if it
     *             provides a valid configuration.
     * @throws TooManyExternalObjectsPerTopicException
     *             in case the configuration does not allow that many assignments
     */
    public void assignOrUpdateExternalObjects(Long blogId,
            Collection<ExternalObject> externalObjects) throws BlogNotFoundException,
            BlogAccessException, ExternalObjectAlreadyAssignedException,
            TooManyExternalObjectsPerTopicException, ExternalSystemNotConfiguredException;

    /**
     * Get an external object which is identified by the given ID and is assigned to the given
     * topic.
     *
     * @param topicId
     *            the ID of the topic
     * @param externalObjectId
     *            the internal ID of the external object
     * @param converter
     *            the converter to transform the external object in the result object
     * @return the converted result or null if the external object does not exist
     * @throws BlogNotFoundException
     *             in case the topic does not exist
     * @throws BlogAccessException
     *             in case the current user has no access to the given topic
     */
    public <T> T getExternalObject(Long topicId, Long externalObjectId,
            Converter<Pair<Blog, ExternalObject>, T> converter)
                    throws BlogNotFoundException, BlogAccessException;

    /**
     * <p>
     * Return the external objects that are assigned to the given blog. The properties will not be
     * loaded. Use the property management to get them.
     * </p>
     *
     * @param blogId
     *            the ID of the blog
     * @return the external objects of the topic. The collection will be empty if the blog has no
     *         external objects.
     * @throws BlogNotFoundException
     *             in case the blog does not exist
     * @throws BlogAccessException
     *             in case the current user has no read access to the blog
     */
    public java.util.Collection<ExternalObject> getExternalObjects(Long blogId)
            throws BlogNotFoundException, BlogAccessException;

    /**
     * <p>
     * Return whether a given blog has external objects.
     * </p>
     *
     * @param blogId
     *            the ID of the blog
     * @return true if there is at least one external object, false otherwise
     * @throws BlogNotFoundException
     *             in case the blog does not exist
     * @throws BlogAccessException
     *             in case the current user has no read access to the blog
     */
    public boolean hasExternalObjects(Long blogId) throws BlogNotFoundException,
            BlogAccessException;

    /**
     * Check if a external object is assigned to a blog
     *
     * @param blogId
     *            the ID of the blog the external object is checked for beeing assigned to
     * @param externalSystemId
     *            the ID of the external system
     * @param externalObjectId
     *            the ID that identifies the external object within the external system
     * @return true if the object is assigned, false otherwise
     * @throws BlogAccessException
     *             in case the current user has no read access to the blog
     * @throws BlogNotFoundException
     *             in case there is no blog for the given ID
     */
    public boolean isExternalObjectAssigned(Long blogId, String externalSystemId,
            String externalObjectId) throws BlogAccessException,
            BlogNotFoundException;

    /**
     * Register an external object source. After registering a source external objects with the ID
     * of the source can be linked to topics.
     *
     * @param source
     *            the source to register
     * @throws ExternalObjectSourceAlreadyExistsException
     *             in case there is already a source with the same ID
     */
    public void registerExternalObjectSource(ExternalObjectSource source)
            throws ExternalObjectSourceAlreadyExistsException;

    /**
     * <p>
     * Remove the external object with the given ID. If the removed external object is the last of
     * the external system, the blog access rights added for that external system will be removed
     * too.
     * </p>
     *
     * @param externalObjectId
     *            the ID of the external object to remove
     * @throws BlogAccessException
     *             in case the current user is not manager of the blog
     * @throws NotFoundException
     *             in case the external object does not exist
     */
    public void removeExternalObject(Long externalObjectId) throws BlogAccessException,
            NotFoundException;

    /**
     * Remove an external object from a blog. If the removed external object is the last of the
     * external system, the blog access rights added for that external system will be removed too.
     *
     * @param blogId
     *            ID of the blog
     * @param externalSystemId
     *            identifier of the external system
     * @param externalObjectId
     *            identifier of the external object in the external system
     * @throws BlogNotFoundException
     *             in case the blog does not exist
     * @throws BlogAccessException
     *             in case the current user is not manager of the blog
     */
    public void removeExternalObject(Long blogId, String externalSystemId, String externalObjectId)
            throws BlogNotFoundException, BlogAccessException;

    /**
     * <p>
     * Remove the external object from the blog it is assigned to. If the removed external object is
     * the last of the external system, the blog access rights added for that external system will
     * be removed too.
     * </p>
     * <p>
     * Similar to the 'trusted' methods of BlogRightsManagement this method requires that the
     * current user is client manager.
     * </p>
     *
     * @param externalObjectId
     *            ID of the external object
     * @throws NotFoundException
     *             in case the external object does not exist
     * @throws AuthorizationException
     *             in case the current user is not client manager
     */
    public void removeExternalObjectTrusted(Long externalObjectId)
            throws NotFoundException, AuthorizationException;

    /**
     * Replace the external objects assigned to a blog with the provided ones. This method works
     * like {@link #assignOrUpdateExternalObjects(Long, Collection)} but additionally removes any
     * assigned external object that is not in the provided external objects.
     *
     * @param blogId
     *            the ID of the blog whose external objects should be replaced
     * @param externalObjects
     *            the new external objects to replace the existing with
     * @throws BlogNotFoundException
     *             in case the blog does not exist
     * @throws BlogAccessException
     *             in case the current user is not manager of the blog
     * @throws ExternalObjectAlreadyAssignedException
     *             in case one of the external objects is assigned to another blog than the provided
     *             one
     * @throws ExternalSystemNotConfiguredException
     * @throws TooManyExternalObjectsPerTopicException
     */
    public void replaceExternalObjects(Long blogId,
            Collection<ExternalObject> externalObjects) throws BlogNotFoundException,
            BlogAccessException, ExternalObjectAlreadyAssignedException,
            TooManyExternalObjectsPerTopicException, ExternalSystemNotConfiguredException;

    /**
     * Remove an external object source that was registered with
     * {@link #registerExternalObjectSource(ExternalObjectSource)}. If the source was not registered
     * the call is ignored
     *
     * @param source
     *            the source to remove
     */
    public void unregisterExternalObjectSource(ExternalObjectSource source);

    /**
     * <p>
     * Update an existing external object. This covers modification of the name and the properties.
     * The properties handling is as follows: If a property key and group combination does not
     * exist, the property is created. If the property key and group combination exists, the value
     * is updated. In case the value is null, the property is removed.
     * </p>
     *
     * @param blogId
     *            the ID of the blog the external object is assigned to
     * @param externalObject
     *            object with details about the external object which are used to resolve the
     *            assigned object and update its data. To find the existing external object the
     *            external ID and the external system ID members are used if set otherwise the ID
     *            member is used.
     * @return the updated external object
     * @throws BlogNotFoundException
     *             in case the blog does not exist
     * @throws BlogAccessException
     *             in case the current user is not manager of the blog
     * @throws NotFoundException
     *             in case the external object does not exist
     * @throws ExternalObjectAlreadyAssignedException
     *             in case the external object is already assigned to another blog than the provided
     *             one
     */
    public ExternalObject updateExternalObject(Long blogId, ExternalObject externalObject)
            throws BlogNotFoundException, BlogAccessException, NotFoundException,
            ExternalObjectAlreadyAssignedException;

}
