package com.communote.server.core.user;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.hibernate.Hibernate;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;

import com.communote.common.converter.IdentityConverter;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.tag.TagStoreType;
import com.communote.server.api.core.tag.TagTO;
import com.communote.server.api.core.user.UserVO;
import com.communote.server.model.user.User;
import com.communote.server.model.user.UserRole;
import com.communote.server.test.CommunoteIntegrationTest;

/**
 * Tests for {@link UserManagement} with focus on tagging.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class UserManagementTestForTags extends CommunoteIntegrationTest {

    private UserManagement userManagement;
    private final IdentityConverter<User> converter = new IdentityConverter<User>() {
        @Override
        public User convert(User source) {
            Hibernate.initialize(source.getTags());
            return super.convert(source);
        }
    };;

    /**
     * Setup.
     */
    @BeforeClass(dependsOnGroups = "integration-test-setup")
    public void setup() {
        userManagement = ServiceLocator.instance().getService(UserManagement.class);
    }

    /**
     * Test the update of the tags for users.
     *
     * @throws Exception
     *             Exception.
     */
    public void testUpdateKenmeiUserForTagTO() throws Exception {
        UserVO userVO = new UserVO(Locale.ENGLISH, random() + "@" + random(),
                new UserRole[] { UserRole.ROLE_KENMEI_USER });
        userVO.setPassword(random());
        userVO.setAlias(random());
        Long userId = userManagement.createUser(userVO, false, false).getId();
        final TagTO tag = new TagTO(random(), TagStoreType.Types.NOTE);
        Set<TagTO> tags = new HashSet<TagTO>();
        tags.add(tag);
        userManagement.updateUserTags(userId, tags);
        User user = userManagement.getUserById(userId, converter);
        Assert.assertEquals(user.getTags().size(), 1);
        Assert.assertEquals(user.getTags().iterator().next().getName(), tag.getDefaultName());
    }
}
