package com.communote.server.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.communote.server.api.core.blog.BlogAccessException;
import com.communote.server.api.core.blog.BlogNotFoundException;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.core.blog.TopicHierarchyManagement;
import com.communote.server.core.blog.ToplevelTopicCannotBeChildException;
import com.communote.server.core.blog.ToplevelTopicIsAlreadyChildBlogManagementException;
import com.communote.server.core.vo.blog.TopicStructureTO;
import com.communote.server.persistence.blog.ParentIsAlreadyChildDataIntegrityViolationException;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
@Service
public class TopicHierarchyService {

    @Autowired
    private TopicHierarchyManagement topicHierarchyManagement;

    /**
     * Method to add a topic as child to another topic.
     * 
     * @param parentTopicId
     *            Id of the parent topic.
     * @param childTopicId
     *            Id of the child topic.
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
    public void addTopic(Long parentTopicId, Long childTopicId) throws BlogNotFoundException,
            BlogAccessException, ToplevelTopicCannotBeChildException,
            ParentIsAlreadyChildDataIntegrityViolationException {
        topicHierarchyManagement.addChildTopic(parentTopicId, childTopicId);
    }

    /**
     * Method to remove a topic as child from another topic.
     * 
     * @param parentTopicId
     *            Id of the parent topic.
     * @param childTopicId
     *            Id of the child topic.
     * @throws BlogAccessException
     *             in case the current user is not manager of the parent topic or has no read access
     *             to the child topic
     */
    public void removeTopic(Long parentTopicId, Long childTopicId) throws BlogAccessException {
        topicHierarchyManagement.removeChildTopic(parentTopicId, childTopicId);

    }

    /**
     * Update the topic structure for a given topic
     * 
     * @param topicId
     *            the ID of the topic
     * @param structureChangeTO
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
    public void updateTopicStructure(Long topicId, TopicStructureTO structureChangeTO)
            throws BlogNotFoundException, AuthorizationException,
            ToplevelTopicCannotBeChildException, ToplevelTopicIsAlreadyChildBlogManagementException {
        topicHierarchyManagement.updateTopicStructure(topicId, structureChangeTO);
    }

}
