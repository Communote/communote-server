package com.communote.server.core.exception.mapper;

import org.springframework.stereotype.Component;

import com.communote.server.core.exception.ExceptionMapper;
import com.communote.server.core.exception.Status;
import com.communote.server.core.external.TooManyExternalObjectsPerTopicException;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
@Component
public class TooManyExternalObjectsPerTopicExceptionMapper implements
        ExceptionMapper<TooManyExternalObjectsPerTopicException> {
    /**
     * {@inheritDoc}
     */
    @Override
    public Class<TooManyExternalObjectsPerTopicException> getExceptionClass() {
        return TooManyExternalObjectsPerTopicException.class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Status mapException(TooManyExternalObjectsPerTopicException exception) {

        return new Status("error.external.object.too.many.per.topic",
                new Object[] { exception.getExternalSystemId(),
                        exception.getNumberOfMaximumExternalObjectsPerTopic() },
                VALIDATION_ERROR);
    }
}
