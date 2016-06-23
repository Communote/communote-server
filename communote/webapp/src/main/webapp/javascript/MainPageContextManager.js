(function(namespace) {
    var MainPageContextManager = new Class({
        Extends: C_FilterEventHandler,

        options: {
            // whether to activate the context with ID defaultContextId when an activateContext event does not contain
            activateDefaultContext: true,
            defaultContextId: false,
            // override defaults of built in contexts
            defaultViews: {}
        },

        // holds the contexts and their subviews as an object that maps the context ID to an 
        // array of view IDs, the array can be empty if the context has no sub views
        contextDefs: null,
        viewManager: null,

        initialize: function(filterGroup, contextDefs, viewManager, options) {
            this.parent(filterGroup, options);
            this.viewManager = viewManager;
            if (contextDefs) {
                // TODO check if valid?
                this.contextDefs = contextDefs;
            } else {
                this.contextDefs = {};
            }
        },

        getHandledEvents: function() {
            // not supporting the onReset or onReplace of the parent
            return [ 'activateContext', 'activateView', 'entitySelected', 'restoreState' ];
        },

        activateContext: function(contextDescriptor) {
            var views, currentContextId, currentViewId, contextChanged, changedParams, viewId;
            var viewChanged, handler, uniqueViewId;
            var contextId = contextDescriptor && contextDescriptor.contextId;
            if (!contextId && this.options.activateDefaultContext) {
                contextId = this.options.defaultContextId;
            }
            // ignore events when contextId is missing or contextId is not known
            if (!contextId || !this.contextDefs[contextId]) {
                return;
            }
            // check for the view to activate. Might be null if the context has no special sub contexts (aka views)
            viewId = contextDescriptor.viewId || this.options.defaultViews[contextId];
            views = this.contextDefs[contextId];

            if (viewId) {
                // check the viewId is known, otherwise ignore
                if (!views || !views.contains(viewId)) {
                    viewId = false;
                }
            }
            if (!viewId && views) {
                // take first view as default, if available
                viewId = views[0];
            }
            changedParams = [];
            currentContextId = this.getCurrentContextId();
            currentViewId = this.getCurrentViewId();
            contextChanged = this.filterParameterStore.setUnresetableFilterParameter('contextId',
                    contextId);
            // add contextId parameter to changed params if context changed
            if (contextChanged) {
                changedParams.push('contextId');
            } else if (contextDescriptor.keepViewId) {
                // keep the view if the context did not change
                viewId = currentViewId;
            }
            if (viewId) {
                viewChanged = this.filterParameterStore.setUnresetableFilterParameter('viewId',
                        viewId);
            } else {
                viewChanged = this.filterParameterStore.removeUnresetableFilterParameter('viewId');
            }
            if (viewChanged) {
                changedParams.push('viewId');
            }
            changedParams.append(this.filterParameterStore
                    .resetCurrentFilterParameters(contextDescriptor.contextFilterParameters));
            // check if there is a handler function for the new context otherwise use generic handler
            handler = this['handleContextChange_' + contextId];
            if (typeOf(handler) != 'function') {
                handler = this.handleContextChange;
            }
            // inform ViewManager do preparations of the new view before creating or showing the
            // new widgets which leads to a better user experience (renders faster and server 
            // requests are reduced to necessary ones) 
            if (this.viewManager && (contextChanged || viewChanged)) {
                uniqueViewId = viewId ? contextId + '_' + viewId : contextId;
                this.viewManager.activateView(uniqueViewId, handler.bind(this, currentContextId,
                        contextId, currentViewId, viewId,
                        contextDescriptor.contextFilterParameters, contextDescriptor.options));
            } else {
                handler.call(this, currentContextId, contextId, currentViewId, viewId,
                        contextDescriptor.contextFilterParameters, contextDescriptor.options);
            }
            return changedParams;
        },

        activateView: function(viewId) {
            var views;
            var currentViewId = this.getCurrentViewId();
            var currentContextId = this.getCurrentContextId();
            if (currentContextId && viewId && currentViewId != viewId) {
                // ignore viewId that does not exist for the context
                views = this.contextDefs[currentContextId];
                if (views && views.contains(viewId)) {
                    this.filterParameterStore.setUnresetableFilterParameter('viewId', viewId);
                    if (this.viewManager) {
                        this.viewManager.activateView(currentContextId + '_' + viewId);
                    }
                    return [ 'viewId' ];
                }
            }
        },

        entitySelected: function(token) {
            var contextId;
            var data = {};
            var filterParams = {};
            // inspect token and determine correct context
            if (token.tagId != undefined) {
                contextId = 'tagSelected';
                data.type = 'tag';
                data.key = token.tagId;
                data.title = token.name;
                filterParams.tagIds = token.tagId;
            } else if (token.longName) {
                contextId = 'userSelected';
                data.type = 'user';
                data.key = token.id;
                data.shortName = token.shortName;
                data.longName = token.longName;
                filterParams.userId = token.id;
            } else {
                contextId = 'topicSelected';
                data.type = 'blog';
                data.key = token.id;
                data.title = token.title;
                filterParams.targetBlogId = token.id;
            }
            // TODO use api utils to publish data 
            namespace.widgetController.getDataStore().put(data);
            // just delegate to activate context
            return this.activateContext({
                contextId: contextId,
                contextFilterParameters: filterParams
            });
        },

        restoreState: function() {
            // TODO
        },
        
        /**
         * @return {String} the ID of the current active context or null
         */
        getCurrentContextId: function() {
            return this.filterParameterStore.getFilterParameter('contextId');
        },
        
        /**
         * @return {String} the ID of the current active view of the current context or null
         */
        getCurrentViewId: function() {
            return this.filterParameterStore.getFilterParameter('viewId');
        },

        /**
         * @return {String} the ID of the default context that was passed as option during construction
         */
        getDefaultContextId: function() {
            return this.options.defaultContextId;
        },
        
        /**
         * Return the filter group of the context identified by a context ID and a view ID.
         * 
         * @param {String} contextId The ID of the context
         * @param {String} viewId The ID of the view of the context
         * @return {FilterGroup} the filter group or null if the context has none
         */
        getFilterGroupForContext: function(contextId, viewId) {
            var groupName = this.createFilterGroupName(contextId, viewId);
            return filterWidgetGroupRepo[groupName];
        },
        /**
         * Return the filter parameter store that belongs to the filter group of the context
         * identified by a context ID and a view ID.
         * 
         * @param {String} contextId The ID of the context
         * @param {String} viewId The ID of the view of the context
         * @return {FilterParameterStore} the filter parameter store or null if the context has none
         */
        getFilterParameterStoreForContext: function(contextId, viewId) {
            var filterGroup = this.getFilterGroupForContext(contextId, viewId);
            return filterGroup && filterGroup.getParameterStore();
        },

        /**
         * Generic function that is called when an activateContext event is received and no custom
         * handleContextChange_[context name] handler exists.
         */
        handleContextChange: function(oldContextId, newContextId, oldViewId, newViewId,
                contextFilterParameters, options) {
            var viewIds, i, handler, viewId;
            // for each view of the new context check if there is a prepare function
            viewIds = this.contextDefs[newContextId];
            if (viewIds) {
                for (i = 0; i < viewIds.length; i++) {
                    viewId = viewIds[i];
                    handler = this['prepareView_' + newContextId + '_' + viewId];
                    if (typeOf(handler) == 'function') {
                        handler.call(this, newContextId, viewId, contextFilterParameters, options);
                    }
                }
            }
        },

        createOrResetFilterEventGroup: function(type, handlerArgs, groupName,
                unresetableFilterParams, filterParams, filteredUnresetableParams) {
            var clearedFilterParams, paramName, params;
            var filterGroup = filterWidgetGroupRepo[groupName];
            if (unresetableFilterParams && filterParams) {
                // remove all unresetable from filterParams
                clearedFilterParams = {};
                for (paramName in filterParams) {
                    if (!unresetableFilterParams[paramName]) {
                        clearedFilterParams[paramName] = filterParams[paramName];
                    }
                }
            }
            params = {
                unresetableParams: unresetableFilterParams,
                filterParams: clearedFilterParams || filterParams
            };
            if (filterGroup) {
                communote.widgetController.getFilterEventProcessor().processEvent('onReplace',
                        [ groupName ], params, null);
            } else {
                if (filteredUnresetableParams) {
                    params.filteredUnresetableParams = filteredUnresetableParams;
                }
                filterGroup = new FilterGroup(groupName, params, type, handlerArgs, communote.widgetController
                        .getFilterEventProcessor());
                filterWidgetGroupRepo[groupName] = filterGroup;
            }
        },

        createFilterGroupName: function(contextId, viewId) {
            return 'filterGroup_' + contextId + '_' + viewId;
        },

        prepareView_notesOverview_all: function(contextId, viewId, contextFilterParameters, options) {
            var unresetableParams, filterParams;
            if (options) {
                unresetableParams = options.unresetableParams;
                filterParams = options.filterParams;
            }
            this.createOrResetFilterEventGroup('NotesFilterEventHandler', null, this
                    .createFilterGroupName(contextId, viewId), unresetableParams, filterParams);
        },

        prepareView_notesOverview_following: function(contextId, viewId, contextFilterParameters,
                options) {
            var unresetableParams, filterParams;
            if (options) {
                if (options.unresetableParams) {
                    unresetableParams = Object.append({}, options.unresetableParams);
                }
                filterParams = options.filterParams;
            }
            if (!unresetableParams) {
                unresetableParams = {};
            }
            unresetableParams.showFollowedItems = true;
            this.createOrResetFilterEventGroup('NotesFilterEventHandler', null, this
                    .createFilterGroupName(contextId, viewId), unresetableParams, filterParams);
        },

        prepareView_notesOverview_mentions: function(contextId, viewId, contextFilterParameters,
                options) {
            var unresetableParams, filterParams;
            if (options) {
                if (options.unresetableParams) {
                    unresetableParams = Object.append({}, options.unresetableParams);
                }
                filterParams = options.filterParams;
            }
            if (!unresetableParams) {
                unresetableParams = {};
            }
            unresetableParams.showPostsForMe = true;
            this.createOrResetFilterEventGroup('NotesFilterEventHandler', null, this
                    .createFilterGroupName(contextId, viewId), unresetableParams, filterParams);
        },

        prepareView_notesOverview_favorites: function(contextId, viewId, contextFilterParameters,
                options) {
            var unresetableParams, filterParams;
            if (options) {
                if (options.unresetableParams) {
                    unresetableParams = Object.append({}, options.unresetableParams);
                }
                filterParams = options.filterParams;
            }
            if (!unresetableParams) {
                unresetableParams = {};
            }
            unresetableParams.showFavorites = true;
            this.createOrResetFilterEventGroup('NotesFilterEventHandler', null, this
                    .createFilterGroupName(contextId, viewId), unresetableParams, filterParams);
        },

        prepareView_topicsOverview_directory: function(contextId, viewId, contextFilterParameters,
                options) {
            this.createOrResetFilterEventGroup('BlogsAndUsersFilterEventHandler', {
                context: 'topic'
            }, this.createFilterGroupName(contextId, viewId), {
                showOnlyToplevelTopics: true
            }, options && options.filterParams);
        },

        prepareView_topicsOverview_all: function(contextId, viewId, contextFilterParameters,
                options) {
            // TODO viewType isn't the best concept as it influences the filter widgets which the view types of CPL do not
            var viewType = communoteLocalStorage.getItem('topicsOverview_all-selectedViewType');
            if (!viewType) {
                viewType = 'CLASSIC';
            }
            this.createOrResetFilterEventGroup('BlogsAndUsersFilterEventHandler', {
                context: 'topic'
            }, this.createFilterGroupName(contextId, viewId), {
                showOnlyRootTopics: viewType != 'CLASSIC',
                excludeToplevelTopics: false
            }, options && options.filterParams);
        },
        prepareView_topicsOverview_following: function(contextId, viewId, contextFilterParameters,
                options) {
            this.createOrResetFilterEventGroup('BlogsAndUsersFilterEventHandler', {
                context: 'topic'
            }, this.createFilterGroupName(contextId, viewId), {
                showFollowedItems: true
            }, options && options.filterParams);
        },
        prepareView_topicsOverview_my: function(contextId, viewId, contextFilterParameters, options) {
            this.createOrResetFilterEventGroup('BlogsAndUsersFilterEventHandler', {
                context: 'topic'
            }, this.createFilterGroupName(contextId, viewId), {
                blogAccess: 'manager'
            }, options && options.filterParams);
        },
        prepareView_topicsOverview_admin: function(contextId, viewId, contextFilterParameters,
                options) {
            this.createOrResetFilterEventGroup('BlogsAndUsersFilterEventHandler', {
                context: 'topic'
            }, this.createFilterGroupName(contextId, viewId), {
                forceAllTopics: true
            }, options && options.filterParams);
        },

        prepareView_usersOverview_all: function(contextId, viewId, contextFilterParameters, options) {
            this
                    .createOrResetFilterEventGroup('BlogsAndUsersFilterEventHandler', {
                        context: 'user'
                    }, this.createFilterGroupName(contextId, viewId), null, options
                            && options.filterParams);
        },
        prepareView_usersOverview_following: function(contextId, viewId, contextFilterParameters,
                options) {
            this.createOrResetFilterEventGroup('BlogsAndUsersFilterEventHandler', {
                context: 'user'
            }, this.createFilterGroupName(contextId, viewId), {
                showFollowedItems: true
            }, options && options.filterParams);
        },

        prepareView_tagSelected_notes: function(contextId, viewId, contextFilterParameters, options) {
            this.createOrResetFilterEventGroup('NotesFilterEventHandler', null, this
                    .createFilterGroupName(contextId, viewId), {
                tagIds: contextFilterParameters.tagIds
            }, options && options.filterParams);
        },

        prepareView_userSelected_notes: function(contextId, viewId, contextFilterParameters,
                options) {
            this.createOrResetFilterEventGroup('NotesFilterEventHandler', null, this
                    .createFilterGroupName(contextId, viewId), {
                userId: contextFilterParameters.userId
            }, options && options.filterParams);
        },

        prepareView_topicSelected_notes: function(contextId, viewId, contextFilterParameters,
                options) {
            var unresetableParams, filterParams;
            if (options) {
                if (options.unresetableParams) {
                    unresetableParams = Object.append({}, options.unresetableParams);
                }
                filterParams = options.filterParams;
            }
            if (!unresetableParams) {
                unresetableParams = {};
            }
            if (contextFilterParameters.targetBlogId != undefined) {
                unresetableParams.targetBlogId = contextFilterParameters.targetBlogId;
            }
            unresetableParams.includeChildTopics = true;
            this.createOrResetFilterEventGroup('NotesFilterEventHandler', null, this
                    .createFilterGroupName(contextId, viewId), unresetableParams, filterParams);
        },

        prepareView_topicSelected_topics: function(contextId, viewId, contextFilterParameters,
                options) {
            this.createOrResetFilterEventGroup('BlogsAndUsersFilterEventHandler', {
                context: 'topic'
            }, this.createFilterGroupName(contextId, viewId), {
                parentTopicIds: contextFilterParameters.targetBlogId
            }, options && options.filterParams);
        }

    });

    if (namespace && namespace.addConstructor) {
        namespace.addConstructor('MainPageContextManager', MainPageContextManager);
    } else {
        window.MainPageContextManager = MainPageContextManager;
    }
})(window.runtimeNamespace);