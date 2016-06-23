package com.communote.server.model.property;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class GlobalProperty extends StringProperty {
    /**
     * Constructs new instances of {@link GlobalProperty}.
     */
    public static final class Factory {
        /**
         * Constructs a new instance of {@link GlobalProperty}.
         */
        public static GlobalProperty newInstance() {
            return new GlobalProperty();
        }

        /**
         * Constructs a new instance of {@link GlobalProperty}, taking all possible properties
         * (except the identifier(s))as arguments.
         */
        public static GlobalProperty newInstance(String propertyValue, String keyGroup,
                String propertyKey, java.util.Date lastModificationDate) {
            final GlobalProperty entity = new GlobalProperty();
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
    private static final long serialVersionUID = -7036169465446481280L;

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