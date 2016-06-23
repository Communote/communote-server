package com.communote.server.core.exception.mapper;

import org.codehaus.jackson.JsonParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.communote.common.i18n.StaticLocalizedMessage;
import com.communote.server.core.exception.ExceptionMapper;
import com.communote.server.core.exception.Reason;
import com.communote.server.core.exception.Status;


/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class JsonParseExceptionMapper implements ExceptionMapper<JsonParseException> {

    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(JsonParseExceptionMapper.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<JsonParseException> getExceptionClass() {
        return JsonParseException.class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Status mapException(JsonParseException exception) {
        LOGGER.error(exception.getMessage(), exception);
        return new Status("error.rest.badrequest", null, BAD_REQUEST, new Reason(
                new StaticLocalizedMessage(exception.getLocalizedMessage()), null, null));
    }
}
