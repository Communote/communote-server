package com.communote.plugins.core.views;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.framework.BundleContext;
import org.springframework.web.servlet.ModelAndView;

import com.communote.server.core.security.SecurityHelper;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class AdministrationViewController extends ViewController {

    /**
     * Constructor.
     * 
     * @param bundleContext
     *            The bundles context.
     */
    public AdministrationViewController(BundleContext bundleContext) {
        super(bundleContext);
    }

    /**
     * Constructor.
     * 
     * @param symbolicName
     *            Symbolic name of the
     */
    public AdministrationViewController(String symbolicName) {
        super(symbolicName);
    }

    /**
     * @return {@link PluginViewController#DEFAULT_MAIN_TEMPLATE_ADMINISTRATION}
     */
    @Override
    public String getMainTemplate() {
        return PluginViewController.DEFAULT_MAIN_TEMPLATE_ADMINISTRATION;
    }

    /**
     * {@inheritDoc}
     * 
     * Also check if the current user is a client manager
     */
    @Override
    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        SecurityHelper.assertCurrentUserIsClientManager();
        return super.handleRequest(request, response);
    }
}
