package com.communote.plugins.api.rest.v30.resource.tag;

import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.communote.plugins.api.rest.v30.resource.DefaultParameter;
import com.communote.plugins.api.rest.v30.resource.DefaultResourceHandler;
import com.communote.plugins.api.rest.v30.resource.tagstoretaglist.tagstoretag.GetCollectionTagStoreTagParameter;
import com.communote.plugins.api.rest.v30.resource.tagstoretaglist.tagstoretag.GetTagStoreTagParameter;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class TagStoreTagResourceHandler
        extends
        DefaultResourceHandler<DefaultParameter, DefaultParameter, DefaultParameter,
        GetTagStoreTagParameter, GetCollectionTagStoreTagParameter> {

    /**
     * Get a tagstore tag
     * 
     * @param getTagStoreTagParameter
     *            - an object that contains all the parameter that can or have to be used for such a
     *            request
     * @param requestedMimeType
     *            - is the mime type that indicates which data exchange format to use
     * @param uriInfo
     *            - this object is created by the request and contains some request specific data
     * @param requestSessionId
     *            - The sessionId for the session
     * @param request
     *            - javax request
     * @return an response object containing a http status code and a message
     * @throws Exception
     *             tag exceptions
     */
    @Override
    public Response handleGetInternally(GetTagStoreTagParameter getTagStoreTagParameter,
            String requestedMimeType, UriInfo uriInfo, String requestSessionId, Request request)
            throws Exception {
        // not implemented
        return null;
    }

    /**
     * Get a tagstore taglist
     * 
     * @param getCollectionTagStoreTagParameter
     *            - an object that contains all the parameter that can or have to be used for such a
     *            request
     * @param requestedMimeType
     *            - is the mime type that indicates which data exchange format to use
     * @param uriInfo
     *            - this object is created by the request and contains some request specific data
     * @param requestSessionId
     *            - The sessionId for the session
     * @param request
     *            - javax request
     * @return an response object containing a http status code and a message
     * @throws Exception
     *             tag exceptions
     */
    @Override
    public Response handleListInternally(
            GetCollectionTagStoreTagParameter getCollectionTagStoreTagParameter,
            String requestedMimeType, UriInfo uriInfo, String requestSessionId, Request request)
            throws Exception {
        // not implemented
        return null;
    }

}
