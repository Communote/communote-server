package com.communote.server.persistence.user.invitationfields;

import java.util.Map;

import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;

import com.communote.server.persistence.user.InvitationField;
import com.communote.server.persistence.user.InviteUserForm;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class LanguageCodeInvitationField extends InvitationField {
    /** Instance of this field */
    public final static LanguageCodeInvitationField INSTANCE = new LanguageCodeInvitationField();

    /**
     * LANGUAGECODE
     */
    private LanguageCodeInvitationField() {
        super("LANGUAGECODE", "languageCode");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void putIntoQueryData(InviteUserForm form, Map<InvitationField, String> queryData) {
        queryData.put(this, form.getLanguageCode());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validate(InviteUserForm form, Errors errors) {
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "languageCode", "error.valueEmpty");
    }
}
