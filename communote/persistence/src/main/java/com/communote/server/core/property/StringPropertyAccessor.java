package com.communote.server.core.property;

import java.util.HashSet;
import java.util.Set;

import com.communote.server.api.core.common.NotFoundException;
import com.communote.server.api.core.event.EventDispatcher;
import com.communote.server.api.core.property.StringPropertyTO;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.model.property.Propertyable;
import com.communote.server.model.property.StringProperty;


/**
 * Property Accessor for String properties
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 * @param <O>
 *            the object having the properties
 * @param <P>
 *            the type of property to handle
 */
public abstract class StringPropertyAccessor<O extends Propertyable, P extends StringProperty>
        extends PropertyAccessor<O, P, String> {

    /**
     * 
     * @param eventDispatcher
     *            the event dispatcher for dispatching event on property changes
     */
    public StringPropertyAccessor(EventDispatcher eventDispatcher) {
        super(eventDispatcher);
    }

    /**
     * Get all properties of the object. The result set will be filtered by the filter definition of
     * the accessor
     * 
     * @param objectId
     *            the ID of the object
     * @return the properties
     * @throws NotFoundException
     *             in case the object does not exist
     * @throws AuthorizationException
     *             in case the user is not allowed to access the properties of the object
     */
    public Set<StringPropertyTO> getAllObjectProperties(Long objectId) throws NotFoundException,
            AuthorizationException {
        return handleGetAllObjectProperties(assertLoadObject(objectId, false));
    }

    /**
     * Get all properties of the object. The result set will be filtered by the filter definition of
     * the accessor
     * 
     * @param object
     *            the object
     * @return the properties
     * @throws AuthorizationException
     *             in case the user is not allowed to access the properties of the object
     */
    protected Set<StringPropertyTO> handleGetAllObjectProperties(O object)
            throws AuthorizationException {
        HashSet<StringPropertyTO> filteredProperties = new HashSet<StringPropertyTO>();
        for (StringProperty property : object.getProperties()) {
            if (getFilterDefintion().isPropertyAllowedToGet(property.getKeyGroup(),
                    property.getPropertyKey())) {
                filteredProperties.add(new StringPropertyTO(property.getPropertyValue(), property
                        .getKeyGroup(), property.getPropertyKey(), property
                        .getLastModificationDate()));
            }
        }
        return filteredProperties;
    }

    /**
     * Sets an set of properties.
     * 
     * @param objectId
     *            the id of the object to get the property of
     * @param properties
     *            set of properties to set for object.
     * @throws AuthorizationException
     *             Thrown, when the user is not allowed to access.
     * @throws NotFoundException
     *             in case there is not object to the given id.
     */
    public void setObjectProperties(Long objectId, Set<StringPropertyTO> properties)
            throws AuthorizationException, NotFoundException {
        // avoid overhead of loading if nothing is to do
        if (properties != null && !properties.isEmpty()) {
            O object = assertLoadObject(objectId, true);
            for (StringPropertyTO propertyTO : properties) {
                handleSetObjectProperty(object, propertyTO.getKeyGroup(),
                        propertyTO.getPropertyKey(),
                        propertyTO.getPropertyValue());
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setPropertyValue(P property, String value) {
        property.setPropertyValue(value);
    }
}