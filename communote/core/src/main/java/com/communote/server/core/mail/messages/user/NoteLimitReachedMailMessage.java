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
public class NoteLimitReachedMailMessage extends MailMessage {

    private final String clientID;
    private final String percent;
    private final String limit;

    /**
     * Construct a new mail message for notifying the client manager.
     * 
     * @param receivers
     *            A list of all set client manager
     * @param clientIDName
     *            The Name of the client for which this message should be sent
     * @param percent
     *            the percent
     * @param limit
     *            the limit
     * @param locale
     *            the locale
     */
    public NoteLimitReachedMailMessage(Collection<User> receivers, Locale locale,
            String clientIDName, String percent, String limit) {
        super("mail.message.user.notify-user-tagged-count-limit", locale, receivers
                .toArray(new User[receivers.size()]));
        this.clientID = clientIDName;
        this.percent = percent;
        this.limit = limit;

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void prepareModel(Map<String, Object> model) {
        model.put(MailModelPlaceholderConstants.Client.CLIENT, clientID);
        model.put(MailModelPlaceholderConstants.USER_TAGGED_COUNT_LIMIT, limit);
        model.put(MailModelPlaceholderConstants.USER_TAGGED_COUNT_PERCENT, percent);
    }
}
