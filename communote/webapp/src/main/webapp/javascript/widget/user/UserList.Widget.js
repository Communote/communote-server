(function(namespace) {
    var UserListWidget = new Class({
        Extends: C_FilterWidget,
        Implements: [ LoadMoreSupport ],

        widgetGroup: 'users',

        observedFilterParams: [ 'searchString', 'tagIds', 'tagPrefix' ],

        checkGoToTopTimeout: null,

        /**
         * @override
         */
        init: function() {
            this.parent();
            this.initLoadMoreSupport();
            this.checkGoToTopTimeout = this.checkGoToTop.periodical(1500, this);
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
            if (params.userId == undefined) {
                return;
            }
            if (this.filterParamStore.getFilterParameter('showFollowedItems')) {
                this.resetLoadMoreState();
                this.refresh();
            } else {
                topicEntry = this.domNode.getElementById(this.widgetId + '-user-' + params.blogId);
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
         * @override
         */
        getListeningEvents: function() {
            return this.parent().combine([ 'onItemFollowed', 'onItemUnfollowed' ]);
        },

        /**
         * @override Implementation of LoadSupport method which sets the new parameters to get more
         *           data
         */
        loadMoreHasMoreData: function(responseMetadata) {
            this.setLoadMoreParameters({
                offset: responseMetadata.nextOffset
            });
        },

        onItemFollowed: function(params) {
            this.followStatusChanged(true, params);
        },

        onItemUnfollowed: function(params) {
            this.followStatusChanged(false, params);
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
            this.domNode.getElements('span[name=lastClear]').dispose();
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
        }

    });
    if (namespace && namespace.addConstructor) {
        namespace.addConstructor('UserListWidget', UserListWidget);
    } else {
        window.UserListWidget = UserListWidget;
    }

})(window.runtimeNamespace);