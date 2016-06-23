(function(namespace) {
    var responsiveUtils;

    function registerCloseNavigationTool(responsiveUtils) {
        var closeTool = {
                filterParametersChanged: function(changedParams) {
                    responsiveUtils.hideNavigation();
                },
                getObservedFilterParameters: function() {
                    // interested in changes of any filter parameter
                    return [ 'viewId', 'contextId' ];
                }
        };
        communote.initializer.addBeforeWidgetScanCallback(function() {
            var contextManager = communote.contextManager;
            if (!contextManager) {
                return;
            }
            contextManager.getFilterGroup().addMember(closeTool);
        });
    }
    
    function registerFilterTool(responsiveUtils) {
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
                    responsiveUtils.toggleFiltered();
                }
            },
            getObservedFilterParameters: function() {
                // observe all changes
                return [ '*' ];
            }
        };
        var toolProvider = {
            horizontalNavigationWidgetId: undefined,
            responsiveUtils: responsiveUtils,
            defFilterToggledOn: undefined,
            defFilterToggledOff: undefined,
            toolIds: undefined,

            addToolsToNavigation: function(viewId) {
                var navWidget = communote.widgetController
                        .getWidget(this.horizontalNavigationWidgetId);
                if (navWidget) {
                    if (!this.knownViews.contains(viewId)) {
                        if (!this.defFilterToggledOn) {
                            this.defFilterToggledOn = {
                                cssClass: 'active',
                                action: this.responsiveUtils.toggleFilter
                                        .bind(this.responsiveUtils)
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
                            isOn: this.responsiveUtils.testResponsiveAttribute({
                                name: 'filterShown',
                                value: true
                            })
                        });
                        this.knownViews.push(viewId);
                    } else {
                        // update state of toggle since state is shared among all views
                        navWidget.updateToggleItemState(viewId, this.toolIds[viewId],
                                this.responsiveUtils.testResponsiveAttribute({
                                    name: 'filterShown',
                                    value: true
                                }), false);
                    }
                }
            },

            filterParametersChanged: function(changedParams) {
                var attachFilterObserver, filterGroup;
                var viewId = this.getViewId();
                if (changedParams.contains('viewId')) {
                    filterGroup = this.getFilterGroup(viewId);
                    if (!changedParams.contains('contextId') && filterGroup) {
                        // ignore viewId changes that go along with contextId changes since the
                        // naviWidget will refresh. Also ignore views which have no filters.
                        this.addToolsToNavigation(viewId);
                    }
                    attachFilterObserver = true;
                } else if (changedParams.contains('contextId')) {
                    attachFilterObserver = true;
                    filterGroup = this.getFilterGroup(viewId);
                }
                if (attachFilterObserver) {
                    filterParameterObserver.detach();
                    if (filterGroup) {
                        filterParameterObserver.attach(filterGroup);
                    }
                }
                // hide the navigation because this event was most likely caused by a selection of an
                // item of the vertical navigation (note: also have to hide on pure viewId changes
                // due to the @ and follow icons in vertical navigation)
                this.responsiveUtils.hideNavigation();
            },

            getContextId: function() {
                return this.contextManager.getCurrentContextId();
            },

            getFilterGroup: function(viewId) {
                var contextManager = this.contextManager;
                if (contextManager) {
                    return contextManager.getFilterGroupForContext(
                            this.getContextId(), viewId);
                }
            },

            getObservedFilterParameters: function() {
                // interested in changes of any filter parameter
                return [ '*' ];
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
                            this.addToolsToNavigation(viewId);
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
            if (namespace.environment && namespace.environment.page === 'main') {
                // add this instance as widget event listener observing the refresh complete
                communote.widgetController.registerWidgetEventListener('onWidgetRefreshComplete',
                        toolProvider);
            }
            toolProvider.contextManager = contextManager;
            contextManager.getFilterGroup().addMember(toolProvider);
        });
        return toolProvider;
    }

    /**
     * Implements the simple (declarative) interface (consisting of methods testResponsiveAttribute
     * and responsiveDesignEnabled) for the responsiveUtils with the help of media queries. The
     * implementation contains a lot of logic which is tailored for the full Communote FE.
     */
    var ResponsiveUtils = new Class({
        viewManager: undefined,
        supported: false,
        attributeMatcher: undefined,
        closeNavigationClickHandler: undefined,
        navigationShown: false,
        fullViewportWidthMQL: undefined,
        mediumViewportWidthMQL: undefined,
        mediumViewportNavigationToggleClickHandler: undefined,
        smallViewportNavigationToggleClickHandler: undefined,
        smallViewportWidthMQL: undefined,
        tinyViewportWidthMQL: undefined,
        userMenuToggleClickHandler: undefined,

        initialize: function(viewManager) {
            this.viewManager = viewManager;
            if (window.matchMedia) {
                this.supported = true;
                this.attributeMatcher = new communote.classes.AttributeMatcher({
                    // whether the filters are shown
                    filterShown: false,
                    // width of the viewport. Can be small, medium or full.
                    viewportWidth: 'full'
                }, this.responsiveAttributeChanged.bind(this));
                this.initMediaQueries();
                if (viewManager) {
                    viewManager.registerConditionEvaluator('responsive',
                            this.attributeMatcher.matchesCondition.bind(this.attributeMatcher));
                }
                if (namespace.environment && namespace.environment.page === 'main') {
                    // TODO this can be optimized, since the toolProvider is not necessary in large
                    // screen modes, but this is kind of difficult
                    registerFilterTool(this);
                } else {
                    // for other pages (editTopic, editUser) add the tool that closes the horizontal navigation
                    registerCloseNavigationTool(this);
                }
            }
        },

        attachNavigationToggleClickHandler: function(mediumWidth) {
            var elem, elems, i, baseURL;
            if (mediumWidth && !this.mediumViewportNavigationToggleClickHandler) {
                elem = document.getElementById('cn-desktop-navigation-toggle');
                if (elem) {
                    this.mediumViewportNavigationToggleClickHandler = this.toggleFilter.bind(this);
                    elem.addEvent('click', this.mediumViewportNavigationToggleClickHandler);
                }
            } else if (!mediumWidth && !this.smallViewportNavigationToggleClickHandler) {
                elem = document.getElementById('cn-mobile-navigation-toggle');
                if (elem) {
                    this.smallViewportNavigationToggleClickHandler = this.toggleNavigation
                            .bind(this);
                    elem.addEvent('click', this.smallViewportNavigationToggleClickHandler);
                }
            }
            // add handler to close navigation when an navigation item with fragment identifier is clicked
            if (!this.closeNavigationClickHandler
                    && (!namespace.environment || namespace.environment.page !== 'main')) {
                this.closeNavigationClickHandler = this.navigationLinkWithFragmentClicked.bind(this);
                baseURL = location.protocol + '//' + location.host + location.pathname + '#';
                elems = document.getElements('.navigation-item a');
                for (i = 0; i < elems.length; i++) {
                    elem = elems[i];
                    if (elem.href.indexOf(baseURL) === 0 || elem.href.indexOf('#') === 0) {
                        elem.addEvent('click', this.closeNavigationClickHandler);
                    }
                }
            }
        },

        attachUserMenuToggleClickHandler: function() {
            var elem;
            if (!this.userMenuToggleClickHandler) {
                elem = document.getElementById('cn-mobile-user-menu-toggle');
                if (elem) {
                    if (Modernizr.touch) {
                        this.userMenuToggleClickHandler = this.toggleUserMenu.bind(this);
                        elem.addEvent('click', this.userMenuToggleClickHandler);
                    } else {
                        elem.addClass("cn-hidden");
                        var mobileNavigation = document.getElementById('cn-profile-navigation-mobile');
                        mobileNavigation.removeClass("cn-hidden");
                    }
                }
            }
        },

        hideNavigation: function(navigationToggle) {
            var communoteContainer;
            if (this.attributeMatcher.matches('viewportWidth', [ 'tiny', 'small' ])
                    && this.navigationShown) {
                communoteContainer = document.getElementById('cn-communote');
                communoteContainer.removeClass('cn-open-menu');
                // if filters are set restore filter class
                if (this.attributeMatcher.matchesExactly('filterShown', true)) {
                    communoteContainer.addClass('cn-open-filter');
                }
                if (!navigationToggle) {
                    navigationToggle = document.getElementById('cn-mobile-navigation-toggle');
                }
                navigationToggle.removeClass('active');
                this.navigationShown = false;
            }
        },
        
        navigationLinkWithFragmentClicked: function(e) {
            var linkElem = communote.utils.getClickedLinkElement(e, false);
            var idx = linkElem.href.indexOf('#') + 1;
            // scroll manually to target and hide navigation afterwards. This is required because
            // browser default behavior uses element.scrollIntoView which does not work too well when 
            // manipulating margins (with transitions, as it happens during hideNavigation). The
            // scrollIntoView moves the content somehow without touching margins. Finally,
            // everything could end up displaced if margins are modified. 
            var fragmentTargetElem = document.getElementById(linkElem.href.substring(idx));
            if (fragmentTargetElem) {
                scrollWindowTo(fragmentTargetElem);
            }
            this.hideNavigation(false);
            // prevent default behavior
            communote.utils.eventPreventDefault(e);
        },

        responsiveAttributeChanged: function(changedAttributes) {
            var value;
            if (this.viewManager) {
                this.viewManager.conditionChanged('responsive');
            }
            if (changedAttributes.indexOf('viewportWidth') > -1) {
                value = this.attributeMatcher.getValue('viewportWidth');
                if (value === 'medium') {
                    this.attachNavigationToggleClickHandler(true);
                } else if (value != 'full') {
                    this.attachNavigationToggleClickHandler(false);
                    this.attachUserMenuToggleClickHandler();
                }
            }
            // let widgets handle the attribute changes
            if (namespace.widgetController) {
                namespace.widgetController.sendEvent('onResponsiveAttributesChanged', null,
                        changedAttributes);
            }
        },

        responsiveDesignEnabled: function() {
            return this.supported;
        },

        showNavigation: function(navigationToggle) {
            var communoteContainer;
            if (this.attributeMatcher.matches('viewportWidth', [ 'tiny', 'small' ])
                    && !this.navigationShown) {
                communoteContainer = document.getElementById('cn-communote');
                communoteContainer.addClass('cn-open-menu');
                // TODO fix CSS, because we have to remove the filter class since it destroys layout
                communoteContainer.removeClass('cn-open-filter');
                if (!navigationToggle) {
                    navigationToggle = document.getElementById('cn-mobile-navigation-toggle');
                }
                navigationToggle.addClass('active');
                // fast scrolling because smooth scrolling feels strange
                scrollWindowTo(null, 0, 0, false, false);
                this.navigationShown = true;
            }
        },

        toggleFilter: function() {
            var filterShown;
            var communoteContainer = document.getElementById('cn-communote');
            if (this.attributeMatcher.matchesExactly('filterShown', true)) {
                communoteContainer.removeClass('cn-open-filter');
                filterShown = false;
            } else {
                communoteContainer.addClass('cn-open-filter');
                // have to remove the menu class to not destroy layout
                communoteContainer.removeClass('cn-open-menu');
                this.navigationShown = false;
                filterShown = true;
            }
            this.attributeMatcher.updateAttribute('filterShown', filterShown);
        },

        toggleFiltered: function() {
            // TODO add to content?
            var communoteContainer = document.getElementById('cn-communote');
            communoteContainer.toggleClass('cn-content-filtered');
        },

        toggleNavigation: function(event) {
            var navigationToggle = event && event.target;
            if (this.navigationShown) {
                this.hideNavigation(navigationToggle);
            } else {
                this.showNavigation(navigationToggle);
            }
        },

        toggleUserMenu: function() {
            var userMenu = document.getElementById('cn-mobile-user-menu-wrapper');
            showDialog(communote.i18n.getMessage('portal.menu.mobile.profile.popup.title'), userMenu.clone(), false, {
                windowCssClasses: 'cn-touch-actions-popup'
            });
        },

        initMediaQueries: function() {
            var mqlListener = this.viewportWidthMqlListener.bind(this);
            var mql = matchMedia('screen and (max-width: 534px)');
            if (mql.matches) {
                this.attributeMatcher.updateAttribute('viewportWidth', 'tiny')
            }
            this.tinyViewportWidthMQL = mql;
            mql.addListener(mqlListener);
            mql = matchMedia('screen and (max-width: 768px) and (min-width: 535px)');
            if (mql.matches) {
                this.attributeMatcher.updateAttribute('viewportWidth', 'small')
            }
            this.smallViewportWidthMQL = mql;
            mql.addListener(mqlListener);
            mql = matchMedia('screen and (max-width: 1024px) and (min-width: 769px)');
            if (mql.matches) {
                this.attributeMatcher.updateAttribute('viewportWidth', 'medium');
            }
            mql.addListener(mqlListener);
            this.mediumViewportWidthMQL = mql;
            // define an mql for full width, but do not add a listener since it is not needed
            this.fullViewportWidthMQL = matchMedia('screen and (min-width: 1025px)');
        },

        testResponsiveAttribute: function(condition) {
            return this.supported && this.attributeMatcher.matchesCondition(condition);
        },

        viewportWidthMqlListener: function(mql) {
            var widthValue;
            if (!mql.matches) {
                // check if full width MQL matches, if not just wait for the listener of the other MQLS
                if (this.fullViewportWidthMQL.matches) {
                    this.attributeMatcher.updateAttribute('viewportWidth', 'full');
                }
            } else {
                // test which mql matched
                if (this.mediumViewportWidthMQL.matches) {
                    widthValue = 'medium';
                } else if (this.smallViewportWidthMQL.matches) {
                    widthValue = 'small';
                } else {
                    widthValue = 'tiny';
                }
                this.attributeMatcher.updateAttribute('viewportWidth', widthValue);
            }
        }
    });

    if (namespace && namespace.addConstructor) {
        namespace.addConstructor('ResponsiveUtils', ResponsiveUtils);
    }
})(window.runtimeNamespace);