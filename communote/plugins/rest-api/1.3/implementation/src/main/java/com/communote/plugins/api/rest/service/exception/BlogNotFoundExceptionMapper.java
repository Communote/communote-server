package com.communote.plugins.api.rest.service.exception;

import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.Provider;

import org.apache.commons.lang3.StringUtils;

import com.communote.server.api.core.blog.BlogNotFoundException;

/**
 * {@link AbstractExceptionMapper} for {@link BlogNotFoundException}
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
@Provider
public class BlogNotFoundExceptionMapper extends AbstractExceptionMapper<BlogNotFoundException> {
    /**
     * {@inheritDoc}
     */
    @Override
    public String getErrorMessage(BlogNotFoundException exception) {
        if (StringUtils.isNotBlank(exception.getBlogNameId())) {
            return getLocalizedMessage("error.blogpost.blog.not.found", exception.getBlogNameId());
        } else {
            return getLocalizedMessage("error.blogpost.blog.not.found",
                    "Id: " + exception.getBlogId());
        }
    }

    /**
     * @return Status.NOT_FOUND
     */
    @Override
    public int getStatusCode() {
        return Status.NOT_FOUND.getStatusCode();
    }

}
