package com.communote.server.core.vo.query.converters;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import com.communote.server.api.core.tag.TagData;
import com.communote.server.core.converter.blog.BlogToBlogDataConverter;
import com.communote.server.core.filter.listitems.blog.BlogTagListItem;
import com.communote.server.model.blog.Blog;
import com.communote.server.model.tag.Tag;

/**
 * This converter convert a {@link Blog} to a {@link BlogTagListItem}
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class BlogToBlogTagListItemQueryResultConverter extends
        DirectQueryResultConverter<Blog, BlogTagListItem> {

    private final BlogToBlogDataConverter<BlogTagListItem> blogConverter;

    private final TagToTagDataQueryResultConverter tagConverter;

    /**
     * @param locale
     *            The locale for filtering.
     */
    public BlogToBlogTagListItemQueryResultConverter(Locale locale) {
        blogConverter = new BlogToBlogDataConverter<BlogTagListItem>(BlogTagListItem.class,
                false);
        tagConverter = new TagToTagDataQueryResultConverter(locale);
    }

    @Override
    public boolean convert(Blog source, BlogTagListItem target) {
        blogConverter.convert(source, target);
        Set<Tag> tags = source.getTags();
        Set<TagData> tagListItems = new HashSet<TagData>();
        for (Tag tag : tags) {
            tagListItems.add(tagConverter.convert(tag));
        }
        target.setTags(tagListItems);
        return true;
    }

    @Override
    public BlogTagListItem create() {
        return new BlogTagListItem();
    }

}
