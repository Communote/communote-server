package com.communote.plugins.api.rest.resource.note;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.communote.common.util.PageableList;
import com.communote.plugins.api.rest.exception.ExtensionNotSupportedException;
import com.communote.plugins.api.rest.exception.ResponseBuildException;
import com.communote.plugins.api.rest.resource.DefaultResourceHandler;
import com.communote.plugins.api.rest.resource.ResourceHandlerHelper;
import com.communote.plugins.api.rest.resource.note.property.PropertyResourceHelper;
import com.communote.plugins.api.rest.response.ResponseHelper;
import com.communote.plugins.api.rest.service.IllegalRequestParameterException;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.blog.BlogNotFoundException;
import com.communote.server.api.core.note.NoteData;
import com.communote.server.api.core.note.NoteRenderContext;
import com.communote.server.api.core.note.NoteStoringTO;
import com.communote.server.api.core.note.processor.NoteStoringPreProcessorException;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.core.blog.NoteNotFoundException;
import com.communote.server.core.storing.ResourceStoringManagement;
import com.communote.server.core.vo.blog.NoteModificationResult;
import com.communote.server.core.vo.query.QueryParameters.OrderDirection;
import com.communote.server.core.vo.query.config.QueryParametersParameterNameProvider;
import com.communote.server.core.vo.query.post.NoteQueryParameters;
import com.communote.server.service.NoteService;

/**
 * Is the handler class to provide data for a note resource to the resource class. All the list
 * parameter are collected in a parameter map expected the <tt>filterHtml</tt>. This value is
 * evaluated within this class.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class NoteResourceHandler
extends
DefaultResourceHandler<CreateNoteParameter, EditNoteParameter, DeleteNoteParameter, GetNoteParameter, GetCollectionNoteParameter> {

    /**
     *
     */
    public NoteResourceHandler() {
        super(new NoteResourceValidator());
    }

    /**
     * Removes orphaned attachments that were previously uploaded but were not attached to the
     * created note.
     *
     * @param request
     *            the request
     * @param attribute
     *            key under which the attachments are stored in the session
     * @param attachmentIds
     *            the attachment IDs that were added to the note
     * @throws AuthorizationException
     *             Thrown, when the user is not allowed to update the attachments.
     */
    private void cleanupAttachments(Request request, String attribute, Long[] attachmentIds)
            throws AuthorizationException {
        Set<Long> uploadedAttachments = ResourceHandlerHelper.getUploadedAttachmentsFromSession(
                request, attribute);
        if (uploadedAttachments != null) {
            // remove all IDs that were saved with note
            if (attachmentIds != null) {
                for (Long id : attachmentIds) {
                    uploadedAttachments.remove(id);
                }
            }
            ServiceLocator.findService(ResourceStoringManagement.class).deleteOrphanedAttachments(
                    uploadedAttachments);
            ResourceHandlerHelper.removeUploadedAttachmentsFromSession(request, attribute);
        }
    }

    /**
     * Converts the note list data items to note resources.
     *
     * @param noteListItems
     *            the items to convert
     * @param notes
     *            collection for adding the converted notes
     */
    private void convertToNoteResources(PageableList<NoteData> noteListItems,
            Collection<NoteResource> notes) {
        for (NoteData postListItem : noteListItems) {
            notes.add(NoteBuildHelper.buildNoteResource(postListItem));
        }
    }

    /**
     * Extract the crosspost topic alias into a set
     *
     * @param crosspostTopicAliases
     *            the aliases, can be null
     * @return the set of topic aliases or null if input was null
     */
    private HashSet<String> extractTopicAliases(String[] crosspostTopicAliases) {
        if (crosspostTopicAliases == null) {
            return null;
        }
        return new HashSet<>(Arrays.asList(crosspostTopicAliases));
    }

    /**
     * Get the notes filtered by direct message restriction and/or notifications for current user
     * restrictions and/or followed items restriction
     *
     * @param getCollectionNoteParameter
     *            an object that contains all the parameters of the request
     * @param renderContext
     *            the context for rendering
     * @param showDirectMessages
     *            whether to show only direct messages
     * @param showNotesForMe
     *            whether to show only notifications for current user
     * @param showFollowedItems
     *            whether to show only followed items
     * @param request
     *            The request.
     * @return the found notes
     */
    private List<NoteResource> getNotesOfDirectNotifyFollowedFilter(
            GetCollectionNoteParameter getCollectionNoteParameter, NoteRenderContext renderContext,
            boolean showDirectMessages, boolean showNotesForMe, boolean showFollowedItems,
            Request request) {
        // TODO: KENMEI-3019
        if ((showDirectMessages ? 1 : 0) + (showNotesForMe ? 1 : 0) + (showFollowedItems ? 1 : 0) > 1) {
            // Currently there is no opportunity to set a correct offset for
            // combined filters.
            getCollectionNoteParameter.setOffset(0);
        }
        HashSet<NoteResource> notes = new HashSet<>();
        QueryParametersParameterNameProvider nameProvider = ResourceHandlerHelper
                .getNameProvider(request);
        // BE doesn't support OR combination of the 3 conditions so we do separate queries and merge
        // the results. Need at most 2 queries since notesForMe contain all DMs.
        if (showNotesForMe || showDirectMessages) {
            getCollectionNoteParameter.setF_showDirectMessages(!showNotesForMe);
            getCollectionNoteParameter.setF_showNotesForMe(showNotesForMe);
            getCollectionNoteParameter.setF_showFollowedItems(false);
            NoteQueryParameters noteQueryInstance = NoteResourceHelper.configureQueryInstance(
                    getCollectionNoteParameter, nameProvider, renderContext.getLocale());
            setSortByDate(getCollectionNoteParameter, noteQueryInstance);
            convertToNoteResources(
                    NoteResourceHelper.getPageableList(noteQueryInstance, renderContext), notes);
        }
        if (showFollowedItems) {
            getCollectionNoteParameter.setF_showDirectMessages(false);
            getCollectionNoteParameter.setF_showNotesForMe(false);
            getCollectionNoteParameter.setF_showFollowedItems(true);
            NoteQueryParameters noteQueryInstance = NoteResourceHelper.configureQueryInstance(
                    getCollectionNoteParameter, nameProvider, renderContext.getLocale());
            setSortByDate(getCollectionNoteParameter, noteQueryInstance);
            // duplicates won't be added to the set
            convertToNoteResources(
                    NoteResourceHelper.getPageableList(noteQueryInstance, renderContext), notes);
        }
        // sort the notes by date and return at most maxCount items
        ArrayList<NoteResource> foundNotes = new ArrayList<>(notes);
        return NoteResourceHelper.sortAndLimitNotes(foundNotes,
                getCollectionNoteParameter.getMaxCount());
    }

    /**
     * Create a single note on the server. This note can either be a reply or a new note
     *
     * @param createNoteParameter
     *            - an object that contains all the parameter that can or have to be used for such a
     *            request
     * @param requestedMimeType
     *            - is the mime type that indicates which data exchange format to use
     * @param uriInfo
     *            - this object is created by the request and contains some request specific data
     * @param sessionId
     *            sessionId
     * @param request
     *            - javax request
     * @return an response object containing a http status code and a message
     * @throws NoteStoringPreProcessorException
     *             Exception
     * @throws IllegalRequestParameterException
     *             if text value is wrong
     * @throws AuthorizationException
     *             user is not authorized
     * @throws BlogNotFoundException
     *             blog was not found
     *
     * @throws ResponseBuildException
     *             exception while building the response
     * @throws ExtensionNotSupportedException
     *             extension is not supported
     */
    @Override
    public Response handleCreateInternally(CreateNoteParameter createNoteParameter,
            String requestedMimeType, UriInfo uriInfo, String sessionId, Request request)
                    throws IllegalRequestParameterException, BlogNotFoundException, AuthorizationException,
            NoteStoringPreProcessorException, ResponseBuildException,
                    ExtensionNotSupportedException {

        NoteStoringTO noteStoringTO = NoteResourceHelper.buildNoteStoringTO(createNoteParameter);

        noteStoringTO.setProperties(PropertyResourceHelper
                .convertPropertyResourcesToStringPropertyTOs(createNoteParameter.getProperties()));

        NoteModificationResult result;

        if (createNoteParameter.getParentNoteId() == null) {

            Set<String> additionalBlogNameIds = extractTopicAliases(createNoteParameter
                    .getCrossPostTopicAliases());

            // create an entirely new note
            result = ServiceLocator.instance().getService(NoteService.class)
                    .createNote(noteStoringTO, additionalBlogNameIds);
        } else {
            // create a reply note
            noteStoringTO.setParentNoteId(createNoteParameter.getParentNoteId());
            result = ServiceLocator.instance().getService(NoteService.class)
                    .createNote(noteStoringTO, null);
        }

        if (noteStoringTO.isPublish()) {
            // pass the upload session ID directly because it's not in the request anymore
            cleanupAttachments(request, createNoteParameter.getAttachmentUploadSessionId(),
                    noteStoringTO.getAttachmentIds());
        }

        return NoteBuildHelper.buildNoteResponse(request, result);
    }

    /**
     * Delete a note on the server
     *
     * @param deleteNoteParameter
     *            - an object that contains all the parameter that can or have to be used for such a
     *            request
     * @param requestedMimeType
     *            - is the mime type that indicates which data exchange format to use
     * @param uriInfo
     *            - this object is created by the request and contains some request specific data
     * @param sessionId
     *            sessionId
     * @param request
     *            - javax request
     * @return an response object containing a http status code and a message
     * @throws AuthorizationException
     *             user is not authorized
     * @throws ResponseBuildException
     *             exception while building the response
     * @throws ExtensionNotSupportedException
     *             extension is not supported
     */
    @Override
    public Response handleDeleteInternally(DeleteNoteParameter deleteNoteParameter,
            String requestedMimeType, UriInfo uriInfo, String sessionId, Request request)
                    throws AuthorizationException, ResponseBuildException, ExtensionNotSupportedException {
        ServiceLocator.instance().getService(NoteService.class)
        .deleteNote(deleteNoteParameter.getNoteId(), false, false);
        return ResponseHelper.buildSuccessResponse(null, request,
                "restapi.message.resource.note.delete", deleteNoteParameter.getNoteId());
    }

    /**
     * Changes an existing note on the server
     *
     * @param editNoteParameter
     *            - an object that contains all the parameter that can or have to be used for such a
     *            request
     * @param requestedMimeType
     *            - is the mime type that indicates which data exchange format to use
     * @param uriInfo
     *            - this object is created by the request and contains some request specific data
     * @param sessionId
     *            sessionId
     * @param request
     *            - javax request
     * @return an response object containing a http status code and a message
     * @throws NoteStoringPreProcessorException
     *             Exception
     * @throws IllegalRequestParameterException
     *             if text value is wrong
     * @throws AuthorizationException
     *             user is not authorized
     * @throws BlogNotFoundException
     *             blog was not found
     * @throws NoteNotFoundException
     *             note was not found
     * @throws ResponseBuildException
     *             exception while building the response
     * @throws ExtensionNotSupportedException
     *             extension is not supported
     */
    @Override
    public Response handleEditInternally(EditNoteParameter editNoteParameter,
            String requestedMimeType, UriInfo uriInfo, String sessionId, Request request)
                    throws IllegalRequestParameterException, NoteNotFoundException, AuthorizationException,
            BlogNotFoundException, NoteStoringPreProcessorException, ResponseBuildException,
            ExtensionNotSupportedException {

        NoteModificationResult result;

        NoteStoringTO noteStoringTO = NoteResourceHelper.buildNoteStoringTO(editNoteParameter);

        noteStoringTO.setProperties(PropertyResourceHelper
                .convertPropertyResourcesToStringPropertyTOs(editNoteParameter.getProperties()));

        Set<String> additionalBlogNameIds = extractTopicAliases(editNoteParameter
                .getCrossPostTopicAliases());

        result = ServiceLocator.instance().getService(NoteService.class)
                .updateNote(noteStoringTO, editNoteParameter.getNoteId(), additionalBlogNameIds);

        if (noteStoringTO.isPublish()) {
            // pass the upload session ID directly because it's not in the request anymore
            cleanupAttachments(request, editNoteParameter.getAttachmentUploadSessionId(),
                    noteStoringTO.getAttachmentIds());
        }

        return NoteBuildHelper.buildNoteResponse(request, result);
    }

    /**
     * Retrieve a single note from the server
     *
     * @param getNoteParameter
     *            - an object that contains all the parameter that can or have to be used for such a
     *            request
     * @param requestedMimeType
     *            - is the mime type that indicates which data exchange format to use
     * @param uriInfo
     *            - this object is created by the request and contains some request specific data
     * @param sessionId
     *            sessionId
     * @param request
     *            - javax request
     * @return an response object containing a http status code and a message
     * @throws NoteNotFoundException
     *             Exception
     * @throws AuthorizationException
     *             Exception
     * @throws ResponseBuildException
     *             exception while building the response
     * @throws ExtensionNotSupportedException
     *             extension is not supported
     */
    @Override
    public Response handleGetInternally(GetNoteParameter getNoteParameter,
            String requestedMimeType, UriInfo uriInfo, String sessionId, Request request)
                    throws AuthorizationException, NoteNotFoundException, ResponseBuildException,
                    ExtensionNotSupportedException {
        // don't beautify because single GETs are usually made to edit the note later on
        NoteRenderContext context = NoteResourceHelper.createNoteRenderContext(
                getNoteParameter.getFilterHtml(), false, request);
        NoteData note = ServiceLocator.instance().getService(NoteService.class)
                .getNote(getNoteParameter.getNoteId(), context);
        NoteResource noteResource = NoteBuildHelper.buildNoteResource(note);
        return ResponseHelper.buildSuccessResponse(noteResource, request);
    }

    /**
     * Get method for the NoteCollection
     *
     * @param getCollectionNoteParameter
     *            - an object that contains all the parameter that can or have to be used for such a
     *            request
     * @param requestedMimeType
     *            The requested mimetype.
     * @param uriInfo
     *            All request information.
     * @param sessionId
     *            the current session Id.
     * @param request
     *            - javax request
     * @return Collection of Notes for the given parameters.
     * @throws ResponseBuildException
     *             exception while building the response
     * @throws ExtensionNotSupportedException
     *             extension is not supported
     */
    @Override
    public Response handleListInternally(GetCollectionNoteParameter getCollectionNoteParameter,
            String requestedMimeType, UriInfo uriInfo, String sessionId, Request request)
                    throws ResponseBuildException, ExtensionNotSupportedException {

        Boolean filterHtml = getCollectionNoteParameter.getFilterHtml();
        NoteQueryParameters noteQueryInstance;
        boolean showDirectMessages = (getCollectionNoteParameter.getF_showDirectMessages() == null) ? false
                : getCollectionNoteParameter.getF_showDirectMessages();
        boolean showNotesForMe = (getCollectionNoteParameter.getF_showNotesForMe() == null) ? false
                : getCollectionNoteParameter.getF_showNotesForMe();
        boolean showFollowedItems = (getCollectionNoteParameter.getF_showFollowedItems() == null) ? false
                : getCollectionNoteParameter.getF_showFollowedItems();
        NoteRenderContext renderContext = NoteResourceHelper.createNoteRenderContext(filterHtml,
                true, request);
        if (!(showDirectMessages || showNotesForMe || showFollowedItems)) {
            noteQueryInstance = NoteResourceHelper.configureQueryInstance(
                    getCollectionNoteParameter, ResourceHandlerHelper.getNameProvider(request),
                    renderContext.getLocale());
            setSortByDate(getCollectionNoteParameter, noteQueryInstance);
            PageableList<NoteData> pageableNoteList = NoteResourceHelper.getPageableList(
                    noteQueryInstance, renderContext);
            Map<String, Object> metaData = ResourceHandlerHelper.generateMetaDataForPaging(
                    getCollectionNoteParameter.getOffset(),
                    getCollectionNoteParameter.getMaxCount(),
                    pageableNoteList.getMinNumberOfElements());
            ArrayList<NoteResource> notes = new ArrayList<>();
            convertToNoteResources(pageableNoteList, notes);
            return ResponseHelper.buildSuccessResponse(notes, request, metaData);
        } else {
            List<NoteResource> notes = getNotesOfDirectNotifyFollowedFilter(
                    getCollectionNoteParameter, renderContext, showDirectMessages, showNotesForMe,
                    showFollowedItems, request);
            return ResponseHelper.buildSuccessResponse(notes, request);
        }
    }

    /**
     * Set sort mode of notes
     *
     * @param getCollectionNoteParameter
     *            {@link GetCollectionNoteParameter}
     * @param noteQueryInstance
     *            {@link NoteQueryParameters}
     */
    private void setSortByDate(GetCollectionNoteParameter getCollectionNoteParameter,
            NoteQueryParameters noteQueryInstance) {
        if (getCollectionNoteParameter.getF_discussionId() != null) {
            noteQueryInstance.setSortByDate(OrderDirection.ASCENDING);
        } else {
            noteQueryInstance.setSortByDate(OrderDirection.DESCENDING);
        }
    }
}
