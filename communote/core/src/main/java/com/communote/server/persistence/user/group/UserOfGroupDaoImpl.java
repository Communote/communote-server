package com.communote.server.persistence.user.group;

import java.util.Collection;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import com.communote.server.model.user.CommunoteEntityConstants;
import com.communote.server.model.user.group.UserOfGroup;
import com.communote.server.model.user.group.UserOfGroupConstants;
import com.communote.server.model.user.group.UserOfGroupModificationType;
import com.communote.server.persistence.user.group.UserOfGroupDaoBase;


/**
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class UserOfGroupDaoImpl extends UserOfGroupDaoBase {
    private static final String USERS_OF_GROUP_QUERY = "select uog." + UserOfGroupConstants.USER
            + "." + CommunoteEntityConstants.ID + " from " + UserOfGroupConstants.CLASS_NAME
            + " uog where " + UserOfGroupConstants.GROUP + "." + CommunoteEntityConstants.ID + "=?";

    /**
     * {@inheritDoc}
     */
    @Override
    protected UserOfGroup handleFindByUserIdGroupId(Long userId, Long groupId) {
        Criteria criteria = getSession().createCriteria(UserOfGroup.class);
        String groupIdProperty = UserOfGroupConstants.GROUP + "." + CommunoteEntityConstants.ID;
        String userIdProperty = UserOfGroupConstants.USER + "." + CommunoteEntityConstants.ID;
        criteria.add(Restrictions.and(Restrictions.eq(groupIdProperty, groupId), Restrictions.eq(
                userIdProperty, userId)));
        List results = criteria.list();
        if (results.size() == 0) {
            return null;
        } else {
            return (UserOfGroup) results.get(0);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Collection<UserOfGroup> handleGetUserOfGroupEntities(
            UserOfGroupModificationType modificationType) {
        Criteria criteria = getSession().createCriteria(UserOfGroup.class);
        Criterion searchCriterion;
        if (modificationType == null) {
            searchCriterion = Restrictions.isNull(UserOfGroupConstants.MODIFICATIONTYPE);
        } else {
            searchCriterion = Restrictions.eq(UserOfGroupConstants.MODIFICATIONTYPE,
                    modificationType);
        }
        criteria.add(searchCriterion).addOrder(Order.asc(UserOfGroupConstants.GROUP));
        return criteria.list();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Collection<Long> handleGetUsersOfGroup(Long groupId) {
        return getHibernateTemplate().find(USERS_OF_GROUP_QUERY, groupId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean handleIsUserOfGroup(Long userId, Long groupId) {
        return handleFindByUserIdGroupId(userId, groupId) != null;
    }

}