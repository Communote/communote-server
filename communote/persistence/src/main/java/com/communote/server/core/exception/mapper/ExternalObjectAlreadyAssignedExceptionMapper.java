package com.communote.server.core.exception.mapper;

import com.communote.server.core.exception.ExceptionMapper;
import com.communote.server.core.exception.Status;
import com.communote.server.core.external.ExternalObjectAlreadyAssignedException;


/**
 * {@link ExceptionMapper} for {@link ExternalObjectAlreadyAssignedException}
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ExternalObjectAlreadyAssignedExceptionMapper implements
        ExceptionMapper<ExternalObjectAlreadyAssignedException> {

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<ExternalObjectAlreadyAssignedException> getExceptionClass() {
        return ExternalObjectAlreadyAssignedException.class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Status mapException(ExternalObjectAlreadyAssignedException exception) {
        return new Status("error.external.object.already.assigned", BAD_REQUEST);
    }

}
