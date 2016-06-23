package com.communote.plugins.mq.message.core.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.communote.common.converter.Converter;
import com.communote.plugins.mq.message.core.data.property.StringProperty;
import com.communote.plugins.mq.message.core.data.tag.Tag;
import com.communote.plugins.mq.message.core.data.topic.ExternalObject;
import com.communote.server.api.core.property.StringPropertyTO;
import com.communote.server.api.core.tag.TagTO;

/**
 * Helper for converting MQ POJOs into Communote entities or TOs.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class ConverterUtils {

    /**
     * String property to StringTO converter
     * 
     */
    public static final Converter<StringProperty, StringPropertyTO> STRING_PROPERTYTO_CONVERTER =
            new Converter<StringProperty, StringPropertyTO>() {

                @Override
                public StringPropertyTO convert(StringProperty sourceProperty) {
                    StringPropertyTO propertyTO = new StringPropertyTO();
                    propertyTO.setPropertyKey(sourceProperty.getKey());
                    propertyTO.setPropertyValue(sourceProperty.getValue());
                    propertyTO.setKeyGroup(sourceProperty.getGroup());
                    return propertyTO;
                }

            };
    /**
     * Converter for {@link StringPropertyTO} to {@link StringProperty}.
     * 
     */
    public static final Converter<StringPropertyTO, StringProperty> STRING_PROPERTY_CONVERTER =
            new Converter<StringPropertyTO, StringProperty>() {

                @Override
                public StringProperty convert(StringPropertyTO sourceProperty) {
                    StringProperty property = new StringProperty();
                    property.setKey(sourceProperty.getPropertyKey());
                    property.setValue(sourceProperty.getPropertyValue());
                    property.setGroup(sourceProperty.getKeyGroup());
                    return property;
                }

            };
    /**
     * Tag converter
     * 
     */
    public static final Converter<com.communote.server.model.tag.Tag, Tag> TAG_CONVERTER =
            new Converter<com.communote.server.model.tag.Tag, Tag>() {

                @Override
                public Tag convert(com.communote.server.model.tag.Tag sourceTag) {
                    Tag tag = new Tag();
                    tag.setId(sourceTag.getId());
                    tag.setDefaultName(sourceTag.getDefaultName());
                    tag.setTagStoreId(tag.getTagStoreId());
                    tag.setTagStoreAlias(sourceTag.getTagStoreAlias());
                    tag.setTagStoreTagId(sourceTag.getTagStoreTagId());
                    return tag;
                }
            };

    /**
     * Existing Tag to Tag TO converter
     * 
     */
    public static final Converter<com.communote.server.model.tag.Tag, TagTO> DB_TAG_TAGTO_CONVERTER =
            new Converter<com.communote.server.model.tag.Tag, TagTO>() {

                @Override
                public TagTO convert(
                        com.communote.server.model.tag.Tag sourceTag) {
                    return createTagTO(new TagMemberAccessor(sourceTag));
                }

            };
    /**
     * converter for converting core external objects into MQ counterpart
     * 
     */
    public static final Converter<com.communote.server.model.external.ExternalObject, ExternalObject> 
    MQ_EXTERNAL_OBJECT_CONVERTER =
            new Converter<com.communote.server.model.external.ExternalObject, ExternalObject>() {
                @Override
                public ExternalObject convert(
                        com.communote.server.model.external.ExternalObject sourceObject) {
                    ExternalObject externalObject = new ExternalObject();
                    externalObject.setExternalObjectId(sourceObject.getExternalId());
                    externalObject.setExternalObjectName(sourceObject.getExternalName());
                    return externalObject;
                }
            };

    /**
     * New tags to TagTO converter
     * 
     */
    public static final Converter<Tag, TagTO> MQ_TAG_TAGTO_CONVERTER = new Converter<Tag, TagTO>() {

        @Override
        public TagTO convert(Tag sourceProperty) {
            // since tags cannot be updated, it makes sense to process only
            // those tags, which have set no id
            if (sourceProperty.getId() == null) {
                return createTagTO(new TagMemberAccessor(sourceProperty));
            } else {
                return null;
            }
        }

    };

    /**
     * Convert an array of source elements to target elements using the provided converter
     * 
     * @param <SourceType>
     *            the source type
     * @param <TargetType>
     *            the target type
     * @param sourceElements
     *            the items to convert
     * @param target
     *            the collection to add the converted elements to
     * @param converter
     *            converter performing actual conversion
     */
    public static <SourceType, TargetType> void convertArray(SourceType[] sourceElements,
            Collection<TargetType> target, Converter<SourceType, TargetType> converter) {
        if (sourceElements != null) {
            for (SourceType sourceElement : sourceElements) {
                // skip elements that were not converted
                TargetType convertedElement = converter.convert(sourceElement);
                if (convertedElement != null) {
                    target.add(convertedElement);
                }
            }
        }
    }

    /**
     * Convert iterable source elements into a collection of target elements. The order of the input
     * will be kept.
     * 
     * @param sourceElements
     *            source elements to convert
     * @param converter
     *            converter performing actual conversion
     * @param <SourceType>
     *            source type
     * @param <TargetType>
     *            target type
     * @return collection of converted elements
     */
    public static <SourceType, TargetType> Collection<TargetType> convertIterableToCollection(
            Iterable<SourceType> sourceElements, Converter<SourceType, TargetType> converter) {
        List<TargetType> targetCollection = new ArrayList<TargetType>();
        if (sourceElements != null) {
            for (SourceType sourceElement : sourceElements) {
                // skip elements that were not converted
                TargetType convertedElement = converter.convert(sourceElement);
                if (convertedElement != null) {
                    targetCollection.add(convertedElement);
                }
            }
        }
        return targetCollection;
    }

    /**
     * Creates a tagTO from the source tag object.
     * 
     * @param tagMemberAccessor
     *            the tag source object
     * @return the tag to
     */
    private static TagTO createTagTO(TagMemberAccessor tagMemberAccessor) {
        TagTO tagTO = new TagTO(tagMemberAccessor.getDefaultName(),
                tagMemberAccessor.getTagStoreAlias());
        tagTO.setId(tagMemberAccessor.getId());
        // TODO descriptions and names are likely to be proxies for Communote tags -> lazy loading
        // exception
        tagTO.setDescriptions(tagMemberAccessor.getDescriptions());
        tagTO.setNames(tagMemberAccessor.getNames());
        tagTO.setTagStoreTagId(tagMemberAccessor.getTagStoreTagId());
        return tagTO;
    }

    /**
     * Private constructor to avoid instances of utility class.
     */
    private ConverterUtils() {
        // Do nothing
    }
}
