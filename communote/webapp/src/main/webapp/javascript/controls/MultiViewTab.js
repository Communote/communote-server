/**
 * A tab control showing one content. A tab-click typically results in rendering another view of the
 * content. Supported static parameters: tabs {String} - all tabs to show in correct order and
 * following format: tabId:tabname,tabId2:tabname2 hiddenTabs {String} - comma separated list of the
 * tabIds from tabs string to be hidden activeTab {String} - the tabId from tabs string to be
 * activated after initialization contentContainer {String} - the ID of the element containing the
 * content [activeTabCssClassSuffix] {String} - CSS class suffix for active tab, default is
 * '-active' [inactiveTabCssClassSuffix] {String} - CSS class suffix for inactive tabs, default is
 * empty string
 */
var MultiViewTabWidget = new Class({
    Extends: C_Widget,

    widgetGroup: "controls",

    activeTab: null,
    /**
     * String[] of tabIds in the correct order.
     */
    tabIds: null,
    firstNonHiddenTabId: null,
    hiddenTabs: [],
    inactiveTabCssClassSuffix: '',
    activeTabCssClassSuffix: 'cn-tab-active',
    tabContentCssClass: 'content-active',
    contentContainer: null,
    // the IDs of the contained widgets
    containedWidgets: null,
    // holds tabIds to (optional) widget mappings to mark widgets of a tab as dirty
    dirtyTabContent: {},

    tabWrapperPrefix: 'tabWrapper-',

    init: function() {
        this.copyStaticParameter('tabs');
        // get order of tabs
        var tabDefs = this.getFilterParameter('tabs');
        var tabIds = [];
        var parts = tabDefs.split(',');
        for ( var i = 0; i < parts.length; i++) {
            var idAndTitle = parts[i].split(':');
            if (idAndTitle.length == 2 && idAndTitle[0])
                tabIds.push(idAndTitle[0].trim());
        }
        this.tabIds = tabIds;
        // make sure the activeTab is defined and exists
        var activeTab = this.getStaticParameter('activeTab');
        if (!activeTab || !tabIds.contains(activeTab)) {
            activeTab = tabIds[0];
        }
        this.copyStaticParameter('hiddenTabs');
        var hiddenTabs = this.getFilterParameter('hiddenTabs');
        if (hiddenTabs) {
            this.hiddenTabs = hiddenTabs.split(',');
            this._updateFirstNonHiddenTab();
            // make sure the activeTab is not hidden
            if (this.hiddenTabs.contains(activeTab))
                activeTab = this.firstNonHiddenTabId;
        } else {
            this._updateFirstNonHiddenTab();
        }
        this.setFilterParameter('activeTab', activeTab);
        this.activeTab = activeTab;
        // inform all about first tab
        E2G('onTabChanged', null, [ null, this.activeTab ]);
        if (!this.staticParams.activeTabCssClassSuffix) {
            this.setFilterParameter('activeTabCssClassSuffix', this.activeTabCssClassSuffix);
        } else {
            this.copyStaticParameter('activeTabCssClassSuffix');
            this.activeTabCssClassSuffix = this.getFilterParameter('activeTabCssClassSuffix');
        }
        if (!this.staticParams.inactiveTabCssClassSuffix) {
            this.setFilterParameter('inactiveTabCssClassSuffix', this.inactiveTabCssClassSuffix);
        } else {
            this.copyStaticParameter('inactiveTabCssClassSuffix');
            this.inactiveTabCssClassSuffix = this.getFilterParameter('inactiveTabCssClassSuffix');
        }
    },

    refreshComplete: function(responseMetadata) {
        this.parent(responseMetadata);
        // insert content div as tab-content
        var placeholder = this.domNode.getElementById(this.widgetId + '-content-placeholder');
        var container = $(this.staticParams.contentContainer);
        container.replaces(placeholder);
        container.addClass(this.tabContentCssClass);
        this.contentContainer = container;
        // get widgets from container and wrap them
        var wrapper = this._getOrCreateWrapper(this.activeTab);
        this.containedWidgets = [];
        var widgets = container.getChildren('.' + this.widgetController.widgetDivCssClass);
        for ( var i = 0; i < widgets.length; i++) {
            var w = widgets[i];
            this.containedWidgets[i] = w.id;
            // make DOM id unique
            w.set('id', w.id + '-' + this.activeTab);
            wrapper.wraps(w);
        }
        // check if we missed a tab event
        if (this.missedTabEvent) {
            this[this.missedTabEvent.event](this.missedTabEvent.params);
        }
    },

    getListeningEvents: function() {
        return [ 'onTabChangeRequest', 'onTabHideRequest', 'onTabContentDirty',
                'onAllTabsContentDirty' ];
    },
    /**
     * @param {String[]} params Array where the first element is the tabId to change to and the
     *            second element defines whether the contained widgets should refresh.
     */
    onTabChangeRequest: function(params) {
        var newTabId;
        if (!this.firstDOMLoadDone) {
            this.missedTabEvent = {
                event: 'onTabChangeRequest',
                params: params
            };
            return;
        }
        newTabId = params[0];
        if (this.tabIds.contains(newTabId)) {
            if (newTabId == this.activeTab)
                return;
            //if (title) this.setTabTitle(newTabId, title);
            this.activateTab(newTabId, params[1]);
        }
    },
    /**
     * @param {String[]} params Array where the first element is the tabId to hide and the second is
     *            the tabId to activate. The third element defines whether the contained widgets
     *            should refresh.
     */
    onTabHideRequest: function(params) {
        var tabId, newTabId;
        if (!this.firstDOMLoadDone) {
            this.missedTabEvent = {
                event: 'onTabChangeRequest',
                params: params
            };
            return;
        }
        tabId = params[0];
        newTabId = params[1];
        // hide the tab with ID tabId and set newTabId as activeTab
        if (tabId == newTabId)
            return;
        this.activateTab(newTabId, params[2]);
        this.hideTab(tabId);
    },

    /**
     * Sent to mark the content of all tabs as dirty. The content of the tabs will be refreshed the
     * next time it is activated.
     * 
     * @param {Object} widgetIds array that holds the IDs of the widgets to be refreshed. The
     *            widgetIds array can be empty which will result in all widgets to be refreshed.
     */
    onAllTabsContentDirty: function(widgetIds) {
        var i = 0, e;
        var tabIds = this.tabIds;
        for (; i < tabIds.length; i++) {
            if (tabIds[i] == this.activeTab) {
                for (e = 0; e < this.containedWidgets.length; e++) {
                    widgetController.getWidget(this.containedWidgets[e]).refresh();
                }
            } else {
                this.markTabContentDirty(tabIds[i], widgetIds);
            }
        }
    },

    /**
     * Sent to mark the content of a tab as dirty. The content of a dirty tab will be refreshed the
     * next time it is activated.
     * 
     * @param {Object} dirtyTab An object with a tabId member that denotes the tabId to mark dirty
     *            and a widgetIds array that holds the IDs of the widgets to be refreshed. The
     *            widgetIds array can be empty which will result in all widgets to be refreshed. The
     *            tabId member can be omitted which will result in marking all tabs, except for the
     *            current, dirty.
     */
    onTabContentDirty: function(dirtyTab) {
        var tabId = dirtyTab.tabId;
        var i, tabIds;
        if (tabId && (tabId == this.activeTab || !this.tabIds.contains(tabId)))
            return;
        if (!tabId) {
            tabIds = this.tabIds;
            for (i = 0; i < tabIds.length; i++) {
                if (tabIds[i] != this.activeTab) {
                    this.markTabContentDirty(tabIds[i], dirtyTab.widgetIds);
                }
            }
        } else {
            this.markTabContentDirty(tabId, dirtyTab.widgetIds);
        }
    },

    markTabContentDirty: function(tabId, widgetIds) {
        if (this.dirtyTabContent[tabId]) {
            if (!widgetIds || widgetIds.length == 0) {
                this.dirtyTabContent[tabId] = [];
            } else {
                this.dirtyTabContent[tabId].combine(widgetIds);
            }
        } else {
            this.dirtyTabContent[tabId] = widgetIds || [];
        }
    },

    refresh: function() {
        // no further refreshs after first load
        if (!this.firstDOMLoadDone)
            this.parent();
    },

    _updateFirstNonHiddenTab: function() {
        var hiddenTabs = this.hiddenTabs;
        var tabs = this.tabIds;
        for ( var i = 0; i < tabs.length; i++) {
            if (!hiddenTabs.contains(tabs[i])) {
                this.firstNonHiddenTabId = tabs[i];
                break;
            }
        }
    },

    _getOrCreateWrapper: function(tabId, createUndisplayed) {
        var wrapperId = this.tabWrapperPrefix + tabId;
        var el = this.contentContainer.getElementById(wrapperId);
        if (!el) {
            el = new Element('div', {
                'id': wrapperId,
                'class': 'tab-content-wrapper ' + tabId
            });
            if (createUndisplayed)
                el.setStyle('display', 'none');
            this.contentContainer.grab(el);
        }
        return el;
    },

    _changeDomNodeOfWidget: function(widgetId, tabId, wrapper) {
        var widget = this.widgetController.getWidget(widgetId);
        var refreshWidget = false;
        var uid = widgetId + '-' + tabId;
        var node = wrapper.getElementById(uid);
        if (!node) {
            node = new Element('div', {
                'id': uid,
                'class': widget.domNode.get('class')
            });
            wrapper.grab(node);
            refreshWidget = true;
        }
        widget.domNode = node;
        return refreshWidget;
    },

    setTabTitle: function(tabId, title) {
        var tabElement = this.domNode.getElementById(tabId);
        if (!tabElement)
            return;
        var anchor = tabElement.getElement('a');
        anchor.title = title;
        anchor.getElement('span').set('text', title);
    },

    /**
     * Shows a hidden tab.
     * 
     * @param {String} tabId The ID of the tab.
     * @return {Element} The shown element.
     */
    showTab: function(tabId) {
        var tabElement = this.domNode.getElementById(tabId);
        if (tabElement.getStyle('display') == 'none') {
            // make sure it has the tab-first class if necessary
            var tabIds = this.tabIds;
            var setNewFirst = false;
            for ( var i = 0; i < tabIds.length; i++) {
                if (tabIds[i] == this.firstNonHiddenTabId)
                    break;
                if (tabIds[i] == tabId) {
                    setNewFirst = true;
                    break;
                }
            }
            if (setNewFirst) {
                var cn = tabElement.className;
                tabElement.className = cn.replace('tab', 'tab-first');
                var curFirstTabElement = this.domNode.getElementById(this.firstNonHiddenTabId);
                cn = curFirstTabElement.className;
                curFirstTabElement.className = cn.replace('tab-first', 'tab');
                this.firstNonHiddenTabId = tabId;
            }
            this.hiddenTabs.erase(tabId);
            tabElement.setStyle('display', '');
        }
        return tabElement;
    },

    /**
     * Hide a shown tab. Do not call this method on the active tab, because the active-tab will not
     * be changed and thus the content won't be changed.
     * 
     * @param {String} tabId The ID of the tab.
     */
    hideTab: function(tabId) {
        var tabElement = this.domNode.getElementById(tabId);
        if (tabElement.getStyle('display') != 'none') {
            // make sure the correct tab is marked as first tab
            this.hiddenTabs.push(tabId);
            if (this.firstNonHiddenTabId == tabId) {
                this._updateFirstNonHiddenTab();
                var newFirstTabElement = this.domNode.getElementById(this.firstNonHiddenTabId);
                var cn = newFirstTabElement.className;
                newFirstTabElement.className = cn.replace('tab', 'tab-first');
                cn = tabElement.className;
                tabElement.className = cn.replace('tab-first', 'tab');
            }
            tabElement.setStyle('display', 'none');
        }
    },

    activateTab: function(tabId, forceRefreshOfContainedWidgets) {
        if (this.activeTab == tabId)
            return;
        var curActiveTab = this.domNode.getElementById(this.activeTab);
        var curActiveTabCn = curActiveTab.className;
        if (this.activeTabCssClassSuffix.length != 0) {
            curActiveTabCn = curActiveTabCn.replace(this.activeTabCssClassSuffix,
                    this.inactiveTabCssClassSuffix);
        } else {
            curActiveTabCn += this.inactiveTabCssClassSuffix;
        }
        curActiveTab.className = curActiveTabCn;
        // if the tab is hidden it's better to force a refresh of the contained widgets
        if (this.hiddenTabs.contains(tabId))
            forceRefreshOfContainedWidgets = true;
        var newActiveTab = this.showTab(tabId);
        var newActiveTabCn = newActiveTab.className;
        if (this.inactiveTabCssClassSuffix.length != 0) {
            newActiveTabCn = newActiveTabCn.replace(this.inactiveTabCssClassSuffix,
                    this.activeTabCssClassSuffix);
        } else {
            newActiveTabCn += this.activeTabCssClassSuffix;
        }
        newActiveTab.className = newActiveTabCn;

        var newWrapper = this._getOrCreateWrapper(tabId, true);
        if (this.firstNonHiddenTabId == tabId) {
            this.contentContainer.addClass('cn-firsttab-active');
        } else {
            this.contentContainer.removeClass('cn-firsttab-active');
        }
        var refreshContainedWidgets = false;
        for ( var i = 0; i < this.containedWidgets.length; i++) {
            var widgetId = this.containedWidgets[i];
            // cancel running refreshs of contained widgets
            if (this.widgetController.cancelRefreshsOfWidget(widgetId)
                    || !this.widgetController.getWidget(widgetId).firstDOMLoadDone) {
                // the widget might not have been refreshed thats why the current tab content should be marked dirty
                this.markTabContentDirty(this.activeTab, widgetId);
            }
            refreshContainedWidgets = this._changeDomNodeOfWidget(widgetId, tabId, newWrapper);
        }
        E2G('onTabChanged', null, [ this.activeTab, tabId ]);
        var refreshAll = refreshContainedWidgets || forceRefreshOfContainedWidgets
                || (this.dirtyTabContent[tabId] && this.dirtyTabContent[tabId].length == 0);
        if (refreshAll || this.dirtyTabContent[tabId]) {
            for ( var i = 0; i < this.containedWidgets.length; i++) {
                var widgetId = this.containedWidgets[i];
                if (refreshAll || this.dirtyTabContent[tabId].contains(widgetId)) {
                    widgetController.getWidget(this.containedWidgets[i]).refresh();
                }
            }
            // clear dirty flag
            if (this.dirtyTabContent[tabId])
                delete this.dirtyTabContent[tabId];
        }
        this._getOrCreateWrapper(this.activeTab).setStyle('display', 'none');
        newWrapper.setStyle('display', null);

        this.activeTab = tabId;
    }
});