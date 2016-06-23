package com.communote.plugins.api.rest.v22.resource.attachment;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.restlet.data.MediaType;

import com.communote.common.io.MaxLengthReachedException;
import com.communote.common.string.StringHelper;
import com.communote.plugins.api.rest.v22.resource.validation.ParameterValidationError;
import com.communote.plugins.api.rest.v22.resource.validation.ParameterValidationException;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.attachment.AttachmentData;
import com.communote.server.api.core.config.type.ApplicationProperty;
import com.communote.server.core.storing.ResourceStoringManagement;
import com.communote.server.core.vo.content.AttachmentTO;

/**
 * Helper for {@link AttachmentResourceHandler}
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public final class AttachmentResourceHelper {

    /**
     * Form type multipart/form-data.
     */
    public static final String MULTIPART_FORM_DATA = MediaType.MULTIPART_FORM_DATA.getName();

    private static final String CONTENT_TYPE_IMAGE_PREFIX = "image";

    /**
     * Check attachment size and throw {@link MaxLengthReachedException}
     *
     * @param contentType
     *            type of content
     * @param fileSize
     *            size of file
     * @param isBase64
     *            is file encoded
     * @throws MaxLengthReachedException
     *             exception for file is to large
     */
    public static void assertAttachmentSize(String contentType, Long fileSize, boolean isBase64)
            throws MaxLengthReachedException {
        long maxAttachmentSize = 0;

        if (contentType.startsWith(CONTENT_TYPE_IMAGE_PREFIX)) {
            maxAttachmentSize = Long.parseLong(CommunoteRuntime.getInstance()
                    .getConfigurationManager().getApplicationConfigurationProperties()
                    .getProperty(ApplicationProperty.IMAGE_MAX_UPLOAD_SIZE));
        } else {
            // check that attachment size is enough
            maxAttachmentSize = Long.parseLong(CommunoteRuntime.getInstance()
                    .getConfigurationManager().getApplicationConfigurationProperties()
                    .getProperty(ApplicationProperty.ATTACHMENT_MAX_UPLOAD_SIZE));
        }

        if (isBase64) {
            // size of byte stream with white spaces is 36% more than without encoding
            maxAttachmentSize = (long) (maxAttachmentSize * 1.36 + 1);
        }

        if (!(maxAttachmentSize >= fileSize)) {
            throw new MaxLengthReachedException();
        }

    }

    /**
     * Converts a list item describing an attachment into an attachment resource
     *
     * @param attachment
     *            a list item to convert
     * @return the created resource
     */
    public static AttachmentResource buildAttachmentResource(AttachmentData attachment) {
        AttachmentResource attachmentResource = new AttachmentResource();
        attachmentResource.setAttachmentId(attachment.getId());
        attachmentResource.setContentLength(attachment.getSize());
        attachmentResource.setFileName(attachment.getFileName());
        attachmentResource.setFileType(attachment.getMimeTyp());
        return attachmentResource;
    }

    /**
     * Builds an attachment resource for REST-API out of the core attachment.
     *
     * @param attachment
     *            the attachment of the core
     * @return attachment resource for REST-API
     */
    public static AttachmentResource buildAttachmentResource(AttachmentTO attachment) {
        AttachmentResource attachmentResource = new AttachmentResource();
        attachmentResource.setContentLength(attachment.getMetadata().getContentSize());
        attachmentResource.setFileName(attachment.getMetadata().getFilename());
        attachmentResource.setFileType(attachment.getMetadata().getMimeType());
        return attachmentResource;
    }

    /**
     * Converts a collection of attachment list items into an array of attachment resources
     *
     * @param attachments
     *            the list items to convert, can be null which will result in an empty array
     * @return the converted resources
     */
    public static AttachmentResource[] buildAttachmentResources(
            Collection<AttachmentData> attachments) {
        List<AttachmentResource> attachmentResources = new ArrayList<AttachmentResource>();
        if (attachments != null) {
            for (AttachmentData attachment : attachments) {
                AttachmentResource attachmentResource = buildAttachmentResource(attachment);
                attachmentResources.add(attachmentResource);
            }
        }
        return attachmentResources.toArray(new AttachmentResource[0]);
    }

    /**
     * Check for attachmentUploadSessionId Parameter and throw {@link ParameterValidationException}
     *
     * @param attachmentUploadSessionId
     *            identifier of attribute in session
     * @throws ParameterValidationException
     *             attachmentUploadSessionId was not set
     */
    public static void checkAttachmentUploadSessionId(String attachmentUploadSessionId)
            throws ParameterValidationException {
        if (StringUtils.isBlank(attachmentUploadSessionId)) {
            List<ParameterValidationError> errors = new ArrayList<ParameterValidationError>();
            ParameterValidationError error = new ParameterValidationError();
            error.setSource("attachmentUploadSessionId");
            error.setMessageKey("restapi.error.attachment.create.noUploadSessionId");
            errors.add(error);
            ParameterValidationException exception = new ParameterValidationException();
            exception.setErrors(errors);
            throw exception;
        }
    }

    /**
     * Builds a byte array response of a given attachment.
     *
     * @param attachment
     *            attachment to be responded
     * @throws IOException
     *             if streaming of the attachment failed.
     *
     * @return response containing the attachment as binary
     */
    public static Response getAttachmentAsBinary(AttachmentTO attachment) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        attachment.write(outputStream);
        outputStream.flush();
        InputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
        String mimeType = attachment.getMetadata().getMimeType();
        String fileName = attachment.getMetadata().getFilename();

        fileName = StringHelper.cleanUpString(fileName, "\"", "-");

        try {
            return Response.ok(inputStream).type(mimeType)
                    .header("Content-Disposition", "inline; filename=\"" + fileName + "\"").build();
        } finally {
            IOUtils.closeQuietly(inputStream);
            IOUtils.closeQuietly(outputStream);
        }
    }

    /**
     * Get the resource storing management.
     *
     * @return resource storing management
     */
    public static ResourceStoringManagement getResourceStoringManagement() {
        return ServiceLocator.findService(ResourceStoringManagement.class);
    }

    /**
     * Default constructor
     */
    private AttachmentResourceHelper() {
        // default constructor
    }

}