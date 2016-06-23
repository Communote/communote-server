var UserGroupListWidget = new Class({

    Extends: C_Widget,
    Implements: LoadMoreSupport,

    widgetGroup: 'management/user/group',

    placehoders: null,
    /**
     * @override
     */
    init: function() {
        this.parent();
        this.initLoadMoreSupport();
    },

    /**
     * @override
     */
    beforeRemove: function() {
        this.parent();
        if (this.placehoders) {
            this.placehoders.destroy();
        }
        this.disposeLoadMoreSupport();
    },
    getListeningEvents: function() {
        return this.parent().combine(
                [ 'onGroupCreated', 'groupDataChanged', 'deleteGroupRequested',
                        'groupMembersChanged' ]);
    },

    refreshComplete: function(responseMetadata) {
        this.parent(responseMetadata);
        this.placehoders = communote.utils.attachPlaceholders('input', this.domNode);
    },

    refreshStart: function() {
        if (this.placehoders) {
            this.placehoders.destroy();
        }
    },
    onGroupCreated: function() {
        this.refresh();
    },

    groupDataChanged: function() {
        this.refresh();
    },

    deleteGroupRequested: function(args) {
        if (args && args.groupId) {
            if (args.event) {
                var mooEvent = new DOMEvent(args.event);
                mooEvent.stopPropagation();
            }
            this.doDeleteGroup(args.groupId);
        }
    },

    groupMembersChanged: function() {
        this.refresh();
    },

    setGroupNameFilter: function(filter) {
        this.setFilterParameter('filter', filter);
        this.resetLoadMoreState();
        this.refresh();
    },

    doDeleteGroup: function(groupId) {
        var buttons = [];
        buttons.push({
            type: 'yes',
            action: function() {
                var that = this;
                var request = buildServiceRequest(
                        '/admin/client/usergroupmanagement/deleteGroup', 'groupId=' + groupId,
                        function(jsonResponse) {
                            that.groupDeleteSuccess(groupId, jsonResponse.message);
                        });
                request.send();
            }.bind(this)
        });
        buttons.push({
            type: 'no'
        });
        showDialog(getJSMessage('client.user.group.management.group.toolbar.delete'),
                getJSMessage('client.user.group.management.delete.question'), buttons);
    },

    groupDeleteSuccess: function(groupId, message) {
        // show success message
        hideNotification();
        showNotification(NOTIFICATION_BOX_TYPES.success, '', message);
        E('onGroupDeleted', groupId);
        this.refresh();
    }

});
