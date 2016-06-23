package com.communote.server.core.image.type;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.communote.common.image.ImageHelper;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.image.ByteArrayImage;
import com.communote.server.api.core.image.Image;
import com.communote.server.api.core.image.ImageNotFoundException;
import com.communote.server.api.core.image.ImageProvider;
import com.communote.server.api.core.image.ImageVO;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.api.core.user.DetailedUserData;
import com.communote.server.core.user.UserManagement;
import com.communote.server.core.user.UserProfileManagement;
import com.communote.server.core.vo.query.converters.UserToUserDataQueryResultConverter;
import com.communote.server.service.UserProfileService;

/**
 * Local image provider which loads the user profile images from the user profile database table.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class UserImageProvider extends ImageProvider {

    /**
     * The identifier to use in the image descriptor to refer to the the default image.
     */
    public static final String DEFAULT_IMAGE_IDENTIFIER = "default";

    /**
     * Identifier of the built-in user profile image provider
     */
    public static final String PROVIDER_IDENTIFIER = "coreUser";
    private static final Logger LOGGER = LoggerFactory.getLogger(UserImageProvider.class);

    /**
     * @param pathToDefaultImage
     *            If the path starts with file: it is interpreted as a file URI otherwise it is
     *            interpreted as the name of a resource containing the default image. This resource
     *            will be loaded with the class loader of this class. If null, there will be no
     *            default image.
     */
    public UserImageProvider(String pathToDefaultImage) {
        super(PROVIDER_IDENTIFIER, pathToDefaultImage);
    }

    @Override
    public boolean canLoad(String imageIdentifier) {
        // can always load the database
        return true;
    }

    @Override
    public String getVersionString(String imageIdentifier) throws AuthorizationException,
            ImageNotFoundException {
        // use the last modification timestamp
        try {
            Long userId = Long.parseLong(imageIdentifier);
            if (ServiceLocator.findService(UserProfileService.class).hasCustomUserImage(userId)) {
                DetailedUserData user = ServiceLocator.findService(UserManagement.class).getUserById(
                        userId, new UserToUserDataQueryResultConverter());
                // if the user did yet not upload an image timestamp is null
                if (user != null && user.getLastPhotoModificationDate() != null) {
                    return String.valueOf(user.getLastPhotoModificationDate().getTime());
                }
            }
        } catch (NumberFormatException e) {
            LOGGER.debug("Cannot load user image for identifier {}", imageIdentifier);
        }
        throw new ImageNotFoundException("There is no image for user " + imageIdentifier);
    }

    @Override
    public boolean isAuthorized(String imageIdentifier) {
        // there are no restrictions
        return true;
    }

    @Override
    public boolean isExternalProvider() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Image loadImage(String imageIdentifier) throws ImageNotFoundException,
            AuthorizationException {
        // do not try the default image
        if (!UserImageProvider.DEFAULT_IMAGE_IDENTIFIER.equals(imageIdentifier)) {
            try {
                ImageVO userImage = ServiceLocator.findService(UserProfileManagement.class)
                        .loadImage(Long.parseLong(imageIdentifier));
                if (userImage != null) {
                    String mimeType = ImageHelper.getMimeType(userImage.getImage());
                    if (mimeType != null) {
                        return new ByteArrayImage(userImage.getImage(), mimeType,
                                userImage.getLastModificationDate(), getIdentifier(), false);
                    }
                }
            } catch (NumberFormatException e) {
                LOGGER.debug("Not a valid user ID provided {}", imageIdentifier);
            }
        }
        throw new ImageNotFoundException("There is no valid image for the user " + imageIdentifier);
    }
}
