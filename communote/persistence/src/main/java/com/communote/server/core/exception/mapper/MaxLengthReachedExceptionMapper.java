package com.communote.server.core.exception.mapper;

import com.communote.common.io.MaxLengthReachedException;
import com.communote.server.core.exception.ExceptionMapper;
import com.communote.server.core.exception.Status;


/**
 * {@link ExceptionMapper} for {@link MaxLengthReachedException}
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class MaxLengthReachedExceptionMapper implements ExceptionMapper<MaxLengthReachedException> {

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<MaxLengthReachedException> getExceptionClass() {
        return MaxLengthReachedException.class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Status mapException(MaxLengthReachedException exception) {
        return new Status("error.attachment.file.max.length", BAD_REQUEST);
    }
}
