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
     * @param receivers
     *            List of receivers the receiver.
     */
    public ActivateUserForManagerMailMessage(User activatedUser, Locale locale,
            boolean firstActivation, Collection<User> receivers) {
        super(firstActivation ? ACTIVATION_EMAIL_TEMPLATE : REACTIVATION_EMAIL_TEMPLATE, locale,
                receivers.toArray(new User[receivers.size()]));
        Assert.notNull(activatedUser, "Receiver cannot be null");
        this.activatedUser = activatedUser;
        Assert.notEmpty(receivers, "at least one receiver must be defined");
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.communote.server.service.mail.messages.AbstractKenmeiMailMessage#prepareModel()
     */
    @Override
    protected void prepareModel(Map<String, Object> model) {
        model.put("client", ClientHelper.getCurrentClient().getName());
        model.put(MailModelPlaceholderConstants.USER, activatedUser);
        model.put(MailModelPlaceholderConstants.Client.USER_PROFILE_LINK,
                ClientUrlHelper.renderConfiguredAbsoluteUrl(
                        MailMessage.ACTIVATION_LINK_PREFIX, true)
                        + "?userId=" + activatedUser.getId());
    }
}
