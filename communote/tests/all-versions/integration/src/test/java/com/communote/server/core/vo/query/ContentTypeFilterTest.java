package com.communote.server.core.vo.query;

import java.util.Date;
import java.util.HashSet;
import java.util.UUID;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.note.NoteStoringTO;
import com.communote.server.api.core.property.PropertyManagement;
import com.communote.server.api.core.property.PropertyType;
import com.communote.server.api.core.property.StringPropertyTO;
import com.communote.server.core.query.QueryManagement;
import com.communote.server.core.vo.query.filter.PropertyFilter;
import com.communote.server.core.vo.query.filter.PropertyFilter.MatchMode;
import com.communote.server.core.vo.query.post.NoteQuery;
import com.communote.server.core.vo.query.post.NoteQueryParameters;
import com.communote.server.model.blog.Blog;
import com.communote.server.model.note.Note;
import com.communote.server.model.user.User;
import com.communote.server.service.NoteService;
import com.communote.server.test.CommunoteIntegrationTest;
import com.communote.server.test.util.TestUtils;

/**
 * This is a test for filtering for content types.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class ContentTypeFilterTest extends CommunoteIntegrationTest {

    private static final String TEST3PROPERTY = UUID.randomUUID().toString();
    private static final String TEST2PROPERTY = UUID.randomUUID().toString();
    private static final String TEST1PROPERTY = UUID.randomUUID().toString();
    private static final String TEST1VALUE = UUID.randomUUID().toString();
    private static final String TEST2VALUE = UUID.randomUUID().toString();
    private static final String TEST3VALUE = UUID.randomUUID().toString();
    private static final String GROUP_KEY = UUID.randomUUID().toString();
    private User user;
    private Blog blog;

    private final NoteQuery queryDefintion = new NoteQuery();
    private QueryManagement queryManagement;

    /**
     * Cleanup after the test
     */
    @AfterClass
    public void cleanup() {
        PropertyManagement propertyManagement = ServiceLocator.instance().getService(
                PropertyManagement.class);
        propertyManagement.removeObjectPropertyFilter(PropertyType.NoteProperty, GROUP_KEY,
                TEST1PROPERTY);
        propertyManagement.removeObjectPropertyFilter(PropertyType.NoteProperty, GROUP_KEY,
                TEST2PROPERTY);
        propertyManagement.removeObjectPropertyFilter(PropertyType.NoteProperty, GROUP_KEY,
                TEST3PROPERTY);
    }

    /**
     * Setups all common parameters for this tests.
     *
     * @throws Exception
     *             Exception.
     */
    @BeforeClass(dependsOnGroups = "integration-test-setup")
    public void setup() throws Exception {
        user = TestUtils.createRandomUser(false);
        blog = TestUtils.createRandomBlog(true, true, user);

        PropertyManagement propertyManagement = ServiceLocator.instance().getService(
                PropertyManagement.class);
        propertyManagement.addObjectPropertyFilter(PropertyType.NoteProperty, GROUP_KEY,
                TEST1PROPERTY);
        propertyManagement.addObjectPropertyFilter(PropertyType.NoteProperty, GROUP_KEY,
                TEST2PROPERTY);
        propertyManagement.addObjectPropertyFilter(PropertyType.NoteProperty, GROUP_KEY,
                TEST3PROPERTY);
        StringPropertyTO property1 = new StringPropertyTO(TEST1VALUE, GROUP_KEY, TEST1PROPERTY,
                new Date());
        StringPropertyTO property2 = new StringPropertyTO(TEST2VALUE, GROUP_KEY, TEST2PROPERTY,
                new Date());
        StringPropertyTO property3 = new StringPropertyTO(TEST3VALUE, GROUP_KEY, TEST3PROPERTY,
                new Date());

        NoteStoringTO noteStoringTO = TestUtils.createCommonNote(blog, user.getId(), "Test1");
        noteStoringTO.getProperties().add(property1);
        noteStoringTO.getProperties().add(property2);
        noteStoringTO.getProperties().add(property3);
        ServiceLocator.instance().getService(NoteService.class)
        .createNote(noteStoringTO, new HashSet<String>());
        NoteStoringTO noteStoringTO2 = TestUtils.createCommonNote(blog, user.getId(), "Test2");
        noteStoringTO2.getProperties().add(property1);
        ServiceLocator.instance().getService(NoteService.class)
                .createNote(noteStoringTO2, new HashSet<String>());
        NoteStoringTO noteStoringTO3 = TestUtils.createCommonNote(blog, user.getId(), "Test3");
        noteStoringTO3.getProperties().add(property3);
        ServiceLocator.instance().getService(NoteService.class)
                .createNote(noteStoringTO3, new HashSet<String>());
        queryManagement = ServiceLocator.findService(QueryManagement.class);
    }

    /**
     * Test whether filtering by content type is possible.
     */
    @Test
    public void testFilterByContentType() {
        NoteQueryParameters queryParameters = queryDefintion.createInstance();
        TaggingCoreItemUTPExtension filterExtension = new TaggingCoreItemUTPExtension();
        filterExtension.setBlogFilter(new Long[] { blog.getId() });
        queryParameters.setTypeSpecificExtension(filterExtension);
        Assert.assertEquals(queryManagement.query(queryDefintion, queryParameters).size(), 3);

        PropertyFilter filter = new PropertyFilter(GROUP_KEY, Note.class);
        filter.addProperty(TEST1PROPERTY, TEST1VALUE, MatchMode.EQUALS);
        queryParameters.addPropertyFilter(filter);
        Assert.assertEquals(queryManagement.query(queryDefintion, queryParameters).size(), 2);

        filter.addProperty(TEST3PROPERTY, TEST3VALUE, MatchMode.EQUALS);
        Assert.assertEquals(queryManagement.query(queryDefintion, queryParameters).size(), 3);

        PropertyFilter filter2 = new PropertyFilter(GROUP_KEY, Note.class);
        filter2.addProperty(TEST2PROPERTY, TEST2VALUE, MatchMode.EQUALS);
        queryParameters.addPropertyFilter(filter2);
        Assert.assertEquals(queryManagement.query(queryDefintion, queryParameters).size(), 1);
    }
}
