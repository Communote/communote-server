package com.communote.server.persistence.user.security;

import java.util.List;

import com.communote.server.core.common.exceptions.IllegalDatabaseState;
import com.communote.server.model.security.SecurityCodeConstants;
import com.communote.server.model.user.security.InviteUserToBlogSecurityCode;
import com.communote.server.model.user.security.InviteUserToBlogSecurityCodeConstants;

/**
 * @see com.communote.server.model.user.security.InviteUserToBlogSecurityCode
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class InviteUserToBlogSecurityCodeDaoImpl extends InviteUserToBlogSecurityCodeDaoBase {

    private final static String FIND_BY_USER_ID_QUERY = "select u from "
            + InviteUserToBlogSecurityCodeConstants.CLASS_NAME
            + " u where u." + SecurityCodeConstants.USER + ".id = ?";

    /**
     * Handle find by user.
     *
     * @param userId
     *            the user id
     * @return the invite user to blog security code
     * @see com.communote.server.persistence.user.security.InviteUserToBlogSecurityCodeDao#findByUser(Long)
     */
    @Override
    protected com.communote.server.model.user.security.InviteUserToBlogSecurityCode handleFindByUser(
            Long userId) {
        List<?> results = getHibernateTemplate().find(
                FIND_BY_USER_ID_QUERY, new Object[] { userId });
        if (results.size() > 1) {
            throw new IllegalDatabaseState("Cannot have more than one securitycode");
        }
        if (results.size() == 0) {
            return null;
        }
        return (InviteUserToBlogSecurityCode) results.iterator().next();
    }

}
