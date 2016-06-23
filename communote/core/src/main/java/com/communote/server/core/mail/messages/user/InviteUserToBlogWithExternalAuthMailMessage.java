package com.communote.server.core.mail.messages.user;

import com.communote.server.model.blog.Blog;
import com.communote.server.model.user.User;


/**
 * A Mail Message to invite user to a blog with existing external authentication like ldap.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class InviteUserToBlogWithExternalAuthMailMessage extends InviteUserToBlogMailMessage {

    /**
     * Instantiates a new invite user to blog with external auth mail message.
     * 
     * @param inviter
     *            the inviter
     * @param receiver
     *            the receiver
     * @param blog
     *            the blog
     * 
     */
    public InviteUserToBlogWithExternalAuthMailMessage(User inviter, User receiver,
            Blog blog) {
        super("mail.message.user.invite-user-to-blog-with-external-authentication", inviter,
                receiver, blog);
    }

}
