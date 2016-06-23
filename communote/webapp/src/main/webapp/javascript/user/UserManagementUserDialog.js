var UserManagementUserDialogWidget = new Class({

    Extends: C_FilterWidget,

    widgetGroup: "management/user",

    observedFilterParams: [ 'userId' ],

    innerWidgetIds: null,
    isHidden: false,

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
        // only one parameter is observed
        this.refresh();
        scrollWindowTo(this.domNode, 0, 0, false, true);
    },
    /**
     * @override
     */
    getListeningEvents: function() {
        return [ 'onUserProfileChanged', 'onUserDeleted' ];
    },

    onUserProfileChanged: function(newProfile) {
        var name = newProfile.firstName + ' ' + newProfile.lastName + ' (@' + newProfile.alias + ')';
        this.setUserName(name);
    },

    onUserDeleted: function(userId) {
        var userFilter = this.filterParamStore.getFilterParameter('userId');
        if (userFilter == userId) {
            this.hideWidget();
        }
    },
    /**
     * @override
     */
    refresh: function() {
        var userFilter = this.filterParamStore.getFilterParameter('userId');
        if (userFilter != null) {
            this.showWidget();
            this.parent();
        } else {
            this.hideWidget();
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
    showWidget: function() {
        var tabId = this.getStaticParameter('tabId');
        var tabGroupId = this.getStaticParameter('tabGroupId');
        if (tabId != null && tabGroupId != null) {
            $(tabId).setStyle('display', 'block');
            $(tabId + 'Close').setStyle('display', 'block');
            tabGroups[tabGroupId].activate(tabId);
        }
    },

    hideWidget: function() {
        var activeElement;
        var tabId = this.getStaticParameter('tabId');
        var tabGroupId = this.getStaticParameter('tabGroupId');
        if (tabId != null && tabGroupId != null) {
            $(tabId).setStyle('display', 'none');
            activeElement = $(tabGroupId).getElement('.active');
            if (activeElement != null && activeElement.id != tabId) {
                tabGroups[tabGroupId].activate(activeElement.id);
            } else {
                tabGroups[tabGroupId].first();
            }
        }
    },

    setUserName: function(name) {
        var tabId = this.getStaticParameter('tabId');
        if (tabId != null) {
            $(tabId).getElement('span').set('html', name);
        }
    }
});
