package com.communote.plugins.core.views;

import org.osgi.framework.BundleContext;
import org.springframework.web.servlet.mvc.Controller;

import com.communote.server.web.commons.controller.BaseFormController;

/**
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class FormViewController extends BaseFormController implements Controller,
        PluginViewController {

    private final ViewControllerData viewControllerData;

    /**
     * Constructor.
     * 
     * @param bundleContext
     *            The bundles context.
     */
    public FormViewController(BundleContext bundleContext) {
        this(bundleContext.getBundle().getSymbolicName());
    }

    /**
     * Constructor.
     * 
     * @param symbolicName
     *            The symbolic name of the bundle the controller is in.
     * 
     */
    public FormViewController(String symbolicName) {
        viewControllerData = new ViewControllerData(symbolicName, this);
    }

    /**
     * @return {@link PluginViewController#DEFAULT_MAIN_TEMPLATE}
     */
    @Override
    public String getMainTemplate() {
        return PluginViewController.DEFAULT_MAIN_TEMPLATE;
    }

    @Override
    public String getSymbolicName() {
        return viewControllerData.getSymbolicName();
    }

    public ViewControllerData getViewControllerData() {
        return viewControllerData;
    }

}
