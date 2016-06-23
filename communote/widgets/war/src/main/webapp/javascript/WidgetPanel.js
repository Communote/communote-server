var C_WidgetPanel = new Class({
	Extends : Fx.Slide,
	panelIsOpen : true,

	initialize : function(elementID, options) {
		this.parent(elementID,options);
		// add CSS class to wrapper to fix IE6 bug
		this.wrapper.addClass('wrapperContainer');
		//this.element.store('wrapper', wrapper);
	},
	show : function(mode) {
		this.panelIsOpen = true;
		var result = this.parent(mode);
		this.panelStatusChanged();
		return result;
	},
	slideIn: function(mode){
		this.panelIsOpen = true;
		return this.parent(mode);
	},
	slideOut : function(mode) {
		this.panelIsOpen = false;
		return this.parent(mode);
	},
	hide : function(mode) {
		this.panelIsOpen = false;
		var result = this.parent(mode);
		this.panelStatusChanged();
		return result;
	},
	toggle : function(mode) {
		this.panelIsOpen = !this.panelIsOpen;
		return this.parent(mode);
	},
	onPanelOpened : function() {
	},
	onPanelClosed : function() {
	},
	panelStatusChanged : function() {
		if(this.panelIsOpen) {
			if(this.options.mode == "vertical") {
				this.wrapper.setStyle('height','auto');
			}
			this.onPanelOpened();
		} else {
			if(this.options.mode == "vertical") {
				this.wrapper.setStyle('height','0');
			}
			this.onPanelClosed();
		}
	},
	onComplete: function() {
		this.panelStatusChanged();
	}
});

var WidgetPanel = C_WidgetPanel;