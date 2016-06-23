package com.communote.plugins.core.views;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.framework.BundleContext;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

/**
 * Abstract controller which aims to ease development of new widgets.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * @deprecated use the {@link com.communote.server.widgets.WidgetFactory} to provide widgets in your
 *             plugins
 */
@Deprecated
public abstract class WidgetViewController extends ViewController implements Controller {

    /** Parameter name for the widgets id */
    public static final String WIDGET_ID = "widgetId";

    /**
     * 
     * Constructor.
     * 
     * @param bundleContext
     *            The bundle context of this controller.
     */
    public WidgetViewController(BundleContext bundleContext) {
        super(bundleContext);
    }

    /**
     * Not used.
     * 
     * @return null.
     */
    @Override
    public String getContentTemplate() {
        return null;
    }

    /**
     * @return The name of the template to be used for the widget.
     */
    public abstract String getWidgetTemplate();

    /**
     * {@inheritDoc}
     */
    @Override
    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        ModelAndView modelAndView = super.handleRequest(request, response);
        Map<String, Object> model = modelAndView.getModel();
        if (model == null) {
            model = new HashMap<String, Object>();
        }
        model.put(WIDGET_ID, request.getParameter(WIDGET_ID));
        modelAndView.setViewName(getWidgetTemplate());
        return modelAndView;
    }
}
