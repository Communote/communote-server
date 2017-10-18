package com.communote.server.core.mail.messages.user;

import java.util.Map;

import com.communote.server.core.mail.messages.MailModelPlaceholderConstants;
import com.communote.server.core.mail.messages.SecurityCodeMailMessage;
import com.communote.server.core.user.helper.UserNameHelper;
import com.communote.server.model.user.User;
import com.communote.server.model.user.security.EmailSecurityCode;

/**
 * Mail to validate an email address.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ConfirmEmailAddressMessage extends SecurityCodeMailMessage {

    private final User receiver;
    private final EmailSecurityCode code;

    /**
     * Construct a new mail message for confirming an email address
     * 
     * @param recipient
     *            The user who should confirm the new email address
     * @param code
     *            The security code containing the new email address
     * @param changedThroughAdmin
     *            whether the address was changed by an administrator and not the user himself.
     */
    public ConfirmEmailAddressMessage(User recipient, EmailSecurityCode code) {
        super("mail.message.user.validate-email-address", recipient.getLanguageLocale());
        this.receiver = recipient;
        this.code = code;
        this.addTo(code.getNewEmailAddress(), UserNameHelper.getCompleteSignature(recipient));
    }

    @Override
    public void prepareModel(Map<String, Object> model) {
        model.put(MailModelPlaceholderConstants.NEW_EMAIL_ADDRESS, code.getNewEmailAddress());
        model.put(MailModelPlaceholderConstants.USER, receiver);
        model.put(MailModelPlaceholderConstants.CONFIRMATION_LINK,
                getSecurityCodeConfirmationLink(code));
    }

}
