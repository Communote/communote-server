SET NUMERIC_ROUNDABORT OFF;
SET ANSI_PADDING, ANSI_WARNINGS, CONCAT_NULL_YIELDS_NULL, ARITHABORT,
QUOTED_IDENTIFIER, ANSI_NULLS ON;

CREATE TABLE
    channel_configuration
    (
        ID BIGINT NOT NULL IDENTITY PRIMARY KEY,
        force_ssl BIT NOT NULL,
        channel_type NVARCHAR(1024) NOT NULL
    );
CREATE TABLE
    configuration
    (
        ID BIGINT NOT NULL IDENTITY PRIMARY KEY,
        CLIENT_CONFIG_FK BIGINT,
    );

CREATE UNIQUE NONCLUSTERED INDEX configuration_client_config_fk_key ON configuration(CLIENT_CONFIG_FK) WHERE CLIENT_CONFIG_FK IS NOT NULL;

CREATE TABLE
    configuration_app_setting
    (
        SETTING_KEY NVARCHAR(255) NOT NULL,
        setting_value nvarchar(max) NOT NULL,
        CONSTRAINT pk_configuration_app_setting PRIMARY KEY (SETTING_KEY)
    );
CREATE TABLE
    configuration_client
    (
        ID BIGINT NOT NULL IDENTITY PRIMARY KEY,
        logo_image VARBINARY(max),
        last_logo_image_modification_d DATETIME,
        time_zone_id NVARCHAR(1024),
        DEFAULT_BLOG_FK BIGINT,
    );

CREATE UNIQUE NONCLUSTERED INDEX configuration_client_default_blog_fk_key ON configuration_client(DEFAULT_BLOG_FK) WHERE DEFAULT_BLOG_FK IS NOT NULL;

CREATE TABLE
    configuration_confluence
    (
        ID BIGINT NOT NULL PRIMARY KEY,
        authentication_api_url NVARCHAR(1024) NOT NULL,
        image_api_url NVARCHAR(1024),
        admin_login NVARCHAR(1024),
        admin_password NVARCHAR(1024),
        service_url NVARCHAR(1024),
        permissions_url NVARCHAR(1024),
        base_path NVARCHAR(1024)
    );
CREATE TABLE
    configuration_external_system
    (
        ID BIGINT NOT NULL IDENTITY PRIMARY KEY,
        allow_external_authentication BIT NOT NULL,
        system_id NVARCHAR(50) NOT NULL,
        primary_authentication BIT NOT NULL,
        synchronize_user_groups BIT NOT NULL,
        CONFIGURATION_FK BIGINT,
        CONSTRAINT configuration_external_system_system_id_key UNIQUE (system_id)
    );
CREATE TABLE
    configuration_ldap
    (
        ID BIGINT NOT NULL PRIMARY KEY,
        url NVARCHAR(1024) NOT NULL,
        manager_password NVARCHAR(1024) NOT NULL,
        manager_d_n NVARCHAR(1024) NOT NULL,
        GROUP_SYNC_CONFIG_FK BIGINT,
        USER_SEARCH_FK BIGINT NOT NULL,        
        user_identifier_is_binary BIT NOT NULL,
        sasl_mode NVARCHAR(1024),
    );

CREATE UNIQUE NONCLUSTERED INDEX configuration_ldap_group_sync_config_fk_key ON configuration_ldap(GROUP_SYNC_CONFIG_FK) WHERE GROUP_SYNC_CONFIG_FK IS NOT NULL;

CREATE TABLE
    configuration_ldap_group
    (
        ID BIGINT NOT NULL IDENTITY PRIMARY KEY,
        member_mode BIT NOT NULL,
        group_identifier_is_binary BIT NOT NULL,
        GROUP_SEARCH_FK BIGINT NOT NULL,
        CONSTRAINT configuration_ldap_group_group_search_fk_key UNIQUE (GROUP_SEARCH_FK)
    );
CREATE TABLE
    configuration_ldap_sbase
    (
        ID BIGINT NOT NULL IDENTITY PRIMARY KEY,
        search_base NVARCHAR(1024) NOT NULL,
        search_subtree BIT NOT NULL,
        LDAP_SEARCH_CONFIGURATION_FK BIGINT,
        sbase_idx INT
    );
CREATE TABLE
    configuration_ldap_search
    (
        ID BIGINT NOT NULL IDENTITY PRIMARY KEY,
        search_filter NVARCHAR(1024),
        property_mapping NVARCHAR(1024) NOT NULL
    );
CREATE TABLE
    configuration_setting
    (
        SETTING_KEY NVARCHAR(255) NOT NULL,
        setting_value NVARCHAR(max),
        CONFIGURATION_FK BIGINT,
        CONSTRAINT pk_configuration_setting PRIMARY KEY (SETTING_KEY)
    );
CREATE TABLE
    configuration_sharepoint
    (
        ID BIGINT NOT NULL PRIMARY KEY,
        authentication_api_url NVARCHAR(1024) NOT NULL,
        admin_password NVARCHAR(1024) NOT NULL,
        admin_login NVARCHAR(1024) NOT NULL,
        image_api_url NVARCHAR(1024)
    );
CREATE TABLE
    core_attachment
    (
        ID BIGINT NOT NULL IDENTITY PRIMARY KEY,
        content_identifier NVARCHAR(1024) NOT NULL,
        repository_identifier NVARCHAR(1024) NOT NULL,
        name NVARCHAR(1024) NOT NULL,
        content_type NVARCHAR(1024),
        size BIGINT,
        status NVARCHAR(1024) NOT NULL,
        GLOBAL_ID_FK BIGINT,
        NOTE_FK BIGINT
    );
    
CREATE UNIQUE NONCLUSTERED INDEX core_attachment_global_id_fk_key ON core_attachment(GLOBAL_ID_FK) WHERE GLOBAL_ID_FK IS NOT NULL;
    
CREATE TABLE
    core_blog
    (
        ID BIGINT NOT NULL IDENTITY PRIMARY KEY,
        title NVARCHAR(450) NOT NULL,
        description NVARCHAR(max),
        creation_date DATETIME NOT NULL,
        name_identifier NVARCHAR(255) NOT NULL,
        last_modification_date DATETIME NOT NULL,
        all_can_read BIT NOT NULL,
        all_can_write BIT NOT NULL,
        public_access BIT NOT NULL,
        create_system_notes BIT NOT NULL,
        GLOBAL_ID_FK BIGINT,
        CONSTRAINT core_blog_name_identifier_key UNIQUE (name_identifier)
    );
    
CREATE UNIQUE NONCLUSTERED INDEX core_blog_global_id_fk_key ON core_blog(GLOBAL_ID_FK) WHERE GLOBAL_ID_FK IS NOT NULL;
    
CREATE TABLE
    core_blog2tag
    (
        BLOGS_FK BIGINT NOT NULL,
        TAGS_FK BIGINT NOT NULL,
        CONSTRAINT blogs_tag_constraint UNIQUE (BLOGS_FK,TAGS_FK),
    );
CREATE TABLE
    core_blog_member
    (
        ID BIGINT NOT NULL IDENTITY PRIMARY KEY,
        class NVARCHAR(255) NOT NULL,
        role NVARCHAR(1024) NOT NULL,
        BLOG_FK BIGINT NOT NULL,
        KENMEI_ENTITY_FK BIGINT NOT NULL,
        external_system_id NVARCHAR(50)
    );
CREATE TABLE
    core_blog_property
    (
        ID BIGINT NOT NULL IDENTITY PRIMARY KEY,
        property_value nvarchar(max) NOT NULL,
        key_group NVARCHAR(128) NOT NULL,
        property_key NVARCHAR(128) NOT NULL,
        last_modification_date DATETIME NOT NULL DEFAULT '1983-06-19 04:09:23',
        BLOG_FK BIGINT
    );
CREATE TABLE
    core_content
    (
        ID BIGINT NOT NULL IDENTITY PRIMARY KEY,
        content nvarchar(max) NOT NULL,
        short_content NVARCHAR(max)
    );
CREATE TABLE
    core_external_object
    (
        ID BIGINT NOT NULL IDENTITY PRIMARY KEY,
        external_system_id NVARCHAR(50) NOT NULL,
        external_id NVARCHAR(200) NOT NULL,
        external_name NVARCHAR(1024),
        BLOG_FK BIGINT,
        CONSTRAINT core_external_object_external_system_id_key UNIQUE (external_system_id, external_id)
    );
CREATE TABLE
    core_external_object_properties
    (
        ID BIGINT NOT NULL IDENTITY PRIMARY KEY,
        property_key NVARCHAR(300) NOT NULL,
        property_value NVARCHAR(1024) NOT NULL,
        EXTERNAL_OBJECT_FK BIGINT
    );
CREATE TABLE
    core_global_binary_property
    (
        ID BIGINT NOT NULL IDENTITY,
        key_group NVARCHAR(128) NOT NULL,
        property_key NVARCHAR(128) NOT NULL,
        last_modification_date DATETIME NOT NULL DEFAULT '1983-06-19 04:09:23',
        property_value VARBINARY(max) NOT NULL
        CONSTRAINT pk_core_global_binary_property PRIMARY KEY (ID)
    );
CREATE TABLE
    core_global_id
    (
        ID BIGINT NOT NULL IDENTITY PRIMARY KEY,
        global_identifier NVARCHAR(255) NOT NULL,
        CONSTRAINT core_global_id_global_identifier_key UNIQUE (global_identifier)
    );
CREATE TABLE
    core_global_string_property
    (
        ID BIGINT NOT NULL IDENTITY PRIMARY KEY,
        property_value nvarchar(max) NOT NULL,
        key_group NVARCHAR(1024) NOT NULL,
        property_key NVARCHAR(1024) NOT NULL,
        last_modification_date DATETIME NOT NULL
    );
CREATE TABLE
    core_note
    (
        ID BIGINT NOT NULL IDENTITY PRIMARY KEY,
        creation_date DATETIME NOT NULL,
        last_modification_date DATETIME NOT NULL,
        creation_source NVARCHAR(1024) NOT NULL,
        direct BIT NOT NULL,
        status NVARCHAR(1024) NOT NULL,
        version BIGINT NOT NULL,
        discussion_path NVARCHAR(1024),
        USER_FK BIGINT NOT NULL,
        CONTENT_FK BIGINT NOT NULL,
        GLOBAL_ID_FK BIGINT,
        BLOG_FK BIGINT NOT NULL,
        PARENT_FK BIGINT,
        ORIGIN_FK BIGINT,
        discussion_id BIGINT,
        CONSTRAINT core_note_content_fk_key UNIQUE (CONTENT_FK)
    );
    
CREATE UNIQUE NONCLUSTERED INDEX  core_note_global_id_fk_key ON core_note(GLOBAL_ID_FK) WHERE GLOBAL_ID_FK IS NOT NULL;

CREATE TABLE
    core_note2direct_user
    (
        DIRECT_NOTES_FK BIGINT NOT NULL,
        DIRECT_USERS_FK BIGINT NOT NULL,
        CONSTRAINT direct_notes_users_key PRIMARY KEY (DIRECT_NOTES_FK,DIRECT_USERS_FK)
    );
CREATE TABLE
    core_note2followable
    (
        NOTES_FK BIGINT NOT NULL,
        FOLLOWABLE_ITEMS_FK BIGINT NOT NULL
        CONSTRAINT notes_followable_items_key PRIMARY KEY (NOTES_FK,FOLLOWABLE_ITEMS_FK)
    );
CREATE TABLE
    core_note_property
    (
        ID BIGINT NOT NULL IDENTITY PRIMARY KEY,
        key_group NVARCHAR(128) NOT NULL,
        property_key NVARCHAR(128) NOT NULL,
        property_value nvarchar(max) NOT NULL,
        NOTE_FK BIGINT,       
        last_modification_date DATETIME NOT NULL DEFAULT '1983-06-19 04:09:23'
    );
CREATE TABLE
    core_notes2crossblogs
    (
        NOTES_FK BIGINT NOT NULL,
        CROSSPOST_BLOGS_FK BIGINT NOT NULL,
        CONSTRAINT notes_crosspost_blogs_key PRIMARY KEY (NOTES_FK,CROSSPOST_BLOGS_FK)
    );
CREATE TABLE
    core_notes2tag
    (
        NOTES_FK BIGINT NOT NULL,
        TAGS_FK BIGINT NOT NULL
        CONSTRAINT notes_tags_key PRIMARY KEY (NOTES_FK,TAGS_FK)
    );
CREATE TABLE
    core_notes2user_to_notify
    (
        NOTES_FK BIGINT NOT NULL,
        USERS_TO_BE_NOTIFIED_FK BIGINT NOT NULL
        CONSTRAINT notes_userstobenotified_key PRIMARY KEY (NOTES_FK,USERS_TO_BE_NOTIFIED_FK)
    );
CREATE TABLE
    core_processed_utp_mail
    (
        ID BIGINT NOT NULL IDENTITY PRIMARY KEY,
        mail_message_id NVARCHAR(255) NOT NULL,
        CONSTRAINT core_processed_utp_mail_mail_message_id_key UNIQUE (mail_message_id)
    );
CREATE TABLE
    core_role2blog
    (
        ID BIGINT NOT NULL IDENTITY PRIMARY KEY,
        blog_id BIGINT NOT NULL,
        user_id BIGINT NOT NULL,
        numeric_role INT NOT NULL,
        external_system_id NVARCHAR(50),
        granted_by_group BIT NOT NULL
    );
CREATE TABLE
    core_role2blog_granting_group
    (
        USER_TO_BLOG_ROLE_MAPPINGS_FK BIGINT NOT NULL,
        GRANTING_GROUPS_FK BIGINT NOT NULL,
        CONSTRAINT brm_gg_key PRIMARY KEY (USER_TO_BLOG_ROLE_MAPPINGS_FK,GRANTING_GROUPS_FK)
    );
CREATE TABLE
    core_tag
    (
        ID BIGINT NOT NULL IDENTITY PRIMARY KEY,
        class NVARCHAR(255) NOT NULL,
        lower_name NVARCHAR(450) NOT NULL,
        name NVARCHAR(255) NOT NULL,
        GLOBAL_ID_FK BIGINT,
        CATEGORY_FK BIGINT,
        ABSTRACT_TAG_CATEGORY_TAGS_IDX INT,
        CONSTRAINT core_tag_name_key UNIQUE (name)
    );
    
CREATE UNIQUE NONCLUSTERED INDEX  core_tag_global_id_fk_key ON core_tag(GLOBAL_ID_FK) WHERE GLOBAL_ID_FK IS NOT NULL;

    
CREATE TABLE
    core_tag2clearance_exclude
    (
        TAG_CLEARANCES_FK BIGINT NOT NULL,
        EXCLUDE_TAGS_FK BIGINT NOT NULL,
        TAG_CLEARANCE_EXCLUDE_TAGS_IDX INT NOT NULL,
        CONSTRAINT tag_clearances_exclude_tags_key PRIMARY KEY (TAG_CLEARANCES_FK,EXCLUDE_TAGS_FK)
    );
CREATE TABLE
    core_tag2clearance_include
    (
        TAG_CLEARANCES_FK BIGINT NOT NULL,
        INCLUDE_TAGS_FK BIGINT NOT NULL,
        TAG_CLEARANCE_INCLUDE_TAGS_IDX INT NOT NULL,
        CONSTRAINT tag_clearances_include_tags_key PRIMARY KEY (TAG_CLEARANCES_FK,TAG_CLEARANCE_INCLUDE_TAGS_IDX)
    );
CREATE TABLE
    core_tag_category
    (
        ID BIGINT NOT NULL IDENTITY PRIMARY KEY,
        class NVARCHAR(255) NOT NULL,
        name NVARCHAR(1024) NOT NULL,
        prefix NVARCHAR(1024) NOT NULL,
        description NVARCHAR(1024),
        multiple_tags BIT NOT NULL
    );
CREATE TABLE
    core_tag_clearance
    (
        ID BIGINT NOT NULL IDENTITY PRIMARY KEY,
        class NVARCHAR(255) NOT NULL,
        include_protected_resources BIT,
        OWNER_FK BIGINT
    );
CREATE TABLE
    core_task
    (
        ID BIGINT NOT NULL IDENTITY PRIMARY KEY,
        unique_name NVARCHAR(255) NOT NULL, 
        task_status NVARCHAR(255) NOT NULL,
        task_interval BIGINT,
        handler_class_name NVARCHAR(1024) NOT NULL,
        active BIT NOT NULL,        
        next_execution DATETIME NOT NULL,
        last_execution DATETIME,
        CONSTRAINT core_task_unique_name_key UNIQUE (unique_name)
    );
CREATE TABLE
    core_task_execs
    (
        ID BIGINT NOT NULL IDENTITY,
        instance_name NVARCHAR(1024) NOT NULL,
        TASK_FK BIGINT NOT NULL,
        CONSTRAINT pk_core_task_execs PRIMARY KEY (ID),
        CONSTRAINT core_task_execs_task_fk_key UNIQUE (TASK_FK)
    );
CREATE TABLE
    core_task_props
    (
        ID BIGINT NOT NULL IDENTITY,
        property_key NVARCHAR(1024) NOT NULL,
        property_value NVARCHAR(1024) NOT NULL,
        TASK_FK BIGINT,
        CONSTRAINT pk_core_task_props PRIMARY KEY (ID)
    );
CREATE TABLE
    core_user2follows
    (
        FOLLOWED_ITEMS_FK BIGINT NOT NULL,
        kenmei_users_fk BIGINT NOT NULL,
        CONSTRAINT followed_items_users_key PRIMARY KEY (kenmei_users_fk, FOLLOWED_ITEMS_FK)
    );
CREATE TABLE
    core_users2favorite_notes
    (
        FAVORITE_NOTES_FK BIGINT NOT NULL,
        FAVORITE_USERS_FK BIGINT NOT NULL,
        CONSTRAINT fav_notes_fav_users PRIMARY KEY (FAVORITE_NOTES_FK, FAVORITE_USERS_FK)
    );
CREATE TABLE
    crc_cache_config
    (
        ID BIGINT NOT NULL PRIMARY KEY,
        time_to_stay BIGINT NOT NULL,
        CURRENT_CACHE_CONNECTOR_CON_FK BIGINT,
        CONSTRAINT crc_cache_config_current_cache_connector_con_fk_key UNIQUE (CURRENT_CACHE_CONNECTOR_CON_FK)
    );
CREATE TABLE
    crc_connector_config
    (
        ID BIGINT NOT NULL IDENTITY PRIMARY KEY,
        supports_metadata BIT NOT NULL,
        connector_id NVARCHAR(1024) NOT NULL
    );
CREATE TABLE
    custom_messages
    (
        ID BIGINT NOT NULL IDENTITY PRIMARY KEY,
        message_key NVARCHAR(255) NOT NULL,
        message nvarchar(max) NOT NULL,
        is_html BIT NOT NULL,
        LANGUAGE_FK BIGINT,
        CONSTRAINT key_language_constraint UNIQUE (message_key, LANGUAGE_FK)
    );
--CREATE TABLE 
--    DATABASECHANGELOG 
--    (
--      ID nvarchar(63) NOT NULL,
--      AUTHOR nvarchar(63) NOT NULL,
--      FILENAME nvarchar(200) NOT NULL,
--      DATEEXECUTED datetime NOT NULL,
--      MD5SUM nvarchar(32) DEFAULT NULL,
--      DESCRIPTION nvarchar(255) DEFAULT NULL,
--      COMMENTS nvarchar(255) DEFAULT NULL,
--      TAG nvarchar(255) DEFAULT NULL,
--      LIQUIBASE nvarchar(10) DEFAULT NULL,
--      CONSTRAINT databasechangelog_pkey PRIMARY KEY (ID,AUTHOR,FILENAME)
--    );

CREATE TABLE
    iprange_channel
    (
        TYPE NVARCHAR(255) NOT NULL PRIMARY KEY,
        enabled BIT NOT NULL
    );
CREATE TABLE
    iprange_filter
    (
        ID BIGINT NOT NULL IDENTITY PRIMARY KEY,
        name NVARCHAR(1024) NOT NULL,
        enabled BIT NOT NULL
    );
CREATE TABLE
    iprange_filter_channel
    (
        IP_RANGE_FILTERS_FK BIGINT NOT NULL,
        CHANNELS_FK NVARCHAR(255) NOT NULL,
        CONSTRAINT range_filters_channels PRIMARY KEY (IP_RANGE_FILTERS_FK, CHANNELS_FK)
    );
CREATE TABLE
    iprange_range
    (
        ID BIGINT NOT NULL IDENTITY PRIMARY KEY,
        start_value VARBINARY(32) NOT NULL,
        end_value VARBINARY(32) NOT NULL,
        string_representation NVARCHAR(1024),
        IP_RANGE_FILTER_IN_FK BIGINT,
        IP_RANGE_FILTER_EX_FK BIGINT
    );
CREATE TABLE
    mc_config
    (
        ID BIGINT NOT NULL IDENTITY PRIMARY KEY,
        type NVARCHAR(1024) NOT NULL,
        properties NVARCHAR(1024) NOT NULL,
        only_if_available BIT NOT NULL,
        priority INT NOT NULL,
        NOTIFICATION_CONFIG_FK BIGINT
    );
CREATE TABLE
    md_country
    (
        ID BIGINT NOT NULL IDENTITY PRIMARY KEY,
        country_code NVARCHAR(255) NOT NULL,
        name NVARCHAR(1024) NOT NULL,
        CONSTRAINT md_country_country_code_key UNIQUE (country_code)
    );
CREATE TABLE
    md_language
    (
        ID BIGINT NOT NULL IDENTITY PRIMARY KEY,
        language_code NVARCHAR(255) NOT NULL,
        name NVARCHAR(1024) NOT NULL,
        CONSTRAINT md_language_language_code_key UNIQUE (language_code)
    );
CREATE TABLE
    misc_query_helper
    (
        ID BIGINT NOT NULL IDENTITY PRIMARY KEY
    );
CREATE TABLE
    notification_config
    (
        ID BIGINT NOT NULL IDENTITY PRIMARY KEY,
        fallback NVARCHAR(1024)
    );
CREATE TABLE
    security_client_approval_code
    (
        ID BIGINT NOT NULL PRIMARY KEY,
        CLIENT_FK BIGINT NOT NULL
    );
CREATE TABLE
    security_client_reg_code
    (
        ID BIGINT NOT NULL PRIMARY KEY,
        CLIENT_FK BIGINT NOT NULL
    );
CREATE TABLE
    security_client_removal_code
    (
        ID BIGINT NOT NULL PRIMARY KEY,
        send_user_notifications BIT NOT NULL,
        CLIENT_FK BIGINT NOT NULL
    );
CREATE TABLE
    security_code
    (
        ID BIGINT NOT NULL IDENTITY PRIMARY KEY,
        code NVARCHAR(255) NOT NULL,
        action NVARCHAR(1024) NOT NULL,
        creating_date DATETIME NOT NULL,
        KENMEI_USER_FK BIGINT,
        CONSTRAINT security_code_code_key UNIQUE (code)
    );
CREATE TABLE
    security_email_code
    (
        ID BIGINT NOT NULL PRIMARY KEY,
        new_email_address NVARCHAR(1024) NOT NULL
    );
CREATE TABLE
    security_forgotten_pw_code
    (
        ID BIGINT NOT NULL PRIMARY KEY
    );
CREATE TABLE
    security_invite_blog
    (
        ID BIGINT NOT NULL PRIMARY KEY
    );
CREATE TABLE
    security_invite_client
    (
        ID BIGINT NOT NULL PRIMARY KEY
    );
CREATE TABLE
    security_user_code
    (
        ID BIGINT NOT NULL PRIMARY KEY
    );

CREATE TABLE
    security_user_auth_failed_status
    (
        ID BIGINT NOT NULL IDENTITY PRIMARY KEY,
        locked_timeout DATETIME,
        failed_auth_counter INT NOT NULL,
        channel_type NVARCHAR(1024) NOT NULL,
        KENMEI_USER_FK BIGINT
    );

CREATE TABLE
    security_user_unlock_code
    (
        ID BIGINT NOT NULL PRIMARY KEY,
        channel NVARCHAR(1024) NOT NULL
    );
CREATE TABLE
    user_authorities
    (
        ID BIGINT NOT NULL IDENTITY PRIMARY KEY,
        role NVARCHAR(1024) NOT NULL,
        KENMEI_USER_FK BIGINT
    );
CREATE TABLE
    user_client
    (
        ID BIGINT NOT NULL IDENTITY PRIMARY KEY,
        client_id NVARCHAR(255) NOT NULL,
        security_key NVARCHAR(1024),
        name NVARCHAR(1024) NOT NULL,
        client_status NVARCHAR(1024) NOT NULL,
        creation_version NVARCHAR(1024),
        creation_time DATETIME,
        creation_revision BIGINT,
        more_data_required BIT NOT NULL,
        CLIENT_D_B_CONFIG_FK BIGINT,
        CREATION_DATA_FK BIGINT,
        CLIENT_PROFILE_FK BIGINT,
    );

CREATE UNIQUE NONCLUSTERED INDEX user_client_client_d_b_config_fk_key ON user_client(CLIENT_D_B_CONFIG_FK) WHERE CLIENT_D_B_CONFIG_FK IS NOT NULL;
CREATE UNIQUE NONCLUSTERED INDEX user_client_creation_data_fk_key ON user_client(CREATION_DATA_FK) WHERE CREATION_DATA_FK IS NOT NULL;
CREATE UNIQUE NONCLUSTERED INDEX user_client_client_id_key ON user_client(client_id) WHERE client_id IS NOT NULL;
CREATE UNIQUE NONCLUSTERED INDEX user_client_client_profile_fk_key ON user_client(CLIENT_PROFILE_FK) WHERE CLIENT_PROFILE_FK IS NOT NULL;

CREATE TABLE
    user_client_activation_code
    (
        ID BIGINT NOT NULL IDENTITY PRIMARY KEY,
        code NVARCHAR(255) NOT NULL,
        used INT NOT NULL,
        created INT NOT NULL,
        CONSTRAINT user_client_activation_code_value_key UNIQUE (code)
    );
CREATE TABLE
    user_client_creation
    (
        ID BIGINT NOT NULL IDENTITY PRIMARY KEY,
        creator_email NVARCHAR(1024) NOT NULL,
        creator_password NVARCHAR(1024) NOT NULL,
        creator_alias NVARCHAR(1024) NOT NULL,
        creator_first_name NVARCHAR(1024),
        creator_last_name NVARCHAR(1024),
        creator_language_code NVARCHAR(1024) NOT NULL,
        time_zone_id NVARCHAR(1024),
        CLIENT_ACTIVATION_CODE_FK BIGINT
    );
CREATE TABLE
    user_client_db_config
    (
        ID BIGINT NOT NULL IDENTITY PRIMARY KEY,
        driver_class_name NVARCHAR(1024) NOT NULL,
        user_name NVARCHAR(1024) NOT NULL,
        password NVARCHAR(1024) NOT NULL,
        schema_name NVARCHAR(1024) NOT NULL,
        protocol NVARCHAR(1024) NOT NULL,
        host NVARCHAR(1024) NOT NULL,
        port NVARCHAR(1024) NOT NULL
    );
CREATE TABLE
    user_client_profile
    (
        ID BIGINT NOT NULL IDENTITY PRIMARY KEY,
        class NVARCHAR(255) NOT NULL,
        company NVARCHAR(1024) NOT NULL,
        department NVARCHAR(1024),
        contact_person_name NVARCHAR(1024) NOT NULL,
        contact_email NVARCHAR(1024) NOT NULL,
        contact_phone_number_country_c NVARCHAR(1024) NOT NULL,
        contact_phone_number_area_code NVARCHAR(1024) NOT NULL,
        contact_phone_number_phone_num NVARCHAR(1024) NOT NULL,
        address NVARCHAR(1024) NOT NULL,
        address_additon NVARCHAR(1024),
        zip NVARCHAR(1024) NOT NULL,
        city NVARCHAR(1024) NOT NULL,
        country_code NVARCHAR(1024) NOT NULL,
        vat_id NVARCHAR(1024)
    );
CREATE TABLE
    user_client_statistic
    (
        ID BIGINT NOT NULL IDENTITY PRIMARY KEY,
        repository_size BIGINT NOT NULL
    );
CREATE TABLE
    user_contact
    (
        ID BIGINT NOT NULL IDENTITY PRIMARY KEY,
        street NVARCHAR(1024),
        zip NVARCHAR(1024),
        city NVARCHAR(1024),
        phone_country_code NVARCHAR(1024) NOT NULL,
        phone_area_code NVARCHAR(1024) NOT NULL,
        phone_phone_number NVARCHAR(1024) NOT NULL,
        fax_country_code NVARCHAR(1024) NOT NULL,
        fax_area_code NVARCHAR(1024) NOT NULL,
        fax_phone_number NVARCHAR(1024) NOT NULL,
        COUNTRY_FK BIGINT
    );
CREATE TABLE
    user_entity
    (
        ID BIGINT NOT NULL IDENTITY PRIMARY KEY,
        GLOBAL_ID_FK BIGINT,
    );
    
CREATE UNIQUE NONCLUSTERED INDEX user_entity_global_id_fk_key ON user_entity(GLOBAL_ID_FK) WHERE GLOBAL_ID_FK IS NOT NULL;

    
CREATE TABLE
    user_external_auth
    (
        ID BIGINT NOT NULL IDENTITY PRIMARY KEY,
        external_user_id NVARCHAR(250) NOT NULL,
        system_id NVARCHAR(50) NOT NULL,
        permanent_id NVARCHAR(1024),
        additional_property NVARCHAR(max),
        KENMEI_USER_FK BIGINT,
        CONSTRAINT user_external_auth_external_user_id_key UNIQUE (external_user_id, system_id)
    );
CREATE TABLE
    user_group
    (
        ID BIGINT NOT NULL PRIMARY KEY,
        alias NVARCHAR(300) NOT NULL,
        name NVARCHAR(1024) NOT NULL,
        description NVARCHAR(max),
        CONSTRAINT user_group_alias_key UNIQUE (alias)
    );
CREATE TABLE
    user_group_external
    (
        ID BIGINT NOT NULL PRIMARY KEY,
        external_system_id NVARCHAR(1024) NOT NULL,
        external_id NVARCHAR(1024) NOT NULL,
        additional_property NVARCHAR(max)
    );
CREATE TABLE
    user_group_member
    (
        GROUP_MEMBERS_FK BIGINT NOT NULL,
        GROUPS_FK BIGINT NOT NULL,
        CONSTRAINT user_group_member_pkey PRIMARY KEY (GROUP_MEMBERS_FK, GROUPS_FK)
    );
CREATE TABLE
    user_group_property
    (
        ID BIGINT NOT NULL IDENTITY PRIMARY KEY,
        property_value nvarchar(max) NOT NULL,
        key_group NVARCHAR(128) NOT NULL,
        property_key NVARCHAR(128) NOT NULL,
        last_modification_date DATETIME NOT NULL DEFAULT '1983-06-19 04:09:23',
        KENMEI_ENTITY_GROUP_FK BIGINT
    );
CREATE TABLE
    user_image
    (
        ID BIGINT NOT NULL IDENTITY PRIMARY KEY,
        image VARBINARY(max) NOT NULL
    );
CREATE TABLE
    user_note_property
    (
        ID BIGINT NOT NULL IDENTITY PRIMARY KEY,
        property_value nvarchar(1024) NOT NULL,
        property_key NVARCHAR(128) NOT NULL,
        key_group NVARCHAR(128) NOT NULL,
        last_modification_date DATETIME NOT NULL DEFAULT '1983-06-19 04:09:23',
        USER_FK BIGINT NOT NULL,
        NOTE_FK BIGINT NOT NULL,
    );
CREATE TABLE
    user_of_group
    (
        ID BIGINT NOT NULL IDENTITY PRIMARY KEY,
        modification_type NVARCHAR(1024),
        GROUP_FK BIGINT NOT NULL,
        USER_FK BIGINT NOT NULL
    );
CREATE TABLE
    user_profile
    (
        ID BIGINT NOT NULL IDENTITY PRIMARY KEY,
        last_name NVARCHAR(450),
        salutation NVARCHAR(1024),
        position NVARCHAR(1024),
        company NVARCHAR(1024),
        first_name NVARCHAR(450),
        last_modification_date DATETIME NOT NULL,
        last_photo_modification_date DATETIME,
        time_zone_id NVARCHAR(1024),
        SMALL_IMAGE_FK BIGINT,
        CONTACT_FK BIGINT,
        MEDIUM_IMAGE_FK BIGINT,
        LARGE_IMAGE_FK BIGINT,
        NOTIFICATION_CONFIG_FK BIGINT NOT NULL,    
    );

CREATE UNIQUE NONCLUSTERED INDEX user_profile_contact_fk_key ON user_profile(CONTACT_FK) WHERE CONTACT_FK IS NOT NULL;
CREATE UNIQUE NONCLUSTERED INDEX user_profile_large_image_fk_key ON user_profile(LARGE_IMAGE_FK) WHERE LARGE_IMAGE_FK IS NOT NULL;
CREATE UNIQUE NONCLUSTERED INDEX user_profile_medium_image_fk_key ON user_profile(MEDIUM_IMAGE_FK) WHERE MEDIUM_IMAGE_FK IS NOT NULL;
CREATE UNIQUE NONCLUSTERED INDEX user_profile_notification_config_fk_key ON user_profile(NOTIFICATION_CONFIG_FK) WHERE NOTIFICATION_CONFIG_FK IS NOT NULL;
CREATE UNIQUE NONCLUSTERED INDEX user_profile_small_image_fk_key ON user_profile(SMALL_IMAGE_FK) WHERE SMALL_IMAGE_FK IS NOT NULL;

CREATE TABLE
    user_user
    (
        ID BIGINT NOT NULL PRIMARY KEY,
        password NVARCHAR(1024),
        email NVARCHAR(255) NOT NULL,
        language_code NVARCHAR(1024) NOT NULL,
        last_login DATETIME,
        status NVARCHAR(1024) NOT NULL,
        alias NVARCHAR(255),
        terms_accepted BIT NOT NULL,
        reminder_mail_sent BIT NOT NULL,
        status_changed DATETIME NOT NULL,
        PROFILE_FK BIGINT NOT NULL,
        CONSTRAINT user_user_email_key UNIQUE (email),
        CONSTRAINT user_user_profile_fk_key UNIQUE (PROFILE_FK)
    );


CREATE UNIQUE NONCLUSTERED INDEX user_user_alias_key ON user_user(alias) WHERE alias IS NOT NULL;

CREATE TABLE
    user_user_property
    (
        ID BIGINT NOT NULL IDENTITY PRIMARY KEY,
        property_value nvarchar(max) NOT NULL,
        key_group NVARCHAR(128) NOT NULL,
        property_key NVARCHAR(128) NOT NULL,
        last_modification_date DATETIME NOT NULL DEFAULT '1983-06-19 04:09:23',
        KENMEI_USER_FK BIGINT
    );
    
    
	
ALTER TABLE
    configuration ADD CONSTRAINT configuration_CLIENT_CONFIG_FC FOREIGN KEY (CLIENT_CONFIG_FK)
    REFERENCES configuration_client (ID);
ALTER TABLE
    configuration_client ADD CONSTRAINT configuration_client_DEFAULT_C FOREIGN KEY (DEFAULT_BLOG_FK
    ) REFERENCES core_blog (ID);
ALTER TABLE
    configuration_confluence ADD CONSTRAINT configuration_confluenceIFKC FOREIGN KEY (ID)
    REFERENCES configuration_external_system (ID);
ALTER TABLE
    configuration_external_system ADD CONSTRAINT configuration_external_systemC FOREIGN KEY
    (CONFIGURATION_FK) REFERENCES configuration (ID);
ALTER TABLE
    configuration_ldap ADD CONSTRAINT configuration_ldapIFKC FOREIGN KEY (ID) REFERENCES
    configuration_external_system (ID);
ALTER TABLE
    configuration_ldap_group ADD CONSTRAINT configuration_ldap_group_GROUC FOREIGN KEY
    (GROUP_SEARCH_FK) REFERENCES configuration_ldap_search (ID);
ALTER TABLE
    configuration_ldap_sbase ADD CONSTRAINT configuration_ldap_sbase_LDAPC FOREIGN KEY
    (LDAP_SEARCH_CONFIGURATION_FK) REFERENCES configuration_ldap_search (ID);
ALTER TABLE
    configuration_setting ADD CONSTRAINT configuration_setting_CONFIGUC FOREIGN KEY
    (CONFIGURATION_FK) REFERENCES configuration (ID);
ALTER TABLE
    configuration_sharepoint ADD CONSTRAINT configuration_sharepointIFKC FOREIGN KEY (ID)
    REFERENCES configuration_external_system (ID);
ALTER TABLE
    core_attachment ADD CONSTRAINT core_attachment_GLOBAL_ID_FKC FOREIGN KEY (GLOBAL_ID_FK)
    REFERENCES core_global_id (ID);
ALTER TABLE
    core_attachment ADD CONSTRAINT core_attachment_NOTE_FKC FOREIGN KEY (NOTE_FK) REFERENCES
    core_note (ID);
ALTER TABLE
    core_blog ADD CONSTRAINT core_blog_GLOBAL_ID_FKC FOREIGN KEY (GLOBAL_ID_FK) REFERENCES
    core_global_id (ID);
ALTER TABLE
    core_blog2tag ADD CONSTRAINT core_tag_BLOGS_FKC FOREIGN KEY (BLOGS_FK) REFERENCES 
	core_blog (ID);
ALTER TABLE
    core_blog2tag ADD CONSTRAINT core_blog_TAGS_FKC FOREIGN KEY (TAGS_FK) REFERENCES 
	core_tag (ID);
ALTER TABLE
    core_blog_member ADD CONSTRAINT core_blog_member_BLOG_FKC FOREIGN KEY (BLOG_FK) REFERENCES
    core_blog (ID);
ALTER TABLE
    core_blog_member ADD CONSTRAINT core_blog_member_KENMEI_ENTITC FOREIGN KEY (KENMEI_ENTITY_FK)
    REFERENCES user_entity (ID);
ALTER TABLE
    core_blog_property ADD CONSTRAINT core_blog_property_BLOG_FKC FOREIGN KEY (BLOG_FK) REFERENCES
    core_blog (ID);
ALTER TABLE
    core_external_object ADD CONSTRAINT core_external_object_BLOG_FKC FOREIGN KEY (BLOG_FK)
    REFERENCES core_blog (ID);
ALTER TABLE
    core_external_object_properties ADD CONSTRAINT core_external_object_propertiC FOREIGN KEY
    (EXTERNAL_OBJECT_FK) REFERENCES core_external_object (ID);
ALTER TABLE
    core_note ADD CONSTRAINT core_note_BLOG_FKC FOREIGN KEY (BLOG_FK) REFERENCES 
	core_blog (ID);
ALTER TABLE
    core_note ADD CONSTRAINT core_note_CONTENT_FKC FOREIGN KEY (CONTENT_FK) REFERENCES 
	core_content (ID);
ALTER TABLE
    core_note ADD CONSTRAINT core_note_GLOBAL_ID_FKC FOREIGN KEY (GLOBAL_ID_FK) REFERENCES
    core_global_id (ID);
ALTER TABLE
    core_note ADD CONSTRAINT core_note_ORIGIN_FKC FOREIGN KEY (ORIGIN_FK) REFERENCES 
	core_note (ID);
ALTER TABLE
    core_note ADD CONSTRAINT core_note_PARENT_FKC FOREIGN KEY (PARENT_FK) REFERENCES 
	core_note (ID);
ALTER TABLE
    core_note ADD CONSTRAINT core_note_USER_FKC FOREIGN KEY (USER_FK) REFERENCES user_user 
	(ID);
ALTER TABLE
    core_note2direct_user ADD CONSTRAINT user_user_DIRECT_NOTES_FKC FOREIGN KEY (DIRECT_NOTES_FK)
    REFERENCES core_note (ID);
ALTER TABLE
    core_note2direct_user ADD CONSTRAINT core_note_DIRECT_USERS_FKC FOREIGN KEY (DIRECT_USERS_FK)
    REFERENCES user_user (ID);
ALTER TABLE
    core_note2followable ADD CONSTRAINT core_note_FOLLOWABLE_ITEMS_FKC FOREIGN KEY
    (FOLLOWABLE_ITEMS_FK) REFERENCES core_global_id (ID);
ALTER TABLE
    core_note2followable ADD CONSTRAINT core_global_id_NOTES_FKC FOREIGN KEY (NOTES_FK) REFERENCES
    core_note (ID);
ALTER TABLE
    core_note_property ADD CONSTRAINT core_note_properties_NOTE_FKC FOREIGN KEY (NOTE_FK) 
    REFERENCES core_note (ID);
ALTER TABLE
    core_notes2crossblogs ADD CONSTRAINT core_note_CROSSPOST_BLOGS_FKC FOREIGN KEY
    (CROSSPOST_BLOGS_FK) REFERENCES core_blog (ID);
ALTER TABLE
    core_notes2crossblogs ADD CONSTRAINT core_blog_NOTES_FKC FOREIGN KEY (NOTES_FK) REFERENCES
    core_note (ID);
ALTER TABLE
    core_notes2tag ADD CONSTRAINT core_note_TAGS_FKC FOREIGN KEY (TAGS_FK) REFERENCES 
	core_tag (ID);
ALTER TABLE
    core_notes2tag ADD CONSTRAINT core_tag_NOTES_FKC FOREIGN KEY (NOTES_FK) REFERENCES 
	core_note (ID);
ALTER TABLE
    core_notes2user_to_notify ADD CONSTRAINT core_note_USERS_TO_BE_NOTIFIEC FOREIGN KEY
    (USERS_TO_BE_NOTIFIED_FK) REFERENCES user_user (ID);
ALTER TABLE
    core_notes2user_to_notify ADD CONSTRAINT user_user_NOTES_FKC FOREIGN KEY (NOTES_FK) REFERENCES
    core_note (ID);
ALTER TABLE
    core_role2blog_granting_group ADD CONSTRAINT core_role2blog_GRANTING_GROUPC FOREIGN KEY
    (GRANTING_GROUPS_FK) REFERENCES user_group (ID);
ALTER TABLE
    core_role2blog_granting_group ADD CONSTRAINT user_group_USER_TO_BLOG_ROLE_C FOREIGN KEY
    (USER_TO_BLOG_ROLE_MAPPINGS_FK) REFERENCES core_role2blog (ID);
ALTER TABLE
    core_tag ADD CONSTRAINT CATEGORIZED_TAG_CATEGORY_FKC FOREIGN KEY (CATEGORY_FK) REFERENCES
    core_tag_category (ID);
ALTER TABLE
    core_tag ADD CONSTRAINT core_tag_GLOBAL_ID_FKC FOREIGN KEY (GLOBAL_ID_FK) REFERENCES
    core_global_id (ID);
ALTER TABLE
    core_tag2clearance_exclude ADD CONSTRAINT core_tag_TAG_CLEARANCES_EXCLUDE_FKC FOREIGN KEY
    (TAG_CLEARANCES_FK) REFERENCES core_tag_clearance (ID);
ALTER TABLE
    core_tag2clearance_exclude ADD CONSTRAINT core_tag_clearance_EXCLUDE_TAC FOREIGN KEY
    (EXCLUDE_TAGS_FK) REFERENCES core_tag (ID);
ALTER TABLE
    core_tag2clearance_include ADD CONSTRAINT core_tag_clearance_INCLUDE_TAC FOREIGN KEY
    (INCLUDE_TAGS_FK) REFERENCES core_tag (ID);
ALTER TABLE
    core_tag2clearance_include ADD CONSTRAINT core_tag_tag_clearances_INCLUDE_FKC FOREIGN KEY
    (TAG_CLEARANCES_FK) REFERENCES core_tag_clearance (ID);
ALTER TABLE
    core_tag_clearance ADD CONSTRAINT core_user_tag_clearance_OWNERC FOREIGN KEY (OWNER_FK)
    REFERENCES user_user (ID);
ALTER TABLE
    core_task_execs ADD CONSTRAINT core_task_execs_TASK_FKC FOREIGN KEY (TASK_FK) REFERENCES
    core_task (ID);
ALTER TABLE
    core_task_props ADD CONSTRAINT core_task_props_TASK_FKC FOREIGN KEY (TASK_FK) REFERENCES
    core_task (ID);
ALTER TABLE
    core_user2follows ADD CONSTRAINT user_user_FOLLOWED_ITEMS_FKC FOREIGN KEY (FOLLOWED_ITEMS_FK)
    REFERENCES core_global_id (ID);
ALTER TABLE
    core_user2follows ADD CONSTRAINT core_global_id_kenmei_users_fC FOREIGN KEY (kenmei_users_fk)
    REFERENCES user_user (ID);
ALTER TABLE
    core_users2favorite_notes ADD CONSTRAINT user_user_FAVORITE_NOTES_FKC FOREIGN KEY
    (FAVORITE_NOTES_FK) REFERENCES core_note (ID);
ALTER TABLE
    core_users2favorite_notes ADD CONSTRAINT core_note_FAVORITE_USERS_FKC FOREIGN KEY
    (FAVORITE_USERS_FK) REFERENCES user_user (ID);
ALTER TABLE
    crc_cache_config ADD CONSTRAINT crc_cache_config_current_cachc FOREIGN KEY
    (CURRENT_CACHE_CONNECTOR_CON_FK) REFERENCES crc_connector_config (ID);
ALTER TABLE
    custom_messages ADD CONSTRAINT custom_messages_language_fkc FOREIGN KEY (LANGUAGE_FK)
    REFERENCES md_language (ID);
ALTER TABLE
    iprange_filter_channel ADD CONSTRAINT iprange_channel_IP_RANGE_FILTC FOREIGN KEY
    (IP_RANGE_FILTERS_FK) REFERENCES iprange_filter (ID);
ALTER TABLE
    iprange_filter_channel ADD CONSTRAINT iprange_filter_CHANNELS_FKC FOREIGN KEY (CHANNELS_FK)
    REFERENCES iprange_channel (TYPE);
ALTER TABLE
    iprange_range ADD CONSTRAINT ip_range_filter_C_ex FOREIGN KEY (IP_RANGE_FILTER_EX_FK)
    REFERENCES iprange_filter (ID);
ALTER TABLE
    iprange_range ADD CONSTRAINT ip_range_filter_C_in FOREIGN KEY (IP_RANGE_FILTER_IN_FK)
    REFERENCES iprange_filter (ID);
ALTER TABLE
    mc_config ADD CONSTRAINT mc_config_NOTIFICATION_CONFIGC FOREIGN KEY (NOTIFICATION_CONFIG_FK)
    REFERENCES notification_config (ID);
ALTER TABLE
    security_client_approval_code ADD CONSTRAINT security_client_approval_codeIFKC FOREIGN KEY (ID)
    REFERENCES security_code (ID);
ALTER TABLE
    security_client_approval_code ADD CONSTRAINT security_client_approval_codeC FOREIGN KEY
    (CLIENT_FK) REFERENCES user_client (ID);
ALTER TABLE
    security_client_reg_code ADD CONSTRAINT security_client_reg_codeIFKC FOREIGN KEY (ID)
    REFERENCES security_code (ID);
ALTER TABLE
    security_client_reg_code ADD CONSTRAINT security_client_reg_code_CLIEC FOREIGN KEY (CLIENT_FK)
    REFERENCES user_client (ID);
ALTER TABLE
    security_client_removal_code ADD CONSTRAINT security_client_removal_codeIFKC FOREIGN KEY (ID)
    REFERENCES security_code (ID);
ALTER TABLE
    security_client_removal_code ADD CONSTRAINT security_client_removal_code_C FOREIGN KEY
    (CLIENT_FK) REFERENCES user_client (ID);
ALTER TABLE
    security_code ADD CONSTRAINT security_code_KENMEI_USER_FKC FOREIGN KEY (KENMEI_USER_FK)
    REFERENCES user_user (ID);
ALTER TABLE
    security_email_code ADD CONSTRAINT security_email_codeIFKC FOREIGN KEY (ID) REFERENCES
    security_code (ID);
ALTER TABLE
    security_forgotten_pw_code ADD CONSTRAINT security_forgotten_pw_codeIFKC FOREIGN KEY (ID)
    REFERENCES security_code (ID);
ALTER TABLE
    security_invite_blog ADD CONSTRAINT security_invite_blogIFKC FOREIGN KEY (ID) REFERENCES
    security_code (ID);
ALTER TABLE
    security_invite_client ADD CONSTRAINT security_invite_clientIFKC FOREIGN KEY (ID) REFERENCES
    security_code (ID);
ALTER TABLE
    security_user_auth_failed_status ADD CONSTRAINT security_user_auth_failed_staC FOREIGN KEY
    (KENMEI_USER_FK) REFERENCES user_user (ID);
ALTER TABLE
    security_user_code ADD CONSTRAINT security_user_codeIFKC FOREIGN KEY (ID) REFERENCES
    security_code (ID);
ALTER TABLE
    security_user_unlock_code ADD CONSTRAINT security_user_unlock_codeIFKC FOREIGN KEY (ID)
    REFERENCES security_code (ID);
ALTER TABLE
    user_authorities ADD CONSTRAINT user_authorities_KENMEI_USER_C FOREIGN KEY (KENMEI_USER_FK)
    REFERENCES user_user (ID);
ALTER TABLE
    user_client ADD CONSTRAINT user_client_CLIENT_D_B_CONFIGC FOREIGN KEY (CLIENT_D_B_CONFIG_FK)
    REFERENCES user_client_db_config (ID);
ALTER TABLE
    user_client ADD CONSTRAINT user_client_CLIENT_PROFILE_FKC FOREIGN KEY (CLIENT_PROFILE_FK)
    REFERENCES user_client_profile (ID);
ALTER TABLE
    user_client ADD CONSTRAINT user_client_CREATION_DATA_FKC FOREIGN KEY (CREATION_DATA_FK)
    REFERENCES user_client_creation (ID);
ALTER TABLE
    user_client_creation ADD CONSTRAINT user_client_creation_CLIENT_AC FOREIGN KEY
    (CLIENT_ACTIVATION_CODE_FK) REFERENCES user_client_activation_code (ID);
ALTER TABLE
    user_contact ADD CONSTRAINT user_contact_COUNTRY_FKC FOREIGN KEY (COUNTRY_FK) REFERENCES
    md_country (ID);
ALTER TABLE
    user_entity ADD CONSTRAINT user_entity_GLOBAL_ID_FKC FOREIGN KEY (GLOBAL_ID_FK) REFERENCES
    core_global_id (ID);
ALTER TABLE
    user_external_auth ADD CONSTRAINT user_external_auth_KENMEI_USEC FOREIGN KEY (KENMEI_USER_FK)
    REFERENCES user_user (ID);
ALTER TABLE
    user_group ADD CONSTRAINT user_groupIFKC FOREIGN KEY (ID) REFERENCES 
	user_entity (ID);
ALTER TABLE
    user_group_external ADD CONSTRAINT user_group_externalIFKC FOREIGN KEY (ID) REFERENCES
    user_group (ID);
ALTER TABLE
    user_group_member ADD CONSTRAINT user_entity_GROUPS_FKC FOREIGN KEY (GROUPS_FK) REFERENCES
    user_group (ID);
ALTER TABLE
    user_group_member ADD CONSTRAINT user_group_GROUP_MEMBERS_FKC FOREIGN KEY (GROUP_MEMBERS_FK)
    REFERENCES user_entity (ID);
ALTER TABLE
    user_group_property ADD CONSTRAINT user_group_property_KENMEI_ENC FOREIGN KEY
    (KENMEI_ENTITY_GROUP_FK) REFERENCES user_group (ID);
ALTER TABLE
    user_note_property ADD CONSTRAINT user_note_property_NOTE_FKC FOREIGN KEY (NOTE_FK) REFERENCES
    core_note (ID);
ALTER TABLE
    user_note_property ADD CONSTRAINT user_note_property_USER_FKC FOREIGN KEY (USER_FK) REFERENCES
    user_user (ID);
ALTER TABLE
    user_of_group ADD CONSTRAINT user_of_group_GROUP_FKC FOREIGN KEY (GROUP_FK) REFERENCES
    user_group (ID);
ALTER TABLE
    user_of_group ADD CONSTRAINT user_of_group_USER_FKC FOREIGN KEY (USER_FK) REFERENCES 
	user_user(ID);
ALTER TABLE
    user_profile ADD CONSTRAINT user_profile_CONTACT_FKC FOREIGN KEY (CONTACT_FK) REFERENCES
    user_contact (ID);
ALTER TABLE
    user_profile ADD CONSTRAINT user_profile_LARGE_IMAGE_FKC FOREIGN KEY (LARGE_IMAGE_FK)
    REFERENCES user_image (ID);
ALTER TABLE
    user_profile ADD CONSTRAINT user_profile_MEDIUM_IMAGE_FKC FOREIGN KEY (MEDIUM_IMAGE_FK)
    REFERENCES user_image (ID);
ALTER TABLE
    user_profile ADD CONSTRAINT user_profile_NOTIFICATION_CONC FOREIGN KEY (NOTIFICATION_CONFIG_FK)
    REFERENCES notification_config (ID);
ALTER TABLE
    user_profile ADD CONSTRAINT user_profile_SMALL_IMAGE_FKC FOREIGN KEY (SMALL_IMAGE_FK)
    REFERENCES user_image (ID);
ALTER TABLE
    user_user ADD CONSTRAINT user_userIFKC FOREIGN KEY (ID) 
	REFERENCES user_entity (ID);
ALTER TABLE
    user_user ADD CONSTRAINT user_user_PROFILE_FKC FOREIGN KEY (PROFILE_FK) 
	REFERENCES user_profile (ID);
ALTER TABLE
    user_user_property ADD CONSTRAINT user_user_property_KENMEI_USEC FOREIGN KEY (KENMEI_USER_FK)
    REFERENCES user_user (ID);

ALTER TABLE
    configuration_ldap ADD CONSTRAINT configuration_ldap_GROUP_SYNCC FOREIGN KEY
    (GROUP_SYNC_CONFIG_FK) REFERENCES configuration_ldap_group (ID);


CREATE INDEX core_blog_name_identifier_index
ON core_blog (name_identifier);
	
CREATE INDEX core_blog_title_index
ON core_blog(title);
	
CREATE INDEX core_note_discussion_id
ON core_note(discussion_id);

CREATE INDEX core_role2blog_uidx
ON core_role2blog(user_id);

CREATE INDEX core_role2blog_bidx
ON core_role2blog (blog_id);

CREATE INDEX core_tag_lower_name_idx 
ON core_tag(lower_name);

CREATE INDEX user_profile_last_name_index
ON user_profile(last_name);

CREATE INDEX user_profile_first_name_index
ON user_profile(first_name);	
	
CREATE INDEX user_user_email_index
ON user_user(email);

CREATE  INDEX core_note_creation_date_index
ON core_note (creation_date DESC) ;
	
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('reset_checksum_001','unknown','de/communardo/kenmei/database/update/db.changelog.default.xml','2011-02-04 10:11:42','6c50cd6ee695161d8f569de01f98956','Custom SQL','Reset Checksums',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('mt2859_2','unknown','de/communardo/kenmei/database/update/v1.0.3/db.changelog.v1.0.3.xml','2011-02-04 10:11:42','b2e0e51b2fdd768127d43d458e21d939','Add Unique Constraint','',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('mt2846_global_id_v2_mysql','unknown','de/communardo/kenmei/database/update/v1.1/db.changelog.v1.1.refactor.xml','2011-02-04 10:11:42','c0cbabe0cea6e8c89d81b7169f92e414','Insert Row','MT 2846: CR 119 - Refactoring Post Backend: Global Id',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('mt2846_global_id_checksum_v2','unknown','de/communardo/kenmei/database/update/v1.1/db.changelog.v1.1.refactor.xml','2011-02-04 10:11:42','304f2354db312562949c37255a17bf','Custom SQL','MT 2846: CR 119 - Refactoring Post Backend: Global Id, Configuration, Checksum',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('mt2846_user_external_auth','unknown','de/communardo/kenmei/database/update/v1.1/db.changelog.v1.1.refactor.xml','2011-02-04 10:11:42','6c8c9284a09b847b41d87b11499cf4b','Rename Table, Rename Column, Drop Foreign Key Constraint, Add Foreign Key Constraint','MT 2846: CR 119 - Refactoring Post Backend - Rename external user auth',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('mt2846_remove_module_status','unknown','de/communardo/kenmei/database/update/v1.1/db.changelog.v1.1.refactor.xml','2011-02-04 10:11:42','c616cb81c99fea1e6427599e6cec5187','Drop Column (x4)','MT 2846: CR 119 - Refactoring Post Backend - Remove module status',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('mt2846_mysql_1','unknown','de/communardo/kenmei/database/update/v1.1/mysql/db.changelog.v1.1.refactor.mysql.xml','2011-02-04 10:11:42','30d723e996f2fe7b25fddc235b2dcfcb','Drop Table (x6)','MT 2846: CR 119 - Refactoring Post Backend - MySql',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('mt2846_mysql_2','unknown','de/communardo/kenmei/database/update/v1.1/mysql/db.changelog.v1.1.refactor.mysql.xml','2011-02-04 10:11:42','a66cd5a4ddf1ad5b2ee81c0574673bd','Drop Foreign Key Constraint (x9)','MT 2846: CR 119 - Refactoring Post Backend - MySql',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('mt2846_mysql_3','unknown','de/communardo/kenmei/database/update/v1.1/mysql/db.changelog.v1.1.refactor.mysql.xml','2011-02-04 10:11:42','86c7a598426a561fc898d3bd5930a3e6','Create Table','MT 2846: CR 119 - Refactoring Post Backend - MySql',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('mt2846_mysql_4','unknown','de/communardo/kenmei/database/update/v1.1/mysql/db.changelog.v1.1.refactor.mysql.xml','2011-02-04 10:11:42','59f5a1279fabc9ed2a3c7f97edad36b8','Create Table','MT 2846: CR 119 - Refactoring Post Backend - MySql',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('mt2846_mysql_4a','unknown','de/communardo/kenmei/database/update/v1.1/mysql/db.changelog.v1.1.refactor.mysql.xml','2011-02-04 10:11:42','774a36dba4aff22ffbdf3bcf16ed6f','SQL From File','MT 2846: CR 119 - Refactoring Post Backend - MySql',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('mt2846_mysql_5','unknown','de/communardo/kenmei/database/update/v1.1/mysql/db.changelog.v1.1.refactor.mysql.xml','2011-02-04 10:11:42','937b3863b7ea9f194231bd2dafff6832','Drop Column (x7)','MT 2846: CR 119 - Refactoring Post Backend - MySql',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('mt2846_mysql_6','unknown','de/communardo/kenmei/database/update/v1.1/mysql/db.changelog.v1.1.refactor.mysql.xml','2011-02-04 10:11:42','a669428a7b42a371372f4e08556acb','SQL From File','MT 2846: CR 119 - Refactoring Post Backend - MySql',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('mt2846_mysql_7','unknown','de/communardo/kenmei/database/update/v1.1/mysql/db.changelog.v1.1.refactor.mysql.xml','2011-02-04 10:11:42','f431f661f875edb7a4c7122b691768','Drop Column, Rename Table, Add Not-Null Constraint','MT 2846: CR 119 - Refactoring Post Backend - MySql',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('mt2846_mysql_8','unknown','de/communardo/kenmei/database/update/v1.1/mysql/db.changelog.v1.1.refactor.mysql.xml','2011-02-04 10:11:42','44a6dfe46a8feb68eaece96d1ffd7ddf','SQL From File','MT 2846: CR 119 - Refactoring Post Backend - MySql',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('mt2846_mysql_9','unknown','de/communardo/kenmei/database/update/v1.1/mysql/db.changelog.v1.1.refactor.mysql.xml','2011-02-04 10:11:42','a8626d4dafc03b63fc2a64ba9cacfc9c','Rename Table','MT 2846: CR 119 - Refactoring Post Backend - MySql',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('mt2846_mysql_10','unknown','de/communardo/kenmei/database/update/v1.1/mysql/db.changelog.v1.1.refactor.mysql.xml','2011-02-04 10:11:42','cf28a7b028c31c85bc9ddeec98c856fc','Add Column (x7), Rename Column','MT 2846: CR 119 - Refactoring Post Backend - MySql',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('mt2846_mysql_11','unknown','de/communardo/kenmei/database/update/v1.1/mysql/db.changelog.v1.1.refactor.mysql.xml','2011-02-04 10:11:42','e3397a39ece50c8ba1ae089f831ac4b','SQL From File','MT 2846: CR 119 - Refactoring Post Backend - MySql',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('mt2846_mysql_12','unknown','de/communardo/kenmei/database/update/v1.1/mysql/db.changelog.v1.1.refactor.mysql.xml','2011-02-04 10:11:42','8f569acbd4f4184a409f268fc9cb46be','Add Not-Null Constraint (x3), Drop Table (x2)','MT 2846: CR 119 - Refactoring Post Backend - MySql',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('mt2846_mysql_13','unknown','de/communardo/kenmei/database/update/v1.1/mysql/db.changelog.v1.1.refactor.mysql.xml','2011-02-04 10:11:42','172934a379fa3cf0a5cf9ee1116b1','Rename Table','MT 2846: CR 119 - Refactoring Post Backend - MySql',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('mt2846_mysql_14','unknown','de/communardo/kenmei/database/update/v1.1/mysql/db.changelog.v1.1.refactor.mysql.xml','2011-02-04 10:11:42','53b64a5a1bb0c3235c619ce40ac1e5','Rename Column, Add Column (x3), Drop Table, Drop Column','MT 2846: CR 119 - Refactoring Post Backend - MySql',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('mt2846_mysql_15','unknown','de/communardo/kenmei/database/update/v1.1/mysql/db.changelog.v1.1.refactor.mysql.xml','2011-02-04 10:11:42','a15f2a38d192f1ea28297d763382923','Custom SQL','MT 2846: CR 119 - Refactoring Post Backend - MySql',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('mt2846_mysql_16','unknown','de/communardo/kenmei/database/update/v1.1/mysql/db.changelog.v1.1.refactor.mysql.xml','2011-02-04 10:11:42','f4f35c79c8f9cbdfa6fe278a2d1b0e','Rename Table','MT 2846: CR 119 - Refactoring Post Backend - MySql',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('mt2846_mysql_17','unknown','de/communardo/kenmei/database/update/v1.1/mysql/db.changelog.v1.1.refactor.mysql.xml','2011-02-04 10:11:42','2c781dee109292b6fd35f10b820c9e0','Rename Column (x5), Drop Column (x4)','MT 2846: CR 119 - Refactoring Post Backend - MySql',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('mt2846_mysql_18','unknown','de/communardo/kenmei/database/update/v1.1/mysql/db.changelog.v1.1.refactor.mysql.xml','2011-02-04 10:11:42','d41d8cd98f0b24e980998ecf8427e','Empty','MT 2846: CR 119 - Refactoring Post Backend - MySql',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('mt2846_mysql_19','unknown','de/communardo/kenmei/database/update/v1.1/mysql/db.changelog.v1.1.refactor.mysql.xml','2011-02-04 10:11:42','9d3c6d7f42f3efccf7973fc8135bde5','Add Foreign Key Constraint (x3), Add Unique Constraint (x3), Add Foreign Key Constraint (x11), Add Unique Constraint, Drop Foreign Key Constraint, Add Foreign Key Constraint (x3), Add Unique Constraint, Add Foreign Key Constraint','MT 2846: CR 119 - Refactoring Post Backend - MySql',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('mt2846_mysql_20','unknown','de/communardo/kenmei/database/update/v1.1/mysql/db.changelog.v1.1.refactor.mysql.xml','2011-02-04 10:11:42','c59f2b28cf96874373cfeacffc881b4','SQL From File','MT 2846: CR 119 - Refactoring Post Backend - MySql',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('mt2846_favorite_users','unknown','de/communardo/kenmei/database/update/v1.1/db.changelog.v1.1.refactor.xml','2011-02-04 10:11:42','9f4a84f23eb8c55a4730335d35c3ab7f','Rename Table','MT 2846: CR 119 - Refactoring Post Backend - Favorite users',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('20091007_mysql','unknown','de/communardo/kenmei/database/update/v1.0.3/db.changelog.v1.0.3.xml','2011-02-04 10:11:42','89652831b7f99da19387e93f72471b','Custom SQL','',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('mt2899_mysql_v3','unknown','de/communardo/kenmei/database/update/v1.0.3/db.changelog.v1.0.3.xml','2011-02-04 10:11:42','2386128fd9dcb86b8fa469f8ff2ec81','Custom SQL','MT 2899: Image caching problems',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('mt2940_1','unknown','de/communardo/kenmei/database/update/v1.1/db.changelog.v1.1.xml','2011-02-04 10:11:42','b962fa5b9ddcecbc21d62b4999d8764','Update Data, Create Table, Add Foreign Key Constraint (x2)','CR 131: autosave refactoring',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('mt2945_1','unknown','de/communardo/kenmei/database/update/v1.1/db.changelog.v1.1.xml','2011-02-04 10:11:42','22b680833818a22cf3f5e695a7b185d','Add Column, Add Foreign Key Constraint','CR 109: Post without selecting a blog',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('mt2957_1','unknown','de/communardo/kenmei/database/update/v1.1/db.changelog.v1.1.xml','2011-02-04 10:11:42','2a75e566753c4ffb3f42c5fb9896cd','Add Column (x2)','CR 122 - Threaded answers',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('mt2957_2_mysql','unknown','de/communardo/kenmei/database/update/v1.1/db.changelog.v1.1.xml','2011-02-04 10:11:42','39dd942ccd10338659ba1bfdb94f9183','Custom SQL','CR 122 - Threaded answers',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('mt2976_1','unknown','de/communardo/kenmei/database/update/v1.1/db.changelog.v1.1.xml','2011-02-04 10:11:42','dcd4c199fd6bf5cd3ca66a34a84afa7','Add Column (x3)','Content Type',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('mt2976_2','unknown','de/communardo/kenmei/database/update/v1.1/db.changelog.v1.1.xml','2011-02-04 10:11:42','6b7314e763c81f1f96c67deb8d806d69','Custom SQL','Attachment Status',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('rename_default_blog_alias_1_mysql','unknown','de/communardo/kenmei/database/update/v1.1/db.changelog.v1.1.xml','2011-02-04 10:11:42','aab6b129c4acfe2705da4d8d127dd7b','Custom SQL','Renames the blog alias of the default blog, if an alias public not exists.',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('mt3022_mysql','unknown','de/communardo/kenmei/database/update/v1.1/db.changelog.v1.1.xml','2011-02-04 10:11:42','a4c8beb7d79b2eb7c18c2d883f2eb3c','Custom SQL','Increase Repository Limit',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('rename_default_blog_alias_2_mysql','unknown','de/communardo/kenmei/database/update/v1.1/db.changelog.v1.1.xml','2011-02-04 10:11:42','b38717f58c847a3c1896dd7425ef50','Custom SQL','Renames the blog alias of the default blog, if it is still the message key.',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('mt3096_1','unknown','de/communardo/kenmei/database/update/v1.1/db.changelog.v1.1.xml','2011-02-04 10:11:42','abcc6ad563565e4050381273ef3fcc5d','Custom SQL','deletes unconnected large user images',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('mt3178_1_mysql','unknown','de/communardo/kenmei/database/update/v1.1/db.changelog.v1.1.xml','2011-02-04 10:11:42','dd97e4a211a78df2d24fbda9552922b','Insert Row','Automated Client Approval',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('mt3187_1','unknown','de/communardo/kenmei/database/update/v1.1/db.changelog.v1.1.xml','2011-02-04 10:11:42','77fa5d42e90366abddbbb7dbb94e61d','Drop Foreign Key Constraint','Adjust the forein key constraint for kenmei_users_fk.',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('mt3187_2','unknown','de/communardo/kenmei/database/update/v1.1/db.changelog.v1.1.xml','2011-02-04 10:11:42','1b315757661762a2659fde38c03259','Add Foreign Key Constraint','Adjust the forein key constraint for kenmei_users_fk.',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('mt3187_3','unknown','de/communardo/kenmei/database/update/v1.1/db.changelog.v1.1.xml','2011-02-04 10:11:42','77fa5d42e90366abddbbb7dbb94e61d','Drop Foreign Key Constraint','Adjust the forein key constraint for kenmei_users_fk.',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('mt3187_4_mysql','unknown','de/communardo/kenmei/database/update/v1.1/db.changelog.v1.1.xml','2011-02-04 10:11:42','925ad9965ef8812edba0b6ba81d9629','Custom SQL, Add Foreign Key Constraint','Adjust the forein key constraint for kenmei_users_fk.',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('mt3196_1','unknown','de/communardo/kenmei/database/update/v1.1.2/db.changelog.v1.1.2.xml','2011-02-04 10:11:42','3f4f52f46b355cec745a24706cca46d4','Add Column','CR 100 - Support Time Zones: add new column to user_profile',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('mt3196_2','unknown','de/communardo/kenmei/database/update/v1.1.2/db.changelog.v1.1.2.xml','2011-02-04 10:11:42','dca47279adc3bbd1ad7e1d7174e10b1','Add Column (x2)','CR 100 - Support Time Zones: add new column to user_client_creation, configuration_client',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('mt3208','unknown','de/communardo/kenmei/database/update/v1.1.2/db.changelog.v1.1.2.xml','2011-02-04 10:11:42','a5e3666944ac3f968080ff852bd777d3','Add Column','CR 68 Read-More functionality',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('mt3277_1_mysql','unknown','de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml','2011-02-04 10:11:42','2ea8e41788851b5f4a58846e05bcc82','Create Table, Custom SQL, Drop Column, Add Foreign Key Constraint (x2)','CR 96 - support for groups: add user_entity and copy users',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('mt3277_2_mysql','unknown','de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml','2011-02-04 10:11:42','88fa38c4a5f8ae66e1cbae3414dfccf','Drop Table (x2)','CR 96 - support for groups: remove obsolete security codes',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('mt3277_3_mysql','unknown','de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml','2011-02-04 10:11:42','f595d3d3bb28aa16d7567246a3a4585','Add Column, Custom SQL','CR 96 - support for groups: copy all_can_x rights from group to blog',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('mt3277_4_mysql','unknown','de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml','2011-02-04 10:11:42','25c695869c8f851fd412ca90db5b2677','Add Column, Custom SQL','CR 96 - support for groups: add blogs to group member',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('mt3277_5_mysql','unknown','de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml','2011-02-04 10:11:42','ce8baa4707c58ddfba1523c015deb0','Drop Foreign Key Constraint (x2), Drop Column (x2), Custom SQL, Rename Table, Rename Column (x2), Add Column, Add Foreign Key Constraint (x2), Add Not-Null Constraint (x3)','CR 96 - support for groups: group member to blog member',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('mt3277_6_mysql','unknown','de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml','2011-02-04 10:11:42','da4346ff7575e6ebf6b3cd1266164','Drop Foreign Key Constraint, Drop Column','CR 96 - support for groups: cleanup core_blog',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('mt3277_7_mysql','unknown','de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml','2011-02-04 10:11:42','3e47ee3a105c684a582e423518decca','Drop Column (x3), Delete Data, Add Column (x3), Add Foreign Key Constraint','CR 96 - support for groups: fix user_group',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('mt3277_8_mysql','unknown','de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml','2011-02-04 10:11:42','9a394f311e01bce782f7d562045c46b','Create Table','CR 96 - support for groups: add helper table for fast blog role lookup',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('mt3277_9_mysql','unknown','de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml','2011-02-04 10:11:42','ae6438d32dde3f4c6124ca8330b5ec6','Custom SQL','CR 96 - support for groups: fill core_role2blog',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('mt3277_10_mysql','unknown','de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml','2011-02-04 10:11:42','fa346348fef8d167f2390c9b247f053','Create Table, Add Primary Key, Add Foreign Key Constraint (x2)','CR 96 - support for groups: allow several granting groups',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('mt3277_11_mysql','unknown','de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml','2011-02-04 10:11:42','c83fd6a7d42279a72d55ededfcfa4d86','Create Table, Add Primary Key, Add Foreign Key Constraint (x2)','CR 96 - support for groups: connect entity with group',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('mt3277_13_mysql','unknown','de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml','2011-02-04 10:11:42','4691cf11e2e377d7897b8abdecc6d88d','Add Not-Null Constraint (x2)','CR 96 - support for groups: not null constraints for all_can_x rights',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('mt3281_1_mysql','unknown','de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml','2011-02-04 10:11:42','3ebca4821f7296e11d97c5913fe8707c','Insert Row','CR 134 - Anonymous Access, Anonymous User',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('mt3281_2_mysql','unknown','de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml','2011-02-04 10:11:42','f4972baac8a2f4504d77fd85c7d15e9','Add Column, Add Not-Null Constraint','CR 134 - Anonymous Access, Anonymous User',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('cr135','amo','de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml','2011-02-04 10:11:42','a02a9368d39aadb2b9affeff80229c','Create Table, Add Foreign Key Constraint','CR 135 - User group synchronization',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('mt3283_external_configuration','unknown','de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml','2011-02-04 10:11:42','c2c4eb85364225670b85647601134e0','Rename Table (x3), Rename Column, Add Column, Drop Foreign Key Constraint, Add Foreign Key Constraint','Refactor External Authentication',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('mt3283_confluence_configuration','unknown','de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml','2011-02-04 10:11:42','5029a38aca9b7c4c25a4f41f619caa1c','Add Column (x2)','',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('mt3283_external_objects','unknown','de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml','2011-02-04 10:11:42','75db72d38b92ade15a93f1b854b9048','Create Table, Add Unique Constraint, Create Table, Add Foreign Key Constraint (x2), Add Column','',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('mt3283_2_mysql','unknown','de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml','2011-02-04 10:11:42','22711a3246654a434873c0e1fbf78119','Set Column as Auto-Increment, Add Not-Null Constraint','Refactor External Authentication',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('mt3283_external_objects_2','unknown','de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml','2011-02-04 10:11:42','ff7ed34175992ead7a175351c0d4e9','Add Column, Add Not-Null Constraint','Add class to blog_member',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('mt3292_confluence_permission_url','unknown','de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml','2011-02-04 10:11:42','21a7986f567bd1c385abbdfead1e8c3','Add Column','CR 135 - Support Synchronization of blog rights with external systems',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('cr135_1','unknown','de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml','2011-02-04 10:11:42','318b839a3fff783a8f4a4a3e7f5866','Add Column','Add serviceUrl to confluence configuration',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('mt3283_external_objects_3','unknown','de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml','2011-02-04 10:11:42','d3cc6233f8abccd7d6c2eb5017cf77','Add Column','',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('mt3277_fix_null_constraint','unknown','de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml','2011-02-04 10:11:42','97f2d0c8945bae1c69f8f827c57e73','Drop Not-Null Constraint','Drop wrong \"not null\" constraint from user entities.',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('mt3178_2_mysql','unknown','de/communardo/kenmei/database/update/v1.1/db.changelog.v1.1.xml','2011-02-04 10:11:42','d1808dcb3e616cfff590fbbdf8494f','Insert Row','Automated Client Approval',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('set_configuration','unknown','de/communardo/kenmei/database/update/db.changelog.final.xml','2011-06-15 14:33:28','6148c09f2e75f3239da193221c5cedca','Custom SQL','Assign the configuration FK',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('mt3283_external_objects_fix_key_unique_mysql','unknown','de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml','2011-02-04 10:11:42','c2294436992eb88562f385fe26135b0','Drop Primary Key, Add Column','CR 135 - Support Synchronization of blog rights with external systems',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('mt3277_14','unknown','de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml','2011-02-04 10:11:42','e694eb5228924a36a25357646af454e4','Add Column, Custom SQL, Add Not-Null Constraint','CR 96 - support for groups: grantedByGroup flag for mapping table',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('mt3350_configuration_column_ldap','unknown','de/communardo/kenmei/database/update/v1.1.2/db.changelog.v1.1.2.xml','2011-02-04 10:11:42','dd10f32f1dfab6778b0c3eb60acd4ba','Drop Column','MT 3350',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('phone_and_fax_country_code_fix_mysql','unknown','de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml','2011-02-04 10:11:42','89e69368ec3f98f683218ccbb47340','Custom SQL','',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('mt_3272_1_mysql','unknown','de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml','2011-02-04 10:11:42','f421adb0107c9087be3d4be78a9086f3','Custom SQL','MT 3272: UserProfile saves space instead of NULL if no time zone is selected',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('phone_country_code_fix__client_mysql','unknown','de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml','2011-02-04 10:11:42','84e1be9cc8d1aa2f974526d73f3cf9e7','Custom SQL','',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('cr69_user_groups_v2','unknown','de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml','2011-02-04 10:11:42','41e53effcad9d4fe485b75dab0922bf1','Rename Column','',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('mt_3314_refactor_confluence_page','unknown','de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml','2011-02-04 10:11:42','86b2b96eee76c0d6bdaad4e1c24b5b25','Add Column','',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('mt3283_external_objects_fix_auto_increment_mysql','unknown','de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml','2011-02-04 10:11:42','c7f24c22f84e8eff695ba917c8fd6d7','Custom SQL','CR 135 - Support Synchronization of blog rights with external systems',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('mt3329_1_mysql','unknown','de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml','2011-02-04 10:11:42','836a30af94fb53cfe359ea11fe4f62fe','Create Table, Custom SQL, Add Foreign Key Constraint (x2)','CR 96 - support for hierarchical groups: add user_of_group and copy users',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('mt3350_configuration_column_source','unknown','de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml','2011-02-04 10:11:42','12af7efa26d7a9eefaedfcd1ad9e2d24','Drop Column','MT 3350',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('confluence_set_basepath_mysql','unknown','de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml','2011-02-04 10:11:42','8dab40b53251883889955704653be51','Custom SQL','',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('remove_uk_country_code','unknown','de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml','2011-02-04 10:11:42','a8a46d136dea83ec4bdfffd11c9b065','Custom SQL','',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('cr_179_add_is_Html_column_v2','unknown','de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml','2011-02-04 10:11:42','51c815f15bbb47cb1990c3f556b2fe','Add Column, Add Not-Null Constraint','',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('cr_179_add_messages_table_v2','unknown','de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml','2011-02-04 10:11:42','51bf6b30763f19d84b1ba2aeac1fd14','Create Table, Add Foreign Key Constraint, Add Unique Constraint','',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('cr_179_add_messages_table_fix_key_column','unknown','de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml','2011-02-04 10:11:42','db6273f493f2c16fa8c63f1a43ea57','Modify Column','',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('cr_179_insert_default_values_for_imprint_terms_of_use_mysql','unknown','de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml','2011-02-04 10:11:42','c7e584ebae6171b861bd5a37f4be42','Custom SQL','',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('jt1528_convert_core_note_to_innoDB','unknown','de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml','2011-02-04 10:11:42','5c59d4c1140af9231be2cdf42c265d','Custom SQL','Change engine of core_note to innoDB',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('KENMEI-1566-Remove_MySQL_Timestamp_Trigger','unknown','de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml','2011-02-04 10:11:42','a4fb25b5e12f2ab2519c961d47707617','Custom SQL','',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('KENMEI-1533_Erzeugung_Global_ID_fehlerhaft-MySQL','unknown','de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml','2011-02-04 10:11:42','5a199146dfd883297b1bfdb6ef6f517','SQL From File','',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('initialize-database-schema_mysql_v1','unknown','de/communardo/kenmei/database/install/mysql/db.changelog.init-db.mysql.xml','2011-02-04 10:11:42','91754b8c92f026f912112d6e6fe849e0','SQL From File','Initialize the Database Schema',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('mt2846_global_id_v3_mysql','unknown','de/communardo/kenmei/database/update/v1.1/db.changelog.v1.1.refactor.xml','2011-06-15 14:33:28','f6819542a3985ddacbed3e84174992','Insert Row','MT 2846: CR 119 - Refactoring Post Backend: Global Id',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('MT_3395_drop_source_column_from_blog_members','unknown','de/communardo/kenmei/database/update/v1.1.4/db.changelog.v1.1.4.xml','2011-02-04 10:11:45','12af7efa26d7a9eefaedfcd1ad9e2d24','Drop Column','',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('CR_115_Add_Application_Settings_1','unknown','de/communardo/kenmei/database/update/v1.1.4/db.changelog.v1.1.4.xml','2011-02-04 10:11:48','63fdc9f116fec14a18b3c3caaa2f6c26','Create Table','',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('CR_115_Add_Application_Settings_Fix_Engine','unknown','de/communardo/kenmei/database/update/v1.1.4/db.changelog.v1.1.4.xml','2011-02-04 10:11:48','3da6fc71eb27642e1c85fb6cc7856b1','Custom SQL','',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('CR_115_Remove_Client_Proxy_Url','unknown','de/communardo/kenmei/database/update/v1.1.4/db.changelog.v1.1.4.xml','2011-02-04 10:11:48','6ff27dc938b73dd590d32872eef4b068','Drop Column','',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('KENMEI_1577_Remove_crc_filesystem_config','unknown','de/communardo/kenmei/database/update/v1.1.4/db.changelog.v1.1.4.xml','2011-02-04 10:11:48','f675d97e34cf9e8f6f42c08df83e4366','Drop Table','',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('KENMEI_1555_Rename_reserved_keywords','unknown','de/communardo/kenmei/database/update/v1.1.4/db.changelog.v1.1.4.xml','2011-02-04 10:11:49','d8bb446dfba4c335fa7ecc4f5381a919','Rename Column (x2)','',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('KENMEI-1857_Option_for_system_notes_per_blog','unknown','de/communardo/kenmei/database/update/v1.1.4/db.changelog.v1.1.4.xml','2011-02-04 10:11:50','3710cfcc8722190afbabf4cdfbd1c50','Add Column, Add Not-Null Constraint','KENMEI-1857 allow disabling of system notes per blog',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('KENMEI-1578_Felder_key_und_value_in_Modellen_aendern','unknown','de/communardo/kenmei/database/update/v1.2/db.changelog.v1.2.xml','2011-02-04 10:11:51','4ff8a66180d5067f9a8337daa846','Rename Column (x4)','',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('KENMEI-1694_Follow_model_mysql','unknown','de/communardo/kenmei/database/update/v1.2/db.changelog.v1.2.xml','2011-02-04 10:11:52','cf2d46294da178b3cec5db11a59cee','Custom SQL, Add Primary Key, Add Foreign Key Constraint (x2), Add Primary Key, Add Foreign Key Constraint (x2)','',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('KENMEI-1694_Follow-support_for_tags-MySQL_v3','unknown','de/communardo/kenmei/database/update/v1.2/db.changelog.v1.2.xml','2011-02-04 10:11:52','eb3cd3e484968abfae07914643ba681','Drop Unique Constraint','',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('KENMEI-1694_Follow-support_for_tags-MySQL_v4','unknown','de/communardo/kenmei/database/update/v1.2/db.changelog.v1.2.xml','2011-02-04 10:11:52','794bcbcb61d474af2dd2a1b946bde68','Drop Foreign Key Constraint','',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('KENMEI-1694_Follow-support_for_tags-MySQL_v5','unknown','de/communardo/kenmei/database/update/v1.2/db.changelog.v1.2.xml','2011-02-04 10:11:52','6731ae54a2b9defa6af2780d3cff338','Drop Unique Constraint','',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('KENMEI-1694_Follow-support_for_tags-MySQL_v6','unknown','de/communardo/kenmei/database/update/v1.2/db.changelog.v1.2.xml','2011-02-04 10:11:52','d697217de5b4c55543b0446c5514dccb','Drop Foreign Key Constraint','',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('KENMEI-1979_Attachments_for_outgoing_mails_mysql','unknown','de/communardo/kenmei/database/update/v1.2/db.changelog.v1.2.xml','2011-02-04 10:11:52','fcf55ea391ee0a078551c12c38c761','Custom SQL','',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('KENMEI-1901_Drop_news_tables','unknown','de/communardo/kenmei/database/update/v1.2/db.changelog.v1.2.xml','2011-02-04 10:11:52','e328a03e4b3621b07cd78ac5881b94dc','Drop Table (x3)','',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('KENMEI-2009_Tables_for_changed_LDAP_Config_mysql','unknown','de/communardo/kenmei/database/update/v1.2/db.changelog.v1.2.xml','2011-02-04 10:11:53','26c4a0b1e75df3286ed279cfc262998','Custom SQL, Add Foreign Key Constraint (x2), Add Column','',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('KENMEI-2009_Migrate_LDAP_Config-MySQL','unknown','de/communardo/kenmei/database/update/v1.2/db.changelog.v1.2.xml','2011-02-04 10:11:53','96744914835013bcccd272c663ba2b8','Custom SQL','',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('KENMEI-2009_Finalize_changed_LDAP_Config','unknown','de/communardo/kenmei/database/update/v1.2/db.changelog.v1.2.xml','2011-02-04 10:11:53','45746a6edbf040d1eec5c559be13d4','Drop Column (x4), Add Not-Null Constraint','',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('KENMEI-2009_Refactor_Group_Sync','unknown','de/communardo/kenmei/database/update/v1.2/db.changelog.v1.2.xml','2011-02-04 10:11:53','379ed5349921bcef239216c3d0b22920','Drop Column (x2)','',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('KENMEI-2009_Ldap_Config_User_UID','unknown','de/communardo/kenmei/database/update/v1.2/db.changelog.v1.2.xml','2011-02-04 10:11:53','382cc4905529ba9151032204e58dcee','Add Column, Add Not-Null Constraint','',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('KENMEI-2009_Ldap_Config_SearchBase_list','unknown','de/communardo/kenmei/database/update/v1.2/db.changelog.v1.2.xml','2011-02-04 10:11:54','3c6bcc15811c64f0494474d31c35104c','Add Column','',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('KENMEI-2009_Ldap_External_User_Auth_Additional_Props','unknown','de/communardo/kenmei/database/update/v1.2/db.changelog.v1.2.xml','2011-02-04 10:11:54','c92fd0704f4ac6551090f6d4ae43ada','Add Column, Custom SQL','',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('KENMEI-2008_Ldap_External_Group_Additional_Props','unknown','de/communardo/kenmei/database/update/v1.2/db.changelog.v1.2.xml','2011-02-04 10:11:54','40578a21037635e7cd0434a3beaeac4','Add Column','',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('KENMEI-2113_Performanzoptimierung_Direct_Message_mysql','unknown','de/communardo/kenmei/database/update/v1.2/db.changelog.v1.2.xml','2011-02-04 10:11:55','d521a632fab7d957ad853854ea48694','Custom SQL, Add Primary Key, Add Foreign Key Constraint (x2)','',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('KENMEI-2113_Performanzoptimierung_Direct_Message_migrate','unknown','de/communardo/kenmei/database/update/v1.2/db.changelog.v1.2.xml','2011-02-04 10:11:55','b35b34b640585fac11e4a5df10dfa','Custom SQL','',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('Fix_Column_Size_In_ROLE2BLOG','unknown','de/communardo/kenmei/database/update/v1.2/db.changelog.v1.2.xml','2011-02-04 10:11:55','2a9f758ec4728aa16fd4bc3c8c22d19','Modify Column','Fix of bug in installer script',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('KENMEI-2302_Index_on_core_role2blog','unknown','de/communardo/kenmei/database/update/v1.2/db.changelog.v1.2.xml','2011-02-04 10:11:55','83576031a02b3bc36cbc918a0c59d79','Create Index (x2)','',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('KENMEI-2335_UniqueJabberResourceIdPerCluster','unknown','de/communardo/kenmei/database/update/v1.2/db.changelog.v1.2.xml','2011-02-04 10:11:55','c298eab7d736523bb2c86346674dabc','Custom SQL','',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('KENMEI-2389_StartTLS_1','unknown','de/communardo/kenmei/database/update/v1.2/db.changelog.v1.2.xml','2011-02-04 10:11:55','c8fe21bfa1ed5b39dc80e84e83a8aa67','Insert Row','',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('KENMEI-2389_StartTLS_2','unknown','de/communardo/kenmei/database/update/v1.2/db.changelog.v1.2.xml','2011-02-04 10:11:55','d5f814f6a5ec5bf9eb501bdbc41c6d41','Custom SQL','',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('KENMEI-2287_StartTLS_1','unknown','de/communardo/kenmei/database/update/v1.2/db.changelog.v1.2.xml','2011-02-04 10:11:55','c629b7e26c3494b0c7ebf583ecce7fdd','Insert Row','',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('KENMEI-2295_Create_personal_blog_mysql','unknown','de/communardo/kenmei/database/update/v1.2/db.changelog.v1.2.xml','2011-02-04 10:11:56','d60537d23ca7d5d226eb2eda3a2e18','Custom SQL','',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('KENMEI-Drop_snc_tables','unknown','de/communardo/kenmei/database/update/v1.3/db.changelog.v1.3.xml','2011-02-04 10:11:56','1996781dbc24996e36864b7ece8f61d','Drop Table (x2)','',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('KENMEI-2154_Jobs_Services_als_Tasks_in_Datenbank_mysql','unknown','de/communardo/kenmei/database/update/v1.3/db.changelog.v1.3.xml','2011-02-04 10:11:56','d1db1d661338d345aab613ed1f94ec9a','Custom SQL (x3), Add Foreign Key Constraint (x2)','',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('KENMEI-User-Grop-Properties_mysql','unknown','de/communardo/kenmei/database/update/v1.3/db.changelog.v1.3.xml','2011-02-04 10:11:57','669a95d06bcb29abe32926762f76d3','Custom SQL, Add Foreign Key Constraint, Custom SQL, Add Foreign Key Constraint','',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('KENMEI-User-Group-Properties_clob','unknown','de/communardo/kenmei/database/update/v1.3/db.changelog.v1.3.xml','2011-02-04 10:11:57','b51dcb936e4e658ea6d26c6fd7f0903b','Modify Column (x2)','',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('KENMEI-Binary-Properties','unknown','de/communardo/kenmei/database/update/v1.3/db.changelog.v1.3.xml','2011-02-04 10:11:57','901bbfc8af68f655b2185ac26d7b126','Create Table','',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('Add_binary_property_autoincrement','unknown','de/communardo/kenmei/database/update/v1.3/db.changelog.v1.3.xml','2011-02-04 10:11:58','75e9bb7923a12a020d14d3cda8a11','Set Column as Auto-Increment','',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('KENMEI-2154_Jobs_Services_als_Tasks_in_Datenbank-Rename-Columns','unknown','de/communardo/kenmei/database/update/v1.3/db.changelog.v1.3.xml','2011-02-04 10:11:58','26b5c92cd2dc3debe146334576775857','Rename Column (x2)','',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('KENMEI-2240_Spalte_discussionId_in_core_note','unknown','de/communardo/kenmei/database/update/v1.3/db.changelog.v1.3.xml','2011-02-04 10:12:00','bedafeee1e858694c28879e61a2c5b26','Add Column','',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('KENMEI-2240_discussionId_befuellen-MySQL','unknown','de/communardo/kenmei/database/update/v1.3/db.changelog.v1.3.xml','2011-02-04 10:12:00','89ee14e79e5c6e8054bff3e64e3d5c49','Custom SQL','',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('KENMEI-Note-properties_mysql','unknown','de/communardo/kenmei/database/update/v1.3/db.changelog.v1.3.xml','2011-02-04 10:12:00','3996e5ba99563e196a658722edc496','Custom SQL, Add Foreign Key Constraint','',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('KENMEI-Note-Properties_clob','unknown','de/communardo/kenmei/database/update/v1.3/db.changelog.v1.3.xml','2011-02-04 10:12:01','61daee3a8c3850f6402bdb11ab5a49bb','Modify Column','',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('KENMEI-2261-LDAP_Authentication_SASL_Support','unknown','de/communardo/kenmei/database/update/v1.3/db.changelog.v1.3.xml','2011-02-04 10:12:01','66db577a2f9c6ee89bdfa2c4848b117f','Add Column','',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('Add_Last_Modification_Date_To_Properties','unknown','de/communardo/kenmei/database/update/v1.3/db.changelog.v1.3.xml','2011-02-04 10:12:01','a452ff6128142840f7a082334267d02b','Add Column (x4)','',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('KENMEI-2288_remove_old_caching_properties','unknown','de/communardo/kenmei/database/update/v1.3/db.changelog.v1.3.xml','2011-02-04 10:12:01','93ac2c8652ac9ce97b5682a4a85cc0','Custom SQL','',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('KENMEI-2315_mediumblob_for_global_binary_property_2','unknown','de/communardo/kenmei/database/update/v1.3/db.changelog.v1.3.xml','2011-02-04 10:12:02','8677bcb553814bf24c8fb3f41bb82ef7','Custom SQL','',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('KENMEI-2465_Index_for_better_performance_mysql','unknown','de/communardo/kenmei/database/update/v1.3.1/db.changelog.v1.3.1.xml','2011-02-04 10:12:03','64af9b8975bff0a14f4c95fa6c94715','Custom SQL','',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('Like-Funktion','UNKNOWN','de/communardo/kenmei/database/update/v1.3.2/db.changelog.v1.3.2.xml','2011-02-04 10:12:03','3bcadf67c0e431aad437221dbfbe6cf5','Create Table','',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('Like-Funktion_MySQL','UNKNOWN','de/communardo/kenmei/database/update/v1.3.2/db.changelog.v1.3.2.xml','2011-02-04 10:12:04','67a7975fddf52af67fae5416ae067','Add Foreign Key Constraint (x2), Set Column as Auto-Increment','',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('KENMEI-2601_Index-fuer-discussion_id-entfernen','UNKNOWN','de/communardo/kenmei/database/update/v1.3.2/db.changelog.v1.3.2.xml','2011-02-04 10:12:04','4ae911eeae58366bebf8ae724282571a','Drop Index','',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('KENMEI-2601_Index-fuer-discussion_id-anlegen','UNKNOWN','de/communardo/kenmei/database/update/v1.3.2/db.changelog.v1.3.2.xml','2011-02-04 10:12:04','a6483cd076bdafe25975ac34fcfc3384','Create Index','',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('KENMEI-Blog-Properties','unknown','de/communardo/kenmei/database/update/v1.3.2/db.changelog.v1.3.2.xml','2011-02-04 10:12:04','6f5bc99c34318034fda2208600b773','Create Table','',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('KENMEI-Blog-Properties_fkc','unknown','de/communardo/kenmei/database/update/v1.3.2/db.changelog.v1.3.2.xml','2011-02-04 10:12:05','e8ba9d17768c1e979aec77c75996bf1a','Add Foreign Key Constraint','',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('KENMEI-Blog-Properties_autoincrement','unknown','de/communardo/kenmei/database/update/v1.3.2/db.changelog.v1.3.2.xml','2011-02-04 10:12:05','8c8dfcead1ae690398bd7706d2851d','Set Column as Auto-Increment','',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('KENMEI-2608-Refactor MostUsedBlogs','unknown','de/communardo/kenmei/database/update/v1.3.3/db.changelog.v1.3.3.xml','2011-02-04 10:12:05','f64386ffc88d3fef2b45eb268f6769b5','Drop Table','',NULL,'1.9.2');
--INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('ADD_INSTALLATION_ID','unknown','de/communardo/kenmei/database/update_2nd_pass/v1.1.4/db.changelog.v1.1.4.xml','2011-02-04 10:12:05','467db414d6b587dab40689753ecef1','Custom Change','Inserts an unique id to the application properties.',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('CR_115_Move_Application_Properties_Into_DB','unknown','de/communardo/kenmei/database/update_2nd_pass/v1.1.4/db.changelog.v1.1.4.xml','2011-02-04 10:12:05','57ff61206a4cf95e4a917e86e6bb0a8','Custom Change','Get rid of property files and store the properties in the DB.',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('CR_115_Refactor_URL_Application_Properties','unknown','de/communardo/kenmei/database/update_2nd_pass/v1.1.4/db.changelog.v1.1.4.xml','2011-02-04 10:12:05','5ff5d0d59e42e57f8e949791cfad13e','Custom Change','Convert old URL related properties to the new properties.',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('Create_Default_Blog_Where_Missing','unknown','de/communardo/kenmei/database/update_2nd_pass/v1.1.4/db.changelog.v1.1.4.xml','2011-02-04 10:12:06','b5f386a532aacea1a461a7be18459a52','Custom Change','Create the default blog for clients where there is none yet.',NULL,'1.9.2');
--INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('Update_Master_Data_From_Property_Files','unknown','de/communardo/kenmei/database/update_2nd_pass/v1.1.4/db.changelog.v1.1.4.xml','2011-02-04 10:12:06','38a37931a03d7a8d2b156f8813123074','Custom Change','Update the master data stored in DB with entries from property files.',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('Create_encrypted_creation_date','unknown','de/communardo/kenmei/database/update_2nd_pass/v1.1.4/db.changelog.v1.1.4.xml','2011-02-04 10:12:06','b8279228f0818d3e3d42ba4a1984199','Custom Change','No comment.',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('KENMEI-1672_Virus_scanner_in_admin_section','unknown','de/communardo/kenmei/database/update_2nd_pass/v1.1.4/db.changelog.v1.1.4.xml','2011-02-04 10:12:06','e99e6c9bad1fb4f2a4b5f15e30e2d681','Custom Change','Add enabled option for virus scanner',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('KENMEI-1617_NullPointerException_bei_Zugriff_auf_Certificates','unknown','de/communardo/kenmei/database/update_2nd_pass/v1.1.4/db.changelog.v1.1.4.xml','2011-02-04 10:12:06','9af4a69dbe173a28caa8ea67321a9612','Custom Change','Add password for keystore if it doesnt exist.',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('KENMEI-1694_Follow-remove_tag_globalIds','unknown','de/communardo/kenmei/database/update_2nd_pass/v1.2/db.changelog.v1.2.xml','2011-02-04 10:12:06','5db46f07a5f2b465bc80d13d44398','Custom SQL','Remove global IDs from tags',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('KENMEI-1694_Follow-add_tag_globalIds','unknown','de/communardo/kenmei/database/update_2nd_pass/v1.2/db.changelog.v1.2.xml','2011-02-04 10:12:06','3ee19392e521fda2a8629f23c9baba98','Custom Change','Create the default blog for clients where there is none yet.',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('KENMEI-1694_Follow-add_blog_globalIds','unknown','de/communardo/kenmei/database/update_2nd_pass/v1.2/db.changelog.v1.2.xml','2011-02-04 10:12:06','c79f0fa2747d5864dcf55aa98b5100','Custom Change','Create the global Id for blogs where there is none yet.',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('KENMEI-2104_Ldap_Passwort_verschluesseln','unknown','de/communardo/kenmei/database/update_2nd_pass/v1.2/db.changelog.v1.2.xml','2011-02-04 10:12:06','f472fa9537c62bf44ffbd73b1ffb6ad','Custom Change','encrypts the LDAP manager password',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('KENMEI-2009_Ldap_config-add_uid_to_mapping','unknown','de/communardo/kenmei/database/update_2nd_pass/v1.2/db.changelog.v1.2.xml','2011-02-04 10:12:06','4c44b01203e35fc2473fdafac8c5cc','Custom Change','Add UID to userSearch property mapping',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('KENMEI-2510_confluence_passwort_verschluesseln','unknown','de/communardo/kenmei/database/update_2nd_pass/v1.2/db.changelog.v1.2.xml','2011-02-04 10:12:06','adf43a30d6bf756fefc6cfb243f428','Custom Change','encrypts the Confluence administrator password',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('KENMEI-2510_sharepoint_passwort_verschluesseln','unknown','de/communardo/kenmei/database/update_2nd_pass/v1.2/db.changelog.v1.2.xml','2011-02-04 10:12:06','80ddc2197d1d616a48d012e6fb802452','Custom Change','encrypts the Sharepoint administrator password',NULL,'1.9.2');
--INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('KENMEI-2154-Jobs_Services_als_Tasks_in_Datenbank','unknown','de/communardo/kenmei/database/update_2nd_pass/v1.3/db.changelog.v1.3.xml','2011-02-04 10:12:06','627db31b9a285c1a388d525db74626','Custom Change','Updating existing jobs.',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('KENMEI_1941_Follow_BE_Add_Followable_Items','unknown','de/communardo/kenmei/database/update_runtime/v1.2/db.changelog.runtime.v1.2.xml','2011-02-04 10:18:10','f26f3ec897d29e80cb3aedbe98993545','Custom Change','add followable items to all existing notes',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('KENMEI-2616_Index_auf_Tag_lower_name_anlegen_mysql','UNKNOWN','de/communardo/kenmei/database/update/v1.3.2/db.changelog.v1.3.2.xml','2011-02-08 10:01:08','8911ae8ae2fdc6b26465e7fc86c3e756','Custom SQL','',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('kenmei-1988-ramconnector-entfernen','unknown','de/communardo/kenmei/database/update/v1.3.3/db.changelog.v1.3.3.xml','2011-02-14 10:23:19','9569702cd7d0b74787b6ab783602989','Drop Table','',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('KENMEI-1521_Purge_Disconnected_External_Auths','unknown','de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml','2011-02-17 08:28:11','2f2d42d3b88a45b1857590a0f80dc75','Custom SQL','',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('KENMEI-2628_Remove_Wrong_Unique_Constraint','unknown','de/communardo/kenmei/database/update/v1.2/db.changelog.v1.2.xml','2011-02-17 08:28:13','b02c2845ed23257c053a0c26cd8dcb4','Custom SQL, Add Unique Constraint','',NULL,'1.9.2');
--INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('KENMEI-2154-Jobs_Services_als_Tasks_in_Datenbank_v2','unknown','de/communardo/kenmei/database/update_2nd_pass/v1.3/db.changelog.v1.3.xml','2011-03-28 10:39:44','627db31b9a285c1a388d525db74626','Custom Change','Updating existing jobs.',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('KENMEI-2629-Reset-Primary-Auth-on-deactivated-external-auth','unknown','de/communardo/kenmei/database/update/v1.3.3/db.changelog.v1.3.3.xml','2011-05-12 09:47:36','d9932ed049603f4d816789d0454a285a','Update Data (x2)','',NULL,'1.9.2');
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES ('KENMEI-2629-Set-Internal-DB-Auth','unknown','de/communardo/kenmei/database/update/v1.3.3/db.changelog.v1.3.3.xml','2011-05-13 16:39:50','15316e7b824f8887b286053f38ac54d','Custom SQL','',NULL,'1.9.2');
	
