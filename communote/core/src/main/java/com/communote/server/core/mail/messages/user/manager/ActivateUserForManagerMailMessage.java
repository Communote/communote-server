package com.communote.server.core.mail.messages.user.manager;

import java.util.Collection;
import java.util.Locale;
import java.util.Map;

import org.springframework.util.Assert;

import com.communote.server.core.mail.messages.MailMessage;
import com.communote.server.core.mail.messages.MailModelPlaceholderConstants;
import com.communote.server.model.user.User;
import com.communote.server.persistence.user.client.ClientHelper;
import com.communote.server.persistence.user.client.ClientUrlHelper;


/**
 * Mail Message which will be send to the client managers if a user got activated.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ActivateUserForManagerMailMessage extends MailMessage {

    /** The email template. */
    private final static String ACTIVATION_EMAIL_TEMPLATE = "mail.message.user.user-activation-manager";
    private final static String REACTIVATION_EMAIL_TEMPLATE = "mail.message.user.user-reactivation-manager";

    /** The activated user. */
    private User activatedUser = null;

    /**
     * Instantiates a new activate user mail message.
     * 
     * @param activatedUser
     *            the user who got activated
     * @param locale
     *            the locale
     * @param firstActivation
     *            whether it is the first activation (e.g. after confirmation) or a re-activation
     *            (e.g. after being temporarily disabled)
     * @param recipients
     *            List of recipients of the message
     */
    public ActivateUserForManagerMailMessage(User activatedUser, Locale locale,
            boolean firstActivation, Collection<User> recipients) {
        super(firstActivation ? ACTIVATION_EMAIL_TEMPLATE : REACTIVATION_EMAIL_TEMPLATE, locale,
                recipients);
        Assert.notNull(activatedUser, "Activated user cannot be null");
        this.activatedUser = activatedUser;
        Assert.notEmpty(recipients, "at least one receiver must be defined");
    }

    @Override
    public void prepareModel(Map<String, Object> model) {
        model.put("client", ClientHelper.getCurrentClient().getName());
        model.put(MailModelPlaceholderConstants.USER, activatedUser);
        model.put(MailModelPlaceholderConstants.Client.USER_PROFILE_LINK,
                ClientUrlHelper.renderConfiguredAbsoluteUrl(
                        MailMessage.ACTIVATION_LINK_PREFIX, true)
                        + "?userId=" + activatedUser.getId());
    }
}
