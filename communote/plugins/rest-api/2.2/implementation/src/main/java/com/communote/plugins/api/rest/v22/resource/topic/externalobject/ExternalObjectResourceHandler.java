package com.communote.plugins.api.rest.v22.resource.topic.externalobject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang3.StringUtils;

import com.communote.plugins.api.rest.v22.converter.ExternalObjectToExternalObjectResourceConverter;
import com.communote.plugins.api.rest.v22.exception.ExtensionNotSupportedException;
import com.communote.plugins.api.rest.v22.exception.ResponseBuildException;
import com.communote.plugins.api.rest.v22.resource.DefaultResourceHandler;
import com.communote.plugins.api.rest.v22.resource.topic.TopicResourceHelper;
import com.communote.plugins.api.rest.v22.response.ResponseHelper;
import com.communote.plugins.api.rest.v22.service.IllegalRequestParameterException;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.blog.BlogAccessException;
import com.communote.server.api.core.blog.BlogManagement;
import com.communote.server.api.core.blog.BlogNotFoundException;
import com.communote.server.api.core.common.NotFoundException;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.core.external.ExternalObjectAlreadyAssignedException;
import com.communote.server.core.external.ExternalObjectManagement;
import com.communote.server.core.external.ExternalSystemNotConfiguredException;
import com.communote.server.core.external.TooManyExternalObjectsPerTopicException;
import com.communote.server.model.external.ExternalObject;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ExternalObjectResourceHandler
        extends
        DefaultResourceHandler<CreateExternalObjectParameter, EditExternalObjectParameter,
        DeleteExternalObjectParameter, GetExternalObjectParameter, GetCollectionExternalObjectParameter> {

    /**
     * Returns the {@link BlogManagement}.
     * 
     * @return Returns the {@link BlogManagement}.
     */
    private BlogManagement getBlogManagement() {
        return ServiceLocator.instance().getService(BlogManagement.class);
    }

    /**
     * Returns the {@link ExternalObjectManagement}.
     * 
     * @return Returns the {@link ExternalObjectManagement}.
     */
    private ExternalObjectManagement getExternalObjectManagement() {
        return ServiceLocator.instance().getService(ExternalObjectManagement.class);
    }

    /**
     * Request to assign external object for topic.
     * 
     * @param createExternalObjectParameter
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
     *             topic was not found
     * @throws ResponseBuildException
     *             exception while building the response
     * @throws ExtensionNotSupportedException
     *             extension is not supported
     * @throws AuthorizationException
     *             user has no manager access for topic
     * @throws BlogNotFoundException
     *             can not found topic
     * @throws ExternalObjectAlreadyAssignedException
     *             in case the topic is already assigned
     * @throws ExternalSystemNotConfiguredException
     * @throws TooManyExternalObjectsPerTopicException
     */
    @Override
    public Response handleCreateInternally(
            CreateExternalObjectParameter createExternalObjectParameter,
            String requestedMimeType, UriInfo uriInfo, String requestSessionId, Request request)
            throws BlogNotFoundException, NotFoundException,
            ResponseBuildException, ExtensionNotSupportedException, AuthorizationException,
            ExternalObjectAlreadyAssignedException, TooManyExternalObjectsPerTopicException,
            ExternalSystemNotConfiguredException {
        Long topicId = TopicResourceHelper
                .getTopicIdByIdentifier(
                        (createExternalObjectParameter.getTopicIdentifier() != null) ? createExternalObjectParameter
                                .getTopicIdentifier().name()
                                : null,
                        createExternalObjectParameter.getTopicId(), getBlogManagement());

        ExternalObject externalObject = ExternalObject.Factory.newInstance();
        externalObject.setExternalId(createExternalObjectParameter.getExternalId());
        externalObject.setExternalName(createExternalObjectParameter.getName());
        externalObject.setExternalSystemId(createExternalObjectParameter.getExternalSystemId());

        ExternalObject resultExternalObject = getExternalObjectManagement()
                .assignExternalObject(topicId, externalObject);

        return ResponseHelper.buildSuccessResponse(resultExternalObject.getId(), request);
    }

    /**
     * Delete the {@link ExternalObjectResource}.
     * 
     * @param deleteExternalObjectParameter
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
     * @return {@link ExternalObject}
     * @throws ResponseBuildException
     *             exception while building the response
     * @throws ExtensionNotSupportedException
     *             extension is not supported
     * @throws NotFoundException
     *             can not found external object
     * @throws AuthorizationException
     *             user has no manager access for topic
     * @throws BlogNotFoundException
     *             can not found topic
     */
    @Override
    public Response handleDeleteInternally(
            DeleteExternalObjectParameter deleteExternalObjectParameter,
            String requestedMimeType, UriInfo uriInfo, String requestSessionId, Request request)
            throws BlogNotFoundException, ResponseBuildException, ExtensionNotSupportedException,
            NotFoundException, AuthorizationException {

        Long topicId = TopicResourceHelper
                .getTopicIdByIdentifier(
                        (deleteExternalObjectParameter.getTopicIdentifier() != null) ? deleteExternalObjectParameter
                                .getTopicIdentifier().name()
                                : null,
                        deleteExternalObjectParameter.getTopicId(), getBlogManagement());

        if (StringUtils.isNotBlank(deleteExternalObjectParameter.getExternalId())
                && StringUtils.isNotBlank(deleteExternalObjectParameter.getExternalSystemId())) {
            getExternalObjectManagement().removeExternalObject(topicId,
                    deleteExternalObjectParameter.getExternalSystemId(),
                    deleteExternalObjectParameter.getExternalId());
        } else {
            getExternalObjectManagement().removeExternalObject(
                    deleteExternalObjectParameter.getExternalObjectId());
        }

        return ResponseHelper.buildSuccessResponse(null, request);
    }

    /**
     * Edit an {@link ExternalObjectResource}
     * 
     * @param editExternalObjectParameter
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
     * @return {@link ExternalObject}
     * @throws BlogNotFoundException
     *             in case the topic does not exist
     * @throws NotFoundException
     *             in case the external object does not exist
     * @throws ExternalObjectAlreadyAssignedException
     *             in case the external object is assigned to another topic
     * @throws BlogAccessException
     *             in case the user is not manager of the topic
     * @throws ResponseBuildException
     *             exception while building the response
     * @throws ExtensionNotSupportedException
     *             extension is not supported
     */
    @Override
    public Response handleEditInternally(EditExternalObjectParameter editExternalObjectParameter,
            String requestedMimeType, UriInfo uriInfo, String requestSessionId, Request request)
            throws BlogNotFoundException, NotFoundException,
            ExternalObjectAlreadyAssignedException, BlogAccessException, ResponseBuildException,
            ExtensionNotSupportedException {
        Long topicId = TopicResourceHelper
                .getTopicIdByIdentifier(
                        (editExternalObjectParameter.getTopicIdentifier() != null) ? editExternalObjectParameter
                                .getTopicIdentifier().name()
                                : null,
                        editExternalObjectParameter.getTopicId(), getBlogManagement());

        ExternalObject externalObject = ExternalObject.Factory.newInstance();
        externalObject.setId(editExternalObjectParameter.getExternalObjectId());
        externalObject.setExternalId(editExternalObjectParameter.getExternalId());
        externalObject.setExternalName(editExternalObjectParameter.getName());
        externalObject.setExternalSystemId(editExternalObjectParameter.getExternalSystemId());

        getExternalObjectManagement().updateExternalObject(topicId, externalObject);

        return ResponseHelper.buildSuccessResponse(
                editExternalObjectParameter.getExternalObjectId(), request);
    }

    /**
     * Get the internal representation of external object as {@link ExternalObjectResource}.
     * 
     * @param getExternalObjectParameter
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
     * @return {@link ExternalObject}
     * @throws ResponseBuildException
     *             exception while building the response
     * @throws ExtensionNotSupportedException
     *             extension is not supported
     * @throws NotFoundException
     *             can not found external object
     * @throws BlogAccessException
     *             in case the current user has no read access to the blog
     */
    @Override
    public Response handleGetInternally(GetExternalObjectParameter getExternalObjectParameter,
            String requestedMimeType, UriInfo uriInfo, String requestSessionId, Request request)
            throws ResponseBuildException, ExtensionNotSupportedException, NotFoundException,
            BlogAccessException {

        Long topicId = TopicResourceHelper
                .getTopicIdByIdentifier(
                        (getExternalObjectParameter.getTopicIdentifier() != null) ? getExternalObjectParameter
                                .getTopicIdentifier().name()
                                : null,
                        getExternalObjectParameter.getTopicId(), getBlogManagement());

        Collection<ExternalObject> externalObjects = getExternalObjectManagement()
                .getExternalObjects(topicId);

        for (ExternalObject externalObject : externalObjects) {
            Long externalObjectId = getExternalObjectParameter.getExternalObjectId();
            if (externalObject.getId() == externalObjectId
                    ||
                    (externalObject.getExternalId().equals(externalObject.getExternalId()) && externalObject
                            .getExternalSystemId().equals(externalObject.getExternalSystemId()))) {
                ExternalObjectResource externalObjectResource = new ExternalObjectResource();
                new ExternalObjectToExternalObjectResourceConverter<ExternalObject, ExternalObjectResource>()
                        .convert(externalObject, externalObjectResource);
                return ResponseHelper.buildSuccessResponse(externalObjectResource, request);
            }
        }

        throw new NotFoundException("External object with ID {}"
                + getExternalObjectParameter.getExternalObjectId() + " not found");

    }

    /**
     * Get an list of external objects
     * 
     * @param getCollectionExternalObjectParameter
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
     * @throws BlogNotFoundException
     *             topic was not found
     * @throws BlogAccessException
     *             in case the current user has no read access to the blog
     */
    @Override
    public Response handleListInternally(
            GetCollectionExternalObjectParameter getCollectionExternalObjectParameter,
            String requestedMimeType, UriInfo uriInfo, String sessionId, Request request)
            throws IllegalRequestParameterException, ResponseBuildException,
            ExtensionNotSupportedException, BlogNotFoundException, BlogAccessException {
        Long topicId = TopicResourceHelper
                .getTopicIdByIdentifier(
                        (getCollectionExternalObjectParameter.getTopicIdentifier() != null)
                                ? getCollectionExternalObjectParameter.getTopicIdentifier().name()
                                : null,
                        getCollectionExternalObjectParameter.getTopicId(), getBlogManagement());

        Collection<ExternalObject> externalObjects = getExternalObjectManagement()
                .getExternalObjects(topicId);

        List<ExternalObjectResource> externalObjectResources = new ArrayList<ExternalObjectResource>();

        for (ExternalObject externalObject : externalObjects) {
            ExternalObjectResource externalObjectResource = new ExternalObjectResource();
            new ExternalObjectToExternalObjectResourceConverter<ExternalObject, ExternalObjectResource>()
                    .convert(externalObject, externalObjectResource);
            externalObjectResources.add(externalObjectResource);
        }

        return ResponseHelper.buildSuccessResponse(externalObjectResources, request);
    }

}
