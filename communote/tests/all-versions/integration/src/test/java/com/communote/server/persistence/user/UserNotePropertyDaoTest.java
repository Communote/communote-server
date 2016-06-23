package com.communote.server.persistence.user;

import java.util.Collection;
import java.util.UUID;

import org.apache.commons.lang.math.RandomUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.common.NotFoundException;
import com.communote.server.api.core.property.PropertyManagement;
import com.communote.server.api.core.property.PropertyType;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.model.blog.Blog;
import com.communote.server.model.user.User;
import com.communote.server.model.user.UserNoteProperty;
import com.communote.server.persistence.blog.NoteDao;
import com.communote.server.test.CommunoteIntegrationTest;
import com.communote.server.test.util.AuthenticationTestUtils;
import com.communote.server.test.util.TestUtils;

/**
 * Tests for {@link UserNotePropertyDao}
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class UserNotePropertyDaoTest extends CommunoteIntegrationTest {

    /**
     * Test the find property methods
     *
     * @throws Exception
     *             in case the test failed
     */
    @Test
    public void testFindProperty() throws Exception {
        String keyGroup = UUID.randomUUID().toString();
        String propertyKey = UUID.randomUUID().toString();
        String propertyValue = UUID.randomUUID().toString();
        PropertyManagement propertyManagement = ServiceLocator.instance().getService(
                PropertyManagement.class);
        propertyManagement.addObjectPropertyFilter(PropertyType.UserNoteProperty, keyGroup,
                propertyKey);
        User dummyUser = TestUtils.createRandomUser(false);
        UserNotePropertyDao userNotePropertyDao = ServiceLocator
                .findService(UserNotePropertyDao.class);
        Blog blog = TestUtils.createRandomBlog(true, true, dummyUser);
        Long noteId = TestUtils.createAndStoreCommonNote(blog, dummyUser.getId(), "Blubbla");
        Collection<UserNoteProperty> properties = userNotePropertyDao.findProperties(noteId,
                keyGroup, propertyKey);
        Assert.assertEquals(properties.size(), 0);
        int limit = 10 + RandomUtils.nextInt(5);
        for (int i = 1; i < limit; i++) {
            User user = TestUtils.createRandomUser(false);
            AuthenticationTestUtils.setSecurityContext(user);
            propertyManagement.setObjectProperty(PropertyType.UserNoteProperty, noteId, keyGroup,
                    propertyKey, propertyValue);
            properties = userNotePropertyDao.findProperties(noteId, keyGroup, propertyKey);
            Assert.assertEquals(properties.size(), i);
            Assert.assertNotNull(userNotePropertyDao.findProperty(noteId, keyGroup, propertyKey));
        }
    }

    /**
     * Tests for {@link UserNotePropertyDao#removePropertiesForNote(Long)}
     *
     * @throws AuthorizationException
     *             in case the test failed
     */
    @Test
    public void testRemovePropertiesForNote() throws AuthorizationException {
        User user = TestUtils.createRandomUser(false);
        Blog blog = TestUtils.createRandomBlog(true, true, user);
        Long noteId = TestUtils.createAndStoreCommonNote(blog, user.getId(), "TestNote");
        PropertyManagement propertyManagement = ServiceLocator.instance().getService(
                PropertyManagement.class);
        int counter = 10 + RandomUtils.nextInt(100);
        long[] propertyIds = new long[counter];
        for (int i = 0; i < counter; i++) {
            AuthenticationTestUtils.setSecurityContext(TestUtils.createRandomUser(false));
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
        }
        UserNotePropertyDao userNotePropertyDao = ServiceLocator
                .findService(UserNotePropertyDao.class);
        int removedProperties = userNotePropertyDao.removePropertiesForNote(noteId);
        Assert.assertEquals(removedProperties, counter);
        Assert.assertNotNull(ServiceLocator.findService(UserDao.class).load(user.getId()));
        Assert.assertNotNull(ServiceLocator.findService(NoteDao.class).load(noteId));
        for (long propertyId : propertyIds) {
            Assert.assertNull(userNotePropertyDao.load(propertyId));
        }
    }

    /**
     * Tests for {@link UserNotePropertyDao#removePropertiesForUser(Long)}
     *
     * @throws Exception
     *             in case the test failed
     */
    @Test
    public void testRemovePropertiesForUser() throws Exception {
        User user = TestUtils.createRandomUser(false);
        Blog blog = TestUtils.createRandomBlog(true, true, user);
        PropertyManagement propertyManagement = ServiceLocator.instance().getService(
                PropertyManagement.class);
        int counter = 10 + RandomUtils.nextInt(100);
        long[] propertyIds = new long[counter];
        for (int i = 0; i < counter; i++) {
            Long noteId = TestUtils.createAndStoreCommonNote(blog, user.getId(), "TestNote");
            AuthenticationTestUtils.setSecurityContext(user);
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
        }
        UserNotePropertyDao userNotePropertyDao = ServiceLocator
                .findService(UserNotePropertyDao.class);
        int removedProperties = userNotePropertyDao.removePropertiesForUser(user.getId());
        Assert.assertEquals(removedProperties, counter);
        Assert.assertNotNull(ServiceLocator.findService(UserDao.class).load(user.getId()));
        for (long propertyId : propertyIds) {
            Assert.assertNull(userNotePropertyDao.load(propertyId));
        }
    }
}
