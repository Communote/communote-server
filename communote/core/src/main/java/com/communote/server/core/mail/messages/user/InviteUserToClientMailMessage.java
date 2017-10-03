package com.communote.server.core.mail.messages.user;

import java.util.Locale;
import java.util.Map;

import javax.mail.MessagingException;

import org.springframework.mail.javamail.MimeMessageHelper;

import com.communote.server.core.mail.messages.MailModelPlaceholderConstants;
import com.communote.server.core.mail.messages.SecurityCodeMailMessage;
import com.communote.server.model.security.SecurityCode;
import com.communote.server.model.user.User;


/**
 * Mail to invite a new user
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class InviteUserToClientMailMessage extends SecurityCodeMailMessage {

    private final User inviter;
    private final SecurityCode securityCode;

    /**
     * Construct an invitation mail message.
     * 
     * @param inviter
     *            the user sending the invitation
     * @param recipientEmail
     *            email address of the user to invite
     * @param locale
     *            the locale of the user
     * @param code
     *            the code the security code
     */
    public InviteUserToClientMailMessage(User inviter, Locale locale, String recipientEmail,
            SecurityCode code) {
        super("mail.message.user.invite-user-to-client", locale);
        this.inviter = inviter;
        this.securityCode = code;
        this.addTo(recipientEmail);
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
        model.put(MailModelPlaceholderConstants.USER, inviter);
        model.put(MailModelPlaceholderConstants.CONFIRMATION_LINK,
                getSecurityCodeConfirmationLink(securityCode));
    }
}
