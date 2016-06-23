package com.communote.server.persistence.user;

import java.util.List;

import com.communote.server.model.user.ExternalUserAuthentication;
import com.communote.server.model.user.ExternalUserAuthenticationConstants;
import com.communote.server.model.user.UserConstants;
import com.communote.server.persistence.user.ExternalUserAuthenticationDaoBase;

/**
 * @see com.communote.server.model.user.ExternalUserAuthentication
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ExternalUserAuthenticationDaoImpl extends ExternalUserAuthenticationDaoBase {
    private static final String PERMA_ID_QUERY = "Select u.id from "
            + UserConstants.CLASS_NAME + " as u inner join u."
            + UserConstants.EXTERNALAUTHENTICATIONS + " auth where auth."
            + ExternalUserAuthenticationConstants.PERMANENTID + "=? and auth."
            + ExternalUserAuthenticationConstants.SYSTEMID + "=?";

    private static final String SYS_ID_QUERY = "from "
            + ExternalUserAuthenticationConstants.CLASS_NAME + " where "
            + ExternalUserAuthenticationConstants.SYSTEMID + "=?";

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    protected List<ExternalUserAuthentication> handleFindBySystemId(String externalSystemId) {
        return getHibernateTemplate().find(SYS_ID_QUERY, new String[] { externalSystemId });
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    protected Long handleFindUserByPermanentId(String externalSystemId, String permanentId) {
        List result = getHibernateTemplate().find(PERMA_ID_QUERY,
                new String[] { permanentId, externalSystemId });
        if (result == null || result.size() == 0) {
            return null;
        }
        return (Long) result.get(0);
    }
}