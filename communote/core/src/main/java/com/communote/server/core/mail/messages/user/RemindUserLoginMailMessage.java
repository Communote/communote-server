package com.communote.server.core.mail.messages.user;

import java.util.Map;

import com.communote.server.core.mail.messages.MailMessage;
import com.communote.server.core.mail.messages.MailModelPlaceholderConstants;
import com.communote.server.model.user.User;


/**
 * Remind user mail message that the user is activated but has not logged in yet.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class RemindUserLoginMailMessage extends MailMessage {

    private final User receiver;

    /**
     * Instantiates a new remind user login mail message.
     * 
     * @param receiver
     *            the receiver
     */
    public RemindUserLoginMailMessage(User receiver) {
        super("mail.message.user.remind-user-login", receiver.getLanguageLocale(), receiver);
        this.receiver = receiver;
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.communote.server.core.mail.messages.MailMessage#prepareModel()
     */
    @Override
    protected void prepareModel(Map<String, Object> model) {
        model.put(MailModelPlaceholderConstants.USER, receiver);
    }
}
