package com.communote.plugins.api.rest.service.exception;

import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.Provider;

import com.communote.server.api.core.user.CommunoteEntityNotFoundException;

/**
 * {@link AbstractExceptionMapper} for {@link CommunoteEntityNotFoundException}
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
@Provider
public class CommunoteEntityNotFoundExceptionMapper extends
        AbstractExceptionMapper<CommunoteEntityNotFoundException> {

    @Override
    public String getErrorMessage(CommunoteEntityNotFoundException exception) {
        return getLocalizedMessage("error.blog.change.rights.failed.noEntity");
    }

    /**
     * @return {@link Status#FORBIDDEN}
     */
    @Override
    public int getStatusCode() {
        return Status.NOT_FOUND.getStatusCode();
    }
}
