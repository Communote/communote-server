package com.communote.server.web.security;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.communote.server.core.security.AuthenticationProviderManagement;
import com.communote.server.core.security.CommunoteAuthenticationProvider;
import com.communote.server.web.security.PluginAuthenticationProvider;

/**
 * Test for correct handling of PluginAuthenticationProvider.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class AuthenticationProviderManagementTest {

    /**
     * Test provider.
     */
    private class TestAuthenticationProvider implements CommunoteAuthenticationProvider {

        private final StringBuilder stringToBuild;
        private final String stringToBeAdded;
        private final int priority;
        private final boolean supports;

        /**
         * Constructor.
         * 
         * @param priority
         *            The priority.
         * @param stringToBeAdded
         *            The string to be added.
         * @param stringToBuild
         *            The string to build.
         * @param supports
         *            Return value of "supports" method.
         */
        public TestAuthenticationProvider(int priority, String stringToBeAdded,
                StringBuilder stringToBuild, boolean supports) {
            this.priority = priority;
            this.stringToBeAdded = stringToBeAdded;
            this.stringToBuild = stringToBuild;
            this.supports = supports;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Authentication authenticate(Authentication authentication)
                throws AuthenticationException {
            stringToBuild.append(stringToBeAdded);
            return authentication;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int getOrder() {
            return priority;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean supports(Class<?> authentication) {
            return supports;
        }
    }

    /**
     * Tests the correct ordering.
     */
    @Test
    public void testCorrectOrder() {
        AuthenticationProviderManagement providerManagement = new AuthenticationProviderManagement();
        List<AuthenticationProvider> providers = new ArrayList<AuthenticationProvider>();
        StringBuilder stringToBuild = new StringBuilder();
        providers.add(new TestAuthenticationProvider(0, "A", stringToBuild, false));
        PluginAuthenticationProvider pluginAuthenticationProvider = new PluginAuthenticationProvider();
        pluginAuthenticationProvider.setAuthenticationProviderManagement(providerManagement);
        providers.add(pluginAuthenticationProvider);
        providers.add(new TestAuthenticationProvider(0, "E", stringToBuild, true));

        // Plugin providers.
        CommunoteAuthenticationProvider lowPriorityProvider =
                new TestAuthenticationProvider(0, "B", stringToBuild, true);
        CommunoteAuthenticationProvider midPriorityProvider =
                new TestAuthenticationProvider(500, "C", stringToBuild, true);

        CommunoteAuthenticationProvider noProvider =
                new TestAuthenticationProvider(1000, "Z", stringToBuild, false);

        ProviderManager providerManager = new ProviderManager(providers);
        // Test without filters
        providerManager.authenticate(new TestingAuthenticationToken(null, null));
        Assert.assertEquals(stringToBuild.toString(), "E");

        // Test with 1 additional filter
        stringToBuild.delete(0, 10);
        providerManagement.addAuthenticationProvider(lowPriorityProvider);
        providerManager.authenticate(new TestingAuthenticationToken(null, null));
        Assert.assertEquals(stringToBuild.toString(), "B");

        // Test with 2 additional filter (higher priority wins)
        stringToBuild.delete(0, 10);
        providerManagement.addAuthenticationProvider(midPriorityProvider);
        providerManager.authenticate(new TestingAuthenticationToken(null, null));
        Assert.assertEquals(stringToBuild.toString(), "C");

        // Test with 3 additional filter
        stringToBuild.delete(0, 10);
        providerManagement.addAuthenticationProvider(noProvider);
        providerManager.authenticate(new TestingAuthenticationToken(null, null));
        Assert.assertEquals(stringToBuild.toString(), "C");

        // Test with 2 additional filter
        stringToBuild.delete(0, 10);
        providerManagement.removeCommunoteAuthenticationProvider(midPriorityProvider);
        providerManager.authenticate(new TestingAuthenticationToken(null, null));
        Assert.assertEquals(stringToBuild.toString(), "B");
    }
}
