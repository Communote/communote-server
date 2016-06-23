package com.communote.plugins.api.rest.service.exception;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.communote.plugins.api.rest.exception.RestApiIllegalRequestParameterException;
import com.communote.plugins.api.rest.to.ApiResult;
import com.communote.plugins.api.rest.to.ApiResultError;

/**
 * {@link AbstractExceptionMapper} for {@link RestApiIllegalRequestParameterException}
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
@Provider
public class RestApiIllegalRequestParameterExceptionMapper extends
        AbstractExceptionMapper<RestApiIllegalRequestParameterException> {

    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(ExceptionMapper.class);

    @Override
    public String getErrorMessage(RestApiIllegalRequestParameterException exception) {
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
     * Handle messages of {@link RestApiIllegalRequestParameterException}
     * 
     * @param exception
     *            {@link RestApiIllegalRequestParameterException}
     * @param apiResult
     *            error result
     * @return {@link ApiResult} with error messages
     */
    @Override
    public ApiResult<Object> handle(RestApiIllegalRequestParameterException exception,
            ApiResult<Object> apiResult) {
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
