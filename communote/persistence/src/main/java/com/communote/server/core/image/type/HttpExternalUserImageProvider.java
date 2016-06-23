package com.communote.server.core.image.type;

import java.util.Date;
import java.util.HashSet;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContext;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.common.NotFoundException;
import com.communote.server.api.core.image.ImageNotFoundException;
import com.communote.server.api.core.property.PropertyManagement;
import com.communote.server.api.core.property.PropertyType;
import com.communote.server.api.core.property.StringPropertyTO;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.core.image.HttpExternalImageProvider;
import com.communote.server.core.security.AuthenticationHelper;
import com.communote.server.model.property.StringProperty;

/**
 * User image provider that loads images from an external system via HTTP GET requests. The provider
 * uses 2 user properties to hold the version string and the last modification timestamp of a
 * retrieved user image.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 * @param <C>
 *            type of the configuration
 */
public abstract class HttpExternalUserImageProvider<C> extends HttpExternalImageProvider<C> {
    private static final Logger LOGGER = LoggerFactory
            .getLogger(HttpExternalUserImageProvider.class);

    /**
     * Key of the user property that stores version string of the image.
     */
    public static final String USER_PROPERTY_KEY_IMAGE_VERSION_STRING = "image.version.string";
    /**
     * Key of the user property that stores last modification timestamp of the image.
     */
    public static final String USER_PROPERTY_KEY_IMAGE_LAST_MODIFIED = "image.last.modified";

    private final String propertyKeyGroup;
    private PropertyManagement propertyManagement;

    /**
     * Create a new image provider
     * 
     * @param identifier
     *            The identifier of the provider. The identifier has to be unique among all
     *            providers that are registered for an image type.
     * @param pathToDefaultImage
     *            If the path starts with file: it is interpreted as a file URI otherwise it is
     *            interpreted as the name of a resource containing the default image. This resource
     *            will be loaded with the class loader of this class. If null, there will be no
     *            default image.
     * @param disablingDuration
     *            the amount of milliseconds to disable the image provider temporarily in certain
     *            situations e.g. if the external server is unreachable. If 0 or less the provider
     *            is never disabled.
     * @param propertyKeyGroup
     *            key group to be used with the user properties
     *            {@link #USER_PROPERTY_KEY_IMAGE_LAST_MODIFIED} and
     *            {@link #USER_PROPERTY_KEY_IMAGE_VERSION_STRING}. The group and properties have to
     *            be added to the filter definitions of the property management.
     */
    public HttpExternalUserImageProvider(String identifier, String pathToDefaultImage,
            long disablingDuration, String propertyKeyGroup) {
        super(identifier, pathToDefaultImage, disablingDuration);
        this.propertyKeyGroup = propertyKeyGroup;
    }

    /**
     * Get the timestamp of the last configuration change. This timestamp is used to decide whether
     * the image version string property of a user should be refreshed. For instance if the
     * configuration changed after the version timesstamp was added it is likely that another image
     * will be returned. In this case the version string is updated.
     * 
     * @param configuration
     *            the configuration as returned by {@link #getConfiguration()}
     * @return timestamp of the last tracked configuration change or null if this information is not
     *         available
     */
    abstract protected Long getConfigurationChangeTimestamp(C configuration);

    /**
     * @return the property key group
     */
    protected String getPropertyKeyGroup() {
        return propertyKeyGroup;
    }

    /**
     * @return lazily initialized property management
     */
    protected PropertyManagement getPropertyManagement() {
        if (propertyManagement == null) {
            propertyManagement = ServiceLocator.findService(PropertyManagement.class);
        }
        return propertyManagement;
    }

    /**
     * Get the value of the image version string property for a user. If the property of the user
     * was not set or is invalid due to an update of the configuration that happened after storing
     * the version string, a new version string is created and stored.
     * 
     * @param userId
     *            the ID of the user
     * @return the version string or null
     */
    private String getUpdatedVersionStringFromProperty(Long userId, C configuration) {
        try {
            StringProperty property = getPropertyManagement().getObjectProperty(
                    PropertyType.UserProperty, userId, propertyKeyGroup,
                    USER_PROPERTY_KEY_IMAGE_VERSION_STRING);
            String versionString = null;
            if (property != null) {
                versionString = property.getPropertyValue();
                Long configLastModified = getConfigurationChangeTimestamp(configuration);
                if (configLastModified != null
                        && configLastModified > property.getLastModificationDate().getTime()) {
                    LOGGER.trace("Image version string of user {} was updated before "
                            + "configuration change, updating version string", userId);
                    versionString = null;
                }
            }
            // if version string is unset or invalid due to config change, save a new version string
            // that can be reused in subsequent calls
            if (versionString == null) {
                // save current time, rounded to seconds to compensate parallel accesses which would
                // otherwise lead to fluctuating version strings and thus bad client-side caching
                // behavior
                versionString = String.valueOf((System.currentTimeMillis() / 1000) * 1000);
                updateUserProperty(userId, USER_PROPERTY_KEY_IMAGE_VERSION_STRING, versionString);
            }
            return versionString;
        } catch (NotFoundException e) {
            LOGGER.debug("User with ID {} not found", userId);
        } catch (AuthorizationException e) {
            LOGGER.error("Unexpected exception accessing property of user {}", userId, e);
        }
        return null;
    }

    /**
     * Convert the image identifier into a user ID if it is a valid ID.
     * 
     * @param imageIdentifier
     *            the imageIdentifer
     * @return the user ID or null
     */
    protected Long getUserId(String imageIdentifier) {
        try {
            long userId = Long.parseLong(imageIdentifier);
            if (userId >= 0) {
                return userId;
            }
        } catch (NumberFormatException e) {
            LOGGER.debug("Image ID {} is not a user ID", imageIdentifier);
        }
        return null;
    }

    @Override
    public String getVersionString(String imageIdentifier) throws AuthorizationException,
            ImageNotFoundException {
        C config = getConfiguration();
        assertNotDisabled(imageIdentifier, config);
        Long userId = getUserId(imageIdentifier);
        if (userId != null) {
            String versionString = getUpdatedVersionStringFromProperty(userId, config);
            if (versionString != null) {
                return versionString;
            }
        }
        throw new ImageNotFoundException("Cannot create a version string for user with ID "
                + imageIdentifier);
    }

    /**
     * If imageIdentifier is not null the version string property is removed so that a new one is
     * created the next time the image with the identifier is accessed. If imageIdentifier is null
     * the provider is re-enabled if it was temporarily disabled.
     */
    @Override
    public void imageChanged(String imageIdentifier) {

        if (imageIdentifier != null) {
            // remove the version string property so that a new one is generated when accessed the
            // next time
            Long userId = getUserId(imageIdentifier);
            if (userId != null) {
                try {
                    updateUserProperty(userId, USER_PROPERTY_KEY_IMAGE_VERSION_STRING, null);
                } catch (NotFoundException e) {
                    LOGGER.error("User with ID {} does not exist", userId, e);
                }

            }
        } else {
            // not removing properties of all users because it is expensive.
            // Moreover the typical use case to invalidate all images is when the configuration
            // changed, but this is already covered with getConfigurationChangeTimestamp. Only
            // reenable the
            enable();
        }
    }

    @Override
    protected void imageNotDownloaded(String imageIdentifier) {
        Long userId = getUserId(imageIdentifier);
        if (userId != null) {
            try {
                StringProperty lastModifiedProperty = propertyManagement.getObjectProperty(
                        PropertyType.UserProperty, userId, propertyKeyGroup,
                        USER_PROPERTY_KEY_IMAGE_LAST_MODIFIED);
                if (lastModifiedProperty != null) {
                    // the image was successfully downloaded before but now the download failed and
                    // the default image will be returned. Need to update the version string because
                    // default image is another image. Also remove the last modified value.
                    String versionString = String
                            .valueOf((System.currentTimeMillis() / 1000) * 1000);
                    updateUserProperties(userId, null, versionString);
                }
            } catch (NotFoundException e) {
                LOGGER.debug("User {} was not found", userId);
            } catch (AuthorizationException e) {
                LOGGER.error("Unexpected exception reading user property of user {}", userId, e);
            }
        }
    }

    @Override
    protected void imageSuccessfullyDownloaded(String imageIdentifier, Date lastModificationDate,
            CloseableHttpResponse response) {
        Long userId = getUserId(imageIdentifier);
        if (userId != null) {
            String lastModifiedString = String.valueOf(lastModificationDate.getTime());
            try {
                StringProperty lastModifiedProperty = propertyManagement.getObjectProperty(
                        PropertyType.UserProperty, userId, propertyKeyGroup,
                        USER_PROPERTY_KEY_IMAGE_LAST_MODIFIED);
                if (lastModifiedProperty == null
                        || !lastModifiedString.equals(lastModifiedProperty.getPropertyValue())) {
                    // update version string if last modified value changed or was not yet set. The
                    // latter covers the case when a user has no custom image and thus the default
                    // image is returned. The browser would still show the default image if the
                    // version string is not updated.
                    updateUserProperties(userId, lastModifiedString, lastModifiedString);
                }
            } catch (NotFoundException e) {
                LOGGER.debug("User {} was not found", userId);
            } catch (AuthorizationException e) {
                LOGGER.error("Unexpected exception reading user property of user {}", userId, e);
            }
        }
    }

    @Override
    public boolean isAuthorized(String imageIdentifier) {
        return true;
    }

    /**
     * Update the 2 properties of a user.
     * 
     * @param userId
     *            the ID of the user whose properties should be updated
     * @param lastModifiedValue
     *            the value to set for the property with key
     *            {@link #USER_PROPERTY_KEY_IMAGE_LAST_MODIFIED}
     * @param versionStringValue
     *            the value to set for the property with key
     *            {@link #USER_PROPERTY_KEY_IMAGE_VERSION_STRING}
     * @throws NotFoundException
     *             in case the user does not exist
     */
    private void updateUserProperties(Long userId, String lastModifiedValue,
            String versionStringValue) throws NotFoundException {
        HashSet<StringPropertyTO> properties = new HashSet<>();
        properties.add(new StringPropertyTO(lastModifiedValue, propertyKeyGroup,
                USER_PROPERTY_KEY_IMAGE_LAST_MODIFIED, null));
        properties.add(new StringPropertyTO(versionStringValue, propertyKeyGroup,
                USER_PROPERTY_KEY_IMAGE_VERSION_STRING, null));
        SecurityContext context = AuthenticationHelper.setInternalSystemToSecurityContext();
        try {
            getPropertyManagement().setObjectProperties(PropertyType.UserProperty, userId,
                    properties);
        } catch (AuthorizationException e) {
            LOGGER.error("Unexpected exception while updating user properties for user {}",
                    userId, e);
        } finally {
            AuthenticationHelper.setSecurityContext(context);
        }
    }

    /**
     * Update a user property
     * 
     * @param userId
     *            ID of the user whose property should be updated
     * @param key
     *            the key of the property
     * @param value
     *            the value
     * @throws NotFoundException
     *             in case the user does not exist
     */
    protected void updateUserProperty(Long userId, String key, String value)
            throws NotFoundException {
        // user cannot update property of another user, thus do it as confluence image provider
        // (internal system)
        SecurityContext context = AuthenticationHelper.setInternalSystemToSecurityContext();
        try {
            getPropertyManagement().setObjectProperty(PropertyType.UserProperty, userId,
                    propertyKeyGroup, key, value);
        } catch (AuthorizationException e) {
            LOGGER.error("Unexpected exception while updating user property {} for user {}", key,
                    userId, e);
        } finally {
            AuthenticationHelper.setSecurityContext(context);
        }
    }

}
