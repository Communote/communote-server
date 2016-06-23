package com.communote.server.core.vo.query.user.group;

/**
 * Enum for user group member result type. To be used to determine what type of users should be
 * included in the result set, given the context of a user group.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public enum UserGroupMemberResultType {
    /**
     * include all users
     */
    all,
    /**
     * include only group members
     */
    onlyUserGroupMembers,
    /**
     * include only non group members
     */
    onlyNonUserGroupMembers
}
