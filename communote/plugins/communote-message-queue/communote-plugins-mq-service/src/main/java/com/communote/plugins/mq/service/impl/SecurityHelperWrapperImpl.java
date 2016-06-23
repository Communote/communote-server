package com.communote.plugins.mq.service.impl;

import com.communote.server.core.security.SecurityHelper;
import com.communote.server.core.security.UserDetails;

/**
 * The Class SecurityHelperWrapperImpl.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
class SecurityHelperWrapperImpl implements SecurityHelperWrapper {

    /*
     * (non-Javadoc)
     *
     * @see com.communote.plugins.mq.service.impl.SecurityHelperWrapper#getCurrentUser()
     */
    @Override
    public UserDetails getCurrentUser() {
        return SecurityHelper.getCurrentUser();
    }

}
