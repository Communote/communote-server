package com.communote.server.core.exception.mapper;

import com.communote.server.core.blog.NoteNotFoundException;
import com.communote.server.core.exception.ExceptionMapper;
import com.communote.server.core.exception.Status;


/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class NoteNotFoundExceptionMapper implements ExceptionMapper<NoteNotFoundException> {
    /**
     * {@inheritDoc}
     */
    @Override
    public Class<NoteNotFoundException> getExceptionClass() {
        return NoteNotFoundException.class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Status mapException(NoteNotFoundException exception) {
        return new Status("error.blogpost.not.found", null, NOT_FOUND);
    }

}
