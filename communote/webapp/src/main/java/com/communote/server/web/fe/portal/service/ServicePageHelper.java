package com.communote.server.web.fe.portal.service;

import com.communote.server.web.WebServiceLocator;
import com.communote.server.web.commons.controller.StaticPageContentManager;
import com.communote.server.web.commons.controller.StaticPageSection;
import com.communote.server.web.commons.controller.StaticPageVelocityViewController;

/**
 * Helper for adding and removing static content sections to the service pages.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class ServicePageHelper {

    private static final String BEAN_ID_IMPRINT_PAGE = "showLegalServicePage";
    private static final String BEAN_ID_TOOLS_PAGE = "showToolsServicePage";

    /**
     * Add a section to the imprint page.
     *
     * @param section
     *            the section to add
     */
    public static void addImprintPageSection(StaticPageSection section) {
        getManager(BEAN_ID_IMPRINT_PAGE).addSection(section);
    }

    /**
     * Add a section to the tools page.
     *
     * @param section
     *            the section to add
     */
    public static void addToolsPageSection(StaticPageSection section) {
        getManager(BEAN_ID_TOOLS_PAGE).addSection(section);
    }

    private static StaticPageContentManager getManager(String controllerBeanId) {
        StaticPageVelocityViewController page = WebServiceLocator.findService(controllerBeanId,
                StaticPageVelocityViewController.class);
        return page.getContentManager();
    }

    /**
     * Remove a section from the imprint page
     *
     * @param id
     *            ID of the section to remove
     * @return the removed section or null if there is no section with that ID
     */
    public static StaticPageSection removeImprintPageSection(String id) {
        return getManager(BEAN_ID_IMPRINT_PAGE).removeSection(id);
    }

    /**
     * Remove a section from the tools page
     *
     * @param id
     *            ID of the section to remove
     * @return the removed section or null if there is no section with that ID
     */
    public static StaticPageSection removeToolsPageSection(String id) {
        return getManager(BEAN_ID_TOOLS_PAGE).removeSection(id);
    }

    private ServicePageHelper() {
    }

}
