package com.communote.plugins.mq.service.exception;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;

import com.communote.common.i18n.LocalizedMessage;
import com.communote.server.core.exception.ExceptionMapper;
import com.communote.server.core.exception.Status;
import com.communote.server.persistence.common.messages.MessageKeyLocalizedMessage;


/**
 * Exception mapper for the parsing exception
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
@Component
@Instantiate
@Provides
public class MessageParsingExceptionMapper implements
        ExceptionMapper<MessageParsingException> {

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<MessageParsingException> getExceptionClass() {
        return MessageParsingException.class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Status mapException(MessageParsingException exception) {

        LocalizedMessage message;

        if (exception.isBeSilent()) {
            // be silent => don't expose exception message
            message = new MessageKeyLocalizedMessage(
                    "communote.plugins.mq.serivce.error.message.parsing.general");
        } else {
            // don't be silent => expose exception message
            message = new MessageKeyLocalizedMessage(
                    "communote.plugins.mq.serivce.error.message.parsing.specific",
                    exception.getMessage());
        }

        return new Status(message, BAD_REQUEST);
    }

}
