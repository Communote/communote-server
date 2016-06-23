package com.communote.server.core.exception.mapper;

import com.communote.server.core.exception.Status;

/**
 * Fallback mapper for exception. This is called, when no more specific mapper is registered.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ExceptionExceptionMapper implements
        com.communote.server.core.exception.ExceptionMapper<Exception> {
    /**
     * {@inheritDoc}
     */
    @Override
    public Class<Exception> getExceptionClass() {
        return Exception.class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Status mapException(Exception exception) {
        return new Status("error.http.500", null, INTERNAL_ERROR);
    }
}
