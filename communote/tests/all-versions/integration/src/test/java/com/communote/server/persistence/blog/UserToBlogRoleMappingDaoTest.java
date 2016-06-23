package com.communote.server.persistence.blog;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.communote.server.api.ServiceLocator;
import com.communote.server.model.blog.Blog;
import com.communote.server.model.blog.UserToBlogRoleMapping;
import com.communote.server.model.user.User;
import com.communote.server.test.CommunoteIntegrationTest;
import com.communote.server.test.util.TestUtils;

/**
 * Tests for {@link UserToBlogRoleMapping}.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class UserToBlogRoleMappingDaoTest extends CommunoteIntegrationTest {

    /**
     * Clean.
     */
    @BeforeMethod
    public void clean() {
        ServiceLocator.findService(UserToBlogRoleMappingDao.class).remove(
                ServiceLocator.findService(UserToBlogRoleMappingDao.class).loadAll());
    }

    /**
     * Tests, that it is not possible to add wrong mappings.
     *
     * @throws Exception
     *             Exception.
     */
    // TODO Run, when KENMEI-2121 is fixed
    @Test(enabled = false)
    public void testAddMapping() throws Exception {
        User user1 = TestUtils.createRandomUser(false);
        User user2 = TestUtils.createRandomUser(false);
        Blog blog1 = TestUtils.createRandomBlog(false, false, user1);
        UserToBlogRoleMappingDao dao = ServiceLocator.findService(UserToBlogRoleMappingDao.class);
        UserToBlogRoleMapping mapping = UserToBlogRoleMapping.Factory.newInstance(blog1.getId(),
                user2.getId(), 1, false);

        dao.create(mapping);
        try {
            dao.create(mapping);
            Assert.fail("It shouldn't be possible to add the same mapping twice.");
        } catch (Exception e) {
            // Okay.
        }

        mapping.setExternalSystemId("External");
        dao.create(mapping);
        try {
            dao.create(mapping);
            Assert.fail("It shouldn't be possible to add the same mapping twice.");
        } catch (Exception e) {
            // Okay.
        }
    }
}
