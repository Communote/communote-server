package com.communote.server.core.mail.messages.fetching;

import java.util.Map;

import com.communote.server.core.mail.messages.MailMessage;
import com.communote.server.core.mail.messages.MailModelPlaceholderConstants;
import com.communote.server.model.user.User;


/**
 * Mail Message to be sent in case of content processing errors during mail based posting.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ContentProcessingErrorMailMessage extends MailMessage {

    /**
     * The possible error types to be handled by this mail message.
     */
    public enum Type {
        /**
         * the mail had no content for posting
         */
        NO_CONTENT("mail.message.fetching.no-content"),
        /**
         * the content could not be processed successfully
         */
        CONTENT_UNPROCESSABLE("mail.message.fetching.content-unprocessable");

        private String templateMessageKey;

        /**
         * @param templateMessageKey
         *            Message key of the template.
         */
        Type(String templateMessageKey) {
            this.templateMessageKey = templateMessageKey;
        }

        /**
         * @return Message key for the template.
         */
        String getTemplatePath() {
            return this.templateMessageKey;
        }
    }

    private final String blogTitle;

    /**
     * @param recipient
     *            the user to inform about the problem that occurred while processing the content
     * @param blogTitle
     *            the blog title
     * @param errorType
     *            the error type
     */
    public ContentProcessingErrorMailMessage(User recipient, String blogTitle, Type errorType) {
        super(errorType.templateMessageKey, recipient.getLanguageLocale(), recipient);
        this.blogTitle = blogTitle;
    }

    @Override
    public void prepareModel(Map<String, Object> model) {
        model.put(MailModelPlaceholderConstants.BLOG_TITLE, blogTitle);
    }
}
