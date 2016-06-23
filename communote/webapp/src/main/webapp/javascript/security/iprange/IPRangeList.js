var IpRangeListWidget = new Class({
    Extends: C_Widget,
    Implements: LoadMoreSupport,

    widgetGroup: 'management/security/iprange',

    /**
     * @override
     */
    init: function() {
        this.parent();
        this.initLoadMoreSupport();
    },
    
    /**
     * @override
     */
    beforeRemove: function() {
        this.parent();
        this.disposeLoadMoreSupport();
    },

    updateIpRange: function(id) {
        this.resetLoadMoreState();
        this.refresh();
    },

    showTab: function(name) {
        var tabId = this.getStaticParameter('updateTabId');
        var tabGroupId = this.getStaticParameter('tabGroupId');
        if (tabId != null && tabGroupId != null) {
            $(tabId).getFirst('span').set('html', name);
            $(tabId).setStyle('display', 'block');
            $(tabId + 'Close').setStyle('display', 'block');
            tabGroups[tabGroupId].activate(tabId);
        }
    }
});
