package com.communote.plugins.mq.service.impl;

import com.communote.server.core.security.UserDetails;

// TODO: what is this class for? is is really a wrapper? Isn't it a simple helper?
/**
 * The Interface SecurityHelperWrapper.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * @deprecated dont use me
 */
@Deprecated
interface SecurityHelperWrapper {

    /**
     * Gets the current user.
     *
     * @return the current user
     */
    UserDetails getCurrentUser();

}
