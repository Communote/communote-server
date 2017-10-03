package com.communote.server.core.mail.messages.fetching;

import java.util.Locale;
import java.util.Map;

import com.communote.server.core.mail.messages.MailMessage;
import com.communote.server.core.user.UserManagementHelper;


/**
 * Mail message to be sent when email based posting failed because of the sender not being a member
 * of the addressed client.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class UserNotInClientMailMessage extends MailMessage {


    /**
     * @param senderEmailAddress
     *            email address of the sender who is not a user of the client
     */
    public UserNotInClientMailMessage(String senderEmailAddress) {
        this(senderEmailAddress, UserManagementHelper.getFallbackLocale());
    }

    /**
     * 
     * @param senderEmailAddress
     *            email address of the sender who is not a user of the client
     * @param locale
     *            the locale to use
     */
    public UserNotInClientMailMessage(String senderEmailAddress, Locale locale) {
        super("mail.message.fetching.user-not-in-client", locale);
        this.addTo(senderEmailAddress);
    }

    /**
     * Does nothing.
     * 
     * {@inheritDoc}
     */
    @Override
    public void prepareModel(Map<String, Object> model) {
    }
}
