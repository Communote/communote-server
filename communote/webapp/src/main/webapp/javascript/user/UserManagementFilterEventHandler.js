var UserManagementFilterEventHandler = new Class( {
    Extends: C_FilterEventHandler,
    
    getHandledEvents: function() {
        return this.parent().combine(['onUserClick', 'onUserSearch', 
                                      'userRoleFilterClick', 'userStatusFilterClick', 'onGroupClick']);
    },
    
    onUserClick: function(userId) {
        if (userId) {
            if (typeOf(userId) != 'string') userId = userId.toString();
            this.filterParameterStore.setFilterParameter('userId', userId);
            // always inform about event
            return ['userId'];
        }
    },
    
    onGroupClick: function(groupId) {
    	if (groupId) {
            if (typeOf(groupId) != 'string') groupId = groupId.toString();
            this.filterParameterStore.setFilterParameter('groupId', groupId);
        } else {
        	this.filterParameterStore.unsetFilterParameter('groupId');
        }
    	// always inform about event
        return ['groupId'];
    },
    
    userRoleFilterClick: function(obj) {
        var role = obj.value;
        if (this.filterParameterStore.appendFilterParameterValue('userRoleFilter', role, true)) {
            return ['userRoleFilter'];
        }
    },
    
    userStatusFilterClick: function(status) {
        if (this.filterParameterStore.appendFilterParameterValue('userStatusFilter', status, true)) {
            return ['userStatusFilter'];
        }
    },
    
    onUserSearch: function(searchString) {
        if (this.filterParameterStore.toggleFilterParameter('searchString', searchString)) {
            return ['searchString'];
        }
    }
});