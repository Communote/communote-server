package com.communote.plugins.api.rest.service.exception;

import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.Provider;

import com.communote.server.api.core.blog.BlogValidationException;

/**
 * {@link AbstractExceptionMapper} for {@link BlogValidationException}
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
@Provider
public class BlogValidationExceptionMapper extends AbstractExceptionMapper<BlogValidationException> {

    @Override
    public String getErrorMessage(BlogValidationException exception) {
        return getLocalizedMessage("error.validation");
    }

    /**
     * @return {@link Status#BAD_REQUEST}
     */
    @Override
    public int getStatusCode() {
        return Status.BAD_REQUEST.getStatusCode();
    }

}
