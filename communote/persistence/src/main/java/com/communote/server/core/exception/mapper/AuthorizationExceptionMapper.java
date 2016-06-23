package com.communote.server.core.exception.mapper;

import org.springframework.stereotype.Component;

import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.core.exception.ExceptionMapper;
import com.communote.server.core.exception.Status;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
@Component
public class AuthorizationExceptionMapper implements ExceptionMapper<AuthorizationException> {
    /**
     * {@inheritDoc}
     */
    @Override
    public Class<AuthorizationException> getExceptionClass() {
        return AuthorizationException.class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Status mapException(AuthorizationException exception) {
        return new Status("common.not.authorized.operation", null, AUTHORIZATION_ERROR);
    }

}
