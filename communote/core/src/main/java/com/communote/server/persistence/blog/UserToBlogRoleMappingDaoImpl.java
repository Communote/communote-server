package com.communote.server.persistence.blog;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.event.EventDispatcher;
import com.communote.server.core.blog.AssignedBlogRoleChangedEvent;
import com.communote.server.core.blog.helper.BlogRoleHelper;
import com.communote.server.model.blog.BlogRole;
import com.communote.server.model.blog.UserToBlogRoleMapping;
import com.communote.server.model.blog.UserToBlogRoleMappingConstants;
import com.communote.server.model.user.CommunoteEntityConstants;
import com.communote.server.model.user.group.Group;
import com.communote.server.persistence.user.group.GroupDao;

/**
 * @see com.communote.server.model.blog.UserToBlogRoleMapping
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class UserToBlogRoleMappingDaoImpl extends UserToBlogRoleMappingDaoBase {

    private String roleOfUserQuery;

    /**
     * New instance
     */
    private UserToBlogRoleMappingDaoImpl() {
        initQueries();
    }

    /**
     * Builds the query for retrieving mappings.
     *
     * @param blogId
     *            blog ID to match
     * @param userId
     *            user ID to match
     * @param groupId
     *            group ID to match
     * @param grantedByGroup
     *            whether the mappings should be granted by a group. If null this doesn't matter,
     *            set to true or false to explicitly include this filter.
     * @param role
     *            role to match
     * @param paramValues
     *            list to store the values for the used parameter names
     * @return string builder holding the query
     */
    private StringBuilder buildMappingsQuery(Long blogId, Long userId, Long groupId,
            Boolean grantedByGroup, BlogRole role, List<Object> paramValues) {
        StringBuilder query = new StringBuilder("select mapping from ");
        query.append(UserToBlogRoleMappingConstants.CLASS_NAME + " mapping ");
        if (grantedByGroup != null && grantedByGroup && groupId != null) {
            query.append("inner join mapping.");
            query.append(UserToBlogRoleMappingConstants.GRANTINGGROUPS);
            query.append(" kGroup ");
        }
        List<String> whereClauses = new ArrayList<String>();
        if (grantedByGroup != null) {
            whereClauses.add("mapping." + UserToBlogRoleMappingConstants.GRANTEDBYGROUP
                    + "=" + grantedByGroup);
        }
        if (blogId != null) {
            whereClauses.add("mapping." + UserToBlogRoleMappingConstants.BLOGID + "=?");
            paramValues.add(blogId);
        }
        if (userId != null) {
            whereClauses.add("mapping." + UserToBlogRoleMappingConstants.USERID + "=?");
            paramValues.add(userId);
        }
        if (role != null) {
            whereClauses.add("mapping." + UserToBlogRoleMappingConstants.NUMERICROLE + "=?");
            paramValues.add(BlogRoleHelper.convertRoleToNumeric(role));
        }

        if (grantedByGroup != null && grantedByGroup && groupId != null) {
            whereClauses.add("kGroup." + CommunoteEntityConstants.ID + "=?");
            paramValues.add(groupId);
        }
        if (!whereClauses.isEmpty()) {
            query.append("where " + StringUtils.join(whereClauses, " AND "));
        }
        return query;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object create(int transform, UserToBlogRoleMapping userToBlogRoleMapping) {
        Object result = super.create(transform, userToBlogRoleMapping);
        ServiceLocator.findService(EventDispatcher.class).fire(
                new AssignedBlogRoleChangedEvent(userToBlogRoleMapping.getBlogId(),
                        userToBlogRoleMapping.getUserId()));
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<UserToBlogRoleMapping> findMappings(Long blogId, Long userId,
            Long groupId, Boolean grantedByGroup, BlogRole role) {
        // no * fetching
        if (blogId == null && userId == null && groupId == null && grantedByGroup != null
                && grantedByGroup && role == null) {
            return null;
        }
        List<Object> paramValues = new ArrayList<Object>();
        StringBuilder query = buildMappingsQuery(blogId, userId, groupId, grantedByGroup, role,
                paramValues);
        return getHibernateTemplate().find(query.toString(), paramValues.toArray());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Collection<UserToBlogRoleMapping> handleFindMappingsForExternal(Long blogId,
            Long userId, Long groupId, boolean grantedByGroup, BlogRole role, String externalSystem) {
        // no * fetching
        if (blogId == null && userId == null && groupId == null && role == null) {
            return null;
        }
        List<Object> paramValues = new ArrayList<Object>();
        StringBuilder query = buildMappingsQuery(blogId, userId, groupId, grantedByGroup, role,
                paramValues);

        query.append(" AND mapping.");
        if (externalSystem == null) {
            query.append(UserToBlogRoleMappingConstants.EXTERNALSYSTEMID + " is null");
        } else {
            query.append(UserToBlogRoleMappingConstants.EXTERNALSYSTEMID + "=?");
            paramValues.add(externalSystem);
        }
        return getHibernateTemplate().find(query.toString(), paramValues.toArray());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected BlogRole handleGetRoleOfUser(Long blogId, Long userId) {
        List<?> results = getHibernateTemplate().find(roleOfUserQuery,
                new Object[] { blogId, userId });
        if (results.get(0) == null) {
            return null;
        }
        Integer numericRole = (Integer) results.get(0);
        return BlogRoleHelper.convertNumericToRole(numericRole.intValue());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void handleRemoveAllForBlog(Long blogId) {
        Collection<UserToBlogRoleMapping> mappings = findMappings(blogId, null, null, false,
                null);
        remove(mappings);
        mappings = findMappings(blogId, null, null, true, null);
        remove(mappings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void handleRemoveAllForGroup(Long groupId) {
        Collection<UserToBlogRoleMapping> mappings = findMappings(null, null, groupId, true,
                null);
        internalRemoveGroupFromGrantingGroups(mappings, groupId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void handleRemoveAllForGroupMember(Long userId, Long groupId) {
        Collection<UserToBlogRoleMapping> mappings = findMappings(null, userId, groupId,
                true, null);
        internalRemoveGroupFromGrantingGroups(mappings, groupId);
    }

    /**
     * Inits some frequently used queries.
     */
    private void initQueries() {
        roleOfUserQuery = "select max(mapping." + UserToBlogRoleMappingConstants.NUMERICROLE
                + ") from " + UserToBlogRoleMappingConstants.CLASS_NAME + " mapping where mapping."
                + UserToBlogRoleMappingConstants.BLOGID + "=? AND mapping."
                + UserToBlogRoleMappingConstants.USERID + "=?";
    }

    /**
     * Removes a group from the blog rights granting groups.
     *
     * @param mappings
     *            the mappings to process
     * @param groupId
     *            the group to remove
     */
    private void internalRemoveGroupFromGrantingGroups(Collection<UserToBlogRoleMapping> mappings,
            Long groupId) {
        Group group = ServiceLocator.findService(GroupDao.class).load(
                groupId);
        for (UserToBlogRoleMapping mapping : mappings) {
            if (mapping.getGrantingGroups().remove(group)) {
                if (mapping.getGrantingGroups().size() == 0) {
                    remove(mapping);
                }
                ServiceLocator.findService(EventDispatcher.class).fire(
                        new AssignedBlogRoleChangedEvent(mapping.getBlogId(), mapping.getUserId()));
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void remove(Collection<UserToBlogRoleMapping> entities) {
        super.remove(entities);
        for (UserToBlogRoleMapping mapping : entities) {
            ServiceLocator.findService(EventDispatcher.class).fire(
                    new AssignedBlogRoleChangedEvent(mapping.getBlogId(), mapping.getUserId()));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void remove(UserToBlogRoleMapping userToBlogRoleMapping) {
        // TODO Auto-generated method stub
        super.remove(userToBlogRoleMapping);
        ServiceLocator.findService(EventDispatcher.class).fire(
                new AssignedBlogRoleChangedEvent(userToBlogRoleMapping.getBlogId(),
                        userToBlogRoleMapping.getUserId()));
    }

}