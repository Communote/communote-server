package com.communote.plugins.core.registries;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.felix.ipojo.annotations.Bind;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Unbind;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.mvc.Controller;

import com.communote.plugins.core.StartpageController;
import com.communote.plugins.core.views.annotations.Page;
import com.communote.plugins.core.views.annotations.UrlMapping;
import com.communote.plugins.core.views.annotations.UrlMappings;
import com.communote.server.web.WebServiceLocator;
import com.communote.server.web.commons.controller.DynamicUrlHandlerMapping;
import com.communote.server.web.commons.controller.StartpageRegistry;
import com.communote.server.web.fe.admin.AdministrationMenuManager;
import com.communote.server.web.fe.admin.AdministrationPageMenuEntry;

/**
 * Component which registers a new UrlMappingController.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
@Component
@Instantiate(name = "UrlMappingRegistry")
public class DynamicUrlHandlerMappingRegistry {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(DynamicUrlHandlerMappingRegistry.class);

    private final DynamicUrlHandlerMapping mapper;
    private StartpageRegistry startpageRegistry;

    /**
     * Constructor.
     */
    public DynamicUrlHandlerMappingRegistry() {
        Object service = WebServiceLocator.instance().getWebApplicationContext().getBean(
                "dynamicUrlHandlerMapping");
        if (service == null) {
            throw new RuntimeException("Service \"dynamicUrlHandlerMapping\" not found.");
        }
        mapper = (DynamicUrlHandlerMapping) service;
    }

    /**
     * Add a controller with an admin URL to the administration view space so that it will appear in
     * the navigation of the administration section.
     *
     * @param mapping
     *            the URL mapping of the controller
     * @param controller
     *            the controller
     */
    private void addAdministrationView(UrlMapping mapping, Controller controller) {
        if (mapping.value().startsWith("/*/admin/")) {
            boolean systemAdministration = mapping.value().startsWith("/*/admin/application/") ? true
                    : false;
            Page page = controller.getClass().getAnnotation(Page.class);
            if (page != null) {
                if (StringUtils.isNotBlank(page.menuMessageKey())) {
                    String menuMessageKey = page.menuMessageKey();
                    if (StringUtils.isBlank(page.submenu())) {
                        LOGGER.warn(
                                "Administration controller with URL mapping {} will not appear in "
                                        + "administration navigation because no submenu ID is provided",
                                mapping.value());
                    } else {
                        WebServiceLocator.findService(AdministrationMenuManager.class)
                        .addToExtensionSection(
                                        new AdministrationPageMenuEntry(page.submenu(),
                                                menuMessageKey, mapping.value().replace("/*", "")),
                                        null, systemAdministration);
                        LOGGER.debug("Administration controller with URL mapping {} added to "
                                + "administration navigation", mapping.value());
                    }
                } else {
                    LOGGER.warn("Administration controller with URL mapping {} will not appear in "
                            + "administration navigation because no message key is provided",
                            mapping.value());
                }
            }
        }
    }

    /**
     * @return the lazily initialized start page registry
     */
    private StartpageRegistry getStartpageRegistry() {
        if (this.startpageRegistry == null) {
            this.startpageRegistry = WebServiceLocator.instance().getStartpageRegistry();
        }
        return this.startpageRegistry;
    }

    /**
     * Registers the Service.
     *
     * @param controller
     *            The controller.
     */
    @Bind(id = "controllerRegistry", optional = true, aggregate = true)
    public void register(Controller controller) {
        List<String> urlPatterns = new ArrayList<String>();
        UrlMapping mapping = controller.getClass().getAnnotation(UrlMapping.class);
        if (mapping != null) {
            urlPatterns.add(mapping.value());
            addAdministrationView(mapping, controller);
        }
        UrlMappings mappings = controller.getClass().getAnnotation(UrlMappings.class);
        if (mappings != null) {
            for (String urlMapping : mappings.mappings()) {
                urlPatterns.add(urlMapping);
            }
        }
        mapper.registerController(controller, urlPatterns);

        StartpageController startpageMarker = controller.getClass().getAnnotation(
                StartpageController.class);
        if (startpageMarker != null) {
            if (urlPatterns.size() == 0) {
                LOGGER.warn("Startpage marker ignored because no URL mapping is defined");
            } else {
                registerStartpageController(controller, urlPatterns.get(0));
            }
        } else {
            LOGGER.debug("No startpage marker found");
        }
    }

    /**
     * Registers the start page of the controller as new start page. The start page must be of legal
     * format.
     *
     * @param controller
     *            the controller providing the start page
     * @param urlPattern
     *            the urlPattern from which the start page should be extracted
     */
    private void registerStartpageController(Controller controller, String urlPattern) {
        String startpageUrl = null;
        if (urlPattern.startsWith("/*/")) {
            startpageUrl = urlPattern.substring(2);
        }

        if (startpageUrl == null || startpageUrl.contains("*")) {
            LOGGER.warn("Ignoring invalid start page URL " + urlPattern);
        } else {
            getStartpageRegistry().registerStartpageController(controller, startpageUrl);
        }
    }

    /**
     * Unregisters the Service.
     *
     * @param controller
     *            The controller.
     */
    @Unbind(id = "controllerRegistry", optional = true, aggregate = true)
    public void unregister(Controller controller) {
        mapper.unregisterController(controller);
        // remove startpage mapping if necessary
        StartpageController startpageMarker = controller.getClass().getAnnotation(
                StartpageController.class);
        if (startpageMarker != null) {
            getStartpageRegistry().unregisterStartpageController(controller);
        }
        UrlMapping mapping = controller.getClass().getAnnotation(UrlMapping.class);
        if (mapping != null && mapping.value().startsWith("/*/admin/")) {
            Page page = controller.getClass().getAnnotation(Page.class);
            if (page != null && StringUtils.isNotBlank(page.submenu())) {
                WebServiceLocator.findService(AdministrationMenuManager.class)
                        .removeFromExtensionSection(page.submenu());
            }
        }
    }
}
