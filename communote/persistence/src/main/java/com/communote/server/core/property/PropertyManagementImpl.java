package com.communote.server.core.property;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.communote.common.converter.Converter;
import com.communote.server.api.core.common.NotFoundException;
import com.communote.server.api.core.event.EventDispatcher;
import com.communote.server.api.core.property.PropertyManagement;
import com.communote.server.api.core.property.PropertyType;
import com.communote.server.api.core.property.StringPropertyTO;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.model.property.BinaryProperty;
import com.communote.server.model.property.Property;
import com.communote.server.model.property.Propertyable;
import com.communote.server.model.property.StringProperty;
import com.communote.server.model.user.User;

/**
 *
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
@Service("propertyManagement")
@Transactional(propagation = Propagation.REQUIRED)
public class PropertyManagementImpl implements PropertyManagement {

    @Autowired
    private EventDispatcher eventDispatcher;

    private final Map<PropertyType, StringPropertyAccessor<?, ?>> propertyAccessors = new HashMap<PropertyType, StringPropertyAccessor<?, ?>>();

    private BinaryPropertyAccessor binaryPropertyAccessor;

    @Override
    @Transactional(propagation = Propagation.SUPPORTS)
    public void addObjectPropertyFilter(PropertyType propertyType, String keyGroup,
            String propertyKey) {
        StringPropertyAccessor<? extends Propertyable, ? extends StringProperty> accessor = getObjectPropertyAccessor(propertyType);
        accessor.addToFilterDefinition(keyGroup, propertyKey);
    }

    @Override
    public Set<StringPropertyTO> getAllObjectProperties(PropertyType propertyType, Long objectId)
            throws NotFoundException, AuthorizationException {
        StringPropertyAccessor<? extends Propertyable, ? extends StringProperty> accessor = getObjectPropertyAccessor(propertyType);
        return accessor.getAllObjectProperties(objectId);
    }

    @Override
    @Transactional(readOnly = true)
    public BinaryProperty getBinaryProperty(String keyGroup, String key) {
        return binaryPropertyAccessor.handleGetObjectPropertyUnfiltered(
                BinaryPropertyAccessor.DUMMY_OBJECT, keyGroup, key);
    }

    @Override
    @Transactional(readOnly = true)
    public Date getBinaryPropertyLastModificationDate(String keyGroup, String key) {
        return binaryPropertyAccessor.getLastModificationDate(keyGroup, key);
    }

    @Override
    @Transactional(readOnly = true)
    public StringProperty getGlobalObjectProperty(PropertyType propertyType, Long objectId,
            String key) throws NotFoundException, AuthorizationException {
        return getObjectPropertyAccessor(propertyType).getGlobalObjectProperty(objectId, key);
    }

    @Override
    @Transactional(readOnly = true)
    public StringProperty getObjectProperty(PropertyType propertyType, Long objectId,
            String keyGroup, String key) throws NotFoundException, AuthorizationException {
        return getObjectPropertyAccessor(propertyType).getObjectProperty(objectId, keyGroup, key);
    }

    /**
     * Get the {@link PropertyAccessor} for the given {@link PropertyType}. Needs a running
     * transaction.
     *
     * @param propertyType
     *            the property type
     * @return the accessor for the property type
     */
    private StringPropertyAccessor<? extends Propertyable, ? extends Property> getObjectPropertyAccessor(
            PropertyType propertyType) {
        StringPropertyAccessor<? extends Propertyable, ? extends StringProperty> propertyAccessor = propertyAccessors
                .get(propertyType);
        if (propertyAccessor == null) {
            throw new IllegalArgumentException("Illegal Type propertyType=" + propertyType);
        }
        return propertyAccessor;
    }

    @Override
    @Transactional(readOnly = true)
    public StringProperty getObjectPropertyUnfiltered(PropertyType propertyType, Long objectId,
            String keyGroup, String key) throws NotFoundException, AuthorizationException {
        return getObjectPropertyAccessor(propertyType).getObjectPropertyUnfiltered(objectId,
                keyGroup, key);
    }

    @Override
    @Transactional(readOnly = true)
    public <T> Collection<T> getUsersOfProperty(Long noteId, String keyGroup, String key,
            String value, Converter<User, T> converter) throws NotFoundException,
            AuthorizationException {
        return ((UserNotePropertyAccessor) propertyAccessors.get(PropertyType.UserNoteProperty))
                .getUsersOfProperty(noteId, keyGroup, key, value, converter);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasBinaryProperty(String keyGroup, String key) {
        return binaryPropertyAccessor.hasProperty(BinaryPropertyAccessor.DUMMY_OBJECT, keyGroup,
                key);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasUserNoteProperty(Long noteId, String keyGroup, String key, String value)
            throws NotFoundException, AuthorizationException {
        return ((UserNotePropertyAccessor) propertyAccessors.get(PropertyType.UserNoteProperty))
                .hasProperty(noteId, keyGroup, key, value);
    }

    /**
     * init stuff
     */
    @PostConstruct
    public void init() {
        propertyAccessors.put(PropertyType.UserProperty, new UserPropertyAccessor(eventDispatcher));
        propertyAccessors.put(PropertyType.NoteProperty, new NotePropertyAccessor(eventDispatcher));
        propertyAccessors.put(PropertyType.AttachmentProperty, new AttachmentPropertyAccessor(
                eventDispatcher));
        propertyAccessors
        .put(PropertyType.BlogProperty, new TopicPropertyAccessor(eventDispatcher));
        propertyAccessors.put(PropertyType.UserNoteProperty, new UserNotePropertyAccessor(
                eventDispatcher));
        propertyAccessors.put(PropertyType.ExternalObjectProperty,
                new ExternalObjectPropertyAccessor(eventDispatcher));
        binaryPropertyAccessor = new BinaryPropertyAccessor(eventDispatcher);
    }

    @Override
    public void removeBinaryProperty(String keyGroup, String propertyKey) {
        binaryPropertyAccessor.removeProperty(keyGroup, propertyKey);
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS)
    public void removeObjectPropertyFilter(PropertyType propertyType, String keyGroup,
            String propertyKey) {
        StringPropertyAccessor<? extends Propertyable, ? extends StringProperty> accessor = getObjectPropertyAccessor(propertyType);
        accessor.removeFromFilterDefinition(keyGroup, propertyKey);
    }

    @Override
    public BinaryProperty setBinaryProperty(String keyGroup, String key, byte[] value)
            throws AuthorizationException {
        return binaryPropertyAccessor.handleSetObjectProperty(BinaryPropertyAccessor.DUMMY_OBJECT,
                keyGroup, key, value);
    }

    @Override
    public StringProperty setGlobalObjectProperty(PropertyType propertyType, Long objectId,
            String key, String value) throws NotFoundException, AuthorizationException {
        return getObjectPropertyAccessor(propertyType)
                .setGlobalObjectProperty(objectId, key, value);
    }

    @Override
    public void setObjectProperties(PropertyType propertyType, Long objectId,
            Set<StringPropertyTO> properties) throws NotFoundException, AuthorizationException {
        getObjectPropertyAccessor(propertyType).setObjectProperties(objectId, properties);
    }

    // TODO use generics to return the correct property subtype - but why return the property
    // anyway?
    @Override
    public StringProperty setObjectProperty(PropertyType propertyType, Long objectId,
            String keyGroup, String key, String value) throws NotFoundException,
            AuthorizationException {
        return getObjectPropertyAccessor(propertyType).setObjectProperty(objectId, keyGroup, key,
                value);
    }
}
