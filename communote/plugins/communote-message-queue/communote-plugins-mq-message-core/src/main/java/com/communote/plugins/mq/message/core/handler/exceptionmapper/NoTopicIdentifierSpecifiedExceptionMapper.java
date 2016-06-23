package com.communote.plugins.mq.message.core.handler.exceptionmapper;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;

import com.communote.plugins.mq.message.core.handler.exception.NoTopicIdentifierSpecifiedException;
import com.communote.server.core.exception.ErrorCodes;
import com.communote.server.core.exception.ExceptionMapper;
import com.communote.server.core.exception.Status;

/**
 * exception mapper
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
@Component
@Instantiate(name = "NoTopicIdentifierSpecifiedExceptionMapper")
@Provides
public class NoTopicIdentifierSpecifiedExceptionMapper implements
        ExceptionMapper<NoTopicIdentifierSpecifiedException> {

    @Override
    public Class<NoTopicIdentifierSpecifiedException> getExceptionClass() {
        return NoTopicIdentifierSpecifiedException.class;
    }

    @Override
    public Status mapException(NoTopicIdentifierSpecifiedException arg0) {
        return new Status("error.topic.identifier.notspecified", null,
                ErrorCodes.BAD_REQUEST);
    }

}
