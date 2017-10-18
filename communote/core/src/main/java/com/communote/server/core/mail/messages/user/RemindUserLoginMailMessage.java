package com.communote.server.core.mail.messages.user;

import java.util.Map;

import com.communote.server.core.mail.messages.MailMessage;
import com.communote.server.core.mail.messages.MailModelPlaceholderConstants;
import com.communote.server.model.user.User;


/**
 * Mail message to remind a user that he was activated but hasn't logged in yet.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class RemindUserLoginMailMessage extends MailMessage {

    private final User receiver;

    /**
     * Instantiates a new remind user login mail message.
     * 
     * @param recipient
     *            the user to remind
     */
    public RemindUserLoginMailMessage(User recipient) {
        super("mail.message.user.remind-user-login", recipient.getLanguageLocale(), recipient);
        this.receiver = recipient;
    }

    @Override
    public void prepareModel(Map<String, Object> model) {
        model.put(MailModelPlaceholderConstants.USER, receiver);
    }
}
