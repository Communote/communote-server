package com.communote.server.core.mail.messages.user;

import java.util.Collection;
import java.util.Locale;
import java.util.Map;

import com.communote.server.core.mail.messages.MailMessage;
import com.communote.server.core.mail.messages.MailModelPlaceholderConstants;
import com.communote.server.model.user.User;

/**
 * Mail message for notifying the client managers that the limit of allowed posts is almost reached
 * or already has been reached
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class NoteLimitReachedMailMessage extends MailMessage {

    private final String clientID;
    private final String percentage;
    private final String limit;

    /**
     * Construct a new mail message for notifying the client manager.
     * 
     * @param managers
     *            A list of client managers to inform
     * @param clientIDName
     *            The ID of the client for which this message should be sent
     * @param percentage
     *            the percentage of the limit which has been reached
     * @param limit
     *            the limit of allowed notes
     * @param locale
     *            the locale
     */
    public NoteLimitReachedMailMessage(Collection<User> managers, Locale locale,
            String clientIDName, String percentage, String limit) {
        super("mail.message.user.notify-user-tagged-count-limit", locale, managers);
        this.clientID = clientIDName;
        this.percentage = percentage;
        this.limit = limit;

    }

    @Override
    public void prepareModel(Map<String, Object> model) {
        model.put(MailModelPlaceholderConstants.Client.CLIENT, clientID);
        model.put(MailModelPlaceholderConstants.USER_TAGGED_COUNT_LIMIT, limit);
        model.put(MailModelPlaceholderConstants.USER_TAGGED_COUNT_PERCENT, percentage);
    }
}
