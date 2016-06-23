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
 * Controller for delivering the JavaScript-File <code>cntw-all.js</code> for the HTMLClient.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
@Component
@Provides
@Instantiate(name = "ScriptController")
@UrlMappings(mappings = { "/*/resources/javascripts/htmlClient-scripts.js",
"/*/portal/htmlClient-scripts.js" })
public class ScriptController implements Controller {

    private final String bundleName;

    public ScriptController(BundleContext bundleContext) {
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
        Map<String, Object> data = new HashMap<String, Object>();
        ControllerUtils.addStaticCachingHeader(response);
        ControllerUtils.addStaticResourcesBasePath(bundleName, data);
        response.setContentType("text/javascript");
        return new ModelAndView("communote.plugins.htmlClient.scripts", data);
    }

}