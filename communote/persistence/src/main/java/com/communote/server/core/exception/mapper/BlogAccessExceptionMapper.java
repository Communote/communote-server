package com.communote.server.core.exception.mapper;

import com.communote.server.api.core.blog.BlogAccessException;
import com.communote.server.core.exception.ErrorCodes;
import com.communote.server.core.exception.ExceptionMapper;
import com.communote.server.core.exception.Status;


/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class BlogAccessExceptionMapper implements ExceptionMapper<BlogAccessException> {
    /**
     * {@inheritDoc}
     */
    @Override
    public Class<BlogAccessException> getExceptionClass() {
        return BlogAccessException.class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Status mapException(BlogAccessException exception) {
        String keySuffix = exception.getRequiredRole() != null ? exception.getRequiredRole()
                .toString() : "none";
        return new Status("blog.error.access.required." + keySuffix, null,
                ErrorCodes.FORBIDDEN);
    }
}
