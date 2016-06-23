package com.communote.plugins.api.rest.service.exception;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.communote.plugins.api.rest.resource.validation.ParameterValidationError;
import com.communote.plugins.api.rest.resource.validation.ParameterValidationException;
import com.communote.plugins.api.rest.to.ApiResult;
import com.communote.plugins.api.rest.to.ApiResultError;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
@Provider
public class ParameterValidationExceptionMapper extends
        AbstractExceptionMapper<ParameterValidationException> {

    /** Logger. */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(ParameterValidationExceptionMapper.class);

    /**
     * converts ParameterValidationError to ApiResultError
     * 
     * @param error
     *            ParameterValidationError object
     * @return ApiResultError object
     */
    private ApiResultError formApiError(ParameterValidationError error) {
        ApiResultError apiError = new ApiResultError();
        apiError.setCause(error.getSource());
        apiError.setMessage(getLocalizedMessage(error.getMessageKey(), error.getParameters()));
        return apiError;
    }

    @Override
    public String getErrorMessage(ParameterValidationException exception) {
        return getLocalizedMessage("error.rest.badrequest");
    }

    @Override
    public int getStatusCode() {
        return Status.BAD_REQUEST.getStatusCode();
    }

    @Override
    public ApiResult<Object> handle(ParameterValidationException exception,
            ApiResult<Object> apiResult) {
        LOGGER.info(exception.getMessage());
        List<ApiResultError> errors = new ArrayList<ApiResultError>();
        for (ParameterValidationError error : exception.getErrors()) {
            errors.add(formApiError(error));
        }
        apiResult.setMessage(getLocalizedMessage(exception.getMessageKey()));
        apiResult.setErrors(errors);
        return apiResult;
    }

}
