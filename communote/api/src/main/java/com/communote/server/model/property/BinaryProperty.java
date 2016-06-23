package com.communote.server.model.property;

/**
 * Property which holds binary data. This property is not associated with one of the entities (Note,
 * Topic, ...).
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class BinaryProperty extends Property {
    /**
     * Constructs new instances of {@link BinaryProperty}.
     */
    public static final class Factory {
        /**
         * Constructs a new instance of {@link BinaryProperty}.
         */
        public static BinaryProperty newInstance() {
            return new BinaryProperty();
        }

        /**
         * Constructs a new instance of {@link BinaryProperty}, taking all possible properties
         * (except the identifier(s))as arguments.
         */
        public static BinaryProperty newInstance(byte[] propertyValue, String keyGroup,
                String propertyKey, java.util.Date lastModificationDate) {
            final BinaryProperty entity = new BinaryProperty();
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
    private static final long serialVersionUID = -1306531576504738884L;

    private byte[] propertyValue;

    /**
     * Builds a string showing the current attribute values
     */
    @Override
    public String attributesToString() {
        StringBuilder sb = new StringBuilder();

        sb.append("class='");
        sb.append(this.getClass().getName());
        sb.append("', ");

        sb.append("propertyValue='");
        sb.append(propertyValue);
        sb.append("', ");

        sb.append(super.attributesToString());

        return sb.toString();
    }

    /**
     * This entity does not have any identifiers but since it extends the <code>Property</code>
     * class it will simply delegate the call up there.
     *
     * @see Property#equals(Object)
     */
    @Override
    public boolean equals(Object object) {
        return super.equals(object);
    }

    /**
     *
     */
    public byte[] getPropertyValue() {
        return this.propertyValue;
    }

    /**
     * This entity does not have any identifiers but since it extends the <code>Property</code>
     * class it will simply delegate the call up there.
     *
     * @see Property#hashCode()
     */
    @Override
    public int hashCode() {
        return super.hashCode();
    }

    public void setPropertyValue(byte[] propertyValue) {
        this.propertyValue = propertyValue;
    }

}