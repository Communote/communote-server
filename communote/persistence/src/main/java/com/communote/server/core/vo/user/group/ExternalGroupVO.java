package com.communote.server.core.vo.user.group;

/**
 * <p>
 * Extended VO for external user groups.
 * </p>
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ExternalGroupVO extends com.communote.server.core.vo.user.group.GroupVO implements
        java.io.Serializable {
    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = -8173311385385707832L;

    private String externalId;

    private String externalSystemId;

    private String additionalProperty;

    private boolean mergeOnAdditionalProperty;

    public ExternalGroupVO() {
        super();
        this.externalId = null;
        this.externalSystemId = null;
    }

    /**
     * Copies constructor from other ExternalGroupVO
     * 
     * @param otherBean
     *            , cannot be <code>null</code>
     * @throws NullPointerException
     *             if the argument is <code>null</code>
     */
    public ExternalGroupVO(ExternalGroupVO otherBean) {
        this(otherBean.getExternalId(), otherBean.getExternalSystemId(), otherBean
                .getAdditionalProperty(), otherBean.getName(), otherBean.getAlias(), otherBean
                .getDescription());
    }

    public ExternalGroupVO(String externalId, String externalSystemId, String name) {
        super(name);
        this.externalId = externalId;
        this.externalSystemId = externalSystemId;
    }

    public ExternalGroupVO(String externalId, String externalSystemId, String additionalProperty,
            String name, String alias, String description) {
        super(name, alias, description);
        this.externalId = externalId;
        this.externalSystemId = externalSystemId;
        this.additionalProperty = additionalProperty;
    }

    /**
     * Copies all properties from the argument value object into this value object.
     */
    public void copy(ExternalGroupVO otherBean) {
        if (otherBean != null) {
            this.setExternalId(otherBean.getExternalId());
            this.setExternalSystemId(otherBean.getExternalSystemId());
            this.setAdditionalProperty(otherBean.getAdditionalProperty());
            this.setName(otherBean.getName());
            this.setAlias(otherBean.getAlias());
            this.setDescription(otherBean.getDescription());
        }
    }

    /**
     * <p>
     * an optional member holding some additional data for the group. The interpretation depends on
     * the external system. For LDAP it would hold the DN for example.
     * </p>
     */
    public String getAdditionalProperty() {
        return this.additionalProperty;
    }

    /**
     * <p>
     * ID that identifies a group within the external system
     * </p>
     */
    public String getExternalId() {
        return this.externalId;
    }

    /**
     * <p>
     * ID that uniquely identifies an external system.
     * </p>
     */
    public String getExternalSystemId() {
        return this.externalSystemId;
    }

    /**
     * @return whether the additional property should be treated like a unique identifier of an
     *         external group
     * @see #setMergeOnAdditionalProperty(boolean)
     */
    public boolean isMergeOnAdditionalProperty() {
        return mergeOnAdditionalProperty;
    }

    public void setAdditionalProperty(String additionalProperty) {
        this.additionalProperty = additionalProperty;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public void setExternalSystemId(String externalSystemId) {
        this.externalSystemId = externalSystemId;
    }

    /**
     * Whether the additional property should be treated like a unique identifier of an external
     * group. If set to true an existing external group should first be searched by the externalId
     * and if not found by the additionalProperty. This will help to update an existing external
     * group when the mapping for the externalId changed.
     * 
     * @param merge
     *            true if the additional property should be treated like a unique identifier and can
     *            be used to find and merge an existing external group
     */
    public void setMergeOnAdditionalProperty(boolean merge) {
        this.mergeOnAdditionalProperty = merge;
    }

}