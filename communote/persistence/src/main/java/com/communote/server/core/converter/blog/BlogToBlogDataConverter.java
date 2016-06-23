package com.communote.server.core.converter.blog;

import com.communote.server.api.core.blog.BlogData;
import com.communote.server.core.converter.PropertyableToListItemConverter;
import com.communote.server.model.blog.Blog;


/**
 * Converter for converting a blog entity into a list item.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 * @param <T>
 *            the target type
 */
public class BlogToBlogDataConverter<T extends BlogData> extends
        PropertyableToListItemConverter<Blog, T> {

    /**
     * Create a new converter
     * 
     * @param clazz
     *            the class of the target type
     * @param includeProperties
     *            whether to add the blog properties to the result. The property key will be
     *            constructed as follows &lt;groupKey&gt;.&lt;propertyKey&gt;
     */
    public BlogToBlogDataConverter(Class<T> clazz, boolean includeProperties) {
        super(clazz, includeProperties);
    }

    @Override
    public void convert(Blog source, T target) {
        super.convert(source, target);
        target.setId(source.getId());
        target.setDescription(source.getDescription());
        target.setLastModificationDate(source.getLastModificationDate());
        target.setCreationDate(source.getCreationDate());
        target.setNameIdentifier(source.getNameIdentifier());
        target.setTitle(source.getTitle());
    }
}
