package com.communote.server.web.fe.widgets.controls;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.communote.server.widgets.AbstractWidget;

/**
 * Server side class for the MultiViewTabWidget that is a tab control which contains one content. A
 * tab-click typically results in rendering another view of the content.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class MultiViewTabWidget extends AbstractWidget {

    private final Map<String, String> tabId2Title;
    private final List<String> tabIds;
    private final Set<String> hiddenTabs;
    private String activeTabId;
    private String firstNonHiddenTabId;

    /**
     * Default constructor.
     */
    public MultiViewTabWidget() {
        tabId2Title = new HashMap<String, String>();
        tabIds = new ArrayList<String>();
        hiddenTabs = new HashSet<String>();
    }

    /**
     * Returns the suffix of the CSS class for active tabs.
     * 
     * @return the suffix
     */
    public String getActiveTabCssClassSuffix() {
        return getParameter("activeTabCssClassSuffix");
    }

    /**
     * @return the activeTabId
     */
    public String getActiveTabId() {
        return activeTabId;
    }

    /**
     * Returns the ID of the first tab not marked as hidden.
     * 
     * @return the firstNonHiddenTabId the first non-hidden tab
     */
    public String getFirstNonHiddenTabId() {
        return firstNonHiddenTabId;
    }

    /**
     * @return the hiddenTabs
     */
    public Set<String> getHiddenTabs() {
        return hiddenTabs;
    }

    /**
     * Returns the suffix of the CSS class for inactive tabs.
     * 
     * @return the suffix
     */
    public String getInactiveTabCssClassSuffix() {
        return getParameter("inactiveTabCssClassSuffix");
    }

    /**
     * @return the tabId2Title
     */
    public Map<String, String> getTabId2Title() {
        return tabId2Title;
    }

    /**
     * @return the tabIds
     */
    public List<String> getTabIds() {
        return tabIds;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTile(String outputType) {
        return "core.widget.controls.multiviewtab";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object handleRequest() {
        // prepare tab-mapping
        parseTabs(getParameter("tabs", ""));
        activeTabId = getParameter("activeTab");
        if (activeTabId == null) {
            activeTabId = tabIds.get(0);
        }
        parseHiddenTabs(getParameter("hiddenTabs"));
        // store first non hidden tab for convenience
        loop: for (String id : tabIds) {
            if (!hiddenTabs.contains(id)) {
                firstNonHiddenTabId = id;
                break loop;
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initParameters() {
        // nothing to do here
    }

    /**
     * Parses the hiddenTabs parameter value which is expected to be a comma separated list of tab
     * IDs to hide.
     * 
     * @param hTabs
     *            the parameter value
     */
    private void parseHiddenTabs(String hTabs) {
        if (hTabs == null) {
            return;
        }
        String[] tIds = hTabs.split(",");
        for (String id : tIds) {
            this.hiddenTabs.add(id.trim());
        }
    }

    /**
     * Parses the tabs parameter value which is expected to look like: tabId:tabname,tabId2:tabname2
     * 
     * @param tabs
     *            the parameter value
     */
    private void parseTabs(String tabs) {
        String[] id2Titles = tabs.split(",");
        for (String s : id2Titles) {
            String[] mapping = s.split(":");
            String tabId = mapping[0].trim();
            String title = mapping[1].trim();
            this.tabId2Title.put(tabId, title);
            this.tabIds.add(tabId);
        }
    }
}
