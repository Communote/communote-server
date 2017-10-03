package com.communote.server.core.mail.messages.user;

import java.util.Locale;
import java.util.Map;

import com.communote.server.core.mail.messages.MailModelPlaceholderConstants;
import com.communote.server.core.mail.messages.SecurityCodeMailMessage;
import com.communote.server.model.user.User;


/**
 * Mail to invite a user from an external user repository
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class InviteUserToClientWithExternalAuthMailMessage extends SecurityCodeMailMessage {

    private final User inviter;


    /**
     * Construct an invitation mail message.
     * 
     * @param inviter
     *            the user sending the invitation
     * @param locale
     *            the locale
     * @param recipientEmail
     *            email address of the one to invite
     */
    public InviteUserToClientWithExternalAuthMailMessage(User inviter, Locale locale,
            String recipientEmail) {
        super("mail.message.user.invite-user-to-client-with-external-authentication", locale);
        this.inviter = inviter;
        this.addTo(recipientEmail);
    }

    @Override
    public String getLinkPrefix() {
        return "/admin/client/confirmuserinvitation.do";
    }

    @Override
    public void prepareModel(Map<String, Object> model) {
        model.put(MailModelPlaceholderConstants.USER, inviter);
    }
}
