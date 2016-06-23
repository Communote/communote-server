package com.communote.server.core.property;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.event.EventDispatcher;
import com.communote.server.api.core.property.PropertyManagement;
import com.communote.server.api.core.property.PropertyType;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.model.external.ExternalObject;
import com.communote.server.model.external.ExternalObjectProperty;
import com.communote.server.persistence.external.ExternalObjectDao;

/***
 * Property accessor for users, don't construct it, use it from the {@link PropertyManagement}
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class ExternalObjectPropertyAccessor extends
        ObjectPropertyAccessor<ExternalObject, ExternalObjectProperty> {

    /**
     * Don't construct it from the outside packages.
     *
     * @param eventDispatcher
     *            the event dispatcher to use for firing events
     */
    protected ExternalObjectPropertyAccessor(EventDispatcher eventDispatcher) {
        super(eventDispatcher);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void assertReadAccess(ExternalObject externalObject) {
        // TODO Assert read access to the topic, but how to get the topic here?
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void assertWriteAccess(ExternalObject object) throws AuthorizationException {
        // TODO Assert client manager or topic manager. Problem is: How to get the topic?
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Long getObjectId(ExternalObject object) {
        return object.getId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PropertyType getPropertyType() {
        return PropertyType.ExternalObjectProperty;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ExternalObjectProperty handleCreateNewProperty(ExternalObject externalObject) {
        ExternalObjectProperty property = ExternalObjectProperty.Factory.newInstance();
        externalObject.getProperties().add(property);
        return property;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ExternalObject load(Long id) {
        return ServiceLocator.findService(ExternalObjectDao.class).load(id);

    }

}
