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
 * Invites an user to the tagging server
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class InviteUserToClientMailMessage extends SecurityCodeMailMessage {

    private final User inviter;
    private final String receiverEmail;
    private final SecurityCode securityCode;

    /**
     * Construct an invitation mail message.
     * 
     * @param inviter
     *            the user who sends the invitation
     * @param receiverEmail
     *            the one to invite
     * @param locale
     *            the locale the locale of the user
     * @param code
     *            the code the security code
     */
    public InviteUserToClientMailMessage(User inviter, Locale locale, String receiverEmail,
            SecurityCode code) {
        super("mail.message.user.invite-user-to-client", locale);
        this.inviter = inviter;
        this.receiverEmail = receiverEmail;
        this.securityCode = code;
    }

    /**
     * {@inheritDoc}
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
        model.put(MailModelPlaceholderConstants.USER, inviter);
        model.put(MailModelPlaceholderConstants.CONFIRMATION_LINK,
                getSecurityCodeConfirmationLink(securityCode));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setReceivers(MimeMessageHelper message) throws MessagingException {
        message.addTo(receiverEmail);
    }

}
