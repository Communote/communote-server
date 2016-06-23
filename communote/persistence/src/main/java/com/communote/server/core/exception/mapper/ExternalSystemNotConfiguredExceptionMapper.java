package com.communote.server.core.exception.mapper;

import org.springframework.stereotype.Component;

import com.communote.server.core.exception.ExceptionMapper;
import com.communote.server.core.exception.Status;
import com.communote.server.core.external.ExternalSystemNotConfiguredException;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
@Component
public class ExternalSystemNotConfiguredExceptionMapper implements
        ExceptionMapper<ExternalSystemNotConfiguredException> {

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<ExternalSystemNotConfiguredException> getExceptionClass() {
        return ExternalSystemNotConfiguredException.class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Status mapException(ExternalSystemNotConfiguredException exception) {

        String systemId = exception.getExternalSystemId() == null ? "" : exception
                .getExternalSystemId();
        return new Status("error.external.system.not.configured",
                new Object[] { systemId },
                VALIDATION_ERROR);
    }
}
