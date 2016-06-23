/**
 * @class
 */
communote.widget.classes.Initializer = communote.Base.extend(
/** 
 * @lends communote.widget.classes.Initializer.prototype
 */	
{
    ready: false,

    constructor: function(configuration) {
        var controllers;
        var self = this;
        var apiAccessor = new communote.widget.classes.CommunoteApiAccessor(configuration);
        var url = apiAccessor.buildApiInformationUrl();
        communote.jQuery.ajax({
            async: false,
            type: "get",
            url: url,
            cache: true,
            data: {},
            dataType: "json",
            success: function(data, textStatus) {
                // can be undefined in anonymous user use-case
                if (data.userId) {
                    communote.currentUser = {
                        alias: data.userAlias,
                        firstName: data.userFirstName,
                        lastName: data.userLastName,
                        userId: data.userId
                    };
                }
                if (configuration.utcTimeZoneOffsetOfCommunote && data.utcTimeZoneOffset != undefined) {
                    configuration.utcTimeZoneOffset = data.utcTimeZoneOffset / 60000;
                }
                if (configuration.lang) {
                    configuration.lang = configuration.lang.replace("-","_");
                    // if user language is provided clear configured language if it is the same so that the lang parameter which overwrites the session locale is not set
                    if (data.languageLocale == configuration.lang) {
                        configuration.lang = undefined;
                    }
                }
            },
            error: function(data, textStatus, thrownError) {
                var message, errorDetails, error;
                // cannot render a error message here because the widget isn't ready yet. Let the
                // integrating code handle it.
                if (!thrownError) {
                    message = 'unknown';
                } else {
                    // can be error object or string
                    message = thrownError.message || thrownError;
                }
                error = new Error(message);
                // textStatus described what happened, will be error if request failed
                error.type = textStatus || 'unknown';
                if (textStatus == 'error') {
                    // add some details
                    error.statusCode = data.status;
                    try {
                        errorDetails = jQuery.parseJSON(data.responseText);
                    } catch(e) {
                        // ignore
                    }
                    error.detailMessage = (errorDetails && errorDetails.message) || 'Server error occured.';
                }
                throw error;
            }
        });
        controllers = [ {
            object: new communote.widget.classes.controllers.I18nController(configuration, apiAccessor)
        }, {
            object: new communote.widget.classes.controllers.EventController()
        }, {
            object: new communote.widget.classes.controllers.TemplateController()
        }, {
            object: new communote.widget.classes.controllers.WidgetController()
        }, {
            object: new communote.widget.classes.controllers.ApiController(configuration, apiAccessor)
        }, {
            object: new communote.widget.classes.controllers.ControlFactory()
        } ];
        this.controllerCheckList = controllers;
        this.controller = {};
        // TODO pretty ugly coding style
        communote.jQuery.each(controllers, function(index, controller) {
            var ctrl = controller.object;
            controller.ready = false;
            self.controller[ctrl.name] = ctrl;
            ctrl.controller = self.controller;
            ctrl.init(self, configuration);
        });
    },

    /**
     * Callback for a controller to signal that there initialization is ready. 
     */
    controllerReady: function(readyController) {
        var i, controllerDescr;
        var ready = true;
        var controllers = this.controllerCheckList;

        for (i = 0; i < controllers.length; i++) {
            controllerDescr = controllers[i];
            if (readyController === controllerDescr.object) {
                controllerDescr.ready = true;
                break;
            }
        }
        for (i = 0; i < controllers.length; i++) {
            if (!controllers[i].ready) {
                ready = false;
                break;
            }
        }
        if (ready) {
            this.ready = true;
            this.startUp();
        }
    },

    initializeWidget: function(rootSelector, configuration) {
        var widget = communote.widget.WidgetController.createWidget(communote.jQuery(rootSelector), configuration);
        if (this.ready) {
            communote.widget.WidgetController.startWidget(widget);
        } else {
            this.queueWidget(widget);
        }
    },
    
    /**
     * 
     */
    queueWidget: function(widget) {
        var self = communote.widget.MainController;
        if (self.widgetQueue === undefined) {
            self.widgetQueue = [];
        }
        self.widgetQueue.push(widget);
    },

    /**
     * 
     */
    startUp: function() {
        if(this.widgetQueue) {
            communote.widget.WidgetController.addWidgets(this.widgetQueue);
        }
        this.widgetQueue = [];
        communote.widget.WidgetController.startWidgets();
    }
});

(function() {
    // singleton initializer
    var initializer = null;

    // Create a configuration by merging the default configuration with the provided object
    var getConfiguration = function(extend) {
        var key, newConfig, valueExtend, minValue, maxValue;
        // TODO won't work with more widgets on a page (object members are shared). Configuration class isn't useful anyway.
        newConfig = new communote.widget.classes.Configuration();
        if (extend) {
            for (key in extend) {
                // only copy defined values
                valueExtend = extend[key];
                if (extend.hasOwnProperty(key) && (valueExtend != undefined)) {
                    if (typeof valueExtend === 'number') {
                        // check min max boundaries if defined
                        minValue = newConfig[key + '_min'];
                        maxValue = newConfig[key + '_max'];
                        if (((minValue == undefined) || (valueExtend >= minValue)) && 
                                ((maxValue == undefined) || (valueExtend <= maxValue))) {
                            newConfig[key] = valueExtend;
                        }
                    } else {
                        newConfig[key] = valueExtend;
                    }
                }
            }
        }
        return newConfig;
    };
    
    // expose start function
    communote.widget.start = function(rootSelector, configuration, errorCallback) {

        communote.jQuery(document).ready(function() {        
            configuration = getConfiguration(configuration);
            if (!initializer) {
                try {
                    initializer = new communote.widget.classes.Initializer(configuration);
                } catch (e) {
                    if (errorCallback) {
                        errorCallback.call(null, e.type, {
                            message: e.message, 
                            statusCode: e.statusCode, 
                            detailMessage: e.detailMessage
                        });
                    }
                    return;
                }
            }
            initializer.initializeWidget(rootSelector, configuration);
        });
    };
})();