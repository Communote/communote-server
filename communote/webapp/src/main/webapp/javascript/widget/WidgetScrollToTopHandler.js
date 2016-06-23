(function(namespace, window) {
    var defaultOptions = {
        lowerOffset: 500,
        upperOffset: 0,
        // selector for the child node of the widget's domNode which contains the scroll-to-top link
        selector: '.control-scroll-top'
    };
    /**
     * Helper providing a scroll-to-top feature for widgets which uses the
     * scrollObserver.addElementOffsetListener based on the domNode of the listener.
     */
    var ScrollToTopHandler = function(widget, options) {
        this.widget = widget;
        this.options = Object.merge({}, defaultOptions);
        Object.merge(this.options, options);
    };

    ScrollToTopHandler.prototype.attach = function() {
        var scrollObserver = namespace.scrollObserver;
        if (scrollObserver) {
            if (!this.scrollObserverId) {
                this.scrollObserverId = scrollObserver.observe(document, false);
            }
            if (!this.scrollListenerId) {
                this.scrollListenerId = scrollObserver.addElementOffsetListener(
                        this.scrollObserverId, this.widget.domNode, this.options.lowerOffset, 
                        this.options.upperOffset, this.scrollEventHandler.bind(this));
            }
            scrollObserver.startListener(this.scrollListenerId, true);
        }
    };
    
    ScrollToTopHandler.prototype.detach = function() {
        var scrollObserver = namespace.scrollObserver;
        if (scrollObserver && this.scrollListenerId) {
            scrollObserver.removeListener(this.scrollListenerId);
            this.scrollListenerId = null;
        }
    };

    /**
     * Callback that is invoked when the domNode of the widget scrolls into or out of the viewport.
     * This callback will show or hide the element with CSS class 'control-cpl-scroll-top'.
     * 
     * @param {Boolean} lowerEdgeChanged Whether the lower edge of the observed element changed its
     *            relative position
     * @param {Number} lowerEdgeRelativePos Number with value -1, 0 or 1 that describes the relative
     *            position of the lower edge of the observed element to the viewport.
     * @param {Boolean} upperEdgeChanged Whether the upper edge of the observed element changed its
     *            relative position
     * @param {Number} upperEdgeRelativePos Number with value -1, 0 or 1 that describes the relative
     *            position of the upper edge of the observed element to the viewport.
     */
    ScrollToTopHandler.prototype.scrollEventHandler = function(lowerEdgeChanged, lowerEdgeRelativePos,
            upperEdgeChanged, upperEdgeRelativePos) {
        var elem;
        if (lowerEdgeChanged) {
            elem = this.widget.domNode.getElement(this.options.selector);
            if (!elem) {
                return;
            }
            if (lowerEdgeRelativePos < 0) {
                elem.removeClass('cn-hidden');
            } else {
                elem.addClass('cn-hidden');
            }
        }
    };
    // TODO define lifecycle events for these callbacks and listen to the events?
    /**
     * Function to be called from the afterShow method of the widget.
     */
    ScrollToTopHandler.prototype.widgetAfterShow = function(isDirty) {
        // restart scrollToTop observer if not going to refresh
        if (!isDirty && this.scrollListenerId) {
            namespace.scrollObserver.startListener(this.scrollListenerId, true);
        }
    };
    /**
     * Function to be called from the beforeHide method of the widget.
     */
    ScrollToTopHandler.prototype.widgetBeforeHide = function() {
        // if the scroll to top scroll observer is attached pause it
        if (this.scrollListenerId) {
            namespace.scrollObserver.pauseListener(this.scrollListenerId);
        }
    };
    /**
     * Function to be called when the widget is doing some cleanup, especially when it is going to do a full refresh or is removed.
     */
    ScrollToTopHandler.prototype.widgetCleanup = function() {
        this.detach();
    };
    
    /**
     * Function to be called from the refreshComplete method of the widget.
     */
    ScrollToTopHandler.prototype.widgetRefreshComplete = function() {
        this.attach();
    };
    namespace.classes.WidgetScrollToTopHandler = ScrollToTopHandler;
})(this.runtimeNamespace, this);