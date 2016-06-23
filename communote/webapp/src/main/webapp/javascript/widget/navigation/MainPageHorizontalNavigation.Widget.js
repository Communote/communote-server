(function(namespace) {
    var HorizontalNavigationWidget = new Class({
        Extends: C_FilterWidget,

        widgetGroup: 'navigation',

        observedFilterParams: [ 'contextId', 'viewId' ],
        contextDefs: null,
        currentContextId: undefined,
        activeViewCssClass: 'active',
        adaptRenderStyleToWidth: true,
        adaptRenderStyleToWidthObserveResize: true,
        tabBarHeightThreshold: 0,
        tabBarRenderStyle: true,
        tabBarRenderStyleCssClass: 'cn-horizontal-navigation-tab-layout',
        toolbars: null,
        optionListOnChangeHandler: undefined,
        onResizeChangeHandler: undefined,

        init: function() {
            var defs, contextId, customLabels, markerCssClass;
            this.parent();
            this.toolbars = {};
            this.contextDefs = {};
            defs = this.getStaticParameter('contextDefinitions');
            for (contextId in defs) {
                if (defs.hasOwnProperty(contextId)) {
                    this.addContextDefinition(contextId, defs[contextId]);
                }
            }
            customLabels = this.getStaticParameter('customLabels');
            if (customLabels) {
                for (contextId in customLabels) {
                    if (customLabels.hasOwnProperty(contextId)) {
                        this.addCustomLabels(contextId, customLabels[contextId]);
                    }
                }
            }
            cssClass = this.getStaticParameter('activeViewCssClass');
            if (cssClass != null) {
                if (cssClass) {
                    this.activeViewCssClass = cssClass;
                } else {
                    // disable highlighting if cssClass is blank or false
                    this.activeViewCssClass = false;
                }
            }
        },

        /**
         * Activate a view of the current context.
         * 
         * @param {String} viewId The ID of the view to activate
         */
        activateView: function(viewId) {
            if (viewId) {
                this.sendFilterGroupEvent('activateView', viewId);
            }
        },

        /**
         * Add a context that should be handled by the navigation.
         * 
         * @param {String} contextId An identifier for the context
         * @param {String[]} viewIds The IDs of the views that belong to the context
         */
        addContextDefinition: function(contextId, viewIds) {
            var newDef;
            if (!this.contextDefs[contextId]) {
                if (viewIds) {
                    this.contextDefs[contextId] = {
                        viewIds: viewIds
                    };
                }
            }
        },

        /**
         * Add custom labels for views of a given context to override the default behavior which
         * uses i18n language packs to find a label for a view.
         * 
         * @param {String} contextId The ID of the context for which the labels should be added
         * @param {String[]} labels An array of strings that look like 'viewId':'label value'
         */
        addCustomLabels: function(contextId, labels) {
            var i, preparedLabels;
            if (this.contextDefs[contextId] && labels) {
                preparedLabels = this.contextDefs[contextId].customLabels || [];
                for (i = 0; i < labels.length; i++) {
                    preparedLabels.include(contextId + '.' + labels[i]);
                }
                if (preparedLabels.length > 0) {
                    this.contextDefs[contextId].customLabels = preparedLabels;
                }
            }
        },

        /**
         * Add an item to a menu of the toolbar of the given view. The menu is identified by a role.
         * 
         * @param {String} viewId The ID of the view whose toolbar should be updated. If the toolbar
         *            does not exist this command is ignored.
         * @param {String} role The role of the menu to update.
         * @param {Object} itemConfig Configuration of the item to add to the menu
         */
        addItemToMenuByRole: function(viewId, role, itemConfig) {
            var toolbar = this.toolbars[viewId];
            if (toolbar) {
                toolbar.addItemToMenuByRole(role, itemConfig);
            }
        },

        addToolToView: function(viewId, toolConfig) {
            var toolbar = this.getOrCreateToolbar(viewId);
            var toolId = toolbar.addTool(toolConfig);
            if (viewId == this.currentViewId) {
                this.changeRenderStyleAdaptively();
            }
            return toolId;
        },

        /**
         * @override
         */
        beforeRemove: function() {
            this.cleanUp();
        },

        changeRenderStyleAdaptively: function() {
            var optionListWrapper;
            var changed = false;
            if (this.adaptRenderStyleToWidth) {
                if (this.tabBarRenderStyle) {
                    if (!this.enoughWidthForTabBar()) {
                        this.domNode.getElementById(this.widgetId + '_tab_bar').setStyle('display',
                                'none');
                        optionListWrapper = this.domNode.getElementById(this.widgetId
                                + '_option_list');
                        optionListWrapper.setStyle('display', '');
                        this.domNode.removeClass(this.tabBarRenderStyleCssClass);
                        if (!this.optionListOnChangeHandler) {
                            this.optionListOnChangeHandler = this.optionListSelectionChanged
                                    .bind(this);
                            optionListWrapper.getElement('select').addEvent('change',
                                    this.optionListOnChangeHandler);
                        }
                        changed = true;
                    }
                } else {
                    if (this.enoughWidthForTabBar()) {
                        this.domNode.getElementById(this.widgetId + '_option_list').setStyle(
                                'display', 'none');
                        this.domNode.getElementById(this.widgetId + '_tab_bar').setStyle('display',
                                '');
                        this.domNode.addClass(this.tabBarRenderStyleCssClass);
                        changed = true;
                    }
                }
            }
            if (changed) {
                this.tabBarRenderStyle = !this.tabBarRenderStyle;
                // active view might not be up-to-date in new renderStyle
                this.highlightActiveView();
            }
            return changed;
        },

        cleanUp: function() {
            var i;
            if (this.firstDOMLoadDone) {
                for (i in this.toolbars) {
                    if (this.toolbars.hasOwnProperty(i)) {
                        this.toolbars[i].destroy();
                    }
                }
                this.toolbars = {};
            }
            if (this.optionListOnChangeHandler) {
                this.domNode.getElement('select').removeEvents('change');
                delete this.optionListOnChangeHandler;
            }
            if (this.onResizeChangeHandler) {
                communote.utils.removeDebouncedResizeEventHandler(this.onResizeChangeHandler);
            }
        },

        enoughWidthForTabBar: function() {
            var navItemHeight, tabBarHeight, threshold;
            var tabBarWrapper = this.domNode.getElementById(this.widgetId + '_tab_bar');
            if (this.tabBarRenderStyle) {
                threshold = this.getTabBarHeightThreshold(tabBarWrapper);
                tabBarHeight = tabBarWrapper.getSize().y;
            } else {
                // tab bar is not shown -> show it to calculate heights.
                tabBarWrapper.setStyle('visibility', 'hidden');
                tabBarWrapper.setStyle('display', '');
                threshold = this.getTabBarHeightThreshold(tabBarWrapper);
                tabBarHeight = tabBarWrapper.getSize().y;
                // restore style, assuming no element style was set
                tabBarWrapper.setStyle('display', 'none');
                tabBarWrapper.setStyle('visibility', '');
            }
            return threshold > tabBarHeight;
        },

        /**
         * @override
         */
        filterParametersChanged: function(changedParams) {
            if (changedParams.indexOf('contextId') != -1) {
                // avoid double refreshs which can occur at startup. This is caused by call to show
                // during initialization and the contexId changed event
                if (this.currentContextId != this.getContextId()) {
                    this.refresh();
                }
            } else {
                // only the viewId changed
                this.viewIdChanged();
            }
        },

        getContextId: function() {
            return this.filterParamStore.getFilterParameter('contextId');
        },

        /**
         * @override
         */
        getListeningEvents: function() {
            var events = this.parent();
            if (this.adaptRenderStyleToWidth) {
                // changing responsive attributes might change the available width
                events.push('onResponsiveAttributesChanged');
            }
            return events;
        },

        getOrCreateToolbar: function(viewId) {
            var constructor;
            var toolbar = this.toolbars[viewId];
            if (!toolbar) {
                constructor = communote.getConstructor('Toolbar');
                toolbar = new constructor(this.domNode.getElementById(this.widgetId
                        + '_toolbar_wrapper'), {
                    cssClass: 'cn-list-settings-bar'
                });
                if (viewId == this.currentViewId) {
                    toolbar.show();
                }
                this.toolbars[viewId] = toolbar;
            }
            return toolbar;
        },

        getTabBarHeightThreshold: function(tabBarWrapper) {
            var tabs, i, height, navItemHeight;
            // tab bar should stay on one line, so compare current height with that of one line
            // note: calculation makes some assumptions: navigation items are direct children of
            // bar and CSS ensures that items break to next line and height of tab-bar increases
            // accordingly and the height of the bar is not higher than twice the height of any
            // navigation item 
            if (this.tabBarHeightThreshold == 0) {
                // use twice the height of the smallest element as threshold
                tabs = tabBarWrapper.getChildren('.control-horizontal-navigation-item');
                navItemHeight = 0;
                for (i = 0; i < tabs.length; i++) {
                    height = tabs[i].getSize().y;
                    if (!navItemHeight || height < navItemHeight) {
                        navItemHeight = height;
                    }
                }
                this.tabBarHeightThreshold = 2 * navItemHeight;
            }
            return this.tabBarHeightThreshold;
        },

        getViewId: function() {
            return this.filterParamStore.getFilterParameter('viewId');
        },
        
        hideTool: function(viewId, toolId) {
            toolbar = this.toolbars[viewId];
            if (toolbar) {
                toolbar.hideTool(toolId);
            }
        },

        /**
         * Highlight the active view by adding the configured CSS class (activeViewCssClass) and
         * removing that class from the previous active view if the current render-style is the
         * tab-bar view. If the render-style is the options list the active view is selected.
         */
        highlightActiveView: function() {
            var viewId, contextId, elem, options, i;
            if (this.tabBarRenderStyle) {
                if (!this.activeViewCssClass) {
                    return;
                }
                elem = this.domNode.getElement('#' + this.widgetId + '_tab_bar .'
                        + this.activeViewCssClass);
                if (elem) {
                    elem.removeClass(this.activeViewCssClass);
                } else {
                    // class will be added for the first time. The height of that tab can change -> recalculate the threshold
                    this.tabBarHeightThreshold = 0;
                }
                viewId = this.getViewId();
                contextId = this.getContextId();
                if (viewId && contextId) {
                    elem = this.domNode.getElement('#' + this.widgetId + '_' + contextId + '_'
                            + viewId);
                    if (elem) {
                        elem.addClass(this.activeViewCssClass);
                    }
                }
            } else {
                viewId = this.getViewId();
                options = this.domNode.getElement('select').getChildren();
                for (i = 0; i < options.length; i++) {
                    if (options[i].value == viewId) {
                        options[i].selected = true;
                        break;
                    }
                }
            }
        },

        /**
         * Handler for the widget event 'onResponsiveAttributesChanged'.
         * 
         * @param {String[]} Names of the changed attributes
         */
        onResponsiveAttributesChanged: function(changedAttributes) {
            // can ignore simple changes of the viewportWidth attribute because already listening
            // to the resize event 
            if (changedAttributes.length
                    && (changedAttributes.length > 1 || changedAttributes[0] !== 'viewportWidth')) {
                // this is butt-ugly: CSS transitions can lead to wrong calculations, so delay the
                // calculation. Could use the transitionend, but this is ugly too... 
                setTimeout(this.changeRenderStyleAdaptively.bind(this), 750);
            }
        },

        optionListSelectionChanged: function(event) {
            if (event.target) {
                event.target.blur();
                this.activateView(event.target.value);
            }
        },

        /**
         * @override
         */
        refresh: function() {
            var contextDef, viewIds;
            // only refresh if a context is set and the context is known
            var contextId = this.getContextId();
            if (contextId) {
                this.currentContextId = contextId;
                contextDef = this.contextDefs[contextId];
                if (contextDef && contextDef.viewIds) {
                    this.setFilterParameter('viewIds', contextDef.viewIds.join(','));
                    if (contextDef.customLabels) {
                        this.setFilterParameter('customLabels', contextDef.customLabels.join(','));
                    } else {
                        this.unsetFilterParameter('customLabels');
                    }
                    this.cleanUp();
                    this.currentViewId = null;
                    this.parent();
                }
            }
        },

        /**
         * @override
         */
        refreshComplete: function(responseMetadata) {
            this.parent(responseMetadata);
            this.tabBarHeightThreshold = 0;
            this.tabBarRenderStyle = true;
            this.domNode.addClass(this.tabBarRenderStyleCssClass);
            this.viewIdChanged();
            if (this.adaptRenderStyleToWidthObserveResize) {
                this.onResizeChangeHandler = this.changeRenderStyleAdaptively.bind(this);
                communote.utils.addDebouncedResizeEventHandler(this.onResizeChangeHandler);
            }
        },

        showTool: function(viewId, toolId) {
            toolbar = this.toolbars[viewId];
            if (toolbar) {
                toolbar.showTool(toolId);
            }
        },
        
        switchToolbar: function(oldViewId, newViewId) {
            var toolbar;
            if (oldViewId) {
                toolbar = this.toolbars[oldViewId];
                if (toolbar) {
                    toolbar.hide();
                }
            }
            toolbar = this.toolbars[newViewId];
            if (toolbar) {
                toolbar.show();
            }
        },

        updateMenuItem: function(viewId, toolId, index, newConfig) {
            var toolbar = this.getOrCreateToolbar(viewId);
            toolbar.updateMenuItem(toolId, index, newConfig);
        },
        updateToggleItemState: function(viewId, toolId, activate, callAction) {
            var toolbar = this.toolbars[viewId];
            if (toolbar) {
                toolbar.updateToggleItemState(toolId, activate, callAction);
            }
        },

        /**
         * Update the navigation after a view change by showing the appropriate toolbar and
         * highlighting the current view.
         */
        viewIdChanged: function() {
            var oldViewId = this.currentViewId;
            this.currentViewId = this.getViewId();
            this.switchToolbar(oldViewId, this.currentViewId);
            if (!oldViewId) {
                // directly after a refresh no active view is selected, but selecting an active
                // view in tab mode can change the threshold, so this must be done before adapting
                // to height
                this.highlightActiveView();
            }
            // width might have changed
            if (!this.changeRenderStyleAdaptively()) {
                this.highlightActiveView();
            }
        }
    });
    namespace.addConstructor('MainPageHorizontalNavigationWidget', HorizontalNavigationWidget);
})(window.runtimeNamespace);