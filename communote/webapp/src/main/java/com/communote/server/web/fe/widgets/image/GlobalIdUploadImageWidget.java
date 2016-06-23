package com.communote.server.web.fe.widgets.image;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.communote.common.image.ImageSize;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.config.type.ApplicationProperty;
import com.communote.server.api.core.image.ImageManager;
import com.communote.server.api.core.image.type.EntityBannerImageDescriptor;
import com.communote.server.api.core.image.type.EntityProfileImageDescriptor;
import com.communote.server.core.image.type.EntityImageManagement;
import com.communote.server.model.user.ImageSizeType;
import com.communote.server.widgets.annotations.AnnotatedSingleResultWidget;
import com.communote.server.widgets.annotations.ViewIdentifier;

/**
 * This widget is used to upload images for entities with Global Ids.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
@ViewIdentifier("widget.entity.global-id.upload.image")
public class GlobalIdUploadImageWidget extends AnnotatedSingleResultWidget {
    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalIdUploadImageWidget.class);

    private boolean isBanner;
    private boolean isDefaultImage;

    /**
     * Helper method which returns the maximal image upload file size.
     *
     * @return the number of bytes representing the upper limit for image uploads
     */
    public String getMaxUploadSize() {
        return FileUtils.byteCountToDisplaySize(Long.parseLong(CommunoteRuntime.getInstance()
                .getConfigurationManager().getApplicationConfigurationProperties()
                .getProperty(ApplicationProperty.IMAGE_MAX_UPLOAD_SIZE)));
    }

    /**
     * abstract method hook that initializes the widget's parameters
     */
    @Override
    protected void initParameters() {
        // Do nothing.
    }

    /**
     * @return whether the banner or profile image should be uploaded
     */
    public boolean isBanner() {
        return isBanner;
    }

    public boolean isDefaultImage() {
        return isDefaultImage;
    }

    /**
     * @return the result
     */
    @Override
    protected Object processSingleResult() {
        String imageType = getParameter("imageType");
        EntityImageManagement.ImageType imageEntityType = EntityImageManagement.ImageType
                .getType(imageType);
        ImageManager imageManagement = ServiceLocator.findService(ImageManager.class);
        ImageSize size;
        String imageTypeName;
        if (imageEntityType.equals(EntityImageManagement.ImageType.BANNER)) {
            isBanner = true;
            imageTypeName = EntityBannerImageDescriptor.IMAGE_TYPE_NAME;
            size = imageManagement.getImageSize(imageTypeName, ImageSizeType.LARGE);

        } else {
            this.isBanner = false;
            imageTypeName = EntityProfileImageDescriptor.IMAGE_TYPE_NAME;
            size = imageManagement.getImageSize(imageTypeName, ImageSizeType.LARGE);
        }
        if (size != null) {
            setParameter("targetHeight", String.valueOf(size.getHeight()));
            setParameter("targetWidth", String.valueOf(size.getWidth()));
            setResponseMetadata("targetHeight", size.getHeight());
            setResponseMetadata("targetWidth", size.getWidth());
        } else {
            LOGGER.error("There is no image provider for entity type {}", imageEntityType);
            throw new IllegalArgumentException("Unsupported entity type " + imageEntityType);
        }
        String entityId = getParameter("entityId");
        // cannot check Image.isDefaultImage() because the image with ID "default" is a default
        // image even if there is a property for that image
        isDefaultImage = !ServiceLocator.findService(EntityImageManagement.class).hasCustomImage(
                entityId, imageEntityType);
        setParameter("imageType", imageType);
        setParameter("entityId", entityId);
        return null;
    }
}
