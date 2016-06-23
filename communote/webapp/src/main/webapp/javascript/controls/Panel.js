var PanelWidget = new Class({

    Extends: C_Widget,

    widgetGroup: "controls",

    targetWidgetId: "",
    targetWidget: null,
    selectionBoxClassName: null,
    selectionBox: null,
    panelToggleCssSelector: ".panel-toggle",

    css: {
        panelClosed: "panel-icon-closed",
        panelOpen: "panel-icon-opened"
    },

    init: function() {
        this.parent();

        this.copyStaticParameter('title');
        this.targetWidgetId = this.getStaticParameter('targetWidgetId');
        this.selectionBoxClassName = this.getStaticParameter('selectionBoxClassName');
        if (this.selectionBoxClassName) {
            this.setFilterParameter('hasSelectionBox', true);
        }
    },

    setup: function() {
        this.parent();
    },

    onFirstDOMLoad: function() {
        if (this.selectionBoxClassName != null) {
            this.setupSelectionBox(this.selectionBoxClassName);
        }
    },
        
    setupSelectionBox : function(selectionBoxClassName) {
        this.selectionBox = new window[selectionBoxClassName](this.targetWidgetId, this.widgetId);
        this.selectionBox.render('panel_selection_box_' + this.widgetId);
    },
        
    getListeningEvents: function() {
        return this.parent().combine(['onWidgetRefreshComplete', 'onPanelClosed', 'onPanelOpen']);
    },
        
    // the panel was opened
    onPanelOpen: function(widget) {
        if(widget.widgetId == this.targetWidgetId) {
            var elem = this.domNode.getElement(this.panelToggleCssSelector);
            elem.addClass(this.css.panelOpen);
            elem.removeClass(this.css.panelClosed);
        }
    },
        
    // the panel was closed
    onPanelClosed: function(widget) {
        if(widget.widgetId == this.targetWidgetId) {
            var elem = this.domNode.getElement(this.panelToggleCssSelector);
            elem.removeClass(this.css.panelOpen);
            elem.addClass(this.css.panelClosed);
        }
    },
        
    // click event, toggle panel
    onPanelClick: function() {
        if(this.targetWidget != null) {
            this.targetWidget.togglePanel();
        }
        return false;
    },
        
    onWidgetRefreshComplete: function(widget) {
        if(widget != null && widget.widgetId == this.widgetId) {
            this.targetWidget = widgetController.getWidget(this.targetWidgetId);
            if(this.targetWidget.panelEnabled == true) {
                var widgetElement = $(this.widgetId);
                if(this.targetWidget.isPanelOpen()) {
                    this.onPanelOpen(this.targetWidget);
                } else {
                    this.onPanelClosed(this.targetWidget);
                }
            }
        }
    },
        
    //do nothing on refresh
    onRefresh : function() {
    }
});
