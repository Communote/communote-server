package com.communote.plugins.mq.message.core.handler;

/**
 * Senseless interface, which could be avoided.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
// TODO Remove this interface and find a good way to use the SecurityHelper for testing.
interface SecurityHelperWrapper {

    /**
     * @return The users id.
     */
    Long assertCurrentUserId();

}
