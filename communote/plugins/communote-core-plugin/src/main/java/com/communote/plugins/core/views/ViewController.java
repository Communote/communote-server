package com.communote.plugins.core.views;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

/**
 * Wrapper for controllers. Subclasses must override one of the do HTTP_METHOD methods or the
 * handleRequest for something to happen.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public abstract class ViewController implements Controller, PluginViewController {

    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(ViewController.class);

    private final ViewControllerData viewControllerData;

    /**
     * Constructor.
     * 
     * @param bundleContext
     *            The bundles context.
     */
    public ViewController(BundleContext bundleContext) {
        this(bundleContext.getBundle().getSymbolicName());
    }

    /**
     * Constructor.
     * 
     * @param symbolicName
     *            The symbolic name of the bundle the controller is in.
     * 
     */
    public ViewController(String symbolicName) {
        viewControllerData = new ViewControllerData(symbolicName, this);
    }

    /**
     * Handles DELETE requests.
     * 
     * @param request
     *            The request.
     * @param response
     *            The response.
     * @param model
     *            The model.
     * @throws ViewControllerException
     *             Exception.
     */
    public void doDelete(HttpServletRequest request, HttpServletResponse response,
            Map<String, Object> model) throws ViewControllerException {
        try {
            LOGGER.debug("DELETE method not implemented");
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        } catch (IOException e) {
            LOGGER.warn("Call to undefined method DELETE on controller.");
        }
    }

    /**
     * Handles GET requests.
     * 
     * @param request
     *            The request.
     * @param response
     *            The response.
     * @param model
     *            The model.
     * @throws ViewControllerException
     *             Exception.
     */
    public void doGet(HttpServletRequest request, HttpServletResponse response,
            Map<String, Object> model) throws ViewControllerException {
        try {
            LOGGER.debug("GET method not implemented");
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        } catch (IOException e) {
            LOGGER.warn("Call to undefined method GET on controller.");
        }
    }

    /**
     * Handles HEAD requests.
     * 
     * @param request
     *            The request.
     * @param response
     *            The response.
     * @param model
     *            The model.
     * @throws ViewControllerException
     *             Exception.
     */
    public void doHead(HttpServletRequest request, HttpServletResponse response,
            Map<String, Object> model) throws ViewControllerException {
        try {
            LOGGER.debug("HEAD method not implemented");
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        } catch (IOException e) {
            LOGGER.warn("Call to undefined method HEAD on controller.");
        }
    }

    /**
     * Handles POST requests.
     * 
     * @param request
     *            The request.
     * @param response
     *            The response.
     * @param model
     *            The model.
     * @throws ViewControllerException
     *             Exception.
     */
    public void doPost(HttpServletRequest request, HttpServletResponse response,
            Map<String, Object> model) throws ViewControllerException {
        try {
            LOGGER.debug("POST method not implemented");
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        } catch (IOException e) {
            LOGGER.warn("Call to undefined method POST on controller.");
        }
    }

    /**
     * Handles PUT requests.
     * 
     * @param request
     *            The request.
     * @param response
     *            The response.
     * @param model
     *            The model.
     * @throws ViewControllerException
     *             Exception.
     */
    public void doPut(HttpServletRequest request, HttpServletResponse response,
            Map<String, Object> model) throws ViewControllerException {
        try {
            LOGGER.debug("PUT method not implemented");
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        } catch (IOException e) {
            LOGGER.warn("Call to undefined method PUT on controller.");
        }
    }

    /**
     * Handles UNKNOWN requests.
     * 
     * @param request
     *            The request.
     * @param response
     *            The response.
     * @param model
     *            The model.
     * @throws ViewControllerException
     *             Exception.
     */
    public void doUnknown(HttpServletRequest request, HttpServletResponse response,
            Map<String, Object> model) throws ViewControllerException {
        try {
            LOGGER.debug("Unknown method");
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        } catch (IOException e) {
            LOGGER.warn("Call to undefined method {} on controller.", request.getMethod());
        }
    }

    @Override
    public abstract String getContentTemplate();

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

    /**
     * {@inheritDoc}
     */
    @Override
    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        Map<String, Object> data = viewControllerData.getDataForRequest(request);
        Map<String, ? extends Object> processedDataMap;
        try {
            processedDataMap = processRequest(request, response);
        } catch (ViewControllerException e) {
            LOGGER.error("Processing the request failed", e);
            request.setAttribute("javax.servlet.error.exception", e);
            request.setAttribute("javax.servlet.error.status_code", e.getErrorCode());
            response.sendError(e.getErrorCode(), e.getMessage());
            return null;
        }
        if (processedDataMap != null) {
            data.putAll(processedDataMap);
        }
        viewControllerData.determineScriptsCompression(request, data);
        viewControllerData.determineStylesCompression(request, data);
        return new ModelAndView(getMainTemplate(), data);
    }

    /**
     * Processes the request and returns a map with additional content.
     * 
     * @param request
     *            The request.
     * @param response
     *            The response.
     * @return Map with additional content.
     * @throws ViewControllerException
     *             Exception.
     */
    public Map<String, ? extends Object> processRequest(HttpServletRequest request,
            HttpServletResponse response) throws ViewControllerException {
        Map<String, Object> model = new HashMap<String, Object>();
        String method = request.getMethod();
        if ("GET".equals(method)) {
            doGet(request, response, model);
        } else if ("POST".equals(method)) {
            doPost(request, response, model);
        } else if ("PUT".equals(method)) {
            doPut(request, response, model);
        } else if ("HEAD".equals(method)) {
            doHead(request, response, model);
        } else if ("DELETE".equals(method)) {
            doDelete(request, response, model);
        } else {
            doUnknown(request, response, model);
        }
        return model;
    }

}
