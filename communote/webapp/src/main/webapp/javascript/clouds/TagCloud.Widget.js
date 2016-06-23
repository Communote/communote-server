/**
 * A TagCloud widget. static parameters: isStaticCloud - denotes whether the tag cloud should be
 * static, which means that a click on a tag will not filter and refresh the tag cloud (default:
 * false)
 */
var TagCloudWidget = new Class({
    Extends: C_FilterWidget,
    Implements: FilterParamsHandlerStrategyByName,

    /**
     * Default action of FilterParamsHandlerStrategyByName is a refresh, thus we want to break.
     */
    breakAfterDefaultAction: true,

    widgetGroup: "clouds",
    isStaticCloud: false,
    showsMaxCountTags: false,

    observedFilterParams: [ 'filter', 'postTextSearchString', 'discussionId',
            'noteId', 'propertyFilter', 'tagIds', 'minRank' ],

    init: function() {
        this.parent();
        var statParams = this.getAllStaticParameters();
        if (this.isTrue(statParams['isStaticCloud'])) {
            this.isStaticCloud = true;
        }
        // set defaults
        this.setFilterParameter('showTitle', true);
        this.setFilterParameter('showHistory', true);
        this.setFilterParameter('numberOfLastDays', 0);
        this.setFilterParameter('tagCloudMode', 'SomeTags');
        this.setFilterParameter('mode', 'trend');
        // read static parameters
        this.copyStaticParameter('maxCount');
        this.copyStaticParameter('showTitle');
        this.copyStaticParameter('showHistory');
        this.copyStaticParameter('numberOfLastDays');
        this.copyStaticParameter('tagCloudMode');
        this.copyStaticParameter('isStaticCloud');
        this.copyStaticParameter('hideSelectedTags');

        this.initObservedParams(statParams);
    },
    
    /**
     * @override
     */
    beforeRemove: function() {
        this.parent();
        this.detachClickEvents();
    },
    
    detachClickEvents: function() {
        if (this.firstDOMLoadDone) {
            if (this.isTrue(this.getFilterParameter('showHistory'))) {
                this.domNode.getElements('div.tagHistory a').removeEvents('click');
            }
            this.domNode.getElements('ul.control-tagcloud li a').removeEvents('click');
        }
    },

    initObservedParams: function(statParams) {
        if (statParams['listenUsers'] == null || this.isTrue(statParams['listenUsers'])) {
            this.observedFilterParams.push('userId');
            this.observedFilterParams.push('searchString');
        }
        if (statParams['listenSearch'] == null || this.isTrue(statParams['listenSearch'])) {
            this.observedFilterParams.push('tagPrefix');
        }
        if (this.isTrue(statParams['listenGroups'])) {
            this.observedFilterParams.push('groupId');
        }
        if (this.isTrue(statParams['listenBlog'])) {
            this.observedFilterParams.push('blogId');
            this.observedFilterParams.push('targetBlogId');
            this.observedFilterParams.push('showPostsForMe');
            this.observedFilterParams.push('startDate');
            this.observedFilterParams.push('endDate');
        }
    },
    
    isTrue: function(value) {
        return (value === true || value == 'true');
    },

    /**
     * Implementation of the default action required by the FilterParamsHandlerStrategyByName.
     */
    handleChangedParamsDefaultAction: function(changedParam) {
        this.refresh();
    },

    /**
     * Sets the maximum of elements to be shown.
     * 
     * @param maxCount the upper limit of tags to be shown
     * @param dayLimit the number of days that should be considered when retrieving the tags. A
     *            value of 0 means that all tags will be considered. A value greater then 0 would
     *            restrict the tags to be considered to the time span expanding from the current day
     *            to the current day minus dayLimit.
     */
    setMaxElementCount: function(maxCount, setTrend) {
        this.setFilterParameter('maxCount', maxCount);
        this.setFilterParameter('mode', setTrend ? 'trend' : 'all');
        this.refresh();
    },

    filterChanged: function() {
        var tags;
        // only refresh if cloud is not static
        if (!this.isStaticCloud) {
            this.refresh();
        } else {
            tags = this.filterParamStore.getFilterParameter('filter');
            if (typeOf(tags) != 'array') {
                tags = [ tags ];
            }
            this.domNode.getElements('a').each(function(item) {
                if (tags.contains(item.getProperty('name'))) {
                    item.addClass('cn-selected');
                } else {
                    item.removeClass('cn-selected');
                }
            });
        }
    },

    refreshComplete: function(responseMetadata) {
        this.parent(responseMetadata);
        this.showsMaxCountTags = responseMetadata.numberOfElementsContained == this
                .getFilterParameter('maxCount');
        if (this.isTrue(this.getFilterParameter('showHistory'))) {
            // attach click event to all tag history links
            this.domNode.getElements('div.tagHistory a').each(
                    function(tag) {
                        var hist = tag.getProperty('rel');
                        var tags = null;
                        if (hist != null && typeOf(hist) == 'string' && hist.length > 0) {
                            tags = hist.split(',');
                        }
                        tag.addEvent('click', E2G.bind(this, 'onTagHistoryClick',
                                this.filterWidgetGroup.id, tags));
                    }, this);
        }
        // attach click event to all tag cloud links
        this.domNode.getElements('ul.control-tagcloud li a').each(function(tag) {
            tag.addEvent('click', this.onTagClick.bind(this, tag));
        }, this);
    },
    
    /**
     * @override
     */
    refreshStart: function() {
        this.parent();
        this.detachClickEvents();
    },

    onTagClick: function(tag) {
        this.sendFilterGroupEvent("onTagIdClick", [ tag.getProperty('rel'),
                tag.getProperty('name'), tag.hasClass("cn-selected") ]);
        return false;
    },

    onNotesChanged: function(params) {
        // only refresh while not showing maxCount tags because it is quite unlikely that tag cloud changed
        if (!this.showsMaxCountTags) {
            this.refresh();
        }
    },

    onReloadPostList: function() {
        this.refresh();
    },

    getListeningEvents: function() {
        var arrEvents = [];
        if (this.isTrue(this.getStaticParameter('listenBlog'))) {
            arrEvents.push('onNotesChanged');
            arrEvents.push('onReloadPostList');
        }
        return arrEvents.combine(this.parent());
    }
});
