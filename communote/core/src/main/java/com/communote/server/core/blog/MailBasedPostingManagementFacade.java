/**
 *
 */
package com.communote.server.core.blog;

import java.util.Set;

import javax.mail.Message;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.client.ClientDelegate;
import com.communote.server.api.core.client.ClientDelegateCallback;
import com.communote.server.api.core.client.ClientTO;

/**
 * Helper for the MailBasedPostingManagement.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class MailBasedPostingManagementFacade {
    private static MailBasedPostingManagementFacade INSTANCE = null;

    /**
     * Returns the singleton instance for the facade.
     *
     * @return the singleton instance
     */
    public static MailBasedPostingManagementFacade instance() {
        if (INSTANCE == null) {
            INSTANCE = new MailBasedPostingManagementFacade();
        }
        return INSTANCE;
    }

    /**
     * Private constructor to avoid instances of utility class.
     */
    private MailBasedPostingManagementFacade() {
        // Do nothing
    }

    /**
     * Creates a user tagged post from a email message.
     *
     * @param client
     *            identifies the client and the associated database to use for creation
     * @param message
     *            the message from which the post will be created
     * @param senderEmail
     *            the email address of the sender of the email
     * @param blogNameIds
     *            name identifiers of blogs where the post should be created in
     */
    public void createNoteFromMail(ClientTO client, final Message message,
            final String senderEmail, final Set<String> blogNameIds) {
        try {
            new ClientDelegate(client).execute(new ClientDelegateCallback<Object>() {
                @Override
                public Object doOnClient(ClientTO client) throws Exception {
                    ServiceLocator.findService(MailBasedPostingManagement.class)
                            .createNoteFromMail(message, senderEmail, blogNameIds);
                    return null;
                }
            });
        } catch (Exception e) {
            throw new MailBasedPostingManagementException(
                    "Unknown exception occured while creating post from email.", e);
        }
    }
}
