package com.communote.server.core.external;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.security.core.context.SecurityContext;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.blog.BlogAccessException;
import com.communote.server.api.core.blog.BlogNotFoundException;
import com.communote.server.api.core.blog.BlogRightsManagement;
import com.communote.server.api.core.property.PropertyManagement;
import com.communote.server.api.core.property.PropertyType;
import com.communote.server.api.core.property.StringPropertyTO;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.core.external.ExternalObjectAlreadyAssignedException;
import com.communote.server.core.external.ExternalObjectManagement;
import com.communote.server.core.external.ExternalSystemNotConfiguredException;
import com.communote.server.core.external.TooManyExternalObjectsPerTopicException;
import com.communote.server.core.filter.ResultSpecification;
import com.communote.server.core.filter.listitems.blog.ExternalObjectListItem;
import com.communote.server.core.query.QueryManagement;
import com.communote.server.core.security.AuthenticationHelper;
import com.communote.server.core.vo.query.blog.external.ExternalObjectQuery;
import com.communote.server.core.vo.query.blog.external.ExternalObjectQueryParameters;
import com.communote.server.model.blog.Blog;
import com.communote.server.model.blog.BlogRole;
import com.communote.server.model.external.ExternalObject;
import com.communote.server.model.external.ExternalObjectProperty;
import com.communote.server.model.user.User;
import com.communote.server.test.CommunoteIntegrationTest;
import com.communote.server.test.external.MockExternalObjectSource;
import com.communote.server.test.util.AuthenticationTestUtils;
import com.communote.server.test.util.TestUtils;

/**
 * Tests for {@link ExternalObjectManagement}.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ExternalObjectManagementTest extends CommunoteIntegrationTest {
    private User user;
    private ExternalObjectManagement externalObjectManagement;
    private PropertyManagement propertyManagement;
    private BlogRightsManagement blogRightsManagement;
    private String externalSystemId;
    private String propertyKey;

    private String propertyKeyGroup;

    private QueryManagement queryManagement;

    /**
     * Test that the blog has the given external object.
     *
     * @param blog
     *            the blog to test
     * @param externalObjectTO
     *            the TO containing external name, external Id and one property the external object
     *            should have
     * @param externalName
     *            external name to check against instead of the one of the external object TO. If
     *            null the one of the TO is used.
     * @throws Exception
     *             in case the test failed
     */
    private void assertExternalObjectAssigned(Blog blog, ExternalObject externalObjectTO,
            String externalName) throws Exception {
        if (externalName == null) {
            externalName = externalObjectTO.getExternalName();
        }
        assertExternalObjectAssigned(blog.getId(), externalObjectTO.getExternalId(), externalName,
                externalObjectTO.getProperties().iterator().next().getPropertyValue());
    }

    /**
     * Test that the blog has the given external object and that the external object has the given
     * property.
     *
     * @param blogId
     *            the ID of the blog
     * @param externalId
     *            the ID of the external object in the external system
     * @param externalName
     *            the name of the external object
     * @param propertyValue
     *            the expected property value, or null if not required
     * @throws Exception
     *             in case the test failed
     */
    private void assertExternalObjectAssigned(Long blogId, String externalId, String externalName,
            String... propertyValue) throws Exception {
        // get the external object from topic
        Collection<ExternalObject> externalObjects = externalObjectManagement
                .getExternalObjects(blogId);
        Assert.assertEquals(externalObjects.size(), 1);
        ExternalObject assignedExternalObject = externalObjects.iterator().next();
        Assert.assertEquals(assignedExternalObject.getExternalId(), externalId);
        Assert.assertEquals(assignedExternalObject.getExternalName(), externalName);
        Assert.assertEquals(assignedExternalObject.getExternalSystemId(), externalSystemId);
        assertExternalObjectProperty(assignedExternalObject.getId(), propertyValue);
    }

    /**
     * Assert that the object properties are set correctly
     *
     * @param externalObjectId
     *            the ID of the external object
     * @param propertyValue
     *            the expected property values, if not provided no properties must be set
     * @throws Exception
     *             in case the test failed
     */
    private void assertExternalObjectProperty(Long externalObjectId, String... propertyValue)
            throws Exception {
        Set<StringPropertyTO> properties = propertyManagement.getAllObjectProperties(
                PropertyType.ExternalObjectProperty, externalObjectId);
        if (propertyValue.length == 0) {
            Assert.assertEquals(properties.size(), 0);
        } else {
            Assert.assertEquals(properties.size(), propertyValue.length);
            for (String propValue : propertyValue) {
                boolean found = false;
                for (StringPropertyTO returnedProperty : properties) {
                    Assert.assertEquals(returnedProperty.getKeyGroup(), propertyKeyGroup);
                    Assert.assertEquals(returnedProperty.getPropertyKey(), propertyKey);
                    if (propValue.equals(returnedProperty.getPropertyValue())) {
                        found = true;
                        break;
                    }
                }
                Assert.assertTrue(found, "Property with value " + propValue + " not found");
            }

        }
    }

    /**
     * Assert the provided external objects are all assigned to the blog
     *
     * @param blog
     *            the blog to test
     * @param externalObjectTOs
     *            the external objects
     * @throws Exception
     *             in case the test failed
     */
    private void assertExternalObjectsAssigned(Blog blog, ExternalObject... externalObjectTOs)
            throws Exception {
        Collection<ExternalObject> externalObjects = externalObjectManagement
                .getExternalObjects(blog.getId());
        Assert.assertEquals(externalObjects.size(), externalObjectTOs.length);
        for (ExternalObject externalObjectTO : externalObjectTOs) {
            boolean found = false;
            for (ExternalObject externalObject : externalObjects) {
                if (externalObjectTO.getExternalId().equals(externalObject.getExternalId())
                        && externalObjectTO.getExternalSystemId().equals(
                                externalObject.getExternalSystemId())) {
                    Assert.assertEquals(externalObject.getExternalName(),
                            externalObjectTO.getExternalName());
                    String[] propValues;
                    if (externalObjectTO.getProperties() == null) {
                        propValues = new String[0];
                    } else {
                        propValues = new String[externalObjectTO.getProperties().size()];
                        int i = 0;
                        for (ExternalObjectProperty property : externalObjectTO.getProperties()) {
                            propValues[i] = property.getPropertyValue();
                            i++;
                        }
                    }
                    assertExternalObjectProperty(externalObject.getId(), propValues);
                    found = true;
                    break;
                }
            }
            Assert.assertTrue(found, "External object " + externalObjectTO.getExternalId()
                    + " not assigned");
        }
    }

    /**
     * Build an external object for test
     *
     * @return an created external object
     */
    private ExternalObject buildExternalObject() {
        ExternalObject externalObject = ExternalObject.Factory.newInstance();

        externalObject.setExternalName(UUID.randomUUID().toString());

        externalObject.setExternalSystemId(externalSystemId);
        externalObject.setExternalId(UUID.randomUUID().toString());

        ExternalObjectProperty externalObjectProperty = ExternalObjectProperty.Factory
                .newInstance();
        externalObjectProperty.setPropertyKey(propertyKey);
        externalObjectProperty.setKeyGroup(propertyKeyGroup);
        externalObjectProperty.setPropertyValue(UUID.randomUUID().toString());

        Set<ExternalObjectProperty> externalProperties = new HashSet<ExternalObjectProperty>();
        externalProperties.add(externalObjectProperty);

        // add to externalObject
        externalObject.setProperties(externalProperties);

        return externalObject;
    }

    /**
     * Setups all common parameters for this tests.
     *
     * @throws Exception
     *             in case the test failed
     */
    @BeforeClass(dependsOnGroups = "integration-test-setup")
    public void setup() throws Exception {
        user = TestUtils.createRandomUser(false);
        externalObjectManagement = ServiceLocator.instance().getService(
                ExternalObjectManagement.class);
        propertyManagement = ServiceLocator.instance().getService(PropertyManagement.class);
        blogRightsManagement = ServiceLocator.instance().getService(BlogRightsManagement.class);

        queryManagement = ServiceLocator.findService(QueryManagement.class);

        AuthenticationTestUtils.setManagerContext();

        MockExternalObjectSource repo = TestUtils.createNewExternalObjectSource(true);
        externalSystemId = repo.getIdentifier();

        propertyKeyGroup = UUID.randomUUID().toString();
        propertyKey = UUID.randomUUID().toString();
        // register group and key combination
        propertyManagement.addObjectPropertyFilter(PropertyType.ExternalObjectProperty,
                propertyKeyGroup, propertyKey);
    }

    /**
     * Test for assigning an external object to a blog that is already assigned
     *
     * @throws Exception
     *             in case the test failed
     */
    @Test(dependsOnMethods = "testAssignExternalObject")
    public void testAssignAssignedExternalObject() throws Exception {
        Blog blog = TestUtils.createRandomBlog(true, true, user);
        ExternalObject externalObjectTO = buildExternalObject();
        String externalName = externalObjectTO.getExternalName();
        externalObjectManagement.assignExternalObject(blog.getId(), externalObjectTO);
        // when assigning to same blog nothing should happen
        externalObjectTO.setExternalName(externalName + "_mod");
        externalObjectManagement.assignExternalObject(blog.getId(), externalObjectTO);
        // check that unmodified
        assertExternalObjectAssigned(blog, externalObjectTO, externalName);

        // try assigning to another blog which should fail
        Blog blog2 = TestUtils.createRandomBlog(true, true, user);
        try {
            externalObjectManagement.assignExternalObject(blog2.getId(), externalObjectTO);
        } catch (ExternalObjectAlreadyAssignedException e) {
            // expected
            // check that still assigned
            assertExternalObjectAssigned(blog, externalObjectTO, externalName);
        }
    }

    /**
     * Test for assigning an external object to a blog
     *
     * @throws Exception
     *             in case the test failed
     */
    @Test
    public void testAssignExternalObject() throws Exception {

        Blog blog = TestUtils.createRandomBlog(true, true, user);

        ExternalObject externalObjectTO = buildExternalObject();

        ExternalObject resultExternalObject = externalObjectManagement.assignExternalObject(
                blog.getId(), externalObjectTO);

        // Test result of assignExternalObjectToBlog method, not checking property because we do not
        // expect it is contained
        Assert.assertEquals(resultExternalObject.getExternalId(), externalObjectTO.getExternalId());
        Assert.assertEquals(resultExternalObject.getExternalName(),
                externalObjectTO.getExternalName());
        Assert.assertEquals(resultExternalObject.getExternalSystemId(), externalSystemId);

        // check that externalObject is assigned and retrievable
        String propertyValue = externalObjectTO.getProperties().iterator().next()
                .getPropertyValue();
        assertExternalObjectAssigned(blog.getId(), externalObjectTO.getExternalId(),
                externalObjectTO.getExternalName(), propertyValue);
        Assert.assertTrue(externalObjectManagement.isExternalObjectAssigned(blog.getId(),
                externalSystemId, externalObjectTO.getExternalId()));

        // test assigning another blog to the same external object, should throw an exception
        Blog topic2 = TestUtils.createRandomBlog(true, true, user);
        try {
            resultExternalObject = externalObjectManagement.assignExternalObject(topic2.getId(),
                    externalObjectTO);
        } catch (ExternalObjectAlreadyAssignedException e) {
            // expected exception

            // the other topic should still be connected with the external object
            assertExternalObjectAssigned(blog.getId(), externalObjectTO.getExternalId(),
                    externalObjectTO.getExternalName(), propertyValue);
            return;
        }
        Assert.fail("ExternalObjectAlreadyAssignedException wasn't thrown");
    }

    /**
     * Tests, if it is possible to assign an external object to an topic for the current user who is
     * not manager of topic.
     *
     * @throws Exception
     *             in case the test failed
     */
    @Test(expectedExceptions = BlogAccessException.class)
    public void testAssignExternalObjectNoBlogAccess() throws Exception {
        User noManagerUser = TestUtils.createRandomUser(false);
        Blog topic = TestUtils.createRandomBlog(true, true, user, noManagerUser);
        // change authentication to user without management access
        AuthenticationTestUtils.setSecurityContext(noManagerUser);
        externalObjectManagement.assignExternalObject(topic.getId(), buildExternalObject());
    }

    /**
     * Test for assigning and updating several external objects of a blog
     *
     * @throws Exception
     *             in case the test failed
     */
    @Test(dependsOnMethods = "testAssignExternalObject")
    public void testAssignOrUpdateExternalObjects() throws Exception {
        Blog blog = TestUtils.createRandomBlog(true, true, user);
        ArrayList<ExternalObject> externalObjects = new ArrayList<ExternalObject>();
        ExternalObject externalObjectTO1 = buildExternalObject();
        ExternalObject externalObjectTO2 = buildExternalObject();
        externalObjects.add(externalObjectTO1);
        externalObjects.add(externalObjectTO2);
        externalObjectManagement.assignOrUpdateExternalObjects(blog.getId(), externalObjects);
        assertExternalObjectsAssigned(blog, externalObjectTO1, externalObjectTO2);
        // create new one to assign and modify one to test update
        ExternalObject externalObjectTO3 = buildExternalObject();
        externalObjectTO2.setExternalName("modified name");
        externalObjectTO2.getProperties().iterator().next()
                .setPropertyValue("modified property value");
        externalObjects.clear();
        externalObjects.add(externalObjectTO3);
        externalObjects.add(externalObjectTO2);
        externalObjectManagement.assignOrUpdateExternalObjects(blog.getId(), externalObjects);
        // paranoid :)
        Assert.assertEquals(externalObjectTO2.getExternalName(), "modified name");
        Assert.assertEquals(externalObjectTO2.getProperties().iterator().next().getPropertyValue(),
                "modified property value");
        assertExternalObjectsAssigned(blog, externalObjectTO1, externalObjectTO2, externalObjectTO3);
        // test that update via ID works using TO1
        externalObjectTO1.setExternalName("modified name");
        // try removing property
        externalObjectTO1.getProperties().iterator().next().setPropertyValue(null);
        Collection<ExternalObject> assignedExternalObjects = externalObjectManagement
                .getExternalObjects(blog.getId());
        ExternalObject persistedExternalObjectTO1 = null;
        for (ExternalObject persistedExternalObject : assignedExternalObjects) {
            if (persistedExternalObject.getExternalId().equals(externalObjectTO1.getExternalId())) {
                persistedExternalObjectTO1 = ExternalObject.Factory.newInstance();
                persistedExternalObjectTO1.setId(persistedExternalObject.getId());
                persistedExternalObjectTO1.setExternalName(externalObjectTO1.getExternalName());
                persistedExternalObjectTO1.setProperties(externalObjectTO1.getProperties());
                break;
            }
        }
        ExternalObject externalObjectTO4 = buildExternalObject();
        externalObjects.clear();
        externalObjects.add(persistedExternalObjectTO1);
        externalObjects.add(externalObjectTO4);
        externalObjectManagement.assignOrUpdateExternalObjects(blog.getId(), externalObjects);
        // clear properties of TO1 before passing to assert
        externalObjectTO1.getProperties().clear();
        assertExternalObjectsAssigned(blog, externalObjectTO1, externalObjectTO2,
                externalObjectTO3, externalObjectTO4);
    }

    /**
     * Test for assigning several external objects to a blog
     *
     * @throws Exception
     *             in case the test failed
     */
    @Test(dependsOnMethods = "testAssignExternalObject")
    public void testAssignSeveralExternalObjects() throws Exception {

        Blog blog = TestUtils.createRandomBlog(true, true, user);

        ExternalObject externalObjectTO1 = buildExternalObject();
        ExternalObject externalObjectTO2 = buildExternalObject();
        ExternalObject externalObjectTO3 = buildExternalObject();
        HashMap<String, ExternalObject> externalIdToObjectTO = new HashMap<String, ExternalObject>();
        externalIdToObjectTO.put(externalObjectTO1.getExternalId(), externalObjectTO1);
        externalIdToObjectTO.put(externalObjectTO2.getExternalId(), externalObjectTO2);
        externalIdToObjectTO.put(externalObjectTO3.getExternalId(), externalObjectTO3);

        externalObjectManagement.assignExternalObject(blog.getId(), externalObjectTO1);
        externalObjectManagement.assignExternalObject(blog.getId(), externalObjectTO2);
        externalObjectManagement.assignExternalObject(blog.getId(), externalObjectTO3);
        Collection<ExternalObject> externalObjects = externalObjectManagement
                .getExternalObjects(blog.getId());
        Assert.assertEquals(externalObjects.size(), 3);
        for (ExternalObject assignedObject : externalObjects) {
            Assert.assertEquals(assignedObject.getExternalSystemId(), externalSystemId);
            ExternalObject objectTO = externalIdToObjectTO.get(assignedObject.getExternalId());
            Assert.assertNotNull(objectTO);
            Assert.assertEquals(assignedObject.getExternalId(), objectTO.getExternalId());
            Assert.assertEquals(assignedObject.getExternalName(), objectTO.getExternalName());
            assertExternalObjectProperty(assignedObject.getId(), objectTO.getProperties()
                    .iterator().next().getPropertyValue());
            externalIdToObjectTO.remove(assignedObject.getExternalId());
        }
    }

    @Test
    public void testExternalObjectQuery() throws Exception {

        User user2 = TestUtils.createRandomUser(false);

        Blog blog1 = TestUtils.createRandomBlog(true, true, user);
        Blog blog2 = TestUtils.createRandomBlog(false, false, user2);

        String systemId = TestUtils.createNewExternalObjectSource(true).getIdentifier();
        String id1 = UUID.randomUUID().toString();
        String id2 = UUID.randomUUID().toString();

        AuthenticationTestUtils.setSecurityContext(user);
        ExternalObject extO = ExternalObject.Factory.newInstance(systemId, id1);
        extO.setExternalName("testname1");
        externalObjectManagement.assignExternalObject(blog1.getId(), extO);

        AuthenticationTestUtils.setSecurityContext(user2);
        extO = ExternalObject.Factory.newInstance(systemId, id2);
        extO.setExternalName("testname2");
        externalObjectManagement.assignExternalObject(blog2.getId(), extO);

        AuthenticationTestUtils.setSecurityContext(user);

        Assert.assertEquals(ServiceLocator.findService(BlogRightsManagement.class)
                .currentUserHasReadAccess(blog2.getId(), false), false);

        final ExternalObjectQuery query = new ExternalObjectQuery();
        final ExternalObjectQueryParameters params = new ExternalObjectQueryParameters();

        params.setExternalId(id1);
        params.setExternalSystemId(systemId);
        params.setResultSpecification(new ResultSpecification(0, 10));

        List<ExternalObjectListItem> result = queryManagement.query(query, params);
        Assert.assertEquals(result.size(), 1);
        Assert.assertEquals(result.get(0).getExternalId(), id1);
        Assert.assertEquals(result.get(0).getExternalSystemId(), systemId);
        Assert.assertEquals(result.get(0).getExternalName(), "testname1");
        Assert.assertEquals(result.get(0).getTopicId(), blog1.getId());
        Assert.assertEquals(result.get(0).getTopicNameIdentifier(), blog1.getNameIdentifier());
        Assert.assertTrue(result.get(0).isHasAccessToTopic());

        params.setExternalId(id2);
        result = queryManagement.query(query, params);
        Assert.assertEquals(result.size(), 1);
        Assert.assertEquals(result.get(0).getExternalId(), id2);
        Assert.assertEquals(result.get(0).getExternalSystemId(), systemId);
        Assert.assertEquals(result.get(0).getExternalName(), null);
        Assert.assertEquals(result.get(0).getTopicId(), blog2.getId());
        Assert.assertEquals(result.get(0).getTopicNameIdentifier(), null);
        Assert.assertFalse(result.get(0).isHasAccessToTopic());

        // Test that normal user is not allowed to query all external objects
        params.setExternalId(null);

        try {
            result = queryManagement.query(query, params);
            Assert.fail("Only internal system user should be allowed to query without external ID");
        } catch (RuntimeException rt) {
            // success
        }
        // test as internal system, should get both external objects
        SecurityContext context = AuthenticationHelper.setInternalSystemToSecurityContext();
        try {
            result = queryManagement.query(query, params);
            Assert.assertEquals(result.size(), 2);
        } finally {
            AuthenticationHelper.setSecurityContext(context);
        }
    }

    /**
     * Test for removing an assigned external object by the external ID. Also tests that blog rights
     * for the external system are removed if the external object is the last assigned object for
     * the external system.
     *
     * @throws Exception
     *             in case the test failed
     */
    @Test(dependsOnMethods = "testAssignSeveralExternalObjects")
    public void testRemoveExternalObjectByExternalId() throws Exception {
        Blog blog1 = TestUtils.createRandomBlog(true, true, user);
        ExternalObject externalObjectTO = buildExternalObject();

        // test remove with external system id and external id
        externalObjectManagement.assignExternalObject(blog1.getId(), externalObjectTO);
        Collection<ExternalObject> externalObjects = externalObjectManagement
                .getExternalObjects(blog1.getId());
        Assert.assertEquals(externalObjects.size(), 1);

        externalObjectManagement.removeExternalObject(blog1.getId(), externalSystemId,
                externalObjectTO.getExternalId());

        externalObjects = externalObjectManagement.getExternalObjects(blog1.getId());
        Assert.assertEquals(externalObjects.size(), 0);
        Assert.assertFalse(externalObjectManagement.isExternalObjectAssigned(blog1.getId(),
                externalSystemId, externalObjectTO.getExternalId()));

        // external object should now be assignable to another blog
        Blog blog2 = TestUtils.createRandomBlog(true, true, user);
        externalObjectManagement.assignExternalObject(blog2.getId(), externalObjectTO);
        assertExternalObjectAssigned(blog2, externalObjectTO, null);

        // test remove with removal of blog rights granted for the external system
        User externalUser = TestUtils.createRandomUser(false, externalSystemId);
        AuthenticationTestUtils.setSecurityContext(user);
        blogRightsManagement.assignEntityForExternal(blog2.getId(), externalUser.getId(),
                BlogRole.MEMBER, externalSystemId, externalObjectTO.getExternalId());
        Assert.assertEquals(
                blogRightsManagement.getRoleOfEntity(blog2.getId(), externalUser.getId(), true),
                BlogRole.MEMBER);
        // assign another external object to test that roles are not removed while still assigned
        ExternalObject externalObjectTO2 = buildExternalObject();
        externalObjectManagement.assignExternalObject(blog2.getId(), externalObjectTO2);
        externalObjects = externalObjectManagement.getExternalObjects(blog2.getId());
        Assert.assertEquals(externalObjects.size(), 2);
        // remove first external object: other must still be assigned and user still has to be
        // member
        externalObjectManagement.removeExternalObject(blog2.getId(), externalSystemId,
                externalObjectTO.getExternalId());
        assertExternalObjectAssigned(blog2, externalObjectTO2, null);
        Assert.assertEquals(
                blogRightsManagement.getRoleOfEntity(blog2.getId(), externalUser.getId(), true),
                BlogRole.MEMBER);
        // remove last external object: member must have been removed
        externalObjectManagement.removeExternalObject(blog2.getId(), externalSystemId,
                externalObjectTO2.getExternalId());
        externalObjects = externalObjectManagement.getExternalObjects(blog1.getId());
        Assert.assertEquals(externalObjects.size(), 0);
        Assert.assertNull(blogRightsManagement.getRoleOfEntity(blog2.getId(), externalUser.getId(),
                true));
    }

    /**
     * Test for removing an assigned external object by the object ID. Also tests that blog rights
     * for the external system are removed if the external object is the last assigned object for
     * the external system.
     *
     * @throws Exception
     *             in case the test failed
     */
    @Test(dependsOnMethods = "testAssignSeveralExternalObjects")
    public void testRemoveExternalObjectById() throws Exception {
        Blog blog1 = TestUtils.createRandomBlog(true, true, user);
        ExternalObject externalObjectTO = buildExternalObject();

        ExternalObject resultExternalObject = externalObjectManagement.assignExternalObject(
                blog1.getId(), externalObjectTO);
        Collection<ExternalObject> externalObjects = externalObjectManagement
                .getExternalObjects(blog1.getId());
        Assert.assertEquals(externalObjects.size(), 1);

        externalObjectManagement.removeExternalObject(resultExternalObject.getId());

        externalObjects = externalObjectManagement.getExternalObjects(blog1.getId());
        Assert.assertEquals(externalObjects.size(), 0);
        Assert.assertFalse(externalObjectManagement.isExternalObjectAssigned(blog1.getId(),
                externalSystemId, externalObjectTO.getExternalId()));

        // external object should now be assignable to another blog
        Blog blog2 = TestUtils.createRandomBlog(true, true, user);
        resultExternalObject = externalObjectManagement.assignExternalObject(blog2.getId(),
                externalObjectTO);
        assertExternalObjectAssigned(blog2, externalObjectTO, null);

        // test remove with removal of blog rights granted for the external system
        User externalUser = TestUtils.createRandomUser(false, externalSystemId);
        AuthenticationTestUtils.setSecurityContext(user);
        blogRightsManagement.assignEntityForExternal(blog2.getId(), externalUser.getId(),
                BlogRole.MEMBER, externalSystemId, externalObjectTO.getExternalId());
        Assert.assertEquals(
                blogRightsManagement.getRoleOfEntity(blog2.getId(), externalUser.getId(), true),
                BlogRole.MEMBER);
        // assign another external object to test that roles are not removed while still assigned
        ExternalObject externalObjectTO2 = buildExternalObject();
        ExternalObject resultExternalObject2 = externalObjectManagement.assignExternalObject(
                blog2.getId(), externalObjectTO2);
        externalObjects = externalObjectManagement.getExternalObjects(blog2.getId());
        Assert.assertEquals(externalObjects.size(), 2);
        // remove first external object: other must still be assigned and user still has to be
        // member
        externalObjectManagement.removeExternalObject(resultExternalObject.getId());
        assertExternalObjectAssigned(blog2, externalObjectTO2, null);
        Assert.assertEquals(
                blogRightsManagement.getRoleOfEntity(blog2.getId(), externalUser.getId(), true),
                BlogRole.MEMBER);
        // remove last external object: member must have been removed
        externalObjectManagement.removeExternalObject(resultExternalObject2.getId());
        externalObjects = externalObjectManagement.getExternalObjects(blog1.getId());
        Assert.assertEquals(externalObjects.size(), 0);
        Assert.assertNull(blogRightsManagement.getRoleOfEntity(blog2.getId(), externalUser.getId(),
                true));
    }

    /**
     * Test for removing an assigned external object by the object ID. Also tests that blog rights
     * for the external system are removed if the external object is the last assigned object for
     * the external system.
     *
     * @throws Exception
     *             in case the test failed
     */
    @Test(dependsOnMethods = "testAssignSeveralExternalObjects")
    public void testRemoveExternalObjectByIdTrusted() throws Exception {
        Blog blog1 = TestUtils.createRandomBlog(true, true, user);
        ExternalObject externalObjectTO = buildExternalObject();
        AuthenticationTestUtils.setSecurityContext(user);
        ExternalObject resultExternalObject = externalObjectManagement.assignExternalObject(
                blog1.getId(), externalObjectTO);
        Collection<ExternalObject> externalObjects = externalObjectManagement
                .getExternalObjects(blog1.getId());
        Assert.assertEquals(externalObjects.size(), 1);
        // only client manager is allowed
        AuthenticationTestUtils.setManagerContext();
        externalObjectManagement.removeExternalObjectTrusted(resultExternalObject.getId());
        AuthenticationTestUtils.setSecurityContext(user);
        externalObjects = externalObjectManagement.getExternalObjects(blog1.getId());
        Assert.assertEquals(externalObjects.size(), 0);
        Assert.assertFalse(externalObjectManagement.isExternalObjectAssigned(blog1.getId(),
                externalSystemId, externalObjectTO.getExternalId()));

        // external object should now be assignable to another blog
        Blog blog2 = TestUtils.createRandomBlog(true, true, user);
        resultExternalObject = externalObjectManagement.assignExternalObject(blog2.getId(),
                externalObjectTO);
        assertExternalObjectAssigned(blog2, externalObjectTO, null);

        // test remove with removal of blog rights granted for the external system
        User externalUser = TestUtils.createRandomUser(false, externalSystemId);
        AuthenticationTestUtils.setSecurityContext(user);
        blogRightsManagement.assignEntityForExternal(blog2.getId(), externalUser.getId(),
                BlogRole.MEMBER, externalSystemId, externalObjectTO.getExternalId());
        Assert.assertEquals(
                blogRightsManagement.getRoleOfEntity(blog2.getId(), externalUser.getId(), true),
                BlogRole.MEMBER);
        // assign another external object to test that roles are not removed while still assigned
        ExternalObject externalObjectTO2 = buildExternalObject();
        ExternalObject resultExternalObject2 = externalObjectManagement.assignExternalObject(
                blog2.getId(), externalObjectTO2);
        externalObjects = externalObjectManagement.getExternalObjects(blog2.getId());
        Assert.assertEquals(externalObjects.size(), 2);
        // remove first external object: other must still be assigned and user still has to be
        // member
        AuthenticationTestUtils.setManagerContext();
        externalObjectManagement.removeExternalObjectTrusted(resultExternalObject.getId());
        AuthenticationTestUtils.setSecurityContext(user);
        assertExternalObjectAssigned(blog2, externalObjectTO2, null);
        Assert.assertEquals(
                blogRightsManagement.getRoleOfEntity(blog2.getId(), externalUser.getId(), true),
                BlogRole.MEMBER);
        // remove last external object: member must have been removed
        AuthenticationTestUtils.setManagerContext();
        externalObjectManagement.removeExternalObjectTrusted(resultExternalObject2.getId());
        AuthenticationTestUtils.setSecurityContext(user);
        externalObjects = externalObjectManagement.getExternalObjects(blog1.getId());
        Assert.assertEquals(externalObjects.size(), 0);
        Assert.assertNull(blogRightsManagement.getRoleOfEntity(blog2.getId(), externalUser.getId(),
                true));
    }

    /**
     * Test that it is not possible to remove an external object from a blog the current user is not
     * manager of
     *
     * @throws Exception
     *             in case the test failed
     */
    @Test(dependsOnMethods = "testAssignExternalObject")
    public void testRemoveExternalObjectNoBlogAccess() throws Exception {
        User noManagerUser = TestUtils.createRandomUser(false);
        Blog blog = TestUtils.createRandomBlog(true, true, user, noManagerUser);
        ExternalObject externalObjectTO = buildExternalObject();

        AuthenticationTestUtils.setSecurityContext(user);
        ExternalObject resultExternalObject = externalObjectManagement.assignExternalObject(
                blog.getId(), externalObjectTO);
        assertExternalObjectAssigned(blog, externalObjectTO, null);

        // change authentication to user without management access
        AuthenticationTestUtils.setSecurityContext(noManagerUser);

        try {
            externalObjectManagement.removeExternalObject(blog.getId(), externalSystemId,
                    externalObjectTO.getExternalId());
            Assert.fail("Expected exception was not thrown");
        } catch (BlogAccessException e) {
            // expected
            assertExternalObjectAssigned(blog, externalObjectTO, null);
        }
        // test remove by ID
        try {
            externalObjectManagement.removeExternalObject(resultExternalObject.getId());
            Assert.fail("Expected exception was not thrown");
        } catch (BlogAccessException e) {
            // expected
            assertExternalObjectAssigned(blog, externalObjectTO, null);
        }
        // now check that client manager isn't allowed neither
        AuthenticationTestUtils.setManagerContext();
        try {
            externalObjectManagement.removeExternalObject(blog.getId(), externalSystemId,
                    externalObjectTO.getExternalId());
            Assert.fail("Expected exception was not thrown");
        } catch (BlogAccessException e1) {
            // expected
            assertExternalObjectAssigned(blog, externalObjectTO, null);
        }
        try {
            externalObjectManagement.removeExternalObject(resultExternalObject.getId());
            Assert.fail("Expected exception was not thrown");
        } catch (BlogAccessException e1) {
            // expected
            assertExternalObjectAssigned(blog, externalObjectTO, null);
        }
    }

    /**
     * Test that it is not possible to remove an external object from a blog the current user is not
     * manager of
     *
     * @throws Exception
     *             in case the test failed
     */
    @Test(dependsOnMethods = "testAssignExternalObject")
    public void testRemoveExternalObjectTrustedNoBlogAccess() throws Exception {
        User noManagerUser = TestUtils.createRandomUser(false);
        Blog blog = TestUtils.createRandomBlog(true, true, user, noManagerUser);
        ExternalObject externalObjectTO = buildExternalObject();

        AuthenticationTestUtils.setSecurityContext(user);
        // test remove with external system id and external id
        ExternalObject resultExternalObject = externalObjectManagement.assignExternalObject(
                blog.getId(), externalObjectTO);
        assertExternalObjectAssigned(blog, externalObjectTO, null);
        try {
            externalObjectManagement.removeExternalObjectTrusted(resultExternalObject.getId());
            Assert.fail("Expected exception was not thrown");
        } catch (AuthorizationException e) {
            // expected
            assertExternalObjectAssigned(blog, externalObjectTO, null);
        }
        AuthenticationTestUtils.setSecurityContext(noManagerUser);
        try {
            externalObjectManagement.removeExternalObjectTrusted(resultExternalObject.getId());
            Assert.fail("Expected exception was not thrown");
        } catch (AuthorizationException e) {
            // expected
            assertExternalObjectAssigned(blog, externalObjectTO, null);
        }
    }

    /**
     * Test the replaceExternalObjects method
     *
     * @throws Exception
     *             in case the test failed
     */
    @Test(dependsOnMethods = "testAssignExternalObject")
    public void testReplaceExternalObjects() throws Exception {
        Blog blog = TestUtils.createRandomBlog(true, true, user);
        ArrayList<ExternalObject> externalObjects = new ArrayList<ExternalObject>();
        ExternalObject externalObjectTO1 = buildExternalObject();
        ExternalObject externalObjectTO2 = buildExternalObject();
        externalObjects.add(externalObjectTO1);
        externalObjects.add(externalObjectTO2);
        externalObjectManagement.replaceExternalObjects(blog.getId(), externalObjects);
        assertExternalObjectsAssigned(blog, externalObjectTO1, externalObjectTO2);
        ExternalObject externalObjectTO3 = buildExternalObject();
        ExternalObject externalObjectTO4 = buildExternalObject();
        externalObjects.clear();
        externalObjects.add(externalObjectTO3);
        externalObjects.add(externalObjectTO4);
        externalObjectManagement.replaceExternalObjects(blog.getId(), externalObjects);
        assertExternalObjectsAssigned(blog, externalObjectTO3, externalObjectTO4);
        // let TO4 be removed and modify TO3, re add TO1
        externalObjectTO3.setExternalName("modified name");
        externalObjectTO3.getProperties().iterator().next()
                .setPropertyValue("modified property value");
        externalObjects.clear();
        externalObjects.add(externalObjectTO1);
        externalObjects.add(externalObjectTO3);
        externalObjectManagement.replaceExternalObjects(blog.getId(), externalObjects);
        Assert.assertEquals(externalObjectTO3.getExternalName(), "modified name");
        Assert.assertEquals(externalObjectTO3.getProperties().iterator().next().getPropertyValue(),
                "modified property value");
        assertExternalObjectsAssigned(blog, externalObjectTO1, externalObjectTO3);

        // check that external blog roles are handled correctly
        String externalSystemIdLocal = TestUtils.createNewExternalObjectSource(true)
                .getIdentifier();
        externalObjectTO2.setExternalSystemId(externalSystemIdLocal);
        externalObjects.add(externalObjectTO2);
        externalObjectManagement.replaceExternalObjects(blog.getId(), externalObjects);
        assertExternalObjectsAssigned(blog, externalObjectTO1, externalObjectTO2, externalObjectTO3);

        User externalUser1 = TestUtils.createRandomUser(false, externalSystemId);
        User externalUser2 = TestUtils.createRandomUser(false, externalSystemIdLocal);
        AuthenticationTestUtils.setSecurityContext(user);
        blogRightsManagement.assignEntityForExternal(blog.getId(), externalUser1.getId(),
                BlogRole.MEMBER, externalSystemId, externalObjectTO1.getExternalId());
        Assert.assertEquals(
                blogRightsManagement.getRoleOfEntity(blog.getId(), externalUser1.getId(), true),
                BlogRole.MEMBER);
        blogRightsManagement.assignEntityForExternal(blog.getId(), externalUser2.getId(),
                BlogRole.MEMBER, externalSystemIdLocal, externalObjectTO2.getExternalId());
        Assert.assertEquals(
                blogRightsManagement.getRoleOfEntity(blog.getId(), externalUser2.getId(), true),
                BlogRole.MEMBER);
        externalObjects.clear();
        externalObjects.add(externalObjectTO1);
        externalObjects.add(externalObjectTO3);
        externalObjectManagement.replaceExternalObjects(blog.getId(), externalObjects);
        assertExternalObjectsAssigned(blog, externalObjectTO1, externalObjectTO3);
        // only user added for external system of TO2 must have been removed
        Assert.assertEquals(
                blogRightsManagement.getRoleOfEntity(blog.getId(), externalUser1.getId(), true),
                BlogRole.MEMBER);
        Assert.assertNull(blogRightsManagement.getRoleOfEntity(blog.getId(), externalUser2.getId(),
                true));
    }

    /**
     * Test that a {@link TooManyExternalObjectsPerTopicException} is thrown
     *
     * @throws BlogNotFoundException
     *             in case of an error
     * @throws BlogAccessException
     *             in case of an error
     * @throws ExternalObjectAlreadyAssignedException
     *             in case of an error
     * @throws ExternalSystemNotConfiguredException
     *             in case of an error
     */
    @Test
    public void testTooManyExternalObjects() throws Exception {
        try {
            MockExternalObjectSource repo = TestUtils.createNewExternalObjectSource(true);
            repo.getConfiguration().setNumberOfMaximumExternalObjectsPerTopic(1);

            Blog blog1 = TestUtils.createRandomBlog(true, true, this.user);
            Blog blog2 = TestUtils.createRandomBlog(true, true, this.user);

            // should work
            externalObjectManagement.assignExternalObject(blog1.getId(), ExternalObject.Factory
                    .newInstance(repo.getIdentifier(), UUID.randomUUID().toString()));
            // should work
            externalObjectManagement.assignExternalObject(blog2.getId(), ExternalObject.Factory
                    .newInstance(repo.getIdentifier(), UUID.randomUUID().toString()));

            // should fail with exception because blog1 already got an object assigned
            externalObjectManagement.assignExternalObject(blog1.getId(), ExternalObject.Factory
                    .newInstance(repo.getIdentifier(), UUID.randomUUID().toString()));

            Assert.fail("TooManyExternalObjectsPerTopicException should have been thrown.");

        } catch (TooManyExternalObjectsPerTopicException e) {
            // success
        }
    }

    /**
     * Test updating an external object by external system ID and external ID
     *
     * @throws Exception
     *             in case the test failed
     */
    @Test(dependsOnMethods = "testAssignExternalObject")
    public void testUpdateExternalObject() throws Exception {
        String originalExternalName = "original external name";
        String modifiedExternalName = "modified external name";
        Blog blog = TestUtils.createRandomBlog(true, true, user);
        // build external object, set name to predefined value and save generated property value
        ExternalObject externalObjectTO = buildExternalObject();
        String externalObjectPropertyValue = externalObjectTO.getProperties().iterator().next()
                .getPropertyValue();
        externalObjectTO.setExternalName(originalExternalName);
        // assign new external object
        externalObjectManagement.assignExternalObject(blog.getId(), externalObjectTO);

        // update external object with another name
        externalObjectTO.setExternalName(modifiedExternalName);
        externalObjectManagement.updateExternalObject(blog.getId(), externalObjectTO);
        assertExternalObjectAssigned(blog.getId(), externalObjectTO.getExternalId(),
                modifiedExternalName, externalObjectPropertyValue);

        // update property value
        String modifiedExternalObjectPropertyValue = externalObjectPropertyValue + "_mod";
        externalObjectTO.getProperties().iterator().next()
                .setPropertyValue(modifiedExternalObjectPropertyValue);
        externalObjectManagement.updateExternalObject(blog.getId(), externalObjectTO);
        assertExternalObjectAssigned(blog.getId(), externalObjectTO.getExternalId(),
                modifiedExternalName, modifiedExternalObjectPropertyValue);

        // remove property by setting property value to null
        externalObjectTO.getProperties().iterator().next().setPropertyValue(null);
        externalObjectManagement.updateExternalObject(blog.getId(), externalObjectTO);
        assertExternalObjectAssigned(blog.getId(), externalObjectTO.getExternalId(),
                modifiedExternalName);
    }

    /**
     * Test that it is not possible to update an external object for a blog it is not assigned to.
     *
     * @throws Exception
     *             in case the test failed
     */
    @Test(dependsOnMethods = "testAssignExternalObject")
    public void testUpdateExternalObjectAssignedToOtherBlog() throws Exception {
        Blog blog1 = TestUtils.createRandomBlog(true, true, user);
        Blog blog2 = TestUtils.createRandomBlog(true, true, user);
        ExternalObject externalObjectTO = buildExternalObject();
        ExternalObject resultExternalObject = externalObjectManagement.assignExternalObject(
                blog1.getId(), externalObjectTO);
        // create external object for updating
        ExternalObject updateExternalObjectTO = ExternalObject.Factory.newInstance();
        updateExternalObjectTO.setExternalName("modified name");
        updateExternalObjectTO.setProperties(externalObjectTO.getProperties());
        // try updating by external ID and system ID for blog2
        updateExternalObjectTO.setExternalId(externalObjectTO.getExternalId());
        updateExternalObjectTO.setExternalSystemId(externalSystemId);
        try {
            externalObjectManagement.updateExternalObject(blog2.getId(), updateExternalObjectTO);
            Assert.fail("Expected exception not thrown");
        } catch (ExternalObjectAlreadyAssignedException e) {
            // expected; object still has to be assigned
            assertExternalObjectAssigned(blog1, externalObjectTO, null);
        }
        // try updating by ID for blog2
        updateExternalObjectTO.setId(resultExternalObject.getId());
        updateExternalObjectTO.setExternalId(null);
        try {
            externalObjectManagement.updateExternalObject(blog2.getId(), updateExternalObjectTO);
            Assert.fail("Expected exception not thrown");
        } catch (ExternalObjectAlreadyAssignedException e) {
            // expected; object still has to be assigned
            assertExternalObjectAssigned(blog1, externalObjectTO, null);
        }
    }

    /**
     * Test updating an external object by ID
     *
     * @throws Exception
     *             in case the test failed
     */
    @Test(dependsOnMethods = "testAssignExternalObject")
    public void testUpdateExternalObjectById() throws Exception {
        String originalExternalName = "original external name";
        String modifiedExternalName = "modified external name";
        Blog blog = TestUtils.createRandomBlog(true, true, user);
        // build external object, set name to predefined value and save generated property value
        ExternalObject externalObjectTO = buildExternalObject();
        String externalId = externalObjectTO.getExternalId();
        String externalObjectPropertyValue = externalObjectTO.getProperties().iterator().next()
                .getPropertyValue();
        externalObjectTO.setExternalName(originalExternalName);
        // assign new external object, copy ID and remove external system ID and external ID to
        // force usage of ID
        ExternalObject resultExternalObject = externalObjectManagement.assignExternalObject(
                blog.getId(), externalObjectTO);
        externalObjectTO.setId(resultExternalObject.getId());
        externalObjectTO.setExternalId(null);
        externalObjectTO.setExternalSystemId(null);

        // update external object with another name
        externalObjectTO.setExternalName(modifiedExternalName);
        externalObjectManagement.updateExternalObject(blog.getId(), externalObjectTO);
        assertExternalObjectAssigned(blog.getId(), externalId, modifiedExternalName,
                externalObjectPropertyValue);

        // update property value
        String modifiedExternalObjectPropertyValue = externalObjectPropertyValue + "_mod";
        externalObjectTO.getProperties().iterator().next()
                .setPropertyValue(modifiedExternalObjectPropertyValue);
        externalObjectManagement.updateExternalObject(blog.getId(), externalObjectTO);
        assertExternalObjectAssigned(blog.getId(), externalId, modifiedExternalName,
                modifiedExternalObjectPropertyValue);

        // remove property by setting property value to null
        externalObjectTO.getProperties().iterator().next().setPropertyValue(null);
        externalObjectManagement.updateExternalObject(blog.getId(), externalObjectTO);
        assertExternalObjectAssigned(blog.getId(), externalId, modifiedExternalName);
    }

    /**
     * Test that it is not possible to update an external object for a blog the current user is not
     * manager of.
     *
     * @throws Exception
     *             in case the test failed
     */
    @Test(dependsOnMethods = "testAssignExternalObject")
    public void testUpdateExternalObjectNoBlogAccess() throws Exception {
        User noManagerUser = TestUtils.createRandomUser(false);
        Blog blog = TestUtils.createRandomBlog(true, true, user, noManagerUser);
        ExternalObject externalObjectTO = buildExternalObject();
        String externalName = externalObjectTO.getExternalName();
        // assign new external object
        externalObjectManagement.assignExternalObject(blog.getId(), externalObjectTO);

        // change authentication to user without management access
        AuthenticationTestUtils.setSecurityContext(noManagerUser);
        externalObjectTO.setExternalName(externalName + "_mod");
        try {
            externalObjectManagement.updateExternalObject(blog.getId(), externalObjectTO);
            Assert.fail("Expected exception not thrown");
        } catch (BlogAccessException e) {
            // expected
            // assert that unmodified
            assertExternalObjectAssigned(blog, externalObjectTO, externalName);
        }
        // assert that client manager isn't allowed
        AuthenticationTestUtils.setManagerContext();
        try {
            externalObjectManagement.updateExternalObject(blog.getId(), externalObjectTO);
            Assert.fail("Expected exception not thrown");
        } catch (BlogAccessException e) {
            // expected
            // assert that unmodified
            assertExternalObjectAssigned(blog, externalObjectTO, externalName);
        }
    }
}
