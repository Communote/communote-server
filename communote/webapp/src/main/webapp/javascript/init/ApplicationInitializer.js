(function(window) {
    var initConditions;
    var conditionManager;
    var initialized = false;
    var loadEventFired = false;

    function invokeCallbacks(callbacks) {
        var i;
        for (i = 0; i < callbacks.length; i++) {
            callbacks[i].call(null);
        }
    }

    /**
     * Initializes the widget framework, including the filterWidgetGroupRepository.
     */
    function initWidgetFramework() {
        var constructor, widgetController;
        var namespace = communote;
        var baseUrl = namespace.server.applicationUrl + '/widgets/';
        var options = {
            widgetDivCssClass: 'TSWidget',
            markLoadingCssClass: 'ajax-loading',
            useRequestParametersForWidget: false
        };
        options.widgetRefreshFailedErrorMessage = namespace.i18n.getMessage(
                'widget.error.message.refresh.failed', []);
        if (namespace.server.applicationSessionId) {
            options.prepareRefreshUrlCallback = insertSessionId;
        }
        constructor = namespace.getConstructor('WidgetController');
        widgetController = new constructor(baseUrl, options);
        // save data store in global variable cnCache for backwards compatibility
        cnCache = widgetController.getDataStore();
        namespace.filterGroupRepo = {};
        // TODO remove legacy global variable
        window.filterWidgetGroupRepo = namespace.filterGroupRepo;
        namespace.widgetController = widgetController;
        // TODO remove next line after cleaning global scope
        window.widgetController = widgetController;
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

    function testConditions() {
        var i, callbacks;
        if (loadEventFired) {
            if (conditionManager) {
                return conditionManager.conditionsFulfilled();
            }
            return true;
        }
        return false;
    }

    /**
     * Helper to initialize the application. The component provides different hooks to do custom
     * initializations at different points during startup and is exposed in the communote namespace
     * as communote.initializer. If the configuration value of
     * communote.configuration.init.initOnLoad is not false the initApplication method is called
     * automatically on page load.
     */
    communote.initializer = {
        afterInitCallbacks: [],
        beforeInitCallbacks: [],
        widgetFrameworkInitializedCallbacks: [],
        afterWidgetScanCallbacks: [],
        beforeWidgetScanCallbacks: [],
        applicationReadyCallbacks: [],

        /**
         * Add a callback function that will be called after the initialization completed. If the
         * widget framework should be initialized this callback will be invoked after any callback
         * added with addWidgetFrameworkInitializedCallback. If scanning for widgets is enabled too,
         * this callback will be invoked before scanning for widgets.
         * 
         * @param {Function} callback The function to be called
         */
        addAfterInitCallback: function(callback) {
            if (initialized) {
                logWarning('Cannot add AfterInitCallback after application was initialized');
            } else {
                this.afterInitCallbacks.push(callback);
            }
        },
        /**
         * Add a callback function that will be called before doing any kind of initialization.
         * 
         * @param {Function} callback The function to be called
         */
        addBeforeInitCallback: function(callback) {
            if (initialized) {
                logWarning('Cannot add BeforeInitCallback after application was initialized');
            } else {
                this.beforeInitCallbacks.push(callback);
            }
        },

        /**
         * Add a condition that needs to be fulfilled before the application can be initialized. The
         * condition can be a string or a function. If a string is passed the so called named
         * condition needs to be fulfilled manually by invoking initConditionFulfilled with the same
         * string. In case a function is provided it will be invoked when an attempt to initialize
         * the application with initApplication is made. This function evaluates a condition to
         * decide whether it is fulfilled or not and should therefore return true or false
         * respectively. The application initializer should be notified as soon as the condition
         * callback is going to return true with a call to initApplication or initConditionFulfilled
         * without argument.
         * <p>
         * When initApplication is called all added init conditions are checked in registration
         * order. If one is not yet fulfilled the application won't start. If a condition callback
         * returns true it will not be invoked again.
         * </p>
         * 
         * @param {String|Function} condition The named condition or condition callback to check
         *            before starting the application
         */
        addInitCondition: function(condition) {
            if (initialized) {
                logWarning('Cannot add init condition after application was initialized');
            } else {
                if (!conditionManager) {
                    conditionManager = new communote.classes.ConditionManager();
                }
                conditionManager.addCondition(condition);
            }
        },

        /**
         * Add a callback function that will be called after the widget framework was initialized.
         * This callback won't be invoked if the widget framework should not be initialized (boolean
         * configuration option init.widgetFramework).
         * 
         * @param {Function} callback The function to be called
         */
        addWidgetFrameworkInitializedCallback: function(callback) {
            if (initialized) {
                logWarning('Cannot add WidgetFrameworkInitializedCallback after application was initialized');
            } else {
                this.widgetFrameworkInitializedCallbacks.push(callback);
            }
        },

        /**
         * Add a callback function that will be called after scanning for widgets during page load.
         * This callback will only be invoked if the widget framework should be initialized and
         * scanning for widgets wasn't disabled with configuration option init.noWidgetScan.
         * 
         * @param {Function} callback The function to be called
         */
        addAfterWidgetScanCallback: function(callback) {
            if (initialized) {
                logWarning('Cannot add AfterWidgetScanCallback after application was initialized');
            } else {
                this.afterWidgetScanCallbacks.push(callback);
            }
        },

        /**
         * Add a callback function that will be called before scanning for widgets during page load.
         * This callback will only be invoked if the widget framework should be initialized and
         * scanning for widgets wasn't disabled with configuration option init.noWidgetScan.
         * 
         * @param {Function} callback The function to be called
         */
        addBeforeWidgetScanCallback: function(callback) {
            if (initialized) {
                logWarning('Cannot add addBeforeWidgetScanCallback after application was initialized');
            } else {
                this.beforeWidgetScanCallbacks.push(callback);
            }
        },

        /**
         * Add a callback function that will be called after everything (initializations and widget
         * scan if required) is ready. If called after initApplication was called the callback will
         * be invoked directly.
         * 
         * @param {Function} callback The function to be called
         */
        addApplicationReadyCallback: function(callback) {
            if (initialized) {
                // call detached to avoid blocking
                setTimeout(callback, 1);
            } else {
                this.applicationReadyCallbacks.push(callback);
            }
        },

        /**
         * Does the initialization and invokes the registered callbacks.
         */
        initApplication: function() {
            var initConfig, initFramework;
            if (initialized || !testConditions()) {
                return;
            }
            invokeCallbacks(this.beforeInitCallbacks);
            // TODO find a better way to configure smoothbox loading URL
            // set global variable used by the smooth box
            TB_loadingImgUrl = buildResourceUrl('/themes/core/images/main/loading.gif');
            // initializing widget framework is disabled by default
            initConfig = communote.configuration && communote.configuration.init;
            initFramework = initConfig && initConfig.widgetFramework;
            if (initFramework) {
                initWidgetFramework();
                invokeCallbacks(this.widgetFrameworkInitializedCallbacks);
            }
            invokeCallbacks(this.afterInitCallbacks);
            // scanning for widgets is active by default if framework is active
            if (initFramework && !initConfig.noWidgetScan) {
                invokeCallbacks(this.beforeWidgetScanCallbacks);
                communote.widgetController.findWidgets();
                invokeCallbacks(this.afterWidgetScanCallbacks);
            }
            invokeCallbacks(this.applicationReadyCallbacks);
            initialized = true;
            // clear callbacks
            delete this.afterInitCallbacks;
            delete this.afterWidgetScanCallbacks;
            delete this.applicationReadyCallbacks;
            delete this.beforeInitCallbacks;
            delete this.beforeWidgetScanCallbacks;
            delete this.widgetFrameworkInitializedCallbacks;
            initConditions = undefined;
        },

        /**
         * Notify the initializer that a condition which was added with addInitCondition is now
         * fulfilled. This method invokes initApplication and if all conditions are now
         * fulfilled the application will be started.
         * 
         * @param {String} [condition] Name if a named condition is fulfilled. Can be omitted if a
         *            condition callback is fulfilled.
         */
        initConditionFulfilled: function(condition) {
            var idx;
            if (!initialized && conditionManager) {
                conditionManager.fulfillCondition(condition);
                this.initApplication();
            }
        }
    };

    // TODO use domready instead of load because it triggers early, DOM ready but CSS and images
    // not necessarily loaded. Changing would have impact on CSS depending JavaScript like element
    // positioning code (like placeholder) or fake elements (surrogate in expanding textarea) 
    window.addEvent('load', function() {
        loadEventFired = true;
        communote.initializer.initApplication();
    });

})(this);