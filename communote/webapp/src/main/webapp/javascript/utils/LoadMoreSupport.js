(function(namespace) {
    /**
     * <p>
     * Mixin to be included in a Widget subclass to get support for loading further content from the
     * server and display it inside the widget. This mixin provides two concepts to show the
     * additional content. The first concept which we call 'paging' will replace the existing
     * content with the new. The second will append the new content to the existing content. </br>
     * When using the append approach the mixin provides a special trigger to fetch the next chunk
     * of data from the server. This trigger will fire when the document was scrolled to a
     * configurable position.
     * </p>
     * <p>
     * As already stated the mixin is intended to be used in Widget classes because it calls methods
     * of the Widget class.
     * </p>
     * <p>
     * The mixin interprets the following static parameters
     * <ul>
     * <li>loadMoreMode: Defines how more data will be loaded. Supported values are 'paging'
     * (default), 'append-manually' and 'append-scroll'. Loading more in 'paging' mode will refresh
     * the content of the widget with that of the next page. The other two modes will append the
     * additional data at the bottom of the widget. The mode 'append-scroll' allows the definition
     * of a scroll trigger that will automatically invoke the loadMore operation. The loadMoreMode
     * parameter will be added as a widget request parameter.</li>
     * <li>loadMoreScrollTrigger: Defines the trigger for the 'append-scroll' mode. Supported
     * values are 'offset:number' where number is a positive integer greater than 0 that denotes the
     * pixel value of the offset from the bottom of the page, 'domNode' and 'domNodeOffset:number'
     * where number should be a negative number. When using offset, loadMore is triggered when the
     * area defined by the offset is scrolled into the view. The trigger 'domNode' states that the
     * bottom edge of the domNode of the widget will cause a loadMore when it is scrolled into the
     * view. When using 'domNodeOffset' the lower edge of the widget shifted by 'number' pixels will
     * be the trigger. The default is 'domNodeOffset:-5' (= trigger loadMore if scrolled up to 5
     * pixels before the lower edge of the widget domNode enters the viewport).</li>
     * <li>loadMoreAppendLimit: Defines the amount of consecutive automatic loadMore invocations
     * that can be caused by scrolling. If the limit is reached there will be no more automatic
     * loadMore. If the value is 0 (default) there is no limit.</li>
     * <li>loadMorePlaceholderNodeSelector: A Mootools element selector to find the node that will
     * be replaced when loading and appending more data</li>
     * </ul>
     * </p>
     */
    var LoadMoreSupport = new Class({
        // group members in object so we do not clutter the implementing class
        loadMoreSettings: {
            mode: 'paging',
            previousPagingOffset: null,
            parameters: null,
            // the shared ScrollObserver instance. Will be unset if loadMoreMode is not append-scroll
            scrollObserver: null,
            scrollObserverId: null,
            scrollListenerId: null,
            scrollTrigger: null,
            observeDomResizeInterval: 1000,
            startListenerDelayTimeout: null,
            // counter holding the current amount of appends to the widget that were caused by the loadMore 
            appendCount: 0,
            appendLimit: 0,
            appendLimitReached: false,
            appendNoMoreData: false,
            placeholderNodeSelector: '.control-loadmore-placeholder',
            feedbackNodeSelector: '.control-loadmore-loading-feedback',
            limitReachedNodeSelector: '.control-loadmore-limit-reached',
            // whether to scroll to top of widget when doing a full refresh and it's not paging mode
            scrollToTopOnRefresh: true,
            // hack for getting around the MultiViewTabWidget weirdness: saves the current domnode when
            // using append-scroll with element listener
            currentDomNode: null
        },

        /**
         * Initialize the mixin by extracting the mode and its settings from the static parameters.
         * This method should be called from within the init method of the widget.
         * 
         * @param {Object} [scope] In case the loadMoreMode is 'append-scroll' a shared
         *            ScrollObserver instance is created which will be stored under the name
         *            scrollObserver in the given scope. If the scope is unspecified the namespace
         *            scope will be used.
         */
        initLoadMoreSupport: function(scope) {
            var mode, placeholderSelector;
            // maxCount is relevant for all modes
            this.copyStaticParameter('maxCount', 10);
            // TODO use preference mechanism as soon as we have it
            if (window.communoteLocalStorage) {
                mode = communoteLocalStorage.getItem(this.widgetId + '_' + 'loadMoreMode');
            }
            if (!mode) {
                mode = this.getStaticParameter('loadMoreMode');
            }
            if (mode && (mode === 'append-manually' || mode === 'append-scroll')) {
                this.loadMoreSettings.mode = mode;
            }
            // set the actual loadMoreMode as request parameter
            this.setFilterParameter('loadMoreMode', this.loadMoreSettings.mode);
            if (this.loadMoreSettings.mode === 'paging') {
                this.copyStaticParameter('offset', 0);
                // TODO do we still need this?
                this.copyStaticParameter('pagingInterval', 5);
            } else {
                // only check one more item because it is enough to find out whether there are more results
                this.setFilterParameter('checkAtLeastMoreResults', 1);
                this.loadMoreSettings.parameters = {};
                this.initLoadMoreScrollSupport(scope);
                placeholderSelector = this.getStaticParameter('loadMorePlaceholderNodeSelector');
                if (placeholderSelector) {
                    this.loadMoreSettings.placeholderNodeSelector = placeholderSelector;
                }
            }
        },

        /**
         * Prepare the load more support if the mode is 'append-scroll'. This method should only be
         * called during initialization.
         * 
         * @param {Object} [scope] Object for storing a shared ScrollObserver instance under the
         *            name scrollObserver. If the scope is unspecified the global scope will be
         *            used.
         */
        initLoadMoreScrollSupport: function(scope) {
            var loadMoreScrollTrigger, limit;
            if (this.loadMoreSettings.mode === 'append-scroll') {
                // create global scrollObserver instance if it does not yet exist
                if (!scope) {
                    scope = namespace;
                }
                if (!scope.scrollObserver) {
                    scope.scrollObserver = new (namespace.getConstructor('ScrollObserver'))();
                }
                this.loadMoreSettings.scrollObserver = scope.scrollObserver;
                // observe y-axis of document of widget's domNode
                this.loadMoreSettings.scrollObserverId = scope.scrollObserver.observe(
                        this.domNode.ownerDocument, false,
                        this.loadMoreSettings.observeDomResizeInterval);
                loadMoreScrollTrigger = this.getStaticParameter('loadMoreScrollTrigger');
                // convert parameter in an array and normalize it
                if (loadMoreScrollTrigger) {
                    // expect it to be a string like 'offset:10' for 10 px offset trigger, domNodeOffset:10 for element offset trigger or 'domNode' for element trigger
                    loadMoreScrollTrigger = loadMoreScrollTrigger.split(':');
                    if (loadMoreScrollTrigger.length > 1) {
                        loadMoreScrollTrigger.splice(2);
                        loadMoreScrollTrigger[1] = loadMoreScrollTrigger[1].toInt();
                        if (isNaN(loadMoreScrollTrigger[1])) {
                            loadMoreScrollTrigger.splice(1);
                        }
                    }
                    this.loadMoreSettings.scrollTrigger = loadMoreScrollTrigger;
                } else {
                    // default to domNodeOffset with 5 pixels (negative because it is based on the bottom edge)
                    this.loadMoreSettings.scrollTrigger = [ 'domNodeOffset', -5 ];
                }
                this.attachLoadMoreListener();
                // check if there is a predefined limit
                limit = this.getStaticParameter('loadMoreAppendLimit');
                if (limit) {
                    limit = limit.toInt();
                    if (limit > 0) {
                        this.loadMoreSettings.appendLimit = limit;
                    }
                }
            }
        },

        /**
         * Attach an appropriate listener to the ScrollObserver instance when the loadMoreMode is
         * 'append-scroll'. Whether an offset or element listener on the domNode is used is defined
         * by the loadMoreScrollTrigger option.
         */
        attachLoadMoreListener: function() {
            var settings = this.loadMoreSettings;
            if (settings.mode == 'append-scroll') {
                if (settings.scrollTrigger[0] === 'offset') {
                    settings.scrollListenerId = settings.scrollObserver.addOffsetListener(
                            settings.scrollObserverId, settings.scrollTrigger[1],
                            this.loadMoreOffsetListener.bind(this));
                } else if (settings.scrollTrigger[0] === 'domNodeOffset') {
                    settings.scrollListenerId = settings.scrollObserver.addElementOffsetListener(
                            settings.scrollObserverId, this.domNode, 0, settings.scrollTrigger[1]
                                    || -5, this.loadMoreElementListener.bind(this));
                } else {
                    // fallback to domNode
                    this.loadMoreSettings.currentDomNode = this.domNode;
                    settings.scrollListenerId = settings.scrollObserver.addElementListener(
                            settings.scrollObserverId, this.domNode, this.loadMoreElementListener
                                    .bind(this));
                }
            }
        },

        /**
         * Listener that is bound to the ScrollObserver when the scrollTrigger is 'offset'. The
         * listener will be called as defined in ScrollObserver.addOffsetListener method.
         * 
         * @param {Boolean} entered Whether the range defined by the offset and the height of the
         *            page was entered or left.
         */
        loadMoreOffsetListener: function(entered) {
            if (entered) {
                this.loadMore();
            }
        },

        /**
         * Listener that is bound to the ScrollObserver when the scrollTrigger is 'domNode' or
         * 'domNodeOffset. The listener will be called as defined in
         * ScrollObserver.addElementListener method.
         * 
         * @param {Boolean} lowerEdgeChanged Whether the lower edge of the observed element changed
         *            its relative position
         * @param {Number} lowerEdgeRelativePos Number with value -1, 0 or 1 that describes the
         *            relative position of the lower edge of the observed element to the viewport.
         * @param {Boolean} upperEdgeChanged Whether the lower edge of the observed element changed
         *            its relative position
         * @param {Number} upperEdgeRelativePos Number with value -1, 0 or 1 that describes the
         *            relative position of the upper edge of the observed element to the viewport.
         */
        loadMoreElementListener: function(lowerEdgeChanged, lowerEdgeRelativePos, upperEdgeChanged,
                upperEdgeRelativePos) {
            // load more if upper edge changed its position and is inside viewport now
            if (upperEdgeChanged && upperEdgeRelativePos == 0) {
                this.loadMore();
            }
        },
        /**
         * Cleanup any resources held by the mixin. This method should be called from the widget's
         * beforeRemove method.
         */
        disposeLoadMoreSupport: function() {
            var settings = this.loadMoreSettings;
            var listenerId = settings.scrollListenerId;
            if (listenerId) {
                settings.scrollObserver.removeListener(listenerId);
            }
            if (settings.startListenerDelayTimeout) {
                clearTimeout(settings.startListenerDelayTimeout);
                settings.startListenerDelayTimeout = null;
            }
        },
        /**
         * Pause the automatic LoadMore features like loading more content when scrolling and
         * append-scroll mode is used. This method should be called from the widget's beforeHide
         * method.
         */
        pauseLoadMoreSupport: function() {
            var settings = this.loadMoreSettings;
            if (settings.scrollListenerId && !settings.appendLimitReached
                    && !settings.appendNoMoreData) {
                settings.scrollObserver.pauseListener(settings.scrollListenerId);
            }
            if (settings.startListenerDelayTimeout) {
                clearTimeout(settings.startListenerDelayTimeout);
                settings.startListenerDelayTimeout = null;
            }
        },
        /**
         * Resume the automatic LoadMore features if they were paused before. This method should be
         * called from the widget's afterShow method, usually only when the widget is not dirty.
         */
        resumeLoadMoreSupport: function() {
            var settings = this.loadMoreSettings;
            if (settings.scrollListenerId && !settings.appendLimitReached
                    && !settings.appendNoMoreData) {
                this.loadMoreListenerStartAfterScrolling(settings.scrollListenerId);
            }
        },
        /**
         * Replace the current state with initial values.
         */
        resetLoadMoreState: function() {
            var settings = this.loadMoreSettings;
            if (settings.mode === 'paging') {
                this.copyStaticParameter('offset', 0);
                settings.previousPagingOffset = null;
            } else {
                settings.parameters = {};
                settings.appendCount = 0;
                settings.appendLimitReached = false;
                settings.appendNoMoreData = false;
                if (settings.scrollListenerId) {
                    // TODO this is a hack to overcome MultiViewTabWidget issues
                    // check whether the DOM node of the widget changed if so attach another listener and pause current
                    if (settings.currentDomNode && settings.currentDomNode != this.domNode) {
                        settings.scrollObserver.pauseListener(settings.scrollListenerId);
                        this.attachLoadMoreListener();
                    }
                    //note: also the listener might be paused due to reaching noMoreData or append limit we do not start it
                    // here since we have no reliable data whether there is really more data. Thus we expect that the next 
                    // refresh starts listener if necessary. And because this method is usually called right before a 
                    // refresh starting the listener could cause double refreshs.
                }
            }
        },

        /**
         * Return the current mode the LoadMoreSupport is operating in.
         * 
         * @return {String} The current mode the LoadMoreSupport is operating in. Can be one of
         *         'paging', 'append-manually' and 'append-scroll'
         */
        getLoadMoreMode: function() {
            return this.loadMoreSettings.mode;
        },

        /**
         * Convenience method to go to the page identified by the provided offset. If in paging mode
         * this method will set the 'offset' filter parameter and refresh the widget. If not in
         * paging mode nothing will happen.
         * 
         * @param {Number} offset The offset within the total result set to start from when
         *            returning results.
         */
        loadPage: function(offset) {
            if (this.loadMoreSettings.mode === 'paging') {
                this.internalUpdatePagingOffset(offset);
                this.refresh();
            }
        },

        /**
         * Helper to update the paging offset parameter by setting it as filterParameter.
         * 
         * @param {String|Number} offset The new paging offset
         */
        internalUpdatePagingOffset: function(offset) {
            if (offset != undefined) {
                this.loadMoreSettings.previousPagingOffset = this.getFilterParameter('offset');
                this.setFilterParameter('offset', offset);
            }
        },

        /**
         * Set additional parameters that should be included when a partial refresh caused by the
         * loadMore method is executed.
         * 
         * @param {Object} parameters The parameters to set. These parameters will be merged with
         *            the current parameters. In case of paging mode only the 'offset' parameter
         *            will be considered.
         */
        setLoadMoreParameters: function(parameters) {
            if (this.loadMoreSettings.mode === 'paging') {
                this.internalUpdatePagingOffset(parameters.offset);
            } else {
                // TODO will override arrays - ok?
                Object.merge(this.loadMoreSettings.parameters, parameters);
            }
        },

        /**
         * <p>
         * Load more data into the widget. The actual action depends on the loadMoreMode. In case of
         * paging the widget will do a full refresh and assumes the new page offset had already been
         * set. In case of one of the append modes the widget will do a partial refresh that will
         * replace the node identified by the configurable selector placeHolderNodeSelector. The
         * partial refresh will include the filter parameters and any parameters set via
         * setLoadMoreParameters.
         * </p>
         * <p>
         * In case of mode append-scroll this method will be called automatically if the condition
         * of the scrollTrigger is met.
         * </p>
         */
        loadMore: function() {
            var placeholderElem, loadingFeedbackElem;
            if (this.loadMoreSettings.mode === 'paging') {
                this.refresh();
            } else {
                // do a partial refresh of the placeholder element
                placeholderElem = this.domNode
                        .getElement(this.loadMoreSettings.placeholderNodeSelector);
                if (placeholderElem) {
                    // if there is a sub node that contains some loading feedback text we show it
                    loadingFeedbackElem = placeholderElem
                            .getElement(this.loadMoreSettings.feedbackNodeSelector);
                    if (loadingFeedbackElem) {
                        loadingFeedbackElem.setStyle('display', 'block');
                    }
                    this.partialRefresh({
                        includeFilterParams: true,
                        additionalParams: this.loadMoreSettings.parameters,
                        domNode: placeholderElem,
                        insertMode: 'replace',
                        showLoadingOverlay: true,
                        markLoading: false,
                        context: {
                            loadMore: true
                        }
                    });
                }
            }
        },

        /**
         * Callback to inform the loadMore support that a partial or full refresh is about to take
         * place. Users of this Mixin should call this method from within their refreshStart and
         * partialRefreshStart methods.
         * 
         * @param {Object} context The context object of the partial refresh or null if the refresh
         *            was a full refresh.
         */
        loadMoreRefreshStart: function(context) {
            var listenerId, settings;
            // in case of a partial refresh ignore all which were not triggered by loadMore
            if (!context || context.loadMore) {
                settings = this.loadMoreSettings;
                // pause the listener if there is one to avoid further loadMore refreshs while still refreshing
                listenerId = settings.scrollListenerId;
                if (listenerId) {
                    this.loadMoreSettings.scrollObserver.pauseListener(listenerId);
                }
                if (!context && settings.mode != 'paging' && settings.scrollToTopOnRefresh) {
                    scrollWindowTo(this.domNode, 0, 0, false, true);
                }
            }
        },
        /**
         * Callback to inform the loadMore support that a partial or full refresh completed. Users
         * of this Mixin should call this method from within their refreshComplete and
         * partialRefreshComplete methods.
         * 
         * @param {Object} context The context object of the partial refresh or null if the refresh
         *            was a full refresh.
         * @param {Object} responseMetadata The response metadata sent by the server side widget
         *            component.
         */
        loadMoreRefreshComplete: function(context, responseMetadata) {
            var settings = this.loadMoreSettings;
            // nothing to do for paging or a partial refresh that was not triggered by loadMore
            if (settings.mode == 'paging' || (context && !context.loadMore)) {
                return;
            }
            // reset internal states after a full refresh while in append mode
            if (context) {
                settings.appendCount++;
            } else {
                settings.parameters = {};
                settings.appendCount = 0;
                settings.appendLimitReached = false;
                settings.appendNoMoreData = false;
            }
            if (responseMetadata.moreElementsAvailable) {
                this.loadMoreHasMoreData(responseMetadata);
                // check for the limit
                // TODO only trigger if hitting the limit?
                if (settings.appendLimit > 0 && settings.appendCount >= settings.appendLimit) {
                    this.loadMoreLimitReached(responseMetadata);
                    settings.appendLimitReached = true;
                } else {
                    if (settings.scrollListenerId) {
                        this.loadMoreListenerStartAfterScrolling(settings.scrollListenerId);
                    }
                }
            } else {
                settings.appendNoMoreData = true;
                this.loadMoreNoMoreData(responseMetadata);
            }
        },

        /**
         * Start the listener that triggers the load more. In case there is currently a smooth
         * scroll running wait until it finished before starting the listener otherwise we would be
         * immediately loading more data if the server response was faster than the scroll
         * animation. This would produce lots of traffic.
         * 
         * @param {String} listenerId ID of the listener to start
         */
        loadMoreListenerStartAfterScrolling: function(listenerId) {
            // only start listener if the widget is not hidden
            if (!this.isHidden()) {
                if (window.smoothScroller && smoothScroller.isRunning()) {
                    this.loadMoreSettings.startListenerDelayTimeout = this.loadMoreListenerStartAfterScrolling
                            .delay(100, this, listenerId);
                } else {
                    // assure the listenerId didn't change in the meantime (tab change or similar)
                    if (listenerId == this.loadMoreSettings.scrollListenerId) {
                        this.loadMoreSettings.scrollObserver.startListener(listenerId, true);
                    }
                }
            }
        },
        /**
         * <p>
         * Callback that is called when the loadeMoreMode is append-scroll and the number of
         * consecutive loadMore calls reached or exceeded the configurable loadMoreAppendLimit.
         * </p>
         * <p>
         * The default implementation will look for an element matching the selector
         * '.control-loadmore-limit-reached' and show it.
         * </p>
         * 
         * @param {Object} responseMetadata The response metadata sent by the server side widget
         *            component.
         */
        loadMoreLimitReached: function(responseMetadata) {
            var limitReachedNode = this.domNode
                    .getElement(this.loadMoreSettings.limitReachedNodeSelector);
            if (limitReachedNode) {
                limitReachedNode.setStyle('display', 'block');
            }
        },
        /**
         * <p>
         * Callback that is called when the loadeMoreMode is not paging, a loadMore call succeeded
         * and the responseMetadata states that there are more results.
         * </p>
         * <p>
         * The default implementation will look for an element matching the selector
         * '.control-loadmore-no-more-data' and show it.
         * </p>
         * 
         * @param {Object} responseMetadata The response metadata sent by the server side widget
         *            component.
         */
        loadMoreNoMoreData: function(responseMetadata) {
            var noMoreDataNode = this.domNode.getElement('.control-loadmore-no-more-data');
            if (noMoreDataNode) {
                noMoreDataNode.setStyle('display', 'block');
            }
        },
        /**
         * <p>
         * Callback that is called when the loadeMoreMode is not paging, a loadMore call succeeded
         * and the responseMetadata states that there are more results. In case the
         * loadMoreAppendLimit was reached this method will be called before loadMoreLimitReached.
         * </p>
         * <p>
         * The default implementation does nothing.
         * </p>
         * 
         * @param {Object} responseMetadata The response metadata sent by the server side widget
         *            component.
         */
        loadMoreHasMoreData: function(responseMetadata) {
        }
    });
    namespace.addConstructor('LoadMoreSupport', LoadMoreSupport);
})(window.runtimeNamespace);