package com.communote.plugins.mq.message.core.handler.exceptionmapper;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;

import com.communote.plugins.mq.message.core.handler.exception.NoTopicSpecifiedException;
import com.communote.server.core.exception.ErrorCodes;
import com.communote.server.core.exception.ExceptionMapper;
import com.communote.server.core.exception.Status;

/**
 * exception mapper
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
@Component
@Instantiate(name = "NoTopicSpecifiedExceptionMapper")
@Provides
public class NoTopicSpecifiedExceptionMapper implements ExceptionMapper<NoTopicSpecifiedException> {

    @Override
    public Class<NoTopicSpecifiedException> getExceptionClass() {
        return NoTopicSpecifiedException.class;
    }

    @Override
    public Status mapException(NoTopicSpecifiedException exception) {
        return new Status("error.topic.notspecified", null,
                ErrorCodes.BAD_REQUEST);
    }

}
