package com.communote.plugins.api.rest.service.exception;

import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.Provider;

import com.communote.server.core.blog.NoteNotFoundException;

/**
 * {@link AbstractExceptionMapper} for {@link NoteNotFoundException}
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
@Provider
public class NoteNotFoundExceptionMapper extends AbstractExceptionMapper<NoteNotFoundException> {

    @Override
    public String getErrorMessage(NoteNotFoundException exception) {
        return getLocalizedMessage("error.blogpost.not.found");
    }

    /**
     * @return {@link Status#NOT_FOUND}
     */
    @Override
    public int getStatusCode() {
        return Status.NOT_FOUND.getStatusCode();
    }

}
