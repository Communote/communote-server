package com.communote.server.core.user;

import java.util.List;
import java.util.Map;

import com.communote.server.api.core.image.ImageVO;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.api.core.user.UserNotFoundException;
import com.communote.server.core.filter.listitems.UserProfileDetailListItem;
import com.communote.server.core.messaging.vo.MessagerConnectorConfigTO;
import com.communote.server.model.messaging.MessagerConnectorConfig;
import com.communote.server.model.messaging.MessagerConnectorType;
import com.communote.server.model.user.UserProfile;
import com.communote.server.persistence.user.UserProfileVO;

/**
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public interface UserProfileManagement {
    /**
     * @param notificationConfigId
     *            Id of the notification config.
     * @param config
     *            The configuration.
     */
    public void addMessagingConnectorConfig(Long notificationConfigId,
            MessagerConnectorConfig config);

    /**
     * <p>
     * Makes a user profile anonymous.
     * </p>
     * 
     * @param profileId
     *            Id of the profile.
     */
    public void anonymizeUserProfile(Long profileId);

    /**
     * <p>
     * Finds the user profile to a user.
     * </p>
     * 
     * @param userId
     *            The users id.
     * @return The profile.
     */
    public UserProfile findUserProfileByUserId(Long userId);

    /**
     * @param userId
     *            The users id.
     * @return The Profile.
     */
    public UserProfileDetailListItem findUserProfileDetailListItemByUserId(Long userId);

    /**
     * @param userId
     *            The users id.
     * @return The profile.
     */
    public UserProfileVO findUserProfileVOByUserId(Long userId);

    /**
     * @param id
     *            the ID of the user
     * @return the profile details of the user identified by the provided ID
     * @deprecated Use {@link #getUserProfileDetailsById(Long, boolean)} instead.
     */
    @Deprecated
    public UserProfileDetails getUserProfileDetailsById(Long id);

    /**
     * @param id
     *            the ID of the user
     * @param needsExternalAuthentications
     *            Set to true, if the external authentications are needed for the user.
     * @return the profile details of the user identified by the provided ID
     */
    public UserProfileDetails getUserProfileDetailsById(Long id,
            boolean needsExternalAuthentications);

    /**
     * Returns the messager connector configurations of the current user.
     * 
     * @return the messager connector configurations of the current user
     */
    public List<MessagerConnectorConfigTO> getMessagerConnectorConfigs();

    /**
     * Return whether a user uploaded a custom image.
     * 
     * @param userId
     *            the ID of the user
     * @return true if the user exists and has a custom image, false otherwise
     */
    boolean hasCustomUserImage(Long userId);

    /**
     * Get the user image
     * 
     * @param userId
     *            The users id.
     * @return The image.
     */
    public ImageVO loadImage(Long userId);

    /**
     * Remove the image of a user.
     * 
     * @param userId
     *            The ID of the user whose image should be removed
     * @return true if the image was removed or false if the user did not have a custom image
     * @throws AuthorizationException
     *             in case the current user is not the user whose image should be updated or the
     *             current user is not client manager or internal system user
     * @throws UserNotFoundException
     *             in case the user with ID userId does not exist
     */
    public boolean removeUserImage(Long userId) throws AuthorizationException,
            UserNotFoundException;

    /**
     * Update the image of a user.
     * 
     * @param userId
     *            The users id.
     * @param largeUserImage
     *            The user image as byte array.
     * @throws AuthorizationException
     *             in case the current user is not the user whose image should be updated or the
     *             current user is not client manager or internal system user
     * @throws UserNotFoundException
     *             in case the user with ID userId does not exist
     */
    public void updateImage(Long userId, byte[] largeUserImage) throws AuthorizationException,
            UserNotFoundException;

    /**
     * Updates the notification configuration of the current user by adding new, removing or
     * updating existing messager connector configurations.
     * 
     * @param connectorConfigs
     *            a definition with the new connector configurations. Configurations for connector
     *            types that are not yet part of the notification configuration of the current user
     *            will be added. Configurations with connector types that are part of the current
     *            notification configuration but that are not listed in the mapping will be removed.
     *            All the the other configurations will be updated if necessary.
     */
    public void updateNotificationConfig(
            Map<MessagerConnectorType, MessagerConnectorConfigTO> connectorConfigs);

    /**
     * Updates the user profile in the database.
     * 
     * @param userId
     *            The users id.
     * @param userProfile
     *            The profile.
     */
    public void updateUserProfile(Long userId, UserProfileVO userProfile);
}
