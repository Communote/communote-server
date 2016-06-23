package com.communote.server.core.exception.mapper;

import com.communote.server.api.core.blog.NoBlogManagerLeftException;
import com.communote.server.core.exception.ExceptionMapper;
import com.communote.server.core.exception.Status;


/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class NoBlogManagerLeftExceptionMapper implements
        ExceptionMapper<NoBlogManagerLeftException> {
    /**
     * {@inheritDoc}
     */
    @Override
    public Class<NoBlogManagerLeftException> getExceptionClass() {
        return NoBlogManagerLeftException.class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Status mapException(NoBlogManagerLeftException exception) {
        return new Status("user.group.member.change.role.no.manager.left.exception", null,
                FORBIDDEN);
    }

}
