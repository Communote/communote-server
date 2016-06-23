package com.communote.server.web.fe.portal.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.config.type.ApplicationProperty;
import com.communote.server.web.commons.resource.ConcatenatedResourceStore;

/**
 * Controller to stream files managed by the {@link ConcatenatedResourceStore}.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ResourceStoreDownloadController {
    private static final Logger LOGGER = LoggerFactory
            .getLogger(ResourceStoreDownloadController.class);
    // 7 days
    private final static int MAX_AGE_IN_MILLIS = 1000 * 60 * 60 * 24 * 7;
    private final static int MAX_AGE_IN_SECONDS = 60 * 60 * 24 * 7;
    private final ConcatenatedResourceStore concatenatedResourceStore;

    private final boolean handlesConcatenatedFile;

    private ApplicationProperty minimizedSetting;

    /**
     * Constructor.
     *
     * @param concatenatedResourceStore
     *            The resource store used by this controller.
     * @param handleConcatenatedFile
     *            whether to stream the concatenated file or the property resource
     */
    public ResourceStoreDownloadController(ConcatenatedResourceStore concatenatedResourceStore,
            boolean handleConcatenatedFile) {
        this.concatenatedResourceStore = concatenatedResourceStore;
        this.handlesConcatenatedFile = handleConcatenatedFile;
        if (concatenatedResourceStore.getContentType().equals("text/javascript")) {
            minimizedSetting = ApplicationProperty.SCRIPTS_COMPRESS;
        } else if (concatenatedResourceStore.getContentType().equals("text/css")) {
            minimizedSetting = ApplicationProperty.STYLES_COMPRESS;
        }
    }

    /**
     * Handler for a GET request which will stream the content of the resource category.
     *
     * @param request
     *            the servlet request
     * @param response
     *            the response
     * @param categoryName
     *            is populated with value of the category parameter which defines the category to
     *            retrieve
     * @param suffix
     *            is populated with the value of the suffix parameter which defines whether the
     *            minimized (value: -min) or original (any other value, preferably: -org) version of
     *            the resource should be returned. If this parameter is missing the configuration
     *            for the content of the resource store is used and if undefined the minimized
     *            version is returned.
     */
    @RequestMapping(method = RequestMethod.GET)
    public void download(HttpServletRequest request, HttpServletResponse response,
            @RequestParam(value = "category", required = true) String categoryName,
            @RequestParam(value = "suffix", required = false) String suffix) throws Exception {
        boolean minimized;
        if (suffix == null) {
            if (minimizedSetting != null) {
                minimized = CommunoteRuntime.getInstance().getConfigurationManager()
                        .getApplicationConfigurationProperties()
                        .getProperty(minimizedSetting, true);
            } else {
                minimized = true;
            }
        } else {
            minimized = suffix.equals(concatenatedResourceStore.getMinimizedSuffix());
        }
        File file = getFile(categoryName, minimized);
        long concatedFileLastModified = getTimestamp(categoryName);
        if (file == null || concatedFileLastModified < 0) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        // round timestamp down to nearest second, since the date header is not supporting
        // milliseconds
        concatedFileLastModified = (concatedFileLastModified / 1000L) * 1000L;
        if (concatedFileLastModified == request.getDateHeader("If-Modified-Since")) {
            response.setStatus(304);
            return;
        }
        response.setContentType(concatenatedResourceStore.getContentType());
        response.setContentLength((int) file.length());
        response.addDateHeader("Last-Modified", concatedFileLastModified);
        response.addDateHeader("Expires", System.currentTimeMillis() + MAX_AGE_IN_MILLIS);
        response.setHeader("Cache-Control", "max-age=" + MAX_AGE_IN_SECONDS + ", public");
        FileInputStream input = null;
        try {
            input = new FileInputStream(file);
            IOUtils.copy(input, response.getOutputStream());
        } catch (IOException e) {
            LOGGER.error("Writing concatenated file for category " + categoryName + " failed", e);
            throw e;
        } finally {
            IOUtils.closeQuietly(input);
        }
    }

    /**
     * Get the file for the category. Will return the concatenated file or the property resource
     * file depending on what is handled by this instance.
     *
     * @param categoryName
     *            the name of the category
     * @param minimized
     *            whether to get the minimized version of the file
     * @return the file or null if the category is not known
     */
    protected File getFile(String categoryName, boolean minimized) {
        if (handlesConcatenatedFile) {
            return concatenatedResourceStore.getConcatenatedFile(categoryName, minimized);
        }
        return concatenatedResourceStore.getPropertyResourceFile(categoryName, minimized);
    }

    /**
     * Get the last modification timestamp for the category. Will return the last modification
     * timestamp of the concatenated file or the property resource file depending on what is handled
     * by this instance.
     *
     * @param categoryName
     *            the name of the category
     * @return the timestamp or -1 if the category is not known
     */
    protected long getTimestamp(String categoryName) {
        if (handlesConcatenatedFile) {
            return concatenatedResourceStore.getConcatenatedFileLastModified(categoryName);
        }
        return concatenatedResourceStore.getPropertyResourceLastModified(categoryName);
    }
}
