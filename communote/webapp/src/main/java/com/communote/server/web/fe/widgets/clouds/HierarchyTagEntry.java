package com.communote.server.web.fe.widgets.clouds;

/**
 * Entry of a tag in a tag hierarchy. The name contains the name of the tag, and the filter the tag
 * filter list for this tag in the hierarchy.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class HierarchyTagEntry {
    private String name;
    private String filter;

    /**
     * Construct a new entry
     * 
     * @param name
     *            the tag name
     * @param filter
     *            the tag filter
     */
    public HierarchyTagEntry(String name, String filter) {
        this.name = name;
        this.filter = filter;
    }

    /**
     * Get the filter
     * 
     * @return the filter
     */
    public String getFilter() {
        return filter;
    }

    /**
     * get the tag name
     * 
     * @return the tag name
     */

    public String getName() {
        return name;
    }

}
