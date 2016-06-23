package com.communote.server.core.exception.mapper;

import com.communote.server.api.core.note.NoteManagementAuthorizationException;
import com.communote.server.core.exception.ExceptionMapper;
import com.communote.server.core.exception.Status;


/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class NoteManagementAuthorizationExceptionMapper implements
        ExceptionMapper<NoteManagementAuthorizationException> {
    /**
     * {@inheritDoc}
     */
    @Override
    public Class<NoteManagementAuthorizationException> getExceptionClass() {
        return NoteManagementAuthorizationException.class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Status mapException(NoteManagementAuthorizationException exception) {
        return new Status("error.blogpost.blog.no.write.access",
                new Object[] { exception.getBlogTitle() }, AUTHORIZATION_ERROR);
    }
}
