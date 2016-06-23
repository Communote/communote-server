-- Alte Daten loeschen

DELETE FROM core_role2blog_granting_group;
DELETE FROM core_role2blog WHERE granted_by_group = 1;

-- Daten in core_role2blog einfuegen

    -- Viewer
    INSERT INTO core_role2blog (blog_id, user_id, numeric_role, external_system_id, granted_by_group)
    (
        SELECT DISTINCT core_blog_member.BLOG_FK as blogId, user_of_group.USER_FK as userId, 1, core_blog_member.external_system_id as externalSystemId, 1
        FROM user_of_group, core_blog_member, user_group 
        WHERE 
        core_blog_member.KENMEI_ENTITY_FK  = user_group.ID 
        AND core_blog_member.role = 'VIEWER'
        AND user_of_group.GROUP_FK = user_group.ID
        UNION
        -- Member
        SELECT DISTINCT core_blog_member.BLOG_FK as blogId,user_of_group.USER_FK as userId,2,
        core_blog_member.external_system_id as externalSystemId, 1
        FROM 
        user_of_group, core_blog_member, user_group 
        WHERE 
        core_blog_member.KENMEI_ENTITY_FK  = user_group.ID 
        AND core_blog_member.role = 'MEMBER'
        AND user_of_group.GROUP_FK = user_group.ID
         UNION
        -- Manager
        SELECT DISTINCT core_blog_member.BLOG_FK as blogId, user_of_group.USER_FK as userId, 3, core_blog_member.external_system_id as externalSystemId, 1
        FROM user_of_group, core_blog_member, user_group 
        WHERE 
        core_blog_member.KENMEI_ENTITY_FK  = user_group.ID
        AND core_blog_member.role = 'MANAGER'
        AND user_of_group.GROUP_FK = user_group.ID
    );
    
-- Daten in core_role2blog_granting_group einfuegen


    INSERT INTO core_role2blog_granting_group (USER_TO_BLOG_ROLE_MAPPINGS_FK,GRANTING_GROUPS_FK)
    (
        SELECT core_role2blog.ID as user_to_blog_role_mappings_fk,
               core_blog_member.KENMEI_ENTITY_FK as granting_groups_fk
        FROM   user_of_group, core_blog_member, core_role2blog
        WHERE  user_of_group.GROUP_FK = core_blog_member.KENMEI_ENTITY_FK
        AND    user_of_group.USER_FK = core_role2blog.user_id 
        AND    core_role2blog.granted_by_group = 1
        AND    core_blog_member.BLOG_FK = core_role2blog.blog_id
        AND    core_role2blog.numeric_role = 1
        AND    core_blog_member.role = 'VIEWER'
        UNION 
        -- Member
        SELECT core_role2blog.ID as user_to_blog_role_mappings_fk,
               core_blog_member.KENMEI_ENTITY_FK as granting_groups_fk
        FROM   user_of_group,core_blog_member,core_role2blog
        WHERE  user_of_group.GROUP_FK = core_blog_member.KENMEI_ENTITY_FK
        AND    user_of_group.USER_FK = core_role2blog.user_id 
        AND    core_role2blog.granted_by_group = 1
        AND    core_blog_member.BLOG_FK = core_role2blog.blog_id
        AND    core_role2blog.numeric_role = 2
        AND    core_blog_member.role = 'MEMBER'
        UNION
        -- Manager
        SELECT core_role2blog.ID as user_to_blog_role_mappings_fk,
               core_blog_member.KENMEI_ENTITY_FK as granting_groups_fk
        FROM   user_of_group, core_blog_member,core_role2blog
        WHERE  user_of_group.GROUP_FK = core_blog_member.KENMEI_ENTITY_FK
        AND    user_of_group.USER_FK = core_role2blog.user_id 
        AND    core_role2blog.granted_by_group = 1
        AND    core_blog_member.BLOG_FK = core_role2blog.blog_id
        AND    core_role2blog.numeric_role = 3
        AND    core_blog_member.role = 'MANAGER'
    );