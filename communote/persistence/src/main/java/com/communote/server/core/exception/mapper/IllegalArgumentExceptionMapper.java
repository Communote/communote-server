package com.communote.server.core.exception.mapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.communote.common.i18n.StaticLocalizedMessage;
import com.communote.server.core.exception.ExceptionMapper;
import com.communote.server.core.exception.Reason;
import com.communote.server.core.exception.Status;


/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class IllegalArgumentExceptionMapper implements ExceptionMapper<IllegalArgumentException> {

    /** Logger. */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(IllegalArgumentExceptionMapper.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<IllegalArgumentException> getExceptionClass() {
        return IllegalArgumentException.class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Status mapException(IllegalArgumentException exception) {
        LOGGER.error(exception.getMessage(), exception);
        Reason error = new Reason(new StaticLocalizedMessage(exception.getLocalizedMessage()),
                null, null);
        return new Status("error.rest.badrequest", null, BAD_REQUEST, error);
    }
}
