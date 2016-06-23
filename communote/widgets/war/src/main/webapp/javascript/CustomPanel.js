Element.Events.beforeOpen = {};
Element.Events.beforeClose = {};
Element.Events.afterOpen = {};
Element.Events.afterClose = {};

// TODO this class is buggy, do not use it!
var C_CustomPanel = new Class( /** @lends C_CustomPanel */{
    
    Extends : Fx.Slide,
    afterHandler : null,
    beforeHandler : null,
    
    /**
     * Create a new custom panel
     * @param {Object} options Options
     * @constructs
     * @class extends slider with events beforeOpen, afterOpen, beforeClose and afterClose to make layout changes possible.
     * @deprecated this class is buggy, do not use it
     */
    initialize : function(options) {
        this.parent(options);
        this.addEvent('chainComplete',function(){
            if(typeOf(this.afterHandler) == 'function') {
                this.afterHandler();
            }
        });
        this.addEvent('start',function(){
            if(typeOf(this.beforeHandler) == 'function') {
                this.beforeHandler();
            }
        });
    },
    
    isOpen : function () {
        return this.open;
    },
    beforeOpen : function() {
        this.fireEvent('beforeOpen');
    },
    resetHandler : function() {
        this.afterHandler = null;
        this.beforeHandler = null;
    },
    swapHandler : function() {
        if(this.isOpen()) {
            this.beforeHandler = this.beforeOpen();
            this.afterHandler = this.afterOpen();
        } else {
            this.beforeHandler = this.beforeClose();
            this.afterHandler = this.afterClose();
        }
    },
    afterOpen : function() {
        this.resetHandler();
        this.fireEvent('afterOpen');
    },
    afterClose : function() {
        this.resetHandler();
        this.fireEvent('afterClose');
    },
    beforeClose : function() {
        this.fireEvent('beforeClose');
    },
    show : function(mode) {
        var result = null; 
        if(!this.isOpen()) {
            this.beforeOpen();
            result = this.parent(mode);
            this.afterOpen();
        }
        return result;
    },
    hide : function(mode) {
        result = null;
        if(this.isOpen()) {
            this.beforeClose();
            result = this.parent(mode);
            this.afterClose();
        }
        return result;
    },
    slideIn : function(mode) {
        if(!this.isOpen()) {
            this.open = true;
            this.swapHandler();
        }
        return this.parent(mode);
    },
    slideOut : function(mode) {
        if(this.isOpen()) {
            this.open = false;
            this.swapHandler();
        }
        return this.parent(mode);
    },
    toggle : function(mode) {
        if(this.isOpen()) {
            this.open = false;
        } else {
            this.open = true;
        }
        this.swapHandler();
        return this.parent(mode);
    }
});

var CustomPanel = C_CustomPanel;