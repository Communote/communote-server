package com.communote.server.core.exception.mapper;

import com.communote.server.core.exception.ExceptionMapper;
import com.communote.server.core.exception.Status;
import com.communote.server.core.tag.TagNotFoundException;

/**
 * Mapper to create a useful error message when a tag does not exist
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class TagNotFoundExceptionMapper implements ExceptionMapper<TagNotFoundException> {

    @Override
    public Class<TagNotFoundException> getExceptionClass() {
        return TagNotFoundException.class;
    }

    @Override
    public Status mapException(TagNotFoundException exception) {
        return new Status("common.error.tag.not.found", NOT_FOUND);
    }

}
