package com.communote.server.core.exception.mapper;

import com.communote.server.api.core.blog.NonUniqueBlogIdentifierException;
import com.communote.server.core.exception.ExceptionMapper;
import com.communote.server.core.exception.Status;


/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class NonUniqueBlogIdentifierExceptionMapper implements
        ExceptionMapper<NonUniqueBlogIdentifierException> {
    /**
     * {@inheritDoc}
     */
    @Override
    public Class<NonUniqueBlogIdentifierException> getExceptionClass() {
        return NonUniqueBlogIdentifierException.class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Status mapException(NonUniqueBlogIdentifierException exception) {
        return new Status("error.blog.identifier.noneunique", null, BAD_REQUEST);
    }

}
