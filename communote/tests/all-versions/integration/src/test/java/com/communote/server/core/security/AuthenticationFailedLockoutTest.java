package com.communote.server.core.security;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.common.ClientAndChannelContextHolder;
import com.communote.server.api.core.config.ClientConfigurationPropertyConstant;
import com.communote.server.api.core.config.type.ClientPropertySecurity;
import com.communote.server.api.core.user.UserVO;
import com.communote.server.core.security.authentication.AuthenticationFailedEventPublisher;
import com.communote.server.core.security.authentication.database.DatabaseAuthenticationProvider;
import com.communote.server.core.user.UserManagement;
import com.communote.server.external.acegi.UserAccountPermanentlyLockedException;
import com.communote.server.external.acegi.UserAccountTemporarilyLockedException;
import com.communote.server.model.security.ChannelType;
import com.communote.server.model.user.UserRole;
import com.communote.server.model.user.security.UnlockUserSecurityCode;
import com.communote.server.persistence.common.security.SecurityCodeNotFoundException;
import com.communote.server.persistence.user.UserDao;
import com.communote.server.persistence.user.security.UnlockUserSecurityCodeDao;
import com.communote.server.test.CommunoteIntegrationTest;
import com.communote.server.test.util.AuthenticationTestUtils;
import com.communote.server.test.util.TestUtils;

/**
 * Tests for locking an account after recurring failed authentication attempts
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class AuthenticationFailedLockoutTest extends CommunoteIntegrationTest {

    private AuthenticationManager authManager;
    private UsernamePasswordAuthenticationToken validAuth;
    private UsernamePasswordAuthenticationToken invalidAuth;

    @Autowired
    private UserDao userDao;
    @Autowired
    private UnlockUserSecurityCodeDao unlockUserSecurityCodeDao;
    @Autowired
    private UserManagement userManagement;

    /**
     * @param token
     *            the token to use
     * @param channel
     *            the channel
     * @throws AuthenticationException
     *             in case of an error
     */
    private void authProcess(UsernamePasswordAuthenticationToken token, ChannelType channel)
            throws AuthenticationException {
        ClientAndChannelContextHolder.setChannel(channel);
        authManager.authenticate(token);
    }

    /**
     * get property: authentication permanent lock limit
     *
     * @return specific value
     */
    private int getFailedAuthLimitPermlock() {
        return CommunoteRuntime
                .getInstance()
                .getConfigurationManager()
                .getClientConfigurationProperties()
                .getProperty(ClientPropertySecurity.FAILED_AUTH_LIMIT_PERMLOCK,
                        ClientPropertySecurity.DEFAULT_FAILED_AUTH_LIMIT_PERMLOCK);
    }

    /**
     * get property: authentication permanent lock limit
     *
     * @return specific value
     */
    private int getFailedAuthLockedTimespan() {
        return CommunoteRuntime
                .getInstance()
                .getConfigurationManager()
                .getClientConfigurationProperties()
                .getProperty(ClientPropertySecurity.FAILED_AUTH_LOCKED_TIMESPAN,
                        ClientPropertySecurity.DEFAULT_FAILED_AUTH_LOCKED_TIMESPAN);
    }

    /**
     * get property: authentication permanent lock limit
     *
     * @return specific value
     */
    private int getFailedAuthStepsTemplock() {
        return CommunoteRuntime
                .getInstance()
                .getConfigurationManager()
                .getClientConfigurationProperties()
                .getProperty(ClientPropertySecurity.FAILED_AUTH_STEPS_TEMPLOCK,
                        ClientPropertySecurity.DEFAULT_FAILED_AUTH_STEPS_TEMPLOCK);
    }

    /**
     * @param alias
     *            the alias
     * @param password
     *            the password
     * @param email
     *            the email
     * @throws Exception
     *             in case of an error
     */
    @BeforeClass(dependsOnGroups = "integration-test-setup")
    public void init() throws Exception {
        UserVO userVO = TestUtils.createKenmeiUserVO(TestUtils.createRandomUserAlias(),
                UserRole.ROLE_KENMEI_USER);
        userVO.setPlainPassword(true);
        userVO.setPassword("123456");
        AuthenticationTestUtils.setManagerContext();
        userManagement.createUser(userVO, false, false);
        Map<ClientConfigurationPropertyConstant, String> map;
        map = new HashMap<ClientConfigurationPropertyConstant, String>();
        // set lower limit for getting permanently locked (to speed up test)
        map.put(ClientPropertySecurity.FAILED_AUTH_LIMIT_PERMLOCK, String.valueOf(6));
        // set shorter wait time for temporarily locked users
        map.put(ClientPropertySecurity.FAILED_AUTH_LOCKED_TIMESPAN, String.valueOf(3));
        CommunoteRuntime.getInstance().getConfigurationManager()
        .updateClientConfigurationProperties(map);
        AuthenticationTestUtils.setAuthentication(null);
        // initiate authenticationManager
        ArrayList<AuthenticationProvider> providers = new ArrayList<>();
        providers.add(new DatabaseAuthenticationProvider());
        ProviderManager providerManager = new ProviderManager(providers);
        providerManager.setAuthenticationEventPublisher(new AuthenticationFailedEventPublisher());
        authManager = providerManager;
        // create valid user + password-token
        validAuth = new UsernamePasswordAuthenticationToken(userVO.getAlias(), userVO.getPassword());
        // create invalid user + password-token
        invalidAuth = new UsernamePasswordAuthenticationToken(userVO.getAlias(),
                userVO.getPassword() + "invalid");
    }

    /**
     * test the authentication process
     *
     */
    @Test
    public void testAuthenticationLocking() {
        final int stepsTemplock = getFailedAuthStepsTemplock();
        final int limitPermlock = getFailedAuthLimitPermlock();
        final int lockedTimespan = getFailedAuthLockedTimespan();

        // 1. STEP: valid authentications for all channels
        try {
            authProcess(validAuth, ChannelType.WEB);
            authProcess(validAuth, ChannelType.RSS);
            authProcess(validAuth, ChannelType.XMPP);
            authProcess(validAuth, ChannelType.API);
        } catch (AuthenticationException failed) {
            Assert.fail("something went wrong with valid authentication");
        }

        // 2. STEP: increase invalid attempts on WEB channel to temporary locked limit
        for (int i = 1; i <= stepsTemplock; ++i) {
            try {
                authProcess(invalidAuth, ChannelType.WEB);
                Assert.fail("a invalid login passed through auth process");
            } catch (AuthenticationException failed) {
                if (failed instanceof UserAccountTemporarilyLockedException) {
                    Assert.fail("UserAccountTemporarilyLockedException was thrown in the false place");
                }
            }
        }

        // 3. STEP: check if the user account for channel is temporary locked
        try {
            authProcess(validAuth, ChannelType.WEB);
            Assert.fail("the user account was not temporary locked although the limit was exceeded");
        } catch (UserAccountTemporarilyLockedException failed) {
            // Do nothing.
        }

        // 4. STEP: check if the user can authenticate on other channel
        try {
            authProcess(validAuth, ChannelType.RSS);
            authProcess(validAuth, ChannelType.XMPP);
            authProcess(validAuth, ChannelType.API);
        } catch (AuthenticationException failed) {
            Assert.fail("the user can not authenticate on other channel");
        }

        // 5. STEP: wait the temporary locked time + 1sec
        sleep((lockedTimespan + 1) * 1000);

        // 6. STEP: test if the user can login on WEB channel
        try {
            authProcess(validAuth, ChannelType.WEB);
        } catch (AuthenticationException failed) {
            Assert.fail("the user can't wrongly authenticate on WEB channel");
        }

        // 7. STEP: increase invalid attempts on RSS channel to permanently locked limit (will take
        // a while)
        int tempLockedCount = 0;
        for (int i = 1; i <= limitPermlock; ++i) {
            try {
                authProcess(invalidAuth, ChannelType.RSS);
                Assert.assertTrue(false, "a invalid login passed through auth process");
            } catch (AuthenticationException failed) {
                if (i % stepsTemplock == 0) {
                    // TODO define a test service which allows resetting the locked time span
                    // wait until the temporary locked timeout is over
                    tempLockedCount++;
                    sleep((lockedTimespan + 1) * tempLockedCount * 1000);
                }
                if (failed instanceof UserAccountPermanentlyLockedException) {
                    Assert.fail("UserAccountPermanentlyLockedException was thrown in the false place");
                }
            }
        }

        // 8. STEP: check if the user can authenticate on RSS channel
        try {
            authProcess(validAuth, ChannelType.RSS);
            Assert.fail("the user account was not permanently locked although the limit was exceeded");
        } catch (UserAccountPermanentlyLockedException failed) {
        }

        // 9. STEP: check if the user can authenticate on other channel
        try {
            authProcess(validAuth, ChannelType.WEB);
            authProcess(validAuth, ChannelType.XMPP);
            authProcess(validAuth, ChannelType.API);
        } catch (AuthenticationException failed) {
            Assert.fail("the user can not authenticate on other channel");
        }

        // 10. STEP: remove permanently locking
        Long userId = userManagement.findUserByAlias(validAuth.getName()).getId();
        UnlockUserSecurityCode code = unlockUserSecurityCodeDao.findByUserAndChannel(userId,
                ChannelType.RSS);
        try {
            userManagement.unlockUser(code.getCode());
        } catch (SecurityCodeNotFoundException e) {
            Assert.fail("Unlock code of user not found.");
        }
        // getAuthenticationFailedStatusDao().remove(authFailedStatus);

        // 11. STEP: check if the user can authenticate on RSS channel
        try {
            authProcess(validAuth, ChannelType.RSS);
        } catch (UserAccountPermanentlyLockedException failed) {
            Assert.fail("the user account is still  permanently locked");
        }
    }
}
