package com.communote.server.core.vo.query.user;

import java.util.Map;

/**
 * The Class UserManagementQueryInstance.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class CommunoteEntityQueryParameters extends UserQueryParameters {

    /** Query Parameter name for the blog to exclude the members from. */
    public final static String PARAM_EXCLUDE_ASSIGNED_TO_BLOG_ID = "excludedAssignedToBlogId";

    /** Exclude this entity. */
    public final static String PARAM_EXCLUDE_ENTITY_ID = "excludedEntityId";

    /**
     * Parameter for restricting the result to users and groups that are direct members of a group
     */
    public final static String PARAM_MEMBERSHIP_GROUP_ID = "membershipGroupId";

    private Long excludedEntityId;
    private Long excludedAssignedToBlogId;
    private Long groupId;

    /**
     * @return the excludedAssignedToBlogId
     */
    public Long getExcludedAssignedToBlogId() {
        return excludedAssignedToBlogId;
    }

    /**
     * @return the excludedEntityId
     */
    public Long getExcludedEntityId() {
        return excludedEntityId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Object> getParameters() {
        Map<String, Object> params = super.getParameters();
        putParameter(params, PARAM_EXCLUDE_ASSIGNED_TO_BLOG_ID, excludedAssignedToBlogId);
        putParameter(params, PARAM_EXCLUDE_ENTITY_ID, excludedEntityId);
        putParameter(params, PARAM_MEMBERSHIP_GROUP_ID, groupId);
        return params;
    }

    /**
     * @return true if a group ID was set to restrict the result to those users and groups that are
     *         members of a group
     * @see #setDirectGroupMembershipFilteringGroupId(Long)
     */
    public boolean isDirectGroupMembership() {
        return this.groupId != null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean needTransformListItem() {
        return false;
    }

    /**
     * Set the ID of a group to restrict the result to those users and groups that are direct
     * members of that group.
     * 
     * @param groupId
     *            the ID of the group
     */
    public void setDirectGroupMembershipFilteringGroupId(Long groupId) {
        this.groupId = groupId;
    }

    /**
     * Set an ID of a topic to exclude all Communote entities from the result set which are already
     * a member of that topic
     * 
     * @param excludedAssignedToBlogId
     *            the ID of the topic
     */
    public void setExcludedAssignedToBlogId(Long excludedAssignedToBlogId) {
        this.excludedAssignedToBlogId = excludedAssignedToBlogId;
    }

    /**
     * @param excludedEntityId
     *            the excludedEntityId to set
     */
    public void setExcludedEntityId(Long excludedEntityId) {
        this.excludedEntityId = excludedEntityId;
    }

}
