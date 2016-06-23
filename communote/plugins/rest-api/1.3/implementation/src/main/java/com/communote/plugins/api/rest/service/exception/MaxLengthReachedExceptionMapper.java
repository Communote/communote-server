package com.communote.plugins.api.rest.service.exception;

import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.Provider;

import com.communote.common.io.MaxLengthReachedException;

/**
 * {@link AbstractExceptionMapper} for {@link MaxLengthReachedException}
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
@Provider
public class MaxLengthReachedExceptionMapper extends
        AbstractExceptionMapper<MaxLengthReachedException> {

    /**
     * {@inheritDoc}
     */
    @Override
    public String getErrorMessage(MaxLengthReachedException exception) {
        return getLocalizedMessage("error.attachment.file.max.lenght");
    }

    /**
     * @return {@link Status#BAD_REQUEST}
     */
    @Override
    public int getStatusCode() {
        return Status.BAD_REQUEST.getStatusCode();
    }
}
