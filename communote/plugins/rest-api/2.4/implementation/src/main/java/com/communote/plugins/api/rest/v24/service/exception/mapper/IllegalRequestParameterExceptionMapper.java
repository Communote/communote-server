package com.communote.plugins.api.rest.v24.service.exception.mapper;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;

import com.communote.plugins.api.rest.v24.service.IllegalRequestParameterException;
import com.communote.server.core.exception.ExceptionMapper;
import com.communote.server.core.exception.Status;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
@Component
@Provides
@Instantiate
public class IllegalRequestParameterExceptionMapper implements
        ExceptionMapper<IllegalRequestParameterException> {

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<IllegalRequestParameterException> getExceptionClass() {
        return IllegalRequestParameterException.class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Status mapException(IllegalRequestParameterException exception) {
        return new Status("error.rest.IllegalRequestParameterException",
                new Object[] { exception.getParameter() }, ILLEGAL_PARAMETERS_ERROR);
    }

}
