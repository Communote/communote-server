(function(window) {
    var namespace, commandHandlers, requestHandlers, handlersToAddAfterLoad, delayedHostMessages;
    var delayedMessages, EmbedMessenger;
    var serializeToString = false;
    var loaded = false;
    var ready = false;
    // check for postMessage support and IFrame
    // TODO actually a 'function' typeof test would be the right way, but IE8 returns 'object' (and
    // the object has no members (call, apply,...)!), although it can be used like a function, weird
    if (window.parent == window || !window.postMessage) {
        return;
    }
    if (!window.communote.embed) {
        window.communote.embed = {};
    }
    namespace = window.communote.embed;
    // detect whether we can send objects (serialized by structured clone algorithm) or only strings
    try {
        window.postMessage({
            toString: function() {
                serializeToString = true;
            }
        }, "*");
    } catch (e) {
        // when structured clone algorithm serialization is supported, an exception will be thrown if a function is encountered
    }

    /**
     * Handle a message that was sent by the host. This function will delegate to actual handler for
     * the message and message type.
     */
    function handleHostMessage(message) {
        var handler;
        var payload = message.payload;
        if (!loaded) {
            // delay if not yet loaded
            if (handlersToAddAfterLoad[message.message]
                    && handlersToAddAfterLoad[message.message].type === message.type) {
                delayedHostMessages.push(message);
                return;
            }
        }
        if (message.type === 'command') {
            handler = commandHandlers[message.message];
            handler.call(null, message.instanceId, iframeElem, payload);
        } else if (message.type === 'request') {
            // check for a request handler
            handler = requestHandlers[message.message];
            if (handler) {
                handler.call(null, payload.id, payload.data, requestHandlerComplete);
            } else {
                // send a special error response message
                sendResponseMessage(payload.id, 'MESSENGER_ERROR', 'no handler for request '
                        + message.message);
            }
        }
    }

    /**
     * Callback that is invoked when the page was loaded or the application initializer reached the
     * application-ready state.
     */
    function onLoadCallback() {
        var handlerName, handlerDef, callback, i, l;
        loaded = true;
        // add all handlers
        for (handlerName in handlersToAddAfterLoad) {
            handlerDef = handlersToAddAfterLoad[handlerName];
            callback = handlerDef.getHandlerCallback.call();
            // if no handler callback is returned do not register
            if (callback) {
                // TODO add other types
                if (handlerDef.type === 'request') {
                    namespace.messenger.addRequestHandler(handlerName, callback);
                }
            }
        }
        // run the delayed messages
        for (i = 0, l = delayedHostMessages.length; i < l; i++) {
            handleHostMessage(delayedHostMessages[i]);
        }
        delayedHostMessages = null;
        handlersToAddAfterLoad = null;
        // TODO send protocol message 'load' to notify communication host?
    }

    /**
     * Callback that is invoked when the DOM is ready.
     */
    function onReadyCallback() {
        var i, l;
        ready = true;
        // notify that we are ready and whether we are in an authenticated context
        namespace.messenger.sendProtocolMessage('ready', !!window.communote.currentUser);
        for (i = 0, l = delayedMessages.length; i < l; i++) {
            sendMessage(delayedMessages[i]);
        }
        delayedMessages = null;
    }

    /**
     * Event handler for the post message events.
     */
    function postMessageReceiver(event) {
        var message;
        if (typeof event.data === 'string') {
            try {
                message = JSON.parse(event.data);
            } catch (e) {
                // ignore
            }
        } else {
            message = event.data;
        }
        // if the message is for this instance, handle it
        if (message && message.instanceId === namespace.instanceId) {
            if (event.origin === namespace.hostUrl) {
                if (window.parent == event.source) {
                    handleHostMessage(message);
                }
            }
        }
    }

    /**
     * Function to be called after a request handler completed the request. This function will send
     * a response message for the request to the host.
     */
    function requestHandlerComplete(requestId, success, data) {
        sendResponseMessage(requestId, success ? 'OK' : 'ERROR', data);
    }

    /**
     * Send a message with postMessage API and take care of stringification if necessary.
     */
    function sendMessage(message) {
        if (!ready) {
            delayedMessages.push(message);
            return;
        }
        message.instanceId = namespace.instanceId;
        if (serializeToString) {
            message = JSON.stringify(message);
        }
        window.parent.postMessage(message, namespace.hostUrl);
    }

    function sendResponseMessage(requestId, messageText, data) {
        var message = {
            type: 'response',
            message: messageText,
            payload: {
                id: requestId,
                data: data
            }
        };
        sendMessage(message);
    }

    if (!namespace.requestParameters) {
        namespace.requestParameters = communote.utils.getObjectFromQueryString(communote.utils
                .extractParameterString(), 'first');
    }

    // in cases where the hosting site also listens for post-message events but does not support
    // object messages it might be necessary to force sending string messages to the hosting site
    if (namespace.requestParameters.embedMessengerForceString) {
        serializeToString = true;
    }

    if (!namespace.hostUrl) {
        namespace.hostUrl = namespace.requestParameters.embedHostUrl;
    }
    if (!namespace.instanceId) {
        namespace.instanceId = namespace.requestParameters.embedInstanceId;
    }

    commandHandlers = {};
    requestHandlers = {};
    handlersToAddAfterLoad = {};
    // collects messages received from the host part of the messenger while not yet loaded
    delayedHostMessages = [];
    // collects messages which were sent while not yet ready
    delayedMessages = [];

    EmbedMessenger = function() {
        if (window.addEventListener) {
            window.addEventListener('message', postMessageReceiver);
        } else {
            window.attachEvent('onmessage', postMessageReceiver);
        }
    };

    EmbedMessenger.prototype.addCommandHandler = function(commandName, handlerCallback) {
        if (!commandHandlers[commandName]) {
            commandHandlers[commandName] = handlerCallback;
        }
    };
    /**
     * Add a handler to handle a request message which was sent by the host.
     * 
     * @param {String} requestName The name of the request to handle
     * @param {Function} handlerCallback The function that handles the request. This function is
     *            passed a request-ID, the payload data and a callback function that should be
     *            called after the request was processed. When invoking this callback pass the
     *            request-ID, a boolean flag indicating whether the request led to a success or
     *            error and some additional data to send in the response as parameters. The data is
     *            request specific and can be a boolean, string, number or object.
     */
    EmbedMessenger.prototype.addRequestHandler = function(requestName, handlerCallback) {
        if (!requestHandlers[requestName]) {
            requestHandlers[requestName] = handlerCallback;
        }
    };
    /**
     * Add a handler after the page/application finished loading.
     * 
     * @param {String} name Name of the operation to handle
     * @param {String} handlerType Type of the handler i.e. 'request' or 'command'
     * @param {Function} getHandlerCallback Function to call as soon as the page/application is
     *            loaded. This function is called without parameters and is expected to return the
     *            actual handler function or null if the operation should not be handled. The
     *            required signature of the handler function is described in the addXyZHandler
     *            methods for the handlerType.
     */
    EmbedMessenger.prototype.addHandlerAfterLoad = function(name, handlerType, getHandlerCallback) {
        if (!loaded) {
            handlersToAddAfterLoad[name] = {
                type: handlerType,
                getHandlerCallback: getHandlerCallback
            }
        }
    };

    /**
     * Helper callback function that can be passed to addRequestHandler to always handle a request
     * message with a not authorized error message as the REST API would return.
     */
    EmbedMessenger.prototype.requestHandlerCallbackRestApiNotAuthorized = function(requestId, data,
            completeCallback) {
        completeCallback.call(null, requestId, false, {
            status: 'ERROR',
            message: '',
            httpStatusCode: 403
        });
    };

    /**
     * Send a command to the host. Command messages are intended to execute some operation in the
     * host site. A response is not expected.
     * 
     * @param {String} commandName The command to execute
     * @param {String|Boolean|Number|Object} [payload] Additional payload that will be interpreted
     *            in the context of the command.
     */
    EmbedMessenger.prototype.sendCommand = function(commandName, payload) {
        var message = {
            type: 'command',
            message: commandName,
            payload: payload
        }
        sendMessage(message);
    };
    /**
     * Send a protocol message to the host. Protocol messages are intended to exchange status
     * information between messenger host and client.
     * 
     * @param {String} protocolMessage The message to send (e.g. 'ready')
     * @param {String|Boolean|Number|Object} [payload] Additional payload that will be interpreted
     *            in the context of the message.
     */
    EmbedMessenger.prototype.sendProtocolMessage = function(protocolMessage, payload) {
        var message = {
            type: 'protocol',
            message: protocolMessage,
            payload: payload
        }
        sendMessage(message);
    };
    namespace.messenger = new EmbedMessenger();

    // necessary to use domready or would a setTimeout be enough? Main goal is to delay until
    // all scripts are loaded so that they can manipulate instanceId and hostUrl parameters.
    window.addEvent('domready', onReadyCallback);

    if (window.communote) {
        window.communote.initializer.addApplicationReadyCallback(onLoadCallback);
    } else {
        // use on load event if initializer does not exist
        window.addEvent('load', onLoadCallback);
    }
})(this);