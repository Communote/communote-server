package com.communote.server.web.fe.portal.user.client.forms;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.image.ImageManager;
import com.communote.server.core.image.type.ClientImageDescriptor;
import com.communote.server.model.user.ImageSizeType;

/**
 * Form backing object for client logo update.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class ClientProfileLogoForm {
    private boolean customClientLogo;
    private boolean resetToDefault;

    private ImageManager imageManagement;

    /**
     * the default height of the client logo
     *
     * @return the image height of the client logo
     */
    public int getBestLogoHeight() {
        return getImageManagement().getImageSize(ClientImageDescriptor.IMAGE_TYPE_NAME,
                ImageSizeType.LARGE).getHeight();
    }

    /**
     * the default width of the client logo
     *
     * @return the image width of a the client logo
     */
    public int getBestLogoWidth() {
        return getImageManagement().getImageSize(ClientImageDescriptor.IMAGE_TYPE_NAME,
                ImageSizeType.LARGE).getWidth();
    }

    /**
     * @return lazily initialized image manager
     */
    private ImageManager getImageManagement() {
        if (imageManagement == null) {
            imageManagement = ServiceLocator.findService(ImageManager.class);
        }
        return imageManagement;
    }

    /**
     * Whether there is a custom logo for the client.
     *
     * @return true if the client has a custom logo
     */
    public boolean isCustomClientLogo() {
        return customClientLogo;
    }

    /**
     * Whether the logo should be reset to the default.
     *
     * @return true if the logo should be reset to the default
     */
    public boolean isResetToDefault() {
        return resetToDefault;
    }

    /**
     * Whether there is a custom logo for the client.
     *
     * @param customClientLogo
     *            true if the client has a custom logo
     */
    public void setCustomClientLogo(boolean customClientLogo) {
        this.customClientLogo = customClientLogo;
    }

    /**
     * Whether the logo should be reset to the default.
     *
     * @param resetToDefault
     *            true if the logo should be reset to the default
     */
    public void setResetToDefault(boolean resetToDefault) {
        this.resetToDefault = resetToDefault;
    }
}
