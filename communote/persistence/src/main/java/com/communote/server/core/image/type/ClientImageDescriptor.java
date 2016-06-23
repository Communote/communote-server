package com.communote.server.core.image.type;

import java.util.Map;

import com.communote.common.image.ImageSize;
import com.communote.server.api.core.image.type.DefaultImageTypeDescriptor;
import com.communote.server.model.user.ImageSizeType;
import com.communote.server.persistence.user.client.ClientHelper;

/**
 * Descriptor for the client logo.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ClientImageDescriptor extends DefaultImageTypeDescriptor {

    /**
     * Name of this type.
     */
    public static final String IMAGE_TYPE_NAME = "client";

    /**
     * Maximum size for a client logo image.
     */
    private static final ImageSize CLIENT_LOGO_IMAGE_SIZE_LARGE = new ImageSize(220, 40);

    @Override
    public String extractImageIdentifier(Map<String, ? extends Object> parameters) {
        // always return the current client ID no matter what's in the parameters
        return ClientHelper.getCurrentClientId();
    }

    @Override
    public String getName() {
        return IMAGE_TYPE_NAME;
    }

    @Override
    public ImageSize getSizeForImageSizeType(ImageSizeType sizeType) {
        // only supporting one size
        return CLIENT_LOGO_IMAGE_SIZE_LARGE;
    }

}
