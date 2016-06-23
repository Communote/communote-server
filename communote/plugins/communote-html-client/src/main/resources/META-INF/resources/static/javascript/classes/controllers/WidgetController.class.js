/**
 * @class
 * @augments communote.widget.classes.controllers.Controller
 */
communote.widget.classes.controllers.WidgetController = communote.widget.classes.controllers.Controller.extend(
/** 
 * @lends communote.widget.classes.controllers.WidgetController.prototype
 */	
{
            name: "WidgetController",
            idCounter: 0,

            /**
             * initializing
             */
            constructor: function() {
                this.widgets = [];
            },

            /**
             * 
             * @param widget
             */
            addWidget: function(widget) {
                // TODO fgr: although the id is an int you are using it like an object. 
                // Use push or the turn this.widgets into {}
                this.widgets[widget.id] = widget;
            },

            /**
             * 
             * @param widgetList
             */
            addWidgets: function(widgetList) {
                var $ = communote.jQuery;
                var self = this;
                if ($.isArray(widgetList)) {
                    $.each(widgetList, function(index, widget) {
                        self.addWidget(widget);
                    });
                }
            },

            /**
             * 
             * @param domNode
             * @param configuration
             */
            createWidget: function(domNode, configuration) {
                var widget = new communote.widget.classes.Widget(this.idCounter++, domNode, 
                        configuration, this.controller);
                return widget;
            },

            /**
             * 
             */
            startWidgets: function() {
                var $ = communote.jQuery;
                $.each(this.widgets, function(index, widget) {
                    widget.start();
                });
            },

            /**
             * 
             * @param widget
             */
            startWidget: function(widget) {
                this.addWidget(widget);
                widget.start();
            }
        });
