package com.communote.server.persistence.user.invitationfields;

import java.util.Map;

import org.springframework.validation.Errors;

import com.communote.server.core.helper.ValidationHelper;
import com.communote.server.core.user.helper.ValidationPatterns;
import com.communote.server.persistence.user.InvitationField;
import com.communote.server.persistence.user.InviteUserForm;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class LastnameInvitationField extends InvitationField {
    /** Instance of this field */
    public final static LastnameInvitationField INSTANCE = new LastnameInvitationField();

    /**
     * FIRSTNAME
     */
    private LastnameInvitationField() {
        super("LASTNAME", "lastname");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void putIntoQueryData(InviteUserForm form, Map<InvitationField, String> queryData) {
        queryData.put(this, form.getLastName());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validate(InviteUserForm form, Errors errors) {
        ValidationHelper.validateString("lastName", errors, ValidationPatterns.PATTERN_LASTNAME,
                ValidationPatterns.LASTNAME_LOWER_BOUND, ValidationPatterns.LASTNAME_UPPER_BOUND);
    }
}
