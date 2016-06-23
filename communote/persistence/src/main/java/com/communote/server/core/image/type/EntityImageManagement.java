package com.communote.server.core.image.type;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.communote.common.io.MaxLengthReachedException;
import com.communote.common.virusscan.VirusScanner;
import com.communote.common.virusscan.exception.VirusFoundException;
import com.communote.common.virusscan.exception.VirusScannerException;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.blog.BlogRightsManagement;
import com.communote.server.api.core.common.NotFoundException;
import com.communote.server.api.core.config.type.ApplicationProperty;
import com.communote.server.api.core.image.ImageManager;
import com.communote.server.api.core.image.ImageVO;
import com.communote.server.api.core.image.type.EntityBannerImageDescriptor;
import com.communote.server.api.core.image.type.EntityProfileImageDescriptor;
import com.communote.server.api.core.property.PropertyManagement;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.core.security.SecurityHelper;
import com.communote.server.model.property.BinaryProperty;

/**
 * Service for storing and accessing images of entities like users, topics or tags. The images are
 * stored as binary properties.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
@Service
public class EntityImageManagement {

    /** Possible types. */
    public enum ImageType {
        /** Banner. */
        BANNER,
        /** Profile. */
        PROFILE;

        /**
         * @param typeAsString
         *            The type as String.
         * @return The type es enum, defaults to PROFILE when typeAsString is null.
         */
        public static ImageType getType(String typeAsString) {
            if (typeAsString == null) {
                return PROFILE;
            }
            return valueOf(typeAsString.toUpperCase());
        }

    }

    /** Id of the default image. */
    public final static String DEFAULT_IMAGE_ENTITY_ID = "default";

    private final static Logger LOGGER = LoggerFactory.getLogger(EntityImageManagement.class);

    @Autowired
    private ImageManager imageManagement;
    @Autowired
    private PropertyManagement propertyManagement;

    @Autowired
    private BlogRightsManagement blogRightsManagement;

    private void assertReadAccess(String entityId) throws AuthorizationException {
        if (!hasReadAccess(entityId)) {
            throw new AuthorizationException("The current user is not allowed to access the "
                    + " image with ID " + entityId);
        }
    }

    /**
     * This method asserts, that the current user is allowed to alter this image.
     *
     * @param entityId
     *            The entity id.
     * @throws AuthorizationException
     *             Thrown, when the user is not allowed to access this entity.
     */
    private void assertWriteAccess(String entityId) throws AuthorizationException {
        if (DEFAULT_IMAGE_ENTITY_ID.equals(entityId) || entityId.startsWith("tag.")) {
            SecurityHelper.assertCurrentUserIsClientManager();
        }
        if (entityId.startsWith("topic.")) {
            Long topicId = Long.parseLong(entityId.split("\\.")[1]);
            if (!blogRightsManagement.currentUserHasManagementAccess(topicId)) {
                throw new AuthorizationException("The current user ("
                        + SecurityHelper.getCurrentUserId()
                        + ") is not allowed to change the image for " + entityId);
            }
        } else if (entityId.startsWith("user.")) {
            if (!SecurityHelper.isClientManager()
                    && !entityId.equals("user." + SecurityHelper.getCurrentUserId())) {
                throw new AuthorizationException("The current user ("
                        + SecurityHelper.getCurrentUserId()
                        + ") is not allowed to change the image for " + entityId);
            }
        }
    }

    /**
     * Get the default image
     *
     * @param imageType
     *            the type of the image
     * @return the default image or null if there is no default image
     */
    public ImageVO getDefaultImage(ImageType imageType) {
        BinaryProperty property = propertyManagement.getBinaryProperty(
                PropertyManagement.KEY_GROUP,
                getKey(EntityImageManagement.DEFAULT_IMAGE_ENTITY_ID, imageType));
        if (property != null) {
            return new ImageVO(property.getLastModificationDate(), property.getPropertyValue());
        }
        return null;
    }

    /**
     * Get the date of the last modification of the default image of the given type.
     *
     * @param imageType
     *            the image type
     * @return the date of the last modification or null if there is no custom default image for the
     *         type
     */
    public Date getDefaultImageLastModified(ImageType imageType) {
        return propertyManagement.getBinaryPropertyLastModificationDate(
                PropertyManagement.KEY_GROUP,
                getKey(EntityImageManagement.DEFAULT_IMAGE_ENTITY_ID, imageType));
    }

    /**
     * Get the image of the entity
     *
     * @param entityId
     *            the ID of the entity
     * @param imageType
     *            the type of the image
     * @return the image or null if there is no custom image for the entity
     * @throws AuthorizationException
     *             in case the current user is not allowed to access the image
     */
    public ImageVO getImage(String entityId, ImageType imageType) throws AuthorizationException {
        assertReadAccess(entityId);
        BinaryProperty property = propertyManagement.getBinaryProperty(
                PropertyManagement.KEY_GROUP, getKey(entityId, imageType));
        if (property != null) {
            return new ImageVO(property.getLastModificationDate(), property.getPropertyValue());
        }
        return null;
    }

    /**
     * Get the date of the last modification of the custom image of an entity
     *
     * @param entityId
     *            the ID of the entity
     * @param imageType
     *            the type of the image
     * @return the last modification date or null if the entity has no custom image
     * @throws AuthorizationException
     *             in case the current user has no read access to the image
     */
    public Date getImageLastModified(String entityId, ImageType imageType)
            throws AuthorizationException {
        assertReadAccess(entityId);
        return propertyManagement.getBinaryPropertyLastModificationDate(
                PropertyManagement.KEY_GROUP, getKey(entityId, imageType));
    }

    /**
     * Create a property key for the image
     *
     * @param entityId
     *            the identifier of the entity
     * @param type
     *            type of the image
     * @return The key of the binary property containing the image
     */
    private String getKey(String entityId, ImageType type) {
        return "image." + entityId + "." + type;
    }

    /**
     * @param entityId
     *            Id of the entity.
     * @param imageType
     *            Type of the image.
     * @return True, if this entity has a custom image.
     */
    public boolean hasCustomImage(String entityId, ImageType imageType) {
        return propertyManagement.hasBinaryProperty(PropertyManagement.KEY_GROUP,
                getKey(entityId, imageType));
    }

    /**
     * Test whether the current user has read access to the image of the given entity.
     *
     * @param entityId
     *            the ID of the entity
     * @return true if the user has access, false otherwise
     */
    public boolean hasReadAccess(String entityId) {
        if (entityId.startsWith("topic.")) {
            String topicIdString = entityId.substring(6);
            try {
                Long topicId = Long.parseLong(topicIdString);
                return blogRightsManagement.currentUserHasReadAccess(topicId, false);
            } catch (NumberFormatException e) {
                return false;
            }
        }
        Long currentUserId = SecurityHelper.getCurrentUserId();
        return currentUserId != null || SecurityHelper.isInternalSystem()
                || SecurityHelper.isPublicUser();
    }

    /**
     * Inform image management that the image changed
     *
     * @param entityId
     *            the ID of the entity whose image changed. Use null to address the default image.
     * @param imageType
     *            the type of the image
     */
    private void imageChanged(String entityId, ImageType imageType) {
        String typeName;
        if (imageType.equals(ImageType.BANNER)) {
            typeName = EntityBannerImageDescriptor.IMAGE_TYPE_NAME;
        } else {
            typeName = EntityProfileImageDescriptor.IMAGE_TYPE_NAME;
        }
        if (entityId != null && !entityId.equals(DEFAULT_IMAGE_ENTITY_ID)) {
            imageManagement.imageChanged(typeName, EntityImageProvider.PROVIDER_IDENTIFIER,
                    entityId);
        } else {
            imageManagement.defaultImageChanged(typeName, EntityImageProvider.PROVIDER_IDENTIFIER);
        }
    }

    /**
     * Method to reset to default image.
     *
     * @param entityId
     *            Id of the entity to reset.
     * @param imageType
     *            Type to reset.
     * @throws AuthorizationException
     *             Thrown, when the user is not allowed to access this.
     */
    public void setDefault(String entityId, ImageType imageType) throws AuthorizationException {
        assertWriteAccess(entityId);
        propertyManagement.setBinaryProperty(PropertyManagement.KEY_GROUP,
                getKey(entityId, imageType), null);
        imageChanged(null, imageType);
        imageChanged(entityId, imageType);
    }

    /**
     * Method to update an image.
     *
     * @param entityId
     *            Id of the entity the image is for.
     * @param imageType
     *            The type of the image.
     * @param image
     *            The image to store.
     * @throws VirusFoundException
     *             Thrown, when a virus was found.
     * @throws VirusScannerException
     *             Thrown, when something with scanning went wrong.
     * @throws AuthorizationException
     *             Thrown, when the user is not allowed to access this.
     * @throws MaxLengthReachedException
     *             Thrown, when the image is to large.
     * @throws NotFoundException
     *             Thrown, when the entity of the image doesn't exists.
     */
    public void storeImage(String entityId, ImageType imageType, byte[] image)
            throws VirusFoundException, VirusScannerException, AuthorizationException,
            MaxLengthReachedException, NotFoundException {
        VirusScanner scanner = ServiceLocator.instance().getVirusScanner();
        if (scanner != null) {
            scanner.scan(image);
        } else {
            LOGGER.debug("No virus scan will be executed because the scanner is disabled");
        }
        assertWriteAccess(entityId);
        if (image.length > Long.parseLong(CommunoteRuntime.getInstance().getConfigurationManager()
                .getApplicationConfigurationProperties()
                .getProperty(ApplicationProperty.IMAGE_MAX_UPLOAD_SIZE))) {
            throw new MaxLengthReachedException();
        }
        propertyManagement.setBinaryProperty(PropertyManagement.KEY_GROUP,
                getKey(entityId, imageType), image);
        imageChanged(entityId, imageType);
    }
}
