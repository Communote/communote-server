var C_Widget = new Class( /** @lends C_Widget.prototype */
{
    domNode: null,
    widgetController: {},
    widgetId: "",
    outputType: "DHTML",
    filterParams: {},
    staticParams: {},
    // whether the widget is shown, be careful to not override it in a subclass unintentionally
    widgetShown: null,
    // whether the widget is dirty; a widget becomes dirty when it is refreshed while not being shown
    widgetDirty: true,
    /* parameter defines if the first refresh has been completed or not */
    firstDOMLoadDone: false,

    /* override this group in the certain widget - this is used in the URL */
    widgetGroup: "noGroup",
    /* the group the widget will listen too and which will be used to match for events */
    widgetListenerGroupId: null,
    
    /**
     * Override the widget type with another type. This allows reuse of the Java widget class
     * implementation but overriding JS logic. The type must be a super class of the current class.
     * This type can also be set as a parameter but will only be evaluated during creation of the widget.
     */
    renderWidgetType: null,

    /**
     * Creates a new widget and calls its {@link #setup} method. A widget should usually be
     * constructed by the WidgetController which manages the widget
     * 
     * @param {String} widgetId The ID the widget should get
     * @param {String|Element} domNode The ID or the element of the DOM node into which the server
     *            response HTML of a refresh should be added.
     * @param {C_WidgetController} widgetController The widget controller that manages the widget
     * @param {Object} params An object containing the static parameters/configuration options of the widget
     * @constructs
     * @class Base class for the JavaScript component of a widget. Custom widgets will have to
     *        inherit from this class.
     */
    initialize: function(widgetId, domNode, widgetController, params) {
        this.domNode = document.id(domNode);
        this.widgetId = widgetId;
        this.filterParams = {};
        if (params) {
            this.staticParams = params;
        } else {
            this.staticParams = {};
        }
        this.widgetController = widgetController;

        // call the simplified setup hook
        this.setup();
    },

    /**
     * Whether to automatically refresh the widget after initialization. This method returns true by
     * default. To prevent the refresh set the static parameter 'refreshOnInitialization' to
     * 'false'. You can also pass a widgetId of another widget which will lead to delaying the
     * initial refresh of the widget until the other widget was refreshed for the first time.
     * 
     * @return {boolean|string} true if the widget should be refreshed after initialization, false
     *         or an widgetId otherwise
     */
    getRefreshOnInitialization: function() {
        return this.refreshOnInit;
    },

    /**
     * is called on object construction
     */
    setup: function() {
    },

    /**
     * is called before the first refresh is executed
     */
    init: function() {
        this.widgetListenerGroupId = this.getStaticParameter('widgetListenerGroupId');
        // reset shown and dirty state just in case someone called show or hide to early
        this.widgetDirty = true;
        this.widgetShown = null;
    },

    /**
     * Handler method called after the widget has been refreshed and the refresh has been
     * successful.
     * 
     * @return true if the widget refresh process should be continued as usual, that is replace the
     *         resulting div with the old one.
     */
    onWidgetRefreshSuccess: function(response) {
        return true;
    },

    getWidgetTemplateParameters: function() {

        var params = this.domNode.getElements('.widgetParameters');
        var arrParams = {};

        if (params && params[0]) {
            params[0].getElements('div').each(function(tag) {
                arrParams[tag.getProperty("title")] = tag.innerHTML;
            });
        }

        return arrParams;
    },

    /* override this hook to add your own events */
    getListeningEvents: function() {
        return [ 'onRefresh' ];
    },

    handleEvent: function(eventId, params) {
        var fn = this[eventId];
        if (typeOf(fn) !== 'function') {
            if (window.console && console.error) {
                console.error('event registered, but handler not defined, event: ' + eventId + ', widget: '
                    + this.widgetId);
            }
        } else {
            /* dynamically call the event method */
            fn.call(this, params);
        }
    },

    /**
     * Show the widget if it is hidden. In case the widget got dirty while it was hidden it will
     * automatically refreshed. For custom operations while showing the widget, subclasses can
     * override the beforeShow or afterShow callbacks.
     */
    show: function() {
        var initPhase, dirty;
        if (this.widgetShown !== true) {
            dirty = this.widgetDirty;
            initPhase = this.widgetShown == null;
            this.beforeShow(initPhase, dirty);
            this.widgetShown = true;
            if (dirty) {
                this.refresh();
            }
            this.domNode.setStyle('display', '');
            this.afterShow(initPhase, dirty);
        }
    },

    /**
     * Hide the widget if it is shown. In case the refresh method is called on the widget while it
     * is hidden, it won't refresh but mark itself as dirty to refresh the next time it is shown.
     * For custom operations while hiding the widget, subclasses can override the beforeHide
     * callback.
     */
    hide: function() {
        if (this.widgetShown !== false) {
            this.beforeHide(this.widgetShown == null);
            this.widgetShown = false;
            this.domNode.setStyle('display', 'none')
        }
    },

    /**
     * @return {Boolean} whether the widget is currently visible
     */
    isHidden: function() {
        return this.widgetShown !== true;
    },

    /**
     * Callback that is invoked before the widget is shown.
     * 
     * @param {Boolean} initPhase Is true if the method is invoked while the widget is still initializing
     * @param {Boolean} isDirty Denotes whether the widget is dirty and will be refreshed after this
     *            call
     */
    beforeShow: function(initPhase, isDirty) {
    },
    /**
     * Callback that is invoked after the widget was shown.
     * 
     * @param {Boolean} initPhase Is true if the method is invoked while the widget is still initializing
     * @param {Boolean} wasDirty Denotes whether the widget was dirty and is currently refreshing
     */
    afterShow: function(initPhase, wasDirty) {
    },
    /**
     * Callback that is invoked before the widget is hidden.
     * 
     * @param {Boolean} initPhase Is true if the method is invoked while the widget is still initializing
     */
    beforeHide: function(initPhase) {
    },

    /**
     * Does a full refresh of the widget. In case the widget is hidden, it won't refresh but mark
     * itself as dirty.
     */
    refresh: function() {
        if (this.widgetShown) {
            this.widgetController.refreshWidget(this);
            this.widgetDirty = false;
        } else {
            this.widgetDirty = true;
        }
    },

    /**
     * Overrideable hook that is invoked before a full refresh of the widget starts.
     */
    refreshStart: function() {
    },

    /**
     * Overrideable hook that is invoked after a full refresh of the widget completed. The default
     * implementation will search for sub-widgets and fire the onWidgetRefreshComplete event.
     * 
     * @param {Object} responseMetadata An object that might contain arbitrary metadata the
     *            server-side component of the widget wants to transmit to the JavaScript component.
     *            The object will at least contain the member applicationSuccess which denotes
     *            whether the refresh was successful.
     */
    refreshComplete: function(responseMetadata) {
        // load eventually existing sub widgets
        // TODO shouldn't do this by default!
        this.widgetController.findWidgets(this.domNode);
    },

    /**
     * Called after the first refresh has been completed and DOM of the widget is set. Is called
     * before refreshComplete
     */
    onFirstDOMLoad: function() {
    },

    /**
     * Triggers the refresh of a single DOM node within a widget. The refresh will do a request to
     * the server side widget class and write the response to the specified DOM node.
     * 
     * @param {Object} refreshDescr Object describing the refresh which contains the following
     *            members:
     * @param {Element|String} refreshDescr.domNode The DOM node to refresh
     * @param {Boolean} [refreshDescr.insertMode] Defines how the server response should be
     *            inserted. The following modes are supported: 'insert' insert the response into the
     *            node and replace any content (default mode), 'replace' to replace the node
     *            completely with the response, 'before' to insert the response before the node and
     *            'after' to insert the node after the node
     * @param {Boolean} refreshDescr.includeFilterParams Whether to include the current
     *            filterParameters of the widget
     * @param {Object} [refreshDescr.additionalParams] Optional parameters to be passed to the
     *            server. Parameters that are already contained in the filterParams will be
     *            replaced.
     * @param {Boolean} [descr.showLoadingOverlay] Whether to create and show an overlay that covers
     *            the node to refresh while refreshing. Defaults to true.
     * @param {Boolean} [descr.markLoading] Whether to add a CSS class to the node to refresh to
     *            mark it as being loading while the refresh is running. Defaults to false.
     * @param {Object} [refreshDescr.context] Object with arbitrary content that is passed to the
     *            callback methods during the refresh
     */
    partialRefresh: function(refreshDescr) {
        var showOverlay, domNode, params = '', filterParamsBackup;
        if (refreshDescr.includeFilterParams && !refreshDescr.additionalParams) {
            params = this.getQueryString();
        } else if (!refreshDescr.includeFilterParams) {
            // only add additionalParams
            params = Hash.toQueryString(refreshDescr.additionalParams);
        } else if (refreshDescr.additionalParams) {
            // combine filterParams and additionalParams by merging them to avoid double params in
            // query string with different values
            filterParamsBackup = this.filterParams;
            // TODO will only handle parameters contained in filterParams and additional ones as added
            // by FilterWidget, guess that is currently OK
            this.filterParams = Object.merge({}, this.filterParams, refreshDescr.additionalParams);
            params = this.getQueryString();
            // restore old filter params
            this.filterParams = filterParamsBackup;
        }
        if (refreshDescr.showLoadingOverlay != undefined) {
            showOverlay = refreshDescr.showLoadingOverlay;
        } else {
            showOverlay = true;
        }
        if (typeOf(refreshDescr.domNode) === 'string') {
            domNode = document.id(refreshDescr.domNode);
        } else {
            domNode = refreshDescr.domNode;
        }
        this.widgetController.partialRefresh({
            widget: this,
            domNode: document.id(refreshDescr.domNode),
            insertMode: refreshDescr.insertMode || 'insert',
            queryString: params,
            showLoadingOverlay: showOverlay,
            markLoading: !!refreshDescr.markLoading,
            context: refreshDescr.context
        });
    },

    /**
     * Callback to inform a widget that a partial refresh is about to begin.
     * 
     * @param {Element} domNode The element whose content will be refreshed
     * @param {Object} context The context element of the object that was passed to partialRefresh
     */
    partialRefreshStart: function(domNode, context) {
    },

    /**
     * Callback to inform a widget that a partial refresh succeeded and the response is about to be
     * rendered.
     * 
     * @param {Object} refreshDescr The refresh descriptor that was passed to the partialRefresh
     *            method. This object will only contain a subset of the members passed to that
     *            method which are insertMode, domNode and context.
     * @param {Element} responseWrapper An element that holds the elements that should be inserted
     *            into the DOM
     * @param {Object} responseMetadata An object that might contain arbitrary metadata the
     *            server-side component of the widget wants to transmit to the JavaScript component.
     *            This argument is never null.
     */
    partialRefreshBeforeRender: function(refreshDescr, responseWrapper, responseMetadata) {
    },
    /**
     * Callback to inform a widget that a partial refresh succeeded.
     * 
     * @param {Element|Elements} domNode The element whose content was refreshed or in case the
     *            partial refresh replaced the DOM node this parameter will contain all the inserted
     *            elements
     * @param {Object} context The context element of the object that was passed to partialRefresh
     * @param {Object} responseMetadata An object that might contain arbitrary metadata the
     *            server-side component of the widget wants to transmit to the JavaScript component.
     *            This argument is never null.
     */
    partialRefreshComplete: function(domNode, context, responseMetadata) {
    },

    /**
     * Callback to inform a widget that a partial refresh failed.
     * 
     * @param {Element} domNode the element whose content should have been refreshed
     * @param {object} context the context element of the object that was passed to partialRefresh
     */
    partialRefreshFailed: function(domNode, context) {
    },

    /**
     * Callback to inform a widget that a partial refresh was canceled, for instance by a full
     * refresh of the widget.
     * 
     * @param {object} context the context element of the object that was passed to partialRefresh
     */
    partialRefreshCanceled: function(context) {
    },

    /**
     * overridable hook that is invoked before the widget gets removed from widgetController and DOM
     */
    beforeRemove: function() {
    },

    getWidgetId: function() {
        return this.widgetId;
    },

    getWidgetGroup: function() {
        return this.widgetGroup;
    },

    /**
     * the widget listener group
     */
    getWidgetListenerGroupId: function() {
        return this.widgetListenerGroupId;
    },

    getOutputType: function() {
        return this.outputType;
    },

    setOutputType: function(sType) {
        this.outputType = sType;
    },

    setFilterParameter: function(sParam, sValue) {
        this.filterParams[sParam] = sValue;
    },

    startLoadingFeedback: function() {
        this.widgetController.startLoading(this);
    },
    
    stopLoadingFeedback: function() {
        this.widgetController.stopLoading(this);
    },
    
    unsetFilterParameter: function(sParam) {
        delete this.filterParams[sParam];
    },

    /**
     * Copy the static parameter to the filter parameters if the static parameter is not empty.
     * 
     * @param {String} paramName The name of the static parameter to copy.
     * @param {Number|String} [defaultValue] An optional fallback value to be set when the static
     *            parameter is not set.
     */
    copyStaticParameter: function(paramName, defaultValue) {
        var value = this.getStaticParameter(paramName);
        if (value != undefined && value !== '') {
            this.setFilterParameter(paramName, value);
        } else if (defaultValue != undefined) {
            this.setFilterParameter(paramName, defaultValue);
        }
    },

    // TODO filter parameter isn't the best name because the parameter could just
    // control the rendering. Name it renderParameter or refreshParameter?
    getFilterParameter: function(sParam) {
        return this.filterParams[sParam];
    },

    getAllFilterParameters: function() {
        return this.filterParams;
    },

    setStaticParameters: function(params) {
        if (params) {
            this.staticParams = params;
        }
    },

    setStaticParameter: function(paramName, value) {
        this.staticParams[paramName] = value;
    },

    getStaticParameter: function(paramName) {
        return this.staticParams[paramName];
    },

    getAllStaticParameters: function() {
        return this.staticParams;
    },

    /**
     * Return the parameters that should be included in a full refresh of the widget. The default
     * implementation returns the filterParameters.
     * 
     * @return {Object} the parameters as a key value mapping
     */
    getRefreshParameters: function() {
        return this.filterParams;
    },

    getQueryString: function() {
        return Hash.toQueryString(this.getRefreshParameters());
    },

    onRefresh: function(senderWidget) {
        /* refresh */
        this.refresh();
    },

    /* send the refresh event to the same group */
    doRefreshGroup: function() {
        E2('onRefresh', this.widgetId, this);
    }

});

var TSWidget = C_Widget;
