package com.communote.plugins.api.rest.service.exception;

import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.Provider;

import com.communote.server.api.core.note.NoteManagementAuthorizationException;

/**
 * {@link AbstractExceptionMapper} for {@link NoteManagementAuthorizationException}
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
@Provider
public class NoteManagementAuthorizationExceptionMapper extends
        AbstractExceptionMapper<NoteManagementAuthorizationException> {
    /**
     * {@inheritDoc}
     */
    @Override
    public String getErrorMessage(NoteManagementAuthorizationException exception) {
        return getLocalizedMessage("error.blogpost.blog.no.write.access", exception.getBlogTitle());
    }

    /**
     * @return {@link Status#UNAUTHORIZED}
     */
    @Override
    public int getStatusCode() {
        return Status.UNAUTHORIZED.getStatusCode();
    }

}
