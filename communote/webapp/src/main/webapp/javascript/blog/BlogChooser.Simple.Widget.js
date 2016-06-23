var BlogChooserSimpleWidget = new Class( {
    Extends: BlogChooserWidget,


    refreshComplete: function(responseMetadata) {
        this.parent(responseMetadata);
        communote.utils.attachPlaceholders(null, this.domNode);
    },

    /**
     * @override 
     */
    suggestionSelected: function(inputElem, selectedElem, topicData, value) {
        document.location = buildRequestUrl('/portal/blogs/'
                + topicData.nameId);
    },

    attachAutocompleter: function() {
        this.parent();

        this.autocompleter.addEvent('show', function() {
            this.inputElement.getParent().addClass('cn-chooser-input-select');
        }.bind(this));
        this.autocompleter.addEvent('hide', function() {
            this.inputElement.getParent().removeClass('cn-chooser-input-select');
        }.bind(this));
    }
});
