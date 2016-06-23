package com.communote.server.core.integration;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

/**
 * Service for integrations.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
@Service
public class IntegrationService {

    private final Map<String, IntegrationLinkGenerator> linkGenerators =
            new HashMap<String, IntegrationLinkGenerator>();

    /**
     * Sets the given generator for the external system the generator refers to. An existing
     * generator will be removed.
     * 
     * @param integrationLinkGenerator
     *            The generator to add.
     */
    public void addIntegrationLinkGenerator(IntegrationLinkGenerator integrationLinkGenerator) {
        linkGenerators
                .put(integrationLinkGenerator.getExternalSystemId(), integrationLinkGenerator);
    }

    /**
     * Returns a link to the given object within the external system.
     * 
     * @param externalSystemId
     *            The external systems id.
     * @param externalObjectId
     *            The external objects id.
     * @return The link or null, if there is no generator for the given System.
     */
    public String getIntegrationLink(String externalSystemId, String externalObjectId) {
        IntegrationLinkGenerator integrationLinkGenerator = linkGenerators.get(externalSystemId);
        if (integrationLinkGenerator != null) {
            return integrationLinkGenerator.getLink(externalObjectId);
        }
        return null;
    }

    /**
     * Removes the given generator for the external system the generator refers to.
     * 
     * @param integrationLinkGenerator
     *            The generator to remove.
     */
    public void removeIntegrationLinkGenerator(IntegrationLinkGenerator integrationLinkGenerator) {
        linkGenerators.remove(integrationLinkGenerator.getExternalSystemId());
    }
}
