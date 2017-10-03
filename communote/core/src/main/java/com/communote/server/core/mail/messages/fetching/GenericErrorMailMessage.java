package com.communote.server.core.mail.messages.fetching;

import java.util.Map;

import com.communote.server.core.mail.messages.MailMessage;
import com.communote.server.core.mail.messages.MailModelPlaceholderConstants;
import com.communote.server.model.user.User;


/**
 * A mail message containing the error message which describes the problem that occurred while
 * creating a note from the email.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class GenericErrorMailMessage extends MailMessage {

    private final String errorMessage;
    private final String blogTitle;

    /**
     * Creates a new error message email.
     * 
     * @param recipient
     *            the recipient of the email
     * @param errorMessage
     *            the error message to include
     * @param blogTitle
     *            the title of the blog which was the target of the post creation resulting in the
     *            error
     */
    public GenericErrorMailMessage(User recipient, String errorMessage, String blogTitle) {
        super("mail.message.fetching.error-message", recipient.getLanguageLocale(), recipient);
        this.errorMessage = errorMessage;
        this.blogTitle = blogTitle;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void prepareModel(Map<String, Object> model) {
        model.put(MailModelPlaceholderConstants.ERROR_MESSAGE, errorMessage);
        model.put(MailModelPlaceholderConstants.BLOG_TITLE, blogTitle);
    }
}
