--
-- View to assign a user to these utr he can access in protected mode, 
-- which are clearanced from some user of his group
--
CREATE OR REPLACE VIEW protected_access AS

select distinct
	ua.id as user_id, 
	utr.id as utr_id, 
	utr.is_anonym as is_anonym
from 
	tag_clearance c,
	tag_clearances2user_groups c2ug,
	tagging_user_group g,
	tagging_user ua,
	tagging_users2user_groups u2ug,
	tag_clearances2tags c2t,
	tag tc,
	user_tagged_resource utr,
	tags2user_tagged_resources t2utr
where 
	c2ug.tag_clearances_fk=c.id
	and c2ug.user_groups_fk=g.id
	and u2ug.tagging_users_fk=ua.id
	and u2ug.user_groups_fk=g.id
	and c2t.tag_clearances_fk=c.id
	and c2t.tags_fk=tc.id
	and c.owner_fk=utr.user_fk
	and t2utr.tags_fk=tc.id
	and t2utr.user_tagged_resources_fk=utr.id;