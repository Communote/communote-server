package com.communote.server.core.mail.messages.fetching;

import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.communote.server.core.mail.messages.MailMessage;
import com.communote.server.model.user.User;


/**
 * A mail message displaying the error message shown when reply to a direct message do not contains
 * correct recipients.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class DirectMessageMissingRecipientMailMessage extends MailMessage {

    private final String[] uninformableUsers;
    private final String[] unresolvableUsers;

    /**
     * Creates a new error message email.
     * 
     * @param receiver
     *            the receiver of the email
     * @param uninformableUsers
     *            the aliases of the uninformable users that caused this exception or null if there
     *            were no uniformable users
     * @param unresolvableUsers
     *            the aliases of the unresolvable users that caused this exception or null if there
     *            were no unresolvable users
     */
    public DirectMessageMissingRecipientMailMessage(User receiver,
            String[] uninformableUsers, String[] unresolvableUsers) {
        super("mail.message.fetching.directmessage-missing-recipient-message", receiver
                .getLanguageLocale(), receiver);
        this.uninformableUsers = uninformableUsers;
        this.unresolvableUsers = unresolvableUsers;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void prepareModel(Map<String, Object> model) {
        if (this.uninformableUsers != null) {
            model.put("uninformableUsers", StringUtils.join(this.uninformableUsers, ", "));
        }
        if (this.unresolvableUsers != null) {
            model.put("unresolvableUsers", StringUtils.join(this.unresolvableUsers, ", "));
        }
    }
}
