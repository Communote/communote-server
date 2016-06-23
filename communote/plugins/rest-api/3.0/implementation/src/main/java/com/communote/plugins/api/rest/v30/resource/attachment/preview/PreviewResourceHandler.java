package com.communote.plugins.api.rest.v30.resource.attachment.preview;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.io.IOUtils;

import com.communote.plugins.api.rest.v30.resource.DefaultParameter;
import com.communote.plugins.api.rest.v30.resource.DefaultResourceHandler;
import com.communote.plugins.api.rest.v30.resource.attachment.CreateAttachmentParameter;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.common.NotFoundException;
import com.communote.server.api.core.image.Image;
import com.communote.server.api.core.image.ImageManager;
import com.communote.server.api.core.image.type.AttachmentImageDescriptor;
import com.communote.server.core.storing.ResourceStoringManagement;
import com.communote.server.core.vo.content.AttachmentTO;
import com.communote.server.model.user.ImageSizeType;

/**
 * Resource Handler for attachment previews.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class PreviewResourceHandler
        extends
        DefaultResourceHandler<CreateAttachmentParameter, DefaultParameter, DefaultParameter, GetPreviewParameter, DefaultParameter> {

    /**
     * {@inheritDoc}
     */
    @Override
    protected Response handleGetInternally(GetPreviewParameter getParameter,
            String requestedMimeType, UriInfo uriInfo, String requestSessionId, Request request)
            throws Exception {
        Long attachmentId = getParameter.getAttachmentId();
        AttachmentTO attachment = ServiceLocator.findService(ResourceStoringManagement.class)
                .getAttachment(attachmentId);
        if (attachment == null) {
            throw new NotFoundException("The attachment with id " + attachmentId
                    + " can't be found");
        }
        if (!attachment.getMetadata().getMimeType().startsWith("image/")) {
            throw new NoPreviewAvailableException(attachmentId);
        }
        ImageManager imageManagement = ServiceLocator.findService(ImageManager.class);
        Image image = imageManagement.getImage(AttachmentImageDescriptor.IMAGE_TYPE_NAME,
                attachmentId.toString(), ImageSizeType.SMALL);
        ResponseBuilder responseBuilder = request.evaluatePreconditions(image
                .getLastModificationDate());
        if (responseBuilder != null) {
            return responseBuilder.build();
        }
        InputStream inputStream = new ByteArrayInputStream(image.getBytes());
        try {
            return Response
                    .ok(inputStream)
                    .type(image.getMimeType())
                    .header("Content-Disposition",
                            "inline; filename=preview" + attachmentId + ".jpg")
                    .lastModified(image.getLastModificationDate()).build();
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
    }
}
