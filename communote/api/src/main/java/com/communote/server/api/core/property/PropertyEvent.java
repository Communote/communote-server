package com.communote.server.api.core.property;

import com.communote.server.api.core.event.Event;

/**
 * Describes an event of a changed property.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
// TODO make type generic so that the byte[] value of a BinaryProperty can be sent too?
public class PropertyEvent implements Event {

    /**
     * The property event type defines why this event was triggered
     *
     * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
     *
     */
    public enum PropertyEventType {
        /**
         * The property is updated
         */
        UPDATE,
        /**
         * The property is created
         */
        CREATE,
        /**
         * The property is deleted
         */
        DELETE;
    }

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private final String keyGroup;
    private final String key;
    private final PropertyType propertyType;
    private Long userId;
    private final Long objectId;
    private PropertyEventType propertyEventType;
    private String oldValue;
    private String newValue;

    /**
     * @param objectId
     *            the id of the object this property change is referring too. The type of the
     *            property can be inferred from the associated propertyType
     * @param propertyType
     *            the type of the changed property
     *
     * @param keyGroup
     *            the key group
     * @param key
     *            the key of the property
     */
    public PropertyEvent(Long objectId, PropertyType propertyType, String keyGroup, String key) {
        this.objectId = objectId;
        this.propertyType = propertyType;
        this.keyGroup = keyGroup;
        this.key = key;
    }

    /**
     *
     * @return the key of the changed property
     */
    public String getKey() {
        return key;
    }

    /**
     *
     * @return the key group of the changed property
     */
    public String getKeyGroup() {
        return keyGroup;
    }

    /**
     * @return the new value of the changed property, will be null on delete or if it is not a
     *         StringProperty
     */
    public String getNewValue() {
        return newValue;
    }

    /**
     *
     * @return the id of the referring object. The type is defined by {@link #getPropertyType()} For
     *         a {@link PropertyType#UserNoteProperty} this id refers to the noteId. For the userId
     *         use {@link #getUserId()}
     */
    public Long getObjectId() {
        return objectId;
    }

    /**
     *
     * @return the old value of the property, will be null on create or if the property is not a
     *         StringProperty
     */
    public String getOldValue() {
        return oldValue;
    }

    /**
     *
     * @return the type indicating if the property has been created, updated or deleted
     */
    public PropertyEventType getPropertyEventType() {
        return propertyEventType;
    }

    /**
     *
     * @return the type of the property
     */
    public PropertyType getPropertyType() {
        return propertyType;
    }

    /**
     * @return the ID of the user associated with a UserNoteProperty. Will be null if the event was
     *         created for another property type.
     */
    public Long getUserId() {
        return userId;
    }

    /**
     *
     * @param newValue
     *            sets the new value (see {@link #getNewValue()} for more details)
     */
    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }

    /**
     *
     * @param oldValue
     *            sets the old value (see {@link #getOldValue()} for more details)
     */
    public void setOldValue(String oldValue) {
        this.oldValue = oldValue;
    }

    /**
     *
     * @param propertyEventType
     *            sets the type of event
     */
    public void setPropertyEventType(PropertyEventType propertyEventType) {
        this.propertyEventType = propertyEventType;
    }

    /**
     * Set the ID of the user associated with a modified UserNoteProperty
     *
     * @param id
     *            the ID of the user
     */
    public void setUserId(Long id) {
        this.userId = id;
    }

}
