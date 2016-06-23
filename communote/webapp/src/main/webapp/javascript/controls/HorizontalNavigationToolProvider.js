(function(window) {
    var communote = window.communote;
    // create a little tool that adds the menu and viewtype switches to the HorizontalNavigation
    // Widget for the different views. In cases where the widget framework isn't required or the
    // mainpage context manager isn't set nothing will be done. 
    communote.initializer
            .addBeforeWidgetScanCallback(function() {
                var toolProvider, exportLinksUpdateNotifier;
                if (!communote.contextManager) {
                    return;
                }
                toolProvider = {
                    horizontalNavigationWidgetId: false,
                    contextManager: communote.contextManager,
                    knownViews: [],
                    cplMenuToolIds: {},
                    exportLinksUpdateNotifiers: {},

                    /**
                     * Implementation of a method defined by the widget event listener interface.
                     */
                    getWidgetListenerGroupId: function() {
                        return undefined;
                    },
                    /**
                     * Implementation of a method defined by the widget event listener interface.
                     */
                    handleEvent: function(eventName, params) {
                        var viewId, contextId, filterParamStore;
                        // only interested in the refreshcomplete of the MainPageHorizontalNavigation widgets
                        if (eventName == 'onWidgetRefreshComplete') {
                            if (params.widgetType == 'MainPageHorizontalNavigationWidget') {
                                if (!this.horizontalNavigationWidgetId) {
                                    this.horizontalNavigationWidgetId = params.widgetId;
                                } else if (this.horizontalNavigationWidgetId != params.widgetId) {
                                    return;
                                }
                                // reset views that were already activated for current context
                                this.knownViews = [ this.getViewId() ];
                                this.cplMenuToolIds = {};
                                this.addToolsToNavigation();
                            }
                        } else if (eventName == 'onInlineDiscussionToggled') {
                            viewId = this.getViewId();
                            contextId = this.getContextId();
                            filterParamStore = this.contextManager.getFilterParameterStoreForContext(contextId, viewId);
                            // when an inline discussion is shown and the CPL is filtered by a single note
                            // update the export links to return that discussion
                            if (filterParamStore.getFilterParameter('noteId')) {
                                if (params.show) {
                                    this.updateExportLinks(contextId, viewId, "discussionId="
                                            + params.discussionId);
                                } else {
                                    this.updateExportLinks(contextId, viewId);
                                }
                            }
                        }
                    },

                    getObservedFilterParameters: function() {
                        return [ 'viewId' ];
                    },

                    filterParametersChanged: function(changedParams) {
                        var viewId;
                        if (changedParams.contains('viewId')) {
                            // ignore viewId changes that go along with contextId changes since the naviWidget will refresh
                            if (!changedParams.contains('contextId')) {
                                viewId = this.getViewId();
                                if (!this.knownViews.contains(viewId)) {
                                    this.knownViews.push(viewId);
                                    this.addToolsToNavigation();
                                }
                            }
                        }
                    },

                    getContextId: function() {
                        return this.contextManager.getCurrentContextId();
                    },
                    getViewId: function() {
                        return this.contextManager.getCurrentViewId();
                    },

                    handleSwitchViewType: function(elem, clickedState) {
                        var widget = communote.widgetController.getWidget(clickedState.widgetId);
                        if (widget) {
                            if (this.getContextId() == 'topicsOverview') {
                                widget.sendFilterGroupEvent('onChangeViewType', {
                                    preferenceId: 'topicsOverview_all',
                                    viewType: clickedState.id
                                });
                            } else {
                                widget.changeViewType(clickedState.id);
                            }
                        }
                    },

                    getExportLink: function(rss, contextId, viewId, additionalParams) {
                        var filterParamStore, queryString, link;
                        filterParamStore = this.contextManager.getFilterParameterStoreForContext(contextId, viewId);
                        queryString = filterParamStore.createQueryString();
                        if (rss) {
                            link = buildRequestUrl('/rss/get.do') + '?format=rss';
                        } else {
                            link = buildRequestUrl('/topic/export.do') + '?format=rtf';
                        }
                        link += '&maxCount=100';
                        // TODO for paging offset of CPL would have to be included
                        if (queryString) {
                            link += '&' + queryString;
                        }
                        if (additionalParams) {
                            link += '&' + additionalParams;
                        }
                        return link;
                    },
                    updateExportLinks: function(contextId, viewId, additionalParams) {
                        var toolId = this.cplMenuToolIds[viewId];
                        var navWidget = widgetController
                                .getWidget(this.horizontalNavigationWidgetId);
                        navWidget.updateMenuItem(viewId, toolId, 1, {
                            type: 'action',
                            url: this.getExportLink(true, contextId, viewId, additionalParams)
                        });
                        navWidget.updateMenuItem(viewId, toolId, 2, {
                            type: 'action',
                            url: this.getExportLink(false, contextId, viewId, additionalParams)
                        });
                    },
                    getViewTypeSwitch: function(type, widgetId, msgKeyPrefix, cssClassPrefix) {
                        return {
                            id: type,
                            widgetId: widgetId,
                            title: getJSMessage(msgKeyPrefix + type.toLowerCase()),
                            cssClass: cssClassPrefix + type,
                            label: false,
                            action: this.handleSwitchViewType.bind(this)
                        };
                    },

                    addToolsToNavigation: function() {
                        var contextId = this.getContextId();
                        var viewId = this.getViewId();
                        var options = communote.configuration;
                        if (options && options.horizontalNavigationTools) {
                            options = options.horizontalNavigationTools;
                        } else {
                            options = {};
                        }
                        // convention: CPL is expected in all views of notesOverview or in views labeled 'notes'
                        if (contextId == 'notesOverview' || viewId == 'notes') {
                            this.addChronologicalPostListTools(contextId, viewId, options);
                        } else if (contextId == 'topicsOverview' && viewId == 'all') {
                            this.addTopicListTools(contextId, viewId, options);
                        }
                    },
                    addChronologicalPostListTools: function(contextId, viewId, options) {
                        var menuItems, notifier;
                        var uniqueViewId = contextId + '_' + viewId;
                        var widgetController = communote.widgetController;
                        var cplWidget = widgetController.getWidget('ChronologicalPostList_'
                                + uniqueViewId);
                        var navWidget = widgetController
                                .getWidget(this.horizontalNavigationWidgetId);
                        if (cplWidget && navWidget) {
                            if (options.viewTypeSwitch !== false) {
                                navWidget.addToolToView(viewId, {
                                    type: 'toggle',
                                    role: 'viewtypeSwitch',
                                    cssClass: 'viewtype-switch',
                                    toggledOn: this.getViewTypeSwitch('CLASSIC', cplWidget.widgetId,
                                            'blog.post.list.head.view.type.', ''),
                                    toggledOff: this.getViewTypeSwitch('COMMENT', cplWidget.widgetId,
                                            'blog.post.list.head.view.type.', ''),
                                    isOn: cplWidget.getCurrentViewType() == 'COMMENT'
                                });
                            }
                            menuItems = [];
                            menuItems.push({
                                type: 'action',
                                role: 'rssExport',
                                url: this.getExportLink(true, contextId, viewId),
                                title: getJSMessage('blog.post.list.export.rss'),
                                label: getJSMessage('blog.post.list.export.rss.menu'),
                                cssClass: 'cn-link action-rss',
                                newWindow: true
                            });
                            menuItems.push({
                                type: 'action',
                                role: 'rtfExport',
                                url: this.getExportLink(false, contextId, viewId),
                                title: getJSMessage('blog.post.list.export.rtf'),
                                label: getJSMessage('blog.post.list.export.rtf.menu'),
                                cssClass: 'cn-link cn-last-child action-word'
                            });
                            this.cplMenuToolIds[viewId] = navWidget.addToolToView(viewId, {
                                type: 'menu',
                                role: 'moreOptions',
                                cssClass: 'cn-more-actions',
                                items: menuItems
                            });
                            // need individual update notifiers because they need to know the filterGroup
                            // they belong to to be able to query the correct store esp. if a filter
                            // parameter changed and the current view is not the one of the filterGroup
                            // (e.g. userSelected_profile and userId changed)
                            if (!this.exportLinksUpdateNotifiers[contextId + '_' + viewId]) {
                                notifier = Object.clone(exportLinksUpdateNotifier);
                                notifier.viewId = viewId;
                                notifier.contextId = contextId;
                                filterWidgetGroupRepo['filterGroup_' + contextId + '_' + viewId]
                                    .addMember(notifier);
                                // save instance to avoid adding it several times to the filter group
                                this.exportLinksUpdateNotifiers[contextId + '_' + viewId] = notifier;
                            }
                        }
                    },
                    addTopicListTools: function(contextId, viewId, options) {
                        var widgetController = communote.widgetController;
                        var topicListWidget = widgetController.getWidget('TopicList_' + contextId
                                + '_' + viewId);
                        var navWidget = widgetController
                                .getWidget(this.horizontalNavigationWidgetId);
                        if (topicListWidget && navWidget && options.viewTypeSwitch !== false) {
                            navWidget.addToolToView(viewId, {
                                type: 'toggle',
                                role: 'viewtypeSwitch',
                                cssClass: 'viewtype-switch',
                                toggledOn: this.getViewTypeSwitch('CLASSIC',
                                        topicListWidget.widgetId, 'blog.overview.view-type.',
                                        'TOPIC-'),
                                toggledOff: this.getViewTypeSwitch('HIERARCHY',
                                        topicListWidget.widgetId, 'blog.overview.view-type.',
                                        'TOPIC-'),
                                isOn: topicListWidget.filterParamStore
                                        .getFilterParameter('showOnlyRootTopics')
                            });
                        }
                    }
                };
                exportLinksUpdateNotifier = {
                    viewId: undefined,
                    contextId: undefined,
                    getObservedFilterParameters: function() {
                        // observe all changes
                        return [ '*' ];
                    },

                    filterParametersChanged: function(changedParams) {
                        toolProvider.updateExportLinks(this.contextId, this.viewId);
                    }
                };

                communote.widgetController.registerWidgetEventListener('onWidgetRefreshComplete',
                        toolProvider);
                communote.widgetController.registerWidgetEventListener('onInlineDiscussionToggled',
                        toolProvider);
                communote.contextManager.getFilterGroup().addMember(toolProvider);
            });
})(this);