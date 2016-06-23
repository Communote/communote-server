var UserManagementListWidget = new Class(
    {

        Extends: C_FilterWidget,
        Implements: [FilterParamsHandlerStrategyByName, LoadMoreSupport],

        widgetGroup: 'management/user',

        /**
         * Default action of FilterParamsHandlerStrategyByName is a refresh, thus we want to break.
         */
        breakAfterDefaultAction: true,

        observedFilterParams: [ 'searchString', 'userId', 'userRoleFilter', 'userStatusFilter' ],
        
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
            this.disposeLoadMoreSupport();
        },

        getListeningEvents: function() {
            return this.parent().combine([ 'onUserUpdate', 'onUserDeleted', 'onUserListNumberElementsChange',
                'onUserProfileChanged' ]);
        },

        /**
         * Implementation of the default action required by the FilterParamsHandlerStrategyByName.
         */
        handleChangedParamsDefaultAction: function(changedParam) {
            // refresh and reset offset
            this.resetLoadMoreState();
            this.refresh();
        },

        userIdChanged: function() {
            this.refreshUserList();
        },

        refreshUserList: function() {
            var selected = this.filterParamStore.getFilterParameter('userId');
            var idString = '';
            if (selected)
                idString = 'client_user_' + selected;
            var list = this.domNode.getElements('div.row');
            list.each(function(item) {
                // using style directly because some versions of IE6 do not support
                // changing CSS class at this point
                if (item.get('id') == idString) {
                    item.setStyle('background-color', '#FFF4E8');
                } else {
                    item.setStyle('background-color', '');
                }
            }, this);
        },

        refreshComplete: function(responseMetadata) {
            searchAndShowRoarNotification(this.domNode);
            this.refreshUserList();
        },

        onUserListNumberElementsChange: function(limit) {
            this.staticParams['maxCount'] = limit;
            this.copyStaticParameter('maxCount');
            this.resetLoadMoreState();
            this.refresh();
        },

        onUserUpdate: function(params) {
            this.refreshListIfUserContained(params[1]);
        },

        onUserDeleted: function(userId) {
            this.refreshListIfUserContained(userId);
        },

        refreshListIfUserContained: function(userId) {
            var elemId = 'client_user_' + userId;
            if (this.domNode.getElementById(elemId)) {
                this.refresh();
            }
        },

        onUserProfileChanged: function(newProfile) {
            this.refreshListIfUserContained(newProfile.userId);
        },

        doActivateUser: function(userId) {
            this.startLoadingFeedback();
            var request = buildServiceRequest('/admin/client/usermanagement/activateUser',
                'userId=' + userId, function(jsonResponse) {
                    // show success message
                    hideNotification();
                    showNotification(NOTIFICATION_BOX_TYPES.success, '', jsonResponse.message);
                    E2('onUserUpdate', null, [ this.widgetId, userId ]);
                }.bind(this), function(jsonResponse) {
                    // show error message
                    hideNotification();
                    showNotification(NOTIFICATION_BOX_TYPES.error, '',
                        jsonResponse ? jsonResponse.message : 'Server Error', {
                            duration: ''
                        });
                    this.stopLoadingFeedback();
                }.bind(this));
            request.send();
        },

        doDisableUser: function(userId) {
            this.startLoadingFeedback();
            var request = buildServiceRequest('/admin/client/usermanagement/disableUser',
                'userId=' + userId, function(jsonResponse) {
                    // show success message
                    hideNotification();
                    showNotification(NOTIFICATION_BOX_TYPES.success, '', jsonResponse.message);
                    E2('onUserUpdate', null, [ this.widgetId, userId ]);
                }.bind(this), function(jsonResponse) {
                    // show error message
                    hideNotification();
                    showNotification(NOTIFICATION_BOX_TYPES.error, '',
                        jsonResponse ? jsonResponse.message : 'Server Error', {
                            duration: ''
                        });
                    this.stopLoadingFeedback();
                }.bind(this));
            request.send();
        },

        doDeleteUser: function(userId, deleteMode) {
            this.startLoadingFeedback();
            var request = buildServiceRequest('/admin/client/usermanagement/deleteUser',
                'userId=' + userId + '&deleteMode=' + deleteMode, function(jsonResponse) {
                    this.handleDeleteUserResponse(jsonResponse, deleteMode, userId);
                }.bind(this), this.handleDeleteUserFailed.bind(this));
            request.send();
        },
        
        handleDeleteUserFailed: function(jsonResponse) {
            this.stopLoadingFeedback();
            // show error message
            hideNotification();
            showNotification(NOTIFICATION_BOX_TYPES.error, '',
                    jsonResponse ? jsonResponse.message : 'Server Error', {
                        duration: ''
                    });
        },

        handleDeleteUserResponse: function(jsonResponse, deleteMode, userId) {
            // closePopup();
            if (jsonResponse && jsonResponse.confirmMessage) {
                var confirmElem = this.domNode.getElementById(
                    'confirmManagerlessGroupsActionTemplate').getFirst().clone();
                confirmElem.getElement('[name=deleteMode]').set('value', deleteMode);
                confirmElem.getElement('[name=confirmedBlogIds]').set('value', jsonResponse.ids);
                confirmElem.getElement('[name=userId]').set('value', userId);
                var optionElems = confirmElem.getElements('input[type=radio]');
                if (!jsonResponse.showBecomeManagerRadio) {
                    var optionContainerEl = optionElems[0].getParent();
                    optionContainerEl.setStyle('display', 'none');
                }
                // usability
                var idPrefix = 'blogHandleOption';
                optionElems[0].id = idPrefix + 1;
                optionElems[1].id = idPrefix + 2;
                var optionLabelElems = confirmElem.getElements('label');
                optionLabelElems[0].set('for', idPrefix + 1);
                optionLabelElems[1].set('for', idPrefix + 2);

                confirmElem.getElements('p')[0].set('text', jsonResponse.message);

                var spanEls = confirmElem.getElements('span');
                spanEls[0].set('text', jsonResponse.optionsMessage);
                spanEls[1].set('text', jsonResponse.confirmMessage);
                // prepare form for being sent
                confirmElem.set('send', {
                    onComplete: function(response) {
                        this.handleDeleteUserResponse(eval('(' + response + ')'), deleteMode,
                            userId);
                    }.bind(this)
                });
                showDialog(jsonResponse.title, confirmElem, [{
                        type: 'yes',
                        action: function() {
                            confirmElem.send();
                        }
                    }, {
                        type: 'no'
                    }
               ]);
            } else {
                showNotification(NOTIFICATION_BOX_TYPES.success, '', jsonResponse.message);
                if (deleteMode == '') {
                    E2('onUserUpdate', null, [ this.widgetId, userId ]);
                } else {
                    E2('onUserDeleted', null, userId);
                }
            }
            this.stopLoadingFeedback();
        },

        doToogleRole: function(userId, setAdminRole) {

            if (setAdminRole) {
                this.doAssignRole(userId, 'ROLE_KENMEI_CLIENT_MANAGER');
            } else {
                this.doRemoveRole(userId, 'ROLE_KENMEI_CLIENT_MANAGER');
            }

        },

        doRemoveRole: function(userId, role) {
            var request = buildServiceRequest("/admin/client/usermanagement/removeRole",
                "userId=" + userId + "&role=" + role, function() {
                    E2('onUserUpdate', null, [ this.widgetId, userId ]);
                }.bind(this));
            request.send();
        },

        doAssignRole: function(userId, role) {
            var url = buildRequestUrl("/admin/client/usermanagement/assignRole?userId=" + userId
                + "&role=" + role);
            new Request({
                url: url,
                method: 'get',
                'onSuccess': function(userId) {
                    E2('onUserUpdate', null, [ this.widgetId, userId ]);
                }.bind(this, userId)
            }).send();
        },

        doShowDeleteDialog: function(title, userid, userSignatureLong) {
            var buttons;
            // create container
            var container = new Element('div', {});
            var text = new Element('span', {
                'class': 'delete-hint',
                'html': getJSMessage('client.user.management.delete.user.data.text',
                    [ userSignatureLong ], null)
            });

            var html = "";
            html += '<input type="radio" name="deleteMode" value="disable" id="deleteMode1" checked="checked" />';
            html += '<label for="deleteMode1">'
                + getJSMessage('widget.user.management.profile.delete.mode.disable', [])
                + '</label>&emsp;';
            html += '<input type="radio" name="deleteMode" value="anonymize" id="deleteMode2" />';
            html += '<label for="deleteMode2">'
                + getJSMessage('widget.user.management.profile.delete.mode.anonymize', [])
                + '</label>';

            var selection = new Element('div', {
                'class': 'delete-selection',
                'html': html
            });

            text.inject(container);
            selection.inject(container);
            buttons = [];
            buttons.push({
                type: 'ok',
                action: function(dialogContainer) {
                    this.doConfirmUserDeletion(userid, dialogContainer);
                }.bind(this)
            });
            buttons.push({
                type: 'cancel'
            });

            showDialog(title, container, buttons, {
                width: 300
            });
            return false;
        },

        doShowAnonymizeDialog: function(title, userid, userSignatureLong) {
            // create container
            var container = new Element('div', {});
            var text = new Element('span', {
                'class': 'delete-hint',
                'html': getJSMessage('client.user.management.delete.user.data.text',
                    [ userSignatureLong ], null)
            });

            var html = "";
            html += '<input type="radio" name="deleteMode" value="anonymize" id="deleteMode2" checked="checked" />';
            html += '<label for="deleteMode2">'
                + getJSMessage('widget.user.management.profile.delete.mode.anonymize', [])
                + '</label>';

            var selection = new Element('div', {
                'class': 'delete-selection',
                'html': html
            });

            text.inject(container);
            selection.inject(container);

            this.doConfirmUserDeletion(userid, container);

            return false;
        },

        doConfirmUserDeletion: function(userId, dialogContainer) {

            var deleteModeElems = dialogContainer.getElements("input[name=deleteMode]");
            var checked = "";

            for ( var i = 0; i < deleteModeElems.length; i++) {
                if (deleteModeElems[i].checked) {
                    checked = deleteModeElems[i].value;
                    break;
                }
            }

            var args = "userId=" + userId + "&deleteMode=" + checked;

            var request = buildServiceRequest("/admin/client/usermanagement/confirmUserDeletion",
                args, function(jsonResponse) {
                    var buttons = [];
                    buttons.push({
                        type: 'yes',
                        action: function() {
                            this.doDeleteUser(userId, checked);
                        }.bind(this)
                    });
                    buttons.push({
                        type: 'no'
                    });
                    showDialog(jsonResponse.title, jsonResponse.confirmMessage, buttons, {
                        width: 300
                    });
                }.bind(this));

            request.send();
        }
    });
