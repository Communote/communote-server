package com.communote.server.persistence.user.security;

import java.util.List;

import com.communote.server.core.common.exceptions.IllegalDatabaseState;
import com.communote.server.model.security.SecurityCodeConstants;
import com.communote.server.model.user.security.InviteUserToClientSecurityCode;
import com.communote.server.model.user.security.InviteUserToClientSecurityCodeConstants;


/**
 * @see com.communote.server.model.user.security.InviteUserToClientSecurityCode
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class InviteUserToClientSecurityCodeDaoImpl extends
        com.communote.server.persistence.user.security.InviteUserToClientSecurityCodeDaoBase {

    /**
     * Handle find by user.
     * 
     * @param userId
     *            the user id
     * @return the invite user to client security code
     * @see com.communote.server.persistence.user.security.InviteUserToClientSecurityCodeDao#findByUser(Long)
     */
    @Override
    protected com.communote.server.model.user.security.InviteUserToClientSecurityCode handleFindByUser(
            Long userId) {
        StringBuffer query = new StringBuffer();
        query.append("select u from ");
        query.append(InviteUserToClientSecurityCodeConstants.CLASS_NAME);
        query.append(" u where u.");
        query.append(SecurityCodeConstants.USER);
        query.append(".id = ?");
        List<InviteUserToClientSecurityCode> results = getHibernateTemplate().find(
                query.toString(), new Object[] { userId });
        if (results.size() > 1) {
            throw new IllegalDatabaseState("Cannot have more than one securitycode");
        }
        if (results.size() == 0) {
            return null;
        }
        return results.iterator().next();
    }

}
