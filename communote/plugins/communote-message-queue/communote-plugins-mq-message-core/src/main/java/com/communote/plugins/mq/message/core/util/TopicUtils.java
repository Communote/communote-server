package com.communote.plugins.mq.message.core.util;

import com.communote.plugins.mq.message.core.data.topic.BaseTopic;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.blog.BlogAccessException;
import com.communote.server.api.core.blog.BlogManagement;
import com.communote.server.api.core.blog.BlogNotFoundException;
import com.communote.server.model.blog.Blog;

/**
 * Helper class for topics.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class TopicUtils {

    private static BlogManagement TOPIC_MANAGEMENT;

    /**
     * Get the ID of the topic
     *
     * @param topic
     *            the topic to get
     * @return the found ID
     * @throws BlogNotFoundException
     *             in case no id is set and topic for alias does not exist
     * @throws BlogAccessException
     *             in case the current user has no access to the topic
     */
    public static Long getTopicId(BaseTopic topic) throws BlogNotFoundException,
            BlogAccessException {
        if (topic.getTopicId() != null && topic.getTopicId() >= 0) {
            return topic.getTopicId();
        }
        if (topic.getTopicAlias() != null) {
            Blog blog = getTopicmanagement().findBlogByIdentifier(topic.getTopicAlias());
            if (blog != null) {
                return blog.getId();
            }
        }
        throw new BlogNotFoundException("Topic not found for topic id " + topic.getTopicId()
                + " or alias " + topic.getTopicAlias(), topic.getTopicId(), topic.getTopicAlias());
    }

    /**
     * @return the topicmanagement
     */
    public static BlogManagement getTopicmanagement() {
        if (TOPIC_MANAGEMENT == null) {
            TOPIC_MANAGEMENT = ServiceLocator.instance().getService(BlogManagement.class);
        }
        return TOPIC_MANAGEMENT;
    }

    /**
     * Private constructor to avoid instances of utility class.
     */
    private TopicUtils() {
        // Do nothing
    }
}
