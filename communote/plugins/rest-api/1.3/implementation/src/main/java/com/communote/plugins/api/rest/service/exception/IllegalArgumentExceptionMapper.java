package com.communote.plugins.api.rest.service.exception;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.communote.plugins.api.rest.to.ApiResult;
import com.communote.plugins.api.rest.to.ApiResultError;

/**
 * {@link AbstractExceptionMapper} for {@link IllegalArgumentException}
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
@Provider
public class IllegalArgumentExceptionMapper extends
        AbstractExceptionMapper<IllegalArgumentException> {

    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(ExceptionMapper.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public String getErrorMessage(IllegalArgumentException exception) {
        LOGGER.error(exception.getMessage(), exception);
        return getLocalizedMessage("error.rest.badrequest");
    }

    /**
     * @return {@link Status#BAD_REQUEST}
     */
    @Override
    public int getStatusCode() {
        return Status.BAD_REQUEST.getStatusCode();
    }

    /**
     * Handle messages of {@link IllegalArgumentException}
     * 
     * @param exception
     *            {@link IllegalArgumentException}
     * @param apiResult
     *            error result
     * @return {@link ApiResult} with error messages
     */
    @Override
    public ApiResult<Object> handle(IllegalArgumentException exception, ApiResult<Object> apiResult) {
        apiResult.setMessage(getErrorMessage(exception));
        List<ApiResultError> apiResultErrors = new ArrayList<ApiResultError>();
        ApiResultError error = new ApiResultError();
        error.setCause("Exception");
        error.setMessage(exception.getLocalizedMessage());
        apiResultErrors.add(error);
        apiResult.setErrors(apiResultErrors);
        return apiResult;
    }

}
