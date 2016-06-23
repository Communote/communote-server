package com.communote.server.model.property;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class StringProperty extends Property {
    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = -6412312544568867168L;

    private String propertyValue;

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
    public String getPropertyValue() {
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

    public void setPropertyValue(String propertyValue) {
        this.propertyValue = propertyValue;
    }

}