var UserGroupMembersListWidget = new Class({

    Extends: C_Widget,
    Implements: LoadMoreSupport,

    widgetGroup: 'management/user/group',

    /**
     * @override
     */
    init: function() {
        this.parent();
        this.initLoadMoreSupport();
        this.copyStaticParameter('groupId');
    },

    /**
     * @override
     */
    beforeRemove: function() {
        this.parent();
        this.disposeLoadMoreSupport();
    },
    
    changeNumberOfShownMembers: function(limit) {
        this.setFilterParameter('maxCount', limit);
        this.resetLoadMoreState();
        this.refresh();
    },

    getListeningEvents: function() {
        var events = this.parent();
        events.push('groupMembersChanged');
        return events;
    },

    groupMembersChanged: function() {
        this.refresh();
    },

    removeGroupMember: function(entityId) {
        var url = buildRequestUrl('/admin/client/usergroupmanagement/removeGroupMember');
        var params = {
            'groupId': this.getFilterParameter('groupId'),
            'entityId': entityId
        };
        var request = new Request.JSON({
            url: url,
            data: params,
            method: 'post'
        });

        request.addEvent('complete', function(jsonResponse) {

            if (jsonResponse.status == 'OK') {
                // show success message
                hideNotification();
                showNotification(NOTIFICATION_BOX_TYPES.success, '', jsonResponse.message);
                E('groupMembersChanged');
            } else {
                hideNotification();
                showNotification(NOTIFICATION_BOX_TYPES.error, '', jsonResponse.message, {
                    duration: ''
                });
            }
        }.bind(this));

        /** send request */
        request.send();

        return false;
    }
});