package com.communote.plugins.core.registries;

import org.apache.felix.ipojo.annotations.Bind;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Unbind;

import com.communote.server.api.ServiceLocator;
import com.communote.server.core.security.AuthenticationProviderManagement;
import com.communote.server.core.security.CommunoteAuthenticationProvider;

/**
 * IPojo registry for registering {@link CommunoteAuthenticationProvider}.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
@Component
@Instantiate(name = "CommunoteAuthenticationProviderRegistry")
public class CommunoteAuthenticationProviderRegistry {

    /**
     * Method to register a provider.
     * 
     * @param provider
     *            The provider to register.
     */
    @Bind(id = "registerProvider", optional = true, aggregate = true)
    public void registerNotePreProcessor(CommunoteAuthenticationProvider provider) {
        ServiceLocator.instance().getService(AuthenticationProviderManagement.class)
                .addAuthenticationProvider(provider);
    }

    /**
     * Method to remove a provider.
     * 
     * @param provider
     *            provider to remove.
     */
    @Unbind(id = "registerProvider", optional = true, aggregate = true)
    public void removeNotePreProcessor(CommunoteAuthenticationProvider provider) {
        ServiceLocator.instance().getService(AuthenticationProviderManagement.class)
                .removeCommunoteAuthenticationProvider(provider);
    }
}
