var UserManagementSearchBoxWidget = new Class( {
    Extends: C_Widget,

	widgetGroup: "management/user",
	filterWidgetGroupId: null,
		
	init: function() {
        this.parent();
        var filterWidgetGroupId = this.getStaticParameter('filterWidgetGroupId');
        this.filterParamStore = window.filterWidgetGroupRepo[filterWidgetGroupId].getParameterStore();
        this.filterWidgetGroupId = filterWidgetGroupId;
    },
    
    refreshComplete: function(responseMetadata) {
    	var searchString = this.filterParamStore.getFilterParameter('searchString');
    	if (searchString) {
    		this.domNode.getElement('input.text').value = searchString;
    	}
    	communote.utils.attachPlaceholders('input', this.domNode);
    	init_tips(this.domNode);
    },
    
    resetSearchBox: function(defaultText) {
    	var searchBox = this.domNode.getElement('input.text');
    	if (searchBox.value != defaultText) {
    		searchBox.value = defaultText;
    		E2G('onUserSearch', this.filterWidgetGroupId, '');
    	}
    },
    
    submitSearchBox: function(defaultText) {
    	var searchBox = this.domNode.getElement('input.text');
    	if (searchBox.value != defaultText) {
    		E2G('onUserSearch', this.filterWidgetGroupId, searchBox.value);
    	}
    }
});
