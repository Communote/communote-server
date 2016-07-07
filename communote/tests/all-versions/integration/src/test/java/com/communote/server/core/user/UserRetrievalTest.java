package com.communote.server.core.user;

import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.communote.server.api.ServiceLocator;
import com.communote.server.core.filter.listitems.RankUserListItem;
import com.communote.server.core.query.QueryManagement;
import com.communote.server.core.security.AuthenticationHelper;
import com.communote.server.core.vo.query.QueryDefinitionRepository;
import com.communote.server.core.vo.query.user.RankUserTaggingCoreQuery;
import com.communote.server.core.vo.query.user.UserTaggingCoreQueryParameters;
import com.communote.server.model.user.User;
import com.communote.server.model.user.UserRole;
import com.communote.server.model.user.UserStatus;
import com.communote.server.service.UserService;
import com.communote.server.test.CommunoteIntegrationTest;
import com.communote.server.test.util.AuthenticationTestUtils;
import com.communote.server.test.util.TestUtils;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class UserRetrievalTest extends CommunoteIntegrationTest {

    private static final Logger LOG = Logger.getLogger(UserRetrievalTest.class);
    @Autowired
    private UserService userService;

    /**
     * Tests the retrieval of all users that are not deleted.
     *
     * @throws Exception
     *             Exception.
     */
    @Test(groups = { "UserRetrieval" })
    public void testFindNotDeletedUsers() throws Exception {
        User userToDelete = TestUtils.createRandomUser(false);
        UserManagement userManagement = ServiceLocator.instance().getService(UserManagement.class);
        AuthenticationTestUtils.setManagerContext();
        userService.anonymizeUser(userToDelete.getId(), null, false);
        List<User> allUsers = userManagement.findUsersByRole(UserRole.ROLE_KENMEI_USER, null);
        allUsers.addAll(userManagement.findUsersByRole(UserRole.ROLE_SYSTEM_USER, null));
        int deletedUsers = 0;
        for (User user : allUsers) {
            UserStatus s = user.getStatus();
            if (UserStatus.DELETED.equals(s) || UserStatus.PERMANENTLY_DISABLED.equals(s)) {
                deletedUsers++;
            }
        }
        Assert.assertTrue(deletedUsers > 0,
                "The test is useless because there are no deleted users.");
        Collection<User> users = userManagement.findNotDeletedUsers(false);
        Assert.assertTrue(users.size() == allUsers.size() - deletedUsers, "No users found.");
        for (User user : users) {
            UserStatus status = user.getStatus();
            boolean isActive = !(UserStatus.DELETED.equals(status) || UserStatus.PERMANENTLY_DISABLED
                    .equals(status));
            Assert.assertTrue(isActive,
                    "Returned user " + user.getAlias() + " with status " + user.getStatus());
        }
    }

    /**
     * Test for getting ranked users
     */
    @Test(groups = { "UserRetrieval" })
    public void testRankUserTaggingCoreQueryDefinition() {
        TestUtils.createRandomUser(false);
        RankUserTaggingCoreQuery query = QueryDefinitionRepository.instance().getQueryDefinition(
                RankUserTaggingCoreQuery.class);

        UserTaggingCoreQueryParameters instance = query.createInstance();

        QueryManagement queryManagement = ServiceLocator.findService(QueryManagement.class);
        AuthenticationHelper.setInternalSystemToSecurityContext();
        List<RankUserListItem> result = queryManagement.query(query, instance);

        Assert.assertNotNull(result, "The resulting list cannot be null.");
        Assert.assertFalse(result.isEmpty(), "The resulting list cannot be empty!");

        for (RankUserListItem item : result) {
            Assert.assertNotNull(item, "The item should not be null.");
            Assert.assertNotNull(item.getEmail(), "The email should not be null.");
            Assert.assertNotNull(item.getId(), "The user id should not be null.");
            Assert.assertNotNull(item.getRank(), "The rank should not be null.");
            Assert.assertTrue(item.getRank().intValue() >= 0, "The rank should be >= 0.");
            if (LOG.isDebugEnabled()) {
                LOG.debug("The email is: " + item.getEmail() + " the rank is: " + item.getRank());
            }
        }
    }

}
