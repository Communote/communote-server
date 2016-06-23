package com.communote.server.model.attachment;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class AttachmentStatus implements java.io.Serializable {
    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = 2785954006304016699L;

    /**
     * <p>
     * The attachment has been uploaded but not assigned to a note yet.
     * </p>
     */
    public static final AttachmentStatus UPLOADED = new AttachmentStatus("UPLOADED");

    /**
     * <p>
     * The attachment has been published and assigned to a note.
     * </p>
     */
    public static final AttachmentStatus PUBLISHED = new AttachmentStatus("PUBLISHED");

    /**
     * <p>
     * The note of the attachment has been deleted or will be. Have this attachment and the
     * associated file be deleted as well.
     * </p>
     */
    public static final AttachmentStatus MARKED_FOR_DELETION = new AttachmentStatus(
            "MARKED_FOR_DELETION");

    private static final java.util.Map<String, AttachmentStatus> values = new java.util.HashMap<String, AttachmentStatus>(
            3, 1);

    private static java.util.List<String> literals = new java.util.ArrayList<String>(3);

    private static java.util.List<String> names = new java.util.ArrayList<String>(3);

    /**
     * Initializes the values.
     */
    static {
        values.put(UPLOADED.value, UPLOADED);
        literals.add(UPLOADED.value);
        names.add("UPLOADED");
        values.put(PUBLISHED.value, PUBLISHED);
        literals.add(PUBLISHED.value);
        names.add("PUBLISHED");
        values.put(MARKED_FOR_DELETION.value, MARKED_FOR_DELETION);
        literals.add(MARKED_FOR_DELETION.value);
        names.add("MARKED_FOR_DELETION");
        literals = java.util.Collections.unmodifiableList(literals);
        names = java.util.Collections.unmodifiableList(names);
    }

    /**
     * Creates an instance of AttachmentStatus from <code>value</code>.
     *
     * @param value
     *            the value to create the AttachmentStatus from.
     */
    public static AttachmentStatus fromString(String value) {
        final AttachmentStatus typeValue = values.get(value);
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
    protected AttachmentStatus() {
    }

    private AttachmentStatus(String value) {
        this.value = value;
    }

    /**
     * @see Comparable#compareTo(Object)
     */
    public int compareTo(Object that) {
        return (this == that) ? 0 : this.getValue().compareTo(((AttachmentStatus) that).getValue());
    }

    /**
     * @see Object#equals(Object)
     */
    public boolean equals(Object object) {
        return (this == object)
                || (object instanceof AttachmentStatus && ((AttachmentStatus) object).getValue()
                        .equals(this.getValue()));
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
        return AttachmentStatus.fromString(this.value);
    }

    /**
     * @see Object#toString()
     */
    public String toString() {
        return String.valueOf(value);
    }
}