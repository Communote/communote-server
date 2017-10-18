package com.communote.server.core.mail.messages;

import java.util.Map;

import com.communote.server.model.user.User;
import com.communote.server.model.user.security.ForgottenPasswordSecurityCode;
import com.communote.server.persistence.user.client.ClientHelper;

/**
 * Mail to send to users who forgot their password.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ForgottenPWMailMessage extends SecurityCodeMailMessage {

    private final ForgottenPasswordSecurityCode securityCode;

    /**
     * Construct a new password link
     * 
     * @param recipient
     *            The one who forgot the password
     * @param securityCode
     *            The code which needs confirmation by the receiver
     */
    public ForgottenPWMailMessage(User recipient, ForgottenPasswordSecurityCode securityCode) {
        super("mail.message.send-new-pw", recipient.getLanguageLocale(), recipient);
        this.securityCode = securityCode;
    }

    @Override
    public void prepareModel(Map<String, Object> model) {
        model.put("client.name", ClientHelper.getCurrentClient().getName());
        model.put(MailModelPlaceholderConstants.CONFIRMATION_LINK,
                getSecurityCodeConfirmationLink(securityCode));
    }
}
