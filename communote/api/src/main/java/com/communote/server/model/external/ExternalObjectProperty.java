package com.communote.server.model.external;

import java.util.Date;

import com.communote.server.model.property.StringProperty;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ExternalObjectProperty extends StringProperty {
    /**
     * Constructs new instances of {@link ExternalObjectProperty}.
     */
    public static final class Factory {
        /**
         * Constructs a new instance of {@link ExternalObjectProperty}.
         */
        public static ExternalObjectProperty newInstance() {
            return new ExternalObjectProperty();
        }

        /**
         * Constructs a new instance of {@link ExternalObjectProperty}, taking all possible
         * properties (except the identifier(s))as arguments.
         */
        public static ExternalObjectProperty newInstance(String propertyValue, String keyGroup,
                String propertyKey, Date lastModificationDate) {
            final ExternalObjectProperty entity = new ExternalObjectProperty();
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
    private static final long serialVersionUID = 775001064955161307L;

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