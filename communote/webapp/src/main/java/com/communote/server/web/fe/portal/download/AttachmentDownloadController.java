package com.communote.server.web.fe.portal.download;

import static com.communote.server.web.commons.view.RepositoryContentView.DOWNLOAD_PARAMETER;
import static com.communote.server.web.commons.view.RepositoryContentView.MODEL_ATTRIBUTE_BINARY_CONTENT;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.log4j.Logger;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.core.blog.AttachmentNotFoundException;
import com.communote.server.core.crc.vo.ContentId;
import com.communote.server.core.storing.ResourceStoringManagement;
import com.communote.server.core.vo.content.AttachmentTO;
import com.communote.server.persistence.crc.ContentRepositoryException;
import com.communote.server.web.commons.MessageHelper;
import com.communote.server.web.commons.helper.ControllerHelper;
import com.communote.server.web.commons.view.RepositoryContentView;

/**
 * Controller for downloading files in the format /portal/files/{contentId}/FileName.ext
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class AttachmentDownloadController implements Controller {

    /** Logger. */
    private final static Logger LOG = Logger.getLogger(AttachmentDownloadController.class);

    private final static RepositoryContentView REPOSITORY_CONTENT_VIEW = new RepositoryContentView();

    private static final String PARAM_CONNECTOR_ID = "connectorId";

    private static final String PARAM_CONTENT_ID = "contentId";

    private static final Pattern CONTENT_ID_PATTERN = Pattern.compile("/portal/files/([0-9]+)/");

    private String errorView;

    /**
     * @param request
     *            The request.
     * @return The {@link ContentId} for this request.
     * @throws AuthorizationException
     *             Exception.
     * @throws ContentRepositoryException
     *             Exception.
     * @throws AttachmentNotFoundException
     *             in case the attachment does not exist
     */
    private AttachmentTO getAttachment(HttpServletRequest request)
            throws ContentRepositoryException, AuthorizationException, AttachmentNotFoundException {
        String url = request.getRequestURI();
        Matcher matcher = CONTENT_ID_PATTERN.matcher(url);
        if (matcher.find()) {
            return getResourceStoringManagement().getAttachment(
                    NumberUtils.toLong(matcher.group(1)));
        }
        // TODO v1.4 CleanUp: Remove stuff for old url.
        ContentId contentId = new ContentId();
        contentId.setConnectorId(request.getParameter(PARAM_CONNECTOR_ID));
        contentId.setContentId(request.getParameter(PARAM_CONTENT_ID));
        return getResourceStoringManagement().getAttachment(contentId);
    }

    /**
     *
     * @return the resource storing management
     */
    private ResourceStoringManagement getResourceStoringManagement() {
        return ServiceLocator.instance().getService(ResourceStoringManagement.class);
    }

    /**
     * Sends the requested content.
     *
     * {@inheritDoc}
     */
    @Override
    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        ModelAndView modelAndView = null;
        try {
            AttachmentTO content = getAttachment(request);
            modelAndView = new ModelAndView(REPOSITORY_CONTENT_VIEW,
                    MODEL_ATTRIBUTE_BINARY_CONTENT, content);
            modelAndView.addObject(DOWNLOAD_PARAMETER, request.getParameter(DOWNLOAD_PARAMETER));
        } catch (ContentRepositoryException e) {
            MessageHelper.saveErrorMessageFromKey(request,
                    "error.content.repository.download.failed");
            if (errorView != null) {
                modelAndView = new ModelAndView(errorView);
            }
            LOG.warn("Error getting attachment: " + e.getMessage());
        } catch (AuthorizationException e) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            LOG.warn("Access restricted, on attachment download: " + e.getMessage());
        } catch (AttachmentNotFoundException | IllegalArgumentException e) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            MessageHelper.saveErrorMessageFromKey(request,
                    "error.content.repository.download.not_found");
            if (errorView != null) {
                modelAndView = new ModelAndView(errorView);
            }
            LOG.warn("The requested download can't be found: " + e.getMessage());
        }
        return modelAndView;
    }

    /**
     * Set the view for error feedback.
     *
     * @param errorView
     *            the errorView to set
     */
    public void setErrorView(String errorView) {
        this.errorView = ControllerHelper.replaceModuleInViewName(errorView);
    }
}
