package com.communote.server.core.mail.messages.user;

import java.io.UnsupportedEncodingException;
import java.util.Map;

import javax.mail.MessagingException;

import org.springframework.mail.javamail.MimeMessageHelper;

import com.communote.server.core.mail.messages.MailModelPlaceholderConstants;
import com.communote.server.core.mail.messages.SecurityCodeMailMessage;
import com.communote.server.model.user.User;
import com.communote.server.model.user.security.EmailSecurityCode;

/**
 * Represents an email validation messages
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ConfirmEmailAddressMessage extends SecurityCodeMailMessage {

    private final User receiver;
    private final EmailSecurityCode code;

    /**
     * Construct a new mail message for email confirmation
     * 
     * @param receiver
     *            The one who gets confirmed
     * @param code
     *            The email security code#
     * @param changedThroughAdmin
     *            Set to true, if the address was changed through an admin and not the user itself.
     */
    public ConfirmEmailAddressMessage(User receiver, EmailSecurityCode code) {
        super("mail.message.user.validate-email-address", receiver.getLanguageLocale());
        this.receiver = receiver;
        this.code = code;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void prepareModel(Map<String, Object> model) {
        model.put(MailModelPlaceholderConstants.NEW_EMAIL_ADDRESS, code.getNewEmailAddress());
        model.put(MailModelPlaceholderConstants.USER, receiver);
        model.put(MailModelPlaceholderConstants.CONFIRMATION_LINK,
                getSecurityCodeConfirmationLink(code));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setReceivers(MimeMessageHelper message) throws MessagingException,
            UnsupportedEncodingException {
        addReceiver(message, code.getNewEmailAddress(), receiver);
    }
}
