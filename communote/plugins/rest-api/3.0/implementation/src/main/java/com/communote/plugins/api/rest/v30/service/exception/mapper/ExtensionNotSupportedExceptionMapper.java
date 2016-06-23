package com.communote.plugins.api.rest.v30.service.exception.mapper;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;

import com.communote.plugins.api.rest.v30.exception.ExtensionNotSupportedException;
import com.communote.server.core.exception.ExceptionMapper;
import com.communote.server.core.exception.Status;
import com.communote.server.persistence.common.messages.MessageKeyLocalizedMessage;


/**
 * {@link ExceptionMapper} for {@link ExtensionNotSupportedException}
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
@Component
@Instantiate
@Provides
public class ExtensionNotSupportedExceptionMapper implements
        ExceptionMapper<ExtensionNotSupportedException> {

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<ExtensionNotSupportedException> getExceptionClass() {
        return ExtensionNotSupportedException.class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public com.communote.server.core.exception.Status mapException(
            ExtensionNotSupportedException exception) {
        return new Status(new MessageKeyLocalizedMessage(
                "error.rest.ExtensionNotSupportedException", exception.getExtention()),
                NOT_ACCEPTABLE);
    }

}
