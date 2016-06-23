package com.communote.server.core.blog;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ToplevelTopicCannotBeChildException extends Exception {
    /**
     * default serial version UID
     */
    private static final long serialVersionUID = 1L;
    private final Long topicId;
    private final String title;

    /**
     * Constructor.
     * 
     * @param topicId
     *            Id of the top level topic.
     * @param title
     *            the title of the topic
     */
    public ToplevelTopicCannotBeChildException(Long topicId, String title) {
        super("The topic " + topicId
                + " is a top level topic and therefor can't be added as child.");
        this.topicId = topicId;
        this.title = title;
    }

    /**
     * @return Id of the top level topic this exception was thrown for.
     */
    public Long getTopicId() {
        return topicId;
    }

    /**
     * @return the title of the top level topic this exception was thrown for.
     */
    public String getTopicTitle() {
        return title;
    }
}
