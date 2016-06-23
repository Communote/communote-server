package com.communote.plugins.core.views;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.framework.BundleContext;
import org.springframework.web.servlet.ModelAndView;

import com.communote.server.core.security.SecurityHelper;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class AdministrationFormViewController extends FormViewController {

    /**
     * Constructor.
     * 
     * @param bundleContext
     *            The bundles context.
     */
    public AdministrationFormViewController(BundleContext bundleContext) {
        super(bundleContext);
    }

    /**
     * Constructor.
     * 
     * @param symbolicName
     *            Symbolic name of the
     */
    public AdministrationFormViewController(String symbolicName) {
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
     * Lets check if the request is done by a client manager
     */
    @Override
    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        SecurityHelper.assertCurrentUserIsClientManager();

        return super.handleRequest(request, response);
    }

}
