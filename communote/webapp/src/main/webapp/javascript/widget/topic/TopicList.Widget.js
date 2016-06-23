(function(namespace) {
    var TopicListWidget = new Class({
        Extends: C_FilterWidget,
        Implements: [ LoadMoreSupport ],

        widgetGroup: 'blog',

        observedFilterParams: [ 'filter', 'tagIds', 'pattern', 'tagPrefix', 'parentTopicIds', 'viewType' ],

        checkGoToTopTimeout: null,

        /**
         * @override
         */
        init: function() {
            this.parent();
            this.initLoadMoreSupport();
            this.copyStaticParameter('showNew');
            this.copyStaticParameter('showAdd');
            if (!this.getStaticParameter('loadMoreMaxCount')) {
                this.setStaticParameter('loadMoreMaxCount', 21);
            }
        },

        /**
         * @override
         */
        afterShow: function(initPhase, wasDirty) {
            if (!initPhase && !wasDirty) {
                this.resumeLoadMoreSupport();
                this.checkGoToTopTimeout = this.checkGoToTop.periodical(1500, this);
            }
        },
        
        /**
         * Method to ask for getting access to the given topic.
         * 
         * @param {Number} topicId Id of the topic.
         * @param {String} currentRole Current role of the current user
         * @param {String} title Title of the topic
         * @param {String[]} existingManagers Array of existing manager names.
         */
        askForGainTopicAccess: function(topicId, title, currentRole, existingManagers) {
            var buttons, htmlElement, html;
            html = getJSMessage('blog.overview.tab.admister.dialog.description', [ existingManagers
                    .join(', ') ]);
            htmlElement = new Element('div', {
                'id': 'cn-gain-topic-access-dialog',
                'html': html
            });

            buttons = [];
            buttons.push({
                type: 'gain-access',
                cssClass: 'main',
                label: getJSMessage('blog.overview.tab.admister.dialog.ok'),
                action: this.gainTopicAccess.bind(this, topicId, currentRole)
            });
            buttons.push({
                type: 'cancel'
            });
            showDialog(title, htmlElement, buttons, {
                cssClasses: 'cn-gain-topic-access'
            });
        },
        
        /**
         * @override
         */
        beforeHide: function(initPhase) {
            if (!initPhase) {
                this.detachGoToTop();
                this.pauseLoadMoreSupport();
            }
        },
        
        /**
         * @override
         */
        beforeRemove: function() {
            this.parent();
            this.detachGoToTop();
            this.disposeLoadMoreSupport();
        },


        checkGoToTop: function() {
            var topScroller = this.domNode.getElementById(this.widgetId + '-top-scroller');
            // might be null if the widget wasn't refreshed yet
            if (topScroller) {
                if (window.getScrollTop() < this.domNode.getCoordinates().top) {
                    topScroller.addClass('cn-hidden');
                } else {
                    topScroller.removeClass('cn-hidden');
                }
            }
        },

        detachGoToTop: function() {
            if (this.checkGoToTopTimeout) {
                clearInterval(this.checkGoToTopTimeout);
                this.checkGoToTopTimeout = null;
            }
        },
        
        /**
         * @override
         */
        filterParametersChanged: function(changedParams) {
            this.resetLoadMoreState();
            this.refresh();
        },

        followStatusChanged: function(follow, params) {
            var topicEntry;
            if (params.blogId == undefined) {
                return;
            }
            if (this.filterParamStore.getFilterParameter('showFollowedItems')) {
                this.resetLoadMoreState();
                this.refresh();
            } else {
                topicEntry = this.domNode.getElementById(this.widgetId + '-topic-' + params.blogId);
                if (topicEntry) {
                    if (follow) {
                        topicEntry.addClass('cn-list-entry-followed');
                    } else {
                        topicEntry.removeClass('cn-list-entry-followed');
                    }
                }
            }
        },
        /**
         * Method to for getting access to the given topic.
         * 
         * @param {Number} topicId The ID of the to topic
         * @param {String} oldRole Old role of the current user
         * @param {Element} dialogContainer The container to contain information about the selected
         *            topic..
         */
        gainTopicAccess: function(topicId, oldRole, dialogContainer) {
            blogUtils.setTopicRole(communote.currentUser.id, topicId, 'MANAGER',
                    this.gainTopicAccess_success.bind(this, topicId, oldRole));
        },
        /**
         * This is called, when the user successfully gained access to the topic.
         * 
         * @param {Number} topicId The ID of the to topic
         * @param {String} oldRole Old role of the current user
         */
        gainTopicAccess_success: function(topicId, oldRole) {
            showNotification(NOTIFICATION_BOX_TYPES.success, null,
                    getJSMessage('blog.overview.tab.admister.dialog.success'));
            E('onTopicRoleChanged', {
                newRole: 'MANAGER',
                oldRole: oldRole,
                topicId: topicId,
                userId: communote.currentUser.id
            });
        },

        /**
         * @override
         */
        getListeningEvents: function() {
            return this.parent().combine(
                    [ 'onItemFollowed', 'onItemUnfollowed', 'onTopicRoleChanged' ]);
        },

        /**
         * @override
         */
        loadMoreHasMoreData: function(responseMetadata) {
            this.setLoadMoreParameters({
                offset: responseMetadata.nextOffset,
                'showNew': false,
                'maxCount': this.getStaticParameter('loadMoreMaxCount')
            });
        },
        
        onItemFollowed: function(params) {
            this.followStatusChanged(true, params);
        },

        onItemUnfollowed: function(params) {
            this.followStatusChanged(false, params);
        },

        onTopicRoleChanged: function(params) {
            var refresh = false;
            if (params.userId == communote.currentUser.id) {
                if (this.filterParamStore.getFilterParameter('blogAccess') == 'manager') {
                    if (params.newRole == 'MANAGER' || params.oldRole == 'MANAGER') {
                        refresh = true;
                    }
                } else if (this.filterParamStore.getFilterParameter('forceAllTopics')) {
                    refresh = true;
                } else if (params.newRole == 'NONE' || params.oldRole == 'NONE') {
                    refresh = true;
                }
            }
            if (refresh) {
                this.resetLoadMoreState();
                this.refresh();
            }
        },
        
        /**
         * @override
         */
        partialRefreshComplete: function(domNode, context, responseMetadata) {
            this.loadMoreRefreshComplete(context, responseMetadata);
        },
        
        /**
         * @override
         */
        partialRefreshStart: function(domNode, context) {
            this.loadMoreRefreshStart(context);
        },

        /**
         * @override
         */
        refreshComplete: function(responseMetadata) {
            init_tips(this.domNode);
            searchAndShowRoarNotification(this.domNode);
            this.checkGoToTopTimeout = this.checkGoToTop.periodical(1500, this);
            this.loadMoreRefreshComplete(null, responseMetadata);
        },
        
        /**
         * @override
         */
        refreshStart: function() {
            this.loadMoreRefreshStart(null);
            this.detachGoToTop();
        },

        /**
         * Method to toggle the click on the more tile.
         * 
         * @param showOnlyNonToplevelRootTopics True or false.
         */
        setShowOnlyNonToplevelRootTopics: function(showOnlyNonToplevelRootTopics) {
            this.setFilterParameter('showOnlyRootTopics', !showOnlyNonToplevelRootTopics);
            this.setFilterParameter('showOnlyNonToplevelRootTopics', showOnlyNonToplevelRootTopics);
            this.resetLoadMoreState();
            this.refresh();
        }
    });

    if (namespace && namespace.addConstructor) {
        namespace.addConstructor('TopicListWidget', TopicListWidget);
    } else {
        window.TopicListWidget = TopicListWidget;
    }

})(window.runtimeNamespace);