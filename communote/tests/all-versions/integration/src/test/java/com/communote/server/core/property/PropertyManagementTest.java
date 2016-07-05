package com.communote.server.core.property;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang.math.RandomUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.communote.common.converter.Converter;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.common.NotFoundException;
import com.communote.server.api.core.event.EventDispatcher;
import com.communote.server.api.core.note.NoteStoringTO;
import com.communote.server.api.core.note.processor.NoteStoringPreProcessorException;
import com.communote.server.api.core.property.PropertyEvent;
import com.communote.server.api.core.property.PropertyEvent.PropertyEventType;
import com.communote.server.api.core.property.PropertyManagement;
import com.communote.server.api.core.property.PropertyType;
import com.communote.server.api.core.property.StringPropertyTO;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.api.core.user.UserData;
import com.communote.server.core.converter.user.UserToUserDataConverter;
import com.communote.server.core.vo.blog.NoteModificationResult;
import com.communote.server.core.vo.blog.NoteModificationStatus;
import com.communote.server.model.attachment.Attachment;
import com.communote.server.model.blog.Blog;
import com.communote.server.model.note.Note;
import com.communote.server.model.property.BinaryProperty;
import com.communote.server.model.property.Property;
import com.communote.server.model.property.StringProperty;
import com.communote.server.model.user.User;
import com.communote.server.model.user.UserNoteProperty;
import com.communote.server.model.user.UserProperty;
import com.communote.server.persistence.user.UserNotePropertyDao;
import com.communote.server.service.NoteService;
import com.communote.server.test.CommunoteIntegrationTest;
import com.communote.server.test.util.AuthenticationTestUtils;
import com.communote.server.test.util.TestUtils;

/**
 * Test for the property management
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class PropertyManagementTest extends CommunoteIntegrationTest {

    private static final String ATTACHMENT_PROP_KEY_GROUP = "com.communote.core";
    private static final String ATTACHMENT_PROP_KEY = "attachment.test";
    private PropertyManagement propertyManagement;
    private User user1;
    private User user2;
    private UserProperty sampleUserProperty;

    private UserProperty sampleUserPropertyDifferentValue;
    private UserProperty sampleGlobalUserProperty;
    private BinaryProperty sampleBinaryProperty;

    private Blog blog1;

    private Long noteId;

    private TestPropertyEventListener eventListener;

    private EventDispatcher eventDispatcher;

    @Autowired
    private NoteService noteService;

    /**
     * Compares and assert common property attributes
     *
     * @param toCheck
     *            the prop to check
     * @param sample
     *            the sample
     */
    private void assertProperty(Property toCheck, Property sample) {
        Assert.assertNotNull(toCheck, "property cannot be null");
        Assert.assertNotNull(toCheck.getId(), "property.id cannot be null");
        Assert.assertEquals(toCheck.getKeyGroup(), sample.getKeyGroup());
        Assert.assertEquals(toCheck.getPropertyKey(), sample.getPropertyKey());

    }

    /**
     * Compares and assert a binary property
     *
     * @param toCheck
     *            the prop to check
     * @param sample
     *            the sample
     */
    private void checkBinaryProperty(BinaryProperty toCheck, BinaryProperty sample) {
        assertProperty(toCheck, sample);
        Assert.assertEquals(toCheck.getPropertyValue(), sample.getPropertyValue());
    }

    /**
     * Gets an event from the listener with the given
     *
     * @param property
     *            the property that will be used for asserting the found one
     * @param id
     *            the id that must match
     * @param oldValue
     *            if null the event must have a "PropertyEventType.CREATE", otherwise it must be an
     *            update and the old value of the event will be checked as well.
     */
    private void checkPropertyEvent(UserProperty property, Long id, String oldValue) {
        PropertyEvent event = eventListener.getProperty(PropertyType.UserProperty,
                property.getKeyGroup(), property.getPropertyKey());
        Assert.assertNotNull(event, "There should be an event for this property!");
        if (oldValue == null) {
            Assert.assertEquals(event.getPropertyEventType(), PropertyEventType.CREATE);
        } else {
            Assert.assertEquals(event.getPropertyEventType(), PropertyEventType.UPDATE);
            Assert.assertEquals(event.getOldValue(), oldValue);
        }
        Assert.assertEquals(event.getNewValue(), property.getPropertyValue());
        Assert.assertEquals(event.getObjectId(), id);
    }

    /**
     * Compare the properties and assert the equality
     *
     * @param toCheck
     *            the property to check
     * @param sample
     *            the sample that it should be equal to
     */
    private void checkStringProperty(StringProperty toCheck, StringProperty sample) {
        assertProperty(toCheck, sample);
        Assert.assertEquals(toCheck.getPropertyValue(), sample.getPropertyValue());
    }

    /**
     * remove the event listener
     */
    @AfterClass
    public void removeEventListener() {
        eventDispatcher.unregister(eventListener);
    }

    /**
     * Setup this test
     */
    @BeforeClass(dependsOnGroups = "integration-test-setup")
    public void setup() throws Exception {

        propertyManagement = ServiceLocator.instance().getService(PropertyManagement.class);

        eventDispatcher = ServiceLocator.findService(EventDispatcher.class);
        eventListener = new TestPropertyEventListener();
        eventDispatcher.register(eventListener);

        user1 = TestUtils.createRandomUser(false);
        user2 = TestUtils.createRandomUser(false);

        blog1 = TestUtils.createRandomBlog(true, true, user1);

        noteId = TestUtils.createAndStoreCommonNote(blog1, user1.getId(), UUID.randomUUID()
                .toString());

        sampleUserProperty = UserProperty.Factory.newInstance();
        sampleUserProperty.setKeyGroup("myKeyGroup");
        sampleUserProperty.setPropertyKey("my.secret.key");
        sampleUserProperty.setPropertyValue("office1");

        sampleUserPropertyDifferentValue = UserProperty.Factory.newInstance();
        sampleUserPropertyDifferentValue.setKeyGroup("myKeyGroup");
        sampleUserPropertyDifferentValue.setPropertyKey("my.secret.key");
        sampleUserPropertyDifferentValue.setPropertyValue("office2");

        sampleGlobalUserProperty = UserProperty.Factory.newInstance();
        sampleGlobalUserProperty.setKeyGroup("global");
        sampleGlobalUserProperty.setPropertyKey(random());
        sampleGlobalUserProperty.setPropertyValue(random());
        sampleBinaryProperty = BinaryProperty.Factory.newInstance();
        sampleBinaryProperty.setKeyGroup(random());
        sampleBinaryProperty.setPropertyKey(random());
        sampleBinaryProperty.setPropertyValue(new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 });

        propertyManagement.addObjectPropertyFilter(PropertyType.UserProperty,
                sampleUserProperty.getKeyGroup(), sampleUserProperty.getPropertyKey());
        propertyManagement.addObjectPropertyFilter(PropertyType.UserProperty,
                sampleUserPropertyDifferentValue.getKeyGroup(),
                sampleUserPropertyDifferentValue.getPropertyKey());
        propertyManagement.addObjectPropertyFilter(PropertyType.UserProperty,
                sampleGlobalUserProperty.getKeyGroup(), sampleGlobalUserProperty.getPropertyKey());

        propertyManagement.addObjectPropertyFilter(PropertyType.AttachmentProperty,
                ATTACHMENT_PROP_KEY_GROUP, ATTACHMENT_PROP_KEY);
    }

    /**
     *
     * @throws NotFoundException
     *             in case of an error
     * @throws AuthorizationException
     *             can not authorized to get property
     * @throws NoteStoringPreProcessorException
     */
    @Test
    public void testAttachmentProperty() throws NotFoundException, AuthorizationException,
            NoteStoringPreProcessorException {

        final String value = UUID.randomUUID().toString();

        final String fileName = "test-attachement-property-attachment.txt";
        final String content = "some randome content. " + UUID.randomUUID().toString();

        Blog blog = TestUtils.createRandomBlog(true, true, user1);

        NoteStoringTO storingTO = TestUtils.generateCommonNoteStoringTO(user1, blog);

        TestUtils.addAttachment(storingTO, fileName, content);

        NoteModificationResult result = noteService.createNote(storingTO, null);

        Assert.assertEquals(result.getStatus(), NoteModificationStatus.SUCCESS);
        Assert.assertNotNull(result.getNoteId());

        Attachment attachment = this.noteService.getNote(result.getNoteId(),
                new Converter<Note, Attachment>() {

                    @Override
                    public Attachment convert(Note source) {
                        return source.getAttachments().iterator().next();
                    }

                });

        Assert.assertNotNull(attachment);
        Set<StringPropertyTO> props = new HashSet<>();

        props.add(new StringPropertyTO(value, ATTACHMENT_PROP_KEY_GROUP, ATTACHMENT_PROP_KEY,
                new Date()));
        propertyManagement.setObjectProperties(PropertyType.AttachmentProperty, attachment.getId(),
                props);

        StringProperty prop = propertyManagement.getObjectProperty(PropertyType.AttachmentProperty,
                attachment.getId(), ATTACHMENT_PROP_KEY_GROUP, ATTACHMENT_PROP_KEY);

        Assert.assertNotNull(prop);
        Assert.assertNotNull(prop.getPropertyValue());
        Assert.assertEquals(prop.getKeyGroup(), ATTACHMENT_PROP_KEY_GROUP);
        Assert.assertEquals(prop.getPropertyKey(), ATTACHMENT_PROP_KEY);
        Assert.assertEquals(prop.getPropertyValue(), value);

    }

    /**
     * Test the binary properties
     *
     * @throws NotFoundException
     *             in case there is no object of the given property type and id
     * @throws AuthorizationException
     *             Thrown, when the user is not allowed to access.
     */
    @Test
    public void testBinaryProperties() throws AuthorizationException, NotFoundException {
        BinaryProperty property = propertyManagement.setBinaryProperty(
                sampleBinaryProperty.getKeyGroup(), sampleBinaryProperty.getPropertyKey(),
                sampleBinaryProperty.getPropertyValue());

        assertProperty(property, sampleBinaryProperty);

        PropertyEvent event = eventListener.getProperty(PropertyType.BinaryProperty,
                sampleBinaryProperty.getKeyGroup(), sampleBinaryProperty.getPropertyKey());

        Assert.assertNotNull(event, "There should be an event for this property!");
        Assert.assertEquals(event.getPropertyEventType(), PropertyEventType.CREATE);

        BinaryProperty property2 = propertyManagement.getBinaryProperty(
                sampleBinaryProperty.getKeyGroup(), sampleBinaryProperty.getPropertyKey());
        checkBinaryProperty(property2, sampleBinaryProperty);

        // the id of the property should be equal, we dont want another property to be created!
        Assert.assertEquals(property.getId(), property2.getId());
    }

    /**
     * Tests, that is not possible to add a property for which no filter was set.
     *
     * @throws Exception
     *             in case the test failed
     */
    @Test
    public void testFailOnInvalidUserNotePropertyFilter() throws Exception {
        String keyGroup = UUID.randomUUID().toString();
        String propertyKey = UUID.randomUUID().toString();
        String propertyValue = UUID.randomUUID().toString();
        AuthenticationTestUtils.setSecurityContext(user1);
        StringProperty property = propertyManagement.setObjectProperty(
                PropertyType.UserNoteProperty, noteId, keyGroup, propertyKey, propertyValue);
        Assert.assertNull(property);

        PropertyEvent event = eventListener.getProperty(PropertyType.BinaryProperty, keyGroup,
                propertyKey);

        Assert.assertNull(event, "There should be NOT an event for this property!");
    }

    /**
     * Test for {@link PropertyManagement#getUserToNotePropertyValue(Long, String, String)}
     *
     * @throws Exception
     *             an error occurred
     */
    @Test(dependsOnMethods = "testSetUserNoteProperty")
    public void testGetUserNoteProperty() throws Exception {
        String keyGroup = UUID.randomUUID().toString();
        String propertyKey = UUID.randomUUID().toString();
        String propertyValue = UUID.randomUUID().toString();
        AuthenticationTestUtils.setSecurityContext(user1);
        propertyManagement.addObjectPropertyFilter(PropertyType.UserNoteProperty, keyGroup,
                propertyKey);
        propertyManagement.setObjectProperty(PropertyType.UserNoteProperty, noteId, keyGroup,
                propertyKey, propertyValue);
        UserNoteProperty userToNoteProperty = (UserNoteProperty) propertyManagement
                .getObjectProperty(PropertyType.UserNoteProperty, noteId, keyGroup, propertyKey);
        Assert.assertEquals(userToNoteProperty.getPropertyValue(), propertyValue);

        PropertyEvent event = eventListener.getProperty(PropertyType.UserNoteProperty, keyGroup,
                propertyKey);

        Assert.assertNotNull(event, "There should be an event for this property!");
        Assert.assertEquals(event.getPropertyEventType(), PropertyEventType.CREATE);
        Assert.assertEquals(event.getNewValue(), propertyValue);
    }

    /**
     * Tests, that it is not possible to get a property, where the current user doesn't have blog
     * access.
     *
     * @throws Exception
     *             in case the test failed
     */
    @Test(dependsOnMethods = "testSetUserNoteProperty")
    public void testGetUserNotePropertyAndFailOnBlogAccess() throws Exception {
        Blog blog2 = TestUtils.createRandomBlog(false, false, user2);
        Long noteId = TestUtils.createAndStoreCommonNote(blog2, user2.getId(), UUID.randomUUID()
                .toString());
        String keyGroup = UUID.randomUUID().toString();
        String propertyKey = UUID.randomUUID().toString();
        String propertyValue = UUID.randomUUID().toString();
        AuthenticationTestUtils.setSecurityContext(user2);
        propertyManagement.addObjectPropertyFilter(PropertyType.UserNoteProperty, keyGroup,
                propertyKey);
        propertyManagement.setObjectProperty(PropertyType.UserNoteProperty, noteId, keyGroup,
                propertyKey, propertyValue);

        PropertyEvent event = eventListener.getProperty(PropertyType.UserNoteProperty, keyGroup,
                propertyKey);

        Assert.assertNotNull(event, "There should be an event for this property!");
        Assert.assertEquals(event.getPropertyEventType(), PropertyEventType.CREATE);
        Assert.assertEquals(event.getNewValue(), propertyValue);
        Assert.assertEquals(event.getUserId(), user2.getId());

        AuthenticationTestUtils.setSecurityContext(user1);
        try {
            propertyManagement.getObjectProperty(PropertyType.UserNoteProperty, noteId, keyGroup,
                    propertyKey);
            Assert.fail("A user shouldn't be able to get a property from a blog, he isn't allowed to access.");
        } catch (AuthorizationException e) {
            // Expected behavior.
        }

    }

    /**
     * Test that (cached) getUsersForProperty returns the correct users.
     *
     * @throws Exception
     *             in case the test failed
     */
    @Test
    public void testGetUsersForProperty() throws Exception {
        String keyGroup = UUID.randomUUID().toString();
        String propertyKey = UUID.randomUUID().toString();
        String propertyValue = UUID.randomUUID().toString();
        PropertyManagement propertyManagement = ServiceLocator.instance().getService(
                PropertyManagement.class);
        propertyManagement.addObjectPropertyFilter(PropertyType.UserNoteProperty, keyGroup,
                propertyKey);
        User dummyUser = TestUtils.createRandomUser(false);
        Blog blog = TestUtils.createRandomBlog(true, true, dummyUser);
        Long noteId = TestUtils.createAndStoreCommonNote(blog, dummyUser.getId(), "Blubbla");
        UserToUserDataConverter<UserData> converter = new UserToUserDataConverter<UserData>(
                UserData.class, false, null);
        Collection<UserData> usersForProperty = propertyManagement.getUsersOfProperty(noteId,
                keyGroup, propertyKey, propertyValue, converter);
        Assert.assertEquals(usersForProperty.size(), 0);
        int limit = 10 + RandomUtils.nextInt(5);
        for (int i = 1; i < limit; i++) {
            User user = TestUtils.createRandomUser(false);
            AuthenticationTestUtils.setSecurityContext(user);
            propertyManagement.setObjectProperty(PropertyType.UserNoteProperty, noteId, keyGroup,
                    propertyKey, propertyValue);
            usersForProperty = propertyManagement.getUsersOfProperty(noteId, keyGroup, propertyKey,
                    propertyValue, converter);
            Assert.assertEquals(usersForProperty.size(), i);
            usersForProperty = propertyManagement.getUsersOfProperty(noteId, keyGroup, propertyKey,
                    UUID.randomUUID().toString(), converter);
            Assert.assertEquals(usersForProperty.size(), 0);
        }
    }

    /**
     *
     * @throws Exception
     *             in case the test failed
     */
    @Test
    public void testGlobalUserProperty() throws Exception {
        StringProperty property;
        // set a global property
        AuthenticationTestUtils.setSecurityContext(user2);
        property = propertyManagement.setGlobalObjectProperty(PropertyType.UserProperty,
                user2.getId(), sampleGlobalUserProperty.getPropertyKey(),
                sampleGlobalUserProperty.getPropertyValue());
        assertProperty(property, sampleGlobalUserProperty);
        property = propertyManagement.getGlobalObjectProperty(PropertyType.UserProperty,
                user2.getId(), sampleGlobalUserProperty.getPropertyKey());
        assertProperty(property, sampleGlobalUserProperty);

        // Test event listener
        PropertyEvent event = eventListener.getProperty(PropertyType.UserProperty, "global",
                sampleGlobalUserProperty.getPropertyKey());
        Assert.assertNotNull(event, "There should be an event for this property!");
        Assert.assertEquals(event.getPropertyEventType(), PropertyEventType.CREATE);
        Assert.assertEquals(event.getNewValue(), sampleGlobalUserProperty.getPropertyValue());

    }

    /**
     * Test for {@link PropertyManagement#setUserToNoteProperty(Long, String, String, String)}
     *
     * @throws Exception
     *             in case the test failed
     */
    @Test
    public void testSetUserNoteProperties() throws Exception {
        String keyGroup = UUID.randomUUID().toString();
        String propertyKey = UUID.randomUUID().toString();
        String propertyValue = UUID.randomUUID().toString();
        AuthenticationTestUtils.setSecurityContext(user1);
        propertyManagement.addObjectPropertyFilter(PropertyType.UserNoteProperty, keyGroup,
                propertyKey);

        Set<StringPropertyTO> properties = new HashSet<StringPropertyTO>();
        properties.add(new StringPropertyTO(propertyValue, keyGroup, propertyKey, new Date()));

        // store properties
        propertyManagement.setObjectProperties(PropertyType.UserNoteProperty, noteId, properties);

        // get property
        UserNoteProperty property = (UserNoteProperty) propertyManagement.getObjectProperty(
                PropertyType.UserNoteProperty, noteId, keyGroup, propertyKey);

        Assert.assertNotNull(property);
        Assert.assertEquals(property.getKeyGroup(), keyGroup);
        Assert.assertEquals(property.getPropertyKey(), propertyKey);
        Assert.assertEquals(property.getPropertyValue(), propertyValue);
        Assert.assertEquals(property.getUser().getId(), user1.getId());
        Assert.assertEquals(property.getNote().getId(), noteId);

        Thread.sleep(2000); // Avoid same time, where dbms doesn't store milliseconds (i.e.
        // MySQL).

        // Update property
        String newPropertyValue = UUID.randomUUID().toString();

        properties = new HashSet<StringPropertyTO>();
        properties.add(new StringPropertyTO(newPropertyValue, keyGroup, propertyKey, new Date()));

        propertyManagement.setObjectProperties(PropertyType.UserNoteProperty, noteId, properties);

        UserNoteProperty updatedProperty = ServiceLocator.findService(UserNotePropertyDao.class)
                .load(property.getId());
        Assert.assertEquals(updatedProperty.getId(), property.getId());
        Assert.assertEquals(updatedProperty.getPropertyValue(), newPropertyValue);
        Assert.assertNotEquals(updatedProperty.getLastModificationDate(),
                property.getLastModificationDate());

        // Update property
        properties = new HashSet<StringPropertyTO>();
        properties.add(new StringPropertyTO(null, keyGroup, propertyKey, new Date()));

        propertyManagement.setObjectProperties(PropertyType.UserNoteProperty, noteId, properties);

        StringProperty stringProperty = propertyManagement.getObjectProperty(
                PropertyType.UserNoteProperty, noteId, keyGroup, propertyKey);

        Assert.assertEquals(stringProperty, null);
        Assert.assertNull(ServiceLocator.findService(UserNotePropertyDao.class).load(
                property.getId()));
    }

    /**
     * Test for {@link PropertyManagement#setUserToNoteProperty(Long, String, String, String)}
     *
     * @throws Exception
     *             in case the test failed
     */
    @Test
    public void testSetUserNoteProperty() throws Exception {
        String keyGroup = UUID.randomUUID().toString();
        String propertyKey = UUID.randomUUID().toString();
        String propertyValue = UUID.randomUUID().toString();
        AuthenticationTestUtils.setSecurityContext(user1);
        propertyManagement.addObjectPropertyFilter(PropertyType.UserNoteProperty, keyGroup,
                propertyKey);

        // Store property
        Long propertyId = propertyManagement.setObjectProperty(PropertyType.UserNoteProperty,
                noteId, keyGroup, propertyKey, propertyValue).getId();
        UserNoteProperty property = ServiceLocator.findService(UserNotePropertyDao.class).load(
                propertyId);
        Assert.assertNotNull(property);
        Assert.assertEquals(property.getKeyGroup(), keyGroup);
        Assert.assertEquals(property.getPropertyKey(), propertyKey);
        Assert.assertEquals(property.getPropertyValue(), propertyValue);
        Assert.assertEquals(property.getUser().getId(), user1.getId());
        Assert.assertEquals(property.getNote().getId(), noteId);

        Thread.sleep(2000); // Avoid same time, where dbms doesn't store milliseconds (i.e.
        // MySQL).

        // Update property
        String newPropertyValue = UUID.randomUUID().toString();
        Long updatedPropertyId = propertyManagement.setObjectProperty(
                PropertyType.UserNoteProperty, noteId, keyGroup, propertyKey, newPropertyValue)
                .getId();
        UserNoteProperty updatedProperty = ServiceLocator.findService(UserNotePropertyDao.class)
                .load(propertyId);
        Assert.assertEquals(updatedPropertyId, propertyId);
        Assert.assertEquals(updatedProperty.getPropertyValue(), newPropertyValue);
        Assert.assertNotEquals(updatedProperty.getLastModificationDate(),
                property.getLastModificationDate());

        propertyManagement.setObjectProperty(PropertyType.UserNoteProperty, noteId, keyGroup,
                propertyKey, null);

        StringProperty stringProperty = propertyManagement.getObjectProperty(
                PropertyType.UserNoteProperty, noteId, keyGroup, propertyKey);

        Assert.assertNull(stringProperty);
        Assert.assertNull(ServiceLocator.findService(UserNotePropertyDao.class).load(propertyId));
    }

    /**
     * Test that it is not possible to set a property if the current user doesn't have blog access.
     *
     * @throws AuthorizationException
     *             in case the test succeeded
     * @throws Exception
     *             in case the test failed
     */
    @Test(dependsOnMethods = "testSetUserNoteProperty", expectedExceptions = { AuthorizationException.class })
    public void testSetUserNotePropertyAndFailOnBlogAccess() throws AuthorizationException,
            Exception {
        Blog blog2 = TestUtils.createRandomBlog(false, false, user2);
        Long noteId = TestUtils.createAndStoreCommonNote(blog2, user2.getId(), UUID.randomUUID()
                .toString());
        String keyGroup = UUID.randomUUID().toString();
        String propertyKey = UUID.randomUUID().toString();
        String propertyValue = UUID.randomUUID().toString();
        AuthenticationTestUtils.setSecurityContext(user1);
        propertyManagement.addObjectPropertyFilter(PropertyType.UserNoteProperty, keyGroup,
                propertyKey);
        propertyManagement.setObjectProperty(PropertyType.UserNoteProperty, noteId, keyGroup,
                propertyKey, propertyValue);
    }

    /**
     *
     * @throws Exception
     *             in case the test failed
     */
    @Test
    public void testUserProperty() throws Exception {
        // add a property
        StringProperty property = propertyManagement.setObjectProperty(PropertyType.UserProperty,
                user1.getId(), sampleUserProperty.getKeyGroup(),
                sampleUserProperty.getPropertyKey(), sampleUserProperty.getPropertyValue());

        checkStringProperty(property, sampleUserProperty);
        // check creation by getting it

        property = propertyManagement.getObjectProperty(PropertyType.UserProperty, user1.getId(),
                sampleUserProperty.getKeyGroup(), sampleUserProperty.getPropertyKey());

        checkStringProperty(property, sampleUserProperty);

        checkPropertyEvent(sampleUserProperty, user1.getId(), null);

        eventListener.getReceivedEvents().clear();

        // add a property to a second user with the same key, group and value
        AuthenticationTestUtils.setSecurityContext(user1);
        try {
            property = propertyManagement.setObjectProperty(PropertyType.UserProperty,
                    user2.getId(), sampleUserProperty.getKeyGroup(),
                    sampleUserProperty.getPropertyKey(), sampleUserProperty.getPropertyValue());
            Assert.fail("The current user should not be able to set the property.");
        } catch (AuthorizationException e) {
            // Okay, as the current user is not allowed to set property.
        }

        // Test event listener
        PropertyEvent event = eventListener.getProperty(PropertyType.UserProperty,
                sampleUserPropertyDifferentValue.getKeyGroup(),
                sampleUserPropertyDifferentValue.getPropertyKey());
        Assert.assertNull(event, "There should be NOT be an event for this property!");

        AuthenticationTestUtils.setSecurityContext(user2);
        property = propertyManagement.setObjectProperty(PropertyType.UserProperty, user2.getId(),
                sampleUserProperty.getKeyGroup(), sampleUserProperty.getPropertyKey(),
                sampleUserProperty.getPropertyValue());
        checkStringProperty(property, sampleUserProperty);
        checkPropertyEvent(sampleUserProperty, user2.getId(), null);
        this.eventListener.getReceivedEvents().clear();

        // now set the same property to a different value
        StringProperty property2 = propertyManagement.setObjectProperty(PropertyType.UserProperty,
                user2.getId(), sampleUserPropertyDifferentValue.getKeyGroup(),
                sampleUserPropertyDifferentValue.getPropertyKey(),
                sampleUserPropertyDifferentValue.getPropertyValue());

        checkStringProperty(property2, sampleUserPropertyDifferentValue);
        checkPropertyEvent(sampleUserPropertyDifferentValue, user2.getId(),
                sampleUserProperty.getPropertyValue());
        this.eventListener.getReceivedEvents().clear();

        // the id of the property should be equal, we dont want another property to be created!
        Assert.assertEquals(property.getId(), property2.getId());

        // and check creation by getting it
        property2 = propertyManagement.getObjectProperty(PropertyType.UserProperty, user2.getId(),
                sampleUserPropertyDifferentValue.getKeyGroup(),
                sampleUserPropertyDifferentValue.getPropertyKey());

        assertProperty(property2, sampleUserPropertyDifferentValue);
        Assert.assertEquals(property.getId(), property2.getId());

        // check remove when value is null
        propertyManagement.setObjectProperty(PropertyType.UserProperty, user2.getId(),
                sampleUserPropertyDifferentValue.getKeyGroup(),
                sampleUserPropertyDifferentValue.getPropertyKey(), null);

        // Test event listener
        event = eventListener.getProperty(PropertyType.UserProperty,
                sampleUserPropertyDifferentValue.getKeyGroup(),
                sampleUserPropertyDifferentValue.getPropertyKey());
        Assert.assertNotNull(event, "There should be an event for this property!");
        Assert.assertEquals(event.getPropertyEventType(), PropertyEventType.DELETE);
        Assert.assertEquals(event.getOldValue(),
                sampleUserPropertyDifferentValue.getPropertyValue());

        StringProperty stringProperty = propertyManagement.getObjectProperty(
                PropertyType.UserNoteProperty, noteId,
                sampleUserPropertyDifferentValue.getKeyGroup(),
                sampleUserPropertyDifferentValue.getPropertyKey());

        Assert.assertEquals(stringProperty, null);

    }
}
