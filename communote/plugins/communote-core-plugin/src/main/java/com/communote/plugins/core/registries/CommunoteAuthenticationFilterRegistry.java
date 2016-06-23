package com.communote.plugins.core.registries;

import org.apache.felix.ipojo.annotations.Bind;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Unbind;
import org.springframework.security.authentication.AuthenticationManager;

import com.communote.server.api.ServiceLocator;
import com.communote.server.core.security.AuthenticationFilterManagement;
import com.communote.server.core.security.CommunoteAuthenticationFilter;
import com.communote.server.web.WebServiceLocator;


/**
 * IPojo registry for registering {@link CommunoteAuthenticationFilter}.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
@Component
@Instantiate(name = "CommunoteAuthenticationFilterRegistry")
public class CommunoteAuthenticationFilterRegistry {

    /**
     * Method to register a filter.
     * 
     * @param filter
     *            The filter to register.
     */
    @Bind(id = "registerFilter", optional = true, aggregate = true)
    public void registerCommunoteAuthenticationFilter(CommunoteAuthenticationFilter filter) {
        AuthenticationManager authenticationManager = WebServiceLocator.instance()
                .getWebApplicationContext()
                .getBean("authenticationManager", AuthenticationManager.class);
        filter.setAuthenticationManager(authenticationManager);
        ServiceLocator.instance().getService(AuthenticationFilterManagement.class)
                .addFilter(filter);
    }

    /**
     * Method to remove a filter.
     * 
     * @param filter
     *            Filter to remove.
     */
    @Unbind(id = "registerFilter", optional = true, aggregate = true)
    public void removeCommunoteAuthenticationFilter(CommunoteAuthenticationFilter filter) {
        ServiceLocator.instance().getService(AuthenticationFilterManagement.class)
                .removeFilter(filter);
    }
}
