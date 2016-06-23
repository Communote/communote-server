var UserGroupEditWidget = new Class( {
	Extends: C_FormWidget,

    widgetGroup: "management/user/group",
    
    refreshStart: function() {
		this.parent();
		this.copyStaticParameter('groupId');
	},
	
	onSubmitSuccess: function() {
        searchAndShowRoarNotification(this.domNode);
        var newName = this.domNode.getElementById('groupEditDisplayName').value;
        var cache = this.widgetController.getDataStore();
        var groupId = this.getFilterParameter('groupId');
        var oldName = cache.get('group', groupId).name;
        if (oldName != newName) {
        	E('groupDataChanged', newName);
        } else {
        	E('groupDataChanged');
        }
	},
	
	onSubmitFailure: function() {
        searchAndShowRoarNotification(this.domNode);
	}
	
});
		