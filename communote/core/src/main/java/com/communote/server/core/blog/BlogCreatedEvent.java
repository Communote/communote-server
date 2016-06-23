package com.communote.server.core.blog;

import com.communote.server.api.core.event.Event;

/**
 * Event to notify about new blogs/topics.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class BlogCreatedEvent implements Event {
    /**
     * default serial version UID
     */
    private static final long serialVersionUID = 1L;
    private final Long userId;
    private final Long blogId;
    private final String topicTitle;
    private final Long parentTopicId;

    /**
     * Create a new event
     * 
     * @param blogId
     *            the ID of the created topic
     * @param topicTitle
     *            the title of the created topic
     * @param userId
     *            Id of the user, who created the topic.
     * @param parentTopicId
     *            Id of a parent topic, might be null.
     */
    public BlogCreatedEvent(Long blogId, String topicTitle, Long userId, Long parentTopicId) {
        this.blogId = blogId;
        this.topicTitle = topicTitle;
        this.userId = userId;
        this.parentTopicId = parentTopicId;
    }

    /**
     * @return the ID of the created topic
     */
    public Long getBlogId() {
        return blogId;
    }

    /**
     * @return Id of a parent topic or null if none.
     */
    public Long getParentTopicId() {
        return parentTopicId;
    }

    /**
     * @return the title of the created topic
     */
    public String getTopicTitle() {
        return topicTitle;
    }

    /**
     * @return the ID of the user that created the topic
     */
    public Long getUserId() {
        return userId;
    }
}
