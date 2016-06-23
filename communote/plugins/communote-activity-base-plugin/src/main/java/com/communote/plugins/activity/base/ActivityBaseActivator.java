package com.communote.plugins.activity.base;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.felix.ipojo.annotations.Bind;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Validate;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.communote.plugins.activity.base.service.ActivityService;
import com.communote.plugins.activity.base.task.DeleteActivitiesTaskHandler;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.property.PropertyManagement;
import com.communote.server.api.core.property.PropertyType;
import com.communote.server.api.core.task.TaskAlreadyExistsException;
import com.communote.server.api.core.task.TaskManagement;
import com.communote.server.api.core.task.TaskStatusException;
import com.communote.server.api.core.task.TaskTO;
import com.communote.server.core.osgi.OSGiManagement;
import com.communote.server.model.task.TaskStatus;

/**
 * iPOJO style Activator for the activity base plugin
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
@Component(factoryMethod = "instantiate")
@Instantiate
public class ActivityBaseActivator {

    private static final Logger LOGGER = LoggerFactory.getLogger(ActivityBaseActivator.class);
    private static final long DEFAULT_DELETE_ACTIVITIES_TASK_START_OFFSET = 3600;

    /**
     * @return the activity service or null if not initialized
     */
    public static ActivityService getActivityService() {
        // activity service won't be available when stop was called so return null
        if (INSTANCE != null && INSTANCE.started) {
            return INSTANCE.activityService;
        }
        return null;
    }

    /**
     * Factory method to create the singleton instance of the activator
     * 
     * @param bundleContext
     *            the context of the bundle as provided by the framework
     * @return the instance of the activator
     */
    public static synchronized ActivityBaseActivator instantiate(BundleContext bundleContext) {
        if (INSTANCE == null) {
            INSTANCE = new ActivityBaseActivator(bundleContext);
        }
        return INSTANCE;
    }

    private final String symbolicName;
    private ActivityService activityService;

    private static ActivityBaseActivator INSTANCE;
    private boolean started;

    /**
     * private constructor to avoid creation from outside
     * 
     * @param bundleContext
     *            the context of the bundle
     */
    private ActivityBaseActivator(BundleContext bundleContext) {
        symbolicName = bundleContext.getBundle().getSymbolicName();
    }

    /**
     * Bind the activity service
     * 
     * @param activityService
     *            the ActivityService service
     */
    // using bind method because Requires annotation does not work (the service is null when
    // accessing it - no idea why)
    @Bind
    public void bindService(ActivityService activityService) {
        this.activityService = activityService;
    }

    /**
     * Callback to initialize the bundle when all required resources are available
     */
    @Validate
    public void start() {
        synchronized (this) {
            this.started = true;
        }
        PropertyManagement propertyManagement = ServiceLocator.instance().getService(
                PropertyManagement.class);
        propertyManagement.addObjectPropertyFilter(PropertyType.NoteProperty,
                ActivityService.PROPERTY_KEY_GROUP,
                ActivityService.NOTE_PROPERTY_KEY_ACTIVITY);
        propertyManagement.addObjectPropertyFilter(PropertyType.NoteProperty,
                ActivityService.PROPERTY_KEY_GROUP,
                ActivityService.NOTE_PROPERTY_KEY_ACTIVITY_UNDELETABLE);

        TaskManagement taskManagement =
                ServiceLocator.instance().getService(TaskManagement.class);
        String taskName = DeleteActivitiesTaskHandler.class.getName();
        taskManagement.addTaskHandler(taskName, DeleteActivitiesTaskHandler.class);
        Map<String, String> properties = new HashMap<String, String>();
        properties.put(OSGiManagement.PROPERTY_KEY_SYMBOLIC_NAME, symbolicName);
        long startOffset = Long.getLong(
                "com.communote.plugins.activity.base.delete.task.start.offset",
                DEFAULT_DELETE_ACTIVITIES_TASK_START_OFFSET) * 1000;
        TaskTO existingTask = taskManagement.findTask(taskName);
        long startTime = System.currentTimeMillis() + startOffset;
        if (existingTask != null) {
            LOGGER.debug("Task {} already exists and has status {}", taskName,
                    existingTask.getStatus());
            if (TaskStatus.PENDING.equals(existingTask.getStatus())
                    && existingTask.getNextExecution().getTime() - startTime < 0) {
                try {
                    LOGGER.debug("Rescheduling task {} to respect start offset", taskName);
                    taskManagement.rescheduleTask(taskName, new Date(startTime));
                } catch (TaskStatusException e) {
                    // can happen in clustered environment but isn't critical
                    LOGGER.debug("Rescheduling task {} failed because it is not pending anymore",
                            taskName);
                }
            }
        } else {
            try {
                taskManagement.addTask(taskName, true, 0L, new Date(startTime), properties,
                        DeleteActivitiesTaskHandler.class);
            } catch (TaskAlreadyExistsException e) {
                // might occur in clustered environment but isn't critical
                LOGGER.debug("Adding task {} failed because it already exists", taskName);
            }
        }
    }

    /**
     * Callback to clean-up after departure of required resources
     */
    @Invalidate
    public void stop() {
        synchronized (this) {
            this.started = false;
        }
        PropertyManagement propertyManagement = ServiceLocator.instance().getService(
                PropertyManagement.class);
        propertyManagement.removeObjectPropertyFilter(PropertyType.NoteProperty,
                ActivityService.PROPERTY_KEY_GROUP, ActivityService.NOTE_PROPERTY_KEY_ACTIVITY);
        propertyManagement.removeObjectPropertyFilter(PropertyType.NoteProperty,
                ActivityService.PROPERTY_KEY_GROUP,
                ActivityService.NOTE_PROPERTY_KEY_ACTIVITY_UNDELETABLE);
        TaskManagement taskManagement = ServiceLocator.instance().getService(TaskManagement.class);
        taskManagement.removeTaskHandler(DeleteActivitiesTaskHandler.class.getName());
    }
}