package com.communote.server.core.common.exceptions;

/**
 * @author Communote Team - <a href="https://github.com/Communote">https://github.com/Communote</a>
 */
public class PasswordLengthException extends PasswordValidationException {

    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = -8822567868836579664L;
    private final int requiredMinLength;
    private final int actualLength;

    /**
     * Constructs a new exception
     *
     * @param requiredMinLength
     *            the required minimum length the password needs to have
     * @param actualLength
     *            the length the password had
     */
    public PasswordLengthException(int requiredMinLength, int actualLength) {
        this("Password has less than " + requiredMinLength + " characters", requiredMinLength,
                actualLength);
    }

    /**
     * Constructs a new exception
     *
     * @param message
     *            a details message
     * @param requiredMinLength
     *            the required minimum length the password needs to have
     * @param actualLength
     *            the length the password had
     */
    public PasswordLengthException(String message, int requiredMinLength, int actualLength) {
        super(message);
        this.requiredMinLength = requiredMinLength;
        this.actualLength = actualLength;
    }

    /**
     * @return the length the password had
     */
    public int getActualLength() {
        return actualLength;
    }

    /**
     *
     * @return the required minimum length the password needs to have
     */
    public int getRequiredMinLength() {
        return requiredMinLength;
    }

}
