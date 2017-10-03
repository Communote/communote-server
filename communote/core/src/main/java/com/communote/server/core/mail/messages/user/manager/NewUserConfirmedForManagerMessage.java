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
     * Instantiates a new mail message.
     * 
     * @param confirmedUser
     *            the user who confirmed her/his account
     * @param managers
     *            the client managers to inform about the confirmed account
     * @param locale
     *            the locale
     */
    public NewUserConfirmedForManagerMessage(User confirmedUser,
            Collection<User> managers, Locale locale) {
        super("mail.message.user.user-confirmed-manager", locale, managers);
        Assert.notNull(confirmedUser, "a confirmed user must be defined");
        Assert.notEmpty(managers, "At least one client manager should be defined");
        this.confirmedUser = confirmedUser;
    }

    /**
     * This method returns the activation link for the confirmed user to be used by a client
     * manager.
     * 
     * @return the activation link
     */
    private String getUserProfileLink() {
        return ClientUrlHelper
                .renderConfiguredAbsoluteUrl(MailMessage.ACTIVATION_LINK_PREFIX, true)
                + "?userId=" + confirmedUser.getId();
    }

    @Override
    public void prepareModel(Map<String, Object> model) {
        model.put(MailModelPlaceholderConstants.USER, confirmedUser);
        model.put(MailModelPlaceholderConstants.Client.USER_PROFILE_LINK, getUserProfileLink());
    }
}
