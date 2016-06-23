package com.communote.plugins.api.rest.v30.response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.communote.plugins.api.rest.v30.exception.ExtensionNotSupportedException;
import com.communote.plugins.api.rest.v30.exception.ResponseBuildException;
import com.communote.plugins.api.rest.v30.request.RequestHelper;
import com.communote.plugins.api.rest.v30.resource.ResourceHandlerHelper;
import com.communote.plugins.api.rest.v30.to.ApiResult;
import com.communote.plugins.api.rest.v30.to.ApiResult.ResultStatus;
import com.communote.plugins.api.rest.v30.to.ApiResultError;
import com.communote.server.core.common.session.SessionHandler;
import com.communote.server.core.exception.ErrorCodes;
import com.communote.server.core.exception.Reason;
import com.communote.server.persistence.common.messages.ResourceBundleManager;

/**
 * The response helper contains all functionality to build the response.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public final class ResponseHelper {

    private static final String EXTENSION_HTML = "html";

    private static String TYPE_APPLICATION_JSON = "application/json";

    private static String TYPE_TEXT_HTML = "text/html";

    /** Logger. */
    private static Logger LOGGER = LoggerFactory.getLogger(ResponseHelper.class);

    /** HTML Container. */
    private static String HTML_RESPONSE_CONTAINER = "<html><head></head><body><script type=\"text/javascript\">"
            + "var response = #jsonResponse#;</script></body></html>";

    public final static ObjectMapper MAPPER = new ObjectMapper();
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
        ERROR_CODES_TO_HTTP_CODES.put(ErrorCodes.AUTHORIZATION_ERROR, 403);
        ERROR_CODES_TO_HTTP_CODES.put(ErrorCodes.FORBIDDEN, 403);
        ERROR_CODES_TO_HTTP_CODES.put(ErrorCodes.NOT_FOUND, 404);
        ERROR_CODES_TO_HTTP_CODES.put(ErrorCodes.NOT_ACCEPTABLE, 406);
        ERROR_CODES_TO_HTTP_CODES.put(ErrorCodes.INTERNAL_ERROR, 500);
    }

    /**
     * Build the error response.
     *
     * @param apiResult
     *            {@link ApiResult}
     * @param responseBuilder
     *            {@link ResponseBuilder}
     * @param httpServletRequest
     *            {@link HttpServletRequest}
     * @return {@link Response}
     * @throws ResponseBuildException
     *             exception while building the response
     * @throws ExtensionNotSupportedException
     *             extension is not supported
     */
    public static Response buildErrorResponse(ApiResult<Object> apiResult,
            ResponseBuilder responseBuilder, HttpServletRequest httpServletRequest)
            throws ResponseBuildException, ExtensionNotSupportedException {

        apiResult.setStatus(ApiResult.ResultStatus.ERROR.toString());
        return buildResponseByExtension(apiResult, responseBuilder, httpServletRequest);
    }

    /**
     * Build an error response from the error describing status object and the current request
     *
     * @param status
     *            the status object holding the details about the occurred error
     * @param request
     *            the current request
     * @return the response
     * @throws ResponseBuildException
     *             in case of an error while building the response
     * @throws ExtensionNotSupportedException
     *             in case the extension describing the response content-type is not supported
     */
    public static Response buildErrorResponse(com.communote.server.core.exception.Status status,
            Request request) throws ResponseBuildException, ExtensionNotSupportedException {
        ApiResult<Object> apiResult = new ApiResult<Object>();
        HttpServletRequest httpServletRequest = RequestHelper.getHttpServletRequest(request);
        Locale currentLocale = SessionHandler.instance().getCurrentLocale(httpServletRequest);
        String message = status.getMessage().toString(currentLocale);
        apiResult.setMessage(message);
        apiResult.setErrors(new ArrayList<ApiResultError>());
        for (Reason reason : status.getErrors()) {
            ApiResultError error = new ApiResultError();
            error.setMessage(reason.getErrorMessage().toString(currentLocale));
            error.setCause(reason.getErrorCause());
            apiResult.getErrors().add(error);
        }
        return ResponseHelper.buildErrorResponse(apiResult,
                Response.status(getHttpCodeFromErrorCode(status.getErrorCode())),
                httpServletRequest);
    }

    /**
     * Get the success response from resource
     *
     * @param <T>
     *            type of resource
     * @param result
     *            resource
     * @param message
     *            message setted in the request
     * @param request
     *            {@link Request}
     * @param status
     *            The status to set.
     * @param errors
     *            Optional errors, which should be send back.
     * @return {@link Response}
     * @throws ResponseBuildException
     *             exception while building the response
     * @throws ExtensionNotSupportedException
     *             extension is not supported
     */
    public static <T> Response buildResponse(T result, String message, Request request,
            ResultStatus status, Reason... errors) throws ResponseBuildException,
            ExtensionNotSupportedException {
        ApiResult<T> apiResult = buildSuccessApiResult(result);
        apiResult.setMessage(message);
        apiResult.setStatus(status.toString());
        if (errors != null && errors.length > 0) {
            apiResult.setErrors(new ArrayList<ApiResultError>());
            for (Reason reason : errors) {
                ApiResultError error = new ApiResultError();
                error.setMessage(reason.getErrorMessage().toString(
                        ResourceHandlerHelper.getCurrentUserLocale(request)));
                error.setCause(reason.getErrorCause());
                apiResult.getErrors().add(error);
            }
        }
        return buildResponseByExtension(apiResult, Response.status(Response.Status.OK), request);
    }

    /**
     * Get the success response from resource
     *
     * @param <T>
     *            type of resource
     * @param result
     *            resource
     * @param message
     *            message setted in the request
     * @param request
     *            {@link Request}
     * @param status
     *            The status to set.
     * @param responseStatus
     *            Status of the response who is required for the status code.
     * @return {@link Response}
     * @throws ResponseBuildException
     *             exception while building the response
     * @throws ExtensionNotSupportedException
     *             extension is not supported
     */
    public static <T> Response buildResponse(T result, String message, Request request,
            ResultStatus status, Status responseStatus) throws ResponseBuildException,
            ExtensionNotSupportedException {
        ApiResult<T> apiResult = buildSuccessApiResult(result);
        apiResult.setMessage(message);
        apiResult.setStatus(status.toString());
        return buildResponseByExtension(apiResult, Response.status(responseStatus), request);
    }

    /**
     * Function checks the path info extension. If extension is empty or .json an JSON-Response is
     * returns. If the extension is .html an HTML Content is the result.
     *
     * TODO read from parameter
     *
     * @param <T>
     *            type of resource
     * @param apiResult
     *            {@link ApiResult} with elements to serialize
     * @param responseBuilder
     *            {@link ResponseBuilder}
     * @param httpServletRequest
     *            {@link HttpServletRequest}
     * @return {@link Response}
     * @throws ResponseBuildException
     *             exception while building the response
     * @throws ExtensionNotSupportedException
     *             extension is not supported
     *
     *
     */
    private static <T> Response buildResponseByExtension(ApiResult<T> apiResult,
            ResponseBuilder responseBuilder, HttpServletRequest httpServletRequest)
                    throws ResponseBuildException, ExtensionNotSupportedException {
        String[] splittedPathInfo = httpServletRequest.getPathInfo().split("\\.");

        if (splittedPathInfo.length == 2) {
            if (splittedPathInfo[1].equalsIgnoreCase(EXTENSION_HTML)) {
                return configureResponseBuilderForHtml(apiResult, responseBuilder).build();
            }
            /*
             * See KENMEI-5729 why JSON should be default
             *
             * else if (!splittedPathInfo[1].equalsIgnoreCase(EXTENSION_JSON)) { throw new
             * ExtensionNotSupportedException(splittedPathInfo[1]); }
             */
        }

        return configureResponseBuilderForJson(apiResult, responseBuilder).build();
    }

    /**
     * Function checks the path info extention. If extention is empty or .json an JSON-Response is
     * returns. If the extention is .html an HTML Content is the result.
     *
     * @param <T>
     *            type of resource
     * @param apiResult
     *            {@link ApiResult} with elements to serialize
     * @param responseBuilder
     *            {@link ResponseBuilder}
     * @param request
     *            {@link Request}
     * @return {@link Response}
     * @throws ResponseBuildException
     *             exception while building the response
     * @throws ExtensionNotSupportedException
     *             extension is not supported
     */
    private static <T> Response buildResponseByExtension(ApiResult<T> apiResult,
            ResponseBuilder responseBuilder, Request request) throws ResponseBuildException,
            ExtensionNotSupportedException {
        return buildResponseByExtension(apiResult, responseBuilder,
                RequestHelper.getHttpServletRequest(request));

    }

    /**
     * Build the success response
     *
     * @param <T>
     *            type of resource
     * @param result
     *            resource witch should result
     * @return {@link ApiResult}
     */
    private static <T> ApiResult<List<T>> buildSuccessApiResult(List<T> result) {
        ApiResult<List<T>> apiResult = new ApiResult<List<T>>();
        apiResult.setResult(result);
        apiResult.setStatus(ApiResult.ResultStatus.OK.toString());
        return apiResult;
    }

    /**
     * Build the success response
     *
     * @param <T>
     *            type of resource
     * @param result
     *            resource witch should result
     * @return {@link ApiResult}
     */
    private static <T> ApiResult<T> buildSuccessApiResult(T result) {
        ApiResult<T> apiResult = new ApiResult<T>();
        apiResult.setResult(result);
        apiResult.setStatus(ApiResult.ResultStatus.OK.toString());
        return apiResult;
    }

    /**
     * Get the success response from resource list
     *
     * @param <T>
     *            type of resource
     * @param result
     *            list of resources
     * @param request
     *            {@link Request}
     * @return {@link Response}
     * @throws ResponseBuildException
     *             exception while building the response
     * @throws ExtensionNotSupportedException
     *             extension is not supported
     */
    public static <T> Response buildSuccessResponse(List<T> result, Request request)
            throws ResponseBuildException, ExtensionNotSupportedException {
        return buildResponseByExtension(buildSuccessApiResult(result),
                Response.status(Response.Status.OK), request);
    }

    /**
     * Get the success response from resource list with specific meta data
     *
     * @param <T>
     *            type of resource
     * @param result
     *            list of resources
     * @param request
     *            {@link Request}
     * @param metaData
     *            map with meta data to extend the result
     * @return {@link Response}
     * @throws ResponseBuildException
     *             exception while building the response
     * @throws ExtensionNotSupportedException
     *             extension is not supported
     */
    public static <T> Response buildSuccessResponse(List<T> result, Request request,
            Map<String, Object> metaData) throws ResponseBuildException,
            ExtensionNotSupportedException {
        ApiResult<List<T>> apiResult = buildSuccessApiResult(result);
        apiResult.setMetaData(metaData);
        return buildResponseByExtension(apiResult, Response.status(Response.Status.OK), request);
    }

    /**
     * Get the success response from resource
     *
     * @param <T>
     *            type of resource
     * @param result
     *            resource
     * @param request
     *            {@link Request}
     * @return {@link Response}
     * @throws ResponseBuildException
     *             exception while building the response
     * @throws ExtensionNotSupportedException
     *             extension is not supported
     */
    public static <T> Response buildSuccessResponse(T result, Request request)
            throws ResponseBuildException, ExtensionNotSupportedException {
        return buildResponseByExtension(buildSuccessApiResult(result),
                Response.status(Response.Status.OK), request);
    }

    /**
     * Get the success response from resource
     *
     * @param <T>
     *            type of resource
     * @param result
     *            resource
     * @param messageKey
     *            key of message
     * @param request
     *            {@link Request}
     * @param arguments
     *            elements witch should replace in message text
     * @return {@link Response}
     * @throws ResponseBuildException
     *             exception while building the response
     * @throws ExtensionNotSupportedException
     *             extension is not supported
     */
    public static <T> Response buildSuccessResponse(T result, Request request, String messageKey,
            Object... arguments) throws ResponseBuildException, ExtensionNotSupportedException {
        return buildSuccessResponse(result, getText(request, messageKey, arguments), request);
    }

    /**
     * Get the success response from resource
     *
     * @param <T>
     *            type of resource
     * @param result
     *            resource
     * @param message
     *            message setted in the request
     * @param request
     *            {@link Request}
     * @return {@link Response}
     * @throws ResponseBuildException
     *             exception while building the response
     * @throws ExtensionNotSupportedException
     *             extension is not supported
     */
    public static <T> Response buildSuccessResponse(T result, String message, Request request)
            throws ResponseBuildException, ExtensionNotSupportedException {
        return buildResponse(result, message, request, ResultStatus.OK);
    }

    /**
     * Configures the response builder for html
     *
     * @param <T>
     *            type of resource
     * @param apiResult
     *            {@link ApiResult} with elements to serialize
     * @param responseBuilder
     *            {@link ResponseBuilder}
     * @return configured {@link ResponseBuilder}
     * @throws ResponseBuildException
     *             exception while building the response
     */
    private static <T> ResponseBuilder configureResponseBuilderForHtml(ApiResult<T> apiResult,
            ResponseBuilder responseBuilder) throws ResponseBuildException {
        responseBuilder.entity(HTML_RESPONSE_CONTAINER.replaceAll("#jsonResponse#",
                serializeToJson(apiResult)));
        responseBuilder.type(TYPE_TEXT_HTML);
        return responseBuilder;
    }

    /**
     * Maps response to JSON.
     *
     * @param <T>
     *            the content of the ApiResult
     * @param apiResult
     *            the apiResult to be mapped to JSON format
     * @param responseBuilder
     *            the response builder with detailed HTTP status information
     * @return response builder
     * @throws ResponseBuildException
     *             exception while building the response
     */
    private static <T> ResponseBuilder configureResponseBuilderForJson(ApiResult<T> apiResult,
            ResponseBuilder responseBuilder) throws ResponseBuildException {
        responseBuilder.entity(serializeToJson(apiResult));
        responseBuilder.type(TYPE_APPLICATION_JSON);
        return responseBuilder;
    }

    /**
     * Convert one of the {@link ErrorCodes} constants to a matching HTTP status code. In case there
     * is no match 500 will be returned.
     *
     * @param errorCode
     *            the code to convert
     * @return the HTTP status codes
     */
    public static final int getHttpCodeFromErrorCode(String errorCode) {
        Integer statusCode = ERROR_CODES_TO_HTTP_CODES.get(errorCode);
        if (statusCode == null) {
            statusCode = 500;
        }
        return statusCode;
    }

    /**
     * Get text of message key with consideration of locale and replace arguments
     *
     * @param <T>
     *            type of resource
     * @param request
     *            {@link Request}
     * @param messageKey
     *            key of message
     * @param arguments
     *            elements witch should replace in message text
     * @return text of messageKey
     */
    protected static <T> String getText(Request request, String messageKey, Object... arguments) {
        return ResourceBundleManager.instance().getText(messageKey,
                ResourceHandlerHelper.getCurrentUserLocale(request), arguments);
    }

    /**
     * Serialize ApiResult to json.
     *
     * @param <T>
     *            type of resource
     * @param apiResult
     *            the apiResult to be mapped to JSON format
     * @return serialized apiResult
     * @throws ResponseBuildException
     *             exception while building the response
     */
    private static <T> String serializeToJson(ApiResult<T> apiResult) throws ResponseBuildException {
        String jsonResponse;
        try {
            jsonResponse = MAPPER.writeValueAsString(apiResult);
        } catch (IOException e) {
            String message = "Exception while json serialisation of api result.";
            LOGGER.error(message);
            throw new ResponseBuildException(message);
        }
        return jsonResponse;
    }

    /**
     * returns response with the 400 (bad request) status code, caused by validation errors
     *
     * @param errors
     *            list of the validation errors
     * @param request
     *            {@link Request}
     * @return appropriate response
     * @throws ResponseBuildException
     *             exception while building the response
     * @throws ExtensionNotSupportedException
     *             extension is not supported
     */
    protected Response buildValidationErrorResult(List<ApiResultError> errors, Request request)
            throws ResponseBuildException, ExtensionNotSupportedException {
        ApiResult<List<ApiResultError>> apiResult = new ApiResult<List<ApiResultError>>();
        apiResult.setStatus(ResultStatus.ERROR.toString());
        apiResult.setErrors(errors);
        return buildResponseByExtension(apiResult, Response.status(Response.Status.BAD_REQUEST),
                request);
    }

}
