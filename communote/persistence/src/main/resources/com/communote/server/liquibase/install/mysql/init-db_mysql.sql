--
-- schema definition from init-db.sql of version 1.1.3 revision 5322
-- with minor bugfixes:
--  corrected unique constraint on custom_messages (andromda bug)
--  renamed index/constraint core_tag_TAG_CLEARANCES_FKC on table core_tag2clearance_include to core_tag_inc_TAG_CLEARANCES_FKC because of name conflict 
create table channel_configuration (
     `ID` BIGINT not null auto_increment,
     `FORCE_SSL` TINYINT not null,
     `CHANNEL_TYPE` VARCHAR(1024) not null,
     primary key (`ID`)
    ) ENGINE=InnoDB;
create table configuration (
    `ID` BIGINT not null auto_increment,
     CLIENT_CONFIG_FK BIGINT unique,
     primary key (`ID`)
    ) ENGINE=InnoDB;
create table configuration_client (
     `ID` BIGINT not null auto_increment,
     `LOGO_IMAGE` LONGBLOB,
     `LAST_LOGO_IMAGE_MODIFICATION_D` TIMESTAMP default 0,
     `TIME_ZONE_ID` VARCHAR(1024),
     DEFAULT_BLOG_FK BIGINT unique,
     primary key (`ID`)
    ) ENGINE=InnoDB;
create table configuration_confluence (
     `ID` BIGINT not null,
     `AUTHENTICATION_API_URL` VARCHAR(1024) not null,
     `IMAGE_API_URL` VARCHAR(1024),
     `ADMIN_LOGIN` VARCHAR(1024),
     `ADMIN_PASSWORD` VARCHAR(1024),
     `SERVICE_URL` VARCHAR(1024),
     `PERMISSIONS_URL` VARCHAR(1024),
     `BASE_PATH` VARCHAR(1024),
     primary key (`ID`)
    ) ENGINE=InnoDB;
create table configuration_external_system (
     `ID` BIGINT not null auto_increment,
     `ALLOW_EXTERNAL_AUTHENTICATION` TINYINT not null,
     `SYSTEM_ID` VARCHAR(50) not null unique,
     `PRIMARY_AUTHENTICATION` TINYINT not null,
     `SYNCHRONIZE_USER_GROUPS` TINYINT not null,
     CONFIGURATION_FK BIGINT,
     primary key (`ID`)
    ) ENGINE=InnoDB;
create table configuration_ldap (
     `ID` BIGINT not null,
     `URL` VARCHAR(1024) not null,
     `MANAGER_PASSWORD` VARCHAR(1024) not null,
     `MANAGER_D_N` VARCHAR(1024) not null,
     `SEARCHBASE` VARCHAR(1024) not null,
     `SEARCHFILTER` VARCHAR(1024) not null,
     `SEARCH_SUB_TREE` TINYINT not null,
     `PROPERTY_MAPPING` VARCHAR(1024) not null,
     primary key (`ID`)
    ) ENGINE=InnoDB;
create table configuration_setting (
     `KEY` VARCHAR(255) not null,
     `VALUE` TEXT,
     CONFIGURATION_FK BIGINT,
     primary key (`KEY`)
    ) ENGINE=InnoDB;
create table configuration_sharepoint (
     `ID` BIGINT not null,
     `AUTHENTICATION_API_URL` VARCHAR(1024) not null,
     `ADMIN_PASSWORD` VARCHAR(1024) not null,
     `ADMIN_LOGIN` VARCHAR(1024) not null,
     `IMAGE_API_URL` VARCHAR(1024),
     primary key (`ID`)
    ) ENGINE=InnoDB;
create table core_attachment (
     `ID` BIGINT not null auto_increment,
     `CONTENT_IDENTIFIER` VARCHAR(1024) not null,
     `REPOSITORY_IDENTIFIER` VARCHAR(1024) not null,
     `NAME` VARCHAR(1024) not null,
     `CONTENT_TYPE` VARCHAR(1024),
     `SIZE` BIGINT,
     `STATUS` VARCHAR(1024) not null,
     GLOBAL_ID_FK BIGINT unique,
     NOTE_FK BIGINT,
     primary key (`ID`)
    ) ENGINE=InnoDB;
create table core_blog (
     `ID` BIGINT not null auto_increment,
     `TITLE` VARCHAR(1024) not null,
     `DESCRIPTION` TEXT,
     `CREATION_DATE` TIMESTAMP default 0 not null,
     `NAME_IDENTIFIER` VARCHAR(255) not null unique,
     `LAST_MODIFICATION_DATE` TIMESTAMP default 0 not null,
     `ALL_CAN_READ` TINYINT not null,
     `ALL_CAN_WRITE` TINYINT not null,
     `PUBLIC_ACCESS` TINYINT not null,
     GLOBAL_ID_FK BIGINT unique,
     primary key (`ID`)
    ) ENGINE=InnoDB;
create table core_blog2tag (
     BLOGS_FK BIGINT not null,
     TAGS_FK BIGINT not null,
     primary key (BLOGS_FK, TAGS_FK)
    ) ENGINE=InnoDB;
create table core_blog_event (
     `ID` BIGINT not null auto_increment,
     `TIME` TIMESTAMP default 0 not null,
     `TYPE` VARCHAR(1024) not null,
     CREATOR_FK BIGINT,
     BLOG_FK BIGINT,
     primary key (`ID`)
    ) ENGINE=InnoDB;
create table core_blog_member (
     `ID` BIGINT not null auto_increment,
     class varchar(255) not null,
     `ROLE` VARCHAR(1024) not null,
     BLOG_FK BIGINT not null,
     KENMEI_ENTITY_FK BIGINT not null,
     `EXTERNAL_SYSTEM_ID` VARCHAR(50),
     primary key (`ID`)
    ) ENGINE=InnoDB;
create table core_content (
     `ID` BIGINT not null auto_increment,
     `CONTENT` TEXT not null,
     `SHORT_CONTENT` TEXT,
     primary key (`ID`)
    ) ENGINE=InnoDB;
create table core_external_object (
     `ID` BIGINT not null auto_increment,
     `EXTERNAL_SYSTEM_ID` VARCHAR(50) not null,
     `EXTERNAL_ID` VARCHAR(200) not null,
     `EXTERNAL_NAME` VARCHAR(1024),
     BLOG_FK BIGINT,
     primary key (`ID`),
     unique (`EXTERNAL_SYSTEM_ID`, `EXTERNAL_ID`)
    ) ENGINE=InnoDB;
create table core_external_object_properties (
     `ID` BIGINT not null auto_increment,
     `KEY` VARCHAR(300) not null,
     `VALUE` VARCHAR(1024) not null,
     EXTERNAL_OBJECT_FK BIGINT,
     primary key (`ID`)
    ) ENGINE=InnoDB;
create table core_global_id (
     `ID` BIGINT not null auto_increment,
     `GLOBAL_IDENTIFIER` VARCHAR(255) not null unique,
     primary key (`ID`)
    ) ENGINE=InnoDB;
create table core_note (
     `ID` BIGINT not null auto_increment,
     `CREATION_DATE` TIMESTAMP default 0 not null,
     `LAST_MODIFICATION_DATE` TIMESTAMP default 0 not null,
     `CREATION_SOURCE` VARCHAR(1024) not null,
     `DIRECT` TINYINT not null,
     `STATUS` VARCHAR(1024) not null,
     `VERSION` BIGINT not null,
     `DISCUSSION_PATH` VARCHAR(1024),
     USER_FK BIGINT not null,
     CONTENT_FK BIGINT not null unique,
     GLOBAL_ID_FK BIGINT unique,
     BLOG_FK BIGINT not null,
     PARENT_FK BIGINT,
     ORIGIN_FK BIGINT,
     primary key (`ID`)
    ) ENGINE=InnoDB;
create table core_notes2crossblogs (
     NOTES_FK BIGINT not null,
     CROSSPOST_BLOGS_FK BIGINT not null,
     primary key (NOTES_FK, CROSSPOST_BLOGS_FK)
    ) ENGINE=InnoDB;
create table core_notes2tag (
     NOTES_FK BIGINT not null,
     TAGS_FK BIGINT not null,
     primary key (NOTES_FK, TAGS_FK)
    ) ENGINE=InnoDB;
create table core_notes2user_to_notify (
     NOTES_FK BIGINT not null,
     USERS_TO_BE_NOTIFIED_FK BIGINT not null,
     primary key (NOTES_FK, USERS_TO_BE_NOTIFIED_FK)
    ) ENGINE=InnoDB;
create table core_processed_utp_mail (
     `ID` BIGINT not null auto_increment,
     `MAIL_MESSAGE_ID` VARCHAR(255) not null unique,
     primary key (`ID`)
    ) ENGINE=InnoDB;
create table core_role2blog (
     `ID` BIGINT not null auto_increment,
     `BLOG_ID` BIGINT not null,
     `USER_ID` BIGINT not null,
     `NUMERIC_ROLE` INTEGER not null,
     `EXTERNAL_SYSTEM_ID` VARCHAR(1024),
     `GRANTED_BY_GROUP` TINYINT not null,
     primary key (`ID`)
    ) ENGINE=InnoDB;
create table core_role2blog_granting_group (
     USER_TO_BLOG_ROLE_MAPPINGS_FK BIGINT not null,
     GRANTING_GROUPS_FK BIGINT not null,
     primary key (USER_TO_BLOG_ROLE_MAPPINGS_FK, GRANTING_GROUPS_FK)
    ) ENGINE=InnoDB;
create table core_tag (
     `ID` BIGINT not null auto_increment,
     class varchar(255) not null,
     `LOWER_NAME` VARCHAR(1024) not null,
     `NAME` VARCHAR(255) not null unique,
     GLOBAL_ID_FK BIGINT unique,
     CATEGORY_FK BIGINT,
     ABSTRACT_TAG_CATEGORY_TAGS_IDX integer,
     primary key (`ID`)
    ) ENGINE=InnoDB;
create table core_tag2clearance_exclude (
     TAG_CLEARANCES_FK BIGINT not null,
     EXCLUDE_TAGS_FK BIGINT not null,
     TAG_CLEARANCE_EXCLUDE_TAGS_IDX integer not null,
     primary key (TAG_CLEARANCES_FK, TAG_CLEARANCE_EXCLUDE_TAGS_IDX)
    ) ENGINE=InnoDB;
create table core_tag2clearance_include (
     TAG_CLEARANCES_FK BIGINT not null,
     INCLUDE_TAGS_FK BIGINT not null,
     TAG_CLEARANCE_INCLUDE_TAGS_IDX integer not null,
     primary key (TAG_CLEARANCES_FK, TAG_CLEARANCE_INCLUDE_TAGS_IDX)
    ) ENGINE=InnoDB;
create table core_tag_category (
     `ID` BIGINT not null auto_increment,
     class varchar(255) not null,
     `NAME` VARCHAR(1024) not null,
     `PREFIX` VARCHAR(1024) not null,
     `DESCRIPTION` VARCHAR(1024),
     `MULTIPLE_TAGS` TINYINT not null,
     primary key (`ID`)
    ) ENGINE=InnoDB;
create table core_tag_clearance (
     `ID` BIGINT not null auto_increment,
     class varchar(255) not null,
     `INCLUDE_PROTECTED_RESOURCES` TINYINT,
     OWNER_FK BIGINT,
     primary key (`ID`)
    ) ENGINE=InnoDB;
create table core_users2favorite_notes (
     FAVORITE_NOTES_FK BIGINT not null,
     FAVORITE_USERS_FK BIGINT not null,
     primary key (FAVORITE_NOTES_FK, FAVORITE_USERS_FK)
    ) ENGINE=InnoDB;
create table crc_cache_config (
     `ID` BIGINT not null auto_increment,
     `TIME_TO_STAY` BIGINT not null,
     CURRENT_CACHE_CONNECTOR_CON_FK BIGINT unique,
     primary key (`ID`)
    ) ENGINE=InnoDB;
create table crc_connector_config (
     `ID` BIGINT not null auto_increment,
     `SUPPORTS_METADATA` TINYINT not null,
     `CONNECTOR_ID` VARCHAR(1024) not null,
     primary key (`ID`)
    ) ENGINE=InnoDB;
create table crc_filesystem_config (
     `ID` BIGINT not null,
     `PATH` VARCHAR(1024) not null,
     primary key (`ID`)
    ) ENGINE=InnoDB;
create table crc_ramconn_config (
     `ID` BIGINT not null,
     `MIN_ABSOLUTE_FREE_MEMORY` BIGINT not null,
     `MIN_FREE_MEMORY_IN_PERCENT` INTEGER not null,
     primary key (`ID`)
    ) ENGINE=InnoDB;
create table custom_messages (
     `ID` BIGINT not null auto_increment,
     `KEY` VARCHAR(255) not null,
     `MESSAGE` TEXT not null,
     `IS_HTML` TINYINT not null,
     LANGUAGE_FK BIGINT unique,
     unique(`KEY`, LANGUAGE_FK),
     primary key (`ID`)
    ) ENGINE=InnoDB;
create table iprange_channel (
     `TYPE` VARCHAR(255) not null,
     `ENABLED` TINYINT not null,
     primary key (`TYPE`)
    ) ENGINE=InnoDB;
create table iprange_filter (
     `ID` BIGINT not null auto_increment,
     `NAME` VARCHAR(1024) not null,
     `ENABLED` TINYINT not null,
     primary key (`ID`)
    ) ENGINE=InnoDB;
create table iprange_filter_channel (
     IP_RANGE_FILTERS_FK BIGINT not null,
     CHANNELS_FK VARCHAR(255) not null,
     primary key (IP_RANGE_FILTERS_FK, CHANNELS_FK)
    ) ENGINE=InnoDB;
create table iprange_range (
     `ID` BIGINT not null auto_increment,
     `START_VALUE` LONGBLOB not null,
     `END_VALUE` LONGBLOB not null,
     `STRING_REPRESENTATION` VARCHAR(1024),
     IP_RANGE_FILTER_IN_FK BIGINT,
     IP_RANGE_FILTER_EX_FK BIGINT,
     primary key (`ID`)
    ) ENGINE=InnoDB;
create table mc_config (
     `ID` BIGINT not null auto_increment,
     `TYPE` VARCHAR(1024) not null,
     `PROPERTIES` VARCHAR(1024) not null,
     `ONLY_IF_AVAILABLE` TINYINT not null,
     `PRIORITY` INTEGER not null,
     NOTIFICATION_CONFIG_FK BIGINT,
     primary key (`ID`)
    ) ENGINE=InnoDB;
create table md_country (
     `ID` BIGINT not null auto_increment,
     `COUNTRY_CODE` VARCHAR(255) not null unique,
     `NAME` VARCHAR(1024) not null,
     primary key (`ID`)
    ) ENGINE=InnoDB;
create table md_language (
     `ID` BIGINT not null auto_increment,
     `LANGUAGE_CODE` VARCHAR(255) not null unique,
     `NAME` VARCHAR(1024) not null,
     primary key (`ID`)
    ) ENGINE=InnoDB;
create table misc_query_helper (
     `ID` BIGINT not null auto_increment,
     primary key (`ID`)
    ) ENGINE=InnoDB;
create table news_feed_cache (
     `ID` BIGINT not null auto_increment,
     `TIMESTAMP` TIMESTAMP default 0 not null,
     `USER_ID` BIGINT not null unique,
     `CONTENT_DATA` TEXT not null,
     primary key (`ID`)
    ) ENGINE=InnoDB;
create table news_widget_feed (
     `ID` BIGINT not null auto_increment,
     `NAME` VARCHAR(1024) not null,
     `TYPE` VARCHAR(1024) not null,
     KENMEI_USER_FK BIGINT,
     primary key (`ID`)
    ) ENGINE=InnoDB;
create table news_widget_parameter (
     `ID` BIGINT not null auto_increment,
     `NAME` VARCHAR(1024) not null,
     `VALUE` VARCHAR(1024) not null,
     WIDGET_NEWS_FEED_FK BIGINT,
     primary key (`ID`)
    ) ENGINE=InnoDB;
create table notification_config (
     `ID` BIGINT not null auto_increment,
     `FALLBACK` VARCHAR(1024),
     primary key (`ID`)
    ) ENGINE=InnoDB;
create table security_code (
     `ID` BIGINT not null auto_increment,
     `CODE` VARCHAR(255) not null unique,
     `ACTION` VARCHAR(1024) not null,
     `CREATING_DATE` TIMESTAMP default 0 not null,
     KENMEI_USER_FK BIGINT,
     primary key (`ID`)
    ) ENGINE=InnoDB;
create table security_email_code (
     `ID` BIGINT not null,
     `NEW_EMAIL_ADDRESS` VARCHAR(1024) not null,
     primary key (`ID`)
    ) ENGINE=InnoDB;
create table security_forgotten_pw_code (
     `ID` BIGINT not null,
     primary key (`ID`)
    ) ENGINE=InnoDB;
create table security_invite_blog (
     `ID` BIGINT not null,
     primary key (`ID`)
    ) ENGINE=InnoDB;
create table security_invite_client (
     `ID` BIGINT not null,
     primary key (`ID`)
    ) ENGINE=InnoDB;
create table security_user_auth_failed_status (
     `ID` BIGINT not null auto_increment,
     `LOCKED_TIMEOUT` TIMESTAMP default 0,
     `FAILED_AUTH_COUNTER` INTEGER not null,
     `CHANNEL_TYPE` VARCHAR(1024) not null,
     KENMEI_USER_FK BIGINT,
     primary key (`ID`)
    ) ENGINE=InnoDB;
create table security_user_code (
     `ID` BIGINT not null,
     primary key (`ID`)
    ) ENGINE=InnoDB;
create table security_user_unlock_code (
     `ID` BIGINT not null,
     `CHANNEL` VARCHAR(1024) not null,
     primary key (`ID`)
    ) ENGINE=InnoDB;
create table snc_config (
     `ID` BIGINT not null auto_increment,
     `USERNAME` VARCHAR(1024) not null,
     `PASSWORD` VARCHAR(1024) not null,
     `PROPERTIES_STRING` VARCHAR(1024) not null,
     `LAST_SYNCHRONIZATION` TIMESTAMP default 0,
     `SOCIAL_NETWORK_TYPE` VARCHAR(1024) not null,
     `REQUEST_INTERVAL` BIGINT not null,
     `SYNCHRONIZATION_ENABLED` TINYINT not null,
     `CONFIG_NAME` VARCHAR(1024) not null,
     USER_PROFILE_FK BIGINT not null,
     primary key (`ID`)
    ) ENGINE=InnoDB;
create table snc_job_details (
     `ID` BIGINT not null auto_increment,
     `LAST_REQUEST` BIGINT,
     `NEXT_REQUEST` BIGINT not null,
     `MISFIRED_COUNTER` INTEGER not null,
     `IS_DISABLED` TINYINT not null,
     `LAST_MESSAGE` VARCHAR(1024),
     SOCIAL_NETWORK_CONFIGURATIO_FK BIGINT not null unique,
     primary key (`ID`)
    ) ENGINE=InnoDB;
create table user_authorities (
     `ID` BIGINT not null auto_increment,
     `ROLE` VARCHAR(1024) not null,
     KENMEI_USER_FK BIGINT,
     primary key (`ID`)
    ) ENGINE=InnoDB;
create table user_client (
     `ID` BIGINT not null auto_increment,
     `CLIENT_ID` VARCHAR(255) not null unique,
     `PROXY_URL_PREFIX` VARCHAR(1024),
     `SECURITY_KEY` VARCHAR(1024),
     `NAME` VARCHAR(1024) not null,
     `CLIENT_STATUS` VARCHAR(1024) not null,
     `CREATION_VERSION` VARCHAR(1024),
     `CREATION_TIME` TIMESTAMP default 0,
     `CREATION_REVISION` BIGINT,
     primary key (`ID`)
    ) ENGINE=InnoDB;
create table user_client_statistic (
     `ID` BIGINT not null auto_increment,
     `REPOSITORY_SIZE` BIGINT not null,
     primary key (`ID`)
    ) ENGINE=InnoDB;
create table user_contact (
     `ID` BIGINT not null auto_increment,
     `STREET` VARCHAR(1024),
     `ZIP` VARCHAR(1024),
     `CITY` VARCHAR(1024),
     `PHONE_COUNTRY_CODE` VARCHAR(1024) not null,
     `PHONE_AREA_CODE` VARCHAR(1024) not null,
     `PHONE_PHONE_NUMBER` VARCHAR(1024) not null,
     `FAX_COUNTRY_CODE` VARCHAR(1024) not null,
     `FAX_AREA_CODE` VARCHAR(1024) not null,
     `FAX_PHONE_NUMBER` VARCHAR(1024) not null,
     COUNTRY_FK BIGINT,
     primary key (`ID`)
    ) ENGINE=InnoDB;
create table user_entity (
     `ID` BIGINT not null auto_increment,
     GLOBAL_ID_FK BIGINT unique,
     primary key (`ID`)
    ) ENGINE=InnoDB;
create table user_external_auth (
     `ID` BIGINT not null auto_increment,
     `EXTERNAL_USER_ID` VARCHAR(250) not null unique,
     `SYSTEM_ID` VARCHAR(50) not null,
     KENMEI_USER_FK BIGINT,
     primary key (`ID`),
     unique (`EXTERNAL_USER_ID`, `SYSTEM_ID`)
    ) ENGINE=InnoDB;
create table user_group (
     `ID` BIGINT not null,
     `ALIAS` VARCHAR(255) not null unique,
     `NAME` VARCHAR(1024) not null,
     `DESCRIPTION` TEXT,
     primary key (`ID`)
    ) ENGINE=InnoDB;
create table user_group_external (
     `ID` BIGINT not null,
     `EXTERNAL_SYSTEM_ID` VARCHAR(1024) not null,
     `EXTERNAL_ID` VARCHAR(1024) not null,
     primary key (`ID`)
    ) ENGINE=InnoDB;
create table user_group_member (
     GROUP_MEMBERS_FK BIGINT not null,
     GROUPS_FK BIGINT not null,
     primary key (GROUP_MEMBERS_FK, GROUPS_FK)
    ) ENGINE=InnoDB;
create table user_image (
     `ID` BIGINT not null auto_increment,
     `IMAGE` LONGBLOB not null,
     primary key (`ID`)
    ) ENGINE=InnoDB;
create table user_of_group (
     `ID` BIGINT not null auto_increment,
     `MODIFICATION_TYPE` VARCHAR(1024),
     GROUP_FK BIGINT not null,
     USER_FK BIGINT not null,
     primary key (`ID`)
    ) ENGINE=InnoDB;
create table user_profile (
     `ID` BIGINT not null auto_increment,
     `LAST_NAME` VARCHAR(1024),
     `SALUTATION` VARCHAR(1024),
     `POSITION` VARCHAR(1024),
     `COMPANY` VARCHAR(1024),
     `FIRST_NAME` VARCHAR(1024),
     `LAST_MODIFICATION_DATE` TIMESTAMP default 0 not null,
     `LAST_PHOTO_MODIFICATION_DATE` TIMESTAMP default 0,
     `TIME_ZONE_ID` VARCHAR(1024),
     SMALL_IMAGE_FK BIGINT unique,
     CONTACT_FK BIGINT unique,
     MEDIUM_IMAGE_FK BIGINT unique,
     LARGE_IMAGE_FK BIGINT unique,
     NOTIFICATION_CONFIG_FK BIGINT not null unique,
     primary key (`ID`)
    ) ENGINE=InnoDB;
create table user_user (
     `ID` BIGINT not null,
     `PASSWORD` VARCHAR(1024),
     `EMAIL` VARCHAR(255) not null unique,
     `LANGUAGE_CODE` VARCHAR(1024) not null,
     `LAST_LOGIN` TIMESTAMP default 0,
     `STATUS` VARCHAR(1024) not null,
     `ALIAS` VARCHAR(255) unique,
     `TERMS_ACCEPTED` TINYINT not null,
     `REMINDER_MAIL_SENT` TINYINT not null,
     `STATUS_CHANGED` TIMESTAMP default 0 not null,
     PROFILE_FK BIGINT not null unique,
     primary key (`ID`)
    ) ENGINE=InnoDB;
alter table configuration 
     add index configuration_CLIENT_CONFIG_FC (CLIENT_CONFIG_FK), 
     add constraint configuration_CLIENT_CONFIG_FC 
     foreign key (CLIENT_CONFIG_FK) 
     references configuration_client (`ID`);
alter table configuration_client 
     add index configuration_client_DEFAULT_C (DEFAULT_BLOG_FK), 
     add constraint configuration_client_DEFAULT_C 
     foreign key (DEFAULT_BLOG_FK) 
     references core_blog (`ID`);
alter table configuration_confluence 
     add index configuration_confluenceIFKC (`ID`), 
     add constraint configuration_confluenceIFKC 
     foreign key (`ID`) 
     references configuration_external_system (`ID`);
alter table configuration_external_system 
     add index configuration_external_systemC (CONFIGURATION_FK), 
     add constraint configuration_external_systemC 
     foreign key (CONFIGURATION_FK) 
     references configuration (`ID`);
alter table configuration_ldap 
     add index configuration_ldapIFKC (`ID`), 
     add constraint configuration_ldapIFKC 
     foreign key (`ID`) 
     references configuration_external_system (`ID`);
alter table configuration_setting 
     add index configuration_setting_CONFIGUC (CONFIGURATION_FK), 
     add constraint configuration_setting_CONFIGUC 
     foreign key (CONFIGURATION_FK) 
     references configuration (`ID`);
alter table configuration_sharepoint 
     add index configuration_sharepointIFKC (`ID`), 
     add constraint configuration_sharepointIFKC 
     foreign key (`ID`) 
     references configuration_external_system (`ID`);
alter table core_attachment 
     add index core_attachment_NOTE_FKC (NOTE_FK), 
     add constraint core_attachment_NOTE_FKC 
     foreign key (NOTE_FK) 
     references core_note (`ID`);
alter table core_attachment 
     add index core_attachment_GLOBAL_ID_FKC (GLOBAL_ID_FK), 
     add constraint core_attachment_GLOBAL_ID_FKC 
     foreign key (GLOBAL_ID_FK) 
     references core_global_id (`ID`);
alter table core_blog 
     add index core_blog_GLOBAL_ID_FKC (GLOBAL_ID_FK), 
     add constraint core_blog_GLOBAL_ID_FKC 
     foreign key (GLOBAL_ID_FK) 
     references core_global_id (`ID`);
alter table core_blog2tag 
     add index core_tag_BLOGS_FKC (BLOGS_FK), 
     add constraint core_tag_BLOGS_FKC 
     foreign key (BLOGS_FK) 
     references core_blog (`ID`);
alter table core_blog2tag 
     add index core_blog_TAGS_FKC (TAGS_FK), 
     add constraint core_blog_TAGS_FKC 
     foreign key (TAGS_FK) 
     references core_tag (`ID`);
alter table core_blog_event 
     add index core_blog_event_CREATOR_FKC (CREATOR_FK), 
     add constraint core_blog_event_CREATOR_FKC 
     foreign key (CREATOR_FK) 
     references user_user (`ID`);
alter table core_blog_event 
     add index core_blog_event_BLOG_FKC (BLOG_FK), 
     add constraint core_blog_event_BLOG_FKC 
     foreign key (BLOG_FK) 
     references core_blog (`ID`);
alter table core_blog_member 
     add index core_blog_member_BLOG_FKC (BLOG_FK), 
     add constraint core_blog_member_BLOG_FKC 
     foreign key (BLOG_FK) 
     references core_blog (`ID`);
alter table core_blog_member 
     add index core_blog_member_KENMEI_ENTITC (KENMEI_ENTITY_FK), 
     add constraint core_blog_member_KENMEI_ENTITC 
     foreign key (KENMEI_ENTITY_FK) 
     references user_entity (`ID`);
alter table core_external_object 
     add index core_external_object_BLOG_FKC (BLOG_FK), 
     add constraint core_external_object_BLOG_FKC 
     foreign key (BLOG_FK) 
     references core_blog (`ID`);
alter table core_external_object_properties 
     add index core_external_object_propertiC (EXTERNAL_OBJECT_FK), 
     add constraint core_external_object_propertiC 
     foreign key (EXTERNAL_OBJECT_FK) 
     references core_external_object (`ID`);
alter table core_note 
     add index core_note_PARENT_FKC (PARENT_FK), 
     add constraint core_note_PARENT_FKC 
     foreign key (PARENT_FK) 
     references core_note (`ID`);
alter table core_note 
     add index core_note_CONTENT_FKC (CONTENT_FK), 
     add constraint core_note_CONTENT_FKC 
     foreign key (CONTENT_FK) 
     references core_content (`ID`);
alter table core_note 
     add index core_note_USER_FKC (USER_FK), 
     add constraint core_note_USER_FKC 
     foreign key (USER_FK) 
     references user_user (`ID`);
alter table core_note 
     add index core_note_ORIGIN_FKC (ORIGIN_FK), 
     add constraint core_note_ORIGIN_FKC 
     foreign key (ORIGIN_FK) 
     references core_note (`ID`);
alter table core_note 
     add index core_note_BLOG_FKC (BLOG_FK), 
     add constraint core_note_BLOG_FKC 
     foreign key (BLOG_FK) 
     references core_blog (`ID`);
alter table core_note 
     add index core_note_GLOBAL_ID_FKC (GLOBAL_ID_FK), 
     add constraint core_note_GLOBAL_ID_FKC 
     foreign key (GLOBAL_ID_FK) 
     references core_global_id (`ID`);
alter table core_notes2crossblogs 
     add index core_note_CROSSPOST_BLOGS_FKC (CROSSPOST_BLOGS_FK), 
     add constraint core_note_CROSSPOST_BLOGS_FKC 
     foreign key (CROSSPOST_BLOGS_FK) 
     references core_blog (`ID`);
alter table core_notes2crossblogs 
     add index core_blog_NOTES_FKC (NOTES_FK), 
     add constraint core_blog_NOTES_FKC 
     foreign key (NOTES_FK) 
     references core_note (`ID`);
alter table core_notes2tag 
     add index core_note_TAGS_FKC (TAGS_FK), 
     add constraint core_note_TAGS_FKC 
     foreign key (TAGS_FK) 
     references core_tag (`ID`);
alter table core_notes2tag 
     add index core_tag_NOTES_FKC (NOTES_FK), 
     add constraint core_tag_NOTES_FKC 
     foreign key (NOTES_FK) 
     references core_note (`ID`);
alter table core_notes2user_to_notify 
     add index core_note_USERS_TO_BE_NOTIFIEC (USERS_TO_BE_NOTIFIED_FK), 
     add constraint core_note_USERS_TO_BE_NOTIFIEC 
     foreign key (USERS_TO_BE_NOTIFIED_FK) 
     references user_user (`ID`);
alter table core_notes2user_to_notify 
     add index user_user_NOTES_FKC (NOTES_FK), 
     add constraint user_user_NOTES_FKC 
     foreign key (NOTES_FK) 
     references core_note (`ID`);
alter table core_role2blog_granting_group 
     add index core_role2blog_GRANTING_GROUPC (GRANTING_GROUPS_FK), 
     add constraint core_role2blog_GRANTING_GROUPC 
     foreign key (GRANTING_GROUPS_FK) 
     references user_group (`ID`);
alter table core_role2blog_granting_group 
     add index user_group_USER_TO_BLOG_ROLE_C (USER_TO_BLOG_ROLE_MAPPINGS_FK), 
     add constraint user_group_USER_TO_BLOG_ROLE_C 
     foreign key (USER_TO_BLOG_ROLE_MAPPINGS_FK) 
     references core_role2blog (`ID`);
alter table core_tag 
     add index CATEGORIZED_TAG_CATEGORY_FKC (CATEGORY_FK), 
     add constraint CATEGORIZED_TAG_CATEGORY_FKC 
     foreign key (CATEGORY_FK) 
     references core_tag_category (`ID`);
alter table core_tag 
     add index core_tag_GLOBAL_ID_FKC (GLOBAL_ID_FK), 
     add constraint core_tag_GLOBAL_ID_FKC 
     foreign key (GLOBAL_ID_FK) 
     references core_global_id (`ID`);
alter table core_tag2clearance_exclude 
     add index core_tag_TAG_CLEARANCES_FKC (TAG_CLEARANCES_FK), 
     add constraint core_tag_TAG_CLEARANCES_FKC 
     foreign key (TAG_CLEARANCES_FK) 
     references core_tag_clearance (`ID`);
alter table core_tag2clearance_exclude 
     add index core_tag_clearance_EXCLUDE_TAC (EXCLUDE_TAGS_FK), 
     add constraint core_tag_clearance_EXCLUDE_TAC 
     foreign key (EXCLUDE_TAGS_FK) 
     references core_tag (`ID`);
alter table core_tag2clearance_include 
     add index core_tag_clearance_INCLUDE_TAC (INCLUDE_TAGS_FK), 
     add constraint core_tag_clearance_INCLUDE_TAC 
     foreign key (INCLUDE_TAGS_FK) 
     references core_tag (`ID`);
alter table core_tag2clearance_include 
     add index core_tag_inc_TAG_CLEARANCES_FKC (TAG_CLEARANCES_FK), 
     add constraint core_tag_inc_TAG_CLEARANCES_FKC 
     foreign key (TAG_CLEARANCES_FK) 
     references core_tag_clearance (`ID`);
alter table core_tag_clearance 
     add index core_user_tag_clearance_OWNERC (OWNER_FK), 
     add constraint core_user_tag_clearance_OWNERC 
     foreign key (OWNER_FK) 
     references user_user (`ID`);
alter table core_users2favorite_notes 
     add index user_user_FAVORITE_NOTES_FKC (FAVORITE_NOTES_FK), 
     add constraint user_user_FAVORITE_NOTES_FKC 
     foreign key (FAVORITE_NOTES_FK) 
     references core_note (`ID`);
alter table core_users2favorite_notes 
     add index core_note_FAVORITE_USERS_FKC (FAVORITE_USERS_FK), 
     add constraint core_note_FAVORITE_USERS_FKC 
     foreign key (FAVORITE_USERS_FK) 
     references user_user (`ID`);
alter table crc_cache_config 
     add index crc_cache_config_CURRENT_CACHC (CURRENT_CACHE_CONNECTOR_CON_FK), 
     add constraint crc_cache_config_CURRENT_CACHC 
     foreign key (CURRENT_CACHE_CONNECTOR_CON_FK) 
     references crc_connector_config (`ID`);
alter table crc_filesystem_config 
     add index crc_filesystem_configIFKC (`ID`), 
     add constraint crc_filesystem_configIFKC 
     foreign key (`ID`) 
     references crc_connector_config (`ID`);
alter table crc_ramconn_config 
     add index crc_ramconn_configIFKC (`ID`), 
     add constraint crc_ramconn_configIFKC 
     foreign key (`ID`) 
     references crc_connector_config (`ID`);
alter table custom_messages 
     add index custom_messages_LANGUAGE_FKC (LANGUAGE_FK), 
     add constraint custom_messages_LANGUAGE_FKC 
     foreign key (LANGUAGE_FK) 
     references md_language (`ID`);
alter table iprange_filter_channel 
     add index iprange_channel_IP_RANGE_FILTC (IP_RANGE_FILTERS_FK), 
     add constraint iprange_channel_IP_RANGE_FILTC 
     foreign key (IP_RANGE_FILTERS_FK) 
     references iprange_filter (`ID`);
alter table iprange_filter_channel 
     add index iprange_filter_CHANNELS_FKC (CHANNELS_FK), 
     add constraint iprange_filter_CHANNELS_FKC 
     foreign key (CHANNELS_FK) 
     references iprange_channel (`TYPE`);
alter table iprange_range 
     add index ip_range_filter_C_ex (IP_RANGE_FILTER_EX_FK), 
     add constraint ip_range_filter_C_ex 
     foreign key (IP_RANGE_FILTER_EX_FK) 
     references iprange_filter (`ID`);
alter table iprange_range 
     add index ip_range_filter_C_in (IP_RANGE_FILTER_IN_FK), 
     add constraint ip_range_filter_C_in 
     foreign key (IP_RANGE_FILTER_IN_FK) 
     references iprange_filter (`ID`);
alter table mc_config 
     add index mc_config_NOTIFICATION_CONFIGC (NOTIFICATION_CONFIG_FK), 
     add constraint mc_config_NOTIFICATION_CONFIGC 
     foreign key (NOTIFICATION_CONFIG_FK) 
     references notification_config (`ID`);
alter table news_widget_feed 
     add index news_widget_feed_KENMEI_USER_C (KENMEI_USER_FK), 
     add constraint news_widget_feed_KENMEI_USER_C 
     foreign key (KENMEI_USER_FK) 
     references user_user (`ID`);
alter table news_widget_parameter 
     add index news_widget_parameter_WIDGET_C (WIDGET_NEWS_FEED_FK), 
     add constraint news_widget_parameter_WIDGET_C 
     foreign key (WIDGET_NEWS_FEED_FK) 
     references news_widget_feed (`ID`);
alter table security_code 
     add index security_code_KENMEI_USER_FKC (KENMEI_USER_FK), 
     add constraint security_code_KENMEI_USER_FKC 
     foreign key (KENMEI_USER_FK) 
     references user_user (`ID`);
alter table security_email_code 
     add index security_email_codeIFKC (`ID`), 
     add constraint security_email_codeIFKC 
     foreign key (`ID`) 
     references security_code (`ID`);
alter table security_forgotten_pw_code 
     add index security_forgotten_pw_codeIFKC (`ID`), 
     add constraint security_forgotten_pw_codeIFKC 
     foreign key (`ID`) 
     references security_code (`ID`);
alter table security_invite_blog 
     add index security_invite_blogIFKC (`ID`), 
     add constraint security_invite_blogIFKC 
     foreign key (`ID`) 
     references security_code (`ID`);
alter table security_invite_client 
     add index security_invite_clientIFKC (`ID`), 
     add constraint security_invite_clientIFKC 
     foreign key (`ID`) 
     references security_code (`ID`);
alter table security_user_auth_failed_status 
     add index security_user_auth_failed_staC (KENMEI_USER_FK), 
     add constraint security_user_auth_failed_staC 
     foreign key (KENMEI_USER_FK) 
     references user_user (`ID`);
alter table security_user_code 
     add index security_user_codeIFKC (`ID`), 
     add constraint security_user_codeIFKC 
     foreign key (`ID`) 
     references security_code (`ID`);
alter table security_user_unlock_code 
     add index security_user_unlock_codeIFKC (`ID`), 
     add constraint security_user_unlock_codeIFKC 
     foreign key (`ID`) 
     references security_code (`ID`);
alter table snc_config 
     add index snc_config_USER_PROFILE_FKC (USER_PROFILE_FK), 
     add constraint snc_config_USER_PROFILE_FKC 
     foreign key (USER_PROFILE_FK) 
     references user_profile (`ID`);
alter table snc_job_details 
     add index snc_job_details_SOCIAL_NETWORC (SOCIAL_NETWORK_CONFIGURATIO_FK), 
     add constraint snc_job_details_SOCIAL_NETWORC 
     foreign key (SOCIAL_NETWORK_CONFIGURATIO_FK) 
     references snc_config (`ID`);
alter table user_authorities 
     add index user_authorities_KENMEI_USER_C (KENMEI_USER_FK), 
     add constraint user_authorities_KENMEI_USER_C 
     foreign key (KENMEI_USER_FK) 
     references user_user (`ID`);
alter table user_contact 
     add index user_contact_COUNTRY_FKC (COUNTRY_FK), 
     add constraint user_contact_COUNTRY_FKC 
     foreign key (COUNTRY_FK) 
     references md_country (`ID`);
alter table user_entity 
     add index user_entity_GLOBAL_ID_FKC (GLOBAL_ID_FK), 
     add constraint user_entity_GLOBAL_ID_FKC 
     foreign key (GLOBAL_ID_FK) 
     references core_global_id (`ID`);
alter table user_external_auth 
     add index user_external_auth_KENMEI_USEC (KENMEI_USER_FK), 
     add constraint user_external_auth_KENMEI_USEC 
     foreign key (KENMEI_USER_FK) 
     references user_user (`ID`);
alter table user_group 
     add index user_groupIFKC (`ID`), 
     add constraint user_groupIFKC 
     foreign key (`ID`) 
     references user_entity (`ID`);
alter table user_group_external 
     add index user_group_externalIFKC (`ID`), 
     add constraint user_group_externalIFKC 
     foreign key (`ID`) 
     references user_group (`ID`);
alter table user_group_member 
     add index user_group_GROUP_MEMBERS_FKC (GROUP_MEMBERS_FK), 
     add constraint user_group_GROUP_MEMBERS_FKC 
     foreign key (GROUP_MEMBERS_FK) 
     references user_entity (`ID`);
alter table user_group_member 
     add index user_entity_GROUPS_FKC (GROUPS_FK), 
     add constraint user_entity_GROUPS_FKC 
     foreign key (GROUPS_FK) 
     references user_group (`ID`);
alter table user_of_group 
     add index user_of_group_GROUP_FKC (GROUP_FK), 
     add constraint user_of_group_GROUP_FKC 
     foreign key (GROUP_FK) 
     references user_group (`ID`);
alter table user_of_group 
     add index user_of_group_USER_FKC (USER_FK), 
     add constraint user_of_group_USER_FKC 
     foreign key (USER_FK) 
     references user_user (`ID`);
alter table user_profile 
     add index user_profile_SMALL_IMAGE_FKC (SMALL_IMAGE_FK), 
     add constraint user_profile_SMALL_IMAGE_FKC 
     foreign key (SMALL_IMAGE_FK) 
     references user_image (`ID`);
alter table user_profile 
     add index user_profile_CONTACT_FKC (CONTACT_FK), 
     add constraint user_profile_CONTACT_FKC 
     foreign key (CONTACT_FK) 
     references user_contact (`ID`);
alter table user_profile 
     add index user_profile_NOTIFICATION_CONC (NOTIFICATION_CONFIG_FK), 
     add constraint user_profile_NOTIFICATION_CONC 
     foreign key (NOTIFICATION_CONFIG_FK) 
     references notification_config (`ID`);
alter table user_profile 
     add index user_profile_MEDIUM_IMAGE_FKC (MEDIUM_IMAGE_FK), 
     add constraint user_profile_MEDIUM_IMAGE_FKC 
     foreign key (MEDIUM_IMAGE_FK) 
     references user_image (`ID`);
alter table user_profile 
     add index user_profile_LARGE_IMAGE_FKC (LARGE_IMAGE_FK), 
     add constraint user_profile_LARGE_IMAGE_FKC 
     foreign key (LARGE_IMAGE_FK) 
     references user_image (`ID`);
alter table user_user 
     add index user_userIFKC (`ID`), 
     add constraint user_userIFKC 
     foreign key (`ID`) 
     references user_entity (`ID`);
alter table user_user 
     add index user_user_PROFILE_FKC (PROFILE_FK), 
     add constraint user_user_PROFILE_FKC 
     foreign key (PROFILE_FK) 
     references user_profile (`ID`);
-- 
-- add changesets to synchronize changelog
--
INSERT INTO `DATABASECHANGELOG` (`ID`, `AUTHOR`, `FILENAME`, `DATEEXECUTED`, `MD5SUM`, `DESCRIPTION`, `COMMENTS`, `TAG`, `LIQUIBASE`) VALUES
('reset_checksum_001', 'unknown', 'de/communardo/kenmei/database/update/db.changelog.default.xml', NOW(), '6c50cd6ee695161d8f569de01f98956', 'Custom SQL', 'Reset Checksums', NULL, '1.9.2'),
('mt2859_2', 'unknown', 'de/communardo/kenmei/database/update/v1.0.3/db.changelog.v1.0.3.xml', NOW(), 'b2e0e51b2fdd768127d43d458e21d939', 'Add Unique Constraint', '', NULL, '1.9.2'),
('mt2846_global_id_v2_mysql', 'unknown', 'de/communardo/kenmei/database/update/v1.1/db.changelog.v1.1.refactor.xml', NOW(), 'c0cbabe0cea6e8c89d81b7169f92e414', 'Insert Row', 'MT 2846: CR 119 - Refactoring Post Backend: Global Id', NULL, '1.9.2'),
('mt2846_global_id_checksum_v2', 'unknown', 'de/communardo/kenmei/database/update/v1.1/db.changelog.v1.1.refactor.xml', NOW(), '304f2354db312562949c37255a17bf', 'Custom SQL', 'MT 2846: CR 119 - Refactoring Post Backend: Global Id, Configuration, Checksum', NULL, '1.9.2'),
('mt2846_user_external_auth', 'unknown', 'de/communardo/kenmei/database/update/v1.1/db.changelog.v1.1.refactor.xml', NOW(), '6c8c9284a09b847b41d87b11499cf4b', 'Rename Table, Rename Column, Drop Foreign Key Constraint, Add Foreign Key Constraint', 'MT 2846: CR 119 - Refactoring Post Backend - Rename external user auth', NULL, '1.9.2'),
('mt2846_remove_module_status', 'unknown', 'de/communardo/kenmei/database/update/v1.1/db.changelog.v1.1.refactor.xml', NOW(), 'c616cb81c99fea1e6427599e6cec5187', 'Drop Column (x4)', 'MT 2846: CR 119 - Refactoring Post Backend - Remove module status', NULL, '1.9.2'),
('mt2846_mysql_1', 'unknown', 'de/communardo/kenmei/database/update/v1.1/mysql/db.changelog.v1.1.refactor.mysql.xml', NOW(), '30d723e996f2fe7b25fddc235b2dcfcb', 'Drop Table (x6)', 'MT 2846: CR 119 - Refactoring Post Backend - MySql', NULL, '1.9.2'),
('mt2846_mysql_2', 'unknown', 'de/communardo/kenmei/database/update/v1.1/mysql/db.changelog.v1.1.refactor.mysql.xml', NOW(), 'a66cd5a4ddf1ad5b2ee81c0574673bd', 'Drop Foreign Key Constraint (x9)', 'MT 2846: CR 119 - Refactoring Post Backend - MySql', NULL, '1.9.2'),
('mt2846_mysql_3', 'unknown', 'de/communardo/kenmei/database/update/v1.1/mysql/db.changelog.v1.1.refactor.mysql.xml', NOW(), '86c7a598426a561fc898d3bd5930a3e6', 'Create Table', 'MT 2846: CR 119 - Refactoring Post Backend - MySql', NULL, '1.9.2'),
('mt2846_mysql_4', 'unknown', 'de/communardo/kenmei/database/update/v1.1/mysql/db.changelog.v1.1.refactor.mysql.xml', NOW(), '59f5a1279fabc9ed2a3c7f97edad36b8', 'Create Table', 'MT 2846: CR 119 - Refactoring Post Backend - MySql', NULL, '1.9.2'),
('mt2846_mysql_4a', 'unknown', 'de/communardo/kenmei/database/update/v1.1/mysql/db.changelog.v1.1.refactor.mysql.xml', NOW(), '774a36dba4aff22ffbdf3bcf16ed6f', 'SQL From File', 'MT 2846: CR 119 - Refactoring Post Backend - MySql', NULL, '1.9.2'),
('mt2846_mysql_5', 'unknown', 'de/communardo/kenmei/database/update/v1.1/mysql/db.changelog.v1.1.refactor.mysql.xml', NOW(), '937b3863b7ea9f194231bd2dafff6832', 'Drop Column (x7)', 'MT 2846: CR 119 - Refactoring Post Backend - MySql', NULL, '1.9.2'),
('mt2846_mysql_6', 'unknown', 'de/communardo/kenmei/database/update/v1.1/mysql/db.changelog.v1.1.refactor.mysql.xml', NOW(), 'a669428a7b42a371372f4e08556acb', 'SQL From File', 'MT 2846: CR 119 - Refactoring Post Backend - MySql', NULL, '1.9.2'),
('mt2846_mysql_7', 'unknown', 'de/communardo/kenmei/database/update/v1.1/mysql/db.changelog.v1.1.refactor.mysql.xml', NOW(), 'f431f661f875edb7a4c7122b691768', 'Drop Column, Rename Table, Add Not-Null Constraint', 'MT 2846: CR 119 - Refactoring Post Backend - MySql', NULL, '1.9.2'),
('mt2846_mysql_8', 'unknown', 'de/communardo/kenmei/database/update/v1.1/mysql/db.changelog.v1.1.refactor.mysql.xml', NOW(), '44a6dfe46a8feb68eaece96d1ffd7ddf', 'SQL From File', 'MT 2846: CR 119 - Refactoring Post Backend - MySql', NULL, '1.9.2'),
('mt2846_mysql_9', 'unknown', 'de/communardo/kenmei/database/update/v1.1/mysql/db.changelog.v1.1.refactor.mysql.xml', NOW(), 'a8626d4dafc03b63fc2a64ba9cacfc9c', 'Rename Table', 'MT 2846: CR 119 - Refactoring Post Backend - MySql', NULL, '1.9.2'),
('mt2846_mysql_10', 'unknown', 'de/communardo/kenmei/database/update/v1.1/mysql/db.changelog.v1.1.refactor.mysql.xml', NOW(), 'cf28a7b028c31c85bc9ddeec98c856fc', 'Add Column (x7), Rename Column', 'MT 2846: CR 119 - Refactoring Post Backend - MySql', NULL, '1.9.2'),
('mt2846_mysql_11', 'unknown', 'de/communardo/kenmei/database/update/v1.1/mysql/db.changelog.v1.1.refactor.mysql.xml', NOW(), 'e3397a39ece50c8ba1ae089f831ac4b', 'SQL From File', 'MT 2846: CR 119 - Refactoring Post Backend - MySql', NULL, '1.9.2'),
('mt2846_mysql_12', 'unknown', 'de/communardo/kenmei/database/update/v1.1/mysql/db.changelog.v1.1.refactor.mysql.xml', NOW(), '8f569acbd4f4184a409f268fc9cb46be', 'Add Not-Null Constraint (x3), Drop Table (x2)', 'MT 2846: CR 119 - Refactoring Post Backend - MySql', NULL, '1.9.2'),
('mt2846_mysql_13', 'unknown', 'de/communardo/kenmei/database/update/v1.1/mysql/db.changelog.v1.1.refactor.mysql.xml', NOW(), '172934a379fa3cf0a5cf9ee1116b1', 'Rename Table', 'MT 2846: CR 119 - Refactoring Post Backend - MySql', NULL, '1.9.2'),
('mt2846_mysql_14', 'unknown', 'de/communardo/kenmei/database/update/v1.1/mysql/db.changelog.v1.1.refactor.mysql.xml', NOW(), '53b64a5a1bb0c3235c619ce40ac1e5', 'Rename Column, Add Column (x3), Drop Table, Drop Column', 'MT 2846: CR 119 - Refactoring Post Backend - MySql', NULL, '1.9.2'),
('mt2846_mysql_15', 'unknown', 'de/communardo/kenmei/database/update/v1.1/mysql/db.changelog.v1.1.refactor.mysql.xml', NOW(), 'a15f2a38d192f1ea28297d763382923', 'Custom SQL', 'MT 2846: CR 119 - Refactoring Post Backend - MySql', NULL, '1.9.2'),
('mt2846_mysql_16', 'unknown', 'de/communardo/kenmei/database/update/v1.1/mysql/db.changelog.v1.1.refactor.mysql.xml', NOW(), 'f4f35c79c8f9cbdfa6fe278a2d1b0e', 'Rename Table', 'MT 2846: CR 119 - Refactoring Post Backend - MySql', NULL, '1.9.2'),
('mt2846_mysql_17', 'unknown', 'de/communardo/kenmei/database/update/v1.1/mysql/db.changelog.v1.1.refactor.mysql.xml', NOW(), '2c781dee109292b6fd35f10b820c9e0', 'Rename Column (x5), Drop Column (x4)', 'MT 2846: CR 119 - Refactoring Post Backend - MySql', NULL, '1.9.2'),
('mt2846_mysql_18', 'unknown', 'de/communardo/kenmei/database/update/v1.1/mysql/db.changelog.v1.1.refactor.mysql.xml', NOW(), 'd41d8cd98f0b24e980998ecf8427e', 'Empty', 'MT 2846: CR 119 - Refactoring Post Backend - MySql', NULL, '1.9.2'),
('mt2846_mysql_19', 'unknown', 'de/communardo/kenmei/database/update/v1.1/mysql/db.changelog.v1.1.refactor.mysql.xml', NOW(), '9d3c6d7f42f3efccf7973fc8135bde5', 'Add Foreign Key Constraint (x3), Add Unique Constraint (x3), Add Foreign Key Constraint (x11), Add Unique Constraint, Drop Foreign Key Constraint, Add Foreign Key Constraint (x3), Add Unique Constraint, Add Foreign Key Constraint', 'MT 2846: CR 119 - Refactoring Post Backend - MySql', NULL, '1.9.2'),
('mt2846_mysql_20', 'unknown', 'de/communardo/kenmei/database/update/v1.1/mysql/db.changelog.v1.1.refactor.mysql.xml', NOW(), 'c59f2b28cf96874373cfeacffc881b4', 'SQL From File', 'MT 2846: CR 119 - Refactoring Post Backend - MySql', NULL, '1.9.2'),
('mt2846_favorite_users', 'unknown', 'de/communardo/kenmei/database/update/v1.1/db.changelog.v1.1.refactor.xml', NOW(), '9f4a84f23eb8c55a4730335d35c3ab7f', 'Rename Table', 'MT 2846: CR 119 - Refactoring Post Backend - Favorite users', NULL, '1.9.2'),
('20091007_mysql', 'unknown', 'de/communardo/kenmei/database/update/v1.0.3/db.changelog.v1.0.3.xml', NOW(), '89652831b7f99da19387e93f72471b', 'Custom SQL', '', NULL, '1.9.2'),
('mt2899_mysql_v3', 'unknown', 'de/communardo/kenmei/database/update/v1.0.3/db.changelog.v1.0.3.xml', NOW(), '2386128fd9dcb86b8fa469f8ff2ec81', 'Custom SQL', 'MT 2899: Image caching problems', NULL, '1.9.2'),
('mt2940_1', 'unknown', 'de/communardo/kenmei/database/update/v1.1/db.changelog.v1.1.xml', NOW(), 'b962fa5b9ddcecbc21d62b4999d8764', 'Update Data, Create Table, Add Foreign Key Constraint (x2)', 'CR 131: autosave refactoring', NULL, '1.9.2'),
('mt2945_1', 'unknown', 'de/communardo/kenmei/database/update/v1.1/db.changelog.v1.1.xml', NOW(), '22b680833818a22cf3f5e695a7b185d', 'Add Column, Add Foreign Key Constraint', 'CR 109: Post without selecting a blog', NULL, '1.9.2'),
('mt2957_1', 'unknown', 'de/communardo/kenmei/database/update/v1.1/db.changelog.v1.1.xml', NOW(), '2a75e566753c4ffb3f42c5fb9896cd', 'Add Column (x2)', 'CR 122 - Threaded answers', NULL, '1.9.2'),
('mt2957_2_mysql', 'unknown', 'de/communardo/kenmei/database/update/v1.1/db.changelog.v1.1.xml', NOW(), '39dd942ccd10338659ba1bfdb94f9183', 'Custom SQL', 'CR 122 - Threaded answers', NULL, '1.9.2'),
('mt2976_1', 'unknown', 'de/communardo/kenmei/database/update/v1.1/db.changelog.v1.1.xml', NOW(), 'dcd4c199fd6bf5cd3ca66a34a84afa7', 'Add Column (x3)', 'Content Type', NULL, '1.9.2'),
('mt2976_2', 'unknown', 'de/communardo/kenmei/database/update/v1.1/db.changelog.v1.1.xml', NOW(), '6b7314e763c81f1f96c67deb8d806d69', 'Custom SQL', 'Attachment Status', NULL, '1.9.2'),
('rename_default_blog_alias_1_mysql', 'unknown', 'de/communardo/kenmei/database/update/v1.1/db.changelog.v1.1.xml', NOW(), 'aab6b129c4acfe2705da4d8d127dd7b', 'Custom SQL', 'Renames the blog alias of the default blog, if an alias ''public'' not exists.', NULL, '1.9.2'),
('mt3022_mysql', 'unknown', 'de/communardo/kenmei/database/update/v1.1/db.changelog.v1.1.xml', NOW(), 'a4c8beb7d79b2eb7c18c2d883f2eb3c', 'Custom SQL', 'Increase Repository Limit', NULL, '1.9.2'),
('rename_default_blog_alias_2_mysql', 'unknown', 'de/communardo/kenmei/database/update/v1.1/db.changelog.v1.1.xml', NOW(), 'b38717f58c847a3c1896dd7425ef50', 'Custom SQL', 'Renames the blog alias of the default blog, if it is still the message key.', NULL, '1.9.2'),
('mt3096_1', 'unknown', 'de/communardo/kenmei/database/update/v1.1/db.changelog.v1.1.xml', NOW(), 'abcc6ad563565e4050381273ef3fcc5d', 'Custom SQL', 'deletes unconnected large user images', NULL, '1.9.2'),
('mt3178_1_mysql', 'unknown', 'de/communardo/kenmei/database/update/v1.1/db.changelog.v1.1.xml', NOW(), 'dd97e4a211a78df2d24fbda9552922b', 'Insert Row', 'Automated Client Approval', NULL, '1.9.2'),
('mt3187_1', 'unknown', 'de/communardo/kenmei/database/update/v1.1/db.changelog.v1.1.xml', NOW(), '77fa5d42e90366abddbbb7dbb94e61d', 'Drop Foreign Key Constraint', 'Adjust the forein key constraint for kenmei_users_fk.', NULL, '1.9.2'),
('mt3187_2', 'unknown', 'de/communardo/kenmei/database/update/v1.1/db.changelog.v1.1.xml', NOW(), '1b315757661762a2659fde38c03259', 'Add Foreign Key Constraint', 'Adjust the forein key constraint for kenmei_users_fk.', NULL, '1.9.2'),
('mt3187_3', 'unknown', 'de/communardo/kenmei/database/update/v1.1/db.changelog.v1.1.xml', NOW(), '77fa5d42e90366abddbbb7dbb94e61d', 'Drop Foreign Key Constraint', 'Adjust the forein key constraint for kenmei_users_fk.', NULL, '1.9.2'),
('mt3187_4_mysql', 'unknown', 'de/communardo/kenmei/database/update/v1.1/db.changelog.v1.1.xml', NOW(), '925ad9965ef8812edba0b6ba81d9629', 'Custom SQL, Add Foreign Key Constraint', 'Adjust the forein key constraint for kenmei_users_fk.', NULL, '1.9.2'),
('mt3196_1', 'unknown', 'de/communardo/kenmei/database/update/v1.1.2/db.changelog.v1.1.2.xml', NOW(), '3f4f52f46b355cec745a24706cca46d4', 'Add Column', 'CR 100 - Support Time Zones: add new column to user_profile', NULL, '1.9.2'),
('mt3196_2', 'unknown', 'de/communardo/kenmei/database/update/v1.1.2/db.changelog.v1.1.2.xml', NOW(), 'dca47279adc3bbd1ad7e1d7174e10b1', 'Add Column (x2)', 'CR 100 - Support Time Zones: add new column to user_client_creation, configuration_client', NULL, '1.9.2'),
('mt3208', 'unknown', 'de/communardo/kenmei/database/update/v1.1.2/db.changelog.v1.1.2.xml', NOW(), 'a5e3666944ac3f968080ff852bd777d3', 'Add Column', 'CR 68 Read-More functionality', NULL, '1.9.2'),
('mt3277_1_mysql', 'unknown', 'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', NOW(), '2ea8e41788851b5f4a58846e05bcc82', 'Create Table, Custom SQL, Drop Column, Add Foreign Key Constraint (x2)', 'CR 96 - support for groups: add user_entity and copy users', NULL, '1.9.2'),
('mt3277_2_mysql', 'unknown', 'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', NOW(), '88fa38c4a5f8ae66e1cbae3414dfccf', 'Drop Table (x2)', 'CR 96 - support for groups: remove obsolete security codes', NULL, '1.9.2'),
('mt3277_3_mysql', 'unknown', 'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', NOW(), 'f595d3d3bb28aa16d7567246a3a4585', 'Add Column, Custom SQL', 'CR 96 - support for groups: copy all_can_x rights from group to blog', NULL, '1.9.2'),
('mt3277_4_mysql', 'unknown', 'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', NOW(), '25c695869c8f851fd412ca90db5b2677', 'Add Column, Custom SQL', 'CR 96 - support for groups: add blogs to group member', NULL, '1.9.2'),
('mt3277_5_mysql', 'unknown', 'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', NOW(), 'ce8baa4707c58ddfba1523c015deb0', 'Drop Foreign Key Constraint (x2), Drop Column (x2), Custom SQL, Rename Table, Rename Column (x2), Add Column, Add Foreign Key Constraint (x2), Add Not-Null Constraint (x3)', 'CR 96 - support for groups: group member to blog member', NULL, '1.9.2'),
('mt3277_6_mysql', 'unknown', 'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', NOW(), 'da4346ff7575e6ebf6b3cd1266164', 'Drop Foreign Key Constraint, Drop Column', 'CR 96 - support for groups: cleanup core_blog', NULL, '1.9.2'),
('mt3277_7_mysql', 'unknown', 'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', NOW(), '3e47ee3a105c684a582e423518decca', 'Drop Column (x3), Delete Data, Add Column (x3), Add Foreign Key Constraint', 'CR 96 - support for groups: fix user_group', NULL, '1.9.2'),
('mt3277_8_mysql', 'unknown', 'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', NOW(), '9a394f311e01bce782f7d562045c46b', 'Create Table', 'CR 96 - support for groups: add helper table for fast blog role lookup', NULL, '1.9.2'),
('mt3277_9_mysql', 'unknown', 'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', NOW(), 'ae6438d32dde3f4c6124ca8330b5ec6', 'Custom SQL', 'CR 96 - support for groups: fill core_role2blog', NULL, '1.9.2'),
('mt3277_10_mysql', 'unknown', 'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', NOW(), 'fa346348fef8d167f2390c9b247f053', 'Create Table, Add Primary Key, Add Foreign Key Constraint (x2)', 'CR 96 - support for groups: allow several granting groups', NULL, '1.9.2'),
('mt3277_11_mysql', 'unknown', 'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', NOW(), 'c83fd6a7d42279a72d55ededfcfa4d86', 'Create Table, Add Primary Key, Add Foreign Key Constraint (x2)', 'CR 96 - support for groups: connect entity with group', NULL, '1.9.2'),
('mt3277_13_mysql', 'unknown', 'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', NOW(), '4691cf11e2e377d7897b8abdecc6d88d', 'Add Not-Null Constraint (x2)', 'CR 96 - support for groups: not null constraints for all_can_x rights', NULL, '1.9.2'),
('mt3281_1_mysql', 'unknown', 'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', NOW(), '3ebca4821f7296e11d97c5913fe8707c', 'Insert Row', 'CR 134 - Anonymous Access, Anonymous User', NULL, '1.9.2'),
('mt3281_2_mysql', 'unknown', 'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', NOW(), 'f4972baac8a2f4504d77fd85c7d15e9', 'Add Column, Add Not-Null Constraint', 'CR 134 - Anonymous Access, Anonymous User', NULL, '1.9.2'),
('cr135', 'amo', 'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', NOW(), 'a02a9368d39aadb2b9affeff80229c', 'Create Table, Add Foreign Key Constraint', 'CR 135 - User group synchronization', NULL, '1.9.2'),
('mt3283_external_configuration', 'unknown', 'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', NOW(), 'c2c4eb85364225670b85647601134e0', 'Rename Table (x3), Rename Column, Add Column, Drop Foreign Key Constraint, Add Foreign Key Constraint', 'Refactor External Authentication', NULL, '1.9.2'),
('mt3283_confluence_configuration', 'unknown', 'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', NOW(), '5029a38aca9b7c4c25a4f41f619caa1c', 'Add Column (x2)', '', NULL, '1.9.2'),
('mt3283_external_objects', 'unknown', 'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', NOW(), '75db72d38b92ade15a93f1b854b9048', 'Create Table, Add Unique Constraint, Create Table, Add Foreign Key Constraint (x2), Add Column', '', NULL, '1.9.2'),
('mt3283_2_mysql', 'unknown', 'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', NOW(), '22711a3246654a434873c0e1fbf78119', 'Set Column as Auto-Increment, Add Not-Null Constraint', 'Refactor External Authentication', NULL, '1.9.2'),
('mt3283_external_objects_2', 'unknown', 'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', NOW(), 'ff7ed34175992ead7a175351c0d4e9', 'Add Column, Add Not-Null Constraint', 'Add class to blog_member', NULL, '1.9.2'),
('mt3292_confluence_permission_url', 'unknown', 'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', NOW(), '21a7986f567bd1c385abbdfead1e8c3', 'Add Column', 'CR 135 - Support Synchronization of blog rights with external systems', NULL, '1.9.2'),
('cr135_1', 'unknown', 'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', NOW(), '318b839a3fff783a8f4a4a3e7f5866', 'Add Column', 'Add serviceUrl to confluence configuration', NULL, '1.9.2'),
('mt3283_external_objects_3', 'unknown', 'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', NOW(), 'd3cc6233f8abccd7d6c2eb5017cf77', 'Add Column', '', NULL, '1.9.2'),
('mt3277_fix_null_constraint', 'unknown', 'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', NOW(), '97f2d0c8945bae1c69f8f827c57e73', 'Drop Not-Null Constraint', 'Drop wrong "not null" constraint from user entities.', NULL, '1.9.2'),
('mt3178_2_mysql', 'unknown', 'de/communardo/kenmei/database/update/v1.1/db.changelog.v1.1.xml', NOW(), 'd1808dcb3e616cfff590fbbdf8494f', 'Insert Row', 'Automated Client Approval', NULL, '1.9.2'),
('set_configuration', 'unknown', 'de/communardo/kenmei/database/update/db.changelog.final.xml', NOW(), '6148c09f2e75f3239da193221c5cedca', 'Custom SQL', 'Assign the configuration FK', NULL, '1.9.2'),
('mt3283_external_objects_fix_key_unique_mysql', 'unknown', 'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', NOW(), 'c2294436992eb88562f385fe26135b0', 'Drop Primary Key, Add Column', 'CR 135 - Support Synchronization of blog rights with external systems', NULL, '1.9.2'),
('mt3277_14', 'unknown', 'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', NOW(), 'e694eb5228924a36a25357646af454e4', 'Add Column, Custom SQL, Add Not-Null Constraint', 'CR 96 - support for groups: grantedByGroup flag for mapping table', NULL, '1.9.2'),
('mt3350_configuration_column_ldap', 'unknown', 'de/communardo/kenmei/database/update/v1.1.2/db.changelog.v1.1.2.xml', NOW(), 'dd10f32f1dfab6778b0c3eb60acd4ba', 'Drop Column', 'MT 3350', NULL, '1.9.2'),
('phone_and_fax_country_code_fix_mysql', 'unknown', 'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', NOW(), '89e69368ec3f98f683218ccbb47340', 'Custom SQL', '', NULL, '1.9.2'),
('mt_3272_1_mysql', 'unknown', 'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', NOW(), 'f421adb0107c9087be3d4be78a9086f3', 'Custom SQL', 'MT 3272: UserProfile saves space instead of NULL if no time zone is selected', NULL, '1.9.2'),
('phone_country_code_fix__client_mysql', 'unknown', 'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', NOW(), '84e1be9cc8d1aa2f974526d73f3cf9e7', 'Custom SQL', '', NULL, '1.9.2'),
('cr69_user_groups_v2', 'unknown', 'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', NOW(), '41e53effcad9d4fe485b75dab0922bf1', 'Rename Column', '', NULL, '1.9.2'),
('mt_3314_refactor_confluence_page', 'unknown', 'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', NOW(), '86b2b96eee76c0d6bdaad4e1c24b5b25', 'Add Column', '', NULL, '1.9.2'),
('mt3283_external_objects_fix_auto_increment_mysql', 'unknown', 'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', NOW(), 'c7f24c22f84e8eff695ba917c8fd6d7', 'Custom SQL', 'CR 135 - Support Synchronization of blog rights with external systems', NULL, '1.9.2'),
('mt3329_1_mysql', 'unknown', 'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', NOW(), '836a30af94fb53cfe359ea11fe4f62fe', 'Create Table, Custom SQL, Add Foreign Key Constraint (x2)', 'CR 96 - support for hierarchical groups: add user_of_group and copy users', NULL, '1.9.2'),
('mt3350_configuration_column_source', 'unknown', 'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', NOW(), '12af7efa26d7a9eefaedfcd1ad9e2d24', 'Drop Column', 'MT 3350', NULL, '1.9.2'),
('confluence_set_basepath_mysql', 'unknown', 'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', NOW(), '8dab40b53251883889955704653be51', 'Custom SQL', '', NULL, '1.9.2'),
('remove_uk_country_code', 'unknown', 'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', NOW(), 'a8a46d136dea83ec4bdfffd11c9b065', 'Custom SQL', '', NULL, '1.9.2'),
('cr_179_add_is_Html_column_v2', 'unknown', 'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', NOW(), '51c815f15bbb47cb1990c3f556b2fe', 'Add Column, Add Not-Null Constraint', '', NULL, '1.9.2'),
('cr_179_add_messages_table_v2', 'unknown', 'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', NOW(), '51bf6b30763f19d84b1ba2aeac1fd14', 'Create Table, Add Foreign Key Constraint, Add Unique Constraint', '', NULL, '1.9.2'),
('cr_179_add_messages_table_fix_key_column', 'unknown', 'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', NOW(), 'db6273f493f2c16fa8c63f1a43ea57', 'Modify Column', '', NULL, '1.9.2'),
('cr_179_insert_default_values_for_imprint_terms_of_use_mysql', 'unknown', 'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', NOW(), 'c7e584ebae6171b861bd5a37f4be42', 'Custom SQL', '', NULL, '1.9.2'),
('jt1528_convert_core_note_to_innoDB', 'unknown', 'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', NOW(), '5c59d4c1140af9231be2cdf42c265d', 'Custom SQL', 'Change engine of core_note to innoDB', NULL, '1.9.2'),
('KENMEI-1566-Remove_MySQL_Timestamp_Trigger', 'unknown', 'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', NOW(), 'a4fb25b5e12f2ab2519c961d47707617', 'Custom SQL', '', NULL, '1.9.2'),
('KENMEI-1533_Erzeugung_Global_ID_fehlerhaft-MySQL', 'unknown', 'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', NOW(), '5a199146dfd883297b1bfdb6ef6f517', 'SQL From File', '', NULL, '1.9.2');