package com.communote.server.core.template.velocity;

import java.util.TimeZone;

import org.apache.velocity.tools.config.DefaultKey;
import org.apache.velocity.tools.generic.DateTool;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;

import com.communote.common.string.StringEscapeHelper;
import com.communote.server.api.core.blog.MinimalBlogData;
import com.communote.server.api.core.note.NoteRenderContext;
import com.communote.server.api.core.note.NoteRenderMode;
import com.communote.server.api.core.user.UserData;
import com.communote.server.api.util.JsonHelper;
import com.communote.server.core.user.UserManagementHelper;

/**
 * Velocity tool with useful functions for note template rendering.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
@DefaultKey("templateTool")
public class NoteTemplateTool {

    private final DateTool dateTool = new DateTool();

    /**
     * Create a JSON with blog details.
     *
     * @param blog
     *            the blog to process
     * @param xmlEncode
     *            whether characters that are reserved in XML should be encoded in the returned
     *            string
     * @return the JSON object as string
     */
    public String blogAsJson(MinimalBlogData blog, boolean xmlEncode) {
        // TODO should we handle the DELETED/REGISTERED status?
        ObjectMapper mapper = JsonHelper.getSharedObjectMapper();
        ObjectNode rootNode = mapper.getNodeFactory().objectNode();
        if (blog != null) {
            // the attributes are the same as in REST API
            rootNode.put("alias", blog.getAlias());
            rootNode.put("title", blog.getTitle());
            rootNode.put("topicId", blog.getId());
            // TODO add anything else?
        }
        String jsonString = JsonHelper.writeJsonTreeAsString(rootNode);
        if (xmlEncode) {
            jsonString = StringEscapeHelper.escapeXml(jsonString);
        }
        return jsonString;
    }

    /**
     * Render a date localized date.
     *
     * @param dateStyle
     *            style string as it is supported by {@link DateTool}
     * @param timeStyle
     *            style string as it is supported by {@link DateTool}
     * @param timestamp
     *            the number of milliseconds since epoch
     * @param renderContext
     *            the current render context
     * @return the date string
     */
    public String formatDate(String dateStyle, String timeStyle, Number timestamp,
            NoteRenderContext renderContext) {
        // TODO the user for whom the note is rendered should be set in the renderContext
        TimeZone timezone = UserManagementHelper.getEffectiveUserTimeZone();
        return dateTool
                .format(dateStyle, timeStyle, timestamp, renderContext.getLocale(), timezone);
    }

    /**
     * Helper for checking whether the current render mode is the HTML mode.
     *
     * @param renderContext
     *            the render context
     * @return true if the mode is the HTML mode
     */
    public boolean isHtml(NoteRenderContext renderContext) {
        return NoteRenderMode.HTML.equals(renderContext.getMode());
    }

    /**
     * Helper for checking whether the current render mode is the PLAIN_TEXT mode.
     *
     * @param renderContext
     *            the render context
     * @return true if the mode is the PLAIN_TEXT mode
     */
    public boolean isPlain(NoteRenderContext renderContext) {
        return NoteRenderMode.PLAIN.equals(renderContext.getMode());
    }

    /**
     * Helper for checking whether the current render mode is the PORTAL mode.
     *
     * @param renderContext
     *            the render context
     * @return true if the mode is the PORTAL mode
     */
    public boolean isPortal(NoteRenderContext renderContext) {
        return NoteRenderMode.PORTAL.equals(renderContext.getMode());
    }

    /**
     * Helper for checking whether the current render mode is one of the repost modes.
     *
     * @param renderContext
     *            the render context
     * @return true if the mode is REPOST or REPOST_PLAIN_TEXT mode
     */
    public boolean isRepost(NoteRenderContext renderContext) {
        return isRepostHtml(renderContext) || isRepostPlain(renderContext);
    }

    /**
     * Helper for checking whether the current render mode is one of the repost HTML mode.
     *
     * @param renderContext
     *            the render context
     * @return true if the mode is the REPOST mode
     */
    public boolean isRepostHtml(NoteRenderContext renderContext) {
        return NoteRenderMode.REPOST.equals(renderContext.getMode());
    }

    /**
     * Helper for checking whether the current render mode is the REPOST_PLAIN_TEXT mode.
     *
     * @param renderContext
     *            the render context
     * @return true if the mode is the REPOST_PLAIN_TEXT mode
     */
    public boolean isRepostPlain(NoteRenderContext renderContext) {
        return NoteRenderMode.REPOST_PLAIN.equals(renderContext.getMode());
    }

    /**
     * Create a JSON with user details.
     *
     * @param user
     *            the user to process
     * @param xmlEncode
     *            whether characters that are reserved in XML should be encoded in the returned
     *            string
     * @return the JSON object as string
     */
    public String userAsJson(UserData user, boolean xmlEncode) {
        // TODO should we handle the DELETED/REGISTERED status?
        ObjectMapper mapper = JsonHelper.getSharedObjectMapper();
        ObjectNode rootNode = mapper.getNodeFactory().objectNode();
        if (user != null) {
            // the attributes are the same as in REST API
            rootNode.put("alias", user.getAlias());
            rootNode.put("firstName", user.getFirstName());
            rootNode.put("lastName", user.getLastName());
            rootNode.put("userId", user.getId());
            rootNode.put("salutation", user.getSalutation());
        }
        String jsonString = JsonHelper.writeJsonTreeAsString(rootNode);
        if (xmlEncode) {
            jsonString = StringEscapeHelper.escapeXml(jsonString);
        }
        return jsonString;
    }
}
