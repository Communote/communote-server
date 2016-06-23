package com.communote.server.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.communote.common.virusscan.VirusScanner;
import com.communote.common.virusscan.exception.InitializeException;
import com.communote.common.virusscan.exception.VirusFoundException;
import com.communote.common.virusscan.exception.VirusScannerException;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.image.ImageManager;
import com.communote.server.api.core.image.type.UserImageDescriptor;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.api.core.user.UserNotFoundException;
import com.communote.server.core.image.type.UserImageProvider;
import com.communote.server.core.security.SecurityHelper;
import com.communote.server.core.user.UserProfileManagement;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
@Service
public class UserProfileService {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserProfileService.class);

    @Autowired
    private UserProfileManagement userProfileManagement;
    @Autowired
    private ImageManager imageManagement;

    /**
     * Return whether the user uploaded a custom image.
     *
     * @param userId
     *            the ID of the user
     * @return true if the user exists and has a custom image, else false
     */
    public boolean hasCustomUserImage(Long userId) {
        return userProfileManagement.hasCustomUserImage(userId);
    }

    /**
     * Remove the image of a user.
     *
     * @param userId
     *            The ID of the user whose image should be removed
     * @throws AuthorizationException
     *             in case the current user is not the user whose image should be updated or the
     *             current user is not client manager or internal system user
     * @throws UserNotFoundException
     *             in case the user with ID userId does not exist
     */
    public void removeUserImage(Long userId) throws UserNotFoundException, AuthorizationException {
        if (userProfileManagement.removeUserImage(userId)) {
            imageManagement.imageChanged(UserImageDescriptor.IMAGE_TYPE_NAME,
                    UserImageProvider.PROVIDER_IDENTIFIER, userId.toString());
        }
    }

    /**
     * Update the image of a user.
     *
     * @param userId
     *            The users id.
     * @param userImageBytes
     *            The user image as byte array.
     * @throws AuthorizationException
     *             in case the current user is not the user whose image should be updated or the
     *             current user is not client manager or internal system user
     * @throws UserNotFoundException
     *             in case the user with ID userId does not exist
     * @throws VirusScannerException
     *             in case the virus scanner is not correctly setup or the image cannot be processed
     * @throws VirusFoundException
     *             in case a virus was found in the image
     *
     */
    public void storeOrUpdateUserImage(Long userId, byte[] userImageBytes)
            throws UserNotFoundException, AuthorizationException, VirusFoundException,
            VirusScannerException {
        try {
            VirusScanner scanner = ServiceLocator.instance().getVirusScanner();
            if (scanner != null) {
                scanner.scan(userImageBytes);
            } else {
                LOGGER.debug("No virus scan will be executed because the scanner is disabled");
            }
        } catch (InitializeException e) {
            throw new VirusScannerException("Virus scanner not initialized", e);
        } catch (VirusFoundException e) {
            LOGGER.warn("Virus found uploading content. userId="
                    + SecurityHelper.getCurrentUserId() + " " + e.getMessage());
            throw e;
        }

        userProfileManagement.updateImage(userId, userImageBytes);
        imageManagement.imageChanged(UserImageDescriptor.IMAGE_TYPE_NAME,
                UserImageProvider.PROVIDER_IDENTIFIER, userId.toString());
    }
}
