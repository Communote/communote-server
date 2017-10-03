package com.communote.server.core.mail.messages.user;

import java.util.Map;

import com.communote.server.core.mail.messages.MailModelPlaceholderConstants;
import com.communote.server.core.mail.messages.SecurityCodeMailMessage;
import com.communote.server.core.security.SecurityHelper;
import com.communote.server.core.user.UserManagement.RegistrationType;
import com.communote.server.model.security.SecurityCode;
import com.communote.server.model.user.User;


/**
 * Mail message for confirming a user
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ConfirmUserMailMessage extends SecurityCodeMailMessage {

    private final User userToConfirm;
    private final SecurityCode securityCode;
    private final RegistrationType type;

    /**
     * Construct a new mail message for user confirmation
     * 
     * @param userToConfirm
     *            the user to confirm
     * @param securityCode
     *            The securityCode
     * @param type
     *            The type of the registration.
     */
    public ConfirmUserMailMessage(User userToConfirm, SecurityCode securityCode,
            RegistrationType type) {
        super(RegistrationType.SELF.equals(type)
                ? "mail.message.user.registration-confirm"
                : "mail.message.user.invite-user-to-client",
                userToConfirm.getLanguageLocale(), userToConfirm);
        this.userToConfirm = userToConfirm;
        this.securityCode = securityCode;
        this.type = type;
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.communote.server.core.mail.messages.MailMessage#getLinkPrefix()
     */
    @Override
    public String getLinkPrefix() {
        return "/user/confirm.do";
    }

    @Override
    public void prepareModel(Map<String, Object> model) {
        model.put(MailModelPlaceholderConstants.USER,
                RegistrationType.SELF.equals(type)
                        ? userToConfirm : SecurityHelper.assertCurrentKenmeiUser());
        String confirmationLink = getSecurityCodeConfirmationLink(securityCode);
        model.put(MailModelPlaceholderConstants.CONFIRMATION_LINK, confirmationLink);
    }
}
