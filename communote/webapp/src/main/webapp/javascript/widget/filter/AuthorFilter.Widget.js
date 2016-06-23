(function(namespace) {
    var AuthorFilterWidget = new Class({
        Extends: C_FilterWidget,
        Implements: [ FilterParamsHandlerStrategyByName, LoadMoreSupport ],

        /**
         * Default action of FilterParamsHandlerStrategyByName is a refresh, thus we want to break.
         */
        breakAfterDefaultAction: true,

        widgetGroup: 'user',
        selectionCssClass: 'cn-selected',
        showsCurrentUser: false,
        // whether the last refreshed returned the requested maxCount users
        showsMaxCountUsers: false,
        showsFirstPage: true,

        observedFilterParams: [ 'targetBlogId', 'blogId', 'tagPrefix', 'userId', 'showPostsForMe',
                'showFavorites', 'showFollowedItems', 'postTextSearchString', 'searchString',
                'startDate', 'endDate', 'filter', 'tagIds', 'discussionId', 'noteId',
                'propertyFilter', 'minRank' ],

        /**
         * @override
         */
        init: function() {
            var selectionClass, userIdFilter, filterLength;
            var ignoreUserIdsFilter = true;
            this.parent();
            selectionClass = this.getStaticParameter('selectionCssClass');
            if (selectionClass) {
                this.selectionCssClass = selectionClass;
            }
            this.initLoadMoreSupport();
            if (this.filterParamStore) {
                // if userId parameter is set as an unresetable parameter do not ignore userId filter
                userIdFilter = this.filterParamStore.getUnresetableFilterParameter('userId');
                if (userIdFilter) {
                    if (typeOf(userIdFilter) === 'array') {
                        filterLength = userIdFilter.length;
                    } else {
                        filterLength = 1;
                    }
                    if (filterLength) {
                        ignoreUserIdsFilter = false;
                    }
                }
            }
            this.setFilterParameter('ignoreUserIdsFilter', ignoreUserIdsFilter);
        },

        /**
         * @override
         */
        beforeRemove: function() {
            this.parent();
            this.disposeLoadMoreSupport();
        },

        /**
         * @override
         */
        getListeningEvents: function() {
            return this.parent().combine(
                    [ 'onUserLogoChanged', 'onUserProfileChanged', 'onNotesChanged',
                      'onReloadPostList' ]);
        },
        /**
         * Implementation of the default action required by the FilterParamsHandlerStrategyByName.
         */
        handleChangedParamsDefaultAction: function(changedParam) {
            this.resetLoadMoreState();
            this.refresh();
        },


        onReloadPostList: function() {
            this.refresh();
        },

        onNotesChanged: function(params) {
            if (params.action === 'delete') {
                // assume if only one note is deleted it is a note of the current user
                if (params.deletedNotesCount > 1 || this.showsCurrentUser) {
                    this.refresh();
                }
            } else if (params.action !== 'edit') {
                // for edit do nothing, for create or comment refresh if current user
                // is not yet contained and less than the maxCount items are shown
                if (!this.showsCurrentUser && !this.showsMaxCountUsers && this.showsFirstPage) {
                    this.refresh();
                }
            }
        },

        onUserLogoChanged: function(imagePath) {
            // check for current user and change his logo
            var userImg = this.domNode.getElementById(this.widgetId + '_userimage_'
                    + communote.currentUser.id);
            if (userImg) {
                userImg.src = imagePath;
            }
        },
        
        onUserProfileChanged: function() {
            if (this.showsCurrentUser) {
                this.refresh();
            }
        },

        /**
         * @override
         */
        refreshComplete: function(responseMetadata) {
            this.parent(responseMetadata);
            this.showsCurrentUser = false;
            this.showsMaxCountUsers = responseMetadata.numberOfElementsContained == this
                    .getFilterParameter('maxCount');
            this.showsFirstPage = this.loadMoreSettings.mode !== 'paging'
                    || this.getFilterParameter('offset') == 0;
            // change CSS class of selected users
            this.refreshUserList();
        },

        refreshUserList: function() {
            var list, i, userId, elem;
            var currentUserId = communote.currentUser.id;
            var selected = Array.from(this.filterParamStore.getFilterParameter('userId', true));
            list = this.domNode.getElements('.control-userlist-item');
            for (i = 0; i < list.length; i++) {
                elem = list[i];
                userId = elem.getProperty('data-cnt-user-id')
                if (!this.showsCurrentUser && currentUserId == userId) {
                    this.showsCurrentUser = true;
                }
                if (selected.contains(userId)) {
                    elem.addClass(this.selectionCssClass);
                } else {
                    elem.removeClass(this.selectionCssClass);
                }
            }
        },

        /**
         * Sets the number maximal elements to be shown.
         * 
         * @param maxCount The number.
         */
        setMaxElementCount: function(maxCount) {
            this.setFilterParameter('maxCount', maxCount);
            this.resetLoadMoreState();
            this.refresh();
        },
        /**
         * @param mode The mode to use, can be "all" or "trend".
         */
        switchMode: function(mode) {
            this.setFilterParameter('mode', mode);
            this.resetLoadMoreState();
            this.refresh();
        },
        userIdChanged: function() {
            this.refreshUserList();
        }
    });
    if (namespace && namespace.addConstructor) {
        namespace.addConstructor('AuthorFilterWidget', AuthorFilterWidget);
    } else {
        window.AuthorFilterWidget = AuthorFilterWidget;
    }
})(window.runtimeNamespace);