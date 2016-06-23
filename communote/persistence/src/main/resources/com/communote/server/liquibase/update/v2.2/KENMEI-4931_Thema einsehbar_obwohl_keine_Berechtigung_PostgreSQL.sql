-- Alte Daten loeschen

DELETE FROM core_role2blog_granting_group;
DELETE FROM core_role2blog WHERE granted_by_group = true;

-- Daten in core_role2blog einfuegen

    -- Viewer
    INSERT INTO core_role2blog (id, blog_id, user_id, numeric_role, external_system_id, granted_by_group)
    (
        -- Viewer
        SELECT DISTINCT nextval('core_role2blog_seq'), core_blog_member.blog_fk as blogId, user_of_group.user_fk as userId, 1, core_blog_member.external_system_id as externalSystemId, true
        FROM user_of_group, core_blog_member, user_group 
        WHERE 
        core_blog_member.kenmei_entity_fk  = user_group.id 
        AND core_blog_member.role = 'VIEWER'
        AND user_of_group.group_fk = user_group.id
        UNION
        -- Member
        SELECT DISTINCT nextval('core_role2blog_seq'),core_blog_member.blog_fk as blogId,user_of_group.user_fk as userId,2,
        core_blog_member.external_system_id as externalSystemId, true
        FROM 
        user_of_group, core_blog_member, user_group 
        WHERE 
        core_blog_member.kenmei_entity_fk  = user_group.id 
        AND core_blog_member.role = 'MEMBER'
        AND user_of_group.group_fk = user_group.id
         UNION
        -- Manager
        SELECT DISTINCT nextval('core_role2blog_seq'),core_blog_member.blog_fk as blogId, user_of_group.user_fk as userId, 3, core_blog_member.external_system_id as externalSystemId, true
        FROM user_of_group, core_blog_member, user_group 
        WHERE 
        core_blog_member.kenmei_entity_fk  = user_group.id 
        AND core_blog_member.role = 'MANAGER'
        AND user_of_group.group_fk = user_group.id
    );
    
-- Daten in core_role2blog_granting_group einfuegen


    INSERT INTO core_role2blog_granting_group (user_to_blog_role_mappings_fk,granting_groups_fk)
    (
        -- Viewer
        SELECT core_role2blog.id as user_to_blog_role_mappings_fk,
               core_blog_member.kenmei_entity_fk as granting_groups_fk
        FROM   user_of_group, core_blog_member, core_role2blog
        WHERE  user_of_group.group_fk = core_blog_member.kenmei_entity_fk
        AND    user_of_group.user_fk = core_role2blog.user_id 
        AND    core_role2blog.granted_by_group = true
        AND    core_blog_member.blog_fk = core_role2blog.blog_id
        AND    core_role2blog.numeric_role = 1
        AND    core_blog_member.role = 'VIEWER'
        UNION 
        -- Member
        SELECT core_role2blog.id as user_to_blog_role_mappings_fk,
               core_blog_member.kenmei_entity_fk as granting_groups_fk
        FROM   user_of_group,core_blog_member,core_role2blog
        WHERE  user_of_group.group_fk = core_blog_member.kenmei_entity_fk
        AND    user_of_group.user_fk = core_role2blog.user_id 
        AND    core_role2blog.granted_by_group = true
        AND    core_blog_member.blog_fk = core_role2blog.blog_id
        AND    core_role2blog.numeric_role = 2
        AND    core_blog_member.role = 'MEMBER'
        UNION
        -- Manager
        SELECT core_role2blog.id as user_to_blog_role_mappings_fk,
               core_blog_member.kenmei_entity_fk as granting_groups_fk
        FROM   user_of_group, core_blog_member,core_role2blog
        WHERE  user_of_group.group_fk = core_blog_member.kenmei_entity_fk
        AND    user_of_group.user_fk = core_role2blog.user_id 
        AND    core_role2blog.granted_by_group = true
        AND    core_blog_member.blog_fk = core_role2blog.blog_id
        AND    core_role2blog.numeric_role = 3
        AND    core_blog_member.role = 'MANAGER'
    );