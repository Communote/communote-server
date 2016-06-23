package com.communote.server.persistence.user.security;

import java.sql.Timestamp;
import java.util.Iterator;

import com.communote.server.model.security.ChannelType;
import com.communote.server.model.user.User;
import com.communote.server.model.user.security.AuthenticationFailedStatus;
import com.communote.server.model.user.security.AuthenticationFailedStatusConstants;
import com.communote.server.persistence.user.security.AuthenticationFailedStatusDaoBase;


/**
 * Implementation of {@link AuthenticationFailedStatusDaoBase}
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class AuthenticationFailedStatusDaoImpl extends AuthenticationFailedStatusDaoBase {

    /**
     * {@inheritDoc}
     */
    @Override
    protected AuthenticationFailedStatus handleFindByUserAndChannel(User user,
            ChannelType channel) {
        Iterator<AuthenticationFailedStatus> authFailedStatesIt = user.getFailedAuthentication()
                .iterator();
        AuthenticationFailedStatus authFailedStatus = null;
        while (authFailedStatesIt.hasNext()) {
            authFailedStatus = authFailedStatesIt.next();
            if (authFailedStatus.getChannelType().equals(channel)) {
                break;
            } else {
                authFailedStatus = null;
            }
        }
        return authFailedStatus;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void handleIncFailedAuthCount(long failedAuthStatusId) {
        getHibernateTemplate().bulkUpdate(
                "update " + AuthenticationFailedStatusConstants.CLASS_NAME + " a set a."
                        + AuthenticationFailedStatusConstants.FAILEDAUTHCOUNTER + "= ("
                        + AuthenticationFailedStatusConstants.FAILEDAUTHCOUNTER
                        + "+1) where a.id = ?", new Object[] { failedAuthStatusId });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void handleUpdateLockedTimeout(long failedAuthStatusId, Timestamp lockedTimeout) {
        getHibernateTemplate().bulkUpdate(
                "update " + AuthenticationFailedStatusConstants.CLASS_NAME + " a set a."
                        + AuthenticationFailedStatusConstants.LOCKEDTIMEOUT + "= ? where a.id = ?",
                new Object[] { lockedTimeout, failedAuthStatusId });
    }
}
