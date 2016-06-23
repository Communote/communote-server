var InviteUserWidget = new Class({

    Extends: C_FormWidget,

    widgetGroup: 'management/user',

    init: function() {
        this.parent();
        this.copyStaticParameter('invitationProvider');
    },

    refreshComplete: function(responseMetadata) {
        this.parent(responseMetadata);
        init_tips(this.domNode);
        searchAndShowRoarNotification(this.domNode);
        // TODO bad style to reference a widget directly. Better fire an appropriate widget event. 
        widgetController.getWidget('UserManagementList').refresh();
    }
});
