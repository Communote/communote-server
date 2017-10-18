package com.communote.server.core.mail.messages.user;

import java.util.Map;

import com.communote.server.core.mail.messages.MailModelPlaceholderConstants;
import com.communote.server.core.mail.messages.SecurityCodeMailMessage;
import com.communote.server.model.security.ChannelType;
import com.communote.server.model.user.User;
import com.communote.server.model.user.security.UnlockUserSecurityCode;


/**
 * Represents a user locked mail
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class UserLockedMailMessage extends SecurityCodeMailMessage {

    private final ChannelType channel;
    private final UnlockUserSecurityCode securityCode;
    private final User receiver;

    /**
     * Construct a user-locked mail message
     * 
     * @param receiver
     *            The one who is locked by system
     * @param channel
     *            type of channel
     * @param securityCode
     *            The code which needs confirmation by the receiver
     */
    public UserLockedMailMessage(User receiver, ChannelType channel,
            UnlockUserSecurityCode securityCode) {
        super("mail.message.user.user-locked", receiver.getLanguageLocale(), receiver);
        this.receiver = receiver;
        this.channel = channel;
        this.securityCode = securityCode;
    }

    @Override
    public void prepareModel(Map<String, Object> model) {
        model.put(MailModelPlaceholderConstants.USER, receiver);
        model.put(MailModelPlaceholderConstants.LOCKED_CHANNEL, channel.getValue());
        model.put(MailModelPlaceholderConstants.CONFIRMATION_LINK,
                getSecurityCodeConfirmationLink(securityCode));
    }
}
