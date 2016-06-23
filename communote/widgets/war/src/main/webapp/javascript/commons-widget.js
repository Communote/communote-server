/**
 * This method distributes the given event via calling E2(event, null, params)
 * 
 * @param event
 *            The event which occured
 * @param params
 *            Parameters.
 * @return The return value of E2(..)
 */
function E(event, params) {
    return E2(event, null, params);
}

/**
 * This method distributes the given event.
 * 
 * @param event
 *            The event which occurred
 * @param senderWidgetId
 *            Id of the sender
 * @param params
 *            Parameters.
 * @return false
 */
function E2(event, senderWidgetId, params) {
    widgetController.sendEvent(event, senderWidgetId, params);
    return false;
}

/**
 * Processes an event by dispatching it to the registered handlers that claim to handle it.
 * @param {String} eventName The name of the event.
 * @param {String[]} [targetGroups] A collection of filter group IDs that should receive
 *  the event. If undefined all groups are addressed.
 * @param {Object} params The parameters to be passed to the handler.
 * @param {Object|Object[]} [details] Some additional data to be stored for later use. See KeyValueStore#put for
 *  documentation.
 * @return {boolean} true if the event caused a change of parameters of a filter parameter store
 */
function E2G(eventName, targetGroups, params, details) {
    return widgetController.getFilterEventProcessor().processEvent(eventName, targetGroups, params, details);
}
