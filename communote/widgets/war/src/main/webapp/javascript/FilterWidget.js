/**
 * @class A widget that belongs to a {@link FilterGroup}. It will automatically include the current
 *        filter parameters stored in the C_FilterParameterStore of the filter widget group when
 *        doing a refresh.
 * @extends C_Widget
 * @extends C_FilterParameterListener
 */
var C_FilterWidget = new Class( /** @lends C_FilterWidget.prototype */
{
    Extends: C_Widget,
    Implements: C_FilterParameterListener,

    /**
     * The filter widget group this widget belongs to.
     * 
     * @type String
     */
    filterWidgetGroup: null,
    /**
     * An array of filter parameter names to be used when the widget request is rendered. Use this
     * member to not include all of the parameters of the associated filter parameter store. This
     * member takes precedence over filterParamsToExclude, that is if both are defined
     * filterParamsToExclude is ignored.
     * 
     * @type String[]
     * @default null
     */
    filterParamsSubset: null,
    /**
     * An array of filter parameter names to exclude when the widget request is rendered. Use this
     * member to not include all of the parameters of the associated filter parameter store.
     * 
     * @type String[]
     * @default null
     */
    filterParamsToExclude: null,
    filterParamStore: null,
    defaultFilterWidgetGroupRepoName: 'filterWidgetGroupRepo',
    /**
     * Array with names of filter parameters that should be observed. If the value of one of the
     * listed parameters changes in the store of the {@link FilterGroup} the callback
     * {@link #filterParametersChanged} will be invoked. For easy modification the
     * observedFilterParameters setting whose value is expected to be an array or a CSV string can
     * be used. Any parameter listed in that array will be added. Parameters prefixed with '-' will
     * be removed. The special parameter '-*' will remove all existing parameters before setting the new ones.
     * 
     * @type String[]
     */
    observedFilterParams: [],

    /**
     * @override
     * @ignore
     */
    init: function() {
        var filterWidgetGroupId, filterWidgetGroupRepoName, widgetGroupRepo, observedParams, i, skipRemove;
        this.parent();
        filterWidgetGroupId = this.staticParams.filterWidgetGroupId;
        // if the groupId was explicitly set to false the widget should not be used as FilterWidget
        // This is useful for cases where a widget can also be used as a normal widget.
        if (filterWidgetGroupId === false) {
            return;
        }
        filterWidgetGroupRepoName = this.staticParams.filterWidgetGroupRepositoryName;
        if (!filterWidgetGroupRepoName) {
            filterWidgetGroupRepoName = this.defaultFilterWidgetGroupRepoName;
        }
        widgetGroupRepo = window[filterWidgetGroupRepoName];
        if (!widgetGroupRepo) {
            throw 'widget group repository ' + filterWidgetGroupRepoName + ' not defined';
        }
        this.filterWidgetGroup = widgetGroupRepo[filterWidgetGroupId];
        if (!this.filterWidgetGroup) {
            throw 'widget group ' + filterWidgetGroupId + ' not defined';
        }
        this.filterParamStore = this.filterWidgetGroup.getParameterStore();
        this.initChangedParamsHandler();
        this.filterWidgetGroup.addMember(this);
        observedParams = this.getStaticParameter('observedFilterParameters');
        if (observedParams) {
            if (typeof observedParams === 'string') {
                observedParams = observedParams.split(',');
            }
            if (observedParams.contains('-*')) {
                this.observedFilterParams = [];
                skipRemove = true;
            }
            for (i = 0; i < observedParams.length; i++) {
                if (observedParams[i].charAt(0) === '-') {
                    if (!skipRemove) {
                        this.observedFilterParams.erase(observedParams[i].substring(1));
                    }
                } else {
                    this.observedFilterParams.include(observedParams[i]);
                }
            }
        }
    },

    /**
     * @override
     * @ignore
     */
    beforeRemove: function() {
        if (this.filterWidgetGroup) {
            this.filterWidgetGroup.removeMember(this);
        }
    },

    /**
     * <p>
     * Implementation of the {@link C_FilterParameterListener#filterParametersChanged} method which
     * refreshes the widget.
     * </p>
     * <p>
     * To get another behavior subclasses should implement one of the FilterParamsHandlerStrategy*
     * classes.
     * </p>
     * 
     * @param {String[]} changedParams A collection of the names of the changed parameters
     * @override
     */
    filterParametersChanged: function(changedParams) {
        this.refresh();
    },

    /**
     * Implementation of {@link C_FilterParameterListener#getObservedFilterParameters} method which
     * returns the member {link #observedFilterParams}.
     * 
     * @return {String[]} Array with filter parameter names to observe
     * @override
     */
    getObservedFilterParameters: function() {
        return this.observedFilterParams;
    },

    /**
     * Send an event to the FilterGroup of this widget.
     * 
     * @param {String} eventName The name of the event.
     * @param {Object} params The parameters to be passed to the handler.
     * @param {Object|Object[]} [details] Some additional data to be stored for later use. See
     *            KeyValueStore#put for documentation.
     * @return {boolean} true if the event caused a change of parameters of a filter parameter store
     */
    sendFilterGroupEvent: function(eventName, params, details) {
        return this.widgetController.getFilterEventProcessor().processEvent(eventName,
                [ this.filterWidgetGroup.id ], params, details);
    },

    /**
     * Overridden to include the current parameters of the associated filter parameter store.
     * 
     * @override
     * @ignore
     */
    getRefreshParameters: function() {
        var params;
        if (this.filterWidgetGroup) {
            params = this.filterParamStore.getCurrentFilterParameters(this.filterParamsSubset,
                    this.filterParamsToExclude);
        } else {
            params = {};
        }
        // filterstore parameters are unlinked so we can merge it
        Object.merge(params, this.parent());
        return params;
    }
});