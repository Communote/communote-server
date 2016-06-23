package com.communote.server.core.mail.messages;

/**
 * Constants to be used in email placeholders
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public final class MailModelPlaceholderConstants {

    /**
     * Client specific Mail constants
     *
     * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
     */
    public final static class Client {

        /**
         * Placeholder for a client
         */
        public static final String CLIENT = "client";

        /**
         * Placeholder for the home url of the client
         */
        public static final String HOMEPAGE = "homepage";

        /**
         * Placeholder for the registration site of the client
         */

        public static final String REGISTRATION = "registration";

        /**
         * Placeholder for the profile link.
         */
        public static final String USER_PROFILE_LINK = "userProfileLink";

        /**
         * Placeholder for client signature.
         */
        public static final String SIGNATURE = "clientSignature";

        /**
         * Placeholder for client specific reply-to email address.
         */
        public static final String REPLY_TO_ADDRESS = "clientReplyToAddress";

        /**
         * Placeholder for client specific reply-to email address personal name.
         */
        public static final String REPLY_TO_NAME = "clientReplyToName";

        /**
         * Private constructor for utility class.
         */
        private Client() {
            // Do nothing.
        }
    }

    /**
     * The model placeholder for the mime mail message for further changes
     */
    public final static String MESSAGE = "message";
    /**
     * Placeholder for the confirmation link
     */
    public static final String CONFIRMATION_LINK = "confirmationLink";
    /**
     * Placeholder for the user
     */
    public static final String USER = "user";
    /**
     * Placeholder for the group
     */
    public static final String GROUP = "group";
    /**
     * Placeholder for the sender
     */
    public static final String SENDER = "sender";
    /**
     * Placeholder for the receiver
     */
    public static final String RECEIVER = "receiver";
    /**
     * Placeholder for the new email address
     */
    public static final String NEW_EMAIL_ADDRESS = "newEmailAddress";
    /**
     * Placeholder for a requesting user
     */
    public static final String REQUESTING_USER = "requestingUser";

    /**
     * Placeholder for a user profile
     */
    public static final String USER_PROFILE = "profile";

    /**
     * Placeholder for the current client crc size limit
     */
    public static final String CONTENT_REPOSITORY_SIZE_LIMIT = "crc_size_limit";

    /**
     * Placeholder for the current client crc size
     */
    public static final String CONTENT_REPOSITORY_SIZE = "crc_size";

    /**
     * Placeholder for the percentage of the current client crc size
     */
    public static final String CONTENT_REPOSITORY_SIZE_PERCENT = "crc_size_percent";

    /**
     * Placeholder for a error message
     */
    public static final String ERROR_MESSAGE = "errorMessage";

    /**
     * Placeholder for the current limit of active user accounts
     */
    public static final String USER_COUNT_LIMIT = "user_count_limit";

    /**
     * Placeholder for the percent of active user accounts reached the limit
     */
    public static final String USER_COUNT_PERCENT = "user_count_percent";

    /** Placeholder for th user count limit */
    public static final String USER_TAGGED_COUNT_LIMIT = "user_tagged_count_limit";

    /** Placeholder for the user count percent value as string. */
    public static final String USER_TAGGED_COUNT_PERCENT = "user_tagged_count_percent";

    /** Placeholder for the blog count limit. */
    public static final String BLOG_COUNT_LIMIT = "blog_count_limit";

    /** Placeholder for the blog count as percent. */
    public static final String BLOG_COUNT_PERCENT = "blog_count_percent";

    /**
     * Placeholder for a user tagged item.
     */
    public static final String UTI = "uti";

    /**
     * Placeholder for a user tagged item creation date.
     */
    public static final String UTI_FORMATED_DATE_CREATE = "uti_formated_date_created";

    /**
     * Placeholder for a user tagged item last modification date.
     */
    public static final String UTI_FORMATED_DATE_MODIFY = "uti_formated_date_modified";

    /**
     * Placeholder for the tags (string) of a user tagged item.
     */
    public static final String UTI_TAGS = "uti_tags";

    /**
     * Placeholder for the content of a user tagged post.
     */
    public static final String UTP_CONTENT = "post_content";

    /**
     * Placeholder for blog title.
     */
    public static final String BLOG_TITLE = "blog_title";

    /**
     * Placeholder for blog name identifiers (as comma separated string).
     */
    public static final String BLOG_NAME_IDS = "blog_name_ids";

    /**
     * Placeholder for unresolvable user aliases (as comma separated string).
     */
    public static final String UNRESOLVABLE_USERS = "unresolvable_users";

    /**
     * Placeholder for one unresolvable user alias.
     */
    public static final String UNRESOLVABLE_USER = "unresolvable_user";

    /**
     * Placeholder for uninformable user aliases (as comma separated string).
     */
    public static final String UNINFORMABLE_USERS = "uninformable_users";

    /**
     * Placeholder for one uninformable user alias.
     */
    public static final String UNINFORMABLE_USER = "uninformable_user";

    /**
     * Placeholder for unresolvable blog aliases (as comma separated string).
     */
    public static final String UNRESOLVABLE_BLOGS = "unresolvable_blogs";

    /**
     * Placeholder for an unresolvable blog alias.
     */
    public static final String UNRESOLVABLE_BLOG = "unresolvable_blog";

    /**
     * Placeholder for unwritable blogs (as comma separated string).
     */
    public static final String UNWRITABLE_BLOGS = "unwritable_blogs";

    /**
     * Placeholder for one unwritable blog.
     */
    public static final String UNWRITABLE_BLOG = "unwritable_blog";
    /**
     * Placeholder for indicating, if the automatic user account activation has been changed due to
     * reaching the user count limit.
     */
    public static final String AUTOMATIC_ACTIVATION_CHANGED = "automatic_activation_changed";

    /**
     * Placeholder for the risk level
     */
    public static final String RISK_LEVEL = "risk_level";

    /**
     * Placeholder for the warning reason
     */
    public static final String WARN_REASON = "warn_reason";

    /**
     * Placeholder for locked channel
     */
    public static final String LOCKED_CHANNEL = "locked_channel";

    /**
     * Placeholder for prepending some data to the subject.
     */
    public static final String SUBJECT_PREPEND = "subject_prepend";

    /**
     * Placeholder for attachments.
     */
    public static final String ATTACHMENTS = "attachments";

    /**
     * Placeholder for whether to render the permanent link of the note
     */
    public static final String RENDER_PERMA_LINK = "renderPermalink";

    /**
     * Placeholder for whether to render the permanent link of the blog
     */
    public static final String RENDER_BLOG_PERMA_LINK = "renderBlogPermalink";
    /**
     * Placeholder for the permanent link of the blog
     */
    public static final String PERMA_LINK_BLOG = "blogPermalink";
    /**
     * Placeholder for the permanent link of the user
     */
    public static final String PERMA_LINK_USER = "userPermalink";
    /**
     * Placeholder for the permanent link of the note
     */
    public static final String PERMA_LINK_NOTE = "notePermalink";
    /**
     * Placeholder for the permanent link of the tag
     */
    public static final String PERMA_LINK_TAG = "tagPermalink";
    /**
     * Set to mark the note as direct message.
     */
    public static final String IS_DIRECT = "isDirectMessage";

    /** Set to mark that the note was modified. */
    public static final String IS_MODIFIED = "modified";

    /**
     * Private constructor to avoid instances of utility class.
     */
    private MailModelPlaceholderConstants() {
        // Do nothing
    }

}
