package com.communote.server.core.security;

import java.util.Arrays;
import java.util.Collection;

import org.springframework.beans.factory.access.BootstrapException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeGroups;
import org.testng.annotations.Test;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.client.ClientTO;
import com.communote.server.api.core.common.ClientAndChannelContextHolder;
import com.communote.server.api.core.config.type.ClientProperty;
import com.communote.server.api.service.ClientRetrievalService;
import com.communote.server.persistence.user.client.ClientHelper;

/**
 * This is a test class for the
 * {@link com.communote.server.web.commons.filter.PublicAccessProcessingFilter}.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */

// TODO this test is not working anymore
public class PublicAccessProcessingFilterTest {

    @BeforeClass
    protected void beforeClassPutGlobalClientInThreadLocal() throws Exception {
        ClientTO client = ServiceLocator.findService(ClientRetrievalService.class).findClient(
                ClientHelper.getGlobalClientId());
        ClientAndChannelContextHolder.setClient(client);
    }

    /**
     * Setups "allow anonymous access".
     */
    @BeforeGroups(groups = { "allowAnonymous" })
    public void setupAllowAnonymous() {
        CommunoteRuntime
        .getInstance()
        .getConfigurationManager()
        .updateClientConfigurationProperty(ClientProperty.CLIENT_BLOG_ALLOW_PUBLIC_ACCESS,
                Boolean.toString(true));

        AnonymousAuthenticationToken authToken = new AnonymousAuthenticationToken("login",
                "anonymous", Arrays.asList(new GrantedAuthority[] { new SimpleGrantedAuthority(
                        AuthenticationHelper.ROLE_ANONYMOUS) }));
        SecurityContextHolder.getContext().setAuthentication(authToken);
    }

    /**
     * Setups "not allow anonymous access".
     */
    @BeforeGroups(groups = { "notAllowAnonymous" })
    public void setupDontAllowAnonymous() {
        CommunoteRuntime
        .getInstance()
        .getConfigurationManager()
        .updateClientConfigurationProperty(ClientProperty.CLIENT_BLOG_ALLOW_PUBLIC_ACCESS,
                Boolean.toString(false));

        AnonymousAuthenticationToken authToken = new AnonymousAuthenticationToken("login",
                "anonymous", Arrays.asList(new GrantedAuthority[] { new SimpleGrantedAuthority(
                        AuthenticationHelper.ROLE_ANONYMOUS) }));
        SecurityContextHolder.getContext().setAuthentication(authToken);
    }

    /**
     * Test the filter.
     *
     * @throws Exception
     *             in case the test fails
     */
    @Test(groups = { "allowAnonymous" })
    public void testDoFilterAllowAccess() throws Exception {
        try {
            // filter.doFilter(request, response, chain);
        } catch (BootstrapException e) {
            // Okay. Method was called, but Service is not available at the moment.
            return;
        }
        Assert.fail("Something went wrong.");
    }

    /**
     * Test the filter.
     *
     * @throws Exception
     *             in case the test fails
     */
    @Test(groups = { "notAllowAnonymous" })
    public void testDoFilterDontAllowAccess() throws Exception {
        // filter.doFilter(request, response, chain);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Collection<? extends GrantedAuthority> grantedAutorities = auth.getAuthorities();

        if (auth.getPrincipal() instanceof UserDetails
                && grantedAutorities.contains(new SimpleGrantedAuthority(
                        AuthenticationHelper.PUBLIC_USER_ROLE))) {
            Assert.fail("No anonymous access is allowed but the securityContext"
                    + "contains the PUBLIC_USER_ROLE.");
        }
        // Okay
        return;
    }

}
