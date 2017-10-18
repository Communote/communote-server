package com.communote.server.core.mail.messages.fetching;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.client.ClientNotFoundException;
import com.communote.server.api.core.client.ClientTO;
import com.communote.server.api.service.ClientRetrievalService;
import com.communote.server.core.mail.messages.MailMessage;
import com.communote.server.core.mail.messages.MailModelPlaceholderConstants;
import com.communote.server.model.user.User;
import com.communote.server.persistence.user.client.ClientHelper;

/**
 * A mail message to inform a user about a missing topic name-identifier/alias inside the subject.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class BlogNameIdMissingInSubjectMailMessage extends MailMessage {

    /** Logger. */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(BlogNameIdMissingInSubjectMailMessage.class);

    /**
     * Creates a new message.
     *
     * @param recipient
     *            the user to notify about the missing topic alias
     */
    public BlogNameIdMissingInSubjectMailMessage(User recipient) {
        super("mail.message.fetching.blog-alias-missing", recipient.getLanguageLocale(), recipient);
    }

    @Override
    public void prepareModel(Map<String, Object> model) {
        try {
            ClientTO client = ServiceLocator.findService(ClientRetrievalService.class).findClient(
                    ClientHelper.getGlobalClientId());
            model.put(MailModelPlaceholderConstants.Client.CLIENT, client);
        } catch (ClientNotFoundException e) {
            LOGGER.error("Global client not found.");
        }
    }
}
