package com.communote.server.core.vo.tag;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class UserTagClearanceVO extends com.communote.server.core.vo.tag.TagClearanceVO implements
        java.io.Serializable {
    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = -8632828004410822724L;

    private Long ownerId;

    private boolean includeProtectedResources;

    public UserTagClearanceVO() {
        super();
        this.ownerId = null;
        this.includeProtectedResources = false;
    }

    public UserTagClearanceVO(Long ownerId, boolean includeProtectedResources, Long userGroupId) {
        super(userGroupId);
        this.ownerId = ownerId;
        this.includeProtectedResources = includeProtectedResources;
    }

    public UserTagClearanceVO(Long ownerId, boolean includeProtectedResources, String includeTags,
            String excludeTags, Long userGroupId) {
        super(includeTags, excludeTags, userGroupId);
        this.ownerId = ownerId;
        this.includeProtectedResources = includeProtectedResources;
    }

    /**
     * Copies constructor from other UserTagClearanceVO
     * 
     * @param otherBean
     *            , cannot be <code>null</code>
     * @throws NullPointerException
     *             if the argument is <code>null</code>
     */
    public UserTagClearanceVO(UserTagClearanceVO otherBean) {
        this(otherBean.getOwnerId(), otherBean.isIncludeProtectedResources(), otherBean
                .getIncludeTags(), otherBean.getExcludeTags(), otherBean.getUserGroupId());
    }

    /**
     * Copies all properties from the argument value object into this value object.
     */
    public void copy(UserTagClearanceVO otherBean) {
        if (otherBean != null) {
            this.setOwnerId(otherBean.getOwnerId());
            this.setIncludeProtectedResources(otherBean.isIncludeProtectedResources());
            this.setIncludeTags(otherBean.getIncludeTags());
            this.setExcludeTags(otherBean.getExcludeTags());
            this.setUserGroupId(otherBean.getUserGroupId());
        }
    }

    /**
     * 
     */
    public Long getOwnerId() {
        return this.ownerId;
    }

    /**
     * 
     */
    public boolean isIncludeProtectedResources() {
        return this.includeProtectedResources;
    }

    public void setIncludeProtectedResources(boolean includeProtectedResources) {
        this.includeProtectedResources = includeProtectedResources;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

}