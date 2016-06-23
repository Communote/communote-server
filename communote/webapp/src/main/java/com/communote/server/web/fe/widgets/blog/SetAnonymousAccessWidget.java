package com.communote.server.web.fe.widgets.blog;

import static com.communote.server.web.fe.widgets.WidgetConstants.PARAM_BLOG_ID;

import org.springframework.web.bind.ServletRequestUtils;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.blog.BlogAccessException;
import com.communote.server.api.core.blog.BlogManagement;
import com.communote.server.api.core.blog.BlogNotFoundException;
import com.communote.server.api.core.blog.BlogRightsManagement;
import com.communote.server.api.core.config.ClientConfigurationProperties;
import com.communote.server.api.core.config.type.ClientProperty;
import com.communote.server.core.config.ClientConfigurationHelper;
import com.communote.server.model.blog.Blog;
import com.communote.server.model.blog.BlogRole;
import com.communote.server.widgets.AbstractWidget;

/**
 * Widget to set, if a blog can be accessed anonymously or not.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class SetAnonymousAccessWidget extends AbstractWidget {

    /**
     * @param outputType
     *            Is not used.
     * @return "core.widget.blog.group.member.management.list"
     */
    @Override
    public String getTile(String outputType) {
        return "core.widget.blog.group.member.management.anonymous";
    }

    /**
     * @return null
     */
    @Override
    public Object handleRequest() {
        long blogId = getLongParameter(PARAM_BLOG_ID, 0);
        Blog blog;
        // TODO should we check some permission here?
        try {
            blog = ServiceLocator.instance().getService(BlogManagement.class)
                    .getBlogById(blogId, false);
        } catch (BlogNotFoundException e) {
            blog = null;
        } catch (BlogAccessException e) {
            blog = null;
        }

        if (blog == null) {
            return null;
        }
        getRequest().setAttribute("blogTitle", blog.getTitle());
        BlogRole blogRole = ServiceLocator.findService(BlogRightsManagement.class)
                .getRoleOfCurrentUser(blogId, true);
        if (BlogRole.MANAGER.equals(blogRole)) {
            getRequest().setAttribute("isBlogManager", true);
            getRequest().setAttribute("editMode",
                    ServletRequestUtils.getBooleanParameter(getRequest(), "editMode", false));
        } else {
            getRequest().setAttribute("isBlogManager", false);
            getRequest().setAttribute("editMode", false);
        }
        getRequest().setAttribute("publicAccessEnabled", blog.isPublicAccess());
        getRequest().setAttribute(
                "publicAccessAllowed",
                ClientProperty.CLIENT_BLOG_ALLOW_PUBLIC_ACCESS
                        .getValue(ClientConfigurationHelper.DEFAULT_ALLOW_PUBLIC_ACCESS));
        return blog;
    }

    /**
     * Does nothing.
     */
    @Override
    protected void initParameters() {
        // Do nothing.
    }

    /**
     * @return True if client manager allows to set public access for blogs
     */
    public boolean isPublicAccessAllowed() {
        ClientConfigurationProperties conf = CommunoteRuntime.getInstance()
                .getConfigurationManager().getClientConfigurationProperties();
        return conf.getProperty(ClientProperty.CLIENT_BLOG_ALLOW_PUBLIC_ACCESS,
                ClientConfigurationHelper.DEFAULT_ALLOW_PUBLIC_ACCESS);
    }

}
