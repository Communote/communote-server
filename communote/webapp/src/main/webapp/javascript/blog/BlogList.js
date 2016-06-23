var BlogListWidget = new Class({
    Extends: C_FilterWidget,
    Implements: [ FilterParamsHandlerStrategyByName, LoadMoreSupport ],

    /**
     * Default action of FilterParamsHandlerStrategyByName is a refresh, thus we want to break.
     */
    breakAfterDefaultAction: true,

    disableLoadMoreOnPopularitySort: false,
    widgetGroup: 'blog',

    regex: /blog_(\d+)/i,

    observedFilterParams: [ 'targetBlogId', 'blogId', 'tagPrefix', 'userId', 'showPostsForMe',
            'postTextSearchString', 'searchString', 'startDate', 'endDate', 'filter', 'tagIds',
            'discussionId', 'noteId', 'propertyFilter', 'minRank' ],
    shownTopicIds: undefined,
    showsFirstPage: false,
    showsMaxCountTopics: false,
    

    /**
     * @override
     */
    init: function() {
        this.parent();
        this.initLoadMoreSupport();
        this.setSortModeParameter(this.getStaticParameter('sortMode'));
        if (this.getStaticParameter('disableLoadMoreOnPopularitySort')) {
            // TODO if we would support one of the append modes we should not initLoadMore and so on
            this.disableLoadMoreOnPopularitySort = true;
        }
        // ignore 'blogId' when refreshing since the selected blogId should be contained and is highlighted
        this.filterParamsToExclude = ['blogId'];
        this.copyStaticParameter('showSortModeSelector', false);
    },

    /**
     * @override
     */
    beforeRemove: function() {
        this.parent();
        this.disposeLoadMoreSupport();
    },
    /**
     * Implementation of the default action required by the FilterParamsHandlerStrategyByName.
     */
    handleChangedParamsDefaultAction: function(changedParam) {
        this.resetLoadMoreState();
        this.refresh();
    },

    blogIdChanged: function() {
        this.refreshBlogList();
    },

    getListeningEvents: function() {
        return this.parent().combine([ 'onBlogUpdate', 'onReloadPostList', 'onNotesChanged' ]);
    },
    onBlogUpdate: function() {
        this.resetLoadMoreState();
        this.refresh();
    },
    
    onNotesChanged: function(params) {
        var topicId;
        var sortAlphabetic = !this.getFilterParameter('topicSortByLatestNote');
        // do nothing when a note was edited
        if (params.action !== 'edit') {
            // always refresh if sorting by popularity
            if (!sortAlphabetic) {
                this.refresh();
            } else {
                topicId = params.topicId;
                // refresh if topic is not yet contained and less than maxCount items are shown
                if (!this.showsMaxCountTopics && this.showsFirstPage && (!topicId || this.shownTopicIds.indexOf(topicId.toString()) == -1)) {
                    this.refresh();
                }
            }
        }
    },
    
    onReloadPostList: function() {
        this.resetLoadMoreState();
        this.refresh();
    },

    refreshBlogList: function() {
        var prefixLength, items, i, item, blogId;
        var selectedBlogs = this.filterParamStore.getFilterParameter('blogId', true);
        if (!selectedBlogs) {
            selectedBlogs = [];
        } else {
            selectedBlogs = Array.from(selectedBlogs);
        }
        this.shownTopicIds = [];
        prefixLength = (this.widgetId + '_blog_').length;
        items = this.domNode.getElements('.bloglistitem');
        for (i = 0; i < items.length; i++) {
            item = items[i];
            blogId = item.get('id').substring(prefixLength);
            this.shownTopicIds.push(blogId);
            if (selectedBlogs.contains(blogId)) {
                item.addClass('cn-active');
            } else {
                item.removeClass('cn-active');
            }
        }
    },
    
    refreshComplete: function(responseMetadata) {
        this.parent(responseMetadata);
        this.showsMaxCountTopics = responseMetadata.numberOfElementsContained == this
                .getFilterParameter('maxCount');
        this.showsFirstPage = this.getLoadMoreMode() !== 'paging'
                || this.getFilterParameter('offset') == 0;
        this.refreshBlogList();
    },
    
    setSortMode: function(sortMode) {
        this.setSortModeParameter(sortMode);
        this.resetLoadMoreState();
        this.refresh();
    },
    setSortModeParameter: function(sortMode) {
        var popularitySort = 'popularity' === sortMode;
        this.setFilterParameter('topicSortByLatestNote', popularitySort);
        if (popularitySort && this.disableLoadMoreOnPopularitySort) {
            this.setFilterParameter('loadMoreMode', null);
        } else if (!this.getFilterParameter('loadMoreMode')) {
            // reenable
            this.setFilterParameter('loadMoreMode', this.getLoadMoreMode());
        }
    }
});
