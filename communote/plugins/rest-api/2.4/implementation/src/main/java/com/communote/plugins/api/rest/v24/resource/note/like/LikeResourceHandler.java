package com.communote.plugins.api.rest.v24.resource.note.like;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.communote.plugins.api.rest.v24.converter.UserToTimelineUserConverter;
import com.communote.plugins.api.rest.v24.exception.ExtensionNotSupportedException;
import com.communote.plugins.api.rest.v24.exception.ResponseBuildException;
import com.communote.plugins.api.rest.v24.resource.DefaultParameter;
import com.communote.plugins.api.rest.v24.resource.DefaultResourceHandler;
import com.communote.plugins.api.rest.v24.resource.timelineuser.TimelineUserResource;
import com.communote.plugins.api.rest.v24.response.ResponseHelper;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.common.NotFoundException;
import com.communote.server.api.core.property.PropertyManagement;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.core.blog.helper.NoteHelper;
import com.communote.server.core.security.SecurityHelper;
import com.communote.server.service.NoteService;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class LikeResourceHandler
        extends
        DefaultResourceHandler<CreateLikeParameter, DefaultParameter, DeleteLikeParameter,
        DefaultParameter, GetCollectionLikeParameter> {

    private static final UserToTimelineUserConverter converter = new UserToTimelineUserConverter();
    /**
     * Property key for the like feature.
     */
    public static final String PROPERTY_KEY_LIKE = "like";

    /**
     * Fetch the users who like the note and build a resource holding the details
     * 
     * @param noteId
     *            ID of the note to check
     * @return the resource with details about the likers of a not
     * @throws AuthorizationException
     *             user is not authorized
     * @throws NotFoundException
     *             in case the note does not exist
     */
    private LikeResource buildLikeResource(Long noteId) throws AuthorizationException,
            NotFoundException {
        LikeResource likeResource = new LikeResource();
        Collection<TimelineUserResource> likers = NoteHelper.getLikersOfNote(noteId, converter);
        TimelineUserResource[] resources = new TimelineUserResource[likers.size()];
        int i = 0;
        boolean currentUserLikesNote = false;
        Long currentUserId = SecurityHelper.getCurrentUserId();
        for (TimelineUserResource resource : likers) {
            if (resource.getUserId().equals(currentUserId)) {
                currentUserLikesNote = true;
            }
            resources[i] = resource;
            i++;
        }
        likeResource.setLike(currentUserLikesNote);
        likeResource.setUsers(resources);
        return likeResource;
    }

    /**
     * Getter for the {@link NoteService}
     * 
     * @return the {@link NoteService}
     */
    public NoteService getNoteManagement() {
        return ServiceLocator.instance().getService(NoteService.class);
    }

    /**
     * Getter for the {@link PropertyManagement}
     * 
     * @return the {@link PropertyManagement}
     */
    public PropertyManagement getPropertyManagement() {
        return ServiceLocator.instance().getService(PropertyManagement.class);
    }

    /**
     * Request to set note as favorite
     * 
     * @param createLikeParameter
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
     * @return {@link LikeResource}
     * @throws AuthorizationException
     *             user is not authorized
     * @throws NotFoundException
     *             can note found property
     * @throws ResponseBuildException
     *             exception while building the response
     * @throws ExtensionNotSupportedException
     *             extension is not supported
     */
    @Override
    public Response handleCreateInternally(CreateLikeParameter createLikeParameter,
            String requestedMimeType, UriInfo uriInfo, String requestSessionId, Request request)
            throws AuthorizationException, NotFoundException, ResponseBuildException,
            ExtensionNotSupportedException {
        if (getNoteManagement().noteExists(createLikeParameter.getNoteId())) {
            NoteHelper.setLikeNote(createLikeParameter.getNoteId(), createLikeParameter.getLike());
        }
        return ResponseHelper.buildSuccessResponse(null, request);
    }

    /**
     * Request to delete note as favorite
     * 
     * @param deleteLikeParameter
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
     * @return {@link LikeResource}
     * 
     * @throws AuthorizationException
     *             user is not authorized
     * @throws NotFoundException
     *             can not found property
     * @throws ResponseBuildException
     *             exception while building the response
     * @throws ExtensionNotSupportedException
     *             extension is not supported
     */
    @Override
    public Response handleDeleteInternally(DeleteLikeParameter deleteLikeParameter,
            String requestedMimeType, UriInfo uriInfo, String requestSessionId, Request request)
            throws AuthorizationException, NotFoundException, ResponseBuildException,
            ExtensionNotSupportedException {
        if (getNoteManagement().noteExists(deleteLikeParameter.getNoteId())) {
            NoteHelper.unlikeNote(deleteLikeParameter.getNoteId());
        }
        return ResponseHelper.buildSuccessResponse(null, request);
    }

    /**
     * Request to list the likes of note
     * 
     * @param getCollectionLikeParameter
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
     * @return {@link LikeResource}
     * 
     * @throws AuthorizationException
     *             user is not authorized
     * @throws ResponseBuildException
     *             exception while building the response
     * @throws ExtensionNotSupportedException
     *             extension is not supported
     */
    @Override
    public Response handleListInternally(GetCollectionLikeParameter getCollectionLikeParameter,
            String requestedMimeType, UriInfo uriInfo, String requestSessionId, Request request)
            throws AuthorizationException, ResponseBuildException,
            ExtensionNotSupportedException {
        LikeResource likeResource = null;
        try {
            likeResource = buildLikeResource(getCollectionLikeParameter.getNoteId());
        } catch (NotFoundException e) {
            // TODO shouldn't we fail with 404 here?
            // note does not exist; return empty collection
        }
        List<LikeResource> likeResources = new ArrayList<LikeResource>();
        likeResources.add(likeResource);
        return ResponseHelper.buildSuccessResponse(likeResources, request);
    }
}
