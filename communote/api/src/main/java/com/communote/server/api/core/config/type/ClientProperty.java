package com.communote.server.api.core.config.type;

import org.apache.commons.lang.BooleanUtils;

import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.config.ClientConfigurationPropertyConstant;

/**
 * Property constants for general client settings.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public enum ClientProperty implements ClientConfigurationPropertyConstant {

    /** Time between 2 automatic saves of a note */
    AUTOSAVE_TIMER("kenmei.autosave.timer.in.seconds"),

    /** Creation Date */
    CREATION_DATE("client.creation.date"),

    /** Default language */
    DEFAULT_LANGUAGE("communote.default.language"),

    /**
     * Denotes whether permalinks to communote should be rendered.
     */
    NOTIFICATION_RENDER_PERMALINKS("kenmei.notification.render.permalinks"),
    /**
     * Denotes whether the links to attachments should be rendered.
     */
    NOTIFICATION_RENDER_ATTACHMENTLINKS("kenmei.notification.render.attachmentlinks"),

    /**
     * Denotes whether a link to the blog will be added to the blog invitation email.
     */
    INVITATION_RENDER_BLOG_PERMALINK("kenmei.notification.invite.render.blog.link"),

    /** The unique id of this client. */
    UNIQUE_CLIENT_IDENTIFER("kenmei.unique.client.identifer"),

    /**
     * The Constant AUTOMATIC_USER_ACTIVATION. TODO describe this constant. The constant should
     * start with kenmei.client
     */
    AUTOMATIC_USER_ACTIVATION("kenmei.automatic.user.activation"),

    /** Property for the limit of active user accounts of the client */
    USER_MANAGEMENT_USER_LIMIT("communote.usermanagement.user.limit"),

    /** Property for the micro blog count limit */
    CLIENT_BLOG_COUNT_LIMIT("kenmei.client.blog.count.limit"),

    /**
     * Property to store the date of the 90 percent notify mail for the blog limit
     */
    CLIENT_BLOG_COUNT_90_MAIL("kenmei.client.blog.count.90.mail"),

    /**
     * Property to store the date of the 100 percent notify mail for the blog limit
     */
    CLIENT_BLOG_COUNT_100_MAIL("kenmei.client.blog.count.100.mail"),

    /**
     * Property to store if public access to blogs will be allowed. If boolean property is true:
     * Allow anonymous access to communote blogs. Otherwise only registered users can access
     * communote content.
     */
    CLIENT_BLOG_ALLOW_PUBLIC_ACCESS("kenmei.blog.allow.public.access"),

    /** Property for the resource count limit */
    CLIENT_USER_TAGGED_COUNT_LIMIT("kenmei.client.user.tagged.count.limit"),

    /**
     * Property to store the date of the 90 percent notify mail for the user tagged item limit
     */
    CLIENT_USER_TAGGED_COUNT_90_MAIL("kenmei.client.user.tagged.count.90.mail"),

    /**
     * Property to store the date of the 90 percent notify mail for the user tagged item limit
     */
    CLIENT_USER_TAGGED_COUNT_100_MAIL("kenmei.client.user.tagged.count.100.mail"),

    /**
     * Property holding a boolean flag that denotes whether the email for reaching 90% of the active
     * user count limit has already been sent
     */
    USER_MANAGEMENT_USER_LIMIT_90_MAIL_SENT("communote.usermanagement.user.limit.90.mail.sent"),

    /**
     * Property holding a boolean flag that denotes whether the email for reaching 100% of the
     * active user count limit has already been sent
     */
    USER_MANAGEMENT_USER_LIMIT_100_MAIL_SENT("communote.usermanagement.user.limit.100.mail.sent"),

    /**
     * Property denoting whether the value of AUTOMATIC_USER_ACTIVATION has been changed
     * automatically after reaching the user count limit.
     */
    USER_MANAGEMENT_USER_LIMIT_ACTIVATION_CHANGED(
            "communote.usermanagement.user.limit.activation.changed"),

            /**
             * Boolean Property, if true: Allow ALL users to set the all can read (or write) flag of a blog.
             * Otherwise only the client manager is allowed to do this.
             */
            ALLOW_ALL_CAN_READ_WRITE_FOR_ALL_USERS("kenmei."
                    + "client.allow.all.can.read.write.for.all.users"),

    /** Property of the reply to address personal name. */
    REPLY_TO_ADDRESS_NAME("kenmei.client.reply.to.address.name"),

    /** Property of the reply to address. */
    REPLY_TO_ADDRESS("kenmei.client.reply.to.address"),

    /** Property of the support email address */
    SUPPORT_EMAIL_ADDRESS("kenmei.client.support.email.address"),

    /**
     * Property defining whether notification emails (user was activated, activation reminder)
     * should not be sent to the user when an external authentication is defined and activated.
     */
    NO_REGISTRATION_USER_NOTIFY_EMAILS_WHEN_EXTERNAL_AUTH(""
            + "kenmei.no.registration.user.notify.emails.when.external.auth"),

    /**
     * time in milliseconds after the user will be reminded to login or to confirm its email adress.
     */
    REMIND_USER_TIME("kenmei.client.user.remind.time"),

    /**
     * Property defining whether a user can be deleted by making him anonymous and removing all his
     * data (UTIs? What's this?).
     */
    DELETE_USER_BY_ANONYMIZE_ENABLED("kenmei.client.delete.user.by.anonymize.enabled"),

    /**
     * Property defining whether a user can be deleted by disabling him, but keeping all his data
     * (UTIs? What's this?).
     */
    DELETE_USER_BY_DISABLE_ENABLED("kenmei.client.delete.user.by.disable.enabled"),

    /** Property defining whether users have to accept the terms of use */
    TERMS_OF_USE_USERS_MUST_ACCEPT("kenmei.client.terms.of.use.users.must.accept"),

    /** Property for the limit of the content repository size. */
    FILE_SYSTEM_REPOSITORY_SIZE_LIMIT("kenmei.crc.file.repository.size.limit"),

    /**
     * Property to show, the email for reaching 90% of the repository size limit was already sent.
     * Check this value while deleting files, changing the size limit.
     */
    FILE_SYSTEM_REPOSITORY_SIZE_90_MAIL("kenmei.crc.file.repository.size.90.mail"),

    /**
     * Property to show, the email for reaching 100% of the repository size limit was already sent
     * today.
     */
    FILE_SYSTEM_REPOSITORY_SIZE_100_MAIL("kenmei.crc.file.repository.size.100.mail"),

    /** Property for setting the creation of personal blog */
    CREATE_PERSONAL_BLOG("communote.create.personal.blog"),

    /** Property defining if all users or only the client admin can create a topic */
    ALLOW_TOPIC_CREATE_FOR_ALL_USERS("communote.allow.topic.create.for.all.users"),

    /** The number of days, which count for the most used blogs. */
    NUMBER_OF_DAYS_FOR_MOST_USED_BLOGS("communote.number.of.days.for.most.used.blogs"),

    /** Mode of the service on determinig the user repositories */
    USER_SERVICE_REPOSITORY_MODE("communote.user.repository.mode"),

    /** External system identifier of the primary repository */
    USER_SERVICE_REPOSITORY_PRIMARY("communote.user.repository.primary"),

    /** Property to decide that an unknown external user should create */
    CREATE_EXTERNAL_USER_AUTOMATICALLY("communote.create.external.user.automatically"),

    /** Property to decide that an unknown external group should create */
    CREATE_EXTERNAL_GROUP_AUTOMATICALLY("communote.create.external.group.automatically"),

    /** Property for the token for the generic authenticator. */
    GENERIC_AUTHENTICATOR_TOKEN_NAME("communote.authenticator.generic.token_name"),

    /** Property for the token for the generic authenticator coming for the request. */
    GENERIC_AUTHENTICATOR_URL_TOKEN_NAME("communote.authenticator.generic.url.token_name"),

    /** Property for the enabling the generic authenticator. */
    GENERIC_AUTHENTICATOR_ENABLED("communote.authenticator.generic.enabled"),

    /** Property for the ad tracking time out */
    ACTIVE_DIRECTORY_TRACKING_TIME_OUT("kenmei.client.active.directory.tracking.time.out"),

    /** Property for the enabling paging for ad tracking */
    ACTIVE_DIRECTORY_TRACKING_ALLOW_PAGING("kenmei.client.active.directory.tracking.allow.paging"),

    /** Persistent USN */
    ACTIVE_DIRECTORY_TRACKING_USER_SEQUENCE_NUMBER("kenmei.client.active.directory.tracking.usn"),

    /** Property for the ad tracking paging size */
    ACTIVE_DIRECTORY_TRACKING_PAGING_SIZE("kenmei.client.active.directory.tracking.paging.size"),

    /** Property for the ad tracking job repeating interval in ms */
    ACTIVE_DIRECTORY_TRACKING_REPEATING_INTERVAL(
            "kenmei.client.active.directory.repeating.interval"),

    /** Property for the enabling the generic authenticator. */
    USER_REGISTRATION_ALLOWED("communote.user.local.registration.enabled"),

    /** The number of notes to respect for trends */
    NOTES_TO_USE_FOR_TRENDS("communote.number.of.notes.for.trends"),

    /** This is used to set a specific value for the following view. */
    NOTES_TO_USE_FOR_TRENDS_IN_FOLLOWING("communote.number.of.notes.for.trends.following"),

    /** The preselected tab. */
    PRESELECTED_TAB("communote.layout.preselected.tab"),

    /** The preselected topic overview tab. */
    PRESELECTED_TOPIC_OVERVIEW_TAB("communote.layout.preselected.topic.overview.tab"),

    /** Property for setting the interval for group synchronizations. */
    GROUP_SYNCHRONIZATION_INTERVAL_IN_MINUTES("communote.group.synchronization.interval"),

    /** Property for setting that the synchronization should do a full sync. */
    GROUP_SYNCHRONIZATION_DO_FULL_SYNC("communote.group.synchronization.do-full"),

    /**
     * If this property is set, the ids of external groups will be considered as always lower case.
     * Used in ExternalGroupDao#findByExternalId.
     */
    COMPARE_EXTERNAL_GROUP_IDS_LOWERCASE("communote.group.external.id.compare.lowercase"),
    /**
     * If this property is set, the ids of external users will be considered as always lower case.
     * Used in UserDao#findByExternalUserId.
     */
    COMPARE_EXTERNAL_USER_IDS_LOWERCASE("communote.user.external.id.compare.lowercase"),
    /**
     * If this property is set (default), the internal alias and the external user alias will be
     * compared ignoring case.
     */
    COMPARE_EXTERNAL_USER_ALIAS_LOWERCASE("communote.user.external.alias.compare.lowercase"),

    /** The preselected view. */
    PRESELECTED_VIEW("communote.layout.preselected.view"),

    /** Defines the maximal amount of users, which can be notified using the @@ syntax. */
    MAX_NUMBER_OF_MENTIONED_USERS("communote.@@-mentions.max-users"),
    /** Property for Top Level Topics. */
    TOP_LEVEL_TOPICS_ENABLED("communote.top-level-topics.enabled");

    /** Values for PRESELECTED_TAB. */
    public enum PRESELECTED_TABS_VALUES {
        /** All. */
        ALL,
        /** Following. */
        FOLLOWED,
        /** Favorites. */
        FAVOR,
        /** My. */
        MY
    }

    /** Values for PRESELECTED_TOPIC_OVERVIEW_TABS_VALUES. */
    public enum PRESELECTED_TOPIC_OVERVIEW_TABS_VALUES {
        /** All. */
        ALL,
        /** Topic Directory */
        DIRECTORY,
        /** Following. */
        FOLLOWING
    }

    /**
     * The repository mode determines how the correct external repository is identified by the user
     * service if a new users arrives.
     *
     * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
     *
     */
    // TODO move to UserService?
    public enum REPOSITORY_MODE {
        /**
         * Strict means that always (if properly configured) the primary user repository will be
         * used for user and group lookups.
         */
        STRICT,
        /**
         * Flexible means that the user repository will be used a current external request comes
         * from, and that external request can be tracked to a properly configured system.
         */
        FLEXIBLE
    }

    /**
     * Default value for {@link ClientProperty#DELETE_USER_BY_ANONYMIZE_ENABLED}.
     */
    public static final boolean DEFAULT_DELETE_USER_BY_ANONYMIZE_ENABLED = false;
    /**
     * Default value for {@link ClientProperty#DELETE_USER_BY_DISABLE_ENABLED}.
     */
    public static final boolean DEFAULT_DELETE_USER_BY_DISABLE_ENABLED = false;
    /**
     * Default value for {@link ClientProperty#NOTIFICATION_RENDER_PERMALINKS}.
     */
    public static final boolean DEFAULT_NOTIFICATION_RENDER_PERMALINKS = true;
    /**
     * Default value for {@link ClientProperty#NOTIFICATION_RENDER_ATTACHMENTLINKS}.
     */
    public static final boolean DEFAULT_NOTIFICATION_RENDER_ATTACHMENTLINKS = true;
    /**
     * Default value for {@link ClientProperty#INVITATION_RENDER_BLOG_PERMALINK}.
     */
    public static final boolean DEFAULT_INVITATION_RENDER_BLOG_PERMALINK = true;
    /**
     * Default value for {@link ClientProperty#USER_SERVICE_REPOSITORY_MODE}.
     */
    public static final REPOSITORY_MODE DEFAULT_USER_SERVICE_REPOSITORY_MODE = REPOSITORY_MODE.FLEXIBLE;
    /**
     * Default value for {@link ClientProperty#CREATE_EXTERNAL_USER_AUTOMATICALLY}.
     */
    public static final boolean DEFAULT_CREATE_EXTERNAL_USER_AUTOMATICALLY = false;
    /**
     * Default value for {@link ClientProperty#CREATE_EXTERNAL_GROUP_AUTOMATICALLY}.
     */
    public static final boolean DEFAULT_CREATE_EXTERNAL_GROUP_AUTOMATICALLY = false;

    /**
     * Default value for {@link ClientProperty#GROUP_SYNCHRONIZATION_INTERVAL_IN_MINUTES}.
     */
    public static final int DEFAULT_GROUP_SYNCHRONIZATION_INTERVAL_IN_MINUTES = 60;

    /**
     * Default value for {@link ClientProperty#MAX_NUMBER_OF_MENTIONED_USERS}.
     */
    public static final int DEFAULT_MAX_NUMBER_OF_MENTIONED_USERS = 100;

    /**
     * Default value for {@link ClientProperty#TOP_LEVEL_TOPICS_ENABLED}.
     */
    public static final boolean DEFAULT_TOP_LEVEL_TOPICS_ENABLED = true;

    /**
     * Default value for {@link ClientProperty#CREATE_PERSONAL_BLOG}.
     */
    public static final boolean DEFAULT_CREATE_PERSONAL_BLOG = true;

    /**
     * Default value for {@link ClientProperty#ALLOW_TOPIC_CREATE_FOR_ALL_USERS}.
     */
    public static final boolean DEFAULT_ALLOW_TOPIC_CREATE_FOR_ALL_USERS = true;

    /**
     * Default value for {@link #AUTOSAVE_TIMER}.
     */
    public static final int DEFAULT_AUTOSAVE_TIMER = 10;

    /**
     * Default value for {@link ClientProperty#TERMS_OF_USE_USERS_MUST_ACCEPT}.
     */
    public static final boolean DEFAULT_TERMS_OF_USE_USERS_MUST_ACCEPT = false;

    /**
     * Default value for {@link #PRESELECTED_TOPIC_OVERVIEW_TAB}.
     */
    public static final String DEFAULT_PRESELECTED_TOPIC_OVERVIEW_TAB = PRESELECTED_TOPIC_OVERVIEW_TABS_VALUES.ALL
            .name();

    private final String key;

    /**
     * Constructor for enum type.
     *
     * @param keyString
     *            the constant as string
     */
    private ClientProperty(String keyString) {
        this.key = keyString;
    }

    /**
     * String representation of the constant to be used as key in Properties objects.
     *
     * @return the constant as string
     */
    @Override
    public String getKeyString() {
        return key;
    }

    /**
     * @param defaultValue
     *            The
     * @return True, when the value of the property is one of "true", "on" or "yes" (case
     *         insensitive) or the given default value, when the property is not set within the
     *         database.
     */
    public boolean getValue(boolean defaultValue) {
        String value = CommunoteRuntime.getInstance().getConfigurationManager()
                .getClientConfigurationProperties().getProperty(this);
        if (value == null) {
            return defaultValue;
        }
        return BooleanUtils.toBoolean(value);
    }

    /**
     * @param defaultValue
     *            The default value.
     * @return The actual value for this property of the current client.
     */
    public int getValue(int defaultValue) {
        String value = CommunoteRuntime.getInstance().getConfigurationManager()
                .getClientConfigurationProperties().getProperty(this);
        if (value == null) {
            return defaultValue;
        }
        return Integer.parseInt(value);
    }

    /**
     * @param defaultValue
     *            The default value.
     * @return The actual value for this property of the current client.
     */
    public String getValue(String defaultValue) {
        String value = CommunoteRuntime.getInstance().getConfigurationManager()
                .getClientConfigurationProperties().getProperty(this);
        if (value == null) {
            return defaultValue;
        }
        return value;
    }
}