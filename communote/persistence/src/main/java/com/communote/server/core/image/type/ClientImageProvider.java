package com.communote.server.core.image.type;

import java.util.Date;

import com.communote.common.image.ImageHelper;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.image.ByteArrayImage;
import com.communote.server.api.core.image.Image;
import com.communote.server.api.core.image.ImageNotFoundException;
import com.communote.server.api.core.image.ImageProvider;
import com.communote.server.api.core.image.ImageVO;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.core.ConfigurationManagement;

/**
 * Provider for the client image. This provider will only load the image of the current client.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ClientImageProvider extends ImageProvider {

    /**
     * Identifier of the built-in client image provider
     */
    public static final String PROVIDER_IDENTIFIER = "coreClient";

    /**
     * Constructor.
     *
     * @param pathToDefaultImage
     *            If the path starts with file: it is interpreted as a file URI otherwise it is
     *            interpreted as the name of a resource containing the default image. This resource
     *            will be loaded with the class loader of this class. If null, there will be no
     *            default image.
     */
    public ClientImageProvider(String pathToDefaultImage) {
        super(PROVIDER_IDENTIFIER, pathToDefaultImage);
    }

    @Override
    public boolean canLoad(String imageIdentifier) {
        return true;
    }

    @Override
    public String getVersionString(String imageIdentifier) throws AuthorizationException,
    ImageNotFoundException {
        Date lastModTime = ServiceLocator.findService(ConfigurationManagement.class)
                .getConfiguration()
                .getClientConfig().getLastLogoImageModificationDate();
        if (lastModTime != null) {
            return String.valueOf(lastModTime.getTime());
        }
        throw new ImageNotFoundException("There is no image for the current client");
    }

    @Override
    public boolean isAuthorized(String imageIdentifier) {
        return true;
    }

    @Override
    public boolean isExternalProvider() {
        return false;
    }

    @Override
    public Image loadImage(String imageIdentifier) throws ImageNotFoundException,
    AuthorizationException {
        ImageVO clientImage = ServiceLocator.findService(ConfigurationManagement.class)
                .getClientLogo();
        if (clientImage != null) {
            String mimeType = ImageHelper.getMimeType(clientImage.getImage());
            if (mimeType != null) {
                return new ByteArrayImage(clientImage.getImage(), mimeType,
                        clientImage.getLastModificationDate(), getIdentifier(), false);
            }
        }
        throw new ImageNotFoundException("There is no image for the current client");
    }
}
