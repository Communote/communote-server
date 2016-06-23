package com.communote.plugins.api.rest.service.exception;

import javax.ws.rs.core.Response.Status;

import com.communote.server.api.core.blog.BlogIdentifierValidationException;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class BlogIdentifierValidationExceptionMapper extends
        AbstractExceptionMapper<BlogIdentifierValidationException> {

    /**
     * {@inheritDoc}
     */
    @Override
    public String getErrorMessage(BlogIdentifierValidationException exception) {
        return getLocalizedMessage("error.blog.identifier.notvalid");
    }

    /**
     * @return Status.BAD_REQUEST.getStatusCode()
     */
    @Override
    public int getStatusCode() {
        return Status.BAD_REQUEST.getStatusCode();
    }
}
