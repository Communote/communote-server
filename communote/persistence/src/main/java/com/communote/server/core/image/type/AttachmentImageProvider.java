package com.communote.server.core.image.type;

import java.io.IOException;

import org.apache.commons.io.output.ByteArrayOutputStream;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.image.ByteArrayImage;
import com.communote.server.api.core.image.Image;
import com.communote.server.api.core.image.ImageNotFoundException;
import com.communote.server.api.core.image.ImageProvider;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.core.blog.AttachmentNotFoundException;
import com.communote.server.core.storing.ResourceStoringManagement;
import com.communote.server.core.vo.content.AttachmentTO;
import com.communote.server.persistence.crc.ContentRepositoryException;

/**
 * Provider to create a preview of an attachment that is an image.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class AttachmentImageProvider extends ImageProvider {
    /**
     * Identifier of the built-in user profile image provider
     */
    public static final String PROVIDER_IDENTIFIER = "coreAttachment";

    /**
     * Constructor.
     *
     * @param pathToDefaultImage
     *            If the path starts with file: it is interpreted as a file URI otherwise it is
     *            interpreted as the name of a resource containing the default image. This resource
     *            will be loaded with the class loader of this class. If null, there will be no
     *            default image.
     */
    public AttachmentImageProvider(String pathToDefaultImage) {
        super(PROVIDER_IDENTIFIER, pathToDefaultImage);
    }

    @Override
    public boolean canLoad(String imageIdentifier) {
        return true;
    }

    @Override
    public String getVersionString(String imageIdentifier) throws AuthorizationException,
            ImageNotFoundException {
        try {
            Long attachmentId = Long.parseLong(imageIdentifier);
            AttachmentTO attachment = ServiceLocator.findService(ResourceStoringManagement.class)
                    .getAttachment(attachmentId);
            return String.valueOf(attachment.getMetadata().getDate().getTime());
        } catch (NumberFormatException | ContentRepositoryException | AttachmentNotFoundException e) {
            throw new ImageNotFoundException("Attachment was not found or accessible", e);
        }
    }

    @Override
    public boolean isAuthorized(String imageIdentifier) {
        try {
            ServiceLocator.findService(ResourceStoringManagement.class).getAttachment(
                    Long.parseLong(imageIdentifier));
        } catch (NumberFormatException | AuthorizationException | ContentRepositoryException
                | AttachmentNotFoundException e) {
            return false;
        }
        return true;
    }

    @Override
    public boolean isExternalProvider() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Image loadImage(String imageIdentifier) throws ImageNotFoundException,
            AuthorizationException {
        AttachmentTO attachment = null;
        try {
            attachment = ServiceLocator.findService(ResourceStoringManagement.class)
                    .getAttachment(Long.parseLong(imageIdentifier));
        } catch (NumberFormatException e) {
            throw new ImageNotFoundException("Image identifier " + imageIdentifier
                    + " is not valid", e);
        } catch (AttachmentNotFoundException | ContentRepositoryException e) {
            throw new ImageNotFoundException("Attachment was not found or accessible", e);
        }
        if (attachment == null || !attachment.getMetadata().getMimeType().startsWith("image/")) {
            throw new ImageNotFoundException("There is no attachment with ID " + imageIdentifier
                    + " whiche has an image mime type");
        }
        // use apache's ByteArrayOutputStream because it handles larger files better
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            attachment.write(outputStream);
        } catch (IOException e) {
            throw new ImageNotFoundException("Converting attachment to byte array failed", e);
        }
        return new ByteArrayImage(outputStream.toByteArray(), attachment.getMetadata()
                .getMimeType(), attachment.getMetadata().getDate(), getIdentifier(), false);
    }
}
