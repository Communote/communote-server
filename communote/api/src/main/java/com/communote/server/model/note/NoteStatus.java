package com.communote.server.model.note;

/**
 * <p>
 * This enumeration holds all possible states of an UserTaggedItem
 * </p>
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class NoteStatus implements java.io.Serializable {
    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = 3327318412957388952L;

    /**
     * 
     */
    public static final NoteStatus PUBLISHED = new NoteStatus("PUBLISHED");

    /**
     * 
     */
    public static final NoteStatus AUTOSAVED = new NoteStatus("AUTOSAVED");

    private static final java.util.Map<String, NoteStatus> values = new java.util.HashMap<String, NoteStatus>(
            2, 1);

    private static java.util.List<String> literals = new java.util.ArrayList<String>(2);

    private static java.util.List<String> names = new java.util.ArrayList<String>(2);

    /**
     * Initializes the values.
     */
    static {
        values.put(PUBLISHED.value, PUBLISHED);
        literals.add(PUBLISHED.value);
        names.add("PUBLISHED");
        values.put(AUTOSAVED.value, AUTOSAVED);
        literals.add(AUTOSAVED.value);
        names.add("AUTOSAVED");
        literals = java.util.Collections.unmodifiableList(literals);
        names = java.util.Collections.unmodifiableList(names);
    }

    /**
     * Creates an instance of NoteStatus from <code>value</code>.
     *
     * @param value
     *            the value to create the NoteStatus from.
     */
    public static NoteStatus fromString(String value) {
        final NoteStatus typeValue = values.get(value);
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
    protected NoteStatus() {
    }

    private NoteStatus(String value) {
        this.value = value;
    }

    /**
     * @see Comparable#compareTo(Object)
     */
    public int compareTo(Object that) {
        return (this == that) ? 0 : this.getValue().compareTo(((NoteStatus) that).getValue());
    }

    /**
     * @see Object#equals(Object)
     */
    public boolean equals(Object object) {
        return (this == object)
                || (object instanceof NoteStatus && ((NoteStatus) object).getValue().equals(
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
        return NoteStatus.fromString(this.value);
    }

    /**
     * @see Object#toString()
     */
    public String toString() {
        return String.valueOf(value);
    }
}