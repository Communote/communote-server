var UserManagementUserOverviewWidget = new Class( {
    Extends: C_FormWidget,
    widgetGroup: 'management/user',

    getListeningEvents: function() {
        return this.parent().combine( [ 'onUserUpdate' ]);
    },

    onUserUpdate: function(params) {
    	var sendingWidgetId = params[0];
    	var userId = params[1];
    	if (this.widgetId != sendingWidgetId && userId == this.getFilterParameter('userId')) {
    		//this.setStaticParameter('userId', userId);
    		this.refresh();
    	}
    },

    refreshStart: function() {
        this.parent();
        this.copyStaticParameter('userId');
    },

    refreshComplete: function(responseMetadata) {
    	// TODO application success is not set correctly, because Java part of widget is not using a controller
    	// -> always send the event although it might not be necessary (error case)
    	if (this.refreshCausedBySubmit) {
    		searchAndShowRoarNotification(this.domNode);
    		E2('onUserUpdate', null, [this.widgetId, this.getFilterParameter('userId')]);
    	}
    	init_tips(this.domNode);
    	this.parent(responseMetadata);
    },

    removeUserFromGroupEntity: function(entityId, groupEntityId) {

        if (groupEntityId && entityId) {

            var url = buildRequestUrl('/admin/client/usermanagement/removeUserFromGroupEntity');
            var request = new Request.JSON( {
                url: url,
                data: 'userId=' + entityId + '&groupEntityId=' + groupEntityId,
                method: 'post'
            });

            request.addEvent('complete', function(jsonResponse) {

                if (jsonResponse.status == 'OK') {
                    // show success message
                    hideNotification();
                    showNotification(NOTIFICATION_BOX_TYPES.success, '', jsonResponse.message);

                    // remove element from view
                    var entry = this.domNode.getElement('li[name=' + groupEntityId + ']');
                    if (entry) {
                        entry.dispose();
                    }
                } else {
                    hideNotification();
                    showNotification(NOTIFICATION_BOX_TYPES.error, '', jsonResponse.message, {
                        duration : ''
                    });
                }
                this.refresh();
            }.bind(this));

            request.send();
        } else {
            /* TODO error message. */
        }
    }

});