package com.communote.plugins.htmlclient;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

import com.communote.plugins.core.views.annotations.UrlMappings;
import com.communote.server.core.osgi.OSGiHelper;

/**
 * Controller for delivering Templates for the HTMLClient.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
@Component
@Provides
@Instantiate(name = "TemplatesController")
@UrlMappings(mappings = { "/*/resources/htmlClient-templates.tmpl.html",
"/*/portal/htmlClient-templates.tmpl.html" })
public class TemplatesController implements Controller {

    /** The Constant TEMPLATE_FILE_EXTENSION. */
    private static final String TEMPLATE_FILE_EXTENSION = ".tmpl.html";

    /** The Constant PATTERN_SPACES. */
    private static final String PATTERN_SPACES = "\\s+";

    /** The Constant STRING_NL_. */
    private static final String STRING_NL_ = "\n";

    /** The Constant STRING_NL_R. */
    private static final String STRING_NL_R = "\r\n";

    /** The Constant STRING_WHITE_SPACE. */
    private static final String STRING_WHITE_SPACE = " ";

    /** The Constant PATTERN_HTML_COMMENTS. */
    private static final String PATTERN_HTML_COMMENTS = "(?s)<!--.*?-->";

    /** The Constant STATIC_TEMPLATES_PATH. */
    private static final String STATIC_TEMPLATES_PATH = "/static/templates";

    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(TemplatesController.class);

    /** The template files map (lazy loaded). */
    private Map<String, String> templateFilesMap = null;

    /** The symbolic bundle name. */
    private final String bundleName;

    /**
     * Instantiates a new templates controller.
     *
     * @param bundleContext
     *            The current bundle context.
     */
    public TemplatesController(BundleContext bundleContext) {
        this.bundleName = bundleContext.getBundle().getSymbolicName();
    }

    /**
     * Gets the template files map by reading from template directory.
     *
     * @return the template files map
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private Map<String, String> getTemplateFilesMap() throws IOException {
        if (templateFilesMap == null) {
            String resouresPath = OSGiHelper.getBundleStorage(bundleName);
            String templateResourcesPath = resouresPath + STATIC_TEMPLATES_PATH;
            Map<String, String> templateFiles = new HashMap<String, String>();
            File dir = new File(templateResourcesPath);
            File[] fileList = dir.listFiles();
            for (File file : fileList) {
                LOGGER.debug("adding file to template map: {}", file.getName());
                String fileContent = FileUtils.readFileToString(file);
                fileContent = fileContent.replaceAll(PATTERN_HTML_COMMENTS, StringUtils.EMPTY)
                        .replace(STRING_NL_R, StringUtils.EMPTY)
                        .replace(STRING_NL_, StringUtils.EMPTY)
                        .replaceAll(PATTERN_SPACES, STRING_WHITE_SPACE);
                String templateName = file.getName().substring(0,
                        file.getName().indexOf(TEMPLATE_FILE_EXTENSION));
                templateFiles.put(templateName, fileContent);
            }
            templateFilesMap = templateFiles;
        }
        return templateFilesMap;
    }

    /**
     * Handle request.
     *
     * @param request
     *            The request.
     * @param response
     *            The response.
     * @return ModelAndView The model and view.
     * @throws Exception
     *             Exception.
     */
    @Override
    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        ControllerUtils.addStaticCachingHeader(response);
        response.setContentType("text/plain");
        Map<String, Object> contextMap = new HashMap<String, Object>();
        Map<String, String> templateFiles = getTemplateFilesMap();
        contextMap.put("templateFiles", templateFiles);
        return new ModelAndView("communote.plugins.htmlClient.templates", contextMap);
    }

}