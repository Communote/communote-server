package com.communote.plugins.api.rest.v30.resource.attachment;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;

import com.communote.server.core.exception.ExceptionMapper;
import com.communote.server.core.exception.Status;

/**
 * Mapper for AttachmentIsEmptyExceptionMapper
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
@Instantiate
@Component
@Provides
public class AttachmentIsEmptyExceptionMapper implements
        ExceptionMapper<AttachmentIsEmptyException> {

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<AttachmentIsEmptyException> getExceptionClass() {
        return AttachmentIsEmptyException.class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Status mapException(AttachmentIsEmptyException exception) {
        return new Status("error.blogpost.upload.empty.file", ILLEGAL_PARAMETERS_ERROR);
    }
}
