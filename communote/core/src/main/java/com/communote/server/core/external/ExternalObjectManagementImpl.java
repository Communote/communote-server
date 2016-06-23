package com.communote.server.core.external;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.communote.common.converter.Converter;
import com.communote.common.util.Pair;
import com.communote.server.api.core.blog.BlogAccessException;
import com.communote.server.api.core.blog.BlogNotFoundException;
import com.communote.server.api.core.blog.BlogRightsManagement;
import com.communote.server.api.core.common.NotFoundException;
import com.communote.server.api.core.property.PropertyManagement;
import com.communote.server.api.core.property.PropertyType;
import com.communote.server.api.core.property.StringPropertyTO;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.core.blog.helper.BlogRoleHelper;
import com.communote.server.core.security.SecurityHelper;
import com.communote.server.model.blog.Blog;
import com.communote.server.model.blog.BlogMember;
import com.communote.server.model.blog.BlogRole;
import com.communote.server.model.external.ExternalObject;
import com.communote.server.model.external.ExternalObjectProperty;
import com.communote.server.persistence.blog.BlogDao;
import com.communote.server.persistence.external.ExternalObjectDao;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
@Service("externalObjectManagement")
public class ExternalObjectManagementImpl extends ExternalObjectManagementBase {

    /** Logger. */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(ExternalObjectManagementImpl.class);

    private final Map<String, ExternalObjectSource> externalObjectSources = new HashMap<>();

    @Autowired
    private PropertyManagement propertyManagement;

    @Autowired
    private BlogDao blogDao;
    @Autowired
    private ExternalObjectDao externalObjectDao;

    @Autowired
    private BlogRightsManagement blogRightsManagement;

    /**
     * Assert that the blog exists and the current user has at least the given role for a blog
     *
     * @param blogId
     *            identifier of the blog
     * @param requiredRole
     *            the role the current user has to have
     * @return the blog
     * @throws BlogAccessException
     *             in case the current user has not the required access role
     * @throws BlogNotFoundException
     *             in case the blog does not exist
     */
    private Blog assertBlogAccess(Long blogId, BlogRole requiredRole) throws BlogAccessException,
            BlogNotFoundException {
        Blog blog = blogDao.load(blogId);
        if (blog == null) {
            throw new BlogNotFoundException("Blog does not exist", blogId, null);
        }
        BlogRole role = blogRightsManagement.getRoleOfCurrentUser(blogId, false);
        if (!BlogRoleHelper.sufficientAccess(role, requiredRole)) {
            throw new BlogAccessException("User has not the required blog access", blogId,
                    requiredRole, role);
        }
        return blog;
    }

    /**
     * Get the blog that is assigned to an external object.
     *
     * @param externalObject
     *            the external object, can be null
     * @param externalObjectId
     *            the ID of the externalObject, only needed for useful error message
     * @return the blog
     */
    private Blog findBlogByExternalObject(ExternalObject externalObject, Long externalObjectId) {
        if (externalObject != null) {
            Blog blog = blogDao.findByExternalObject(externalObject.getId());
            if (blog != null) {
                return blog;
            } else {
                LOGGER.warn("Found unassigned external object with ID {}. Will delete it.",
                        externalObject.getId());
                externalObjectDao.remove(externalObject);
            }
        }
        return null;
    }

    /**
     * Get the blog that is associated with an external object.
     *
     * @param externalObject
     *            the external object, can be null
     * @param externalObjectId
     *            the ID of the externalObject, only needed for useful error message
     * @return the blog
     * @throws NotFoundException
     *             in case the provided externalObject is null or it is not associated with a blog
     */
    private Blog findBlogByExternalObjectAssertExists(ExternalObject externalObject,
            Long externalObjectId) throws NotFoundException {
        Blog blog = findBlogByExternalObject(externalObject, externalObjectId);
        if (blog == null) {
            throw new NotFoundException("The external object with ID " + externalObjectId
                    + " does not exist");
        }
        return blog;
    }

    /**
     * Find an external object that is assigned to a given blog and has the given external system
     * identifier and external object identifier.
     *
     * @param blog
     *            blog the external object is assigned to
     * @param externalSystemId
     *            the external system id
     * @param externalObjectId
     *            the external object id of the external system
     * @return the external object or null
     */
    private ExternalObject findExternalObject(Blog blog, String externalSystemId,
            String externalObjectId) {
        if (blog.getExternalObjects() != null) {
            for (ExternalObject externalObject : blog.getExternalObjects()) {
                if (StringUtils.equals(externalSystemId, externalObject.getExternalSystemId())
                        && StringUtils.equals(externalObjectId, externalObject.getExternalId())) {
                    return externalObject;
                }
            }
        }
        return null;
    }

    /**
     * Find an external object that is assigned to a given blog and has the given external system
     * identifier and external object identifier. If the object is not assigned to the blog, it is
     * asserted that the object is not assigned to another blog.
     *
     * @param blog
     *            the blog the external object is assigned to
     * @param externalSystemId
     *            the external system id
     * @param externalObjectId
     *            the external object id of the external system
     * @return the external object or null
     * @throws ExternalObjectAlreadyAssignedException
     *             in case the external object is assigned to another blog
     */
    private ExternalObject findExternalObjectAssertNotAssignedToOtherBlog(Blog blog,
            String externalSystemId, String externalObjectId)
            throws ExternalObjectAlreadyAssignedException {
        ExternalObject externalObject = findExternalObject(blog, externalSystemId, externalObjectId);
        if (externalObject == null) {
            // check if there is another topic that is already assigned to the external object
            Blog assignedBlog = blogDao.findByExternalObject(externalSystemId, externalObjectId);
            if (assignedBlog != null) {
                throw new ExternalObjectAlreadyAssignedException(
                        "The external object is already assigned", externalSystemId,
                        externalObjectId, assignedBlog.getId());
            }
        }
        return externalObject;
    }

    @Override
    public <T> T getExternalObject(Long topicId, Long externalObjectId,
            Converter<Pair<Blog, ExternalObject>, T> converter) throws BlogAccessException,
            BlogNotFoundException {
        Blog topic = assertBlogAccess(topicId, BlogRole.VIEWER);
        Collection<ExternalObject> externalObjects = topic.getExternalObjects();
        if (externalObjects != null) {
            for (ExternalObject externalObject : externalObjects) {
                if (externalObjectId.equals(externalObject.getId())) {
                    Pair<Blog, ExternalObject> source = new Pair<>(topic, externalObject);
                    return converter.convert(source);
                }
            }
        }
        return null;
    }

    @Override
    protected ExternalObject handleAssignExternalObject(Long blogId, ExternalObject externalObject)
            throws BlogNotFoundException, BlogAccessException,
            ExternalObjectAlreadyAssignedException, TooManyExternalObjectsPerTopicException,
            ExternalSystemNotConfiguredException {
        Blog topic = assertBlogAccess(blogId, BlogRole.MANAGER);

        ExternalObject assignedExternalObject = findExternalObjectAssertNotAssignedToOtherBlog(
                topic, externalObject.getExternalSystemId(), externalObject.getExternalId());
        // return external object if already assigned
        if (assignedExternalObject != null) {
            return assignedExternalObject;
        }

        return internalAssignExternalObject(topic, externalObject);
    }

    @Override
    protected void handleAssignOrUpdateExternalObjects(Long blogId,
            Collection<ExternalObject> externalObjects) throws BlogNotFoundException,
            BlogAccessException, ExternalObjectAlreadyAssignedException,
            TooManyExternalObjectsPerTopicException, ExternalSystemNotConfiguredException {
        internalAssignOrUpdateExternalObjects(blogId, externalObjects, false);
    }

    @Override
    protected Collection<ExternalObject> handleGetExternalObjects(Long topicId)
            throws BlogNotFoundException, BlogAccessException {
        // only need read access
        Blog blog = assertBlogAccess(topicId, BlogRole.VIEWER);
        // avoid lazy init exceptions for external objects and their properties
        Collection<ExternalObject> externalObjects = blog.getExternalObjects();
        if (externalObjects != null) {
            externalObjects.size();
        } else {
            externalObjects = Collections.emptyList();
        }
        return externalObjects;
    }

    @Override
    protected boolean handleIsExternalObjectAssigned(Long blogId, String externalSystemId,
            String externalObjectId) throws BlogNotFoundException, BlogAccessException {
        Blog blog = assertBlogAccess(blogId, BlogRole.VIEWER);
        // return whether it is assigned
        return findExternalObject(blog, externalSystemId, externalObjectId) != null;
    }

    @Override
    protected void handleRemoveExternalObject(Long externalObjectId) throws NotFoundException,
            BlogAccessException {
        ExternalObject externalObject = externalObjectDao.load(externalObjectId);
        Blog blog = findBlogByExternalObjectAssertExists(externalObject, externalObjectId);
        BlogRole currentUserRole = blogRightsManagement.getRoleOfCurrentUser(blog.getId(), false);
        if (BlogRole.MANAGER.equals(currentUserRole)) {
            internalRemoveExternalObject(blog, externalObject, true);
        } else {
            throw new BlogAccessException("The current user is not manager of the blog",
                    blog.getId(), BlogRole.MANAGER, currentUserRole);
        }
    }

    @Override
    protected void handleRemoveExternalObject(Long blogId, String externalSystemId,
            String externalObjectId) throws BlogNotFoundException, BlogAccessException {

        Blog blog = assertBlogAccess(blogId, BlogRole.MANAGER);
        ExternalObject object = findExternalObject(blog, externalSystemId, externalObjectId);
        if (object != null) {
            internalRemoveExternalObject(blog, object, true);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void handleRemoveExternalObjectTrusted(Long externalObjectId)
            throws NotFoundException, AuthorizationException {
        ExternalObject externalObject = externalObjectDao.load(externalObjectId);
        Blog blog = findBlogByExternalObjectAssertExists(externalObject, externalObjectId);
        if (SecurityHelper.isClientManager()) {
            internalRemoveExternalObject(blog, externalObject, true);
        } else {
            throw new AuthorizationException(
                    "The current user needs to be client manager to remove an external object in trusted mode");
        }

    }

    @Override
    protected void handleReplaceExternalObjects(Long blogId,
            Collection<ExternalObject> externalObjects) throws BlogNotFoundException,
            BlogAccessException, ExternalObjectAlreadyAssignedException,
            TooManyExternalObjectsPerTopicException, ExternalSystemNotConfiguredException {
        internalAssignOrUpdateExternalObjects(blogId, externalObjects, true);
    }

    @Override
    protected ExternalObject handleUpdateExternalObject(Long blogId,
            ExternalObject newExternalObject) throws BlogNotFoundException, BlogAccessException,
            NotFoundException, ExternalObjectAlreadyAssignedException {
        Blog blog = assertBlogAccess(blogId, BlogRole.MANAGER);
        ExternalObject externalObject = resolveExternalObject(blog, newExternalObject);
        if (externalObject == null) {
            throw new NotFoundException("External object does not exist");
        }
        internalUpdateExternalObject(externalObject, newExternalObject);
        return externalObject;
    }

    /**
     * Check whether the blog has an external object for the given external system.
     *
     * @param blog
     *            the blog to test for external objects
     * @param externalSystemId
     *            identifier of the external system
     * @return true if there is at least on external object for the given system ID
     */
    private boolean hasExternalObject(Blog blog, String externalSystemId) {
        if (blog.getExternalObjects() != null) {
            for (ExternalObject externalObject : blog.getExternalObjects()) {
                if (externalObject.getExternalSystemId().equals(externalSystemId)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasExternalObjects(Long blogId) throws BlogNotFoundException,
            BlogAccessException {
        Blog blog = assertBlogAccess(blogId, BlogRole.VIEWER);
        return blog.getExternalObjects() != null && !blog.getExternalObjects().isEmpty();
    }

    /**
     * Creates a new external object and assigns it to the blog.
     *
     * @param blog
     *            the to assign the external object to
     * @param externalObject
     *            external object providing the external name and ID, the system ID and the
     *            properties that should be used for the new external object
     * @return the created and assigned new external object
     * @throws TooManyExternalObjectsPerTopicException
     * @throws ExternalSystemNotConfiguredException
     */
    private ExternalObject internalAssignExternalObject(Blog blog, ExternalObject externalObject)
            throws TooManyExternalObjectsPerTopicException, ExternalSystemNotConfiguredException {

        ExternalObjectSource source = this.externalObjectSources.get(externalObject
                .getExternalSystemId());
        ExternalObjectSourceConfiguration conf;
        if (source != null) {
            conf = source.getConfiguration();
        } else {
            conf = null;
        }

        if (conf == null) {
            throw new ExternalSystemNotConfiguredException(
                    "External System is not configured. No external object can be assigned. externalObject="
                            + externalObject.attributesToString(),
                    externalObject.getExternalSystemId());

        }

        if (conf.getNumberOfMaximumExternalObjectsPerTopic() > 0) {
            // the number of external objects of the same system id connected to the topic
            int count = 0;
            for (ExternalObject existing : blog.getExternalObjects()) {
                if (StringUtils.equals(externalObject.getExternalSystemId(),
                        existing.getExternalSystemId())) {
                    count++;
                }
            }
            // if there are more or exactly the same number of external objects already connected to
            // the topic it is not allowed to add one more
            if (conf.getNumberOfMaximumExternalObjectsPerTopic() <= count) {
                throw new TooManyExternalObjectsPerTopicException(
                        "The external system does not allow that many external objects connected to a single topic. maxObjects: "
                                + conf.getNumberOfMaximumExternalObjectsPerTopic()
                                + " externalObject: " + externalObject.attributesToString(),
                        externalObject.getExternalSystemId(),
                        conf.getNumberOfMaximumExternalObjectsPerTopic());
            }
        }

        // properties are through #setExternalObjectProperties
        ExternalObject newExternalObject = ExternalObject.Factory.newInstance(
                externalObject.getExternalSystemId(), externalObject.getExternalId(),
                externalObject.getExternalName(), new HashSet<ExternalObjectProperty>());
        newExternalObject = externalObjectDao.create(newExternalObject);
        setExternalObjectProperties(newExternalObject, externalObject.getProperties());
        blog.getExternalObjects().add(newExternalObject);
        return newExternalObject;
    }

    /**
     * Assign or update a collection of external objects and optionally remove the external objects
     * that were not provided.
     *
     * @param blogId
     *            the ID of the blog
     * @param externalObjects
     *            the external objects to process
     * @param removeRemaining
     *            whether to remove any external object that is not in the externalObjects
     * @throws BlogNotFoundException
     *             in case the blog does not exist
     * @throws BlogAccessException
     *             in case the current user is not manager of the blog
     * @throws ExternalObjectAlreadyAssignedException
     *             in case one of the provided external objects is assigned to another blog
     * @throws ExternalSystemNotConfiguredException
     * @throws TooManyExternalObjectsPerTopicException
     */
    private void internalAssignOrUpdateExternalObjects(Long blogId,
            Collection<ExternalObject> externalObjects, boolean removeRemaining)
            throws BlogNotFoundException, BlogAccessException,
            ExternalObjectAlreadyAssignedException, TooManyExternalObjectsPerTopicException,
            ExternalSystemNotConfiguredException {
        Blog blog = assertBlogAccess(blogId, BlogRole.MANAGER);
        // first resolve the provided external objects into existing and new objects, update
        // afterwards. This is required to avoid a manual rollback when one of the provided external
        // objects is assigned to another blog than the provided one
        // ExternalObjectAlreadyAssignedException
        HashMap<ExternalObject, ExternalObject> externalObjectsToUpdate = new HashMap<ExternalObject, ExternalObject>();
        HashSet<Long> externalObjectIdsToUpdate = new HashSet<Long>();
        HashSet<String> externalSystemIds = new HashSet<String>();
        ArrayList<ExternalObject> externalObjectsToAssign = new ArrayList<ExternalObject>();
        for (ExternalObject providedExternalObject : externalObjects) {
            ExternalObject existingExternalObject = resolveExternalObject(blog,
                    providedExternalObject);
            if (existingExternalObject == null) {
                externalObjectsToAssign.add(providedExternalObject);
            } else {
                externalObjectsToUpdate.put(existingExternalObject, providedExternalObject);
                externalObjectIdsToUpdate.add(existingExternalObject.getId());
            }
        }
        if (removeRemaining) {
            ArrayList<ExternalObject> externalObjectsToRemove = new ArrayList<ExternalObject>();
            // get external objects currently assigned but not in the provided objects so they can
            // be removed. Also log the external system IDs of the objects to remove to be able to
            // clear the members for the external system.
            for (ExternalObject existingExternalObject : blog.getExternalObjects()) {
                if (!externalObjectIdsToUpdate.contains(existingExternalObject.getId())) {
                    externalObjectsToRemove.add(existingExternalObject);
                    externalSystemIds.add(existingExternalObject.getExternalSystemId());
                }
            }
            for (ExternalObject existingExternalObject : externalObjectsToRemove) {
                internalRemoveExternalObject(blog, existingExternalObject, false);
            }
        }
        for (Map.Entry<ExternalObject, ExternalObject> entry : externalObjectsToUpdate.entrySet()) {
            internalUpdateExternalObject(entry.getKey(), entry.getValue());
            externalSystemIds.remove(entry.getKey().getExternalSystemId());
        }
        for (ExternalObject newExternalObject : externalObjectsToAssign) {
            internalAssignExternalObject(blog, newExternalObject);
            externalSystemIds.remove(newExternalObject.getExternalSystemId());
        }
        // check the logged external system IDs to remove the members for that external system
        removeMembersForExternal(blog, externalSystemIds);
    }

    /**
     * Remove an external object from a blog.
     *
     * @param blog
     *            the blog the external object is assigned to
     * @param externalObject
     *            the external object to remove
     * @param removeMembers
     *            whether to remove members for the external system of the external object if there
     *            are no other external objects for that system
     */
    private void internalRemoveExternalObject(Blog blog, ExternalObject externalObject,
            boolean removeMembers) {
        blog.getExternalObjects().remove(externalObject);
        // remove external object from database, properties are removed automatically by cascade
        externalObjectDao.remove(externalObject);
        if (removeMembers && !hasExternalObject(blog, externalObject.getExternalSystemId())) {
            removeMembersForExternal(blog, externalObject.getExternalSystemId());
        }
    }

    /**
     * Update an existing external object by replacing the name and updating the properties.
     *
     * @param existingExternalObject
     *            the existing object to update
     * @param newExternalObject
     *            object with new data to set
     */
    private void internalUpdateExternalObject(ExternalObject existingExternalObject,
            ExternalObject newExternalObject) {
        // only update name and properties, another external ID or system ID is the assign use case
        existingExternalObject.setExternalName(newExternalObject.getExternalName());
        setExternalObjectProperties(existingExternalObject, newExternalObject.getProperties());
    }

    @Override
    @Transactional(propagation = Propagation.NEVER)
    public synchronized void registerExternalObjectSource(ExternalObjectSource source)
            throws ExternalObjectSourceAlreadyExistsException {
        if (this.externalObjectSources.containsKey(source.getIdentifier())) {
            throw new ExternalObjectSourceAlreadyExistsException(source.getIdentifier());
        }
        externalObjectSources.put(source.getIdentifier(), source);
    }

    /**
     * Test for a blog and a collection of external system IDs whether there are assigned external
     * objects and if there are none remove the members of the blog that were assigned for the
     * external system.
     *
     * @param blog
     *            the blog to test
     * @param externalSystemIds
     *            the external system IDs to test
     */
    private void removeMembersForExternal(Blog blog, Set<String> externalSystemIds) {
        for (String externalSystemId : externalSystemIds) {
            if (!hasExternalObject(blog, externalSystemId)) {
                removeMembersForExternal(blog, externalSystemId);
            }
        }
    }

    /**
     * Remove all blog members for a given external system
     *
     * @param blog
     *            the blog whose members should be removed
     * @param externalSystemId
     *            identifier of the external system
     */
    private void removeMembersForExternal(Blog blog, String externalSystemId) {

        Set<Long> entitysToRemove = new HashSet<Long>();

        for (BlogMember member : blog.getMembers()) {
            if (externalSystemId.equals(member.getExternalSystemId())) {
                entitysToRemove.add(member.getMemberEntity().getId());
            }
        }
        try {
            for (Long entity : entitysToRemove) {
                blogRightsManagement.removeMemberByEntityIdForExternal(blog.getId(), entity,
                        externalSystemId);
            }
        } catch (BlogNotFoundException e) {
            // the provided blog exists
            LOGGER.error("Unexpected exception", e);
            throw new ExternalObjectManagementException("Unexpected exception", e);
        } catch (BlogAccessException e) {
            // TODO is it correct that it can't occur? Currently the blog manager and the client
            // manager are allowed to remove the rights
            LOGGER.error("Unexpected exception", e);
            throw new ExternalObjectManagementException("Unexpected exception", e);
        }
    }

    /**
     * Resolve an existing external object that is assigned to the given blog.
     *
     * @param blog
     *            the blog the object should be assigned to
     * @param searchedExternalObject
     *            the container with details about the searched object. If externalId and
     *            externalSystemId are set they will be used to find the object. Otherwise the ID is
     *            used.
     * @return the found object or null
     * @throws ExternalObjectAlreadyAssignedException
     *             in case the external object exists but is assigned to another blog
     */
    private ExternalObject resolveExternalObject(Blog blog, ExternalObject searchedExternalObject)
            throws ExternalObjectAlreadyAssignedException {
        ExternalObject externalObject;
        // resolve the external object by external ID and system ID
        if (searchedExternalObject.getExternalId() != null
                && searchedExternalObject.getExternalSystemId() != null) {
            externalObject = findExternalObjectAssertNotAssignedToOtherBlog(blog,
                    searchedExternalObject.getExternalSystemId(),
                    searchedExternalObject.getExternalId());
        } else {
            // use the entity ID and ensure it is assigned to the same blog
            externalObject = externalObjectDao.load(searchedExternalObject.getId());
            Blog assignedBlog = findBlogByExternalObject(externalObject,
                    searchedExternalObject.getId());
            if (assignedBlog == null) {
                externalObject = null;
            } else {
                if (!assignedBlog.getId().equals(blog.getId())) {
                    throw new ExternalObjectAlreadyAssignedException(
                            "The external object is already assigned",
                            externalObject.getExternalSystemId(), externalObject.getExternalId(),
                            assignedBlog.getId());
                }
            }
        }
        return externalObject;
    }

    /**
     * Create, update or remove properties of an external object
     *
     * @param externalObject
     *            existing external object whose properties should be modified
     * @param externalObjectProperties
     *            the properties to set or modify
     */
    private void setExternalObjectProperties(ExternalObject externalObject,
            Set<ExternalObjectProperty> externalObjectProperties) {
        if (externalObjectProperties != null) {
            try {
                Set<StringPropertyTO> properties = new HashSet<StringPropertyTO>();
                for (ExternalObjectProperty externalObjectProperty : externalObjectProperties) {
                    StringPropertyTO stringPropertyTO = new StringPropertyTO();
                    stringPropertyTO.setKeyGroup(externalObjectProperty.getKeyGroup());
                    stringPropertyTO.setPropertyKey(externalObjectProperty.getPropertyKey());
                    stringPropertyTO.setPropertyValue(externalObjectProperty.getPropertyValue());
                    properties.add(stringPropertyTO);
                }
                propertyManagement.setObjectProperties(PropertyType.ExternalObjectProperty,
                        externalObject.getId(), properties);
            } catch (AuthorizationException e) {
                // only used in usernotepropertyaccessor
                // externalobjectpropertyaccessor can not thrown this exception
                LOGGER.error("Unexpected exception", e);
                throw new ExternalObjectManagementException("Unexpected exception", e);
            } catch (NotFoundException e) {
                // the provided external object exists
                LOGGER.error("Unexpected exception", e);
                throw new ExternalObjectManagementException("Unexpected exception", e);
            }
        }
    }

    @Override
    @Transactional(propagation = Propagation.NEVER)
    public synchronized void unregisterExternalObjectSource(ExternalObjectSource source) {
        ExternalObjectSource existingSource = this.externalObjectSources
                .get(source.getIdentifier());
        if (existingSource != null && existingSource.equals(source)) {
            externalObjectSources.remove(source.getIdentifier());
        }
    }

}
