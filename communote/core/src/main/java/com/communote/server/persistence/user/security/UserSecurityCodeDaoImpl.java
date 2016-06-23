package com.communote.server.persistence.user.security;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import com.communote.server.core.common.exceptions.IllegalDatabaseState;
import com.communote.server.model.security.SecurityCodeAction;
import com.communote.server.model.security.SecurityCodeConstants;
import com.communote.server.model.user.User;
import com.communote.server.model.user.security.UserSecurityCode;
import com.communote.server.model.user.security.UserSecurityCodeConstants;
import com.communote.server.persistence.user.security.UserSecurityCodeDaoBase;


/**
 * @see com.communote.server.model.user.security.UserSecurityCode
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class UserSecurityCodeDaoImpl extends UserSecurityCodeDaoBase {
    /**
     * {@inheritDoc}
     */
    @Override
    protected UserSecurityCode handleCreateCode(User user) {
        UserSecurityCode code = UserSecurityCode.Factory.newInstance();
        code.setAction(SecurityCodeAction.CONFIRM_USER);
        code.generateNewCode();
        code.setCreatingDate(new Timestamp(new Date().getTime()));
        code.setUser(user);
        create(code);
        return code;
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    protected UserSecurityCode handleFindBySecurityCode(String code) {
        StringBuffer query = new StringBuffer();
        query.append("select u from ");
        query.append(UserSecurityCodeConstants.CLASS_NAME);
        query.append(" u where u.");
        query.append(SecurityCodeConstants.CODE);
        query.append("=?");
        List<UserSecurityCode> results = getHibernateTemplate().find(query.toString(), code);
        if (results.size() > 1) {
            throw new IllegalDatabaseState("Cannot have more than one securitycode");
        }
        if (results.size() == 0) {
            return null;
        }
        return results.iterator().next();
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    protected UserSecurityCode handleFindByUser(Long userId, SecurityCodeAction action) {
        StringBuffer query = new StringBuffer();
        query.append("select u from ");
        query.append(UserSecurityCodeConstants.CLASS_NAME);
        query.append(" u where u.");
        query.append(SecurityCodeConstants.USER);
        query.append(".id=?");
        query.append(" and u.");
        query.append(SecurityCodeConstants.ACTION);
        query.append("=?");
        List<UserSecurityCode> results = getHibernateTemplate().find(query.toString(),
                new Object[] { userId, action });
        if (results.size() > 1) {
            throw new IllegalDatabaseState("Cannot have more than one securitycode");
        }
        if (results.size() == 0) {
            return null;
        }
        return results.iterator().next();
    }

}
