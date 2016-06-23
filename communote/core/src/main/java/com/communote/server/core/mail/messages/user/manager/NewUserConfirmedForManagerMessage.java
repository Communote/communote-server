package com.communote.server.core.mail.messages.user.manager;

import java.util.Collection;
import java.util.Locale;
import java.util.Map;

import org.springframework.util.Assert;

import com.communote.server.core.mail.messages.MailMessage;
import com.communote.server.core.mail.messages.MailModelPlaceholderConstants;
import com.communote.server.model.user.User;
import com.communote.server.persistence.user.client.ClientUrlHelper;


/**
 * If a new user confirmed his/her account and needs activation this email will be send to client.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class NewUserConfirmedForManagerMessage extends MailMessage {

    private final User confirmedUser;

    /**
     * Instantiates a new activate user mail message.
     * 
     * @param confirmedUser
     *            the user who got confirmed
     * @param receivers
     *            the receiver
     * @param locale
     *            the locale
     */
    public NewUserConfirmedForManagerMessage(User confirmedUser,
            Collection<User> receivers, Locale locale) {
        super("mail.message.user.user-confirmed-manager", locale, receivers
                .toArray(new User[receivers.size()]));
        Assert.notNull(confirmedUser, "a confiremd user must be defined");
        Assert.notEmpty(receivers, "At least one receiver should be defined");
        this.confirmedUser = confirmedUser;
    }

    /**
     * This method returns the activation link for the confirmed user to be used by a Kenmei
     * manager.
     * 
     * @return the activation link over a secure channel
     */
    private String getUserProfileLink() {
        return ClientUrlHelper
                .renderConfiguredAbsoluteUrl(MailMessage.ACTIVATION_LINK_PREFIX, true)
                + "?userId=" + confirmedUser.getId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void prepareModel(Map<String, Object> model) {
        model.put(MailModelPlaceholderConstants.USER, confirmedUser);
        model.put(MailModelPlaceholderConstants.Client.USER_PROFILE_LINK, getUserProfileLink());
    }
}
