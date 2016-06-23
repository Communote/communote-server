package com.communote.server.web.commons.viewtool;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.velocity.tools.config.DefaultKey;

import com.communote.common.menu.CategoryEntry;
import com.communote.common.menu.SimpleMenu;
import com.communote.server.web.WebServiceLocator;
import com.communote.server.web.fe.admin.AdministrationMenuEntryDetails;
import com.communote.server.web.fe.admin.AdministrationMenuManager;
import com.communote.server.web.fe.admin.AdministrationPageMenuEntry;
import com.communote.server.web.fe.admin.BasicMenuEntry;

/**
 * Velocity tool for rendering the Communote administration section.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
@DefaultKey("administrationTool")
public class AdministrationTool {

    /**
     * Delegates to {@link AdministrationMenuManager#getAccountMenu()}
     *
     * @return the account menu
     * @see AdministrationMenuManager#getAccountMenu()
     */
    public SimpleMenu<BasicMenuEntry, AdministrationPageMenuEntry> getAccountMenu() {
        return WebServiceLocator.findService(AdministrationMenuManager.class).getAccountMenu();
    }

    public String getCssClassForPageContent(AdministrationPageMenuEntry menuEntry) {
        if (menuEntry != null) {
            return menuEntry.getId().replace('.', '-');
        }
        return StringUtils.EMPTY;
    }

    /**
     * Delegates to {@link AdministrationMenuManager#getCurrentEntry(HttpServletRequest)}
     *
     * @param request
     *            the current request
     * @return the current entry or null
     * @see AdministrationMenuManager#getCurrentEntry(HttpServletRequest)
     */
    public AdministrationMenuEntryDetails getCurrentMenuEntry(HttpServletRequest request) {
        return WebServiceLocator.findService(AdministrationMenuManager.class).getCurrentEntry(
                request);
    }

    /**
     * Delegates to {@link AdministrationMenuManager#getExtensions()}
     *
     * @return the available extensions
     * @see AdministrationMenuManager#getExtensions()
     */
    public CategoryEntry<BasicMenuEntry, AdministrationPageMenuEntry> getExtensions() {
        return WebServiceLocator.findService(AdministrationMenuManager.class).getExtensions();
    }

    /**
     * Delegates to {@link AdministrationMenuManager#getSystemMenu()}
     *
     * @return the system menu
     * @see AdministrationMenuManager#getSystemMenu()
     */
    public SimpleMenu<BasicMenuEntry, AdministrationPageMenuEntry> getSystemMenu() {
        return WebServiceLocator.findService(AdministrationMenuManager.class).getSystemMenu();
    }
}
