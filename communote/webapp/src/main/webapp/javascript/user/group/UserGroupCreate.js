var UserGroupCreateWidget = new Class( {

    Extends: C_FormWidget,

    widgetGroup: 'management/user/group',

    refreshComplete: function(responseMetadata) {
		init_tips(this.domNode);
		this.parent(responseMetadata);
	},
    
    onSubmitSuccess: function() {
        E('onGroupCreated', null);
        searchAndShowRoarNotification(this.domNode);
    },

    onSubmitFailure: function() {
        searchAndShowRoarNotification(this.domNode);
    }
});
