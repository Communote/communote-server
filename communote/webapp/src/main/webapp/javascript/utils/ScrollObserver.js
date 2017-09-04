(function(namespace, window) {
	var rootDoc = window.document;
    /**
     * Helper class to observe scroll events of a scrollable container like a DIV or the Document.
     * After observing such a container listeners can be added. These listeners are invoked when the
     * container is scrolled and additional conditions defined by the listener are met.
     */
    var ScrollObserver = new Class({
        Implements: Options,

        options: {
            scrollThrottleTimeout: 250,
            resizeThrottleTimeout: 250,
            mozScrollAreaChangedThrottleTimeout: 250,
            minDomResizeCheckInterval: 250,
            observeWindowResize: true
        },
        observerCount: 0,
        observers: null,
        boundEventHandlers: null,
        listenerIdToObserverId: null,

        initialize: function(options) {
            this.setOptions(options);
            this.observers = {};
            this.boundEventHandlers = {};
            this.listenerIdToObserverId = {};
        },

        /**
         * Observe a container for scroll events. The container can be any element with scollbars
         * including the Document.
         * 
         * @param {Element|String} container A Mootools element object or a selector of a container
         *            to observe.
         * @param {Boolean} xAxis Whether to observe the scroll events of the x- or the y-axis.
         * @param {Number} observeDomResizeInterval Interval for observing changes of the container
         *            size which are caused by DOM modifications like adding, showing or hiding
         *            elements. The interval defines the time in milliseconds to wait between
         *            checking for changes of the size. A value of 0 or less means no checking.
         * @return {String} An ID to reference the created observer in other methods that require an
         *         observerId or null if the observer could not be created because the element does
         *         not exist.
         */
        observe: function(container, xAxis, observeDomResizeInterval) {
            var type, observerId, observers;
            if (container) {
                if (typeOf(container) === 'string') {
                    container = document.getElement(container);
                }
                type = typeOf(container);
                if (type === 'document' || type === 'element') {
                    return this.internalFindOrCreateObserver(container, xAxis,
                            observeDomResizeInterval);
                }
            }
            return null;
        },

        /**
         * Return an existing observer for the given container and scroll direction or create a new
         * one if there is none.
         * 
         * @param {Element} container The container for which a observer should be retrieved
         * @param {Boolean} xAxis Whether to observe the scroll events of the x- or the y-axis.
         * @param {Number} observeDomResizeInterval Interval for observing changes of the container
         *            size which are caused by DOM modifications like adding, showing or hiding
         *            elements. The interval defines the time in milliseconds to wait between
         *            checking for changes of the size. A value of 0 or less means no checking.
         * @return {String} An ID to reference the observer in other methods.
         */
        internalFindOrCreateObserver: function(container, xAxis, observeDomResizeInterval) {
            var observerId;
            var observers = this.observers;
            // normalize the interval
            if (observeDomResizeInterval > 0
                    && observeDomResizeInterval < this.options.minDomResizeCheckInterval) {
                observeDomResizeInterval = this.options.minDomResizeCheckInterval;
            }
            // normalize document nodes because of some optimizations and special handling later on
            if (container.nodeType === 1) {
                if (container === container.ownerDocument.documentElement) {
                    container = container.ownerDocument
                } else if (container === container.ownerDocument.body) {
                    // use document instead of body because size calculations on body usually ignores
                    // elements with fixed positioning. W.r.t. scrolling there is no difference if
                    // document or body is scrolled.
                    container = container.ownerDocument
                }
            }
            for (observerId in observers) {
                if (observers.hasOwnProperty(observerId)) {
                    if (observers[observerId].xAxis == xAxis
                            && observers[observerId].container == container) {
                        // TODO handle case observeDomResizeTimeout is changed
                        return observerId;
                    }
                }
            }
            observerId = 'observer_' + this.observerCount;
            this.observerCount++;
            observers[observerId] = {
                container: container,
                xAxis: xAxis,
                listeners: [],
                listenerIdSequence: 0,
                timeouts: {},
                activeListenerCount: 0,
                observeDomResizeInterval: observeDomResizeInterval,
                previousContainerSizeDetails: this.internalGetScrolledContainerDetails(container,
                        xAxis)
            };
            return observerId;
        },

        /**
         * Attach the scroll event listener to the container element of the given observer if not
         * already attached.
         * 
         * @param {String} observerId ID of the observer whose container's scroll events should be
         *            observed
         */
        internalAttachScrollEvent: function(observerId) {
            var container, boundHandlers, observeDomResize, isDocumentNode, observeDomResizeInterval;
            if (!this.boundEventHandlers[observerId]) {
                container = this.observers[observerId].container;
                isDocumentNode = container.nodeType === 9;
                observeDomResizeInterval = this.observers[observerId].observeDomResizeInterval;
                boundHandlers = {};
                this.boundEventHandlers[observerId] = boundHandlers;
                boundHandlers.scroll = this.internalRunEventHandlerThrottled
                        .bind(this, 'internalHandleScrollEvent',
                                this.options.scrollThrottleTimeout, observerId);
                // need to bind 'scroll' to window and not document as it does not work in IEs<9
                if (isDocumentNode) {
                    container.window.addEvent('scroll', boundHandlers.scroll);
                } else {
                    container.addEvent('scroll', boundHandlers.scroll);
                }
                if (this.options.observeWindowResize && isDocumentNode) {
                    boundHandlers.resize = this.internalRunEventHandlerThrottled.bind(this,
                            'internalHandleResizeEvent', this.options.resizeThrottleTimeout,
                            observerId);
                    container.window.addEvent('resize', boundHandlers.resize);
                }
                if (observeDomResizeInterval > 0) {
                    // firefox provides an event that pushes changes of the DOM size, but only for Document
                    if (isDocumentNode && window.ScrollAreaEvent) {
                        boundHandlers.mozDomResize = this.internalRunEventHandlerThrottled.bind(
                                this, 'internalHandleMozScrolledAreaChangedEvent',
                                this.options.mozScrollAreaChangedThrottleTimeout, observerId);
                        // cannot use addEvent as Mootools does not support 'MozScrolledAreaChanged'
                        container.addEventListener('MozScrolledAreaChanged',
                                boundHandlers.mozDomResize);
                    } else {
                        // use a pull mechanism which checks for changes periodically, note: MutationObserver wouldn't
                        // help as it only monitors user triggered changes
                        this.observers[observerId].timeouts.domResize = this.internalHandleDomResize
                                .periodical(observeDomResizeInterval, this, observerId);
                    }
                }
            }
        },

        /**
         * Remove the scroll event listener from the container element of the given observer.
         * 
         * @param {String} observerId ID of the observer
         */
        internalRemoveScrollEvent: function(observerId) {
            var container, observer;
            var boundHandlers = this.boundEventHandlers[observerId];
            if (boundHandlers) {
                observer = this.observers[observerId];
                container = observer.container;
                if (container.nodeType === 9) {
                    container.window.removeEvent('scroll', boundHandlers.scroll);
                } else {
                    container.removeEvent('scroll', boundHandlers.scroll);
                }
                if (boundHandlers.resize) {
                    container.window.removeEvent('resize', boundHandlers.resize);
                }
                if (boundHandlers.mozDomResize) {
                    container.removeEventListener('MozScrolledAreaChanged',
                            boundHandlers.mozDomResize);
                } else if (observer.timeouts.domResize) {
                    clearTimeout(observer.timeouts.domResize);
                }
                delete this.boundEventHandlers[observerId];
            }
        },

        /**
         * Return an object which holds data to describe the viewport and total size of a scrollable
         * container.
         * 
         * @param {Element} container The scrollable container for which the sizes should be
         *            calculated
         * @param {Boolean} xAxis Whether to consider the sizes of the x or y axis
         * @return {Object} an object with following members
         *         <ul>
         *         <li>viewportLower The lower pixel value of the viewport. If the associated
         *         observer is observing the x-axis this will be the pixel value of the left side of
         *         the viewport. For the y-axis it is top.</li>
         *         <li>viewportUpper The upper pixel value of the viewport. If the associated
         *         observer is observing the x-axis this will be the pixel value of the left side of
         *         the viewport plus its width. For the y-axis it is top + height.</li>
         *         <li> totalSize The total size of the scrollable container on the given axis</li>
         *         </ul>
         */
        internalGetScrolledContainerDetails: function(container, xAxis) {
            var size;
            var details = {};
            var scroll = container.getScroll();
            var scrollSize = container.getScrollSize();
            if (xAxis) {
                details.viewportLower = scroll.x;
                details.totalSize = scrollSize.x;
                size = container.nodeType === 9 ? container.documentElement.clientWidth : container.clientWidth;
                details.viewportUpper = details.viewportLower + size;
            } else {
                details.viewportLower = scroll.y;
                details.totalSize = scrollSize.y;
                // special handling if the container is the top-level (i.e. not of an iframe) document:
                // mobile browsers like Chrome on android always report a clientHeight value which is
                // based on the smallest viewport height (the height when the URL bar is hidden).
                // However, innerHeight of window holds the correct value.
                if (container === rootDoc) {
                    if (this.viewportHeightFromWindow == null) {
                        size = container.documentElement.clientHeight;
                        // innerHeight of window includes height of scrollbars if shown, clientHeight doesn't
                        if (window.innerHeight > size) {
                            // check if there are horizontal scrollbars
                            // TODO also check overflow:scroll and related styles (which is kind of expensive)?
                            if (scrollSize.x === container.documentElement.clientWidth) {
                                this.viewportHeightFromWindow = true;
                                size = window.innerHeight;
                            } else {
                                // likely not a mobile device because there is no space consuming scrollbar
                                this.viewportHeightFromWindow = false;
                            }
                        }
                    } else if (this.viewportHeightFromWindow) {
                        size = window.innerHeight;
                    } else {
                        size = container.documentElement.clientHeight;
                    }
                } else {
                    size = container.nodeType === 9 ? container.documentElement.clientHeight : container.clientHeight;
                }
                details.viewportUpper = details.viewportLower + size;
            }
            details.xAxis = xAxis;
            return details;
        },

        /**
         * Helper which debounces/throttles the handling of events like scrolling or resizing to
         * avoid slow-downs when the event is triggered with a high frequency. Debouncing is
         * achieved by delaying the execution of the actual event handler by some milliseconds and
         * if in that time another event is recorded the previous one will be ignored and the
         * handling of that new one is delayed again.
         * 
         * @param {String} eventHandlerName The name of the handler function
         * @param {Number} timeout The timeout in milliseconds to use to delay the execution
         * @param {String} observerId ID of an observer
         */
        internalRunEventHandlerThrottled: function(eventHandlerName, timeout, observerId) {
            var args;
            var observer = this.observers[observerId];
            var timeouts = observer.timeouts;
            if (timeouts[eventHandlerName]) {
                clearTimeout(timeouts[eventHandlerName]);
            }
            if (arguments.length > 3) {
                args = Array.prototype.slice.call(arguments);
                args = args.splice(2);
                timeouts[eventHandlerName] = this[eventHandlerName].delay(timeout, this, args);
            } else {
                timeouts[eventHandlerName] = this[eventHandlerName]
                        .delay(timeout, this, observerId);
            }
        },
        /**
         * Handler that is triggered when the container of the provided observer is scrolled on the
         * x- or y-axis. The handler will invoke all callbacks of those listeners added to the
         * observer whose condition is fulfilled.
         * 
         * @param {String} observerId ID of an observer
         */
        internalHandleScrollEvent: function(observerId) {
            var observer, containerDetails;
            observer = this.observers[observerId];
            containerDetails = this.internalGetScrolledContainerDetails(observer.container,
                    observer.xAxis);
            this.internalInvokeActiveListeners(observer, containerDetails);
            observer.previousContainerSizeDetails = containerDetails;
        },

        /**
         * Handler that is called when the browser window is resized.
         * 
         * @param {String} observerId ID of an observer
         */
        internalHandleResizeEvent: function(observerId) {
            // just delegate to scroll handler as they do the same. This method is required to have
            // distinctive throttles.
            this.internalHandleScrollEvent(observerId);
        },

        /**
         * Handler for Firefox's MozScrolledAreaChangedEvent that is called when the size of the
         * scrollable area of the Document changed.
         * 
         * @param {String} observerId ID of an observer
         * @param {MozScrolledAreaChangedEvent} event Object with details on the event.
         */
        internalHandleMozScrolledAreaChangedEvent: function(observerId, event) {
            var observer, containerDetails, newSize;
            observer = this.observers[observerId];
            if (observer.xAxis) {
                newSize = event.width;
                console.log('new x: ' + event.x + ' new width: ' + event.width);
            } else {
                newSize = event.height;
            }
            // event is sometimes also triggered for window resize but not always, so ignore all events
            // which did not lead to a resize of the scrollable area
            // Note: also MDN docu states that weight and height are int, they are sometimes floats
            if (Math.round(newSize) != observer.previousContainerSizeDetails.totalSize) {
                containerDetails = this.internalGetScrolledContainerDetails(observer.container,
                        observer.xAxis);
                this.internalInvokeActiveListeners(observer, containerDetails);
                observer.previousContainerSizeDetails = containerDetails;
            }
        },

        /**
         * Handler to be called if the size of the container of the given observer might have
         * changed. If the size has changed the handler will invoke the listeners of the observer
         * 
         * @param {String} observerId ID of an observer
         */
        internalHandleDomResize: function(observerId) {
            var observer, containerDetails, changed;
            observer = this.observers[observerId];
            containerDetails = this.internalGetScrolledContainerDetails(observer.container,
                    observer.xAxis);
            // check if size of scrolled area changed
            if (!observer.previousContainerSizeDetails
                    || observer.previousContainerSizeDetails.totalSize != containerDetails.totalSize) {
                this.internalInvokeActiveListeners(observer, containerDetails);
                observer.previousContainerSizeDetails = containerDetails;
            }
        },

        /**
         * Invoke all active listeners of the given observer. This includes calling their callbacks
         * if the conditions are fulfilled.
         * 
         * @param {Object} observer The observer descriptor
         * @param {Object} containerSizeDetails An object as returned by
         *            #internalGetScrolledContainerDetails
         */
        internalInvokeActiveListeners: function(observer, containerSizeDetails) {
            var i, listener;
            for (i = 0; i < observer.listeners.length; i++) {
                listener = observer.listeners[i];
                // skip paused listeners
                if (listener.active) {
                    this.internalInvokeListener(listener, true, observer, containerSizeDetails);
                }
            }
        },

        /**
         * Helper that delegates to the correct listener function.
         */
        internalInvokeListener: function(listener, callCallback, observer, containerSizeDetails) {
            if (listener.offset != null) {
                this.internalInvokeOffsetListener(listener, callCallback, containerSizeDetails);
            } else if (listener.element) {
                this.internalInvokeElementListener(listener, callCallback, observer,
                        containerSizeDetails);
            }
            listener.dirty = false;
        },

        /**
         * Called after a scroll event to let the listener decide whether to invoke the callback.
         * The callback will be triggered if callCallback is true and the range defined by the
         * listener scrolled into or out of the viewport. If the range has already been in/out the
         * viewport nothing will happen.
         * 
         * @param {Object} listener The object holding the details about the listener
         * @param {Boolean} callCallback Whether to call the callback. If false the callback won't
         *            be called even if the range was scrolled into the viewport. A value of false
         *            is useful to save the current scroll-state of the range (i.e. in or outside
         *            the viewport) without invoking the callback.
         * @param {Object} containerSizeDetails An object as returned by
         *            #internalGetScrolledContainerDetails
         */
        internalInvokeOffsetListener: function(listener, callCallback, containerSizeDetails) {
            var observedPos = containerSizeDetails.totalSize - listener.offset;
            var wasInside = listener.inside;
            var isInside = containerSizeDetails.viewportUpper > observedPos;
            if (callCallback) {
                if (listener.dirty || wasInside != isInside) {
                    // pass whether we entered the range defined by the offset
                    listener.callback.call(null, isInside);
                }
            }
            listener.inside = isInside;
        },

        internalInvokeElementListener: function(listener, callCallback, observer,
                containerSizeDetails) {
            var elemLower, elemUpper, oldElemLower, oldElemUpper, previousContainerSizeDetails;
            var lowerEdgeViewportPos, upperEdgeViewportPos, oldLowerEdgeViewportPos, oldUpperEdgeViewportPos;
            var coords, previousCoords;
            // get coordinates relative to observed element; pass null if observing the document since mootools can't handle it
            coords = listener.element.getCoordinates(observer.container.nodeType === 9 ? null
                    : observer.container);
            // if the element is not visible because it or one of its parents has style display none
            // the coords, are all null -> ignore
            if (coords.right === 0 && coords.bottom === 0 && coords.left === 0 && coords.top === 0) {
                return;
            }
            previousCoords = listener.previousCoords || coords;
            listener.previousCoords = coords;

            if (containerSizeDetails.xAxis) {
                coords.left += listener.lowerOffset;
                coords.right += listener.upperOffset;
                elemLower = coords.left;
                elemUpper = coords.right;
                oldElemLower = previousCoords.left;
                oldElemUpper = previousCoords.right;
            } else {
                coords.top += listener.lowerOffset;
                coords.bottom += listener.upperOffset;
                elemLower = coords.top;
                elemUpper = coords.bottom;
                oldElemLower = previousCoords.top;
                oldElemUpper = previousCoords.bottom;
            }
            // calculate the relative position of each relevant element edge to the viewport edges.
            // set edge pos to -1 if edge is outside on left/top of viewport, 0 if in viewport and 1 if outside right/bottom
            lowerEdgeViewportPos = this.internalCalculateRelativeViewportPosition(elemLower,
                    containerSizeDetails);
            upperEdgeViewportPos = this.internalCalculateRelativeViewportPosition(elemUpper,
                    containerSizeDetails);
            if (callCallback) {
                // if dirty ignore the last recorded state 
                if (!listener.dirty) {
                    previousContainerSizeDetails = observer.previousContainerSizeDetails;
                    // calculate old relative position to decide whether something changed and the callback needs to be called
                    oldLowerEdgeViewportPos = this.internalDetermineOldRelativeViewportPosition(
                            listener.previousLowerEdgeViewportPos, lowerEdgeViewportPos,
                            oldElemLower, elemLower, previousContainerSizeDetails,
                            containerSizeDetails);
                    oldUpperEdgeViewportPos = this.internalDetermineOldRelativeViewportPosition(
                            listener.previousUpperEdgeViewportPos, upperEdgeViewportPos,
                            oldElemUpper, elemUpper, previousContainerSizeDetails,
                            containerSizeDetails);
                }
                // only call callback if something changed or dirty
                if (oldLowerEdgeViewportPos != lowerEdgeViewportPos
                        || oldUpperEdgeViewportPos != upperEdgeViewportPos) {
                    listener.callback.call(null, oldLowerEdgeViewportPos != lowerEdgeViewportPos,
                            lowerEdgeViewportPos, oldUpperEdgeViewportPos != upperEdgeViewportPos,
                            upperEdgeViewportPos);
                }
            }
            // save current relative position for next event
            listener.previousLowerEdgeViewportPos = lowerEdgeViewportPos;
            listener.previousUpperEdgeViewportPos = upperEdgeViewportPos;
        },

        /**
         * Calculate the relative position to the viewport of an absolute x or y position.
         * 
         * @param {Number} position The absolute position on the x- or y-axis. The viewportUpper and
         *            viewportLower values in the containerSizeDetails have to belong to the same
         *            axis.
         * @param {Object} containerSizeDetails An object as returned by
         *            #internalGetScrolledContainerDetails that describes the viewport of the
         *            scrolled container
         * @return {Number} -1 if the position is outside (top/left) of the viewport, 0 if the
         *         position is inside the viewport or 1 if the position is outside (bottom/right) of
         *         the viewport
         */
        internalCalculateRelativeViewportPosition: function(position, containerSizeDetails) {
            return position < containerSizeDetails.viewportLower ? -1
                    : (position > containerSizeDetails.viewportUpper ? 1 : 0);
        },

        /**
         * Determine the relative position to the viewport an absolute x or y position had before
         * the event currently processed. This method takes the previously recorded relative
         * position and changes of the viewport and DOM into account.
         * 
         * @param {Number} previousRelativePosition A recorded relative position from the event
         *            handled before this one.
         * @param {Number} relativePosition The current relative position
         * @param {Number} previousPosition The absolute position recorded while handling the last
         *            event.
         * @param {Number} position The current absolute position (x or y axis position, e.g. of an
         *            element edge)
         * @param {Object} previousContainerSizeDetails An object as returned by
         *            #internalGetScrolledContainerDetails that describes the viewport of the
         *            scrolled container that was recorded while handling the last event.
         * @param {Object} containerSizeDetails An object as returned by
         *            #internalGetScrolledContainerDetails that describes the viewport of the
         *            scrolled container
         * @return {Number} the previous relative position which can be -1 if the position is
         *         outside (top/left) of the viewport, 0 if the position is inside the viewport or 1
         *         if the position is outside (bottom/right) of the viewport
         */
        internalDetermineOldRelativeViewportPosition: function(previousRelativePosition,
                relativePosition, previousPosition, position, previousContainerSizeDetails,
                containerSizeDetails) {
            var viewportChanged = false;
            var oldRelativePos = relativePosition;
            // if the viewport position or size changed w.r.t. the last recorded value we take the old
            // viewport position. This covers scrolls and viewport resizes.
            if (previousContainerSizeDetails.viewportLower != containerSizeDetails.viewportLower
                    || previousContainerSizeDetails.viewportUpper != containerSizeDetails.viewportUpper) {
                oldRelativePos = previousRelativePosition;
                viewportChanged = true;
            }
            // check if the element/edge the position refers to has changed in size or position. This covers when 
            // elements were added to or removed from the DOM somewhere before the element or children of it were
            // added or removed. However this check is not necessary if we already know that the relative position
            // of the edge changed. This will only occur if both viewport and element position/size changed and
            // we assume that we missed one of these changes and thus call the callback to inform it about the
            // current relative position.
            if (oldRelativePos == relativePosition && previousPosition != position) {
                // calculate the old position by checking the current coordinates against the old viewport if
                // the viewport changed. Using the old viewport because we treat the resize/reposition of the
                // element as the missed operation which is more likely.
                if (viewportChanged) {
                    oldRelativePos = this.internalCalculateRelativeViewportPosition(position,
                            previousContainerSizeDetails);
                } else {
                    // just the position changed: the old position is the recorded value
                    oldRelativePos = previousRelativePosition;
                }
            }
            return oldRelativePos;
        },

        /**
         * <p>
         * Add a listener that is invoked when a given range is scrolled into or out of the viewport
         * of the container of the provided observer. The range is defined by the provided offset
         * and the upper boundary of the container which is the width of the container when
         * observing the x-axis and the height for the y-axis. For example when observing the y-axis
         * the listener will be triggered when the container is scrolled so that the range from
         * height - offset to height enters or leaves the visible area (viewport).
         * </p>
         * <p>
         * The added listener will be paused by default and has to be started explicitly via a call
         * to startListener.
         * </p>
         * 
         * @param {String} observerId ID of the observer whose container's scroll events the
         *            listener should be informed about
         * @param {Number} [offset] The number of pixels to span a range from height/width - offset
         *            to height/width. Must be a positive value. If omitted, 1 pixel is assumed.
         * @param {Function} callback The function to be called when the range enters or leaves the
         *            viewport. The function needs to have the following signature: (boolean) The
         *            boolean parameter will be true if the range was entered and false if it was
         *            left.
         * @return {String} A unique ID of the listener to be used in other methods.
         */
        addOffsetListener: function(observerId, offset, callback) {
            if (offset == null || offset <= 0) {
                offset = 1;
            }
            return this.internalAddListener(observerId, {
                offset: offset
            }, callback);
        },
        /**
         * <p>
         * Add a listener that is invoked when a given element is scrolled into or out of the
         * viewport of the container of the provided observer. The listener will also be informed if
         * only an edge of the element enters or leaves the viewport.
         * </p>
         * <p>
         * The added listener will be paused by default and has to be started explicitly via a call
         * to startListener.
         * </p>
         * 
         * @param {String} observerId ID of the observer whose container's scroll events the
         *            listener should be informed about
         * @param {Element} element The element to observe
         * @param {Function} callback The function to be called when the element enters or leaves
         *            the viewport. The function needs to have the following signature:
         *            (lowerEdgeChanged, lowerEdgeRelativePos, upperEdgeChanged,
         *            upperEdgeRelativePos). The arguments are defined as follows
         *            <ul>
         *            <li>{Boolean} lowerEdgeChanged Denotes whether the lower edge of the element
         *            changed its relative position to the viewport w.r.t. the last observed scroll
         *            event. This argument will for example be true if the lower edge was outside
         *            the viewport during the last scroll event and now is inside the viewport. The
         *            lower edge of the element refers to the edge with the lower value, that is the
         *            left edge for an x-axis scroll observer and the top edge for a y-axis
         *            observer.</li>
         *            <li>{Number} lowerEdgeRelativePos Describes the current position of the lower
         *            edge of the element relative to the viewport. This argument can be -1, 0 or 1
         *            if the edge is outside before the lower viewport edge, inside the viewport or
         *            outside the upper viewport edge.</li>
         *            <li>{Boolean} upperEdgeChanged Denotes whether the upper edge of the element
         *            changed its relative position to the viewport w.r.t. the last observed scroll
         *            event. This argument will for example be true if the upper edge was outside
         *            the viewport during the last scroll event and now is inside the viewport. The
         *            upper edge of the element refers to the edge with the higher value, that is
         *            the right edge for an x-axis scroll observer and the bottom edge for a y-axis
         *            observer.</li>
         *            <li>{Number} upperEdgeRelativePos Describes the current position of the upper
         *            edge of the element relative to the viewport. This argument can be -1, 0 or 1
         *            if the edge is outside before the lower viewport edge, inside the viewport or
         *            outside the upper viewport edge.</li>
         *            </ul>
         *            When this callback is invoked at least one of the arguments lowerEdgeChanged
         *            and upperEdgeChanged will be true.
         * @return {String} A unique ID of the listener to be used in other methods.
         * 
         */
        addElementListener: function(observerId, element, callback) {
            return this.internalAddListener(observerId, {
                element: element
            }, callback);
        },
        
        /**
         * <p>
         * Add a listener that works like one added with addElementListener with the difference that
         * its upper and lower edges can be shifted by arbitrary offsets.
         * </p>
         * <p>
         * The added listener will be paused by default and has to be started explicitly via a call
         * to startListener.
         * </p>
         * 
         * @param {String} observerId ID of the observer whose container's scroll events the
         *            listener should be informed about
         * @param {Element} element The element to observe
         * @param {Number} lowerOffset The positive or negative number of pixels the lower edge of
         *            the element should be shifted before calculating the relative position to the
         *            viewport.
         * @param {Number} upperOffset The positive or negative number of pixels the upper edge of
         *            the element should be shifted before calculating the relative position to the
         *            viewport.
         * @param {Function} callback The function to be called when the shifted edgedes of the
         *            element enter or leave the viewport. The signature of the callback is the same
         *            as that of the callback of addElementListener.
         * @return {String} A unique ID of the listener to be used in other methods.
         */
        addElementOffsetListener: function(observerId, element, lowerOffset, upperOffset, callback) {
            return this.internalAddListener(observerId, {
                element: element,
                lowerOffset: lowerOffset,
                upperOffset: upperOffset
            }, callback);
        },

        /**
         * Helper that adds the listener to the observer and adds some common data for later use.
         * This method also adds the ID of the listener to a map for fast lookup of the observer by
         * the listener ID.
         * 
         * @param {String} observerId ID of the observer whose container's scroll events the
         *            listener should be bound to
         * @param {Object} listenerData An object with custom data needed by the listener
         * @param {Function} callback The function to be called when the condition of the listener
         *            is fulfilled
         * @return {String} A unique ID of the listener to be used in other methods.
         */
        internalAddListener: function(observerId, listenerData, callback) {
            var listenerId;
            var observer = this.observers[observerId];
            if (observer) {
                listenerId = observerId + '-listener_' + observer.listenerIdSequence;
                observer.listenerIdSequence++;
                listenerData.callback = callback;
                listenerData.active = false;
                listenerData.dirty = false;
                listenerData.id = listenerId;
                if (listenerData.element) {
                    if (listenerData.lowerOffset == undefined) {
                        listenerData.lowerOffset = 0;
                    }
                    if (listenerData.upperOffset == undefined) {
                        listenerData.upperOffset = 0;
                    }
                }
                observer.listeners.push(listenerData);
                this.listenerIdToObserverId[listenerId] = observerId;
                return listenerId;
            }
            return null;
        },
        /**
         * Get the index of the given listener within the lister array of its observer.
         * 
         * @param {String} listenerId ID identifying the listener
         * @return {Number} the found index or -1
         */
        internalFindListenerIndex: function(listenerId) {
            var i, observerId, observer;
            observerId = this.listenerIdToObserverId[listenerId];
            if (observerId) {
                observer = this.observers[observerId];
                for (i = 0; i < observer.listeners.length; i++) {
                    if (observer.listeners[i].id == listenerId) {
                        return i;
                    }
                }
            }
            return -1;
        },

        /**
         * Remove an existing listener. This method should typically called when the listener isn't
         * needed anymore (e.g. when the element of an element-listener got removed from the DOM).
         * 
         * @param {String} listenerId The ID of the listener to remove.
         */
        removeListener: function(listenerId) {
            var idx, observerId;
            idx = this.internalFindListenerIndex(listenerId);
            if (idx >= 0) {
                observerId = this.listenerIdToObserverId[listenerId];
                this.internalPauseListener(observerId, idx);
                this.observers[observerId].listeners.splice(idx, 1);
                delete this.listenerIdToObserverId[listenerId];
            }
        },

        /**
         * Start a paused listener. After starting a listener its callback will be invoked each time
         * a scroll event occurs that fulfills the listener's condition.
         * 
         * @param {String} listenerId The ID of the listener to pause.
         * @param {Boolean} ignoreLastState When starting a listener the conditions will be checked
         *            and if fulfilled the callback will be invoked. When setting this parameter to
         *            true the listener will ignore the last recorded state while being active which
         *            usually will result in calling the callback.
         */
        startListener: function(listenerId, ignoreLastState) {
            var idx, observerId, observer, listener;
            idx = this.internalFindListenerIndex(listenerId);
            if (idx >= 0) {
                observerId = this.listenerIdToObserverId[listenerId];
                observer = this.observers[observerId];
                listener = observer.listeners[idx];
                if (!listener.active) {
                    listener.active = true;
                    observer.activeListenerCount++;
                    if (observer.activeListenerCount == 1) {
                        this.internalAttachScrollEvent(observerId);
                    }
                    if (ignoreLastState) {
                        listener.dirty = true;
                    }
                    // call invoke to update internal state of listener
                    this.internalInvokeListener(listener, true, observer,
                            this.internalGetScrolledContainerDetails(observer.container,
                                    observer.xAxis));
                }
            }
        },

        /**
         * Pause a listener. The callback of a paused listener won't be invoked anymore.
         * 
         * @param {String} listenerId The ID of the listener to pause.
         */
        pauseListener: function(listenerId) {
            var idx = this.internalFindListenerIndex(listenerId);
            if (idx >= 0) {
                this.internalPauseListener(this.listenerIdToObserverId[listenerId], idx);
            }
        },

        /**
         * Pause the listener.
         * 
         * @param {String} observerId ID of the observer that manages the listener.
         * @param {Number} listenerIdx Index of the listener within the managed listeners of the
         *            observer
         */
        internalPauseListener: function(observerId, listenerIdx) {
            var observer = this.observers[observerId];
            if (observer.listeners[listenerIdx].active) {
                observer.listeners[listenerIdx].active = false;
                observer.activeListenerCount--;
                // for performance reasons remove the scroll event if not needed anymore
                if (observer.activeListenerCount == 0) {
                    this.internalRemoveScrollEvent(observerId);
                }
            }
        }
    });
    if (namespace && namespace.addConstructor) {
        namespace.addConstructor('ScrollObserver', ScrollObserver);
    } else {
        window.ScrollObserver = ScrollObserver;
    }
})(window.runtimeNamespace, window);