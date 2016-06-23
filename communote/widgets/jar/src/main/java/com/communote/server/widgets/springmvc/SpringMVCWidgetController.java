package com.communote.server.widgets.springmvc;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

import com.communote.server.widgets.Widget;
import com.communote.server.widgets.WidgetController;

/**
 * WidgetController supporting Spring MVC
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class SpringMVCWidgetController extends WidgetController<ModelAndView> implements Controller {

    /**
     * Prepare the view rendering by storing all required details in a ModelAndView instance.
     * 
     * @param request
     *            the request
     * @param widget
     *            the widget to be rendered
     * @param result
     *            the result returned by the query method of the widget
     * @param resultType
     *            a constant defining the type of the result parameter
     * @return a ModelAndView
     */
    private ModelAndView createModelAndView(HttpServletRequest request, Widget widget,
            Object result, String resultType) {
        ModelAndView mav = new ModelAndView(widget.getViewIdentifier());
        mav.addObject(resultType, result);
        prepareModelAndView(mav, widget);
        return mav;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ModelAndView createMultiResultView(HttpServletRequest request, Widget widget,
            List<?> result) {
        return createModelAndView(request, widget, result, WidgetController.OBJECT_LIST);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ModelAndView createSingleResultView(HttpServletRequest request, Widget widget,
            Object result) {
        ModelAndView mav;
        // not that beautiful but SpringFormWidget returns a model and view
        if (result instanceof ModelAndView) {
            mav = (ModelAndView) result;
            prepareModelAndView(mav, widget);
        } else {
            mav = createModelAndView(request, widget, result, WidgetController.OBJECT_SINGLE);
        }
        return mav;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        ModelAndView result = handleWidgetRequest(request, response);
        return result;
    }

    /**
     * Add additional parameters to the model.
     * 
     * @param mav
     *            the model and view
     * @param widget
     *            the widget
     */
    private void prepareModelAndView(ModelAndView mav, Widget widget) {
        mav.addObject(WidgetController.OBJECT_WIDGET, widget);
    }

}
