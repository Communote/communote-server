package com.communote.server.web.api.service;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.web.HttpRequestMethodNotSupportedException;

import com.communote.server.web.api.to.ApiResult;

/**
 * Base controller for api rest calls
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * @deprecated Use new generated REST-API instead
 * @param <GET>
 *            Return type of doGet
 * @param <POST>
 *            Return type of doPost
 */
@Deprecated
public class BaseRestApiController<GET, POST> extends ApiResultApiController {

    /**
     * Do the get on a resource
     * 
     * @param apiResult
     *            the apiResult
     * @param request
     *            the request
     * @param response
     *            the response
     * @return the post resource
     * @throws ApiException
     *             an error occured
     */
    protected GET doGet(ApiResult apiResult, HttpServletRequest request,
            HttpServletResponse response) throws ApiException {
        throw new ApiException("Request Method '" + request.getMethod() + "'not supported");
    }

    /**
     * Do a a post resource
     * 
     * @param apiResult
     *            the apiResult
     * @param request
     *            the request
     * @param response
     *            the response
     * @return null
     * @throws HttpRequestMethodNotSupportedException
     *             if method not implemented
     * @throws ApiException
     *             in case of an error
     * @throws Exception
     */
    protected POST doPost(ApiResult apiResult, HttpServletRequest request,
            HttpServletResponse response) throws HttpRequestMethodNotSupportedException,
            ApiException {
        throw new ApiException("Request Method '" + request.getMethod() + "'not supported");
    }

    /**
     * {@inheritDoc}
     * 
     * @throws RequestedResourceNotFoundException
     * @throws HttpRequestMethodNotSupportedException
     */
    @Override
    protected Object execute(ApiResult apiResult, HttpServletRequest request,
            HttpServletResponse response) throws ApiException, ServletException {

        if (StringUtils.equalsIgnoreCase("GET", request.getMethod())) {
            return doGet(apiResult, request, response);
        } else if (StringUtils.equalsIgnoreCase("POST", request.getMethod())) {
            return doPost(apiResult, request, response);
        }
        throw new ApiException("Request Method '" + request.getMethod() + "'not supported");

    }
}
