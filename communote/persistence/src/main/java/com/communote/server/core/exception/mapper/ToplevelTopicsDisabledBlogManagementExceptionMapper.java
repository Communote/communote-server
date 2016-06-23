package com.communote.server.core.exception.mapper;

import org.springframework.stereotype.Component;

import com.communote.server.core.blog.ToplevelTopicsDisabledBlogManagementException;
import com.communote.server.core.exception.ExceptionMapper;
import com.communote.server.core.exception.Status;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
@Component
public class ToplevelTopicsDisabledBlogManagementExceptionMapper implements
        ExceptionMapper<ToplevelTopicsDisabledBlogManagementException> {
    /**
     * @return The class this mapper is for.
     */
    @Override
    public Class<ToplevelTopicsDisabledBlogManagementException> getExceptionClass() {
        return ToplevelTopicsDisabledBlogManagementException.class;
    }

    /**
     * Maps the given exception to a status.
     * 
     * @param exception
     *            The exception to map.
     * @return The status for the exception.
     */
    @Override
    public Status mapException(ToplevelTopicsDisabledBlogManagementException exception) {
        return new Status("error.http.403." + exception.getClass().getName(), null, FORBIDDEN);
    }
}
