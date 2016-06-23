package com.communote.server.core.mail.messages.user;

import java.util.Map;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.config.type.ClientProperty;
import com.communote.server.core.blog.export.PermalinkGenerationManagement;
import com.communote.server.core.mail.messages.MailModelPlaceholderConstants;
import com.communote.server.core.mail.messages.SecurityCodeMailMessage;
import com.communote.server.model.blog.Blog;
import com.communote.server.model.user.User;
import com.communote.server.model.user.security.InviteUserToBlogSecurityCode;

/**
 * Mail Message to invite user to a blog.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class InviteUserToBlogMailMessage extends SecurityCodeMailMessage {

    private final User inviter;
    private final Blog blog;

    private String localizedBlogTitle;

    private InviteUserToBlogSecurityCode code;

    /**
     * Instantiates a new invite user to blog mail message.
     *
     * @param template
     *            the template
     * @param inviter
     *            the inviter
     * @param blog
     *            the blog
     * @param receiver
     *            the receiver
     */
    protected InviteUserToBlogMailMessage(String template, User inviter, User receiver, Blog blog) {
        super(template, receiver.getLanguageLocale(), receiver);
        this.blog = blog;
        this.localizedBlogTitle = blog.getTitle();
        this.inviter = inviter;
    }

    /**
     * Instantiates a new mail message.
     *
     * @param inviter
     *            the inviter
     * @param blog
     *            the blog
     * @param code
     *            the code
     * @param receiver
     *            the receiver
     */
    public InviteUserToBlogMailMessage(User inviter, User receiver, Blog blog,
            InviteUserToBlogSecurityCode code) {
        this("mail.message.user.invite-user-to-blog", inviter, receiver, blog);
        this.setCode(code);
        this.localizedBlogTitle = blog.getTitle();
    }

    /**
     * Gets the code.
     *
     * @return the code
     */
    public InviteUserToBlogSecurityCode getCode() {
        return code;
    }

    /**
     * {@inheritDoc}
     *
     * @see com.communote.server.core.mail.messages.MailMessage#getLinkPrefix()
     */
    @Override
    public String getLinkPrefix() {
        return "/user/confirm.do";
    }

    /**
     * {@inheritDoc}
     *
     * @see com.communote.server.core.mail.messages.MailMessage#prepareModel()
     */
    @Override
    protected void prepareModel(Map<String, Object> model) {
        model.put(MailModelPlaceholderConstants.BLOG_TITLE, this.localizedBlogTitle);
        model.put(MailModelPlaceholderConstants.USER, inviter);
        boolean renderLink = CommunoteRuntime
                .getInstance()
                .getConfigurationManager()
                .getClientConfigurationProperties()
                .getProperty(ClientProperty.INVITATION_RENDER_BLOG_PERMALINK,
                        ClientProperty.DEFAULT_INVITATION_RENDER_BLOG_PERMALINK);
        model.put(MailModelPlaceholderConstants.RENDER_BLOG_PERMA_LINK, renderLink);
        model.put(MailModelPlaceholderConstants.PERMA_LINK_BLOG,
                ServiceLocator.instance().getService(PermalinkGenerationManagement.class)
                .getBlogLink(blog.getNameIdentifier(), true));
        if (code != null) {
            model.put(MailModelPlaceholderConstants.CONFIRMATION_LINK,
                    getSecurityCodeConfirmationLink(code));
        }
    }

    /**
     * Sets the code.
     *
     * @param code
     *            the new code
     */
    public void setCode(InviteUserToBlogSecurityCode code) {
        this.code = code;
    }
}
