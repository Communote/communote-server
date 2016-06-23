package com.communote.plugins.api.rest.service.exception;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.Provider;

import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.JsonMappingException.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.communote.plugins.api.rest.to.ApiResult;
import com.communote.plugins.api.rest.to.ApiResultError;

/**
 * {@link AbstractExceptionMapper} for {@link JsonMappingException}
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
@Provider
public class JsonMappingExceptionMapper extends AbstractExceptionMapper<JsonMappingException> {

    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(ExceptionMapper.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public String getErrorMessage(JsonMappingException exception) {
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
     * Handle messages of {@link JsonMappingException}
     * 
     * @param exception
     *            {@link JsonMappingException}
     * @param apiResult
     *            error result
     * @return {@link ApiResult} with error messages
     */
    @Override
    public ApiResult<Object> handle(JsonMappingException exception, ApiResult<Object> apiResult) {
        apiResult.setMessage(getErrorMessage(exception));
        List<Reference> references = exception.getPath();
        List<ApiResultError> apiResultErrors = new ArrayList<ApiResultError>();
        if (references != null) {
            ApiResultError error = new ApiResultError();
            error.setCause("Exception");
            String message = new String("wrong elements: ");
            for (Reference reference : references) {
                message += reference.getFieldName() + ", ";
            }
            message = message.substring(0, message.length() - 2);
            message += ".";
            error.setMessage(message);
            apiResultErrors.add(error);
        }
        apiResult.setErrors(apiResultErrors);
        return apiResult;
    }
}
