package com.communote.server.core.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.communote.server.api.core.common.NotFoundException;
import com.communote.server.core.security.AuthenticationTokenManagement;
import com.communote.server.model.user.User;
import com.communote.server.persistence.user.UserDao;
import com.communote.server.test.CommunoteIntegrationTest;
import com.communote.server.test.util.AuthenticationTestUtils;
import com.communote.server.test.util.TestUtils;


/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class AuthenticationTokenManagementTest extends CommunoteIntegrationTest {

    @Autowired
    private AuthenticationTokenManagement authenticationTokenManagement;

    @Autowired
    private UserDao userDao;

    /**
     * Test for {@link AuthenticationTokenManagement#getAuthenticationToken(Long)}
     * 
     * @throws NotFoundException
     *             The test should fail, if this exception is thrown.
     */
    @Test
    public void testGetAuthenticationToken() throws NotFoundException {
        User user = TestUtils.createRandomUser(false);
        AuthenticationTestUtils.setSecurityContext(user);
        String originalToken = authenticationTokenManagement.getAuthenticationToken(user.getId());
        String originalToken2 = authenticationTokenManagement.getAuthenticationToken(user.getId());
        Assert.assertNotNull(originalToken);
        Assert.assertEquals(originalToken2, originalToken);
        String dbToken = userDao.load(user.getId()).getAuthenticationToken();
        Assert.assertEquals(dbToken.split(":")[1], originalToken);
        // Test the expiration timeout.
        user.setAuthenticationToken("0:" + originalToken);
        userDao.update(user);
        String newToken = authenticationTokenManagement.getAuthenticationToken(user.getId());
        Assert.assertNotEquals(newToken, originalToken);
    }
}
