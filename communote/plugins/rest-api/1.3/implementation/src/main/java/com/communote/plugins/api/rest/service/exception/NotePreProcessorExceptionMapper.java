package com.communote.plugins.api.rest.service.exception;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.Provider;

import com.communote.server.api.core.note.processor.NoteStoringPreProcessorException;
import com.communote.server.core.blog.OnlyCrosspostMarkupException;
import com.communote.server.core.blog.notes.DirectMessageConversionException;
import com.communote.server.core.blog.notes.ReplyNotDirectMessageException;
import com.communote.server.core.blog.notes.processors.exceptions.DirectMessageMissingRecipientException;
import com.communote.server.core.blog.notes.processors.exceptions.DirectMessageWrongRecipientForAnswerException;
import com.communote.server.core.blog.notes.processors.exceptions.MessageKeyNoteContentException;

/**
 * {@link AbstractExceptionMapper} for {@link NoteStoringPreProcessorException}.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
@Provider
public class NotePreProcessorExceptionMapper extends
        AbstractExceptionMapper<NoteStoringPreProcessorException> {

    private final static Map<Class<? extends NoteStoringPreProcessorException>, String> EXCEPTION_TO_MESSAGE_KEY_MAP =
            new HashMap<Class<? extends NoteStoringPreProcessorException>, String>();
    static {
        EXCEPTION_TO_MESSAGE_KEY_MAP.put(OnlyCrosspostMarkupException.class,
                "error.blogpost.create.no.real.content");
        EXCEPTION_TO_MESSAGE_KEY_MAP.put(DirectMessageWrongRecipientForAnswerException.class,
                "error.blogpost.blog.content.processing.failed.direct.wrong.recipient");
        EXCEPTION_TO_MESSAGE_KEY_MAP.put(DirectMessageConversionException.class,
                "error.blogpost.convert.note.to.direct.failed");
        EXCEPTION_TO_MESSAGE_KEY_MAP.put(ReplyNotDirectMessageException.class,
                "error.blogpost.create.reply.not.direct");
    }

    /**
     * @param exception
     *            The thrown exception.
     * @return A appropriate error message.
     */
    @Override
    public String getErrorMessage(NoteStoringPreProcessorException exception) {
        if (exception instanceof DirectMessageMissingRecipientException) {
            return ((DirectMessageMissingRecipientException) exception)
                    .getLocalizedMessage(getCurrentUserLocale(org.restlet.Request.getCurrent()));
        }
        if (exception instanceof MessageKeyNoteContentException) {
            MessageKeyNoteContentException messageKeyNoteContentException = (MessageKeyNoteContentException) exception;
            return getLocalizedMessage(messageKeyNoteContentException.getMessageKey(),
                    messageKeyNoteContentException.getParameters());
        }
        String messageKey = EXCEPTION_TO_MESSAGE_KEY_MAP.get(exception.getClass());
        return getLocalizedMessage(messageKey != null ? messageKey
                : "error.blogpost.blog.content.processing.failed");
    }

    /**
     * @return {@link Status#BAD_REQUEST}
     */
    @Override
    public int getStatusCode() {
        return Status.BAD_REQUEST.getStatusCode();
    }

}
