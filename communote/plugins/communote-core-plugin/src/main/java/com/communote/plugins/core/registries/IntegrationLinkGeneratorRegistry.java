package com.communote.plugins.core.registries;

import org.apache.felix.ipojo.annotations.Bind;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Unbind;

import com.communote.server.api.ServiceLocator;
import com.communote.server.core.integration.IntegrationLinkGenerator;
import com.communote.server.core.integration.IntegrationService;

/**
 * Registry for {@link IntegrationLinkGenerator}.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
@Component
@Instantiate(name = "IntegrationLinkGeneratorRegistry")
public class IntegrationLinkGeneratorRegistry {
    /**
     * Method to register a link generator.
     * 
     * @param integrationLinkGenerator
     *            The provider to register.
     */
    @Bind(id = "registerIntegrationLinkGenerator", optional = true, aggregate = true)
    public void registerIntegrationLinkGenerator(IntegrationLinkGenerator integrationLinkGenerator) {
        ServiceLocator.instance().getService(IntegrationService.class)
                .addIntegrationLinkGenerator(integrationLinkGenerator);
    }

    /**
     * Method to remove a link generator.
     * 
     * @param integrationLinkGenerator
     *            provider to remove.
     */
    @Unbind(id = "registerIntegrationLinkGenerator", optional = true, aggregate = true)
    public void removeIntegrationLinkGenerator(IntegrationLinkGenerator integrationLinkGenerator) {
        ServiceLocator.instance().getService(IntegrationService.class)
                .removeIntegrationLinkGenerator(integrationLinkGenerator);
    }
}
