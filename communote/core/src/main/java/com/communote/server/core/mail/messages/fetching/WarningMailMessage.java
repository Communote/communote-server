package com.communote.server.core.mail.messages.fetching;

import java.util.Collection;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.communote.server.core.mail.messages.MailMessage;
import com.communote.server.core.mail.messages.MailModelPlaceholderConstants;
import com.communote.server.model.user.User;


/**
 * Mail message to be sent after posting via email when not all specified users could be informed
 * about the new post or crossposting failed and is treated as warning.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class WarningMailMessage extends MailMessage {

    private final Collection<String> unresolvableUsers;
    private final Collection<String> uninformableUsers;
    private final Collection<String> unresolvableBlogs;
    private final Collection<String> unwritableBlogs;
    private final String blogTitle;
    private boolean userWarning = false;
    private boolean crosspostWarning = false;

    private final boolean isDirect;

    /**
     * Creates a warning e-mail message.
     * 
     * @param receiver
     *            the user to receive the mail
     * @param unresolvableUsers
     *            collection of user aliases that could not be resolved to existing users
     * @param uninformableUsers
     *            collection of user aliases that could not be informed because they do not have
     *            write access to the blog
     * @param unresolvableBlogs
     *            a collection of crosspost blog name identifiers that could not be resolved to
     *            existing blogs
     * @param unwritableBlogs
     *            a collection of crosspost blog name IDs for which the user has no write access
     * @param blogTitle
     *            the title of the blog in which the note was created
     * @param isDirect
     *            True, if this message is private message.
     */
    public WarningMailMessage(User receiver, Collection<String> unresolvableUsers,
            Collection<String> uninformableUsers, Collection<String> unresolvableBlogs,
            Collection<String> unwritableBlogs, String blogTitle, boolean isDirect) {
        super("mail.message.fetching.warning", receiver.getLanguageLocale(), receiver);
        this.unresolvableUsers = unresolvableUsers;
        this.uninformableUsers = uninformableUsers;
        this.unresolvableBlogs = unresolvableBlogs;
        this.unwritableBlogs = unwritableBlogs;
        this.isDirect = isDirect;
        if (unresolvableUsers.size() != 0 || uninformableUsers.size() != 0) {
            userWarning = true;
        }
        if (unresolvableBlogs.size() != 0 || unwritableBlogs.size() != 0) {
            crosspostWarning = true;
        }
        this.blogTitle = blogTitle;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void prepareModel(Map<String, Object> model) {
        model.put("user_notify_warning", userWarning);
        model.put("crosspost_warning", crosspostWarning);
        if (unresolvableUsers.size() > 0) {
            String users = StringUtils.join(unresolvableUsers, ", ");
            if (unresolvableUsers.size() == 1) {
                model.put(MailModelPlaceholderConstants.UNRESOLVABLE_USER, users);
            } else {
                model.put(MailModelPlaceholderConstants.UNRESOLVABLE_USERS, users);
            }
        }
        if (uninformableUsers.size() > 0) {
            String users = StringUtils.join(uninformableUsers, ", ");
            if (uninformableUsers.size() == 1) {
                model.put(MailModelPlaceholderConstants.UNINFORMABLE_USER, users);
            } else {
                model.put(MailModelPlaceholderConstants.UNINFORMABLE_USERS, users);
            }
        }
        if (unresolvableBlogs.size() > 0) {
            String blogs = StringUtils.join(unresolvableBlogs, ", ");
            if (unresolvableBlogs.size() == 1) {
                model.put(MailModelPlaceholderConstants.UNRESOLVABLE_BLOG, blogs);
            } else {
                model.put(MailModelPlaceholderConstants.UNRESOLVABLE_BLOGS, blogs);
            }
        }
        if (unwritableBlogs.size() != 0) {
            String blogs = StringUtils.join(unwritableBlogs, ", ");
            if (unwritableBlogs.size() == 1) {
                model.put(MailModelPlaceholderConstants.UNWRITABLE_BLOG, blogs);
            } else {
                model.put(MailModelPlaceholderConstants.UNWRITABLE_BLOGS, blogs);
            }
        }
        model.put(MailModelPlaceholderConstants.IS_DIRECT, isDirect);
        model.put(MailModelPlaceholderConstants.BLOG_TITLE, blogTitle);
    }
}
