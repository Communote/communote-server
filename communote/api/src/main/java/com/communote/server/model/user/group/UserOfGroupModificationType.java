package com.communote.server.model.user.group;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class UserOfGroupModificationType implements java.io.Serializable {
    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = 1185673512561169320L;

    /**
     * 
     */
    public static final UserOfGroupModificationType ADD = new UserOfGroupModificationType("ADD");

    /**
     * 
     */
    public static final UserOfGroupModificationType REMOVE = new UserOfGroupModificationType(
            "REMOVE");

    private static final java.util.Map<String, UserOfGroupModificationType> values = new java.util.HashMap<String, UserOfGroupModificationType>(
            2, 1);

    private static java.util.List<String> literals = new java.util.ArrayList<String>(2);

    private static java.util.List<String> names = new java.util.ArrayList<String>(2);

    /**
     * Initializes the values.
     */
    static {
        values.put(ADD.value, ADD);
        literals.add(ADD.value);
        names.add("ADD");
        values.put(REMOVE.value, REMOVE);
        literals.add(REMOVE.value);
        names.add("REMOVE");
        literals = java.util.Collections.unmodifiableList(literals);
        names = java.util.Collections.unmodifiableList(names);
    }

    /**
     * Creates an instance of UserOfGroupModificationType from <code>value</code>.
     *
     * @param value
     *            the value to create the UserOfGroupModificationType from.
     */
    public static UserOfGroupModificationType fromString(String value) {
        final UserOfGroupModificationType typeValue = values.get(value);
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
    protected UserOfGroupModificationType() {
    }

    private UserOfGroupModificationType(String value) {
        this.value = value;
    }

    /**
     * @see Comparable#compareTo(Object)
     */
    public int compareTo(Object that) {
        return (this == that) ? 0 : this.getValue().compareTo(
                ((UserOfGroupModificationType) that).getValue());
    }

    /**
     * @see Object#equals(Object)
     */
    public boolean equals(Object object) {
        return (this == object)
                || (object instanceof UserOfGroupModificationType && ((UserOfGroupModificationType) object)
                        .getValue().equals(this.getValue()));
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
        return UserOfGroupModificationType.fromString(this.value);
    }

    /**
     * @see Object#toString()
     */
    public String toString() {
        return String.valueOf(value);
    }
}