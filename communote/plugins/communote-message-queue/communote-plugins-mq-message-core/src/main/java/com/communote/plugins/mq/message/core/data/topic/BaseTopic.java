package com.communote.plugins.mq.message.core.data.topic;

/**
 * The Class BaseTopic.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class BaseTopic {

    /** The topic id. */
    private Long topicId;

    /** The topic alias. */
    private String topicAlias;

    public BaseTopic() {
    }

    public BaseTopic(Long topicId) {
        this.topicId = topicId;
    }

    public BaseTopic(String topicAlias) {
        this.topicAlias = topicAlias;
    }

    /**
     * Gets the topic alias.
     * 
     * @return the topicAlias
     */
    public String getTopicAlias() {
        return topicAlias;
    }

    /**
     * Gets the topic id.
     * 
     * @return the topicId
     */
    public Long getTopicId() {
        return topicId;
    }

    /**
     * Sets the topic alias.
     * 
     * @param topicAlias
     *            the topicAlias to set
     */
    public void setTopicAlias(String topicAlias) {
        this.topicAlias = topicAlias;
    }

    /**
     * Sets the topic id.
     * 
     * @param topicId
     *            the topicId to set
     */
    public void setTopicId(Long topicId) {
        this.topicId = topicId;
    }

}
