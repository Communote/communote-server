package com.communote.plugins.api.rest.v22.service.exception.mapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.restlet.engine.http.HttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.communote.plugins.api.rest.v22.exception.ExtensionNotSupportedException;
import com.communote.plugins.api.rest.v22.exception.ResponseBuildException;
import com.communote.plugins.api.rest.v22.response.ResponseHelper;
import com.communote.plugins.api.rest.v22.to.ApiResult;
import com.communote.plugins.api.rest.v22.to.ApiResultError;
import com.communote.server.api.ServiceLocator;
import com.communote.server.core.common.session.SessionHandler;
import com.communote.server.core.exception.ErrorCodes;
import com.communote.server.core.exception.ExceptionMapperManagement;
import com.communote.server.core.exception.Reason;
import com.communote.server.core.exception.Status;

/**
 * Adapter that creates a Restlet Response from an exception with the help of the Communote
 * exception mapping infrastructure.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * @see ExceptionMapperManagement
 */
@Provider
public class CommunoteExceptionMapper implements ExceptionMapper<Exception> {

    /** Logger. */
    private final static Logger LOGGER = LoggerFactory.getLogger(CommunoteExceptionMapper.class);

    private final static String FALLBACK_RESPONSE_CONTENT_TYPE = "application/json";

    /** A Mapping to HTTP status codes */
    private final static Map<String, Integer> ERROR_CODES_TO_HTTP_CODES = new HashMap<String, Integer>();

    static {
        ERROR_CODES_TO_HTTP_CODES.put(ErrorCodes.UNKNOWN_ERROR, 500);
        ERROR_CODES_TO_HTTP_CODES.put(ErrorCodes.OKAY, 200);
        ERROR_CODES_TO_HTTP_CODES.put(ErrorCodes.WARNING, 200);
        ERROR_CODES_TO_HTTP_CODES.put(ErrorCodes.BAD_REQUEST, 400);
        ERROR_CODES_TO_HTTP_CODES.put(ErrorCodes.ILLEGAL_PARAMETERS_ERROR, 400);
        ERROR_CODES_TO_HTTP_CODES.put(ErrorCodes.VALIDATION_ERROR, 400);
        ERROR_CODES_TO_HTTP_CODES.put(ErrorCodes.AUTHENTICATION_ERROR, 401);
        ERROR_CODES_TO_HTTP_CODES.put(ErrorCodes.AUTHORIZATION_ERROR, 401);
        ERROR_CODES_TO_HTTP_CODES.put(ErrorCodes.FORBIDDEN, 403);
        ERROR_CODES_TO_HTTP_CODES.put(ErrorCodes.NOT_FOUND, 404);
        ERROR_CODES_TO_HTTP_CODES.put(ErrorCodes.NOT_ACCEPTABLE, 406);
        ERROR_CODES_TO_HTTP_CODES.put(ErrorCodes.INTERNAL_ERROR, 500);
    }

    private final ExceptionMapperManagement exceptionMapperManagement = ServiceLocator.instance()
            .getService(ExceptionMapperManagement.class);

    /**
     * @return The locale of the current user
     */
    private Locale getCurrentUserLocale() {
        HttpServletRequest httpRequest = getHttpServletRequest();
        return SessionHandler.instance().getCurrentLocale(httpRequest);
    }

    /**
     * Get the {@link HttpServletRequest}
     * 
     * @return {@link HttpServletRequest}
     */
    private HttpServletRequest getHttpServletRequest() {
        HttpRequest httpRequest = (HttpRequest) org.restlet.Request
                .getCurrent();
        return ((org.restlet.ext.servlet.internal.ServletCall) httpRequest.getHttpCall())
                .getRequest();
    }

    /**
     * Subclass can override this method to add more detailed information to the response via the
     * api result. This implementation will write the message of the exception as message of the api
     * result.
     * 
     * @param status
     *            The status to handle.
     * @param apiResult
     *            The api result this mapper can handle with.
     * @return The {@link ApiResult} which should be used for the response.
     */
    private ApiResult<Object> handle(Status status, ApiResult<Object> apiResult) {
        String message = status.getMessage().toString(getCurrentUserLocale());
        apiResult.setMessage(message);
        apiResult.setErrors(new ArrayList<ApiResultError>());
        for (Reason reason : status.getErrors()) {
            ApiResultError error = new ApiResultError();
            error.setMessage(reason.getErrorMessage().toString(getCurrentUserLocale()));
            error.setCause(reason.getErrorCause());
            apiResult.getErrors().add(error);
        }
        return apiResult;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Response toResponse(Exception exception) {
        Status status = exceptionMapperManagement.mapException(exception);
        Integer statusCode = ERROR_CODES_TO_HTTP_CODES.get(status.getErrorCode());
        if (statusCode == null) {
            statusCode = 500;
        }
        if (statusCode >= 500) {
            LOGGER.error(exception.getMessage(), exception);
        } else {
            LOGGER.debug(exception.getMessage(), exception);
        }

        try {
            return ResponseHelper.buildErrorResponse(handle(status, new ApiResult<Object>()),
                    Response.status(statusCode),
                    getHttpServletRequest());
        } catch (ResponseBuildException e) {
            status = new ResponseBuildExceptionMapper().mapException(e);
            LOGGER.error(exception.getMessage(), exception);
        } catch (ExtensionNotSupportedException e) {
            status = new ExtensionNotSupportedExceptionMapper().mapException(e);
            LOGGER.debug(exception.getMessage(), exception);
        }
        ApiResult<Object> apiResult = new ApiResult<Object>();
        apiResult.setMessage(status.getMessage().toString(getCurrentUserLocale()));
        apiResult.setStatus(status.getErrorCode());
        ResponseBuilder responseBuilder = Response.status(statusCode);
        responseBuilder.type(FALLBACK_RESPONSE_CONTENT_TYPE);
        apiResult.setStatus(ApiResult.ResultStatus.ERROR.toString());
        responseBuilder.entity(apiResult);
        responseBuilder.status(ERROR_CODES_TO_HTTP_CODES.get(status.getErrorCode()));
        return responseBuilder.build();
    }
}
