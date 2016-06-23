package com.communote.server.core.user;

import java.util.Locale;
import java.util.UUID;

import org.testng.annotations.Test;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.user.UserVO;
import com.communote.server.core.user.UserManagement;
import com.communote.server.core.user.UserProfileManagement;
import com.communote.server.model.user.User;
import com.communote.server.model.user.UserRole;
import com.communote.server.persistence.user.UserProfileVO;
import com.communote.server.test.CommunoteIntegrationTest;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class UserProfileManagementTest extends CommunoteIntegrationTest {

    /**
     * Test that the profile of a user created with UserManagement#createUser can be updated
     *
     * @throws Exception
     *             in case the test failed
     */
    @Test
    public void testUpdateUserProfileOfCreatedUser() throws Exception {
        UserVO user = new UserVO();
        user.setAlias(UUID.randomUUID().toString());
        user.setFirstName(UUID.randomUUID().toString());
        user.setLastName(UUID.randomUUID().toString());
        user.setEmail(UUID.randomUUID().toString() + "@" + UUID.randomUUID().toString());
        user.setLanguage(Locale.ENGLISH);
        user.setRoles(new UserRole[] { UserRole.ROLE_KENMEI_USER });
        user.setPassword(UUID.randomUUID().toString());
        User dbUser = ServiceLocator.instance().getService(UserManagement.class)
                .createUser(user, false, false);
        UserProfileVO userProfile = new UserProfileVO();
        ServiceLocator.instance().getService(UserProfileManagement.class)
        .updateUserProfile(dbUser.getId(), userProfile);
    }
}
