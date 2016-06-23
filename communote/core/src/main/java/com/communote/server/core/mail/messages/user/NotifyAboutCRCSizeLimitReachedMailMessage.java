package com.communote.server.core.mail.messages.user;

import java.util.Collection;
import java.util.Locale;
import java.util.Map;

import com.communote.server.core.mail.messages.MailMessage;
import com.communote.server.core.mail.messages.MailModelPlaceholderConstants;
import com.communote.server.model.user.User;

/**
 * Mail message for notifying the client manager that the content repository size is short.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class NotifyAboutCRCSizeLimitReachedMailMessage extends MailMessage {

    private final String clientID;
    private final String crcSize;
    private final String crcSizePercent;
    private final String crcSizeLimit;

    /**
     * Construct a new mail message for notifying the client manager.
     *
     * @param receivers
     *            A list of all set client manager
     * @param clientIDName
     *            The Name of the client for which this message should be sent
     * @param crcCurrentSize
     *            the current size of the content repository in MB
     * @param crcCurrentSizePercent
     *            the current size of the content repository in %, including the '%' character
     * @param crcCurrentSizeLimit
     *            the current limit for the content repository
     * @param locale
     *            the locale
     */
    public NotifyAboutCRCSizeLimitReachedMailMessage(Collection<User> receivers, Locale locale,
            String clientIDName, String crcCurrentSize, String crcCurrentSizePercent,
            String crcCurrentSizeLimit) {
        super("mail.message.user.notify-crc-size-limit", locale, receivers
                .toArray(new User[receivers.size()]));
        this.clientID = clientIDName;
        this.crcSize = crcCurrentSize;
        this.crcSizePercent = crcCurrentSizePercent;
        this.crcSizeLimit = crcCurrentSizeLimit;

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void prepareModel(Map<String, Object> model) {
        model.put(MailModelPlaceholderConstants.Client.CLIENT, clientID);
        model.put(MailModelPlaceholderConstants.CONTENT_REPOSITORY_SIZE, crcSize);
        model.put(MailModelPlaceholderConstants.CONTENT_REPOSITORY_SIZE_PERCENT, crcSizePercent);
        model.put(MailModelPlaceholderConstants.CONTENT_REPOSITORY_SIZE_LIMIT, crcSizeLimit);
    }
}
