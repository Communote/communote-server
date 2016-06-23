package com.communote.server.persistence.user.invitationfields;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;

import com.communote.common.validation.EmailValidator;
import com.communote.server.core.user.helper.ValidationPatterns;
import com.communote.server.persistence.user.InvitationField;
import com.communote.server.persistence.user.InviteUserForm;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class EmailAliasInvitationField extends InvitationField {

    /** Instance of this field */
    public final static EmailAliasInvitationField INSTANCE = new EmailAliasInvitationField();

    /**
     * EMAILALIAS
     */
    private EmailAliasInvitationField() {
        super("EMAILALIAS", "emailAlias");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void putIntoQueryData(InviteUserForm form, Map<InvitationField, String> queryData) {
        String formValue = form.getEmailAlias();
        if (StringUtils.isNotEmpty(formValue)) {
            if (formValue.contains("@")) {
                queryData.put(EmailInvitationField.INSTANCE, formValue);
            } else {
                queryData.put(AliasInvitationField.INSTANCE, formValue);
            }
            queryData.put(EmailAliasInvitationField.INSTANCE, formValue);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validate(InviteUserForm form, Errors errors) {
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "emailAlias", "error.valueEmpty");
        if (!errors.hasFieldErrors("emailAlias")) {
            if (form.getEmailAlias().contains("@")) {
                if (!EmailValidator.validateEmailAddressByRegex(form.getEmailAlias())) {
                    errors.rejectValue("emailAlias", "error.email.not.valid",
                            "The entered email address is not valid!");
                }
            } else {
                if (!form.getEmailAlias().matches(ValidationPatterns.PATTERN_ALIAS)) {
                    errors.rejectValue("emailAlias", "error.alias.not.valid",
                            "The entered login is not valid!");
                }
            }
        }
    }
}
