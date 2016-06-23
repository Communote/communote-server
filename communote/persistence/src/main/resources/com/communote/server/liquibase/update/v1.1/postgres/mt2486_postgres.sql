--drop sequence core_source_seq;
--drop sequence core_source_meta_seq;

--remove old constraints or the ones to be renamed
----ALTER TABLE ONLY core_uti DROP CONSTRAINT core_uti_resource_fkc;
----ALTER TABLE ONLY core_uti DROP CONSTRAINT core_uti_user_fkc;
-- alter table ONLY core_uti drop constraint unique_user_resource;
--alter table ONLY core_utp drop constraint core_utpifkc;
--alter table ONLY core_utr drop constraint core_utrifkc;
--ALTER TABLE ONLY core_utr2tag DROP CONSTRAINT core_tag_user_tagged_items_fkc;
--ALTER TABLE ONLY core_utr2tag DROP CONSTRAINT core_uti_tags_fkc;
--ALTER TABLE ONLY core_uti2user_to_notify DROP CONSTRAINT core_uti_users_to_be_notifiedc;
--ALTER TABLE ONLY core_uti2user_to_notify DROP CONSTRAINT user_user_user_tagged_items_fc;
---- ALTER TABLE ONLY core_resource DROP CONSTRAINT unique_resource; 
--ALTER TABLE ONLY core_resource DROP CONSTRAINT core_resource_content_text_rec;
--ALTER TABLE ONLY core_resource DROP CONSTRAINT core_resource_pkey;
--ALTER TABLE ONLY core_utr2tag DROP CONSTRAINT core_utr2tag_pkey;
--ALTER TABLE ONLY core_uti DROP CONSTRAINT core_uti_pkey;
--ALTER TABLE ONLY core_uti2user_to_notify DROP CONSTRAINT core_uti2user_to_notify_pkey;

---- global id
--create table core_global_id (
--   ID BIGINT not null,
--   GLOBAL_IDENTIFIER CHARACTER VARYING(300) not null unique,
--   primary key (ID)
-- );
-- create sequence core_global_id_seq;
 
---- resources and attachments
-- create table core_attachment (
--   ID BIGINT not null,
--   CONTENT_IDENTIFIER CHARACTER VARYING(1024) not null,
--   REPOSITORY_IDENTIFIER CHARACTER VARYING(1024) not null,
--   NAME CHARACTER VARYING(1024) not null,
--   GLOBAL_ID_FK BIGINT unique,
--   NOTE_FK BIGINT,
--   primary key (ID)
-- );
   
-- db specific code END part 1
 insert into core_attachment (
   ID,
   CONTENT_IDENTIFIER,
   REPOSITORY_IDENTIFIER,
   NAME
 )
   select r.id, r.CONTENT_IDENTIFIER, r.REPOSITORY_IDENTIFIER, r.label
   from core_resource r where  class='ContentResourceImpl';
   
 update core_attachment set note_fk = (
   select note.id from core_uti note where note.resource_fk = (
     select att.text_resource_fk from core_resource att where att.id = core_attachment.id and class = 'ContentResourceImpl'
   )
 );
 
create sequence core_attachment_seq;
select setval('core_attachment_seq', (SELECT max(id)+1 FROM core_attachment));
-- db specific code END part 2
 
  
   
 alter table core_resource drop column label;
 alter table core_resource drop column language;
 alter table core_resource drop column creation_date;
 alter table core_resource drop column url;
 alter table core_resource drop column content_identifier;
 alter table core_resource drop column repository_identifier;
 alter table core_resource drop column text_resource_fk;
 
 delete from core_resource where class = 'ContentResourceImpl';
 delete from core_resource where class = 'WebResourceImpl';
 
 alter table core_resource drop column class;
      
 alter table core_resource rename to core_content;
 alter table core_resource_seq rename TO core_content_seq;
 alter table core_content alter column content SET NOT NULL;

 

 
 -- the note
 delete from core_uti where id not in (select p.id from core_utp p);
 delete from core_utr2tag where user_tagged_items_fk not in (select p.id from core_utp p);

 alter table core_uti rename to core_note;
 alter table core_uti_seq rename TO core_note_seq;
    
 alter table core_note add column GLOBAL_ID_FK BIGINT;
 alter table core_note add column DIRECT BOOLEAN;
 alter table core_note add column STATUS CHARACTER VARYING(1024);
 alter table core_note add column VERSION BIGINT;
 alter table core_note rename column RESOURCE_FK to CONTENT_FK;
 alter table core_note add column BLOG_FK BIGINT;
 alter table core_note add column PARENT_FK BIGINT;
 alter table core_note add column ORIGIN_FK BIGINT;
     
 update core_note set DIRECT=false;
 update core_note note set STATUS = (select STATUS from core_utp where core_utp.id = note.id);
 update core_note note set VERSION = (select VERSION from core_utp where core_utp.id = note.id);
 update core_note note set BLOG_FK = (select BLOG_FK from core_utp where core_utp.id = note.id);
 update core_note note set PARENT_FK = (select PARENT_FK from core_utp where core_utp.id = note.id);
 update core_note note set ORIGIN_FK = (select ORIGIN_FK from core_utp where core_utp.id = note.id);

 alter table core_note alter column DIRECT SET NOT NULL;
 alter table core_note alter column STATUS SET NOT NULL;
 alter table core_note alter column VERSION SET NOT NULL;

 drop table core_utp;
 drop table core_utr;
 
 alter table core_utr2tag rename to core_notes2tag;
 alter table core_notes2tag rename column USER_TAGGED_ITEMS_FK to notes_fk;

 alter table core_blog add column GLOBAL_ID_FK BIGINT;
 alter table core_tag add column GLOBAL_ID_FK BIGINT;
 alter table user_user  add column GLOBAL_ID_FK BIGINT;

 -- blog
 drop table core_tag_category2blog;
 alter table core_tag_clearance drop column user_group_fk;
 

-- user     
 create table FAVORITE_NOTES2KENMEI_USERS (
   KENMEI_USERS_FK BIGINT not null,
   FAVORITE_NOTES_FK BIGINT not null,
   primary key (KENMEI_USERS_FK, FAVORITE_NOTES_FK)
 );
 
 alter table core_uti2user_to_notify rename to core_notes2user_to_notify;
 alter table core_notes2user_to_notify rename column USER_TAGGED_ITEMS_FK to notes_fk;
 
 ALTER TABLE news_widget_feed RENAME COLUMN TAGGING_USER_FK TO KENMEI_USER_FK;
 ALTER TABLE security_code RENAME COLUMN TAGGING_USER_FK TO KENMEI_USER_FK;
 ALTER TABLE security_user_auth_failed_status RENAME COLUMN TAGGING_USER_FK TO KENMEI_USER_FK;
 ALTER TABLE user_authorities RENAME COLUMN TAGGING_USER_FK TO KENMEI_USER_FK;
   
 ALTER TABLE user_group DROP COLUMN DESCRIPTION;
 ALTER TABLE user_group DROP COLUMN VISIBILITY_TYPE;
 ALTER TABLE user_group DROP COLUMN GROUP_TYPE;
 ALTER TABLE user_group DROP COLUMN TITLE; 
 
 
 -- configuration  alter table configuration_client drop column MODULE_STATUS_tagging_server;
 alter table configuration_client drop column MODULE_STATUS_micro_blog;
 alter table configuration drop column ldap_config_fk;
 
 -- client
 alter table user_client_creation drop column module_status_tagging_server;
 alter table user_client_creation drop column module_status_micro_blog;
    
 -- constraints (primary key)
 ALTER TABLE ONLY core_content ADD CONSTRAINT core_content_pkey PRIMARY KEY (id);
 ALTER TABLE ONLY core_note ADD CONSTRAINT core_note_pkey PRIMARY KEY (id);
 ALTER TABLE ONLY core_notes2tag ADD CONSTRAINT core_notes2tag_pkey PRIMARY KEY (notes_fk, tags_fk);
 ALTER TABLE ONLY core_notes2user_to_notify ADD CONSTRAINT core_notes2user_to_notify_pkey PRIMARY KEY (notes_fk, users_to_be_notified_fk);

 -- constraints (foreign key, unique)
 alter table core_attachment  add constraint core_attachment_NOTE_FKC foreign key (NOTE_FK) references core_note;
 alter table core_attachment  add constraint core_attachment_GLOBAL_ID_FKC foreign key (GLOBAL_ID_FK) references core_global_id;
 alter table core_blog add constraint core_blog_GLOBAL_ID_FKC foreign key (GLOBAL_ID_FK) references core_global_id;
 ALTER TABLE ONLY core_blog ADD CONSTRAINT core_blog_global_id_fk_key UNIQUE (global_id_fk);
 
 
 ALTER TABLE ONLY core_note ADD CONSTRAINT core_note_content_fk_key UNIQUE (content_fk);
 ALTER TABLE ONLY core_note ADD CONSTRAINT core_note_global_id_fk_key UNIQUE (global_id_fk);
 alter table core_note add constraint core_note_PARENT_FKC foreign key (PARENT_FK) references core_note;
 alter table core_note add constraint core_note_CONTENT_FKC foreign key (CONTENT_FK) references core_content;
 alter table core_note add constraint core_note_USER_FKC foreign key (USER_FK) references user_user;
 alter table core_note add constraint core_note_ORIGIN_FKC foreign key (ORIGIN_FK) references core_note;
 alter table core_note add constraint core_note_BLOG_FKC foreign key (BLOG_FK) references core_blog;
 alter table core_note add constraint core_note_GLOBAL_ID_FKC foreign key (GLOBAL_ID_FK) references core_global_id;    
   
 alter table core_notes2tag add constraint core_note_TAGS_FKC foreign key (TAGS_FK) references core_tag;
 alter table core_notes2tag add constraint core_tag_NOTES_FKC foreign key (NOTES_FK) references core_note;
 alter table core_notes2user_to_notify add constraint core_note_USERS_TO_BE_NOTIFIEC foreign key (USERS_TO_BE_NOTIFIED_FK) references user_user;
 alter table core_notes2user_to_notify  add constraint user_user_NOTES_FKC foreign key (NOTES_FK) references core_note;
 ALTER TABLE ONLY core_tag ADD CONSTRAINT core_tag_global_id_fk_key UNIQUE (global_id_fk);
 alter table core_tag  add constraint core_tag_GLOBAL_ID_FKC foreign key (GLOBAL_ID_FK) references core_global_id;
     
 alter table news_widget_feed  drop constraint news_widget_feed_TAGGING_USERC;
 alter table news_widget_feed  add constraint news_widget_feed_KENMEI_USER_C foreign key (KENMEI_USER_FK) references user_user;
   
 -- user
 alter table FAVORITE_NOTES2KENMEI_USERS add constraint user_user_FAVORITE_NOTES_FKC foreign key (FAVORITE_NOTES_FK) references core_note;
 alter table FAVORITE_NOTES2KENMEI_USERS  add constraint core_note_KENMEI_USERS_FKC  foreign key (KENMEI_USERS_FK)  references user_user;
 ALTER TABLE ONLY user_user ADD CONSTRAINT user_user_global_id_fk_key UNIQUE (global_id_fk);
 ALTER TABLE ONLY user_user ADD CONSTRAINT user_user_global_id_fkc FOREIGN KEY (global_id_fk) REFERENCES core_global_id(id);
 -- ALTER TABLE ONLY user_client_creation DROP CONSTRAINT user_client_creation_creator_alias_key;
 -- ALTER TABLE ONLY user_profile ADD CONSTRAINT user_profile_notification_config_fk_key UNIQUE (notification_config_fk);
 
-- insert global ids
alter table core_global_id add column help_id BIGINT;

insert into core_global_id (ID, GLOBAL_IDENTIFIER, HELP_ID) 
select nextval('core_global_id_seq'), 
(select '/' || sett.value from configuration_setting sett where sett.key = 'kenmei.unique.client.identifer') 
|| '/note/' 
|| note.id, 
note.id 
from core_note note;

insert into core_global_id (ID, GLOBAL_IDENTIFIER, HELP_ID) 
select nextval('core_global_id_seq'),
'/' ||
(select sett.value from configuration_setting sett where sett.key = 'kenmei.unique.client.identifer') 
|| '/tag/' 
|| tag.id, 
tag.id 
from core_tag tag;

insert into core_global_id (ID, GLOBAL_IDENTIFIER, HELP_ID) 
select nextval('core_global_id_seq'), 
'/' ||
(select sett.value from configuration_setting sett where sett.key = 'kenmei.unique.client.identifer') 
|| '/user/'
|| u.id, 
u.id 
from user_user u;

insert into core_global_id (ID, GLOBAL_IDENTIFIER, HELP_ID) 
select nextval('core_global_id_seq'), 
'/' ||
(select sett.value from configuration_setting sett where sett.key = 'kenmei.unique.client.identifer') 
|| '/attachment/' 
|| attachment.id, 
attachment.id 
from core_attachment attachment;


update core_note set global_id_fk = (
  select g.id from core_global_id g 
  where g.help_id = core_note.id and g.GLOBAL_IDENTIFIER like '%/note/%'
);

update user_user set global_id_fk = (
  select g.id from core_global_id g 
  where g.help_id = user_user.id and g.GLOBAL_IDENTIFIER like '%/user/%'
);

update core_attachment set global_id_fk = (
  select g.id from core_global_id g 
  where g.help_id = core_attachment.id and g.GLOBAL_IDENTIFIER like '%/attachment/%'
);

update core_tag set global_id_fk = (
  select g.id from core_global_id g 
  where g.help_id = core_tag.id and g.GLOBAL_IDENTIFIER like '%/tag/%'
);

alter table core_global_id DROP column help_id;
 