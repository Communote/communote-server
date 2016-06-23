package com.communote.server.web.commons.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import com.communote.server.web.commons.helper.ControllerHelper;

/**
 * A simple view controller which just forwards the request to a particular tiles view
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class SimpleViewController extends AbstractController {

    private String view;

    /**
     * The name of the view
     *
     * @return the view name
     */
    public String getView() {
        return view;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request,
            HttpServletResponse response) {
        return new ModelAndView(ControllerHelper.replaceModuleInViewName(view));
    }

    /**
     * Set the name of the view
     *
     * @param view
     *            the view
     */
    public void setView(String view) {
        this.view = view;
    }

}
