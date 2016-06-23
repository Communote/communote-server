package com.communote.plugins.api.rest.service.exception;

import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.Provider;

import com.communote.server.api.core.blog.BlogAccessException;

/**
 * {@link AbstractExceptionMapper} for {@link BlogAccessException}
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
@Provider
public class BlogAccessExceptionMapper extends AbstractExceptionMapper<BlogAccessException> {

    /**
     * {@inheritDoc}
     */
    @Override
    public String getErrorMessage(BlogAccessException exception) {
        String keySuffix = exception.getRequiredRole() != null ? exception.getRequiredRole()
                .toString() : "none";
        return getLocalizedMessage("blog.error.access.required." + keySuffix);
    }

    /**
     * @return {@link Status#FORBIDDEN}
     */
    @Override
    public int getStatusCode() {
        return Status.FORBIDDEN.getStatusCode();
    }

}
