package com.communote.server.core.exception.mapper;

import com.communote.server.api.core.common.NotFoundException;
import com.communote.server.core.exception.ExceptionMapper;
import com.communote.server.core.exception.Status;


/**
 * {@link ExceptionMapper} for {@link NotFoundException}
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class NotFoundExceptionMapper implements ExceptionMapper<NotFoundException> {

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<NotFoundException> getExceptionClass() {
        return NotFoundException.class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Status mapException(NotFoundException exception) {
        return new Status("error.rest.NotFoundException", NOT_FOUND);
    }
}
