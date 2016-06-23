package com.communote.server.api.core.image.type;

import com.communote.common.image.ImageSize;
import com.communote.server.api.core.config.ClientConfigurationPropertyConstant;
import com.communote.server.model.user.ImageSizeType;

/**
 * Descriptor for banner images of entities like topics, tags or users.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
// Note: banner and profile are different types because there are different (unscaled) images for
// each type
public class EntityBannerImageDescriptor extends CustomizableImageTypeDescriptor {

    private static final class BannerCustomizationProperty implements
            ClientConfigurationPropertyConstant {
        @Override
        public String getKeyString() {
            return "communote.image.scaling.entityBanner";
        }
    }

    private final static ImageSize BANNER_SIZE = new ImageSize(Integer.getInteger(
            "com.communote.entity.banner.width", 960), Integer.getInteger(
                    "com.communote.entity.banner.height", 100));

    /**
     * Name of this type.
     */
    public static final String IMAGE_TYPE_NAME = "entity-banner";

    public EntityBannerImageDescriptor() {
        super(new BannerCustomizationProperty());
    }

    @Override
    public String getName() {
        return IMAGE_TYPE_NAME;
    }

    @Override
    public ImageSize getSizeForImageSizeType(ImageSizeType sizeType) {
        // just one size
        return BANNER_SIZE;
    }
}
