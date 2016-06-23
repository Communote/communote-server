package com.communote.plugins.api.rest.service.exception;

import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.Provider;

import com.communote.server.api.core.security.AuthorizationException;

/**
 * {@link AbstractExceptionMapper} for {@link AuthorizationException}.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
@Provider
public class AuthorizationExceptionMapper extends AbstractExceptionMapper<AuthorizationException> {

    @Override
    public String getErrorMessage(AuthorizationException exception) {
        return getLocalizedMessage("common.not.authorized.operation");
    }

    /**
     * @return {@link Status#UNAUTHORIZED}
     */
    @Override
    public int getStatusCode() {
        return Status.UNAUTHORIZED.getStatusCode();
    }

}
