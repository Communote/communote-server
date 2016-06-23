package com.communote.server.web.fe.portal.controller;

import com.communote.common.i18n.LocalizedMessage;

/**
 * Holds the results of the confirmation of a security code.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class SecurityCodeConfirmationResult {

    private final boolean success;
    private LocalizedMessage successMessage;
    private LocalizedMessage errorMessage;

    private LocalizedMessage warningMessage;

    /**
     * Create a new result
     *
     * @param success
     *            whether the confirmation was successful
     * @param message
     *            if success is true the success message will be set to this value otherwise it will
     *            be stored as the error message
     */
    public SecurityCodeConfirmationResult(boolean success, LocalizedMessage message) {
        this.success = success;
        if (this.success) {
            this.successMessage = message;
        } else {
            this.errorMessage = message;
        }
    }

    /**
     * @return the error message. Is only relevant if {@link #isSuccess()} returns false.
     */
    public LocalizedMessage getErrorMessage() {
        return errorMessage;
    }

    /**
     * @return the success message. Is only relevant if {@link #isSuccess()} returns true and no
     *         warning message exists.
     */
    public LocalizedMessage getSuccessMessage() {
        return successMessage;
    }

    /**
     * @return the warning message. Is only relevant if {@link #isSuccess()} returns true.
     */
    public LocalizedMessage getWarningMessage() {
        return warningMessage;
    }

    /**
     * @return whether the code confirmation was successful
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Set a message that describes what went wrong during confirmation of the code
     * 
     * @param errorMessage
     *            the error message
     */
    public void setErrorMessage(LocalizedMessage errorMessage) {
        this.errorMessage = errorMessage;
    }

    /**
     * Set a message to give some feedback about the successful code confirmation
     * 
     * @param successMessage
     *            the success message
     */
    public void setSuccessMessage(LocalizedMessage successMessage) {
        this.successMessage = successMessage;
    }

    /**
     * Set a message with warning. This message will be shown instead of the success message if set
     * and the confirmation was successful.
     * 
     * @param warningMessage
     *            the warning message
     */
    public void setWarningMessage(LocalizedMessage warningMessage) {
        this.warningMessage = warningMessage;
    }
}
