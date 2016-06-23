 delete from core_uti where id not in (select p.id from core_utp p);
 delete from core_utr2tag where user_tagged_items_fk not in (select p.id from core_utp p);