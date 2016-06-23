package com.communote.plugins.api.rest.v30.resource.user;

import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.communote.plugins.api.rest.v30.resource.tag.TagHelper;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.image.ImageManager;
import com.communote.server.api.core.image.ImageNotFoundException;
import com.communote.server.api.core.image.type.UserImageDescriptor;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.core.follow.FollowManagement;
import com.communote.server.core.vo.query.blog.DataAccessBlogConverter;
import com.communote.server.model.user.Contact;
import com.communote.server.model.user.User;
import com.communote.server.model.user.UserProfile;

/**
 * This converter convert a {@link User} to a {@link UserResource}
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 * @param <T>
 *            The User which is the incoming
 * @param <O>
 *            The UserResource which is the final
 */
public class UserResourceConverter<T extends User, O extends UserResource> extends
DataAccessBlogConverter<T, O> {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserResourceConverter.class);

    private final Locale locale;
    private ImageManager imageManagement;

    /**
     * Constructor of {@link UserResourceConverter}
     *
     * @param locale
     *            current user locale
     */
    public UserResourceConverter(Locale locale) {
        super();
        this.locale = locale;
    }

    @Override
    public boolean convert(T source, O target) {
        fillingResultItem(source, target);
        return true;
    }

    @Override
    public O create() {
        return (O) new UserResource();
    }

    /**
     * Filling the PostListItem
     *
     * @param user
     *            The blogListItem with tags.
     * @param userResource
     *            The resource of an blog.
     */
    private void fillingResultItem(T user, O userResource) {
        userResource.setAlias(user.getAlias());
        UserProfile profile = user.getProfile();
        if (profile != null) {
            userResource.setFirstName(profile.getFirstName());
            userResource.setLastName(profile.getLastName());
            userResource.setLastModificationDate(profile.getLastModificationDate());
            // TODO lastPhotoModificationDate is deprecated - remove in next version
            userResource.setLastPhotoModificationDate(profile.getLastPhotoModificationDate());
            userResource.setProfileImageVersion(getImageVersionString(user.getId()));
            userResource.setSalutation(profile.getSalutation());
            userResource.setCompany(profile.getCompany());
            userResource.setPosition(profile.getPosition());
            Contact contact = profile.getContact();
            if (contact != null) {
                if (contact.getPhone() != null) {
                    userResource.setPhoneCountryCode(contact.getPhone().getCountryCode());
                    userResource.setPhoneAreaCode(contact.getPhone().getAreaCode());
                    userResource.setPhoneNumber(contact.getPhone().getPhoneNumber());
                }
                if (contact.getFax() != null) {
                    userResource.setFaxCountryCode(contact.getFax().getCountryCode());
                    userResource.setFaxAreaCode(contact.getFax().getAreaCode());
                    userResource.setFaxNumber(contact.getFax().getPhoneNumber());
                }
            }
        }
        userResource.setIsFollow(ServiceLocator.findService(FollowManagement.class).followsUser(
                user.getId()));
        userResource.setUserId(user.getId());
        UserProfile userProfile = profile;
        userResource.setTimeZoneId(userProfile.getTimeZoneId());
        userResource.setLanguageLocale(user.getLanguageLocale().getLanguage());
        if (locale != null && user.getTags() != null && user.getTags().size() > 0) {
            userResource.setTags(TagHelper.buildTagResource(user.getTags(), locale));
        }
    }

    /**
     * @return lazily initialized ImageManager
     */
    private ImageManager getImageManagement() {
        if (imageManagement == null) {
            imageManagement = ServiceLocator.findService(ImageManager.class);
        }
        return imageManagement;
    }

    /**
     * Get the version string of the user profile image
     *
     * @param userId
     *            the ID of the user
     * @return the version string
     */
    private String getImageVersionString(Long userId) {
        try {
            return getImageManagement().getImageVersionString(UserImageDescriptor.IMAGE_TYPE_NAME,
                    userId.toString());
        } catch (ImageNotFoundException | AuthorizationException e) {
            // should not occur since in same session
            LOGGER.error("Unexpected exception getting profile image version", e);
            return "0";
        }
    }
}