package com.communote.plugins.api.rest.resource.topic.rolebulkexternal;

import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.communote.plugins.api.rest.exception.ExtensionNotSupportedException;
import com.communote.plugins.api.rest.exception.ResponseBuildException;
import com.communote.plugins.api.rest.request.RequestHelper;
import com.communote.plugins.api.rest.resource.DefaultParameter;
import com.communote.plugins.api.rest.resource.DefaultResourceHandler;
import com.communote.plugins.api.rest.response.ResponseHelper;
import com.communote.plugins.api.rest.service.IllegalRequestParameterException;
import com.communote.plugins.api.rest.to.ApiResult;
import com.communote.server.api.core.blog.BlogNotFoundException;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class RoleBulkExternalResourceHandler
        extends
        DefaultResourceHandler<CreateRoleBulkExternalParameter, DefaultParameter, DefaultParameter,
        GetRoleBulkExternalParameter, DefaultParameter> {

    /**
     * Get blog role bulk for external
     * 
     * @param createRoleBulkExternalParameter
     *            - an object that contains all the parameter that can or have to be used for such a
     *            request
     * @param requestedMimeType
     *            - is the mime type that indicates which data exchange format to use
     * @param uriInfo
     *            - this object is created by the request and contains some request specific data
     * @param requestSessionId
     *            sessionId
     * @param request
     *            - javax request
     * @return an response object containing a http status code and a message
     * @throws BlogNotFoundException
     *             Exception.
     * @throws IllegalRequestParameterException
     *             illegal parameter in request
     * @throws ResponseBuildException
     *             exception while building the response
     * @throws ExtensionNotSupportedException
     *             extension is not supported
     */
    @Override
    public Response handleCreateInternally(
            CreateRoleBulkExternalParameter createRoleBulkExternalParameter,
            String requestedMimeType, UriInfo uriInfo, String requestSessionId, Request request)
            throws BlogNotFoundException, IllegalRequestParameterException, ResponseBuildException,
            ExtensionNotSupportedException {
        // the 1.3 API does not ensure that a topic is assigned to an external object, thus we can't
        // support this any longer
        ApiResult<Object> apiResult = new ApiResult<Object>();
        apiResult.setMessage("not supported anymore");
        return ResponseHelper.buildErrorResponse(apiResult,
                Response.status(Response.Status.FORBIDDEN),
                RequestHelper.getHttpServletRequest(request));
    }

    /**
     * Get blog role bulk for external
     * 
     * @param getRoleBulkExternalParameter
     *            - an object that contains all the parameter that can or have to be used for such a
     *            request
     * @param requestedMimeType
     *            - is the mime type that indicates which data exchange format to use
     * @param uriInfo
     *            - this object is created by the request and contains some request specific data
     * @param requestSessionId
     *            sessionId
     * @param request
     *            - javax request
     * @return null because it is not implemented
     */
    @Override
    public Response handleGetInternally(GetRoleBulkExternalParameter getRoleBulkExternalParameter,
            String requestedMimeType, UriInfo uriInfo, String requestSessionId, Request request) {
        // TODO Auto-generated method stub
        return null;
    }

}
