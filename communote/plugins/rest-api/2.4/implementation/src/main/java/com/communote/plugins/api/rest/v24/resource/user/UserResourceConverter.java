package com.communote.plugins.api.rest.v24.resource.user;

import java.util.Locale;

import com.communote.plugins.api.rest.v24.resource.tag.TagHelper;
import com.communote.server.api.ServiceLocator;
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

    private final Locale locale;

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
            userResource.setLastPhotoModificationDate(profile.getLastPhotoModificationDate());
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
}