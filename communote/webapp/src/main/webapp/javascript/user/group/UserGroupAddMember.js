var UserGroupAddMemberWidget = new Class( {

    Extends: C_FormWidget,

    widgetGroup: 'management/user/group',

    queryMaxResultCount: 15,
    autocompleter: null,

    /**
     * @override
     */
    refreshStart: function() {
        this.parent();
        this.copyStaticParameter('groupId');
        // because the DOM changes on refresh remove the autocompleter to avoid memory leakage
        if (this.autocompleter) {
            this.autocompleter.destroy();
        }
    },

    /**
     * @override
     */
    refreshComplete: function(responseMetadata) {
        var postData;
        this.parent(responseMetadata);
        var input = this.domNode.getElement("#keyword");
        if (input) {
            postData = {
                'maxCount': this.userQueryMaxResultCount,
                'entityId': this.getStaticParameter('groupId')
            };
            this.autocompleter = autocompleterFactory.createUserAutocompleter(input, null, postData, 'ENTITY', true);
            this.autocompleter.addEvent('onChoiceSelected', function(inputElem, choiceElem, token, value) {
                var entityField = $('entityId');
                entityField.set('value', token.id);
                var isGroupField = $('isGroup');
                isGroupField.set('value', token.isGroup);
            });
        }
    },

    /**
     * @override
     */
    beforeRemove: function() {
        if (this.autocompleter) {
            this.autocompleter.destroy();
        }
    },
    
    /**
     * @override
     */
    onSubmitSuccess: function() {
        searchAndShowRoarNotification(this.domNode);
        E('groupMembersChanged');
    },

    /**
     * @override
     */
    onSubmitFailure: function() {
        searchAndShowRoarNotification(this.domNode);
    },
    
    /**
     * Submits the form and refresh the widget.
     */
    doSubmit: function() {
    	// call it separately because IE opens widget in new page when pressing enter in input,
    	// thus removed input field from form
    	var formElem = this.domNode.getElement('form');
    	this.onFormSubmit(formElem);
    }
});
