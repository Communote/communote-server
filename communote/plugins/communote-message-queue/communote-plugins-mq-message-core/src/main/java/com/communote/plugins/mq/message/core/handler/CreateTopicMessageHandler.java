package com.communote.plugins.mq.message.core.handler;

import com.communote.plugins.mq.message.base.handler.CommunoteMessageHandler;
import com.communote.plugins.mq.message.base.message.CommunoteReplyMessage;
import com.communote.plugins.mq.message.core.data.property.StringProperty;
import com.communote.plugins.mq.message.core.data.tag.Tag;
import com.communote.plugins.mq.message.core.data.topic.ExternalObject;
import com.communote.plugins.mq.message.core.data.topic.Topic;
import com.communote.plugins.mq.message.core.handler.converter.TopicConverter;
import com.communote.plugins.mq.message.core.handler.exception.NoTopicSpecifiedException;
import com.communote.plugins.mq.message.core.message.topic.CreateTopicMessage;
import com.communote.plugins.mq.message.core.message.topic.TopicReplyMessage;
import com.communote.plugins.mq.message.core.util.StoringPolicy;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.blog.BlogIdentifierValidationException;
import com.communote.server.api.core.blog.BlogManagement;
import com.communote.server.api.core.blog.CreationBlogTO;
import com.communote.server.api.core.blog.NonUniqueBlogIdentifierException;
import com.communote.server.api.core.property.PropertyManagement;
import com.communote.server.core.external.ExternalObjectManagement;
import com.communote.server.core.security.SecurityHelper;
import com.communote.server.model.blog.Blog;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Create Topic Message
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
@Provides(specifications = CommunoteMessageHandler.class)
@Component
@Instantiate
public class CreateTopicMessageHandler extends
        CommunoteMessageHandler<CreateTopicMessage> {

    /** The LOG. */
    private static Logger LOG = LoggerFactory
            .getLogger(CreateTopicMessageHandler.class);

    private BlogManagement blogManagement;
    private ExternalObjectManagement externalObjectManagement;
    private PropertyManagement propertyManagement;

    /**
     * creates topic TO
     * 
     * @param message
     *            edit topic message
     * @return topic TO
     */
    private CreationBlogTO createTopicTO(CreateTopicMessage message) {
        Topic topic = message.getTopic();
        CreationBlogTO topicTO = new CreationBlogTO();
        topicTO.setCreatorUserId(SecurityHelper.assertCurrentUserId());
        String description = topic.getDescription();
        String title = topic.getTitle();
        String alias = topic.getTopicAlias();

        topicTO.setDescription(description);
        topicTO.setTitle(title);
        topicTO.setNameIdentifier(alias);

        Tag[] newTags = topic.getTags() != null ? topic.getTags() : new Tag[0];
        topicTO.setTags(TopicMessageHandlerUtils.extractTagTOs(newTags));

        if (topic.getTopicRights() != null) {
            topicTO.setAllCanRead(topic.getTopicRights().isAllCanRead());
            topicTO.setAllCanWrite(topic.getTopicRights().isAllCanWrite());
        }

        StringProperty[] newProperties = topic.getProperties() == null ? new StringProperty[0]
                : topic.getProperties();
        topicTO.setProperties(TopicMessageHandlerUtils
                .extractPropertiesTO(newProperties));

        return topicTO;
    }

    /**
     * Gets the blog management.
     * 
     * @return the blog management
     */
    private BlogManagement getBlogManagement() {
        if (blogManagement == null) {
            blogManagement = ServiceLocator.instance().getService(BlogManagement.class);
        }
        return blogManagement;
    }

    /**
     * Gets the blog management.
     * 
     * @return the blog management
     */
    private ExternalObjectManagement getExternalObjectManagement() {
        if (externalObjectManagement == null) {
            externalObjectManagement = ServiceLocator.instance().getService(
                    ExternalObjectManagement.class);
        }
        return externalObjectManagement;
    }

    @Override
    public Class<CreateTopicMessage> getHandledMessageClass() {
        return CreateTopicMessage.class;
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

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public CommunoteReplyMessage handleMessage(CreateTopicMessage message)
            throws Exception {
        validateMessage(message);
        CreationBlogTO topicTO = createTopicTO(message);

        try {
            Blog res = getBlogManagement().createBlog(topicTO);

            ExternalObject[] externalObjects = message.getTopic().getExternalObjects();
            TopicMessageHandlerUtils.updateExternalObjects(res.getId(),
                    message.getExternalSystemId(), externalObjects, StoringPolicy.MERGE,
                    getExternalObjectManagement());

            LOG.debug("Blog was successfully created");
            TopicReplyMessage replyMessage = new TopicReplyMessage();
            replyMessage.setTopic(new TopicConverter(getPropertyManagement())
                    .convertBlogToTopic(res,
                            getExternalObjectManagement().getExternalObjects(res.getId())));
            return replyMessage;
        } catch (NonUniqueBlogIdentifierException e) {
            LOG.error("Alias " + message.getTopic().getTopicAlias()
                    + " cannot be used, since it already exists");
            throw e;
        } catch (BlogIdentifierValidationException e) {
            LOG.error("Alias " + message.getTopic().getTopicAlias()
                    + " is not valid");
            throw e;
        }
    }

    /**
     * @param propertyManagement
     *            the propertyManagement to set
     */
    public void setPropertyManagement(PropertyManagement propertyManagement) {
        this.propertyManagement = propertyManagement;
    }

    /**
     * @param message
     *            message to be validated
     * @throws NoTopicSpecifiedException
     *             exception
     */
    public void validateMessage(CreateTopicMessage message)
            throws NoTopicSpecifiedException {
        if (message.getTopic() == null) {
            throw new NoTopicSpecifiedException();
        }
    }
}
