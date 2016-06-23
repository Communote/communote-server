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