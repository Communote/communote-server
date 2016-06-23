package com.communote.plugins.mq.message.core.data.role;

import com.communote.plugins.mq.message.core.data.user.BaseEntity;

/**
 * 
 * External role of use for topic
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ExternalTopicRole {
    private String topicRole;

    private String externalObjectId;

    private BaseEntity entity = new BaseEntity();

    /**
     * @return the base entity
     */
    public BaseEntity getEntity() {
        return entity;
    }

    /**
     * @return the externalObjectId
     */
    public String getExternalObjectId() {
        return externalObjectId;
    }

    /**
     * @return the topic role as an uppercase string
     */
    public String getTopicRole() {
        return topicRole;
    }

    /**
     * @param entity
     *            the baseentity to set
     */
    public void setEntity(BaseEntity entity) {
        this.entity = entity;
    }

    /**
     * @param externalObjectId
     *            the externalObjectId to set
     */
    public void setExternalObjectId(String externalObjectId) {
        this.externalObjectId = externalObjectId;
    }

    /**
     * @param topicRole
     *            the topicRole to set
     */
    public void setTopicRole(String topicRole) {
        this.topicRole = topicRole != null ? topicRole.toUpperCase() : topicRole;
    }

}
