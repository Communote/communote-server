package com.communote.server.api.core.bootstrap;

/**
 * Listener which is called during startup of the application, after the application was completely
 * initialized.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public interface InitializationCompleteListener {

    void initializationComplete();
}
