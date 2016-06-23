package com.communote.server.model.note;

import com.communote.server.model.property.StringProperty;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class NoteProperty extends StringProperty {
    /**
     * Constructs new instances of {@link NoteProperty}.
     */
    public static final class Factory {
        /**
         * Constructs a new instance of {@link NoteProperty}.
         */
        public static NoteProperty newInstance() {
            return new NoteProperty();
        }

        /**
         * Constructs a new instance of {@link NoteProperty}, taking all possible properties (except
         * the identifier(s))as arguments.
         */
        public static NoteProperty newInstance(String propertyValue, String keyGroup,
                String propertyKey, java.util.Date lastModificationDate) {
            final NoteProperty entity = new NoteProperty();
            entity.setPropertyValue(propertyValue);
            entity.setKeyGroup(keyGroup);
            entity.setPropertyKey(propertyKey);
            entity.setLastModificationDate(lastModificationDate);
            return entity;
        }
    }

    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = -6005741180646176608L;

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

}