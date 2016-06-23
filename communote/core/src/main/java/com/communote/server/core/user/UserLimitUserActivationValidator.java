package com.communote.server.core.user;

import com.communote.server.core.common.LimitHelper;
import com.communote.server.core.user.validation.UserActivationValidationException;
import com.communote.server.core.user.validation.UserActivationValidator;
import com.communote.server.model.user.User;
import com.communote.server.persistence.user.UserDao;

/**
 * Tests whether a configurable upper limit for active users is reached. If the limit is reached the
 * reason message of the exception will have the suffix <code>userLimitReached</code>
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class UserLimitUserActivationValidator implements UserActivationValidator {

    private final UserDao userDao;

    public UserLimitUserActivationValidator(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public int getOrder() {
        // should be called pretty early
        return UserActivationValidator.DEFAULT_ORDER + 10;
    }

    @Override
    public void validateUserActivation(User user) throws UserActivationValidationException {
        if (LimitHelper.isCountLimitReached(userDao.getActiveUserCount(),
                UserManagementHelper.getCountLimit())) {
            throw new UserActivationValidationException(
                    "The limit for active users is reached or was exceeded", "userLimitReached",
                    user.getAlias());
        }
    }

}
