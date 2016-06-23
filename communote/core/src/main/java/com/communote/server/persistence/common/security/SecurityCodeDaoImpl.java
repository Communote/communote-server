package com.communote.server.persistence.common.security;

import java.util.List;

import org.hibernate.Query;

import com.communote.server.core.common.exceptions.IllegalDatabaseState;
import com.communote.server.model.security.SecurityCode;
import com.communote.server.model.security.SecurityCodeConstants;

/**
 * @see com.communote.server.model.security.SecurityCode
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class SecurityCodeDaoImpl extends
com.communote.server.persistence.common.security.SecurityCodeDaoBase {

    /**
     * {@inheritDoc}
     */
    @Override
    protected void handleDeleteAllCodesByUser(Long userId, Class<? extends SecurityCode> clazz) {
        // StringBuilder queryStr = new StringBuilder();
        // queryStr.append("delete from ");
        // queryStr.append(clazz.getName());
        // queryStr.append(" where ");
        // queryStr.append(SecurityCodeConstants.TAGGINGUSER);
        // queryStr.append(".id=");
        // queryStr.append(userId.toString());
        // Query query = getSession().createQuery(queryStr.toString());
        // query.executeUpdate();

        StringBuilder queryStr = new StringBuilder();
        queryStr.append("select clazz from ");
        queryStr.append(clazz.getName());
        queryStr.append(" as clazz where ");
        queryStr.append(SecurityCodeConstants.USER);
        queryStr.append(".id=");
        queryStr.append(userId.toString());
        Query query = getSession().createQuery(queryStr.toString());
        List<SecurityCode> codes = query.list();
        for (SecurityCode securityCode : codes) {
            remove(securityCode);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected SecurityCode handleFindByCode(String securityCode) {
        StringBuilder query = new StringBuilder();
        query.append("select u from ");
        query.append(SecurityCodeConstants.CLASS_NAME);
        query.append(" u where u.");
        query.append(SecurityCodeConstants.CODE);
        query.append("=?");

        List<SecurityCode> codes = getHibernateTemplate().find(query.toString(), securityCode);
        if (codes.size() > 1) {
            throw new IllegalDatabaseState(
                    "Cannot have more than one SecurityCode with the same Guid!, " + "Guid is: '"
                            + securityCode + "'");
        }
        if (codes.size() == 0) {
            return null;
        }
        return codes.iterator().next();
    }

}
