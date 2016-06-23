package com.communote.plugins.api.rest.v22.resource.tag;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.core.MultivaluedMap;

import org.apache.commons.lang3.StringUtils;

import com.communote.plugins.api.rest.v22.resource.RestApiMultivaluedMapWrapper;
import com.communote.plugins.api.rest.v22.service.IllegalRequestParameterException;
import com.communote.server.api.core.tag.TagData;
import com.communote.server.api.core.tag.TagStoreType;
import com.communote.server.api.core.tag.TagTO;
import com.communote.server.core.vo.query.converters.TagToTagDataQueryResultConverter;
import com.communote.server.model.i18n.Message;
import com.communote.server.model.tag.Tag;
import com.communote.server.model.user.Language;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public final class TagHelper {

    private final static Collection<String> COMMA_SEPERATED_PARAMETERS = new ArrayList<String>();

    static {
        COMMA_SEPERATED_PARAMETERS.add("f_userIds");
        COMMA_SEPERATED_PARAMETERS.add("f_suggestionAliases");
        COMMA_SEPERATED_PARAMETERS.add("f_suggestionProviderAliases");
    }

    /**
     * Get {@link TagResource} from set of {@link Tag}
     * 
     * @param tags
     *            set of {@link Tag}
     * @param locale
     *            of the currentUser
     * @return array of {@link TagResource}
     */
    public static TagResource[] buildTagResource(Set<Tag> tags, Locale locale) {
        TagResource[] tagResources = new TagResource[tags.size()];
        int i = 0;
        for (Tag tag : tags) {
            tagResources[i] = buildTagResource(new TagToTagDataQueryResultConverter(locale)
                    .convert(tag));
            i++;
        }
        return tagResources;
    }

    /**
     * Build the tag resource of an taglistitem
     * 
     * @param tagListItem
     *            {@link TagData}
     * @return {@link TagResource}
     */
    public static TagResource buildTagResource(TagData tagListItem) {
        TagResource tagResource = new TagResource();
        tagResource.setTagId(tagListItem.getId());
        tagResource.setDefaultName(tagListItem.getDefaultName());
        tagResource.setName(tagListItem.getName());
        tagResource.setTagStoreAlias(tagListItem.getTagStoreAlias());
        tagResource.setTagStoreTagId(tagListItem.getTagStoreTagId());
        tagResource.setDescription(tagListItem.getDescription());
        if (tagListItem.getLocale() != null) {
            tagResource.setLanguageCode(tagListItem.getLocale().toString());
        }
        return tagResource;
    }

    /**
     * Build the {@link TagTO} from the {@link TagResource} and a {@link TagStoreType}
     * 
     * @param tagResource
     *            {@link TagResource}
     * @param tagStoreAlias
     *            {@link TagStoreType}
     * @return {@link TagResource}
     * @throws IllegalRequestParameterException
     *             exception in request parameter
     */
    public static TagTO buildTagTO(TagResource tagResource, TagStoreType tagStoreAlias)
            throws IllegalRequestParameterException {
        TagTO tagTO = new TagTO(tagResource.getDefaultName(), tagStoreAlias);
        boolean isName = false, isDescription = false;

        tagTO.setId(tagResource.getTagId());
        tagTO.setTagStoreAlias(tagResource.getTagStoreAlias());
        tagTO.setTagStoreTagId(tagResource.getTagStoreTagId());

        if ((isName = StringUtils.isNotBlank(tagResource.getName())
                || (isDescription = StringUtils.isNotBlank(tagResource.getDescription())))) {
            Language language;
            if (StringUtils.isBlank(tagResource.getLanguageCode())) {
                throw new IllegalRequestParameterException("languageCode",
                        tagResource.getLanguageCode(), "Language code is empty.");
            } else {
                language = Language.Factory.newInstance();
                language.setLanguageCode(tagResource.getLanguageCode());
            }

            if (isName) {
                Set<Message> messages = new HashSet<Message>();
                Message message = Message.Factory.newInstance("tag.name.translation",
                        tagResource.getName(), false);
                message.setLanguage(language);
                messages.add(message);
                tagTO.setNames(messages);
            }

            if (isDescription) {
                Set<Message> messages = new HashSet<Message>();
                Message message = Message.Factory.newInstance("tag.description.translation",
                        tagResource.getDescription(), false);
                message.setLanguage(language);
                messages.add(message);
                tagTO.setDescriptions(messages);
            }
        }

        return tagTO;
    }

    /**
     * Get the {@link TagStoreType} by default {@link TagStoreType.Types#NOTE}
     * 
     * @param typeOfTagStore
     *            type of tagStore as string
     * @return {@link TagStoreType}
     */
    public static TagStoreType getTagStoreType(String typeOfTagStore) {
        if (StringUtils.isNotBlank(typeOfTagStore)) {
            return TagStoreType.Types.valueOf(typeOfTagStore.toUpperCase());
        }
        return TagStoreType.Types.NOTE;
    }

    /**
     * @param multivaluedMap
     *            The multivalued map to wrap.
     * @return A {@link RestApiMultivaluedMapWrapper}
     */
    public static Map<String, Object> toMap(MultivaluedMap<String, String> multivaluedMap) {
        return new RestApiMultivaluedMapWrapper(multivaluedMap, COMMA_SEPERATED_PARAMETERS);
    }

    /**
     * Default constructor
     */
    private TagHelper() {

    }
}
