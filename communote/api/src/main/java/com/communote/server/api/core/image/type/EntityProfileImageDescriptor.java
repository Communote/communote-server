package com.communote.server.api.core.image.type;

import com.communote.common.image.ImageSize;
import com.communote.server.api.core.config.ClientConfigurationPropertyConstant;
import com.communote.server.model.user.ImageSizeType;

/**
 * Descriptor for images of entities like topics or tags. The profile images of users are covered by
 * a separate type ({@link UserImageDescriptor}).
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class EntityProfileImageDescriptor extends CustomizableImageTypeDescriptor {

    private static final class ProfileCustomizationProperty implements
            ClientConfigurationPropertyConstant {
        @Override
        public String getKeyString() {
            return "communote.image.scaling.entityProfile";
        }
    }

    /** Dimension for the large profile image. */
    private final static ImageSize SIZE_PROFILE_LARGE = new ImageSize(Integer.getInteger(
            "com.communote.entity.profile.large.width", 160), Integer.getInteger(
                    "com.communote.entity.profile.large.height", 120));
    private final static ImageSize SIZE_PROFILE_SMALL = new ImageSize(Integer.getInteger(
            "com.communote.entity.profile.small.width", 40), Integer.getInteger(
                    "com.communote.entity.profile.small.height", 30));

    /**
     * Name of this type.
     */
    public static final String IMAGE_TYPE_NAME = "entity-profile";

    public EntityProfileImageDescriptor() {
        super(new ProfileCustomizationProperty());
    }

    @Override
    public String getName() {
        return IMAGE_TYPE_NAME;
    }

    @Override
    public ImageSize getSizeForImageSizeType(ImageSizeType sizeType) {
        if (ImageSizeType.SMALL.equals(sizeType)) {
            return SIZE_PROFILE_SMALL;
        }
        return SIZE_PROFILE_LARGE;
    }

}
