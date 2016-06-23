package com.communote.server.persistence.user.invitationfields;

import java.util.Map;

import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;

import com.communote.server.persistence.user.InvitationField;
import com.communote.server.persistence.user.InviteUserForm;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ExternalUsernameInvitationField extends InvitationField {

    /** Instance of this field */
    public final static ExternalUsernameInvitationField INSTANCE = new ExternalUsernameInvitationField();

    /**
     * EXTERNAL_USERNAME
     */
    private ExternalUsernameInvitationField() {
        super("EXTERNAL_USERNAME", "externalUsername");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void putIntoQueryData(InviteUserForm form, Map<InvitationField, String> queryData) {
        queryData.put(this, form.getExternalUsername());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validate(InviteUserForm form, Errors errors) {
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "externalUsername", "error.valueEmpty");
    }
}
