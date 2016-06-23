package com.communote.server.core.application;

import com.communote.server.api.core.application.ApplicationInformation;

/**
 * Strategy for retrieving the application information on startup
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public interface ApplicationInformationProvider {

    /**
     * Resolve the application information. This method will be called on startup and allows loading
     * the information from somewhere. If this fails a
     * {@link com.communote.server.api.core.bootstrap.BootstrapException} can be thrown.
     */
    ApplicationInformation load();
}
