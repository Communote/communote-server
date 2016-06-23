(function(namespace) {

    var ViewManager = new Class({
        Implements: Options,

        options: {
            // a CSS class to append to the containers that should be hidden. If not defined
            // 'display' style will be changed to 'none'.
            hiddenCssClass: false
        },
        baseElement: undefined,
        widgetController: undefined,
        widgetConfigurations: {},
        viewDefinitions: {},
        // mapping from condition type to function that evaluates a
        // condition of conditionalVisibleWidgets or selectors
        conditionEvaluators: {},
        currentViewId: false,
        // array of IDs of the widgets shown in the current view
        currentVisibleWidgetIds: undefined,
        // mapping from condition type to array of IDs of the conditional widgets currently shown
        currentConditionalVisibleWidgetIds: undefined,
        // array of IDs of the widgets hidden in the current view
        currentHiddenWidgetIds: undefined,
        // array of selectors of the containers hidden in the current view
        currentHiddenContainers: undefined,
        // mapping from widgetId to the current selector. If no selector is defined, the value is false.
        currentWidgetSelectors: {},
        // array containing all the widgetIds of the current view as they are found during view activation.
        // Widgets of parentViews will be contained after those of the actual view!
        currentOrderedWidgetIds: undefined,

        initialize: function(widgetController, baseElement, configuration, options) {
            baseElement = typeOf(baseElement) == 'string' ? document.getElement(baseElement)
                    : document.id(baseElement);
            if (!baseElement) {
                throw "Base element not found in DOM";
            }
            this.baseElement = baseElement;
            this.setOptions(options);
            this.widgetController = widgetController;
            this.extractWidgetConfigurations(configuration);
            this.extractViewDefinitions(configuration);
            this.currentHiddenWidgetIds = [];
        },

        /**
         * Activate a view and deactivate the current active view. When a view is activated the
         * widgets of the view and its parent views will be shown or created. New widgets will only
         * be created if they were registered with a call to addWidgetConfiguration or were
         * specified within the configuration passed to the constructor. Moreover, the parent node
         * of the widget must be the baseElement managed by the view manager or a child node of it.
         * The widgets of the previous view will be hidden or removed as defined by the
         * previousViewAction of the view definition. If the view definition of the current or new
         * view define that containers should be hidden those containers will be handled too. If the
         * provided view identifier is not known the call is ignored.
         * 
         * @param {String} viewId ID of the view to activate. The view must have been added with a
         *            call to addViewDefinition or as part of the configuration passed to the
         *            constructor.
         * @param {Function} [beforeHandleNewViewWidgetsCallback] Optional callback that if provided
         *            will be invoked before creating or showing the widgets of the new view. The
         *            function must not call activateView.
         */
        activateView: function(viewId, beforeHandleNewViewWidgetsCallback) {
            var oldDefinition, newDefinition, widgetsAndContainers, widgetIdsDiff, containerDiff;
            var hiddenWidgetIds, action;
            var utils = namespace.utils;
            if (this.currentViewId == viewId || !this.viewDefinitions[viewId]) {
                return;
            }
            oldDefinition = this.currentViewId ? this.viewDefinitions[this.currentViewId] : false;
            newDefinition = this.viewDefinitions[viewId];
            widgetsAndContainers = this.getAllWidgetsAndHiddenContainers(newDefinition);
            widgetIdsDiff = utils.createDiff(this.currentVisibleWidgetIds,
                    widgetsAndContainers.widgetIds);
            containerDiff = utils.createDiff(this.currentHiddenContainers,
                    widgetsAndContainers.hiddenContainers);
            if (widgetIdsDiff.removed.length) {
                action = this.getPreviousViewAction(oldDefinition, newDefinition);
                this.hideOrRemoveWidgets(widgetIdsDiff.removed, action);
                if (action == 'remove') {
                    // when removing also remove hidden widgets which are not shown in the new view
                    hiddenWidgetIds = utils.createDiff(this.currentHiddenWidgetIds,
                            widgetIdsDiff.added);
                    this.hideOrRemoveWidgets(hiddenWidgetIds.removed, action);
                    this.currentHiddenWidgetIds = [];
                }
            }
            // hide newly added hidden containers and show containers that were only hidden in previous view 
            this.showOrHideContainers(containerDiff.added, false);
            this.showOrHideContainers(containerDiff.removed, true);
            if (beforeHandleNewViewWidgetsCallback) {
                beforeHandleNewViewWidgetsCallback.call(null);
            }
            this.showOrCreateWidgets(widgetIdsDiff.added);
            this.currentViewId = viewId;
            this.currentVisibleWidgetIds = widgetsAndContainers.widgetIds;
            this.currentConditionalVisibleWidgetIds = widgetsAndContainers.conditionalWidgetIds;
            this.currentHiddenContainers = widgetsAndContainers.hiddenContainers;
            this.currentOrderedWidgetIds = widgetsAndContainers.orderedWidgetIds;
        },

        /**
         * Add a new view definition.
         * 
         * @param {String} viewId The ID of the view
         * @param {String} [parentViewId] The ID of the parent view, if there is one. The visible
         *            widgets defined in the view definition of the parent view will also be visible
         *            if this view is activated.
         * @param {String|String[]} visibleWidges Widget ID or array of Widget IDs that should be
         *            shown when the view is activated
         * @param {Object|Object[]} [conditionalVisibleWidgets] Object or array of objects that
         *            describe that some widgets should only be shown under certain conditions
         * @param {String} previousViewAction The string 'hide' or 'remove' that describe what
         *            should happen with the currently visible widgets are no longer visible if this
         *            view is activated. If the this view has a parent view and which differs from
         *            the parent view of the previews active view the previousViewAction will be
         *            considered too. If the parent view defines a differing action the stronger (=
         *            remove) will be used.
         * @param {String|String[]} [hiddenContainers] CSS-selector or array of selectors of
         *            containers that should be hidden when this view is activated.
         */
        addViewDefinition: function(viewId, parentViewId, visibleWidgets,
                conditionalVisibleWidgets, previousViewAction, hiddenContainers) {
            var definition, i, l;
            if (!viewId || this.viewDefinitions[viewId]) {
                return;
            }
            definition = {};
            definition.parentViewId = parentViewId;
            // default to remove
            if (previousViewAction != 'hide') {
                definition.previousViewAction = 'remove';
            } else {
                definition.previousViewAction = previousViewAction;
            }
            definition.widgetIds = [];
            if (visibleWidgets) {
                visibleWidgets = Array.from(visibleWidgets);
                for (i = 0, l = visibleWidgets.length; i < l; i++) {
                    // assert the widget exists
                    if (this.widgetConfigurations[visibleWidgets[i]]) {
                        definition.widgetIds.push(visibleWidgets[i]);
                    }
                }
            }
            if (conditionalVisibleWidgets) {
                // flat copy
                conditionalVisibleWidgets = Array.from(conditionalVisibleWidgets);
                l = conditionalVisibleWidgets.length;
                definition.conditionalWidgets = new Array(l);
                for (i = 0; i < l; i++) {
                    definition.conditionalWidgets[i] = conditionalVisibleWidgets[i];
                }
            }
            definition.hiddenContainers = [];
            if (hiddenContainers) {
                hiddenContainers = Array.from(hiddenContainers);
                for (i = 0, l = hiddenContainers.length; i < l; i++) {
                    definition.hiddenContainers.push(hiddenContainers[i]);
                }
            }
            this.viewDefinitions[viewId] = definition;
        },

        addWidgetConfiguration: function(widgetId, widgetType, parentContainerSelector,
                conditionalParentContainerSelectors, settings) {
            var config, i, l;
            if (!widgetId || !widgetType || this.widgetConfigurations[widgetId]) {
                return;
            }
            config = {};
            config.type = widgetType;
            if (typeof parentContainerSelector == 'string') {
                config.selector = parentContainerSelector;
            }
            if (conditionalParentContainerSelectors) {
                // flat copy
                conditionalParentContainerSelectors = Array
                        .from(conditionalParentContainerSelectors);
                l = conditionalParentContainerSelectors.length;
                config.conditionalSelectors = new Array(l);
                for (i = 0; i < l; i++) {
                    config.conditionalSelectors[i] = conditionalParentContainerSelectors[i];
                }
            }
            config.settings = settings;
            this.widgetConfigurations[widgetId] = config;
        },

        /**
         * Function to be called when a condition changed. This function will show and hide widgets
         * after reevaluating the condition for the current view.
         * 
         * @param {String} type The type of the condition that changed
         */
        conditionChanged: function(type) {
            var viewDefinition, conditionalWidgets, visibleWidgetIds, i, l, diff, utils;
            if (!this.currentViewId) {
                return;
            }
            // TODO parentViews!
            viewDefinition = this.viewDefinitions[this.currentViewId];
            if (viewDefinition.conditionalWidgets) {
                visibleWidgetIds = [];
                for (i = 0, l = viewDefinition.conditionalWidgets.length; i < l; i++) {
                    // ignore conditional widgets of another type
                    conditionalWidgets = viewDefinition.conditionalWidgets[i];
                    if (type === conditionalWidgets.type) {
                        if (this.evaluateCondition(type, conditionalWidgets.condition, true)) {
                            visibleWidgetIds.append(conditionalWidgets.visibleWidgets);
                        }
                    }
                }
                utils = namespace.utils;
                // find widgets to hide or show by creating a diff to current visible widgets of the condition
                diff = utils.createDiff(this.currentConditionalVisibleWidgetIds[type],
                        visibleWidgetIds);
                // avoid hiding widgets which should be always visible
                utils.removeAllFromArray(diff.removed, viewDefinition.widgetIds);
                // hide removed widgets
                this.hideOrRemoveWidgets(diff.removed, 'hide');
                // move visible widgets that have a conditional selector to another container if necessary
                this.moveVisibleWidgetsAfterConditionChanged(type, diff.added);
                // show added widgets
                this.showOrCreateWidgets(diff.added);
                // update local status elements
                this.currentVisibleWidgetIds.combine(diff.added);
                this.currentConditionalVisibleWidgetIds[type] = visibleWidgetIds;
            } else {
                // no new widgets to show, so just move the currently visible widgets
                this.moveVisibleWidgetsAfterConditionChanged(type, false);
            }
        },

        /**
         * Evaluate a condition with a registered function for the given type.
         * 
         * @param {String} type The type of the condition to evaluate
         * @param {Object} condition The condition to evaluate by the registered function
         * @param {Boolean} fallback The value to return if there is no function for the type
         */
        evaluateCondition: function(type, condition, fallback) {
            var callback = this.conditionEvaluators[type];
            if (callback) {
                return callback.call(null, condition);
            }
            return fallback;
        },

        /**
         * Parse the configuration and add the contained view definitions.
         * 
         * @param {Object} configuration The view definitions are expected to be provided as a
         *            mapping from viewId to view definition object. This mapping should be provided
         *            in the views member of the configuration object. A view definition object
         *            should have the following members: parentViewId, visibleWidgets,
         *            conditionalVisibleWidgets, previousViewAction and hiddenContainers. For a
         *            description of the members see documentation of addViewDefinition.
         */
        extractViewDefinitions: function(configuration) {
            var viewId, config;
            if (configuration.views) {
                for (viewId in configuration.views) {
                    if (configuration.views.hasOwnProperty(viewId)) {
                        config = configuration.views[viewId];
                        this.addViewDefinition(viewId, config.parentViewId, config.visibleWidgets,
                                config.conditionalVisibleWidgets, config.previousViewAction,
                                config.hiddenContainers);
                    }
                }
            }
        },

        extractWidgetConfigurations: function(configuration) {
            var widgetId, config;
            if (configuration.widgets) {
                for (widgetId in configuration.widgets) {
                    if (configuration.widgets.hasOwnProperty(widgetId)) {
                        config = configuration.widgets[widgetId];
                        this.addWidgetConfiguration(widgetId, config.widgetType,
                                config.containerSelector, config.conditionalContainerSelectors,
                                config.settings);
                    }
                }
            }
        },

        /**
         * Get the widget of the current view which is visible or hidden and has the same selector
         * and should be positioned after the given widget. The position is derived from the ordered
         * widget IDs array which was created by activating the view.
         * 
         * @param {String} selector The selector of the container the widget should be rendered in
         * @param {String} widgetId The ID of the widget for which the next widget should be found
         * @return {Widget} the found widget or null
         */
        findNextWidget: function(selector, widgetId) {
            var idx, i, l, nextWidgetId, nextWidget;
            idx = this.currentOrderedWidgetIds.indexOf(widgetId);
            l = this.currentVisibleWidgetIds.length;
            for (i = idx + 1; i < l; i++) {
                nextWidgetId = this.currentVisibleWidgetIds[i];
                // widgets might be listed more than once in ordered widgetIDs array, thus ignore
                // all widgets also listed earlier before the provided widget 
                if (selector === this.currentWidgetSelectors[nextWidgetId]
                        && this.currentOrderedWidgetIds.indexOf(nextWidgetId) > idx) {
                    nextWidget = this.widgetController.getWidget(nextWidgetId);
                    if (nextWidget) {
                        return nextWidget;
                    }
                }
            }
            return null;
        },

        findParentContainer: function(selector) {
            return selector ? this.baseElement.getElement(selector) : this.baseElement;
        },
        findParentContainerSelector: function(widgetConfig, type) {
            var i, l, conditionalSelector, selector;
            if (widgetConfig.conditionalSelectors) {
                l = widgetConfig.conditionalSelectors.length;
                for (i = 0; i < l; i++) {
                    conditionalSelector = widgetConfig.conditionalSelectors[i];
                    if ((!type || type === conditionalSelector.type)
                            && this.evaluateCondition(conditionalSelector.type,
                                    conditionalSelector.condition, false)) {
                        selector = conditionalSelector.selector;
                        break;
                    }
                }
            }
            if (!selector) {
                selector = widgetConfig.selector;
            }
            return selector;
        },

        /**
         * Get all widgetIds and hiddenContainers of a view definition including those of the parent
         * views of that view.
         * 
         * @param {Object} [viewDefinition] The view whose widgetIds and and hidden containers
         *            should be retrieved. If omitted the result will contain two empty arrays.
         * @return {Object} an object containing 2 arrays one containing the widgetIds and one the
         *         selectors of the hiddenContainers.
         */
        getAllWidgetsAndHiddenContainers: function(viewDefinition) {
            var i, l, type;
            var conditionalWidgetIds = {};
            var result = {
                widgetIds: [],
                hiddenContainers: [],
                conditionalWidgetIds: conditionalWidgetIds,
                orderedWidgetIds: []
            };
            while (viewDefinition) {
                result.widgetIds.combine(viewDefinition.widgetIds);
                result.orderedWidgetIds.append(viewDefinition.widgetIds);
                if (viewDefinition.conditionalWidgets) {
                    for (i = 0, l = viewDefinition.conditionalWidgets.length; i < l; i++) {
                        result.orderedWidgetIds
                                .append(viewDefinition.conditionalWidgets[i].visibleWidgets);
                        type = viewDefinition.conditionalWidgets[i].type;
                        if (this.evaluateCondition(type,
                                viewDefinition.conditionalWidgets[i].condition, true)) {
                            if (!conditionalWidgetIds[type]) {
                                conditionalWidgetIds[type] = [];
                            }
                            // save conditional widgets per type
                            conditionalWidgetIds[type]
                                    .append(viewDefinition.conditionalWidgets[i].visibleWidgets);
                            // add all widgets to show, avoiding duplicates
                            result.widgetIds
                                    .combine(viewDefinition.conditionalWidgets[i].visibleWidgets);
                        }
                    }
                }
                result.hiddenContainers.combine(viewDefinition.hiddenContainers);
                if (viewDefinition.parentViewId) {
                    viewDefinition = this.viewDefinitions[viewDefinition.parentViewId];
                } else {
                    break;
                }
            }
            return result;
        },

        /**
         * Get the action for handling the widgets of the previous view which are not the new view.
         * This will take the parent views into account if they changed too. The returned action
         * will be the strongest found in the definition itself or in one of its parents. The
         * 'remove' action is considered stronger than the 'hide' action.
         * 
         * @param {Object} [oldDefinition] The definition of the current view which will be replaced
         *            with the new definition. Can be omitted if there is no current view.
         * @param {Object} newDefinition The definition of the view to be activated.
         * @return {String} The action which will be either 'hide' or 'remove'
         */
        getPreviousViewAction: function(oldDefinition, newDefinition) {
            var viewDefinition;
            var action = newDefinition.previousViewAction;
            var oldDefParentViewId = oldDefinition && oldDefinition.parentViewId;
            var newDefParentViewId = newDefinition.parentViewId;
            // if there is an old definition check actions of parent views in case there is one and
            // the parent view changed. In case one view defines that the widgets should be removed
            // stop since this is the strongest action.
            while (action != 'remove' && newDefParentViewId
                    && newDefParentViewId != oldDefParentViewId) {
                viewDefinition = this.viewDefinitions[newDefParentViewId];
                action = viewDefinition.previousViewAction;
                newDefParentViewId = viewDefinition.parentViewId;
                if (oldDefParentViewId) {
                    oldDefParentViewId = this.viewDefinitions[oldDefParentViewId].parentViewId;
                }
            }
            return action;
        },

        /**
         * Hide or remove the provided widgets.
         * 
         * @param {String[]} widgetIds Array of widget IDs to hide or remove.
         * @param {String} action The action which can be one of 'hide' or 'remove'
         */
        hideOrRemoveWidgets: function(widgetIds, action) {
            var i, widget;
            var hide = (action == 'hide');
            for (i = 0; i < widgetIds.length; i++) {
                widget = this.widgetController.getWidget(widgetIds[i]);
                if (widget) {
                    if (hide) {
                        widget.hide();
                        this.currentHiddenWidgetIds.push(widgetIds[i]);
                    } else {
                        this.widgetController.removeWidget(widget);
                        delete this.currentWidgetSelectors[widgetIds[i]];
                    }
                }
            }
        },

        moveVisibleWidgetAfterConditionChanged: function(type, widgetId) {
            var i, l, widgetConfig, conditionalSelector, selector, nextWidget, widget;
            widgetConfig = this.widgetConfigurations[widgetId];
            if (widgetConfig && widgetConfig.conditionalSelectors) {
                // if there is a matching conditional selector, get the selector and move
                // widget if container changed
                for (i = 0, l = widgetConfig.conditionalSelectors.length; i < l; i++) {
                    conditionalSelector = widgetConfig.conditionalSelectors[i];
                    if (conditionalSelector.type == type) {
                        selector = this.findParentContainerSelector(widgetConfig, type);
                        if (selector !== this.currentWidgetSelectors[widgetId]) {
                            widget = this.widgetController.getWidget(widgetId);
                            nextWidget = this.findNextWidget(selector, widgetId);
                            if (nextWidget) {
                                widget.domNode.inject(nextWidget.domNode, 'before');
                            } else {
                                this.findParentContainer(selector).grab(widget.domNode);
                            }
                            this.currentWidgetSelectors[widgetId] = selector;
                        }
                        return;
                    }
                }
            }
        },
        moveVisibleWidgetsAfterConditionChanged: function(type, widgetIdsToShow) {
            var i, l, widgetId;
            for (i = 0, l = this.currentVisibleWidgetIds.length; i < l; i++) {
                widgetId = this.currentVisibleWidgetIds[i];
                this.moveVisibleWidgetAfterConditionChanged(type, widgetId);
            }
            if (widgetIdsToShow) {
                for (i = 0, l = widgetIdsToShow.length; i < l; i++) {
                    widgetId = widgetIdsToShow[i];
                    // ignore non existing widgets as they will be created inside the right parent container
                    if (this.widgetController.getWidget(widgetId)) {
                        this.moveVisibleWidgetAfterConditionChanged(type, widgetId);
                    }
                }
            }
        },

        /**
         * Register a function for evaluating conditions of a certain type. The registered
         * evaluation functions will be used when deciding whether a conditional widget should be
         * visible or where a widget should be rendered when there are conditionalSelectors for that
         * widget.
         * 
         * @param {String} type The type the condition function can handle.
         * @param {Function} evalFunction A function that will be passed the condition to evaluate.
         *            The function should return true if the condition is met false otherwise.
         */
        registerConditionEvaluator: function(type, evalFunction) {
            this.conditionEvaluators[type] = evalFunction;
        },

        /**
         * Show or create the provided widgets.
         * 
         * @param {String[]} widgetIds Array of widget IDs to show or create.
         */
        showOrCreateWidgets: function(widgetIds) {
            var i, widget, widgetId, config, selector, parentContainer;
            for (i = 0; i < widgetIds.length; i++) {
                widgetId = widgetIds[i];
                // if widget exists show it, otherwise create it
                widget = this.widgetController.getWidget(widgetId);
                if (widget) {
                    widget.show();
                    this.currentHiddenWidgetIds.erase(widgetId);
                } else {
                    config = this.widgetConfigurations[widgetId];
                    if (config) {
                        selector = this.findParentContainerSelector(config);
                        this.currentWidgetSelectors[widgetId] = selector || false;
                        parentContainer = this.findParentContainer(selector);
                        if (parentContainer) {
                            this.widgetController.addWidget(parentContainer, config.type, widgetId,
                                    config.settings);
                        }
                    }
                }
            }
        },

        /**
         * Show or hide containers that are children of the base element managed by this view
         * manager. This method checks for the hiddenCssClass option and if defined uses it for
         * hiding the containers otherwise the display style will be set.
         * 
         * @param {String[]} selectors The selectors of the child elements
         * @param {boolean} show Whether to show or hide the containers
         */
        showOrHideContainers: function(selectors, show) {
            var action, elem, i;
            var args = [];
            if (show) {
                if (this.options.hiddenCssClass) {
                    action = 'removeClass';
                    args.push(this.options.hiddenCssClass);
                } else {
                    action = 'setStyle';
                    args.push('display');
                    args.push('');
                }
            } else {
                if (this.options.hiddenCssClass) {
                    action = 'addClass';
                    args.push(this.options.hiddenCssClass);
                } else {
                    action = 'setStyle';
                    args.push('display');
                    args.push('none');
                }
            }
            for (i = 0; i < selectors.length; i++) {
                elem = this.baseElement.getElement(selectors[i]);
                if (elem) {
                    elem[action].apply(elem, args);
                }
            }
        },

        /**
         * Remove a registered condition evaluation function for the given type.
         * 
         * @param {String} type The type of the condition function to remove
         */
        unregisterConditionEvaluator: function(type) {
            delete this.conditionEvaluators[type];
        }

    });

    if (namespace && namespace.addConstructor) {
        namespace.addConstructor('ViewManager', ViewManager);
    } else {
        window.ViewManager = ViewManager;
    }
})(window.runtimeNamespace);