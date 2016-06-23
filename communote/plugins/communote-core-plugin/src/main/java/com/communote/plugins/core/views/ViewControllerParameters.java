package com.communote.plugins.core.views;

/**
 * Possible parameters.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public enum ViewControllerParameters {
    /** Title */
    PAGE_TITLE("pageTitle"),
    /** CSS categories */
    CSS_CATEGORIES("cssCategories"),
    /**
     * Name of the CSS resource category that provides CSS of the tinyMCE IFrame. Defaults to
     * tinyMCE-content.
     */
    TINY_MCE_CONTENT_CSS_CATEGORY("tinyMceContentCssCategory"),
    /** JavaScript categories */
    JAVASCRIPT_CATEGORIES("javaScriptCategories"),
    /** the category name for which the localized JS messages should be included */
    JS_MESSAGES_CATEGORY("jsMessagesCategory"),
    /** contentTemplate */
    CONTENT_TEMPLATE("contentTemplate"),
    /** Menu */
    MENU("menu"),
    /** Submenu */
    SUBMENU("submenu"),
    /** Symbolic name of this bundle. */
    SYMBOLIC_NAME("symbolicName"),
    /** Message key for the menu entry **/
    MENU_ENTRY_MESSAGE_KEY("menuEntryMessageKey");

    private final String name;

    /**
     * Constructor
     *
     * @param name
     *            The name.
     */
    private ViewControllerParameters(String name) {
        this.name = name;
    }

    /**
     * @return this.name.
     */
    @Override
    public String toString() {
        return this.name;
    }
}