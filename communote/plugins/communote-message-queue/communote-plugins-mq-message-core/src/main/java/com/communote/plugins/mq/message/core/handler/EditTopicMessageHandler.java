package com.communote.plugins.mq.message.core.handler;

import java.util.HashSet;
import java.util.Set;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.communote.plugins.mq.message.base.handler.CommunoteMessageHandler;
import com.communote.plugins.mq.message.base.message.CommunoteReplyMessage;
import com.communote.plugins.mq.message.core.data.property.StringProperty;
import com.communote.plugins.mq.message.core.data.tag.Tag;
import com.communote.plugins.mq.message.core.data.topic.BaseTopic;
import com.communote.plugins.mq.message.core.data.topic.Topic;
import com.communote.plugins.mq.message.core.handler.converter.TopicConverter;
import com.communote.plugins.mq.message.core.handler.exception.NoTopicIdentifierSpecifiedException;
import com.communote.plugins.mq.message.core.handler.exception.NoTopicSpecifiedException;
import com.communote.plugins.mq.message.core.message.topic.EditTopicMessage;
import com.communote.plugins.mq.message.core.message.topic.TopicReplyMessage;
import com.communote.plugins.mq.message.core.util.StoringPolicy;
import com.communote.plugins.mq.message.core.util.TopicUtils;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.blog.BlogAccessException;
import com.communote.server.api.core.blog.BlogIdentifierValidationException;
import com.communote.server.api.core.blog.BlogManagement;
import com.communote.server.api.core.blog.BlogNotFoundException;
import com.communote.server.api.core.blog.BlogRightsManagement;
import com.communote.server.api.core.blog.BlogTO;
import com.communote.server.api.core.blog.NonUniqueBlogIdentifierException;
import com.communote.server.api.core.property.PropertyManagement;
import com.communote.server.core.external.ExternalObjectManagement;
import com.communote.server.model.blog.Blog;

/**
 * The Class EditTopicMessageHandler.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
@Provides(specifications = CommunoteMessageHandler.class)
@Component
@Instantiate
public class EditTopicMessageHandler extends
        CommunoteMessageHandler<EditTopicMessage> {

    /** The LOG. */
    private static Logger LOG = LoggerFactory
            .getLogger(EditTopicMessageHandler.class);

    private BlogManagement blogManagement;
    private BlogRightsManagement rightsManagement;
    private ExternalObjectManagement externalObjectManagement;
    private PropertyManagement propertyManagement;

    private SecurityHelperWrapper securityHelper = new SecurityHelperWrapperImpl();

    /**
     * 
     * creates topic TO
     * 
     * @param message
     *            edit topic message
     * @param existingTopic
     *            existing topic
     * @return topic TO
     */
    private BlogTO createTopicTO(EditTopicMessage message, Blog existingTopic) {
        Topic topic = message.getTopic();
        BlogTO topicTO = new BlogTO();
        topicTO.setCreatorUserId(securityHelper.assertCurrentUserId());
        String description = message.isUpdateDescription() ? topic
                .getDescription() : existingTopic.getDescription();
        String title = message.isUpdateTitle() ? topic.getTitle()
                : existingTopic.getTitle();
        String alias = message.isUpdateAlias() ? topic.getTopicAlias()
                : existingTopic.getNameIdentifier();

        topicTO.setDescription(description);
        topicTO.setTitle(title);
        topicTO.setNameIdentifier(alias);

        Tag[] newTags = topic.getTags() != null ? topic.getTags() : new Tag[0];
        Set<com.communote.server.model.tag.Tag> existingTags = existingTopic
                .getTags() != null ? existingTopic.getTags()
                : new HashSet<com.communote.server.model.tag.Tag>();
        topicTO.setTags(TopicMessageHandlerUtils.extractTagTOs(newTags,
                existingTags, getTagsExtractionPolicy(message)));

        // topic properties processing
        StringProperty[] newProperties = topic.getProperties() == null ? new StringProperty[0]
                : topic.getProperties();
        topicTO.setProperties(TopicMessageHandlerUtils.extractPropertiesTO(
                newProperties, getPropertiesExtractionPolicy(message)));

        return topicTO;
    }

    /**
     * @param topic
     *            to be found
     * @param isTopicAliasUpdated
     *            specify whether topic alias was updated, if it was it cannot be used to extract
     *            topic
     * @return topic, stored in communote
     * @throws BlogNotFoundException
     *             in case topic does not exist
     * @throws BlogAccessException
     */
    private Blog getExistingTopic(Topic topic, boolean isTopicAliasUpdated)
            throws BlogNotFoundException, BlogAccessException {
        Long topicId = TopicUtils.getTopicId(topic);
        if (topicId != null) {
            return getTopicManagement().getBlogById(topicId, true);
        }
        throw new BlogNotFoundException("Topic not found for topic id " + topic.getTopicId()
                + " or alias " + topic.getTopicAlias(), topic.getTopicId(), topic.getTopicAlias());
    }

    /**
     * Get the storing police of external objects
     * 
     * @param message
     *            edit topic message
     * @return external object storing policy
     */
    private StoringPolicy getExternalObjectExtractionPolicy(EditTopicMessage message) {
        StoringPolicy res;
        if (message.isSetExternalObjects()) {
            res = StoringPolicy.SET_NEW;
        } else if (message.isMergeExternalObjects()) {
            res = StoringPolicy.MERGE;
        } else if (message.isDeleteAllExternalObjects()) {
            res = StoringPolicy.DELETE;
        } else {
            res = StoringPolicy.PRESERVE_EXISTING;
        }
        return res;
    }

    /**
     * @return the externalObjectManagement
     */
    public ExternalObjectManagement getExternalObjectManagement() {
        if (externalObjectManagement == null) {
            externalObjectManagement = ServiceLocator.instance().getService(
                    ExternalObjectManagement.class);
        }
        return externalObjectManagement;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.communote.plugins.mq.service.message.CntMessageHandler# getHandledMessageClass()
     */
    @Override
    public Class<EditTopicMessage> getHandledMessageClass() {
        return EditTopicMessage.class;
    }

    /**
     * @param message
     *            ET message
     * @return property storing policy
     */
    private StoringPolicy getPropertiesExtractionPolicy(EditTopicMessage message) {
        if (message.isMergeProperties()) {
            return StoringPolicy.MERGE;
        } else {
            return StoringPolicy.PRESERVE_EXISTING;
        }
    }

    /**
     * @return the propertyManagement
     */
    private PropertyManagement getPropertyManagement() {
        if (propertyManagement == null) {
            propertyManagement = ServiceLocator.instance().getService(PropertyManagement.class);
        }
        return propertyManagement;
    }

    /**
     * @param message
     *            ET Message
     * @return tags storing policy
     */
    private StoringPolicy getTagsExtractionPolicy(EditTopicMessage message) {
        StoringPolicy result;
        if (message.isSetTags()) {
            return StoringPolicy.SET_NEW;
        }
        if (message.isMergeTags()) {
            result = StoringPolicy.MERGE;
        } else if (message.isDeleteAllTags()) {
            result = StoringPolicy.DELETE;
        } else {
            result = StoringPolicy.PRESERVE_EXISTING;
        }
        return result;
    }

    /**
     * Gets the blog management.
     * 
     * @return the blog management
     */
    private BlogManagement getTopicManagement() {
        if (blogManagement == null) {
            blogManagement = ServiceLocator.instance().getService(BlogManagement.class);
        }
        return blogManagement;
    }

    /**
     * @return the lazily initialized blog rights management
     */
    private BlogRightsManagement getTopicRightsManagement() {
        if (rightsManagement == null) {
            rightsManagement = ServiceLocator.instance().getService(BlogRightsManagement.class);
        }
        return rightsManagement;
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public CommunoteReplyMessage handleMessage(EditTopicMessage message)
            throws Exception {
        try {
            LOG.debug("Message is received by EditTopicMessageHandler.");
            validateMessage(message);
            Blog existingTopic = getExistingTopic(message.getTopic(),
                    message.isUpdateAlias());
            if (existingTopic == null) {
                LOG.error("There exists no topic with id == "
                        + message.getTopic().getTopicId() + " or alias == "
                        + message.getTopic().getTopicAlias());
                throw new BlogNotFoundException("No Topic was found", message
                        .getTopic().getTopicId(), message.getTopic()
                        .getTopicAlias());
            }
            BlogTO topicTO = createTopicTO(message, existingTopic);
            Blog updatedTopic = getTopicManagement().updateBlog(existingTopic.getId(), topicTO);
            Topic mqTopic = message.getTopic();
            if (message.isUpdateTopicRights() && mqTopic.getTopicRights() != null) {
                getTopicRightsManagement().setAllCanReadAllCanWrite(existingTopic.getId(),
                        mqTopic.getTopicRights().isAllCanRead(),
                        mqTopic.getTopicRights().isAllCanWrite());
            }
            ExternalObjectManagement externalObjectManagement = getExternalObjectManagement();
            TopicMessageHandlerUtils.updateExternalObjects(updatedTopic.getId(),
                    message.getExternalSystemId(), message.getTopic().getExternalObjects(),
                    getExternalObjectExtractionPolicy(message), externalObjectManagement);

            LOG.debug("Blog was successfully updated");

            TopicReplyMessage replyMessage = new TopicReplyMessage();
            updatedTopic = getTopicManagement().getBlogById(updatedTopic.getId(), true);
            replyMessage
                    .setTopic(new TopicConverter(getPropertyManagement()).convertBlogToTopic(
                            updatedTopic,
                            externalObjectManagement.getExternalObjects(updatedTopic.getId())));
            return replyMessage;
        } catch (NonUniqueBlogIdentifierException e) {
            LOG.error("Alias " + message.getTopic().getTopicAlias()
                    + " cannot be used, since it already exists");
            throw e;
        } catch (BlogIdentifierValidationException e) {
            LOG.error("Alias " + message.getTopic().getTopicAlias()
                    + " is not valid");
            throw e;
        } catch (BlogAccessException e) {
            LOG.error(e.getMessage(), e);
            throw e;
        }

    }

    /**
     * Inits the.
     */
    @Validate
    public void init() {
        LOG.info("EditTopicMessageHandler is instantiated");
    }

    /**
     * checks whether any of topic identification means are provided
     * 
     * @param topic
     *            to be chcked
     * @param updateAlias
     *            flag, that specifies, whether topic alias is going to be updated or not
     * @return true if at least one identification mean was provided, false otherwise
     */
    private boolean isTopicIdentifierAvailable(BaseTopic topic,
            boolean updateAlias) {
        return topic.getTopicId() != null && topic.getTopicId() != 0
                || !updateAlias && topic.getTopicAlias() != null && !topic
                        .getTopicAlias().equals("");
    }

    /**
     * @param blogManagement
     *            the blogManagement to set
     */
    void setBlogManagement(BlogManagement blogManagement) {
        this.blogManagement = blogManagement;
    }

    /**
     * @param externalObjectManagement
     *            the externalObjectManagement to set
     */
    public void setExternalObjectManagement(ExternalObjectManagement externalObjectManagement) {
        this.externalObjectManagement = externalObjectManagement;
    }

    /**
     * @param propertyManagement
     *            the propertyManagement to set
     */
    public void setPropertyManagement(PropertyManagement propertyManagement) {
        this.propertyManagement = propertyManagement;
    }

    /**
     * @param securityHelper
     *            the securityHelper to set
     */
    void setSecurityHelper(SecurityHelperWrapper securityHelper) {
        this.securityHelper = securityHelper;
    }

    /**
     * @param message
     *            message to be validated
     * @throws NoTopicSpecifiedException
     *             exception
     * @throws NoTopicIdentifierSpecifiedException
     *             exception
     */
    public void validateMessage(EditTopicMessage message)
            throws NoTopicSpecifiedException,
            NoTopicIdentifierSpecifiedException {
        if (message.getTopic() == null) {
            throw new NoTopicSpecifiedException();
        } else if (!isTopicIdentifierAvailable(message.getTopic(),
                message.isUpdateAlias())) {
            throw new NoTopicIdentifierSpecifiedException();
        }
    }

}
