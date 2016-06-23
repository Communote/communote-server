package com.communote.server.core.exception.mapper;

import java.util.HashMap;
import java.util.Map;

import com.communote.common.i18n.LocalizedMessage;
import com.communote.server.api.core.note.processor.NoteStoringPreProcessorException;
import com.communote.server.core.blog.OnlyCrosspostMarkupException;
import com.communote.server.core.blog.notes.DirectMessageConversionException;
import com.communote.server.core.blog.notes.ReplyNotDirectMessageException;
import com.communote.server.core.blog.notes.processors.exceptions.DirectMessageMissingRecipientException;
import com.communote.server.core.blog.notes.processors.exceptions.DirectMessageWrongRecipientForAnswerException;
import com.communote.server.core.blog.notes.processors.exceptions.MessageKeyNoteContentException;
import com.communote.server.core.blog.notes.processors.exceptions.ParentDoesNotExistsNotePreProcessorException;
import com.communote.server.core.exception.ExceptionMapper;
import com.communote.server.core.exception.Status;
import com.communote.server.persistence.common.messages.MessageKeyLocalizedMessage;


/**
 * {@link ExceptionMapper} for {@link NoteStoringPreProcessorException}.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class NotePreProcessorExceptionMapper implements ExceptionMapper<NoteStoringPreProcessorException> {

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
        EXCEPTION_TO_MESSAGE_KEY_MAP.put(ParentDoesNotExistsNotePreProcessorException.class,
                "error.blogpost.create.reply.no.parent");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<NoteStoringPreProcessorException> getExceptionClass() {
        return NoteStoringPreProcessorException.class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Status mapException(final NoteStoringPreProcessorException exception) {
        LocalizedMessage message = null;
        if (exception instanceof DirectMessageMissingRecipientException) {
            message = ((DirectMessageMissingRecipientException) exception)
                    .getPreparedLocalizedMessage();
        } else if (exception instanceof MessageKeyNoteContentException) {
            MessageKeyNoteContentException messageKeyNoteContentException = (MessageKeyNoteContentException) exception;
            message = new MessageKeyLocalizedMessage(
                    messageKeyNoteContentException.getMessageKey(),
                    messageKeyNoteContentException.getParameters());
        } else {
            String messageKey = EXCEPTION_TO_MESSAGE_KEY_MAP.get(exception.getClass());
            message = new MessageKeyLocalizedMessage(messageKey != null ? messageKey
                    : "error.blogpost.blog.content.processing.failed");
        }
        return new Status(message, BAD_REQUEST);
    }
}
