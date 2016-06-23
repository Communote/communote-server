alter table core_global_id add column help_id BIGINT;

insert into core_global_id (GLOBAL_IDENTIFIER, HELP_ID) 
select  
CONCAT('/', (select sett.value from configuration_setting sett where sett.key = 'kenmei.unique.client.identifer'), '/note/', note.id), 
note.id 
from core_note note;

insert into core_global_id (GLOBAL_IDENTIFIER, HELP_ID) 
select 
CONCAT('/', (select sett.value from configuration_setting sett where sett.key = 'kenmei.unique.client.identifer'), '/tag/', tag.id), 
tag.id 
from core_tag tag;

insert into core_global_id (GLOBAL_IDENTIFIER, HELP_ID) 
select  
CONCAT('/', (select sett.value from configuration_setting sett where sett.key = 'kenmei.unique.client.identifer'), '/user/', u.id), 
u.id 
from user_user u;

insert into core_global_id (GLOBAL_IDENTIFIER, HELP_ID) 
select 
CONCAT('/', (select sett.value from configuration_setting sett where sett.key = 'kenmei.unique.client.identifer'), '/attachment/', attachment.id),
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