var C_FilterEventHandler = new Class( /** @lends C_FilterEventHandler.prototype */
{

    Implements: Options,

    filterGroup: null,
    filterParameterStore: null,

    options: {},

    /**
     * Create an event handler for a given {@link FilterGroup}.
     * 
     * @param {FilterGroup} filterGroup The FilterGroup for which the EventHandler should handle
     *            events
     * @param {Object} [options] Options to be merged with the default options
     * @constructs
     * @class Base class for all event handlers. An event handler handles filter events for the
     *        members (usually widgets) of an associated {@link FilterGroup}.
     */
    initialize: function(filterGroup, options) {
        this.setOptions(options);
        this.setFilterGroup(filterGroup);
    },

    /**
     * Set the filterGroup if not yet set.
     */
    setFilterGroup: function(filterGroup) {
        if (!this.filterGroup && filterGroup) {
            this.filterGroup = filterGroup;
            // reference to filter parameter store for faster access
            this.filterParameterStore = filterGroup.getParameterStore();
        }
    },
    
    /**
     * Returns the associated FilterGroup.
     * 
     * @return {FilterGroup} The group.
     */
    getFilterGroup: function() {
        return this.filterGroup;
    },

    /**
     * Returns the ID of the associated FilterGroup.
     * 
     * @return {String} The ID of the group.
     */
    getFilterGroupId: function() {
        return this.filterGroup.id;
    },

    /**
     * Returns the names of the handled events. Subclasses should combine their events with those
     * returned here, to get support for the default events.
     * 
     * @return {String[]} A collection with parameter names.
     */
    getHandledEvents: function() {
        return [ 'onReset', 'onReplace' ];
    },

    /**
     * Handle an event sent to the associated {@link FilterGroup} of this handler. This
     * implementation calls a method named like the event and invokes the #informListener method of
     * the associated filter parameter store for the returned collection of the changed parameter
     * names. In case there is no function named like the event nothing will happen.
     * 
     * @param {String} eventName The name of the event (e.g. 'onBlogClick').
     * @param {String|Number|Array} [params] Some additional parameters.
     * @return {boolean} true if the event resulted in a change of the parameters, false otherwise
     */
    handleEvent: function(eventName, params) {
        var changedParams, listeners;
        var fn = this[eventName];
        // don't inform listeners that are added by the handler to avoid changed events although
        // nothing changed in the perspective of the handler (e.g. if FilterWidgets are added by
        // the handler).
        listeners = this.filterParameterStore.listener.slice(0);
        if (fn && typeOf(fn) == 'function') {
            changedParams = fn.call(this, params);
            if (changedParams) {
                this.filterParameterStore.informListener(changedParams, listeners);
                return true;
            }
        }
        return false;
    },

    /**
     * Handler for onReset event which can be used to reset filter parameters.
     * 
     * @param {String|String[]} [args] An array of at most two elements where the first denotes the
     *            name of the filter parameter to reset and second the value to be reset. The second
     *            entry is only useful for parameters with array values and allows removing a
     *            specific value. In case args is undefined all parameters will be reset.
     * @return {String[]} The changed parameters.
     */
    onReset: function(args) {
        var param, value;
        if (args && args.length > 0) {
            param = args[0];
            value = args[1];
            if (this.filterParameterStore.unsetFilterParameter(param, value)) {
                return [ param ];
            }
        } else {
            return this.filterParameterStore.resetCurrentFilterParameters();
        }
    },

    /**
     * Handler for onReplace event which can be used to completely replace the current resetable and
     * unresetable parameters.
     * 
     * @param {Object} [args] An object with the members 'unresetableParams' that holds the new
     *            unresetable parameters and 'filterParams' which contains the new filter
     *            parameters. Both members can be omitted if only the current parameters should be
     *            removed and no new parameters should be set.
     * @return {String[]} The changed parameters.
     */
    onReplace: function(args) {
        return this.filterParameterStore.replaceCurrentParameters(args.filterParams,
                args.unresetableParams);
    }

});