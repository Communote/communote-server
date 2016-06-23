package com.communote.server.core.exception.mapper;

import org.springframework.stereotype.Component;

import com.communote.server.core.common.exceptions.UnexpectedAuthorizationException;
import com.communote.server.core.exception.ExceptionMapper;
import com.communote.server.core.exception.Status;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
@Component
public class UnexpectedAuthorizationExceptionMapper implements
        ExceptionMapper<UnexpectedAuthorizationException> {
    /**
     * {@inheritDoc}
     */
    @Override
    public Class<UnexpectedAuthorizationException> getExceptionClass() {
        return UnexpectedAuthorizationException.class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Status mapException(UnexpectedAuthorizationException exception) {
        return new Status("common.not.authorized.operation", null, AUTHORIZATION_ERROR);
    }

}
