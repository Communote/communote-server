package com.communote.plugins.api.rest.v24.service.exception.mapper;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;

import com.communote.plugins.api.rest.v24.exception.ResponseBuildException;
import com.communote.server.core.exception.ExceptionMapper;
import com.communote.server.core.exception.Status;

/**
 * {@link ExceptionMapper} for {@link ResponseBuildException}
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
@Component
@Instantiate
@Provides
public class ResponseBuildExceptionMapper implements
        ExceptionMapper<ResponseBuildException> {

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<ResponseBuildException> getExceptionClass() {
        return ResponseBuildException.class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Status mapException(ResponseBuildException exception) {
        return new Status("error.rest.ResponseBuildException", INTERNAL_ERROR);
    }

}
