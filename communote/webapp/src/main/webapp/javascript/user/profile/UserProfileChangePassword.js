var UserProfileChangePasswordWidget = new Class( {
    Extends: C_FormWidget,

    widgetGroup: "user/profile",

    onSubmitSuccess : function() {
        // look for the success container
        searchAndShowRoarNotification(this.domNode);
    },
    
    onSubmitFailure : function() {
        // look for the error container
        searchAndShowRoarNotification(this.domNode);
    }
});