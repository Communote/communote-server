package com.communote.server.core.user;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Hibernate;
import org.springframework.transaction.annotation.Transactional;

import com.communote.common.string.StringHelper;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.image.ImageVO;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.api.core.tag.TagData;
import com.communote.server.api.core.user.UserNotFoundException;
import com.communote.server.core.filter.listitems.UserProfileDetailListItem;
import com.communote.server.core.messaging.NotificationManagement;
import com.communote.server.core.messaging.vo.MessagerConnectorConfigTO;
import com.communote.server.core.security.SecurityHelper;
import com.communote.server.model.messaging.MessagerConnectorConfig;
import com.communote.server.model.messaging.MessagerConnectorType;
import com.communote.server.model.tag.Tag;
import com.communote.server.model.user.Contact;
import com.communote.server.model.user.Country;
import com.communote.server.model.user.ExternalUserAuthentication;
import com.communote.server.model.user.NotificationConfig;
import com.communote.server.model.user.User;
import com.communote.server.model.user.UserImage;
import com.communote.server.model.user.UserProfile;
import com.communote.server.model.user.UserStatus;
import com.communote.server.persistence.user.UserProfileVO;

/**
 * @see com.communote.server.core.user.UserProfileManagement
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class UserProfileManagementImpl extends UserProfileManagementBase {

    // TODO this is a hack. Better solution would be if there were a MessagerConnector for the mail
    // notification and the service layer would call the enableUser method for all connectors.
    // Moreover it could still be necessary that a connector does not need to enable a user, but
    // this should be modeled as a property of the ConnectorType (not really possible with andromda)
    private final Set<MessagerConnectorType> connectorRequiresUserEnable = new HashSet<MessagerConnectorType>();
    {
        connectorRequiresUserEnable.add(MessagerConnectorType.XMPP);
    }

    private User assertValidUser(Long userId) throws AuthorizationException, UserNotFoundException {
        User user = getUserDao().load(userId);
        if (user == null) {
            throw new UserNotFoundException("User with ID " + userId + " does not exist");
        }
        if (!userId.equals(SecurityHelper.getCurrentUserId()) && !SecurityHelper.isInternalSystem()
                && !SecurityHelper.isClientManager()) {
            throw new AuthorizationException(
                    "Only the user himself or the internal system or client manager is allowed to perfom this operation");
        }
        return user;
    }

    /**
     * Returns the notification configuration of the current user.
     *
     * @param userId
     *            the ID of the user for which the notification config is to be retrieved
     * @param createIfMissing
     *            whether to create a configuration if the user does not yet have one
     * @return the configuration or null if the user has none and createIfMissing was false
     */
    private NotificationConfig getNotificationConfigOfUser(Long userId, boolean createIfMissing) {
        NotificationConfig nc = null;
        User user = getUserDao().load(userId);
        if (user != null) {
            nc = user.getProfile().getNotificationConfig();
            if (createIfMissing && nc == null) {
                // TODO refactor: this is copy/paste of UserManagementImpl.handleSaveNewKenmeiUser
                // which itself is copy/paste of UserDaoImpl.getDefaultNotificationConfig
                nc = NotificationConfig.Factory.newInstance();
                nc.setFallback("mail");
                nc = getNotificationConfigDao().create(nc);
                user.getProfile().setNotificationConfig(nc);
            }
        }
        return nc;
    }

    /**
     * * {@inheritDoc}
     *
     * @return return getUserProfileDetailsById(userId, true);
     */
    @Override
    @Transactional(readOnly = true)
    public UserProfileDetails getUserProfileDetailsById(Long userId) {
        return getUserProfileDetailsById(userId, true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public UserProfileDetails getUserProfileDetailsById(Long userId,
            boolean needExternalAuthentications) {
        if (userId == null) {
            throw new IllegalArgumentException("userId cannot be null.");
        }

        User user = null;
        UserProfileDetails userProfileDetails = null;

        user = getUserDao().load(userId);

        // ignoring registered users since they have no alias or profile
        if (user != null && !UserStatus.REGISTERED.equals(user.getStatus())) {
            userProfileDetails = new UserProfileDetails(user.getId(), user.getAlias());
            userProfileDetails.setFirstName(user.getProfile().getFirstName());
            userProfileDetails.setLastName(user.getProfile().getLastName());
            userProfileDetails.setSalutation(user.getProfile().getSalutation());
            userProfileDetails.setTimeZoneId(user.getProfile().getTimeZoneId());
            userProfileDetails.setUserLocale(user.getLanguageLocale());
            userProfileDetails.setUserStatus(user.getStatus());
            if (needExternalAuthentications && user.getExternalAuthentications() != null) {
                for (ExternalUserAuthentication auth : user.getExternalAuthentications()) {
                    userProfileDetails.addExternalUserId(auth.getSystemId(),
                            auth.getExternalUserId());
                }
            }
        }

        return userProfileDetails;
    }

    /**
     * {@inheritDoc}
     */
    // TODO remove this method and use the updateNotificationConfiguration - requires refactoring of
    // UserManagementImpl.handleSaveNewKenmeiUser and several methods of UserDaoImpl
    @Override
    protected void handleAddMessagingConnectorConfig(Long notificationConfigId,
            MessagerConnectorConfig config) {
        NotificationConfig notificationConfig = getNotificationConfigDao().load(
                notificationConfigId);
        notificationConfig.getConfigs().add(config);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void handleAnonymizeUserProfile(Long profileId) {
        UserProfile profile = getUserProfileDao().load(profileId);
        if (profile != null) {
            profile.setFirstName(null);
            profile.setLastName(null);
            profile.setCompany(null);
            Contact contact = profile.getContact();
            if (contact != null) {
                profile.setContact(null);
                getContactDao().remove(contact);
            }
            profile.setPosition(null);
            profile.setSalutation(null);
            removeImages(profile);
            profile.setLastModificationDate(new Timestamp(new Date().getTime()));
            getUserProfileDao().update(profile);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected UserProfile handleFindUserProfileByUserId(Long userId) {
        User user = getUserDao().load(userId);
        if (user != null) {
            UserProfile profile = user.getProfile();
            if (profile.getContact() != null) {
                if (!Hibernate.isInitialized(profile.getContact())) {
                    Hibernate.initialize(profile.getContact());
                    if (profile.getContact().getCountry() != null) {
                        if (!Hibernate.isInitialized(profile.getContact().getCountry())) {
                            Hibernate.initialize(profile.getContact().getCountry());
                        }
                    }
                }
            }
            return profile;
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected UserProfileDetailListItem handleFindUserProfileDetailListItemByUserId(Long userId) {
        User user = getUserDao().load(userId);
        if (user == null || user.getProfile() == null) {
            return null;
        }
        UserProfileDetailListItem result = new UserProfileDetailListItem();
        UserProfile profile = user.getProfile();
        result.setAlias(user.getAlias());
        result.setFirstName(profile.getFirstName());
        result.setLastName(profile.getLastName());
        result.setSalutation(profile.getSalutation());
        result.setPosition(profile.getPosition());
        result.setCompany(profile.getCompany());
        if (profile.getContact() != null) {
            result.setStreet(profile.getContact().getStreet());
            result.setZip(profile.getContact().getZip());
            result.setCity(profile.getContact().getCity());
            result.setPhone(profile.getContact().getPhone());
            result.setFax(profile.getContact().getFax());
            if (profile.getContact().getCountry() != null) {
                result.setCountryCode(profile.getContact().getCountry().getCountryCode());
            }
        }
        for (Tag tag : user.getTags()) {
            result.getTags().add(new TagData(tag));
        }
        result.setTimeZoneId(profile.getTimeZoneId());
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected UserProfileVO handleFindUserProfileVOByUserId(Long userId) {
        User user = getUserDao().load(userId);
        if (user != null && user.getProfile() != null) {
            return getUserProfileDao().toUserProfileVO(user.getProfile());
        }
        return new UserProfileVO();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<MessagerConnectorConfigTO> handleGetMessagerConnectorConfigs() {
        List<MessagerConnectorConfigTO> configs = new ArrayList<MessagerConnectorConfigTO>();
        Long userId = SecurityHelper.assertCurrentUserId();
        NotificationConfig nc = getNotificationConfigOfUser(userId, false);
        if (nc != null) {
            for (MessagerConnectorConfig conf : nc.getConfigs()) {
                Map<String, String> properties = new HashMap<String, String>();
                if (StringUtils.isNotEmpty(conf.getProperties())) {
                    properties = StringHelper.getStringAsMap(conf.getProperties());
                }
                MessagerConnectorConfigTO to = new MessagerConnectorConfigTO(conf.getType(),
                        conf.getPriority(), properties, conf.isOnlyIfAvailable());
                configs.add(to);

            }
        }
        return configs;
    }

    @Override
    @Transactional(readOnly = true)
    protected boolean handleHasCustomUserImage(Long userId) {
        User user = getUserDao().load(userId);
        if (user != null) {
            UserProfile profile = user.getProfile();
            if (profile != null) {
                return profile.getLargeImage() != null;
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ImageVO handleLoadImage(Long userId) {
        User user = getUserDao().load(userId);
        if (user == null) {
            return null;
        }
        UserProfile profile = user.getProfile();
        UserImage image = profile.getLargeImage();
        ImageVO imageVO = null;
        if (image != null) {
            Hibernate.initialize(image);
            imageVO = new ImageVO();
            imageVO.setLastModificationDate(profile.getLastPhotoModificationDate());
            imageVO.setImage(image.getImage());
        }
        return imageVO;
    }

    @Override
    protected boolean handleRemoveUserImage(Long userId) throws AuthorizationException,
    UserNotFoundException {
        User user = assertValidUser(userId);
        boolean changed = false;
        if (user.getProfile() != null) {
            changed = removeImages(user.getProfile());
        }
        return changed;
    }

    @Override
    protected void handleUpdateImage(Long userId, byte[] userImage) throws UserNotFoundException,
    AuthorizationException {
        User user = assertValidUser(userId);

        UserProfile profile = user.getProfile();
        if (profile == null) {
            profile = UserProfile.Factory.newInstance();
            profile.setLastModificationDate(new Timestamp(new Date().getTime()));
            user.setProfile(profile);
            getUserProfileDao().create(profile);
        }
        profile.setLastPhotoModificationDate(new Timestamp(new Date().getTime()));

        if (profile.getLargeImage() == null) {
            UserImage large = UserImage.Factory.newInstance();
            large.setImage(userImage);
            getUserImageDao().create(large);
            profile.setLargeImage(large);
        } else {
            profile.getLargeImage().setImage(userImage);
            getUserImageDao().update(profile.getLargeImage());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void handleUpdateNotificationConfig(
            Map<MessagerConnectorType, MessagerConnectorConfigTO> connectorConfigs) {
        Long userId = SecurityHelper.assertCurrentUserId();
        NotificationConfig nc = getNotificationConfigOfUser(userId, true);
        Map<MessagerConnectorType, MessagerConnectorConfig> existingConfigs;
        existingConfigs = new HashMap<MessagerConnectorType, MessagerConnectorConfig>();
        if (nc.getConfigs() != null) {
            for (MessagerConnectorConfig mcc : nc.getConfigs()) {
                existingConfigs.put(mcc.getType(), mcc);
            }
        }
        // remove configs that where not passed to the method
        for (MessagerConnectorType type : existingConfigs.keySet()) {
            if (!connectorConfigs.containsKey(type)) {
                MessagerConnectorConfig config = existingConfigs.get(type);
                nc.getConfigs().remove(config);
                getMessagerConnectorConfigDao().remove(config);
                if (connectorRequiresUserEnable.contains(type)) {
                    NotificationManagement msgManagement = ServiceLocator
                            .findService(NotificationManagement.class);
                    msgManagement.disableUser(userId, type.toString());
                }
            }
        }
        // update changed and add new configs
        for (MessagerConnectorType type : connectorConfigs.keySet()) {
            internalUpdateMessagerConnectorConfig(nc, existingConfigs.get(type),
                    connectorConfigs.get(type), userId);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void handleUpdateUserProfile(Long userId, UserProfileVO userProfile) {
        User user = getUserDao().load(userId);
        if (user.getProfile() == null) {
            user.setProfile(UserProfile.Factory.newInstance());
        }
        getUserProfileDao().userProfileVOToEntity(userProfile, user.getProfile(), true);
        user.getProfile().setLastModificationDate(new Timestamp(new Date().getTime()));
        // save profile
        if (user.getProfile().getId() != null) {
            getUserProfileDao().update(user.getProfile());
        } else {
            getUserProfileDao().create(user.getProfile());
        }
        if (user.getProfile().getContact() != null) {
            Contact contact = user.getProfile().getContact();
            if (contact.getId() != null) {
                getContactDao().update(contact);
            } else {
                getContactDao().create(contact);
            }
        }
        // find country by code results in database flush
        if (userProfile.getCountryCode() != null) {
            Country country = null;
            if (userProfile.getCountryCode() != null) {
                country = ServiceLocator.findService(MasterDataManagement.class).findCountryByCode(
                        userProfile.getCountryCode());
            }
            if (user.getProfile().getContact() == null) {
                user.getProfile().setContact(Contact.Factory.newInstance());
            }
            user.getProfile().getContact().setCountry(country);
            if (user.getProfile().getContact().getId() != null) {
                getContactDao().update(user.getProfile().getContact());
            } else {
                getContactDao().create(user.getProfile().getContact());
            }
        }
    }

    /**
     * Updates or creates a messager connector configuration
     *
     * @param nc
     *            the notification configuration of the current user
     * @param existingConfig
     *            the existing messager connector configuration to update, if null a new one will be
     *            created
     * @param newConfig
     *            the new configuration data to set
     * @param userId
     *            the ID of the current user
     */
    private void internalUpdateMessagerConnectorConfig(NotificationConfig nc,
            MessagerConnectorConfig existingConfig, MessagerConnectorConfigTO newConfig, Long userId) {
        String flatProperties = null;

        if (StringUtils.isNotEmpty(StringHelper.toString(newConfig.getProperties()))) {
            flatProperties = StringHelper.toString(newConfig.getProperties());
        }

        if (existingConfig == null) {
            // create new config
            existingConfig = MessagerConnectorConfig.Factory.newInstance(newConfig.getType(),
                    flatProperties, newConfig.isOnlyIfAvailable(), newConfig.getPriority());
            existingConfig = getMessagerConnectorConfigDao().create(existingConfig);
            nc.getConfigs().add(existingConfig);
            // enable user if necessary
            if (connectorRequiresUserEnable.contains(newConfig.getType())) {
                ServiceLocator.findService(NotificationManagement.class).enableUser(userId,
                        newConfig.getType().toString());
            }
        } else {
            existingConfig.setProperties(flatProperties);
            existingConfig.setOnlyIfAvailable(newConfig.isOnlyIfAvailable());
            existingConfig.setPriority(newConfig.getPriority());
        }
    }

    /**
     * Remove the user images from a user profile.
     *
     * @param profile
     *            the profile to update
     * @return whether something changed
     */
    private boolean removeImages(UserProfile profile) {
        boolean changed = false;
        if (profile.getSmallImage() != null) {
            getUserImageDao().remove(profile.getSmallImage());
            profile.setSmallImage(null);
            changed = true;
        }
        if (profile.getLargeImage() != null) {
            getUserImageDao().remove(profile.getLargeImage());
            profile.setLargeImage(null);
            changed = true;
        }
        if (profile.getMediumImage() != null) {
            getUserImageDao().remove(profile.getMediumImage());
            profile.setMediumImage(null);
            changed = true;
        }
        if (changed) {
            profile.setLastPhotoModificationDate(new Timestamp(new Date().getTime()));
        }
        return changed;
    }

}
