package com.communote.plugins.mq.message.core.message.topic;

import com.communote.plugins.mq.message.base.message.BaseMessage;
import com.communote.plugins.mq.message.core.data.topic.BaseTopic;

/**
 * The Class TopicSpecificMessage.
 * 
 * @param <T>
 *            The base topic.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class TopicSpecificMessage<T extends BaseTopic> extends BaseMessage {

    private T topic;

    /**
     * Gets the topic.
     * 
     * @return the topic
     */
    public T getTopic() {
        return topic;
    }

    /**
     * Sets the topic
     * 
     * @param topic
     *            the topic
     */
    public void setTopic(T topic) {
        this.topic = topic;
    }

}
