package com.communote.server.model.user;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class UserStatus implements java.io.Serializable {
    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = 8762567792355814578L;

    /**
     *
     */
    public static final UserStatus REGISTERED = new UserStatus("REGISTERED");

    /**
     *
     */
    public static final UserStatus CONFIRMED = new UserStatus("CONFIRMED");

    /**
     *
     */
    public static final UserStatus ACTIVE = new UserStatus("ACTIVE");

    /**
     * <p>
     * The user was temporarily disabled by the client manager and can be re-activated by the client
     * manager.
     * </p>
     */
    public static final UserStatus TEMPORARILY_DISABLED = new UserStatus("TEMPORARILY_DISABLED");

    /**
     *
     */
    public static final UserStatus DELETED = new UserStatus("DELETED");

    /**
     *
     */
    public static final UserStatus INVITED = new UserStatus("INVITED");

    /**
     * <p>
     * The user decided to leave the system or was disabled by the client manager. All his data is
     * kept but he cannot login anymore. The account cannot be re-activated.
     * </p>
     */
    public static final UserStatus PERMANENTLY_DISABLED = new UserStatus("PERMANENTLY_DISABLED");

    /**
     *
     */
    public static final UserStatus TERMS_NOT_ACCEPTED = new UserStatus("TERMS_NOT_ACCEPTED");

    private static final java.util.Map<String, UserStatus> values = new java.util.HashMap<String, UserStatus>(
            8, 1);

    private static java.util.List<String> literals = new java.util.ArrayList<String>(8);

    private static java.util.List<String> names = new java.util.ArrayList<String>(8);

    /**
     * Initializes the values.
     */
    static {
        values.put(REGISTERED.value, REGISTERED);
        literals.add(REGISTERED.value);
        names.add("REGISTERED");
        values.put(CONFIRMED.value, CONFIRMED);
        literals.add(CONFIRMED.value);
        names.add("CONFIRMED");
        values.put(ACTIVE.value, ACTIVE);
        literals.add(ACTIVE.value);
        names.add("ACTIVE");
        values.put(TEMPORARILY_DISABLED.value, TEMPORARILY_DISABLED);
        literals.add(TEMPORARILY_DISABLED.value);
        names.add("TEMPORARILY_DISABLED");
        values.put(DELETED.value, DELETED);
        literals.add(DELETED.value);
        names.add("DELETED");
        values.put(INVITED.value, INVITED);
        literals.add(INVITED.value);
        names.add("INVITED");
        values.put(PERMANENTLY_DISABLED.value, PERMANENTLY_DISABLED);
        literals.add(PERMANENTLY_DISABLED.value);
        names.add("PERMANENTLY_DISABLED");
        values.put(TERMS_NOT_ACCEPTED.value, TERMS_NOT_ACCEPTED);
        literals.add(TERMS_NOT_ACCEPTED.value);
        names.add("TERMS_NOT_ACCEPTED");
        literals = java.util.Collections.unmodifiableList(literals);
        names = java.util.Collections.unmodifiableList(names);
    }

    /**
     * Creates an instance of UserStatus from <code>value</code>.
     *
     * @param value
     *            the value to create the UserStatus from.
     */
    public static UserStatus fromString(String value) {
        final UserStatus typeValue = values.get(value);
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
    protected UserStatus() {
    }

    private UserStatus(String value) {
        this.value = value;
    }

    /**
     * @see Comparable#compareTo(Object)
     */
    public int compareTo(Object that) {
        return (this == that) ? 0 : this.getValue().compareTo(((UserStatus) that).getValue());
    }

    /**
     * @see Object#equals(Object)
     */
    @Override
    public boolean equals(Object object) {
        return (this == object)
                || (object instanceof UserStatus && ((UserStatus) object).getValue().equals(
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
        return UserStatus.fromString(this.value);
    }

    /**
     * @see Object#toString()
     */
    @Override
    public String toString() {
        return String.valueOf(value);
    }
}