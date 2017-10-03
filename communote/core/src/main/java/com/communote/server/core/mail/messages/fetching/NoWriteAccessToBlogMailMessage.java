package com.communote.server.core.mail.messages.fetching;

import java.util.Map;

import com.communote.server.core.mail.messages.MailMessage;
import com.communote.server.core.mail.messages.MailModelPlaceholderConstants;
import com.communote.server.model.user.User;


/**
 * Mail message to be sent when posting failed because of missing write access to a blog.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class NoWriteAccessToBlogMailMessage extends MailMessage {

    private final String blogTitle;

    /**
     * Creates the message.
     * 
     * @param recipient
     *            the user to send the mail to
     * @param blogTitle
     *            the title of the blog for which the write access is missing
     */
    public NoWriteAccessToBlogMailMessage(User recipient, String blogTitle) {
        super("mail.message.fetching.no-write-access", recipient.getLanguageLocale(), recipient);
        this.blogTitle = blogTitle;
    }

    @Override
    public void prepareModel(Map<String, Object> model) {
        model.put(MailModelPlaceholderConstants.BLOG_TITLE, blogTitle);
    }
}
