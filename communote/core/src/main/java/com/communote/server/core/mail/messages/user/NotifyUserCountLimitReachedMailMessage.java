package com.communote.server.core.mail.messages.user;

import java.util.Collection;
import java.util.Locale;
import java.util.Map;

import com.communote.server.core.mail.messages.MailMessage;
import com.communote.server.core.mail.messages.MailModelPlaceholderConstants;
import com.communote.server.model.user.User;

/**
 * Mail message for notifying the client manager that the limit of active user accounts is almost
 * reached or has been reached.
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
     * @param managers
     *            a list of client managers to inform
     * @param clientName
     *            The Name of the client for which this message should be sent
     * @param userActivationHasChanged
     *            whether the automatic user activation has been deactivated by the system after
     *            reaching the limit
     * @param userCountAsPercent
     *            the current count of active user accounts in percent
     * @param userCntLimit
     *            the current limit of active user accounts
     * @param locale
     *            the locale
     */
    public NotifyUserCountLimitReachedMailMessage(Collection<User> managers, Locale locale,
            String clientName, boolean userActivationHasChanged, int userCountAsPercent,
            String userCntLimit) {
        super("mail.message.user.notify-user-count-limit", locale, managers);
        this.clientName = clientName;
        this.userActivationChanged = userActivationHasChanged;
        this.userCountPercent = userCountAsPercent;
        this.userCountLimit = userCntLimit;
    }

    @Override
    public void prepareModel(Map<String, Object> model) {
        model.put(MailModelPlaceholderConstants.Client.CLIENT, clientName);
        model.put(MailModelPlaceholderConstants.USER_COUNT_LIMIT, userCountLimit);
        model.put(MailModelPlaceholderConstants.AUTOMATIC_ACTIVATION_CHANGED,
                userActivationChanged);
        model.put(MailModelPlaceholderConstants.USER_COUNT_PERCENT, userCountPercent);
    }
}
