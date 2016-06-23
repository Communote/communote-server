package com.communote.server.persistence.user.invitationfields;

import java.util.Map;

import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;

import com.communote.common.validation.EmailValidator;
import com.communote.server.persistence.user.InvitationField;
import com.communote.server.persistence.user.InviteUserForm;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class EmailInvitationField extends InvitationField {

    /** Instance of this field */
    public final static EmailInvitationField INSTANCE = new EmailInvitationField();

    /**
     * EMAIL
     */
    private EmailInvitationField() {
        super("EMAIL", "email");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void putIntoQueryData(InviteUserForm form, Map<InvitationField, String> queryData) {
        queryData.put(this, form.getEmail());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validate(InviteUserForm form, Errors errors) {
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "email", "error.valueEmpty");
        if (!errors.hasFieldErrors("email")
                && !EmailValidator.validateEmailAddressByRegex(form.getEmail())) {
            errors.rejectValue("email", "error.email.not.valid",
                    "The entered email address is not valid!");
        }
    }

}
