package com.communote.server.web.commons;

/**
 * Interface that holds all the constants that are used in the frontend
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public final class FeConstants {
    /**
     * Attributes.
     */
    public static final class Attributes {
        /**
         * Attribute for the list of members of a group. The associated object will be a list of
         * type <code>UserGroupMember</code>
         */
        public static final String MEMBERS_LIST = "membersList";

        /**
         * Boolean if a form / action should be editable
         */
        public static final String EDITABLE = "editable";

        /**
         * User group
         */
        public static final String USER_GROUP = "userGroup";

        /**
         * the groups a user may manage
         */
        public static final String MANAGEABLE_USER_GROUPS = "manageableUserGroups";
        /** Initial start date */
        public static final String INITIAL_START_DATE = "initialStartDate";
    }

    /**
     * Parameters.
     */
    public static class Params {
        /** Group Id */
        public static final String GROUP_ID = "groupId";
    }

    /**
     * Constants of plain password
     */
    public static final String PASSWORD_FORMAT_PLAIN = "plain";

    /**
     * Constants of md5 password
     */
    public static final String PASSWORD_FORMAT_MD5 = "md5";

    /**
     * Private constructor to avoid instances of utility class.
     */
    private FeConstants() {
        // Do nothing
    }
}
