package com.communote.plugins.api.rest.service.exception;

import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.communote.plugins.api.rest.exception.ResponseBuildException;

/**
 * {@link AbstractExceptionMapper} for {@link ResponseBuildException}
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
@Provider
public class ResponseBuildExceptionMapper extends
        AbstractExceptionMapper<ResponseBuildException> {

    /** Logger. */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(ResponseBuildExceptionMapper.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public String getErrorMessage(ResponseBuildException exception) {
        LOGGER.error(exception.getMessage(), exception);
        return getLocalizedMessage("error.rest.ResponseBuildException");
    }

    /**
     * @return {@link Status#INTERNAL_SERVER_ERROR}
     */
    @Override
    public int getStatusCode() {
        return Status.INTERNAL_SERVER_ERROR.getStatusCode();
    }

}
