package com.communote.server.core.vo.query.user;

import com.communote.server.model.user.User;

/**
 * Query for users in form of User.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class UserQuery extends AbstractUserQuery<User, UserQueryParameters> {

    /**
     * {@inheritDoc}
     */
    @Override
    public UserQueryParameters createInstance() {
        return new UserQueryParameters();
    }
}
