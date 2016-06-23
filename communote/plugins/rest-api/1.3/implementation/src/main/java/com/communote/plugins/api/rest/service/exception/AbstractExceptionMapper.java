package com.communote.plugins.api.rest.service.exception;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.restlet.engine.http.HttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.communote.plugins.api.rest.exception.ExtensionNotSupportedException;
import com.communote.plugins.api.rest.exception.ResponseBuildException;
import com.communote.plugins.api.rest.response.ResponseHelper;
import com.communote.plugins.api.rest.to.ApiResult;
import com.communote.plugins.api.rest.to.ApiResultError;
import com.communote.server.core.common.session.SessionHandler;
import com.communote.server.persistence.common.messages.ResourceBundleManager;

/**
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 * @param <E>
 *            Type of the Exception this mapper works for.
 */
public abstract class AbstractExceptionMapper<E extends Throwable> implements
        javax.ws.rs.ext.ExceptionMapper<E> {

    private static final String FALLBACK_RESPONSE_CONTENT_TYPE = "application/json";
    private static final String FIRST_STATUS_CODE_NUMBER_OF_SERVER_ERRORS = "5";
    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractExceptionMapper.class);

    /**
     * Get the locale of the current user
     * 
     * @param request
     *            {@link Request}
     * @return {@link Locale}
     */
    protected Locale getCurrentUserLocale(org.restlet.Request request) {
        HttpServletRequest httpRequest = getHttpServletRequest(request);
        return SessionHandler.instance().getCurrentLocale(httpRequest);
    }

    /**
     * Get the message of the exception in mapper.
     * 
     * @param exception
     *            thrown
     * @return message of exception
     */
    public abstract String getErrorMessage(E exception);

    /**
     * Get the {@link HttpServletRequest} of {@link Request}
     * 
     * @param request
     *            {@link Request}
     * @return {@link HttpServletRequest}
     */
    private HttpServletRequest getHttpServletRequest(org.restlet.Request request) {

        return ((org.restlet.ext.servlet.internal.ServletCall) ((HttpRequest) request)
                .getHttpCall()).getRequest();
    }

    /**
     * 
     * @param messageKey
     *            key of the message
     * @param arguments
     *            arguments
     * @return localized message
     */
    protected String getLocalizedMessage(String messageKey, Object... arguments) {
        return ResourceBundleManager.instance().getText(messageKey,
                getCurrentUserLocale(org.restlet.Request.getCurrent()), arguments);
    }

    /**
     * @return The status code of the response.
     */
    public abstract int getStatusCode();

    /**
     * Subclass can override this method to add more detailed information to the response via the
     * api result. This implementation will write the message of the exception as message of the api
     * result.
     * 
     * @param exception
     *            The exception.
     * @param apiResult
     *            The api result this mapper can handle with.
     * @return The {@link ApiResult} which should be used for the response.
     */
    public ApiResult<Object> handle(E exception, ApiResult<Object> apiResult) {
        apiResult.setMessage(getErrorMessage(exception));
        ApiResultError error = new ApiResultError();
        error.setMessage(getErrorMessage(exception));
        if (apiResult.getErrors() != null) {
            apiResult.getErrors().add(error);
        } else {
            List<ApiResultError> apiResultErrors = new ArrayList<ApiResultError>();
            apiResultErrors.add(error);
            apiResult.setErrors(apiResultErrors);
        }
        return apiResult;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public javax.ws.rs.core.Response toResponse(E exception) {
        if (String.valueOf(getStatusCode()).startsWith(FIRST_STATUS_CODE_NUMBER_OF_SERVER_ERRORS)) {
            LOGGER.error(exception.getMessage(), exception);
        } else {
            LOGGER.debug(exception.getMessage(), exception);
        }

        ApiResult<Object> apiResult = null;
        int statusCode;
        try {
            return ResponseHelper.buildErrorResponse(handle(exception, new ApiResult<Object>()),
                    Response.status(getStatusCode()),
                    getHttpServletRequest(org.restlet.Request.getCurrent()));
        } catch (ResponseBuildException e) {
            ResponseBuildExceptionMapper mapper = new ResponseBuildExceptionMapper();
            apiResult = mapper.handle(e, new ApiResult<Object>());
            statusCode = mapper.getStatusCode();
            LOGGER.error(exception.getMessage(), exception);
        } catch (ExtensionNotSupportedException e) {
            ExtensionNotSupportedExceptionMapper mapper = new ExtensionNotSupportedExceptionMapper();
            apiResult = mapper.handle(e, new ApiResult<Object>());
            statusCode = mapper.getStatusCode();
            LOGGER.debug(exception.getMessage(), exception);
        }

        ResponseBuilder responseBuilder = Response.status(statusCode);
        responseBuilder.type(FALLBACK_RESPONSE_CONTENT_TYPE);
        apiResult.setStatus(ApiResult.ResultStatus.ERROR.toString());
        responseBuilder.entity(apiResult);
        return responseBuilder.build();
    }
}
