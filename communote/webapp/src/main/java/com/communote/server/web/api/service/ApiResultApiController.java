package com.communote.server.web.api.service;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.servlet.ModelAndView;

import com.communote.server.api.util.JsonHelper;
import com.communote.server.web.api.to.ApiResult;

/**
 * ApiResultApiController is the base class for all API calls returning a {@link ApiResult}
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * @deprecated Use new generated REST-API instead.
 */
@Deprecated
public abstract class ApiResultApiController extends BaseApiController {
    private static final String UTF_8 = "UTF-8";
    private static final String CONTENT_TYPE_JSON = "text/x-json";
    private final static Logger LOG = Logger.getLogger(ApiResultApiController.class);

    /**
     * Convert the result into the request format, e.g. json
     * 
     * @param request
     *            the http request
     * @param response
     *            the http response
     * @param result
     *            the result object to be returned to the response
     * @throws Exception
     *             in case of an error
     */
    protected void convert(HttpServletRequest request, HttpServletResponse response,
            ApiResult result) throws Exception {
        // convert result and write to response
        if (StringUtils.substringBefore(request.getRequestURI(), ";").endsWith(".json")) {
            String content = JsonHelper.getSharedObjectMapper().writeValueAsString(result);
            /*
             * JSONObject jsonObject = new JSONObject(result, includeSuperClass); String content =
             * jsonObject.toString();
             */

            response.setContentLength(content.getBytes(UTF_8).length);
            response.setContentType(CONTENT_TYPE_JSON);

            response.getWriter().write(content);
        } else {
            throw new RuntimeException("Illegal request for uri : " + request.getRequestURI()
                    + ". Uri has to end with \".json\".");
        }
    }

    /**
     * This methods executes the main API action and returns the object to be sent back.
     * 
     * @param apiResult
     *            the apiResult
     * @param request
     *            the http request
     * @param response
     *            the http resptonse
     * @return the result object
     * @throws ApiException
     *             in case of an illegal use of the api (wrong parameters, invalid resources)
     * @throws ServletException
     *             in case of an request error
     */
    protected abstract Object execute(ApiResult apiResult, HttpServletRequest request,
            HttpServletResponse response) throws ServletException, ApiException;

    /**
     * @param request
     *            the http request
     * @return the request uri without '.json'
     */
    protected String getRequestUriName(HttpServletRequest request) {
        String normalizedRequestUri = StringUtils.substringBefore(request.getRequestURI(), ";");
        if (normalizedRequestUri.endsWith(".json")) {
            return normalizedRequestUri.substring(0, normalizedRequestUri.length() - 5);
        }
        throw new RuntimeException("Illegal request for uri: " + request.getRequestURI());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        try {
            ApiResult apiResult = new ApiResult();
            apiResult.setStatus(ApiResult.ResultStatus.OK.name());

            Object resultObject = execute(apiResult, request, response);

            apiResult.setResult(resultObject);
            convert(request, response, apiResult);

        } catch (AuthenticationException e) {
            handleUnauthorized(request, response, e);
        } catch (AccessDeniedException e) {

            handleUnauthorized(request, response, e);
        } catch (ApiException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Error executing API request for " + request.getRequestURI() + "! "
                        + e.getMessage(), e);
            }
            ApiResult apiResult = new ApiResult();
            apiResult.setStatus(ApiResult.ResultStatus.ERROR.name());
            apiResult.setMessage(e.getMessage());
            convert(request, response, apiResult);
        } catch (Throwable e) {
            LOG.error("Error executing API request for " + request.getRequestURI() + "! "
                    + e.getMessage(), e);
            ApiResult apiResult = new ApiResult();
            apiResult.setStatus(ApiResult.ResultStatus.ERROR.name());
            // TODO i18n message
            apiResult.setMessage("Server error");
            convert(request, response, apiResult);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }

        return null;
    }

    /**
     * Send an api result and a status code reflecting a denied access or authorization exception
     * 
     * @param request
     *            the request
     * @param response
     *            the response
     * @param e
     *            the message
     * @throws Exception
     *             the exception
     */
    private void handleUnauthorized(HttpServletRequest request, HttpServletResponse response,
            Exception e) throws Exception {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Error executing API request for " + request.getRequestURI() + "! "
                    + e.getMessage(), e);
        }
        ApiResult apiResult = new ApiResult();
        apiResult.setStatus(ApiResult.ResultStatus.ERROR.name());
        apiResult.setMessage(e.getMessage());
        convert(request, response, apiResult);

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }

}
