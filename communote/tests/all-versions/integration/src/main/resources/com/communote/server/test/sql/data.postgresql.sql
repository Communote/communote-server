--
-- PostgreSQL database dump
--

SET statement_timeout = 0;
SET lock_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;

SET search_path = public, pg_catalog;

SET default_tablespace = '';

SET default_with_oids = false;

--
-- Name: channel_configuration; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE channel_configuration (
    id bigint NOT NULL,
    force_ssl boolean NOT NULL,
    channel_type character varying(1024) NOT NULL
);


--
-- Name: channel_configuration_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE channel_configuration_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: con_test; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE con_test (
    a character(1)
);


--
-- Name: configuration; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE configuration (
    id bigint NOT NULL,
    client_config_fk bigint
);


--
-- Name: configuration_app_setting; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE configuration_app_setting (
    setting_key character varying(255) NOT NULL,
    setting_value text
);


--
-- Name: configuration_client; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE configuration_client (
    id bigint NOT NULL,
    logo_image bytea,
    last_logo_image_modification_d timestamp without time zone,
    time_zone_id character varying(1024),
    default_blog_fk bigint
);


--
-- Name: configuration_client_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE configuration_client_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: configuration_confluence; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE configuration_confluence (
    id bigint NOT NULL,
    authentication_api_url character varying(1024) NOT NULL,
    image_api_url character varying(1024),
    admin_login character varying(1024),
    admin_password character varying(1024),
    service_url character varying(1024),
    permissions_url character varying(1024),
    base_path character varying(1024)
);


--
-- Name: configuration_external_sys; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE configuration_external_sys (
    id bigint NOT NULL,
    allow_external_authentication boolean NOT NULL,
    system_id character varying(50) NOT NULL,
    primary_authentication boolean NOT NULL,
    synchronize_user_groups boolean NOT NULL,
    configuration_fk bigint
);


--
-- Name: configuration_external_sys_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE configuration_external_sys_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: configuration_ldap; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE configuration_ldap (
    id bigint NOT NULL,
    url character varying(1024) NOT NULL,
    manager_password character varying(1024) NOT NULL,
    manager_d_n character varying(1024) NOT NULL,
    group_sync_config_fk bigint,
    user_search_fk bigint NOT NULL,
    user_identifier_is_binary boolean NOT NULL,
    sasl_mode character varying(1024),
    server_domain character varying(255),
    query_prefix character varying(255),
    dynamic_mode boolean DEFAULT false
);


--
-- Name: configuration_ldap_group; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE configuration_ldap_group (
    id bigint NOT NULL,
    member_mode boolean NOT NULL,
    group_identifier_is_binary boolean NOT NULL,
    group_search_fk bigint NOT NULL
);


--
-- Name: configuration_ldap_group_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE configuration_ldap_group_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: configuration_ldap_sbase; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE configuration_ldap_sbase (
    id bigint NOT NULL,
    search_base character varying(1024) NOT NULL,
    search_subtree boolean NOT NULL,
    ldap_search_configuration_fk bigint NOT NULL,
    sbase_idx integer
);


--
-- Name: configuration_ldap_sbase_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE configuration_ldap_sbase_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: configuration_ldap_search; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE configuration_ldap_search (
    id bigint NOT NULL,
    search_filter character varying(1024),
    property_mapping character varying(1024) NOT NULL
);


--
-- Name: configuration_ldap_search_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE configuration_ldap_search_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: configuration_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE configuration_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: configuration_setting; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE configuration_setting (
    setting_key character varying(255) NOT NULL,
    setting_value text,
    configuration_fk bigint,
    last_modification_timestamp bigint
);


--
-- Name: core_attachment; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE core_attachment (
    id bigint NOT NULL,
    content_identifier character varying(250) NOT NULL,
    repository_identifier character varying(1024) NOT NULL,
    name character varying(1024) NOT NULL,
    content_type character varying(1024),
    attachment_size bigint,
    status character varying(1024) NOT NULL,
    global_id_fk bigint,
    note_fk bigint,
    uploader_fk bigint,
    upload_date timestamp without time zone
);


--
-- Name: core_attachment_property; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE core_attachment_property (
    id bigint NOT NULL,
    key_group character varying(128) NOT NULL,
    property_key character varying(128) NOT NULL,
    property_value character varying(4000) NOT NULL,
    attachment_fk bigint,
    last_modification_date timestamp with time zone DEFAULT '1983-06-19 04:09:23+02'::timestamp with time zone NOT NULL
);


--
-- Name: core_attachment_property_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE core_attachment_property_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: core_attachment_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE core_attachment_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: core_blog; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE core_blog (
    id bigint NOT NULL,
    title character varying(1024) NOT NULL,
    description text,
    creation_date timestamp without time zone NOT NULL,
    name_identifier character varying(255) NOT NULL,
    last_modification_date timestamp without time zone NOT NULL,
    all_can_read boolean NOT NULL,
    all_can_write boolean NOT NULL,
    public_access boolean NOT NULL,
    global_id_fk bigint,
    create_system_notes boolean NOT NULL,
    toplevel_topic boolean DEFAULT false,
    crawl_last_modification_date timestamp without time zone
);


--
-- Name: core_blog2blog; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE core_blog2blog (
    children_fk bigint NOT NULL,
    parents_fk bigint NOT NULL
);


--
-- Name: core_blog2blog_resolved; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE core_blog2blog_resolved (
    id bigint NOT NULL,
    parent_topic_id bigint NOT NULL,
    child_topic_id bigint NOT NULL,
    topic_path character varying(255) NOT NULL
);


--
-- Name: core_blog2blog_resolved_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE core_blog2blog_resolved_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: core_blog2blog_resolved_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE core_blog2blog_resolved_seq OWNED BY core_blog2blog_resolved.id;


--
-- Name: core_blog2tag; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE core_blog2tag (
    blogs_fk bigint NOT NULL,
    tags_fk bigint NOT NULL
);


--
-- Name: core_blog_event_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE core_blog_event_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: core_blog_member; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE core_blog_member (
    id bigint NOT NULL,
    class character varying(255) NOT NULL,
    role character varying(1024) NOT NULL,
    blog_fk bigint NOT NULL,
    kenmei_entity_fk bigint NOT NULL,
    external_system_id character varying(50)
);


--
-- Name: core_blog_member_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE core_blog_member_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: core_blog_property; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE core_blog_property (
    id bigint NOT NULL,
    key_group character varying(128) NOT NULL,
    property_key character varying(128) NOT NULL,
    property_value character varying(4000) NOT NULL,
    blog_fk bigint,
    last_modification_date timestamp without time zone DEFAULT '1983-06-19 04:09:23+02'::timestamp with time zone NOT NULL
);


--
-- Name: core_blog_property_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE core_blog_property_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: core_blog_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE core_blog_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: core_content; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE core_content (
    id bigint NOT NULL,
    content text NOT NULL,
    short_content text
);


--
-- Name: core_content_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE core_content_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: core_entity2tags; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE core_entity2tags (
    kenmei_entities_fk bigint NOT NULL,
    tags_fk bigint NOT NULL
);


--
-- Name: core_external_object; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE core_external_object (
    id bigint NOT NULL,
    external_system_id character varying(50) NOT NULL,
    external_id character varying(200) NOT NULL,
    external_name character varying(1024),
    blog_fk bigint
);


--
-- Name: core_external_object_prop; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE core_external_object_prop (
    id bigint NOT NULL,
    property_key character varying(128) NOT NULL,
    property_value character varying(4000) NOT NULL,
    external_object_fk bigint,
    key_group character varying(128) NOT NULL,
    last_modification_date timestamp without time zone DEFAULT '1983-06-19 04:08:23+02'::timestamp with time zone NOT NULL
);


--
-- Name: core_external_object_prop_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE core_external_object_prop_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: core_external_object_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE core_external_object_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: core_global_binary_prop; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE core_global_binary_prop (
    id bigint NOT NULL,
    key_group character varying(128) NOT NULL,
    property_key character varying(128) NOT NULL,
    property_value bytea NOT NULL,
    kenmei_entity_group_fk bigint,
    last_modification_date timestamp without time zone DEFAULT '1983-06-19 04:09:23+02'::timestamp with time zone NOT NULL
);


--
-- Name: core_global_binary_prop_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE core_global_binary_prop_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: core_global_id; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE core_global_id (
    id bigint NOT NULL,
    global_identifier character varying(255) NOT NULL
);


--
-- Name: core_global_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE core_global_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: core_note; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE core_note (
    id bigint NOT NULL,
    creation_date timestamp without time zone NOT NULL,
    last_modification_date timestamp without time zone NOT NULL,
    creation_source character varying(1024) NOT NULL,
    direct boolean NOT NULL,
    status character varying(1024) NOT NULL,
    version bigint NOT NULL,
    discussion_path character varying(1024),
    user_fk bigint NOT NULL,
    content_fk bigint NOT NULL,
    global_id_fk bigint,
    blog_fk bigint NOT NULL,
    parent_fk bigint,
    origin_fk bigint,
    discussion_id bigint,
    last_discussion_creation_date timestamp without time zone,
    mention_topic_readers boolean DEFAULT false,
    mention_topic_authors boolean DEFAULT false,
    mention_topic_managers boolean DEFAULT false,
    mention_discussion_authors boolean DEFAULT false,
    crawl_last_modification_date timestamp without time zone
);


--
-- Name: core_note2direct_user; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE core_note2direct_user (
    direct_users_fk bigint NOT NULL,
    direct_notes_fk bigint NOT NULL
);


--
-- Name: core_note2followable; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE core_note2followable (
    notes_fk bigint NOT NULL,
    followable_items_fk bigint NOT NULL
);


--
-- Name: core_note_property; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE core_note_property (
    id bigint NOT NULL,
    key_group character varying(128) NOT NULL,
    property_key character varying(128) NOT NULL,
    property_value character varying(4000) NOT NULL,
    note_fk bigint,
    last_modification_date timestamp without time zone DEFAULT '1983-06-19 04:09:23+02'::timestamp with time zone NOT NULL
);


--
-- Name: core_note_property_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE core_note_property_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: core_note_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE core_note_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: core_notes2crossblogs; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE core_notes2crossblogs (
    notes_fk bigint NOT NULL,
    crosspost_blogs_fk bigint NOT NULL
);


--
-- Name: core_notes2tag; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE core_notes2tag (
    notes_fk bigint NOT NULL,
    tags_fk bigint NOT NULL
);


--
-- Name: core_notes2user_to_notify; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE core_notes2user_to_notify (
    notes_fk bigint NOT NULL,
    users_to_be_notified_fk bigint NOT NULL
);


--
-- Name: core_plugin_properties; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE core_plugin_properties (
    id bigint NOT NULL,
    key_group character varying(128) NOT NULL,
    property_key character varying(128) NOT NULL,
    property_value text NOT NULL,
    last_modification_date timestamp without time zone DEFAULT '1983-06-19 04:08:23+02'::timestamp with time zone NOT NULL,
    application_property boolean DEFAULT false
);


--
-- Name: core_plugin_properties_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE core_plugin_properties_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: core_plugin_properties_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE core_plugin_properties_id_seq OWNED BY core_plugin_properties.id;


--
-- Name: core_plugin_properties_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE core_plugin_properties_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: core_processed_utp_mail; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE core_processed_utp_mail (
    id bigint NOT NULL,
    mail_message_id character varying(255) NOT NULL
);


--
-- Name: core_processed_utp_mail_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE core_processed_utp_mail_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: core_role2blog; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE core_role2blog (
    id bigint NOT NULL,
    blog_id bigint NOT NULL,
    user_id bigint NOT NULL,
    numeric_role integer NOT NULL,
    external_system_id character varying(50),
    granted_by_group boolean NOT NULL
);


--
-- Name: core_role2blog_granting_group; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE core_role2blog_granting_group (
    user_to_blog_role_mappings_fk bigint NOT NULL,
    granting_groups_fk bigint NOT NULL
);


--
-- Name: core_role2blog_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE core_role2blog_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: core_tag; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE core_tag (
    id bigint NOT NULL,
    class character varying(255) NOT NULL,
    tag_store_tag_id character varying(255) NOT NULL,
    default_name character varying(255) NOT NULL,
    global_id_fk bigint,
    category_fk bigint,
    abstract_tag_category_tags_idx integer,
    tag_store_alias character varying(255) DEFAULT 'DefaultNoteTagStore'::character varying NOT NULL
);


--
-- Name: core_tag2descriptions; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE core_tag2descriptions (
    tags_fk bigint NOT NULL,
    descriptions_fk bigint NOT NULL
);


--
-- Name: core_tag2names; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE core_tag2names (
    tags_fk bigint NOT NULL,
    names_fk bigint NOT NULL
);


--
-- Name: core_tag_category; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE core_tag_category (
    id bigint NOT NULL,
    class character varying(255) NOT NULL,
    name character varying(1024) NOT NULL,
    prefix character varying(1024) NOT NULL,
    description character varying(1024),
    multiple_tags boolean NOT NULL
);


--
-- Name: core_tag_category_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE core_tag_category_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: core_tag_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE core_tag_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: core_task; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE core_task (
    id bigint NOT NULL,
    unique_name character varying(255) NOT NULL,
    task_status character varying(255) NOT NULL,
    task_interval bigint,
    handler_class_name character varying(1024) NOT NULL,
    active boolean NOT NULL,
    next_execution timestamp without time zone NOT NULL,
    last_execution timestamp without time zone
);


--
-- Name: core_task_execs; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE core_task_execs (
    id bigint NOT NULL,
    instance_name character varying(1024) NOT NULL,
    task_fk bigint NOT NULL
);


--
-- Name: core_task_execs_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE core_task_execs_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: core_task_props; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE core_task_props (
    id bigint NOT NULL,
    property_key character varying(1024) NOT NULL,
    property_value character varying(1024),
    task_fk bigint
);


--
-- Name: core_task_props_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE core_task_props_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: core_task_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE core_task_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: core_user2follows; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE core_user2follows (
    kenmei_users_fk bigint NOT NULL,
    followed_items_fk bigint NOT NULL
);


--
-- Name: core_users2favorite_notes; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE core_users2favorite_notes (
    favorite_notes_fk bigint NOT NULL,
    favorite_users_fk bigint NOT NULL
);


--
-- Name: crc_cache_config_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE crc_cache_config_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: crc_connector_config_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE crc_connector_config_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: custom_messages; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE custom_messages (
    id bigint NOT NULL,
    message_key character varying(255) NOT NULL,
    message text NOT NULL,
    is_html boolean NOT NULL,
    language_fk bigint NOT NULL
);


--
-- Name: custom_messages_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE custom_messages_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: databasechangelog; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE databasechangelog (
    id character varying(63) NOT NULL,
    author character varying(63) NOT NULL,
    filename character varying(200) NOT NULL,
    dateexecuted timestamp with time zone NOT NULL,
    md5sum character varying(32),
    description character varying(255),
    comments character varying(255),
    tag character varying(255),
    liquibase character varying(10)
);


--
-- Name: databasechangeloglock; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE databasechangeloglock (
    id integer NOT NULL,
    locked boolean NOT NULL,
    lockgranted timestamp with time zone,
    lockedby character varying(255)
);


--
-- Name: iprange_channel; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE iprange_channel (
    type character varying(255) NOT NULL,
    enabled boolean NOT NULL
);


--
-- Name: iprange_filter; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE iprange_filter (
    id bigint NOT NULL,
    name character varying(1024) NOT NULL,
    enabled boolean NOT NULL
);


--
-- Name: iprange_filter_channel; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE iprange_filter_channel (
    ip_range_filters_fk bigint NOT NULL,
    channels_fk character varying(255) NOT NULL
);


--
-- Name: iprange_filter_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE iprange_filter_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: iprange_range; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE iprange_range (
    id bigint NOT NULL,
    start_value bytea NOT NULL,
    end_value bytea NOT NULL,
    string_representation character varying(1024),
    ip_range_filter_in_fk bigint,
    ip_range_filter_ex_fk bigint
);


--
-- Name: iprange_range_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE iprange_range_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: mc_config; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE mc_config (
    id bigint NOT NULL,
    type character varying(1024) NOT NULL,
    properties character varying(1024),
    only_if_available boolean NOT NULL,
    priority integer NOT NULL,
    notification_config_fk bigint
);


--
-- Name: mc_config_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE mc_config_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: md_country; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE md_country (
    id bigint NOT NULL,
    country_code character varying(255) NOT NULL,
    name character varying(1024) NOT NULL
);


--
-- Name: md_country_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE md_country_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: md_language; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE md_language (
    id bigint NOT NULL,
    language_code character varying(255) NOT NULL,
    name character varying(1024) NOT NULL
);


--
-- Name: md_language_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE md_language_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: misc_query_helper_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE misc_query_helper_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: notification_config; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE notification_config (
    id bigint NOT NULL,
    fallback character varying(1024)
);


--
-- Name: notification_config_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE notification_config_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: security_code; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE security_code (
    id bigint NOT NULL,
    code character varying(255) NOT NULL,
    action character varying(1024) NOT NULL,
    creating_date timestamp without time zone NOT NULL,
    kenmei_user_fk bigint
);


--
-- Name: security_code_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE security_code_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: security_email_code; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE security_email_code (
    id bigint NOT NULL,
    new_email_address character varying(1024) NOT NULL
);


--
-- Name: security_forgotten_pw_code; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE security_forgotten_pw_code (
    id bigint NOT NULL
);


--
-- Name: security_invite_blog; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE security_invite_blog (
    id bigint NOT NULL,
    invitor_id bigint
);


--
-- Name: security_invite_client; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE security_invite_client (
    id bigint NOT NULL
);


--
-- Name: security_user_code; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE security_user_code (
    id bigint NOT NULL
);


--
-- Name: security_user_status; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE security_user_status (
    id bigint NOT NULL,
    locked_timeout timestamp without time zone,
    failed_auth_counter integer NOT NULL,
    channel_type character varying(1024) NOT NULL,
    kenmei_user_fk bigint
);


--
-- Name: security_user_status_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE security_user_status_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: security_user_unlock_code; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE security_user_unlock_code (
    id bigint NOT NULL,
    channel character varying(1024) NOT NULL
);


--
-- Name: user_authorities; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE user_authorities (
    id bigint NOT NULL,
    role character varying(1024) NOT NULL,
    kenmei_user_fk bigint
);


--
-- Name: user_authorities_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE user_authorities_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: user_client; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE user_client (
    id bigint NOT NULL,
    client_id character varying(255) NOT NULL,
    name character varying(1024) NOT NULL,
    client_status character varying(1024) NOT NULL,
    creation_version character varying(1024),
    creation_time timestamp without time zone,
    creation_revision character varying(1024)
);


--
-- Name: user_client_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE user_client_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: user_client_statistic; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE user_client_statistic (
    id bigint NOT NULL,
    repository_size bigint NOT NULL
);


--
-- Name: user_client_statistic_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE user_client_statistic_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: user_contact; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE user_contact (
    id bigint NOT NULL,
    street character varying(1024),
    zip character varying(1024),
    city character varying(1024),
    phone_country_code character varying(1024),
    phone_area_code character varying(1024),
    phone_phone_number character varying(1024),
    fax_country_code character varying(1024),
    fax_area_code character varying(1024),
    fax_phone_number character varying(1024),
    country_fk bigint
);


--
-- Name: user_contact_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE user_contact_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: user_entity; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE user_entity (
    id bigint NOT NULL,
    global_id_fk bigint
);


--
-- Name: user_entity_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE user_entity_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: user_external_auth; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE user_external_auth (
    id bigint NOT NULL,
    external_user_id character varying(250) NOT NULL,
    system_id character varying(50) NOT NULL,
    kenmei_user_fk bigint,
    permanent_id character varying(1024),
    additional_property text
);


--
-- Name: user_external_auth_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE user_external_auth_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: user_group; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE user_group (
    id bigint NOT NULL,
    alias character varying(300) NOT NULL,
    name character varying(1024) NOT NULL,
    description text
);


--
-- Name: user_group_external; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE user_group_external (
    id bigint NOT NULL,
    external_system_id character varying(50) NOT NULL,
    external_id character varying(250) NOT NULL,
    additional_property text
);


--
-- Name: user_group_member; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE user_group_member (
    group_members_fk bigint NOT NULL,
    groups_fk bigint NOT NULL
);


--
-- Name: user_group_property; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE user_group_property (
    id bigint NOT NULL,
    key_group character varying(128) NOT NULL,
    property_key character varying(128) NOT NULL,
    property_value character varying(4000) NOT NULL,
    kenmei_entity_group_fk bigint,
    last_modification_date timestamp without time zone DEFAULT '1983-06-19 04:09:23+02'::timestamp with time zone NOT NULL
);


--
-- Name: user_group_property_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE user_group_property_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: user_image; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE user_image (
    id bigint NOT NULL,
    image bytea NOT NULL
);


--
-- Name: user_image_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE user_image_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: user_navigation_item; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE user_navigation_item (
    id bigint NOT NULL,
    item_index integer NOT NULL,
    data text NOT NULL,
    last_access_date timestamp without time zone NOT NULL,
    owner_fk bigint NOT NULL,
    name character varying(255) NOT NULL
);


--
-- Name: user_navigation_item_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE user_navigation_item_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: user_navigation_item_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE user_navigation_item_seq OWNED BY user_navigation_item.id;


--
-- Name: user_note_entity; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE user_note_entity (
    id bigint NOT NULL,
    rank bigint,
    note_fk bigint NOT NULL,
    user_fk bigint NOT NULL
);


--
-- Name: user_note_entity_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE user_note_entity_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: user_note_entity_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE user_note_entity_id_seq OWNED BY user_note_entity.id;


--
-- Name: user_note_entity_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE user_note_entity_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: user_note_property; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE user_note_property (
    id bigint NOT NULL,
    property_key character varying(128) NOT NULL,
    property_value character varying(1024) NOT NULL,
    key_group character varying(128) NOT NULL,
    last_modification_date timestamp without time zone DEFAULT '1983-06-19 04:09:23+02'::timestamp with time zone NOT NULL,
    user_fk bigint NOT NULL,
    note_fk bigint NOT NULL,
    user_note_entity_fk bigint
);


--
-- Name: user_note_property_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE user_note_property_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: user_of_group; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE user_of_group (
    id bigint NOT NULL,
    modification_type character varying(1024),
    group_fk bigint NOT NULL,
    user_fk bigint NOT NULL
);


--
-- Name: user_of_group_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE user_of_group_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: user_profile; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE user_profile (
    id bigint NOT NULL,
    last_name character varying(1024),
    salutation character varying(1024),
    "position" character varying(1024),
    company character varying(1024),
    first_name character varying(1024),
    last_modification_date timestamp without time zone NOT NULL,
    last_photo_modification_date timestamp without time zone,
    time_zone_id character varying(1024),
    small_image_fk bigint,
    contact_fk bigint,
    medium_image_fk bigint,
    large_image_fk bigint,
    notification_config_fk bigint NOT NULL
);


--
-- Name: user_profile_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE user_profile_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: user_user; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE user_user (
    id bigint NOT NULL,
    password character varying(1024),
    email character varying(255) NOT NULL,
    language_code character varying(1024) NOT NULL,
    last_login timestamp without time zone,
    status character varying(1024) NOT NULL,
    alias character varying(255),
    terms_accepted boolean NOT NULL,
    reminder_mail_sent boolean NOT NULL,
    status_changed timestamp without time zone NOT NULL,
    profile_fk bigint NOT NULL,
    authentication_token character varying(1024)
);


--
-- Name: user_user_property; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE user_user_property (
    id bigint NOT NULL,
    key_group character varying(128) NOT NULL,
    property_key character varying(128) NOT NULL,
    property_value character varying(4000) NOT NULL,
    kenmei_user_fk bigint,
    last_modification_date timestamp without time zone DEFAULT '1983-06-19 04:09:23+02'::timestamp with time zone NOT NULL
);


--
-- Name: user_user_property_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE user_user_property_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY core_blog2blog_resolved ALTER COLUMN id SET DEFAULT nextval('core_blog2blog_resolved_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY core_plugin_properties ALTER COLUMN id SET DEFAULT nextval('core_plugin_properties_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY user_navigation_item ALTER COLUMN id SET DEFAULT nextval('user_navigation_item_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY user_note_entity ALTER COLUMN id SET DEFAULT nextval('user_note_entity_id_seq'::regclass);


--
-- Data for Name: channel_configuration; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Name: channel_configuration_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('channel_configuration_seq', 1, false);


--
-- Data for Name: con_test; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: configuration; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO configuration VALUES (1, 1);


--
-- Data for Name: configuration_app_setting; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO configuration_app_setting VALUES ('mailfetching.starttls', 'true');
INSERT INTO configuration_app_setting VALUES ('mailing.starttls', 'true');
INSERT INTO configuration_app_setting VALUES ('communote.standalone.installation', 'true');
INSERT INTO configuration_app_setting VALUES ('installation.unique.id', 'cf94e02c-770d-47e3-be5d-c1058a182c5c');
INSERT INTO configuration_app_setting VALUES ('mailing.from.address', 'communote-installer-test@localhost');
INSERT INTO configuration_app_setting VALUES ('mailing.host', 'localhost');
INSERT INTO configuration_app_setting VALUES ('mailing.port', '25');
INSERT INTO configuration_app_setting VALUES ('mailing.from.address.name', '[Local Test] Communote-Team');
INSERT INTO configuration_app_setting VALUES ('kenmei.captcha.disable', 'true');
INSERT INTO configuration_app_setting VALUES ('installation.date', 'W6BWSHhq-I6mqR8plIOJUg:lZYDR-Cm02IEJw');
INSERT INTO configuration_app_setting VALUES ('com.communote.core.keystore.password', 'a8fa18db-8561-401d-9c5b-121b4d61280c');
INSERT INTO configuration_app_setting VALUES ('kenmei.trusted.ca.keystore.password', '6f96baff-40bb-497a-8d04-64b46d3b0cd9');
INSERT INTO configuration_app_setting VALUES ('kenmei.image.max.upload.size', '1048576');
INSERT INTO configuration_app_setting VALUES ('kenmei.attachment.max.upload.size', '10485760');
INSERT INTO configuration_app_setting VALUES ('kenmei.crc.file.repository.storage.dir.root', '/tmp/communote-test/data/filerepository');
INSERT INTO configuration_app_setting VALUES ('virus.scanner.enabled', 'false');


--
-- Data for Name: configuration_client; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO configuration_client VALUES (1, NULL, NULL, 'time.zones.gmt.Europe/Amsterdam', 1);


--
-- Name: configuration_client_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('configuration_client_seq', 1, true);


--
-- Data for Name: configuration_confluence; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: configuration_external_sys; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Name: configuration_external_sys_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('configuration_external_sys_seq', 1, false);


--
-- Data for Name: configuration_ldap; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: configuration_ldap_group; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Name: configuration_ldap_group_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('configuration_ldap_group_seq', 1, false);


--
-- Data for Name: configuration_ldap_sbase; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Name: configuration_ldap_sbase_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('configuration_ldap_sbase_seq', 1, false);


--
-- Data for Name: configuration_ldap_search; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Name: configuration_ldap_search_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('configuration_ldap_search_seq', 1, false);


--
-- Name: configuration_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('configuration_seq', 1, true);


--
-- Data for Name: configuration_setting; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO configuration_setting VALUES ('kenmei.notification.render.attachmentlinks', NULL, NULL, NULL);
INSERT INTO configuration_setting VALUES ('kenmei.notification.render.permalinks', 'true', 1, 1464982509503);
INSERT INTO configuration_setting VALUES ('client.creation.date', 'tswfq0nNPw9iNsgsflDUhw:yMRF5GfekJbVkyO2mOfvY5x1LA', 1, 1464982509506);
INSERT INTO configuration_setting VALUES ('kenmei.client.blog.count.100.mail', '', 1, 1464982509512);
INSERT INTO configuration_setting VALUES ('kenmei.client.reply.to.address', '', 1, 1464982509515);
INSERT INTO configuration_setting VALUES ('kenmei.client.allow.all.can.read.write.for.all.users', 'true', 1, 1464982509518);
INSERT INTO configuration_setting VALUES ('kenmei.client.blog.count.limit', '0', 1, 1464982509520);
INSERT INTO configuration_setting VALUES ('kenmei.unique.client.identifer', 'ac11a690-1728-4e6c-a26c-233482f5d704', 1, 1464982509521);
INSERT INTO configuration_setting VALUES ('kenmei.client.blog.count.90.mail', '', 1, 1464982509523);
INSERT INTO configuration_setting VALUES ('kenmei.client.delete.user.by.disable.enabled', 'false', 1, 1464982509525);
INSERT INTO configuration_setting VALUES ('kenmei.crc.file.repository.size.90.mail', 'false', 1, 1464982509526);
INSERT INTO configuration_setting VALUES ('kenmei.crc.file.repository.size.limit', '0', 1, 1464982509527);
INSERT INTO configuration_setting VALUES ('kenmei.client.user.tagged.count.limit', '0', 1, 1464982509529);
INSERT INTO configuration_setting VALUES ('kenmei.client.user.tagged.count.90.mail', '', 1, 1464982509531);
INSERT INTO configuration_setting VALUES ('kenmei.client.delete.user.by.anonymize.enabled', 'false', 1, 1464982509534);
INSERT INTO configuration_setting VALUES ('kenmei.crc.file.repository.size.100.mail', '2016-06-02', 1, 1464982509537);
INSERT INTO configuration_setting VALUES ('kenmei.automatic.user.activation', 'false', 1, 1464982509539);
INSERT INTO configuration_setting VALUES ('kenmei.client.user.tagged.count.100.mail', '', 1, 1464982509541);
INSERT INTO configuration_setting VALUES ('kenmei.client.support.email.address', '', 1, 1464982509542);
INSERT INTO configuration_setting VALUES ('kenmei.client.reply.to.address.name', '', 1, 1464982509544);
INSERT INTO configuration_setting VALUES ('kenmei.blog.allow.public.access', 'false', 1, 1464982509545);


--
-- Data for Name: core_attachment; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: core_attachment_property; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Name: core_attachment_property_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('core_attachment_property_seq', 1, false);


--
-- Name: core_attachment_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('core_attachment_seq', 1, false);


--
-- Data for Name: core_blog; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO core_blog VALUES (1, 'Global Test Client', NULL, '2016-06-03 21:35:09.685', 'default', '2016-06-03 21:35:09.685', true, true, false, 3, false, false, '2016-06-03 21:35:09.729');


--
-- Data for Name: core_blog2blog; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: core_blog2blog_resolved; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Name: core_blog2blog_resolved_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('core_blog2blog_resolved_seq', 1, false);


--
-- Data for Name: core_blog2tag; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO core_blog2tag VALUES (1, 1);


--
-- Name: core_blog_event_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('core_blog_event_seq', 1, false);


--
-- Data for Name: core_blog_member; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO core_blog_member VALUES (1, 'BlogMemberImpl', 'MANAGER', 1, 1, NULL);


--
-- Name: core_blog_member_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('core_blog_member_seq', 1, true);


--
-- Data for Name: core_blog_property; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Name: core_blog_property_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('core_blog_property_seq', 1, false);


--
-- Name: core_blog_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('core_blog_seq', 1, true);


--
-- Data for Name: core_content; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Name: core_content_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('core_content_seq', 1, false);


--
-- Data for Name: core_entity2tags; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: core_external_object; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: core_external_object_prop; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Name: core_external_object_prop_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('core_external_object_prop_seq', 1, false);


--
-- Name: core_external_object_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('core_external_object_seq', 1, false);


--
-- Data for Name: core_global_binary_prop; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Name: core_global_binary_prop_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('core_global_binary_prop_seq', 1, false);


--
-- Data for Name: core_global_id; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO core_global_id VALUES (1, '/ac11a690-1728-4e6c-a26c-233482f5d704/user/1');
INSERT INTO core_global_id VALUES (2, '/ac11a690-1728-4e6c-a26c-233482f5d704/tag/1');
INSERT INTO core_global_id VALUES (3, '/ac11a690-1728-4e6c-a26c-233482f5d704/blog/1');


--
-- Name: core_global_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('core_global_id_seq', 3, true);


--
-- Data for Name: core_note; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: core_note2direct_user; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: core_note2followable; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: core_note_property; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Name: core_note_property_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('core_note_property_seq', 1, false);


--
-- Name: core_note_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('core_note_seq', 1, false);


--
-- Data for Name: core_notes2crossblogs; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: core_notes2tag; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: core_notes2user_to_notify; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: core_plugin_properties; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Name: core_plugin_properties_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('core_plugin_properties_id_seq', 1, false);


--
-- Name: core_plugin_properties_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('core_plugin_properties_seq', 1, false);


--
-- Data for Name: core_processed_utp_mail; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Name: core_processed_utp_mail_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('core_processed_utp_mail_seq', 1, false);


--
-- Data for Name: core_role2blog; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO core_role2blog VALUES (1, 1, 1, 3, NULL, false);


--
-- Data for Name: core_role2blog_granting_group; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Name: core_role2blog_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('core_role2blog_seq', 1, true);


--
-- Data for Name: core_tag; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO core_tag VALUES (1, 'TagImpl', 'default', 'default', 2, NULL, NULL, 'DefaultBlogTagStore');


--
-- Data for Name: core_tag2descriptions; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: core_tag2names; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: core_tag_category; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Name: core_tag_category_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('core_tag_category_seq', 1, false);


--
-- Name: core_tag_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('core_tag_seq', 1, true);


--
-- Data for Name: core_task; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO core_task VALUES (1, 'SynchronizeGroups', 'PENDING', 3600000, 'com.communote.server.core.user.groups.UserGroupSynchronizationTaskHandler', true, '2016-06-03 21:40:09.213', NULL);
INSERT INTO core_task VALUES (2, 'RemindUsers', 'PENDING', 8640000, 'com.communote.server.core.user.RemindUserJob', true, '2016-06-03 21:40:09.213', NULL);
INSERT INTO core_task VALUES (3, 'DeleteOrphanedAttachments', 'PENDING', 604800000, 'com.communote.server.core.tasks.DeleteOrphanedAttachmentsTaskHandler', true, '2016-06-04 00:35:09.294', NULL);


--
-- Data for Name: core_task_execs; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Name: core_task_execs_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('core_task_execs_seq', 1, false);


--
-- Data for Name: core_task_props; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Name: core_task_props_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('core_task_props_seq', 1, false);


--
-- Name: core_task_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('core_task_seq', 3, true);


--
-- Data for Name: core_user2follows; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: core_users2favorite_notes; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Name: crc_cache_config_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('crc_cache_config_seq', 1, false);


--
-- Name: crc_connector_config_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('crc_connector_config_seq', 1, false);


--
-- Data for Name: custom_messages; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Name: custom_messages_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('custom_messages_seq', 1, false);


--
-- Data for Name: databasechangelog; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO databasechangelog VALUES ('reset_checksum_001', 'unknown', 'de/communardo/kenmei/database/update/db.changelog.default.xml', '2016-06-03 21:34:27.183+02', '6c50cd6ee695161d8f569de01f98956', 'Custom SQL', 'Reset Checksums', NULL, '1.9.2');
INSERT INTO databasechangelog VALUES ('mt2849', 'unknown', 'classpath:de/communardo/kenmei/database/update/v1.0.2/db.changelog.v1.0.2.xml', '2016-06-03 21:34:27.183+02', 'dab22a3ed157bef34b18d0a48199f19d', 'Add Column (x2)', '2849: [Widget] Render Image Url to external systems into the widget', NULL, '1.9.2');
INSERT INTO databasechangelog VALUES ('mt2866_1', 'unknown', 'de/communardo/kenmei/database/update/v1.0.3/db.changelog.v1.0.3.xml', '2016-06-03 21:34:27.183+02', '57da76f41130a0648519cdaa55eccb8', 'Drop Unique Constraint', 'Manage constraints', NULL, '1.9.2');
INSERT INTO databasechangelog VALUES ('mt2866_2', 'unknown', 'de/communardo/kenmei/database/update/v1.0.3/db.changelog.v1.0.3.xml', '2016-06-03 21:34:27.183+02', '7f4c97e9c7653cff1e8284ec6560', 'Drop Unique Constraint', 'Manage constraints', NULL, '1.9.2');
INSERT INTO databasechangelog VALUES ('mt2866_3', 'unknown', 'de/communardo/kenmei/database/update/v1.0.3/db.changelog.v1.0.3.xml', '2016-06-03 21:34:27.183+02', 'f8cebad98d84d81f7f83b48ebc0a534', 'Drop Index', 'Manage constraints', NULL, '1.9.2');
INSERT INTO databasechangelog VALUES ('mt2866_4', 'unknown', 'de/communardo/kenmei/database/update/v1.0.3/db.changelog.v1.0.3.xml', '2016-06-03 21:34:27.183+02', '7a6f867828c55aaed72d20c4a0c25281', 'Drop Index', 'Manage constraints', NULL, '1.9.2');
INSERT INTO databasechangelog VALUES ('mt2866_5', 'unknown', 'de/communardo/kenmei/database/update/v1.0.3/db.changelog.v1.0.3.xml', '2016-06-03 21:34:27.183+02', '6e10995d477abf4e43b1eb508c74cd', 'Add Unique Constraint', 'Manage constraints', NULL, '1.9.2');
INSERT INTO databasechangelog VALUES ('mt2859_1', 'unknown', 'de/communardo/kenmei/database/update/v1.0.3/db.changelog.v1.0.3.xml', '2016-06-03 21:34:27.183+02', '56c07a2f5b0e2ae40d3273b6259475a', 'Add Column (x2), Add Foreign Key Constraint, Drop Column, Drop Unique Constraint, Add Column (x2), Custom SQL, Add Unique Constraint, Add Not-Null Constraint (x2)', 'CR 122 Securing External Authentication', NULL, '1.9.2');
INSERT INTO databasechangelog VALUES ('mt2859_2', 'unknown', 'de/communardo/kenmei/database/update/v1.0.3/db.changelog.v1.0.3.xml', '2016-06-03 21:34:27.183+02', 'b2e0e51b2fdd768127d43d458e21d939', 'Add Unique Constraint', '', NULL, '1.9.2');
INSERT INTO databasechangelog VALUES ('test10', 'unknown', 'classpath:de/communardo/kenmei/database/update/v1.0.1/db.changelog.v1.0.1.xml', '2016-06-03 21:34:27.183+02', '1fcdece94dbda9f7a75d7b6abf09db0', 'Custom SQL', 'just a test change set', NULL, '1.9.2');
INSERT INTO databasechangelog VALUES ('mt2213_1', 'unknown', 'classpath:de/communardo/kenmei/database/update/v1.0.1/db.changelog.v1.0.1.xml', '2016-06-03 21:34:27.183+02', 'aef67110d2b012e7747cc3d1573caaa', 'Custom SQL', '2213: CR 2 - API for Mobile Access and 3rd Systems: Last Modification Date on User profile', NULL, '1.9.2');
INSERT INTO databasechangelog VALUES ('mt2213_2', 'unknown', 'classpath:de/communardo/kenmei/database/update/v1.0.1/db.changelog.v1.0.1.xml', '2016-06-03 21:34:27.183+02', 'cd55f272caed519568b49fb2a168646', 'Custom SQL', '2213: CR 2 - API for Mobile Access and 3rd Systems: Last Modification Date on User profile', NULL, '1.9.2');
INSERT INTO databasechangelog VALUES ('mt2213_3', 'unknown', 'classpath:de/communardo/kenmei/database/update/v1.0.1/db.changelog.v1.0.1.xml', '2016-06-03 21:34:27.183+02', '2cba3b1533411c8c06710f3f654c3', 'Custom SQL', '2213: CR 2 - API for Mobile Access and 3rd Systems: Property for downloading the mobile application', NULL, '1.9.2');
INSERT INTO databasechangelog VALUES ('mt2695_1', 'unknown', 'classpath:de/communardo/kenmei/database/update/v1.0.1/db.changelog.v1.0.1.xml', '2016-06-03 21:34:27.183+02', 'f9ad58a8c74f90472d1c7bcb93cac554', 'Custom SQL', '2695: showing the IP range in update dialog the way it was inserted: Keep IPv4 IP range in IPv4 format', NULL, '1.9.2');
INSERT INTO databasechangelog VALUES ('mt2711_1', 'unknown', 'classpath:de/communardo/kenmei/database/update/v1.0.1/db.changelog.v1.0.1.xml', '2016-06-03 21:34:27.183+02', '761357100419dea39489e458c6f3cb', 'Custom SQL', '2711: [CompositeWidget] - CR 18 - Confluence Integration - Optimisation', NULL, '1.9.2');
INSERT INTO databasechangelog VALUES ('mt2773_1', 'unknown', 'classpath:de/communardo/kenmei/database/update/v1.0.1/db.changelog.v1.0.1.xml', '2016-06-03 21:34:27.183+02', 'c37026804025a07bcd84cce2a2f1db92', 'Custom SQL', '2773: CR 19 - Sharepoint integration', NULL, '1.9.2');
INSERT INTO databasechangelog VALUES ('mt1957_1', 'unknown', 'classpath:de/communardo/kenmei/database/update/v1.0.2/db.changelog.v1.0.2.xml', '2016-06-03 21:34:27.183+02', '109b8a23e6b76127568cd4441a962715', 'Custom SQL', '1957: CR 99 Create posts for blog right changes', NULL, '1.9.2');
INSERT INTO databasechangelog VALUES ('mt2276_1', 'unknown', 'classpath:de/communardo/kenmei/database/update/v1.0.2/db.changelog.v1.0.2.xml', '2016-06-03 21:34:27.183+02', 'bc1f408713c4875855223778974bb3d', 'Custom SQL', '2276: CR 43 Delete/Disable user: update name of user status constant DISABLED', NULL, '1.9.2');
INSERT INTO databasechangelog VALUES ('mt2699_1', 'unknown', 'classpath:de/communardo/kenmei/database/update/v1.0.2/db.changelog.v1.0.2.xml', '2016-06-03 21:34:27.183+02', '31cec3dd4dc32191a4e5d578127ffd5', 'Custom SQL', '2699: CR 71 - Force SSL | 2698: Redirect einer https Anfrage wird zu http Anfrage', NULL, '1.9.2');
INSERT INTO databasechangelog VALUES ('mt2710_1', 'unknown', 'classpath:de/communardo/kenmei/database/update/v1.0.2/db.changelog.v1.0.2.xml', '2016-06-03 21:34:27.183+02', '36b9adb128bbb5f23297f8b44d227015', 'Custom SQL', '2710: tags get lost when removing origin of crosspost created through edit operation', NULL, '1.9.2');
INSERT INTO databasechangelog VALUES ('mt2899_postgresql', 'unknown', 'de/communardo/kenmei/database/update/v1.0.3/db.changelog.v1.0.3.xml', '2016-06-03 21:34:27.183+02', '1a4419e2f5f8b265b2b7a39b3af0ad80', 'Custom SQL', 'MT 2899: Image caching problems', NULL, '1.9.2');
INSERT INTO databasechangelog VALUES ('20091007_postgresql', 'unknown', 'de/communardo/kenmei/database/update/v1.0.3/db.changelog.v1.0.3.xml', '2016-06-03 21:34:27.183+02', '89652831b7f99da19387e93f72471b', 'Custom SQL', '', NULL, '1.9.2');
INSERT INTO databasechangelog VALUES ('mt2846_global_id_checksum_v2', 'unknown', 'de/communardo/kenmei/database/update/v1.1/db.changelog.v1.1.refactor.xml', '2016-06-03 21:34:27.183+02', '304f2354db312562949c37255a17bf', 'Custom SQL', 'MT 2846: CR 119 - Refactoring Post Backend: Global Id, Configuration, Checksum', NULL, '1.9.2');
INSERT INTO databasechangelog VALUES ('mt2846_user_external_auth', 'unknown', 'de/communardo/kenmei/database/update/v1.1/db.changelog.v1.1.refactor.xml', '2016-06-03 21:34:27.183+02', '6c8c9284a09b847b41d87b11499cf4b', 'Rename Table, Rename Column, Drop Foreign Key Constraint, Add Foreign Key Constraint', 'MT 2846: CR 119 - Refactoring Post Backend - Rename external user auth', NULL, '1.9.2');
INSERT INTO databasechangelog VALUES ('mt2846_remove_module_status', 'unknown', 'de/communardo/kenmei/database/update/v1.1/db.changelog.v1.1.refactor.xml', '2016-06-03 21:34:27.183+02', 'c616cb81c99fea1e6427599e6cec5187', 'Drop Column (x4)', 'MT 2846: CR 119 - Refactoring Post Backend - Remove module status', NULL, '1.9.2');
INSERT INTO databasechangelog VALUES ('mt2846_postgresql', 'unknown', 'de/communardo/kenmei/database/update/v1.1/postgres/db.changelog.v1.1.refactor.postgres.xml', '2016-06-03 21:34:27.183+02', 'b191e89b90e3ff4f1f99e15ea9862831', 'Drop Table (x6), Drop Sequence (x2), Drop Foreign Key Constraint (x9), Drop Primary Key (x4), Create Table, Create Sequence, Create Table, SQL From File, Drop Column (x7), SQL From File, Drop Column, Rename Table (x2), Add Not-Null Constraint, SQL From...', 'MT 2846: CR 119 - Refactoring Post Backend - postgresql', NULL, '1.9.2');
INSERT INTO databasechangelog VALUES ('mt2846_favorite_users', 'unknown', 'de/communardo/kenmei/database/update/v1.1/db.changelog.v1.1.refactor.xml', '2016-06-03 21:34:27.183+02', '9f4a84f23eb8c55a4730335d35c3ab7f', 'Rename Table', 'MT 2846: CR 119 - Refactoring Post Backend - Favorite users', NULL, '1.9.2');
INSERT INTO databasechangelog VALUES ('mt2940_1', 'unknown', 'de/communardo/kenmei/database/update/v1.1/db.changelog.v1.1.xml', '2016-06-03 21:34:27.183+02', 'b962fa5b9ddcecbc21d62b4999d8764', 'Update Data, Create Table, Add Foreign Key Constraint (x2)', 'CR 131: autosave refactoring', NULL, '1.9.2');
INSERT INTO databasechangelog VALUES ('mt2945_1', 'unknown', 'de/communardo/kenmei/database/update/v1.1/db.changelog.v1.1.xml', '2016-06-03 21:34:27.183+02', '22b680833818a22cf3f5e695a7b185d', 'Add Column, Add Foreign Key Constraint', 'CR 109: Post without selecting a blog', NULL, '1.9.2');
INSERT INTO databasechangelog VALUES ('mt2957_1', 'unknown', 'de/communardo/kenmei/database/update/v1.1/db.changelog.v1.1.xml', '2016-06-03 21:34:27.183+02', '2a75e566753c4ffb3f42c5fb9896cd', 'Add Column (x2)', 'CR 122 - Threaded answers', NULL, '1.9.2');
INSERT INTO databasechangelog VALUES ('mt2957_2_postgresql', 'unknown', 'de/communardo/kenmei/database/update/v1.1/db.changelog.v1.1.xml', '2016-06-03 21:34:27.183+02', '8eff1dc49b3e288edc26369aadbdfd', 'Custom SQL', 'CR 122 - Threaded answers', NULL, '1.9.2');
INSERT INTO databasechangelog VALUES ('mt2976_1', 'unknown', 'de/communardo/kenmei/database/update/v1.1/db.changelog.v1.1.xml', '2016-06-03 21:34:27.183+02', 'dcd4c199fd6bf5cd3ca66a34a84afa7', 'Add Column (x3)', 'Content Type', NULL, '1.9.2');
INSERT INTO databasechangelog VALUES ('mt2976_2', 'unknown', 'de/communardo/kenmei/database/update/v1.1/db.changelog.v1.1.xml', '2016-06-03 21:34:27.183+02', '6b7314e763c81f1f96c67deb8d806d69', 'Custom SQL', 'Attachment Status', NULL, '1.9.2');
INSERT INTO databasechangelog VALUES ('cr147_1_postgresql', 'unknown', 'de/communardo/kenmei/database/update/v1.1/db.changelog.v1.1.xml', '2016-06-03 21:34:27.183+02', '5c1f960517d5aa7758b6a10ef29d543', 'Drop Unique Constraint', 'Client Registration without Activation Code', NULL, '1.9.2');
INSERT INTO databasechangelog VALUES ('rename_sequence_for_external_authentication', 'unknown', 'de/communardo/kenmei/database/update/v1.1/db.changelog.v1.1.xml', '2016-06-03 21:34:27.183+02', '4619992ec3fc7887d883c037c0d389', 'Drop Sequence, Custom SQL', '', NULL, '1.9.2');
INSERT INTO databasechangelog VALUES ('rename_default_blog_alias_1_postgresql', 'unknown', 'de/communardo/kenmei/database/update/v1.1/db.changelog.v1.1.xml', '2016-06-03 21:34:27.183+02', 'aab6b129c4acfe2705da4d8d127dd7b', 'Custom SQL', 'Renames the blog alias of the default blog, if an alias ''public'' not exists.', NULL, '1.9.2');
INSERT INTO databasechangelog VALUES ('make_default_blog_default_postgresql', 'unknown', 'de/communardo/kenmei/database/update/v1.1/db.changelog.v1.1.xml', '2016-06-03 21:34:27.183+02', '38cfdbb7258c1ee87a0538f80fe66aa', 'Custom SQL', '', NULL, '1.9.2');
INSERT INTO databasechangelog VALUES ('rename_default_blog_alias_2_postgresql', 'unknown', 'de/communardo/kenmei/database/update/v1.1/db.changelog.v1.1.xml', '2016-06-03 21:34:27.183+02', '2e2a954a68a522fff8e78dd5ddb88f98', 'Custom SQL', 'Renames the blog alias of the default blog, if it is still the message key.', NULL, '1.9.2');
INSERT INTO databasechangelog VALUES ('mt3096_1', 'unknown', 'de/communardo/kenmei/database/update/v1.1/db.changelog.v1.1.xml', '2016-06-03 21:34:27.183+02', 'abcc6ad563565e4050381273ef3fcc5d', 'Custom SQL', 'deletes unconnected large user images', NULL, '1.9.2');
INSERT INTO databasechangelog VALUES ('set_configuration', 'unknown', 'de/communardo/kenmei/database/update/db.changelog.final.xml', '2016-06-03 21:34:27.183+02', '6148c09f2e75f3239da193221c5cedca', 'Custom SQL', 'Assign the configuration FK', NULL, '1.9.2');
INSERT INTO databasechangelog VALUES ('mt3178_2_postgresql', 'unknown', 'de/communardo/kenmei/database/update/v1.1/db.changelog.v1.1.xml', '2016-06-03 21:34:27.183+02', 'd1808dcb3e616cfff590fbbdf8494f', 'Insert Row', 'Automated Client Approval', NULL, '1.9.2');
INSERT INTO databasechangelog VALUES ('mt3187_1', 'unknown', 'de/communardo/kenmei/database/update/v1.1/db.changelog.v1.1.xml', '2016-06-03 21:34:27.183+02', '77fa5d42e90366abddbbb7dbb94e61d', 'Drop Foreign Key Constraint', 'Adjust the forein key constraint for kenmei_users_fk.', NULL, '1.9.2');
INSERT INTO databasechangelog VALUES ('mt3187_2', 'unknown', 'de/communardo/kenmei/database/update/v1.1/db.changelog.v1.1.xml', '2016-06-03 21:34:27.183+02', '1b315757661762a2659fde38c03259', 'Add Foreign Key Constraint', 'Adjust the forein key constraint for kenmei_users_fk.', NULL, '1.9.2');
INSERT INTO databasechangelog VALUES ('mt3187_3', 'unknown', 'de/communardo/kenmei/database/update/v1.1/db.changelog.v1.1.xml', '2016-06-03 21:34:27.183+02', '77fa5d42e90366abddbbb7dbb94e61d', 'Drop Foreign Key Constraint', 'Drop the forein key constraint for kenmei_users_fk.', NULL, '1.9.2');
INSERT INTO databasechangelog VALUES ('mt3187_4_postgresql', 'unknown', 'de/communardo/kenmei/database/update/v1.1/db.changelog.v1.1.xml', '2016-06-03 21:34:27.183+02', '8aaffcfeede332f65df9db8a7e8a21d5', 'Rename Column, Add Foreign Key Constraint', 'Adjust core_users2favorite_notes and rename the column kenmei_user_fk to favorite_users_fk.', NULL, '1.9.2');
INSERT INTO databasechangelog VALUES ('mt3196_1', 'unknown', 'de/communardo/kenmei/database/update/v1.1.2/db.changelog.v1.1.2.xml', '2016-06-03 21:34:27.183+02', '3f4f52f46b355cec745a24706cca46d4', 'Add Column', 'CR 100 - Support Time Zones: add new column to user_profile', NULL, '1.9.2');
INSERT INTO databasechangelog VALUES ('mt3196_2', 'unknown', 'de/communardo/kenmei/database/update/v1.1.2/db.changelog.v1.1.2.xml', '2016-06-03 21:34:27.183+02', 'dca47279adc3bbd1ad7e1d7174e10b1', 'Add Column (x2)', 'CR 100 - Support Time Zones: add new column to user_client_creation, configuration_client', NULL, '1.9.2');
INSERT INTO databasechangelog VALUES ('mt3208', 'unknown', 'de/communardo/kenmei/database/update/v1.1.2/db.changelog.v1.1.2.xml', '2016-06-03 21:34:27.183+02', 'a5e3666944ac3f968080ff852bd777d3', 'Add Column', 'CR 68 Read-More functionality', NULL, '1.9.2');
INSERT INTO databasechangelog VALUES ('mt3350_configuration_column_ldap', 'unknown', 'de/communardo/kenmei/database/update/v1.1.2/db.changelog.v1.1.2.xml', '2016-06-03 21:34:27.183+02', 'dd10f32f1dfab6778b0c3eb60acd4ba', 'Drop Column', 'MT 3350', NULL, '1.9.2');
INSERT INTO databasechangelog VALUES ('mt2846_global_id_v2', 'unknown', 'de/communardo/kenmei/database/update/v1.1/db.changelog.v1.1.refactor.xml', '2016-06-03 21:34:27.183+02', '58bc1037a63ce304caa7c551b843d7', 'Insert Row', 'MT 2846: CR 119 - Refactoring Post Backend: Global Id', NULL, '1.9.2');
INSERT INTO databasechangelog VALUES ('mt3277_1_postgresql', 'unknown', 'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', '2016-06-03 21:34:27.183+02', '3e79f8a44a768c7fbb276e9f42e1ec9e', 'Create Table, Custom SQL, Drop Column, Add Foreign Key Constraint, Create Sequence, Custom SQL, Drop Sequence, Add Foreign Key Constraint', 'CR 96 - support for groups: add user_entity and copy users', NULL, '1.9.2');
INSERT INTO databasechangelog VALUES ('mt3277_2_postgresql', 'unknown', 'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', '2016-06-03 21:34:27.183+02', '88fa38c4a5f8ae66e1cbae3414dfccf', 'Drop Table (x2)', 'CR 96 - support for groups: remove obsolete security codes', NULL, '1.9.2');
INSERT INTO databasechangelog VALUES ('mt3277_3_postgresql', 'unknown', 'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', '2016-06-03 21:34:27.183+02', 'b72b96a07378fb73577720522850d5d4', 'Add Column, Custom SQL', 'CR 96 - support for groups: copy all_can_x rights from group to blog', NULL, '1.9.2');
INSERT INTO databasechangelog VALUES ('mt3277_4_postgresql', 'unknown', 'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', '2016-06-03 21:34:27.183+02', '25c695869c8f851fd412ca90db5b2677', 'Add Column, Custom SQL', 'CR 96 - support for groups: add blogs to group member', NULL, '1.9.2');
INSERT INTO databasechangelog VALUES ('mt3277_5_postgresql', 'unknown', 'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', '2016-06-03 21:34:27.183+02', 'ec2e40b661e8abf982e63f7cdc2a207b', 'Drop Foreign Key Constraint (x2), Drop Primary Key, Drop Column (x2), Custom SQL, Rename Table, Rename Column (x2), Add Column, Add Foreign Key Constraint (x2), Custom SQL, Add Not-Null Constraint', 'CR 96 - support for groups: group member to blog member', NULL, '1.9.2');
INSERT INTO databasechangelog VALUES ('mt3277_6_postgresql', 'unknown', 'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', '2016-06-03 21:34:27.183+02', 'da4346ff7575e6ebf6b3cd1266164', 'Drop Foreign Key Constraint, Drop Column', 'CR 96 - support for groups: cleanup core_blog', NULL, '1.9.2');
INSERT INTO databasechangelog VALUES ('mt3277_7_postgresql', 'unknown', 'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', '2016-06-03 21:34:27.183+02', 'e3ade68c679ed8297e1837d4157a806e', 'Drop Column (x3), Delete Data, Drop Sequence, Add Column (x3), Add Foreign Key Constraint', 'CR 96 - support for groups: fix user_group', NULL, '1.9.2');
INSERT INTO databasechangelog VALUES ('mt3277_8_postgresql', 'unknown', 'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', '2016-06-03 21:34:27.183+02', 'aeb31ecf1d4fcc24eab6609a8ca47465', 'Create Table, Create Sequence', 'CR 96 - support for groups: add helper table for fast blog role lookup', NULL, '1.9.2');
INSERT INTO databasechangelog VALUES ('mt3277_9_postgresql', 'unknown', 'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', '2016-06-03 21:34:27.183+02', 'c27edd6bf78bb4fb2e7ed6ad76884', 'Custom SQL', 'CR 96 - support for groups: fill core_role2blog', NULL, '1.9.2');
INSERT INTO databasechangelog VALUES ('mt3277_10_postgresql', 'unknown', 'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', '2016-06-03 21:34:27.183+02', 'fa346348fef8d167f2390c9b247f053', 'Create Table, Add Primary Key, Add Foreign Key Constraint (x2)', 'CR 96 - support for groups: allow several granting groups', NULL, '1.9.2');
INSERT INTO databasechangelog VALUES ('mt3277_11_postgresql', 'unknown', 'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', '2016-06-03 21:34:27.183+02', 'c83fd6a7d42279a72d55ededfcfa4d86', 'Create Table, Add Primary Key, Add Foreign Key Constraint (x2)', 'CR 96 - support for groups: connect entity with group', NULL, '1.9.2');
INSERT INTO databasechangelog VALUES ('KENMEI-Drop_snc_tables', 'unknown', 'de/communardo/kenmei/database/update/v1.3/db.changelog.v1.3.xml', '2016-06-03 21:34:42.234+02', '1996781dbc24996e36864b7ece8f61d', 'Drop Table (x2)', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('mt3277_12_postgresql', 'unknown', 'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', '2016-06-03 21:34:27.183+02', 'ee8051d2be251e5b45b1991b5b9a9d8d', 'Add Primary Key', 'CR 96 - support for groups: primary key for core_blog_member', NULL, '1.9.2');
INSERT INTO databasechangelog VALUES ('mt3277_13_postgresql', 'unknown', 'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', '2016-06-03 21:34:27.183+02', 'a34dbbe11227d567214cdd8ec5e5bc76', 'Add Not-Null Constraint (x2)', 'CR 96 - support for groups: not null constraints for all_can_x rights', NULL, '1.9.2');
INSERT INTO databasechangelog VALUES ('mt3281_1_postgresql', 'unknown', 'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', '2016-06-03 21:34:27.183+02', '3ebca4821f7296e11d97c5913fe8707c', 'Insert Row', 'CR 134 - Anonymous Access, Anonymous User', NULL, '1.9.2');
INSERT INTO databasechangelog VALUES ('mt3281_2_postgresql', 'unknown', 'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', '2016-06-03 21:34:27.183+02', '2dc05cec435434be83aba19fcba92b', 'Add Column, Add Not-Null Constraint', 'CR 134 - Anonymous Access, Anonymous User', NULL, '1.9.2');
INSERT INTO databasechangelog VALUES ('cr135', 'amo', 'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', '2016-06-03 21:34:27.183+02', 'a02a9368d39aadb2b9affeff80229c', 'Create Table, Add Foreign Key Constraint', 'CR 135 - User group synchronization', NULL, '1.9.2');
INSERT INTO databasechangelog VALUES ('mt3283_external_configuration', 'unknown', 'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', '2016-06-03 21:34:27.183+02', 'c2c4eb85364225670b85647601134e0', 'Rename Table (x3), Rename Column, Add Column, Drop Foreign Key Constraint, Add Foreign Key Constraint', 'Refactor External Authentication', NULL, '1.9.2');
INSERT INTO databasechangelog VALUES ('mt3283_confluence_configuration', 'unknown', 'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', '2016-06-03 21:34:27.183+02', '5029a38aca9b7c4c25a4f41f619caa1c', 'Add Column (x2)', '', NULL, '1.9.2');
INSERT INTO databasechangelog VALUES ('mt3283_external_objects', 'unknown', 'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', '2016-06-03 21:34:27.183+02', '75db72d38b92ade15a93f1b854b9048', 'Create Table, Add Unique Constraint, Create Table, Add Foreign Key Constraint (x2), Add Column', '', NULL, '1.9.2');
INSERT INTO databasechangelog VALUES ('mt3283_seq_postgresql', 'unknown', 'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', '2016-06-03 21:34:27.183+02', '5580b0e798d54a4d6998dccd0ea3054', 'Rename Table, Create Sequence, Add Not-Null Constraint', 'Refactor External Authentication', NULL, '1.9.2');
INSERT INTO databasechangelog VALUES ('mt3283_external_objects_2', 'unknown', 'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', '2016-06-03 21:34:27.183+02', 'ff7ed34175992ead7a175351c0d4e9', 'Add Column, Add Not-Null Constraint', 'Add class to blog_member', NULL, '1.9.2');
INSERT INTO databasechangelog VALUES ('cr135_1', 'unknown', 'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', '2016-06-03 21:34:27.183+02', '318b839a3fff783a8f4a4a3e7f5866', 'Add Column', 'Add serviceUrl to confluence configuration', NULL, '1.9.2');
INSERT INTO databasechangelog VALUES ('mt3277_fix_null_constraint', 'unknown', 'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', '2016-06-03 21:34:27.183+02', '97f2d0c8945bae1c69f8f827c57e73', 'Drop Not-Null Constraint', 'Drop wrong "not null" constraint from user entities.', NULL, '1.9.2');
INSERT INTO databasechangelog VALUES ('mt3283_external_objects_3', 'unknown', 'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', '2016-06-03 21:34:27.183+02', 'd3cc6233f8abccd7d6c2eb5017cf77', 'Add Column', '', NULL, '1.9.2');
INSERT INTO databasechangelog VALUES ('mt3292_confluence_permission_url', 'unknown', 'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', '2016-06-03 21:34:27.183+02', '21a7986f567bd1c385abbdfead1e8c3', 'Add Column', 'CR 135 - Support Synchronization of blog rights with external systems', NULL, '1.9.2');
INSERT INTO databasechangelog VALUES ('mt3283_external_objects_fix_key_unique_postgres', 'unknown', 'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', '2016-06-03 21:34:27.183+02', '84c82d1e3b62faa383038847a657b6a', 'Drop Primary Key, Add Column, Create Sequence', 'CR 135 - Support Synchronization of blog rights with external systems', NULL, '1.9.2');
INSERT INTO databasechangelog VALUES ('mt3277_14', 'unknown', 'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', '2016-06-03 21:34:27.183+02', 'e694eb5228924a36a25357646af454e4', 'Add Column, Custom SQL, Add Not-Null Constraint', 'CR 96 - support for groups: grantedByGroup flag for mapping table', NULL, '1.9.2');
INSERT INTO databasechangelog VALUES ('phone_and_fax_country_code_fix_postgres', 'unknown', 'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', '2016-06-03 21:34:27.183+02', 'c3ab12cef7ea1a81296b35895c9e5246', 'Custom SQL', '', NULL, '1.9.2');
INSERT INTO databasechangelog VALUES ('mt_3272_1_postgres', 'unknown', 'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', '2016-06-03 21:34:27.183+02', '23a0a8c695a167eb63674a3c2bd0ef3c', 'Custom SQL', 'MT 3272: UserProfile saves space instead of NULL if no time zone is selected', NULL, '1.9.2');
INSERT INTO databasechangelog VALUES ('phone_country_code_fix__client_postgres', 'unknown', 'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', '2016-06-03 21:34:27.183+02', '43a246c3c36b1aa24b98d79667aecfc8', 'Custom SQL', '', NULL, '1.9.2');
INSERT INTO databasechangelog VALUES ('cr69_user_groups_v2', 'unknown', 'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', '2016-06-03 21:34:27.183+02', '41e53effcad9d4fe485b75dab0922bf1', 'Rename Column', '', NULL, '1.9.2');
INSERT INTO databasechangelog VALUES ('mt_3314_refactor_confluence_page', 'unknown', 'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', '2016-06-03 21:34:27.183+02', '86b2b96eee76c0d6bdaad4e1c24b5b25', 'Add Column', '', NULL, '1.9.2');
INSERT INTO databasechangelog VALUES ('mt3329_1_postgresql', 'unknown', 'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', '2016-06-03 21:34:27.183+02', '4ca078799d6d2921f43bf663bf31e2b6', 'Create Table, Create Sequence, Custom SQL, Add Foreign Key Constraint (x2)', 'CR 96 - support for hierarchical groups: add user_of_group and copy users', NULL, '1.9.2');
INSERT INTO databasechangelog VALUES ('mt3350_configuration_column_source', 'unknown', 'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', '2016-06-03 21:34:27.183+02', '12af7efa26d7a9eefaedfcd1ad9e2d24', 'Drop Column', 'MT 3350', NULL, '1.9.2');
INSERT INTO databasechangelog VALUES ('confluence_set_basepath_postgresql_new', 'unknown', 'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', '2016-06-03 21:34:27.183+02', 'ae735018153b6174ec84f47772f8f30', 'Custom SQL', '', NULL, '1.9.2');
INSERT INTO databasechangelog VALUES ('remove_uk_country_code', 'unknown', 'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', '2016-06-03 21:34:27.183+02', 'a8a46d136dea83ec4bdfffd11c9b065', 'Custom SQL', '', NULL, '1.9.2');
INSERT INTO databasechangelog VALUES ('cr_179_add_messages_table_v2', 'unknown', 'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', '2016-06-03 21:34:27.183+02', '51bf6b30763f19d84b1ba2aeac1fd14', 'Create Table, Add Foreign Key Constraint, Add Unique Constraint', '', NULL, '1.9.2');
INSERT INTO databasechangelog VALUES ('cr_179_add_messages_table_fix_key_column', 'unknown', 'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', '2016-06-03 21:34:27.183+02', 'db6273f493f2c16fa8c63f1a43ea57', 'Modify Column', '', NULL, '1.9.2');
INSERT INTO databasechangelog VALUES ('cr_179_add_messages_add_sequence', 'unknown', 'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', '2016-06-03 21:34:27.183+02', 'c49bc921e260e39014fcaf49fa5cb2db', 'Create Sequence', '', NULL, '1.9.2');
INSERT INTO databasechangelog VALUES ('cr_179_insert_default_values_for_imprint_terms_of_use', 'unknown', 'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', '2016-06-03 21:34:27.183+02', 'bd894dec5e563ba3bbe80105072455c', 'Custom SQL', '', NULL, '1.9.2');
INSERT INTO databasechangelog VALUES ('cr_179_add_is_Html_column_v2', 'unknown', 'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', '2016-06-03 21:34:27.183+02', '51c815f15bbb47cb1990c3f556b2fe', 'Add Column, Add Not-Null Constraint', '', NULL, '1.9.2');
INSERT INTO databasechangelog VALUES ('initialize-database-schema_v1', 'unknown', 'de/communardo/kenmei/database/install/postgres/db.changelog.init-db.postgres.xml', '2016-06-03 21:34:34.205+02', '453f5a369bf2ca5dbafc1c9af33a63', 'SQL From File', 'Initialize the Database Schema', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('mt2846_global_id_v3', 'unknown', 'de/communardo/kenmei/database/update/v1.1/db.changelog.v1.1.refactor.xml', '2016-06-03 21:34:34.225+02', '369b883e28f95386f767185331bce78b', 'Insert Row', 'MT 2846: CR 119 - Refactoring Post Backend: Global Id', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-1555_Better_Database_Interoperability_v2', 'unknown', 'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', '2016-06-03 21:34:36.928+02', 'a8cb84caa81681b6fd5591a06c708d4e', 'SQL From File', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-1555_Better_Database_Interoperability_pre_1_1_4', 'unknown', 'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', '2016-06-03 21:34:37.057+02', '6ed31fd0db19c2a66fc996798d984ae', 'Modify Column', 'Reduce varchar length of key column', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-1521_Purge_Disconnected_External_Auths', 'unknown', 'de/communardo/kenmei/database/update/v1.1.3/db.changelog.v1.1.3.xml', '2016-06-03 21:34:37.068+02', '2f2d42d3b88a45b1857590a0f80dc75', 'Custom SQL', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('MT_3395_drop_source_column_from_blog_members', 'unknown', 'de/communardo/kenmei/database/update/v1.1.4/db.changelog.v1.1.4.xml', '2016-06-03 21:34:37.082+02', '12af7efa26d7a9eefaedfcd1ad9e2d24', 'Drop Column', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('CR_115_Add_Application_Settings_1', 'unknown', 'de/communardo/kenmei/database/update/v1.1.4/db.changelog.v1.1.4.xml', '2016-06-03 21:34:39.396+02', '63fdc9f116fec14a18b3c3caaa2f6c26', 'Create Table', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('CR_115_Remove_Client_Proxy_Url', 'unknown', 'de/communardo/kenmei/database/update/v1.1.4/db.changelog.v1.1.4.xml', '2016-06-03 21:34:39.474+02', '6ff27dc938b73dd590d32872eef4b068', 'Drop Column', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI_1577_Remove_crc_filesystem_config', 'unknown', 'de/communardo/kenmei/database/update/v1.1.4/db.changelog.v1.1.4.xml', '2016-06-03 21:34:39.486+02', 'f675d97e34cf9e8f6f42c08df83e4366', 'Drop Table', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI_1555_Rename_reserved_keywords', 'unknown', 'de/communardo/kenmei/database/update/v1.1.4/db.changelog.v1.1.4.xml', '2016-06-03 21:34:39.499+02', 'd8bb446dfba4c335fa7ecc4f5381a919', 'Rename Column (x2)', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-1857_Option_for_system_notes_per_blog', 'unknown', 'de/communardo/kenmei/database/update/v1.1.4/db.changelog.v1.1.4.xml', '2016-06-03 21:34:39.52+02', '3710cfcc8722190afbabf4cdfbd1c50', 'Add Column, Add Not-Null Constraint', 'KENMEI-1857 allow disabling of system notes per blog', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-1578_Felder_key_und_value_in_Modellen_aendern_v2', 'unknown', 'de/communardo/kenmei/database/update/v1.2/db.changelog.v1.2.xml', '2016-06-03 21:34:39.534+02', '93d4a658b399a9dbd9f5499b3629cf1', 'Rename Column (x3)', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-1694_Follow_model', 'unknown', 'de/communardo/kenmei/database/update/v1.2/db.changelog.v1.2.xml', '2016-06-03 21:34:39.698+02', '5e356a200c1a5952656661b4ffca8ae', 'Create Table, Add Primary Key, Add Foreign Key Constraint (x2), Create Table, Add Primary Key, Add Foreign Key Constraint (x2)', 'Add required tables for storing follow information', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-1694_Follow-support_for_tags', 'unknown', 'de/communardo/kenmei/database/update/v1.2/db.changelog.v1.2.xml', '2016-06-03 21:34:39.71+02', '6731ae54a2b9defa6af2780d3cff338', 'Drop Unique Constraint', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-1979_Attachments_for_outgoing_mails.', 'unknown', 'de/communardo/kenmei/database/update/v1.2/db.changelog.v1.2.xml', '2016-06-03 21:34:39.721+02', '7461879ab6699adb3297db5bc13289', 'Custom SQL', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-1901_Drop_news_tables', 'unknown', 'de/communardo/kenmei/database/update/v1.2/db.changelog.v1.2.xml', '2016-06-03 21:34:39.739+02', 'e328a03e4b3621b07cd78ac5881b94dc', 'Drop Table (x3)', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-1901_Drop_news_sequence', 'unknown', 'de/communardo/kenmei/database/update/v1.2/db.changelog.v1.2.xml', '2016-06-03 21:34:39.794+02', 'edea245d55ad8bcce612509a9229ec', 'Drop Sequence (x3)', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-2009_Tables_for_changed_LDAP_Config_v2', 'unknown', 'de/communardo/kenmei/database/update/v1.2/db.changelog.v1.2.xml', '2016-06-03 21:34:40.055+02', '341878e047a29522e2fd6f85726e40', 'Create Table (x3), Add Foreign Key Constraint (x2), Add Column', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-2009_Sequences_for_changed_LDAP_Config-PostgreSQL', 'unknown', 'de/communardo/kenmei/database/update/v1.2/db.changelog.v1.2.xml', '2016-06-03 21:34:40.071+02', '5bf0f07d6b6540c7941fe8dcd2f5ea44', 'Create Sequence (x3)', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-2009_Migrate_LDAP_Config-PostgreSQL', 'unknown', 'de/communardo/kenmei/database/update/v1.2/db.changelog.v1.2.xml', '2016-06-03 21:34:40.079+02', 'f555f38b82a2da54cc756e97ca42211', 'Custom SQL', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-2009_Finalize_changed_LDAP_Config', 'unknown', 'de/communardo/kenmei/database/update/v1.2/db.changelog.v1.2.xml', '2016-06-03 21:34:40.094+02', '45746a6edbf040d1eec5c559be13d4', 'Drop Column (x4), Add Not-Null Constraint', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-2009_Refactor_Group_Sync', 'unknown', 'de/communardo/kenmei/database/update/v1.2/db.changelog.v1.2.xml', '2016-06-03 21:34:40.099+02', '379ed5349921bcef239216c3d0b22920', 'Drop Column (x2)', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-2009_Ldap_Config_User_UID', 'unknown', 'de/communardo/kenmei/database/update/v1.2/db.changelog.v1.2.xml', '2016-06-03 21:34:40.108+02', '382cc4905529ba9151032204e58dcee', 'Add Column, Add Not-Null Constraint', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-2009_Ldap_Config_SearchBase_list', 'unknown', 'de/communardo/kenmei/database/update/v1.2/db.changelog.v1.2.xml', '2016-06-03 21:34:40.116+02', '3c6bcc15811c64f0494474d31c35104c', 'Add Column', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-2009_Ldap_External_User_Auth_Additional_Props', 'unknown', 'de/communardo/kenmei/database/update/v1.2/db.changelog.v1.2.xml', '2016-06-03 21:34:40.192+02', 'c92fd0704f4ac6551090f6d4ae43ada', 'Add Column, Custom SQL', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-2008_Ldap_External_Group_Additional_Props', 'unknown', 'de/communardo/kenmei/database/update/v1.2/db.changelog.v1.2.xml', '2016-06-03 21:34:40.202+02', '40578a21037635e7cd0434a3beaeac4', 'Add Column', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-2113_Performanzoptimierung_Direct_Message_v2', 'unknown', 'de/communardo/kenmei/database/update/v1.2/db.changelog.v1.2.xml', '2016-06-03 21:34:40.275+02', '3ead8c215fccb339785d97cdc06deecf', 'Create Table, Add Primary Key, Add Foreign Key Constraint (x2)', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-2113_Performanzoptimierung_Direct_Message_migrate', 'unknown', 'de/communardo/kenmei/database/update/v1.2/db.changelog.v1.2.xml', '2016-06-03 21:34:40.281+02', 'b35b34b640585fac11e4a5df10dfa', 'Custom SQL', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('Fix_Column_Size_In_ROLE2BLOG', 'unknown', 'de/communardo/kenmei/database/update/v1.2/db.changelog.v1.2.xml', '2016-06-03 21:34:40.407+02', '2a9f758ec4728aa16fd4bc3c8c22d19', 'Modify Column', 'Fix of bug in installer script', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-2302_Index_on_core_role2blog', 'unknown', 'de/communardo/kenmei/database/update/v1.2/db.changelog.v1.2.xml', '2016-06-03 21:34:40.51+02', '83576031a02b3bc36cbc918a0c59d79', 'Create Index (x2)', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-2335_UniqueJabberResourceIdPerCluster', 'unknown', 'de/communardo/kenmei/database/update/v1.2/db.changelog.v1.2.xml', '2016-06-03 21:34:40.519+02', 'c298eab7d736523bb2c86346674dabc', 'Custom SQL', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-2389_StartTLS_1', 'unknown', 'de/communardo/kenmei/database/update/v1.2/db.changelog.v1.2.xml', '2016-06-03 21:34:40.53+02', 'c8fe21bfa1ed5b39dc80e84e83a8aa67', 'Insert Row', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-2389_StartTLS_2', 'unknown', 'de/communardo/kenmei/database/update/v1.2/db.changelog.v1.2.xml', '2016-06-03 21:34:40.538+02', 'd5f814f6a5ec5bf9eb501bdbc41c6d41', 'Custom SQL', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-2287_StartTLS_1', 'unknown', 'de/communardo/kenmei/database/update/v1.2/db.changelog.v1.2.xml', '2016-06-03 21:34:40.548+02', 'c629b7e26c3494b0c7ebf583ecce7fdd', 'Insert Row', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-2628_Remove_Wrong_Unique_Constraint_postgres', 'unknown', 'de/communardo/kenmei/database/update/v1.2/db.changelog.v1.2.xml', '2016-06-03 21:34:40.557+02', 'f615d302864b9f069deb569fee1728', 'Drop Unique Constraint (x2), Add Unique Constraint', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-2628_Remove_Wrong_Unique_Constraint_postgres9', 'unknown', 'de/communardo/kenmei/database/update/v1.2/db.changelog.v1.2.xml', '2016-06-03 21:34:42.219+02', '2fb0e2505c9882fd7a4dab537428db2', 'Drop Unique Constraint (x2), Add Unique Constraint', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-Drop_snc_sequences', 'unknown', 'de/communardo/kenmei/database/update/v1.3/db.changelog.v1.3.xml', '2016-06-03 21:34:42.245+02', '6118c2a607cabfecf5fe52443619d', 'Drop Sequence (x2)', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-2154_Jobs_Services_als_Tasks_in_Datenbank', 'unknown', 'de/communardo/kenmei/database/update/v1.3/db.changelog.v1.3.xml', '2016-06-03 21:34:42.662+02', '7cd74b4927f5d543fd4cd4692d129b2f', 'Create Table (x3), Add Foreign Key Constraint (x2), Create Sequence (x3)', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-User-Grop-Properties', 'unknown', 'de/communardo/kenmei/database/update/v1.3/db.changelog.v1.3.xml', '2016-06-03 21:34:42.884+02', '4355887337d1fadc19d12b42c97d8a9', 'Create Table, Add Foreign Key Constraint, Create Table, Add Foreign Key Constraint, Create Sequence (x2)', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-User-Group-Properties_clob', 'unknown', 'de/communardo/kenmei/database/update/v1.3/db.changelog.v1.3.xml', '2016-06-03 21:34:42.894+02', 'b51dcb936e4e658ea6d26c6fd7f0903b', 'Modify Column (x2)', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-Binary-Properties', 'unknown', 'de/communardo/kenmei/database/update/v1.3/db.changelog.v1.3.xml', '2016-06-03 21:34:43.028+02', '901bbfc8af68f655b2185ac26d7b126', 'Create Table', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-Binary-Properties_seq', 'unknown', 'de/communardo/kenmei/database/update/v1.3/db.changelog.v1.3.xml', '2016-06-03 21:34:43.038+02', '96c98613d446acd3c63473a21cfc8236', 'Create Sequence', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-2154_Jobs_Services_als_Tasks_in_Datenbank-Rename-Columns', 'unknown', 'de/communardo/kenmei/database/update/v1.3/db.changelog.v1.3.xml', '2016-06-03 21:34:43.048+02', '26b5c92cd2dc3debe146334576775857', 'Rename Column (x2)', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-2240_Spalte_discussionId_in_core_note', 'unknown', 'de/communardo/kenmei/database/update/v1.3/db.changelog.v1.3.xml', '2016-06-03 21:34:44.844+02', 'bedafeee1e858694c28879e61a2c5b26', 'Add Column', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-2240_discussionId_befuellen-PostgreSQL', 'unknown', 'de/communardo/kenmei/database/update/v1.3/db.changelog.v1.3.xml', '2016-06-03 21:34:44.849+02', 'ab718af88c8eede22af2fbce8ecc18', 'Custom SQL', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-Note-properties', 'unknown', 'de/communardo/kenmei/database/update/v1.3/db.changelog.v1.3.xml', '2016-06-03 21:34:44.969+02', '1f46342bf6cb714d605e51e9736d53', 'Create Table, Add Foreign Key Constraint, Create Sequence', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-Note-Properties_clob', 'unknown', 'de/communardo/kenmei/database/update/v1.3/db.changelog.v1.3.xml', '2016-06-03 21:34:44.979+02', '61daee3a8c3850f6402bdb11ab5a49bb', 'Modify Column', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-2261-LDAP_Authentication_SASL_Support', 'unknown', 'de/communardo/kenmei/database/update/v1.3/db.changelog.v1.3.xml', '2016-06-03 21:34:44.987+02', '66db577a2f9c6ee89bdfa2c4848b117f', 'Add Column', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('Add_Last_Modification_Date_To_Properties', 'unknown', 'de/communardo/kenmei/database/update/v1.3/db.changelog.v1.3.xml', '2016-06-03 21:34:45.451+02', 'a452ff6128142840f7a082334267d02b', 'Add Column (x4)', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-2288_remove_old_caching_properties', 'unknown', 'de/communardo/kenmei/database/update/v1.3/db.changelog.v1.3.xml', '2016-06-03 21:34:45.46+02', '93ac2c8652ac9ce97b5682a4a85cc0', 'Custom SQL', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-2465_Index_for_better_performance_postgresql', 'unknown', 'de/communardo/kenmei/database/update/v1.3.1/db.changelog.v1.3.1.xml', '2016-06-03 21:34:45.855+02', 'a4bdaaccf1152ba3570a13a817b97c', 'Custom SQL', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('Like-Funktion', 'UNKNOWN', 'de/communardo/kenmei/database/update/v1.3.2/db.changelog.v1.3.2.xml', '2016-06-03 21:34:45.96+02', '3bcadf67c0e431aad437221dbfbe6cf5', 'Create Table', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('Like-Funktion_PostgreSQL', 'UNKNOWN', 'de/communardo/kenmei/database/update/v1.3.2/db.changelog.v1.3.2.xml', '2016-06-03 21:34:45.976+02', '84523c98606261fbb3408015c0faf7d0', 'Add Foreign Key Constraint (x2), Create Sequence', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-2601_Index-fuer-discussion_id-entfernen', 'UNKNOWN', 'de/communardo/kenmei/database/update/v1.3.2/db.changelog.v1.3.2.xml', '2016-06-03 21:34:46.033+02', '4ae911eeae58366bebf8ae724282571a', 'Drop Index', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-2601_Index-fuer-discussion_id-anlegen', 'UNKNOWN', 'de/communardo/kenmei/database/update/v1.3.2/db.changelog.v1.3.2.xml', '2016-06-03 21:34:46.099+02', 'a6483cd076bdafe25975ac34fcfc3384', 'Create Index', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-Blog-Properties', 'unknown', 'de/communardo/kenmei/database/update/v1.3.2/db.changelog.v1.3.2.xml', '2016-06-03 21:34:46.21+02', '6f5bc99c34318034fda2208600b773', 'Create Table', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-Blog-Properties_fkc', 'unknown', 'de/communardo/kenmei/database/update/v1.3.2/db.changelog.v1.3.2.xml', '2016-06-03 21:34:46.22+02', 'e8ba9d17768c1e979aec77c75996bf1a', 'Add Foreign Key Constraint', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-Blog-Properties_seq', 'unknown', 'de/communardo/kenmei/database/update/v1.3.2/db.changelog.v1.3.2.xml', '2016-06-03 21:34:46.23+02', 'd8c0a8f748b21f124869fe3de21cfeef', 'Create Sequence', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-2616_Index_auf_Tag_lower_name_anlegen_postgresql', 'UNKNOWN', 'de/communardo/kenmei/database/update/v1.3.2/db.changelog.v1.3.2.xml', '2016-06-03 21:34:46.317+02', '5c148828a893cc4bb48f136d04c9db', 'Create Index', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-2608-Refactor MostUsedBlogs', 'unknown', 'de/communardo/kenmei/database/update/v1.3.3/db.changelog.v1.3.3.xml', '2016-06-03 21:34:46.328+02', 'f64386ffc88d3fef2b45eb268f6769b5', 'Drop Table', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('kenmei-1988-ramconnector-entfernen', 'unknown', 'de/communardo/kenmei/database/update/v1.3.3/db.changelog.v1.3.3.xml', '2016-06-03 21:34:46.338+02', '9569702cd7d0b74787b6ab783602989', 'Drop Table', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-2629-Reset-Primary-Auth-on-deactivated-external-auth', 'unknown', 'de/communardo/kenmei/database/update/v1.3.3/db.changelog.v1.3.3.xml', '2016-06-03 21:34:46.344+02', 'd9932ed049603f4d816789d0454a285a', 'Update Data (x2)', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-3719_clean_postgresql', 'unknown', 'de/communardo/kenmei/database/update/v1.3.3/db.changelog.v1.3.3.xml', '2016-06-03 21:34:46.395+02', 'a485d035446d2f867d8857ac6ab8e1', 'Custom SQL', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-3719_2', 'unknown', 'de/communardo/kenmei/database/update/v1.3.3/db.changelog.v1.3.3.xml', '2016-06-03 21:34:46.461+02', 'f23f1e2a63e54359e668ff1c60de1e', 'Add Unique Constraint', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('Unknown', 'KENMEI-4109-TagClearance_entfernen', 'de/communardo/kenmei/database/update/v2.1/db.changelog.v2.1.1.xml', '2016-06-03 21:34:46.476+02', '1e29d9d524219933d8377380bdc495', 'Drop Table (x3)', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('Unknown', 'KENMEI-4109-TagClearance_entfernen-PostgreSQL', 'de/communardo/kenmei/database/update/v2.1/db.changelog.v2.1.1.xml', '2016-06-03 21:34:46.485+02', 'aaaa4fdaa73a5af8dbc6a1328595dbe', 'Drop Sequence', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-4017-Create_entity_to_tag_association', 'UNKNOWN', 'de/communardo/kenmei/database/update/v2.1/db.changelog.v2.1.1.xml', '2016-06-03 21:34:46.493+02', '93ecae16b48c48e51cbc4b317dffa7cf', 'Create Table', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-4017-Create_entity_to_tag_association_Constraints', 'UNKNOWN', 'de/communardo/kenmei/database/update/v2.1/db.changelog.v2.1.1.xml', '2016-06-03 21:34:46.563+02', 'f5b4027ca2a47f2ad953c6b7b8aa0b3', 'Create Index, Add Foreign Key Constraint (x2)', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-4017-Create_tag2names', 'UNKNOWN', 'de/communardo/kenmei/database/update/v2.1/db.changelog.v2.1.1.xml', '2016-06-03 21:34:46.574+02', 'dde5e06d91cd71ddb96a3dbd5dfcbaaa', 'Create Table', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-4017-Create_tag2names_Constraints', 'UNKNOWN', 'de/communardo/kenmei/database/update/v2.1/db.changelog.v2.1.1.xml', '2016-06-03 21:34:46.642+02', '80ff97a3048ada5f861cce2e96c321a', 'Add Foreign Key Constraint (x2), Add Unique Constraint', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-4017-Create_tag2descriptions', 'UNKNOWN', 'de/communardo/kenmei/database/update/v2.1/db.changelog.v2.1.1.xml', '2016-06-03 21:34:46.652+02', 'd555bf5d16beaec8a1224b7efece5e5', 'Create Table', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-4017-Create_tag2descriptions_Constraints', 'UNKNOWN', 'de/communardo/kenmei/database/update/v2.1/db.changelog.v2.1.1.xml', '2016-06-03 21:34:46.729+02', 'c02defbee8416feb3bce5f405fdfe9', 'Add Foreign Key Constraint (x2), Add Unique Constraint', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-4017-Add_Tag_Store_information', 'UNKNOWN', 'de/communardo/kenmei/database/update/v2.1/db.changelog.v2.1.1.xml', '2016-06-03 21:34:47.162+02', 'ed6fd58dac11fe96a55b96cb2a12b83e', 'Modify Column, Rename Column, Add Column', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-4017-Add_Tag_Store_information_2_not_mysql', 'UNKNOWN', 'de/communardo/kenmei/database/update/v2.1/db.changelog.v2.1.1.xml', '2016-06-03 21:34:47.172+02', 'f13bc14d5a9d781ca41e2c3d0456e2', 'Drop Unique Constraint, Rename Column', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-4017-Consolidate_tags', 'UNKNOWN', 'de/communardo/kenmei/database/update/v2.1/db.changelog.v2.1.1.xml', '2016-06-03 21:34:47.195+02', '42d023c30d8171784495ea347c85c', 'SQL From File', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-4017-Add_Tag_Store_information_2_add_index', 'UNKNOWN', 'de/communardo/kenmei/database/update/v2.1/db.changelog.v2.1.1.xml', '2016-06-03 21:34:47.263+02', '7a1ce9db257c89aa7aacd4125416230', 'Add Unique Constraint', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-4153_postgresql', 'UNKNOWN', 'de/communardo/kenmei/database/update/v2.1/db.changelog.v2.1.1.xml', '2016-06-03 21:34:47.274+02', '6c68a227ab93536c8c2e621888e1bc6', 'Custom SQL', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-4153_activate_default_blog_postgresql', 'UNKNOWN', 'de/communardo/kenmei/database/update/v2.1/db.changelog.v2.1.1.xml', '2016-06-03 21:34:47.283+02', '9879efed9f9359f546cf5e23ec5cb599', 'Custom SQL', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-3997_Ausschalten_des_Features_Selbstregistrierung', 'Commuote', 'de/communardo/kenmei/database/update/v2.1/db.changelog.v2.1.1.xml', '2016-06-03 21:34:47.294+02', '35adb7e64b6c407b9877a3215362c3ef', 'Add Column, Add Foreign Key Constraint', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-3997_Selbstregistrierung_remove2_postgresql', 'Commuote', 'de/communardo/kenmei/database/update/v2.1/db.changelog.v2.1.1.xml', '2016-06-03 21:34:47.304+02', 'fc69e1804173f442a9d0ed3fc3c51ceb', 'Drop Foreign Key Constraint', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-3997_Selbstregistrierung_remove2', 'Commuote', 'de/communardo/kenmei/database/update/v2.1/db.changelog.v2.1.1.xml', '2016-06-03 21:34:47.313+02', 'eabe999544ee2285adc23e6c59c247', 'Drop Column', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-3997_Selbstregistrierung_2', 'Commuote', 'de/communardo/kenmei/database/update/v2.1/db.changelog.v2.1.1.xml', '2016-06-03 21:34:47.321+02', '12e56d5965b4e7f44e9a7bc7892bbf46', 'Add Column', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-3005_fix_column_name', 'UNKNOWN', 'de/communardo/kenmei/database/update/v2.1/db.changelog.v2.1.1.xml', '2016-06-03 21:34:47.329+02', '141350baf72f232673fde54fa9a863aa', 'Rename Column', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-3005_fix_large_table_name', 'Commuote', 'de/communardo/kenmei/database/update/v2.1/db.changelog.v2.1.1.xml', '2016-06-03 21:34:47.339+02', 'c8ae46c35b97d7c6ba91bd77458367', 'Rename Table', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-3005_fix_large_table_name_seq_postgresql', 'Communote', 'de/communardo/kenmei/database/update/v2.1/db.changelog.v2.1.1.xml', '2016-06-03 21:34:47.347+02', '7ce384f05718a881138ff64069921fb7', 'Rename Table', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-4357-TagCloud-bei-Nutzerübersicht-geht-nicht_postgresql', 'Communote', 'de/communardo/kenmei/database/update/v2.1/db.changelog.v2.1.1.xml', '2016-06-03 21:34:47.355+02', '18f9a77c21bb211b85566f72fb1c4', 'Add Not-Null Constraint (x2)', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-3005_change_property_value_types_postgres', 'UNKNOWN', 'de/communardo/kenmei/database/update/v2.1/db.changelog.v2.1.1.xml', '2016-06-03 21:34:47.878+02', 'e15e49ad8479ded83a474140864ae', 'Custom SQL', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-3005_rename_tables_new', 'UNKNOWN', 'de/communardo/kenmei/database/update/v2.1/db.changelog.v2.1.1.xml', '2016-06-03 21:34:47.892+02', 'e6b3d3595e3c557cc17e79983a14515', 'Rename Table (x3)', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-3005_drop_not_null_constraints_postgres', 'UNKNOWN', 'de/communardo/kenmei/database/update/v2.1/db.changelog.v2.1.1.xml', '2016-06-03 21:34:47.904+02', 'c2a3fa44d561a98bd6d9fe94194398b', 'Custom SQL', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-3005_change_sequence_name_postgres_24', 'UNKNOWN', 'de/communardo/kenmei/database/update/v2.1/db.changelog.v2.1.1.xml', '2016-06-03 21:34:47.916+02', '206b9b2bd1ef74f5fcd19e1f257de5', 'Custom SQL', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-4164-Dem Default Blog einen Blognamen zuweisen_postgres', 'Communote', 'de/communardo/kenmei/database/update/v2.1/db.changelog.v2.1.1.xml', '2016-06-03 21:34:47.924+02', '6f629ef8ac4755728c48e846210194b', 'Custom SQL', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-4164-Dem Default Blog einen Blognamen zuweisen_all', 'Communote', 'de/communardo/kenmei/database/update/v2.1/db.changelog.v2.1.1.xml', '2016-06-03 21:34:47.931+02', 'b13eb5fd90ccf643748dbafdbffc9ac', 'Custom SQL', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-3005_set_null_postgresql', 'UNKNOWN', 'de/communardo/kenmei/database/update/v2.1/db.changelog.v2.1.1.xml', '2016-06-03 21:34:47.939+02', '975bed57a12772868baa145b4ff77', 'Custom SQL', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-3005_rename_tables_v2', 'UNKNOWN', 'de/communardo/kenmei/database/update/v2.1/db.changelog.v2.1.1.xml', '2016-06-03 21:34:47.947+02', '95325bf04ee3b01dc3f9fdbf25f28f3a', 'Rename Table', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-3005_rename_sequence_postgresql_v2', 'UNKNOWN', 'de/communardo/kenmei/database/update/v2.1/db.changelog.v2.1.1.xml', '2016-06-03 21:34:47.954+02', '1f5bf27d5944d7fe863f76a66a782bd', 'Custom SQL', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('Update global ids for tags_postgresql', 'Communote', 'de/communardo/kenmei/database/update/v2.1/db.changelog.v2.1.1.xml', '2016-06-03 21:34:47.96+02', '96fcbf41537b7a9e4a2fec4e8e64486d', 'Custom SQL', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-4216_alter_anonymized_user_prefix_postgresql', 'Communote', 'de/communardo/kenmei/database/update/v2.1/db.changelog.v2.1.1.xml', '2016-06-03 21:34:47.966+02', '294c10731e7f4e10e9203fab8e3a058', 'Custom SQL', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-4504-SQL-Fulltextfunction-Postgresql-Note', 'Unknown', 'de/communardo/kenmei/database/update/v2.1/db.changelog.fulltext.v2.1.1.xml', '2016-06-03 21:34:47.985+02', 'f2ffbf21c6529be90fae99a9fd6ee30', 'Custom SQL', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-4504-SQL-Fulltextfunction-Postgresql-Topic-Drop', 'Unknown', 'de/communardo/kenmei/database/update/v2.1/db.changelog.fulltext.v2.1.1.xml', '2016-06-03 21:34:47.989+02', 'e1183ecba8e33eb79ddb9fab70d84ccf', 'Custom SQL', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-4504-SQL-Fulltextfunction-Postgresql-Tag-Drop', 'Unknown', 'de/communardo/kenmei/database/update/v2.1/db.changelog.fulltext.v2.1.1.xml', '2016-06-03 21:34:47.995+02', '9fa7e111fb7e3a91fd72d43aa8557b8d', 'Custom SQL', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-4504-SQL-Fulltextfunction-Postgresql-User-Drop', 'Unknown', 'de/communardo/kenmei/database/update/v2.1/db.changelog.fulltext.v2.1.1.xml', '2016-06-03 21:34:48.001+02', '4996b964a5ad393834c8c7b433e063', 'Custom SQL', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-4504-SQL-Fulltextfunction-Postgresql-User-Profile-Drop', 'Unknown', 'de/communardo/kenmei/database/update/v2.1/db.changelog.fulltext.v2.1.1.xml', '2016-06-03 21:34:48.007+02', '36389a7816a4696815414480c8e6e66', 'Custom SQL', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-4504-SQL-Fulltextfunction-Postgresql-Custom-Message-Drop', 'Communote', 'de/communardo/kenmei/database/update/v2.1/db.changelog.fulltext.v2.1.1.xml', '2016-06-03 21:34:48.012+02', 'f9d97d2b6746c4d4fb2488a5dc409c', 'Custom SQL', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-4774_Eigenschaften_fuer_Plugins', 'Communote', 'de/communardo/kenmei/database/update/v2.2/db.changelog.v2.2.xml', '2016-06-03 21:34:48.095+02', '60d6e794152c2bc11a3ce93698f988', 'Create Table', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-4774_Eigenschaften_fuer_Plugins_seq', 'Communote', 'de/communardo/kenmei/database/update/v2.2/db.changelog.v2.2.xml', '2016-06-03 21:34:48.105+02', 'eed61ce5d749ec6aca0cbd443361579', 'Create Sequence', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-4774_Eigenschaften_fuer_Plugins_unique', 'Communote', 'de/communardo/kenmei/database/update/v2.2/db.changelog.v2.2.xml', '2016-06-03 21:34:48.17+02', '4d6666ef5d19164f3b101864397bccef', 'Add Unique Constraint', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-4774_Column_for_application_property', 'Communote', 'de/communardo/kenmei/database/update/v2.2/db.changelog.v2.2.xml', '2016-06-03 21:34:48.344+02', 'a7db319957f9d9988b0156db5ebb937', 'Add Column', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-4781_Refactoring_von_ExternalObjectManagement_v2', 'Communote', 'de/communardo/kenmei/database/update/v2.2/db.changelog.v2.2.xml', '2016-06-03 21:34:48.615+02', 'ff956eeef12bf22171df6ebd4168865', 'Modify Column (x2), Add Column (x2), Update Data (x2), Add Not-Null Constraint (x2)', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-3838_Domain_Controller_LDAP_Server_automatisch_ermitteln', 'Communote', 'de/communardo/kenmei/database/update/v2.2/db.changelog.v2.2.xml', '2016-06-03 21:34:48.745+02', '3e644577adfac5759ae154e4497737d', 'Add Column', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-3838_Set_Dynamic_Mode_to_false', 'Communote', 'de/communardo/kenmei/database/update/v2.2/db.changelog.v2.2.xml', '2016-06-03 21:34:48.754+02', 'ea36cdad1cb713fb847563ab187b665d', 'Update Data', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-5019_UniqueConstraint_ExternalGroups_postgresql', 'Communote', 'de/communardo/kenmei/database/update/v2.2/db.changelog.v2.2.xml', '2016-06-03 21:34:49.041+02', '6f12557e2359a6c725171aa05ded6e', 'Modify Column, Add Unique Constraint', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-4931_Thema einsehbar_obwohl_keine_Berechtigung_pg_2', 'Communote', 'de/communardo/kenmei/database/update/v2.2/db.changelog.v2.2.xml', '2016-06-03 21:34:49.048+02', '3d8a4aa74d3729c76f149a61f9e4f1', 'SQL From File', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-4817-Follow-optimieren-Create-Index-2', 'Communote', 'de/communardo/kenmei/database/update/v2.2/db.changelog.v2.2.xml', '2016-06-03 21:34:49.145+02', 'bdedb6b88c186a7232aee1c0cef8220', 'Create Index (x2)', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-5241-Posgtres-Volltextsuche-sehr-langsam ', 'Communote', 'de/communardo/kenmei/database/update/v2.2/db.changelog.v2.2.xml', '2016-06-03 21:34:49.156+02', 'f8f5cd8a997c1fa3444a3aa427e7', 'Custom SQL', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-5395-LastDiscussionNoteCreationDate-einfuegen', 'Communote', 'de/communardo/kenmei/database/update/v2.3/db.changelog.v2.3.xml', '2016-06-03 21:34:49.163+02', '7db328c1f9ba177cab22cef6b335a3f8', 'Add Column', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-5416_Setzen_LastDiscussionNoteCreationDate_pg_4', 'Communote', 'de/communardo/kenmei/database/update/v2.3/db.changelog.v2.3.xml', '2016-06-03 21:34:49.172+02', '8332ac9a19a37e24d5ebb6ddf3b839', 'Custom SQL', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-5455_Alternative_Methode_fuer_Nachladender_Notes', 'Communote', 'de/communardo/kenmei/database/update/v2.3/db.changelog.v2.3.1.xml', '2016-06-03 21:34:49.232+02', '65d83c24ca415f4d6b533d05163c022', 'Custom SQL', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-5524_1_1', 'Communote', 'de/communardo/kenmei/database/update/v2.3/db.changelog.v2.3.1.xml', '2016-06-03 21:34:49.298+02', '78b6368bd5b2ed7ce6adc0992b709586', 'Create Table', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-5524_1_2', 'Communote', 'de/communardo/kenmei/database/update/v2.3/db.changelog.v2.3.1.xml', '2016-06-03 21:34:49.313+02', 'b3f6fd8aa13f6553ae28b334d5a50f7', 'Add Foreign Key Constraint (x2)', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-5524_2', 'Communote', 'de/communardo/kenmei/database/update/v2.3/db.changelog.v2.3.1.xml', '2016-06-03 21:34:49.322+02', '1be3c4fd17523430d591c232776497bd', 'Create Sequence', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-5524_5', 'Communote', 'de/communardo/kenmei/database/update/v2.3/db.changelog.v2.3.1.xml', '2016-06-03 21:34:49.381+02', 'd29af978c3eaf6beb1b5d925dab827', 'Add Unique Constraint', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-5524_6', 'Communote', 'de/communardo/kenmei/database/update/v2.3/db.changelog.v2.3.1.xml', '2016-06-03 21:34:49.392+02', '7cedf3fcefd676807524cce4973bf329', 'Add Column, Add Foreign Key Constraint', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-5562_Index_last_discussion_creation_date', 'Communote', 'de/communardo/kenmei/database/update/v2.3/db.changelog.v2.3.1.xml', '2016-06-03 21:34:49.455+02', 'b0299e1c45133f98f5bb4f6235a78a', 'Custom SQL', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-5563-TIMESTAMP-without-TimeZone_pg', 'Communote', 'de/communardo/kenmei/database/update/v2.3/db.changelog.v2.3.1.xml', '2016-06-03 21:34:49.903+02', 'b84f8fedafa27d696ff895f7fba87d', 'Custom SQL', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-5034-Angemeldet_bleiben_LDAP', 'Communote', 'de/communardo/kenmei/database/update/v2.3/db.changelog.v2.3.2.xml', '2016-06-03 21:34:49.912+02', '62806cf1a8b837291c472ff30fd7798', 'Add Column', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-5555_Anonymisieren_Nutzer_mit_vielen_Notes_sehr_lange', 'Communote', 'de/communardo/kenmei/database/update/v2.3/db.changelog.v2.3.2.xml', '2016-06-03 21:34:50.022+02', '1230ad57dbf563db0a680e9f779d8', 'Create Index (x2)', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('Remove_QueryHelper', 'Communote', 'de/communardo/kenmei/database/update/v2.4/db.changelog.v2.4.xml', '2016-06-03 21:34:50.031+02', 'c9b4be9fd66455b18d2563688146a26', 'Drop Table', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-5742_Packagestruktur_aufraeumen_Tasks_v2', 'Communote', 'de/communardo/kenmei/database/update/v2.4/db.changelog.v2.4.xml', '2016-06-03 21:34:50.04+02', '16bfbfd1c0f47030b4da5777c3f672', 'Custom SQL', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-5828-Localized-Email-Signature_pg', 'Communote', 'de/communardo/kenmei/database/update/v2.4/db.changelog.v2.4.xml', '2016-06-03 21:34:50.046+02', '17dbbed56c92497fcaa4222b982fa48', 'Custom SQL', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-5828-Localized-Email-Signature_delete_old', 'Communote', 'de/communardo/kenmei/database/update/v2.4/db.changelog.v2.4.xml', '2016-06-03 21:34:50.054+02', '8bcc4071f291a6ca8f748e932debc26', 'Custom SQL', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-5828-Localized-Email-imprint_terms', 'Communote', 'de/communardo/kenmei/database/update/v2.4/db.changelog.v2.4.xml', '2016-06-03 21:34:50.06+02', 'a787203f141bd68c1913ddaffeb1b6b', 'Custom SQL', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-5859_Neuer_Inhaltstyp_Anhaenge_pg', 'Communote', 'de/communardo/kenmei/database/update/v2.4/db.changelog.v2.4.xml', '2016-06-03 21:34:50.065+02', 'cfc567b9fff6e38d60f676f69e298917', 'Custom SQL', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-5923_Benachrichtung_Themen_und_Diskussionen', 'Communote', 'de/communardo/kenmei/database/update/v2.4/db.changelog.v2.4.xml', '2016-06-03 21:34:51.511+02', '4189499ff788adfaac769c0d75e5f', 'Add Column', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-4847-TIMESTAMP-without-TimeZone_pg_1', 'Communote', 'de/communardo/kenmei/database/update/v2.4/db.changelog.v2.4.xml', '2016-06-03 21:34:52.029+02', 'a9e69334eacd309eb9568cf1af9d5fc4', 'Custom SQL', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-6098-Bild-Filter-zeigt-Nachrichten-ohne-Bild', 'Communote', 'de/communardo/kenmei/database/update/v2.4/db.changelog.v2.4.xml', '2016-06-03 21:34:52.04+02', '7ecafa23555e8df5323e84979bbb184', 'Custom SQL', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-6122-Add_missing_index', 'Communote', 'de/communardo/kenmei/database/update/v2.4/db.changelog.v2.4.xml', '2016-06-03 21:34:52.114+02', '4f7bc98c7c74ba4ab5cae9873ab19', 'Create Index', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-4847-TIMESTAMP-without-TimeZone_pg_v3', 'Communote', 'de/communardo/kenmei/database/update/v3.x/db.changelog.v3.0.xml', '2016-06-03 21:34:53.2+02', '132e78b531570e9caa911e58169c4d7', 'Custom SQL', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-6192_Themenhierarchien_Umsetzung_blog2blog', 'Communote', 'de/communardo/kenmei/database/update/v3.x/db.changelog.v3.0.xml', '2016-06-03 21:34:53.254+02', 'ffe2ff18c648d3e68139433d1fa8da8f', 'Create Table', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-6192_Themenhierarchien_Umsetzung_blog2blog_resolved', 'Communote', 'de/communardo/kenmei/database/update/v3.x/db.changelog.v3.0.xml', '2016-06-03 21:34:53.304+02', '79cb9a52016696d76b8219e79d1874f', 'Create Table', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-6192_Themenhierarchien_Umsetzung_foreign_keys', 'Communote', 'de/communardo/kenmei/database/update/v3.x/db.changelog.v3.0.xml', '2016-06-03 21:34:53.316+02', '52e253dadf622f36c1a81be4dc46fe', 'Add Foreign Key Constraint (x2)', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-6192_Themenhierarchien_Umsetzung_sequence_postgresql', 'Communote', 'de/communardo/kenmei/database/update/v3.x/db.changelog.v3.0.xml', '2016-06-03 21:34:53.324+02', '445568a96b3b49a1c5f72e63ab29920', 'Custom SQL', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-6192_Themenhierarchien_Umsetzung_constraint_2', 'Communote', 'de/communardo/kenmei/database/update/v3.x/db.changelog.v3.0.xml', '2016-06-03 21:34:53.382+02', '42f59fa11a3443dc208225977a1ed8', 'Add Unique Constraint', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-6192_Themenhierarchien_Umsetzung_toplevel_flag', 'Communote', 'de/communardo/kenmei/database/update/v3.x/db.changelog.v3.0.xml', '2016-06-03 21:34:53.807+02', 'acaeb509982a5828c99841b1ccaa546', 'Add Column', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-6256-NavigationItems', 'Communote', 'de/communardo/kenmei/database/update/v3.x/db.changelog.v3.0.xml', '2016-06-03 21:34:53.918+02', 'f36ce97067d97f5cf0892041b9bf15f7', 'Create Table', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-6256-NavigationItems_foreign_keys', 'Communote', 'de/communardo/kenmei/database/update/v3.x/db.changelog.v3.0.xml', '2016-06-03 21:34:53.928+02', '7fa1bdfeec2113f5f29f448b918f20b6', 'Add Foreign Key Constraint', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-6256-NavigationItems_sequence_postgresql', 'Communote', 'de/communardo/kenmei/database/update/v3.x/db.changelog.v3.0.xml', '2016-06-03 21:34:53.936+02', '7cab3269cf2d9f2bac72dfdc17296cba', 'Custom SQL', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-6256-NavigationItems_rename_index', 'Communote', 'de/communardo/kenmei/database/update/v3.x/db.changelog.v3.0.xml', '2016-06-03 21:34:53.944+02', '33d323521e9aa43155c9504ccd4134', 'Rename Column', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-6256-NavigationItems_add_name', 'Communote', 'de/communardo/kenmei/database/update/v3.x/db.changelog.v3.0.xml', '2016-06-03 21:34:53.952+02', 'd884929523cd4d419f67af45537892a', 'Add Column', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-6270-ClientAktivierung-Make-The-Git-Hash-Work', 'Communote', 'de/communardo/kenmei/database/update/v3.x/db.changelog.v3.0.xml', '2016-06-03 21:34:54.118+02', '1e5cd6666e7728841c3453ebd29043b9', 'Modify Column', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-1264_Offline-Autosave', 'Communote', 'de/communardo/kenmei/database/update/v3.x/db.changelog.v3.1.xml', '2016-06-03 21:34:54.128+02', 'beb0cac28fc0a9df3b983d6cefefd9f0', 'Add Column', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-1264_Offline-Autosave_Foreign_Keys', 'Communote', 'de/communardo/kenmei/database/update/v3.x/db.changelog.v3.1.xml', '2016-06-03 21:34:54.138+02', 'f94dc4491622b20d6bd97b3e358698a', 'Add Foreign Key Constraint', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-1264_Offline-Autosave_AddUploader_postgresql', 'Communote', 'de/communardo/kenmei/database/update/v3.x/db.changelog.v3.1.xml', '2016-06-03 21:34:54.147+02', '8ce61da9acec72e46e56c2aaa6cc13d', 'Custom SQL', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-7199-Drop-SharePoint-Configuration', 'Communote', 'de/communardo/kenmei/database/update/v3.x/db.changelog.v3.2.xml', '2016-06-03 21:34:54.159+02', 'dc9630efe277bfd22645bcac93646ec', 'Drop Table, Custom SQL', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-7154-Terms-Of_Use_1-postgres', 'Communote', 'de/communardo/kenmei/database/update/v3.x/db.changelog.v3.2.xml', '2016-06-03 21:34:54.166+02', '393e8a68cb75ba02cfad698a698e685', 'Custom SQL', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-7154-Terms-Of_Use_2-postgres', 'Communote', 'de/communardo/kenmei/database/update/v3.x/db.changelog.v3.2.xml', '2016-06-03 21:34:54.173+02', '31e72cd89f88f1b34d72cf54d1f2abb', 'Custom SQL', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-7369-Drop-CRC-Configuration', 'Communote', 'de/communardo/kenmei/database/update/v3.x/db.changelog.v3.3.xml', '2016-06-03 21:34:54.182+02', 'be4e654ee8abde97480e229775678a7', 'Drop Table (x2)', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-7392-Properties-for-Attachments-v2', 'Communote', 'de/communardo/kenmei/database/update/v3.x/db.changelog.v3.3.xml', '2016-06-03 21:34:54.342+02', 'cd90d6cdfddc6fd3132373d6ea1d1ea9', 'Create Table', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-7392-Properties-for-Attachments_fkc_2-v2', 'Communote', 'de/communardo/kenmei/database/update/v3.x/db.changelog.v3.3.xml', '2016-06-03 21:34:54.354+02', '6553f6171bbb6e5d74ccd51a723fdc40', 'Add Foreign Key Constraint', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-7392-Properties-for-Attachments_seq-v2', 'Communote', 'de/communardo/kenmei/database/update/v3.x/db.changelog.v3.3.xml', '2016-06-03 21:34:54.364+02', 'a7be7f3965b8bb841343a49fdcd6884', 'Create Sequence', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-7383-Index-for-Attachment-ContentId_postgresl', 'Communote', 'de/communardo/kenmei/database/update/v3.x/db.changelog.v3.3.xml', '2016-06-03 21:34:54.575+02', '754efb261d3ee3a9208bac41c581b4d1', 'Modify Column, Add Unique Constraint', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-7385-Crawl-Last-Modification-Date', 'Communote', 'de/communardo/kenmei/database/update/v3.x/db.changelog.v3.3.xml', '2016-06-03 21:34:54.585+02', '5dbf4f53deff5ccee7175ce559252c7', 'Add Column', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-7385-Crawl-Last-Modification-Date-Not-Null', 'Communote', 'de/communardo/kenmei/database/update/v3.x/db.changelog.v3.3.xml', '2016-06-03 21:34:54.594+02', '81be19ddddc7fa6b719d8a3d6f0889f', 'Modify Column', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-7386-Crawl-Last-Modification-Date-Note', 'Communote', 'de/communardo/kenmei/database/update/v3.x/db.changelog.v3.3.xml', '2016-06-03 21:34:54.603+02', 'ed93ec5d51918c57aaf0415a1228ee88', 'Add Column', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-7386-Crawl-Last-Modification-Date-Note-Not-Null', 'Communote', 'de/communardo/kenmei/database/update/v3.x/db.changelog.v3.3.xml', '2016-06-03 21:34:54.612+02', 'eaddef8b311ed12935ccfdb45b866f3', 'Modify Column', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-7524_remove_license_mode_subscription_seq', 'Communote', 'com/communote/server/liquibase/update/v3.x/db.changelog.v3.4.xml', '2016-06-03 21:34:54.618+02', 'bbaeb9fb36c521e02a2dbe3bf564bcf4', 'Drop Sequence', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-7524_remove_license_mode_subscription', 'Communote', 'com/communote/server/liquibase/update/v3.x/db.changelog.v3.4.xml', '2016-06-03 21:34:54.627+02', '716e958a59e65ba079b9ab4b2c4cb5c', 'Drop Table', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('installation_type_protection', 'Communote', 'com/communote/server/liquibase/update/v3.x/db.changelog.v3.4.xml', '2016-06-03 21:34:54.637+02', 'ff39513379a4a6b048f2ad17774313a1', 'Insert Row', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-7524_remove_new_credits_seq', 'Communote', 'com/communote/server/liquibase/update/v3.x/db.changelog.v3.4.xml', '2016-06-03 21:34:54.644+02', '35f736b2ff8a3a49dd11d7df625de270', 'Drop Sequence (x2)', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-7524_remove_old_credits_seq_postgresql', 'Communote', 'com/communote/server/liquibase/update/v3.x/db.changelog.v3.4.xml', '2016-06-03 21:34:54.653+02', '2f8922177dcc525069bcf7c18c1e6562', 'Drop Sequence (x2)', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-7524_remove_license_seq', 'Communote', 'com/communote/server/liquibase/update/v3.x/db.changelog.v3.4.xml', '2016-06-03 21:34:56.513+02', '9fa5d29eac36f678d77bdc5b16c5c', 'Drop Sequence', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-7524_remove_old_credits_tables_v2', 'Communote', 'com/communote/server/liquibase/update/v3.x/db.changelog.v3.4.xml', '2016-06-03 21:34:58.228+02', 'a9f2d3d5ffa903cbbacb1ab77292098', 'Drop Table (x2)', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-7524_remove_new_credits_tables_v2', 'Communote', 'com/communote/server/liquibase/update/v3.x/db.changelog.v3.4.xml', '2016-06-03 21:34:59.993+02', 'b6db57b305ec7f0669cbead915423f', 'Drop Table (x2)', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-7524_remove_license_table_v2', 'Communote', 'com/communote/server/liquibase/update/v3.x/db.changelog.v3.4.xml', '2016-06-03 21:35:01.737+02', 'aa32adb832b1e3df63aa193cced44f9', 'Drop Table', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-7523_remove_old_jobs', 'Communote', 'com/communote/server/liquibase/update/v3.x/db.changelog.v3.4.xml', '2016-06-03 21:35:03.477+02', 'd38b603a8ea8cde9543cb7ebba9ea0fc', 'Delete Data', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-7537_cleanup_client_table', 'Communote', 'com/communote/server/liquibase/update/v3.x/db.changelog.v3.4.xml', '2016-06-03 21:35:03.482+02', 'd7a8f6e553ad6b021bf0f361f2bf', 'Drop Column', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-7537_cleanup_client_tables_pg', 'Communote', 'com/communote/server/liquibase/update/v3.x/db.changelog.v3.4.xml', '2016-06-03 21:35:03.51+02', '4f5c964d46a9eb49a8d892912c984f56', 'Drop Sequence (x3), Drop Unique Constraint (x3), Drop Foreign Key Constraint (x3), Drop Column (x4), Drop Table (x6)', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-7537_act_code_old_pg', 'Communote', 'com/communote/server/liquibase/update/v3.x/db.changelog.v3.4.xml', '2016-06-03 21:35:05.262+02', '7fe567897dbce9229b8125204ed84d6', 'Drop Sequence, Drop Table', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-7537_act_code_new_pg', 'Communote', 'com/communote/server/liquibase/update/v3.x/db.changelog.v3.4.xml', '2016-06-03 21:35:06.966+02', 'ca5ae387a7801857602b767d69cfe972', 'Drop Sequence, Drop Table', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-7547_modification_time_of_setting', 'Communote', 'com/communote/server/liquibase/update/v3.x/db.changelog.v3.4.xml', '2016-06-03 21:35:08.685+02', '78c1f960fb942e58e57baf7ebcf9a41', 'Add Column', '', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('set_configuration_v2', 'unknown', 'de/communardo/kenmei/database/update/db.changelog.final.xml', '2016-06-03 21:35:08.691+02', '77ed99ff17bb8b6b7dff62f6eaa738', 'Custom SQL', 'Assign the configuration FK', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('ADD_INSTALLATION_ID', 'unknown', 'de/communardo/kenmei/database/update_2nd_pass/v1.1.4/db.changelog.v1.1.4.xml', '2016-06-03 21:35:08.925+02', '467db414d6b587dab40689753ecef1', 'Custom Change', 'Inserts an unique id to the application properties.', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('CR_115_Move_Application_Properties_Into_DB', 'unknown', 'de/communardo/kenmei/database/update_2nd_pass/v1.1.4/db.changelog.v1.1.4.xml', '2016-06-03 21:35:08.934+02', '57ff61206a4cf95e4a917e86e6bb0a8', 'Custom Change', 'Get rid of property files and store the properties in the DB.', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('CR_115_Refactor_URL_Application_Properties', 'unknown', 'de/communardo/kenmei/database/update_2nd_pass/v1.1.4/db.changelog.v1.1.4.xml', '2016-06-03 21:35:08.942+02', '5ff5d0d59e42e57f8e949791cfad13e', 'Custom Change', 'Convert old URL related properties to the new properties.', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('Create_Default_Blog_Where_Missing', 'unknown', 'de/communardo/kenmei/database/update_2nd_pass/v1.1.4/db.changelog.v1.1.4.xml', '2016-06-03 21:35:09.039+02', 'b5f386a532aacea1a461a7be18459a52', 'Custom Change', 'Create the default blog for clients where there is none yet.', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('Update_Master_Data_From_Property_Files', 'unknown', 'de/communardo/kenmei/database/update_2nd_pass/v1.1.4/db.changelog.v1.1.4.xml', '2016-06-03 21:35:09.128+02', '38a37931a03d7a8d2b156f8813123074', 'Custom Change', 'Update the master data stored in DB with entries from property files.', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('Create_encrypted_creation_date', 'unknown', 'de/communardo/kenmei/database/update_2nd_pass/v1.1.4/db.changelog.v1.1.4.xml', '2016-06-03 21:35:09.134+02', 'b8279228f0818d3e3d42ba4a1984199', 'Custom Change', 'No comment.', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-1672_Virus_scanner_in_admin_section', 'unknown', 'de/communardo/kenmei/database/update_2nd_pass/v1.1.4/db.changelog.v1.1.4.xml', '2016-06-03 21:35:09.14+02', 'e99e6c9bad1fb4f2a4b5f15e30e2d681', 'Custom Change', 'Add enabled option for virus scanner', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-1617_NullPointerException_bei_Zugriff_auf_Certificates', 'unknown', 'de/communardo/kenmei/database/update_2nd_pass/v1.1.4/db.changelog.v1.1.4.xml', '2016-06-03 21:35:09.146+02', '9af4a69dbe173a28caa8ea67321a9612', 'Custom Change', 'Add password for keystore if it doesn''t exist.', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-1694_Follow-remove_tag_globalIds', 'unknown', 'de/communardo/kenmei/database/update_2nd_pass/v1.2/db.changelog.v1.2.xml', '2016-06-03 21:35:09.152+02', '5db46f07a5f2b465bc80d13d44398', 'Custom SQL', 'Remove global IDs from tags', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-1694_Follow-add_tag_globalIds', 'unknown', 'de/communardo/kenmei/database/update_2nd_pass/v1.2/db.changelog.v1.2.xml', '2016-06-03 21:35:09.157+02', '3ee19392e521fda2a8629f23c9baba98', 'Custom Change', 'Create the default blog for clients where there is none yet.', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-1694_Follow-add_blog_globalIds', 'unknown', 'de/communardo/kenmei/database/update_2nd_pass/v1.2/db.changelog.v1.2.xml', '2016-06-03 21:35:09.166+02', 'c79f0fa2747d5864dcf55aa98b5100', 'Custom Change', 'Create the global Id for blogs where there is none yet.', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-2104_Ldap_Passwort_verschluesseln', 'unknown', 'de/communardo/kenmei/database/update_2nd_pass/v1.2/db.changelog.v1.2.xml', '2016-06-03 21:35:09.188+02', 'f472fa9537c62bf44ffbd73b1ffb6ad', 'Custom Change', 'encrypts the LDAP manager password', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-2009_Ldap_config-add_uid_to_mapping', 'unknown', 'de/communardo/kenmei/database/update_2nd_pass/v1.2/db.changelog.v1.2.xml', '2016-06-03 21:35:09.192+02', '4c44b01203e35fc2473fdafac8c5cc', 'Custom Change', 'Add UID to userSearch property mapping', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-2510_confluence_passwort_verschluesseln', 'unknown', 'de/communardo/kenmei/database/update_2nd_pass/v1.2/db.changelog.v1.2.xml', '2016-06-03 21:35:09.203+02', 'adf43a30d6bf756fefc6cfb243f428', 'Custom Change', 'encrypts the Confluence administrator password', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-2510_sharepoint_passwort_verschluesseln', 'unknown', 'de/communardo/kenmei/database/update_2nd_pass/v1.2/db.changelog.v1.2.xml', '2016-06-03 21:35:09.207+02', '80ddc2197d1d616a48d012e6fb802452', 'Custom Change', 'encrypts the Sharepoint administrator password', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-2154-Jobs_Services_als_Tasks_in_Datenbank_v2', 'unknown', 'de/communardo/kenmei/database/update_2nd_pass/v1.3/db.changelog.v1.3.xml', '2016-06-03 21:35:09.225+02', '627db31b9a285c1a388d525db74626', 'Custom Change', 'Updating existing jobs.', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-4xxx_MQ', 'Communote', 'de/communardo/kenmei/database/update_2nd_pass/v2.2/db.changelog.v2.2.xml', '2016-06-03 21:35:09.231+02', 'fed0868ba054bcdcb65459b0e92f5f2', 'Custom Change', 'Add password for keystore if it doesn''t exist.', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-6543-Create_BuiltIn_NaviItems', 'Communote', 'de/communardo/kenmei/database/update_2nd_pass/v3.x/db.changelog.v3.0.xml', '2016-06-03 21:35:09.288+02', 'ff6b36f04575f9aa72335b2310e36087', 'Custom Change', 'Add built-in navigation items', NULL, '1.9.5');
INSERT INTO databasechangelog VALUES ('KENMEI-1264_Offline-Autosave_TaskHandler_3', 'Communote', 'de/communardo/kenmei/database/update_2nd_pass/v3.x/db.changelog.v3.1.xml', '2016-06-03 21:35:09.297+02', 'f343c72a10c5f04551c4344a8490a2e5', 'Custom Change', 'Add DeleteOrphanedAttachmentsTaskHandler to tasks', NULL, '1.9.5');


--
-- Data for Name: databasechangeloglock; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO databasechangeloglock VALUES (1, false, NULL, NULL);


--
-- Data for Name: iprange_channel; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: iprange_filter; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: iprange_filter_channel; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Name: iprange_filter_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('iprange_filter_seq', 1, false);


--
-- Data for Name: iprange_range; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Name: iprange_range_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('iprange_range_seq', 1, false);


--
-- Data for Name: mc_config; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO mc_config VALUES (1, 'mail', NULL, false, 1, 1);


--
-- Name: mc_config_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('mc_config_seq', 1, true);


--
-- Data for Name: md_country; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO md_country VALUES (1, 'lb', 'Lebanon');
INSERT INTO md_country VALUES (2, 'la', 'Lao People''s Democratic Republic');
INSERT INTO md_country VALUES (3, 'kz', 'Kazakhstan');
INSERT INTO md_country VALUES (4, 'ky', 'Cayman Islands');
INSERT INTO md_country VALUES (5, 'kw', 'Kuwait');
INSERT INTO md_country VALUES (6, 'kr', 'Korea, Republic of');
INSERT INTO md_country VALUES (7, 'kp', 'Korea, Democratic People''s Republic of');
INSERT INTO md_country VALUES (8, 'kn', 'Saint Kitts and Nevis');
INSERT INTO md_country VALUES (9, 'km', 'Comoros');
INSERT INTO md_country VALUES (10, 'ki', 'Kiribati');
INSERT INTO md_country VALUES (11, 'kh', 'Cambodia');
INSERT INTO md_country VALUES (12, 'ws', 'Samoa');
INSERT INTO md_country VALUES (13, 'kg', 'Kyrgyzstan');
INSERT INTO md_country VALUES (14, 'ke', 'Kenya');
INSERT INTO md_country VALUES (15, 'wf', 'Wallis and Futuna');
INSERT INTO md_country VALUES (16, 'jp', 'Japan');
INSERT INTO md_country VALUES (17, 'jo', 'Jordan');
INSERT INTO md_country VALUES (18, 'jm', 'Jamaica');
INSERT INTO md_country VALUES (19, 'vu', 'Vanuatu');
INSERT INTO md_country VALUES (20, 'je', 'Jersey');
INSERT INTO md_country VALUES (21, 'vn', 'Vietnam');
INSERT INTO md_country VALUES (22, 'vi', 'Virgin Islands, U.S.');
INSERT INTO md_country VALUES (23, 'vg', 'Virgin Islands, British');
INSERT INTO md_country VALUES (24, 've', 'Venezuela');
INSERT INTO md_country VALUES (25, 'vc', 'Saint Vincent and the Grenadines');
INSERT INTO md_country VALUES (26, 'va', 'Holy See (Vatican City State)');
INSERT INTO md_country VALUES (27, 'it', 'Italy');
INSERT INTO md_country VALUES (28, 'is', 'Iceland');
INSERT INTO md_country VALUES (29, 'ir', 'Iran, Islamic Republic of');
INSERT INTO md_country VALUES (30, 'iq', 'Iraq');
INSERT INTO md_country VALUES (31, 'io', 'British Indian Ocean Territory');
INSERT INTO md_country VALUES (32, 'uz', 'Uzbekistan');
INSERT INTO md_country VALUES (33, 'in', 'India');
INSERT INTO md_country VALUES (34, 'uy', 'Uruguay');
INSERT INTO md_country VALUES (35, 'im', 'Isle of Man');
INSERT INTO md_country VALUES (36, 'il', 'Israel');
INSERT INTO md_country VALUES (37, 'us', 'United States');
INSERT INTO md_country VALUES (38, 'ie', 'Ireland');
INSERT INTO md_country VALUES (39, 'id', 'Indonesia');
INSERT INTO md_country VALUES (40, 'um', 'United States Minor Outlying Islands');
INSERT INTO md_country VALUES (41, 'ug', 'Uganda');
INSERT INTO md_country VALUES (42, 'hu', 'Hungary');
INSERT INTO md_country VALUES (43, 'ua', 'Ukraine');
INSERT INTO md_country VALUES (44, 'ht', 'Haiti');
INSERT INTO md_country VALUES (45, 'hr', 'Croatia');
INSERT INTO md_country VALUES (46, 'tz', 'Tanzania, United Republic of');
INSERT INTO md_country VALUES (47, 'hn', 'Honduras');
INSERT INTO md_country VALUES (48, 'hm', 'Heard Island and McDonald Islands');
INSERT INTO md_country VALUES (49, 'tw', 'Taiwan, Province of China');
INSERT INTO md_country VALUES (50, 'hk', 'Hong Kong');
INSERT INTO md_country VALUES (51, 'tv', 'Tuvalu');
INSERT INTO md_country VALUES (52, 'tt', 'Trinidad and Tobago');
INSERT INTO md_country VALUES (53, 'tr', 'Turkey');
INSERT INTO md_country VALUES (54, 'to', 'Tonga');
INSERT INTO md_country VALUES (55, 'tn', 'Tunisia');
INSERT INTO md_country VALUES (56, 'tm', 'Turkmenistan');
INSERT INTO md_country VALUES (57, 'tl', 'Timor-Leste');
INSERT INTO md_country VALUES (58, 'tk', 'Tokelau');
INSERT INTO md_country VALUES (59, 'tj', 'Tajikistan');
INSERT INTO md_country VALUES (60, 'th', 'Thailand');
INSERT INTO md_country VALUES (61, 'tg', 'Togo');
INSERT INTO md_country VALUES (62, 'tf', 'French Southern Territories');
INSERT INTO md_country VALUES (63, 'gy', 'Guyana');
INSERT INTO md_country VALUES (64, 'td', 'Chad');
INSERT INTO md_country VALUES (65, 'gw', 'Guinea-bissau');
INSERT INTO md_country VALUES (66, 'tc', 'Turks and Caicos Islands');
INSERT INTO md_country VALUES (67, 'gu', 'Guam');
INSERT INTO md_country VALUES (68, 'gt', 'Guatemala');
INSERT INTO md_country VALUES (69, 'gs', 'South Georgia and the South Sandwich Islands');
INSERT INTO md_country VALUES (70, 'gr', 'Greece');
INSERT INTO md_country VALUES (71, 'gq', 'Equatorial Guinea');
INSERT INTO md_country VALUES (72, 'gp', 'Guadeloupe');
INSERT INTO md_country VALUES (73, 'sz', 'Swaziland');
INSERT INTO md_country VALUES (74, 'gn', 'Guinea');
INSERT INTO md_country VALUES (75, 'sy', 'Syrian Arab Republic');
INSERT INTO md_country VALUES (76, 'gm', 'Gambia');
INSERT INTO md_country VALUES (77, 'gl', 'Greenland');
INSERT INTO md_country VALUES (78, 'sv', 'El Salvador');
INSERT INTO md_country VALUES (79, 'gi', 'Gibraltar');
INSERT INTO md_country VALUES (80, 'st', 'Sao Tome and Principe');
INSERT INTO md_country VALUES (81, 'gh', 'Ghana');
INSERT INTO md_country VALUES (82, 'gg', 'Guernsey');
INSERT INTO md_country VALUES (83, 'sr', 'Suriname');
INSERT INTO md_country VALUES (84, 'gf', 'French Guiana');
INSERT INTO md_country VALUES (85, 'ge', 'Georgia');
INSERT INTO md_country VALUES (86, 'gd', 'Grenada');
INSERT INTO md_country VALUES (87, 'so', 'Somalia');
INSERT INTO md_country VALUES (88, 'sn', 'Senegal');
INSERT INTO md_country VALUES (89, 'gb', 'United Kingdom');
INSERT INTO md_country VALUES (90, 'sm', 'San Marino');
INSERT INTO md_country VALUES (91, 'ga', 'Gabon');
INSERT INTO md_country VALUES (92, 'sl', 'Sierra Leone');
INSERT INTO md_country VALUES (93, 'sk', 'Slovakia');
INSERT INTO md_country VALUES (94, 'sj', 'Svalbard and Jan Mayen');
INSERT INTO md_country VALUES (95, 'si', 'Slovenia');
INSERT INTO md_country VALUES (96, 'sh', 'Saint Helena');
INSERT INTO md_country VALUES (97, 'sg', 'Singapore');
INSERT INTO md_country VALUES (98, 'se', 'Sweden');
INSERT INTO md_country VALUES (99, 'sd', 'Sudan');
INSERT INTO md_country VALUES (100, 'sc', 'Seychelles');
INSERT INTO md_country VALUES (101, 'sb', 'Solomon Islands');
INSERT INTO md_country VALUES (102, 'sa', 'Saudi Arabia');
INSERT INTO md_country VALUES (103, 'fr', 'France');
INSERT INTO md_country VALUES (104, 'fo', 'Faroe Islands');
INSERT INTO md_country VALUES (105, 'fm', 'Micronesia, Federated States of');
INSERT INTO md_country VALUES (106, 'rw', 'Rwanda');
INSERT INTO md_country VALUES (107, 'fk', 'Falkland Islands (Malvinas)');
INSERT INTO md_country VALUES (108, 'fj', 'Fiji');
INSERT INTO md_country VALUES (109, 'ru', 'Russian Federation');
INSERT INTO md_country VALUES (110, 'fi', 'Finland');
INSERT INTO md_country VALUES (111, 'rs', 'Serbia');
INSERT INTO md_country VALUES (112, 'ro', 'Romania');
INSERT INTO md_country VALUES (113, 're', 'Reunion');
INSERT INTO md_country VALUES (114, 'et', 'Ethiopia');
INSERT INTO md_country VALUES (115, 'es', 'Spain');
INSERT INTO md_country VALUES (116, 'er', 'Eritrea');
INSERT INTO md_country VALUES (117, 'eh', 'Western Sahara');
INSERT INTO md_country VALUES (118, 'eg', 'Egypt');
INSERT INTO md_country VALUES (119, 'ee', 'Estonia');
INSERT INTO md_country VALUES (120, 'ec', 'Ecuador');
INSERT INTO md_country VALUES (121, 'dz', 'Algeria');
INSERT INTO md_country VALUES (122, 'qa', 'Qatar');
INSERT INTO md_country VALUES (123, 'do', 'Dominican Republic');
INSERT INTO md_country VALUES (124, 'py', 'Paraguay');
INSERT INTO md_country VALUES (125, 'dm', 'Dominica');
INSERT INTO md_country VALUES (126, 'pw', 'Palau');
INSERT INTO md_country VALUES (127, 'dk', 'Denmark');
INSERT INTO md_country VALUES (128, 'dj', 'Djibouti');
INSERT INTO md_country VALUES (129, 'pt', 'Portugal');
INSERT INTO md_country VALUES (130, 'ps', 'Palestinian Territory, occupied');
INSERT INTO md_country VALUES (131, 'pr', 'Puerto Rico');
INSERT INTO md_country VALUES (132, 'de', 'Germany');
INSERT INTO md_country VALUES (133, 'pn', 'Pitcairn');
INSERT INTO md_country VALUES (134, 'pm', 'Saint Pierre and Miquelon');
INSERT INTO md_country VALUES (135, 'pl', 'Poland');
INSERT INTO md_country VALUES (136, 'pk', 'Pakistan');
INSERT INTO md_country VALUES (137, 'ph', 'Philippines');
INSERT INTO md_country VALUES (138, 'pg', 'Papua New Guinea');
INSERT INTO md_country VALUES (139, 'cz', 'Czech Republic');
INSERT INTO md_country VALUES (140, 'pf', 'French Polynesia');
INSERT INTO md_country VALUES (141, 'cy', 'Cyprus');
INSERT INTO md_country VALUES (142, 'pe', 'Peru');
INSERT INTO md_country VALUES (143, 'cx', 'Christmas Island');
INSERT INTO md_country VALUES (144, 'cv', 'Cape Verde');
INSERT INTO md_country VALUES (145, 'cu', 'Cuba');
INSERT INTO md_country VALUES (146, 'pa', 'Panama');
INSERT INTO md_country VALUES (147, 'cr', 'Costa Rica');
INSERT INTO md_country VALUES (148, 'co', 'Colombia');
INSERT INTO md_country VALUES (149, 'cn', 'China');
INSERT INTO md_country VALUES (150, 'cm', 'Cameroon');
INSERT INTO md_country VALUES (151, 'cl', 'Chile');
INSERT INTO md_country VALUES (152, 'ck', 'Cook Islands');
INSERT INTO md_country VALUES (153, 'ci', 'Côte D''Ivoire');
INSERT INTO md_country VALUES (154, 'ch', 'Switzerland');
INSERT INTO md_country VALUES (155, 'cg', 'Congo');
INSERT INTO md_country VALUES (156, 'cf', 'Central African Republic');
INSERT INTO md_country VALUES (157, 'cd', 'Congo, the Democratic Republic of the');
INSERT INTO md_country VALUES (158, 'cc', 'Cocos (Keeling) Islands');
INSERT INTO md_country VALUES (159, 'om', 'Oman');
INSERT INTO md_country VALUES (160, 'ca', 'Canada');
INSERT INTO md_country VALUES (161, 'bz', 'Belize');
INSERT INTO md_country VALUES (162, 'by', 'Belarus');
INSERT INTO md_country VALUES (163, 'bw', 'Botswana');
INSERT INTO md_country VALUES (164, 'bv', 'Bouvet Island');
INSERT INTO md_country VALUES (165, 'bt', 'Bhutan');
INSERT INTO md_country VALUES (166, 'bs', 'Bahamas');
INSERT INTO md_country VALUES (167, 'br', 'Brazil');
INSERT INTO md_country VALUES (168, 'bo', 'Bolivia');
INSERT INTO md_country VALUES (169, 'nz', 'New Zealand');
INSERT INTO md_country VALUES (170, 'bn', 'Brunei Darussalam');
INSERT INTO md_country VALUES (171, 'bm', 'Bermuda');
INSERT INTO md_country VALUES (172, 'bl', 'Saint Barthélemy');
INSERT INTO md_country VALUES (173, 'bj', 'Benin');
INSERT INTO md_country VALUES (174, 'nu', 'Niue');
INSERT INTO md_country VALUES (175, 'bi', 'Burundi');
INSERT INTO md_country VALUES (176, 'bh', 'Bahrain');
INSERT INTO md_country VALUES (177, 'bg', 'Bulgaria');
INSERT INTO md_country VALUES (178, 'nr', 'Nauru');
INSERT INTO md_country VALUES (179, 'bf', 'Burkina Faso');
INSERT INTO md_country VALUES (180, 'be', 'Belgium');
INSERT INTO md_country VALUES (181, 'np', 'Nepal');
INSERT INTO md_country VALUES (182, 'bd', 'Bangladesh');
INSERT INTO md_country VALUES (183, 'no', 'Norway');
INSERT INTO md_country VALUES (184, 'bb', 'Barbados');
INSERT INTO md_country VALUES (185, 'ba', 'Bosnia and Herzegovina');
INSERT INTO md_country VALUES (186, 'nl', 'Netherlands');
INSERT INTO md_country VALUES (187, 'zw', 'Zimbabwe');
INSERT INTO md_country VALUES (188, 'ni', 'Nicaragua');
INSERT INTO md_country VALUES (189, 'ng', 'Nigeria');
INSERT INTO md_country VALUES (190, 'az', 'Azerbaijan');
INSERT INTO md_country VALUES (191, 'nf', 'Norfolk Island');
INSERT INTO md_country VALUES (192, 'ne', 'Niger');
INSERT INTO md_country VALUES (193, 'ax', 'Åland Islands');
INSERT INTO md_country VALUES (194, 'aw', 'Aruba');
INSERT INTO md_country VALUES (195, 'nc', 'New Caledonia');
INSERT INTO md_country VALUES (196, 'zm', 'Zambia');
INSERT INTO md_country VALUES (197, 'au', 'Australia');
INSERT INTO md_country VALUES (198, 'na', 'Namibia');
INSERT INTO md_country VALUES (199, 'at', 'Austria');
INSERT INTO md_country VALUES (200, 'as', 'American Samoa');
INSERT INTO md_country VALUES (201, 'ar', 'Argentina');
INSERT INTO md_country VALUES (202, 'aq', 'Antarctica');
INSERT INTO md_country VALUES (203, 'ao', 'Angola');
INSERT INTO md_country VALUES (204, 'mz', 'Mozambique');
INSERT INTO md_country VALUES (205, 'an', 'Netherlands Antilles');
INSERT INTO md_country VALUES (206, 'my', 'Malaysia');
INSERT INTO md_country VALUES (207, 'am', 'Armenia');
INSERT INTO md_country VALUES (208, 'mx', 'Mexico');
INSERT INTO md_country VALUES (209, 'al', 'Albania');
INSERT INTO md_country VALUES (210, 'mw', 'Malawi');
INSERT INTO md_country VALUES (211, 'mv', 'Maldives');
INSERT INTO md_country VALUES (212, 'mu', 'Mauritius');
INSERT INTO md_country VALUES (213, 'za', 'South Africa');
INSERT INTO md_country VALUES (214, 'ai', 'Anguilla');
INSERT INTO md_country VALUES (215, 'mt', 'Malta');
INSERT INTO md_country VALUES (216, 'ms', 'Montserrat');
INSERT INTO md_country VALUES (217, 'ag', 'Antigua and Barbuda');
INSERT INTO md_country VALUES (218, 'mr', 'Mauritania');
INSERT INTO md_country VALUES (219, 'af', 'Afghanistan');
INSERT INTO md_country VALUES (220, 'mq', 'Martinique');
INSERT INTO md_country VALUES (221, 'ae', 'United Arab Emirates');
INSERT INTO md_country VALUES (222, 'mp', 'Northern Mariana Islands');
INSERT INTO md_country VALUES (223, 'ad', 'Andorra');
INSERT INTO md_country VALUES (224, 'mo', 'Macao');
INSERT INTO md_country VALUES (225, 'mn', 'Mongolia');
INSERT INTO md_country VALUES (226, 'mm', 'Myanmar');
INSERT INTO md_country VALUES (227, 'ml', 'Mali');
INSERT INTO md_country VALUES (228, 'mk', 'Macedonia, the Former Yugoslav Republic of');
INSERT INTO md_country VALUES (229, 'yt', 'Mayotte');
INSERT INTO md_country VALUES (230, 'mh', 'Marshall Islands');
INSERT INTO md_country VALUES (231, 'mg', 'Madagascar');
INSERT INTO md_country VALUES (232, 'mf', 'Saint Martin');
INSERT INTO md_country VALUES (233, 'me', 'Montenegro');
INSERT INTO md_country VALUES (234, 'md', 'Moldova, Republic of');
INSERT INTO md_country VALUES (235, 'mc', 'Monaco');
INSERT INTO md_country VALUES (236, 'ma', 'Morocco');
INSERT INTO md_country VALUES (237, 'ly', 'Libyan Arab Jamahiriya');
INSERT INTO md_country VALUES (238, 'ye', 'Yemen');
INSERT INTO md_country VALUES (239, 'lv', 'Latvia');
INSERT INTO md_country VALUES (240, 'lu', 'Luxembourg');
INSERT INTO md_country VALUES (241, 'lt', 'Lithuania');
INSERT INTO md_country VALUES (242, 'ls', 'Lesotho');
INSERT INTO md_country VALUES (243, 'lr', 'Liberia');
INSERT INTO md_country VALUES (244, 'lk', 'Sri Lanka');
INSERT INTO md_country VALUES (245, 'li', 'Liechtenstein');
INSERT INTO md_country VALUES (246, 'lc', 'Saint Lucia');


--
-- Name: md_country_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('md_country_seq', 246, true);


--
-- Data for Name: md_language; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO md_language VALUES (1, 'en', 'English');
INSERT INTO md_language VALUES (2, 'de', 'Deutsch');


--
-- Name: md_language_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('md_language_seq', 2, true);


--
-- Name: misc_query_helper_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('misc_query_helper_seq', 1, false);


--
-- Data for Name: notification_config; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO notification_config VALUES (1, 'mail');


--
-- Name: notification_config_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('notification_config_seq', 1, true);


--
-- Data for Name: security_code; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Name: security_code_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('security_code_seq', 1, false);


--
-- Data for Name: security_email_code; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: security_forgotten_pw_code; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: security_invite_blog; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: security_invite_client; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: security_user_code; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: security_user_status; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Name: security_user_status_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('security_user_status_seq', 1, false);


--
-- Data for Name: security_user_unlock_code; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: user_authorities; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO user_authorities VALUES (1, 'ROLE_KENMEI_CLIENT_MANAGER', 1);
INSERT INTO user_authorities VALUES (2, 'ROLE_KENMEI_USER', 1);


--
-- Name: user_authorities_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('user_authorities_seq', 2, true);


--
-- Data for Name: user_client; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO user_client VALUES (1, 'global', 'Global Test Client', 'ACTIVE', '3.4.e090c', '2016-06-03 21:35:09', 'e090c');


--
-- Name: user_client_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('user_client_seq', 1, true);


--
-- Data for Name: user_client_statistic; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO user_client_statistic VALUES (1, 0);


--
-- Name: user_client_statistic_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('user_client_statistic_seq', 1, true);


--
-- Data for Name: user_contact; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Name: user_contact_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('user_contact_seq', 1, false);


--
-- Data for Name: user_entity; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO user_entity VALUES (1, 1);


--
-- Name: user_entity_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('user_entity_seq', 1, true);


--
-- Data for Name: user_external_auth; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Name: user_external_auth_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('user_external_auth_seq', 1, false);


--
-- Data for Name: user_group; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: user_group_external; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: user_group_member; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: user_group_property; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Name: user_group_property_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('user_group_property_seq', 1, false);


--
-- Data for Name: user_image; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Name: user_image_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('user_image_seq', 1, false);


--
-- Data for Name: user_navigation_item; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO user_navigation_item VALUES (1, -1, '{"contextType":"notesOverview","contextId":null,"filters":{"showFollowedItems":true}}', '2016-06-03 21:35:09.644', 1, 'following');
INSERT INTO user_navigation_item VALUES (2, -1, '{"contextType":"notesOverview","contextId":null,"filters":{"showPostsForMe":true}}', '2016-06-03 21:35:09.675', 1, 'mentions');


--
-- Name: user_navigation_item_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('user_navigation_item_seq', 2, true);


--
-- Data for Name: user_note_entity; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Name: user_note_entity_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('user_note_entity_id_seq', 1, false);


--
-- Name: user_note_entity_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('user_note_entity_seq', 1, false);


--
-- Data for Name: user_note_property; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Name: user_note_property_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('user_note_property_seq', 1, false);


--
-- Data for Name: user_of_group; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Name: user_of_group_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('user_of_group_seq', 1, false);


--
-- Data for Name: user_profile; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO user_profile VALUES (1, 'Admin', NULL, NULL, NULL, 'Peter', '2016-06-03 21:35:09.587', NULL, 'time.zones.gmt.Europe/Amsterdam', NULL, NULL, NULL, NULL, 1);


--
-- Name: user_profile_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('user_profile_seq', 1, true);


--
-- Data for Name: user_user; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO user_user VALUES (1, 'e10adc3949ba59abbe56e057f20f883e', 'communote@localhost', 'en', NULL, 'ACTIVE', 'communote', true, false, '2016-06-03 21:35:09.589', 1, NULL);


--
-- Data for Name: user_user_property; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Name: user_user_property_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('user_user_property_seq', 1, false);


--
-- Name: channel_configuration_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY channel_configuration
    ADD CONSTRAINT channel_configuration_pkey PRIMARY KEY (id);


--
-- Name: configuration_client_config_fk_key; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY configuration
    ADD CONSTRAINT configuration_client_config_fk_key UNIQUE (client_config_fk);


--
-- Name: configuration_client_default_blog_fk_key; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY configuration_client
    ADD CONSTRAINT configuration_client_default_blog_fk_key UNIQUE (default_blog_fk);


--
-- Name: configuration_client_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY configuration_client
    ADD CONSTRAINT configuration_client_pkey PRIMARY KEY (id);


--
-- Name: configuration_confluence_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY configuration_confluence
    ADD CONSTRAINT configuration_confluence_pkey PRIMARY KEY (id);


--
-- Name: configuration_external_system_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY configuration_external_sys
    ADD CONSTRAINT configuration_external_system_pkey PRIMARY KEY (id);


--
-- Name: configuration_external_system_system_id_key; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY configuration_external_sys
    ADD CONSTRAINT configuration_external_system_system_id_key UNIQUE (system_id);


--
-- Name: configuration_ldap_group_group_search_fk_key; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY configuration_ldap_group
    ADD CONSTRAINT configuration_ldap_group_group_search_fk_key UNIQUE (group_search_fk);


--
-- Name: configuration_ldap_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY configuration_ldap
    ADD CONSTRAINT configuration_ldap_pkey PRIMARY KEY (id);


--
-- Name: configuration_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY configuration
    ADD CONSTRAINT configuration_pkey PRIMARY KEY (id);


--
-- Name: configuration_setting_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY configuration_setting
    ADD CONSTRAINT configuration_setting_pkey PRIMARY KEY (setting_key);


--
-- Name: content_identifier_unique_key; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY core_attachment
    ADD CONSTRAINT content_identifier_unique_key UNIQUE (content_identifier);


--
-- Name: core_attachment_global_id_fk_key; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY core_attachment
    ADD CONSTRAINT core_attachment_global_id_fk_key UNIQUE (global_id_fk);


--
-- Name: core_attachment_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY core_attachment
    ADD CONSTRAINT core_attachment_pkey PRIMARY KEY (id);


--
-- Name: core_blog2tag_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY core_blog2tag
    ADD CONSTRAINT core_blog2tag_pkey PRIMARY KEY (blogs_fk, tags_fk);


--
-- Name: core_blog_global_id_fk_key; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY core_blog
    ADD CONSTRAINT core_blog_global_id_fk_key UNIQUE (global_id_fk);


--
-- Name: core_blog_member_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY core_blog_member
    ADD CONSTRAINT core_blog_member_pkey PRIMARY KEY (id);


--
-- Name: core_blog_name_identifier_key; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY core_blog
    ADD CONSTRAINT core_blog_name_identifier_key UNIQUE (name_identifier);


--
-- Name: core_blog_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY core_blog
    ADD CONSTRAINT core_blog_pkey PRIMARY KEY (id);


--
-- Name: core_content_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY core_content
    ADD CONSTRAINT core_content_pkey PRIMARY KEY (id);


--
-- Name: core_external_object_external_system_id_external_id_key; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY core_external_object
    ADD CONSTRAINT core_external_object_external_system_id_external_id_key UNIQUE (external_system_id, external_id);


--
-- Name: core_external_object_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY core_external_object
    ADD CONSTRAINT core_external_object_pkey PRIMARY KEY (id);


--
-- Name: core_external_object_properties_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY core_external_object_prop
    ADD CONSTRAINT core_external_object_properties_pkey PRIMARY KEY (id);


--
-- Name: core_global_id_global_identifier_key; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY core_global_id
    ADD CONSTRAINT core_global_id_global_identifier_key UNIQUE (global_identifier);


--
-- Name: core_global_id_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY core_global_id
    ADD CONSTRAINT core_global_id_pkey PRIMARY KEY (id);


--
-- Name: core_note2direct_user_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY core_note2direct_user
    ADD CONSTRAINT core_note2direct_user_pkey PRIMARY KEY (direct_users_fk, direct_notes_fk);


--
-- Name: core_note2followable_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY core_note2followable
    ADD CONSTRAINT core_note2followable_pkey PRIMARY KEY (notes_fk, followable_items_fk);


--
-- Name: core_note_content_fk_key; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY core_note
    ADD CONSTRAINT core_note_content_fk_key UNIQUE (content_fk);


--
-- Name: core_note_global_id_fk_key; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY core_note
    ADD CONSTRAINT core_note_global_id_fk_key UNIQUE (global_id_fk);


--
-- Name: core_note_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY core_note
    ADD CONSTRAINT core_note_pkey PRIMARY KEY (id);


--
-- Name: core_notes2crossblogs_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY core_notes2crossblogs
    ADD CONSTRAINT core_notes2crossblogs_pkey PRIMARY KEY (notes_fk, crosspost_blogs_fk);


--
-- Name: core_notes2tag_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY core_notes2tag
    ADD CONSTRAINT core_notes2tag_pkey PRIMARY KEY (notes_fk, tags_fk);


--
-- Name: core_notes2user_to_notify_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY core_notes2user_to_notify
    ADD CONSTRAINT core_notes2user_to_notify_pkey PRIMARY KEY (notes_fk, users_to_be_notified_fk);


--
-- Name: core_processed_utp_mail_mail_message_id_key; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY core_processed_utp_mail
    ADD CONSTRAINT core_processed_utp_mail_mail_message_id_key UNIQUE (mail_message_id);


--
-- Name: core_processed_utp_mail_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY core_processed_utp_mail
    ADD CONSTRAINT core_processed_utp_mail_pkey PRIMARY KEY (id);


--
-- Name: core_role2blog_granting_group_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY core_role2blog_granting_group
    ADD CONSTRAINT core_role2blog_granting_group_pkey PRIMARY KEY (user_to_blog_role_mappings_fk, granting_groups_fk);


--
-- Name: core_role2blog_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY core_role2blog
    ADD CONSTRAINT core_role2blog_pkey PRIMARY KEY (id);


--
-- Name: core_tag2descriptions_idx; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY core_tag2descriptions
    ADD CONSTRAINT core_tag2descriptions_idx UNIQUE (tags_fk, descriptions_fk);


--
-- Name: core_tag2names_idx; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY core_tag2names
    ADD CONSTRAINT core_tag2names_idx UNIQUE (tags_fk, names_fk);


--
-- Name: core_tag_category_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY core_tag_category
    ADD CONSTRAINT core_tag_category_pkey PRIMARY KEY (id);


--
-- Name: core_tag_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY core_tag
    ADD CONSTRAINT core_tag_pkey PRIMARY KEY (id);


--
-- Name: core_tag_store_idx; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY core_tag
    ADD CONSTRAINT core_tag_store_idx UNIQUE (tag_store_tag_id, tag_store_alias);


--
-- Name: core_task_execs_task_fk_key; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY core_task_execs
    ADD CONSTRAINT core_task_execs_task_fk_key UNIQUE (task_fk);


--
-- Name: core_task_unique_name_key; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY core_task
    ADD CONSTRAINT core_task_unique_name_key UNIQUE (unique_name);


--
-- Name: core_user2follows_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY core_user2follows
    ADD CONSTRAINT core_user2follows_pkey PRIMARY KEY (kenmei_users_fk, followed_items_fk);


--
-- Name: core_users2favorite_notes_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY core_users2favorite_notes
    ADD CONSTRAINT core_users2favorite_notes_pkey PRIMARY KEY (favorite_notes_fk, favorite_users_fk);


--
-- Name: custom_messages_key_language_fk_key; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY custom_messages
    ADD CONSTRAINT custom_messages_key_language_fk_key UNIQUE (message_key, language_fk);


--
-- Name: custom_messages_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY custom_messages
    ADD CONSTRAINT custom_messages_pkey PRIMARY KEY (id);


--
-- Name: iprange_channel_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY iprange_channel
    ADD CONSTRAINT iprange_channel_pkey PRIMARY KEY (type);


--
-- Name: iprange_filter_channel_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY iprange_filter_channel
    ADD CONSTRAINT iprange_filter_channel_pkey PRIMARY KEY (ip_range_filters_fk, channels_fk);


--
-- Name: iprange_filter_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY iprange_filter
    ADD CONSTRAINT iprange_filter_pkey PRIMARY KEY (id);


--
-- Name: iprange_range_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY iprange_range
    ADD CONSTRAINT iprange_range_pkey PRIMARY KEY (id);


--
-- Name: kenmei6192cnstrnt; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY core_blog2blog_resolved
    ADD CONSTRAINT kenmei6192cnstrnt UNIQUE (parent_topic_id, child_topic_id, topic_path);


--
-- Name: mc_config_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY mc_config
    ADD CONSTRAINT mc_config_pkey PRIMARY KEY (id);


--
-- Name: md_country_country_code_key; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY md_country
    ADD CONSTRAINT md_country_country_code_key UNIQUE (country_code);


--
-- Name: md_country_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY md_country
    ADD CONSTRAINT md_country_pkey PRIMARY KEY (id);


--
-- Name: md_language_language_code_key; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY md_language
    ADD CONSTRAINT md_language_language_code_key UNIQUE (language_code);


--
-- Name: md_language_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY md_language
    ADD CONSTRAINT md_language_pkey PRIMARY KEY (id);


--
-- Name: notification_config_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY notification_config
    ADD CONSTRAINT notification_config_pkey PRIMARY KEY (id);


--
-- Name: pk_configuration_app_setting; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY configuration_app_setting
    ADD CONSTRAINT pk_configuration_app_setting PRIMARY KEY (setting_key);


--
-- Name: pk_configuration_ldap_group; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY configuration_ldap_group
    ADD CONSTRAINT pk_configuration_ldap_group PRIMARY KEY (id);


--
-- Name: pk_configuration_ldap_sbase; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY configuration_ldap_sbase
    ADD CONSTRAINT pk_configuration_ldap_sbase PRIMARY KEY (id);


--
-- Name: pk_configuration_ldap_search; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY configuration_ldap_search
    ADD CONSTRAINT pk_configuration_ldap_search PRIMARY KEY (id);


--
-- Name: pk_core_attachment_property; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY core_attachment_property
    ADD CONSTRAINT pk_core_attachment_property PRIMARY KEY (id);


--
-- Name: pk_core_blog2blog; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY core_blog2blog
    ADD CONSTRAINT pk_core_blog2blog PRIMARY KEY (children_fk, parents_fk);


--
-- Name: pk_core_blog2blog_resolved; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY core_blog2blog_resolved
    ADD CONSTRAINT pk_core_blog2blog_resolved PRIMARY KEY (id);


--
-- Name: pk_core_blog_property; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY core_blog_property
    ADD CONSTRAINT pk_core_blog_property PRIMARY KEY (id);


--
-- Name: pk_core_global_binary_property; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY core_global_binary_prop
    ADD CONSTRAINT pk_core_global_binary_property PRIMARY KEY (id);


--
-- Name: pk_core_note_property; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY core_note_property
    ADD CONSTRAINT pk_core_note_property PRIMARY KEY (id);


--
-- Name: pk_core_plugin_properties; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY core_plugin_properties
    ADD CONSTRAINT pk_core_plugin_properties PRIMARY KEY (id);


--
-- Name: pk_core_task; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY core_task
    ADD CONSTRAINT pk_core_task PRIMARY KEY (id);


--
-- Name: pk_core_task_execs; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY core_task_execs
    ADD CONSTRAINT pk_core_task_execs PRIMARY KEY (id);


--
-- Name: pk_core_task_props; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY core_task_props
    ADD CONSTRAINT pk_core_task_props PRIMARY KEY (id);


--
-- Name: pk_databasechangelog; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY databasechangelog
    ADD CONSTRAINT pk_databasechangelog PRIMARY KEY (id, author, filename);


--
-- Name: pk_databasechangeloglock; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY databasechangeloglock
    ADD CONSTRAINT pk_databasechangeloglock PRIMARY KEY (id);


--
-- Name: pk_user_group_property; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY user_group_property
    ADD CONSTRAINT pk_user_group_property PRIMARY KEY (id);


--
-- Name: pk_user_navigation_item; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY user_navigation_item
    ADD CONSTRAINT pk_user_navigation_item PRIMARY KEY (id);


--
-- Name: pk_user_note_entity; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY user_note_entity
    ADD CONSTRAINT pk_user_note_entity PRIMARY KEY (id);


--
-- Name: pk_user_note_property; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY user_note_property
    ADD CONSTRAINT pk_user_note_property PRIMARY KEY (id);


--
-- Name: pk_user_user_property; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY user_user_property
    ADD CONSTRAINT pk_user_user_property PRIMARY KEY (id);


--
-- Name: plugin_prop_cnst; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY core_plugin_properties
    ADD CONSTRAINT plugin_prop_cnst UNIQUE (property_key, key_group);


--
-- Name: security_code_code_key; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY security_code
    ADD CONSTRAINT security_code_code_key UNIQUE (code);


--
-- Name: security_code_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY security_code
    ADD CONSTRAINT security_code_pkey PRIMARY KEY (id);


--
-- Name: security_email_code_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY security_email_code
    ADD CONSTRAINT security_email_code_pkey PRIMARY KEY (id);


--
-- Name: security_forgotten_pw_code_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY security_forgotten_pw_code
    ADD CONSTRAINT security_forgotten_pw_code_pkey PRIMARY KEY (id);


--
-- Name: security_invite_blog_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY security_invite_blog
    ADD CONSTRAINT security_invite_blog_pkey PRIMARY KEY (id);


--
-- Name: security_invite_client_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY security_invite_client
    ADD CONSTRAINT security_invite_client_pkey PRIMARY KEY (id);


--
-- Name: security_user_auth_failed_status_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY security_user_status
    ADD CONSTRAINT security_user_auth_failed_status_pkey PRIMARY KEY (id);


--
-- Name: security_user_code_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY security_user_code
    ADD CONSTRAINT security_user_code_pkey PRIMARY KEY (id);


--
-- Name: security_user_unlock_code_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY security_user_unlock_code
    ADD CONSTRAINT security_user_unlock_code_pkey PRIMARY KEY (id);


--
-- Name: us_no_prop_constr; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY user_note_property
    ADD CONSTRAINT us_no_prop_constr UNIQUE (property_key, key_group, user_fk, note_fk);


--
-- Name: user_authorities_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY user_authorities
    ADD CONSTRAINT user_authorities_pkey PRIMARY KEY (id);


--
-- Name: user_client_client_id_key; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY user_client
    ADD CONSTRAINT user_client_client_id_key UNIQUE (client_id);


--
-- Name: user_client_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY user_client
    ADD CONSTRAINT user_client_pkey PRIMARY KEY (id);


--
-- Name: user_client_statistic_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY user_client_statistic
    ADD CONSTRAINT user_client_statistic_pkey PRIMARY KEY (id);


--
-- Name: user_contact_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY user_contact
    ADD CONSTRAINT user_contact_pkey PRIMARY KEY (id);


--
-- Name: user_entity_global_id_fk_key; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY user_entity
    ADD CONSTRAINT user_entity_global_id_fk_key UNIQUE (global_id_fk);


--
-- Name: user_entity_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY user_entity
    ADD CONSTRAINT user_entity_pkey PRIMARY KEY (id);


--
-- Name: user_external_auth_external_user_id_key; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY user_external_auth
    ADD CONSTRAINT user_external_auth_external_user_id_key UNIQUE (external_user_id, system_id);


--
-- Name: user_external_auth_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY user_external_auth
    ADD CONSTRAINT user_external_auth_pkey PRIMARY KEY (id);


--
-- Name: user_group_alias_key; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY user_group
    ADD CONSTRAINT user_group_alias_key UNIQUE (alias);


--
-- Name: user_group_external_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY user_group_external
    ADD CONSTRAINT user_group_external_pkey PRIMARY KEY (id);


--
-- Name: user_group_external_unique_key; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY user_group_external
    ADD CONSTRAINT user_group_external_unique_key UNIQUE (external_id, external_system_id);


--
-- Name: user_group_member_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY user_group_member
    ADD CONSTRAINT user_group_member_pkey PRIMARY KEY (group_members_fk, groups_fk);


--
-- Name: user_group_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY user_group
    ADD CONSTRAINT user_group_pkey PRIMARY KEY (id);


--
-- Name: user_image_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY user_image
    ADD CONSTRAINT user_image_pkey PRIMARY KEY (id);


--
-- Name: user_note_entity_fk_idx; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY user_note_entity
    ADD CONSTRAINT user_note_entity_fk_idx UNIQUE (note_fk, user_fk);


--
-- Name: user_of_group_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY user_of_group
    ADD CONSTRAINT user_of_group_pkey PRIMARY KEY (id);


--
-- Name: user_profile_contact_fk_key; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY user_profile
    ADD CONSTRAINT user_profile_contact_fk_key UNIQUE (contact_fk);


--
-- Name: user_profile_large_image_fk_key; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY user_profile
    ADD CONSTRAINT user_profile_large_image_fk_key UNIQUE (large_image_fk);


--
-- Name: user_profile_medium_image_fk_key; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY user_profile
    ADD CONSTRAINT user_profile_medium_image_fk_key UNIQUE (medium_image_fk);


--
-- Name: user_profile_notification_config_fk_key; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY user_profile
    ADD CONSTRAINT user_profile_notification_config_fk_key UNIQUE (notification_config_fk);


--
-- Name: user_profile_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY user_profile
    ADD CONSTRAINT user_profile_pkey PRIMARY KEY (id);


--
-- Name: user_profile_small_image_fk_key; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY user_profile
    ADD CONSTRAINT user_profile_small_image_fk_key UNIQUE (small_image_fk);


--
-- Name: user_user_alias_key; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY user_user
    ADD CONSTRAINT user_user_alias_key UNIQUE (alias);


--
-- Name: user_user_email_key; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY user_user
    ADD CONSTRAINT user_user_email_key UNIQUE (email);


--
-- Name: user_user_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY user_user
    ADD CONSTRAINT user_user_pkey PRIMARY KEY (id);


--
-- Name: user_user_profile_fk_key; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY user_user
    ADD CONSTRAINT user_user_profile_fk_key UNIQUE (profile_fk);


--
-- Name: core_blog_name_identifier_index; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX core_blog_name_identifier_index ON core_blog USING btree (name_identifier);


--
-- Name: core_blog_title_index; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX core_blog_title_index ON core_blog USING btree (title);


--
-- Name: core_entity2tags_idx; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX core_entity2tags_idx ON core_entity2tags USING btree (kenmei_entities_fk, tags_fk);


--
-- Name: core_note_creation_date_index; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX core_note_creation_date_index ON core_note USING btree (creation_date DESC);


--
-- Name: core_note_discussion_id; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX core_note_discussion_id ON core_note USING btree (discussion_id);


--
-- Name: core_note_follow_items_fk_idx; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX core_note_follow_items_fk_idx ON core_note2followable USING btree (followable_items_fk);


--
-- Name: core_note_follow_note_fk_idx; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX core_note_follow_note_fk_idx ON core_note2followable USING btree (notes_fk);


--
-- Name: core_role2blog_bidx; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX core_role2blog_bidx ON core_role2blog USING btree (blog_id);


--
-- Name: core_role2blog_uidx; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX core_role2blog_uidx ON core_role2blog USING btree (user_id);


--
-- Name: core_tag_lower_name_idx; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX core_tag_lower_name_idx ON core_tag USING btree (tag_store_tag_id);


--
-- Name: ft_simple_core_content_content_idx; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX ft_simple_core_content_content_idx ON core_content USING gin (to_tsvector('simple'::regconfig, content));


--
-- Name: kenmei5455_idx; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX kenmei5455_idx ON core_note USING btree (creation_date DESC, id DESC);


--
-- Name: kenmei5562_idx; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX kenmei5562_idx ON core_note USING btree (last_discussion_creation_date DESC, id DESC);


--
-- Name: kenmei6122_idx; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX kenmei6122_idx ON core_note USING btree (blog_fk, user_fk);


--
-- Name: note_fk_idx; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX note_fk_idx ON user_note_property USING btree (note_fk);


--
-- Name: user_fk_idx; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX user_fk_idx ON user_note_property USING btree (user_fk);


--
-- Name: user_profile_first_name_index; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX user_profile_first_name_index ON user_profile USING btree (first_name);


--
-- Name: user_profile_last_name_index; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX user_profile_last_name_index ON user_profile USING btree (last_name);


--
-- Name: user_user_email_index; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX user_user_email_index ON user_user USING btree (email);


--
-- Name: categorized_tag_category_fkc; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY core_tag
    ADD CONSTRAINT categorized_tag_category_fkc FOREIGN KEY (category_fk) REFERENCES core_tag_category(id);


--
-- Name: configuration_client_config_fc; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY configuration
    ADD CONSTRAINT configuration_client_config_fc FOREIGN KEY (client_config_fk) REFERENCES configuration_client(id);


--
-- Name: configuration_client_default_c; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY configuration_client
    ADD CONSTRAINT configuration_client_default_c FOREIGN KEY (default_blog_fk) REFERENCES core_blog(id);


--
-- Name: configuration_confluenceifkc; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY configuration_confluence
    ADD CONSTRAINT configuration_confluenceifkc FOREIGN KEY (id) REFERENCES configuration_external_sys(id);


--
-- Name: configuration_external_systemc; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY configuration_external_sys
    ADD CONSTRAINT configuration_external_systemc FOREIGN KEY (configuration_fk) REFERENCES configuration(id);


--
-- Name: configuration_ldap_group_GROUC; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY configuration_ldap_group
    ADD CONSTRAINT "configuration_ldap_group_GROUC" FOREIGN KEY (group_search_fk) REFERENCES configuration_ldap_search(id);


--
-- Name: configuration_ldap_sbase_LDAPC; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY configuration_ldap_sbase
    ADD CONSTRAINT "configuration_ldap_sbase_LDAPC" FOREIGN KEY (ldap_search_configuration_fk) REFERENCES configuration_ldap_search(id);


--
-- Name: configuration_ldapifkc; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY configuration_ldap
    ADD CONSTRAINT configuration_ldapifkc FOREIGN KEY (id) REFERENCES configuration_external_sys(id);


--
-- Name: configuration_setting_configuc; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY configuration_setting
    ADD CONSTRAINT configuration_setting_configuc FOREIGN KEY (configuration_fk) REFERENCES configuration(id);


--
-- Name: core_attachment_UPLOADER_FKC; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY core_attachment
    ADD CONSTRAINT "core_attachment_UPLOADER_FKC" FOREIGN KEY (uploader_fk) REFERENCES user_user(id);


--
-- Name: core_attachment_global_id_fkc; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY core_attachment
    ADD CONSTRAINT core_attachment_global_id_fkc FOREIGN KEY (global_id_fk) REFERENCES core_global_id(id);


--
-- Name: core_attachment_note_fkc; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY core_attachment
    ADD CONSTRAINT core_attachment_note_fkc FOREIGN KEY (note_fk) REFERENCES core_note(id);


--
-- Name: core_attachment_property_ATTAC; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY core_attachment_property
    ADD CONSTRAINT "core_attachment_property_ATTAC" FOREIGN KEY (attachment_fk) REFERENCES core_attachment(id);


--
-- Name: core_blog_CHILDREN_FKC; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY core_blog2blog
    ADD CONSTRAINT "core_blog_CHILDREN_FKC" FOREIGN KEY (children_fk) REFERENCES core_blog(id);


--
-- Name: core_blog_PARENTS_FKC; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY core_blog2blog
    ADD CONSTRAINT "core_blog_PARENTS_FKC" FOREIGN KEY (parents_fk) REFERENCES core_blog(id);


--
-- Name: core_blog_global_id_fkc; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY core_blog
    ADD CONSTRAINT core_blog_global_id_fkc FOREIGN KEY (global_id_fk) REFERENCES core_global_id(id);


--
-- Name: core_blog_member_blog_fkc; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY core_blog_member
    ADD CONSTRAINT core_blog_member_blog_fkc FOREIGN KEY (blog_fk) REFERENCES core_blog(id);


--
-- Name: core_blog_member_kenmei_entitc; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY core_blog_member
    ADD CONSTRAINT core_blog_member_kenmei_entitc FOREIGN KEY (kenmei_entity_fk) REFERENCES user_entity(id);


--
-- Name: core_blog_notes_fkc; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY core_notes2crossblogs
    ADD CONSTRAINT core_blog_notes_fkc FOREIGN KEY (notes_fk) REFERENCES core_note(id);


--
-- Name: core_blog_property_BLOG_FKC; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY core_blog_property
    ADD CONSTRAINT "core_blog_property_BLOG_FKC" FOREIGN KEY (blog_fk) REFERENCES core_blog(id);


--
-- Name: core_blog_tags_fkc; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY core_blog2tag
    ADD CONSTRAINT core_blog_tags_fkc FOREIGN KEY (tags_fk) REFERENCES core_tag(id);


--
-- Name: core_external_object_blog_fkc; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY core_external_object
    ADD CONSTRAINT core_external_object_blog_fkc FOREIGN KEY (blog_fk) REFERENCES core_blog(id);


--
-- Name: core_external_object_propertic; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY core_external_object_prop
    ADD CONSTRAINT core_external_object_propertic FOREIGN KEY (external_object_fk) REFERENCES core_external_object(id);


--
-- Name: core_global_id_KENMEI_USERS_FC; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY core_user2follows
    ADD CONSTRAINT "core_global_id_KENMEI_USERS_FC" FOREIGN KEY (kenmei_users_fk) REFERENCES user_user(id);


--
-- Name: core_global_id_NOTES_FKC; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY core_note2followable
    ADD CONSTRAINT "core_global_id_NOTES_FKC" FOREIGN KEY (notes_fk) REFERENCES core_note(id);


--
-- Name: core_note_DIRECT_USERS_FKC; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY core_note2direct_user
    ADD CONSTRAINT "core_note_DIRECT_USERS_FKC" FOREIGN KEY (direct_users_fk) REFERENCES user_user(id);


--
-- Name: core_note_FOLLOWABLE_ITEMS_FKC; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY core_note2followable
    ADD CONSTRAINT "core_note_FOLLOWABLE_ITEMS_FKC" FOREIGN KEY (followable_items_fk) REFERENCES core_global_id(id);


--
-- Name: core_note_blog_fkc; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY core_note
    ADD CONSTRAINT core_note_blog_fkc FOREIGN KEY (blog_fk) REFERENCES core_blog(id);


--
-- Name: core_note_content_fkc; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY core_note
    ADD CONSTRAINT core_note_content_fkc FOREIGN KEY (content_fk) REFERENCES core_content(id);


--
-- Name: core_note_crosspost_blogs_fkc; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY core_notes2crossblogs
    ADD CONSTRAINT core_note_crosspost_blogs_fkc FOREIGN KEY (crosspost_blogs_fk) REFERENCES core_blog(id);


--
-- Name: core_note_favorite_users_fkc; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY core_users2favorite_notes
    ADD CONSTRAINT core_note_favorite_users_fkc FOREIGN KEY (favorite_users_fk) REFERENCES user_user(id);


--
-- Name: core_note_global_id_fkc; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY core_note
    ADD CONSTRAINT core_note_global_id_fkc FOREIGN KEY (global_id_fk) REFERENCES core_global_id(id);


--
-- Name: core_note_origin_fkc; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY core_note
    ADD CONSTRAINT core_note_origin_fkc FOREIGN KEY (origin_fk) REFERENCES core_note(id);


--
-- Name: core_note_parent_fkc; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY core_note
    ADD CONSTRAINT core_note_parent_fkc FOREIGN KEY (parent_fk) REFERENCES core_note(id);


--
-- Name: core_note_properties_NOTE_FKC; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY core_note_property
    ADD CONSTRAINT "core_note_properties_NOTE_FKC" FOREIGN KEY (note_fk) REFERENCES core_note(id);


--
-- Name: core_note_tags_fkc; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY core_notes2tag
    ADD CONSTRAINT core_note_tags_fkc FOREIGN KEY (tags_fk) REFERENCES core_tag(id);


--
-- Name: core_note_user_fkc; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY core_note
    ADD CONSTRAINT core_note_user_fkc FOREIGN KEY (user_fk) REFERENCES user_user(id);


--
-- Name: core_note_users_to_be_notifiec; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY core_notes2user_to_notify
    ADD CONSTRAINT core_note_users_to_be_notifiec FOREIGN KEY (users_to_be_notified_fk) REFERENCES user_user(id);


--
-- Name: core_role2blog_granting_groupc; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY core_role2blog_granting_group
    ADD CONSTRAINT core_role2blog_granting_groupc FOREIGN KEY (granting_groups_fk) REFERENCES user_group(id);


--
-- Name: core_tag_DESCRIPTIONS_FKC; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY core_tag2descriptions
    ADD CONSTRAINT "core_tag_DESCRIPTIONS_FKC" FOREIGN KEY (descriptions_fk) REFERENCES custom_messages(id);


--
-- Name: core_tag_KENMEI_ENTITIES_FKC; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY core_entity2tags
    ADD CONSTRAINT "core_tag_KENMEI_ENTITIES_FKC" FOREIGN KEY (kenmei_entities_fk) REFERENCES user_entity(id);


--
-- Name: core_tag_NAMES_FKC; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY core_tag2names
    ADD CONSTRAINT "core_tag_NAMES_FKC" FOREIGN KEY (names_fk) REFERENCES custom_messages(id);


--
-- Name: core_tag_blogs_fkc; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY core_blog2tag
    ADD CONSTRAINT core_tag_blogs_fkc FOREIGN KEY (blogs_fk) REFERENCES core_blog(id);


--
-- Name: core_tag_global_id_fkc; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY core_tag
    ADD CONSTRAINT core_tag_global_id_fkc FOREIGN KEY (global_id_fk) REFERENCES core_global_id(id);


--
-- Name: core_tag_notes_fkc; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY core_notes2tag
    ADD CONSTRAINT core_tag_notes_fkc FOREIGN KEY (notes_fk) REFERENCES core_note(id);


--
-- Name: core_task_execs_TASK_FKC; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY core_task_execs
    ADD CONSTRAINT "core_task_execs_TASK_FKC" FOREIGN KEY (task_fk) REFERENCES core_task(id);


--
-- Name: core_task_props_TASK_FKC; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY core_task_props
    ADD CONSTRAINT "core_task_props_TASK_FKC" FOREIGN KEY (task_fk) REFERENCES core_task(id);


--
-- Name: custom_messages_TAGS_DESC_FKC; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY core_tag2descriptions
    ADD CONSTRAINT "custom_messages_TAGS_DESC_FKC" FOREIGN KEY (tags_fk) REFERENCES core_tag(id);


--
-- Name: custom_messages_TAGS_FKC; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY core_tag2names
    ADD CONSTRAINT "custom_messages_TAGS_FKC" FOREIGN KEY (tags_fk) REFERENCES core_tag(id);


--
-- Name: custom_messages_language_fkc; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY custom_messages
    ADD CONSTRAINT custom_messages_language_fkc FOREIGN KEY (language_fk) REFERENCES md_language(id);


--
-- Name: ip_range_filter_c_ex; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY iprange_range
    ADD CONSTRAINT ip_range_filter_c_ex FOREIGN KEY (ip_range_filter_ex_fk) REFERENCES iprange_filter(id);


--
-- Name: ip_range_filter_c_in; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY iprange_range
    ADD CONSTRAINT ip_range_filter_c_in FOREIGN KEY (ip_range_filter_in_fk) REFERENCES iprange_filter(id);


--
-- Name: iprange_channel_ip_range_filtc; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY iprange_filter_channel
    ADD CONSTRAINT iprange_channel_ip_range_filtc FOREIGN KEY (ip_range_filters_fk) REFERENCES iprange_filter(id);


--
-- Name: iprange_filter_channels_fkc; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY iprange_filter_channel
    ADD CONSTRAINT iprange_filter_channels_fkc FOREIGN KEY (channels_fk) REFERENCES iprange_channel(type);


--
-- Name: mc_config_notification_configc; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY mc_config
    ADD CONSTRAINT mc_config_notification_configc FOREIGN KEY (notification_config_fk) REFERENCES notification_config(id);


--
-- Name: security_code_kenmei_user_fkc; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY security_code
    ADD CONSTRAINT security_code_kenmei_user_fkc FOREIGN KEY (kenmei_user_fk) REFERENCES user_user(id);


--
-- Name: security_email_codeifkc; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY security_email_code
    ADD CONSTRAINT security_email_codeifkc FOREIGN KEY (id) REFERENCES security_code(id);


--
-- Name: security_forgotten_pw_codeifkc; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY security_forgotten_pw_code
    ADD CONSTRAINT security_forgotten_pw_codeifkc FOREIGN KEY (id) REFERENCES security_code(id);


--
-- Name: security_invite_blogifkc; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY security_invite_blog
    ADD CONSTRAINT security_invite_blogifkc FOREIGN KEY (id) REFERENCES security_code(id);


--
-- Name: security_invite_clientifkc; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY security_invite_client
    ADD CONSTRAINT security_invite_clientifkc FOREIGN KEY (id) REFERENCES security_code(id);


--
-- Name: security_user_auth_failed_stac; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY security_user_status
    ADD CONSTRAINT security_user_auth_failed_stac FOREIGN KEY (kenmei_user_fk) REFERENCES user_user(id);


--
-- Name: security_user_codeifkc; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY security_user_code
    ADD CONSTRAINT security_user_codeifkc FOREIGN KEY (id) REFERENCES security_code(id);


--
-- Name: security_user_unlock_codeifkc; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY security_user_unlock_code
    ADD CONSTRAINT security_user_unlock_codeifkc FOREIGN KEY (id) REFERENCES security_code(id);


--
-- Name: user_authorities_kenmei_user_c; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY user_authorities
    ADD CONSTRAINT user_authorities_kenmei_user_c FOREIGN KEY (kenmei_user_fk) REFERENCES user_user(id);


--
-- Name: user_contact_country_fkc; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY user_contact
    ADD CONSTRAINT user_contact_country_fkc FOREIGN KEY (country_fk) REFERENCES md_country(id);


--
-- Name: user_entity_TAGS_FKC; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY core_entity2tags
    ADD CONSTRAINT "user_entity_TAGS_FKC" FOREIGN KEY (tags_fk) REFERENCES core_tag(id);


--
-- Name: user_entity_global_id_fkc; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY user_entity
    ADD CONSTRAINT user_entity_global_id_fkc FOREIGN KEY (global_id_fk) REFERENCES core_global_id(id);


--
-- Name: user_entity_groups_fkc; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY user_group_member
    ADD CONSTRAINT user_entity_groups_fkc FOREIGN KEY (groups_fk) REFERENCES user_group(id);


--
-- Name: user_external_auth_kenmei_usec; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY user_external_auth
    ADD CONSTRAINT user_external_auth_kenmei_usec FOREIGN KEY (kenmei_user_fk) REFERENCES user_user(id);


--
-- Name: user_group_externalifkc; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY user_group_external
    ADD CONSTRAINT user_group_externalifkc FOREIGN KEY (id) REFERENCES user_group(id);


--
-- Name: user_group_group_members_fkc; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY user_group_member
    ADD CONSTRAINT user_group_group_members_fkc FOREIGN KEY (group_members_fk) REFERENCES user_entity(id);


--
-- Name: user_group_property_KENMEI_ENC; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY user_group_property
    ADD CONSTRAINT "user_group_property_KENMEI_ENC" FOREIGN KEY (kenmei_entity_group_fk) REFERENCES user_group(id);


--
-- Name: user_group_user_to_blog_role_c; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY core_role2blog_granting_group
    ADD CONSTRAINT user_group_user_to_blog_role_c FOREIGN KEY (user_to_blog_role_mappings_fk) REFERENCES core_role2blog(id);


--
-- Name: user_groupifkc; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY user_group
    ADD CONSTRAINT user_groupifkc FOREIGN KEY (id) REFERENCES user_entity(id);


--
-- Name: user_navigation_item_OWNER_FKC; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY user_navigation_item
    ADD CONSTRAINT "user_navigation_item_OWNER_FKC" FOREIGN KEY (owner_fk) REFERENCES user_user(id);


--
-- Name: user_note_entity_note_fkc; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY user_note_entity
    ADD CONSTRAINT user_note_entity_note_fkc FOREIGN KEY (note_fk) REFERENCES core_note(id);


--
-- Name: user_note_entity_user_fkc; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY user_note_entity
    ADD CONSTRAINT user_note_entity_user_fkc FOREIGN KEY (user_fk) REFERENCES user_user(id);


--
-- Name: user_note_property_NOTE_FKC; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY user_note_property
    ADD CONSTRAINT "user_note_property_NOTE_FKC" FOREIGN KEY (note_fk) REFERENCES core_note(id);


--
-- Name: user_note_property_USER_FKC; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY user_note_property
    ADD CONSTRAINT "user_note_property_USER_FKC" FOREIGN KEY (user_fk) REFERENCES user_user(id);


--
-- Name: user_note_property_user_note_c; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY user_note_property
    ADD CONSTRAINT user_note_property_user_note_c FOREIGN KEY (user_note_entity_fk) REFERENCES user_note_entity(id);


--
-- Name: user_of_group_group_fkc; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY user_of_group
    ADD CONSTRAINT user_of_group_group_fkc FOREIGN KEY (group_fk) REFERENCES user_group(id);


--
-- Name: user_of_group_user_fkc; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY user_of_group
    ADD CONSTRAINT user_of_group_user_fkc FOREIGN KEY (user_fk) REFERENCES user_user(id);


--
-- Name: user_profile_contact_fkc; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY user_profile
    ADD CONSTRAINT user_profile_contact_fkc FOREIGN KEY (contact_fk) REFERENCES user_contact(id);


--
-- Name: user_profile_large_image_fkc; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY user_profile
    ADD CONSTRAINT user_profile_large_image_fkc FOREIGN KEY (large_image_fk) REFERENCES user_image(id);


--
-- Name: user_profile_medium_image_fkc; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY user_profile
    ADD CONSTRAINT user_profile_medium_image_fkc FOREIGN KEY (medium_image_fk) REFERENCES user_image(id);


--
-- Name: user_profile_notification_conc; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY user_profile
    ADD CONSTRAINT user_profile_notification_conc FOREIGN KEY (notification_config_fk) REFERENCES notification_config(id);


--
-- Name: user_profile_small_image_fkc; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY user_profile
    ADD CONSTRAINT user_profile_small_image_fkc FOREIGN KEY (small_image_fk) REFERENCES user_image(id);


--
-- Name: user_user_DIRECT_NOTES_FKC; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY core_note2direct_user
    ADD CONSTRAINT "user_user_DIRECT_NOTES_FKC" FOREIGN KEY (direct_notes_fk) REFERENCES core_note(id);


--
-- Name: user_user_FOLLOWED_ITEMS_FKC; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY core_user2follows
    ADD CONSTRAINT "user_user_FOLLOWED_ITEMS_FKC" FOREIGN KEY (followed_items_fk) REFERENCES core_global_id(id);


--
-- Name: user_user_favorite_notes_fkc; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY core_users2favorite_notes
    ADD CONSTRAINT user_user_favorite_notes_fkc FOREIGN KEY (favorite_notes_fk) REFERENCES core_note(id);


--
-- Name: user_user_notes_fkc; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY core_notes2user_to_notify
    ADD CONSTRAINT user_user_notes_fkc FOREIGN KEY (notes_fk) REFERENCES core_note(id);


--
-- Name: user_user_profile_fkc; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY user_user
    ADD CONSTRAINT user_user_profile_fkc FOREIGN KEY (profile_fk) REFERENCES user_profile(id);


--
-- Name: user_user_property_KENMEI_USEC; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY user_user_property
    ADD CONSTRAINT "user_user_property_KENMEI_USEC" FOREIGN KEY (kenmei_user_fk) REFERENCES user_user(id);


--
-- Name: user_userifkc; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY user_user
    ADD CONSTRAINT user_userifkc FOREIGN KEY (id) REFERENCES user_entity(id);


--
-- PostgreSQL database dump complete
--

