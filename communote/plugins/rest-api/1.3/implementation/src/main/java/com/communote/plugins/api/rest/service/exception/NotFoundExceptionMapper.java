package com.communote.plugins.api.rest.service.exception;

import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.Provider;

import com.communote.server.api.core.common.NotFoundException;

/**
 * {@link AbstractExceptionMapper} for {@link NotFoundException}
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
@Provider
public class NotFoundExceptionMapper extends AbstractExceptionMapper<NotFoundException> {

    @Override
    public String getErrorMessage(NotFoundException exception) {
        return getLocalizedMessage("error.rest.NotFoundException");
    }

    /**
     * @return Status.NOT_FOUND
     */
    @Override
    public int getStatusCode() {
        return Status.NOT_FOUND.getStatusCode();
    }

}
