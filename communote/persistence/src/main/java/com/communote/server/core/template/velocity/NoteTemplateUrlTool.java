package com.communote.server.core.template.velocity;

import org.apache.velocity.tools.config.DefaultKey;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.blog.MinimalBlogData;
import com.communote.server.api.core.note.NoteRenderContext;
import com.communote.server.api.core.user.UserData;
import com.communote.server.core.blog.export.PermalinkGenerationManagement;
import com.communote.server.core.blog.export.PermalinkGenerator;
import com.communote.server.model.user.UserStatus;

/**
 * A velocity tool for rendering URLs which supports the NoteRenderContext.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
// TODO is it confusing to use the same name as in FE?
@DefaultKey("urlTool")
public class NoteTemplateUrlTool {

    private PermalinkGenerator permalinkGenerator;

    /**
     * Check whether the request was secure. If no request is contained false will be returned
     * 
     * @param context
     *            the current render context
     * @return true if the request was secure, false otherwise
     */
    private boolean forceSecure(NoteRenderContext context) {
        return context.getRequest() != null ? context.getRequest().isSecure() : false;
    }

    /**
     * Return the link to a topic. The link is generated with the help of the permalink generator
     * infrastructure.
     * 
     * @param context
     *            the note render context
     * @param blog
     *            the topic
     * @return the link to the blog or empty string if the provided topic does not exist
     */
    public String getBlog(NoteRenderContext context, MinimalBlogData blog) {
        if (blog == null) {
            return "";
        }
        return getPermalinkGenerator().getBlogLink(blog.getNameIdentifier(), forceSecure(context));
    }

    /**
     * @return the lazily initialized permalink generator
     */
    private PermalinkGenerator getPermalinkGenerator() {
        if (permalinkGenerator == null) {
            permalinkGenerator = ServiceLocator.instance().getService(
                    PermalinkGenerationManagement.class);
        }
        return permalinkGenerator;
    }

    /**
     * Return the link to a user. The link is generated with the help of the permalink generator
     * infrastructure.
     * 
     * @param context
     *            the note render context
     * @param user
     *            the user
     * @return the link to the user or an empty string if the user is null or the status is DELETED
     *         or REGISTERED
     */
    public String getUser(NoteRenderContext context, UserData user) {
        if (user == null || UserStatus.DELETED.equals(user.getStatus())
                || UserStatus.REGISTERED.equals(user.getStatus())) {
            return "";
        }
        return getPermalinkGenerator().getUserLink(user.getAlias(), forceSecure(context));
    }
}
