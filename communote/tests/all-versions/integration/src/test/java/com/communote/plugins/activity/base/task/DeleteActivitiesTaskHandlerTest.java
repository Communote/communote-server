package com.communote.plugins.activity.base.task;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.time.DateUtils;
import org.easymock.EasyMock;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.communote.common.i18n.StaticLocalizedMessage;
import com.communote.common.util.Pair;
import com.communote.plugins.activity.base.ActivityBaseActivator;
import com.communote.plugins.activity.base.data.ActivityConfiguration;
import com.communote.plugins.activity.base.data.ActivityDefinition;
import com.communote.plugins.activity.base.service.ActivityService;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.property.PropertyManagement;
import com.communote.server.api.core.property.PropertyType;
import com.communote.server.api.core.task.TaskTO;
import com.communote.server.core.blog.FavoriteManagement;
import com.communote.server.core.blog.helper.NoteHelper;
import com.communote.server.core.osgi.OSGiManagement;
import com.communote.server.core.template.NoteTemplateService;
import com.communote.server.model.blog.Blog;
import com.communote.server.model.user.User;
import com.communote.server.persistence.blog.NoteDao;
import com.communote.server.test.CommunoteIntegrationTest;
import com.communote.server.test.util.TestUtils;

/**
 * Test for the {@link DeleteActivitiesTaskHandler}.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class DeleteActivitiesTaskHandlerTest extends CommunoteIntegrationTest {

    private static final String BUNDLE_SYMBOLIC_NAME = "test-bundle" + UUID.randomUUID();

    private NoteDao noteDao;

    private final Set<Long> commonNotes = new HashSet<Long>();
    private final List<Long> activityIds = new ArrayList<Long>();

    private ActivityConfiguration activityConfiguration;

    private final int numberOfActivites = 100;
    private final long startTimeMillis = System.currentTimeMillis() - 100
            * DateUtils.MILLIS_PER_DAY;

    private FavoriteManagement favoriteManagement;
    private PropertyManagement propertyManagement;

    private Collection<Long> noteIdsThatShouldStayForever;
    private Collection<Long> noteIdsThatShouldStayForeverFavs;
    private Collection<Long> noteIdsThatShouldStayForeverLikes;

    private Collection<Long> discussionNoteIdsToKeep;
    private Collection<Long> discussionActivityNoteIdsToKeep;
    private Collection<Long> discussionActivityNoteIdsToRemove;

    private long getCreationDateForActivity(int i) {
        return startTimeMillis + (DateUtils.MILLIS_PER_DAY * i);
    }

    /**
     * Prepares the test.
     *
     * @throws Exception
     *             Exception.
     */
    @BeforeClass
    public void setup() throws Exception {
        favoriteManagement = ServiceLocator.findService(FavoriteManagement.class);
        propertyManagement = ServiceLocator.instance().getService(PropertyManagement.class);

        propertyManagement.addObjectPropertyFilter(PropertyType.NoteProperty,
                ActivityService.PROPERTY_KEY_GROUP, ActivityService.NOTE_PROPERTY_KEY_ACTIVITY);
        propertyManagement.addObjectPropertyFilter(PropertyType.NoteProperty,
                ActivityService.PROPERTY_KEY_GROUP,
                ActivityService.NOTE_PROPERTY_KEY_ACTIVITY_UNDELETABLE);
        User user = TestUtils.createRandomUser(false);
        Long authorId = user.getId();
        Blog topic = TestUtils.createRandomBlog(false, false, user);
        noteDao = ServiceLocator.findService(NoteDao.class);
        ActivityDefinition activityDefinition = new ActivityDefinition(random(),
                new StaticLocalizedMessage(random()), new StaticLocalizedMessage(random()), false,
                false);
        NoteTemplateService noteTemplateService = ServiceLocator.instance().getService(
                NoteTemplateService.class);
        noteTemplateService.addDefinition(activityDefinition);

        noteIdsThatShouldStayForever = new HashSet<Long>();
        noteIdsThatShouldStayForeverFavs = new HashSet<Long>();
        noteIdsThatShouldStayForeverLikes = new HashSet<Long>();

        // Create some normal notes, so we can make sure, they are not deleted too
        for (int i = 0; i < numberOfActivites; i++) {
            commonNotes.add(TestUtils.createAndStoreCommonNote(topic, user.getId(), random()));
        }

        // create activities with a day difference
        // some of the activities get a like, a fav, a comment or both
        for (int i = 0; i < numberOfActivites; i++) {
            Long activityId = DeleteActivitiesTestUtils.createActivity(topic, authorId,
                    activityDefinition.getTemplateId(), getCreationDateForActivity(i));

            if (i % 7 <= 1) {
                favoriteManagement.markNoteAsFavorite(activityId);
                // add it to both lists to allow easier differentiation of a fav or a like failed
                // later
                noteIdsThatShouldStayForever.add(activityId);
                noteIdsThatShouldStayForeverFavs.add(activityId);
            }
            if (i % 7 == 1 || i % 7 == 2) {
                NoteHelper.likeNote(activityId);
                // add it to both lists to allow easier differentiation of a fav or a like failed
                // later
                noteIdsThatShouldStayForever.add(activityId);
                noteIdsThatShouldStayForeverLikes.add(activityId);
            }
            if (i % 10 == 0) {
                // create a note that is a comment to the activity, the activity and the comment
                // should never be deleted
                noteIdsThatShouldStayForever.add(activityId);
                commonNotes.add(TestUtils.createAndStoreCommonNote(topic, authorId,
                        "i am a comment to an activity: " + activityId, activityId, new Date(
                                getCreationDateForActivity(i) + DateUtils.MILLIS_PER_HOUR)));
            }

            activityIds.add(activityId);
        }
        // create a discussion with activities as comments, some of them will have comment too
        discussionNoteIdsToKeep = new HashSet<Long>();
        discussionActivityNoteIdsToKeep = new HashSet<Long>();
        discussionActivityNoteIdsToRemove = new HashSet<Long>();
        Long parentNoteId = TestUtils.createAndStoreCommonNote(topic, user.getId(), random());
        discussionNoteIdsToKeep.add(parentNoteId);
        discussionNoteIdsToKeep.add(TestUtils.createAndStoreCommonNote(topic, user.getId(),
                random(), parentNoteId));
        Long commentNoteId1 = TestUtils.createAndStoreCommonNote(topic, user.getId(), random(),
                parentNoteId);
        discussionNoteIdsToKeep.add(commentNoteId1);
        Long commentNoteId2 = TestUtils.createAndStoreCommonNote(topic, user.getId(), random(),
                parentNoteId);
        discussionNoteIdsToKeep.add(commentNoteId2);

        Long activityId = DeleteActivitiesTestUtils.createActivityAsComment(parentNoteId, topic,
                authorId, activityDefinition.getTemplateId(), getCreationDateForActivity(1));
        discussionActivityNoteIdsToRemove.add(activityId);

        activityId = DeleteActivitiesTestUtils.createActivityAsComment(commentNoteId1, topic,
                authorId, activityDefinition.getTemplateId(), getCreationDateForActivity(1));
        discussionActivityNoteIdsToRemove.add(activityId);

        activityId = DeleteActivitiesTestUtils.createActivityAsComment(commentNoteId2, topic,
                authorId, activityDefinition.getTemplateId(), getCreationDateForActivity(1));
        discussionActivityNoteIdsToKeep.add(activityId);
        discussionNoteIdsToKeep.add(TestUtils.createAndStoreCommonNote(topic, user.getId(),
                random(), activityId));

        activityConfiguration = new ActivityConfiguration(activityDefinition.getTemplateId(),
                activityDefinition.getStaticName(), false);
        List<Pair<ActivityDefinition, ActivityConfiguration>> activities = new ArrayList<Pair<ActivityDefinition, ActivityConfiguration>>();
        Pair<ActivityDefinition, ActivityConfiguration> pair = new Pair<ActivityDefinition, ActivityConfiguration>(
                activityDefinition, activityConfiguration);
        activities.add(pair);
        ActivityService activityService = EasyMock.createMock(ActivityService.class);
        EasyMock.expect(activityService.getActivities(null)).andReturn(activities).anyTimes();
        Bundle bundle = EasyMock.createMock(Bundle.class);
        EasyMock.expect(bundle.getSymbolicName()).andReturn(BUNDLE_SYMBOLIC_NAME).anyTimes();
        BundleContext mockBundleContext = EasyMock.createMock(BundleContext.class);
        EasyMock.expect(mockBundleContext.getBundle()).andReturn(bundle).anyTimes();
        EasyMock.replay(activityService, bundle, mockBundleContext);

        ActivityBaseActivator activator = ActivityBaseActivator.instantiate(mockBundleContext);
        activator.bindService(activityService);
        Field startedField = ActivityBaseActivator.class.getDeclaredField("started");
        startedField.setAccessible(true);
        startedField.set(activator, Boolean.TRUE);
        BundleEvent bundleEvent = new BundleEvent(BundleEvent.STARTED, bundle);
        ServiceLocator.instance().getService(OSGiManagement.class).bundleChanged(bundleEvent);

    }

    /**
     * Tests the deletion.
     *
     * @throws Exception
     *             Exception.
     */
    @Test
    public void test() throws Exception {

        DeleteActivitiesTaskHandler taskHandler = new DeleteActivitiesTaskHandler();
        activityConfiguration.setExpirationTimeout(getCreationDateForActivity(0)
                + DateUtils.MILLIS_PER_DAY);
        TaskTO task = new TaskTO();
        task.setProperty(OSGiManagement.PROPERTY_KEY_SYMBOLIC_NAME, BUNDLE_SYMBOLIC_NAME);
        taskHandler.run(task);
        Assert.assertFalse(taskHandler.isDuplicateNoteIdDetected());
        for (Long activityId : activityIds) {
            Assert.assertNotNull(noteDao.load(activityId));
        }
        for (int i = numberOfActivites / 2; i < numberOfActivites; i += 5) {
            // use some offset to avoid equality of creation date, the expire date now refers to all
            // notes < i
            long expire = System.currentTimeMillis() - getCreationDateForActivity(i) + 1000;
            activityConfiguration.setExpirationTimeout(expire);
            taskHandler.run(task);
            Assert.assertFalse(taskHandler.isDuplicateNoteIdDetected());
            // now all notes for < i should be deleted
            for (int d = 0; d < i; d++) {
                // unless the note has a like or a fav
                if (!noteIdsThatShouldStayForever.contains(activityIds.get(d))) {
                    Assert.assertNull(noteDao.load(activityIds.get(d)), "checkIndex=" + d
                            + " currentExpire=" + i);
                }
            }

            // notes >= i should still exist
            for (int d = i; d < numberOfActivites; d++) {
                Assert.assertNotNull(noteDao.load(activityIds.get(d)));
            }
        }

        // check that non activity notes or notes with likes, favs are not deleted
        for (Long id : commonNotes) {
            Assert.assertNotNull(noteDao.load(id));
        }
        for (Long id : this.noteIdsThatShouldStayForeverLikes) {
            Assert.assertNotNull(noteDao.load(id), "activity with like should stay forever.");
        }
        for (Long id : this.noteIdsThatShouldStayForeverFavs) {
            Assert.assertNotNull(noteDao.load(id), "activity with favorite should stay forever.");
        }
        for (Long id : this.noteIdsThatShouldStayForever) {
            Assert.assertNotNull(noteDao.load(id));
        }
        // check that notes and activities of discussion were handled correctly
        for (Long id : this.discussionNoteIdsToKeep) {
            Assert.assertNotNull(noteDao.load(id));
        }
        for (Long id : this.discussionActivityNoteIdsToKeep) {
            Assert.assertNotNull(noteDao.load(id),
                    "Activity notes with comments must not be removed");
        }
        for (Long id : this.discussionActivityNoteIdsToRemove) {
            Assert.assertNull(noteDao.load(id), "Activity with ID " + id
                    + " that was created as a comment without other comments was not removed");
        }
    }
}
