package com.communote.server.persistence.blog;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.communote.server.model.blog.BlogConstants;
import com.communote.server.model.blog.BlogMember;
import com.communote.server.model.blog.BlogMemberConstants;
import com.communote.server.model.blog.BlogRole;
import com.communote.server.model.blog.ExternalBlogMemberConstants;
import com.communote.server.model.user.CommunoteEntityConstants;
import com.communote.server.persistence.blog.BlogMemberDaoBase;


/**
 * @see com.communote.server.model.blog.BlogMember
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class BlogMemberDaoImpl extends BlogMemberDaoBase {

    private static final String QUERY_STRING_FIND_BLOG_MEMBERS_FOR_ENTITY = "from "
            + BlogMemberConstants.CLASS_NAME + " where " + BlogMemberConstants.MEMBERENTITY + "."
            + CommunoteEntityConstants.ID + "=?";

    private static final String QUERY_STRING_FIND_BLOG_MEMBERS_FOR_BLOG_AND_ENTITY = "select mem from "
            + BlogConstants.CLASS_NAME
            + " blog left join blog."
            + BlogConstants.MEMBERS
            + " mem left join mem."
            + BlogMemberConstants.MEMBERENTITY
            + " entity where blog.id=? and entity.id=? order by mem."
            + ExternalBlogMemberConstants.EXTERNALSYSTEMID;

    private static final String QUERY_STRING_GET_BLOG_ROLES = "select mem from "
            + ExternalBlogMemberConstants.CLASS_NAME + " mem left join mem."
            + BlogMemberConstants.BLOG + " blog where blog.id=? and mem."
            + ExternalBlogMemberConstants.EXTERNALSYSTEMID + "=?";

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<BlogMember> handleFindByBlogAndEntity(Long blogId, Long entityId) {
        return getHibernateTemplate().find(QUERY_STRING_FIND_BLOG_MEMBERS_FOR_BLOG_AND_ENTITY,
                new Object[] { blogId, entityId });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Collection<BlogMember> handleFindByEntity(Long entityId) {
        return getHibernateTemplate().find(QUERY_STRING_FIND_BLOG_MEMBERS_FOR_ENTITY, entityId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Map<Long, BlogRole> handleGetBlogRoles(Long blogId, String externalSystemId) {
        List<BlogMember> blogMembers = getHibernateTemplate().find(QUERY_STRING_GET_BLOG_ROLES,
                new Object[] { blogId, externalSystemId });
        Map<Long, BlogRole> entityRoles = new HashMap<Long, BlogRole>();
        for (BlogMember blogMember : blogMembers) {
            entityRoles.put(blogMember.getMemberEntity().getId(), blogMember.getRole());
        }
        return entityRoles;
    }
}
