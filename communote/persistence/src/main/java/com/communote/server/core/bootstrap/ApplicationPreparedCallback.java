package com.communote.server.core.bootstrap;

import org.springframework.context.ApplicationContext;

import com.communote.server.api.core.bootstrap.BootstrapException;

/**
 * Callback which is invoked during startup of Communote after basic preparations or if Communote
 * was not yet installed as soon as the installation of Communote has been completed and Communote
 * can now be started.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public interface ApplicationPreparedCallback {

    /**
     * Method that is invoked during startup or after the installation has been completed. If this
     * method does further startup preparations and encounters an error it should throw a
     * {@link BootstrapException}.
     *
     * @param applicationContext
     *            The created ApplicationContext
     */
    void applicationPrepared(ApplicationContext applicationContext);
}
