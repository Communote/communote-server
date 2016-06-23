package com.communote.plugins.htmlclient;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.osgi.framework.BundleContext;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

import com.communote.plugins.core.views.annotations.UrlMappings;

/**
 * Controller for delivering Style Sheets for the HTMLClient.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
@Component
@Provides
@Instantiate(name = "StylesController")
@UrlMappings(mappings = { "/*/resources/styles/htmlClient-styles.css",
        "/*/portal/htmlClient-styles.css" })
public class StylesController implements Controller {

    private final String bundleName;

    public StylesController(BundleContext bundleContext) {
        this.bundleName = bundleContext.getBundle().getSymbolicName();
    }

    /**
     *
     * @param response
     *            The response.
     * @param request
     *            The request.
     *
     * @return ModelAndView The model and view.
     *
     * @throws Exception
     *             Exception.
     */
    @Override
    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        ControllerUtils.addStaticCachingHeader(response);
        response.setContentType("text/css");
        Map<String, Object> data = new HashMap<String, Object>();
        ControllerUtils.addRefererPathToContext(request, data);
        ControllerUtils.addStaticResourcesBasePath(bundleName, data);
        return new ModelAndView("communote.plugins.htmlClient.styles", data);
    }

}