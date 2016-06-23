package com.communote.server.core.vo.tag;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class TagClearanceVO implements java.io.Serializable {
    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = -7143755055192081645L;

    private String includeTags;

    private String excludeTags;

    private Long userGroupId;

    public TagClearanceVO() {
        this.userGroupId = null;
    }

    public TagClearanceVO(Long userGroupId) {
        this.userGroupId = userGroupId;
    }

    public TagClearanceVO(String includeTags, String excludeTags, Long userGroupId) {
        this.includeTags = includeTags;
        this.excludeTags = excludeTags;
        this.userGroupId = userGroupId;
    }

    /**
     * Copies constructor from other TagClearanceVO
     * 
     * @param otherBean
     *            , cannot be <code>null</code>
     * @throws NullPointerException
     *             if the argument is <code>null</code>
     */
    public TagClearanceVO(TagClearanceVO otherBean) {
        this(otherBean.getIncludeTags(), otherBean.getExcludeTags(), otherBean.getUserGroupId());
    }

    /**
     * Copies all properties from the argument value object into this value object.
     */
    public void copy(TagClearanceVO otherBean) {
        if (otherBean != null) {
            this.setIncludeTags(otherBean.getIncludeTags());
            this.setExcludeTags(otherBean.getExcludeTags());
            this.setUserGroupId(otherBean.getUserGroupId());
        }
    }

    /**
     * 
     */
    public String getExcludeTags() {
        return this.excludeTags;
    }

    /**
     * 
     */
    public String getIncludeTags() {
        return this.includeTags;
    }

    /**
     * 
     */
    public Long getUserGroupId() {
        return this.userGroupId;
    }

    public void setExcludeTags(String excludeTags) {
        this.excludeTags = excludeTags;
    }

    public void setIncludeTags(String includeTags) {
        this.includeTags = includeTags;
    }

    public void setUserGroupId(Long userGroupId) {
        this.userGroupId = userGroupId;
    }

}