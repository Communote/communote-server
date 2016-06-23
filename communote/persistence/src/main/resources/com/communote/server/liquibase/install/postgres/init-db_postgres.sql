--
-- schema definition from init-db.sql of version 1.1.3 revision 5200
-- with minor bugfixes:
--  corrected unique constraint on custom_messages (andromda bug)
--
    create table channel_configuration (
        ID BIGINT not null,
        FORCE_SSL BOOLEAN not null,
        CHANNEL_TYPE CHARACTER VARYING(1024) not null,
        primary key (ID)
    );

    create table configuration (
        ID BIGINT not null,
        CLIENT_CONFIG_FK BIGINT unique,
        primary key (ID)
    );

    create table configuration_client (
        ID BIGINT not null,
        LOGO_IMAGE BYTEA,
        LAST_LOGO_IMAGE_MODIFICATION_D TIMESTAMP WITHOUT TIME ZONE,
        TIME_ZONE_ID CHARACTER VARYING(1024),
        DEFAULT_BLOG_FK BIGINT unique,
        primary key (ID)
    );

    create table configuration_confluence (
        ID BIGINT not null,
        AUTHENTICATION_API_URL CHARACTER VARYING(1024) not null,
        IMAGE_API_URL CHARACTER VARYING(1024),
        ADMIN_LOGIN CHARACTER VARYING(1024),
        ADMIN_PASSWORD CHARACTER VARYING(1024),
        SERVICE_URL CHARACTER VARYING(1024),
        PERMISSIONS_URL CHARACTER VARYING(1024),
        BASE_PATH CHARACTER VARYING(1024),
        primary key (ID)
    );

    create table configuration_external_system (
        ID BIGINT not null,
        ALLOW_EXTERNAL_AUTHENTICATION BOOLEAN not null,
        SYSTEM_ID CHARACTER VARYING(50) not null unique,
        PRIMARY_AUTHENTICATION BOOLEAN not null,
        SYNCHRONIZE_USER_GROUPS BOOLEAN not null,
        CONFIGURATION_FK BIGINT,
        primary key (ID)
    );

    create table configuration_ldap (
        ID BIGINT not null,
        URL CHARACTER VARYING(1024) not null,
        MANAGER_PASSWORD CHARACTER VARYING(1024) not null,
        MANAGER_D_N CHARACTER VARYING(1024) not null,
        SEARCHBASE CHARACTER VARYING(1024) not null,
        SEARCHFILTER CHARACTER VARYING(1024) not null,
        SEARCH_SUB_TREE BOOLEAN not null,
        PROPERTY_MAPPING CHARACTER VARYING(1024) not null,
        primary key (ID)
    );

    create table configuration_setting (
        KEY CHARACTER VARYING(300) not null,
        VALUE TEXT,
        CONFIGURATION_FK BIGINT,
        primary key (KEY)
    );

    create table configuration_sharepoint (
        ID BIGINT not null,
        AUTHENTICATION_API_URL CHARACTER VARYING(1024) not null,
        ADMIN_PASSWORD CHARACTER VARYING(1024) not null,
        ADMIN_LOGIN CHARACTER VARYING(1024) not null,
        IMAGE_API_URL CHARACTER VARYING(1024),
        primary key (ID)
    );

    create table core_attachment (
        ID BIGINT not null,
        CONTENT_IDENTIFIER CHARACTER VARYING(1024) not null,
        REPOSITORY_IDENTIFIER CHARACTER VARYING(1024) not null,
        NAME CHARACTER VARYING(1024) not null,
        CONTENT_TYPE CHARACTER VARYING(1024),
        SIZE BIGINT,
        STATUS CHARACTER VARYING(1024) not null,
        GLOBAL_ID_FK BIGINT unique,
        NOTE_FK BIGINT,
        primary key (ID)
    );

    create table core_blog (
        ID BIGINT not null,
        TITLE CHARACTER VARYING(1024) not null,
        DESCRIPTION TEXT,
        CREATION_DATE TIMESTAMP WITHOUT TIME ZONE not null,
        NAME_IDENTIFIER CHARACTER VARYING(300) not null unique,
        LAST_MODIFICATION_DATE TIMESTAMP WITHOUT TIME ZONE not null,
        ALL_CAN_READ BOOLEAN not null,
        ALL_CAN_WRITE BOOLEAN not null,
        PUBLIC_ACCESS BOOLEAN not null,
        GLOBAL_ID_FK BIGINT unique,
        primary key (ID)
    );

    create table core_blog2tag (
        BLOGS_FK BIGINT not null,
        TAGS_FK BIGINT not null,
        primary key (BLOGS_FK, TAGS_FK)
    );

    create table core_blog_event (
        ID BIGINT not null,
        TIME TIMESTAMP WITHOUT TIME ZONE not null,
        TYPE CHARACTER VARYING(1024) not null,
        CREATOR_FK BIGINT,
        BLOG_FK BIGINT,
        primary key (ID)
    );

    create table core_blog_member (
        ID BIGINT not null,
        class varchar(255) not null,
        ROLE CHARACTER VARYING(1024) not null,
        BLOG_FK BIGINT not null,
        KENMEI_ENTITY_FK BIGINT not null,
        EXTERNAL_SYSTEM_ID CHARACTER VARYING(50),
        primary key (ID)
    );

    create table core_content (
        ID BIGINT not null,
        CONTENT TEXT not null,
        SHORT_CONTENT TEXT,
        primary key (ID)
    );

    create table core_external_object (
        ID BIGINT not null,
        EXTERNAL_SYSTEM_ID CHARACTER VARYING(50) not null,
        EXTERNAL_ID CHARACTER VARYING(200) not null,
        EXTERNAL_NAME CHARACTER VARYING(1024),
        BLOG_FK BIGINT,
        primary key (ID),
        unique (EXTERNAL_SYSTEM_ID, EXTERNAL_ID)
    );

    create table core_external_object_properties (
        ID BIGINT not null,
        KEY CHARACTER VARYING(300) not null,
        VALUE CHARACTER VARYING(1024) not null,
        EXTERNAL_OBJECT_FK BIGINT,
        primary key (ID)
    );

    create table core_global_id (
        ID BIGINT not null,
        GLOBAL_IDENTIFIER CHARACTER VARYING(300) not null unique,
        primary key (ID)
    );

    create table core_note (
        ID BIGINT not null,
        CREATION_DATE TIMESTAMP WITHOUT TIME ZONE not null,
        LAST_MODIFICATION_DATE TIMESTAMP WITHOUT TIME ZONE not null,
        CREATION_SOURCE CHARACTER VARYING(1024) not null,
        DIRECT BOOLEAN not null,
        STATUS CHARACTER VARYING(1024) not null,
        VERSION BIGINT not null,
        DISCUSSION_PATH CHARACTER VARYING(1024),
        USER_FK BIGINT not null,
        CONTENT_FK BIGINT not null unique,
        GLOBAL_ID_FK BIGINT unique,
        BLOG_FK BIGINT not null,
        PARENT_FK BIGINT,
        ORIGIN_FK BIGINT,
        primary key (ID)
    );

    create table core_notes2crossblogs (
        NOTES_FK BIGINT not null,
        CROSSPOST_BLOGS_FK BIGINT not null,
        primary key (NOTES_FK, CROSSPOST_BLOGS_FK)
    );

    create table core_notes2tag (
        NOTES_FK BIGINT not null,
        TAGS_FK BIGINT not null,
        primary key (NOTES_FK, TAGS_FK)
    );

    create table core_notes2user_to_notify (
        NOTES_FK BIGINT not null,
        USERS_TO_BE_NOTIFIED_FK BIGINT not null,
        primary key (NOTES_FK, USERS_TO_BE_NOTIFIED_FK)
    );

    create table core_processed_utp_mail (
        ID BIGINT not null,
        MAIL_MESSAGE_ID CHARACTER VARYING(300) not null unique,
        primary key (ID)
    );

    create table core_role2blog (
        ID BIGINT not null,
        BLOG_ID BIGINT not null,
        USER_ID BIGINT not null,
        NUMERIC_ROLE INTEGER not null,
        EXTERNAL_SYSTEM_ID CHARACTER VARYING(1024),
        GRANTED_BY_GROUP BOOLEAN not null,
        primary key (ID)
    );

    create table core_role2blog_granting_group (
        USER_TO_BLOG_ROLE_MAPPINGS_FK BIGINT not null,
        GRANTING_GROUPS_FK BIGINT not null,
        primary key (USER_TO_BLOG_ROLE_MAPPINGS_FK, GRANTING_GROUPS_FK)
    );

    create table core_tag (
        ID BIGINT not null,
        class varchar(255) not null,
        LOWER_NAME CHARACTER VARYING(1024) not null,
        NAME CHARACTER VARYING(300) not null unique,
        GLOBAL_ID_FK BIGINT unique,
        CATEGORY_FK BIGINT,
        ABSTRACT_TAG_CATEGORY_TAGS_IDX int4,
        primary key (ID)
    );

    create table core_tag2clearance_exclude (
        TAG_CLEARANCES_FK BIGINT not null,
        EXCLUDE_TAGS_FK BIGINT not null,
        TAG_CLEARANCE_EXCLUDE_TAGS_IDX int4 not null,
        primary key (TAG_CLEARANCES_FK, TAG_CLEARANCE_EXCLUDE_TAGS_IDX)
    );

    create table core_tag2clearance_include (
        TAG_CLEARANCES_FK BIGINT not null,
        INCLUDE_TAGS_FK BIGINT not null,
        TAG_CLEARANCE_INCLUDE_TAGS_IDX int4 not null,
        primary key (TAG_CLEARANCES_FK, TAG_CLEARANCE_INCLUDE_TAGS_IDX)
    );

    create table core_tag_category (
        ID BIGINT not null,
        class varchar(255) not null,
        NAME CHARACTER VARYING(1024) not null,
        PREFIX CHARACTER VARYING(1024) not null,
        DESCRIPTION CHARACTER VARYING(1024),
        MULTIPLE_TAGS BOOLEAN not null,
        primary key (ID)
    );

    create table core_tag_clearance (
        ID BIGINT not null,
        class varchar(255) not null,
        INCLUDE_PROTECTED_RESOURCES BOOLEAN,
        OWNER_FK BIGINT,
        primary key (ID)
    );

    create table core_users2favorite_notes (
        FAVORITE_NOTES_FK BIGINT not null,
        FAVORITE_USERS_FK BIGINT not null,
        primary key (FAVORITE_NOTES_FK, FAVORITE_USERS_FK)
    );

    create table crc_cache_config (
        ID BIGINT not null,
        TIME_TO_STAY BIGINT not null,
        CURRENT_CACHE_CONNECTOR_CON_FK BIGINT unique,
        primary key (ID)
    );

    create table crc_connector_config (
        ID BIGINT not null,
        SUPPORTS_METADATA BOOLEAN not null,
        CONNECTOR_ID CHARACTER VARYING(1024) not null,
        primary key (ID)
    );

    create table crc_filesystem_config (
        ID BIGINT not null,
        PATH CHARACTER VARYING(1024) not null,
        primary key (ID)
    );

    create table crc_ramconn_config (
        ID BIGINT not null,
        MIN_ABSOLUTE_FREE_MEMORY BIGINT not null,
        MIN_FREE_MEMORY_IN_PERCENT INTEGER not null,
        primary key (ID)
    );

    create table custom_messages (
        ID BIGINT not null,
        KEY CHARACTER VARYING(300) not null,
        MESSAGE TEXT not null,
        IS_HTML BOOLEAN not null,
        LANGUAGE_FK BIGINT not null,
        unique (KEY, LANGUAGE_FK),
        primary key (ID)
    );

    create table iprange_channel (
        TYPE CHARACTER VARYING(300) not null,
        ENABLED BOOLEAN not null,
        primary key (TYPE)
    );

    create table iprange_filter (
        ID BIGINT not null,
        NAME CHARACTER VARYING(1024) not null,
        ENABLED BOOLEAN not null,
        primary key (ID)
    );

    create table iprange_filter_channel (
        IP_RANGE_FILTERS_FK BIGINT not null,
        CHANNELS_FK CHARACTER VARYING(300) not null,
        primary key (IP_RANGE_FILTERS_FK, CHANNELS_FK)
    );

    create table iprange_range (
        ID BIGINT not null,
        START_VALUE BYTEA not null,
        END_VALUE BYTEA not null,
        STRING_REPRESENTATION CHARACTER VARYING(1024),
        IP_RANGE_FILTER_IN_FK BIGINT,
        IP_RANGE_FILTER_EX_FK BIGINT,
        primary key (ID)
    );

    create table mc_config (
        ID BIGINT not null,
        TYPE CHARACTER VARYING(1024) not null,
        PROPERTIES CHARACTER VARYING(1024) not null,
        ONLY_IF_AVAILABLE BOOLEAN not null,
        PRIORITY INTEGER not null,
        NOTIFICATION_CONFIG_FK BIGINT,
        primary key (ID)
    );

    create table md_country (
        ID BIGINT not null,
        COUNTRY_CODE CHARACTER VARYING(300) not null unique,
        NAME CHARACTER VARYING(1024) not null,
        primary key (ID)
    );

    create table md_language (
        ID BIGINT not null,
        LANGUAGE_CODE CHARACTER VARYING(255) not null unique,
        NAME CHARACTER VARYING(1024) not null,
        primary key (ID)
    );

    create table misc_query_helper (
        ID BIGINT not null,
        primary key (ID)
    );

    create table news_feed_cache (
        ID BIGINT not null,
        TIMESTAMP TIMESTAMP WITHOUT TIME ZONE not null,
        USER_ID BIGINT not null unique,
        CONTENT_DATA TEXT not null,
        primary key (ID)
    );

    create table news_widget_feed (
        ID BIGINT not null,
        NAME CHARACTER VARYING(1024) not null,
        TYPE CHARACTER VARYING(1024) not null,
        KENMEI_USER_FK BIGINT,
        primary key (ID)
    );

    create table news_widget_parameter (
        ID BIGINT not null,
        NAME CHARACTER VARYING(1024) not null,
        VALUE CHARACTER VARYING(1024) not null,
        WIDGET_NEWS_FEED_FK BIGINT,
        primary key (ID)
    );

    create table notification_config (
        ID BIGINT not null,
        FALLBACK CHARACTER VARYING(1024),
        primary key (ID)
    );

    create table security_code (
        ID BIGINT not null,
        CODE CHARACTER VARYING(300) not null unique,
        ACTION CHARACTER VARYING(1024) not null,
        CREATING_DATE TIMESTAMP WITHOUT TIME ZONE not null,
        KENMEI_USER_FK BIGINT,
        primary key (ID)
    );

    create table security_email_code (
        ID BIGINT not null,
        NEW_EMAIL_ADDRESS CHARACTER VARYING(1024) not null,
        primary key (ID)
    );

    create table security_forgotten_pw_code (
        ID BIGINT not null,
        primary key (ID)
    );

    create table security_invite_blog (
        ID BIGINT not null,
        primary key (ID)
    );

    create table security_invite_client (
        ID BIGINT not null,
        primary key (ID)
    );

    create table security_user_auth_failed_status (
        ID BIGINT not null,
        LOCKED_TIMEOUT TIMESTAMP WITHOUT TIME ZONE,
        FAILED_AUTH_COUNTER INTEGER not null,
        CHANNEL_TYPE CHARACTER VARYING(1024) not null,
        KENMEI_USER_FK BIGINT,
        primary key (ID)
    );

    create table security_user_code (
        ID BIGINT not null,
        primary key (ID)
    );

    create table security_user_unlock_code (
        ID BIGINT not null,
        CHANNEL CHARACTER VARYING(1024) not null,
        primary key (ID)
    );

    create table snc_config (
        ID BIGINT not null,
        USERNAME CHARACTER VARYING(1024) not null,
        PASSWORD CHARACTER VARYING(1024) not null,
        PROPERTIES_STRING CHARACTER VARYING(1024) not null,
        LAST_SYNCHRONIZATION TIMESTAMP WITHOUT TIME ZONE,
        SOCIAL_NETWORK_TYPE CHARACTER VARYING(1024) not null,
        REQUEST_INTERVAL BIGINT not null,
        SYNCHRONIZATION_ENABLED BOOLEAN not null,
        CONFIG_NAME CHARACTER VARYING(1024) not null,
        USER_PROFILE_FK BIGINT not null,
        primary key (ID)
    );

    create table snc_job_details (
        ID BIGINT not null,
        LAST_REQUEST BIGINT,
        NEXT_REQUEST BIGINT not null,
        MISFIRED_COUNTER INTEGER not null,
        IS_DISABLED BOOLEAN not null,
        LAST_MESSAGE CHARACTER VARYING(1024),
        SOCIAL_NETWORK_CONFIGURATIO_FK BIGINT not null unique,
        primary key (ID)
    );

    create table user_authorities (
        ID BIGINT not null,
        ROLE CHARACTER VARYING(1024) not null,
        KENMEI_USER_FK BIGINT,
        primary key (ID)
    );

    create table user_client (
        ID BIGINT not null,
        CLIENT_ID CHARACTER VARYING(300) not null unique,
        PROXY_URL_PREFIX CHARACTER VARYING(1024),
        SECURITY_KEY CHARACTER VARYING(1024),
        NAME CHARACTER VARYING(1024) not null,
        CLIENT_STATUS CHARACTER VARYING(1024) not null,
        CREATION_VERSION CHARACTER VARYING(1024),
        CREATION_TIME TIMESTAMP WITHOUT TIME ZONE,
        CREATION_REVISION BIGINT,
        primary key (ID)
    );

    create table user_client_statistic (
        ID BIGINT not null,
        REPOSITORY_SIZE BIGINT not null,
        primary key (ID)
    );

    create table user_contact (
        ID BIGINT not null,
        STREET CHARACTER VARYING(1024),
        ZIP CHARACTER VARYING(1024),
        CITY CHARACTER VARYING(1024),
        PHONE_COUNTRY_CODE CHARACTER VARYING(1024) not null,
        PHONE_AREA_CODE CHARACTER VARYING(1024) not null,
        PHONE_PHONE_NUMBER CHARACTER VARYING(1024) not null,
        FAX_COUNTRY_CODE CHARACTER VARYING(1024) not null,
        FAX_AREA_CODE CHARACTER VARYING(1024) not null,
        FAX_PHONE_NUMBER CHARACTER VARYING(1024) not null,
        COUNTRY_FK BIGINT,
        primary key (ID)
    );

    create table user_entity (
        ID BIGINT not null,
        GLOBAL_ID_FK BIGINT unique,
        primary key (ID)
    );

    create table user_external_auth (
        ID BIGINT not null,
        EXTERNAL_USER_ID CHARACTER VARYING(250) not null unique,
        SYSTEM_ID CHARACTER VARYING(50) not null,
        KENMEI_USER_FK BIGINT,
        primary key (ID),
        unique (EXTERNAL_USER_ID, SYSTEM_ID)
    );

    create table user_group (
        ID BIGINT not null,
        ALIAS CHARACTER VARYING(300) not null unique,
        NAME CHARACTER VARYING(1024) not null,
        DESCRIPTION TEXT,
        primary key (ID)
    );

    create table user_group_external (
        ID BIGINT not null,
        EXTERNAL_SYSTEM_ID CHARACTER VARYING(1024) not null,
        EXTERNAL_ID CHARACTER VARYING(1024) not null,
        primary key (ID)
    );

    create table user_group_member (
        GROUP_MEMBERS_FK BIGINT not null,
        GROUPS_FK BIGINT not null,
        primary key (GROUP_MEMBERS_FK, GROUPS_FK)
    );

    create table user_image (
        ID BIGINT not null,
        IMAGE BYTEA not null,
        primary key (ID)
    );

    create table user_of_group (
        ID BIGINT not null,
        MODIFICATION_TYPE CHARACTER VARYING(1024),
        GROUP_FK BIGINT not null,
        USER_FK BIGINT not null,
        primary key (ID)
    );

    create table user_profile (
        ID BIGINT not null,
        LAST_NAME CHARACTER VARYING(1024),
        SALUTATION CHARACTER VARYING(1024),
        POSITION CHARACTER VARYING(1024),
        COMPANY CHARACTER VARYING(1024),
        FIRST_NAME CHARACTER VARYING(1024),
        LAST_MODIFICATION_DATE TIMESTAMP WITHOUT TIME ZONE not null,
        LAST_PHOTO_MODIFICATION_DATE TIMESTAMP WITHOUT TIME ZONE,
        TIME_ZONE_ID CHARACTER VARYING(1024),
        SMALL_IMAGE_FK BIGINT unique,
        CONTACT_FK BIGINT unique,
        MEDIUM_IMAGE_FK BIGINT unique,
        LARGE_IMAGE_FK BIGINT unique,
        NOTIFICATION_CONFIG_FK BIGINT not null unique,
        primary key (ID)
    );

    create table user_user (
        ID BIGINT not null,
        PASSWORD CHARACTER VARYING(1024),
        EMAIL CHARACTER VARYING(300) not null unique,
        LANGUAGE_CODE CHARACTER VARYING(1024) not null,
        LAST_LOGIN TIMESTAMP WITHOUT TIME ZONE,
        STATUS CHARACTER VARYING(1024) not null,
        ALIAS CHARACTER VARYING(300) unique,
        TERMS_ACCEPTED BOOLEAN not null,
        REMINDER_MAIL_SENT BOOLEAN not null,
        STATUS_CHANGED TIMESTAMP WITHOUT TIME ZONE not null,
        PROFILE_FK BIGINT not null unique,
        primary key (ID)
    );

    alter table configuration 
        add constraint configuration_CLIENT_CONFIG_FC 
        foreign key (CLIENT_CONFIG_FK) 
        references configuration_client;

    alter table configuration_client 
        add constraint configuration_client_DEFAULT_C 
        foreign key (DEFAULT_BLOG_FK) 
        references core_blog;

    alter table configuration_confluence 
        add constraint configuration_confluenceIFKC 
        foreign key (ID) 
        references configuration_external_system;

    alter table configuration_external_system 
        add constraint configuration_external_systemC 
        foreign key (CONFIGURATION_FK) 
        references configuration;

    alter table configuration_ldap 
        add constraint configuration_ldapIFKC 
        foreign key (ID) 
        references configuration_external_system;

    alter table configuration_setting 
        add constraint configuration_setting_CONFIGUC 
        foreign key (CONFIGURATION_FK) 
        references configuration;

    alter table configuration_sharepoint 
        add constraint configuration_sharepointIFKC 
        foreign key (ID) 
        references configuration_external_system;

    alter table core_attachment 
        add constraint core_attachment_NOTE_FKC 
        foreign key (NOTE_FK) 
        references core_note;

    alter table core_attachment 
        add constraint core_attachment_GLOBAL_ID_FKC 
        foreign key (GLOBAL_ID_FK) 
        references core_global_id;

    alter table core_blog 
        add constraint core_blog_GLOBAL_ID_FKC 
        foreign key (GLOBAL_ID_FK) 
        references core_global_id;

    alter table core_blog2tag 
        add constraint core_tag_BLOGS_FKC 
        foreign key (BLOGS_FK) 
        references core_blog;

    alter table core_blog2tag 
        add constraint core_blog_TAGS_FKC 
        foreign key (TAGS_FK) 
        references core_tag;

    alter table core_blog_event 
        add constraint core_blog_event_CREATOR_FKC 
        foreign key (CREATOR_FK) 
        references user_user;

    alter table core_blog_event 
        add constraint core_blog_event_BLOG_FKC 
        foreign key (BLOG_FK) 
        references core_blog;

    alter table core_blog_member 
        add constraint core_blog_member_BLOG_FKC 
        foreign key (BLOG_FK) 
        references core_blog;

    alter table core_blog_member 
        add constraint core_blog_member_KENMEI_ENTITC 
        foreign key (KENMEI_ENTITY_FK) 
        references user_entity;

    alter table core_external_object 
        add constraint core_external_object_BLOG_FKC 
        foreign key (BLOG_FK) 
        references core_blog;

    alter table core_external_object_properties 
        add constraint core_external_object_propertiC 
        foreign key (EXTERNAL_OBJECT_FK) 
        references core_external_object;

    alter table core_note 
        add constraint core_note_PARENT_FKC 
        foreign key (PARENT_FK) 
        references core_note;

    alter table core_note 
        add constraint core_note_CONTENT_FKC 
        foreign key (CONTENT_FK) 
        references core_content;

    alter table core_note 
        add constraint core_note_USER_FKC 
        foreign key (USER_FK) 
        references user_user;

    alter table core_note 
        add constraint core_note_ORIGIN_FKC 
        foreign key (ORIGIN_FK) 
        references core_note;

    alter table core_note 
        add constraint core_note_BLOG_FKC 
        foreign key (BLOG_FK) 
        references core_blog;

    alter table core_note 
        add constraint core_note_GLOBAL_ID_FKC 
        foreign key (GLOBAL_ID_FK) 
        references core_global_id;

    alter table core_notes2crossblogs 
        add constraint core_note_CROSSPOST_BLOGS_FKC 
        foreign key (CROSSPOST_BLOGS_FK) 
        references core_blog;

    alter table core_notes2crossblogs 
        add constraint core_blog_NOTES_FKC 
        foreign key (NOTES_FK) 
        references core_note;

    alter table core_notes2tag 
        add constraint core_note_TAGS_FKC 
        foreign key (TAGS_FK) 
        references core_tag;

    alter table core_notes2tag 
        add constraint core_tag_NOTES_FKC 
        foreign key (NOTES_FK) 
        references core_note;

    alter table core_notes2user_to_notify 
        add constraint core_note_USERS_TO_BE_NOTIFIEC 
        foreign key (USERS_TO_BE_NOTIFIED_FK) 
        references user_user;

    alter table core_notes2user_to_notify 
        add constraint user_user_NOTES_FKC 
        foreign key (NOTES_FK) 
        references core_note;

    alter table core_role2blog_granting_group 
        add constraint core_role2blog_GRANTING_GROUPC 
        foreign key (GRANTING_GROUPS_FK) 
        references user_group;

    alter table core_role2blog_granting_group 
        add constraint user_group_USER_TO_BLOG_ROLE_C 
        foreign key (USER_TO_BLOG_ROLE_MAPPINGS_FK) 
        references core_role2blog;

    alter table core_tag 
        add constraint CATEGORIZED_TAG_CATEGORY_FKC 
        foreign key (CATEGORY_FK) 
        references core_tag_category;

    alter table core_tag 
        add constraint core_tag_GLOBAL_ID_FKC 
        foreign key (GLOBAL_ID_FK) 
        references core_global_id;

    alter table core_tag2clearance_exclude 
        add constraint core_tag_TAG_CLEARANCES_FKC 
        foreign key (TAG_CLEARANCES_FK) 
        references core_tag_clearance;

    alter table core_tag2clearance_exclude 
        add constraint core_tag_clearance_EXCLUDE_TAC 
        foreign key (EXCLUDE_TAGS_FK) 
        references core_tag;

    alter table core_tag2clearance_include 
        add constraint core_tag_clearance_INCLUDE_TAC 
        foreign key (INCLUDE_TAGS_FK) 
        references core_tag;

    alter table core_tag2clearance_include 
        add constraint core_tag_TAG_CLEARANCES_FKC 
        foreign key (TAG_CLEARANCES_FK) 
        references core_tag_clearance;

    alter table core_tag_clearance 
        add constraint core_user_tag_clearance_OWNERC 
        foreign key (OWNER_FK) 
        references user_user;

    alter table core_users2favorite_notes 
        add constraint user_user_FAVORITE_NOTES_FKC 
        foreign key (FAVORITE_NOTES_FK) 
        references core_note;

    alter table core_users2favorite_notes 
        add constraint core_note_FAVORITE_USERS_FKC 
        foreign key (FAVORITE_USERS_FK) 
        references user_user;

    alter table crc_cache_config 
        add constraint crc_cache_config_CURRENT_CACHC 
        foreign key (CURRENT_CACHE_CONNECTOR_CON_FK) 
        references crc_connector_config;

    alter table crc_filesystem_config 
        add constraint crc_filesystem_configIFKC 
        foreign key (ID) 
        references crc_connector_config;

    alter table crc_ramconn_config 
        add constraint crc_ramconn_configIFKC 
        foreign key (ID) 
        references crc_connector_config;

    alter table custom_messages 
        add constraint custom_messages_LANGUAGE_FKC 
        foreign key (LANGUAGE_FK) 
        references md_language;

    alter table iprange_filter_channel 
        add constraint iprange_channel_IP_RANGE_FILTC 
        foreign key (IP_RANGE_FILTERS_FK) 
        references iprange_filter;

    alter table iprange_filter_channel 
        add constraint iprange_filter_CHANNELS_FKC 
        foreign key (CHANNELS_FK) 
        references iprange_channel;

    alter table iprange_range 
        add constraint ip_range_filter_C_ex 
        foreign key (IP_RANGE_FILTER_EX_FK) 
        references iprange_filter;

    alter table iprange_range 
        add constraint ip_range_filter_C_in 
        foreign key (IP_RANGE_FILTER_IN_FK) 
        references iprange_filter;

    alter table mc_config 
        add constraint mc_config_NOTIFICATION_CONFIGC 
        foreign key (NOTIFICATION_CONFIG_FK) 
        references notification_config;

    alter table news_widget_feed 
        add constraint news_widget_feed_KENMEI_USER_C 
        foreign key (KENMEI_USER_FK) 
        references user_user;

    alter table news_widget_parameter 
        add constraint news_widget_parameter_WIDGET_C 
        foreign key (WIDGET_NEWS_FEED_FK) 
        references news_widget_feed;

    alter table security_code 
        add constraint security_code_KENMEI_USER_FKC 
        foreign key (KENMEI_USER_FK) 
        references user_user;

    alter table security_email_code 
        add constraint security_email_codeIFKC 
        foreign key (ID) 
        references security_code;

    alter table security_forgotten_pw_code 
        add constraint security_forgotten_pw_codeIFKC 
        foreign key (ID) 
        references security_code;

    alter table security_invite_blog 
        add constraint security_invite_blogIFKC 
        foreign key (ID) 
        references security_code;

    alter table security_invite_client 
        add constraint security_invite_clientIFKC 
        foreign key (ID) 
        references security_code;

    alter table security_user_auth_failed_status 
        add constraint security_user_auth_failed_staC 
        foreign key (KENMEI_USER_FK) 
        references user_user;

    alter table security_user_code 
        add constraint security_user_codeIFKC 
        foreign key (ID) 
        references security_code;

    alter table security_user_unlock_code 
        add constraint security_user_unlock_codeIFKC 
        foreign key (ID) 
        references security_code;

    alter table snc_config 
        add constraint snc_config_USER_PROFILE_FKC 
        foreign key (USER_PROFILE_FK) 
        references user_profile;

    alter table snc_job_details 
        add constraint snc_job_details_SOCIAL_NETWORC 
        foreign key (SOCIAL_NETWORK_CONFIGURATIO_FK) 
        references snc_config;

    alter table user_authorities 
        add constraint user_authorities_KENMEI_USER_C 
        foreign key (KENMEI_USER_FK) 
        references user_user;

    alter table user_contact 
        add constraint user_contact_COUNTRY_FKC 
        foreign key (COUNTRY_FK) 
        references md_country;

    alter table user_entity 
        add constraint user_entity_GLOBAL_ID_FKC 
        foreign key (GLOBAL_ID_FK) 
        references core_global_id;

    alter table user_external_auth 
        add constraint user_external_auth_KENMEI_USEC 
        foreign key (KENMEI_USER_FK) 
        references user_user;

    alter table user_group 
        add constraint user_groupIFKC 
        foreign key (ID) 
        references user_entity;

    alter table user_group_external 
        add constraint user_group_externalIFKC 
        foreign key (ID) 
        references user_group;

    alter table user_group_member 
        add constraint user_group_GROUP_MEMBERS_FKC 
        foreign key (GROUP_MEMBERS_FK) 
        references user_entity;

    alter table user_group_member 
        add constraint user_entity_GROUPS_FKC 
        foreign key (GROUPS_FK) 
        references user_group;

    alter table user_of_group 
        add constraint user_of_group_GROUP_FKC 
        foreign key (GROUP_FK) 
        references user_group;

    alter table user_of_group 
        add constraint user_of_group_USER_FKC 
        foreign key (USER_FK) 
        references user_user;

    alter table user_profile 
        add constraint user_profile_SMALL_IMAGE_FKC 
        foreign key (SMALL_IMAGE_FK) 
        references user_image;

    alter table user_profile 
        add constraint user_profile_CONTACT_FKC 
        foreign key (CONTACT_FK) 
        references user_contact;

    alter table user_profile 
        add constraint user_profile_NOTIFICATION_CONC 
        foreign key (NOTIFICATION_CONFIG_FK) 
        references notification_config;

    alter table user_profile 
        add constraint user_profile_MEDIUM_IMAGE_FKC 
        foreign key (MEDIUM_IMAGE_FK) 
        references user_image;

    alter table user_profile 
        add constraint user_profile_LARGE_IMAGE_FKC 
        foreign key (LARGE_IMAGE_FK) 
        references user_image;

    alter table user_user 
        add constraint user_userIFKC 
        foreign key (ID) 
        references user_entity;

    alter table user_user 
        add constraint user_user_PROFILE_FKC 
        foreign key (PROFILE_FK) 
        references user_profile;

    create sequence channel_configuration_seq;

    create sequence configuration_client_seq;

    create sequence configuration_external_system_seq;

    create sequence configuration_seq;

    create sequence core_attachment_seq;

    create sequence core_blog_event_seq;

    create sequence core_blog_member_seq;

    create sequence core_blog_seq;

    create sequence core_content_seq;

    create sequence core_external_object_properties_seq;

    create sequence core_external_object_seq;

    create sequence core_global_id_seq;

    create sequence core_note_seq;

    create sequence core_processed_utp_mail_seq;

    create sequence core_role2blog_seq;

    create sequence core_tag_category_seq;

    create sequence core_tag_clearance_seq;

    create sequence core_tag_seq;

    create sequence crc_cache_config_seq;

    create sequence crc_connector_config_seq;

    create sequence custom_messages_seq;

    create sequence iprange_filter_seq;

    create sequence iprange_range_seq;

    create sequence mc_config_seq;

    create sequence md_country_seq;

    create sequence md_language_seq;

    create sequence misc_query_helper_seq;

    create sequence news_feed_cache_seq;

    create sequence news_widget_feed_seq;

    create sequence news_widget_parameter_seq;

    create sequence notification_config_seq;

    create sequence security_code_seq;

    create sequence security_user_auth_failed_status_seq;

    create sequence snc_config_seq;

    create sequence snc_job_details_seq;

    create sequence user_authorities_seq;

    create sequence user_client_seq;

    create sequence user_client_statistic_seq;

    create sequence user_contact_seq;

    create sequence user_entity_seq;

    create sequence user_external_auth_seq;

    create sequence user_image_seq;

    create sequence user_of_group_seq;

    create sequence user_profile_seq;

-- 
-- add changesets to synchronize changelog
--
INSERT INTO databasechangelog (id, author, filename, dateexecuted, md5sum, description, comments, tag, liquibase) VALUES ('reset_checksum_001', 'unknown', 'de/communardo/kenmei/database/update/db.changelog.default.xml', CURRENT_TIMESTAMP, '6c50cd6ee695161d8f569de01f98956', 'Custom SQL', 'Reset Checksums', NULL, '1.9.2');
INSERT INTO databasechangelog (id, author, filename, dateexecuted, md5sum, description, comments, tag, liquibase) VALUES ('mt2849', 'unknown', 'classpath:de/communardo/kenmei/database/update/v1.0.2/db.changelog.v1.0.2.xml', CURRENT_TIMESTAMP, 'dab22a3ed157bef34b18d0a48199f19d', 'Add Column (x2)', '2849: [Widget] Render Image Url to external systems into the widget', NULL, '1.9.2');
INSERT INTO databasechangelog (id, author, filename, dateexecuted, md5sum, description, comments, tag, liquibase) VALUES ('mt2866_1', 'unknown', 'de/communardo/kenmei/database/update/v1.0.3/db.changelog.v1.0.3.xml', CURRENT_TIMESTAMP, '57da76f41130a0648519cdaa55eccb8', 'Drop Unique Constraint', 'Manage constraints', NULL, '1.9.2');
INSERT INTO databasechangelog (id, author, filename, dateexecuted, md5sum, description, comments, tag, liquibase) VALUES ('mt2866_2', 'unknown', 'de/communardo/kenmei/database/update/v1.0.3/db.changelog.v1.0.3.xml', CURRENT_TIMESTAMP, '7f4c97e9c7653cff1e8284ec6560', 'Drop Unique Constraint', 'Manage constraints', NULL, '1.9.2');
INSERT INTO databasechangelog (id, author, filename, dateexecuted, md5sum, description, comments, tag, liquibase) VALUES ('mt2866_3', 'unknown', 'de/communardo/kenmei/database/update/v1.0.3/db.changelog.v1.0.3.xml', CURRENT_TIMESTAMP, 'f8cebad98d84d81f7f83b48ebc0a534', 'Drop Index', 'Manage constraints', NULL, '1.9.2');
INSERT INTO databasechangelog (id, author, filename, dateexecuted, md5sum, description, comments, tag, liquibase) VALUES ('mt2866_4', 'unknown', 'de/communardo/kenmei/database/update/v1.0.3/db.changelog.v1.0.3.xml', CURRENT_TIMESTAMP, '7a6f867828c55aaed72d20c4a0c25281', 'Drop Index', 'Manage constraints', NULL, '1.9.2');
INSERT INTO databasechangelog (id, author, filename, dateexecuted, md5sum, description, comments, tag, liquibase) VALUES ('mt2866_5', 'unknown', 'de/communardo/kenmei/database/update/v1.0.3/db.changelog.v1.0.3.xml', CURRENT_TIMESTAMP, '6e10995d477abf4e43b1eb508c74cd', 'Add Unique Constraint', 'Manage constraints', NULL, '1.9.2');
INSERT INTO databasechangelog (id, author, filename, dateexecuted, md5sum, description, comments, tag, liquibase) VALUES ('mt2859_1', 'unknown', 'de/communardo/kenmei/database/update/v1.0.3/db.changelog.v1.0.3.xml', CURRENT_TIMESTAMP, '56c07a2f5b0e2ae40d3273b6259475a', 'Add Column (x2), Add Foreign Key Constraint, Drop Column, Drop Unique Constraint, Add Column (x2), Custom SQL, Add Unique Constraint, Add Not-Null Constraint (x2)', 'CR 122 Securing External Authentication', NULL, '1.9.2');
INSERT INTO databasechangelog (id, author, filename, dateexecuted, md5sum, description, comments, tag, liquibase) VALUES ('mt2859_2', 'unknown', 'de/communardo/kenmei/database/update/v1.0.3/db.changelog.v1.0.3.xml', CURRENT_TIMESTAMP, 'b2e0e51b2fdd768127d43d458e21d939', 'Add Unique Constraint', '', NULL, '1.9.2');
INSERT INTO databasechangelog (id, author, filename, dateexecuted, md5sum, description, comments, tag, liquibase) VALUES ('test10', 'unknown', 'classpath:de/communardo/kenmei/database/update/v1.0.1/db.changelog.v1.0.1.xml', CURRENT_TIMESTAMP, '1fcdece94dbda9f7a75d7b6abf09db0', 'Custom SQL', 'just a test change set', NULL, '1.9.2');
INSERT INTO databasechangelog (id, author, filename, dateexecuted, md5sum, description, comments, tag, liquibase) VALUES ('mt2213_1', 'unknown', 'classpath:de/communardo/kenmei/database/update/v1.0.1/db.changelog.v1.0.1.xml', CURRENT_TIMESTAMP, 'aef67110d2b012e7747cc3d1573caaa', 'Custom SQL', '2213: CR 2 - API for Mobile Access and 3rd Systems: Last Modification Date on User profile', NULL, '1.9.2');
INSERT INTO databasechangelog (id, author, filename, dateexecuted, md5sum, description, comments, tag, liquibase) VALUES ('mt2213_2', 'unknown', 'classpath:de/communardo/kenmei/database/update/v1.0.1/db.changelog.v1.0.1.xml', CURRENT_TIMESTAMP, 'cd55f272caed519568b49fb2a168646', 'Custom SQL', '2213: CR 2 - API for Mobile Access and 3rd Systems: Last Modification Date on User profile', NULL, '1.9.2');
INSERT INTO databasechangelog (id, author, filename, dateexecuted, md5sum, description, comments, tag, liquibase) VALUES ('mt2213_3', 'unknown', 'classpath:de/communardo/kenmei/database/update/v1.0.1/db.changelog.v1.0.1.xml', CURRENT_TIMESTAMP, '2cba3b1533411c8c06710f3f654c3', 'Custom SQL', '2213: CR 2 - API for Mobile Access and 3rd Systems: Property for downloading the mobile application', NULL, '1.9.2');
INSERT INTO databasechangelog (id, author, filename, dateexecuted, md5sum, description, comments, tag, liquibase) VALUES ('mt2695_1', 'unknown', 'classpath:de/communardo/kenmei/database/update/v1.0.1/db.changelog.v1.0.1.xml', CURRENT_TIMESTAMP, 'f9ad58a8c74f90472d1c7bcb93cac554', 'Custom SQL', '2695: showing the IP range in update dialog the way it was inserted: Keep IPv4 IP range in IPv4 format', NULL, '1.9.2');
INSERT INTO databasechangelog (id, author, filename, dateexecuted, md5sum, description, comments, tag, liquibase) VALUES ('mt2711_1', 'unknown', 'classpath:de/communardo/kenmei/database/update/v1.0.1/db.changelog.v1.0.1.xml', CURRENT_TIMESTAMP, '761357100419dea39489e458c6f3cb', 'Custom SQL', '2711: [CompositeWidget] - CR 18 - Confluence Integration - Optimisation', NULL, '1.9.2');
INSERT INTO databasechangelog (id, author, filename, dateexecuted, md5sum, description, comments, tag, liquibase) VALUES ('mt2773_1', 'unknown', 'classpath:de/communardo/kenmei/database/update/v1.0.1/db.changelog.v1.0.1.xml', CURRENT_TIMESTAMP, 'c37026804025a07bcd84cce2a2f1db92', 'Custom SQL', '2773: CR 19 - Sharepoint integration', NULL, '1.9.2');
INSERT INTO databasechangelog (id, author, filename, dateexecuted, md5sum, description, comments, tag, liquibase) VALUES ('mt1957_1', 'unknown', 'classpath:de/communardo/kenmei/database/update/v1.0.2/db.changelog.v1.0.2.xml', CURRENT_TIMESTAMP, '109b8a23e6b76127568cd4441a962715', 'Custom SQL', '1957: CR 99 Create posts for blog right changes', NULL, '1.9.2');
INSERT INTO databasechangelog (id, author, filename, dateexecuted, md5sum, description, comments, tag, liquibase) VALUES ('mt2276_1', 'unknown', 'classpath:de/communardo/kenmei/database/update/v1.0.2/db.changelog.v1.0.2.xml', CURRENT_TIMESTAMP, 'bc1f408713c4875855223778974bb3d', 'Custom SQL', '2276: CR 43 Delete/Disable user: update name of user status constant DISABLED', NULL, '1.9.2');

INSERT INTO databasechangelog (id, author, filename, dateexecuted, md5sum, description, comments, tag, liquibase) VALUES ('mt2699_1', 'unknown', 'classpath:de/communardo/kenmei/database/update/v1.0.2/db.changelog.v1.0.2.xml', CURRENT_TIMESTAMP, '31cec3dd4dc32191a4e5d578127ffd5', 'Custom SQL', '2699: CR 71 - Force SSL | 2698: Redirect einer https Anfrage wird zu http Anfrage', NULL, '1.9.2');
INSERT INTO databasechangelog (id, author, filename, dateexecuted, md5sum, description, comments, tag, liquibase) VALUES ('mt2710_1', 'unknown', 'classpath:de/communardo/kenmei/database/update/v1.0.2/db.changelog.v1.0.2.xml', CURRENT_TIMESTAMP, '36b9adb128bbb5f23297f8b44d227015', 'Custom SQL', '2710: tags get lost when removing origin of crosspost created through edit operation', NULL, '1.9.2');
INSERT INTO databasechangelog (id, author, filename, dateexecuted, md5sum, description, comments, tag, liquibase) VALUES ('mt2899_postgresql', 'unknown', 'de/communardo/kenmei/database/update/v1.0.3/db.changelog.v1.0.3.xml', CURRENT_TIMESTAMP, '1a4419e2f5f8b265b2b7a39b3af0ad80', 'Custom SQL', 'MT 2899: Image caching problems', NULL, '1.9.2');
INSERT INTO databasechangelog (id, author, filename, dateexecuted, md5sum, description, comments, tag, liquibase) VALUES ('20091007_postgresql', 'unknown', 'de/communardo/kenmei/database/update/v1.0.3/db.changelog.v1.0.3.xml', CURRENT_TIMESTAMP, '89652831b7f99da19387e93f72471b', 'Custom SQL', '', NULL, '1.9.2');
INSERT INTO databasechangelog (id, author, filename, dateexecuted, md5sum, description, comments, tag, liquibase) VALUES ('mt2846_global_id_checksum_v2', 'unknown', 'de/communardo/kenmei/database/update/v1.1/db.changelog.v1.1.refactor.xml', CURRENT_TIMESTAMP, '304f2354db312562949c37255a17bf', 'Custom SQL', 'MT 2846: CR 119 - Refactoring Post Backend: Global Id, Configuration, Checksum', NULL, '1.9.2');
INSERT INTO databasechangelog (id, author, filename, dateexecuted, md5sum, description, comments, tag, liquibase) VALUES ('mt2846_user_external_auth', 'unknown', 'de/communardo/kenmei/database/update/v1.1/db.changelog.v1.1.refactor.xml', CURRENT_TIMESTAMP, '6c8c9284a09b847b41d87b11499cf4b', 'Rename Table, Rename Column, Drop Foreign Key Constraint, Add Foreign Key Constraint', 'MT 2846: CR 119 - Refactoring Post Backend - Rename external user auth', NULL, '1.9.2');
INSERT INTO databasechangelog (id, author, filename, dateexecuted, md5sum, description, comments, tag, liquibase) VALUES ('mt2846_remove_module_status', 'unknown', 'de/communardo/kenmei/database/update/v1.1/db.changelog.v1.1.refactor.xml', CURRENT_TIMESTAMP, 'c616cb81c99fea1e6427599e6cec5187', 'Drop Column (x4)', 'MT 2846: CR 119 - Refactoring Post Backend - Remove module status', NULL, '1.9.2');
INSERT INTO databasechangelog (id, author, filename, dateexecuted, md5sum, description, comments, tag, liquibase) VALUES ('mt2846_postgresql', 'unknown', 'de/communardo/kenmei/database/update/v1.1/postgres/db.changelog.v1.1.refactor.postgres.xml', CURRENT_TIMESTAMP, 'b191e89b90e3ff4f1f99e15ea9862831', 'Drop Table (x6), Drop Sequence (x2), Drop Foreign Key Constraint (x9), Drop Primary Key (x4), Create Table, Create Sequence, Create Table, SQL From File, Drop Column (x7), SQL From File, Drop Column, Rename Table (x2), Add Not-Null Constraint, SQL From...', 'MT 2846: CR 119 - Refactoring Post Backend - postgresql', NULL, '1.9.2');
INSERT INTO databasechangelog (id, author, filename, dateexecuted, md5sum, description, comments, tag, liquibase) VALUES ('mt2846_favorite_users', 'unknown', 'de/communardo/kenmei/database/update/v1.1/db.changelog.v1.1.refactor.xml', CURRENT_TIMESTAMP, '9f4a84f23eb8c55a4730335d35c3ab7f', 'Rename Table', 'MT 2846: CR 119 - Refactoring Post Backend - Favorite users', NULL, '1.9.2');
INSERT INTO databasechangelog (id, author, filename, dateexecuted, md5sum, description, comments, tag, liquibase) VALUES ('mt2940_1', 'unknown', 'de/communardo/kenmei/database/update/v1.1/db.changelog.v1.1.xml', CURRENT_TIMESTAMP, 'b962fa5b9ddcecbc21d62b4999d8764', 'Update Data, Create Table, Add Foreign Key Constraint (x2)', 'CR 131: autosave refactoring', NULL, '1.9.2');
INSERT INTO databasechangelog (id, author, filename, dateexecuted, md5sum, description, comments, tag, liquibase) VALUES ('mt2945_1', 'unknown', 'de/communardo/kenmei/database/update/v1.1/db.changelog.v1.1.xml', CURRENT_TIMESTAMP, '22b680833818a22cf3f5e695a7b185d', 'Add Column, Add Foreign Key Constraint', 'CR 109: Post without selecting a blog', NULL, '1.9.2');
INSERT INTO databasechangelog (id, author, filename, dateexecuted, md5sum, description, comments, tag, liquibase) VALUES ('mt2957_1', 'unknown', 'de/communardo/kenmei/database/update/v1.1/db.changelog.v1.1.xml', CURRENT_TIMESTAMP, '2a75e566753c4ffb3f42c5fb9896cd', 'Add Column (x2)', 'CR 122 - Threaded answers', NULL, '1.9.2');
INSERT INTO databasechangelog (id, author, filename, dateexecuted, md5sum, description, comments, tag, liquibase) VALUES ('mt2957_2_postgresql', 'unknown', 'de/communardo/kenmei/database/update/v1.1/db.changelog.v1.1.xml', CURRENT_TIMESTAMP, '8eff1dc49b3e288edc26369aadbdfd', 'Custom SQL', 'CR 122 - Threaded answers', NULL, '1.9.2');
INSERT INTO databasechangelog (id, author, filename, dateexecuted, md5sum, description, comments, tag, liquibase) VALUES ('mt2976_1', 'unknown', 'de/communardo/kenmei/database/update/v1.1/db.changelog.v1.1.xml', CURRENT_TIMESTAMP, 'dcd4c199fd6bf5cd3ca66a34a84afa7', 'Add Column (x3)', 'Content Type', NULL, '1.9.2');
INSERT INTO databasechangelog (id, author, filename, dateexecuted, md5sum, description, comments, tag, liquibase) VALUES ('mt2976_2', 'unknown', 'de/communardo/kenmei/database/update/v1.1/db.changelog.v1.1.xml', CURRENT_TIMESTAMP, '6b7314e763c81f1f96c67deb8d806d69', 'Custom SQL', 'Attachment Status', NULL, '1.9.2');
INSERT INTO databasechangelog (id, author, filename, dateexecuted, md5sum, description, comments, tag, liquibase) VALUES ('cr147_1_postgresql', 'unknown', 'de/communardo/kenmei/database/update/v1.1/db.changelog.v1.1.xml', CURRENT_TIMESTAMP, '5c1f960517d5aa7758b6a10ef29d543', 'Drop Unique Constraint', 'Client Registration without Activation Code', NULL, '1.9.2');
INSERT INTO databasechangelog (id, author, filename, dateexecuted, md5sum, description, comments, tag, liquibase) VALUES ('rename_sequence_for_external_authentication', 'unknown', 'de/communardo/kenmei/database/update/v1.1/db.changelog.v1.1.xml', CURRENT_TIMESTAMP, '4619992ec3fc7887d883c037c0d389', 'Drop Sequence, Custom SQL', '', NULL, '1.9.2');
INSERT INTO databasechangelog (id, author, filename, dateexecuted, md5sum, description, comments, tag, liquibase) VALUES ('rename_default_blog_alias_1_postgresql', 'unknown', 'de/communardo/kenmei/database/update/v1.1/db.changelog.v1.1.xml', CURRENT_TIMESTAMP, 'aab6b129c4acfe2705da4d8d127dd7b', 'Custom SQL', 'Renames the blog alias of the default blog, if an alias ''public'' not exists.', NULL, '1.9.2');
INSERT INTO databasechangelog (id, author, filename, dateexecuted, md5sum, description, comments, tag, liquibase) VALUES ('make_default_blog_default_postgresql', 'unknown', 'de/communardo/kenmei/database/update/v1.1/db.changelog.v1.1.xml', CURRENT_TIMESTAMP, '38cfdbb7258c1ee87a0538f80fe66aa', 'Custom SQL', '', NULL, '1.9.2');
INSERT INTO databasechangelog (id, author, filename, dateexecuted, md5sum, description, comments, tag, liquibase) VALUES ('rename_default_blog_alias_2_postgresql', 'unknown', 'de/communardo/kenmei/database/update/v1.1/db.changelog.v1.1.xml', CURRENT_TIMESTAMP, '2e2a954a68a522fff8e78dd5ddb88f98', 'Custom SQL', 'Renames the blog alias of the default blog, if it is still the message key.', NULL, '1.9.2');
INSERT INTO databasechangelog (id, author, filename, dateexecuted, md5sum, description, comments, tag, liquibase) VALUES ('mt3096_1', 'unknown', 'de/communardo/kenmei/database/update/v1.1/db.changelog.v1.1.xml', CURRENT_TIMESTAMP, 'abcc6ad563565e4050381273ef3fcc5d', 'Custom SQL', 'deletes unconnected large user images', NULL, '1.9.2');
INSERT INTO databasechangelog (id, author, filename, dateexecuted, md5sum, description, comments, tag, liquibase) VALUES ('set_configuration', 'unknown', 'de/communardo/kenmei/database/update/db.changelog.final.xml', CURRENT_TIMESTAMP, '6148c09f2e75f3239da193221c5cedca', 'Custom SQL', 'Assign the configuration FK', NULL, '1.9.2');
--
-- following changeset is one with global context, but it doesn't matter if it is added to the client database as well
--
INSERT INTO databasechangelog (id, author, filename, dateexecuted, md5sum, description, comments, tag, liquibase) VALUES ('mt3178_2_postgresql', 'unknown', 'de/communardo/kenmei/database/update/v1.1/db.changelog.v1.1.xml', CURRENT_TIMESTAMP, 'd1808dcb3e616cfff590fbbdf8494f', 'Insert Row', 'Automated Client Approval', NULL, '1.9.2');
INSERT INTO databasechangelog (id, author, filename, dateexecuted, md5sum, description, comments, tag, liquibase) VALUES ('mt3187_1', 'unknown', 'de/communardo/kenmei/database/update/v1.1/db.changelog.v1.1.xml', CURRENT_TIMESTAMP, '77fa5d42e90366abddbbb7dbb94e61d', 'Drop Foreign Key Constraint', 'Adjust the forein key constraint for kenmei_users_fk.', NULL, '1.9.2');
INSERT INTO databasechangelog (id, author, filename, dateexecuted, md5sum, description, comments, tag, liquibase) VALUES ('mt3187_2', 'unknown', 'de/communardo/kenmei/database/update/v1.1/db.changelog.v1.1.xml', CURRENT_TIMESTAMP, '1b315757661762a2659fde38c03259', 'Add Foreign Key Constraint', 'Adjust the forein key constraint for kenmei_users_fk.', NULL, '1.9.2');
INSERT INTO databasechangelog (id, author, filename, dateexecuted, md5sum, description, comments, tag, liquibase) VALUES ('mt3187_3', 'unknown', 'de/communardo/kenmei/database/update/v1.1/db.changelog.v1.1.xml', CURRENT_TIMESTAMP, '77fa5d42e90366abddbbb7dbb94e61d', 'Drop Foreign Key Constraint', 'Drop the forein key constraint for kenmei_users_fk.', NULL, '1.9.2');
INSERT INTO databasechangelog (id, author, filename, dateexecuted, md5sum, description, comments, tag, liquibase) VALUES ('mt3187_4_postgresql', 'unknown', 'de/communardo/kenmei/database/update/v1.1/db.changelog.v1.1.xml', CURRENT_TIMESTAMP, '8aaffcfeede332f65df9db8a7e8a21d5', 'Rename Column, Add Foreign Key Constraint', 'Adjust core_users2favorite_notes and rename the column kenmei_user_fk to favorite_users_fk.', NULL, '1.9.2');
INSERT INTO databasechangelog (id, author, filename, dateexecuted, md5sum, description, comments, tag, liquibase) VALUES ('mt3196_1', 'unknown', 'de/communardo/kenmei/database/update/v1.1.2/db.changelog.v1.1.2.xml', CURRENT_TIMESTAMP, '3f4f52f46b355cec745a24706cca46d4', 'Add Column', 'CR 100 - Support Time Zones: add new column to user_profile', NULL, '1.9.2');
INSERT INTO databasechangelog (id, author, filename, dateexecuted, md5sum, description, comments, tag, liquibase) VALUES ('mt3196_2', 'unknown', 'de/communardo/kenmei/database/update/v1.1.2/db.changelog.v1.1.2.xml', CURRENT_TIMESTAMP, 'dca47279adc3bbd1ad7e1d7174e10b1', 'Add Column (x2)', 'CR 100 - Support Time Zones: add new column to user_client_creation, configuration_client', NULL, '1.9.2');
INSERT INTO databasechangelog (id, author, filename, dateexecuted, md5sum, description, comments, tag, liquibase) VALUES ('mt3208', 'unknown', 'de/communardo/kenmei/database/update/v1.1.2/db.changelog.v1.1.2.xml', CURRENT_TIMESTAMP, 'a5e3666944ac3f968080ff852bd777d3', 'Add Column', 'CR 68 Read-More functionality', NULL, '1.9.2');
INSERT INTO databasechangelog (id, author, filename, dateexecuted, md5sum, description, comments, tag, liquibase) VALUES ('mt3350_configuration_column_ldap', 'unknown', 'de/communardo/kenmei/database/update/v1.1.2/db.changelog.v1.1.2.xml', CURRENT_TIMESTAMP, 'dd10f32f1dfab6778b0c3eb60acd4ba', 'Drop Column', 'MT 3350', NULL, '1.9.2');
INSERT INTO databasechangelog (id, author, filename, dateexecuted, md5sum, description, comments, tag, liquibase) VALUES ('mt2846_global_id_v2', 'unknown', 'de/communardo/kenmei/database/update/v1.1/db.changelog.v1.1.refactor.xml', CURRENT_TIMESTAMP, '58bc1037a63ce304caa7c551b843d7', 'Insert Row', 'MT 2846: CR 119 - Refactoring Post Backend: Global Id', NULL, '1.9.2');
INSERT INTO databasechangelog (id, author, filename, dateexecuted, md5sum, description, comments, tag, liquibase) VALUES ('mt3277_1_postgresql', 'unknown', 'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', CURRENT_TIMESTAMP, '3e79f8a44a768c7fbb276e9f42e1ec9e', 'Create Table, Custom SQL, Drop Column, Add Foreign Key Constraint, Create Sequence, Custom SQL, Drop Sequence, Add Foreign Key Constraint', 'CR 96 - support for groups: add user_entity and copy users', NULL, '1.9.2');
INSERT INTO databasechangelog (id, author, filename, dateexecuted, md5sum, description, comments, tag, liquibase) VALUES ('mt3277_2_postgresql', 'unknown', 'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', CURRENT_TIMESTAMP, '88fa38c4a5f8ae66e1cbae3414dfccf', 'Drop Table (x2)', 'CR 96 - support for groups: remove obsolete security codes', NULL, '1.9.2');
INSERT INTO databasechangelog (id, author, filename, dateexecuted, md5sum, description, comments, tag, liquibase) VALUES ('mt3277_3_postgresql', 'unknown', 'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', CURRENT_TIMESTAMP, 'b72b96a07378fb73577720522850d5d4', 'Add Column, Custom SQL', 'CR 96 - support for groups: copy all_can_x rights from group to blog', NULL, '1.9.2');
INSERT INTO databasechangelog (id, author, filename, dateexecuted, md5sum, description, comments, tag, liquibase) VALUES ('mt3277_4_postgresql', 'unknown', 'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', CURRENT_TIMESTAMP, '25c695869c8f851fd412ca90db5b2677', 'Add Column, Custom SQL', 'CR 96 - support for groups: add blogs to group member', NULL, '1.9.2');
INSERT INTO databasechangelog (id, author, filename, dateexecuted, md5sum, description, comments, tag, liquibase) VALUES ('mt3277_5_postgresql', 'unknown', 'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', CURRENT_TIMESTAMP, 'ec2e40b661e8abf982e63f7cdc2a207b', 'Drop Foreign Key Constraint (x2), Drop Primary Key, Drop Column (x2), Custom SQL, Rename Table, Rename Column (x2), Add Column, Add Foreign Key Constraint (x2), Custom SQL, Add Not-Null Constraint', 'CR 96 - support for groups: group member to blog member', NULL, '1.9.2');
INSERT INTO databasechangelog (id, author, filename, dateexecuted, md5sum, description, comments, tag, liquibase) VALUES ('mt3277_6_postgresql', 'unknown', 'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', CURRENT_TIMESTAMP, 'da4346ff7575e6ebf6b3cd1266164', 'Drop Foreign Key Constraint, Drop Column', 'CR 96 - support for groups: cleanup core_blog', NULL, '1.9.2');
INSERT INTO databasechangelog (id, author, filename, dateexecuted, md5sum, description, comments, tag, liquibase) VALUES ('mt3277_7_postgresql', 'unknown', 'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', CURRENT_TIMESTAMP, 'e3ade68c679ed8297e1837d4157a806e', 'Drop Column (x3), Delete Data, Drop Sequence, Add Column (x3), Add Foreign Key Constraint', 'CR 96 - support for groups: fix user_group', NULL, '1.9.2');
INSERT INTO databasechangelog (id, author, filename, dateexecuted, md5sum, description, comments, tag, liquibase) VALUES ('mt3277_8_postgresql', 'unknown', 'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', CURRENT_TIMESTAMP, 'aeb31ecf1d4fcc24eab6609a8ca47465', 'Create Table, Create Sequence', 'CR 96 - support for groups: add helper table for fast blog role lookup', NULL, '1.9.2');
INSERT INTO databasechangelog (id, author, filename, dateexecuted, md5sum, description, comments, tag, liquibase) VALUES ('mt3277_9_postgresql', 'unknown', 'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', CURRENT_TIMESTAMP, 'c27edd6bf78bb4fb2e7ed6ad76884', 'Custom SQL', 'CR 96 - support for groups: fill core_role2blog', NULL, '1.9.2');
INSERT INTO databasechangelog (id, author, filename, dateexecuted, md5sum, description, comments, tag, liquibase) VALUES ('mt3277_10_postgresql', 'unknown', 'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', CURRENT_TIMESTAMP, 'fa346348fef8d167f2390c9b247f053', 'Create Table, Add Primary Key, Add Foreign Key Constraint (x2)', 'CR 96 - support for groups: allow several granting groups', NULL, '1.9.2');
INSERT INTO databasechangelog (id, author, filename, dateexecuted, md5sum, description, comments, tag, liquibase) VALUES ('mt3277_11_postgresql', 'unknown', 'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', CURRENT_TIMESTAMP, 'c83fd6a7d42279a72d55ededfcfa4d86', 'Create Table, Add Primary Key, Add Foreign Key Constraint (x2)', 'CR 96 - support for groups: connect entity with group', NULL, '1.9.2');
INSERT INTO databasechangelog (id, author, filename, dateexecuted, md5sum, description, comments, tag, liquibase) VALUES ('mt3277_12_postgresql', 'unknown', 'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', CURRENT_TIMESTAMP, 'ee8051d2be251e5b45b1991b5b9a9d8d', 'Add Primary Key', 'CR 96 - support for groups: primary key for core_blog_member', NULL, '1.9.2');
INSERT INTO databasechangelog (id, author, filename, dateexecuted, md5sum, description, comments, tag, liquibase) VALUES ('mt3277_13_postgresql', 'unknown', 'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', CURRENT_TIMESTAMP, 'a34dbbe11227d567214cdd8ec5e5bc76', 'Add Not-Null Constraint (x2)', 'CR 96 - support for groups: not null constraints for all_can_x rights', NULL, '1.9.2');
INSERT INTO databasechangelog (id, author, filename, dateexecuted, md5sum, description, comments, tag, liquibase) VALUES ('mt3281_1_postgresql', 'unknown', 'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', CURRENT_TIMESTAMP, '3ebca4821f7296e11d97c5913fe8707c', 'Insert Row', 'CR 134 - Anonymous Access, Anonymous User', NULL, '1.9.2');
INSERT INTO databasechangelog (id, author, filename, dateexecuted, md5sum, description, comments, tag, liquibase) VALUES ('mt3281_2_postgresql', 'unknown', 'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', CURRENT_TIMESTAMP, '2dc05cec435434be83aba19fcba92b', 'Add Column, Add Not-Null Constraint', 'CR 134 - Anonymous Access, Anonymous User', NULL, '1.9.2');
INSERT INTO databasechangelog (id, author, filename, dateexecuted, md5sum, description, comments, tag, liquibase) VALUES ('cr135', 'amo', 'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', CURRENT_TIMESTAMP, 'a02a9368d39aadb2b9affeff80229c', 'Create Table, Add Foreign Key Constraint', 'CR 135 - User group synchronization', NULL, '1.9.2');
INSERT INTO databasechangelog (id, author, filename, dateexecuted, md5sum, description, comments, tag, liquibase) VALUES ('mt3283_external_configuration', 'unknown', 'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', CURRENT_TIMESTAMP, 'c2c4eb85364225670b85647601134e0', 'Rename Table (x3), Rename Column, Add Column, Drop Foreign Key Constraint, Add Foreign Key Constraint', 'Refactor External Authentication', NULL, '1.9.2');
INSERT INTO databasechangelog (id, author, filename, dateexecuted, md5sum, description, comments, tag, liquibase) VALUES ('mt3283_confluence_configuration', 'unknown', 'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', CURRENT_TIMESTAMP, '5029a38aca9b7c4c25a4f41f619caa1c', 'Add Column (x2)', '', NULL, '1.9.2');
INSERT INTO databasechangelog (id, author, filename, dateexecuted, md5sum, description, comments, tag, liquibase) VALUES ('mt3283_external_objects', 'unknown', 'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', CURRENT_TIMESTAMP, '75db72d38b92ade15a93f1b854b9048', 'Create Table, Add Unique Constraint, Create Table, Add Foreign Key Constraint (x2), Add Column', '', NULL, '1.9.2');
INSERT INTO databasechangelog (id, author, filename, dateexecuted, md5sum, description, comments, tag, liquibase) VALUES ('mt3283_seq_postgresql', 'unknown', 'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', CURRENT_TIMESTAMP, '5580b0e798d54a4d6998dccd0ea3054', 'Rename Table, Create Sequence, Add Not-Null Constraint', 'Refactor External Authentication', NULL, '1.9.2');
INSERT INTO databasechangelog (id, author, filename, dateexecuted, md5sum, description, comments, tag, liquibase) VALUES ('mt3283_external_objects_2', 'unknown', 'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', CURRENT_TIMESTAMP, 'ff7ed34175992ead7a175351c0d4e9', 'Add Column, Add Not-Null Constraint', 'Add class to blog_member', NULL, '1.9.2');
INSERT INTO databasechangelog (id, author, filename, dateexecuted, md5sum, description, comments, tag, liquibase) VALUES ('cr135_1', 'unknown', 'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', CURRENT_TIMESTAMP, '318b839a3fff783a8f4a4a3e7f5866', 'Add Column', 'Add serviceUrl to confluence configuration', NULL, '1.9.2');
INSERT INTO databasechangelog (id, author, filename, dateexecuted, md5sum, description, comments, tag, liquibase) VALUES ('mt3277_fix_null_constraint', 'unknown', 'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', CURRENT_TIMESTAMP, '97f2d0c8945bae1c69f8f827c57e73', 'Drop Not-Null Constraint', 'Drop wrong "not null" constraint from user entities.', NULL, '1.9.2');
INSERT INTO databasechangelog (id, author, filename, dateexecuted, md5sum, description, comments, tag, liquibase) VALUES ('mt3283_external_objects_3', 'unknown', 'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', CURRENT_TIMESTAMP, 'd3cc6233f8abccd7d6c2eb5017cf77', 'Add Column', '', NULL, '1.9.2');
INSERT INTO databasechangelog (id, author, filename, dateexecuted, md5sum, description, comments, tag, liquibase) VALUES ('mt3292_confluence_permission_url', 'unknown', 'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', CURRENT_TIMESTAMP, '21a7986f567bd1c385abbdfead1e8c3', 'Add Column', 'CR 135 - Support Synchronization of blog rights with external systems', NULL, '1.9.2');
INSERT INTO databasechangelog (id, author, filename, dateexecuted, md5sum, description, comments, tag, liquibase) VALUES ('mt3283_external_objects_fix_key_unique_postgres', 'unknown', 'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', CURRENT_TIMESTAMP, '84c82d1e3b62faa383038847a657b6a', 'Drop Primary Key, Add Column, Create Sequence', 'CR 135 - Support Synchronization of blog rights with external systems', NULL, '1.9.2');
INSERT INTO databasechangelog (id, author, filename, dateexecuted, md5sum, description, comments, tag, liquibase) VALUES ('mt3277_14', 'unknown', 'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', CURRENT_TIMESTAMP, 'e694eb5228924a36a25357646af454e4', 'Add Column, Custom SQL, Add Not-Null Constraint', 'CR 96 - support for groups: grantedByGroup flag for mapping table', NULL, '1.9.2');
INSERT INTO databasechangelog (id, author, filename, dateexecuted, md5sum, description, comments, tag, liquibase) VALUES ('phone_and_fax_country_code_fix_postgres', 'unknown', 'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', CURRENT_TIMESTAMP, 'c3ab12cef7ea1a81296b35895c9e5246', 'Custom SQL', '', NULL, '1.9.2');
INSERT INTO databasechangelog (id, author, filename, dateexecuted, md5sum, description, comments, tag, liquibase) VALUES ('mt_3272_1_postgres', 'unknown', 'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', CURRENT_TIMESTAMP, '23a0a8c695a167eb63674a3c2bd0ef3c', 'Custom SQL', 'MT 3272: UserProfile saves space instead of NULL if no time zone is selected', NULL, '1.9.2');
INSERT INTO databasechangelog (id, author, filename, dateexecuted, md5sum, description, comments, tag, liquibase) VALUES ('phone_country_code_fix__client_postgres', 'unknown', 'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', CURRENT_TIMESTAMP, '43a246c3c36b1aa24b98d79667aecfc8', 'Custom SQL', '', NULL, '1.9.2');
INSERT INTO databasechangelog (id, author, filename, dateexecuted, md5sum, description, comments, tag, liquibase) VALUES ('cr69_user_groups_v2', 'unknown', 'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', CURRENT_TIMESTAMP, '41e53effcad9d4fe485b75dab0922bf1', 'Rename Column', '', NULL, '1.9.2');
INSERT INTO databasechangelog (id, author, filename, dateexecuted, md5sum, description, comments, tag, liquibase) VALUES ('mt_3314_refactor_confluence_page', 'unknown', 'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', CURRENT_TIMESTAMP, '86b2b96eee76c0d6bdaad4e1c24b5b25', 'Add Column', '', NULL, '1.9.2');
INSERT INTO databasechangelog (id, author, filename, dateexecuted, md5sum, description, comments, tag, liquibase) VALUES ('mt3329_1_postgresql', 'unknown', 'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', CURRENT_TIMESTAMP, '4ca078799d6d2921f43bf663bf31e2b6', 'Create Table, Create Sequence, Custom SQL, Add Foreign Key Constraint (x2)', 'CR 96 - support for hierarchical groups: add user_of_group and copy users', NULL, '1.9.2');
INSERT INTO databasechangelog (id, author, filename, dateexecuted, md5sum, description, comments, tag, liquibase) VALUES ('mt3350_configuration_column_source', 'unknown', 'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', CURRENT_TIMESTAMP, '12af7efa26d7a9eefaedfcd1ad9e2d24', 'Drop Column', 'MT 3350', NULL, '1.9.2');
INSERT INTO databasechangelog (id, author, filename, dateexecuted, md5sum, description, comments, tag, liquibase) VALUES ('confluence_set_basepath_postgresql_new', 'unknown', 'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', CURRENT_TIMESTAMP, 'ae735018153b6174ec84f47772f8f30', 'Custom SQL', '', NULL, '1.9.2');
INSERT INTO databasechangelog (id, author, filename, dateexecuted, md5sum, description, comments, tag, liquibase) VALUES ('remove_uk_country_code', 'unknown', 'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', CURRENT_TIMESTAMP, 'a8a46d136dea83ec4bdfffd11c9b065', 'Custom SQL', '', NULL, '1.9.2');
INSERT INTO databasechangelog (id, author, filename, dateexecuted, md5sum, description, comments, tag, liquibase) VALUES ('cr_179_add_messages_table_v2', 'unknown', 'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', CURRENT_TIMESTAMP, '51bf6b30763f19d84b1ba2aeac1fd14', 'Create Table, Add Foreign Key Constraint, Add Unique Constraint', '', NULL, '1.9.2');
INSERT INTO databasechangelog (id, author, filename, dateexecuted, md5sum, description, comments, tag, liquibase) VALUES ('cr_179_add_messages_table_fix_key_column', 'unknown', 'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', CURRENT_TIMESTAMP, 'db6273f493f2c16fa8c63f1a43ea57', 'Modify Column', '', NULL, '1.9.2');
INSERT INTO databasechangelog (id, author, filename, dateexecuted, md5sum, description, comments, tag, liquibase) VALUES ('cr_179_add_messages_add_sequence', 'unknown', 'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', CURRENT_TIMESTAMP, 'c49bc921e260e39014fcaf49fa5cb2db', 'Create Sequence', '', NULL, '1.9.2');
INSERT INTO databasechangelog (id, author, filename, dateexecuted, md5sum, description, comments, tag, liquibase) VALUES ('cr_179_insert_default_values_for_imprint_terms_of_use', 'unknown', 'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', CURRENT_TIMESTAMP, 'bd894dec5e563ba3bbe80105072455c', 'Custom SQL', '', NULL, '1.9.2');
INSERT INTO databasechangelog (id, author, filename, dateexecuted, md5sum, description, comments, tag, liquibase) VALUES ('cr_179_add_is_Html_column_v2', 'unknown', 'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', CURRENT_TIMESTAMP, '51c815f15bbb47cb1990c3f556b2fe', 'Add Column, Add Not-Null Constraint', '', NULL, '1.9.2');
