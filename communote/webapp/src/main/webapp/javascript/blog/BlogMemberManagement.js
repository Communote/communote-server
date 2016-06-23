(function(namespace) {
    var BlogMemberManagementWidget = new Class({

        Extends: C_FilterWidget,
        Implements: LoadMoreSupport,

        widgetGroup: 'blog',

        userQueryMaxResultCount: 25,

        rightsQueryUrlBase: '/web/v1.1.3/blogRights.json',
        rolesQueryUrlBase: '/web/v1.1.3/blogRoles.json',

        editMode: false,
        addEntityAutocompleter: null,
        topicUtils: null,
        blogIdFilterParamName: null,
        placeholders: null,

        init: function() {
            this.parent();
            if (this.filterWidgetGroup) {
                blogIdFilterParameterName = this.getStaticParameter('blogIdFilterParameterName')
                        || 'blogId';
                // register for blogId changes by observing the provided filter parameter name
                this.observedFilterParams = [ blogIdFilterParameterName ];
                this.blogIdFilterParamName = blogIdFilterParameterName;
            } else {
                // get reference to topicUtils if available to take newest blog ID from there
                this.topicUtils = window.blogUtils;
            }
            this.initLoadMoreSupport();
            this.copyStaticParameter('editMode', false);
            this.editMode = this.getFilterParameter('editMode');
            this.copyStaticParameter('showEditModeToggle', true);
        },

        beforeRemove: function() {
            this.parent();
            this.disposeLoadMoreSupport();
            this.cleanup();
        },

        getListeningEvents: function() {
            return this.parent().combine(
                    [ 'onBlogMemberDelete', 'onBlogMemberSelfDelete', 'onBlogMemberSaveClick',
                            'onBlogMemberSelfSaveClick', 'onExternalObjectDelete' ]);
        },

        /**
         * @param entityId the id of the entity to set the role for
         * @param role the role of the user to set (VIEWER; MEMBER; MANAGER; NONE), NONE to delete
         * @param whileAdding set to TRUE if you try to add a new user, FALSE to modify an existing
         *            one.
         * @param selfAction set to TRUE if the action targets the currently authenticated user
         */
        setMemberBlogRole: function(entityId, role, whileAdding, selfAction) {

            if (role == null)
                role = 'NONE';

            new Request.JSON({
                url: buildRequestUrl(this.rolesQueryUrlBase),
                'onComplete': function(response) {
                    var newRole, eventHandler, blogId;
                    this.stopLoadingFeedback();
                    if (response.status == 'ERROR') {
                        showNotification(NOTIFICATION_BOX_TYPES.failure, null, response.message,
                                null);
                    } else {
                        if (role == 'NONE') {
                            // user was deleted
                            showNotification(NOTIFICATION_BOX_TYPES.success, null,
                                    getJSMessage('blog.member.management.removeuser.success'), null);
                        } else {
                            // user was added OR MODIFIED
                            showNotification(NOTIFICATION_BOX_TYPES.success, null,
                                    getJSMessage('blog.member.management.addentity.success'), null);
                        }
                        if (selfAction) {
                            blogId = this.getCurrentBlogId();
                            // check the role of the current user via a sync request
                            // TODO this is a terrible hack - refactor this method to blogutils!
                            eventHandler = this.widgetController.getFilterEventProcessor()
                                    .getEventHandler('createNoteFilterGroup');
                            newRole = eventHandler._getBlogRole(blogId);
                            if (newRole != 'MANAGER') {
                                if (newRole != 'NONE') {
                                    // close management section
                                    E('onSaveManagementSection', 'blog-management');
                                    // TODO fire custom events to inform other widgets about new role
                                } else {
                                    // TODO ugly, use special event or smth else
                                    // refresh all other widgets by reseting and than selecting blog again
                                    E2G('onBlogClick', null, null);
                                    E2G('onBlogClick', null, blogId);
                                }
                            } else {
                                this.resetAddEntity();
                            }
                        } else {
                            // TODO pass some details about what changed and maybe rename to topicRoleChanged
                            E('onUserRoleChanged');
                            this.resetLoadMoreState();
                            this.refresh();
                        }
                    }
                }.bind(this)
            }).post({
                'blogId': this.getCurrentBlogId(),
                'entityId': entityId,
                'role': role
            });
        },
        /**
         * @param role the role to set for all users to set (VIEWER; MEMBER),
         * 
         */
        setAllUserRole: function(role) {
            var allCanRead = role == 'VIEWER' || role == 'MEMBER';
            var allCanWrite = role == 'MEMBER';
            new Request.JSON({
                url: buildRequestUrl(this.rightsQueryUrlBase),
                'onComplete': function(response) {

                    if (response.status == 'ERROR') {

                        showNotification(NOTIFICATION_BOX_TYPES.failure, null, response.message,
                                null);
                    } else {

                        showNotification(NOTIFICATION_BOX_TYPES.success, null, getJSMessage(
                                'blog.member.management.public.updated', []), null);

                        E('onUserRoleChanged');
                        this.resetLoadMoreState();
                        this.refresh();
                    }

                    // refresh widget or update by jscript
                }.bind(this)
            }).post({
                'blogId': this.getCurrentBlogId(),
                'allCanRead': allCanRead,
                'allCanWrite': allCanWrite
            });
        },
        inviteUser: function() {

        },

        cleanup: function() {
            if (this.addEntityAutocompleter) {
                this.addEntityAutocompleter.destroy();
            }
            if (this.placeholders) {
                this.placeholders.destroy();
            }
        },
        
        refreshStart: function() {
            this.cleanup();
            this.setFilterParameter('blogId', this.getCurrentBlogId());
        },

        refreshComplete: function(responseMetadata) {
            searchAndShowRoarNotification(this.domNode);
            this.attachAddEntityAutocompleter();
        },
        
        attachAddEntityAutocompleter: function() {
            var autocompleter, acOptions, userInput;
            if (!this.editMode) {
                return;
            }
            userInput = this.domNode.getElement("#add-member");
            // only add auto completer if the input is there (i.e. if the user is manager)
            if (userInput) {
                acOptions = {
                    'dataSourceOptions': {
                        'postData': {
                            'maxCount': this.userQueryMaxResultCount,
                            'excludeBlogId': this.getCurrentBlogId()
                        }
                    }
                };
                autocompleter = autocompleterFactory.createUserAutocompleter(userInput, acOptions,
                        null, 'ENTITY', true);
                autocompleter.addEvent('onChoiceSelected',
                        function(elem, choiceElem, token, value) {
                            var entityField = $('add-entityId');
                            entityField.set('value', token['id']);
                            this.domNode.getElementById('save-member-role_add').setStyle('display',
                                    'block');
                            this.domNode.getElementById('add-member').setProperty('disabled',
                                    'disabled');
                        }.bind(this));
                this.placeholders = communote.utils.attachPlaceholders(null, this.domNode);
                this.addEntityAutocompleter = autocompleter;
            }
        },

        addEntity: function() {
            var buttons, entity, role, resetEntityFunction;
            this.startLoadingFeedback();
            entity = this.domNode.getElement("#add-entityId");
            role = this.domNode.getElement("#add-role");

            if (entity.value == '') {

                showNotification(NOTIFICATION_BOX_TYPES.warning, null, getJSMessage(
                        'blog.member.management.addentity.error.empty.entityid', []), {
                    'duration': '12000'
                });
                this.resetAddEntity();
            } else if (entity.value == communote.currentUser.id) {
                resetEntityFunction = this.resetAddEntity.bind(this);
                buttons = [];
                buttons.push({
                    type: 'yes',
                    action: this.setMemberBlogRole
                            .bind(this, entity.value, role.value, false, true)
                });
                buttons.push({
                    type: 'no',
                    action: resetEntityFunction
                });
                showDialog(getJSMessage('blog.member.management.selfmodify.title'),
                        getJSMessage('blog.member.management.selfmodify.question'), buttons, {
                            onCloseCallback: resetEntityFunction
                        });
            } else {
                this.setMemberBlogRole(entity.value, role.value, true, false);
            }

            return false;
        },

        resetAddEntity: function() {
            var addMemberElem = this.domNode.getElementById('add-member');
            addMemberElem.value = '';
            addMemberElem.removeProperty('disabled');
            this.domNode.getElementById('add-entityId').value = '';
            this.domNode.getElementById('save-member-role_add').setStyle('display', 'none');
            var errorDiv = addMemberElem.getNext('.error');
            if (errorDiv != null) {
                errorDiv.dispose();
            }
            this.stopLoadingFeedback();
        },

        onBlogMemberDelete: function(memberId) {
            this.startLoadingFeedback();
            this.setMemberBlogRole(memberId, null, false, false);
        },

        onBlogMemberSelfDelete: function(memberId) {
            var buttons = [];
            buttons.push({
                type: 'yes',
                action: function() {
                    this.setMemberBlogRole(memberId, null, false, true);
                    E('onBlogUpdate');
                    E('onBlogDelete');
                }.bind(this)
            });
            buttons.push({
                type: 'no'
            });

            showDialog(getJSMessage('blog.member.management.selfdelete.title'),
                    getJSMessage('blog.member.management.selfdelete.question'), buttons);
        },

        saveAllUserRoleClick: function() {
            this.startLoadingFeedback();
            var newRole = this.domNode.getElement("#change-member_all").value;
            this.setAllUserRole(newRole);
            return false;
        },

        showHideEditActions: function(el, userId, show) {
            // check if action is located in sub members
            var actionDisplayStyle, viewDisplyStyle;
            var prefix = $(el).getParent().get('id').split('_');
            if (prefix[0] != 'sub') {
                prefix = '';
            } else {
                prefix = prefix[0] + '_';
            }
            if (show) {
                actionDisplayStyle = 'block';
                viewDisplyStyle = 'none';
            } else {
                actionDisplayStyle = 'none';
                viewDisplyStyle = 'block';
            }
            if (!show) {
                this.domNode.getElement('#' + prefix + 'view-member-role_' + userId).getParent()
                        .reset();
            }

            // change buttons to submit
            this.domNode.getElement('#' + prefix + 'modify-member-role_' + userId).setStyle(
                    'display', viewDisplyStyle);
            this.domNode.getElement('#' + prefix + 'save-member-role_' + userId).setStyle(
                    'display', actionDisplayStyle);
            // always return false for using as onclick-handler
            return false;
        },

        toggleSystems: function(el, entityId) {
            var systems = this.domNode.getElement('#sub_view-systems_' + entityId);
            if (systems.hasClass('cn-hidden')) {
                $(el).set('text', getJSMessage('blog.member.management.details.hide'));
                systems.removeClass('cn-hidden');
            } else {
                $(el).set('text', getJSMessage('blog.member.management.details.show'));
                systems.addClass('cn-hidden');
            }

            // always return false for using as onclick-handler
            return false;
        },

        toggleSubMembers: function(el, memberId) {
            var subMemberDiv = this.domNode.getElement('#sub-members_' + memberId);

            // making an element in Internet Explorer "grab" all the Element methods
            el = $(el);

            if (el.hasClass('icon-closed')) {
                el.addClass('icon-opened');
                el.removeClass('icon-closed');
                subMemberDiv.setStyle('display', 'block');
            } else {
                el.addClass('icon-closed');
                el.removeClass('icon-opened');
                subMemberDiv.setStyle('display', 'none');
            }

            // always return false for using as onclick-handler
            return false;
        },

        onBlogMemberSaveClick: function(memberInfo) {
            this.startLoadingFeedback();
            var newRole = this.domNode.getElement('#change-member_' + memberInfo.id).value;
            this.setMemberBlogRole(memberInfo.id, newRole);
            return false;
        },

        onBlogMemberSelfSaveClick: function(memberInfo) {
            var buttons = [];
            buttons.push({
                type: 'yes',
                action: function() {
                    var newRole = this.domNode.getElement('#change-member_' + memberInfo.id).value;
                    this.setMemberBlogRole(memberInfo.id, newRole, false, true);
                    E('onBlogUpdate');
                }.bind(this)
            });
            buttons.push({
                type: 'no'
            });

            showDialog(getJSMessage('blog.member.management.selfmodify.title'),
                    getJSMessage('blog.member.management.selfmodify.question'), buttons);
            return false;
        },

        onExternalObjectDelete: function() {
            this.resetLoadMoreState();
            this.refresh();
        },

        getCurrentBlogId: function() {
            var blogId;
            if (this.filterWidgetGroup) {
                // take blog ID from observed filter parameter store
                blogId = this.filterParamStore.getFilterParameter(this.blogIdFilterParamName);
            } else {
                // use topicUtils to get newest blogId otherwise take from static parameters
                if (this.topicUtils) {
                    blogId = this.topicUtils.getCurrentBlogId();
                } else {
                    blogId = this.getStaticParameter('blogId');
                }
            }
            return blogId;
        },
        
        openEditMode: function(edit) {
            if (this.editMode != edit) {
                if (!edit && this.getStaticParameter('cancelUrl')) {
                    location.href = this.getStaticParameter('cancelUrl');
                    return;
                }
                this.editMode = edit;
                this.setFilterParameter('editMode', edit);
                this.resetLoadMoreState();
                this.refresh();
            }
        }
    });
    
    if (namespace && namespace.addConstructor) {
        namespace.addConstructor('BlogMemberManagementWidget', BlogMemberManagementWidget);
    } else {
        window.BlogMemberManagementWidget = BlogMemberManagementWidget;
    }
})();