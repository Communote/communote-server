package com.communote.server.core.vo.query.converters;

import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.blog.BlogAccessException;
import com.communote.server.api.core.blog.BlogData;
import com.communote.server.api.core.blog.BlogManagement;
import com.communote.server.core.filter.listitems.blog.BlogTagListItem;

/**
 * This converter convert a {@link BlogData} to a {@link BlogTagListItem}
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class BlogDataToBlogTagListItemQueryResultConverter extends
DirectQueryResultConverter<BlogData, BlogTagListItem> {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(BlogDataToBlogTagListItemQueryResultConverter.class);
    private final BlogToBlogTagListItemQueryResultConverter converter;

    /**
     * @param locale
     *            The locale for filtering.
     */
    public BlogDataToBlogTagListItemQueryResultConverter(Locale locale) {
        converter = new BlogToBlogTagListItemQueryResultConverter(locale);
    }

    @Override
    public boolean convert(BlogData source, BlogTagListItem target) {
        BlogTagListItem convertedBlog;
        try {
            convertedBlog = getBlogManagement().getBlogById(source.getId(), converter);
            target.copy(convertedBlog);
            return true;
        } catch (BlogAccessException e) {
            LOGGER.error("Unexpected error converting a topic list item", e);
            return false;
        }
    }

    @Override
    public BlogTagListItem create() {
        return new BlogTagListItem();
    }

    /**
     * @return The blog management
     */
    private BlogManagement getBlogManagement() {
        return ServiceLocator.findService(BlogManagement.class);
    }

}
