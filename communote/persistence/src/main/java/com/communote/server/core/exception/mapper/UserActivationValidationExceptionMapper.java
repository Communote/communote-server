package com.communote.server.core.exception.mapper;

import com.communote.server.core.exception.ExceptionMapper;
import com.communote.server.core.exception.Status;
import com.communote.server.core.user.validation.UserActivationValidationException;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class UserActivationValidationExceptionMapper implements
        ExceptionMapper<UserActivationValidationException> {
    /**
     * {@inheritDoc}
     */
    @Override
    public Class<UserActivationValidationException> getExceptionClass() {
        return UserActivationValidationException.class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Status mapException(UserActivationValidationException exception) {
        return new Status(exception.getReason("client.user.management.activate.user.error."),
                FORBIDDEN);
    }

}
