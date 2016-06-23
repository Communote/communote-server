var CertificateListWidget = new Class({
    Extends: C_FormWidget,

    widgetGroup: 'management/system/application',

    setup: function() {
        this.parent();
    },
    refreshComplete: function(responseMetadata) {
        this.parent(responseMetadata);
        init_tips(this.domNode);
        searchAndShowRoarNotification(this.domNode);
    }
});
