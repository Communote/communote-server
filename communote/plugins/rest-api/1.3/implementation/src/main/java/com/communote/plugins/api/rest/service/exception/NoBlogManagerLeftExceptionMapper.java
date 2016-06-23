package com.communote.plugins.api.rest.service.exception;

import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.Provider;

import com.communote.server.api.core.blog.NoBlogManagerLeftException;

/**
 * {@link AbstractExceptionMapper} for {@link NoBlogManagerLeftException}
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
@Provider
public class NoBlogManagerLeftExceptionMapper extends
        AbstractExceptionMapper<NoBlogManagerLeftException> {

    @Override
    public String getErrorMessage(NoBlogManagerLeftException exception) {
        return getLocalizedMessage("user.group.member.change.role.no.manager.left.exception");
    }

    /**
     * @return {@link Status#FORBIDDEN}
     */
    @Override
    public int getStatusCode() {
        return Status.FORBIDDEN.getStatusCode();
    }

}
