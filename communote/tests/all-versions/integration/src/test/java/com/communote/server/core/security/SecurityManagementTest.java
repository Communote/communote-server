package com.communote.server.core.security;

import org.testng.annotations.Test;

import com.communote.server.model.user.User;
import com.communote.server.test.CommunoteIntegrationTest;
import com.communote.server.test.util.AuthenticationTestUtils;
import com.communote.server.test.util.TestUtils;

/**
 *
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class SecurityManagementTest extends CommunoteIntegrationTest {

    /**
     * Test the authentication helper which sets a user in the security context
     */
    @Test
    public void testAuthenticationHelper() {
        User user = TestUtils.createRandomUser(false);
        AuthenticationTestUtils.setSecurityContext(user);
        SecurityHelper.assertCurrentUser(user.getId());
    }

}
