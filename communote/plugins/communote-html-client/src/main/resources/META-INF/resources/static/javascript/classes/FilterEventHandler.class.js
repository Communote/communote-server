/**
 * Handler for events of name 'filterEvent' that are fired on the widget channel. This handler is
 * associated with a FilterParameterStore and will update it when it receives a 'filterEvent'. If a
 * 'filterEvent' leads to a change in the current filter parameters a 'filterChanged' event will be
 * fired on the widget channel.
 */
communote.widget.classes.FilterEventHandler = communote.Base.extend(
/** @lends communote.widget.classes.FilterEventHandler.prototype */	
{
    paramStore: null,
    singleValueParams: null,
    jQuery: null,
    eventController: null,
    channel: null,

    /**
     * @constructs
     * Creates a new FilterEventHandler
     * 
     * @param {FilterParameterStore} filterParamStore The FilterParameterStore this handler
     *            should update
     * @param {Array} singelValueParams Array of filter parameter names that represent
     *            filters that should only have a single value (like a text search). Can be
     *            null.
     * @param {String} channel The channel to listen for filterEvent events and to fire
     *            filterChanged events
     */
    constructor: function(filterParamStore, singleValueParams, channel) {
        this.paramStore = filterParamStore;
        this.singleValueParams = singleValueParams;
        this.jQuery = communote.jQuery;
        this.eventController = communote.widget.EventController;
        this.channel = channel;
        this.eventController.registerListener('filterEvent', channel, this, 'handleFilterEvent');
    },

    /**
     * Handles a filterEvent event.
     * 
     * The data object holds details about the filter parameter to modify. The data object
     * is expected to have the members 'paramName', 'value', 'added' and 'label'. The
     * 'paramName' represents the name of the filter parameter to modify. If this value is
     * not contained, all filter parameters will be unset. In that case the other parameters
     * aren't necessary. The 'value' represents the filter parameter value to set or remove.
     * If not contained the filter parameter will be unset. The boolean member 'added'
     * denotes whether the filer parameter should be added or removed. The 'label' parameter
     * is optional and holds a human readable representation of the filter parameter to give
     * some user feedback, for instance in a filter summary.
     * 
     * This method will delegate to a local method named <data.paramName>Changed if
     * available.
     * 
     * The 'filterChanged' event will be sent with a data object that can be an array of the
     * changed filter parameter names or an object with the same members as passed to this
     * function.
     * 
     * @param {Object} data an object as described above
     */
    handleFilterEvent: function(data) {
        var handlerName, newEventData, paramName;
        paramName = data.paramName;
        if (!paramName) {
            // special case: reset all filter parameter
            newEventData = this.paramStore.resetCurrentFilterParameters();
        } else {
            handlerName = paramName + 'Changed';
            if (this.jQuery.isFunction(this[handlerName])) {
                newEventData = this[handlerName](data);
            } else {
                newEventData = this.defaultHandler(data);
            }
        }
        if ((newEventData != null)
                && (!this.jQuery.isArray(newEventData) || (newEventData.length > 0))) {
            // inform other components about changed filter parameters
            this.eventController.fireEvent('filterChanged', this.channel, newEventData,
                    true);
        }
    },
    /**
     * Default handler that is called when there is no custom handler for the filter
     * parameter name
     * 
     * @param {Object} data The data passed to handleFilterEvent
     * @return {Object} the data to be passed to the filterChanged event or null if nothing
     *         changed
     */
    defaultHandler: function(data) {
        var changed, newEventData, paramName, value;
        changed = false;
        paramName = data.paramName;
        value = data.value;
        // if the value is null unset the filter parameter
        if (value == null) {
            changed = this.paramStore.unsetFilterParameter(paramName);
        } else {
            // if a single value parameter just set new value 
            if (this.singleValueParams && this.jQuery.inArray(paramName, this.singleValueParams) !== -1) {
                if (data.added === false) {
                    changed = this.paramStore.unsetFilterParameter(paramName);
                } else {
                    changed = this.paramStore.setFilterParameter(paramName, value);
                }
            } else {
                // ensure the value is a string because operations are type sensitive
                value = value.toString();
                // check if there is a hint about the filter operation
                if (data.added === false) {
                    changed = this.paramStore.unsetFilterParameter(paramName, value);
                } else {
                    changed = this.paramStore.appendFilterParameter(paramName, value,
                            data.added == null);
                }
            }
        }
        if (changed) {
            newEventData = {};
            newEventData.paramName = paramName;
            newEventData.value = value;
            newEventData.added = value == null ? false : data.added;
            newEventData.label = data.label;
        }
        return newEventData;
    },
    
    viewFilterChanged: function(data) {
        var currentViewFilter, newViewFilter, paramStore, changed;
        // null, 'all' and unknown view names are equivalent and lead to 
        // removal of the current view parameter
        switch (data.value) {
            case 'following':
                newViewFilter = 'showFollowedItems';
                break;
            case 'me':
                newViewFilter = 'showNotesForMe';
                break;
            case 'favorites':
                newViewFilter = 'showFavorites';
                break;
            default: 
                newViewFilter = null;
        }
        // get current view filter
        paramStore = this.paramStore;
        if (paramStore.getUnresetableFilterParameter('showFollowedItems')) {
            currentViewFilter = 'showFollowedItems';
        } else if (paramStore.getUnresetableFilterParameter('showNotesForMe')) {
            currentViewFilter = 'showNotesForMe';
        } else if (paramStore.getUnresetableFilterParameter('showFavorites')) {
            currentViewFilter = 'showFavorites';
        }
        if (newViewFilter && currentViewFilter !== newViewFilter) {
            if (currentViewFilter) {
                paramStore.unsetUnresetableFilterParameter(currentViewFilter);
            }
            paramStore.setUnresetableFilterParameter(newViewFilter, true);
            changed = true;
        } else if (!newViewFilter && currentViewFilter) {
            paramStore.unsetUnresetableFilterParameter(currentViewFilter);
            changed = true;
        }
        if (changed) {
            return {
                paramName: data.paramName,
                value: newViewFilter ? data.value : 'all',
                added: true,
                label: data.label
            };
        }
        return null;
    }
});