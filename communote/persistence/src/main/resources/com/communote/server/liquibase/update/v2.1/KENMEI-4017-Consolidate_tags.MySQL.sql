DROP TABLE core_tag_temp;
DROP TABLE core_notes2tag_temp;
DROP TABLE core_blog2tag_temp;
CREATE TEMPORARY TABLE core_tag_temp (LIKE core_tag);
UPDATE core_tag SET tag_store_alias = 'DefaultNoteTagStore';
-- Insert tags and filter duplicates (max(global_id) = max(id) ) ?
INSERT INTO core_tag_temp (id, global_id_fk, tag_store_tag_id,class, default_name, tag_store_alias) 
SELECT  max(id), max(global_id_fk), tag_store_tag_id, 'TagImpl', '', 'DefaultNoteTagStore' FROM core_tag GROUP BY tag_store_tag_id;
-- set default name
UPDATE core_tag_temp,core_tag as tags SET core_tag_temp.default_name = tags.default_name WHERE core_tag_temp.id = tags.id;

-- cleanup associations between notes and tags
CREATE TEMPORARY TABLE core_notes2tag_temp (LIKE core_notes2tag);
ALTER TABLE core_notes2tag_temp
    DROP INDEX `core_tag_NOTES_FKC`, DROP INDEX `core_note_TAGS_FKC`, DROP PRIMARY KEY; 
INSERT INTO core_notes2tag_temp select * from core_notes2tag;
UPDATE core_notes2tag_temp,core_tag, core_tag_temp as temp_tags SET core_notes2tag_temp.tags_fk = temp_tags.id
        WHERE core_notes2tag_temp.tags_fk = core_tag.id
          AND core_tag.tag_store_tag_id = temp_tags.tag_store_tag_id
          AND core_tag.id != temp_tags.id
          AND core_tag.tag_store_alias = 'DefaultNoteTagStore';
DELETE FROM core_notes2tag;
INSERT INTO core_notes2tag select min(notes_fk), min(tags_fk) from core_notes2tag_temp n2t group by n2t.notes_fk, n2t.tags_fk;

-- cleanup associations between notes and blogs
CREATE TEMPORARY TABLE core_blog2tag_temp (LIKE core_blog2tag);
ALTER TABLE core_blog2tag_temp
    DROP PRIMARY KEY; 
INSERT INTO core_blog2tag_temp select * from core_blog2tag;   
UPDATE core_blog2tag_temp, core_tag, core_tag_temp as temp_tags SET core_blog2tag_temp.tags_fk = temp_tags.id
        WHERE core_blog2tag_temp.tags_fk = core_tag.id
          AND core_tag.tag_store_tag_id = temp_tags.tag_store_tag_id
          AND core_tag.id != temp_tags.id
          AND core_tag.tag_store_alias = 'DefaultNoteTagStore';      
DELETE FROM core_blog2tag;
INSERT INTO core_blog2tag select min(blogs_fk), min(tags_fk) from core_blog2tag_temp b2t group by b2t.blogs_fk, b2t.tags_fk;

-- insert topic tags and cleanup associations   
INSERT INTO core_tag (class, tag_store_tag_id, default_name, tag_store_alias) 
        SELECT 'TagImpl', max(core_tag.tag_store_tag_id) , max(core_tag.default_name), 'DefaultBlogTagStore' FROM core_tag,core_blog2tag  WHERE core_tag.id = core_blog2tag.tags_fk GROUP BY core_blog2tag.tags_fk;
UPDATE core_blog2tag,core_tag as temp_tags, core_tag as temp_tags2 SET core_blog2tag.tags_fk = temp_tags2.id 
        WHERE core_blog2tag.tags_fk = temp_tags.id
          AND temp_tags2.tag_store_tag_id = temp_tags.tag_store_tag_id
          AND temp_tags.id != temp_tags2.id
          AND temp_tags2.tag_store_alias = 'DefaultBlogTagStore';
          
-- delete obsolete duplicate tags
DELETE FROM core_tag WHERE id not in (SELECT tags_fk FROM core_blog2tag UNION SELECT tags_fk FROM core_notes2tag);

-- update global IDs of tags: /{}/tags/lowerName => /{}/tags/12 
UPDATE core_global_id,configuration_setting as settings,core_tag as tags 
    SET core_global_id.global_identifier = concat('/' , settings.setting_value, '/tag/', tags.id) 
    WHERE core_global_id.id = tags.global_id_fk
      AND settings.setting_key = 'kenmei.unique.client.identifer';
