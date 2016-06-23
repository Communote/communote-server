var BlogTagCloudWidget = new Class({
    Extends: C_FilterWidget,

    widgetGroup: 'clouds',

    observedFilterParams: [ 'filter', 'tagIds',  'pattern', 'tagPrefix', 'parentTopicIds', 'viewType' ],

    init: function() {
        this.parent();
        // read static parameters
        this.copyStaticParameter('maxCount');
        this.copyStaticParameter('hideSelectedTags');
    },

    attachClickEvents: function() {
        // attach click event to all tag cloud links
        this.domNode.getElements('ul.control-tagcloud li a').each(function(tag) {
            tag.addEvent('click', this.onTagClick.bind(this, tag));
        }, this);
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
            this.domNode.getElements('ul.control-tagcloud li a').removeEvents('click');
        }
    },

    /**
     * @override
     */
    getListeningEvents: function() {
        return this.parent().combine([ 'onItemFollowed', 'onItemUnfollowed' ]);
    },

    onItemFollowed: function(params) {
        if (this.filterParamStore.getFilterParameter('showFollowedItems')) {
            this.refresh();
        }
    },

    onItemUnfollowed: function(params) {
        if (this.filterParamStore.getFilterParameter('showFollowedItems')) {
            this.refresh();
        }
    },
    
    onTagClick: function(tag) {
        var isSelected = tag.hasClass("cn-selected");
        // TODO use automatic data store insertion of E2G
        widgetController.getDataStore().put({
            type: 'tag',
            key: tag.getProperty('rel'),
            title: tag.getProperty('name')
        });
        this.sendFilterGroupEvent("onTagIdClick", [ tag.getProperty('rel'),
                tag.getProperty('name'), isSelected ]);
        return false;
    },
    
    /**
     * @override
     */
    refreshComplete: function(responseMetadata) {
        this.parent(responseMetadata);
        this.attachClickEvents();
    },
    
    /**
     * @override
     */
    refreshStart: function() {
        this.parent();
        this.detachClickEvents();
    },

    /**
     * Sets the number of elements to show.
     * 
     * @param maxCount The maximal elements to show.
     * @returns nothing
     */
    setMaxElementCount: function(maxCount) {
        this.setFilterParameter('maxCount', maxCount);
        this.refresh();
    }

});
