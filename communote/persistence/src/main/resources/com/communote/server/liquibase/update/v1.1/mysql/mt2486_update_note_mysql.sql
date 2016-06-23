update core_note set DIRECT=false;
update core_note note set STATUS = (select STATUS from core_utp where core_utp.id = note.id);
update core_note note set VERSION = (select VERSION from core_utp where core_utp.id = note.id);
update core_note note set BLOG_FK = (select BLOG_FK from core_utp where core_utp.id = note.id);
update core_note note set PARENT_FK = (select PARENT_FK from core_utp where core_utp.id = note.id);
update core_note note set ORIGIN_FK = (select ORIGIN_FK from core_utp where core_utp.id = note.id);