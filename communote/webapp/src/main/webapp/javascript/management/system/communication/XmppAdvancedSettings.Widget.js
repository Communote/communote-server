var XmppAdvancedSettingsWidget = new Class( {
    Extends: C_FormWidget,

    widgetGroup: 'management/system/communication',

    refreshComplete: function(responseMetadata) {
        this.parent(responseMetadata);
        init_tips(this.domNode);
        searchAndShowRoarNotification(this.domNode);
    }
});
