package com.communote.server.web.commons.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.core.PriorityOrdered;
import org.springframework.util.Assert;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.servlet.handler.AbstractUrlHandlerMapping;
import org.springframework.web.servlet.mvc.Controller;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class DynamicUrlHandlerMapping extends AbstractUrlHandlerMapping implements
PriorityOrdered {

    /** Logger. */
    private final static Logger LOG = LoggerFactory.getLogger(DynamicUrlHandlerMapping.class);

    /** */
    private final Map<Controller, List<String>> dynamicUrlMaps = new HashMap<Controller, List<String>>();

    /** */
    private final Map<String, Controller> mergedUrlMap = new HashMap<String, Controller>();

    /**
     * Look up a handler instance for the given URL path.
     * <p>
     * Supports direct matches, e.g. a registered "/test" matches "/test", and various Ant-style
     * pattern matches, e.g. a registered "/t*" matches both "/test" and "/team". For details, see
     * the AntPathMatcher class.
     * <p>
     * Looks for the most exact pattern, where most exact is defined as the longest path pattern.
     *
     * @param urlPath
     *            URL the bean is mapped to
     * @param request
     *            current HTTP request (to expose the path within the mapping to)
     *
     * @return the associated handler instance, or <code>null</code> if not found
     *
     * @throws Exception
     *             in case of an error
     *
     * @see #exposePathWithinMapping
     * @see org.springframework.util.AntPathMatcher
     */
    @Override
    protected Object lookupHandler(String urlPath, HttpServletRequest request) throws Exception {
        // Direct match?
        Object handler = this.mergedUrlMap.get(urlPath);
        if (handler != null) {
            validateHandler(handler, request);
            return buildPathExposingHandler(handler, urlPath, null, null);
        }
        // Pattern match?
        String bestPathMatch = null;
        for (String registeredPath : this.mergedUrlMap.keySet()) {
            if (getPathMatcher().match(registeredPath, urlPath)
                    && (bestPathMatch == null || bestPathMatch.length() < registeredPath.length())) {
                bestPathMatch = registeredPath;
            }
        }
        if (bestPathMatch != null) {
            handler = this.mergedUrlMap.get(bestPathMatch);
            validateHandler(handler, request);
            String pathWithinMapping = getPathMatcher().extractPathWithinPattern(bestPathMatch,
                    urlPath);
            handler = buildPathExposingHandler(handler, pathWithinMapping, null, null);
        }
        // No handler found ... should be null
        return handler;
    }

    /**
     *
     * @param controller
     *            the controller
     * @param urlPatterns
     *            the url patterns of the controller
     */
    public synchronized void registerController(Controller controller,
            List<String> urlPatterns) {

        setServletContext(controller);

        dynamicUrlMaps.put(controller, urlPatterns);
        reloadHandlerMap();
        LOG.info("Added a controller (" + controller.getClass().getSimpleName() + ") for URLs: "
                + urlPatterns);
    }

    /**
     * Register the specified handler for the given URL path.
     *
     * @param urlPath
     *            the URL the bean should be mapped to
     * @param handler
     *            the handler instance or handler bean name String (a bean name will automatically
     *            be resolved into the corresponding handler bean)
     *
     * @throws BeansException
     *             if the handler couldn't be registered
     * @throws IllegalStateException
     *             if there is a conflicting handler registered
     */
    protected void registerDefaultHandler(String urlPath, Controller handler)
            throws BeansException,
            IllegalStateException {
        Assert.notNull(urlPath, "URL path must not be null");
        Assert.notNull(handler, "Handler object must not be null");
        Object resolvedHandler = handler;

        Object mappedHandler = this.mergedUrlMap.get(urlPath);
        if (mappedHandler != null) {
            if (mappedHandler != resolvedHandler) {
                throw new IllegalStateException(
                        "Cannot map handler [" + handler + "] to URL path [" + urlPath
                        + "]: There is already handler [" + resolvedHandler + "] mapped.");
            }
        } else {
            if (urlPath.equals("/")) {
                setRootHandler(resolvedHandler);
            } else if (urlPath.equals("/*")) {
                setDefaultHandler(resolvedHandler);
            }
        }
    }

    /**
     * reloades the merged url map
     */
    public synchronized void reloadHandlerMap() {
        mergedUrlMap.clear();

        for (Entry<Controller, List<String>> entry : dynamicUrlMaps.entrySet()) {
            for (String urlPattern : entry.getValue()) {
                mergedUrlMap.put(urlPattern, entry.getKey());

            }
        }

        setRootHandler(null);
        setDefaultHandler(null);
        for (Entry<String, Controller> urlMapEntry : mergedUrlMap.entrySet()) {
            registerDefaultHandler(urlMapEntry.getKey(), urlMapEntry.getValue());
        }
    }

    /**
     * Check if the controller implements {@link ServletContextAware} and set the servlet context
     *
     * @param controller
     *            the controller to set the servlet context
     */
    private void setServletContext(Controller controller) {
        if (controller instanceof ServletContextAware) {
            ((ServletContextAware) controller).setServletContext(this.getServletContext());
        }
    }

    /**
     * Unregister all handlers specified in the URL map for the corresponding plugin.
     *
     * @param controller
     *            the controller
     */
    public synchronized void unregisterController(Controller controller) {
        dynamicUrlMaps.remove(controller);
        reloadHandlerMap();
        LOG.info("Removed a controller " + controller.getClass().getSimpleName());
    }
}