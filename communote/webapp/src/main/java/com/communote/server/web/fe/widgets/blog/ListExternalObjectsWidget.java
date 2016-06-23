package com.communote.server.web.fe.widgets.blog;

import org.apache.log4j.Logger;
import org.springframework.web.bind.ServletRequestUtils;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.blog.BlogAccessException;
import com.communote.server.api.core.blog.BlogNotFoundException;
import com.communote.server.api.core.blog.BlogRightsManagement;
import com.communote.server.core.external.ExternalObjectManagement;
import com.communote.server.model.blog.BlogRole;
import com.communote.server.widgets.EmptyWidget;

/**
 * Widget to list all external objects for a blog.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ListExternalObjectsWidget extends EmptyWidget {

    /** Logger. */
    private final static Logger LOG = Logger.getLogger(ListExternalObjectsWidget.class);

    /**
     * {@inheritDoc}
     *
     * @return "core.widget.blog.external.objects"
     */
    @Override
    public String getTile(String outputType) {
        return "core.widget.blog.external.objects";
    }

    @Override
    public Object handleRequest() {
        long blogId = ServletRequestUtils.getLongParameter(getRequest(), "blogId", -1);
        if (blogId >= 0) {
            try {
                getRequest().setAttribute(
                        "externalObjects",
                        ServiceLocator.findService(ExternalObjectManagement.class)
                                .getExternalObjects(blogId));
            } catch (BlogNotFoundException e) {
                LOG.warn(e.getMessage());
            } catch (BlogAccessException e) {
                LOG.warn(e.getMessage());
            }
        }
        BlogRole roleOfCurrentUser = ServiceLocator.findService(BlogRightsManagement.class)
                .getRoleOfCurrentUser(blogId, false);
        boolean isManager = BlogRole.MANAGER.equals(roleOfCurrentUser);
        getRequest().setAttribute("editMode", isManager && getBooleanParameter("editMode", false));
        getRequest().setAttribute("isBlogManager", isManager);
        return super.handleRequest();
    }

}
