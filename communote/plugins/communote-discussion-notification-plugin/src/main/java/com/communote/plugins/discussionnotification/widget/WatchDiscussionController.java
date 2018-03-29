package com.communote.plugins.discussionnotification.widget;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

import com.communote.plugins.core.views.annotations.UrlMapping;
import com.communote.plugins.discussionnotification.DiscussionNotificationActivator;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.common.NotFoundException;
import com.communote.server.api.core.property.PropertyManagement;
import com.communote.server.api.core.property.PropertyType;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.api.util.JsonHelper;
import com.communote.server.web.commons.helper.JsonRequestHelper;

/**
 * @author Communote Team - <a href="https://github.com/Communote">https://github.com/Communote</a>
 */
@Component(immediate = true)
@Instantiate
@Provides
@UrlMapping("/*/" + DiscussionNotificationActivator.KEY_GROUP + "/watch")
public class WatchDiscussionController implements Controller {

    private static final Logger LOGGER = LoggerFactory.getLogger(WatchDiscussionController.class);

    @Override
    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        int errorCode = -1;
        if (request.getMethod().equals("POST")) {
            JsonNode noteIdNode = null;
            try {
                JsonNode node = JsonHelper.getSharedObjectMapper()
                        .readTree(request.getInputStream());
                noteIdNode = node.get("noteId");
                JsonNode watchNode = node.get("watch");
                if (noteIdNode != null && noteIdNode.isNumber() && watchNode != null
                        && watchNode.isBoolean()) {
                    long noteId = node.get("noteId").asLong();
                    boolean watch = node.get("watch").asBoolean();
                    // TODO ensure the noteId is a discussion or get the discussionId of the note
                    ServiceLocator.findService(PropertyManagement.class).setObjectProperty(
                            PropertyType.UserNoteProperty, noteId,
                            DiscussionNotificationActivator.KEY_GROUP,
                            DiscussionNotificationActivator.PROPERTY_KEY_WATCHED_DISCUSSION,
                            String.valueOf(watch));
                    JsonRequestHelper.writeJsonResponse(response,
                            JsonRequestHelper.createJsonSuccessResponse(null, (ObjectNode) node));
                } else {
                    LOGGER.debug("Note ID and/or watch flag not provided or of invalid type");
                    errorCode = HttpServletResponse.SC_BAD_REQUEST;
                }
            } catch (NotFoundException e) {
                LOGGER.debug("Note {} does not exist", noteIdNode);
                errorCode = HttpServletResponse.SC_NOT_FOUND;
            } catch (AuthorizationException e) {
                LOGGER.debug(
                        "Current user is not allowed to start or stop watching discussion of note {}",
                        noteIdNode);
                errorCode = HttpServletResponse.SC_FORBIDDEN;
            } catch (IOException e) {
                errorCode = HttpServletResponse.SC_BAD_REQUEST;
            }
        } else {
            LOGGER.debug("Only POST requests supported");
            errorCode = HttpServletResponse.SC_BAD_REQUEST;
        }
        if (errorCode > 0) {
            response.setContentType("application/json");
            response.sendError(errorCode);
        }
        return null;
    }

}
