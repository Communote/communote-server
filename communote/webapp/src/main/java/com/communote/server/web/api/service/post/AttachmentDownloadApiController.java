package com.communote.server.web.api.service.post;

import static com.communote.server.web.commons.view.RepositoryContentView.DOWNLOAD_PARAMETER;
import static com.communote.server.web.commons.view.RepositoryContentView.MODEL_ATTRIBUTE_BINARY_CONTENT;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.servlet.ModelAndView;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.core.blog.AttachmentNotFoundException;
import com.communote.server.core.storing.ResourceStoringManagement;
import com.communote.server.core.vo.content.AttachmentTO;
import com.communote.server.web.api.service.BaseApiController;
import com.communote.server.web.commons.view.RepositoryContentView;

/**
 * Controller for downloading attachments using the API. In case of error it sets the http server
 * result.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 * @deprecated Use new generated REST-API instead.
 */
@Deprecated
public class AttachmentDownloadApiController extends BaseApiController {

    private final static Logger LOG = Logger.getLogger(AttachmentDownloadApiController.class);

    private final static RepositoryContentView REPOSITORY_CONTENT_VIEW = new RepositoryContentView();

    /**
     *
     * @return the resource storing management
     */
    private ResourceStoringManagement getResourceStoringManagement() {
        return ServiceLocator.findService(ResourceStoringManagement.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        Long attachmentId = getResourceId(request, true);
        try {
            AttachmentTO attachment = getResourceStoringManagement().getAttachment(attachmentId);

            ModelAndView modelAndView = new ModelAndView(REPOSITORY_CONTENT_VIEW,
                    MODEL_ATTRIBUTE_BINARY_CONTENT, attachment);
            modelAndView.addObject(DOWNLOAD_PARAMETER, request.getParameter(DOWNLOAD_PARAMETER));

            return modelAndView;

        } catch (AuthorizationException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Error executing API request for " + request.getRequestURI() + "! "
                        + e.getMessage(), e);
            }
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);

        } catch (AuthenticationException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Error executing API request for " + request.getRequestURI() + "! "
                        + e.getMessage(), e);
            }
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        } catch (AttachmentNotFoundException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Error executing API request for " + request.getRequestURI() + "! "
                        + e.getMessage(), e);
            }
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        } catch (Throwable e) {
            LOG.fatal("Error executing API request for " + request.getRequestURI() + "! "
                    + e.getMessage(), e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }

        return null;
    }

}
