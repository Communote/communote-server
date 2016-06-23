package com.communote.server.core.integration;

/**
 * Inteface for link generators for external systems (integrations).
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public interface IntegrationLinkGenerator {

    /**
     * Method to get the id of the external system this integration renders links for.
     * 
     * @return The id of the external system.
     */
    String getExternalSystemId();

    /**
     * Method to get the link for the given external object.
     * 
     * @param externalObjectId
     *            Id of the external object.
     * @return The link to the object.
     */
    String getLink(String externalObjectId);

}
