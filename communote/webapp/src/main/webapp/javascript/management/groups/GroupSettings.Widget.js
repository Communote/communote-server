var GroupSettingsWidget = new Class({

    Extends: C_FormWidget,

    widgetGroup: 'management/user/group',

    refreshComplete: function(responseMetadata) {
        this.parent(responseMetadata);
        init_tips(this.domNode);
        searchAndShowRoarNotification(this.domNode);
    },

    startFullSynchronisation: function() {
        var syncUrl = buildRequestUrl('/user/client/usergroupmanagement/startSynchronization');
        var req = new Request.JSON({
            url: syncUrl
        });
        req.addEvent('success', function(response) {
            if (response.status == 'OK') {
                showNotification(NOTIFICATION_BOX_TYPES.success, null, response.message);
            } else {
                showNotification(NOTIFICATION_BOX_TYPES.error, null, response.message || getJSMessage('common.error.unspecified'), {
                    duration: ''
                })
            }
        });
        req.addEvent('failure', function(xhr) {
            showNotification(NOTIFICATION_BOX_TYPES.error, null, getJSMessage('common.error.unspecified'), {
                duration: ''
            })
        });
        req.send();
    }
});
