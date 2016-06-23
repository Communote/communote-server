package com.communote.server.core.retrieval.helper;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.attachment.AttachmentData;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.core.security.ssl.ChannelManagement;
import com.communote.server.core.storing.ResourceStoringManagement;
import com.communote.server.model.attachment.Attachment;
import com.communote.server.model.security.ChannelType;
import com.communote.server.persistence.user.client.ClientUrlHelper;

/**
 * Helper class for attachments
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public final class AttachmentHelper {

    /** Logger. */
    private final static Logger LOG = Logger.getLogger(AttachmentHelper.class);
    /**
     * Changing this also check CreateNote.js
     */
    public static final String ICON_MIMETYPE_PREFIX = "icon20_mimetype-";

    /**
     * Extension of the mime type icon
     */
    public static final String ICON_MIMETYPE_EXT = ".gif";

    /**
     * Application type for unknown stuff
     */
    public static final String MIME_TYPE_APPLICATION_UNKNOWN = "application/unknown";

    private static final String MIME_TYPE_DEFAULT = "default";

    private static final String MIME_TYPE_IMAGE = "image";

    private static final String MIME_TYPE_VIDEO = "video";

    private static final String MIME_TYPE_AUDIO = "audio";

    /**
     * Application type association
     */

    private static final Map<String, String> MIME_TYPES = new HashMap<String, String>();

    // TODO Export this to a properties file.
    static {
        // MS Excel
        MIME_TYPES.put("application/excel", "application-msexcel");
        MIME_TYPES.put("application/msexcel", "application-msexcel");
        MIME_TYPES.put("application/vnd.ms-excel", "application-msexcel");
        MIME_TYPES.put("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "application-msexcel");
        MIME_TYPES.put("application/x-excel", "application-msexcel");
        MIME_TYPES.put("application/vnd.oasis.opendocument.spreadsheet", "application-msexcel");

        // MS Word
        MIME_TYPES.put("application/msword", "application-msword");
        MIME_TYPES.put("text/rtf", "application-msword");
        MIME_TYPES.put("application/vnd.ms-word", "application-msword");
        MIME_TYPES.put("application/vnd.oasis.opendocument.text", "application-msword");
        MIME_TYPES.put("application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "application-msword");

        // MS PowerPoint
        MIME_TYPES.put("application/powerpoint", "application-mspowerpoint");
        MIME_TYPES.put("application/vnd.ms-powerpoint", "application-mspowerpoint");
        MIME_TYPES.put("application/vnd.oasis.opendocument.presentation",
                "application-mspowerpoint");
        MIME_TYPES.put("application/x-mspowerpoint", "application-mspowerpoint");
        MIME_TYPES.put("application/vnd.openxmlformats-officedocument.presentationml.presentation",
                "application-mspowerpoint");

        // Archives (ZIP-Symbol)
        MIME_TYPES.put("application/rar", "application-zip");
        MIME_TYPES.put("application/rar", "application-zip");
        MIME_TYPES.put("application/x-compressed", "application-zip");
        MIME_TYPES.put("application/x-rar-compressed", "application-zip");
        MIME_TYPES.put("application/x-zip", "application-zip");
        MIME_TYPES.put("application/x-zip-compressed", "application-zip");
        MIME_TYPES.put("application/zip", "application-zip");
        MIME_TYPES.put("multipart/x-zip", "application-zip");

        // Additional
        MIME_TYPES.put("application/pdf", "application-pdf");
        MIME_TYPES.put("text/plain", "text-plain");
        MIME_TYPES.put("audio/x-ms-wmv", "video");
    }

    /**
     * Copies all attachments of a note and returns an array containing the attachment ids of the
     * copies.
     *
     * @param attachments
     *            Attachments, which should be copied
     * @return return the array with attachment ids or null
     * @throws AuthorizationException
     *             in case there is no current user
     */
    public static Long[] copyAttachments(Collection<Attachment> attachments)
            throws AuthorizationException {
        if (attachments == null) {
            return null;
        }
        ArrayList<Long> attachmentIds = new ArrayList<Long>(attachments.size());
        for (Attachment contentRes : attachments) {
            Attachment copy = ServiceLocator.findService(ResourceStoringManagement.class)
                    .storeCopyOfAttachment(contentRes);
            attachmentIds.add(copy.getId());
        }
        return attachmentIds.toArray(new Long[attachmentIds.size()]);
    }

    /**
     * Determine the absolute URL Path for a resource
     *
     * @param resource
     *            the Item to get the absolute URL for accessing this file
     * @return the absolute URL as a String
     */
    public static String determineAbsoluteAttachmentUrl(Attachment resource) {
        return determineAttachmentUrl(resource.getId(), resource.getName(), true, false);
    }

    /**
     * Determine the absolute URL Path for a resource
     *
     * @param resource
     *            the Item to get the absolute URL for accessing this file
     * @param isSecure
     *            whether the URL should be rendered with the HTTPS or HTTP protocol. If the
     *            configuration states that HTTPS is not supported, the returned URL will use the
     *            HTTP protocol.
     * @return the absolute URL as a String
     */
    public static String determineAbsoluteAttachmentUrl(Attachment resource, boolean isSecure) {
        return determineAttachmentUrl(resource.getId(), resource.getName(), true, isSecure);
    }

    /**
     * Determine the absolute URL Path for a resource
     *
     * @param resource
     *            the Item to get the absolute URL for accessing this file
     * @param isSecure
     *            whether the URL should be rendered with the HTTPS or HTTP protocol. If the
     *            configuration states that HTTPS is not supported, the returned URL will use the
     *            HTTP protocol.
     * @return the absolute URL as a String
     */
    public static String determineAbsoluteAttachmentUrl(AttachmentData resource,
            boolean isSecure) {
        return determineAttachmentUrl(resource.getId(), resource.getFileName(), true, isSecure);
    }

    /**
     * Determine the url of the given resource
     *
     * @param id
     *            the id of the resource
     * @param fileName
     *            the file name of the resource
     * @param useAbsoluteUrl
     *            true to render absolute url
     * @param isSecure
     *            whether the URL should be rendered with the HTTPS or HTTP protocol.
     * @return the url
     */
    private static String determineAttachmentUrl(Long id, String fileName, boolean useAbsoluteUrl,
            boolean isSecure) {
        String url = StringUtils.EMPTY;
        if (useAbsoluteUrl) {
            // use security settings as defined in channel settings for the client (the channel is
            // always web since it is a URL in /portal/....)
            url = ClientUrlHelper
                    .renderConfiguredAbsoluteUrl(null, isSecure
                            || ServiceLocator.findService(ChannelManagement.class).isForceSsl(
                                    ChannelType.WEB));
        }
        String encodeFilename = "download.file";
        try {
            encodeFilename = URLEncoder.encode(fileName, "UTF-8");
            int lastDot = encodeFilename.lastIndexOf(".");
            if (lastDot != -1) {
                encodeFilename = encodeFilename.substring(0, lastDot).replace(".", "_")
                        + encodeFilename.substring(lastDot);
            }
        } catch (UnsupportedEncodingException e) {
            // This exception is no problem, as the filename in the url is not important for the
            // final download.
            LOG.warn("There was a problem encoding the files name: " + e.getMessage());
        }
        return url + "/portal/files/" + id + "/" + encodeFilename;
    }

    /**
     * Match the contentype of the attachment to the known mime types
     *
     * @param attachment
     *            the attachment to use
     * @return the mime type
     */
    public static String determineKnownMimeType(Attachment attachment) {
        return determineKnownMimeType(attachment.getContentType());
    }

    /**
     * Match the contentype of the attachment to the known mime types
     *
     * @param attachment
     *            the attachment to use
     * @return the mime type
     */
    public static String determineKnownMimeType(AttachmentData attachment) {
        return determineKnownMimeType(attachment.getMimeTyp());
    }

    /**
     * Match the mime type to the known mime types
     *
     * @param mimeType
     *            the mime type to match
     * @return the mime type
     */
    public static String determineKnownMimeType(String mimeType) {
        String knownType = MIME_TYPES.get(mimeType);
        if (knownType != null) {
            return knownType;
        }
        if (mimeType != null && mimeType.startsWith("image/")) {
            knownType = MIME_TYPE_IMAGE;
        } else if (mimeType != null && mimeType.startsWith("video/")) {
            knownType = MIME_TYPE_VIDEO;
        } else if (mimeType != null && mimeType.startsWith("audio/")) {
            knownType = MIME_TYPE_AUDIO;
        } else {
            knownType = MIME_TYPE_DEFAULT;
        }
        return knownType;
    }

    /**
     * Well its not frontend here, but we do this anyway in here
     *
     * @param attachment
     *            the attachment
     * @return the icon name
     */
    public static String determineMimeTypeIconName(Attachment attachment) {
        return ICON_MIMETYPE_PREFIX + determineKnownMimeType(attachment).replace("/", "-")
                + ICON_MIMETYPE_EXT;
    }

    /**
     * Determine the resource url for download/access for the given item
     *
     * @param resource
     *            the resource
     * @return the download url
     */
    public static String determineRelativeAttachmentUrl(Attachment resource) {
        return determineAttachmentUrl(resource.getId(), resource.getName(), false, false);
    }

    /**
     * Determine the resource URL for download/access for the given list item
     *
     * @param resource
     *            the resource list item
     * @return the download url
     */
    public static String determineRelativeAttachmentUrl(AttachmentData resource) {
        return determineAttachmentUrl(resource.getId(), resource.getFileName(), false, false);
    }

    /**
     * Private constructor to avoid instances of utility class.
     */
    private AttachmentHelper() {
        // Do nothing
    }
}
