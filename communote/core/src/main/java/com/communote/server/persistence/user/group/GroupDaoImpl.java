package com.communote.server.persistence.user.group;

import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.dao.support.DataAccessUtils;

import com.communote.server.core.common.exceptions.IllegalDatabaseState;
import com.communote.server.model.user.CommunoteEntity;
import com.communote.server.model.user.group.Group;
import com.communote.server.model.user.group.GroupConstants;

/**
 * @see com.communote.server.model.user.group.Group
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class GroupDaoImpl extends GroupDaoBase {

    private final static String MEMBERSHIP_QUERY = "from " + GroupConstants.CLASS_NAME
            + " kGroup inner join kGroup." + GroupConstants.GROUPMEMBERS
            + " kEntity where kGroup.id=?";
    private final static String IS_MEMBER_QUERY = MEMBERSHIP_QUERY + " AND kEntity.id=?";
    private final static String GROUPS_OF_USER_QUERY = "select kGroup from "
            + GroupConstants.CLASS_NAME + " kGroup inner join kGroup."
            + GroupConstants.GROUPMEMBERS + " kEntity where kEntity.id=?";

    /**
     * {@inheritDoc}
     */
    @Override
    public Object create(final int transform, final Group group) {
        if (group == null) {
            throw new IllegalArgumentException("Group.create - 'group' can not be null");
        }
        this.getHibernateTemplate().save(group);
        group.setGlobalId(getGlobalIdDao().createGlobalId(group));
        return this.transformEntity(transform, group);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected int handleCount(String filter) {
        if (StringUtils.isNotBlank(filter)) {
            Criteria criteria = getSession().createCriteria(Group.class);
            criteria.add(Restrictions.ilike(GroupConstants.NAME, "%" + filter + "%"));
            criteria.add(Restrictions.ilike(GroupConstants.ALIAS, "%" + filter + "%"));
            criteria.setProjection(Projections.rowCount());
            Object result = criteria.uniqueResult();
            return Integer.parseInt(result.toString());
        }
        return DataAccessUtils.intResult(getHibernateTemplate().find(
                "select count(*) from " + GroupConstants.CLASS_NAME));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected int handleCountMembers(long groupId) {
        Group group = load(groupId);
        if (group != null) {
            return DataAccessUtils.intResult(getHibernateTemplate().find(
                    "select count(*) " + MEMBERSHIP_QUERY, groupId));
        }
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Group handleFindByAlias(String alias) {
        List<?> results = getHibernateTemplate().find(
                "from " + GroupConstants.CLASS_NAME + " where lower(" + GroupConstants.ALIAS
                + ") = ?", alias.toLowerCase());
        if (results.size() > 1) {
            throw new IllegalDatabaseState("Cannot have more than one group with the same alias, "
                    + "Alias is: '" + alias + "'");
        }
        Group group = null;
        if (results.size() > 0) {
            group = (Group) results.iterator().next();
        }

        return group;
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    protected Collection<Group> handleGetGroupsOfUser(Long userId) {
        List results = getHibernateTemplate().find(GROUPS_OF_USER_QUERY, new Object[] { userId });
        return results;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean handleIsEntityMember(Long groupId, Long entityId) {
        List<?> results = getHibernateTemplate().find(IS_MEMBER_QUERY,
                new Object[] { groupId, entityId });
        return results.size() != 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Collection<Group> handleLoadAllWithReferences() {
        List<Group> groups = (List<Group>) super.loadAll();
        for (Group group : groups) {
            for (CommunoteEntity entity : group.getGroupMembers()) {
                entity.getId();
            }
        }
        return groups;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Collection<Group> handleLoadWithReferences(int offset, int count, String filter) {
        Criteria criteria = getSession().createCriteria(Group.class);
        criteria.setFirstResult(offset);
        criteria.setMaxResults(count);
        criteria.addOrder(Order.asc(GroupConstants.NAME).ignoreCase());
        if (StringUtils.isNotBlank(filter)) {
            criteria.add(Restrictions.or(
                    Restrictions.ilike(GroupConstants.NAME, "%" + filter + "%"),
                    Restrictions.ilike(GroupConstants.ALIAS, "%" + filter + "%")));
        }
        Collection<Group> groups = criteria.list();
        // Criteria.setFetchMode doesn't work, as it doesn't groups the results correctly.
        for (Group group : groups) {
            for (CommunoteEntity entity : group.getGroupMembers()) {
                entity.getId();
            }
        }
        return groups;
    }

}
