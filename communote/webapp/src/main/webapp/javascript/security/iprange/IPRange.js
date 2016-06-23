var IpRangeWidget = new Class({
    Extends: C_FormWidget,

    widgetGroup: 'management/security/iprange',

    getListeningEvents: function() {
        if (this.getStaticParameter('creationMode')) {
            return this.parent();
        }
        return this.parent().combine([ "updateIpRange" ]);
    },


    refreshStart: function() {
        this.parent();
        this.copyStaticParameter('filterId');
        this.copyStaticParameter('alwaysOpen');
    },

    onSubmitSuccess: function() {
        this.onSubmitFailure();
    },

    onSubmitFailure: function() {
        searchAndShowRoarNotification(this.domNode);
        var widget = widgetController.getWidget('IpRangeList');
        if (widget != null) {
            widget.refresh();
        }

        var thisDiv = $(this.widgetId);
        var openFilterId = thisDiv.getElement('input[name=openFilterId]');
        var openFilterName = thisDiv.getElement('input[name=openFilterName]');
        if (openFilterId != null && openFilterName != null) {
            E('updateIpRange', openFilterId.get('value'));
            widgetController.getWidget('IpRangeList').showTab(openFilterName.get('value'));
        } else if (this.domNode.getElement('input[name=name]').value == '') {
            var tabId = this.getStaticParameter('updateTabId');
            var tabGroupId = this.getStaticParameter('tabGroupId');
            if (tabId != null && tabGroupId != null) {
                tabGroups[tabGroupId].first();
                $(tabId).setStyle('display', 'none');
            }
        }
    },

    updateIpRange: function(id) {
        this.setStaticParameter("filterId", id);
        this.refresh();
    }
});
