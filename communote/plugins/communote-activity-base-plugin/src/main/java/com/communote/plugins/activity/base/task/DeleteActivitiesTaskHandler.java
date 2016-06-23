package com.communote.plugins.activity.base.task;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContext;

import com.communote.common.util.PageableList;
import com.communote.common.util.Pair;
import com.communote.plugins.activity.base.ActivityBaseActivator;
import com.communote.plugins.activity.base.data.ActivityConfiguration;
import com.communote.plugins.activity.base.data.ActivityDefinition;
import com.communote.plugins.activity.base.service.ActivityService;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.common.NotFoundException;
import com.communote.server.api.core.note.NoteData;
import com.communote.server.api.core.note.NoteManagementAuthorizationException;
import com.communote.server.api.core.note.NoteRenderContext;
import com.communote.server.api.core.note.NoteRenderMode;
import com.communote.server.api.core.property.PropertyManagement;
import com.communote.server.api.core.property.PropertyType;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.api.core.task.TaskHandler;
import com.communote.server.api.core.task.TaskTO;
import com.communote.server.api.core.user.UserData;
import com.communote.server.core.blog.FavoriteManagement;
import com.communote.server.core.osgi.OSGiManagement;
import com.communote.server.core.query.QueryManagement;
import com.communote.server.core.security.AuthenticationHelper;
import com.communote.server.core.tasks.ClientTaskHandler;
import com.communote.server.core.vo.query.note.SimpleNoteListItemToNoteDataQueryResultConverter;
import com.communote.server.core.vo.query.post.NoteQuery;
import com.communote.server.service.NoteService;

/**
 * TaskHandler for deleting old activities.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class DeleteActivitiesTaskHandler extends ClientTaskHandler implements TaskHandler {

    private static final String PROPERTY_DELETION_RESCHEDULE_INTERVAL = "com.communote.activity.job.delete.reschedule.in.hours";
    private static final Logger LOGGER = LoggerFactory.getLogger(DeleteActivitiesTaskHandler.class);
    private final NoteService noteManagement = ServiceLocator.instance().getService(
            NoteService.class);

    private final QueryManagement queryManagement = ServiceLocator.instance().getService(
            QueryManagement.class);

    private final OSGiManagement osgiManagement = ServiceLocator.instance().getService(
            OSGiManagement.class);

    private final PropertyManagement propertyManagement = ServiceLocator.instance().getService(
            PropertyManagement.class);
    private final NoteQuery query = new NoteQuery();
    private final NoteRenderContext renderContext = new NoteRenderContext(NoteRenderMode.PLAIN,
            Locale.ENGLISH);
    private boolean duplicateNoteIdDetected;

    private boolean canDeleteActivity(NoteData activityToVisit) throws AuthorizationException {

        boolean canDelete = false;

        if (activityToVisit.getNumberOfComments() == 0) {

            Collection<UserData> likers = activityToVisit.getProperty("usersWhichLikeThisPost");

            if (likers.size() == 0) {

                int numFavor = ServiceLocator.findService(FavoriteManagement.class)
                        .getNumberOfFavorites(activityToVisit.getId());

                if (numFavor == 0) {
                    canDelete = true;
                }
            }
        }

        return canDelete;
    }

    @Override
    protected boolean checkRunTask(TaskTO task) {
        duplicateNoteIdDetected = false;
        return super.checkRunTask(task);
    }

    /**
     * Method to finally delete the activities for the given template id.
     *
     * @param templateId
     *            Template id of the activity to delete.
     * @param expirationTimeout
     *            Expiration timeout of the activity to delete.
     * @throws DuplicateNoteIdDetectedForDeletionException
     */
    private void deleteActivities(String templateId, long expirationTimeout)
            throws DuplicateNoteIdDetectedForDeletionException {
        LOGGER.debug("Deleting activities for template id: {}", templateId);
        DeleteActivitiesNoteQueryParameters queryParameters = new DeleteActivitiesNoteQueryParameters(
                templateId, expirationTimeout);
        queryParameters.getResultSpecification().setNumberOfElements(25);
        queryParameters.getResultSpecification().setCheckAtLeastMoreResults(1);

        PageableList<NoteData> activitiesNotesToVisit;
        Set<Long> vistedNoteIds = new HashSet<Long>();

        final SimpleNoteListItemToNoteDataQueryResultConverter<NoteData> converter = new SimpleNoteListItemToNoteDataQueryResultConverter<NoteData>(
                NoteData.class, renderContext);

        do {

            activitiesNotesToVisit = queryManagement.query(query, queryParameters, converter);
            LOGGER.debug("Will visit {} activities for template id {} and check for deletion",
                    activitiesNotesToVisit.size(), templateId);
            int deletedActivities = 0;
            int failedDeletedActivities = 0;
            for (NoteData activityToVisit : activitiesNotesToVisit) {
                if (vistedNoteIds.contains(activityToVisit.getId())) {
                    throw new DuplicateNoteIdDetectedForDeletionException(
                            "Visting noteId '"
                                    + activityToVisit.getId()
                                    + " ' twice. This may lead to an infinite loop. Will not continue for templateId '"
                                    + templateId + " ' ");
                }
                vistedNoteIds.add(activityToVisit.getId());
                try {
                    if (canDeleteActivity(activityToVisit)) {

                        try {
                            noteManagement.deleteNote(activityToVisit.getId(), false, true);
                            deletedActivities++;
                        } catch (NoteManagementAuthorizationException e) {
                            LOGGER.error("It was not possible to delete an activity ({}).",
                                    activityToVisit.getId(), e);
                            failedDeletedActivities++;
                        }

                    } else {

                        // cannot delete activity than mark it for undeletable and ignore it in the
                        // next
                        // runs
                        markActivityAsUndeletable(activityToVisit);
                    }
                } catch (NotFoundException e) {
                    if (LOGGER.isTraceEnabled()) {
                        LOGGER.trace("Error finding not with id, probably already deleted. Will ignore it. noteId="
                                + activityToVisit.getId() + " errrorMessage=" + e.getMessage());
                    }
                } catch (AuthorizationException e) {
                    LOGGER.error(
                            "Error marking activity for non-deletion because of AuthorizationException.",
                            e);
                    throw new RuntimeException(
                            "Error marking activity for non-deletion because of AuthorizationException.",
                            e);
                }

            }

            LOGGER.debug(
                    "Deleted {} of {} activites for template id {}. {} ended in error of deletion.",
                    deletedActivities, activitiesNotesToVisit.size(), failedDeletedActivities);
        } while (activitiesNotesToVisit.getMinNumberOfAdditionalElements() > 0);
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * It is possible to set the difference in hours for the next schedule using the system property
     * "com.communote.activity.job.delete.reschedule.in.hours". The value must be an integer.
     * </p>
     */
    @Override
    public Date getRescheduleDate(Date now) {
        Calendar nextExecution = Calendar.getInstance();
        nextExecution.setTime(now);
        String property = System.getProperty(PROPERTY_DELETION_RESCHEDULE_INTERVAL, "24");
        try {
            nextExecution.add(Calendar.HOUR_OF_DAY, Integer.parseInt(property));
        } catch (NumberFormatException e) {
            LOGGER.warn("The value of '{}' was not an integer ({})",
                    PROPERTY_DELETION_RESCHEDULE_INTERVAL, property);
            nextExecution.add(Calendar.HOUR_OF_DAY, 24);
        }
        return nextExecution.getTime();
    }

    public boolean isDuplicateNoteIdDetected() {
        return duplicateNoteIdDetected;
    }

    private void markActivityAsUndeletable(NoteData activityToVisit) throws NotFoundException,
    AuthorizationException {

        propertyManagement.setObjectProperty(PropertyType.NoteProperty, activityToVisit.getId(),
                ActivityService.PROPERTY_KEY_GROUP,
                ActivityService.NOTE_PROPERTY_KEY_ACTIVITY_UNDELETABLE, Boolean.TRUE.toString());

    }

    /**
     * <p>
     * Collects all activities and deletes activity notes for them.
     * </p>
     * {@inheritDoc}
     */
    @Override
    protected void runOnClient(TaskTO task) throws Exception {
        String symbolicName = task.getProperty(OSGiManagement.PROPERTY_KEY_SYMBOLIC_NAME);
        // TODO add a method isBundleStarted(class classFromBundle) to osgiManagement which checks
        // whether a bundle is active with help of FrameworkUtil.getBundle(classFromBundle)
        if (!osgiManagement.isBundleStarted(symbolicName)) {
            LOGGER.warn(
                    "Deletion of activites will be skipped as a required plugin is not available: {}",
                    symbolicName);
            return;
        }
        SecurityContext currentContext = AuthenticationHelper.setInternalSystemToSecurityContext();
        try {
            List<Pair<ActivityDefinition, ActivityConfiguration>> activities = ActivityBaseActivator
                    .getActivityService().getActivities(null);
            LOGGER.info("Starting to delete old activities.");
            long startTime = System.currentTimeMillis();
            int counter = 0;
            for (Pair<ActivityDefinition, ActivityConfiguration> activity : activities) {
                if (activity.getRight().getExpirationTimeout() <= 0) {
                    continue;
                }
                counter++;
                String templateId = activity.getLeft() != null ? activity.getLeft().getTemplateId()
                        : activity.getRight().getTemplateId();
                try {
                    deleteActivities(templateId, activity.getRight().getExpirationTimeout());
                } catch (DuplicateNoteIdDetectedForDeletionException e) {
                    LOGGER.error(e.getMessage(), e);
                    duplicateNoteIdDetected = true;
                }
            }
            LOGGER.info(
                    "Finished deleting old activities for {} activity definitions. Needed about {} s",
                    counter, (System.currentTimeMillis() - startTime) / 1000);
        } finally {
            AuthenticationHelper.setSecurityContext(currentContext);
        }
    }
}
