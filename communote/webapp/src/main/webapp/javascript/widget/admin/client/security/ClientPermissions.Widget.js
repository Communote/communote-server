var ClientPermissionsWidget = new Class({
    Extends: C_FormWidget,

    widgetGroup: 'admin/client/security',

    autocompleter: null,

    choiceCssClass: 'choice',

    defaultBlog: null,
    inputField: null,

    refreshComplete: function(responseMetadata) {
        this.parent(responseMetadata);
        var field = this.domNode.getElementById('defaultBlog');
        this.attachBlogAutocompleter(field);
        searchAndShowRoarNotification(this.domNode);
        // save current default blog if set
        if (field.value != '') {
            this.defaultBlog = {};
            this.defaultBlog.title = field.value;
            this.defaultBlog.id = this.domNode.getElementById('defaultBlogId').value;
        } else {
            this.defaultBlog = null;
        }
        this.inputField = field;
    },

    attachBlogAutocompleter: function(field) {
        var acOptions = {
            inputFieldOptions: {
                positionSource: this.domNode.getElementById('defaultBlogChooser')
            }
        };
        var ac = autocompleterFactory.createTopicAutocompleter(field, acOptions, null, false, 'manager');
        ac.addEvent('onChoiceSelected', this.choiceSelected.bind(this));
        // add blur listener to reset title when user didn't selected a new blog from ac
        ac.addEvent('blur', this.updateInputField.bind(this));
        this.autocompleter = ac;
    },

    setNewDefaultBlog: function(newBlog) {
        var field;
        if (newBlog) {
            if (!this.defaultBlog) {
                this.defaultBlog = {};
            } else if (newBlog.id == this.defaultBlog.id) {
                // nothing changed
                return;
            }
            this.defaultBlog.id = newBlog.id;
            this.defaultBlog.title = newBlog.title;
            field = this.domNode.getElementById('defaultBlogId');
            field.set('value', newBlog.id);
        }
    },

    choiceSelected: function(inputElem, choiceElem, token, value) {
        this.setNewDefaultBlog(token);
    },

    updateInputField: function() {
        var title = '';
        // set title of selected blog to give correct feedback
        if (this.defaultBlog) {
            title = this.defaultBlog.title;
        }
        this.inputField.set('value', title);
    }

});