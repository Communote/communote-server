package com.communote.plugins.mq.service.impl;

import com.communote.server.core.security.AuthenticationHelper;
import com.communote.server.model.user.User;

/**
 * The Class AuthenticationHelperWrapperImpl.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
class AuthenticationHelperWrapperImpl implements AuthenticationHelperWrapper {

    @Override
    public void setSecurityContext(User user) {
        AuthenticationHelper.setAsAuthenticatedUser(user);
    }

}
