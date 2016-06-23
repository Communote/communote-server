package com.communote.server.core.vo.query.user;

import com.communote.server.api.ServiceLocator;
import com.communote.server.core.vo.query.QueryResultConverter;
import com.communote.server.model.user.User;
import com.communote.server.persistence.user.UserDao;

/**
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class DataAccessUserConverter<I, O> extends QueryResultConverter<I, O> {

    /**
     * Getting user object by id
     *
     * @param userId
     *            the user id
     * @return
     * @return user object
     */

    protected User getUser(Long userId) {
        return getUserDao().load(userId);
    }

    /**
     * Getting Instance of UserDao
     *
     * @return
     *
     * @return UserDao Instance
     */

    protected UserDao getUserDao() {
        return ServiceLocator.findService(UserDao.class);
    }

}
