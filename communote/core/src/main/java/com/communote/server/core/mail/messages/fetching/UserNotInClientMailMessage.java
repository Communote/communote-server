package com.communote.server.core.mail.messages.fetching;

import java.io.UnsupportedEncodingException;
import java.util.Locale;
import java.util.Map;

import javax.mail.MessagingException;

import org.springframework.mail.javamail.MimeMessageHelper;

import com.communote.server.core.mail.messages.MailMessage;
import com.communote.server.core.user.UserManagementHelper;


/**
 * Mail message to be sent when email based posting failed because of the sender not being a member
 * of the addressed client.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class UserNotInClientMailMessage extends MailMessage {

    private final String senderEmailAddress;

    /**
     * @param senderEmailAddress
     *            sender of the email with unresolvable user
     */
    public UserNotInClientMailMessage(String senderEmailAddress) {
        this(senderEmailAddress, UserManagementHelper.getFallbackLocale());
    }

    /**
     * 
     * @param senderEmailAddress
     *            sender of the email with unresolvable user
     * @param locale
     *            the locale to use
     */
    public UserNotInClientMailMessage(String senderEmailAddress, Locale locale) {
        super("mail.message.fetching.user-not-in-client", locale);
        this.senderEmailAddress = senderEmailAddress;
    }

    /**
     * Does nothing.
     * 
     * {@inheritDoc}
     */
    @Override
    protected void prepareModel(Map<String, Object> model) {
        // Do nothing.
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setReceivers(MimeMessageHelper message) throws MessagingException,
            UnsupportedEncodingException {
        message.addTo(senderEmailAddress);
    }
}
