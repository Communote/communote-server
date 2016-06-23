package com.communote.server.core.exception.mapper;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import com.communote.server.core.exception.ErrorCodes;
import com.communote.server.core.exception.ExceptionMapper;
import com.communote.server.core.exception.Status;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
@Component
public class SpringAccessDeniedExceptionMapper implements ExceptionMapper<AccessDeniedException> {
    /**
     * {@inheritDoc}
     */
    @Override
    public Class<AccessDeniedException> getExceptionClass() {
        return AccessDeniedException.class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Status mapException(AccessDeniedException exception) {
        return new Status("common.not.authorized.operation", null, ErrorCodes.AUTHORIZATION_ERROR);
    }
}
