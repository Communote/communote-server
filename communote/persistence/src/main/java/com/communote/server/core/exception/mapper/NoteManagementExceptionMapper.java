package com.communote.server.core.exception.mapper;

import com.communote.server.core.blog.NoteManagementException;
import com.communote.server.core.exception.ErrorCodes;
import com.communote.server.core.exception.ExceptionMapper;
import com.communote.server.core.exception.Status;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class NoteManagementExceptionMapper implements ExceptionMapper<NoteManagementException> {

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<NoteManagementException> getExceptionClass() {
        return NoteManagementException.class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Status mapException(NoteManagementException exception) {
        // TODO is there a way to get a more specific error message or at least make it context
        // sensitive to the failed operation?
        return new Status("common.error.unspecified", null, ErrorCodes.UNKNOWN_ERROR);
    }
}
