package com.communote.plugins.activity.base.permission;

import java.util.Set;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.communote.plugins.activity.base.service.ActivityService;
import com.communote.plugins.activity.base.service.ActivityServiceException;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.common.NotFoundException;
import com.communote.server.api.core.note.NotePermissionManagement;
import com.communote.server.api.core.note.NoteStoringTO;
import com.communote.server.api.core.property.PropertyManagement;
import com.communote.server.api.core.property.PropertyType;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.api.core.security.permission.Permission;
import com.communote.server.core.permission.filters.NotePermissionFilter;
import com.communote.server.core.template.NoteTemplateService;
import com.communote.server.model.note.Note;
import com.communote.server.model.property.StringProperty;

/**
 * Permission filter which removes the DELETE permission of notes that are activities and belong to
 * an activity type which does not allow the deletion.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
@Component
@Instantiate(name = "ActivityNotePermissionFilter")
@Provides
public class ActivityNotePermissionFilter implements NotePermissionFilter {
    private static final Logger LOGGER = LoggerFactory
            .getLogger(ActivityNotePermissionFilter.class);
    private PropertyManagement propertyManagement;

    @Requires
    private ActivityService activityService;

    @Override
    public void filter(Note entity, Set<Permission<Note>> permissions) {
        // if the permission we are trying to remove is not contained do nothing
        if (permissions.contains(NotePermissionManagement.PERMISSION_DELETE)) {
            // check if the note is an activity
            String templateId = isActivity(entity.getId());
            if (templateId != null) {
                try {
                    if (!activityService.isActivityDeletableByUser(templateId)) {
                        permissions.remove(NotePermissionManagement.PERMISSION_DELETE);
                    }
                    permissions.remove(NotePermissionManagement.PERMISSION_MOVE);
                } catch (ActivityServiceException e) {
                    // TODO should we propagate the exception?
                    LOGGER.error("Filtering ermissions of activity failed", e);
                }
            }
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void filterForCreation(NoteStoringTO entity, Set<Permission<Note>> permissions) {
        // nothing to do
    }

    @Override
    public int getOrder() {
        return NotePermissionFilter.DEFAULT_ORDER_VALUE;
    }

    /**
     * @return the lazily initialized property management
     */
    private PropertyManagement getPropertyManagement() {
        if (this.propertyManagement == null) {
            this.propertyManagement = ServiceLocator.instance()
                    .getService(PropertyManagement.class);
        }
        return this.propertyManagement;
    }

    /**
     * Test if the note is an activity and return the ID of the activity if it is one.
     * 
     * @param entityId
     *            the ID of the note to test
     * @return the templateId of the activity or null if the note is not an activity
     */
    private String isActivity(Long entityId) {
        PropertyManagement propertyManagement = getPropertyManagement();
        try {
            StringProperty property = propertyManagement.getObjectProperty(
                    PropertyType.NoteProperty, entityId, ActivityService.PROPERTY_KEY_GROUP,
                    ActivityService.NOTE_PROPERTY_KEY_ACTIVITY);
            if (property != null
                    && ActivityService.NOTE_PROPERTY_VALUE_ACTIVITY.equals(property
                            .getPropertyValue())) {
                property = propertyManagement
                        .getObjectProperty(PropertyType.NoteProperty, entityId,
                                PropertyManagement.KEY_GROUP,
                                NoteTemplateService.NOTE_PROPERTY_KEY_TEMPLATE_ID);
                return property != null ? property.getPropertyValue() : null;
            }
        } catch (AuthorizationException e) {
            LOGGER.error("Unexpected exception: permission filter was called although"
                    + " current user is not allowed to read the note", e);
        } catch (NotFoundException e) {
            LOGGER.error("Unexpected exception: the provided note (" + entityId
                    + " ) does not exist", e);
        }
        return null;
    }

}
