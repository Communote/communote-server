package com.communote.plugins.mq.service.exception;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;

import com.communote.common.i18n.LocalizedMessage;
import com.communote.server.core.exception.ExceptionMapper;
import com.communote.server.core.exception.Status;
import com.communote.server.persistence.common.messages.MessageKeyLocalizedMessage;


/**
 * Exception mapper for the validation exception
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
@Component
@Instantiate
@Provides
public class MessageValidationExceptionMapper implements
        ExceptionMapper<MessageValidationException> {

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<MessageValidationException> getExceptionClass() {
        return MessageValidationException.class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Status mapException(MessageValidationException exception) {
        LocalizedMessage message = exception.getValidationMessage();
        if (message == null) {
            message = new MessageKeyLocalizedMessage(
                    "communote.plugins.mq.serivce.error.message.validation.general.unkown",
                    exception.getMessage());
        }

        return new Status(message, VALIDATION_ERROR, exception.getReasons());
    }

}
