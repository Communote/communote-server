package com.communote.plugins.mq.message.core.handler.exceptionmapper;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;

import com.communote.plugins.mq.message.core.handler.exception.NoNoteSpecifiedException;
import com.communote.server.core.exception.ErrorCodes;
import com.communote.server.core.exception.ExceptionMapper;
import com.communote.server.core.exception.Status;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
@Component
@Instantiate(name = "NoNoteSpecifiedExceptionMapper")
@Provides
public class NoNoteSpecifiedExceptionMapper implements ExceptionMapper<NoNoteSpecifiedException> {

    @Override
    public Class<NoNoteSpecifiedException> getExceptionClass() {
        return NoNoteSpecifiedException.class;
    }

    @Override
    public Status mapException(NoNoteSpecifiedException exception) {
        return new Status("error.note.notspecified", null,
                ErrorCodes.BAD_REQUEST);
    }

}
