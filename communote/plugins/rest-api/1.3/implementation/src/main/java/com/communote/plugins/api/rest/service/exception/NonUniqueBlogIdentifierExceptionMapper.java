package com.communote.plugins.api.rest.service.exception;

import javax.ws.rs.core.Response.Status;

import com.communote.server.api.core.blog.NonUniqueBlogIdentifierException;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class NonUniqueBlogIdentifierExceptionMapper extends
        AbstractExceptionMapper<NonUniqueBlogIdentifierException> {

    /**
     * {@inheritDoc}
     */
    @Override
    public String getErrorMessage(NonUniqueBlogIdentifierException exception) {
        return getLocalizedMessage("error.blog.identifier.noneunique");
    }

    /**
     * @return Status.BAD_REQUEST.getStatusCode()
     */
    @Override
    public int getStatusCode() {
        return Status.BAD_REQUEST.getStatusCode();
    }

}
