package com.communote.server.core.external;

import java.util.Collection;

import org.springframework.transaction.annotation.Transactional;

import com.communote.server.api.core.blog.BlogAccessException;
import com.communote.server.api.core.blog.BlogNotFoundException;
import com.communote.server.api.core.common.NotFoundException;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.model.external.ExternalObject;

/**
 * <p>
 * Spring Service base class for <code>ExternalObjectManagementException</code>, provides access to
 * all services and entities referenced by this service.
 * </p>
 * 
 * @see ExternalObjectManagementException
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
@Transactional
public abstract class ExternalObjectManagementBase implements ExternalObjectManagement {

    @Override
    public ExternalObject assignExternalObject(Long blogId, ExternalObject externalObject)
            throws BlogNotFoundException, BlogAccessException,
            ExternalObjectAlreadyAssignedException, TooManyExternalObjectsPerTopicException,
            ExternalSystemNotConfiguredException {
        if (blogId == null) {
            throw new IllegalArgumentException(
                    "ExternalObjectManagement.assignOrUpdateExternalObject(blogId, externalObject) "
                            + "- blogId must not be null");
        }
        if (externalObject == null) {
            throw new IllegalArgumentException(
                    "ExternalObjectManagement.assignOrUpdateExternalObject(blogId, externalObject) "
                            + "- externalObjects must not be null");
        }
        try {
            return this.handleAssignExternalObject(blogId, externalObject);
        } catch (RuntimeException rt) {
            throw new ExternalObjectManagementException(
                    "Error performing ExternalObjectManagement."
                            + "assignOrUpdateExternalObject(blogId, externalObject) --> "
                            + rt,
                    rt);
        }
    }

    @Override
    public void assignOrUpdateExternalObjects(Long blogId,
            Collection<ExternalObject> externalObjects) throws BlogNotFoundException,
            BlogAccessException, ExternalObjectAlreadyAssignedException,
            TooManyExternalObjectsPerTopicException, ExternalSystemNotConfiguredException {
        if (blogId == null) {
            throw new IllegalArgumentException(
                    "ExternalObjectManagement.assignOrUpdateExternalObjects(blogId, externalObjects) "
                            + "- blogId must not be null");
        }
        if (externalObjects == null) {
            throw new IllegalArgumentException(
                    "ExternalObjectManagement.assignOrUpdateExternalObjects(blogId, externalObjects) "
                            + "- externalObjects must not be null");
        }
        try {
            handleAssignOrUpdateExternalObjects(blogId, externalObjects);
        } catch (RuntimeException e) {
            throw new ExternalObjectManagementException(
                    "Error performing ExternalObjectManagement."
                            + "assignOrUpdateExternalObjects(blogId, externalObjects) --> "
                            + e, e);
        }
    }

    @Override
    public java.util.Collection<ExternalObject> getExternalObjects(Long blogId)
            throws BlogNotFoundException, BlogAccessException {
        if (blogId == null) {
            throw new IllegalArgumentException(
                    "ExternalObjectManagementException.getExternalObjects(Long blogId) - 'blogId' must not be null");
        }
        try {
            return this.handleGetExternalObjects(blogId);
        } catch (RuntimeException rt) {
            throw new ExternalObjectManagementException(
                    "Error performing 'ExternalObjectManagementException.getExternalObjects(Long blogId)' --> "
                            + rt,
                    rt);
        }
    }

    /**
     * Performs the core logic for {@link #assignExternalObject(Long, ExternalObject)}
     * 
     * @param blogId
     *            the ID of the blog to assign the external object to
     * @param externalObject
     *            object describing the external object to assign
     * @return the assigned external object
     * @throws BlogNotFoundException
     *             in case the topic was not found
     * @throws BlogAccessException
     *             in case the user is not a manager of the blog
     * @throws ExternalObjectAlreadyAssignedException
     *             in case the external object is assigned to another blog than the provided one
     * @throws ExternalSystemNotConfiguredException
     * @throws TooManyExternalObjectsPerTopicException
     */
    protected abstract ExternalObject handleAssignExternalObject(Long blogId,
            ExternalObject externalObject) throws BlogNotFoundException,
            BlogAccessException, ExternalObjectAlreadyAssignedException,
            TooManyExternalObjectsPerTopicException, ExternalSystemNotConfiguredException;

    /**
     * Performs the core logic for {@link #assignOrUpdateExternalObjects(Long, Collection)}
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
     * @throws TooManyExternalObjectsPerTopicException
     */
    protected abstract void handleAssignOrUpdateExternalObjects(Long blogId,
            Collection<ExternalObject> externalObjects) throws BlogNotFoundException,
            BlogAccessException, ExternalObjectAlreadyAssignedException,
            TooManyExternalObjectsPerTopicException, ExternalSystemNotConfiguredException;

    /**
     * Performs the core logic for {@link #getExternalObjects(Long)}
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
    protected abstract java.util.Collection<ExternalObject> handleGetExternalObjects(Long blogId)
            throws BlogNotFoundException, BlogAccessException;

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
    protected abstract boolean handleIsExternalObjectAssigned(Long blogId,
            String externalSystemId, String externalObjectId)
            throws BlogAccessException, BlogNotFoundException;

    /**
     * Performs the core logic for {@link #removeExternalObject(Long)}
     * 
     * @param externalObjectId
     *            the ID of the external object to remove
     * @throws BlogAccessException
     *             in case the current user is not manager of the blog
     * @throws NotFoundException
     *             in case the external object does not exist
     */
    protected abstract void handleRemoveExternalObject(Long externalObjectId)
            throws BlogAccessException, NotFoundException;

    /**
     * Performs the core logic for {@link #removeExternalObject(Long, String, String)}
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
    protected abstract void handleRemoveExternalObject(Long blogId,
            String externalSystemId, String externalObjectId)
            throws BlogNotFoundException, BlogAccessException;

    /**
     * Performs the core logic for {@link #removeExternalObjectTrusted(Long)}
     * 
     * @param externalObjectId
     *            ID of the external object *
     * @throws NotFoundException
     *             in case the external object does not exist
     * @throws AuthorizationException
     *             in case the current user is not client manager
     */
    protected abstract void handleRemoveExternalObjectTrusted(
            Long externalObjectId) throws NotFoundException, AuthorizationException;

    /**
     * Performs the core logic for {@link #replaceExternalObjects(Long, Collection)}
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
    protected abstract void handleReplaceExternalObjects(Long blogId,
            Collection<ExternalObject> externalObjects) throws BlogNotFoundException,
            BlogAccessException, ExternalObjectAlreadyAssignedException,
            TooManyExternalObjectsPerTopicException, ExternalSystemNotConfiguredException;

    /**
     * Performs the core logic for {@link #updateExternalObject(Long, ExternalObject)}
     * 
     * @param blogId
     *            the ID of the blog the external object is assigned to
     * @param externalObject
     *            object with details about the external object which are used to resolve the
     *            assigned object and update its data
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
    protected abstract ExternalObject handleUpdateExternalObject(Long blogId,
            ExternalObject externalObject) throws BlogNotFoundException, NotFoundException,
            BlogAccessException, ExternalObjectAlreadyAssignedException;

    @Override
    public boolean isExternalObjectAssigned(Long blogId, String externalSystemId,
            String externalObjectId) throws BlogNotFoundException, BlogAccessException {
        if (blogId == null) {
            throw new IllegalArgumentException(
                    "ExternalObjectManagementException.isExternalObjectAssigned(blogId, externalSystemId,"
                            + " externalObjectId) - 'blogId' must not be null");
        }
        if (externalSystemId == null || externalSystemId.trim().length() == 0) {
            throw new IllegalArgumentException(
                    "ExternalObjectManagementException.isExternalObjectAssigned(blogId, externalSystemId,"
                            + " externalObjectId) - 'externalSystemId' must not be null or empty");
        }
        if (externalObjectId == null || externalObjectId.trim().length() == 0) {
            throw new IllegalArgumentException(
                    "ExternalObjectManagementException.isExternalObjectAssigned(blogId, externalSystemId,"
                            + " externalObjectId) - 'externalObjectId' must not be null or empty");
        }
        try {
            return this.handleIsExternalObjectAssigned(blogId, externalSystemId, externalObjectId);
        } catch (RuntimeException rt) {
            throw new ExternalObjectManagementException(
                    "Error performing 'ExternalObjectManagementException."
                            + "isExternalObjectAssigned(blogId, externalSystemId, externalObjectId)' --> "
                            + rt, rt);
        }
    }

    @Override
    public void removeExternalObject(Long externalObjectId) throws BlogAccessException,
            NotFoundException {
        if (externalObjectId == null) {
            throw new IllegalArgumentException(
                    "ExternalObjectManagementException.removeExternalObject(Long internalExternalObjectId) "
                            + "- 'internalExternalObjectId' must not be null");
        }
        try {
            this.handleRemoveExternalObject(externalObjectId);
        } catch (RuntimeException rt) {
            throw new ExternalObjectManagementException(
                    "Error performing 'ExternalObjectManagementException."
                            + "removeExternalObject(Long internalExternalObjectId)' --> "
                            + rt, rt);
        }
    }

    @Override
    public void removeExternalObject(Long blogId, String externalSystemId,
            String externalObjectId)
            throws BlogNotFoundException, BlogAccessException {
        if (blogId == null) {
            throw new IllegalArgumentException(
                    "ExternalObjectManagementException.removeExternalObject(blogId, externalSystemId,"
                            + " externalObjectId) - 'blogId' must not be null");
        }
        if (externalSystemId == null || externalSystemId.trim().length() == 0) {
            throw new IllegalArgumentException(
                    "ExternalObjectManagementException.removeExternalObject(blogId, externalSystemId,"
                            + " externalObjectId) - 'externalSystemId' must not be null or empty");
        }
        if (externalObjectId == null || externalObjectId.trim().length() == 0) {
            throw new IllegalArgumentException(
                    "ExternalObjectManagementException.removeExternalObject(blogId, externalSystemId,"
                            + " externalObjectId) - 'externalObjectId' must not be null or empty");
        }
        try {
            this.handleRemoveExternalObject(blogId, externalSystemId, externalObjectId);
        } catch (RuntimeException rt) {
            throw new ExternalObjectManagementException(
                    "Error performing 'ExternalObjectManagementException."
                            + "removeExternalObject(blogId, externalSystemId, externalObjectId)' --> "
                            + rt, rt);
        }
    }

    @Override
    public void removeExternalObjectTrusted(Long externalObjectId)
            throws NotFoundException, AuthorizationException {
        if (externalObjectId == null) {
            throw new IllegalArgumentException(
                    "ExternalObjectManagement.removeExternalObjectWithoutBlogManagementRights - "
                            + "'objectId' must not be null");
        }
        this.handleRemoveExternalObjectTrusted(externalObjectId);

    }

    @Override
    public void replaceExternalObjects(Long blogId, Collection<ExternalObject> externalObjects)
            throws BlogNotFoundException, BlogAccessException,
            ExternalObjectAlreadyAssignedException, TooManyExternalObjectsPerTopicException,
            ExternalSystemNotConfiguredException {
        if (blogId == null) {
            throw new IllegalArgumentException(
                    "ExternalObjectManagement.replaceExternalObjects(blogId, externalObjects) "
                            + "- blogId must not be null");
        }
        if (externalObjects == null) {
            throw new IllegalArgumentException(
                    "ExternalObjectManagement.replaceExternalObjects(blogId, externalObjects) "
                            + "- externalObjects must not be null");
        }
        try {
            handleReplaceExternalObjects(blogId, externalObjects);
        } catch (RuntimeException e) {
            throw new ExternalObjectManagementException(
                    "Error performing ExternalObjectManagement."
                            + "replaceExternalObjects(blogId, externalObjects) --> " + e, e);
        }
    }

    @Override
    public ExternalObject updateExternalObject(Long blogId, ExternalObject externalObject)
            throws BlogNotFoundException, NotFoundException, BlogAccessException,
            ExternalObjectAlreadyAssignedException {
        if (blogId == null) {
            throw new IllegalArgumentException(
                    "ExternalObjectManagementException.updateExternalObject(blogId, externalObject) "
                            + "- 'blogId' must not be null");
        }
        if (externalObject == null) {
            throw new IllegalArgumentException(
                    "ExternalObjectManagementException.updateExternalObject(blogId, externalObject) "
                            + "- 'externalObject' must not be null");
        }
        try {
            return this.handleUpdateExternalObject(blogId, externalObject);
        } catch (RuntimeException rt) {
            throw new ExternalObjectManagementException(
                    "Error performing 'ExternalObjectManagementException."
                            + "updateExternalObject(blogId, externalObject)' --> " + rt, rt);
        }
    }
}