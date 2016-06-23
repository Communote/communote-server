package com.communote.server.web.fe.portal.blog.helper;

import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.FileUtils;
import org.codehaus.jackson.node.ObjectNode;

import com.communote.server.api.core.attachment.AttachmentData;
import com.communote.server.api.util.JsonHelper;
import com.communote.server.core.retrieval.helper.AttachmentHelper;
import com.communote.server.model.attachment.Attachment;

/**
 * Helper class for blog post creation.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class CreateBlogPostFeHelper {

    /**
     * Name of parameter which holds the (unique) key for storing IDs of uploaded attachments in the
     * session.
     */
    public static final String PARAM_ATTACHMENTS_SESSION_ID = "attachmentUploadSessionId";

    /**
     * Name of parameter which holds the ID of the widget that made the upload.
     */
    public static final String PARAM_UPLOADING_WIDGET_ID = "uploadingWidgetId";

    /**
     * Creates a JSON object that describes an attachment.
     * 
     * @param attachment
     *            the attachment
     * @return the create JSON object
     */
    public static ObjectNode createAttachmentJSONObject(Attachment attachment) {
        return createAttachmentJSONObject(attachment.getId(),
                AttachmentHelper.determineRelativeAttachmentUrl(attachment), attachment.getName(),
                AttachmentHelper.determineKnownMimeType(attachment), attachment.getSize());
    }

    /**
     * Creates a JSON object that describes an attachment.
     * 
     * @param attachment
     *            the attachment
     * @return the create JSON object
     */
    public static ObjectNode createAttachmentJSONObject(AttachmentData attachment) {
        return createAttachmentJSONObject(attachment.getId(),
                AttachmentHelper.determineRelativeAttachmentUrl(attachment),
                attachment.getFileName(), AttachmentHelper.determineKnownMimeType(attachment),
                attachment.getSize());
    }

    /**
     * Creates a JSON object that describes an attachment.
     * 
     * @param id
     *            the id of the attachment
     * @param url
     *            the download URL for the attachment
     * @param fileName
     *            the file name of the attachment
     * @param mimeType
     *            the mime type of the attachment
     * @param size
     *            the size in bytes
     * @return the create JSON object
     */
    private static ObjectNode createAttachmentJSONObject(Long id, String url, String fileName,
            String mimeType, Long size) {
        ObjectNode result = JsonHelper.getSharedObjectMapper().createObjectNode();
        result.put("id", id);
        result.put("url", url);
        result.put("fileName", fileName);
        result.put("mime", mimeType);
        result.put("size", FileUtils.byteCountToDisplaySize(size));
        return result;
    }

    /**
     * Returns a set with attachment IDs uploaded in the current session. The list stored in the
     * session is identified by the value of the request parameter
     * {@link #PARAM_ATTACHMENTS_SESSION_ID}. If the parameter is not provided null is returned. If
     * the list is not yet in the session it will be put there.
     * 
     * @param request
     *            the servlet request
     * @return the set with attachment IDs or null if the identifier is not one of the request
     *         parameters
     */
    @SuppressWarnings(value = { "unchecked" })
    public static Set<Long> getUploadedAttachmentsFromSession(HttpServletRequest request) {
        String attribute = request.getParameter(PARAM_ATTACHMENTS_SESSION_ID);
        Set<Long> attachmentIds = null;
        if (attribute != null) {
            HttpSession session = request.getSession(true);
            Object value = session.getAttribute(attribute);
            if (value == null) {
                attachmentIds = new HashSet<Long>();
                session.setAttribute(attribute, attachmentIds);
            } else {
                attachmentIds = (Set<Long>) value;
            }

        }
        return attachmentIds;
    }

    /**
     * Removes the attachment IDs set from the current session.
     * 
     * @param request
     *            the servlet request
     */
    public static void removeUploadedAttachmentsFromSession(HttpServletRequest request) {
        String attribute = request.getParameter(PARAM_ATTACHMENTS_SESSION_ID);
        if (attribute != null) {
            if (request.getSession(false) != null) {
                request.getSession().removeAttribute(attribute);
            }
        }
    }

    /**
     * Empty constructor to avoid instances of utility class.
     */
    private CreateBlogPostFeHelper() {
        // Do nothing.
    }
}
