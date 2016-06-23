package com.communote.server.core.filter;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class SortType implements java.io.Serializable {
    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = 7969908217316960553L;

    /**
     *
     */
    public static final SortType ALPHA = new SortType("ALPHA");

    /**
     *
     */
    public static final SortType COUNT = new SortType("COUNT");

    private static final java.util.Map<String, SortType> values = new java.util.HashMap<String, SortType>(
            2, 1);

    private static java.util.List<String> literals = new java.util.ArrayList<String>(2);

    private static java.util.List<String> names = new java.util.ArrayList<String>(2);

    /**
     * Initializes the values.
     */
    static {
        values.put(ALPHA.value, ALPHA);
        literals.add(ALPHA.value);
        names.add("ALPHA");
        values.put(COUNT.value, COUNT);
        literals.add(COUNT.value);
        names.add("COUNT");
        literals = java.util.Collections.unmodifiableList(literals);
        names = java.util.Collections.unmodifiableList(names);
    }

    /**
     * Creates an instance of SortType from <code>value</code>.
     *
     * @param value
     *            the value to create the SortType from.
     */
    public static SortType fromString(String value) {
        final SortType typeValue = values.get(value);
        if (typeValue == null) {
            throw new IllegalArgumentException("invalid value '" + value
                    + "', possible values are: " + literals);
        }
        return typeValue;
    }

    /**
     * Returns an unmodifiable list containing the literals that are known by this enumeration.
     *
     * @return A List containing the actual literals defined by this enumeration, this list can not
     *         be modified.
     */
    public static java.util.List<String> literals() {
        return literals;
    }

    /**
     * Returns an unmodifiable list containing the names of the literals that are known by this
     * enumeration.
     *
     * @return A List containing the actual names of the literals defined by this enumeration, this
     *         list can not be modified.
     */
    public static java.util.List<String> names() {
        return names;
    }

    private String value;

    /**
     * The default constructor allowing super classes to access it.
     */
    protected SortType() {
    }

    private SortType(String value) {
        this.value = value;
    }

    /**
     * @see Comparable#compareTo(Object)
     */
    public int compareTo(Object that) {
        return (this == that) ? 0 : this.getValue().compareTo(((SortType) that).getValue());
    }

    /**
     * @see Object#equals(Object)
     */
    @Override
    public boolean equals(Object object) {
        return (this == object)
                || (object instanceof SortType && ((SortType) object).getValue().equals(
                        this.getValue()));
    }

    /**
     * Gets the underlying value of this type safe enumeration.
     *
     * @return the underlying value.
     */
    public String getValue() {
        return this.value;
    }

    /**
     * @see Object#hashCode()
     */
    @Override
    public int hashCode() {
        return this.getValue().hashCode();
    }

    /**
     * This method allows the deserialization of an instance of this enumeration type to return the
     * actual instance that will be the singleton for the JVM in which the current thread is
     * running.
     * <p/>
     * Doing this will allow users to safely use the equality operator <code>==</code> for
     * enumerations because a regular deserialized object is always a newly constructed instance and
     * will therefore never be an existing reference; it is this <code>readResolve()</code> method
     * which will intercept the deserialization process in order to return the proper singleton
     * reference.
     * <p/>
     * This method is documented here: <a
     * href="http://java.sun.com/j2se/1.3/docs/guide/serialization/spec/input.doc6.html">Java Object
     * Serialization Specification</a>
     */
    private Object readResolve() throws java.io.ObjectStreamException {
        return SortType.fromString(this.value);
    }

    /**
     * @see Object#toString()
     */
    @Override
    public String toString() {
        return String.valueOf(value);
    }
}