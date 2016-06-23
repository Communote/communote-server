package com.communote.server.core.converter.user;

import com.communote.common.converter.Converter;
import com.communote.server.api.core.tag.TagData;
import com.communote.server.api.core.user.UserData;
import com.communote.server.core.converter.PropertyableToListItemConverter;
import com.communote.server.model.tag.Tag;
import com.communote.server.model.user.User;


/**
 * Converter to transform a user entity into a list item.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 * @param <T>
 *            the target type of the conversion
 */
public class UserToUserDataConverter<T extends UserData> extends
        PropertyableToListItemConverter<User, T> {

    private final Converter<Tag, TagData> tagConverter;

    /**
     * Create a new converter
     * 
     * @param clazz
     *            the class of the target type
     * @param includeProperties
     *            whether to add the blog properties to the result. The property key will be
     *            constructed as follows &lt;groupKey&gt;.&lt;propertyKey&gt;
     * @param tagConverter
     *            the converter to use for converting the tags. If null the tags won't be added to
     *            the result.
     */
    public UserToUserDataConverter(Class<T> clazz, boolean includeProperties,
            Converter<Tag, TagData> tagConverter) {
        super(clazz, includeProperties);
        this.tagConverter = tagConverter;
    }

    @Override
    public void convert(User source, T target) {
        super.convert(source, target);
        target.setAlias(source.getAlias());
        target.setEmail(source.getEmail());
        target.setFirstName(source.getProfile().getFirstName());
        target.setId(source.getId());
        target.setLastName(source.getProfile().getLastName());
        target.setSalutation(source.getProfile().getSalutation());
        target.setStatus(source.getStatus());
        if (tagConverter != null && source.getTags() != null) {
            for (Tag tag : source.getTags()) {
                TagData convertedTag = tagConverter.convert(tag);
                if (convertedTag != null) {
                    target.getTags().add(convertedTag);
                }
            }
        }
    }

}
