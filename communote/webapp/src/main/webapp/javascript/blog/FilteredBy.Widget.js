var FilteredByWidget = new Class({
    Extends: C_FilterWidget,
    Implements: FilterParamsHandlerStrategyByName,

    widgetGroup: 'blog',

    filterStore: null,

    /**
     * whether the end date should work as an inclusive filter, that is also notes created on the end
     * date should be retrieved. It's true by default. If another behavior is required define an appropriate
     * static parameter.
     */
    isEndDateInclusive: true,

    users: [],

    observedFilterParams: [ 'filter', 'tagIds', 'userId', 'startDate', 'endDate',
            'blogId', 'propertyFilter', 'tagPrefix', 'searchString', 'postTextSearchString',
            'noteId', 'discussionId', 'pattern', 'targetBlogId', 'showPostsForMe', 'showFavorites',
            'showFollowedItems' ],

    init: function() {
        this.parent();
        this.copyStaticParameter('showFABs');
    },

    refreshComplete: function(responseMetadata) {
        init_tips(this.domNode);
        searchAndShowRoarNotification(this.domNode);
        this.noteIdChanged();
        this.resetFiltersWithoutNoteId();
    },

    resetFiltersWithoutNoteId: function() {
        this.tagIdsChanged();
        this.postTextSearchStringChanged();
        this.searchStringChanged();
        this.patternChanged();
        this.showPostsForMeChanged();
        this.showFavoritesChanged();
        this.showFollowedItemsChanged();
        this.filterChanged();
    },

    startDateChanged: function() {
        this.changeDate(this.filterParamStore);
    },

    endDateChanged: function() {
        this.changeDate(this.filterParamStore);
    },

    discussionIdChanged: function() {
        this.dataChanged('cono-filter-settings-discussionId', this.filterWidgetGroup
                .getParameterStore().getFilterParameter('discussionId'), 'onResetDiscussionId');
    },

    filterChanged: function() {
        this.dataChanged("cono-filter-settings-filter", this.filterParamStore.getFilterParameter(
                'filter', true), 'onTagClick');
    },

    tagIdsChanged: function() {
        var dataStore = this.widgetController.getDataStore();
        var tagIds = this.filterParamStore.getFilterParameter('tagIds', true);
        var names = [];
        var oldToNewMapping = {};
        if (tagIds != undefined) {
            tagIds.each(function(item, index, array) {
                var name = dataStore.get('tag', item).title;
                names.push(name);
                oldToNewMapping[name] = item;
            });
        }
        this.dataChanged("cono-filter-settings-tagIds", names, 'onTagIdClick', oldToNewMapping);
    },

    noteIdChanged: function() {
        var noteId = this.filterParamStore.getFilterParameter('noteId');
        if (noteId) {
            noteId = widgetController.getDataStore().get('note', noteId).title;
        }
        this.dataChanged('cono-filter-settings-noteId', noteId, 'onResetNoteId');
        this.resetFiltersWithoutNoteId();
    },

    postTextSearchStringChanged: function() {
        this.dataChanged('cono-filter-settings-search-postTextSearchString', this.filterWidgetGroup
                .getParameterStore().getFilterParameter('postTextSearchString'), 'onSetTextFilter');
    },

    searchStringChanged: function() {
        this.dataChanged('cono-filter-settings-search-searchString', this.filterWidgetGroup
                .getParameterStore().getFilterParameter('searchString'), 'onSetTextFilter');
    },

    tagPrefixChanged: function() {
        this.dataChanged('cono-filter-settings-search-tagPrefix', this.filterWidgetGroup
                .getParameterStore().getFilterParameter('tagPrefix'), 'onTagSearchKeyUp');
    },

    patternChanged: function() {
        this.dataChanged('cono-filter-settings-search-blogTextSearchString', this.filterWidgetGroup
                .getParameterStore().getFilterParameter('pattern'), 'onSetTextFilter');
    },

    showPostsForMeChanged: function() {
        this.dataChanged('cono-filter-settings-showPostsForMe', this.filterWidgetGroup
                .getParameterStore().getFilterParameter('showPostsForMe'), 'onShowPostsForMe');
    },

    showFavoritesChanged: function() {
        this.dataChanged('cono-filter-settings-showFavorites', this.filterWidgetGroup
                .getParameterStore().getFilterParameter('showFavorites'), 'onShowFavorites');
    },

    showFollowedItemsChanged: function() {
        this
                .dataChanged('cono-filter-settings-showFollowedItems', this.filterWidgetGroup
                        .getParameterStore().getFilterParameter('showFollowedItems'),
                        'onShowFollowedItems');
    },

    handleChangedParamsDefaultAction: function(changedParam) {
        this.blogIdChanged();
        this.changeDate(this.filterParamStore);
        this.discussionIdChanged();
        this.filterChanged();
        this.noteIdChanged();
        this.postTextSearchStringChanged();
        this.propertyFilterChanged();
        this.searchStringChanged();
        this.tagPrefixChanged();
        this.patternChanged();
        this.userIdChanged();
        this.tagIdsChanged();
        this.showPostsForMeChanged();
        this.showFavoritesChanged();
        this.showFollowedItemsChanged();
    },

    /**
     * Invoked when a property filter changes.
     */
    propertyFilterChanged: function() {
        var value = this.filterParamStore.getFilterParameter('propertyFilter', true);
        var newValue = [];
        var oldToNewMapping = {};
        if (value != null) {
            if (typeOf(value) == 'string') {
                value = [ value ];
            }
            value.each(function(item, index, array) {
                var decodedItem = JSON.decode(item);
                newValue[index] = new Element('a', {
                    'class': 'content-type-' + decodedItem.type,
                    'html': decodedItem.name
                });
                oldToNewMapping[newValue[index]] = decodedItem;
            });
        }
        this.dataChanged('cono-filter-settings-types', newValue, 'onPropertyFilterChanged',
                oldToNewMapping);
    },

    userIdChanged: function() {
        var value;
        if (this.getStaticParameter('ignoreUserIdChanges') == 'true') {
            return;
        }
        value = this.filterParamStore.getFilterParameter('userId', true);
        var newArray = [];
        var oldToNewMapping = {};
        if (value != null) {
            if (typeOf(value) != 'array') {
                value = [ value ];
            }
            value.each(function(item, index, array) {
                newArray[index] = this.widgetController.getDataStore().get('user', item).shortName;
                oldToNewMapping[newArray[index]] = item;
            });
        }
        this.dataChanged('cono-filter-settings-userId', newArray, 'onUserToggled', oldToNewMapping);
    },

    changeDate: function(filterStore) {
        var startDate = filterStore.getFilterParameter('startDate');
        var endDate = filterStore.getFilterParameter('endDate');
        var filterElement = this.domNode.getElements(".cono-filter-settings-startDate-endDate");
        var dataElement = filterElement.getElement(".cn-icon-label");

        dataElement.setProperty('text', '');
        if (!startDate && !endDate) {
            filterElement.setStyle("display", "none");
            this.checkVisibility();
            return;
        }

        var formatPattern = getJSMessage('blog.filter.summary.period.dateformat');

        if (startDate != null) {
            var startDateElement = new Element('span', {
                text: localizedDateFormatter.format(new Date(startDate).increment('ms',
                        communote.currentUser.timeZoneOffset), formatPattern)
            });
            startDateElement.addEvent('click', this.removeValue.bind(this, 'removeStartDate', null,
                    null));
            if (endDate == null) {
                dataElement.appendText(getJSMessage('blog.filter.summary.period.start') + ' ');
                dataElement.grab(startDateElement);
            } else {
                dataElement.grab(startDateElement);
                dataElement.appendText(' - ');
            }
        }
        if (endDate != null) {
            if (this.isEndDateInclusive) {
                // decrement end day by one day to correctly visualize the inclusive filter
                endDate -= 86400000;
            }
            var endDateElement = new Element('span', {
                text: localizedDateFormatter.format(new Date(endDate).increment('ms',
                        communote.currentUser.timeZoneOffset), formatPattern)
            });
            endDateElement.addEvent('click', this.removeValue.bind(this, 'removeEndDate', null,
                    null));
            if (startDate == null) {
                dataElement.appendText(getJSMessage('blog.filter.summary.period.end') + ' ');
            }
            dataElement.grab(endDateElement);
        }
        filterElement.setStyle('display', 'block');
        this.checkVisibility();
    },

    /**
     * Invoked, when the user selects a workspace.
     */
    blogIdChanged: function() {
        var blogIds = this.filterParamStore.getFilterParameter('blogId');
        var newArray = [];
        var oldToNewMapping = {};
        if (blogIds != null) {
            if (typeOf(blogIds) != 'array') {
                blogIds = [ blogIds ];
            }
            blogIds.each(function(item, index, array) {
                newArray[index] = this.widgetController.getDataStore().get('blog', item).title;
                oldToNewMapping[newArray[index]] = item;
            });
        }
        this.dataChanged('cono-filter-settings-workspaces', newArray, 'onBlogToggled',
                oldToNewMapping);
    },

    /**
     * 
     * @param elementId Id of the title element (excluding "-data")
     * @param data The data, which have been toggled.
     */
    dataChanged: function(elementId, value, removeEvent, oldToNewMapping) {
        var filterElement = this.domNode.getElements("." + elementId);
        var dataElement = filterElement.getElement(".cn-icon-label");
        if (dataElement == null) {
            return;
        }
        
        if (filterElement.getProperty('data-cnt-filteredby') != 'boolean') {
            dataElement.set('html', '<!-- Empty -->');
        }
        
        if (typeOf(value) == 'number') {
            value = value.toString();
        }
        if (typeOf(value) == 'boolean') {
            if (value) {
                filterElement.setStyle('display', 'block');
                filterElement.addEvent('click', this.removeValue.bind(this, removeEvent, false));
            } else {
                filterElement.setStyle('display', 'none');
                filterElement.removeEvent('click');
            }
        } else if ((value && value.length > 0)) {
            if (typeOf(value) != 'array') {
                value = [ value ];
            }
            value.sort(function(x, y) {
                var a = String(x).toUpperCase();
                var b = String(y).toUpperCase();
                if (a > b)
                    return 1;
                if (a < b)
                    return -1;
                return 0;
            });
            var length = value.length;
            value.each(function(item, index, array) {
                var span;
                if (elementId == 'cono-filter-settings-types') {
                    filterElement.set('class', 'cono-filter-settings-types ' + item.className);
                    filterElement.set('html', item.innerHTML);
                    dataElement = filterElement.getElement('.cn-icon-label');
                    dataElement.addEvent('click', this.removeValue.bind(this, removeEvent, item,
                            oldToNewMapping));
                } else {
                    span = new Element('span', {
                        text: elementId == 'cono-filter-settings-search-tagPrefix' ? item + '*'
                                : item
                    });
                    span.addEvent('click', this.removeValue.bind(this, removeEvent, item,
                            oldToNewMapping));
                    dataElement.grab(span);
                    if (index < length - 1) {
                        dataElement.appendText(', ');
                    }
                }
            }, this);
            filterElement.setStyle('display', 'block');
        } else {
            filterElement.setStyle('display', 'none');
        }
        this.checkVisibility();
    },

    /**
     * Checks, if the widget should be visible or not.
     */
    checkVisibility: function() {
        var i;
        var elements = this.domNode.getElement('.cn-content').getElements('a');
        for (i = 0; i < elements.length; i++) {
            if (elements[i].getStyle('display') != 'none') {
                this.widgetController.unmarkAsEmpty(this);
                return;
            }
        }
        this.widgetController.markAsEmpty(this);
    },

    removeValue: function(event, eventData, oldToNewMapping) {
        switch (event) {
        case 'onTagIdClick':
            eventData = [ oldToNewMapping[eventData], undefined, true ];
            break;
        case 'onTagClick':
            eventData = [ eventData, true ];
            break;
        case 'onBlogToggled':
        case 'onUserToggled':
            eventData = oldToNewMapping[eventData];
            break;
        case 'onPropertyFilterChanged':
            // TODO it is not responsibility of this widget to modify another widget!
            $$('div.cn-filter-content a.cn-entry[name=' + eventData.name.toLowerCase() + ']')
                    .removeClass('cono-selected');
            eventData = {
                property: oldToNewMapping[eventData],
                selected: false
            };
            break;
        case 'removeStartDate':
            event = 'onSetPeriodFilter';
            eventData = {
                start: null
            };
            break;
        case 'removeEndDate':
            event = 'onSetPeriodFilter';
            eventData = {
                end: null
            };
            break;
        case 'onTagSearchKeyUp':
        case 'onSetUserSearch':
        case 'onSetTextFilter':
        case 'onSetBlogSearch':
            eventData = '';
            break;
        case 'onResetDiscussionId':
            event = 'onReset';
            eventData = [ 'discussionId' ];
            break;
        case 'onResetNoteId':
            event = 'onReset';
            eventData = [ 'noteId' ];
            break;
        }
        this.sendFilterGroupEvent(event, eventData);
    }
});
