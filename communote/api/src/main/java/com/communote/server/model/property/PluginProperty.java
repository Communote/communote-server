package com.communote.server.model.property;

import java.util.Date;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class PluginProperty extends StringProperty {
    /**
     * Constructs new instances of {@link PluginProperty}.
     */
    public static final class Factory {
        /**
         * Constructs a new instance of {@link PluginProperty}.
         */
        public static PluginProperty newInstance() {
            return new PluginProperty();
        }

        /**
         * Constructs a new instance of {@link PluginProperty}, taking all possible properties
         * (except the identifier(s))as arguments.
         */
        public static PluginProperty newInstance(boolean applicationProperty, String propertyValue,
                String keyGroup, String propertyKey, Date lastModificationDate) {
            final PluginProperty entity = new PluginProperty();
            entity.setApplicationProperty(applicationProperty);
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
    private static final long serialVersionUID = -7177393338465155164L;

    private boolean applicationProperty = false;

    /**
     * Builds a string showing the current attribute values
     */
    @Override
    public String attributesToString() {
        StringBuilder sb = new StringBuilder();

        sb.append("class='");
        sb.append(this.getClass().getName());
        sb.append("', ");

        sb.append("applicationProperty='");
        sb.append(applicationProperty);
        sb.append("', ");

        sb.append(super.attributesToString());

        return sb.toString();
    }

    /**
     * <p>
     *
     * @return The symbolic name (same as keyGroup).
     *         </p>
     */
    public String getSymbolicName() {
        return getKeyGroup();
    }

    /**
     *
     */
    public boolean isApplicationProperty() {
        return this.applicationProperty;
    }

    public void setApplicationProperty(boolean applicationProperty) {
        this.applicationProperty = applicationProperty;
    }

    /**
     * <p>
     * Sets the symbolic name (same as keyGroup).
     * </p>
     */
    public void setSymbolicName(String symbolicName) {
        setKeyGroup(symbolicName);
    }

}