package com.communote.server.core.mail.messages;

import java.io.UnsupportedEncodingException;
import java.util.Map;

import javax.mail.MessagingException;

import org.springframework.mail.javamail.MimeMessageHelper;

import com.communote.server.model.user.User;
import com.communote.server.model.user.security.ForgottenPasswordSecurityCode;
import com.communote.server.persistence.user.client.ClientHelper;


/**
 * Represents a forgotten password mail
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ForgottenPWMailMessage extends SecurityCodeMailMessage {

    private final String receiverEmail;
    private final ForgottenPasswordSecurityCode securityCode;

    /**
     * Construct a new password link
     * 
     * @param receiver
     *            The one who forgot the password
     * @param securityCode
     *            The code which needs confirmation by the receiver
     */
    public ForgottenPWMailMessage(User receiver, ForgottenPasswordSecurityCode securityCode) {
        super("mail.message.send-new-pw", receiver.getLanguageLocale());
        this.securityCode = securityCode;
        this.receiverEmail = receiver.getEmail();
    }

    @Override
    protected void prepareModel(Map<String, Object> model) {
        model.put("client.name", ClientHelper.getCurrentClient().getName());
        model.put(MailModelPlaceholderConstants.CONFIRMATION_LINK,
                getSecurityCodeConfirmationLink(securityCode));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setReceivers(MimeMessageHelper message) throws MessagingException,
            UnsupportedEncodingException {
        message.addTo(receiverEmail);
    }
}
