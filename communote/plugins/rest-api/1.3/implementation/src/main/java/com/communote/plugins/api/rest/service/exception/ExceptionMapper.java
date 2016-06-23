package com.communote.plugins.api.rest.service.exception;

import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link AbstractExceptionMapper} for {@link Exception}
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
@Provider
public class ExceptionMapper extends AbstractExceptionMapper<Exception> {

    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(ExceptionMapper.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public String getErrorMessage(Exception exception) {
        LOGGER.error(exception.getMessage(), exception);
        return getLocalizedMessage("error.http.500");
    }

    /**
     * @return {@link Status#INTERNAL_SERVER_ERROR}
     */
    @Override
    public int getStatusCode() {
        return Status.INTERNAL_SERVER_ERROR.getStatusCode();
    }

}
