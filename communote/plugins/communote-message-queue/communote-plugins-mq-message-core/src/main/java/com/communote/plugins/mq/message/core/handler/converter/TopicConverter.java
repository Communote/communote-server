package com.communote.plugins.mq.message.core.handler.converter;

import java.util.Collection;
import java.util.Set;

import com.communote.plugins.mq.message.core.data.property.StringProperty;
import com.communote.plugins.mq.message.core.data.tag.Tag;
import com.communote.plugins.mq.message.core.data.topic.ExternalObject;
import com.communote.plugins.mq.message.core.data.topic.Topic;
import com.communote.plugins.mq.message.core.data.topic.TopicRights;
import com.communote.plugins.mq.message.core.util.ConverterUtils;
import com.communote.server.api.core.common.NotFoundException;
import com.communote.server.api.core.property.PropertyManagement;
import com.communote.server.api.core.property.PropertyType;
import com.communote.server.api.core.property.StringPropertyTO;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.model.blog.Blog;

/**
 * Class for converting Communote topics to MQ topics.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class TopicConverter {

    private final PropertyManagement propertyManagement;

    /**
     * Constructor.
     * 
     * @param propertyManagement
     *            The property management to use.
     */
    public TopicConverter(PropertyManagement propertyManagement) {
        this.propertyManagement = propertyManagement;
    }

    /**
     * @param blog
     *            blog to be converted
     * @param externalObjects
     *            the external objects of blog
     * @return the topic
     * @throws AuthorizationException
     *             in case the current user is not allowed to read the blog properties
     * @throws NotFoundException
     *             in case the blog does not exist
     */
    public Topic convertBlogToTopic(Blog blog,
            Collection<com.communote.server.model.external.ExternalObject> externalObjects)
            throws NotFoundException, AuthorizationException {
        Topic topic = new Topic();
        topic.setTopicId(blog.getId());
        topic.setTopicAlias(blog.getNameIdentifier());
        topic.setDescription(blog.getDescription());
        topic.setTitle(blog.getTitle());
        topic.setTopicRights(new TopicRights());
        topic.getTopicRights().setAllCanRead(blog.isAllCanRead());
        topic.getTopicRights().setAllCanWrite(blog.isAllCanWrite());
        Set<StringPropertyTO> topicProperties = propertyManagement.getAllObjectProperties(
                PropertyType.BlogProperty, blog.getId());
        topic.setProperties(ConverterUtils.convertIterableToCollection(topicProperties,
                ConverterUtils.STRING_PROPERTY_CONVERTER).toArray(
                new StringProperty[0]));
        topic.setTags(ConverterUtils.convertIterableToCollection(blog.getTags(),
                ConverterUtils.TAG_CONVERTER).toArray(new Tag[blog.getTags().size()]));
        topic.setExternalObjects(ConverterUtils.convertIterableToCollection(externalObjects,
                ConverterUtils.MQ_EXTERNAL_OBJECT_CONVERTER).toArray(new ExternalObject[0]));
        return topic;
    }
}
