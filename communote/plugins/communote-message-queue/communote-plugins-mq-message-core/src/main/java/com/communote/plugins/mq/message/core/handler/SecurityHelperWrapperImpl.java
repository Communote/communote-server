package com.communote.plugins.mq.message.core.handler;

import com.communote.server.core.security.SecurityHelper;

/**
 * Senseless impl of the wrapper.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
// TODO Remove this implementation and find a good way to use the SecurityHelper for testing.
class SecurityHelperWrapperImpl implements SecurityHelperWrapper {
    /**
     * @return SecurityHelper.assertCurrentUserId()
     */
    @Override
    public Long assertCurrentUserId() {
        return SecurityHelper.assertCurrentUserId();
    }

}
