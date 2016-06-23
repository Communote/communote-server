package com.communote.plugins.api.rest.resource.user;

import java.util.Locale;

import com.communote.plugins.api.rest.resource.tag.TagHelper;
import com.communote.server.api.ServiceLocator;
import com.communote.server.core.follow.FollowManagement;
import com.communote.server.core.vo.query.blog.DataAccessBlogConverter;
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

    @SuppressWarnings("unchecked")
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
        if (user.getProfile() != null) {
            userResource.setFirstName(user.getProfile().getFirstName());
            userResource.setLastName(user.getProfile().getLastName());
            userResource.setLastModificationDate(user.getProfile().getLastModificationDate());
            userResource.setLastPhotoModificationDate(user.getProfile()
                    .getLastPhotoModificationDate());
            userResource.setSalutation(user.getProfile().getSalutation());
        }
        userResource.setIsFollow(ServiceLocator.findService(FollowManagement.class).followsUser(
                user.getId()));
        userResource.setUserId(user.getId());
        UserProfile userProfile = user.getProfile();
        userResource.setTimeZoneId(userProfile.getTimeZoneId());
        userResource.setLanguageLocale(user.getLanguageLocale().getLanguage());
        if (locale != null && user.getTags() != null && user.getTags().size() > 0) {
            userResource.setTags(TagHelper.buildTagResource(user.getTags(), locale));
        }
    }
}