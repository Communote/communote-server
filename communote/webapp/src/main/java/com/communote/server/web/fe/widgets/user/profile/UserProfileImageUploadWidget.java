package com.communote.server.web.fe.widgets.user.profile;

import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.communote.common.image.ImageSize;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.config.type.ApplicationProperty;
import com.communote.server.api.core.image.Image;
import com.communote.server.api.core.image.ImageManager;
import com.communote.server.api.core.image.ImageNotFoundException;
import com.communote.server.api.core.image.type.UserImageDescriptor;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.core.security.SecurityHelper;
import com.communote.server.model.user.ImageSizeType;
import com.communote.server.widgets.EmptyWidget;

/**
 * Widget for displaying the upload form for an user image
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class UserProfileImageUploadWidget extends EmptyWidget {

    /** Logger. */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(UserProfileImageUploadWidget.class);
    private int bestImageHeight;
    private int bestImageWidth;
    private boolean externalImage;
    private boolean defaultImage;

    /**
     * the default height of the user image
     *
     * @return the image height of a large user image
     */
    public int getBestImageHeight() {
        return bestImageHeight;
    }

    /**
     * the default width of the user image
     *
     * @return the image width of a large user image
     */
    public int getBestImageWidth() {
        return bestImageWidth;
    }

    /**
     * the image size type of the user image to display inside the user profile view
     *
     * @return the image size
     */
    public ImageSizeType getImageSizeType() {
        return ImageSizeType.LARGE;
    }

    /**
     * Helper method which returns the maximal image upload file size.
     *
     * @return the number of bytes representing the upper limit for image uploads
     */
    private long getMaxUploadSize() {
        return Long.parseLong(CommunoteRuntime.getInstance().getConfigurationManager()
                .getApplicationConfigurationProperties()
                .getProperty(ApplicationProperty.IMAGE_MAX_UPLOAD_SIZE));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTile(String outputType) {
        return "core.widget.user.profile.image";
    }

    /**
     * returns the user ID of the current user
     *
     * @return the ID of the current user
     */
    public Long getUserId() {
        return SecurityHelper.getCurrentUserId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object handleRequest() {
        getRequest().setAttribute("maxImageUploadSize",
                FileUtils.byteCountToDisplaySize(getMaxUploadSize()));
        ImageManager imageManagement = ServiceLocator.findService(ImageManager.class);
        ImageSize large = imageManagement.getImageSize(UserImageDescriptor.IMAGE_TYPE_NAME,
                ImageSizeType.LARGE);
        bestImageHeight = large.getHeight();
        bestImageWidth = large.getWidth();
        String userId = getUserId().toString();
        try {
            Image image = imageManagement.getImage(UserImageDescriptor.IMAGE_TYPE_NAME, userId,
                    ImageSizeType.LARGE);
            externalImage = image.isExternal();
            defaultImage = image.isDefaultImage();
        } catch (ImageNotFoundException | AuthorizationException | IOException e) {
            LOGGER.error("Loading image for user {} failed", e);
            externalImage = false;
            defaultImage = false;
        }
        return super.handleRequest();
    }

    /**
     * Checks for an external image provider
     *
     * @return true if current user image is provided by an external image provider
     */
    public boolean isExternalImage() {
        return externalImage;
    }

    /**
     * Returns true if the user with ID userId has a default image.
     *
     * @return false if the user uploaded a personal image
     */
    public boolean userHasDefaultImage() {
        return defaultImage;
    }
}
