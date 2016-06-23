CREATE TEMP TABLE core_tag_temp (LIKE core_tag);
UPDATE core_tag SET tag_store_alias = 'DefaultNoteTagStore';
-- Einfuegen der der Tags, Duplikate werden gefiltert(max(global_id) = max(id) ) ?
INSERT INTO core_tag_temp (id, global_id_fk, tag_store_tag_id,class, default_name, tag_store_alias) 
SELECT  max(id), max(global_id_fk), tag_store_tag_id, 'TagImpl', '', 'DefaultNoteTagStore' FROM core_tag GROUP BY tag_store_tag_id;
-- Default name setzen 
UPDATE core_tag_temp SET default_name = tags.default_name FROM core_tag as tags WHERE core_tag_temp.id = tags.id;

-- Verknuepfungen von Beitraege und Tags bereinigen
CREATE TEMP TABLE core_notes2tag_temp (LIKE core_notes2tag);
INSERT INTO core_notes2tag_temp select * from core_notes2tag;
UPDATE core_notes2tag_temp SET tags_fk = temp_tags.id FROM core_tag, core_tag_temp as temp_tags
        WHERE core_notes2tag_temp.tags_fk = core_tag.id
          AND core_tag.tag_store_tag_id = temp_tags.tag_store_tag_id
          AND core_tag.id != temp_tags.id
          AND core_tag.tag_store_alias = 'DefaultNoteTagStore';
DELETE FROM core_notes2tag;
INSERT INTO core_notes2tag select min(notes_fk), min(tags_fk) from core_notes2tag_temp n2t group by n2t.notes_fk, n2t.tags_fk;

-- Verknuepfungen von Blogs und Tags bereinigen
CREATE TEMP TABLE core_blog2tag_temp (LIKE core_blog2tag);
INSERT INTO core_blog2tag_temp select * from core_blog2tag;   
UPDATE core_blog2tag_temp SET tags_fk = temp_tags.id FROM core_tag, core_tag_temp as temp_tags
        WHERE core_blog2tag_temp.tags_fk = core_tag.id
          AND core_tag.tag_store_tag_id = temp_tags.tag_store_tag_id
          AND core_tag.id != temp_tags.id
          AND core_tag.tag_store_alias = 'DefaultNoteTagStore';      
DELETE FROM core_blog2tag;
INSERT INTO core_blog2tag select min(blogs_fk), min(tags_fk) from core_blog2tag_temp b2t group by b2t.blogs_fk, b2t.tags_fk;

-- Tags fuer Blogs einfuegen und Verknuepfungen aktualisieren      
INSERT INTO core_tag (id, class, tag_store_tag_id, default_name, tag_store_alias) 
        SELECT nextval('core_tag_seq'),'TagImpl', max(core_tag.tag_store_tag_id) , max(core_tag.default_name), 'DefaultBlogTagStore' FROM core_tag,core_blog2tag  WHERE core_tag.id = core_blog2tag.tags_fk GROUP BY core_blog2tag.tags_fk;
UPDATE core_blog2tag SET tags_fk = temp_tags2.id FROM core_tag as temp_tags, core_tag as temp_tags2
        WHERE core_blog2tag.tags_fk = temp_tags.id
          AND temp_tags2.tag_store_tag_id = temp_tags.tag_store_tag_id
          AND temp_tags.id != temp_tags2.id
          AND temp_tags2.tag_store_alias = 'DefaultBlogTagStore';
          
-- Loeschen von nicht mehr verwendeten duplikaten Tags
DELETE FROM core_tag WHERE id not in (SELECT tags_fk FROM core_blog2tag UNION SELECT tags_fk FROM core_notes2tag);

-- Global ID von Tag aktualisieren von /{}/tags/lowerName => /{}/tags/12 
UPDATE core_global_id SET global_identifier = ('/' || settings.setting_value || '/tag/' || tags.id) 
        FROM configuration_setting as settings,core_tag as tags
        WHERE core_global_id.id = tags.global_id_fk
          AND settings.setting_key = 'kenmei.unique.client.identifer';
