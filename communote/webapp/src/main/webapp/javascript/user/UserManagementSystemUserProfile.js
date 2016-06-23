var UserManagementSystemUserProfileWidget = new Class({

    Extends: C_FormWidget,

    widgetGroup: "management/user",

    init: function() {
        this.copyStaticParameter('userId');
    },
    
    refreshComplete: function(responseMetadata) {
        var formElem;
        this.parent(responseMetadata);
        // add userId to form because it is not there and FormWidget ignores filter parameters on submit
        formElem = this.getWidgetForm();
        formElem.grab(new Element('input', {type: 'hidden', name: 'userId', value: this.getFilterParameter('userId')}));
    },

    onSubmitSuccess: function() {
        searchAndShowRoarNotification(this.domNode);
    },

    onSubmitFailure: function() {
        searchAndShowRoarNotification(this.domNode);
    }
});