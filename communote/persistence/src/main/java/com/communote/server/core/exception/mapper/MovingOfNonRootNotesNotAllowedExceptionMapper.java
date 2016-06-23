package com.communote.server.core.exception.mapper;

import org.springframework.stereotype.Component;

import com.communote.server.core.blog.MovingOfNonRootNotesNotAllowedException;
import com.communote.server.core.exception.ExceptionMapper;
import com.communote.server.core.exception.Status;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
@Component
public class MovingOfNonRootNotesNotAllowedExceptionMapper implements
        ExceptionMapper<MovingOfNonRootNotesNotAllowedException> {

    /**
     * @return MovingOfNonRootNotesNotAllowedException.class
     */
    @Override
    public Class<MovingOfNonRootNotesNotAllowedException> getExceptionClass() {
        return MovingOfNonRootNotesNotAllowedException.class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Status mapException(MovingOfNonRootNotesNotAllowedException exception) {
        return new Status("error.note.move-non-parent", FORBIDDEN);
    }
}
