package com.communote.server.web.fe.widgets.blog;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.blog.BlogRightsManagement;
import com.communote.server.widgets.AbstractWidget;

/**
 * Widget for deleting a topic.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class DeleteTopicWidget extends AbstractWidget {

    @Override
    @Deprecated
    public String getTile(String outputType) {
        return "core.widget.topic.delete";
    }

    @Override
    public Object handleRequest() {
        Long blogId = getLongParameter("blogId", -1L);
        BlogRightsManagement topicRightsManagement = ServiceLocator
                .findService(BlogRightsManagement.class);
        if (blogId > -1 && topicRightsManagement.currentUserHasManagementAccess(blogId)) {
            Long defaultBlogId = CommunoteRuntime.getInstance().getConfigurationManager()
                    .getClientConfigurationProperties().getDefaultBlogId();
            if (defaultBlogId != null) {
                if (blogId.equals(defaultBlogId)) {
                    getRequest().setAttribute("isDefaultBlog", Boolean.TRUE);
                }
            }
            getRequest().setAttribute("isManager", Boolean.TRUE);
        } else {
            getRequest().setAttribute("isManager", Boolean.FALSE);
        }
        return null;
    }

    @Override
    protected void initParameters() {
        // nothing to do here
    }
}
