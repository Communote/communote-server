package com.communote.server.core.exception.mapper;

import org.springframework.stereotype.Component;

import com.communote.server.core.blog.ToplevelTopicCannotBeChildException;
import com.communote.server.core.exception.ExceptionMapper;
import com.communote.server.core.exception.Status;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
@Component
public class ToplevelTopicCannotBeChildExceptionMapper implements
        ExceptionMapper<ToplevelTopicCannotBeChildException> {
    /**
     * @return The class this mapper is for.
     */
    @Override
    public Class<ToplevelTopicCannotBeChildException> getExceptionClass() {
        return ToplevelTopicCannotBeChildException.class;
    }

    /**
     * Maps the given exception to a status.
     * 
     * @param exception
     *            The exception to map.
     * @return The status for the exception.
     */
    @Override
    public Status mapException(ToplevelTopicCannotBeChildException exception) {
        return new Status("error.topic.structure.ToplevelTopicCannotBeChildException",
                new Object[] { exception.getTopicTitle() }, FORBIDDEN);
    }
}
