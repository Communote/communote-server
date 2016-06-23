package com.communote.server.core.external;

/**
 * Describes a source of external objects that can be linked to topics.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public interface ExternalObjectSource {

    /**
     * @return get the configuration of the external source. If null is returned the external source
     *         is considered to not be active and linking will not be possible.
     */
    ExternalObjectSourceConfiguration getConfiguration();

    /**
     * @return the identifier of the external object source.
     */
    String getIdentifier();
}
