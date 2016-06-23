var BlogChooserWidget = new Class({
    Extends: C_Widget,
    widgetGroup: 'blog',

    autocompleter: null,
    choiceCssClass: 'cn-choice',
    choiceSummaryCssClass: 'cn-choice-summary',

    inputElement: null,

    beforeRemove: function() {
        this.autocompleter.destroy();
        this.parent();
    },

    refreshComplete: function(responseMetadata) {
        this.inputElement = this.domNode.getElement('input');
        this.attachAutocompleter();
    },

    attachAutocompleter: function() {
        // check if there is a positionSource element
        var positionSource = this.domNode.getElement('.control-autocompleter-position-source');
        this.autocompleter = autocompleterFactory.createTopicAutocompleter(this.inputElement, {
            'suggestionsOptions': {
                'selectFirst': true
            },
            'inputFieldOptions': {
                'positionSource': positionSource
            }
        }, null, false, 'read');
        this.autocompleter.addEvent('choiceSelected', this.suggestionSelected.bind(this));
    },

    /**
     * Called when one of the suggestions of the autocompleter is clicked.
     */
    suggestionSelected: function(inputElem, selectedElem, topicData, value) {
        E2G('onBlogClick', null, topicData.id, {
            type: 'blog',
            key: topicData.id,
            title: topicData.title
        });
    }
});
