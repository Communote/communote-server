package com.communote.server.core.exception.mapper;

import com.communote.server.api.core.user.CommunoteEntityNotFoundException;
import com.communote.server.core.exception.ExceptionMapper;
import com.communote.server.core.exception.Status;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class CommunoteEntityNotFoundExceptionMapper implements
        ExceptionMapper<CommunoteEntityNotFoundException> {
    /**
     * {@inheritDoc}
     */
    @Override
    public Class<CommunoteEntityNotFoundException> getExceptionClass() {
        return CommunoteEntityNotFoundException.class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Status mapException(CommunoteEntityNotFoundException exception) {
        // TODO this message is not generic enough!
        return new Status("error.blog.change.rights.failed.noEntity", null, NOT_FOUND);
    }

}
