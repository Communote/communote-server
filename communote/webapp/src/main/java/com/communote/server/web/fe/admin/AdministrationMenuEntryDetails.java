package com.communote.server.web.fe.admin;

/**
 * Encapsulates the details of a menu entry
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class AdministrationMenuEntryDetails {

    private final BasicMenuEntry category;
    private final AdministrationPageMenuEntry menuEntry;

    /**
     * Create a new wrapper for the menu entry details
     *
     * @param category
     *            the category the menu entry belongs to
     * @param menuEntry
     *            the menu entry
     */
    public AdministrationMenuEntryDetails(BasicMenuEntry category,
            AdministrationPageMenuEntry menuEntry) {
        this.category = category;
        this.menuEntry = menuEntry;
    }

    /**
     * @return the category the menu entry belongs to
     */
    public BasicMenuEntry getCategory() {
        return category;
    }

    /**
     * @return the menu entry
     */
    public AdministrationPageMenuEntry getEntry() {
        return menuEntry;
    }
}
