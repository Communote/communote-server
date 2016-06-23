communote.configuration.factory = {
    mediumWidthFilterVisibilityCondition: {
        attributes: [ {
            name: 'viewportWidth',
            value: 'medium'
        }, {
            name: 'filterShown',
            value: true
        } ]
    },
    smallWidthFilterVisibilityCondition: {
        attributes: [ {
            name: 'viewportWidth',
            value: [ 'tiny', 'small' ]
        }, {
            name: 'filterShown',
            value: true
        } ]
    },
    fullWidthFilterVisibilityCondition: {
        name: 'viewportWidth',
        value: 'full'
    },
    // default render options for the different note views. Can be overridden by plugins.
    defaultNoteViewRenderOptions: {
        notesOverview: {
            includeAuthorFilter: true,
            includeTopicFilter: false,
            includeTagCloud: true,
            includeContentTypeFilter: true,
            includeSearchFilter: true,
            includeDateFilter: true,
            includeFilterSummary: true,
            contentTypeFilterShowFAB: false
        },
        tagSelected: {
            includeAuthorFilter: true,
            includeTopicFilter: false,
            includeTagCloud: true,
            includeContentTypeFilter: true,
            includeSearchFilter: true,
            includeDateFilter: true,
            includeFilterSummary: true,
            contentTypeFilterShowFAB: true
        },
        userSelected: {
            includeAuthorFilter: false,
            includeTopicFilter: true,
            includeTagCloud: true,
            includeContentTypeFilter: true,
            includeSearchFilter: true,
            includeDateFilter: true,
            includeFilterSummary: true,
            contentTypeFilterShowFAB: true
        },
        topicSelected: {
            includeAuthorFilter: true,
            includeTopicFilter: true,
            includeTagCloud: true,
            includeContentTypeFilter: true,
            includeSearchFilter: true,
            includeDateFilter: true,
            includeFilterSummary: true,
            contentTypeFilterShowFAB: true
        }
    },

    createCPLWidgetDef: function(selector, filterWidgetGroupId, options) {
        return {
            widgetType: 'ChronologicalPostListDefaultWidget',
            containerSelector: selector,
            settings: {
                filterWidgetGroupId: filterWidgetGroupId,
                renderWidgetType: 'ChronologicalPostListWidget',
                createNoteWidgetClass: 'CreateNoteDefaultWidget',
                preferenceId: options.cplPreferenceId,
                predefinedViewType: options.cplPredefinedViewType,
                createNoteWidgetStaticParams: {
                    renderWidgetType: 'CreateNoteWidget',
                    tagAutocompleterCategories: options.createNoteTagAutocompleterCategories
                },
                draftTimer: 10,
                showFooter: true,
                showHeader: true,
                maxCount: 15,
                loadMoreMode: options.cplLoadMoreMode || 'append-scroll',
                loadMoreAppendLimit: 10
            }
        };
    },
    createFilteredByWidgetDef: function(selector, filterWidgetGroupId, showFAB) {
        return {
            widgetType: 'FilteredByWidget',
            containerSelector: selector,
            settings: {
                filterWidgetGroupId: filterWidgetGroupId,
                showFABs: showFAB
            }
        };
    },

    createShowBannerWidgetDef: function(selector, tinyViewportWidthSelector, filterWidgetGroupId,
            title) {
        var widgetDef = {
            widgetType: 'GlobalIdShowBannerImageWidget',
            containerSelector: selector,
            settings: {
                filterWidgetGroupId: filterWidgetGroupId
            }
        };
        if (tinyViewportWidthSelector) {
            widgetDef.conditionalContainerSelectors = {
                type: 'responsive',
                selector: tinyViewportWidthSelector,
                condition: {
                    name: 'viewportWidth',
                    value: 'tiny'
                }
            };
        }
        if (title) {
            widgetDef.settings.title = title;
        }
        return widgetDef;
    },

    createSubViewViewDef: function(parentViewId, visibleWidgets, conditionalWidgetsFullWidth,
            conditionalWidgetsMediumWidth, conditionalWidgetsSmallWidth) {
        var conditionalVisibleWidgets;
        var viewDef = {};
        viewDef.parentViewId = parentViewId;
        viewDef.previousViewAction = 'hide';
        viewDef.visibleWidgets = visibleWidgets;
        conditionalVisibleWidgets = [];
        if (conditionalWidgetsFullWidth) {
            conditionalVisibleWidgets.push({
                type: 'responsive',
                condition: this.fullWidthFilterVisibilityCondition,
                visibleWidgets: conditionalWidgetsFullWidth
            });
        }
        if (conditionalWidgetsMediumWidth && conditionalWidgetsMediumWidth.length) {
            conditionalVisibleWidgets.push({
                type: 'responsive',
                condition: this.mediumWidthFilterVisibilityCondition,
                visibleWidgets: conditionalWidgetsMediumWidth
            });
        }
        if (conditionalWidgetsSmallWidth && conditionalWidgetsSmallWidth.length) {
            conditionalVisibleWidgets.push({
                type: 'responsive',
                condition: this.smallWidthFilterVisibilityCondition,
                visibleWidgets: conditionalWidgetsSmallWidth
            });
        }
        if (conditionalVisibleWidgets.length) {
            viewDef.conditionalVisibleWidgets = conditionalVisibleWidgets;
        }
        return viewDef;
    },

    prepareCommonNoteViewDef: function(viewDefs, subViewName, parentViewId, options) {
        var conditionalFilterWidgets = [];
        var minimalConditionalFilterWidgets = [];
        if (options.includeSearchFilter) {
            conditionalFilterWidgets.push('SearchBox_' + subViewName);
            minimalConditionalFilterWidgets.push('SearchBox_' + subViewName);
        }
        if (options.includeFilterSummary) {
            conditionalFilterWidgets.push('FilteredBy_' + subViewName);
            minimalConditionalFilterWidgets.push('FilteredBy_' + subViewName);
        }
        if (options.includeTopicFilter) {
            conditionalFilterWidgets.push('TopicFilter_' + subViewName);
        }
        if (options.includeTagCloud) {
            conditionalFilterWidgets.push('TagCloud_' + subViewName);
        }
        if (options.includeAuthorFilter) {
            conditionalFilterWidgets.push('AuthorFilter_' + subViewName);
        }
        if (options.includeContentTypeFilter) {
            conditionalFilterWidgets.push('ContentType_' + subViewName);
        }
        if (options.includeDateFilter) {
            conditionalFilterWidgets.push('DateFilter_' + subViewName);
        }
        viewDefs[subViewName] = this.createSubViewViewDef(parentViewId, [ 'ChronologicalPostList_'
                + subViewName ], conditionalFilterWidgets, conditionalFilterWidgets,
                minimalConditionalFilterWidgets);
        return viewDefs;
    },
    
    prepareCommonNoteViewRenderOptions: function(parentViewId) {
        var key;
        var defaultOptions = this.defaultNoteViewRenderOptions[parentViewId];
        var renderOptions = {};
        if (defaultOptions) {
            // expecting flat options and simple object
            for (key in defaultOptions) {
                renderOptions[key] = defaultOptions[key];
            }
        }
        return renderOptions;
    },

    prepareCommonNoteWidgetDefs: function(widgetDefs, subViewName, cplWidgetSelector,
            filterWidgetsSelector, options) {
        if (!options.filterWidgetGroupId) {
            options.filterWidgetGroupId = 'filterGroup_' + subViewName;
        }
        widgetDefs['ChronologicalPostList_' + subViewName] = this.createCPLWidgetDef(
                cplWidgetSelector, options.filterWidgetGroupId, options);
        this.prepareNoteFilterWidgetDefs(widgetDefs, subViewName, filterWidgetsSelector, options);
        return widgetDefs;
    },

    prepareCommonTopicWidgetDefs: function(widgetDefs, subViewName, topicListWidgetSelector,
            filterWidgetsSelector, options) {
        var filterWidgetGroupId = options.filterWidgetGroupId || 'filterGroup_' + subViewName;
        widgetDefs['TopicList_' + subViewName] = {
            widgetType: 'TopicListWidget',
            containerSelector: topicListWidgetSelector,
            settings: {
                filterWidgetGroupId: filterWidgetGroupId,
                maxCount: 35,
                loadMoreMode: 'append-scroll',
                loadMoreAppendLimit: 10,
                showNew: options.topicListShowNew,
                showAdd: options.topicListShowAdd
            }
        };
        widgetDefs['SearchBox_' + subViewName] = {
            widgetType: 'SearchBoxWidget',
            containerSelector: filterWidgetsSelector,
            settings: {
                filterWidgetGroupId: filterWidgetGroupId,
                renderSearchModeSwitches: false,
                searchModes: 'mixed',
                searchModeOptions: {
                    mixed: {
                        submitEvent: 'onSetTextFilter',
                        acSelectEvent: 'onEntitySuggestionSelected',
                        acFactoryFunction: 'createEntityAutocompleter',
                        acOptions: {
                            suggestionsOptions: {
                                width: 'auto-min'
                            },
                            userSuggestionDisabled: true,
                            topicSuggestionDisabled: true,
                            tagCategories: options.searchBoxFilterTagCategories,
                            tagSuggestionType: 'BLOG'
                        }
                    }
                }
            }
        };
        if (subViewName == 'topicsOverview_all') {
            widgetDefs['SearchBox_topicsOverview_all'].settings.observedFilterParameters = [ '-*',
                    'tagIds', 'viewType' ];
        }
        widgetDefs['FilteredBy_' + subViewName] = this.createFilteredByWidgetDef(
                filterWidgetsSelector, filterWidgetGroupId, options.contentTypeFilterShowFAB);
        widgetDefs['TagCloud_' + subViewName] = {
            widgetType: 'BlogTagCloudWidget',
            containerSelector: filterWidgetsSelector,
            settings: {
                filterWidgetGroupId: filterWidgetGroupId,
                maxCount: 50
            }
        };
        return widgetDefs;
    },
    prepareCommonUserWidgetDefs: function(widgetDefs, subViewName, userListWidgetSelector,
            filterWidgetsSelector, options) {
        var filterWidgetGroupId = options.filterWidgetGroupId || 'filterGroup_' + subViewName;
        widgetDefs['UserList_' + subViewName] = {
            widgetType: 'UserListWidget',
            containerSelector: userListWidgetSelector,
            settings: {
                filterWidgetGroupId: filterWidgetGroupId,
                maxCount: 20,
                loadMoreMode: 'append-scroll',
                loadMoreAppendLimit: 10
            }
        };
        widgetDefs['SearchBox_' + subViewName] = {
            widgetType: 'SearchBoxWidget',
            containerSelector: filterWidgetsSelector,
            settings: {
                filterWidgetGroupId: filterWidgetGroupId,
                renderSearchModeSwitches: false,
                searchModes: 'mixed',
                searchModeOptions: {
                    mixed: {
                        submitEvent: 'onSetTextFilter',
                        acSelectEvent: 'onEntitySuggestionSelected',
                        acFactoryFunction: 'createEntityAutocompleter',
                        acOptions: {
                            suggestionsOptions: {
                                width: 'auto-min'
                            },
                            userSuggestionDisabled: true,
                            topicSuggestionDisabled: true,
                            tagCategories: options.searchBoxFilterTagCategories,
                            tagSuggestionType: 'ENTITY'
                        }
                    }
                }
            }
        };
        widgetDefs['FilteredBy_' + subViewName] = this.createFilteredByWidgetDef(
                filterWidgetsSelector, filterWidgetGroupId, options.contentTypeFilterShowFAB);
        widgetDefs['TagCloud_' + subViewName] = {
            widgetType: 'UserTagCloudWidget',
            containerSelector: filterWidgetsSelector,
            settings: {
                filterWidgetGroupId: filterWidgetGroupId,
                maxCount: 50
            }
        };
        return widgetDefs;
    },

    prepareNoteFilterWidgetDefs: function(widgetDefs, subViewName, selector, options) {
        var filterWidgetGroupId = options.filterWidgetGroupId || 'filterGroup_' + subViewName;
        if (options.includeSearchFilter) {
            widgetDefs['SearchBox_' + subViewName] = {
                widgetType: 'SearchBoxWidget',
                containerSelector: selector,
                settings: {
                    filterWidgetGroupId: filterWidgetGroupId,
                    renderSearchModeSwitches: false,
                    searchModes: 'mixed',
                    searchModeOptions: {
                        mixed: {
                            submitEvent: 'onSetTextFilter',
                            acSelectEvent: 'onEntitySuggestionSelected',
                            acFactoryFunction: 'createEntityAutocompleter',
                            acOptions: {
                                suggestionsOptions: {
                                    width: 'auto-min'
                                },
                                userSuggestionDisabled: !options.includeAuthorFilter,
                                userSuggestionType: 'AUTHOR',
                                topicSuggestionDisabled: !options.includeTopicFilter,
                                topicSuggestionTimeline: true,
                                tagSuggestionDisabled: !options.includeTagCloud,
                                tagCategories: options.searchBoxFilterSearchCategories
                            },
                            tagSuggestionType: 'NOTE'
                        }
                    }
                }
            };
        }
        if (options.includeFilterSummary) {
            widgetDefs['FilteredBy_' + subViewName] = this.createFilteredByWidgetDef(selector,
                    filterWidgetGroupId, options.contentTypeFilterShowFAB);
        }
        if (options.includeTopicFilter) {
            widgetDefs['TopicFilter_' + subViewName] = {
                widgetType: 'BlogListWidget',
                containerSelector: selector,
                settings: {
                    filterWidgetGroupId: filterWidgetGroupId,
                    markEmptyIfNoContent: true,
                    maxCount: 10
                }
            };
        }
        if (options.includeTagCloud) {
            widgetDefs['TagCloud_' + subViewName] = {
                widgetType: 'TagCloudWidget',
                containerSelector: selector,
                settings: {
                    filterWidgetGroupId: filterWidgetGroupId,
                    showTitle: false,
                    showHistory: false,
                    listenBlog: true,
                    numberOfLastDays: 0,
                    maxCount: 50
                }
            };
        }
        if (options.includeAuthorFilter) {
            widgetDefs['AuthorFilter_' + subViewName] = {
                widgetType: 'AuthorFilterWidget',
                containerSelector: selector,
                settings: {
                    filterWidgetGroupId: filterWidgetGroupId,
                    maxCount: 24
                }
            };
        }
        if (options.includeContentTypeFilter) {
            widgetDefs['ContentType_' + subViewName] = {
                widgetType: 'ContentTypeWidget',
                containerSelector: selector,
                settings: {
                    filterWidgetGroupId: filterWidgetGroupId,
                    categories: 'notes',
                    showFAB: options.contentTypeFilterShowFAB
                }
            };
        }
        if (options.includeDateFilter) {
            widgetDefs['DateFilter_' + subViewName] = {
                    widgetType: 'BlogPostFilterPeriodWidget',
                    containerSelector: selector,
                    settings: {
                        filterWidgetGroupId: filterWidgetGroupId
                    }
            };
        }
        return widgetDefs;
    }

};
