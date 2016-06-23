package com.communote.server.web.commons;

/**
 * Contains action strings for forms
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */

// TODO Refactor actions to enum
public interface FormAction {

    /**
     * Use this to determine of which type your response is.
     */
    public enum RESPONSE_TYPE {
        /** HTML */
        HTML,
        /** JSON */
        JSON,
        /** XML */
        XML
    }

    /**
     * This is the response header field name for the response types. Use it like this:
     * javax.servlet.http.HttpServletResponse.setHeader(RESPONSE_TYPE_HEADER, RESPONSE_TYPE)}
     */
    public final static String RESPONSE_TYPE_HEADER = "X-TYPE";
    /**
     * Constants for a delete action
     */
    public final static String DELETE = "delete";

    /**
     * Constants for a submit action
     */
    public final static String SUBMIT = "submit";

    /**
     * Constants for a create action
     */
    public final static String CREATE = "create";
    /** Create edit */
    public final static String CREATE_EDIT = "create_edit";

    /**
     * Constants for a update action
     */
    public final static String EDIT = "edit";

    /**
     * Constants for blog post comment action
     */
    public final static String COMMENT = "comment";

    /**
     * Constants for a search action
     */
    public final static String SEARCH_USER = "searchuser";

    /**
     * Constants for a search user group action
     */
    public final static String SEARCH_USER_GROUP = "searchusergroup";

    /**
     * Constants for a delete social network config action
     */
    public final static String SYNCHRONIZE_SOCIALNET = "synchronizewithdbobject";

    /**
     * Constants for a password changing action
     */
    public final static String USER_SAVE_CHANGED_PASSWORD = "usersavechangedpassword";

    /**
     * Constants for a email changing action
     */
    public final static String USER_SAVE_CHANGED_EMAIL = "usersavechangedemail";

    /**
     * Constants for a email changing action
     */
    public final static String SELECTED_SOCIAL_CONFIG = "socialnetconfig";

    /**
     * Constants for a image upload via ajax
     */
    public final static String UPLOAD_IMAGE_FILE_AJAX = "uploadimagefileajax";

    /**
     * Constants for a email changing action
     */
    public final static String UPLOAD_IMAGE_FILE = "uploadimagefile";

    /**
     * Constant for reseting user image via ajax to default.
     */
    public final static String RESET_USER_IMAGE_AJAX = "resetuserimageajax";
    
    /**
     * Constant for reseting user image via ajax to default.
     */
    public final static String REFRESH_EXTERNAL_USER_IMAGE_AJAX = "refreshexternaluserimageajax";

    /**
     * Constant for a email changing action
     */
    public final static String UPDATE_USER_PROFILE = "updateuserprofile";

    /**
     * Constant for updating the messaging options.
     */
    public final static String UPDATE_MESSAGING = "updatemessaging";

    /**
     * Constant for updating the rights only
     */
    public static final String UPDATE_RIGHTS = "updateRights";
    /**
     * Constant for requesting XMPP friendship
     */
    public static final String XMPP_REQUEST_FRIENDSHIP = "xmppRequestFriendship";

}
