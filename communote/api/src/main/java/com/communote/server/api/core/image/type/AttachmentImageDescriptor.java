package com.communote.server.api.core.image.type;

import com.communote.common.image.ImageSize;
import com.communote.server.model.user.ImageSizeType;

/**
 * Descriptor for previews of attachments which are images.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class AttachmentImageDescriptor extends DefaultImageTypeDescriptor {

    private final static ImageSize PREVIEW_SIZE = new ImageSize(200, 133);

    /**
     * Name of this type.
     */
    public static final String IMAGE_TYPE_NAME = "attachment";

    @Override
    public String getName() {
        return IMAGE_TYPE_NAME;
    }

    @Override
    public ImageSize getSizeForImageSizeType(ImageSizeType sizeType) {
        return PREVIEW_SIZE;
    }

    @Override
    public boolean isDrawBackground() {
        return false;
    }
}
