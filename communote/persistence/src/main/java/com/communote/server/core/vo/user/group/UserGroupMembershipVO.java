package com.communote.server.core.vo.user.group;

/**
 * <p>
 * This value object holds a user group and a specific user group member that depends or is filtered
 * on a certain context. The context is defined by the function used.
 * </p>
 * <p>
 * If the user group member is null it means it does not exists in the group.
 * </p>
 * <p>
 * If the user group member is not null it means that the member is part of the group.
 * </p>
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class UserGroupMembershipVO implements java.io.Serializable {
    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = -3748061007993062345L;

    private com.communote.server.model.user.group.Group userGroup;

    public UserGroupMembershipVO() {
        this.userGroup = null;
    }

    public UserGroupMembershipVO(com.communote.server.model.user.group.Group userGroup) {
        this.userGroup = userGroup;
    }

    /**
     * Copies constructor from other UserGroupMembershipVO
     * 
     * @param otherBean
     *            , cannot be <code>null</code>
     * @throws NullPointerException
     *             if the argument is <code>null</code>
     */
    public UserGroupMembershipVO(UserGroupMembershipVO otherBean) {
        this(otherBean.getUserGroup());
    }

    /**
     * Copies all properties from the argument value object into this value object.
     */
    public void copy(UserGroupMembershipVO otherBean) {
        if (otherBean != null) {
            this.setUserGroup(otherBean.getUserGroup());
        }
    }

    /**
     * Get the userGroup
     * 
     */
    public com.communote.server.model.user.group.Group getUserGroup() {
        return this.userGroup;
    }

    /**
     * Sets the userGroup
     */
    public void setUserGroup(com.communote.server.model.user.group.Group userGroup) {
        this.userGroup = userGroup;
    }

}