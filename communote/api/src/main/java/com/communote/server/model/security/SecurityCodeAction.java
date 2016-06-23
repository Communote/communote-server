package com.communote.server.model.security;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The action of a security code. If more actions than the predefined ones are needed new actions
 * can be defined via {@link #define(String)}
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class SecurityCodeAction implements java.io.Serializable {
    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = 1714265566204965809L;

    /**
     *
     */
    public static final SecurityCodeAction CONFIRM_USER = new SecurityCodeAction("CONFIRM_USER");

    /**
     *
     */
    public static final SecurityCodeAction CONFIRM_EMAIL = new SecurityCodeAction("CONFIRM_EMAIL");

    /**
     *
     */
    public static final SecurityCodeAction INVITE_USER = new SecurityCodeAction("INVITE_USER");

    /**
     *
     */
    public static final SecurityCodeAction REQUEST_MEMBERSHIP = new SecurityCodeAction(
            "REQUEST_MEMBERSHIP");

    /**
     *
     */
    public static final SecurityCodeAction FORGOTTEN_PASSWORD = new SecurityCodeAction(
            "FORGOTTEN_PASSWORD");

    /**
     *
     */
    public static final SecurityCodeAction CONFIRM_CLIENT_INVITATION = new SecurityCodeAction(
            "CONFIRM_CLIENT_INVITATION");

    /**
     *
     */
    public static final SecurityCodeAction INVITE_CLIENT = new SecurityCodeAction("INVITE_CLIENT");

    /**
     *
     */
    public static final SecurityCodeAction REMOVE_CLIENT = new SecurityCodeAction("REMOVE_CLIENT");

    /**
     *
     */
    public static final SecurityCodeAction UNLOCK_USER = new SecurityCodeAction("UNLOCK_USER");

    private static final Map<String, SecurityCodeAction> values = new HashMap<>(9, 1);
    private static final Map<String, SecurityCodeAction> extendedValues = new HashMap<>();

    private static List<String> literals = new ArrayList<>(9);

    /**
     * Initializes the values.
     */
    static {
        values.put(CONFIRM_USER.value, CONFIRM_USER);
        literals.add(CONFIRM_USER.value);
        values.put(CONFIRM_EMAIL.value, CONFIRM_EMAIL);
        literals.add(CONFIRM_EMAIL.value);
        values.put(INVITE_USER.value, INVITE_USER);
        literals.add(INVITE_USER.value);
        values.put(REQUEST_MEMBERSHIP.value, REQUEST_MEMBERSHIP);
        literals.add(REQUEST_MEMBERSHIP.value);
        values.put(FORGOTTEN_PASSWORD.value, FORGOTTEN_PASSWORD);
        literals.add(FORGOTTEN_PASSWORD.value);
        values.put(CONFIRM_CLIENT_INVITATION.value, CONFIRM_CLIENT_INVITATION);
        literals.add(CONFIRM_CLIENT_INVITATION.value);
        values.put(INVITE_CLIENT.value, INVITE_CLIENT);
        literals.add(INVITE_CLIENT.value);
        values.put(REMOVE_CLIENT.value, REMOVE_CLIENT);
        literals.add(REMOVE_CLIENT.value);
        values.put(UNLOCK_USER.value, UNLOCK_USER);
        literals.add(UNLOCK_USER.value);
        literals = Collections.unmodifiableList(literals);
    }

    private static SecurityCodeAction addAction(String value) {
        synchronized (extendedValues) {
            SecurityCodeAction action = extendedValues.get(value);
            if (action == null) {
                action = new SecurityCodeAction(value);
                extendedValues.put(value, action);
                ArrayList<String> newLiterals = new ArrayList<String>(literals);
                newLiterals.add(value);
                literals = Collections.unmodifiableList(newLiterals);
            }
            return action;
        }
    }

    /**
     * Define a new security code action
     *
     * @param value
     *            the value of the action
     * @return the added action
     */
    public static SecurityCodeAction define(String value) {
        SecurityCodeAction action = values.get(value);
        if (action == null) {
            action = extendedValues.get(action);
            if (action == null) {
                action = addAction(value);
            }
        }
        return action;
    }

    /**
     * Creates an instance of SecurityCodeAction from <code>value</code>.
     *
     * @param value
     *            the value to create the SecurityCodeAction from.
     * @throws IllegalArgumentException
     *             in case the value is not among the literals of the built-in or additionally
     *             defined actions
     */
    public static SecurityCodeAction fromString(String value) {
        SecurityCodeAction typeValue = values.get(value);
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
     * Returns an unmodifiable list containing the built-in and additionally defined literals.
     *
     * @return A List containing the actual defined literals, this list can not be modified.
     */
    public static List<String> literals() {
        return literals;
    }

    /**
     * Remove an action that was previously added via {@link #define(String)}. After undefining an
     * action fromString will throw an exception if the action to resolve is not known. The built-in
     * types cannot be undefined.
     *
     * @param action
     *            the action to remove
     */
    public static void undefine(SecurityCodeAction action) {
        synchronized (extendedValues) {
            if (extendedValues.remove(action.getValue()) != null) {
                ArrayList<String> newLiterals = new ArrayList<String>(literals);
                newLiterals.remove(action.value);
                literals = Collections.unmodifiableList(newLiterals);
            }
        }
    }

    private String value;

    /**
     * The default constructor allowing super classes to access it.
     */
    protected SecurityCodeAction() {
    }

    private SecurityCodeAction(String value) {
        this.value = value;
    }

    /**
     * @see Comparable#compareTo(Object)
     */
    public int compareTo(Object that) {
        return (this == that) ? 0 : this.getValue().compareTo(
                ((SecurityCodeAction) that).getValue());
    }

    /**
     * @see Object#equals(Object)
     */
    @Override
    public boolean equals(Object object) {
        return (this == object)
                || (object instanceof SecurityCodeAction && ((SecurityCodeAction) object)
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
        return SecurityCodeAction.fromString(this.value);
    }

    /**
     * @see Object#toString()
     */
    @Override
    public String toString() {
        return String.valueOf(value);
    }
}