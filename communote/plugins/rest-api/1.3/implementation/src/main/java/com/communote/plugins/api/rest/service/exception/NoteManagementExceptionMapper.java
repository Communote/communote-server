package com.communote.plugins.api.rest.service.exception;

import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.communote.server.core.blog.NoteManagementException;

/**
 * {@link AbstractExceptionMapper} for {@link NoteManagementException}.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
@Provider
public class NoteManagementExceptionMapper extends AbstractExceptionMapper<NoteManagementException> {

    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(ExceptionMapper.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public String getErrorMessage(NoteManagementException exception) {
        LOGGER.error(exception.getMessage(), exception);
        return getLocalizedMessage("error.rest.NoteManagementException");
    }

    /**
     * @return {@link Status#FORBIDDEN}
     */
    @Override
    public int getStatusCode() {
        return Status.FORBIDDEN.getStatusCode();
    }
}
