package com.communote.server.model.attachment;

import java.util.Date;

import com.communote.server.model.property.StringProperty;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class AttachmentProperty extends StringProperty {
    /**
     * Constructs new instances of {@link AttachmentProperty}.
     */
    public static final class Factory {
        /**
         * Constructs a new instance of {@link AttachmentProperty}.
         */
        public static AttachmentProperty newInstance() {
            return new AttachmentProperty();
        }

        /**
         * Constructs a new instance of {@link AttachmentProperty}, taking all possible properties
         * (except the identifier(s))as arguments.
         */
        public static AttachmentProperty newInstance(String propertyValue, String keyGroup,
                String propertyKey, Date lastModificationDate) {
            final AttachmentProperty entity = new AttachmentProperty();
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
    private static final long serialVersionUID = -2123441675781533199L;

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