package com.communote.server.core.mail.messages.user;

import java.util.Map;

import com.communote.server.core.mail.messages.MailMessage;
import com.communote.server.core.mail.messages.MailModelPlaceholderConstants;
import com.communote.server.model.user.User;


/**
 * Mail Message on user account activation.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ActivateUserMailMessage extends MailMessage {

    /** The email template. */
    private final static String FIRST_ACTIVATION_EMAIL_TEMPLATE = "mail.message.user.user-activation";
    private final static String REACTIVATION_EMAIL_TEMPLATE = "mail.message.user.user-reactivation";
    private final User recipient;

    /**
     * Instantiates a new activate user mail message.
     * 
     * @param recipient
     *            the user who was activated
     * @param firstActivation
     *            whether it is the first activation (e.g. after confirmation) or a re-activation
     *            (e.g. after being temporarily disabled)
     */
    public ActivateUserMailMessage(User recipient, boolean firstActivation) {
        super(firstActivation ? FIRST_ACTIVATION_EMAIL_TEMPLATE : REACTIVATION_EMAIL_TEMPLATE,
                recipient.getLanguageLocale(), recipient);
        this.recipient = recipient;
    }

    @Override
    public void prepareModel(Map<String, Object> model) {
        model.put(MailModelPlaceholderConstants.USER, recipient);
    }
}
