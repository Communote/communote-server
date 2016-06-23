(function(window) {
    var communote = window.communote;

    function registerFilterToggleTool(responsiveUtils) {
        var filterParameterObserver = {
            filterGroup: undefined,
            filterGroupHasFilters: false,

            attach: function(filterGroup) {
                filterGroup.addMember(this);
                this.filterGroup = filterGroup;
                this.filterParametersChanged(null);
            },
            detach: function() {
                if (this.filterGroup) {
                    this.filterGroup.removeMember(this);
                }
            },
            filterParametersChanged: function(changedParams) {
                var newState = this.filterGroup.getParameterStore().hasFilterParameters(true);
                if (newState != this.filterGroupHasFilters) {
                    this.filterGroupHasFilters = newState;
                    responsiveUtils.toggleFilteredState();
                }
            },
            getObservedFilterParameters: function() {
                // observe all changes
                return [ '*' ];
            }
        };
        var filterToggleToolProvider = {
            contextManager: undefined,
            defFilterToggledOn: undefined,
            defFilterToggledOff: undefined,
            horizontalNavigationWidgetId: undefined,
            knownViews: undefined,
            toolIds: undefined,

            addToolToNavigation: function(viewId) {
                var navWidget = communote.widgetController
                        .getWidget(this.horizontalNavigationWidgetId);
                if (navWidget) {
                    if (this.knownViews.indexOf(viewId) < 0) {
                        if (!this.defFilterToggledOn) {
                            this.defFilterToggledOn = {
                                cssClass: 'active',
                                action: responsiveUtils.toggleFilter.bind(responsiveUtils)
                            };
                            this.defFilterToggledOff = {
                                action: this.defFilterToggledOn.action
                            };
                        }
                        this.toolIds[viewId] = navWidget.addToolToView(viewId, {
                            type: 'toggle',
                            cssClass: 'cn-filter-toggle cn-icon',
                            toggledOn: this.defFilterToggledOn,
                            toggledOff: this.defFilterToggledOff,
                            isOn: responsiveUtils.testResponsiveAttribute({
                                name: 'filterShown',
                                value: true
                            }),
                            hidden: responsiveUtils.testResponsiveAttribute({
                                name: 'viewportWidth',
                                value: 'full'
                            }) 
                        });
                        this.knownViews.push(viewId);
                    } else {
                        // update state of toggle since state is shared among all views
                        navWidget.updateToggleItemState(viewId, this.toolIds[viewId],
                                responsiveUtils.testResponsiveAttribute({
                                    name: 'filterShown',
                                    value: true
                                }), false);
                    }
                }
            },

            /**
             * Implementation of FilterParameterListener interface method. Will be called if the
             * viewId changed.
             */
            filterParametersChanged: function(changedParams) {
                var viewId = this.getViewId();
                var filterGroup = this.getFilterGroup(viewId);
                filterParameterObserver.detach();
                if (filterGroup) {
                    // ignore viewId changes that go along with contextId changes since the
                    // naviWidget will refresh. Also ignore views which have no filters.
                    this.addToolToNavigation(viewId);
                    filterParameterObserver.attach(filterGroup);
                }
            },

            getFilterGroup: function(viewId) {
                var contextManager = this.contextManager;
                if (contextManager) {
                    return contextManager.getFilterGroupForContext(contextManager
                            .getCurrentContextId(), viewId);
                }
            },

            /**
             * Implementation of FilterParameterListener interface method.
             */
            getObservedFilterParameters: function() {
                // in embedView there are no contextId changes so only interested viewId changes
                return [ 'viewId' ];
            },

            getViewId: function() {
                return this.contextManager.getCurrentViewId();
            },

            /**
             * Implementation of a method defined by the widget event listener interface.
             */
            handleEvent: function(eventName, params) {
                var viewId;
                // only interested in the refreshcomplete of the MainPageHorizontalNavigation widgets
                if (eventName == 'onWidgetRefreshComplete') {
                    if (params.widgetType == 'MainPageHorizontalNavigationWidget') {
                        if (!this.horizontalNavigationWidgetId) {
                            this.horizontalNavigationWidgetId = params.widgetId;
                        } else if (this.horizontalNavigationWidgetId != params.widgetId) {
                            return;
                        }
                        // reset views that were already activated for current context
                        this.knownViews = [];
                        this.toolIds = {};
                        viewId = this.getViewId();
                        if (this.getFilterGroup(viewId)) {
                            this.addToolToNavigation(viewId);
                        }
                    }
                }
            },
            
            showHideFilterToggle: function(show) {
                var navWidget, i, viewId;
                if (this.horizontalNavigationWidgetId && this.knownViews) {
                    navWidget = communote.widgetController.getWidget(this.horizontalNavigationWidgetId);
                    // show/hide for all views
                    for (i = 0; i < this.knownViews.length; i++) {
                        viewId = this.knownViews[i];
                        if (show) {
                            navWidget.showTool(viewId, this.toolIds[viewId]);
                        } else {
                            navWidget.hideTool(viewId, this.toolIds[viewId]);
                        }
                    }
                }
            }
        };
        communote.initializer.addBeforeWidgetScanCallback(function() {
            var contextManager = communote.contextManager;
            if (!contextManager) {
                return;
            }
            // add filterToggleToolProvider as widget event listener observing the refresh complete
            communote.widgetController.registerWidgetEventListener('onWidgetRefreshComplete',
                    filterToggleToolProvider);
            filterToggleToolProvider.contextManager = contextManager;
            contextManager.getFilterGroup().addMember(filterToggleToolProvider);
        });
        return filterToggleToolProvider;
    }

    var ResponsiveUtils = function(viewManager) {
        if (window.matchMedia && communote.embed.filterEnabled) {
            this.supported = true;
            this.attributeMatcher = new communote.classes.AttributeMatcher({
                // whether the filters are shown
                filterShown: false,
                // width of the viewport. Can be medium or full.
                viewportWidth: 'full'
            }, this.responsiveAttributeChanged.bind(this));
            this.filterToggleTool = registerFilterToggleTool(this);
            this.initMediaQueries();
            if (viewManager) {
                this.viewManager = viewManager;
                viewManager.registerConditionEvaluator('responsive',
                        this.attributeMatcher.matchesCondition.bind(this.attributeMatcher));
            }
        } else {
            this.supported = false;
        }
    };
    ResponsiveUtils.prototype.initMediaQueries = function() {
        var mqlListener = this.viewportWidthMqlListener.bind(this);
        var mql = window.matchMedia('screen and (max-width: 700px)');
        if (mql.matches) {
            this.attributeMatcher.updateAttribute('viewportWidth', 'medium')
        }
        mql.addListener(mqlListener);
    };
    /**
     * Change callback for the AttributeMatcher.
     */
    ResponsiveUtils.prototype.responsiveAttributeChanged = function(changedAttributes) {
        if (this.viewManager) {
            this.viewManager.conditionChanged('responsive');
        }
        if (changedAttributes.indexOf('viewportWidth') != -1) {
            if (this.attributeMatcher.matchesExactly('viewportWidth', 'medium')) {
                this.filterToggleTool.showHideFilterToggle(true);
            } else {
                this.filterToggleTool.showHideFilterToggle(false);
            }
        }
        // let widgets handle the attribute changes
        if (communote.widgetController) {
            communote.widgetController.sendEvent('onResponsiveAttributesChanged', null,
                    changedAttributes);
        }
    };
    /**
     * Implementation of the (descriptive) ResponsiveUtils interface method.
     * 
     * @return {Boolean} true if responsive design is enabled
     */
    ResponsiveUtils.prototype.responsiveDesignEnabled = function() {
        return this.supported;
    };
    /**
     * Implementation of the (descriptive) ResponsiveUtils interface method.
     * 
     * @param {Object} condition The condition to test. Can be any condition that is supported by
     *            AttributeMatcher.matchesCondition
     * @return {Boolean} true if the condition matches
     */
    ResponsiveUtils.prototype.testResponsiveAttribute = function(condition) {
        return this.supported && this.attributeMatcher.matchesCondition(condition);
    };

    ResponsiveUtils.prototype.toggleFilter = function() {
        var filterShown;
        var communoteContainer = document.getElementById('cn-communote');
        if (this.attributeMatcher.matchesExactly('filterShown', true)) {
            communoteContainer.removeClass('cn-open-filter');
            filterShown = false;
        } else {
            communoteContainer.addClass('cn-open-filter');
            filterShown = true;
        }
        this.attributeMatcher.updateAttribute('filterShown', filterShown);
    };

    ResponsiveUtils.prototype.toggleFilteredState = function() {
        var communoteContainer = document.getElementById('cn-communote');
        communoteContainer.toggleClass('cn-content-filtered');
    };
    ResponsiveUtils.prototype.viewportWidthMqlListener = function(mql) {
        if (!mql.matches) {
            this.attributeMatcher.updateAttribute('viewportWidth', 'full');
        } else {
            this.attributeMatcher.updateAttribute('viewportWidth', 'medium');
        }
    };
    communote.classes.ResponsiveUtils = ResponsiveUtils;
})(this);