package com.communote.server.core.exception.mapper;

import com.communote.server.api.core.blog.BlogValidationException;
import com.communote.server.core.exception.ExceptionMapper;
import com.communote.server.core.exception.Status;


/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class BlogValidationExceptionMapper implements ExceptionMapper<BlogValidationException> {
    /**
     * {@inheritDoc}
     */
    @Override
    public Class<BlogValidationException> getExceptionClass() {
        return BlogValidationException.class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Status mapException(BlogValidationException exception) {
        return new Status("error.validation", null, BAD_REQUEST);
    }
}
