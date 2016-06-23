insert into core_attachment (
   ID,
   CONTENT_IDENTIFIER,
   REPOSITORY_IDENTIFIER,
   NAME
)
select r.id+100, r.CONTENT_IDENTIFIER, r.REPOSITORY_IDENTIFIER, r.label
from core_resource r where  class='ContentResourceImpl';
   
update core_attachment set note_fk = (
   select note.id from core_uti note where note.resource_fk = (
       select att.text_resource_fk from core_resource att where att.id = core_attachment.id 
       and class = 'ContentResourceImpl'
   )
);

select * from user_user;
