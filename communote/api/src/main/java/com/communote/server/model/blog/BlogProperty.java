package com.communote.server.model.blog;

import com.communote.server.model.property.StringProperty;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class BlogProperty extends StringProperty {
    /**
     * Constructs new instances of {@link BlogProperty}.
     */
    public static final class Factory {
        /**
         * Constructs a new instance of {@link BlogProperty}.
         */
        public static BlogProperty newInstance() {
            return new BlogProperty();
        }

        /**
         * Constructs a new instance of {@link BlogProperty}, taking all possible properties (except
         * the identifier(s))as arguments.
         */
        public static BlogProperty newInstance(String propertyValue, String keyGroup,
                String propertyKey, java.util.Date lastModificationDate) {
            final BlogProperty entity = new BlogProperty();
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
    private static final long serialVersionUID = 5714601908283499698L;

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