var UserProfileNotificationsWidget = new Class( {
    Extends: C_FormWidget,

    widgetGroup: "user/profile",
    
    onSubmitSuccess : function() {
        searchAndShowRoarNotification(this.domNode);
    },
    
    onSubmitFailure : function() {
        searchAndShowRoarNotification(this.domNode);
    }
});