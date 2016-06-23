package com.communote.server.api.core.image.type;

import com.communote.common.image.ImageSize;
import com.communote.server.api.core.config.ClientConfigurationPropertyConstant;
import com.communote.server.model.user.ImageSizeType;

/**
 * Descriptor for user profile pictures.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class UserImageDescriptor extends CustomizableImageTypeDescriptor {

    private static final class UserCustomizationProperty implements
    ClientConfigurationPropertyConstant {
        @Override
        public String getKeyString() {
            return "communote.image.scaling.user";
        }
    }

    /**
     * Name of this type.
     */
    public static final String IMAGE_TYPE_NAME = "user";
    /**
     * Predefined size for a small image.
     */
    private static final ImageSize USER_IMAGE_SIZE_SMALL = new ImageSize(40, 40);

    /**
     * Predefined size for a medium sized image.
     */
    private static final ImageSize USER_IMAGE_SIZE_MEDIUM = new ImageSize(50, 50);

    /**
     * Predefined size for a large image.
     */
    private static final ImageSize USER_IMAGE_SIZE_LARGE = new ImageSize(200, 200);

    public UserImageDescriptor() {
        super(new UserCustomizationProperty());
    }

    @Override
    public String getName() {
        return IMAGE_TYPE_NAME;
    }

    @Override
    public ImageSize getSizeForImageSizeType(ImageSizeType sizeType) {
        if (ImageSizeType.LARGE.equals(sizeType)) {
            return USER_IMAGE_SIZE_LARGE;
        }
        if (ImageSizeType.MEDIUM.equals(sizeType)) {
            return USER_IMAGE_SIZE_MEDIUM;
        }
        return USER_IMAGE_SIZE_SMALL;
    }

}
