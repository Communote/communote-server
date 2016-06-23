package com.communote.server.web.commons.view;

import static com.communote.server.web.commons.view.RepositoryContentView.MODEL_ATTRIBUTE_BINARY_CONTENT;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

import com.communote.common.io.MimeTypeHelper;
import com.communote.server.api.ServiceLocator;
import com.communote.server.core.crc.vo.ContentMetadata;
import com.communote.server.core.osgi.OSGiHelper;
import com.communote.server.core.osgi.OSGiManagement;
import com.communote.server.core.vo.content.AttachmentFileTO;
import com.communote.server.core.vo.content.AttachmentTO;

/**
 * Controller for enabling download of plugin resources.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class PluginResourceDownloadController implements Controller {

    private final static RepositoryContentView REPOSITORY_CONTENT_VIEW =
            new RepositoryContentView(null);

    /** Logger. */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(PluginResourceDownloadController.class);

    private OSGiManagement osgiManagement;

    /**
     * Checks, if the file was modified since the last request.
     * 
     * @param request
     *            The request.
     * @param file
     *            The file to check.
     * @return True, if the file was modified.
     */
    private boolean checkWasModified(HttpServletRequest request, File file) {
        long modifiedSince = request.getDateHeader("If-Modified-Since");
        return modifiedSince == -1 || modifiedSince < (file.lastModified() / 60000 * 60000);
    }

    /**
     * 
     * @param request
     *            The request.
     * @param response
     *            The response.
     * @param fileName
     *            The files name.
     * @param bundleStorageBasePath
     *            Base storage path of the bundle.
     * @return The ModelAndView or null if none.
     * @throws IOException
     *             Exception.
     */
    private ModelAndView getModelAndView(HttpServletRequest request, HttpServletResponse response,
            String fileName, String bundleStorageBasePath) throws IOException {
        File file = new File(fileName);
        if (!file.exists()
                || file.isDirectory()
                || !file.canRead()
                || !file.getCanonicalPath().startsWith(
                        new File(bundleStorageBasePath).getCanonicalPath())) {
            LOGGER.warn(
                    "There was an invalid request for downloading a plugin resource file from {}. "
                            + "The file would have been located locally at {}",
                    request.getRequestURI(), file.getAbsolutePath());
            response.sendError(404);
            return null;
        }
        if (!checkWasModified(request, file)) {
            response.setStatus(304);
            return null;
        }
        String mimeType = MimeTypeHelper.getMimeType(fileName);
        AttachmentTO attachmentTO = new AttachmentFileTO(file);
        attachmentTO.setMetadata(new ContentMetadata());
        attachmentTO.getMetadata().setMimeType(mimeType);
        attachmentTO.getMetadata().setContentSize(file.length());
        Date lastModificationDate = new Date(file.lastModified());
        attachmentTO.getMetadata().setDate(lastModificationDate);
        attachmentTO.getMetadata().setFilename(file.getName());
        response.setDateHeader("Last-Modified", file.lastModified());
        return new ModelAndView(REPOSITORY_CONTENT_VIEW,
                MODEL_ATTRIBUTE_BINARY_CONTENT, attachmentTO);
    }

    /**
     * @param request
     *            The request.
     * @param response
     *            The response.
     * @return <code>null</code>. The result will be written into the Stream directly.
     * 
     * @throws Exception
     *             Exception.
     */
    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        String url = StringUtils.substringBefore(request.getRequestURI(), ";");
        url = url.substring(url.indexOf("plugins/") + 8).replace(".do", "");
        String symbolicName = url.substring(0, url.indexOf("/"));
        if (!isBundleStarted(symbolicName)) {
            LOGGER.warn(
                    "A requested file can't be download, because the bundle ('{}') was not started: {}",
                    symbolicName, request.getRequestURI());
            response.sendError(423); // LOCKED, best I found for this type of error
            return null;
        }
        String bundleStorageBasePath = OSGiHelper.getBundleStorage(symbolicName) + "/static";
        String fileName = bundleStorageBasePath + url.replace(symbolicName, "");
        return getModelAndView(request, response, fileName, bundleStorageBasePath);
    }

    /**
     * 
     * @param symbolicName
     *            The symbolic name of the bundle.
     * @throws IllegalAccessError
     *             When the bundle is not started.
     * @return True, if the bundle is started.
     */
    private boolean isBundleStarted(String symbolicName) {
        if (osgiManagement == null) {
            osgiManagement = ServiceLocator.instance().getService(OSGiManagement.class);
        }
        if (!osgiManagement.isBundleStarted(symbolicName)) {
            return false;
        }
        return true;
    }
}
