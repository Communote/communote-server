package com.communote.server.core.exception.mapper;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import com.communote.common.i18n.StaticLocalizedMessage;
import com.communote.server.api.core.blog.BlogNotFoundException;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.core.blog.BlogManagementException;
import com.communote.server.core.exception.ExceptionMapper;
import com.communote.server.core.exception.Status;


/**
 * {@link ExceptionMapper} to handle {@link BlogManagementException}.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
@Component
public class BlogManagementExceptionMapper implements ExceptionMapper<BlogManagementException> {

    /**
     * @return BlogManagementException.class
     */
    @Override
    public Class<BlogManagementException> getExceptionClass() {
        return BlogManagementException.class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Status mapException(BlogManagementException exception) {
        Throwable cause = exception.getCause();
        if (cause instanceof AccessDeniedException || cause instanceof AuthorizationException) {
            return new Status("error.blogpost.blog.no.access", null, AUTHORIZATION_ERROR);
        }
        if (cause instanceof BlogNotFoundException) {
            return new Status("error.blogpost.blog.not.found",
                    new Object[] { ((BlogNotFoundException) cause).getBlogNameId() }, NOT_FOUND);
        }
        return new Status(new StaticLocalizedMessage(exception.getMessage()), UNKNOWN_ERROR);
    }
}
