package com.communote.server.core.exception.mapper;

import org.apache.commons.lang3.StringUtils;

import com.communote.server.api.core.blog.BlogNotFoundException;
import com.communote.server.core.exception.ExceptionMapper;
import com.communote.server.core.exception.Status;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class BlogNotFoundExceptionMapper implements ExceptionMapper<BlogNotFoundException> {
    /**
     * {@inheritDoc}
     */
    @Override
    public Class<BlogNotFoundException> getExceptionClass() {
        return BlogNotFoundException.class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Status mapException(BlogNotFoundException exception) {
        if (StringUtils.isNotBlank(exception.getBlogNameId())) {
            return new Status("error.blogpost.blog.not.found",
                    new Object[] { exception.getBlogNameId() }, NOT_FOUND);
        }
        // not putting ID in error message because it's not useful for users
        return new Status("common.error.topic.not.found", NOT_FOUND);
    }
}
