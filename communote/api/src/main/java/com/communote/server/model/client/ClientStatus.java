package com.communote.server.model.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ClientStatus implements java.io.Serializable {
    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = 6989147740626816939L;

    /**
     * <p>
     * The client has been created and is active.
     * </p>
     */
    public static final ClientStatus ACTIVE = new ClientStatus("ACTIVE");

    private static final Map<String, ClientStatus> values = new HashMap<>(1, 1);
    private static final Map<String, ClientStatus> extendedValues = new HashMap<>();

    private static List<String> literals = new ArrayList<String>(1);

    /**
     * Initializes the values.
     */
    static {
        values.put(ACTIVE.value, ACTIVE);
        literals.add(ACTIVE.value);

        literals = Collections.unmodifiableList(literals);
    }

    private static ClientStatus addStatus(String value) {
        synchronized (extendedValues) {
            ClientStatus status = extendedValues.get(value);
            if (status == null) {
                status = new ClientStatus(value);
                extendedValues.put(value, status);
                ArrayList<String> newLiterals = new ArrayList<String>(literals);
                newLiterals.add(value);
                literals = Collections.unmodifiableList(newLiterals);
            }
            return status;
        }
    }

    /**
     * Define a new client status type
     *
     * @param value
     *            the value of the status
     * @return the added status
     */
    public static ClientStatus define(String value) {
        ClientStatus status = values.get(value);
        if (status == null) {
            status = extendedValues.get(status);
            if (status == null) {
                status = addStatus(value);
            }
        }
        return status;
    }

    /**
     * Creates an instance of ClientStatus from <code>value</code>.
     *
     * @param value
     *            the value to create the ClientStatus from.
     */
    public static ClientStatus fromString(String value) {
        ClientStatus typeValue = values.get(value);
        if (typeValue == null) {
            typeValue = extendedValues.get(value);
            if (typeValue == null) {
                throw new IllegalArgumentException("invalid value '" + value
                        + "', possible values are: " + literals);
            }
        }
        return typeValue;
    }

    /**
     * Returns an unmodifiable list containing the literals that are known by this enumeration.
     *
     * @return A List containing the actual literals defined by this enumeration, this list can not
     *         be modified.
     */
    public static List<String> literals() {
        return literals;
    }

    /**
     * Remove a status type that was previously added via {@link #define(String)}. After undefining
     * the type fromString will throw an exception if the status to resolve is not known. The
     * built-in types cannot be undefined.
     *
     * @param status
     *            the status to remove
     */
    public static void undefine(ClientStatus status) {
        synchronized (extendedValues) {
            if (extendedValues.remove(status.getValue()) != null) {
                ArrayList<String> newLiterals = new ArrayList<String>(literals);
                newLiterals.remove(status.value);
                literals = Collections.unmodifiableList(newLiterals);
            }
        }
    }

    private String value;

    /**
     * The default constructor allowing super classes to access it.
     */
    protected ClientStatus() {
    }

    private ClientStatus(String value) {
        this.value = value;
    }

    /**
     * @see Comparable#compareTo(Object)
     */
    public int compareTo(Object that) {
        return (this == that) ? 0 : this.getValue().compareTo(((ClientStatus) that).getValue());
    }

    /**
     * @see Object#equals(Object)
     */
    @Override
    public boolean equals(Object object) {
        return (this == object)
                || (object instanceof ClientStatus && ((ClientStatus) object).getValue().equals(
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
        return ClientStatus.fromString(this.value);
    }

    /**
     * @see Object#toString()
     */
    @Override
    public String toString() {
        return String.valueOf(value);
    }
}