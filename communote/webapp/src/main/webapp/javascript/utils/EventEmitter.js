(function() {
    function EventListener(fn, context) {
        this.fn = fn;
        this.context = context || null;
        this.removed = false;
    }

    function EventEmitter() {
        this.events = {};
    }

    /**
     * Emit the event to all listeners.
     * 
     * @param {String} eventName The name of the event to emit.
     * @param {*} [eventData] Any data to pass to the listener function.
     * @return {boolean} weather a listener was called
     */
    EventEmitter.prototype.emit = function(eventName, eventData) {
        var i, l, listener;
        var listeners = this.events[eventName];
        if (!listeners) {
            return false;
        }
        for (i = 0, l = listeners.length; i < l; i++) {
            listener = listeners[i];
            if (!listener.removed) {
                listener.fn.call(listener.context, eventData);
            }
        }
        return true;
    }

    /**
     * Remove matching listeners.
     * 
     * @param {String} eventName The name of the event for which the listeners should be removed.
     * @param {Function} [fn] The listener function to remove. If omitted, all listeners will be
     *            removed.
     * @param {*} [context] The this context to remove only listeners with a specific context. If
     *            undefined, matching listeners with any context are removed.
     * @returns {EventEmitter} this instance
     */
    EventEmitter.prototype.off = function(eventName, fn, context) {
        var i, l, anyContext, listener, cleanedListeners;
        var listeners = this.events[eventName];
        if (!listeners) {
            return this;
        }
        anyContext = context === undefined;
        if (!fn && anyContext) {
            delete this.events[eventName];
        } else {
            // copy to not interfere with emit if remove was caused by emit
            cleanedListeners = [];
            for (i = 0, l = listeners.length; i < l; i++) {
                listener = listeners[i];
                if (listener.fn !== fn || (!anyContext && listener.context !== context)) {
                    cleanedListeners.push(listener);
                } else {
                    // mark as removed so it is not called if remove was caused by emit
                    listener.removed = true;
                }
            }
            if (cleanedListeners.length) {
                this.events[eventName] = cleanedListeners;
            } else {
                // no listeners left for the event
                delete this.events[eventName];
            }
        }
        return this;
    };

    /**
     * Add a listener for a given event.
     * 
     * @param {String} eventName The name of the event.
     * @param {Function} fn The listener function.
     * @param {*} [context] The this context to call the listener function with.
     * @returns {EventEmitter} this instance
     */
    EventEmitter.prototype.on = function(eventName, fn, context) {
        var listener = new EventListener(fn, context);
        var listeners = this.events[eventName];
        if (!listeners) {
            listeners = [];
            this.events[eventName] = listeners;
        }
        listeners.push(listener);
        return this;
    };
    // some commonly used aliases
    EventEmitter.prototype.removeListener = EventEmitter.prototype.off;
    EventEmitter.prototype.addListener = EventEmitter.prototype.on;

    communote.classes.EventEmitter = EventEmitter;
})();