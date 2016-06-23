var ContentTypeWidget = new Class({
    Extends: C_FilterWidget,
    Implements: FilterParamsHandlerStrategyByName,

    widgetGroup: "type",

    observedFilterParams: [ 'propertyFilter', 'showPostsForMe', 'showFavorites',
            'showFollowedItems' ],

    contentTypes: {},

    init: function() {
        this.parent();
        this.copyStaticParameter('categories');
        this.copyStaticParameter('showFAB');
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
            this.domNode.getElements('.cn-types-wrapper a.cn-entry').removeEvents('click');
            this.domNode.getElements('a.cn-entry[data-cnt-filter]').removeEvents('click');
        }
    },

    // TODO really necessary to rfresh??
    handleChangedParamsDefaultAction: function(changedParam) {
        this.refresh();
    },

    refreshComplete: function(responseMetadata) {
        var filter, i, fabFilters;
        this.parent(responseMetadata);
        var contentTypeElements = this.domNode.getElements('.cn-types-wrapper a.cn-entry');
        for (i = 0; i < contentTypeElements.length; i++) {
            contentTypeElements[i].addEvent('click', this.onContentTypeClick.pass(
                    contentTypeElements[i], this));
        }
        fabFilters = this.domNode.getElements('a.cn-entry[data-cnt-filter]');
        for (i = 0; i < fabFilters.length; i++) {
            filter = JSON.decode(fabFilters[i].get('data-cnt-filter').replace(/'/g, "\""));
            fabFilters[i].addEvent('click', this.onFabFilterClick.pass([ fabFilters[i], filter ],
                    this));
        }
        this.propertyFilterChanged();
    },
    
    /**
     * @override
     */
    refreshStart: function() {
        this.parent();
        this.detachClickEvents();
    },

    onFabFilterClick: function(element, filter) {
        this.sendFilterGroupEvent(filter['filterKey'], !element.hasClass('cn-active'));
    },

    showPostsForMeChanged: function(event) {
        this.shohFABChanged('a.cn-mention', 'showPostsForMe');
    },

    showFavoritesChanged: function(event) {
        this.shohFABChanged('a.cn-favorite', 'showFavorites');
    },
    showFollowedItemsChanged: function(event) {
        this.shohFABChanged('a.cn-follow', 'showFollowedItems');
    },

    shohFABChanged: function(selector, filterName) {
        var filterSet = this.filterParamStore.getFilterParameter(filterName);
        if (filterSet) {
            this.domNode.getElements(selector).addClass('cn-active');
        } else {
            this.domNode.getElements(selector).removeClass('cn-active');
        }
    },

    onContentTypeClick: function(element) {
        this.sendFilterGroupEvent("onPropertyFilterChanged", {
            property: {
                'name': element.get('html'),
                'value': element.getProperty('data-property-filter'),
                'type': element.getProperty('name')
            },
            selected: !element.hasClass("cn-active")
        });
    },

    propertyFilterChanged: function() {
        var value = this.filterParamStore.getFilterParameter('propertyFilter');
        this.domNode.getElements('.cn-types-wrapper a.cn-entry').removeClass('cn-active');
        if (value != null) {
            if (typeOf(value) == 'string') {
                value = [ value ];
            }
            value.each(function(item, index, array) {
                var decodedItem = JSON.decode(item);
                this.domNode.getElements(
                        '.cn-types-wrapper a.cn-entry[name=' + decodedItem.type + ']').addClass(
                        'cn-active');
            }.bind(this));
        }
    }
});
