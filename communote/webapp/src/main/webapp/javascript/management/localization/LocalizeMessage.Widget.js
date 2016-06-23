var LocalizeMessageWidget = new Class({
    Extends: C_FormWidget,

    responseMetadata: undefined,

    widgetGroup: 'management/localization',
    init: function() {
        this.copyStaticParameter('messageKey');
        this.copyStaticParameter('showIsHtml');
        this.parent();
    },

    refreshComplete: function(responseMetadata) {
        var isHtml;
        this.parent(responseMetadata);
        init_tips(this.domNode);
        searchAndShowRoarNotification(this.domNode);
        isHtml = this.domNode.getElement('#isHtml');
        if (isHtml) {
            isHtml.addEvent('click', this.disableSelect.bind(this));
        }
        this.domNode.getElement('#message').addEvent('keyup', this.disableSelect.bind(this));
        this.domNode.getElement('#languageCodeSelector').addEvent('change',
                this.switchField.bind(this));
        this.responseMetadata = responseMetadata;
    },

    disableSelect: function() {
        this.domNode.getElement('#languageCodeSelector').setProperty('disabled', 'disabled');
    },

    switchField: function() {
        var languageCode = this.domNode.getElement('#languageCodeSelector');
        var value = languageCode.options[languageCode.selectedIndex].value;
        this.domNode.getElement('#languageCode').set('value', value);
        this.domNode.getElement('#message').set('value', this.responseMetadata[value + '_value']);
        var isHtml = this.domNode.getElement('#isHtml');
        if (isHtml) {
            if (this.responseMetadata[value + '_isHtml']) {
                isHtml.setProperty('checked', 'checked');
            } else {
                isHtml.removeProperty('checked');
            }
        }
    }
});
