package com.communote.server.core.vo.query.converters;

import java.util.Locale;

import com.communote.server.api.core.tag.TagData;
import com.communote.server.core.tag.TagManagement;
import com.communote.server.model.tag.Tag;


/**
 * This converter adds localized information to the TagData.
 * 
 * @param <T>
 *            Type of the source item to convert.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class TagListItemToTagListItemQueryResultConverter<T extends TagData> extends
        DirectQueryResultConverter<T, TagData> {

    private final TagToTagDataQueryResultConverter converter;
    private final TagManagement tagManagement;

    /**
     * @param locale
     *            The locale to use.
     * @param tagManagement
     *            to find tag by identifier
     */
    public TagListItemToTagListItemQueryResultConverter(Locale locale, TagManagement tagManagement) {
        this.tagManagement = tagManagement;
        converter = new TagToTagDataQueryResultConverter(locale);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean convert(T source, TagData target) {
        Tag tag = tagManagement.findTag(source.getId());
        converter.convert(tag, target);
        return true;
    }

    /**
     * @return new {@link TagData}
     */
    @Override
    public TagData create() {
        return new TagData();
    }
}