package com.communote.server.core.property;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.event.EventDispatcher;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.model.property.Propertyable;
import com.communote.server.model.property.StringProperty;
import com.communote.server.persistence.property.PropertyDao;

/**
 * Object property accessor for string properties. The "object" in {@link ObjectPropertyAccessor}
 * stands for the assignment of property directly to one object. In database you can see only one
 * foreign key relation of property table.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 * @param <O>
 *            the object having the properties
 * @param <P>
 *            the type of property to handle
 */
public abstract class ObjectPropertyAccessor<O extends Propertyable, P extends StringProperty>
        extends StringPropertyAccessor<O, P> {

    /**
     *
     * @param eventDispatcher
     *            the event dispatcher for dispatching event on property changes
     */
    public ObjectPropertyAccessor(EventDispatcher eventDispatcher) {
        super(eventDispatcher);
    }

    /**
     * Get an global property, that is one with no key group (internal the key group will be global
     * however).
     *
     * @param object
     *            the object to get the property of
     * @param keyGroup
     *            the key group of the property to get
     * @param key
     *            the key of the property
     * @return the property or null if the property does not exists
     * @throws AuthorizationException
     *             Thrown, when the user is not allowed to access.
     */
    @Override
    protected P handleGetObjectPropertyUnfiltered(O object, String keyGroup, String key)
            throws AuthorizationException {
        for (StringProperty property : object.getProperties()) {
            if (property.keyEquals(keyGroup, key)) {
                return (P) property;
            }
        }
        return null;
    }

    /**
     * Removes the property of an object
     *
     * @param object
     *            object to get the property of
     * @param keyGroup
     *            the key group of the property to get
     * @param key
     *            the key of the property
     * @throws AuthorizationException
     *             Thrown, when the user is not allowed to access.
     */
    @Override
    protected void handleRemoveObjectProperty(O object, String keyGroup, String key)
            throws AuthorizationException {
        P property = handleGetObjectPropertyUnfiltered(object, keyGroup, key);
        if (property != null) {
            object.getProperties().remove(property);
            PropertyDao propertyDao = ServiceLocator.findService(PropertyDao.class);
            propertyDao.remove(property);
        }
    }
}