package com.communote.server.core.mail.messages.user;

import java.util.Collection;
import java.util.Locale;
import java.util.Map;

import com.communote.server.core.mail.messages.MailMessage;
import com.communote.server.core.mail.messages.MailModelPlaceholderConstants;
import com.communote.server.model.user.User;

/**
 * Mail message for notifying the client manager that the limit of active user account is reached by
 * now or next.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class NotifyUserCountLimitReachedMailMessage extends MailMessage {

    private final String clientName;
    private final boolean userActivationChanged;
    private final int userCountPercent;
    private final String userCountLimit;

    /**
     * Construct a new mail message for notifying the client manager.
     *
     * @param receivers
     *            A list of all set client manager
     * @param clientName
     *            The Name of the client for which this message should be sent
     * @param userActivationHasChanged
     *            Has the user activation setting been changed due the current active user accounts
     *            limit has been reached.
     * @param userCountAsPercent
     *            the current count of active user accounts in percent
     * @param userCntLimit
     *            the current limit of active user accounts
     * @param locale
     *            the locale
     */
    public NotifyUserCountLimitReachedMailMessage(Collection<User> receivers, Locale locale,
            String clientName, boolean userActivationHasChanged, int userCountAsPercent,
            String userCntLimit) {
        super("mail.message.user.notify-user-count-limit", locale, receivers
                .toArray(new User[receivers.size()]));
        this.clientName = clientName;
        this.userActivationChanged = userActivationHasChanged;
        this.userCountPercent = userCountAsPercent;
        this.userCountLimit = userCntLimit;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void prepareModel(Map<String, Object> model) {
        model.put(MailModelPlaceholderConstants.Client.CLIENT, clientName);
        model.put(MailModelPlaceholderConstants.USER_COUNT_LIMIT, userCountLimit);
        model.put(MailModelPlaceholderConstants.AUTOMATIC_ACTIVATION_CHANGED, userActivationChanged);
        model.put(MailModelPlaceholderConstants.USER_COUNT_PERCENT, userCountPercent);
    }
}
