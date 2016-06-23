package com.communote.plugins.api.rest.v22.resource.note.favorite;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.communote.plugins.api.rest.v22.exception.ExtensionNotSupportedException;
import com.communote.plugins.api.rest.v22.exception.ResponseBuildException;
import com.communote.plugins.api.rest.v22.resource.DefaultParameter;
import com.communote.plugins.api.rest.v22.resource.DefaultResourceHandler;
import com.communote.plugins.api.rest.v22.response.ResponseHelper;
import com.communote.server.api.ServiceLocator;
import com.communote.server.core.blog.FavoriteManagement;
import com.communote.server.core.blog.NoteNotFoundException;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class FavoriteResourceHandler
        extends
        DefaultResourceHandler
        <CreateFavoriteParameter, DefaultParameter, DeleteFavoriteParameter, DefaultParameter,
        GetCollectionFavoriteParameter> {

    /**
     * Build the {@link FavoriteResource}
     * 
     * @param favorite
     *            is favorite
     * @return {@link FavoriteResource}
     */
    private FavoriteResource buildFavoriteResource(Boolean favorite) {
        FavoriteResource favoriteResource = new FavoriteResource();
        favoriteResource.setFavorite(favorite);
        return favoriteResource;
    }

    /**
     * Get the {@link FavoriteManagement}
     * 
     * @return {@link FavoriteManagement}
     */
    private FavoriteManagement getFavoriteManagement() {
        return ServiceLocator.instance().getService(FavoriteManagement.class);
    }

    /**
     * Request to set note as favorite
     * 
     * @param createFavoriteParameter
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
     * @return {@link FavoriteResource}
     * 
     * @throws NoteNotFoundException
     *             note was not found to favorize
     * @throws ResponseBuildException
     *             exception while building the response
     * @throws ExtensionNotSupportedException
     *             extension is not supported
     */
    @Override
    public Response handleCreateInternally(CreateFavoriteParameter createFavoriteParameter,
            String requestedMimeType, UriInfo uriInfo, String requestSessionId, Request request)
            throws NoteNotFoundException, ResponseBuildException, ExtensionNotSupportedException {
        if (createFavoriteParameter.getFavorite()) {
            getFavoriteManagement().markNoteAsFavorite(createFavoriteParameter.getNoteId());
        } else {
            getFavoriteManagement().unmarkNoteAsFavorite(createFavoriteParameter.getNoteId());
        }
        return ResponseHelper.buildSuccessResponse(null, request);
    }

    /**
     * Request to delete note as favorite
     * 
     * @param deleteFavoriteParameter
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
     * @return {@link FavoriteResource}
     * 
     * @throws NoteNotFoundException
     *             note was not found to favorize
     * @throws ResponseBuildException
     *             exception while building the response
     * @throws ExtensionNotSupportedException
     *             extension is not supported
     */
    @Override
    public Response handleDeleteInternally(DeleteFavoriteParameter deleteFavoriteParameter,
            String requestedMimeType, UriInfo uriInfo, String requestSessionId, Request request)
            throws NoteNotFoundException, ResponseBuildException, ExtensionNotSupportedException {
        if (getFavoriteManagement().isFavorite(deleteFavoriteParameter.getNoteId())) {
            getFavoriteManagement().unmarkNoteAsFavorite(deleteFavoriteParameter.getNoteId());
        }
        return ResponseHelper.buildSuccessResponse(null, request);
    }

    /**
     * Request to list that a note is favorite oder not
     * 
     * @param getCollectionFavoriteParameter
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
     * @return {@link FavoriteResource}
     * @throws ResponseBuildException
     *             exception while building the response
     * @throws ExtensionNotSupportedException
     *             extension is not supported
     * 
     */
    @Override
    public Response handleListInternally(
            GetCollectionFavoriteParameter getCollectionFavoriteParameter,
            String requestedMimeType, UriInfo uriInfo, String requestSessionId, Request request)
            throws ResponseBuildException, ExtensionNotSupportedException {
        FavoriteResource favoriteResource = buildFavoriteResource(getFavoriteManagement()
                .isFavorite(getCollectionFavoriteParameter.getNoteId()));
        List<FavoriteResource> favoriteResources = new ArrayList<FavoriteResource>();
        favoriteResources.add(favoriteResource);
        return ResponseHelper.buildSuccessResponse(favoriteResources, request);
    }

}
