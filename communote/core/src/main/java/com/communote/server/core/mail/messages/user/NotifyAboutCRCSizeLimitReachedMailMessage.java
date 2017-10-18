package com.communote.server.core.mail.messages.user;

import java.util.Collection;
import java.util.Locale;
import java.util.Map;

import com.communote.server.core.mail.messages.MailMessage;
import com.communote.server.core.mail.messages.MailModelPlaceholderConstants;
import com.communote.server.model.user.User;

/**
 * Mail message for notifying the client managers that the available space of the content repository
 * which stores the attachments is low or has been consumed.
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
     * @param managers
     *            A list client managers to notify
     * @param clientIDName
     *            The ID of the client for which this message should be sent
     * @param crcCurrentSize
     *            the current size of the content repository in MB
     * @param crcCurrentSizePercent
     *            the current size of the content repository in %, including the '%' character
     * @param crcCurrentSizeLimit
     *            the current limit of the content repository
     * @param locale
     *            the locale
     */
    public NotifyAboutCRCSizeLimitReachedMailMessage(Collection<User> managers, Locale locale,
            String clientIDName, String crcCurrentSize, String crcCurrentSizePercent,
            String crcCurrentSizeLimit) {
        super("mail.message.user.notify-crc-size-limit", locale, managers);
        this.clientID = clientIDName;
        this.crcSize = crcCurrentSize;
        this.crcSizePercent = crcCurrentSizePercent;
        this.crcSizeLimit = crcCurrentSizeLimit;

    }

    @Override
    public void prepareModel(Map<String, Object> model) {
        model.put(MailModelPlaceholderConstants.Client.CLIENT, clientID);
        model.put(MailModelPlaceholderConstants.CONTENT_REPOSITORY_SIZE, crcSize);
        model.put(MailModelPlaceholderConstants.CONTENT_REPOSITORY_SIZE_PERCENT, crcSizePercent);
        model.put(MailModelPlaceholderConstants.CONTENT_REPOSITORY_SIZE_LIMIT, crcSizeLimit);
    }
}
