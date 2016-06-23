package com.communote.server.api.core.application;

/**
 * Builder to configure and create the {@link Runtime}.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public interface RuntimeBuilder {

    /**
     * @return the configured runtime
     */
    Runtime build();
}
