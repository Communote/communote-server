var InviteUserViaEmailWidget = new Class({

    Extends: C_FormWidget,

    widgetGroup: "user",

    onSubmitSuccess: function() {
        searchAndShowRoarNotification(this.domNode);
    },

    onSubmitFailure: function() {
        searchAndShowRoarNotification(this.domNode);
    }
});
