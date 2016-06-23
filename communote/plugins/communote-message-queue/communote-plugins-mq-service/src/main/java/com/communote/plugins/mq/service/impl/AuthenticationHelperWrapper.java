package com.communote.plugins.mq.service.impl;

import com.communote.server.model.user.User;

// TODO: what is this class for? is it really a wrapper? Isn't it a simple Helper?
// this class allows to isolate static invocation to the authentication helper,
// it could be tested. It cannot be mocked with EasyMock, since it is final
/**
 * The Interface AuthenticationHelperWrapper.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
interface AuthenticationHelperWrapper {

    /**
     * Sets the security context.
     *
     * @param user
     *            the new security context
     */
    void setSecurityContext(User user);

}
