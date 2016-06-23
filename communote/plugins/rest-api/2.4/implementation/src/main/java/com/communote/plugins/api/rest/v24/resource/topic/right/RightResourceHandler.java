package com.communote.plugins.api.rest.v24.resource.topic.right;

import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang3.StringUtils;

import com.communote.plugins.api.rest.v24.exception.ExtensionNotSupportedException;
import com.communote.plugins.api.rest.v24.exception.ResponseBuildException;
import com.communote.plugins.api.rest.v24.resource.DefaultParameter;
import com.communote.plugins.api.rest.v24.resource.DefaultResourceHandler;
import com.communote.plugins.api.rest.v24.response.ResponseHelper;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.blog.BlogAccessException;
import com.communote.server.api.core.blog.BlogManagement;
import com.communote.server.api.core.blog.BlogNotFoundException;
import com.communote.server.api.core.blog.BlogRightsManagement;
import com.communote.server.core.converter.blog.BlogToDetailBlogListItemConverter;
import com.communote.server.core.filter.listitems.blog.DetailBlogListItem;
import com.communote.server.model.blog.Blog;

/**
 * Handler for {@link RightResource}
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class RightResourceHandler
extends
DefaultResourceHandler<DefaultParameter, EditRightParameter, DefaultParameter, GetRightParameter, DefaultParameter> {

    /**
     * Convert the user identifier from string seperated with comma to a long array
     *
     * @param uIds
     *            The user ids as string
     * @return An array of user ids in Long
     */
    private static Long[] convertUserIds(String uIds) {
        if (StringUtils.isNotBlank(uIds)) {
            String[] userIdsStr = uIds.split(",");
            Long[] userIds = new Long[userIdsStr.length];
            int i = 0;
            for (String userId : userIdsStr) {
                userIds[i++] = Long.parseLong(userId.trim());
            }
            return userIds;
        } else {
            return null;
        }
    }

    /**
     * Returns the {@link BlogManagement}.
     *
     * @return Returns the {@link BlogManagement}.
     */
    private BlogManagement getBlogManagement() {
        return ServiceLocator.instance().getService(BlogManagement.class);
    }

    /**
     * Returns the {@link BlogRightsManagement}.
     *
     * @return Returns the {@link BlogRightsManagement}.
     */
    private BlogRightsManagement getBlogRightsManagement() {
        return ServiceLocator.findService(BlogRightsManagement.class);
    }

    /**
     * Update access rights of blog.
     *
     * @param editRightParameter
     *            - an object that contains all the parameter that can or have to be used for such a
     *            request
     * @param requestedMimeType
     *            - is the mime type that indicates which data exchange format to use
     * @param uriInfo
     *            - this object is created by the request and contains some request specific data
     * @param requestSessionId
     *            - The requestSessionId for the session
     * @param request
     *            - javax request
     * @return an response object containing a http status code and a message
     * @throws BlogNotFoundException
     *             can not found blog
     * @throws BlogAccessException
     *             can not access blog
     * @throws ResponseBuildException
     *             exception while building the response
     * @throws ExtensionNotSupportedException
     *             extension is not supported
     */
    @Override
    public Response handleEditInternally(EditRightParameter editRightParameter,
            String requestedMimeType, UriInfo uriInfo, String requestSessionId, Request request)
                    throws BlogNotFoundException, BlogAccessException, ResponseBuildException,
                    ExtensionNotSupportedException {
        Long blogId = Long.parseLong(editRightParameter.getTopicId());
        Blog blog = getBlogManagement().getBlogById(blogId, false);
        if (blog == null) {
            throw new BlogNotFoundException(blogId);
        }

        boolean publicAccess = (editRightParameter.getPublicAccess() == null) ? false
                : editRightParameter.getPublicAccess();
        boolean allCanWrite = (editRightParameter.getAllCanWrite() == null) ? false
                : editRightParameter.getPublicAccess();
        boolean allCanRead = (editRightParameter.getAllCanRead() == null) ? false
                : editRightParameter.getPublicAccess();
        getBlogRightsManagement().changePublicAccess(blog.getId(), publicAccess);
        getBlogRightsManagement().setAllCanReadAllCanWrite(blog.getId(), allCanRead, allCanWrite);
        return ResponseHelper.buildSuccessResponse(null, request,
                "restapi.message.resource.blog.right.update");
    }

    /**
     * Get the access rights of blog.
     *
     * @param getRightParameter
     *            - an object that contains all the parameter that can or have to be used for such a
     *            request
     * @param requestedMimeType
     *            - is the mime type that indicates which data exchange format to use
     * @param uriInfo
     *            - this object is created by the request and contains some request specific data
     * @param requestSessionId
     *            - The requestSessionId for the session
     * @param request
     *            - javax request
     * @return an response object containing a http status code and a message
     * @throws BlogNotFoundException
     *             can not found blog
     * @throws ResponseBuildException
     *             exception while building the response
     * @throws ExtensionNotSupportedException
     *             extension is not supported
     * @throws BlogAccessException
     *             in case the current user has no access to the topic
     */
    @Override
    public Response handleGetInternally(GetRightParameter getRightParameter,
            String requestedMimeType, UriInfo uriInfo, String requestSessionId, Request request)
                    throws BlogNotFoundException, ResponseBuildException, ExtensionNotSupportedException,
                    BlogAccessException {

        Long topicId = Long.parseLong(getRightParameter.getTopicId());

        Blog blog = getBlogManagement().getBlogById(topicId, false);
        if (blog == null) {
            throw new BlogNotFoundException(topicId);
        }
        RightResource rightsResource = new RightResource();
        rightsResource.setAllCanRead(blog.isAllCanRead());
        rightsResource.setAllCanWrite(blog.isAllCanWrite());
        rightsResource.setPublicAccess(blog.isPublicAccess());
        BlogToDetailBlogListItemConverter<DetailBlogListItem> converter;
        converter = new BlogToDetailBlogListItemConverter<DetailBlogListItem>(
                DetailBlogListItem.class, false, false, true, false, false, null);
        DetailBlogListItem detailBlogListItem = getBlogManagement().getBlogById(topicId, converter);

        rightsResource.setManagingUserIds(convertUserIds(detailBlogListItem.getManagingUserIds()));
        rightsResource.setReadingUserIds(convertUserIds(detailBlogListItem.getReadingUserIds()));
        rightsResource.setWritingUserIds(convertUserIds(detailBlogListItem.getWritingUserIds()));

        return ResponseHelper.buildSuccessResponse(rightsResource, request);
    }
}
