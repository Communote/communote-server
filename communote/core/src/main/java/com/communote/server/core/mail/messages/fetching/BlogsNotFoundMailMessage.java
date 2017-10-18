package com.communote.server.core.mail.messages.fetching;

import java.util.Collection;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.communote.server.core.mail.messages.MailMessage;
import com.communote.server.core.mail.messages.MailModelPlaceholderConstants;
import com.communote.server.model.user.User;


/**
 * Mail message to be sent when email based posting failed because of not found blogs.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class BlogsNotFoundMailMessage extends MailMessage {

    private static final String TEMPLATE_KEY_MULTIPLE = "mail.message.fetching.blogs-not-found";
    private static final String TEMPLATE_KEY_SINGLE = "mail.message.fetching.blog-not-found";
    private static final String TEMPLATE_KEY_META_POST_DATA_BLOG =
            "mail.message.fetching.meta-post-data-blog-not-found";

    private final Collection<String> notFoundBlogNameIds;

    /**
     * Creates a mail message describing that some blogs were not found.
     * 
     * @param recipient
     *            the recipient of the message
     * @param blogNameIds
     *            collection of blog name identifiers that were not found
     * @param metaPostDataBlog
     *            whether the not found blogs are meta-post-data blogs which means that the
     *            identifiers were found within the email-message encoded with the meta-post-data
     *            syntax ('&BlogName')
     */
    public BlogsNotFoundMailMessage(User recipient, Collection<String> blogNameIds,
            boolean metaPostDataBlog) {
        super(metaPostDataBlog ? TEMPLATE_KEY_META_POST_DATA_BLOG
                : (blogNameIds.size() == 1 ? TEMPLATE_KEY_SINGLE : TEMPLATE_KEY_MULTIPLE),
                recipient.getLanguageLocale(), recipient);
        this.notFoundBlogNameIds = blogNameIds;
    }

    @Override
    public void prepareModel(Map<String, Object> model) {
        model.put(MailModelPlaceholderConstants.BLOG_NAME_IDS, StringUtils.join(
                notFoundBlogNameIds, ", "));
    }
}
