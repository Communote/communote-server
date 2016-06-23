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

    private final User receiver;
    private final SecurityCode securityCode;
    private final RegistrationType type;

    /**
     * Construct a new mail message for user confirmation
     * 
     * @param receiver
     *            The one who gets confirmed
     * @param securityCode
     *            The securityCode
     * @param type
     *            The type of the registration.
     */
    public ConfirmUserMailMessage(User receiver, SecurityCode securityCode,
            RegistrationType type) {
        super(RegistrationType.SELF.equals(type)
                ? "mail.message.user.registration-confirm"
                : "mail.message.user.invite-user-to-client",
                receiver.getLanguageLocale(), receiver);
        this.receiver = receiver;
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

    /**
     * {@inheritDoc}
     */
    @Override
    protected void prepareModel(Map<String, Object> model) {
        model.put(MailModelPlaceholderConstants.USER,
                RegistrationType.SELF.equals(type)
                        ? receiver : SecurityHelper.assertCurrentKenmeiUser());
        String confirmationLink = getSecurityCodeConfirmationLink(securityCode);
        model.put(MailModelPlaceholderConstants.CONFIRMATION_LINK, confirmationLink);
    }
}
