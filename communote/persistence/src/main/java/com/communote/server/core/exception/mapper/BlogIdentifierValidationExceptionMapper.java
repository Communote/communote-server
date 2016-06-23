package com.communote.server.core.exception.mapper;

import com.communote.server.api.core.blog.BlogIdentifierValidationException;
import com.communote.server.core.exception.ExceptionMapper;
import com.communote.server.core.exception.Status;


/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class BlogIdentifierValidationExceptionMapper implements
        ExceptionMapper<BlogIdentifierValidationException> {
    /**
     * {@inheritDoc}
     */
    @Override
    public Class<BlogIdentifierValidationException> getExceptionClass() {
        return BlogIdentifierValidationException.class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Status mapException(BlogIdentifierValidationException exception) {
        return new Status("error.blog.identifier.notvalid", null, BAD_REQUEST);
    }
}
