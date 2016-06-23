package com.communote.server.web.fe.portal;

import java.util.HashMap;
import java.util.Map;

/**
 * abstract bean to be used in forms that provides basic functionality
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
@Deprecated
public abstract class AbstractFormBean {

    private final Map<String, String> errors = new HashMap<String, String>();
    private String formName;

    /**
     * add new error message to internal message stack
     *
     * @param key
     *            the addressed form field
     * @param message
     *            the message
     */

    public void addErrorMessage(String key, String message) {

        // add new error field or append to existing field
        if (errors.containsKey(key)) {

            errors.put(errors.get(key) + " " + key, message);
        } else {

            errors.put(key, message);
        }
    }

    /**
     * @return returns true if there are some errors on the stack, otherwise false
     */

    public boolean errorsOccured() {

        return (errors.size() > 0);
    }

    /**
     * check if there are errors for a certain form field
     *
     * @param key
     *            the name of the form field
     * @return true if there are some errors
     */

    public boolean errorsOccured(String key) {

        return (getErrorMessage(key) != null);
    }

    /**
     * @return all error messages, mapped by form fields
     */

    public Map<String, String> getAllErrorMessages() {

        return errors;
    }

    /**
     * get the error message for a certain form field
     *
     * @param key
     *            the name of the form field
     * @return the message for this form field
     */

    public String getErrorMessage(String key) {

        return errors.get(key);
    }

    /**
     * @return returns the transmitted name of the form
     */

    public String getFormName() {

        return formName;
    }

    /**
     * abstract hook for after-validation action
     *
     * @return true if action successful
     */

    abstract public boolean performAction();

    /**
     * set the form name of this form on incoming request. This indicated that the form was sent.
     * This method is only used as bean setter function by JSP
     *
     * @param formName
     *            the name of the form
     */

    public void setFormName(String formName) {

        this.formName = formName;
    }

    /**
     * abstract hook for validation logic
     *
     * @return if validation successful true, otherwise false
     */

    abstract public boolean validate();

    /**
     * @return returns true if the form was sent. The indicator for that is a non-empty form name
     */

    public boolean wasSent() {

        return (formName != null);
    }
}
