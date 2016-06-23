package com.communote.server.core.mail.messages.fetching;

import java.util.Map;

import com.communote.server.core.mail.messages.MailMessage;
import com.communote.server.core.mail.messages.MailModelPlaceholderConstants;
import com.communote.server.model.user.User;


/**
 * Mail message to be sent when posting failed because of missing write access for a blog.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class NoWriteAccessToBlogMailMessage extends MailMessage {

    private final String blogTitle;

    /**
     * Creates the message.
     * 
     * @param receiver
     *            the user who is the receiver
     * @param blogTitle
     *            the title of the blog for which the write access is missing
     */
    public NoWriteAccessToBlogMailMessage(User receiver, String blogTitle) {
        super("mail.message.fetching.no-write-access", receiver.getLanguageLocale(), receiver);
        this.blogTitle = blogTitle;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void prepareModel(Map<String, Object> model) {
        model.put(MailModelPlaceholderConstants.BLOG_TITLE, blogTitle);
    }
}
