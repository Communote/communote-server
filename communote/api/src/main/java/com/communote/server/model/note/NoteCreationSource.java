package com.communote.server.model.note;

/**
 * <p>
 * Possible types of creation sources for user tagged items
 * </p>
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class NoteCreationSource implements java.io.Serializable {
    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = -2475148832926396790L;

    /**
     * <p>
     * Created via mail-in
     * </p>
     */
    public static final NoteCreationSource MAIL = new NoteCreationSource("MAIL");

    /**
     * <p>
     * Created via XMPP
     * </p>
     */
    public static final NoteCreationSource XMPP = new NoteCreationSource("XMPP");

    /**
     * <p>
     * Created via web interface
     * </p>
     */
    public static final NoteCreationSource WEB = new NoteCreationSource("WEB");

    /**
     * <p>
     * Automatically created by the system
     * </p>
     */
    public static final NoteCreationSource SYSTEM = new NoteCreationSource("SYSTEM");

    /**
     * <p>
     * Created by the API
     * </p>
     */
    public static final NoteCreationSource API = new NoteCreationSource("API");

    /**
     * <p>
     * Created by the Message Queue
     * </p>
     */
    public static final NoteCreationSource MQ = new NoteCreationSource("MQ");

    private static final java.util.Map<String, NoteCreationSource> values = new java.util.HashMap<String, NoteCreationSource>(
            6, 1);

    private static java.util.List<String> literals = new java.util.ArrayList<String>(6);

    private static java.util.List<String> names = new java.util.ArrayList<String>(6);

    /**
     * Initializes the values.
     */
    static {
        values.put(MAIL.value, MAIL);
        literals.add(MAIL.value);
        names.add("MAIL");
        values.put(XMPP.value, XMPP);
        literals.add(XMPP.value);
        names.add("XMPP");
        values.put(WEB.value, WEB);
        literals.add(WEB.value);
        names.add("WEB");
        values.put(SYSTEM.value, SYSTEM);
        literals.add(SYSTEM.value);
        names.add("SYSTEM");
        values.put(API.value, API);
        literals.add(API.value);
        names.add("API");
        values.put(MQ.value, MQ);
        literals.add(MQ.value);
        names.add("MQ");
        literals = java.util.Collections.unmodifiableList(literals);
        names = java.util.Collections.unmodifiableList(names);
    }

    /**
     * Creates an instance of NoteCreationSource from <code>value</code>.
     *
     * @param value
     *            the value to create the NoteCreationSource from.
     */
    public static NoteCreationSource fromString(String value) {
        final NoteCreationSource typeValue = values.get(value);
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
    protected NoteCreationSource() {
    }

    private NoteCreationSource(String value) {
        this.value = value;
    }

    /**
     * @see Comparable#compareTo(Object)
     */
    public int compareTo(Object that) {
        return (this == that) ? 0 : this.getValue().compareTo(
                ((NoteCreationSource) that).getValue());
    }

    /**
     * @see Object#equals(Object)
     */
    public boolean equals(Object object) {
        return (this == object)
                || (object instanceof NoteCreationSource && ((NoteCreationSource) object)
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
        return NoteCreationSource.fromString(this.value);
    }

    /**
     * @see Object#toString()
     */
    public String toString() {
        return String.valueOf(value);
    }
}