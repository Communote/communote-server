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

--    alter table core_tag2clearance_exclude 
--        add index core_tag_clearance_EXCLUDE_TAC (EXCLUDE_TAGS_FK), 
--        add constraint core_tag_clearance_EXCLUDE_TAC 
--        foreign key (EXCLUDE_TAGS_FK) 
--        references core_tag (`ID`);

    alter table core_tag2clearance_include 
        add index core_tag_clearance_INCLUDE_TAC (INCLUDE_TAGS_FK), 
        add constraint core_tag_clearance_INCLUDE_TAC 
        foreign key (INCLUDE_TAGS_FK) 
        references core_tag (`ID`);

    alter table core_tag2clearance_include 
        add index core_tag_TAG_CLEARANCES_FKC (TAG_CLEARANCES_FK), 
        add constraint core_tag_TAG_CLEARANCES_FKC 
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

    alter table user_client 
        add index user_client_CREATION_DATA_FKC (CREATION_DATA_FK), 
        add constraint user_client_CREATION_DATA_FKC 
        foreign key (CREATION_DATA_FK) 
        references user_client_creation (`ID`);

    alter table user_client 
        add index user_client_CLIENT_PROFILE_FKC (CLIENT_PROFILE_FK), 
        add constraint user_client_CLIENT_PROFILE_FKC 
        foreign key (CLIENT_PROFILE_FK) 
        references user_client_profile (`ID`);

    alter table user_client 
        add index user_client_CLIENT_D_B_CONFIGC (CLIENT_D_B_CONFIG_FK), 
        add constraint user_client_CLIENT_D_B_CONFIGC 
        foreign key (CLIENT_D_B_CONFIG_FK) 
        references user_client_db_config (`ID`);

    alter table user_client_creation 
        add index user_client_creation_CLIENT_AC (CLIENT_ACTIVATION_CODE_FK), 
        add constraint user_client_creation_CLIENT_AC 
        foreign key (CLIENT_ACTIVATION_CODE_FK) 
        references user_client_activation_code (`ID`);

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
