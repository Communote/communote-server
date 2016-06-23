package com.communote.server.core.security;

import org.springframework.security.access.AccessDeniedException;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.communote.server.test.CommunoteIntegrationTest;

/**
 * Test the security context for the internal system user
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class SystemUserTest extends CommunoteIntegrationTest {

    /**
     * Test the change of security context for the internal system user
     */
    @Test
    public void testInternalSystem() {

        Assert.assertFalse(SecurityHelper.isInternalSystem(),
                "It should not be the interal system before.");

        AuthenticationHelper.setInternalSystemToSecurityContext();

        Assert.assertTrue(SecurityHelper.isInternalSystem(), "It should be the interal system now.");

        Assert.assertFalse(SecurityHelper.isPublicUser(), "It should not be the public user.");

        try {
            SecurityHelper.assertCurrentUserOrPublicUser();
            Assert.fail("Current user must not be a regular user or the public user");
        } catch (AccessDeniedException ade) {
            // expected
        }
        AuthenticationHelper.removeAuthentication();

        Assert.assertFalse(SecurityHelper.isInternalSystem(),
                "It should not be the interal system after.");
    }
}
