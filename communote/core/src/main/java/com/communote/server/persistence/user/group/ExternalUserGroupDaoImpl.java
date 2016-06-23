package com.communote.server.persistence.user.group;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.config.type.ClientProperty;
import com.communote.server.core.common.exceptions.IllegalDatabaseState;
import com.communote.server.model.user.CommunoteEntityConstants;
import com.communote.server.model.user.group.ExternalUserGroup;
import com.communote.server.model.user.group.ExternalUserGroupConstants;
import com.communote.server.persistence.global.GlobalIdDao;

/**
 * @see com.communote.server.model.user.group.ExternalUserGroup
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ExternalUserGroupDaoImpl extends
com.communote.server.persistence.user.group.ExternalUserGroupDaoBase {

    /**
     * {@inheritDoc}.
     */
    @Override
    public Object create(int transform, ExternalUserGroup externalUserGroup) {
        getHibernateTemplate().save(externalUserGroup);
        GlobalIdDao globalIdDao = ServiceLocator.findService(GlobalIdDao.class);
        externalUserGroup.setGlobalId(globalIdDao.createGlobalId(externalUserGroup));
        return this.transformEntity(transform, externalUserGroup);
    }

    @Override
    protected ExternalUserGroup handleFindByAdditionalProperty(String additionalProperty,
            String externalSystemId) {
        List<?> results = getHibernateTemplate().find(
                "from " + ExternalUserGroupConstants.CLASS_NAME + " where "
                        + ExternalUserGroupConstants.ADDITIONALPROPERTY + " = ? and "
                        + ExternalUserGroupConstants.EXTERNALSYSTEMID + " = ?",
                        new Object[] { additionalProperty, externalSystemId });
        if (results.size() > 0) {
            return (ExternalUserGroup) results.get(0);
        }
        return null;
    }

    @Override
    protected ExternalUserGroup handleFindByExternalId(String externalId, String externalSystemId) {
        boolean compareIdLowerCase = CommunoteRuntime.getInstance().getConfigurationManager()
                .getClientConfigurationProperties()
                .getProperty(ClientProperty.COMPARE_EXTERNAL_GROUP_IDS_LOWERCASE, false);

        String key = ExternalUserGroupConstants.EXTERNALID;
        if (compareIdLowerCase) {
            key = "lower(" + ExternalUserGroupConstants.EXTERNALID + ")";
            externalId = externalId.toLowerCase();
        }

        List<?> results = getHibernateTemplate().find(
                "from " + ExternalUserGroupConstants.CLASS_NAME + " where " + key + " = ? and "
                        + ExternalUserGroupConstants.EXTERNALSYSTEMID + " = ?",
                        new Object[] { externalId, externalSystemId });
        if (results.size() > 1) {
            throw new IllegalDatabaseState("Cannot have more than one group (" + results.size()
                    + ") with the same external id: '" + externalId + "'");
        }
        ExternalUserGroup group = null;
        if (results.size() > 0) {
            group = (ExternalUserGroup) results.iterator().next();
        }
        return group;
    }

    /**
     * @see com.communote.server.persistence.user.group.ExternalUserGroupDao#findBySystemId(String)
     * @param systemId
     *            id of the foreign system.
     * @return List of {@link ExternalUserGroup}.
     */
    @Override
    protected List<ExternalUserGroup> handleFindBySystemId(String systemId) {
        return handleFindLatestBySystemId(systemId, null, 0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<ExternalUserGroup> handleFindLatestBySystemId(String externalSystemId,
            Long startId, int maxCount) {
        Criteria criteria = getSession().createCriteria(ExternalUserGroup.class);
        criteria.add(Restrictions.eq(ExternalUserGroupConstants.EXTERNALSYSTEMID, externalSystemId));
        if (startId != null) {
            criteria.add(Restrictions.gt(CommunoteEntityConstants.ID, startId));
        }
        if (maxCount > 0) {
            criteria.setMaxResults(maxCount);
        }
        criteria.addOrder(Order.asc(CommunoteEntityConstants.ID));
        return criteria.list();
    }
}
