package com.communote.server.model.user;

import java.util.Date;

import com.communote.server.model.property.StringProperty;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class UserNoteProperty extends StringProperty {
    /**
     * Constructs new instances of {@link UserNoteProperty}.
     */
    public static final class Factory {
        /**
         * Constructs a new instance of {@link UserNoteProperty}.
         */
        public static UserNoteProperty newInstance() {
            return new UserNoteProperty();
        }

        /**
         * Constructs a new instance of {@link UserNoteProperty}, taking all possible properties
         * (except the identifier(s))as arguments.
         */
        public static UserNoteProperty newInstance(String propertyValue, String keyGroup,
                String propertyKey, Date lastModificationDate, User user,
                com.communote.server.model.note.Note note) {
            final UserNoteProperty entity = new UserNoteProperty();
            entity.setPropertyValue(propertyValue);
            entity.setKeyGroup(keyGroup);
            entity.setPropertyKey(propertyKey);
            entity.setLastModificationDate(lastModificationDate);
            entity.setUser(user);
            entity.setNote(note);
            return entity;
        }
    }

    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = -6101197622432416520L;

    private User user;

    private com.communote.server.model.note.Note note;

    /**
     * Builds a string showing the current attribute values
     */
    @Override
    public String attributesToString() {
        StringBuilder sb = new StringBuilder();

        sb.append("class='");
        sb.append(this.getClass().getName());
        sb.append("', ");

        sb.append(super.attributesToString());

        return sb.toString();
    }

    /**
     *
     */
    public com.communote.server.model.note.Note getNote() {
        return this.note;
    }

    /**
     *
     */
    public User getUser() {
        return this.user;
    }

    public void setNote(com.communote.server.model.note.Note note) {
        this.note = note;
    }

    public void setUser(User user) {
        this.user = user;
    }

}