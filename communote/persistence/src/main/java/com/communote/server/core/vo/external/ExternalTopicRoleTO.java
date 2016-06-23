package com.communote.server.core.vo.external;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ExternalTopicRoleTO implements java.io.Serializable {
    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = -6798749903060082388L;

    private com.communote.server.model.blog.BlogRole role;

    private Long entityId;

    private String entityAlias;

    private Boolean isGroup;

    private String externalObjectId;

    private String externalEntityId;

    public ExternalTopicRoleTO() {
        this.role = null;
        this.externalObjectId = null;
    }

    public ExternalTopicRoleTO(com.communote.server.model.blog.BlogRole role, Long entityId,
            String entityAlias, Boolean isGroup, String externalObjectId, String externalEntityId) {
        this.role = role;
        this.entityId = entityId;
        this.entityAlias = entityAlias;
        this.isGroup = isGroup;
        this.externalObjectId = externalObjectId;
        this.externalEntityId = externalEntityId;
    }

    public ExternalTopicRoleTO(com.communote.server.model.blog.BlogRole role,
            String externalObjectId) {
        this.role = role;
        this.externalObjectId = externalObjectId;
    }

    /**
     * Copies constructor from other ExternalTopicRoleTO
     *
     * @param otherBean
     *            , cannot be <code>null</code>
     * @throws NullPointerException
     *             if the argument is <code>null</code>
     */
    public ExternalTopicRoleTO(ExternalTopicRoleTO otherBean) {
        this(otherBean.getRole(), otherBean.getEntityId(), otherBean.getEntityAlias(), otherBean
                .getIsGroup(), otherBean.getExternalObjectId(), otherBean.getExternalEntityId());
    }

    /**
     * Copies all properties from the argument value object into this value object.
     */
    public void copy(ExternalTopicRoleTO otherBean) {
        if (otherBean != null) {
            this.setRole(otherBean.getRole());
            this.setEntityId(otherBean.getEntityId());
            this.setEntityAlias(otherBean.getEntityAlias());
            this.setIsGroup(otherBean.getIsGroup());
            this.setExternalObjectId(otherBean.getExternalObjectId());
            this.setExternalEntityId(otherBean.getExternalEntityId());
        }
    }

    /**
     * <p>
     * Alias of user or group.
     * </p>
     */
    public String getEntityAlias() {
        return this.entityAlias;
    }

    /**
     * <p>
     * Identifier of user or group.
     * </p>
     */
    public Long getEntityId() {
        return this.entityId;
    }

    /**
     *
     */
    public String getExternalEntityId() {
        return this.externalEntityId;
    }

    /**
     * <p>
     * Identifier of external object in external system.
     * </p>
     */
    public String getExternalObjectId() {
        return this.externalObjectId;
    }

    /**
     * <p>
     * Set that the entityId or the entityAlias is an group. For default the entityId is the
     * identifier of an user and the alias the alias of an user.
     * </p>
     */
    public Boolean getIsGroup() {
        return this.isGroup;
    }

    /**
     * <p>
     * Role of entity for topic.
     * </p>
     */
    public com.communote.server.model.blog.BlogRole getRole() {
        return this.role;
    }

    public void setEntityAlias(String entityAlias) {
        this.entityAlias = entityAlias;
    }

    public void setEntityId(Long entityId) {
        this.entityId = entityId;
    }

    public void setExternalEntityId(String externalEntityId) {
        this.externalEntityId = externalEntityId;
    }

    public void setExternalObjectId(String externalObjectId) {
        this.externalObjectId = externalObjectId;
    }

    public void setIsGroup(Boolean isGroup) {
        this.isGroup = isGroup;
    }

    public void setRole(com.communote.server.model.blog.BlogRole role) {
        this.role = role;
    }

}