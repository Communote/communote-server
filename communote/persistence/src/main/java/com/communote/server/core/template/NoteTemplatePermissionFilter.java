package com.communote.server.core.template;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.common.NotFoundException;
import com.communote.server.api.core.note.NotePermissionManagement;
import com.communote.server.api.core.note.NoteStoringTO;
import com.communote.server.api.core.property.PropertyManagement;
import com.communote.server.api.core.property.PropertyType;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.api.core.security.permission.Permission;
import com.communote.server.core.permission.filters.NotePermissionFilter;
import com.communote.server.model.note.Note;
import com.communote.server.model.property.StringProperty;

/**
 * Permission filter which removes the EDIT permission from template notes.
 * 
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class NoteTemplatePermissionFilter implements NotePermissionFilter {
    private static final Logger LOGGER = LoggerFactory
            .getLogger(NoteTemplatePermissionFilter.class);

    private PropertyManagement propertyManagement;

    @Override
    public void filter(Note entity, Set<Permission<Note>> permissions) {
        // if the permission we are trying to remove is not contained do nothing
        if (permissions.contains(NotePermissionManagement.PERMISSION_EDIT)) {
            if (getTemplateId(entity.getId()) != null) {
                permissions.remove(NotePermissionManagement.PERMISSION_EDIT);
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
     * Test if the note is a template note and return the ID of the template if it is one.
     * 
     * @param entityId
     *            the ID of the note to test
     * @return the templateId of the note or null if the note is not a template note
     */
    private String getTemplateId(Long entityId) {
        try {
            StringProperty property = getPropertyManagement()
                    .getObjectProperty(PropertyType.NoteProperty, entityId,
                            PropertyManagement.KEY_GROUP,
                            NoteTemplateService.NOTE_PROPERTY_KEY_TEMPLATE_ID);
            return property != null ? property.getPropertyValue() : null;

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
