package com.communote.server.core.mail.messages.user;

import java.util.Locale;
import java.util.Map;

import javax.mail.MessagingException;

import org.springframework.mail.javamail.MimeMessageHelper;

import com.communote.server.core.mail.messages.MailModelPlaceholderConstants;
import com.communote.server.core.mail.messages.SecurityCodeMailMessage;
import com.communote.server.model.user.User;


/**
 * Invites an user to the tagging server
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class InviteUserToClientWithExternalAuthMailMessage extends SecurityCodeMailMessage {

    private final User inviter;

    private final String receiverEmail;

    /**
     * Construct an invitation mail message.
     * 
     * @param inviter
     *            the user who sends the invitation
     * @param locale
     *            the locale
     * @param receiverEmail
     *            the one to invite
     */
    public InviteUserToClientWithExternalAuthMailMessage(User inviter, Locale locale,
            String receiverEmail) {
        super("mail.message.user.invite-user-to-client-with-external-authentication", locale);
        this.inviter = inviter;
        this.receiverEmail = receiverEmail;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getLinkPrefix() {
        return "/admin/client/confirmuserinvitation.do";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void prepareModel(Map<String, Object> model) {
        model.put(MailModelPlaceholderConstants.USER, inviter);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setReceivers(MimeMessageHelper message) throws MessagingException {
        message.addTo(receiverEmail);
    }

}
