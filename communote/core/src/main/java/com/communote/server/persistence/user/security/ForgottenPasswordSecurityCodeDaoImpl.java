package com.communote.server.persistence.user.security;

import java.sql.Timestamp;
import java.util.Date;

import com.communote.server.model.security.SecurityCodeAction;
import com.communote.server.model.user.User;
import com.communote.server.model.user.security.ForgottenPasswordSecurityCode;


/**
 * @see com.communote.server.model.user.security.ForgottenPasswordSecurityCode
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ForgottenPasswordSecurityCodeDaoImpl extends
        com.communote.server.persistence.user.security.ForgottenPasswordSecurityCodeDaoBase {
    /**
     * {@inheritDoc}
     */
    @Override
    protected ForgottenPasswordSecurityCode handleCreateCode(User user) {
        ForgottenPasswordSecurityCode code = ForgottenPasswordSecurityCode.Factory.newInstance();
        code.setAction(SecurityCodeAction.FORGOTTEN_PASSWORD);
        code.generateNewCode();
        code.setCreatingDate(new Timestamp(new Date().getTime()));
        code.setUser(user);
        create(code);
        return code;
    }
}
