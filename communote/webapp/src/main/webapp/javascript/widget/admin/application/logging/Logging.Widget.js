var LoggingWidget = new Class( {
    Extends: C_Widget,

    widgetGroup: 'admin/application/logging',

    refreshComplete: function(responseMetadata) {
        this.parent(responseMetadata);
        init_tips(this.domNode);
        init_tabs();
        searchAndShowRoarNotification(this.domNode);
    }
});
