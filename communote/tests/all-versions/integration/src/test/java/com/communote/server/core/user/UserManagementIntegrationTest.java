package com.communote.server.core.user;

import java.util.UUID;

import org.apache.commons.lang.math.RandomUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.common.NotFoundException;
import com.communote.server.api.core.config.type.ClientProperty;
import com.communote.server.api.core.property.PropertyManagement;
import com.communote.server.api.core.property.PropertyType;
import com.communote.server.core.security.SecurityHelper;
import com.communote.server.core.user.UserManagement;
import com.communote.server.model.blog.Blog;
import com.communote.server.model.user.User;
import com.communote.server.model.user.UserNoteProperty;
import com.communote.server.persistence.blog.NoteDao;
import com.communote.server.persistence.user.UserDao;
import com.communote.server.persistence.user.UserNotePropertyDao;
import com.communote.server.test.CommunoteIntegrationTest;
import com.communote.server.test.util.AuthenticationTestUtils;
import com.communote.server.test.util.TestUtils;

/**
 * Test for {@link UserManagement}
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
// TODO merge with UserManagementTest2. Is the test of this class doing the same as
// UserManagementTest2.testAnonymizeUserWithNoteProperties()?
public class UserManagementIntegrationTest extends CommunoteIntegrationTest {
    /**
     * Test that it is possible to anonymize a user who created a note with properties
     *
     * @throws Exception
     *             in case the test failed
     */
    @Test
    public void testAnonymizeUserWithPropertiesOnNote() throws Exception {
        CommunoteRuntime
        .getInstance()
        .getConfigurationManager()
        .updateClientConfigurationProperty(ClientProperty.DELETE_USER_BY_ANONYMIZE_ENABLED,
                Boolean.TRUE.toString());
        User user = TestUtils.createRandomUser(false);
        AuthenticationTestUtils.setManagerContext();
        Blog blog = TestUtils.createRandomBlog(true, true,
                SecurityHelper.assertCurrentKenmeiUser(), user);
        Long noteId = TestUtils.createAndStoreCommonNote(blog, user.getId(), "TestNote");
        PropertyManagement propertyManagement = ServiceLocator.instance().getService(
                PropertyManagement.class);
        int counter = 10 + RandomUtils.nextInt(100);
        long[] propertyIds = new long[counter];
        AuthenticationTestUtils.setSecurityContext(user);
        for (int i = 0; i < counter; i++) {
            String keyGroup = UUID.randomUUID().toString();
            String propertyKey = UUID.randomUUID().toString();
            propertyManagement.addObjectPropertyFilter(PropertyType.UserNoteProperty, keyGroup,
                    propertyKey);
            try {
                propertyIds[i] = ((UserNoteProperty) propertyManagement.setObjectProperty(
                        PropertyType.UserNoteProperty, noteId, keyGroup, propertyKey, UUID
                        .randomUUID().toString())).getId();
            } catch (NotFoundException e) {
                Assert.fail("user note property was not found.");
            }
            Assert.assertNotNull(ServiceLocator.findService(UserNotePropertyDao.class).load(
                    propertyIds[i]));
        }
        ServiceLocator.instance().getService(UserManagement.class)
        .anonymizeUser(user.getId(), new Long[] { blog.getId() }, true);
        Assert.assertNotNull(ServiceLocator.findService(UserDao.class).load(user.getId()));
        Assert.assertNull(ServiceLocator.findService(NoteDao.class).load(noteId));
        for (long propertyId : propertyIds) {
            Assert.assertNull(ServiceLocator.findService(UserNotePropertyDao.class)
                    .load(propertyId));
        }
    }
}
