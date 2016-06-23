package com.communote.server.model.user;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class UserRole implements java.io.Serializable {
    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = -1168874152823863841L;

    /**
     * 
     */
    public static final UserRole ROLE_KENMEI_USER = new UserRole("ROLE_KENMEI_USER");

    /**
     * 
     */
    public static final UserRole ROLE_KENMEI_CLIENT_MANAGER = new UserRole(
            "ROLE_KENMEI_CLIENT_MANAGER");

    /**
     * <p>
     * A system user for internal tasks or for the use of external systems
     * </p>
     */
    public static final UserRole ROLE_SYSTEM_USER = new UserRole("ROLE_SYSTEM_USER");

    /**
     * 
     */
    public static final UserRole ROLE_CRAWL_USER = new UserRole("ROLE_CRAWL_USER");

    private static final java.util.Map<String, UserRole> values = new java.util.HashMap<String, UserRole>(
            4, 1);

    private static java.util.List<String> literals = new java.util.ArrayList<String>(4);

    private static java.util.List<String> names = new java.util.ArrayList<String>(4);

    /**
     * Initializes the values.
     */
    static {
        values.put(ROLE_KENMEI_USER.value, ROLE_KENMEI_USER);
        literals.add(ROLE_KENMEI_USER.value);
        names.add("ROLE_KENMEI_USER");
        values.put(ROLE_KENMEI_CLIENT_MANAGER.value, ROLE_KENMEI_CLIENT_MANAGER);
        literals.add(ROLE_KENMEI_CLIENT_MANAGER.value);
        names.add("ROLE_KENMEI_CLIENT_MANAGER");
        values.put(ROLE_SYSTEM_USER.value, ROLE_SYSTEM_USER);
        literals.add(ROLE_SYSTEM_USER.value);
        names.add("ROLE_SYSTEM_USER");
        values.put(ROLE_CRAWL_USER.value, ROLE_CRAWL_USER);
        literals.add(ROLE_CRAWL_USER.value);
        names.add("ROLE_CRAWL_USER");
        literals = java.util.Collections.unmodifiableList(literals);
        names = java.util.Collections.unmodifiableList(names);
    }

    /**
     * Creates an instance of UserRole from <code>value</code>.
     *
     * @param value
     *            the value to create the UserRole from.
     */
    public static UserRole fromString(String value) {
        final UserRole typeValue = values.get(value);
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
    protected UserRole() {
    }

    private UserRole(String value) {
        this.value = value;
    }

    /**
     * @see Comparable#compareTo(Object)
     */
    public int compareTo(Object that) {
        return (this == that) ? 0 : this.getValue().compareTo(((UserRole) that).getValue());
    }

    /**
     * @see Object#equals(Object)
     */
    public boolean equals(Object object) {
        return (this == object)
                || (object instanceof UserRole && ((UserRole) object).getValue().equals(
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
        return UserRole.fromString(this.value);
    }

    /**
     * @see Object#toString()
     */
    public String toString() {
        return String.valueOf(value);
    }
}