package com.communote.plugins.api.rest.v30.resource.topic;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.communote.common.util.PageableList;
import com.communote.plugins.api.rest.v30.exception.ExtensionNotSupportedException;
import com.communote.plugins.api.rest.v30.exception.ResponseBuildException;
import com.communote.plugins.api.rest.v30.resource.DefaultResourceHandler;
import com.communote.plugins.api.rest.v30.resource.ResourceHandlerHelper;
import com.communote.plugins.api.rest.v30.resource.topic.externalobject.ExternalObjectResourceHelper;
import com.communote.plugins.api.rest.v30.resource.topic.property.PropertyResourceHelper;
import com.communote.plugins.api.rest.v30.resource.topic.role.RoleResourceHelper;
import com.communote.plugins.api.rest.v30.resource.topic.roleexternal.RoleExternalResourceHelper;
import com.communote.plugins.api.rest.v30.response.ResponseHelper;
import com.communote.plugins.api.rest.v30.service.IllegalRequestParameterException;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.blog.BlogAccessException;
import com.communote.server.api.core.blog.BlogData;
import com.communote.server.api.core.blog.BlogManagement;
import com.communote.server.api.core.blog.BlogNotFoundException;
import com.communote.server.api.core.blog.BlogValidationException;
import com.communote.server.api.core.blog.NoBlogManagerLeftException;
import com.communote.server.api.core.common.NotFoundException;
import com.communote.server.api.core.external.ExternalObjectNotAssignedException;
import com.communote.server.api.core.note.NoteManagementAuthorizationException;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.core.exception.ErrorCodes;
import com.communote.server.core.exception.Status;
import com.communote.server.core.external.ExternalObjectAlreadyAssignedException;
import com.communote.server.core.external.ExternalSystemNotConfiguredException;
import com.communote.server.core.external.TooManyExternalObjectsPerTopicException;
import com.communote.server.core.filter.listitems.blog.BlogTagListItem;
import com.communote.server.core.query.QueryManagement;
import com.communote.server.core.vo.query.blog.BlogQuery;
import com.communote.server.core.vo.query.blog.BlogQueryParameters;
import com.communote.server.core.vo.query.converters.BlogDataToBlogTagListItemQueryResultConverter;
import com.communote.server.core.vo.query.converters.BlogToBlogTagListItemQueryResultConverter;
import com.communote.server.model.blog.Blog;

/**
 * Handler for {@link TopicResource}
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class TopicResourceHandler
extends
DefaultResourceHandler<CreateTopicParameter, EditTopicParameter, DeleteTopicParameter, GetTopicParameter, GetCollectionTopicParameter> {

    /**
     * Set the validator for the resource handler
     */
    public TopicResourceHandler() {
        super(new TopicResourceValidator());
    }

    /**
     * Returns the {@link BlogManagement}.
     *
     * @return Returns the {@link BlogManagement}.
     */
    private BlogManagement getTopicManagement() {
        return ServiceLocator.instance().getService(BlogManagement.class);
    }

    /**
     * Get the list of topics for the specified {@link BlogQuery}
     *
     * @param blogQueryParameters
     *            {@link BlogQueryParameters}
     * @param request
     *            {@link Request}
     * @return pageable list of {@link BlogData}
     * @throws IllegalRequestParameterException
     *             if request parameter is empty
     */
    private PageableList<BlogTagListItem> getTopics(BlogQueryParameters blogQueryParameters,
            Request request) throws IllegalRequestParameterException {
        return ServiceLocator.findService(QueryManagement.class).query(
                BlogQuery.DEFAULT_QUERY,
                blogQueryParameters,
                new BlogDataToBlogTagListItemQueryResultConverter(ResourceHandlerHelper
                        .getCurrentUserLocale(request)));
    }

    /**
     * Retrieves the last or most used topics.
     *
     * @param mostUsed
     *            if true the most used topics will be fetched otherwise the last used
     * @param limit
     *            the limit of items to retrieve
     * @param blogListItemToBlogTagListItemConverter
     *            {@link BlogDataToBlogTagListItemQueryResultConverter}
     * @return the topic as topic resource objects
     */
    private List<TopicResource> getUsedBlogs(boolean mostUsed, int limit,
            BlogDataToBlogTagListItemQueryResultConverter blogListItemToBlogTagListItemConverter) {
        List<BlogData> items;
        if (mostUsed) {
            items = getTopicManagement().getMostUsedBlogs(limit, true);
        } else {
            items = getTopicManagement().getLastUsedBlogs(limit, true);
        }
        List<TopicResource> resources = new ArrayList<TopicResource>(items.size());
        for (BlogData item : items) {
            TopicResource topicResource = new TopicResource();
            TopicResourceHelper.CONVERTER.convert(
                    blogListItemToBlogTagListItemConverter.convert(item), topicResource);
            resources.add(topicResource);
        }
        return resources;
    }

    /**
     * Request to create a topic
     *
     * @param createTopicParameter
     *            - an object that contains all the parameter that can or have to be used for such a
     *            request
     * @param requestedMimeType
     *            - is the mime type that indicates which data exchange format to use
     * @param uriInfo
     *            - this object is created by the request and contains some request specific data
     * @param sessionId
     *            - The sessionId for the session
     * @param request
     *            - javax request
     * @return an response object containing a http status code and a message
     * @throws BlogValidationException
     *             parameter to get blog are not valid
     * @throws AuthorizationException
     *             user is not authorizied.
     * @throws NotFoundException
     *             can not found element.
     * @throws ResponseBuildException
     *             exception while building the response
     * @throws ExtensionNotSupportedException
     *             extension is not supported
     * @throws NoBlogManagerLeftException
     *             cannot add role because no topic manager exists after create of topic
     * @throws ExternalObjectAlreadyAssignedException
     *             in case the topic is already assigned
     * @throws ExternalSystemNotConfiguredException
     * @throws TooManyExternalObjectsPerTopicException
     * @throws ExternalObjectNotAssignedException
     */
    @Override
    public Response handleCreateInternally(CreateTopicParameter createTopicParameter,
            String requestedMimeType, UriInfo uriInfo, String sessionId, Request request)
                    throws BlogValidationException, NotFoundException, AuthorizationException,
                    ResponseBuildException, ExtensionNotSupportedException, NoBlogManagerLeftException,
            ExternalObjectAlreadyAssignedException, TooManyExternalObjectsPerTopicException,
            ExternalSystemNotConfiguredException, ExternalObjectNotAssignedException {
        Blog topic = getTopicManagement().createBlog(
                TopicResourceHelper.getBlogTO(createTopicParameter));
        PropertyResourceHelper.setProperties(topic.getId(), createTopicParameter.getProperties());

        RoleResourceHelper.assignRoleResources(createTopicParameter.getRoles(), topic.getId());

        ExternalObjectResourceHelper.addOrUpdateExternalObjectResources(
                createTopicParameter.getExternalObjects(), topic.getId(), false);

        RoleExternalResourceHelper.assignRoleExternalResources(
                createTopicParameter.getRoleExternals(), topic.getId());

        return ResponseHelper.buildSuccessResponse(topic.getId(), request, "blog.create.success");
    }

    /**
     * Request to delete a topic
     *
     * @param deleteTopicParameter
     *            - an object that contains all the parameter that can or have to be used for such a
     *            request
     * @param requestedMimeType
     *            - is the mime type that indicates which data exchange format to use
     * @param uriInfo
     *            - this object is created by the request and contains some request specific data
     * @param sessionId
     *            - The sessionId for the session
     * @param request
     *            - javax request
     * @return an response object containing a http status code and a message
     * @throws BlogNotFoundException
     *             Exception
     * @throws NoteManagementAuthorizationException
     *             Exception
     * @throws ResponseBuildException
     *             exception while building the response
     * @throws ExtensionNotSupportedException
     *             extension is not supported
     * @throws BlogAccessException
     *             in case the current user has no access to the topic
     */
    @Override
    public Response handleDeleteInternally(DeleteTopicParameter deleteTopicParameter,
            String requestedMimeType, UriInfo uriInfo, String sessionId, Request request)
                    throws BlogNotFoundException, NoteManagementAuthorizationException,
            ResponseBuildException, ExtensionNotSupportedException, BlogAccessException {
        Long topicId = TopicResourceHelper.getTopicIdByIdentifier(deleteTopicParameter
                .getTopicIdentifier() != null ? deleteTopicParameter.getTopicIdentifier().name()
                        : null, deleteTopicParameter.getTopicId(), getTopicManagement());
        getTopicManagement().deleteBlog(topicId, null);
        return ResponseHelper.buildSuccessResponse(null, request, "blog.management.delete.success");
    }

    /**
     * Request to edit a topic
     *
     * @param editTopicParameter
     *            - an object that contains all the parameter that can or have to be used for such a
     *            request
     * @param requestedMimeType
     *            - is the mime type that indicates which data exchange format to use
     * @param uriInfo
     *            - this object is created by the request and contains some request specific data
     * @param sessionId
     *            - The sessionId for the session
     * @param request
     *            - javax request
     * @return an response object containing a http status code and a message
     * @throws BlogValidationException
     *             topic input parameter are not valid
     * @throws NotFoundException
     *             topic is not found
     * @throws AuthorizationException
     *             user is not authorized
     * @throws BlogNotFoundException
     *             can not found topic
     * @throws ResponseBuildException
     *             exception while building the response
     * @throws ExtensionNotSupportedException
     *             extension is not supported
     * @throws NoBlogManagerLeftException
     *             cannot change role because no topic manager exists after update of topic
     * @throws ExternalObjectAlreadyAssignedException
     *             in case the topic is already assigned
     * @throws ExternalSystemNotConfiguredException
     * @throws TooManyExternalObjectsPerTopicException
     * @throws ExternalObjectNotAssignedException
     */
    @Override
    public Response handleEditInternally(EditTopicParameter editTopicParameter,
            String requestedMimeType, UriInfo uriInfo, String sessionId, Request request)
                    throws BlogValidationException, AuthorizationException, NotFoundException,
            BlogNotFoundException, ResponseBuildException, ExtensionNotSupportedException,
            NoBlogManagerLeftException, ExternalObjectAlreadyAssignedException,
            TooManyExternalObjectsPerTopicException, ExternalSystemNotConfiguredException,
            ExternalObjectNotAssignedException {
        // TODO kind of inefficient since this helper already fetches the blog from the BE if the
        // alias is set (can't be refactored easily: see handleGet method)
        Long topicId = TopicResourceHelper.getTopicIdByIdentifier(editTopicParameter
                .getTopicIdentifier() != null ? editTopicParameter.getTopicIdentifier().name()
                : null, editTopicParameter.getTopicId(), getTopicManagement());
        Blog topic = getTopicManagement().getBlogById(topicId, false);
        if (topic == null) {
            throw new BlogNotFoundException("Topic not found", topicId, null);
        }
        topic = getTopicManagement().updateBlog(topic.getId(),
                TopicResourceHelper.getBlogTO(editTopicParameter, topic));

        PropertyResourceHelper.setProperties(topic.getId(), editTopicParameter.getProperties());

        RoleResourceHelper.assignRoleResources(editTopicParameter.getRoles(), topic.getId());

        ExternalObjectResourceHelper.addOrUpdateExternalObjectResources(
                editTopicParameter.getExternalObjects(), topic.getId(), false);

        RoleExternalResourceHelper.assignRoleExternalResources(
                editTopicParameter.getRoleExternals(), topic.getId());

        return ResponseHelper.buildSuccessResponse(topic.getId(), request, "blog.update.success");
    }

    /**
     * Retrieve a single topic from the server
     *
     * @param getTopicParameter
     *            - an object that contains all the parameter that can or have to be used for such a
     *            request
     * @param requestedMimeType
     *            - is the mime type that indicates which data exchange format to use
     * @param uriInfo
     *            - this object is created by the request and contains some request specific data
     * @param sessionId
     *            - The sessionId for the session
     * @param request
     *            - javax request
     * @return an response object containing a http status code and a message
     * @throws BlogNotFoundException
     *             blog was not found
     * @throws ResponseBuildException
     *             exception while building the response
     * @throws ExtensionNotSupportedException
     *             extension is not supported
     * @throws BlogAccessException
     *             in case the current user has no access to the topic
     */
    @Override
    public Response handleGetInternally(GetTopicParameter getTopicParameter,
            String requestedMimeType, UriInfo uriInfo, String sessionId, Request request)
                    throws BlogNotFoundException, ResponseBuildException, ExtensionNotSupportedException,
                    BlogAccessException {
        Long topicId = TopicResourceHelper.getTopicIdByIdentifier(getTopicParameter
                .getTopicIdentifier() != null ? getTopicParameter.getTopicIdentifier().name()
                        : null, getTopicParameter.getTopicId(), getTopicManagement());
        try {
            BlogTagListItem blogListItem = getTopicManagement().getBlogById(
                    topicId,
                    new BlogToBlogTagListItemQueryResultConverter(ResourceHandlerHelper
                            .getCurrentUserLocale(request)));
            if (blogListItem == null) {
                throw new BlogNotFoundException("topic with id " + topicId + " does not exist.",
                        topicId, null);
            }
            TopicResource topicResource = new TopicResource();
            TopicResourceHelper.CONVERTER.fillingResultItem(blogListItem, topicResource);
            return ResponseHelper.buildSuccessResponse(topicResource, request);
        } catch (BlogAccessException e) {
            // use custom exception handling to get a more useful message
            return ResponseHelper.buildErrorResponse(new Status("common.error.topic.no.access",
                    ErrorCodes.FORBIDDEN), request);
        }
    }

    /**
     * Get an collection of topics
     *
     * @param getCollectionTopicParameter
     *            - an object that contains all the parameter that can or have to be used for such a
     *            request
     * @param requestedMimeType
     *            - is the mime type that indicates which data exchange format to use
     * @param uriInfo
     *            - this object is created by the request and contains some request specific data
     * @param sessionId
     *            - The sessionId for the session
     * @param request
     *            - javax request
     * @return an response object containing a http status code and a message
     * @throws IllegalRequestParameterException
     *             request parameter is not valid
     * @throws ResponseBuildException
     *             exception while building the response
     * @throws ExtensionNotSupportedException
     *             extension is not supported
     */
    @Override
    public Response handleListInternally(GetCollectionTopicParameter getCollectionTopicParameter,
            String requestedMimeType, UriInfo uriInfo, String sessionId, Request request)
                    throws IllegalRequestParameterException, ResponseBuildException,
                    ExtensionNotSupportedException {
        if (getCollectionTopicParameter.getTopicListType() == null) {
            throw new IllegalRequestParameterException("topicListType", "null", "Invalid value.");
        }
        List<TopicResource> topicResources = null;
        boolean mostUsed = getCollectionTopicParameter.getTopicListType().equals(
                ETopicListType.MOST_USED);
        if (mostUsed
                || getCollectionTopicParameter.getTopicListType().equals(ETopicListType.LAST_USED)) {
            topicResources = getUsedBlogs(
                    mostUsed,
                    10,
                    new BlogDataToBlogTagListItemQueryResultConverter(ResourceHandlerHelper
                            .getCurrentUserLocale(request)));
            return ResponseHelper.buildSuccessResponse(topicResources, request);
        } else {
            PageableList<TopicResource> blogResourceList = TopicResourceHelper.CONVERTER
                    .convert(getTopics(TopicResourceHelper.configureQueryInstance(
                            getCollectionTopicParameter,
                            ResourceHandlerHelper.getNameProvider(request)), request));
            Map<String, Object> metaData;
            metaData = ResourceHandlerHelper.generateMetaDataForPaging(
                    getCollectionTopicParameter.getOffset(),
                    getCollectionTopicParameter.getMaxCount(),
                    blogResourceList.getMinNumberOfElements());
            topicResources = new ArrayList<TopicResource>();
            topicResources.addAll(blogResourceList);
            return ResponseHelper.buildSuccessResponse(topicResources, request, metaData);
        }

    }

}
