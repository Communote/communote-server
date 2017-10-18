package com.communote.server.core.mail.messages.user;

import java.util.Map;

import com.communote.server.core.mail.messages.MailModelPlaceholderConstants;
import com.communote.server.core.mail.messages.SecurityCodeMailMessage;
import com.communote.server.model.user.User;
import com.communote.server.model.user.security.UserSecurityCode;


/**
 * Remind user mail message for email confirmation.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class RemindUserRegistrationMailMessage extends SecurityCodeMailMessage {

    private final User receiver;
    private final UserSecurityCode securityCode;

    /**
     * Instantiates a new remind user registration mail message.
     * 
     * @param receiver
     *            the receiver
     * @param securityCode
     *            the code to use
     */
    public RemindUserRegistrationMailMessage(User receiver, UserSecurityCode securityCode) {
        super("mail.message.user.remind-user-registration", receiver.getLanguageLocale(), receiver);
        this.receiver = receiver;
        this.securityCode = securityCode;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getLinkPrefix() {
        return "/user/confirm.do";
    }

    @Override
    public void prepareModel(Map<String, Object> model) {
        model.put(MailModelPlaceholderConstants.USER, receiver);
        String confirmationLink = getSecurityCodeConfirmationLink(securityCode);
        model.put(MailModelPlaceholderConstants.CONFIRMATION_LINK, confirmationLink);
    }
}
