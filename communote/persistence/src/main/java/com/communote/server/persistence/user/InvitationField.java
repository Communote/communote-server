package com.communote.server.persistence.user;

import java.util.Map;

import org.springframework.validation.Errors;

/**
 * Abstraction for invitation fields.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public abstract class InvitationField {

    private final String name;
    private final String errorField;

    /**
     * @param name
     *            Name of this field.
     * @param errorField
     *            Name of the error field.
     */
    public InvitationField(String name, String errorField) {
        this.name = name;
        this.errorField = errorField;
    }

    /**
     * @return The name of the error field.
     */
    public String getErrorField() {
        return errorField;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param form
     *            The form.
     * @param queryData
     *            The query data to append.
     */
    public abstract void putIntoQueryData(InviteUserForm form,
            Map<InvitationField, String> queryData);

    /**
     * @param form
     *            The form to validate.
     * @param errors
     *            The errors object.
     */
    public abstract void validate(InviteUserForm form, Errors errors);
}
