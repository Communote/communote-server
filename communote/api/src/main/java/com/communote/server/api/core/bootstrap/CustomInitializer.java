package com.communote.server.api.core.bootstrap;

/**
 * Extension that allows custom initializations during startup. These initializers will be called
 * after the core components were initialized.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public interface CustomInitializer {

    /**
     * Do additional initializations. In case of an exception an
     * {@link ApplicationInitializationException} should be thrown.
     */
    void initialize();
}
