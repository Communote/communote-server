var UserGroupDialogWidget = new Class({
    Extends: C_FilterWidget,

    innerWidgetIds: null,
    widgetGroup: 'management/user/group',
    tabShown: false,
    observedFilterParams: [ 'groupId' ],

    /**
     * @override
     */
    beforeRemove: function() {
        this.cleanup();
    },

    cleanup: function() {
        var i;
        if (this.innerWidgetIds) {
            for (i = 0; i < this.innerWidgetIds.length; i++) {
                this.widgetController.removeWidgetById(this.innerWidgetIds[i]);
            }
            this.innerWidgetIds = null;
        }
    },

    filterParametersChanged: function(changedParams) {
        var groupName, groupId;
        // only one parameter is observed
        groupId = this.filterParamStore.getFilterParameter('groupId');
        if (groupId) {
            groupName = cnCache.get('group', groupId).name;
            this.showTab(groupName);
        } else {
            this.hideTab();
        }
        this.refresh();

    },

    getListeningEvents: function() {
        return this.parent().combine([ 'groupDataChanged', 'onGroupDeleted' ]);
    },

    groupDataChanged: function(newName) {
        if (newName) {
            this.setTabTitle(null, name);
        }
    },

    hideTab: function() {
        var tabElem, tabId, tabGroupId;
        if (!this.tabShown) {
            return;
        }
        tabId = this.getStaticParameter('tabId');
        tabGroupId = this.getStaticParameter('tabGroupId');
        if (tabId && tabGroupId) {
            tabElem = document.id(tabId);
            tabElem.setStyle('display', 'none');
            tabElem.getElementById(tabId + 'Close').setStyle('display', 'none');
            tabGroups[tabGroupId].first();
            this.tabShown = false;
        }
    },
    
    onGroupDeleted: function(deletedGroupId) {
        var groupId = this.filterParamStore.getFilterParameter('groupId');
        if (groupId == deletedGroupId) {
            this.hideTab();
        }
    },
    /**
     * @override
     */
    refreshComplete: function(responseMetadata) {
        this.innerWidgetIds = this.widgetController.findWidgets(this.domNode);
        if (responseMetadata.userSignature) {
            this.setUserName(responseMetadata.userSignature);
        }
    },
    /**
     * @override
     */
    refreshStart: function() {
        this.cleanup();
    },
    
    setTabTitle: function(tabElem, name) {
        if (!tabElem) {
            tabElem = document.id(this.getStaticParameter('tabId'));
        }
        tabElem.getFirst('span').set('html', name);
    },
    
    showTab: function(name) {
        var tabElem;
        var tabId = this.getStaticParameter('tabId');
        var tabGroupId = this.getStaticParameter('tabGroupId');
        if (tabId && tabGroupId) {
            tabElem = $(tabId);
            this.setTabTitle(tabElem, name);
            tabElem.setStyle('display', 'block');
            tabElem.getElementById(tabId + 'Close').setStyle('display', 'block');
            tabGroups[tabGroupId].activate(tabId);
            this.tabShown = true;
        }
    }
});
