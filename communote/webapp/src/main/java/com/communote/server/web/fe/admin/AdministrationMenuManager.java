package com.communote.server.web.fe.admin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.communote.common.menu.CategoryEntry;
import com.communote.common.menu.MenuEntryFilter;
import com.communote.common.menu.PositionDescriptor;
import com.communote.common.menu.SimpleMenu;
import com.communote.common.menu.SimpleMenuBuilder;
import com.communote.common.util.Pair;
import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.persistence.user.client.ClientHelper;

/**
 * Manager of the navigation menu of the administration section. The navigation menu consists of
 * menu entries that point to the administration pages and categories that group these entries
 * semantically.
 * <p>
 * The categories are distributed over two menus and an additional extension section. The two menus
 * are the system and the account menu. The system menu is intended to hold entries to configure and
 * administer the Communote installation (server settings, logging, ...). The account menu holds
 * entries which are relevant to configure an account (aka a client in a non-standalone environment)
 * like user or group management. When running a non-standalone version the system menu will only be
 * available to the global client.
 * </p>
 * <p>
 * System and account menu can be extended with new categories and entries for the existing or new
 * categories. Additionally filters can be added to include or exclude certain categories or entries
 * in specific situations. Administration menu entries of extensions that should not be added to a
 * category can just be added to the additional extension section.
 * </p>
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
@Service
public class AdministrationMenuManager {
    private static final String URL_PREFIX = "/admin/";
    private static final Logger LOGGER = LoggerFactory.getLogger(AdministrationMenuManager.class);
    private static final String EXTENSIONS_CATEGORY = "system-extension";
    private static final String EXTENSIONS_CATEGORY_ENTRY_OVERVIEW = EXTENSIONS_CATEGORY
            + "_overview";
    public static final String SYSTEM_CATEGORY_CONTENT = "system-content";
    public static final String SYSTEM_CATEGORY_LOGGING = "system-logging";
    public static final String SYSTEM_CATEGORY_COMMUNICATION = "system-communication";
    public static final String SYSTEM_CATEGORY_APPLICATION = "system-application";
    public static final String ACCOUNT_CATEGORY_SECURITY = "admin-security";
    public static final String ACCOUNT_CATEGORY_INTEGRATION = "admin-integration";
    public static final String ACCOUNT_CATEGORY_USER_MANAGEMENT = "admin-usermanagement";
    public static final String ACCOUNT_CATEGORY_PROFILE = "admin-clientprofil";
    public static final String ACCOUNT_CATEGORY_OVERVIEW = "admin-communote";

    private final SimpleMenuBuilder<BasicMenuEntry, AdministrationPageMenuEntry> accountMenu = new SimpleMenuBuilder<>();
    private final SimpleMenuBuilder<BasicMenuEntry, AdministrationPageMenuEntry> systemMenu = new SimpleMenuBuilder<>();
    private final SimpleMenuBuilder<BasicMenuEntry, AdministrationPageMenuEntry> extensionMenu = new SimpleMenuBuilder<>();
    private GlobalClientFilter<AdministrationPageMenuEntry> accountMenuGlobalClientFilter;
    private GlobalClientFilter<AdministrationPageMenuEntry> extensionMenuGlobalClientFilter;
    private final HashSet<String> coreSystemMenuCategories = new HashSet<>();
    private final HashSet<String> coreSystemMenuCategoryEntries = new HashSet<>();
    private final HashSet<String> coreAccountMenuCategories = new HashSet<>();
    private final HashSet<String> coreAccountMenuCategoryEntries = new HashSet<>();
    private final HashMap<String, List<Pair<String, String>>> urlToEntryMapping = new HashMap<>();

    /**
     * Add a new category to the account menu. If there is already a category with that ID the new
     * one is ignored.
     * <p>
     * If called several times with position type <code>TOP</code> the categories which were added
     * later will be positioned before those added earlier. If called several times with position
     * type <code>BOTTOM</code> or <code>AFTER</code> (and the same menu entry ID to position
     * after), the categories which were added later will be positioned after those added earlier.
     * Entries with position type <code>TOP</code> are always before the <code>BOTTOM</code>
     * positioned categories. Categories with position type <code>AFTER</code> will not appear in
     * the resulting menu if the referenced category does not exist. However, if that referenced
     * category is added later on, the categories to position after that category will than be
     * contained in the menu.
     * </p>
     * <p>
     * To position after one of the core account menu categories the menu entry ID of the position
     * descriptor can be set to one of the <code>ACCOUNT_CATEGORY_*</code> constants.
     * </p>
     *
     * @param category
     *            the category to add. The ID of the category must not be blank or contain
     *            whitespaces.
     * @param positionDescriptor
     *            descriptor defining where the item should be added. If null the category will be
     *            added to the bottom.
     * @throws IllegalArgumentException
     *             in case the ID of the category is not valid
     */
    public void addAccountMenuCategory(BasicMenuEntry category,
            PositionDescriptor positionDescriptor) {
        assertValidCategory(category);
        if (accountMenu.addCategory(category, positionDescriptor)) {
            LOGGER.debug("Added category with ID {} to account menu", category.getId());
        }
    }

    /**
     * Add a filter that will be called when the system menu is built to decide whether a menu entry
     * should be added to its category.
     * <p>
     * Note: there is already a filter for excluding certain entries if the current client is not
     * the global client. See {@link #excludeAccountMenuEntryOnGlobalClient(String)}
     * </p>
     *
     * @param filter
     *            the filter to add
     */
    public void addAccountMenuCategoryEntryFilter(
            MenuEntryFilter<AdministrationPageMenuEntry> filter) {
        accountMenu.addCategoryEntryFilter(filter);
    }

    /**
     * Add a filter that will be called when the account menu is built to decide whether a category
     * should be included in the menu.
     *
     * @param filter
     *            the filter to add
     */
    public void addAccountMenuCategoryFilter(MenuEntryFilter<BasicMenuEntry> filter) {
        accountMenu.addCategoryFilter(filter);
    }

    private void addCoreAccountMenuCategory(String id, String messageKey) {
        accountMenu.addCategory(new BasicMenuEntry(id, messageKey), null);
        coreAccountMenuCategories.add(id);
    }

    private void addCoreAccountMenuCategoryEntry(String categoryId, String suffix,
            String messageKey, String pageUrl) {
        String id = categoryId + suffix;
        accountMenu.addToCategory(categoryId, new AdministrationPageMenuEntry(id, messageKey,
                pageUrl), null);
        coreAccountMenuCategoryEntries.add(id);
        addUrlLookup(pageUrl, categoryId, id);
    }

    private void addCoreSystemMenuCategory(String id, String messageKey) {
        systemMenu.addCategory(new BasicMenuEntry(id, messageKey), null);
        coreSystemMenuCategories.add(id);
    }

    private void addCoreSystemMenuCategoryEntry(String categoryId, String suffix,
            String messageKey, String pageUrl) {
        String id = categoryId + suffix;
        systemMenu.addToCategory(categoryId, new AdministrationPageMenuEntry(id, messageKey,
                pageUrl), null);
        coreSystemMenuCategoryEntries.add(id);
        addUrlLookup(pageUrl, categoryId, id);
    }

    /**
     * Same as {@link #addAccountMenuCategory(BasicMenuEntry, PositionDescriptor)} except that the
     * category is added to the system menu.
     *
     * <p>
     * To position after one of the core system menu categories the menu entry ID of the position
     * descriptor can be set to one of the <code>SYSTEM_CATEGORY_*</code> constants.
     * </p>
     *
     *
     * @param category
     *            the category to add. The ID of the category must not be blank or contain
     *            whitespaces.
     * @param positionDescriptor
     *            descriptor defining where the item should be added. If null the category will be
     *            added to the bottom.
     * @throws IllegalArgumentException
     *             in case the ID of the category is not valid
     * @see AdministrationMenuManager#addAccountMenuCategory(BasicMenuEntry, PositionDescriptor)
     */
    public void addSystemMenuCategory(BasicMenuEntry category, PositionDescriptor positionDescriptor) {
        assertValidCategory(category);
        if (accountMenu.addCategory(category, positionDescriptor)) {
            LOGGER.debug("Added category with ID {} to system menu", category.getId());
        }
    }

    /**
     * Add a filter that will be called when the system menu is built to decide whether a menu entry
     * should be added to its category.
     *
     * @param filter
     *            the filter to add
     */
    public void addSystemMenuCategoryEntryFilter(MenuEntryFilter<AdministrationPageMenuEntry> filter) {
        systemMenu.addCategoryEntryFilter(filter);
    }

    /**
     * Add a filter that will be called when the system menu is built to decide whether a category
     * should be included in the menu.
     *
     * @param filter
     *            the filter to add
     */
    public void addSystemMenuCategoryFilter(MenuEntryFilter<BasicMenuEntry> filter) {
        systemMenu.addCategoryFilter(filter);
    }

    /**
     * Add a menu entry to a category of the account menu. If the category does not exist the entry
     * won't be contained in the menu. But if the category is added later on this entry will be
     * added automatically to the category.
     * <p>
     * If called several times with position type <code>TOP</code> the entries which were added
     * later will be positioned before those added earlier. If called several times with position
     * type <code>BOTTOM</code> or <code>AFTER</code> (and the same menu entry ID to position
     * after), the entries which were added later will be positioned after those added earlier.
     * Entries with position type <code>TOP</code> are always before the <code>BOTTOM</code>
     * positioned entries. Entries with position type <code>AFTER</code> will not appear in the
     * resulting menu if the referenced menu entry does not exist. However, if that referenced entry
     * is added later on, the entries to position after that entry will than be contained in the
     * category.
     * </p>
     *
     * @param categoryId
     *            the ID of the category. The <code>ACCOUNT_CATEGORY_*</code> constants can be used
     *            to add the entry to one of the core account menu categories.
     * @param entry
     *            the entry to add. The URL of the entry has to start with <code>/admin/</code> and
     *            the ID must not be blank or contain whitespaces otherwise an exception is thrown.
     * @param positionDescriptor
     *            descriptor defining where the item should be added. If null the item will be added
     *            to the bottom
     * @throws IllegalArgumentException
     *             in case the entry is not valid
     */
    public void addToAccountMenuCategory(String categoryId, AdministrationPageMenuEntry entry,
            PositionDescriptor positionDescriptor) {
        assertValidEntry(entry);
        if (accountMenu.addToCategory(categoryId, entry, positionDescriptor)) {
            addUrlLookup(entry.getPageUrl(), categoryId, entry.getId());
            LOGGER.debug("Added entry with ID {} to account menu category with ID {}",
                    entry.getId(), categoryId);
        }
    }

    /**
     * Add an entry to the extension section.
     * <p>
     * If called several times with position type <code>TOP</code> the entries which were added
     * later will be positioned before those added earlier. If called several times with position
     * type <code>BOTTOM</code> or <code>AFTER</code> (and the same menu entry ID to position
     * after), the entries which were added later will be positioned after those added earlier.
     * Entries with position type <code>TOP</code> are always before the <code>BOTTOM</code>
     * positioned entries. Entries with position type <code>AFTER</code> will not appear in the
     * resulting menu if the referenced menu entry does not exist. However, if that referenced entry
     * is added later on, the entries to position after that entry will than be contained in the
     * category.
     * </p>
     *
     * @param entry
     *            the entry to add. The URL of the entry has to start with <code>/admin/</code> and
     *            the ID must not be blank or contain whitespaces otherwise an exception is thrown.
     * @param positionDescriptor
     *            descriptor defining where the item should be added. If null the item will be added
     *            to the bottom
     * @param systemPage
     *            true if the page is intended to configure or administer the installation, false if
     *            it is intended for the account. This is similar to the differentiation between
     *            system and account menu. With a non-standalone installation, a systemPage will
     *            only be included if it is rendered on the global client
     * @throws IllegalArgumentException
     *             in case the entry is not valid
     */
    public void addToExtensionSection(AdministrationPageMenuEntry entry,
            PositionDescriptor positionDescriptor, boolean systemPage) {
        assertValidEntry(entry);
        if (extensionMenu.addToCategory(EXTENSIONS_CATEGORY, entry, positionDescriptor)) {
            if (systemPage
                    && !CommunoteRuntime.getInstance().getApplicationInformation().isStandalone()) {
                // exclude on a client which is not the global client
                extensionMenuGlobalClientFilter.addEntryToInclude(entry.getId());
            }
            addUrlLookup(entry.getPageUrl(), EXTENSIONS_CATEGORY, entry.getId());
            LOGGER.debug("Added entry with ID {} to extension section", entry.getId());
        }
    }

    /**
     * Same as
     * {@link #addToAccountMenuCategory(String, AdministrationPageMenuEntry, PositionDescriptor)}
     * except that the entry is added to a category of the system menu.
     *
     * @param categoryId
     *            the ID of the category. The <code>SYSTEM_CATEGORY_*</code> constants can be used
     *            to add the entry to one of the core system menu categories.
     * @param entry
     *            the entry to add. The URL of the entry has to start with <code>/admin/</code> and
     *            the ID must not be blank or contain whitespaces otherwise an exception is thrown.
     * @param positionDescriptor
     *            descriptor defining where the item should be added. If null the item will be added
     *            to the bottom
     * @see #addToAccountMenuCategory(String, AdministrationPageMenuEntry, PositionDescriptor)
     * @throws IllegalArgumentException
     *             in case the entry is not valid
     */
    public void addToSystemMenuCategory(String categoryId, AdministrationPageMenuEntry entry,
            PositionDescriptor positionDescriptor) {
        assertValidEntry(entry);
        if (systemMenu.addToCategory(categoryId, entry, positionDescriptor)) {
            addUrlLookup(entry.getPageUrl(), categoryId, entry.getId());
            LOGGER.debug("Added entry with ID {} to system menu category with ID {}",
                    entry.getId(), categoryId);
        }
    }

    private synchronized void addUrlLookup(String url, String categoryId, String entryId) {
        List<Pair<String, String>> urlEntries = urlToEntryMapping.get(url);
        if (urlEntries == null) {
            urlEntries = new ArrayList<>();
            urlToEntryMapping.put(url, urlEntries);
        }
        urlEntries.add(new Pair<String, String>(categoryId, entryId));
    }

    private void assertValidCategory(BasicMenuEntry entry) {
        if (StringUtils.isBlank(entry.getId()) || entry.getId().contains(" ")) {
            throw new IllegalArgumentException("The ID of the category is not valid: "
                    + entry.getId());
        }
    }

    private void assertValidEntry(AdministrationPageMenuEntry entry) {
        if (StringUtils.isBlank(entry.getId()) || entry.getId().contains(" ")) {
            throw new IllegalArgumentException("The ID of the entry is not valid: " + entry.getId());
        }
        if (entry.getPageUrl() == null || !entry.getPageUrl().startsWith(URL_PREFIX)) {
            throw new IllegalArgumentException("The URL of the entry is not valid: "
                    + entry.getPageUrl());
        }
    }

    /**
     * Exclude a account menu entry if the menu is rendered on the global client. This is a
     * convenience function which will automatically add (and reuse) an appropriate filter.
     *
     * @param menuEntryId
     *            the ID of the menu entry to exclude
     */
    public void excludeAccountMenuEntryOnGlobalClient(String menuEntryId) {
        if (this.accountMenuGlobalClientFilter == null) {
            synchronized (this) {
                this.accountMenuGlobalClientFilter = new GlobalClientFilter<>();
                this.addAccountMenuCategoryEntryFilter(accountMenuGlobalClientFilter);
            }
        }
        accountMenuGlobalClientFilter.addEntryToExclude(menuEntryId);
    }

    public SimpleMenu<BasicMenuEntry, AdministrationPageMenuEntry> getAccountMenu() {
        return accountMenu.buildMenu();
    }

    /**
     * Return the entry that belongs to the current request. The entry is retrieved by checking the
     * URL of the current request to against the registered administration menu entries. If there is
     * a match and the category exists the entry will be returned, even if it is not in the
     * extensions, system or account menu as returned by their getters, because the filters are not
     * applied.
     *
     * @param request
     *            the current request
     * @return the entry or null if the request does not belong to a registered page
     */
    public AdministrationMenuEntryDetails getCurrentEntry(HttpServletRequest request) {
        String uri = request.getServletPath();
        if (request.getPathInfo() != null) {
            uri += request.getPathInfo();
        }
        boolean forwardChecked = false;
        if (uri.contains("/WEB-INF/") && uri.endsWith(".jsp")) {
            // called from a jsp which is usually invoked by forwarding the request to the jsp. This
            // overrides the path information.
            uri = getOriginalRequestUri(request);
            forwardChecked = true;
        }
        int idx = uri.indexOf(URL_PREFIX);
        if (idx < 0 && !forwardChecked) {
            uri = getOriginalRequestUri(request);
            idx = uri.indexOf(URL_PREFIX);
        }
        if (idx >= 0) {
            uri = uri.substring(idx);
            List<Pair<String, String>> entries = getEntriesFromUrlLookup(uri);
            if (entries != null && entries.size() > 0) {
                // always take last one
                try {
                    Pair<String, String> entry = entries.get(entries.size() - 1);
                    return getEntry(entry.getLeft(), entry.getRight());
                } catch (IndexOutOfBoundsException e) {
                    // can happen if entry got removed by another thread -> ignore
                }
            }
        }
        return null;
    }

    private List<Pair<String, String>> getEntriesFromUrlLookup(String uri) {
        List<Pair<String, String>> entries = urlToEntryMapping.get(uri);
        if (entries == null) {
            // check for old .do extension
            if (uri.endsWith(".do")) {
                entries = urlToEntryMapping.get(uri.substring(0, uri.length() - 3));
            }
        }
        return entries;
    }

    /**
     * Get an entry of a given category. This will also return entries which would be filtered out
     * and thus would not be in the system or account menu or the extensions returned by their
     * getters. However, if the category does not exist the entry is not returned.
     *
     * @param categoryId
     *            the ID of the category
     * @param entryId
     *            the ID of the entry in the category
     * @return the details of the entry or null if the entry or the category does not exist
     */
    public AdministrationMenuEntryDetails getEntry(String categoryId, String entryId) {
        BasicMenuEntry category;
        AdministrationPageMenuEntry entry = null;
        if (EXTENSIONS_CATEGORY.equals(categoryId)) {
            entry = extensionMenu.getCategoryEntry(categoryId, entryId);
            category = extensionMenu.getCategory(categoryId);
        } else {
            category = accountMenu.getCategory(categoryId);
            if (category == null) {
                category = systemMenu.getCategory(categoryId);
                if (category != null) {
                    entry = systemMenu.getCategoryEntry(categoryId, entryId);
                }
            } else {
                entry = accountMenu.getCategoryEntry(categoryId, entryId);
            }
        }
        if (entry != null) {
            return new AdministrationMenuEntryDetails(category, entry);
        }
        return null;
    }

    /**
     * @return the administration menu entries that were added to the extensions section. Because of
     *         filtering the entries can be empty.
     */
    public CategoryEntry<BasicMenuEntry, AdministrationPageMenuEntry> getExtensions() {
        SimpleMenu<BasicMenuEntry, AdministrationPageMenuEntry> extensions = extensionMenu
                .buildMenu();
        return extensions.getCategories().get(0);
    }

    private String getOriginalRequestUri(HttpServletRequest request) {
        Object attribute = request.getAttribute(RequestDispatcher.FORWARD_REQUEST_URI);
        if (attribute != null) {
            return attribute.toString();
        }
        return StringUtils.EMPTY;
    }

    public SimpleMenu<BasicMenuEntry, AdministrationPageMenuEntry> getSystemMenu() {
        if (!CommunoteRuntime.getInstance().getApplicationInformation().isStandalone()
                && !ClientHelper.isCurrentClientGlobal()) {
            // return an empty menu because application settings are only available on global client
            // note: same could be achieved with an entry filter for the categories but this is way
            // faster
            return new SimpleMenu<>();
        }
        return systemMenu.buildMenu();
    }

    @PostConstruct
    protected void init() {
        // add core menu items
        prepareCoreAccountMenu();
        prepareCoreSystemMenu();
        prepareExtensionMenu();
    }

    private void prepareCoreAccountMenu() {
        String categoryId = ACCOUNT_CATEGORY_OVERVIEW;
        addCoreAccountMenuCategory(categoryId, "client.administration.communote");
        addCoreAccountMenuCategoryEntry(categoryId, "_welcome", "client.administration.overview",
                "/admin/client/welcome");

        categoryId = ACCOUNT_CATEGORY_PROFILE;
        addCoreAccountMenuCategory(categoryId, "administration.title.menu.admin-clientprofil");
        addCoreAccountMenuCategoryEntry(categoryId, "_general-settings",
                "administration.client.generalSettings.title",
                "/admin/client/profile/general-settings");
        addCoreAccountMenuCategoryEntry(categoryId, "_logos",
                "administration.title.submenu.admin-clientprofil.logos",
                "/admin/client/profile/logo");
        addCoreAccountMenuCategoryEntry(categoryId, "_email", "client.change.email.title",
                "/admin/client/profile/email");
        addCoreAccountMenuCategoryEntry(categoryId, "_notifications",
                "administration.title.submenu.admin-clientprofil.notifications",
                "/admin/client/profile/notifications");

        categoryId = ACCOUNT_CATEGORY_USER_MANAGEMENT;
        addCoreAccountMenuCategory(categoryId, "administration.title.menu.user-management");
        addCoreAccountMenuCategoryEntry(categoryId, "_usermanagement",
                "administration.title.menu.user-management", "/admin/client/usermanagementview");
        addCoreAccountMenuCategoryEntry(categoryId, "_usergroupmanagement",
                "client.user.group.management", "/admin/client/usergroupmanagement");
        addCoreAccountMenuCategoryEntry(categoryId, "_settings",
                "administration.title.submenu.user-management.settings",
                "/admin/client/users/settings");
        addCoreAccountMenuCategoryEntry(categoryId, "_imprint",
                "client.customization.localization.key.imprint", "/admin/client/customize/imprint");
        addCoreAccountMenuCategoryEntry(categoryId, "_termsofuse",
                "client.customization.localization.key.termsofuse",
                "/admin/client/customize/termsofuse/show");

        categoryId = ACCOUNT_CATEGORY_INTEGRATION;
        addCoreAccountMenuCategory(categoryId, "client.authentication");
        addCoreAccountMenuCategoryEntry(categoryId, "_overview", "client.integration.overview",
                "/admin/client/integration/overview");
        addCoreAccountMenuCategoryEntry(categoryId, "_confluence",
                "client.authentication.confluence.link.text",
                "/admin/client/confluenceAuthentication");
        addCoreAccountMenuCategoryEntry(categoryId, "_ldap",
                "client.authentication.ldap.link.text", "/admin/client/ldapAuthentication");

        categoryId = ACCOUNT_CATEGORY_SECURITY;
        addCoreAccountMenuCategory(categoryId, "client.security");
        addCoreAccountMenuCategoryEntry(categoryId, "_authentication",
                "client.security.authentication", "/admin/client/security/authentication");
        addCoreAccountMenuCategoryEntry(categoryId, "_permissions",
                "client.security.permissions.link.text", "/admin/client/permissions");
        addCoreAccountMenuCategoryEntry(categoryId, "_iprange", "client.iprange.link.text",
                "/admin/client/iprange");
        addCoreAccountMenuCategoryEntry(categoryId, "_sslconfig", "client.security.ssl.link.text",
                "/admin/client/sslConfiguration");
        HttpsSupportFilter<AdministrationPageMenuEntry> httpsSupportFilter = new HttpsSupportFilter<>();
        httpsSupportFilter.addEntryToInclude(categoryId + "_sslconfig");
        addAccountMenuCategoryEntryFilter(httpsSupportFilter);
    }

    private void prepareCoreSystemMenu() {
        String categoryId = SYSTEM_CATEGORY_APPLICATION;
        addCoreSystemMenuCategory(categoryId, "client.system.application");
        addCoreSystemMenuCategoryEntry(categoryId, "_server", "client.system.application.server",
                "/admin/application/general/server");
        addCoreSystemMenuCategoryEntry(categoryId, "_virusscanner",
                "client.system.application.virusscanning",
                "/admin/application/general/virus-scanner");
        addCoreSystemMenuCategoryEntry(categoryId, "_certificates",
                "client.system.application.certificate", "/admin/application/general/certificate");
        addCoreSystemMenuCategoryEntry(categoryId, "_cache",
                "client.system.application.cacheinvalidation",
                "/admin/application/general/cache-invalidation");

        categoryId = SYSTEM_CATEGORY_COMMUNICATION;
        addCoreSystemMenuCategory(categoryId, "client.system.communication");
        addCoreSystemMenuCategoryEntry(categoryId, "_mail-in",
                "client.system.communication.mail.in", "/admin/application/communication/mail-in");
        addCoreSystemMenuCategoryEntry(categoryId, "_mail-out",
                "client.system.communication.mail.out", "/admin/application/communication/mail-out");
        addCoreSystemMenuCategoryEntry(categoryId, "_xmpp", "client.system.communication.xmpp",
                "/admin/application/communication/xmpp");

        categoryId = SYSTEM_CATEGORY_LOGGING;
        addCoreSystemMenuCategory(categoryId, "client.system.logging");
        addCoreSystemMenuCategoryEntry(categoryId, "_overview", "client.system.logging.overview",
                "/admin/application/logging/overview");

        categoryId = SYSTEM_CATEGORY_CONTENT;
        addCoreSystemMenuCategory(categoryId, "client.system.content");
        addCoreSystemMenuCategoryEntry(categoryId, "_storage",
                "client.system.content.file.storage", "/admin/application/contents/file-storage");
        addCoreSystemMenuCategoryEntry(categoryId, "_upload", "client.system.content.file.upload",
                "/admin/application/contents/file-upload");
    }

    private void prepareExtensionMenu() {
        PositionDescriptor defaultPosDescriptor = new PositionDescriptor();
        String categoryId = EXTENSIONS_CATEGORY;
        extensionMenu.addCategory(new BasicMenuEntry(categoryId, "client.system.extensions"),
                defaultPosDescriptor);
        extensionMenuGlobalClientFilter = new GlobalClientFilter<>();
        extensionMenu.addCategoryEntryFilter(extensionMenuGlobalClientFilter);
        addToExtensionSection(new AdministrationPageMenuEntry(EXTENSIONS_CATEGORY_ENTRY_OVERVIEW,
                "client.system.extensions.overview",
                "/admin/application/extensions/extensions-overview"), defaultPosDescriptor, true);
    }

    /**
     * Remove a previously added category from the account menu. The items that were added to that
     * category are not removed but will instead just not be in the account menu. If the category is
     * added again the items will also be contained in the menu again. The built-in core categories
     * cannot be removed.
     *
     * @param categoryId
     *            the ID of the category to remove.
     */
    public void removeAccountMenuCategory(String categoryId) {
        if (coreAccountMenuCategories.contains(categoryId)) {
            LOGGER.warn("Cannot remove core category {}", categoryId);
        } else if (accountMenu.removeCategory(categoryId)) {
            LOGGER.debug("Removed category with ID {} to account menu", categoryId);
        }
    }

    /**
     * Remove a previously added filter
     *
     * @param filter
     *            the filter to remove
     */
    public void removeAccountMenuCategoryEntryFilter(
            MenuEntryFilter<AdministrationPageMenuEntry> filter) {
        accountMenu.removeCategoryEntryFilter(filter);
    }

    /**
     * Remove a previously added filter
     *
     * @param filter
     *            the filter to remove
     */
    public void removeAccountMenuCategoryFilter(MenuEntryFilter<BasicMenuEntry> filter) {
        accountMenu.removeCategoryFilter(filter);
    }

    /**
     * Remove a previously added entry from a category of the account menu.
     *
     * @param categoryId
     *            the ID of the category from which the entry should be removed
     * @param entryId
     *            the ID of the entry to remove
     */
    public void removeFromAccountMenuCategory(String categoryId, String entryId) {
        if (coreAccountMenuCategoryEntries.contains(entryId)) {
            LOGGER.warn("Cannot remove core category entry {}", entryId);
        } else {
            AdministrationPageMenuEntry removedEntry = accountMenu.removeFromCategory(categoryId,
                    entryId);
            if (removedEntry != null) {
                removeUrlLookup(removedEntry.getPageUrl(), categoryId, entryId);
                LOGGER.debug("Removed entry with ID {} from account menu category with ID {}",
                        entryId, categoryId);
            }
        }
    }

    public void removeFromExtensionSection(String entryId) {
        if (EXTENSIONS_CATEGORY_ENTRY_OVERVIEW.equals(entryId)) {
            LOGGER.warn("Cannot remove core extension entry {}", entryId);
        } else {
            AdministrationPageMenuEntry removedEntry = extensionMenu.removeFromCategory(
                    EXTENSIONS_CATEGORY, entryId);
            if (removedEntry != null) {
                removeUrlLookup(removedEntry.getPageUrl(), EXTENSIONS_CATEGORY, entryId);
                LOGGER.debug("Removed entry with ID {} from extension section", entryId);
            }
        }
    }

    /**
     * Remove a previously added entry from a category of the system menu.
     *
     * @param categoryId
     *            the ID of the category from which the entry should be removed
     * @param entryId
     *            the ID of the entry to remove
     */
    public void removeFromSystemMenuCategory(String categoryId, String entryId) {
        if (coreSystemMenuCategoryEntries.contains(entryId)) {
            LOGGER.warn("Cannot remove core category entry {}", entryId);
        } else {
            AdministrationPageMenuEntry removedEntry = systemMenu.removeFromCategory(categoryId,
                    entryId);
            if (removedEntry != null) {
                removeUrlLookup(removedEntry.getPageUrl(), categoryId, entryId);
                LOGGER.debug("Removed entry with ID {} from system menu category with ID {}",
                        entryId, categoryId);
            }
        }
    }

    /**
     * Same as {@link #removeAccountMenuCategory(String)} except that the category is removed from
     * the system menu.
     *
     * @param categoryId
     *            the ID of the category to remove
     */
    public void removeSystemMenuCategory(String categoryId) {
        if (this.coreSystemMenuCategories.contains(categoryId)) {
            LOGGER.warn("Cannot remove core category {}", categoryId);
        } else if (systemMenu.removeCategory(categoryId)) {
            LOGGER.debug("Removed category with ID {} from system menu", categoryId);
        }
    }

    /**
     * Remove a previously added filter
     *
     * @param filter
     *            the filter to remove
     */
    public void removeSystemMenuCategoryEntryFilter(
            MenuEntryFilter<AdministrationPageMenuEntry> filter) {
        systemMenu.removeCategoryEntryFilter(filter);
    }

    /**
     * Remove a previously added filter
     *
     * @param filter
     *            the filter to remove
     */
    public void removeSystemMenuCategoryFilter(MenuEntryFilter<BasicMenuEntry> filter) {
        systemMenu.removeCategoryFilter(filter);
    }

    private synchronized void removeUrlLookup(String url, String categoryId, String entryId) {
        List<Pair<String, String>> urlEntries = urlToEntryMapping.get(url);
        if (urlEntries != null) {
            Iterator<Pair<String, String>> iterator = urlEntries.iterator();
            while (iterator.hasNext()) {
                Pair<String, String> lookup = iterator.next();
                if (lookup.getLeft().equals(categoryId) && lookup.getRight().equals(entryId)) {
                    iterator.remove();
                    return;
                }
            }
        }
    }
}
