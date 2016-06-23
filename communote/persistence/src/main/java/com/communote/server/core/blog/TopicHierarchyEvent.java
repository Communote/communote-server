package com.communote.server.core.blog;

import com.communote.server.api.core.event.Event;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class TopicHierarchyEvent implements Event {
    /**
     * Possible types this event was fired for.
     */
    public enum Type {
        /** A connection between topics was added. */
        ADD,
        /** A connection between topics was removed */
        REMOVE
    }

    /**
     * default serial version UID
     */
    private static final long serialVersionUID = 1L;
    private final Long parentTopicId;
    private final String parentTopicTitle;
    private final Long childTopicId;
    private final String childTopicTitle;
    private final Long userId;

    private final Type type;

    /**
     * Constructor.
     * 
     * @param parentTopicId
     *            Id of the parent topic.
     * @param parentTopicTitle
     *            title of the parent topic
     * @param childTopicId
     *            Id of the child topic.
     * @param childTopicTitle
     *            title of the child topic
     * @param userId
     *            Id of the invoking user.
     * @param type
     *            Type of the event.
     */
    public TopicHierarchyEvent(Long parentTopicId, String parentTopicTitle, Long childTopicId,
            String childTopicTitle, Long userId, Type type) {
        this.parentTopicId = parentTopicId;
        this.parentTopicTitle = parentTopicTitle;
        this.childTopicId = childTopicId;
        this.childTopicTitle = childTopicTitle;
        this.userId = userId;
        this.type = type;
    }

    /**
     * @return Id of the affected child.
     */
    public Long getChildTopicId() {
        return childTopicId;
    }

    /**
     * @return the title of the child topic
     */
    public String getChildTopicTitle() {
        return childTopicTitle;
    }

    /**
     * @return Id of the affected parent.
     */
    public Long getParentTopicId() {
        return parentTopicId;
    }

    /**
     * @return the title of the parent topic
     */
    public String getParentTopicTitle() {
        return parentTopicTitle;
    }

    /**
     * @return Type of this event.
     */
    public Type getType() {
        return type;
    }

    /**
     * @return Id of the invoking user.
     */
    public Long getUserId() {
        return userId;
    }
}
