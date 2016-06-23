package com.communote.plugins.embed;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.servlet.ModelAndView;

/**
 * Processor which can be used to modify the ModelAndView before the response of the EmbedController
 * is rendered.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public interface EmbedControllerRequestPostProcessor {

    /**
     * Called after the EmbedController handled the request and prepared the ModelAndView for
     * rendering.
     * 
     * @param request
     *            the current request
     * @param modelAndView
     *            the prepared ModelAndView
     */
    void process(HttpServletRequest request, ModelAndView modelAndView);

}
