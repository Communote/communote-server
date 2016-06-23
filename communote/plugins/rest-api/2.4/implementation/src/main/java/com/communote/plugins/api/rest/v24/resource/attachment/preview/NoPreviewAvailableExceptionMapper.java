package com.communote.plugins.api.rest.v24.resource.attachment.preview;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;

import com.communote.server.core.exception.ExceptionMapper;
import com.communote.server.core.exception.Status;

/**
 * ExceptionMapper for {@link NoPreviewAvailableException}
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
@Component
@Instantiate
@Provides
public class NoPreviewAvailableExceptionMapper implements
        ExceptionMapper<NoPreviewAvailableException> {
    /**
     * {@inheritDoc}
     */
    @Override
    public Class<NoPreviewAvailableException> getExceptionClass() {
        return NoPreviewAvailableException.class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Status mapException(NoPreviewAvailableException exception) {
        return new Status("error.rest.NoPreviewAvailableExceptionMapper", INTERNAL_ERROR);
    }
}
