package com.communote.server.persistence.user.invitationfields;

import java.util.Map;

import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;

import com.communote.server.core.user.helper.ValidationPatterns;
import com.communote.server.persistence.user.InvitationField;
import com.communote.server.persistence.user.InviteUserForm;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class AliasInvitationField extends InvitationField {

    /** Instance of this field */
    public final static AliasInvitationField INSTANCE = new AliasInvitationField();

    /**
     * Alias.
     */
    private AliasInvitationField() {
        super("ALIAS", "alias");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void putIntoQueryData(InviteUserForm form, Map<InvitationField, String> queryData) {
        queryData.put(this, form.getAlias());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validate(InviteUserForm form, Errors errors) {
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "alias", "error.valueEmpty");
        if (!errors.hasFieldErrors("alias")
                && !form.getAlias().matches(ValidationPatterns.PATTERN_ALIAS)) {
            errors.rejectValue("alias", "error.alias.not.valid", "The entered login is not valid!");
        }
    }
}
