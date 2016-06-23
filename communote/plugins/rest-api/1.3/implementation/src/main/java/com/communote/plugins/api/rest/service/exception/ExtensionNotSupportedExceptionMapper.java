package com.communote.plugins.api.rest.service.exception;

import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.Provider;

import com.communote.plugins.api.rest.exception.ExtensionNotSupportedException;

/**
 * {@link AbstractExceptionMapper} for {@link ExtensionNotSupportedException}
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
@Provider
public class ExtensionNotSupportedExceptionMapper extends
        AbstractExceptionMapper<ExtensionNotSupportedException> {

    /**
     * {@inheritDoc}
     */
    @Override
    public String getErrorMessage(ExtensionNotSupportedException exception) {
        return getLocalizedMessage("error.rest.ExtensionNotSupportedException",
                exception.getExtention());
    }

    /**
     * @return {@link Status#INTERNAL_SERVER_ERROR}
     */
    @Override
    public int getStatusCode() {
        return Status.NOT_ACCEPTABLE.getStatusCode();
    }

}
