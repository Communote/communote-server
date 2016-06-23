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
public class FirstnameInvitationField extends InvitationField {
    /** Instance of this field */
    public final static FirstnameInvitationField INSTANCE = new FirstnameInvitationField();

    /**
     * FIRSTNAME
     */
    private FirstnameInvitationField() {
        super("FIRSTNAME", "firstname");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void putIntoQueryData(InviteUserForm form, Map<InvitationField, String> queryData) {
        queryData.put(this, form.getFirstName());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validate(InviteUserForm form, Errors errors) {
        ValidationHelper.validateString("firstName", errors, ValidationPatterns.PATTERN_FIRSTNAME,
                ValidationPatterns.FIRSTNAME_LOWER_BOUND, ValidationPatterns.FIRSTNAME_UPPER_BOUND);
    }
}
