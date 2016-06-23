package com.communote.plugins.api.rest.v24.resource.topic.follow;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.communote.plugins.api.rest.v24.exception.ExtensionNotSupportedException;
import com.communote.plugins.api.rest.v24.exception.ResponseBuildException;
import com.communote.plugins.api.rest.v24.resource.DefaultParameter;
import com.communote.plugins.api.rest.v24.resource.DefaultResourceHandler;
import com.communote.plugins.api.rest.v24.response.ResponseHelper;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.blog.BlogAccessException;
import com.communote.server.api.core.common.NotFoundException;
import com.communote.server.core.follow.FollowManagement;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class FollowResourceHandler
        extends
        DefaultResourceHandler<CreateFollowParameter, DefaultParameter, DeleteFollowParameter,
        DefaultParameter, GetCollectionFollowParameter> {

    /**
     * Assign the follow of blog to {@link FollowManagement}
     * 
     * @param blogId
     *            identifier of blog
     * @param follow
     *            or unfollow
     * @throws NotFoundException
     *             blog not found
     * @throws BlogAccessException
     *             blog can not access by user
     */
    private void assignFollow(Long blogId, Boolean follow) throws BlogAccessException,
            NotFoundException {
        if (follow) {
            getFollowManagement().followBlog(blogId);
        } else {
            getFollowManagement().unfollowBlog(blogId);
        }
    }

    /**
     * Returns the {@link FollowManagement}.
     * 
     * @return Returns the {@link FollowManagement}.
     */
    private FollowManagement getFollowManagement() {
        return ServiceLocator.instance().getService(FollowManagement.class);
    }

    /**
     * Request to set blog follow
     * 
     * @param createFollowParameter
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
     * @return null
     * @throws NotFoundException
     *             blog not found
     * @throws BlogAccessException
     *             blog can not access by user
     * @throws NumberFormatException
     *             blog id is wrong
     * @throws ResponseBuildException
     *             exception while building the response
     * @throws ExtensionNotSupportedException
     *             extension is not supported
     */
    @Override
    public Response handleCreateInternally(CreateFollowParameter createFollowParameter,
            String requestedMimeType, UriInfo uriInfo, String requestSessionId, Request request)
            throws NumberFormatException, BlogAccessException, NotFoundException,
            ResponseBuildException, ExtensionNotSupportedException {
        assignFollow(Long.parseLong(createFollowParameter.getTopicId()),
                createFollowParameter.getFollow());
        return ResponseHelper.buildSuccessResponse(null, request);
    }

    /**
     * Request to delete follow of blog
     * 
     * @param deleteFollowParameter
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
     * @return null
     * @throws NotFoundException
     *             blog not found
     * @throws BlogAccessException
     *             blog can not access by user
     * @throws NumberFormatException
     *             blog id is wrong
     * @throws ResponseBuildException
     *             exception while building the response
     * @throws ExtensionNotSupportedException
     *             extension is not supported
     */
    @Override
    public Response handleDeleteInternally(DeleteFollowParameter deleteFollowParameter,
            String requestedMimeType, UriInfo uriInfo, String requestSessionId, Request request)
            throws NumberFormatException, BlogAccessException, NotFoundException,
            ResponseBuildException, ExtensionNotSupportedException {
        assignFollow(Long.parseLong(deleteFollowParameter.getTopicId()), false);
        return ResponseHelper.buildSuccessResponse(null, request);
    }

    /**
     * Request to get follow of blog
     * 
     * @param getCollectionFollowParameter
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
     * @return {@link FollowResource}
     * @throws ResponseBuildException
     *             exception while building the response
     * @throws ExtensionNotSupportedException
     *             extension is not supported
     */
    @Override
    public Response handleListInternally(GetCollectionFollowParameter getCollectionFollowParameter,
            String requestedMimeType, UriInfo uriInfo, String requestSessionId, Request request)
            throws ResponseBuildException, ExtensionNotSupportedException {
        FollowResource followResource = new FollowResource();
        followResource.setFollow(getFollowManagement().followsBlog(
                Long.parseLong(getCollectionFollowParameter.getTopicId())));
        List<FollowResource> followResources = new ArrayList<FollowResource>();
        followResources.add(followResource);
        return ResponseHelper.buildSuccessResponse(followResources, request);
    }

}
