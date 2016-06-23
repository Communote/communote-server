package com.communote.plugins.mq.message.core.data.user;

/**
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class BaseEntity {

    private Long entityId;

    private String entityAlias;

    private String externalId;
    private boolean isGroup;

    /**
     * @return the Communote alias of the entity
     */
    public String getEntityAlias() {
        return entityAlias;
    }

    /**
     * @return the Communote ID of the entity
     */
    public Long getEntityId() {
        return entityId;
    }

    /**
     * @return the ID of the entity in the external system
     */
    public String getExternalId() {
        return externalId;
    }

    /**
     * @return whether the entity represents a group or a user
     */
    public boolean getIsGroup() {
        return isGroup;
    }

    /**
     * @param entityAlias
     *            the entityAlias to set
     */
    public void setEntityAlias(String entityAlias) {
        this.entityAlias = entityAlias;
    }

    /**
     * @param entityId
     *            the entityId to set
     */
    public void setEntityId(Long entityId) {
        this.entityId = entityId;
    }

    /**
     * Set the ID of the entity in the external system
     * 
     * @param externalEntityId
     *            ID of the entity in the external system
     */
    public void setExternalId(String externalEntityId) {
        this.externalId = externalEntityId;
    }

    /**
     * @param isGroup
     *            the isGroup to set
     */
    public void setIsGroup(boolean isGroup) {
        this.isGroup = isGroup;
    }

}
