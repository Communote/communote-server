package com.communote.server.core.image.type;

import java.util.Date;

import com.communote.common.image.ImageHelper;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.image.ByteArrayImage;
import com.communote.server.api.core.image.Image;
import com.communote.server.api.core.image.ImageNotFoundException;
import com.communote.server.api.core.image.ImageProvider;
import com.communote.server.api.core.image.ImageVO;
import com.communote.server.api.core.security.AuthorizationException;

/**
 * Provider that loads the images of entities like topics or tags from binary properties.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class EntityImageProvider extends ImageProvider {

    private final EntityImageManagement.ImageType type;
    private EntityImageManagement entityImageService;

    /**
     * Identifier of the built-in entity image provider
     */
    public static final String PROVIDER_IDENTIFIER = "coreEntity";

    /**
     * Constructor.
     * 
     * @param pathToDefaultImage
     *            If the path starts with file: it is interpreted as a file URI otherwise it is
     *            interpreted as the name of a resource containing the default image. This resource
     *            will be loaded with the class loader of this class. If null, there will be no
     *            default image.
     * @param type
     *            The type of the image to load for the entity
     */
    public EntityImageProvider(String pathToDefaultImage, EntityImageManagement.ImageType type) {
        super(PROVIDER_IDENTIFIER, pathToDefaultImage);
        this.type = type;
    }

    @Override
    public boolean canLoad(String imageIdentifier) {
        return true;
    }

    /**
     * Convert the VO to a ByteArrayImage
     * 
     * @param imageVO
     *            the VO to convert can be null
     * @param defaultImage
     *            whether the imageVO represents the default image
     * @return the image or null
     */
    private ByteArrayImage convertToImage(ImageVO imageVO, boolean defaultImage) {
        if (imageVO != null) {
            String mimeType = ImageHelper.getMimeType(imageVO.getImage());
            if (mimeType != null) {
                return new ByteArrayImage(imageVO.getImage(), mimeType,
                        imageVO.getLastModificationDate(), this.getIdentifier(), defaultImage);
            }
        }
        return null;
    }

    @Override
    public String getDefaultImageVersionString() throws ImageNotFoundException {
        Date lastModified = getEntityImageService().getDefaultImageLastModified(type);
        if (lastModified == null) {
            return super.getDefaultImageVersionString();
        }
        return String.valueOf(lastModified.getTime());
    }

    /**
     * @return the lazily initialized entity image service
     */
    private EntityImageManagement getEntityImageService() {
        if (entityImageService == null) {
            entityImageService = ServiceLocator.findService(EntityImageManagement.class);
        }
        return entityImageService;
    }

    @Override
    public String getVersionString(String imageIdentifier) throws AuthorizationException,
            ImageNotFoundException {
        if (EntityImageManagement.DEFAULT_IMAGE_ENTITY_ID.equals(imageIdentifier)) {
            return getDefaultImageVersionString();
        }
        Date lastModified = getEntityImageService().getImageLastModified(imageIdentifier, type);
        if (lastModified == null) {
            throw new ImageNotFoundException("The " + type + " image with ID " + imageIdentifier
                    + " does not exist");
        }
        return String.valueOf(lastModified.getTime());
    }

    @Override
    public boolean isAuthorized(String imageIdentifier) {
        return getEntityImageService().hasReadAccess(imageIdentifier);
    }

    @Override
    public boolean isExternalProvider() {
        return false;
    }

    @Override
    public Image loadDefaultImage() throws ImageNotFoundException {
        ByteArrayImage image = convertToImage(getEntityImageService().getDefaultImage(type), true);
        if (image != null) {
            return image;
        }
        return super.loadDefaultImage();
    }

    @Override
    public Image loadImage(String imageIdentifier) throws ImageNotFoundException,
            AuthorizationException {
        // default image should be handled by getDefaultImage
        if (!imageIdentifier.equals(EntityImageManagement.DEFAULT_IMAGE_ENTITY_ID)) {
            ByteArrayImage image = convertToImage(
                    getEntityImageService().getImage(imageIdentifier, type), false);
            if (image != null) {
                return image;
            }
        }
        throw new ImageNotFoundException("The " + type + " image with ID " + imageIdentifier
                + " does not exist");
    }
}
