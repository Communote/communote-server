package com.communote.server.model.user;

import java.util.Date;

import com.communote.server.model.property.StringProperty;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class UserProperty extends StringProperty {
    /**
     * Constructs new instances of {@link UserProperty}.
     */
    public static final class Factory {
        /**
         * Constructs a new instance of {@link UserProperty}.
         */
        public static UserProperty newInstance() {
            return new UserProperty();
        }

        /**
         * Constructs a new instance of {@link UserProperty}, taking all possible properties (except
         * the identifier(s))as arguments.
         */
        public static UserProperty newInstance(String propertyValue, String keyGroup,
                String propertyKey, Date lastModificationDate) {
            final UserProperty entity = new UserProperty();
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
    private static final long serialVersionUID = -4376519535123457691L;

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