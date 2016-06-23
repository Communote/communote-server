/**
 * Provides communote.messenger which is the part of the Communote cross-domain messenger service
 * that runs in the hosting site. This messenger provides an interface to exchange different kinds
 * of messages (e.g. commands and requests) with messenger clients. Messenger clients are Communote
 * sites which are running in an IFrame within the hosting site and contain the client part of the
 * messenger service.
 * 
 * @param {Window} window The window object
 */
(function(window) {
    var EmbedMessenger, hiddenClientCount, clientInstances, postMessageListenerAttached, commandHandlers;
    var requestIdCount, delayedIFrames;
    var serializeToString = false;

    if (!window.communote) {
        window.communote = {};
    }
    // TODO actually a 'function' typeof test would be the right way, but IE8 returns 'object' (and
    // the object has no members (call, apply,...)!), although it can be used like a function, weird
    if (window.communote.messenger || !window.postMessage) {
        return;
    }

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

    function createDelayedIFrames() {
        var i, l;
        var bodyElem = window.document.body;
        if (bodyElem) {
            for (i = 0, l = delayedIFrames.length; i < l; i++) {
                createHiddenIFrame(delayedIFrames[i].id, delayedIFrames[i].url, bodyElem);
            }
            delayedIFrames = null;
        } else {
            setTimeout(createDelayedIFrames, 200);
        }
    }

    function createHiddenIFrame(iframeId, url, bodyElem) {
        var iframeElem, paramString;
        // can be called before body exists, in this case just create later. Messages won't be sent
        // before client signals that it is ready and thus not before the IFrame was created.
        if (!bodyElem) {
            if (!delayedIFrames) {
                delayedIFrames = [];
                setTimeout(createDelayedIFrames, 200);
            }
            delayedIFrames.push({
                id: iframeId,
                url: url
            });
            return;
        } else {
            iframeElem = window.document.createElement('IFRAME');
            iframeElem.setAttribute('frameborder', '0');
            iframeElem.name = 'communote-embed-communication';
            iframeElem.id = iframeId;
            iframeElem.style.display = 'none';
            paramString = 'embedInstanceId=' + iframeId;
            paramString += '&embedHostUrl='
                    + encodeURIComponent(location.protocol + '//' + location.host);
            // in case we are sent to the login page disable autoheight
            paramString += '&autoHeight=false';
            if (url.indexOf('?') === -1) {
                url += '?';
            } else {
                url += '&';
            }
            iframeElem.src = url + paramString;
            bodyElem.appendChild(iframeElem);
        }
    }

    function handlePostMessage(message, messengerClient, iframeElem) {
        var handler, payload, request;
        if (message.type === 'command') {
            handler = commandHandlers[message.message];
            if (handler) {
                handler.call(null, message.instanceId, iframeElem, message.payload);
            }
        } else if (message.type === 'protocol') {
            if (message.message === 'ready') {
                messengerClient.ready = true;
                messengerClient.authenticated = message.payload;
                sendDelayedMessages(messengerClient, message.instanceId);
            }
        } else if (message.type === 'response') {
            // find matching request and call callback
            payload = message.payload;
            if (payload && payload.id != undefined) {
                request = messengerClient.runningRequests[payload.id];
                delete messengerClient.runningRequests[payload.id];
                if (request) {
                    if (message.message === 'MESSENGER_ERROR') {
                        if (window.console && window.console.log) {
                            window.console
                                    .log('WARN embed-messenger-host: request message failed: '
                                            + payload.data);
                        }
                    } else if (message.message === 'ERROR') {
                        if (request.errorCallback) {
                            request.errorCallback.call(null, message.instanceId, iframeElem,
                                    payload.data);
                        }
                    } else {
                        // expect that there is a success callback
                        request.successCallback.call(null, message.instanceId, iframeElem,
                                payload.data);
                    }
                }
            }
        }
    }

    /**
     * Event handler for the post message events.
     */
    function postMessageReceiver(event) {
        var message, messengerClient, iframeElem;
        if (typeof event.data === 'string') {
            try {
                message = JSON.parse(event.data);
            } catch (e) {
                // ignore
            }
        } else {
            message = event.data;
        }
        // if the message is from one of the iframes, handle it
        if (message && message.instanceId) {
            messengerClient = clientInstances[message.instanceId];
            if (messengerClient && messengerClient.url && event.origin === messengerClient.url) {
                iframeElem = window.document.getElementById(message.instanceId);
                if (iframeElem && iframeElem.contentWindow == event.source) {
                    handlePostMessage(message, messengerClient, iframeElem);
                }
            }
        }
    }

    function sendDelayedMessages(messengerClient, instanceId) {
        var i, l, iframeElem;
        if (messengerClient.delayedMessages) {
            iframeElem = window.document.getElementById(instanceId);
            for (i = 0, l = messengerClient.delayedMessages.length; i < l; i++) {
                sendMessage(messengerClient.delayedMessages[i], messengerClient, instanceId,
                        iframeElem);
            }
            delete messengerClient.delayedMessages;
        }
    }

    function sendMessage(message, messengerClient, instanceId, iframeElem) {
        if (messengerClient.ready) {
            if (!iframeElem) {
                iframeElem = window.document.getElementById(instanceId);
            }
            message.instanceId = instanceId;
            if (serializeToString) {
                message = JSON.stringify(message);
            }
            iframeElem.contentWindow.postMessage(message, messengerClient.url);
        } else {
            messengerClient.delayedMessages.push(message);
        }
    }

    // mapping from instanceId to an object with details about a messenger client (iframe where embedCommunicationClient script is loaded)
    clientInstances = {};
    // mapping from command name to function that executes the command
    commandHandlers = {};
    hiddenClientCount = 0;
    requestIdCount = 0;
    EmbedMessenger = function() {

    };

    EmbedMessenger.prototype.addCommandHandler = function(commandName, handlerCallback) {
        if (!commandHandlers[commandName]) {
            commandHandlers[commandName] = handlerCallback;
        }
    };

    /**
     * Add a messenger client for which the calling code will create an IFrame afterwards. After
     * adding the client, messages can be send to the client because the messenger will delay the
     * messages until IFrame is created and it's content loaded.
     * 
     * @param {String} instanceId The ID of the client. This ID must be used as ID of the IFrame.
     * @param {String} communoteUrl The URL of a Communote page where the client side scripts of the
     *            Communote embed messenger are running. This should be the src attribute of the
     *            IFrame.
     */
    EmbedMessenger.prototype.addMessengerClient = function(instanceId, communoteUrl) {
        // extract real base URL (without path)
        var idx = communoteUrl.indexOf('://');
        idx = communoteUrl.indexOf('/', idx + 3);
        if (idx !== -1) {
            communoteUrl = communoteUrl.substring(0, idx);
        }
        clientInstances[instanceId] = {
            url: communoteUrl,
            ready: false,
            authenticated: false,
            // mapping from request ID to callback functions for handling the responses
            runningRequests: {},
            // collects all messages to be sent while not yet ready
            delayedMessages: []
        };
        if (!postMessageListenerAttached) {
            if (window.addEventListener) {
                window.addEventListener('message', postMessageReceiver);
            } else {
                window.attachEvent('onmessage', postMessageReceiver);
            }
            postMessageListenerAttached = true;
        }
    };

    /**
     * Create a messenger client for the given URL. The IFrame of the client will be added to the
     * bottom of the body and is hidden. After creating the instance messages can be send to it.
     * 
     * @param {String} communoteUrl URL of a Communote page where the client side scripts of the
     *            Communote embed messenger are running.
     * @return {String} the ID of the created client instance
     */
    EmbedMessenger.prototype.createHiddenMessengerClient = function(communoteUrl) {
        // create hidden IFrame for the given Communote URL and add it as client
        var instanceId = 'communote-embed-communication_' + (hiddenClientCount++);
        createHiddenIFrame(instanceId, communoteUrl, window.document.body);
        this.addMessengerClient(instanceId, communoteUrl);
        clientInstances[instanceId].hidden = true;
        return instanceId;
    };

    /**
     * Get the status of a messenger client.
     * 
     * @param {String} instanceId The ID of the client
     * @return {Object} an object with the boolean flags ready and authenticated which denote
     *         whether the client is ready and there is an authenticated user in Communote
     */
    EmbedMessenger.prototype.getMessengerClientStatus = function(instanceId) {
        var messengerClient = clientInstances[instanceId];
        if (messengerClient) {
            return {
                authenticated: messengerClient.authenticated,
                ready: messengerClient.ready
            };
        }
        return null;
    };

    /**
     * Restart a hidden messenger client by reloading the IFrame.
     * 
     * @param {String} instanceId The ID of the client instance that was returned from
     *            createHiddenMessengerClient.
     * @param {Boolean} cancelMessages Whether to cancel currently sent messages
     */
    EmbedMessenger.prototype.restartHiddenMessengerClient = function(instanceId, cancelMessages) {
        var iframeElem;
        var messengerClient = clientInstances[instanceId];
        if (messengerClient && messengerClient.hidden) {
            if (messengerClient.ready || cancelMessages) {
                // reset state if it was ready
                messengerClient.runningRequests = {};
                messengerClient.ready = false;
                messengerClient.authenticated = false;
                messengerClient.delayedMessages = [];
            }
            // do nothing if the IFrame hasn't been created yet
            if (delayedIFrames == undefined) {
                iframeElem = window.document.getElementById(instanceId);
                if (iframeElem) {
                    // location methods can be accessed cross-domain. Replace avoids history entry.
                    iframeElem.contentWindow.location.replace(iframeElem.src);
                }
            }
        }
    };

    EmbedMessenger.prototype.sendRequestMessage = function(instanceId, requestName, data,
            successCallback, errorCallback) {
        var requestId, message;
        var client = clientInstances[instanceId];
        if (client) {
            // TODO validate requestName for supported operation?
            // TODO do not send if auth is required and there is no auth?
            requestId = 'r' + requestIdCount++;
            client.runningRequests[requestId] = {
                successCallback: successCallback,
                errorCallback: errorCallback
            };
            message = {
                type: 'request',
                message: requestName,
                payload: {
                    id: requestId,
                    data: data
                }
            };
            sendMessage(message, client, instanceId);
        }
    };

    window.communote.messenger = new EmbedMessenger();
})(this);