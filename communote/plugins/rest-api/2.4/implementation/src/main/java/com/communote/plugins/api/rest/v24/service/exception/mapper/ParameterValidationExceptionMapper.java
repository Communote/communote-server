package com.communote.plugins.api.rest.v24.service.exception.mapper;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.communote.plugins.api.rest.v24.resource.validation.ParameterValidationError;
import com.communote.plugins.api.rest.v24.resource.validation.ParameterValidationException;
import com.communote.server.core.exception.ExceptionMapper;
import com.communote.server.core.exception.Reason;
import com.communote.server.core.exception.Status;
import com.communote.server.persistence.common.messages.MessageKeyLocalizedMessage;


/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
@Component
@Instantiate
@Provides
public class ParameterValidationExceptionMapper implements
        ExceptionMapper<ParameterValidationException> {

    /** Logger. */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(ParameterValidationExceptionMapper.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<ParameterValidationException> getExceptionClass() {
        return ParameterValidationException.class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Status mapException(ParameterValidationException exception) {
        LOGGER.info(exception.getMessage());
        Collection<Reason> reasons = new ArrayList<Reason>();
        for (ParameterValidationError error : exception.getErrors()) {
            Reason reason = new Reason(new MessageKeyLocalizedMessage(error.getMessageKey(),
                    error.getParameters()), error.getSource(), error.getSource());
            reasons.add(reason);
        }
        Status status = new Status(exception.getMessageKey(), BAD_REQUEST,
                reasons.toArray(new Reason[reasons.size()]));
        return status;
    }
}
