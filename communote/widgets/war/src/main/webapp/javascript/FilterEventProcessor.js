var C_FilterEventProcessor = new Class( /** @lends C_FilterEventProcessor.prototype */
{

    store: null,
    /**
     * Maps event names to FilterEventHandlers that were registered for the event.
     */
    eventHandlerMapping: {},
    /**
     * stores the registered event handlers
     */
    registeredEventHandlers: {},

    /**
     * Create a new processor.
     * 
     * @param {C_KeyValueStore} [store] A store for saving the additional data passed to
     *            processEvent.
     * @constructs
     * @class A class that processes all filter events by dispatching it to the registered
     *        {@link C_FilterEventHandler}. At runtime there is usually only one instance of this
     *        class.
     */
    initialize: function(store) {
        this.store = store;
    },

    /**
     * Register an EventHandler if not yet existing. The handler belongs to a group and will only be
     * informed about events send to the group.
     * 
     * @param {C_FilterEventHandler} handler The handler to be registered.
     */
    registerEventHandler: function(handler) {
        var i, eventName, handledEvents;
        var groupId = handler.getFilterGroupId();
        if (this.registeredEventHandlers[groupId]) {
            return;
        }
        this.registeredEventHandlers[groupId] = handler;
        handledEvents = handler.getHandledEvents();
        for (i = 0; i < handledEvents.length; i++) {
            eventName = handledEvents[i];
            if (!this.eventHandlerMapping[eventName]) {
                this.eventHandlerMapping[eventName] = [];
            }
            this.eventHandlerMapping[eventName].push(handler);
        }
    },

    /**
     * Return a registered event handler.
     * 
     * @param {String} id The identifier of the filter widget group of the handler to retrieve.
     * @return {C_FilterEventHandler} the event handler or undefined if not found
     */
    getEventHandler: function(id) {
        return this.registeredEventHandlers[id];
    },

    /**
     * Process an event by dispatching it to the registered handlers that claim to handle it.
     * 
     * @param {String} eventName The name of the event.
     * @param {String[]} [targetGroups] A collection of filter group IDs that should receive the
     *            event. If undefined all groups are addressed.
     * @param {Object} params The parameters to be passed to the handler.
     * @param {Object|Object[]} [details] Some additional data to be cached. See KeyValueStore#put
     *            for documentation. The data will only be stored if a store was passed to the
     *            constructor.
     * @return {boolean} true if the event caused a change of parameters of a filter parameter store
     */
    processEvent: function(eventName, targetGroups, params, details) {
        var i, handlers, paramsChanged;
        if (details != null && this.store) {
            if (typeOf(details) == 'array') {
                for (i = 0; i < details.length; i++) {
                    this.store.put(details[i]);
                }
            } else {
                this.store.put(details);
            }
        }
        handlers = this.eventHandlerMapping[eventName];
        paramsChanged = false;
        if (handlers) {
            for (i = 0; i < handlers.length; i++) {
                if (!targetGroups || targetGroups.contains(handlers[i].getFilterGroupId())) {
                    if (handlers[i].handleEvent(eventName, params)) {
                        paramsChanged = true;
                    }
                }
            }
        }
        return paramsChanged;
    }
});