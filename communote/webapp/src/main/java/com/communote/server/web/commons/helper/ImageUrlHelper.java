package com.communote.server.web.commons.helper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.image.ImageManager;
import com.communote.server.api.core.image.type.EntityBannerImageDescriptor;
import com.communote.server.api.core.image.type.EntityProfileImageDescriptor;
import com.communote.server.api.core.image.type.UserImageDescriptor;
import com.communote.server.core.image.CoreImageType;
import com.communote.server.core.image.type.ClientImageDescriptor;
import com.communote.server.core.image.type.EntityImageManagement;
import com.communote.server.core.image.type.UserImageProvider;
import com.communote.server.model.user.ImageSizeType;
import com.communote.server.persistence.user.client.ClientHelper;

/**
 * Helper for building image URLs.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class ImageUrlHelper {

    private static final String IMAGE_PATH_PREFIX = "/image/";
    private static final Logger LOGGER = LoggerFactory.getLogger(ImageUrlHelper.class);
    private static EntityImageManagement GLOBAL_ID_IMAGE_MANAGEMENT;

    private static ImageManager IMAGE_MANAGEMENT;

    /**
     * Create a relative image url for the given parameters
     *
     * @param entityId
     *            Id
     * @param imageType
     *            Image Type
     * @param imageSizeType
     *            Image size
     * @return Image Url
     */
    public static String buildImageUrl(String entityId, CoreImageType imageType,
            ImageSizeType imageSizeType) {
        StringBuilder url = new StringBuilder(IMAGE_PATH_PREFIX);
        String versionString = null;
        switch (imageType) {
        case userlogo:
            url.append("user.jpg?id=" + entityId);
            try {
                versionString = getImageManagement().getImageVersionString(
                        UserImageDescriptor.IMAGE_TYPE_NAME, entityId);
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
            url.append("&");
            break;
        case clientlogo:
            url.append("client.jpg?");
            try {
                versionString = getImageManagement().getImageVersionString(
                        ClientImageDescriptor.IMAGE_TYPE_NAME, ClientHelper.getCurrentClientId());
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
            break;
        case entityBanner:
            if (!EntityImageManagement.DEFAULT_IMAGE_ENTITY_ID.equals(entityId)
                    && !getGlobalIdImageManagement().hasCustomImage(entityId,
                            EntityImageManagement.ImageType.BANNER)) {
                entityId = EntityImageManagement.DEFAULT_IMAGE_ENTITY_ID;
            }
            url.append("entity-banner.jpg?id=" + entityId + "&");
            try {
                versionString = getImageManagement().getImageVersionString(
                        EntityBannerImageDescriptor.IMAGE_TYPE_NAME, entityId);
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
            break;
        case entityProfile:
            if (!EntityImageManagement.DEFAULT_IMAGE_ENTITY_ID.equals(entityId)
                    && !getGlobalIdImageManagement().hasCustomImage(entityId,
                            EntityImageManagement.ImageType.PROFILE)) {
                entityId = EntityImageManagement.DEFAULT_IMAGE_ENTITY_ID;
            }
            url.append("entity-profile.jpg?id=" + entityId + "&");
            try {
                // TODO should be some constant
                versionString = getImageManagement().getImageVersionString(
                        EntityProfileImageDescriptor.IMAGE_TYPE_NAME, entityId);
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
            break;
        }
        url.append("size=");
        url.append(imageSizeType);
        if (versionString != null) {
            url.append("&lastModified=");
            url.append(versionString);
        }
        return url.toString();
    }

    /**
     * Create a relative image url for the given parameters
     *
     * @param userId
     *            User Id
     * @param imageType
     *            Image Type
     * @param imageSizeType
     *            Image size
     * @return Image Url
     */
    public static String buildUserImageUrl(Long userId, ImageSizeType imageSizeType) {
        return ImageUrlHelper
                .buildImageUrl(userId != null ? userId.toString()
                        : UserImageProvider.DEFAULT_IMAGE_IDENTIFIER, CoreImageType.userlogo,
                        imageSizeType);
    }

    /**
     * @return the lazily initialized image management
     */
    private static EntityImageManagement getGlobalIdImageManagement() {
        if (GLOBAL_ID_IMAGE_MANAGEMENT == null) {
            GLOBAL_ID_IMAGE_MANAGEMENT = ServiceLocator.findService(EntityImageManagement.class);
        }
        return GLOBAL_ID_IMAGE_MANAGEMENT;
    }

    /**
     * @return the lazily initialized image management
     */
    private static ImageManager getImageManagement() {
        if (IMAGE_MANAGEMENT == null) {
            IMAGE_MANAGEMENT = ServiceLocator.findService(ImageManager.class);

        }
        return IMAGE_MANAGEMENT;
    }

    /**
     * Private to avoid construction
     */
    private ImageUrlHelper() {
    }

}
