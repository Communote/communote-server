package com.communote.server.model.user.group;

import java.util.Date;

import com.communote.server.model.property.StringProperty;

/**
 * A property that is associated with a group.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class GroupProperty extends StringProperty {
    /**
     * Constructs new instances of {@link GroupProperty}.
     */
    public static final class Factory {
        /**
         * Constructs a new instance of {@link GroupProperty}.
         */
        public static GroupProperty newInstance() {
            return new GroupProperty();
        }

        /**
         * Constructs a new instance of {@link GroupProperty}, taking all possible properties
         * (except the identifier(s))as arguments.
         */
        public static GroupProperty newInstance(String propertyValue, String keyGroup,
                String propertyKey, Date lastModificationDate) {
            final GroupProperty entity = new GroupProperty();
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
    private static final long serialVersionUID = 3943415440226303803L;

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