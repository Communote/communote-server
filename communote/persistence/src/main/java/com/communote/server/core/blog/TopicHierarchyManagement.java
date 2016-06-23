package com.communote.server.core.blog;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.communote.server.api.core.blog.BlogAccessException;
import com.communote.server.api.core.blog.BlogNotFoundException;
import com.communote.server.api.core.blog.BlogRightsManagement;
import com.communote.server.api.core.config.type.ClientProperty;
import com.communote.server.api.core.event.EventDispatcher;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.core.security.SecurityHelper;
import com.communote.server.core.vo.blog.TopicStructureTO;
import com.communote.server.model.blog.Blog;
import com.communote.server.model.blog.BlogRole;
import com.communote.server.persistence.blog.BlogDao;
import com.communote.server.persistence.blog.ParentIsAlreadyChildDataIntegrityViolationException;
import com.communote.server.persistence.blog.ResolvedTopicToTopicDao;

/**
 * Management for
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
@Service
@Transactional
public class TopicHierarchyManagement {

    private static final Logger LOGGER = LoggerFactory.getLogger(TopicHierarchyManagement.class);
    @Autowired
    private ResolvedTopicToTopicDao resolvedTopicToTopicDao;
    @Autowired
    private EventDispatcher eventDispatcher;
    @Autowired
    private BlogDao blogDao;
    @Autowired
    private BlogRightsManagement blogRightsManagement;

    /**
     * Add a topic as child to another topic.
     * 
     * @param parentTopicId
     *            Id of the parent topic.
     * @param childTopicId
     *            Id of the child topic.
     * @return whether the topic was added or not
     * @throws BlogAccessException
     *             in case the current user is not manager of the parent topic or has no read access
     *             to the child topic
     * @throws BlogNotFoundException
     *             in case the parent and/or child topic do not exist
     * @throws ToplevelTopicCannotBeChildException
     *             in case the child topic is a top-level topic
     * @throws ParentIsAlreadyChildDataIntegrityViolationException
     *             in case the parent would be added as its own child
     */
    public boolean addChildTopic(Long parentTopicId, Long childTopicId)
            throws BlogNotFoundException, BlogAccessException, ToplevelTopicCannotBeChildException {
        Blog parentTopic = blogRightsManagement.getAndCheckBlogAccess(parentTopicId,
                BlogRole.MANAGER);
        Blog childTopic = getTopicToAddToParent(childTopicId);
        return internalAddChildTopic(parentTopic, childTopic);
    }

    /**
     * Get a topic for being added as a child topic to another topic.
     * 
     * @param childTopicId
     *            the ID of the topic to add
     * @return the topic
     * @throws BlogNotFoundException
     *             in case the topic does not exist
     * @throws BlogAccessException
     *             in case the current user has no read access to that topic
     * @throws ToplevelTopicCannotBeChildException
     *             in case the child topic is a top-level topic
     */
    private Blog getTopicToAddToParent(Long childTopicId) throws BlogNotFoundException,
            BlogAccessException, ToplevelTopicCannotBeChildException {
        Blog childTopic = blogRightsManagement.getAndCheckBlogAccess(childTopicId, BlogRole.VIEWER);
        if (childTopic.isToplevelTopic()) {
            throw new ToplevelTopicCannotBeChildException(childTopic.getId(), childTopic.getTitle());
        }
        return childTopic;
    }

    /**
     * Add a topic as child to another topic.
     * 
     * @param parentTopic
     *            the parent topic.
     * @param childTopic
     *            the child topic.
     * @return whether the topic was added or not
     * @throws ParentIsAlreadyChildDataIntegrityViolationException
     *             in case the parent would be added as its own child
     */
    private boolean internalAddChildTopic(Blog parentTopic, Blog childTopic)
            throws ParentIsAlreadyChildDataIntegrityViolationException {
        // test if already a direct or indirect child
        if (resolvedTopicToTopicDao.isChild(parentTopic.getId(), childTopic.getId())) {
            return false;
        }
        parentTopic.getChildren().add(childTopic);
        childTopic.getParents().add(parentTopic);
        resolvedTopicToTopicDao.connect(parentTopic.getId(), childTopic.getId());
        eventDispatcher.fire(new TopicHierarchyEvent(parentTopic.getId(), parentTopic.getTitle(),
                childTopic.getId(), childTopic.getTitle(), SecurityHelper.getCurrentUserId(),
                TopicHierarchyEvent.Type.ADD));
        return true;
    }

    /**
     * Remove a child topic from its parent
     * 
     * @param parentTopic
     *            The parent topic.
     * @param childTopic
     *            The child topic.
     * @return whether the child topic was removed
     */
    private boolean internalRemoveChildTopic(Blog parentTopic, Blog childTopic) {
        if (childTopic.getParents().remove(parentTopic)) {
            parentTopic.getChildren().remove(childTopic);
            resolvedTopicToTopicDao.disconnect(parentTopic.getId(), childTopic.getId());
            eventDispatcher.fire(new TopicHierarchyEvent(parentTopic.getId(), parentTopic
                    .getTitle(), childTopic.getId(), childTopic.getTitle(), SecurityHelper
                    .getCurrentUserId(), TopicHierarchyEvent.Type.REMOVE));
            return true;
        }
        return false;
    }

    /**
     * Removes all child and parent connections for the given topic.
     * 
     * @param topicId
     *            The topic to remove all connections for.
     * @throws BlogAccessException
     *             in case the current user is not manager of the topic to be removed from its topic
     *             structure
     */
    public void removeAllConnections(Long topicId) throws BlogAccessException {
        Blog topic = blogDao.load(topicId);
        if (topic == null) {
            // paranoid removal in case there is some corrupted data
            resolvedTopicToTopicDao.disconnect(topicId);
            LOGGER.warn("Tried to disconnect a non existend topic: {}", topicId);
            return;
        }
        if (!blogRightsManagement.currentUserHasManagementAccess(topicId)) {
            throw new BlogAccessException(
                    "Current user is not manager of the topic which should be removed from the topic structure",
                    topicId, BlogRole.MANAGER, null);
        }
        resolvedTopicToTopicDao.disconnect(topicId);
        for (Blog child : topic.getChildren()) {
            child.getParents().remove(topic);
        }
        for (Blog parent : topic.getParents()) {
            parent.getChildren().remove(topic);
        }
        topic.getChildren().clear();
        topic.getParents().clear();
    }

    /**
     * Remove a child topic from its parent
     * 
     * @param parentTopicId
     *            Id of the parent topic.
     * @param childTopicId
     *            Id of the child topic.
     * @return whether the child topic was removed
     * @throws BlogAccessException
     *             in case the current user is not manager of the parent topic or has no read access
     *             to the child topic
     */
    public boolean removeChildTopic(Long parentTopicId, Long childTopicId)
            throws BlogAccessException {
        Blog parentTopic = blogDao.load(parentTopicId);
        Blog childTopic = blogDao.load(childTopicId);
        if (childTopic == null || parentTopic == null) {
            // No need to throw an exception here, but we are paranoid and remove the resolved topic
            // relation to clean corrupted data if it exists
            resolvedTopicToTopicDao.disconnect(parentTopicId, childTopicId);
            LOGGER.warn(
                    "Tried to disconnect two topics where at least one does not exist: {} -> {}, {} -> {}",
                    parentTopicId, parentTopic == null, childTopicId, childTopic == null);
            return false;
        }
        if (!blogRightsManagement.currentUserHasManagementAccess(parentTopicId)) {
            throw new BlogAccessException("Current user is not manager of the parent topic",
                    parentTopicId, BlogRole.MANAGER, null);
        }
        if (!blogRightsManagement.currentUserHasReadAccess(childTopicId, false)) {
            throw new BlogAccessException("Current user has no read access to the child topic",
                    childTopicId, BlogRole.VIEWER, null);
        }
        return internalRemoveChildTopic(parentTopic, childTopic);
    }

    /**
     * Method to assert an set the flag for top level topic.
     * 
     * @param topic
     *            The topic to update.
     * @param toplevelTopic
     *            True, if this should become a top level topic.
     * @throws AuthorizationException
     *             in case the current user is not client manager
     * @throws ToplevelTopicIsAlreadyChildBlogManagementException
     *             in case the topic is already a child of another topic
     */
    private void setToplevelTopic(Blog topic, boolean toplevelTopic) throws AuthorizationException {
        if (toplevelTopic == topic.isToplevelTopic()) {
            return; // In any case, leave the status as it is.
        }
        if (!SecurityHelper.isClientManager()) {
            throw new AuthorizationException("The current user needs to be client manager "
                    + "to change the top-level flag of a topic");
        }
        if (toplevelTopic) {
            if (!ClientProperty.TOP_LEVEL_TOPICS_ENABLED
                    .getValue(ClientProperty.DEFAULT_TOP_LEVEL_TOPICS_ENABLED)) {
                throw new ToplevelTopicsDisabledBlogManagementException(topic.getId());
            }
            if (!topic.getParents().isEmpty()) {
                throw new ToplevelTopicIsAlreadyChildBlogManagementException(topic.getId());
            }
        }
        topic.setToplevelTopic(toplevelTopic);
    }

    /**
     * Update the topic structure for a given topic
     * 
     * @param topicId
     *            the ID of the topic
     * @param structureChange
     *            TO describing the changes to conduct
     * @throws BlogNotFoundException
     *             in case the topic or one of the child topics to add or remove do not exist
     * @throws AuthorizationException
     *             in case the the current user is not authorized change the top-level state or add
     *             or remove child topics
     * @throws ToplevelTopicCannotBeChildException
     *             in case one of the child topics to add is a top-level topic
     * @throws ToplevelTopicIsAlreadyChildBlogManagementException
     *             in case the topic should be made a top level topic but is already a child of
     *             another topic
     * @throws ParentIsAlreadyChildDataIntegrityViolationException
     *             in case the parent would be added as its own child
     */
    public void updateTopicStructure(Long topicId, TopicStructureTO structureChange)
            throws BlogNotFoundException, AuthorizationException,
            ToplevelTopicCannotBeChildException, ToplevelTopicIsAlreadyChildBlogManagementException {
        Blog topic = blogRightsManagement.getAndCheckBlogAccess(topicId, BlogRole.MANAGER);
        ArrayList<Blog> childTopicsToRemove = new ArrayList<Blog>();
        ArrayList<Blog> childTopicsToAdd = new ArrayList<Blog>();
        // fetch all topics and check access before changing something
        if (structureChange.getChildTopicsToRemove() != null) {
            for (Long id : structureChange.getChildTopicsToRemove()) {
                childTopicsToRemove.add(blogRightsManagement.getAndCheckBlogAccess(id,
                        BlogRole.VIEWER));
            }
        }
        if (structureChange.getChildTopicsToAdd() != null) {
            for (Long id : structureChange.getChildTopicsToAdd()) {
                childTopicsToAdd.add(getTopicToAddToParent(id));
            }
        }
        if (structureChange.getToplevel() != null) {
            this.setToplevelTopic(topic, structureChange.getToplevel());
        }
        for (Blog childTopic : childTopicsToAdd) {
            internalAddChildTopic(topic, childTopic);
        }
        for (Blog childTopic : childTopicsToRemove) {
            internalRemoveChildTopic(topic, childTopic);
        }
    }
}
