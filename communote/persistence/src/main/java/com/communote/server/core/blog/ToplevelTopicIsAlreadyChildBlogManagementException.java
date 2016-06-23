package com.communote.server.core.blog;

/**
 * This exception is thrown, when somebody tries to declace a topic as top level topic if it is
 * already a child itself.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ToplevelTopicIsAlreadyChildBlogManagementException extends BlogManagementException {

    private final Long topicId;

    /**
     * Constructor.
     * 
     * @param topicId
     *            Id of the conflicting topic.
     */
    public ToplevelTopicIsAlreadyChildBlogManagementException(Long topicId) {
        super("The topic (" + topicId
                + ") is already a child and therefor can't be a top level topic.");
        this.topicId = topicId;
    }

    /**
     * @return Id of the conflicting topic.
     */
    public Long getTopicId() {
        return topicId;
    }

}
