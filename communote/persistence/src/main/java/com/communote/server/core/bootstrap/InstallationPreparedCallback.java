package com.communote.server.core.bootstrap;

/**
 * Callback which is invoked during startup of Communote when Communote is not yet installed but
 * basic preparations like setting up logging are completed and the installation can now be started.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public interface InstallationPreparedCallback {

    /**
     * Method that is invoked after the installation has been prepared.
     */
    void installationPrepared();
}
