package com.communote.plugins.api.rest.v30.resource.note;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.communote.plugins.api.rest.v30.exception.ExtensionNotSupportedException;
import com.communote.plugins.api.rest.v30.exception.ResponseBuildException;
import com.communote.plugins.api.rest.v30.resource.DefaultParameter;
import com.communote.plugins.api.rest.v30.resource.DefaultResourceHandler;
import com.communote.plugins.api.rest.v30.resource.ResourceHandlerHelper;
import com.communote.plugins.api.rest.v30.resource.note.property.PropertyResourceHelper;
import com.communote.plugins.api.rest.v30.response.ResponseHelper;
import com.communote.plugins.api.rest.v30.service.IllegalRequestParameterException;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.blog.BlogNotFoundException;
import com.communote.server.api.core.note.NoteData;
import com.communote.server.api.core.note.NoteManagementAuthorizationException;
import com.communote.server.api.core.note.NoteRenderContext;
import com.communote.server.api.core.note.NoteStoringTO;
import com.communote.server.api.core.note.processor.NoteStoringPreProcessorException;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.core.blog.NoteNotFoundException;
import com.communote.server.core.storing.ResourceStoringManagement;
import com.communote.server.core.vo.blog.NoteModificationResult;
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
        DefaultResourceHandler
        <CreateNoteParameter, EditNoteParameter, DeleteNoteParameter, GetNoteParameter, DefaultParameter> {

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
                request,
                attribute);
        if (uploadedAttachments != null) {
            // remove all IDs that were saved with note
            if (attachmentIds != null) {
                for (Long id : attachmentIds) {
                    uploadedAttachments.remove(id);
                }
            }
            ServiceLocator.findService(ResourceStoringManagement.class)
                    .deleteOrphanedAttachments(uploadedAttachments);
            ResourceHandlerHelper.removeUploadedAttachmentsFromSession(request, attribute);
        }
    }

    /**
     * Gets the {@link NoteService}
     * 
     * @return {@link NoteService}
     */
    private NoteService getNoteService() {
        return ServiceLocator.instance().getService(NoteService.class);
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
     * @throws NoteManagementAuthorizationException
     *             user is not authorized
     * @throws BlogNotFoundException
     *             blog was not found
     * @throws NoteNotFoundException
     *             note was not found
     * @throws ResponseBuildException
     *             exception while building the response
     * @throws ExtensionNotSupportedException
     *             extension is not supported
     * @throws AuthorizationException
     *             Thrown, when the user is not allowed to update the attachments.
     */
    @Override
    public Response handleCreateInternally(CreateNoteParameter createNoteParameter,
            String requestedMimeType, UriInfo uriInfo, String sessionId, Request request)
            throws IllegalRequestParameterException, BlogNotFoundException,
            AuthorizationException, NoteStoringPreProcessorException, NoteNotFoundException,
            ResponseBuildException, ExtensionNotSupportedException {

        NoteStoringTO noteStoringTO = NoteResourceHelper.buildNoteStoringTO(createNoteParameter);

        noteStoringTO.setProperties(PropertyResourceHelper
                .convertPropertyResourcesToStringPropertyTOs(createNoteParameter.getProperties()));

        NoteService noteManagment = getNoteService();
        NoteModificationResult result;

        if (createNoteParameter.getParentNoteId() == null) {

            Set<String> additionalBlogNameIds = null;
            if (createNoteParameter.getCrossPostTopicAliases() != null) {
                additionalBlogNameIds = new HashSet<>(Arrays.asList(createNoteParameter
                        .getCrossPostTopicAliases()));
            }

            // create an entirely new note
            result = noteManagment.createNote(noteStoringTO, additionalBlogNameIds);
        } else {
            // create a reply note
            noteStoringTO.setParentNoteId(createNoteParameter.getParentNoteId());
            result = noteManagment.createNote(noteStoringTO, null);
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
     * @throws NoteManagementAuthorizationException
     *             user is not authorized
     * @throws ResponseBuildException
     *             exception while building the response
     * @throws ExtensionNotSupportedException
     *             extension is not supported
     */
    @Override
    public Response handleDeleteInternally(DeleteNoteParameter deleteNoteParameter,
            String requestedMimeType, UriInfo uriInfo, String sessionId, Request request)
            throws NoteManagementAuthorizationException, ResponseBuildException,
            ExtensionNotSupportedException {
        getNoteService().deleteNote(deleteNoteParameter.getNoteId(), false, false);
        return ResponseHelper.buildSuccessResponse(null, request,
                "restapi.message.resource.note.delete",
                deleteNoteParameter.getNoteId());
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
     * @throws NoteManagementAuthorizationException
     *             user is not authorized
     * @throws BlogNotFoundException
     *             blog was not found
     * @throws NoteNotFoundException
     *             note was not found
     * @throws ResponseBuildException
     *             exception while building the response
     * @throws ExtensionNotSupportedException
     *             extension is not supported
     * @throws AuthorizationException
     *             Thrown, when the user is not allowed to update the attachments.
     */
    @Override
    public Response handleEditInternally(EditNoteParameter editNoteParameter,
            String requestedMimeType, UriInfo uriInfo, String sessionId, Request request)
            throws IllegalRequestParameterException, NoteNotFoundException,
            AuthorizationException, BlogNotFoundException, NoteStoringPreProcessorException,
            ResponseBuildException, ExtensionNotSupportedException {

        NoteService noteService = getNoteService();
        NoteModificationResult result;

        NoteStoringTO noteStoringTO = NoteResourceHelper.buildNoteStoringTO(editNoteParameter);

        noteStoringTO.setProperties(PropertyResourceHelper
                .convertPropertyResourcesToStringPropertyTOs(editNoteParameter.getProperties()));

        Set<String> additionalBlogNameIds = null;
        if (editNoteParameter.getCrossPostTopicAliases() != null) {
            additionalBlogNameIds = new HashSet<>(Arrays.asList(editNoteParameter
                    .getCrossPostTopicAliases()));
        }

        result = noteService.updateNote(noteStoringTO, editNoteParameter.getNoteId(),
                additionalBlogNameIds, true);

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
        NoteData note = getNoteService().getNote(getNoteParameter.getNoteId(), context);
        NoteResource noteResource = NoteBuildHelper.buildNoteResource(note);
        return ResponseHelper.buildSuccessResponse(noteResource, request);
    }

}
