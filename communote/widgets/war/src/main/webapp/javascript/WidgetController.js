(function(namespace) {
    if (!namespace || !namespace.addConstructor || !namespace.getConstructor) {
        namespace = false;
    }

    function getConstructor(name, namespaceName) {
        var subNamespaces, i, l, ns;
        var classNamespace = window;
        if (namespaceName) {
            subNamespaces = namespaceName.split('.');
            for (i = 0, l = subNamespaces.length; i < l; i++) {
                ns = classNamespace[subNamespaces[i]];
                if (ns) {
                    classNamespace = ns;
                } else {
                    throw new Error('Class "' + name + '" not found because sub-namespace "'
                            + subNamespaces[i] + '" of namespace "' + namespaceName
                            + '" does not exist.');
                }
            }
        } else if (namespace) {
            return namespace.getConstructor(name);
        }
        return classNamespace[name];
    }

    function logWarning(msg) {
        var console = window.console;
        if (console) {
            if (console.warn) {
                console.warn(msg);
            } else if (console.log) {
                console.log('WARN: ' + msg);
            }
        }
    }

    function resolveWidgetType(widgetType, namespaceName, widget, settings) {
        var clazz;
        var renderWidgetType = widget.renderWidgetType;
        if (settings && settings.renderWidgetType) {
            renderWidgetType = settings.renderWidgetType;
        }
        if (renderWidgetType) {
            clazz = getConstructor(renderWidgetType, namespaceName);
            if (clazz && widget instanceof clazz) {
                widgetType = renderWidgetType;
            } else {
                throw new Error('RenderWidgetType "' + renderWidgetType
                        + '" cannot be resolved to an appropriate class');
            }
        }
        return widgetType;
    }

    var WidgetController = new Class( /** @lends WidgetController.prototype */
    {
        refreshBaseUrl: null,
        markLoadingCssClass: 'ajax-loading',

        eventListeners: {},
        filterEventProcessor: null,
        managedWidgets: {},
        /**
         * Default CSS class to add to the widget container when markAsEmpty is called. See method
         * for details.
         */
        markEmptyCssClass: 'cn-empty-widget',
        partialRefreshLoadingCssClass: 'partial-refresh-loading',
        prepareRefreshUrlCallback: undefined,
        /**
         * selector to get the element that has a data attribute that conveys metadata from BE to FE
         */
        responseMetadataSelector: '.control-widget-response-metadata',
        /**
         * currently running partial widget refreshs as a mapping from widgetId to an array of
         * objects where each holds the request, the DOM node and the refresh context
         */
        runningPartialRefreshs: {},
        /** currently running widget refreshs as a mapping from widgetId to request object */
        runningRefreshs: {},
        splitNameValuePattern: /^\s*([^=]+)\s*=\s*([^\r]*)/,
        /*
         * States if the request parameters should be used to instantiate a widget, and if the url
         * mapping should be ignored. If set to true the group id and widget stuff will not be
         * appended to the url, only the url parameters can be used to get the widget information
         */
        // TODO still needed?
        useRequestParametersForWidget: false,
        /** name of the div class which should be parsed as widget */
        widgetDivCssClass: 'C_Widget',
        widgetDivPattern: null,
        widgetInitConditions: null,
        // default error message when full refresh failed, can be overridden in the instance
        // TODO allow setting a callback for this
        widgetRefreshFailedErrorMessage: '<p>Loading content failed.</p>',
        widgetSettingsAttribute: 'data-cnt-widget-settings',

        /**
         * Create a new WidgetController.
         * 
         * @param {String} refreshBaseUrl URL path to be used in widget refreshs
         * @param {String} [options.widgetDivCssClass] Name of the div class which should be parsed
         *            as widget, defaults to 'C_Widget' if missing
         * @param {String} [options.markLoadingCssClass] CSS class to set to the widget refresh
         *            feedback container, defaults to 'ajax-loading' if not provided
         * @param {boolean} options.useRequestParametersForWidget States if the request parameters
         *            should be used to instantiate a widget, and if the URL mapping should be
         *            ignored. If set to true the group id and type of the widget will not be
         *            appended to the refreshBaseUrl and thus, only the URL parameters can be used
         *            to get the widget information
         * @param {String} [options.dataStoreClass] The name of the class to instantiate as shared
         *            data store. Defaults to 'C_KeyValueStore' when not set or the class does not
         *            exist.
         * @param {String} [options.widgetRefreshFailedErrorMessage] Error message to be shown when
         *            the widget refresh failed.
         * @param {Function} [options.prepareRefreshUrlCallback] Function to be called for
         *            additional refresh URL processing. The function will be passed the prepared
         *            refresh URL which can contain a query string. The callback has to return the
         *            modified URL.
         * @constructs
         * @class Main class of the JavaScript widget framework which manages the lifecycle of a set
         *        of {@link C_Widget} instances. This includes discovery of widget placeholders in
         *        an HTML page, creation and removal of the widgets and doing AJAX requests to fetch
         *        the HTML that should be displayed as the content of the widget.
         */
        initialize: function(refreshBaseUrl, options) {
            var dataStoreConstructor;
            this.refreshBaseUrl = refreshBaseUrl;
            if (options) {
                if (options.widgetDivCssClass) {
                    this.widgetDivCssClass = options.widgetDivCssClass;
                }
                if (options.markLoadingCssClass) {
                    this.markLoadingCssClass = options.markLoadingCssClass;
                }
                if (options.useRequestParametersForWidget != undefined) {
                    this.useRequestParametersForWidget = options.useRequestParametersForWidget;
                }
                if (options.widgetRefreshFailedErrorMessage) {
                    this.widgetRefreshFailedErrorMessage = options.widgetRefreshFailedErrorMessage;
                }
                if (typeof options.prepareRefreshUrlCallback === 'function') {
                    this.prepareRefreshUrlCallback = options.prepareRefreshUrlCallback;
                }
                dataStoreConstructor = options.dataStoreClass
                        && getConstructor(options.dataStoreClass);
            }
            if (dataStoreConstructor) {
                this.dataStore = new dataStoreClass();
            } else {
                this.dataStore = new C_KeyValueStore();
            }
            this.filterEventProcessor = new C_FilterEventProcessor(this.dataStore);
            this.widgetDivPattern = new RegExp("^" + this.widgetDivCssClass
                    + "\\s(\\S+)(\\s+\\S+)?$");
        },

        addPartialRefreshOfWidget: function(widgetId, request, domNode, context) {
            var running;
            if (!this.runningPartialRefreshs.hasOwnProperty(widgetId)) {
                running = [];
                this.runningPartialRefreshs[widgetId] = running;
            } else {
                running = this.runningPartialRefreshs[widgetId];
            }
            // we assume that cancel was called before so there are parallel refreshs with
            // the same DOM node
            running.push({
                request: request,
                domNode: domNode,
                context: context
            });
        },

        /**
         * Create and add a widget of the given type if it does not yet exist.
         * 
         * @param {String|Element} parentNodeId The parent node or the ID of the parent node to add
         *            the widget to. The widget is injected as last child.
         * @param {String} widgetType The class name of the widget to create. The widget class will
         *            be searched in the default namespace or in the namespace given by the string
         *            option settings#widgetClassNamespace. This namespace name can contain dots to
         *            define sub-namespaces. The type will also be added as a CSS class to the
         *            widget container element. If this is not desired a custom CSS class can be
         *            defined with the option settings#widgetDivCssClass.
         * @param {String} widgetId The ID of the widget to create. If this controller already
         *            manages a widget with that ID it will not be created again. The ID will also
         *            be used as the ID attribute of the widget container, thus it should be unique.
         * @param {Object} [settings] Optional settings to configure the widget creation. These
         *            settings will also be passed to the widget constructor.
         */
        addWidget: function(parentNodeId, widgetType, widgetId, settings) {
            var parentNode, div;
            var widget = this.getWidget(widgetId);
            if (!widget || !document.getElementById(widgetId)) {
                parentNode = document.id(parentNodeId);
                div = new Element('div');
                div.addClass(this.widgetDivCssClass);
                if (settings && settings.widgetDivCssClass) {
                    div.addClass(settings.widgetDivCssClass);
                } else {
                    div.addClass(widgetType);
                }
                div.setAttribute('id', widgetId);
                div.inject(parentNode);
            }
            if (!widget) {
                widget = this.createWidget(widgetId, widgetType, div, settings);
            }
            return widget;
        },

        /**
         * Add a condition which needs to be fulfilled before a widget is initialized. The condition
         * can be a string or a function. In case a string is passed the so called named condition
         * has to be fulfilled manually by invoking widgetInitConditionFulfilled with the same
         * string. In case a function is provided it will be invoked when an attempt is made to
         * initialize the widget. This function evaluates a condition to decide whether it is
         * fulfilled or not and should therefore return true or false respectively. The caller of
         * this method should notify the WidgetController when the passed condition is fulfilled by
         * invoking widgetInitConditionFulfilled.
         * 
         * @param {String} widgetId The ID of the widget for which a condition should be added. If
         *            the widget is already initialized the call is ignored.
         * @param {String|Function} condition The named condition or the condition callback that
         *            should be added
         */
        addWidgetInitCondition: function(widgetId, condition) {
            var conditionManager;
            var widgetData = this.managedWidgets[widgetId];
            if (widgetData && widgetData.initialized) {
                logWarning('Cannot add init condition after widget ' + widgetId
                        + ' was initialized');
                return;
            }
            if (!this.widgetInitConditions) {
                this.widgetInitConditions = {};
            }
            conditionManager = this.widgetInitConditions[widgetId];
            if (!conditionManager) {
                conditionManager = new (getConstructor('ConditionManager'))();
                this.widgetInitConditions[widgetId] = conditionManager;
            }
            conditionManager.addCondition(condition);
        },

        /**
         * Cancels a running refresh of a widget. This includes partial and full refreshs.
         * 
         * @param {String} widgetId the ID of the widget whose refresh should be canceled
         * @return true if there was at least one running refresh and it was canceled, false
         *         otherwise
         */
        cancelRefreshsOfWidget: function(widgetId) {
            /** check if there is a running refresh for this widget, when true cancel it */
            var running = this.runningRefreshs;
            var canceled = false;
            if (running.hasOwnProperty(widgetId)) {
                running[widgetId].cancel();
                delete running[widgetId];
                canceled = true;
            }
            canceled = this.removePartialRefreshsOfWidget(widgetId, null, true) || canceled;
            return canceled;
        },

        /**
         * Creates an AJAX loading overlay.
         * 
         * @param {Element} containerElem the element whose content should be overlayed. Can be null
         *            to only create the unconnected overlay.
         * @param {boolean} shown whether to show or hide the element
         * @return the overlay element
         */
        createAjaxLoadingOverlay: function(containerElem, shown) {
            var overlay, loadingImgDiv;
            // avoid stacking of overlays
            var existingOverlays = containerElem.getChildren('.' + this.markLoadingCssClass);
            if (existingOverlays.length) {
                return existingOverlays[0];
            }
            overlay = new Element('div', {
                'class': this.markLoadingCssClass
            });
            loadingImgDiv = new Element('div', {
                'class': this.markLoadingCssClass + '-image',
                'html': '&nbsp;'
            });
            overlay.grab(loadingImgDiv, 'top');
            if (!shown) {
                overlay.setStyle('display', 'none');
            }
            if (containerElem) {
                containerElem.grab(overlay, 'bottom');
            }
            return overlay;
        },

        /**
         * Creates a string with request parameters that all widgets need to send to the server when
         * doing a request for the server side component (e.g. widgetId).
         * 
         * @param {Widget} widget the widget for which the parameter string will be created
         * @return {String} the parameter string starting with '?' character
         */
        createCommonWidgetRequestParameters: function(widget, widgetType) {
            var params = '?';
            params += Hash.toQueryString({
                widget: widgetType,
                widgetGroup: widget.getWidgetGroup(),
                type: widget.getOutputType(),
                /** due to IEs brute caching */
                random: Math.random()
            });
            params += "&widgetId=" + widget.getWidgetId();
            return params;
        },

        createWidget: function(widgetId, widgetType, container, settings) {
            var widgetData;
            var namespaceName = settings && settings.widgetClassNamespace;
            var constructor = getConstructor(widgetType, namespaceName);
            if (!constructor) {
                throw new Error('Constructor "' + widgetType + '" of Widget with ID "' + widgetId
                        + '" does not exist');
            }
            widgetData = {
                className: widgetType,
                classNamespaceName: namespaceName,
                initialized: false,
                refreshOnInit: true,
                type: null,
                widget: null
            };
            if (!settings) {
                settings = {};
            }
            // since constructor exists notify about widget creation. Listeners can use this to easily modify the settings.
            this.sendWidgetLifecycleEvent('onWidgetCreate', widgetId, widgetData, settings);
            // get instance of widget class
            widgetData.widget = new (constructor)(widgetId, container, this, settings);
            // resolve actual widget render type
            widgetData.type = resolveWidgetType(widgetType, namespaceName, widgetData.widget, settings);
            widgetData.refreshOnInit = settings.refreshOnInitialization !== 'false';

            this.managedWidgets[widgetId] = widgetData;
            this.registerWidgetEvents(widgetData.widget);
            this.initWidget(widgetData);
            return widgetData.widget;
        }.protect(),

        /**
         * Builds the request URL to query the server side widget class. The URL will already
         * contain common widget request parameters and the session ID if necessary.
         * 
         * @param {Widget} widget the widget for which the request URL should be created
         * @return {String} the request URL
         */
        createWidgetRequestUrl: function(widget, widgetType) {
            var widgetGroup;
            var url = this.refreshBaseUrl;
            if (!this.useRequestParametersForWidget) {
                widgetGroup = widget.getWidgetGroup();
                if (widgetGroup) {
                    url += widgetGroup + '/';
                }
                url += widgetType + '.widget';
            }
            url += this.createCommonWidgetRequestParameters(widget, widgetType);
            if (this.prepareRefreshUrlCallback) {
                url = this.prepareRefreshUrlCallback.call(null, url);
            }
            return url;
        },

        destroyAjaxLoadingOverlay: function(containerElem) {
            containerElem.getElements('div.' + this.markLoadingCssClass).dispose();
        },

        /**
         * Look for a node which contains some metadata that the server-side widget component wants
         * to transmit to the JavaScript component. The metadata is encode in JSON and stored in a
         * data attribute of that node.
         * 
         * @param {Widget} widget The widget instance
         * @param {Element} startNode The node to start from when looking for the metadata providing
         *            node
         * @param {Boolean} remove Whether to remove the metadata providing node
         * @return {Object} The object containing the metadata. The object will be empty if no
         *         metadata was found.
         */
        extractResponseMetadata: function(widget, startNode, remove) {
            var metadata, metadataNode, metadataAttribute;
            metadataNode = startNode.getElement(this.responseMetadataSelector);
            if (metadataNode) {
                metadataAttribute = metadataNode.getAttribute('data-widget-metadata');
                if (metadataAttribute) {
                    try {
                        metadata = JSON.decode(metadataAttribute, true);
                    } catch (e) {
                        throw new Error('Widget metadata of widget ' + widget.widgetId
                                + ' is not valid JSON: ' + metadataAttribute);
                    }
                }
                if (remove) {
                    metadataNode.dispose();
                }
            }
            // never return null
            if (!metadata) {
                return {};
            }
            return metadata;
        },

        extractWidgetSettings: function(widgetContainer) {
            var settings, content, lines, i, line;
            var dataAttribute = widgetContainer.getProperty(this.widgetSettingsAttribute);
            // take settings from data attribute if defined, otherwise use the old method that
            // looks for a child node that is a comment and parses the content of the comment
            if (dataAttribute != undefined) {
                if (dataAttribute.length > 0) {
                    // sanitize the content of the data attribute 
                    if (dataAttribute.charAt(0) != '{') {
                        dataAttribute = '{' + dataAttribute + '}';
                    }
                    settings = JSON.decode(dataAttribute);
                }
            } else if (widgetContainer.firstChild && widgetContainer.firstChild.data) {
                content = widgetContainer.innerHTML;
                content = content.replace(/<br>/gi, "\n");
                lines = content.split("\n");
                settings = {};
                for (i = 0; i < lines.length; i++) {
                    line = lines[i];
                    if (this.splitNameValuePattern.exec(line)) {
                        settings[RegExp.$1] = RegExp.$2.trim();
                    }
                }
            }
            return settings;
        },

        /**
         * extract widget type out of the class attribute of the given node. Expects the marker CSS
         * class to be the first in the class list.
         */
        extractWidgetType: function(widgetNode) {
            var sClass = widgetNode.getProperty("class");
            var sType = '';
            if (this.widgetDivPattern.exec(sClass)) {
                sType = RegExp.$1;
            }
            return sType;
        },

        /**
         * Find and create all widgets within a given node. An element is considered to be a widget
         * if its ID attribute is set and has the CSS class #widgetDivCssClass. The value of the ID
         * attribute will become the ID of the widget. If there is already a widget with the ID the
         * new one will be ignored.
         * 
         * 
         * @param {Element} [parentNode] The DOM node whose children should be searched for widgets.
         *            If omitted the whole document will be scanned.
         * @return {String[]} the IDs of the created widgets
         */
        findWidgets: function(parentNode) {
            var constructor, len, i, container, widgetId, widget, settings;
            var createdWidgetIds = [];
            var widgetNodes = [];
            if (parentNode) {
                widgetNodes = parentNode.getElements('.' + this.widgetDivCssClass);
            } else {
                widgetNodes = document.getElements('.' + this.widgetDivCssClass);
            }
            // iterate over all found widgets
            len = widgetNodes.length;
            for (i = 0; i < len; i++) {
                container = widgetNodes[i];
                widgetId = container.getProperty('id');
                if (widgetId) {
                    widget = this.getWidget(widgetId);
                    if (!widget) {
                        settings = this.extractWidgetSettings(container);
                        widget = this.createWidget(widgetId, this.extractWidgetType(container),
                                container, settings);
                        createdWidgetIds.push(widget.widgetId);
                    }
                }
            }
            return createdWidgetIds;
        },

        /**
         * @return {KeyValueStore} an instance of KeyValueStore to allow sharing data
         */
        getDataStore: function() {
            return this.dataStore;
        },

        /**
         * Returns the filter event processor instance.
         * 
         * @return {C_FilterEventProcessor} the event processor
         */
        getFilterEventProcessor: function() {
            return this.filterEventProcessor;
        },

        /**
         * Returns the indexes of all running partial refreshs of the given widget.
         * 
         * @param {String} widgetId id of the widget
         * @param {Element} [domNode] the domNode the running refresh is trying to update, if null
         *            all running partial refreshs of the widget will be considered
         */
        getPartialRefreshsOfWidget: function(widgetId, domNode) {
            var running, found, i;
            if (this.runningPartialRefreshs.hasOwnProperty(widgetId)) {
                found = [];
                running = this.runningPartialRefreshs[widgetId];
                for (i = 0; i < running.length; i++) {
                    if (domNode && running[i].domNode != domNode) {
                        continue;
                    }
                    found.push(i);
                }
            }
            return found;
        },

        /**
         * see #setUseRequestParametersForWidget
         * 
         * @return useRequestParametersForWidget
         */
        getUseRequestParametersForWidget: function() {
            return this.useRequestParametersForWidget;
        },

        /**
         * Return a widget that is managed by this WidgetController
         * 
         * @param {String} id The ID of the widget to return
         * @return {Widget} the found widget or undefined
         */
        getWidget: function(id) {
            var widgetData = this.managedWidgets[id];
            if (widgetData) {
                return widgetData.widget;
            }
            return undefined;
        },

        /**
         * Return the (render) type of a widget that is managed by this WidgetController
         * 
         * @param {String} id The ID of the widget to return
         * @return {Widget} the found widget or undefined
         */
        getWidgetType: function(id) {
            var widgetData = this.managedWidgets[id];
            if (widgetData) {
                return widgetData.type;
            }
            return undefined;
        },

        handleFirstDOMLoad: function(widget) {
            if (!widget.firstDOMLoadDone) {
                widget.onFirstDOMLoad();
                widget.firstDOMLoadDone = true;
                return true;
            }
            return false;
        },

        initWidget: function(widgetData) {
            var widget, widgetId, conditionManager;
            if (widgetData.initialized) {
                return;
            }
            widget = widgetData.widget;
            widgetId = widget.widgetId;
            conditionManager = this.widgetInitConditions && this.widgetInitConditions[widgetId];
            if (conditionManager && !conditionManager.conditionsFulfilled()) {
                return;
            }
            widget.init();
            widgetData.initialized = true;
            this.sendWidgetLifecycleEvent('onWidgetInitComplete', widgetId, widgetData);
            if (widgetData.refreshOnInit === true) {
                // show, will trigger refresh
                widget.show();
            } else {
                widget.hide();
            }
            // remove unnecessary field
            delete widgetData.refreshOnInit;
        }.protect(),

        /**
         * Mark a widget as empty by adding a CSS class to the DOM node of the widget. The CSS class
         * to add will be the value of the widget setting markEmptyCssClass or if this setting is
         * not set the value the local member markEmptyCssClass. This method will be called
         * automatically after a refresh if the widget has the setting markEmptyIfNoContent set to
         * true and the response metadata contains the noContent flag with value true.
         * 
         * @param {Widget} widget The widget to mark as empty.
         */
        markAsEmpty: function(widget) {
            var cssClass;
            if (!widget) {
                return;
            }
            cssClass = widget.getStaticParameter('markEmptyCssClass') || this.markEmptyCssClass;
            widget.domNode.addClass(cssClass);
        },

        /**
         * Does a partial refresh of a widget. A partial refresh will only replace a specific DOM
         * node inside the widgets body and not the complete widget. This method will call the
         * widget's partialRefreshStart method before starting the refresh. When the refresh
         * succeeded the methods partialRefreshBeforeRender and partialRefreshComplete will be
         * invoked. The first callback is triggered just before inserting the response into the DOM
         * and the other afterwards. In case the partial refresh failed partialRefreshFailed is
         * invoked.
         * 
         * @param {Object} descr object holding details for the refresh. This object has the
         *            following members
         * @param {Widget} descr.widget The widget to be partially refreshed
         * @param {Element} descr.domNode The DOM node to be refreshed
         * @param {String} [descr.insertMode] Defines how the server response should be inserted.
         *            The following modes are supported: 'insert' insert the response into the node
         *            and replacing any content (default mode), 'replace' to replace the node
         *            completely with the response, 'before' to insert the response before the node
         *            and 'after' to insert the node after the node
         * @param {Boolean} [descr.renderEmpty] Defines whether to render the server response when
         *            the responseMetadata entry numberOfElementsContained states (with a value of
         *            0) that the response contains non of the requested items (but for instance an
         *            error message). This option has no effect on the callbacks of the widget that
         *            is they will still be invoked. The default is false. If
         *            numberOfElementsContained is not contained in the responseMetadata this
         *            setting is ignored.
         * @param {Boolean} [descr.showLoadingOverlay] Whether to create and show an overlay that
         *            covers the node to refresh while refreshing.
         * @param {Boolean} [descr.markLoading] Whether to add a CSS class to the node to refresh to
         *            mark it as being loading while the refresh is running.
         * @param {String} descr.queryString A string with request parameters which must not start
         *            with the '?' or '&' character
         * @param {Object} [descr.context] An object with arbitrary content
         */
        partialRefresh: function(descr) {
            var request, url;
            var widgetId = descr.widget.widgetId;
            var widgetType = this.getWidgetType(widgetId);
            if (!widgetType) {
                throw new Error('Widget with ID ' + widgetId
                        + ' is not managed by this controller.');
            }
            // ignore if the widget's first DOM load didn't finish yet or
            // a full widget refresh is running
            if (!descr.widget.firstDOMLoadDone || this.refreshOfWidgetRunning(widgetId)) {
                return;
            }
            if (!descr.domNode) {
                throw "The DOM node must not be null";
            }
            // stop running partial refreshs that try to update the same DOM node
            this.removePartialRefreshsOfWidget(widgetId, descr.domNode, true);
            descr.widget.partialRefreshStart(descr.domNode, descr.context);
            this.partialRefreshCreateLoadingFeedback(descr);
            url = this.createWidgetRequestUrl(descr.widget, widgetType);
            if (descr.queryString.length != 0) {
                url += '&';
                url += descr.queryString;
            }
            request = new Request({
                url: url,
                method: 'get'
            });
            request.addEvent('complete', this.partialRefreshComplete.bind(this, request, descr));
            this.addPartialRefreshOfWidget(widgetId, request, descr.domNode, descr.context);
            request.send();
        },

        /**
         * Called when a partial refresh completed.
         * 
         * @param {Request} request The request of the partial refresh
         * @param {Object} descr The request descriptor passed to partialRefresh method.
         * @param {String} response The response, typically HTML, returned from the server
         */
        partialRefreshComplete: function(request, descr, response) {
            var tempNode, addedNodes, i, responseMetadata, mode, tempDescr;
            if (this.refreshSucceeded(request)) {
                tempNode = new Element('div');
                tempNode.set('html', response);
                responseMetadata = this.extractResponseMetadata(descr.widget, tempNode, true);
                tempDescr = {
                    insertMode: descr.insertMode,
                    domNode: descr.domNode,
                    context: descr.context
                };
                descr.widget.partialRefreshBeforeRender(tempDescr, tempNode, responseMetadata);
                // the callback might have modified the insertMode after checking the response
                mode = tempDescr.insertMode;
                if (descr.renderEmpty || responseMetadata.numberOfElementsContained == undefined
                        || responseMetadata.numberOfElementsContained > 0) {
                    if (mode === 'before' || mode === 'after' || mode === 'replace') {
                        addedNodes = tempNode.getChildren();
                        // always the original descr when removing the feedback and not the possibly
                        // updated values from the callback invocation
                        this.partialRefreshRemoveLoadingFeedback(descr);
                        if (mode === 'after') {
                            for (i = addedNodes.length - 1; i >= 0; i--) {
                                addedNodes[i].inject(tempDescr.domNode, 'after');
                            }
                        } else {
                            for (i = 0; i < addedNodes.length; i++) {
                                addedNodes[i].inject(tempDescr.domNode, 'before');
                            }
                        }
                        if (mode === 'replace') {
                            // remove any event listeners to avoid memory leaks
                            tempDescr.domNode.destroy();
                        }
                    } else {
                        // remove any event listeners to avoid memory leaks
                        tempDescr.domNode.getChildren().destroy();
                        tempDescr.domNode.set('html', tempNode.get('html'));
                        this.partialRefreshRemoveLoadingFeedback(descr);
                    }
                } else {
                    this.partialRefreshRemoveLoadingFeedback(descr);
                }
                tempNode.dispose();
                descr.widget.partialRefreshComplete(addedNodes || tempDescr.domNode, descr.context,
                        responseMetadata);
            } else {
                this.partialRefreshRemoveLoadingFeedback(descr);
                descr.widget.partialRefreshFailed(descr.domNode, descr.context);
            }
            this.removePartialRefreshsOfWidget(descr.widget.widgetId, descr.domNode, false);
        },
        /**
         * Show loading feedback for a partial refresh. The refresh descriptor describes what kind
         * of feedback should be shown.
         * 
         * @param {Object} descr The object with details about the partial refresh. The relevant
         *            members are domNode, showLoadingOverlay and markLoading. For details see the
         *            documentation at method partialRefresh.
         */
        partialRefreshCreateLoadingFeedback: function(descr) {
            var i;
            if (descr.showLoadingOverlay) {
                if (typeOf(descr.domNode) == 'elements') {
                    for (i = 0; i < descr.domNode.length; i++) {
                        this.createAjaxLoadingOverlay(descr.domNode[i], true);
                    }
                } else {
                    this.createAjaxLoadingOverlay(descr.domNode, true);
                }
            }
            if (descr.markLoading) {
                descr.domNode.addClass(this.partialRefreshLoadingCssClass);
            }
        },
        /**
         * Remove loading feedback for a partial refresh. The refresh descriptor describes what kind
         * of feedback was shown and should no be removed.
         * 
         * @param {Object} descr The object with details about the partial refresh. The relevant
         *            members are domNode, showLoadingOverlay and markLoading. For details see the
         *            documentation at method partialRefresh.
         */
        partialRefreshRemoveLoadingFeedback: function(descr) {
            if (descr.showLoadingOverlay) {
                this.destroyAjaxLoadingOverlay(descr.domNode);
            }
            if (descr.markLoading) {
                descr.domNode.removeClass(this.partialRefreshLoadingCssClass);
            }
        },

        /**
         * @return whether a complete refresh of the named widget is running
         */
        refreshOfWidgetRunning: function(widgetId) {
            return this.runningRefreshs.hasOwnProperty(widgetId);
        },

        /**
         * Return whether a partial or full refresh of a widget succeeded.
         * 
         * @param {Request} request The request that was sent to refresh the widget
         * @return {Boolean} True if the refresh succeeded that is the response has status code of
         *         200 and if the response header X-APPLICATION-RESULT is set its value is OK.
         *         Otherwise false is returned.
         */
        refreshSucceeded: function(request) {
            var appSuccess = request.getHeader('X-APPLICATION-RESULT');
            return request.status == 200 && (!appSuccess || appSuccess == 'OK');
        },

        /**
         * refresh the widget
         */
        refreshWidget: function(widget) {
            this.refreshWidgetByMethod(widget, false, true);
        },

        /**
         * refresh the widget and say if it is a submit or not
         */
        refreshWidgetByMethod: function(widget, isSubmit, replaceHtmlWithResponse) {
            var widgetUrl, request, checkDelayedWidgets, loadingFeedback, widgetType;
            var widgetId = widget.widgetId;
            var widgetData = this.managedWidgets[widgetId];
            if (!widgetData) {
                throw new Error('Widget with ID ' + widgetId
                        + ' is not managed by this controller.');
            }
            if (!widgetData.initialized) {
                throw new Error('Widget with ID ' + widgetId + ' is not initialized.');
            }
            widgetType = widgetData.type;
            if (replaceHtmlWithResponse == null) {
                replaceHtmlWithResponse = true;
            }
            // inform widget that refresh is about to start and show loading feedback
            widget.refreshStart();
            loadingFeedback = widget.getStaticParameter('noLoadingFeedbackOnRefresh') !== 'true';
            if (loadingFeedback) {
                this.startLoading(widget);
            }
            widgetUrl = this.createWidgetRequestUrl(widget, widgetType);
            this.cancelRefreshsOfWidget(widgetId);
            request = new Request({
                url: widgetUrl,
                method: isSubmit ? 'post' : 'get',
                data: widget.getQueryString(isSubmit)
            });
            request.addEvent('complete', function(response, xml) {
                var runningRefreshs, responseMetadata, tempNode;
                runningRefreshs = this.runningRefreshs;
                if (runningRefreshs.hasOwnProperty(widgetId)) {
                    delete runningRefreshs[widgetId];
                }
                // TODO use mootools destroy() before replacing html content to avoid memory leaks or
                // is it too expensive to iterate all elements (might be several thousands)?

                // refresh content if requested and 200 status code (so we do not insert an error page)
                if (replaceHtmlWithResponse && request.status == 200) {
                    widget.domNode.set('html', response);
                    responseMetadata = this.extractResponseMetadata(widget, widget.domNode, true);
                    if (widget.getStaticParameter('markEmptyIfNoContent') === true) {
                        if (responseMetadata.noContent === true) {
                            this.markAsEmpty(widget);
                        } else {
                            this.unmarkAsEmpty(widget);
                        }
                    }
                } else {
                    responseMetadata = {};
                    if (replaceHtmlWithResponse) {
                        // TODO make it more flexible: let developers add a callback function 
                        widget.domNode.set('html', this.widgetRefreshFailedErrorMessage);
                    }
                }
                if (loadingFeedback) {
                    this.stopLoading(widget);
                }
                responseMetadata.applicationSuccess = this.refreshSucceeded(request);
                // TODO probably better to call a separate method when the refresh failed (analogous to
                // partialRefresh), but this is currently problematic w.r.t. FormWidgets
                widget.refreshComplete(responseMetadata);
                // inform widget about first DOM load in case it is was the first
                this.handleFirstDOMLoad(widget);

                this.sendWidgetLifecycleEvent('onWidgetRefreshComplete', widgetId, widgetData);
            }.bind(this));
            this.runningRefreshs[widgetId] = request;
            request.send();
        },

        registerWidgetEventListener: function(eventName, listener) {
            var listeners = this.eventListeners[eventName];
            /* create event array if not exists */
            if (!listeners) {
                listeners = [];
                this.eventListeners[eventName] = listeners;
            }
            /* add listener to event list */
            listeners[listeners.length] = listener;
        },

        registerWidgetEvents: function(widget) {
            var widgetEvents, i, eventName;
            widgetEvents = widget.getListeningEvents();
            if (widgetEvents) {
                for (i = 0; i < widgetEvents.length; i++) {
                    eventName = widgetEvents[i];
                    this.registerWidgetEventListener(eventName, widget);
                }
            }
        }.protect(),

        /**
         * Completely removes a widget and all contained inner widgets. The widgets will be removed
         * from the event listeners, the widget registry and finally their DOM nodes will be
         * deleted. By implementing the beforeRemove method, each widget gets the chance to do
         * clean-up operations before being removed.
         * 
         * @param {Widget} widget the widget to remove
         */
        removeWidget: function(widget) {
            var innerWidgets;
            var widgetId = widget.widgetId;
            var widgetData = this.managedWidgets[widgetId];
            if (!widgetData) {
                throw new Error('Widget with ID ' + widgetId
                        + ' is not managed by this controller.');
            }
            this.cancelRefreshsOfWidget(widgetId);
            this.sendWidgetLifecycleEvent('onWidgetRemove', widgetId, widgetData);
            /* give widget the chance to cleanup */
            widget.beforeRemove();

            // remove all embedded widgets
            innerWidgets = widget.domNode.getElements('.' + this.widgetDivCssClass);
            innerWidgets.each(function(container) {
                var widgetId = container.get('id');
                widgetController.removeWidgetById(widgetId);
            });
            delete this.managedWidgets[widgetId];
            if (this.widgetInitConditions) {
                delete this.widgetInitConditions[widgetId];
            }

            /* remove the widget from the listening events */
            widget.getListeningEvents().each(function(eventName) {
                this.unregisterWidgetEventListener(eventName, widget);
            }, this);

            /* delete the dom node of the widget */
            if (widget.domNode && widget.domNode.getParent()) {
                // TODO use mootools destroy() to avoid memory leaks or is it too expensive to
                // iterate all elements (might be several thousands)?
                widget.domNode.getParent().removeChild(widget.domNode);
            }
            this.sendWidgetLifecycleEvent('onWidgetRemoveComplete', widgetId, widgetData);
        },

        /**
         * Completely removes a widget and all contained inner widgets. This methods delegates to
         * #removeWidget(widget) after resolving the widgetId. If the provided ID does not belong to
         * a registered widget nothing will happen.
         * 
         * @param {String} widgetId ID of the widget to remove
         */
        removeWidgetById: function(widgetId) {
            var widget = this.getWidget(widgetId);
            if (widget) {
                this.removeWidget(widget);
            }
        },

        removePartialRefreshsOfWidget: function(widgetId, domNode, cancel) {
            var i, running, refresh, widget;
            var matching = this.getPartialRefreshsOfWidget(widgetId, domNode);
            if (matching) {
                widget = this.getWidget(widgetId);
                running = this.runningPartialRefreshs[widgetId];
                for (i = 0; i < matching.length; i++) {
                    if (cancel) {
                        refresh = running[matching[i]];
                        refresh.request.cancel();
                        this.partialRefreshRemoveLoadingFeedback(refresh.context);
                        widget.partialRefreshCanceled(refresh.context);
                    }
                    running.splice(matching[i], 1);
                }
                if (running.length == 0) {
                    delete this.runningPartialRefreshs[widgetId];
                }
                return matching.length > 0;
            }
            return false;
        },

        sendEvent: function(eventName, sendingWidgetId, params) {
            var i, l, listener;
            var sendingWidget = sendingWidgetId ? this.getWidget(sendingWidgetId) : null;
            var widgetListenerGroupId = sendingWidget ? sendingWidget.getWidgetListenerGroupId()
                    : null;
            var listeners = this.eventListeners[eventName];
            if (listeners) {
                for (i = 0, l = listeners.length; i < l; i++) {
                    listener = listeners[i];
                    if (!widgetListenerGroupId || !listener.getWidgetListenerGroupId
                            || widgetListenerGroupId == listener.getWidgetListenerGroupId()) {
                        listener.handleEvent(eventName, params);
                    }
                }
            }
        },
        /**
         * Send an event to notify listeners about a change in the lifecycle of a widget (init,
         * refresh, remove, ...). The event won't be send to the widget whose lifecycle changed.
         * 
         * @param {String} eventName Name of the event that describes the change of the lifecycle
         * @param {String} sendingWidgetId The ID of the widget whose lifecycle changed.
         * @param {Object} sendingWidgetData The data from the managedWidegts data holder of the
         *            widget whose lifecycle changed.
         * @param {Object} [eventData] Additional event data to send along
         * @private
         */
        sendWidgetLifecycleEvent: function(eventName, sendingWidgetId, sendingWidgetData, eventData) {
            var i, l, listener, sendingWidget;
            var listeners = this.eventListeners[eventName];
            if (listeners) {
                sendingWidget = sendingWidgetData.widget;
                for (i = 0, l = listeners.length; i < l; i++) {
                    listener = listeners[i];
                    if (sendingWidget != listener) {
                        listener.handleEvent(eventName, {
                            widgetClassName: sendingWidgetData.className,
                            widgetClassNamespaceName: sendingWidgetData.classNamespaceName,
                            widgetId: sendingWidgetId,
                            widgetType: sendingWidgetData.type,
                            data: eventData
                        });
                    }
                }
            }
        },

        /**
         * Adds a loading overlay to the given widget.
         * 
         * @param widget The widget.
         */
        startLoading: function(widget) {
            /** attach ajax hidden params class */
            this.createAjaxLoadingOverlay(widget.domNode, true);
        },

        stopLoading: function(widget) {
            this.destroyAjaxLoadingOverlay(widget.domNode);
        },

        /**
         * Remove the CSS class added by markAsEmpty.
         * 
         * @param {Widget} widget The widget to mark as empty.
         */
        unmarkAsEmpty: function(widget) {
            var cssClass;
            if (!widget) {
                return;
            }
            cssClass = widget.getStaticParameter('markEmptyCssClass') || this.markEmptyCssClass;
            widget.domNode.removeClass(cssClass);
        },

        /**
         * Remove an event listener.
         */
        unregisterWidgetEventListener: function(eventName, listener) {
            var cleanedListeners, i, l;
            var listeners = this.eventListeners[eventName];
            if (listeners) {
                // take care that a running send-event operation is not disturbed
                // by concurrently modifying the same array 
                cleanedListeners = [];
                for (i = 0, l = listeners.length; i < l; i++) {
                    if (listeners[i] !== listener) {
                        cleanedListeners.push(listeners[i]);
                    }
                }
                if (listeners.length > cleanedListeners.length) {
                    this.eventListeners[eventName] = cleanedListeners;
                }
            }
        },

        /**
         * Notify the widgetController that a widget initialization condition which was added with
         * addWidgetInitCondition is fulfilled. The controller will initialize the widget if all
         * conditions are fulfilled. The condition callbacks will be called in order of
         * registration. If a condition fails the remaining are not evaluated.
         * 
         * @param {String} widgetId The ID of the widget for which an init condition is fulfilled
         * @param {String} [condition] The named condition that was fulfilled. If a condition
         *            callback was fulfilled this argument can be ignored.
         */
        widgetInitConditionFulfilled: function(widgetId, condition) {
            var conditionManager;
            var widgetData = this.managedWidgets[widgetId];
            if (this.widgetInitConditions && widgetData && !widgetData.initialized) {
                conditionManager = this.widgetInitConditions[widgetId];
                if (conditionManager) {
                    conditionManager.fulfillCondition(condition);
                    this.initWidget(widgetData);
                }
            }
        }
    });
    if (namespace) {
        namespace.addConstructor('WidgetController', WidgetController);
    } else {
        window.WidgetController = WidgetController;
    }
})(this.runtimeNamespace);
