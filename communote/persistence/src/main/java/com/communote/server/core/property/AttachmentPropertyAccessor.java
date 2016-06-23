package com.communote.server.core.property;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.event.EventDispatcher;
import com.communote.server.api.core.property.PropertyType;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.core.blog.AttachmentNotFoundException;
import com.communote.server.core.storing.ResourceStoringManagement;
import com.communote.server.model.attachment.Attachment;
import com.communote.server.model.attachment.AttachmentProperty;
import com.communote.server.persistence.resource.AttachmentDao;

/**
 * Accessor for the properties of the attachment ressource.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class AttachmentPropertyAccessor extends
ObjectPropertyAccessor<Attachment, AttachmentProperty> {

    private ResourceStoringManagement resourceStoringManagement;

    public AttachmentPropertyAccessor(EventDispatcher eventDispatcher) {
        super(eventDispatcher);
    }

    @Override
    protected void assertReadAccess(Attachment attachment) throws AuthorizationException,
    AttachmentNotFoundException {

        getResourceStoringManagement().assertReadAccess(attachment.getId());

    }

    @Override
    protected void assertWriteAccess(Attachment attachment) throws AuthorizationException,
    AttachmentNotFoundException {

        getResourceStoringManagement().assertWriteAccess(attachment.getId());

    }

    @Override
    protected Long getObjectId(Attachment attachment) {
        return attachment.getId();
    }

    @Override
    public PropertyType getPropertyType() {
        return PropertyType.AttachmentProperty;
    }

    private ResourceStoringManagement getResourceStoringManagement() {
        if (resourceStoringManagement == null) {
            resourceStoringManagement = ServiceLocator.findService(ResourceStoringManagement.class);
        }
        return resourceStoringManagement;
    }

    @Override
    protected AttachmentProperty handleCreateNewProperty(Attachment attachment) {
        AttachmentProperty property = AttachmentProperty.Factory.newInstance();
        attachment.getProperties().add(property);
        return property;
    }

    @Override
    protected Attachment load(Long id) {
        return ServiceLocator.findService(AttachmentDao.class).load(id);
    }

}
