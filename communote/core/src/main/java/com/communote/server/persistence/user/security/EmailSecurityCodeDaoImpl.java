package com.communote.server.persistence.user.security;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import com.communote.server.core.common.exceptions.IllegalDatabaseState;
import com.communote.server.model.security.SecurityCodeAction;
import com.communote.server.model.security.SecurityCodeConstants;
import com.communote.server.model.user.User;
import com.communote.server.model.user.security.EmailSecurityCode;
import com.communote.server.model.user.security.EmailSecurityCodeConstants;
import com.communote.server.persistence.user.security.EmailSecurityCodeDaoBase;


/**
 * @see com.communote.server.model.user.security.EmailSecurityCode
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class EmailSecurityCodeDaoImpl extends EmailSecurityCodeDaoBase {

    /**
     * {@inheritDoc}
     */
    @Override
    protected EmailSecurityCode handleCreateCode(User user, String newEmail) {
        EmailSecurityCode code = EmailSecurityCode.Factory.newInstance();
        code.setAction(SecurityCodeAction.CONFIRM_EMAIL);
        code.setNewEmailAddress(newEmail);
        code.setUser(user);
        code.setCreatingDate(new Timestamp(new Date().getTime()));
        code.generateNewCode();
        create(code);
        return code;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected EmailSecurityCode handleFindByEmailAddress(String email) {
        StringBuffer query = new StringBuffer();
        query.append("select u from ");
        query.append(EmailSecurityCodeConstants.CLASS_NAME);
        query.append(" u where u.");
        query.append(EmailSecurityCodeConstants.NEWEMAILADDRESS);
        query.append("=?");
        List<EmailSecurityCode> results = getHibernateTemplate().find(query.toString(), email);
        if (results.size() == 0) {
            return null;
        }
        if (results.size() > 1) {
            throw new IllegalDatabaseState(
                    "Cannot have more than one EmailSecurityCode with the same eMail address. eMail-Address: "
                            + email);
        }
        return results.iterator().next();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected EmailSecurityCode handleFindBySecurityCode(String code) {
        StringBuffer query = new StringBuffer();
        query.append("select u from ");
        query.append(EmailSecurityCodeConstants.CLASS_NAME);
        query.append(" u where u.");
        query.append(SecurityCodeConstants.CODE);
        query.append("=?");
        List<EmailSecurityCode> results = getHibernateTemplate().find(query.toString(), code);
        if (results.size() > 1) {
            throw new IllegalDatabaseState("Cannot have more than one securitycode");
        }
        if (results.size() == 0) {
            return null;
        }
        return results.iterator().next();
    }

}
