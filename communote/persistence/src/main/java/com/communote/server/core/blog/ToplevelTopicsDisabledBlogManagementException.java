package com.communote.server.core.blog;

/**
 * This exception is thrown, when somebody tries to declare a topic as top level topic if top level
 * topics are disabled.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ToplevelTopicsDisabledBlogManagementException extends BlogManagementException {

    private final Long topicId;

    /**
     * Constructor.
     * 
     * @param topicId
     *            Id of the conflicting topic.
     */
    public ToplevelTopicsDisabledBlogManagementException(Long topicId) {
        super("Top level topics are disabled, so it is not possible to set the topic (" + topicId
                + ") as top level topic.");
        this.topicId = topicId;
    }

    /**
     * @return Id of the conflicting topic.
     */
    public Long getTopicId() {
        return topicId;
    }

}
