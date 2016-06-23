package com.communote.server.core.exception.mapper;

import java.util.List;

import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.JsonMappingException.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.communote.common.i18n.StaticLocalizedMessage;
import com.communote.server.core.exception.ExceptionMapper;
import com.communote.server.core.exception.Reason;
import com.communote.server.core.exception.Status;


/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class JsonMappingExceptionMapper implements ExceptionMapper<JsonMappingException> {

    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(JsonMappingExceptionMapper.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<JsonMappingException> getExceptionClass() {
        return JsonMappingException.class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Status mapException(JsonMappingException exception) {
        LOGGER.error(exception.getMessage(), exception);
        List<Reference> references = exception.getPath();
        if (references != null) {
            String message = new String("Wrong elements: ");
            for (Reference reference : references) {
                message += reference.getFieldName() + ", ";
            }
            message = message.substring(0, message.length() - 2);
            message += ".";
            return new Status("error.rest.badrequest", null, BAD_REQUEST, new Reason(
                    new StaticLocalizedMessage(message), null, null));
        }
        return new Status("error.rest.badrequest", null, BAD_REQUEST);
    }

}
