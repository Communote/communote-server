package com.communote.server.core.messaging;

/**
 * Abstract class for notification definitions.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
// TODO why is this class abstract??
public abstract class NotificationDefinition {
    /** Possible support template types. */
    public enum NotificationTypes {
        /** Play text. */
        PLAIN,
        /** Text can contain html. */
        HTML
    }

    private final String id;

    private final String messageKey;

    /**
     * Constructor.
     * 
     * @param id
     *            Unique id for this definition.
     */
    public NotificationDefinition(String id) {
        this(id, null);
    }

    /**
     * Constructor.
     * 
     * @param id
     *            Unique id for this definition.
     * @param messageKey
     *            Message key for a readable name of this definition. If this is null a default key
     *            will be generated (user.profile.notification.definitions. + id).
     */
    public NotificationDefinition(String id, String messageKey) {
        if (id == null) {
            throw new IllegalArgumentException();
        }
        if (messageKey == null || messageKey.length() == 0) {
            messageKey = "user.profile.notification.definitions." + id;
        }
        this.messageKey = messageKey;
        this.id = id;
    }

    /*
     * (non-Javadoc)
     * 
     * @see Object#equals(Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        NotificationDefinition other = (NotificationDefinition) obj;
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        return true;
    }

    /**
     * @return If of this definition. This must be unique.
     */
    public String getId() {
        return id;
    }

    /**
     * Method to get the message key, which can be used for the message.
     * 
     * @param type
     *            Type of notification.
     * @return getMessageKeyForName() + "." + type.name().toLowerCase() + ".message"
     */
    public String getMessageKeyForMessage(NotificationTypes type) {
        return getMessageKeyForName() + "." + type.name().toLowerCase() + ".message";
    }

    /**
     * @return A message key used to retrieve a name for this definition. This message key also
     *         works as a prefix for other derived messages keys.
     */
    public String getMessageKeyForName() {
        return messageKey;
    }

    /**
     * Method to get the message key, which can be used for a title or subject.
     * 
     * @param type
     *            Type of notification.
     * @return getMessageKeyForMessage() + ".subject"
     */
    public String getMessageKeyForSubject(NotificationTypes type) {
        return getMessageKeyForMessage(type) + ".subject";
    }

    /*
     * (non-Javadoc)
     * 
     * @see Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }
}
