package com.communote.server.persistence.blog;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ExportFormat implements java.io.Serializable {
    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = 5871395737145604275L;

    /**
     * 
     */
    public static final ExportFormat RTF = new ExportFormat("rtf");

    /**
     * 
     */
    public static final ExportFormat PDF = new ExportFormat("pdf");

    /**
     * 
     */
    public static final ExportFormat XML = new ExportFormat("xml");

    /**
     * 
     */
    public static final ExportFormat CSV = new ExportFormat("csv");

    /**
     * 
     */
    public static final ExportFormat HTML = new ExportFormat("html");

    private static final java.util.Map<String, ExportFormat> values = new java.util.HashMap<String, ExportFormat>(
            5, 1);

    private static java.util.List<String> literals = new java.util.ArrayList<String>(5);

    private static java.util.List<String> names = new java.util.ArrayList<String>(5);

    /**
     * Initializes the values.
     */
    static {
        values.put(RTF.value, RTF);
        literals.add(RTF.value);
        names.add("RTF");
        values.put(PDF.value, PDF);
        literals.add(PDF.value);
        names.add("PDF");
        values.put(XML.value, XML);
        literals.add(XML.value);
        names.add("XML");
        values.put(CSV.value, CSV);
        literals.add(CSV.value);
        names.add("CSV");
        values.put(HTML.value, HTML);
        literals.add(HTML.value);
        names.add("HTML");
        literals = java.util.Collections.unmodifiableList(literals);
        names = java.util.Collections.unmodifiableList(names);
    }

    /**
     * Creates an instance of ExportFormat from <code>value</code>.
     *
     * @param value
     *            the value to create the ExportFormat from.
     */
    public static ExportFormat fromString(String value) {
        final ExportFormat typeValue = values.get(value);
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
    protected ExportFormat() {
    }

    private ExportFormat(String value) {
        this.value = value;
    }

    /**
     * @see Comparable#compareTo(Object)
     */
    public int compareTo(Object that) {
        return (this == that) ? 0 : this.getValue().compareTo(((ExportFormat) that).getValue());
    }

    /**
     * @see Object#equals(Object)
     */
    public boolean equals(Object object) {
        return (this == object)
                || (object instanceof ExportFormat && ((ExportFormat) object).getValue().equals(
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
        return ExportFormat.fromString(this.value);
    }

    /**
     * @see Object#toString()
     */
    public String toString() {
        return String.valueOf(value);
    }
}