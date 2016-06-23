package com.communote.server.web.fe.portal.blog.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;

import com.communote.common.util.ParameterHelper;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.note.NoteManagementAuthorizationException;
import com.communote.server.api.util.JsonHelper;
import com.communote.server.core.blog.NoteManagementException;
import com.communote.server.service.NoteService;
import com.communote.server.web.commons.MessageHelper;


/**
 * MultiActionController for blog post deletion.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class DeleteBlogPostController extends MultiActionController {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeleteBlogPostController.class);

    private static final String STATUS_ERROR = "ERROR";
    private static final String STATUS_OK = "OK";

    private static final String JSON_STATUS_KEY = "status";
    private static final String JSON_ERROR_MESSAGE_KEY = "message";
    private static final String PARAM_NOTE_ID = "noteId";

    /**
     * Deletes the user tagged post with the id found in request parameter blogpostId.
     * 
     * @param request
     *            the request
     * @param response
     *            the response
     * @throws Exception
     *             in case of error
     */
    public void confirmed(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        String errorMessage = null;
        Long noteId = ParameterHelper.getParameterAsLong(request.getParameterMap(), PARAM_NOTE_ID);
        if (noteId != null) {
            try {
                ServiceLocator.instance().getService(NoteService.class)
                        .deleteNote(noteId, false, false);
            } catch (NoteManagementAuthorizationException e) {
                errorMessage = MessageHelper.getText(request,
                        "error.blogpost.blog.no.write.access", new String[] { e.getBlogTitle() });
            } catch (Exception e) {
                errorMessage = MessageHelper.getText(request, "error.blogpost.delete.failed");
                LOGGER.error("Error deleting blog post with id " + noteId, e);
            }
        } else {
            errorMessage = MessageHelper.getText(request, "error.blogpost.delete.no.post");
        }
        String status = errorMessage != null ? STATUS_ERROR : STATUS_OK;
        writeJsonResponse(response, status, errorMessage);
    }

    /**
     * Deletes an autosave.
     * 
     * @param request
     *            the request
     * @param response
     *            the response
     * @throws Exception
     *             in case of error
     */
    public void deleteAutosave(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        String errorMsg = null;
        Long postId = ParameterHelper.getParameterAsLong(request.getParameterMap(), PARAM_NOTE_ID);
        if (postId != null) {
            try {
                ServiceLocator.instance().getService(NoteService.class).deleteAutosave(postId);
            } catch (NoteManagementAuthorizationException e) {
                errorMsg = MessageHelper.getText(request, "error.blogpost.draft.delete.denied");
            } catch (NoteManagementException e) {
                LOGGER.error("Error deleting autosave with id " + postId, e);
                errorMsg = MessageHelper.getText(request, "error.blogpost.draft.delete.failed");
            }
        } else {
            LOGGER.debug("Draft deletion failed because postId or version is missing.");
            errorMsg = MessageHelper.getText(request,
                    "error.blogpost.draft.delete.incorrect.request");
        }
        String status = errorMsg != null ? STATUS_ERROR : STATUS_OK;
        writeJsonResponse(response, status, errorMsg);
    }

    /**
     * Writes a JSON object to the response.
     * 
     * @param response
     *            the response to write to
     * @param status
     *            the status to set
     * @param message
     *            the message, can be null
     * @throws IOException
     *             in case of an error while writing to the response
     */
    private void writeJsonResponse(HttpServletResponse response, String status, String message)
            throws IOException {
        ObjectNode responseObj = JsonHelper.getSharedObjectMapper().createObjectNode();
        responseObj.put(JSON_STATUS_KEY, status);
        if (message != null) {
            responseObj.put(JSON_ERROR_MESSAGE_KEY, message);
        }
        JsonHelper.writeJsonTree(response.getWriter(), responseObj);
    }
}
