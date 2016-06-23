package com.communote.plugins.api.rest.v24.service.exception.mapper;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;

import com.communote.common.i18n.StaticLocalizedMessage;
import com.communote.plugins.api.rest.v24.exception.RestApiIllegalRequestParameterException;
import com.communote.server.core.exception.ExceptionMapper;
import com.communote.server.core.exception.Reason;
import com.communote.server.core.exception.Status;


/**
 * {@link ExceptionMapper} for {@link RestApiIllegalRequestParameterException}
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
@Component
@Instantiate
@Provides
public class RestApiIllegalRequestParameterExceptionMapper implements
        ExceptionMapper<RestApiIllegalRequestParameterException> {

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<RestApiIllegalRequestParameterException> getExceptionClass() {
        return RestApiIllegalRequestParameterException.class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Status mapException(RestApiIllegalRequestParameterException exception) {
        Reason reason = new Reason(new StaticLocalizedMessage(exception.getLocalizedMessage()),
                null, exception.getMessage());
        Status status = new Status("error.rest.badrequest", BAD_REQUEST, reason);
        return status;
    }
}
