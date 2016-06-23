package com.communote.server.web.fe.portal.user.client.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.blog.BlogManagement;
import com.communote.server.core.blog.helper.BlogManagementHelper;
import com.communote.server.core.common.LimitHelper;
import com.communote.server.core.crc.ContentRepositoryManagementHelper;
import com.communote.server.core.crc.FilesystemConnector;
import com.communote.server.core.crc.RepositoryConnector;
import com.communote.server.core.crc.RepositoryConnectorDelegate;
import com.communote.server.core.crc.RepositoryConnectorNotFoundException;
import com.communote.server.core.storing.ResourceStoringHelper;
import com.communote.server.core.user.UserManagement;
import com.communote.server.core.user.UserManagementHelper;
import com.communote.server.service.NoteService;
import com.communote.server.web.commons.controller.SimpleViewController;

/**
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ClientOverviewController extends SimpleViewController {

    @Override
    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        long userCount = ServiceLocator.findService(UserManagement.class).getActiveUserCount();
        long userLimit = UserManagementHelper.getCountLimit();
        request.setAttribute("userCount", userCount);
        request.setAttribute("userCountLimit", LimitHelper.getCountLimitAsString(userLimit));
        if (LimitHelper.getCount(userCount, userLimit) >= 0.9F) {
            request.setAttribute("userPercent",
                    LimitHelper.getCountPercentAsString(userCount, userLimit));
        }
        request.setAttribute("userLimitReached",
                LimitHelper.isCountLimitReached(userCount, userLimit));

        prepareFileRepositorySizeDetails(request);

        long userTaggedCount = ServiceLocator.findService(NoteService.class).getNoteCount();
        long userTaggedLimit = ResourceStoringHelper.getCountLimit();
        request.setAttribute("userTaggedSize", userTaggedCount);
        request.setAttribute("userTaggedLimit", LimitHelper.getCountLimitAsString(userTaggedLimit));
        if (LimitHelper.getCount(userTaggedCount, userTaggedLimit) >= 0.9F) {
            request.setAttribute("userTaggedPercent",
                    LimitHelper.getCountPercentAsString(userTaggedCount, userTaggedLimit));
        }
        request.setAttribute("userTaggedLimitReached",
                LimitHelper.isCountLimitReached(userTaggedCount, userTaggedLimit));

        long blogCount = ServiceLocator.findService(BlogManagement.class).getBlogCount();
        long blogLimit = BlogManagementHelper.getCountLimit();
        request.setAttribute("blogSize", blogCount);
        request.setAttribute("blogLimit", LimitHelper.getCountLimitAsString(blogLimit));
        if (LimitHelper.getCount(blogCount, blogLimit) >= 0.9F) {
            request.setAttribute("blogPercent",
                    LimitHelper.getCountPercentAsString(blogCount, blogLimit));
        }
        request.setAttribute("blogLimitReached",
                LimitHelper.isCountLimitReached(blogCount, blogLimit));
        return super.handleRequest(request, response);
    }

    /**
     * Prepare attributes holding details about the file repository
     *
     * @param request
     *            the current request
     */
    private void prepareFileRepositorySizeDetails(HttpServletRequest request) {
        long repoSize = -1;
        try {
            RepositoryConnector connector = ServiceLocator.findService(
                    RepositoryConnectorDelegate.class).getDefaultRepositoryConnector();
            if (connector instanceof FilesystemConnector) {
                repoSize = connector.getRepositorySize();
            }
        } catch (RepositoryConnectorNotFoundException e) {
        }
        if (repoSize == -1) {
            request.setAttribute("crcDataAvailable", false);
            return;
        } else {
            request.setAttribute("crcDataAvailable", true);
        }
        long crcLimit = ContentRepositoryManagementHelper.getSizeLimit();
        request.setAttribute("crcSize", ContentRepositoryManagementHelper.getSizeAsString(repoSize));
        request.setAttribute("crcLimit",
                ContentRepositoryManagementHelper.getSizeLimitAsString(crcLimit));
        if (LimitHelper.getCount(repoSize, crcLimit) >= 0.9F) {
            request.setAttribute("crcSizePercent",
                    LimitHelper.getCountPercentAsString(repoSize, crcLimit));
        }
        request.setAttribute("crcLimitReached", LimitHelper.isCountLimitReached(repoSize, crcLimit));
    }
}
