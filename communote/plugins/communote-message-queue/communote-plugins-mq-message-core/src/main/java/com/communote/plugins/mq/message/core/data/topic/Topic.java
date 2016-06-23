package com.communote.plugins.mq.message.core.data.topic;

import com.communote.plugins.mq.message.core.data.property.StringProperty;
import com.communote.plugins.mq.message.core.data.tag.Tag;

/**
 * The Class Topic.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class Topic extends BaseTopic {

    /** The title. */
    private String title;

    /** The description. */
    private String description;

    /** The tags. */
    private Tag[] tags;

    /**
     * Topic rights
     */
    private TopicRights topicRights;

    /**
     * Topic properties
     */
    private StringProperty[] properties;

    /**
     * External objects
     */
    private ExternalObject[] externalObjects;

    /**
     * Gets the description.
     * 
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @return the externalObjects
     */
    public ExternalObject[] getExternalObjects() {
        return externalObjects;
    }

    /**
     * @return the topicProperties
     */
    public StringProperty[] getProperties() {
        return properties;
    }

    /**
     * Gets the tags.
     * 
     * @return the tags
     */
    public Tag[] getTags() {
        return tags;
    }

    /**
     * Gets the title.
     * 
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @return the topicRights
     */
    public TopicRights getTopicRights() {
        return topicRights;
    }

    /**
     * Sets the description.
     * 
     * @param description
     *            the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @param externalObjects
     *            the externalObjects to set
     */
    public void setExternalObjects(ExternalObject[] externalObjects) {
        this.externalObjects = externalObjects;
    }

    /**
     * @param properties
     *            the topicProperties to set
     */
    public void setProperties(StringProperty[] properties) {
        this.properties = properties;
    }

    /**
     * Sets the tags.
     * 
     * @param tags
     *            the tags to set
     */
    public void setTags(Tag[] tags) {
        this.tags = tags;
    }

    /**
     * Sets the title.
     * 
     * @param title
     *            the title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @param topicRights
     *            the topicRights to set
     */
    public void setTopicRights(TopicRights topicRights) {
        this.topicRights = topicRights;
    }

}
