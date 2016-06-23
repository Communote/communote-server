package com.communote.server.core.user;

import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.communote.common.converter.IdentityConverter;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.common.ClientAndChannelContextHolder;
import com.communote.server.api.core.config.type.ClientProperty;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.core.config.ClientConfigurationHelper;
import com.communote.server.core.security.AccountNotActivatedException;
import com.communote.server.core.security.AccountTemporarilyDisabledException;
import com.communote.server.core.security.AuthenticationHelper;
import com.communote.server.core.security.AuthenticationManagement;
import com.communote.server.core.security.TermsNotAcceptedException;
import com.communote.server.core.user.InvalidUserStatusTransitionException;
import com.communote.server.core.user.UserManagement;
import com.communote.server.model.security.ChannelType;
import com.communote.server.model.user.User;
import com.communote.server.model.user.UserRole;
import com.communote.server.model.user.UserStatus;
import com.communote.server.persistence.user.ExternalUserVO;
import com.communote.server.test.CommunoteIntegrationTest;
import com.communote.server.test.util.TestUtils;

/**
 * Tests for the Terms of Use feature.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */

// TODO calling resetTermsOfUse which affects all users. Should we accept terms for all users in
// afterClass method?

public class TermsOfUseTest extends CommunoteIntegrationTest {

    private UserManagement userManagement;
    private User adminUser;

    private boolean setAutomaticActivation(boolean activateAutomatically) {
        boolean currentValue = ClientProperty.AUTOMATIC_USER_ACTIVATION
                .getValue(ClientConfigurationHelper.DEFAULT_AUTOMATIC_USER_ACTIVATION);
        Authentication curentAuth = AuthenticationHelper.setAsAuthenticatedUser(adminUser);
        try {
            CommunoteRuntime
            .getInstance()
            .getConfigurationManager()
                    .updateClientConfigurationProperty(ClientProperty.AUTOMATIC_USER_ACTIVATION,
                            String.valueOf(activateAutomatically));
        } finally {
            AuthenticationHelper.setAuthentication(curentAuth);
        }
        return currentValue;
    }

    private boolean setTermsHaveToBeAccepted(boolean accept) {
        boolean currentValue = ClientProperty.TERMS_OF_USE_USERS_MUST_ACCEPT
                .getValue(ClientProperty.DEFAULT_TERMS_OF_USE_USERS_MUST_ACCEPT);
        Authentication curentAuth = AuthenticationHelper.setAsAuthenticatedUser(adminUser);
        try {
            CommunoteRuntime
            .getInstance()
            .getConfigurationManager()
            .updateClientConfigurationProperty(
                    ClientProperty.TERMS_OF_USE_USERS_MUST_ACCEPT, String.valueOf(accept));
        } finally {
            AuthenticationHelper.setAuthentication(curentAuth);
        }
        return currentValue;
    }

    /**
     * prepare tests
     */
     @BeforeClass
     public void setup() {
         this.userManagement = ServiceLocator.findService(UserManagement.class);
         this.adminUser = TestUtils.createRandomUser(true);
     }

     /**
      * Test accepting the terms.
      *
      * @throws Exception
      *             in case the test failed
      */
     @Test
     public void testAcceptTerms() throws Exception {
         Authentication currentAuth = null;
         boolean mustAcceptTerms = setTermsHaveToBeAccepted(false);
         try {
             User userWithAcceptedTerms = TestUtils.createRandomUser(false);
             setTermsHaveToBeAccepted(true);
             User userWithoutAcceptedTerms = TestUtils.createRandomUser(false);
             Assert.assertTrue(userWithoutAcceptedTerms.hasStatus(UserStatus.TERMS_NOT_ACCEPTED));
             try {
                 currentAuth = AuthenticationHelper.setAsAuthenticatedUser(userWithAcceptedTerms);
                 userManagement.acceptTermsOfUse(userWithoutAcceptedTerms.getId());
                 Assert.fail("A user should not be allowed to accept the terms of use for another user");
             } catch (AuthorizationException e) {
                 // expected
             }
             try {
                 AuthenticationHelper.setAsAuthenticatedUser(adminUser);
                 userManagement.acceptTermsOfUse(userWithoutAcceptedTerms.getId());
                 Assert.fail("An admin should not be allowed to accept the terms of use for another user");
             } catch (AuthorizationException e) {
                 // expected
             }
             userWithoutAcceptedTerms = userManagement.getUserById(userWithoutAcceptedTerms.getId(),
                     new IdentityConverter<User>());
             Assert.assertTrue(userWithoutAcceptedTerms.hasStatus(UserStatus.TERMS_NOT_ACCEPTED));
             // current user should be allowed to accept terms
             AuthenticationHelper.setAsAuthenticatedUser(userWithoutAcceptedTerms);
             userManagement.acceptTermsOfUse(userWithoutAcceptedTerms.getId());
             User user = userManagement.getUserById(userWithoutAcceptedTerms.getId(),
                     new IdentityConverter<User>());
             Assert.assertEquals(user.getStatus(), UserStatus.ACTIVE);
             Assert.assertTrue(user.isTermsAccepted());
             Assert.assertTrue(user.isActivated());
             // internal system should also be allowed
             userWithoutAcceptedTerms = TestUtils.createRandomUser(false);
             Assert.assertTrue(userWithoutAcceptedTerms.hasStatus(UserStatus.TERMS_NOT_ACCEPTED));
             SecurityContext curContext = AuthenticationHelper.setInternalSystemToSecurityContext();
             userManagement.acceptTermsOfUse(userWithoutAcceptedTerms.getId());
             user = userManagement.getUserById(userWithoutAcceptedTerms.getId(),
                     new IdentityConverter<User>());
             Assert.assertEquals(user.getStatus(), UserStatus.ACTIVE);
             Assert.assertTrue(user.isTermsAccepted());
             Assert.assertTrue(user.isActivated());
             AuthenticationHelper.setSecurityContext(curContext);
             // final case resulting from interactive login: no user is set
             userWithoutAcceptedTerms = TestUtils.createRandomUser(false);
             AuthenticationHelper.setAuthentication(null);
             userManagement.acceptTermsOfUse(userWithoutAcceptedTerms.getId());
             user = userManagement.getUserById(userWithoutAcceptedTerms.getId(),
                     new IdentityConverter<User>());
             Assert.assertEquals(user.getStatus(), UserStatus.ACTIVE);
             Assert.assertTrue(user.isTermsAccepted());
             Assert.assertTrue(user.isActivated());
             // confirmed user should not be able to accept terms
             user = TestUtils.createRandomUser(true, null, UserRole.ROLE_KENMEI_USER);
             Assert.assertTrue(user.hasStatus(UserStatus.CONFIRMED));
             userManagement.acceptTermsOfUse(user.getId());
             user = userManagement.getUserById(user.getId(), new IdentityConverter<User>());
             Assert.assertEquals(user.getStatus(), UserStatus.CONFIRMED);
             Assert.assertFalse(user.isTermsAccepted());
             Assert.assertFalse(user.isActivated());
         } finally {
             setTermsHaveToBeAccepted(mustAcceptTerms);
             if (currentAuth != null) {
                 AuthenticationHelper.setAuthentication(currentAuth);
             }
         }
     }

     /**
      * Test that new users which have not accepted the terms of use are not counted as active users.
      *
      * @throws Exception
      *             in case the test failed
      */
     @Test
     public void testActiveUserCount() throws Exception {
         long currentUserCount = userManagement.getActiveUserCount();
         boolean mustAcceptTerms = setTermsHaveToBeAccepted(true);
         try {
             User user = TestUtils.createRandomUser(false);
             long newUserCount = userManagement.getActiveUserCount();
             Assert.assertEquals(newUserCount, currentUserCount);
             // accept terms of use, user should be counted as active users
             Authentication currentAuth = AuthenticationHelper.setAsAuthenticatedUser(user);
             try {
                 userManagement.acceptTermsOfUse(user.getId());
                 newUserCount = userManagement.getActiveUserCount();
                 Assert.assertEquals(newUserCount, currentUserCount + 1);
                 user = userManagement.getUserById(user.getId(), new IdentityConverter<User>());
                 Assert.assertEquals(user.getStatus(), UserStatus.ACTIVE);
                 Assert.assertTrue(user.isTermsAccepted());
                 Assert.assertTrue(user.isActivated());
             } finally {
                 AuthenticationHelper.setAuthentication(currentAuth);
             }
             setTermsHaveToBeAccepted(false);
             user = TestUtils.createRandomUser(false);
             newUserCount = userManagement.getActiveUserCount();
             Assert.assertEquals(newUserCount, currentUserCount + 2);
         } finally {
             setTermsHaveToBeAccepted(mustAcceptTerms);
         }
     }

     /**
      * Test changing user status by manager when users do not have to accept the terms of use
      *
      * @throws Exception
      *             in case the test failed
      */
     // TODO move to another test class?
     @Test
     public void testChangeStatusByManagerIfTermsNotRequired() throws Exception {
         boolean mustAcceptTerms = setTermsHaveToBeAccepted(true);
         Authentication currentAuth = null;
         try {
             User userWithoutAcceptedTerms = TestUtils.createRandomUser(false);
             setTermsHaveToBeAccepted(false);
             User userWithAcceptedTerms = TestUtils.createRandomUser(false);
             User confirmedUser = TestUtils.createRandomUser(true, null, UserRole.ROLE_KENMEI_USER);
             currentAuth = AuthenticationHelper.setAsAuthenticatedUser(adminUser);
             User invitedUser = userManagement.inviteUserToClient(TestUtils.createKenmeiUserVO(
                     TestUtils.createRandomUserAlias(), UserRole.ROLE_KENMEI_USER));

             userManagement.changeUserStatusByManager(confirmedUser.getId(), UserStatus.ACTIVE);
             User user = userManagement.getUserById(confirmedUser.getId(),
                     new IdentityConverter<User>());
             Assert.assertEquals(user.getStatus(), UserStatus.ACTIVE);
             Assert.assertTrue(user.isTermsAccepted());

             userManagement.changeUserStatusByManager(userWithoutAcceptedTerms.getId(),
                     UserStatus.ACTIVE);
             user = userManagement.getUserById(userWithoutAcceptedTerms.getId(),
                     new IdentityConverter<User>());
             Assert.assertEquals(user.getStatus(), UserStatus.ACTIVE);
             Assert.assertTrue(user.isTermsAccepted());

             userManagement.changeUserStatusByManager(userWithAcceptedTerms.getId(),
                     UserStatus.TEMPORARILY_DISABLED);
             user = userManagement.getUserById(userWithAcceptedTerms.getId(),
                     new IdentityConverter<User>());
             Assert.assertEquals(user.getStatus(), UserStatus.TEMPORARILY_DISABLED);
             Assert.assertTrue(user.isTermsAccepted());
             userManagement.changeUserStatusByManager(user.getId(), UserStatus.ACTIVE);
             user = userManagement.getUserById(user.getId(), new IdentityConverter<User>());
             Assert.assertEquals(user.getStatus(), UserStatus.ACTIVE);
             Assert.assertTrue(user.isTermsAccepted());

             userManagement.changeUserStatusByManager(invitedUser.getId(), UserStatus.ACTIVE);
             user = userManagement.getUserById(invitedUser.getId(), new IdentityConverter<User>());
             Assert.assertEquals(user.getStatus(), UserStatus.ACTIVE);
             Assert.assertTrue(user.isTermsAccepted());
         } finally {
             setTermsHaveToBeAccepted(mustAcceptTerms);
             if (currentAuth != null) {
                 AuthenticationHelper.setAuthentication(currentAuth);
             }
         }
     }

     /**
      * Test changing user status by manager when users have to accept the terms of use
      *
      * @throws Exception
      *             in case the test failed
      */
     @Test
     public void testChangeStatusByManagerIfTermsRequired() throws Exception {
         boolean mustAcceptTerms = setTermsHaveToBeAccepted(false);
         Authentication currentAuth = null;
         try {
             User userWithAcceptedTerms = TestUtils.createRandomUser(false);
             User confirmedUser = TestUtils.createRandomUser(true, null, UserRole.ROLE_KENMEI_USER);
             setTermsHaveToBeAccepted(true);
             User userWithoutAcceptedTerms = TestUtils.createRandomUser(false);
             currentAuth = AuthenticationHelper.setAsAuthenticatedUser(adminUser);
             User invitedUser = userManagement.inviteUserToClient(TestUtils.createKenmeiUserVO(
                     TestUtils.createRandomUserAlias(), UserRole.ROLE_KENMEI_USER));
             Assert.assertTrue(invitedUser.hasStatus(UserStatus.INVITED));
             // should not be possible to change terms accepted by other user than current
             userManagement.changeUserStatusByManager(userWithoutAcceptedTerms.getId(),
                     UserStatus.ACTIVE);
             User user = userManagement.getUserById(userWithoutAcceptedTerms.getId(),
                     new IdentityConverter<User>());
             Assert.assertEquals(user.getStatus(), UserStatus.TERMS_NOT_ACCEPTED);
             Assert.assertFalse(user.isTermsAccepted());
             // changing TERMS_NOT_ACCEPTED to TEMP_DISABLED should fail (because it is not
             // revertable)
             try {
                 userManagement.changeUserStatusByManager(userWithoutAcceptedTerms.getId(),
                         UserStatus.TEMPORARILY_DISABLED);
                 Assert.fail("Disabling terms not accepted user should fail");
             } catch (InvalidUserStatusTransitionException e) {
                 // expected
             }
             user = userManagement.getUserById(userWithoutAcceptedTerms.getId(),
                     new IdentityConverter<User>());
             Assert.assertEquals(user.getStatus(), UserStatus.TERMS_NOT_ACCEPTED);
             Assert.assertFalse(user.isTermsAccepted());
             // when activating a confirmed user the user should get status TERMS_NOT_ACCEPTED
             userManagement.changeUserStatusByManager(confirmedUser.getId(), UserStatus.ACTIVE);
             user = userManagement.getUserById(confirmedUser.getId(), new IdentityConverter<User>());
             Assert.assertEquals(user.getStatus(), UserStatus.TERMS_NOT_ACCEPTED);
             Assert.assertFalse(user.isTermsAccepted());
             // active users should not be affected
             userManagement.changeUserStatusByManager(userWithAcceptedTerms.getId(),
                     UserStatus.TEMPORARILY_DISABLED);
             user = userManagement.getUserById(userWithAcceptedTerms.getId(),
                     new IdentityConverter<User>());
             Assert.assertEquals(user.getStatus(), UserStatus.TEMPORARILY_DISABLED);
             Assert.assertTrue(user.isTermsAccepted());
             try {
                 userManagement.changeUserStatusByManager(userWithAcceptedTerms.getId(),
                         UserStatus.TERMS_NOT_ACCEPTED);
                 Assert.fail("Changing disabled user to terms not affected should fail");
             } catch (InvalidUserStatusTransitionException e) {
                 // expected
                 user = userManagement.getUserById(userWithAcceptedTerms.getId(),
                         new IdentityConverter<User>());
                 Assert.assertEquals(user.getStatus(), UserStatus.TEMPORARILY_DISABLED);
             }
             userManagement.changeUserStatusByManager(userWithAcceptedTerms.getId(),
                     UserStatus.ACTIVE);
             user = userManagement.getUserById(userWithAcceptedTerms.getId(),
                     new IdentityConverter<User>());
             Assert.assertEquals(user.getStatus(), UserStatus.ACTIVE);
             Assert.assertTrue(user.isTermsAccepted());
             try {
                 userManagement.changeUserStatusByManager(userWithAcceptedTerms.getId(),
                         UserStatus.TERMS_NOT_ACCEPTED);
                 Assert.fail("Changing active user to terms not affected should fail");
             } catch (InvalidUserStatusTransitionException e) {
                 // expected
                 user = userManagement.getUserById(userWithAcceptedTerms.getId(),
                         new IdentityConverter<User>());
                 Assert.assertEquals(user.getStatus(), UserStatus.ACTIVE);
             }
             // invited users should not be activated
             userManagement.changeUserStatusByManager(invitedUser.getId(), UserStatus.ACTIVE);
             user = userManagement.getUserById(invitedUser.getId(), new IdentityConverter<User>());
             Assert.assertEquals(user.getStatus(), UserStatus.TERMS_NOT_ACCEPTED);
             Assert.assertFalse(user.isTermsAccepted());
         } finally {
             setTermsHaveToBeAccepted(mustAcceptTerms);
             if (currentAuth != null) {
                 AuthenticationHelper.setAuthentication(currentAuth);
             }
         }
     }

     /**
      * Test that external users are not activated when updating the details of a user with status
      * TERMS_NOT_ACCEPTED.
      *
      * @throws Exception
      *             in case the test failed
      */
     @Test
     public void testExternalUser() throws Exception {
         boolean mustAcceptTerms = setTermsHaveToBeAccepted(true);
         boolean automaticActivation = setAutomaticActivation(true);
         Authentication currentAuth = null;
         try {
             ExternalUserVO userVo = TestUtils.createRandomUserVoForExternalSystem(UUID.randomUUID()
                     .toString().replace("-", ""));
             userVo.setStatus(UserStatus.ACTIVE);
             // create use-case
             Long userId = userManagement.createOrUpdateExternalUser(userVo).getId();
             User user = userManagement.getUserById(userId, new IdentityConverter<User>());
             Assert.assertEquals(user.getStatus(), UserStatus.TERMS_NOT_ACCEPTED);
             Assert.assertFalse(user.isTermsAccepted());
             Assert.assertFalse(user.isActivated());
             // update use-case: status should not be overridden
             userVo.setUpdateFirstName(true);
             userVo.setUpdateLastName(true);
             userVo.setLastName(userVo.getLastName() + "1");
             Assert.assertEquals(userManagement.createOrUpdateExternalUser(userVo).getId(), userId);
             user = userManagement.getUserById(userId, new IdentityConverter<User>());
             Assert.assertEquals(user.getStatus(), UserStatus.TERMS_NOT_ACCEPTED);
             Assert.assertFalse(user.isTermsAccepted());
             // test other update method too
             userVo.setFirstName(userVo.getFirstName() + "1");
             Assert.assertEquals(userManagement.updateExternalUser(userVo).getId(), userId);
             user = userManagement.getUserById(userId, new IdentityConverter<User>());
             Assert.assertEquals(user.getStatus(), UserStatus.TERMS_NOT_ACCEPTED);
             Assert.assertFalse(user.isTermsAccepted());
             // test behavior with disabled automatic activation with a new user
             setAutomaticActivation(false);
             userVo = TestUtils.createRandomUserVoForExternalSystem(UUID.randomUUID().toString()
                     .replace("-", ""));
             userVo.setStatus(UserStatus.ACTIVE);
             // create use-case
             userId = userManagement.createOrUpdateExternalUser(userVo).getId();
             user = userManagement.getUserById(userId, new IdentityConverter<User>());
             Assert.assertEquals(user.getStatus(), UserStatus.CONFIRMED);
             Assert.assertFalse(user.isTermsAccepted());
             userVo.setUpdateFirstName(true);
             userVo.setUpdateLastName(true);
             // update, status should not change. Test both methods.
             userVo.setLastName(userVo.getLastName() + "1");
             try {
                 userManagement.updateExternalUser(userVo);
                 Assert.fail("Admin must activate the user");
             } catch (InvalidUserStatusTransitionException e) {
                 // expected
             }
             try {
                 userManagement.createOrUpdateExternalUser(userVo);
                 Assert.fail("Admin must activate the user");
             } catch (InvalidUserStatusTransitionException e) {
                 // expected
             }
             // activate by manager
             currentAuth = AuthenticationHelper.setAsAuthenticatedUser(adminUser);
             userManagement.changeUserStatusByManager(userId, UserStatus.ACTIVE);
             user = userManagement.getUserById(userId, new IdentityConverter<User>());
             Assert.assertEquals(user.getStatus(), UserStatus.TERMS_NOT_ACCEPTED);
             Assert.assertFalse(user.isTermsAccepted());
             // accept terms as user
             AuthenticationHelper.setAsAuthenticatedUser(user);
             userManagement.acceptTermsOfUse(userId);
             user = userManagement.getUserById(userId, new IdentityConverter<User>());
             Assert.assertEquals(user.getStatus(), UserStatus.ACTIVE);
             Assert.assertTrue(user.isTermsAccepted());
         } finally {
             setTermsHaveToBeAccepted(mustAcceptTerms);
             setAutomaticActivation(automaticActivation);
             if (currentAuth != null) {
                 AuthenticationHelper.setAuthentication(currentAuth);
             }
         }
     }

     /**
      * Test correct handling of resetTermsOfUse.
      *
      * @throws Exception
      *             in case the test failed
      */
     @Test
     public void testResetTermsOfUse() throws Exception {
         Authentication currentAuth = null;
         boolean mustAcceptTerms = setTermsHaveToBeAccepted(false);
         try {
             User activeUser = TestUtils.createRandomUser(false);
             User confirmedUser = TestUtils.createRandomUser(true, null, UserRole.ROLE_KENMEI_USER);
             User disabledUser = TestUtils.createRandomUser(false);
             setTermsHaveToBeAccepted(true);
             User termsNotAcceptedUser = TestUtils.createRandomUser(false);
             currentAuth = AuthenticationHelper.setAsAuthenticatedUser(adminUser);
             userManagement.changeUserStatusByManager(disabledUser.getId(),
                     UserStatus.TEMPORARILY_DISABLED);
             User invitedUser = userManagement.inviteUserToClient(TestUtils.createKenmeiUserVO(
                     TestUtils.createRandomUserAlias(), UserRole.ROLE_KENMEI_USER));
             long activeUserCount = userManagement.getActiveUserCount();

             userManagement.resetTermsOfUse();
             // active user count should have changed
             Assert.assertEquals(userManagement.getActiveUserCount(), activeUserCount);
             // current admin user should be excluded
             User user = userManagement
                     .getUserById(adminUser.getId(), new IdentityConverter<User>());
             Assert.assertEquals(user.getStatus(), UserStatus.ACTIVE);
             Assert.assertTrue(user.isTermsAccepted());
             // check status and terms of use flag of users. Status should never change, but the flag
             // should be false now.
             user = userManagement.getUserById(activeUser.getId(), new IdentityConverter<User>());
             Assert.assertEquals(user.getStatus(), UserStatus.ACTIVE);
             Assert.assertFalse(user.isTermsAccepted());
             user = userManagement.getUserById(disabledUser.getId(), new IdentityConverter<User>());
             Assert.assertEquals(user.getStatus(), UserStatus.TEMPORARILY_DISABLED);
             Assert.assertFalse(user.isTermsAccepted());
             user = userManagement.getUserById(confirmedUser.getId(), new IdentityConverter<User>());
             Assert.assertEquals(user.getStatus(), UserStatus.CONFIRMED);
             Assert.assertFalse(user.isTermsAccepted());
             user = userManagement.getUserById(termsNotAcceptedUser.getId(),
                     new IdentityConverter<User>());
             Assert.assertEquals(user.getStatus(), UserStatus.TERMS_NOT_ACCEPTED);
             Assert.assertFalse(user.isTermsAccepted());
             user = userManagement.getUserById(invitedUser.getId(), new IdentityConverter<User>());
             Assert.assertEquals(user.getStatus(), UserStatus.INVITED);
             Assert.assertFalse(user.isTermsAccepted());
             // try accepting terms
             AuthenticationHelper.setAsAuthenticatedUser(userManagement.getUserById(
                     activeUser.getId(), new IdentityConverter<User>()));
             userManagement.acceptTermsOfUse(activeUser.getId());
             user = userManagement.getUserById(activeUser.getId(), new IdentityConverter<User>());
             Assert.assertEquals(user.getStatus(), UserStatus.ACTIVE);
             Assert.assertTrue(user.isTermsAccepted());
             AuthenticationHelper.setAsAuthenticatedUser(userManagement.getUserById(
                     termsNotAcceptedUser.getId(), new IdentityConverter<User>()));
             userManagement.acceptTermsOfUse(termsNotAcceptedUser.getId());
             user = userManagement.getUserById(termsNotAcceptedUser.getId(),
                     new IdentityConverter<User>());
             Assert.assertEquals(user.getStatus(), UserStatus.ACTIVE);
             Assert.assertTrue(user.isTermsAccepted());
             // check that authorization is tested
             try {
                 userManagement.resetTermsOfUse();
                 Assert.fail("Only admin should be allowed to reset the terms of use");
             } catch (AuthorizationException e) {
                 // expeted
             }
         } finally {
             setTermsHaveToBeAccepted(mustAcceptTerms);
             if (currentAuth != null) {
                 AuthenticationHelper.setAuthentication(currentAuth);
             }
         }
     }

     /**
      * Test that UserManagement.createUser is respecting the setting which defines whether the Terms
     * of Use have to be accepted.
     *
      * @throws Exception
      *             in case the test failed
      */
     @Test
     public void testUserCreation() throws Exception {
         boolean mustAcceptTerms = setTermsHaveToBeAccepted(true);
         try {
             User user = TestUtils.createRandomUser(false);
             Assert.assertEquals(user.getStatus(), UserStatus.TERMS_NOT_ACCEPTED);
             Assert.assertFalse(user.isTermsAccepted());
             Assert.assertFalse(user.isActivated());
             user = TestUtils.createRandomUser(true, null, UserRole.ROLE_KENMEI_USER);
             Assert.assertEquals(user.getStatus(), UserStatus.CONFIRMED);
             Assert.assertFalse(user.isTermsAccepted());
             Assert.assertFalse(user.isActivated());
             setTermsHaveToBeAccepted(false);
             user = TestUtils.createRandomUser(false);
             Assert.assertEquals(user.getStatus(), UserStatus.ACTIVE);
             Assert.assertTrue(user.isTermsAccepted());
             Assert.assertTrue(user.isActivated());
             user = TestUtils.createRandomUser(true, null, UserRole.ROLE_KENMEI_USER);
             Assert.assertEquals(user.getStatus(), UserStatus.CONFIRMED);
             Assert.assertFalse(user.isTermsAccepted());
             Assert.assertFalse(user.isActivated());
         } finally {
             setTermsHaveToBeAccepted(mustAcceptTerms);
         }
     }

     /**
      * Test that AuthenticationManagement.validateUserLogin considers the TERMS_NOT_ACCEPTED status.
      *
      * @throws Exception
      *             in case the test failed
      */
     @Test
     public void testValidateLogin() throws Exception {
         boolean mustAcceptTerms = setTermsHaveToBeAccepted(false);
         Authentication currentAuth = null;
         ChannelType currentChannelType = ClientAndChannelContextHolder.getChannel();
         try {
             // test different kinds of users
             User activeUser = TestUtils.createRandomUser(false);
             User confirmedUser = TestUtils.createRandomUser(true, null, UserRole.ROLE_KENMEI_USER);
             User disabledUser = TestUtils.createRandomUser(false);
             setTermsHaveToBeAccepted(true);
             User termsNotAcceptedUser = TestUtils.createRandomUser(false);
             currentAuth = AuthenticationHelper.setAsAuthenticatedUser(adminUser);
             userManagement.changeUserStatusByManager(disabledUser.getId(),
                     UserStatus.TEMPORARILY_DISABLED);
             User invitedUser = userManagement.inviteUserToClient(TestUtils.createKenmeiUserVO(
                     TestUtils.createRandomUserAlias(), UserRole.ROLE_KENMEI_USER));

             AuthenticationHelper.setAuthentication(null);
             ClientAndChannelContextHolder.setChannel(ChannelType.WEB);
             AuthenticationManagement authManagement = ServiceLocator
                     .findService(AuthenticationManagement.class);
             authManagement.validateUserLogin(activeUser.getId());
             try {
                 authManagement.validateUserLogin(confirmedUser.getId());
                 Assert.fail("User should not be allowed to login");
             } catch (AccountNotActivatedException e) {
                 // expected
             }
             try {
                 authManagement.validateUserLogin(invitedUser.getId());
                 Assert.fail("User should not be allowed to login");
             } catch (AccountNotActivatedException e) {
                 // expected
             }
             try {
                 authManagement.validateUserLogin(disabledUser.getId());
                 Assert.fail("User should not be allowed to login");
             } catch (AccountTemporarilyDisabledException e) {
                 // expected
             }
             try {
                 authManagement.validateUserLogin(termsNotAcceptedUser.getId());
                 Assert.fail("User should not be allowed to login");
             } catch (TermsNotAcceptedException e) {
                 // expected
             }
             // test other channel type
             ClientAndChannelContextHolder.setChannel(ChannelType.API);
             try {
                 authManagement.validateUserLogin(termsNotAcceptedUser.getId());
                 Assert.fail("User should not be allowed to login");
             } catch (TermsNotAcceptedException e) {
                 // expected
             }
             // when terms do not have to be accepted anymore the user status should be promoted to
             // active during login
             setTermsHaveToBeAccepted(false);
             authManagement.validateUserLogin(termsNotAcceptedUser.getId());
             User user = userManagement.getUserById(termsNotAcceptedUser.getId(),
                     new IdentityConverter<User>());
             Assert.assertEquals(user.getStatus(), UserStatus.ACTIVE);
             Assert.assertTrue(user.isTermsAccepted());
             // after resetting the terms of use active users should be blocked, but only on channel
             // web
             AuthenticationHelper.setAsAuthenticatedUser(adminUser);
             userManagement.resetTermsOfUse();
             AuthenticationHelper.setAuthentication(null);
             setTermsHaveToBeAccepted(true);
             authManagement.validateUserLogin(activeUser.getId());
             ClientAndChannelContextHolder.setChannel(ChannelType.WEB);
             try {
                 authManagement.validateUserLogin(activeUser.getId());
                 Assert.fail("User should not be allowed to login");
             } catch (TermsNotAcceptedException e) {
                 // expected
             }
             // terms do not have to be accepted, login should be possible even on web channel
             setTermsHaveToBeAccepted(false);
             authManagement.validateUserLogin(activeUser.getId());
             // terms flag should not be reset
             user = userManagement.getUserById(termsNotAcceptedUser.getId(),
                     new IdentityConverter<User>());
             Assert.assertEquals(user.getStatus(), UserStatus.ACTIVE);
             Assert.assertFalse(user.isTermsAccepted());
         } finally {
             setTermsHaveToBeAccepted(mustAcceptTerms);
             ClientAndChannelContextHolder.setChannel(currentChannelType);
             if (currentAuth != null) {
                 AuthenticationHelper.setAuthentication(currentAuth);
             }
         }
     }

}
