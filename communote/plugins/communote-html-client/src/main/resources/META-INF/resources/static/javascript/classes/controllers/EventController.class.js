/**
 * @class
 * @augments communote.widget.classes.controllers.Controller
 */
communote.widget.classes.controllers.EventController = communote.widget.classes.controllers.Controller.extend(
/** 
 * @lends communote.widget.classes.controllers.EventController.prototype
 */		
{
    name: "EventController",
    listeners: {},

    /**
     * register an event on an element
     * 
     * @param eventName
     * @param channel
     * @param listener
     * @param listenerFunction
     */
    registerListener: function(eventName, channel, listener, listenerFunction) {
        var id = listener.channel;
        var listeners = this.listeners;
        if (listenerFunction == undefined){listenerFunction = eventName;}
        if (listeners[eventName] == undefined){listeners[eventName] = {};}
        if (listeners[eventName][channel] == undefined){listeners[eventName][channel] = {};}
        if (listeners[eventName][channel][id] == undefined){listeners[eventName][channel][id] = {
	    listener: listener,
	    channel: channel,
	    listenerFunction: listenerFunction
	};}
    },

    /**
     * an event is firing
     * 
     * @param eventName
     * @param channel
     * @param data
     * @param from
     */
    fireEvent: function(eventName, channel, data, from) {
        var listeners, list, item, listener, func, j;
        var $ = communote.jQuery;
        // iterate over channels if array
        if ($.isArray(channel)) {
            for (var i = 0; i < channel.length; i++) {
                this.fireEvent(eventName, channel[i], data, from);
            }
            return;
        }
        
        if (channel == undefined){channel = "global";}
        if (!from){from = {
	    template: undefined
	};}
        if (data == undefined){data = {};}
        listeners = this.listeners;
        data.fromControl = from;
        if (listeners[eventName] && listeners[eventName][channel]) {
            list = listeners[eventName][channel];
            for (j in list) {
                item = list[j];
                if (item !== undefined) {
                    listener = item.listener;
                    func = item.listenerFunction;
                    if (typeof listener[func] === "function") {
                        try {
                            listener[func](data);
                        } catch (e) {
                            var msg = communote.utils.printError("fireEvent: " +  eventName
                                    + "; channel: " + channel + "; function: " + func, e);
                            listener.fireEvent('error', listener.channel, {
                                message: 'internal error:<br />' + communote.utils.nl2br(msg)
                            });
                        }
                    } else {
                        msg = communote.utils.printError("fireEvent: " +  eventName
                                + "; channel: " + channel + "; function: " + func);
                    }
                }
            }
        }
    },

    unregisterListener : function(eventName, channel, listener) {
        var list = this.listeners;
        if (list[eventName]) {
            if (list[eventName][channel]) {
                if (list[eventName][channel][listener.channel]) {
                    delete list[eventName][channel][listener.channel];
                }
            }
        }
    },
    
    /**
     * unregisters all listeners for the given channel and all events.
     */
    unregisterChannel: function(channel) {
        var eventName;
        // iterate all events
        for (eventName in this.listeners) {
            // delete specified channel for each event
            delete this.listeners[eventName][channel];
        }
    },
    
    
    /**
     * Call this function via the console:
     * communote.widget.EventController.showListenerStatistics();
     * 
     * By doing so multiple times (after interaction) you get information about 
     * possible memory leaks (increasing listener counts indicate not destroyed Controls)
     * The statistics is printed out to the console.
     */
    showListenerStatistics: function() {
        var eventName;
        var statistics = {};
        for (eventName in this.listeners) {
            statistics[eventName] = Object.keys(this.listeners[eventName]).length;
        }
        
        var sum = 0;
        var out = "";
        for (eventName in statistics) {
            
            out += eventName +": "+ statistics[eventName] + "\n";
            sum += statistics[eventName];
        }
        
        out += "--------------------------------------\n";
        out += "Total count: " + sum;
        
        console.log(out);
    }
});