package com.communote.plugins.activity.base.task;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.communote.common.i18n.StaticLocalizedMessage;
import com.communote.common.util.PageableList;
import com.communote.plugins.activity.base.data.ActivityDefinition;
import com.communote.plugins.activity.base.service.ActivityService;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.property.PropertyManagement;
import com.communote.server.api.core.property.PropertyType;
import com.communote.server.core.blog.FavoriteManagement;
import com.communote.server.core.filter.listitems.SimpleNoteListItem;
import com.communote.server.core.query.QueryManagement;
import com.communote.server.core.security.AuthenticationHelper;
import com.communote.server.core.template.NoteTemplateService;
import com.communote.server.core.vo.query.post.NoteQuery;
import com.communote.server.model.blog.Blog;
import com.communote.server.model.property.StringProperty;
import com.communote.server.model.user.User;
import com.communote.server.test.CommunoteIntegrationTest;
import com.communote.server.test.util.TestUtils;

/**
 * This test the correct filter behavior of {@link DeleteActivitiesNoteQueryParameters}.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class DeleteActivitiesNoteQueryParametersTest extends CommunoteIntegrationTest {

    private PropertyManagement propertyManagement;

    private User user;
    private Blog topic;
    private QueryManagement queryManagement;
    private ActivityDefinition activity1Definition;
    private ActivityDefinition activity2Definition;
    private Long activity1Id;
    private Long activity1WithLikeId;
    private Long activity1WithCommentId;
    private Long activity2Id;
    private Long activity1WithFavor;

    /**
     * Creates the query instance.
     * 
     * @param activityDefinition
     *            The activity to use.
     * @return The query instance.
     */
    private DeleteActivitiesNoteQueryParameters createQueryInstance(
            ActivityDefinition activityDefinition) {
        DeleteActivitiesNoteQueryParameters queryInstance = new DeleteActivitiesNoteQueryParameters(
                activityDefinition.getTemplateId(), 0);
        return queryInstance;
    }

    /**
     * Prepares the test.
     * 
     * @throws Exception
     *             Exception.
     */
    @BeforeClass(dependsOnGroups = GROUP_INTEGRATION_TEST_SETUP)
    public void setup() throws Exception {

        queryManagement = ServiceLocator.instance().getService(QueryManagement.class);
        propertyManagement = ServiceLocator.instance().getService(PropertyManagement.class);

        user = TestUtils.createRandomUser(false);
        topic = TestUtils.createRandomBlog(false, false, user);

        propertyManagement.addObjectPropertyFilter(PropertyType.NoteProperty,
                ActivityService.PROPERTY_KEY_GROUP,
                ActivityService.NOTE_PROPERTY_KEY_ACTIVITY);
        propertyManagement.addObjectPropertyFilter(PropertyType.NoteProperty,
                ActivityService.PROPERTY_KEY_GROUP,
                ActivityService.NOTE_PROPERTY_KEY_ACTIVITY_UNDELETABLE);

        activity1Definition = new ActivityDefinition(random(),
                new StaticLocalizedMessage(random()),
                new StaticLocalizedMessage(random()), false, false);
        activity2Definition = new ActivityDefinition(random(),
                new StaticLocalizedMessage(random()),
                new StaticLocalizedMessage(random()), false, false);
        NoteTemplateService noteTemplateService = ServiceLocator.instance().getService(
                NoteTemplateService.class);
        noteTemplateService.addDefinition(activity1Definition);
        noteTemplateService.addDefinition(activity2Definition);
        // Create some normal notes, so we can make sure, they are not filtered too
        TestUtils.createAndStoreCommonNote(topic, user.getId(), random());
        TestUtils.createAndStoreCommonNote(topic, user.getId(), random());
        TestUtils.createAndStoreCommonNote(topic, user.getId(), random());

        activity1Id = DeleteActivitiesTestUtils.createActivity(topic, user.getId(),
                activity1Definition.getTemplateId());
        activity1WithLikeId = DeleteActivitiesTestUtils.createActivity(topic, user.getId(),
                activity1Definition.getTemplateId());
        propertyManagement.setObjectProperty(PropertyType.UserNoteProperty,
                activity1WithLikeId, PropertyManagement.KEY_GROUP,
                "like", "true");
        activity1WithFavor = DeleteActivitiesTestUtils.createActivity(topic, user.getId(),
                activity1Definition.getTemplateId());
        ServiceLocator.instance().getService(FavoriteManagement.class)
                .markNoteAsFavorite(activity1WithFavor);
        activity1WithCommentId = DeleteActivitiesTestUtils.createActivity(topic, user.getId(),
                activity1Definition.getTemplateId());
        TestUtils.createAndStoreCommonNote(topic, user.getId(), random(), activity1WithCommentId);
        activity2Id = DeleteActivitiesTestUtils.createActivity(topic, user.getId(),
                activity2Definition.getTemplateId());
    }

    /**
     * Test that all activities are found.
     * 
     * @throws Exception
     *             in case the test failed
     */
    @Test
    public void testFiltering() throws Exception {
        AuthenticationHelper.setInternalSystemToSecurityContext();
        DeleteActivitiesNoteQueryParameters queryParameters = createQueryInstance(activity1Definition);
        NoteQuery query = new NoteQuery();
        PageableList<SimpleNoteListItem> result = queryManagement.query(query, queryParameters);
        // query has to return all activities, filtering is done in the task handler
        Assert.assertEquals(result.size(), 4);

        queryParameters = createQueryInstance(activity2Definition);
        result = queryManagement.query(query,
                queryParameters);
        Assert.assertEquals(result.size(), 1);
        Assert.assertEquals(result.get(0).getId(), activity2Id);

        // mark one activity of type activity1 as undeletable
        StringProperty prop = propertyManagement.setObjectProperty(
                PropertyType.NoteProperty,
                activity1WithCommentId,
                ActivityService.PROPERTY_KEY_GROUP,
                ActivityService.NOTE_PROPERTY_KEY_ACTIVITY_UNDELETABLE,
                Boolean.TRUE.toString());
        Assert.assertNotNull(prop);

        // double check if property is set correctly
        prop = propertyManagement.getObjectProperty(
                PropertyType.NoteProperty,
                activity1WithCommentId,
                ActivityService.PROPERTY_KEY_GROUP,
                ActivityService.NOTE_PROPERTY_KEY_ACTIVITY_UNDELETABLE);
        Assert.assertNotNull(prop);
        Assert.assertNotNull(prop.getPropertyValue());
        Assert.assertTrue(Boolean.parseBoolean(prop.getPropertyValue()));

        queryParameters = createQueryInstance(activity1Definition);
        result = queryManagement.query(query, queryParameters);
        Assert.assertEquals(result.size(), 3);
        for (SimpleNoteListItem item : result) {
            if (item.getId().equals(activity1WithCommentId)) {
                Assert.fail("Undeletable activity was not excluded!");
            }
        }
    }
}
