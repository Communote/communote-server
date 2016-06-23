package com.communote.server.core.exception.mapper;

import com.communote.server.core.exception.ExceptionMapper;
import com.communote.server.core.exception.Status;
import com.communote.server.core.security.SwitchUserNotAllowedException;


/**
 * Exception Mapper to map SwitchUserNotAllowed Exceptions to a message
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class SwitchUserNotAllowedExceptionMapper implements
        ExceptionMapper<SwitchUserNotAllowedException> {
    /**
     * {@inheritDoc}
     */
    @Override
    public Class<SwitchUserNotAllowedException> getExceptionClass() {
        return SwitchUserNotAllowedException.class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Status mapException(SwitchUserNotAllowedException exception) {
        return new Status("error.authorization.switch.user.not.allowed", new Object[] {
                exception.getOrginalUserId(), exception.getTargetUserId() }, AUTHORIZATION_ERROR);
    }
}
