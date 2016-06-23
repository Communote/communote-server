package com.communote.server.core.user;

import java.util.List;
import java.util.Map;

import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.api.core.user.UserNotFoundException;
import com.communote.server.core.filter.listitems.UserProfileDetailListItem;
import com.communote.server.core.messaging.vo.MessagerConnectorConfigTO;
import com.communote.server.model.messaging.MessagerConnectorConfig;
import com.communote.server.model.messaging.MessagerConnectorType;

/**
 * <p>
 * Spring Service base class for
 * <code>com.communote.server.service.user.UserProfileManagement</code>, provides access to all
 * services and entities referenced by this service.
 * </p>
 *
 * @see com.communote.server.core.user.UserProfileManagement
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class UserProfileManagementBase implements UserProfileManagement {

    private com.communote.server.persistence.user.UserDao userDao;

    private com.communote.server.persistence.user.UserProfileDao userProfileDao;

    private com.communote.server.persistence.user.UserImageDao userImageDao;

    private com.communote.server.persistence.user.ContactDao contactDao;

    private com.communote.server.persistence.messaging.config.MessagerConnectorConfigDao messagerConnectorConfigDao;

    private com.communote.server.persistence.user.NotificationConfigDao notificationConfigDao;

    /**
     * @see com.communote.server.core.user.UserProfileManagement#addMessagingConnectorConfig(Long,
     *      MessagerConnectorConfig)
     */
    @Override
    public void addMessagingConnectorConfig(Long notificationConfigId,
            MessagerConnectorConfig config) {
        if (notificationConfigId == null) {
            throw new IllegalArgumentException(
                    this.getClass().getName()
                    + ".addMessagingConnectorConfig(Long notificationConfigId, MessagerConnectorConfig config) - 'notificationConfigId' can not be null");
        }
        if (config == null) {
            throw new IllegalArgumentException(
                    this.getClass().getName()
                    + ".addMessagingConnectorConfig(Long notificationConfigId, MessagerConnectorConfig config) - 'config' can not be null");
        }
        try {
            this.handleAddMessagingConnectorConfig(notificationConfigId, config);
        } catch (RuntimeException rt) {
            throw new com.communote.server.core.user.UserProfileManagementException(
                    "Error performing 'com.communote.server.service.user.UserProfileManagement.addMessagingConnectorConfig(Long notificationConfigId, MessagerConnectorConfig config)' --> "
                            + rt, rt);
        }
    }

    /**
     * @see com.communote.server.core.user.UserProfileManagement#anonymizeUserProfile(Long)
     */
    @Override
    public void anonymizeUserProfile(Long profileId) {
        if (profileId == null) {
            throw new IllegalArgumentException(this.getClass().getName()
                    + ".anonymizeUserProfile(Long profileId) - 'profileId' can not be null");
        }
        try {
            this.handleAnonymizeUserProfile(profileId);
        } catch (RuntimeException rt) {
            throw new com.communote.server.core.user.UserProfileManagementException(
                    "Error performing 'com.communote.server.service.user.UserProfileManagement.anonymizeUserProfile(Long profileId)' --> "
                            + rt, rt);
        }
    }

    /**
     * @see com.communote.server.core.user.UserProfileManagement#findUserProfileByUserId(Long)
     */
    @Override
    public com.communote.server.model.user.UserProfile findUserProfileByUserId(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException(this.getClass().getName()
                    + ".findUserProfileByUserId(Long userId) - 'userId' can not be null");
        }
        try {
            return this.handleFindUserProfileByUserId(userId);
        } catch (RuntimeException rt) {
            throw new com.communote.server.core.user.UserProfileManagementException(
                    "Error performing 'com.communote.server.service.user.UserProfileManagement.findUserProfileByUserId(Long userId)' --> "
                            + rt, rt);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserProfileDetailListItem findUserProfileDetailListItemByUserId(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException(
                    this.getClass().getName()
                    + ".findUserProfileDetailListItemByUserId(Long userId) - 'userId' can not be null");
        }
        try {
            return this.handleFindUserProfileDetailListItemByUserId(userId);
        } catch (RuntimeException rt) {
            throw new UserProfileManagementException(
                    "Error performing 'com.communote.server.service.user."
                            + "UserProfileManagement.findUserProfileDetailListItemByUserId(Long userId)' --> "
                            + rt, rt);
        }
    }

    /**
     * @see com.communote.server.core.user.UserProfileManagement#findUserProfileVOByUserId(Long)
     */
    @Override
    public com.communote.server.persistence.user.UserProfileVO findUserProfileVOByUserId(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException(this.getClass().getName()
                    + ".findUserProfileVOByUserId(Long userId) - 'userId' can not be null");
        }
        try {
            return this.handleFindUserProfileVOByUserId(userId);
        } catch (RuntimeException rt) {
            throw new com.communote.server.core.user.UserProfileManagementException(
                    "Error performing 'com.communote.server.service.user.UserProfileManagement.findUserProfileVOByUserId(Long userId)' --> "
                            + rt, rt);
        }
    }

    /**
     * Gets the reference to <code>contact</code>'s DAO.
     */
    protected com.communote.server.persistence.user.ContactDao getContactDao() {
        return this.contactDao;
    }

    /**
     * Gets the reference to <code>messagerConnectorConfig</code>'s DAO.
     */
    protected com.communote.server.persistence.messaging.config.MessagerConnectorConfigDao getMessagerConnectorConfigDao() {
        return this.messagerConnectorConfigDao;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<MessagerConnectorConfigTO> getMessagerConnectorConfigs() {
        try {
            return this.handleGetMessagerConnectorConfigs();
        } catch (RuntimeException rt) {
            throw new com.communote.server.core.user.UserProfileManagementException(
                    "Error performing 'com.communote.server.service.user."
                            + "UserProfileManagement.getMessagerConnectorConfigs()' --> " + rt, rt);
        }
    }

    /**
     * Gets the reference to <code>notificationConfig</code>'s DAO.
     */
    protected com.communote.server.persistence.user.NotificationConfigDao getNotificationConfigDao() {
        return this.notificationConfigDao;
    }

    /**
     * Gets the current <code>principal</code> if one has been set, otherwise returns
     * <code>null</code>.
     *
     * @return the current principal
     */
    protected java.security.Principal getPrincipal() {
        return com.communote.server.PrincipalStore.get();
    }

    /**
     * Gets the reference to <code>kenmeiUser</code>'s DAO.
     */
    protected com.communote.server.persistence.user.UserDao getUserDao() {
        return this.userDao;
    }

    /**
     * Gets the reference to <code>userImage</code>'s DAO.
     */
    protected com.communote.server.persistence.user.UserImageDao getUserImageDao() {
        return this.userImageDao;
    }

    /**
     * Gets the reference to <code>userProfile</code>'s DAO.
     */
    protected com.communote.server.persistence.user.UserProfileDao getUserProfileDao() {
        return this.userProfileDao;
    }

    /**
     * Performs the core logic for
     * {@link #addMessagingConnectorConfig(Long, MessagerConnectorConfig)}
     */
    protected abstract void handleAddMessagingConnectorConfig(Long notificationConfigId,
            MessagerConnectorConfig config);

    /**
     * Performs the core logic for {@link #anonymizeUserProfile(Long)}
     */
    protected abstract void handleAnonymizeUserProfile(Long profileId);

    /**
     * Performs the core logic for {@link #findUserProfileByUserId(Long)}
     */
    protected abstract com.communote.server.model.user.UserProfile handleFindUserProfileByUserId(
            Long userId);

    /**
     * Performs the core logic for {@link #findUserProfileDetailListItemByUserId(Long)}
     *
     * @param userId
     *            the ID of the user
     * @return the profile details of the user identified by the provided ID
     */
    protected abstract UserProfileDetailListItem handleFindUserProfileDetailListItemByUserId(
            Long userId);

    /**
     * Performs the core logic for {@link #findUserProfileVOByUserId(Long)}
     */
    protected abstract com.communote.server.persistence.user.UserProfileVO handleFindUserProfileVOByUserId(
            Long userId);

    /**
     * Performs the core logic for {@link #getMessagerConnectorConfigs()}
     *
     * @return the messager connector configurations of the current user
     */
    protected abstract List<MessagerConnectorConfigTO> handleGetMessagerConnectorConfigs();

    /**
     * @return whether the user has a custom image
     */
    protected abstract boolean handleHasCustomUserImage(Long userId);

    /**
     * Performs the core logic for
     * {@link #loadImage(Long, com.communote.server.persistence.user.ImageSizeType)}
     *
     * @param userId
     *            The users id.
     * @return The image of the user.
     */
    protected abstract com.communote.server.api.core.image.ImageVO handleLoadImage(Long userId);

    /**
     * Remove the image of a user.
     *
     * @param userId
     *            The ID of the user whose image should be removed
     * @return true if the image was removed or false if the user did not have a custom image
     * @throws AuthorizationException
     *             in case the current user is not the user whose image should be removed or client
     *             manager or internal system user
     * @throws UserNotFoundException
     *             in case the user with ID userId does not exist
     */
    protected abstract boolean handleRemoveUserImage(Long userId) throws AuthorizationException,
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
    protected abstract void handleUpdateImage(Long userId, byte[] userImage)
            throws UserNotFoundException, AuthorizationException;

    /**
     * Performs the core logic for {@link #updateNotificationConfig(Map)}
     *
     * @param connectorConfigs
     *            the new configuration data
     */
    protected abstract void handleUpdateNotificationConfig(
            Map<MessagerConnectorType, MessagerConnectorConfigTO> connectorConfigs);

    /**
     * Performs the core logic for
     * {@link #updateUserProfile(Long, com.communote.server.persistence.user.UserProfileVO)}
     */
    protected abstract void handleUpdateUserProfile(Long userId,
            com.communote.server.persistence.user.UserProfileVO userProfile);

    @Override
    public boolean hasCustomUserImage(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId must not be null");
        }
        return handleHasCustomUserImage(userId);
    }

    /**
     * @see com.communote.server.core.user.UserProfileManagement#loadImage(Long,
     *      com.communote.server.persistence.user.ImageSizeType)
     */
    @Override
    public com.communote.server.api.core.image.ImageVO loadImage(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException(this.getClass().getName()
                    + ".loadImage(userId, ImageSizeType imageSizeType) - 'userId' can not be null");
        }
        try {
            return this.handleLoadImage(userId);
        } catch (RuntimeException rt) {
            throw new com.communote.server.core.user.UserProfileManagementException(
                    "Error performing 'UserProfileManagement.loadImage(Long "
                            + "userId, com.communote.server.persistence.user.ImageSizeType imageSizeType)' --> "
                            + rt, rt);
        }
    }

    @Override
    public boolean removeUserImage(Long userId) throws UserNotFoundException,
    AuthorizationException {
        if (userId == null) {
            throw new IllegalArgumentException(this.getClass().getName()
                    + ".removeUserImage(Long userId) - 'userId' can not be null");
        }
        try {
            return this.handleRemoveUserImage(userId);
        } catch (RuntimeException rt) {
            throw new com.communote.server.core.user.UserProfileManagementException(
                    "Error performing 'com.communote.server.service.user.UserProfileManagement.removeUserImage(Long userId)' --> "
                            + rt, rt);
        }
    }

    /**
     * Sets the reference to <code>contact</code>'s DAO.
     */
    public void setContactDao(com.communote.server.persistence.user.ContactDao contactDao) {
        this.contactDao = contactDao;
    }

    /**
     * Sets the reference to <code>messagerConnectorConfig</code>'s DAO.
     */
    public void setMessagerConnectorConfigDao(
            com.communote.server.persistence.messaging.config.MessagerConnectorConfigDao messagerConnectorConfigDao) {
        this.messagerConnectorConfigDao = messagerConnectorConfigDao;
    }

    /**
     * Sets the reference to <code>notificationConfig</code>'s DAO.
     */
    public void setNotificationConfigDao(
            com.communote.server.persistence.user.NotificationConfigDao notificationConfigDao) {
        this.notificationConfigDao = notificationConfigDao;
    }

    /**
     * Sets the reference to <code>kenmeiUser</code>'s DAO.
     */
    public void setUserDao(com.communote.server.persistence.user.UserDao userDao) {
        this.userDao = userDao;
    }

    /**
     * Sets the reference to <code>userImage</code>'s DAO.
     */
    public void setUserImageDao(com.communote.server.persistence.user.UserImageDao userImageDao) {
        this.userImageDao = userImageDao;
    }

    /**
     * Sets the reference to <code>kenmeiUserProfile</code>'s DAO.
     */
    public void setUserProfileDao(
            com.communote.server.persistence.user.UserProfileDao userProfileDao) {
        this.userProfileDao = userProfileDao;
    }

    @Override
    public void updateImage(Long userId, byte[] userImage) throws UserNotFoundException,
    AuthorizationException {
        if (userId == null) {
            throw new IllegalArgumentException(this.getClass().getName()
                    + ".updateImage(Long userId, byte[] largeUserImage) - 'userId' can not be null");
        }
        if (userImage == null) {
            throw new IllegalArgumentException(
                    this.getClass().getName()
                    + ".updateImage(Long userId, byte[] userImage) - 'largeUserImage' can not be null");
        }
        try {
            this.handleUpdateImage(userId, userImage);
        } catch (RuntimeException rt) {
            throw new UserProfileManagementException(
                    "Error performing 'com.communote.server.service.user.UserProfileManagement.updateImage(Long userId, byte[] smallUserImage, byte[] largeUserImage, byte[] mediumImage)' --> "
                            + rt, rt);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateNotificationConfig(
            Map<MessagerConnectorType, MessagerConnectorConfigTO> connectorConfigs) {
        if (connectorConfigs == null) {
            throw new IllegalArgumentException(
                    this.getClass().getName()
                    + ".updateNotificationConfig(Map<MessagerConnectorType, MessagerConnectorConfigTO>"
                    + " - 'connectorConfigs' can not be null");
        }
        try {
            this.handleUpdateNotificationConfig(connectorConfigs);
        } catch (RuntimeException rt) {
            throw new UserProfileManagementException(
                    "Error performing 'com.communote.server.service.user.UserProfileManagement"
                            + ".updateNotificationConfig(Map<MessagerConnectorType, MessagerConnectorConfigTO>"
                            + " connectorConfigs)' --> " + rt, rt);
        }
    }

    /**
     * @see com.communote.server.core.user.UserProfileManagement#updateUserProfile(Long,
     *      com.communote.server.persistence.user.UserProfileVO)
     */
    @Override
    public void updateUserProfile(Long userId,
            com.communote.server.persistence.user.UserProfileVO userProfile) {
        if (userId == null) {
            throw new IllegalArgumentException(
                    this.getClass().getName()
                    + ".updateUserProfile(Long userId, com.communote.server.persistence.user.UserProfileVO userProfile) - 'userId' can not be null");
        }
        if (userProfile == null) {
            throw new IllegalArgumentException(
                    this.getClass().getName()
                    + ".updateUserProfile(Long userId, com.communote.server.persistence.user.UserProfileVO userProfile) - 'userProfile' can not be null");
        }
        try {
            this.handleUpdateUserProfile(userId, userProfile);
        } catch (RuntimeException rt) {
            throw new com.communote.server.core.user.UserProfileManagementException(
                    "Error performing 'com.communote.server.service.user.UserProfileManagement.updateUserProfile(Long userId, com.communote.server.persistence.user.UserProfileVO userProfile)' --> "
                            + rt, rt);
        }
    }
}