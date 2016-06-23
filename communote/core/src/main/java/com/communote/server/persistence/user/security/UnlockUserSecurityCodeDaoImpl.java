package com.communote.server.persistence.user.security;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import com.communote.server.model.security.ChannelType;
import com.communote.server.model.security.SecurityCodeAction;
import com.communote.server.model.security.SecurityCodeConstants;
import com.communote.server.model.user.User;
import com.communote.server.model.user.security.UnlockUserSecurityCode;
import com.communote.server.model.user.security.UnlockUserSecurityCodeConstants;

/**
 * @see com.communote.server.model.user.security.UnlockUserSecurityCode
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class UnlockUserSecurityCodeDaoImpl extends
        com.communote.server.persistence.user.security.UnlockUserSecurityCodeDaoBase {
    /**
     * {@inheritDoc}
     */
    @Override
    protected UnlockUserSecurityCode handleCreateCode(User user, ChannelType channel) {
        UnlockUserSecurityCode code = UnlockUserSecurityCode.Factory.newInstance();
        code.setAction(SecurityCodeAction.UNLOCK_USER);
        code.generateNewCode();
        code.setCreatingDate(new Timestamp(new Date().getTime()));
        code.setUser(user);
        code.setChannel(channel);
        create(code);
        return code;
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    protected UnlockUserSecurityCode handleFindBySecurityCode(String code) {
        StringBuffer query = new StringBuffer();
        query.append("select u from ");
        query.append(UnlockUserSecurityCodeConstants.CLASS_NAME);
        query.append(" u where u.");
        query.append(SecurityCodeConstants.CODE);
        query.append("=?");
        List<UnlockUserSecurityCode> results = getHibernateTemplate().find(query.toString(), code);
        if (results.size() > 0) {
            return results.get(0);
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected UnlockUserSecurityCode handleFindByUserAndChannel(Long userId, ChannelType channel) {
        StringBuffer query = new StringBuffer();
        query.append("select u from ");
        query.append(UnlockUserSecurityCodeConstants.CLASS_NAME);
        query.append(" u where u.");
        query.append(SecurityCodeConstants.USER);
        query.append(".id = ?");
        query.append(" and u.");
        query.append(SecurityCodeConstants.ACTION);
        query.append("=?");
        query.append(" and u.");
        query.append(UnlockUserSecurityCodeConstants.CHANNEL);
        query.append("=?");
        List<UnlockUserSecurityCode> results = getHibernateTemplate().find(query.toString(),
                new Object[] { userId, SecurityCodeAction.UNLOCK_USER, channel });
        if (results.size() > 0) {
            return results.get(0);
        }
        return null;
    }

}
