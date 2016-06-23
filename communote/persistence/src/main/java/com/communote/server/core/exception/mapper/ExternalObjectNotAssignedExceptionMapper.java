package com.communote.server.core.exception.mapper;

import com.communote.server.api.core.external.ExternalObjectNotAssignedException;
import com.communote.server.core.exception.ExceptionMapper;
import com.communote.server.core.exception.Status;


/**
 * {@link ExceptionMapper} for {@link ExternalObjectNotAssignedException}
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ExternalObjectNotAssignedExceptionMapper implements
        ExceptionMapper<ExternalObjectNotAssignedException> {

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<ExternalObjectNotAssignedException> getExceptionClass() {
        return ExternalObjectNotAssignedException.class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Status mapException(ExternalObjectNotAssignedException exception) {
        return new Status("error.external.object.notactivated.assigned", FORBIDDEN);
    }

}
