package com.communote.server.core.external;

/**
 * Provides some configuration options that should be considered when linking external objects of
 * the associated source with a topic.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public interface ExternalObjectSourceConfiguration {

    /**
     * @return the number of external objects that can be linked to one topic. If 0 is returned
     *         there is no limit.
     */
    int getNumberOfMaximumExternalObjectsPerTopic();
}
