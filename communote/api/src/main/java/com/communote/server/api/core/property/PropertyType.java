package com.communote.server.api.core.property;

/**
 * The available property types
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public enum PropertyType {
    /**
     * A property which is associated with a user
     */
    UserProperty,
    /**
     * A property which is associated with a group
     */
    EntityGroupProperty,
    /**
     * A property which is associated with a blog/topic
     */
    BlogProperty,
    /**
     * Property type for a note
     */
    NoteProperty,
    /**
     * Binary Property
     */
    BinaryProperty,
    /**
     * A property which is associated with a note and a user
     */
    UserNoteProperty,
    /**
     * A property which is associated with an external object
     */
    ExternalObjectProperty,
    /**
     * A property which is associated with an attachment
     */
    AttachmentProperty
}
