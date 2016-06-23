(function(namespace) {
    var TagManagementWidget = new Class({
        Extends: C_FilterWidget,
        widgetGroup: 'tag',
        placeholders: null,
        tagTextboxList: null,
        tagAutocompleter: null,
        targetTagName: null,
        targetTagId: null,
        tagName: null,

        observedFilterParams: [ 'tagIds' ],

        /**
         * @override
         */
        beforeRemove: function() {
            this.cleanup();
        },

        changeTag: function(newTagId, newTagName) {
            var deleteTagCallback, renameTagCallback;
            if (newTagId == null) {
                renameTagCallback = this.changeTagCallback.bind(this, newTagName);
                noteTagUtils.renameTag(this.filterParamStore.getFilterParameter('tagIds'),
                        newTagName, renameTagCallback, renameTagCallback);
            } else {
                deleteTagCallback = this.deleteTagCallback.bind(this);
                noteTagUtils.deleteTag(this.filterParamStore.getFilterParameter('tagIds'),
                        newTagId, deleteTagCallback, deleteTagCallback);
            }
        },

        changeTagButtonClicked: function() {
            if (this.targetTagName != null) {
                this.changeTagWithConfirmation(null, this.targetTagId, this.targetTagName);
            } else if (this.domNode.getElementById(this.widgetId + '-rename-tag-input').value != '') {
                this.changeTag(null, this.domNode.getElementById(this.widgetId
                        + '-rename-tag-input').value);
            }
        },
        
        changeTagCallback: function(targetTagName, response) {
            var error, cause;
            if (response.status == 'OK') {
                location.href = location.href;
            } else {
                error = response.errors && response.errors[0];
                if (error) {
                    cause = error.cause;
                    if (cause.indexOf('tagExists:') == 0) {
                        this.changeTagWithConfirmation(error.message, cause.split(':')[1], targetTagName);
                        return;
                    }
                }
                showNotification(NOTIFICATION_BOX_TYPES.error, '', response.message);
            }
        },
        
        changeTagWithConfirmation: function(confirmMessagePrefix, targetTagId, targetTagName) {
            var contentElem;
            var confirmMessage = getJSMessage('widget.tagManagement.replace.confirmation.content', 
                    [this.tagName, targetTagName]);
            var confirmMessageHint = getJSMessage('widget.tagManagement.replace.confirmation.content.hint', 
                    [this.tagName, targetTagName]);
            if (confirmMessagePrefix) {
                confirmMessage = confirmMessagePrefix + " " + confirmMessage;
            }
            
            contentElem = new Element('div', {
                'html': "<p>"+confirmMessage + "</p><p><i>" + confirmMessageHint + "</i></p>",
                'class': 'dialog-paragraph'
            });
            
            showConfirmDialog(getJSMessage('widget.tagManagement.replace.confirmation.title'),
                    contentElem, this.changeTag.bind(this, targetTagId, targetTagName));
        },

        cleanup: function() {
            if (this.tagAutocompleter) {
                this.tagAutocompleter.destroy();
                this.tagAutocompleter = null;
            }
            if (this.placeholders) {
                this.placeholders.destroy();
                this.placeholders = null;
            }
            if (this.tagTextboxList) {
                this.tagTextboxList.destroy();
                this.tagTextboxList = null;
            }
        },

        deleteTag: function() {
            noteTagUtils.deleteTag(this.filterParamStore.getFilterParameter('tagIds'), null,
                    this.deleteTagCallback.bind(this), this.deleteTagCallback.bind(this));
        },

        deleteTagButtonClicked: function() {
            showConfirmDialog(getJSMessage('widget.tagManagement.delete.confirmation.title'),
                    getJSMessage('widget.tagManagement.delete.confirmation.content'), this.deleteTag
                            .bind(this));
        },

        deleteTagCallback: function(response) {
            if (response.status == 'OK') {
                location.href = namespace.server.applicationUrl;
            } else {
                showNotification(NOTIFICATION_BOX_TYPES.error, '', response.message);
            }
        },

        /**
         * @override
         */
        refreshComplete: function(responseMetadata) {
            var autocompleterOptions, inputElem, TextboxList, suggestionsOptions, postData;
            this.tagName = responseMetadata.tagName;
            inputElem = this.domNode.getElementById(this.widgetId + '-rename-tag-input');
            if (!inputElem) {
                return;
            }
            TextboxList = communote.getConstructor('TextboxList');
            this.tagTextboxList = new TextboxList(inputElem, {
                allowDuplicates: false,
                itemLimit: 1,
                listCssClass: 'cn-border',
                itemLimitReachedAction: 'hide',
                itemRemoveCssClass: 'textboxlist-item-remove cn-icon',
                itemRemoveTitle: getJSMessage('common.tagManagement.remove.tag.tooltip')
            });
            this.tagTextboxList.addEvent('itemRemoved', function() {
                delete this.targetTagName;
                delete this.targetTagId;
            }.bind(this));

            suggestionsOptions = {};
            suggestionsOptions.categories = [];
            suggestionsOptions.categories.push({
                id: 'DefaultNoteTagStore',
                provider: 'DefaultNoteTagStore',
                title: ''
            });
            autocompleterOptions = {
               inputFieldOptions: {
                   positionSource: this.domNode.getElement('.cn-border')
               },
                suggestionsOptions: suggestionsOptions                
            };

            postData = {
                tagIdsToExclude: this.filterParamStore.getFilterParameter('tagIds')
            };
            this.tagAutocompleter = autocompleterFactory.createTagAutocompleter(inputElem,
                    autocompleterOptions, postData, 'NOTE', true, true);

            this.tagAutocompleter.addEvent('onChoiceSelected', this.tagChoiceSelected.bind(this));
            this.placeholders = communote.utils.attachPlaceholders(inputElem);

            this.parent(responseMetadata);
        },

        /**
         * @override
         */
        refreshStart: function() {
            this.parent();
            this.cleanup();
        },

        tagChoiceSelected: function(inputElem, choiceElem, token, value) {
            this.tagTextboxList.addItem(token.name);
            this.tagAutocompleter.resetQuery(true);
            this.targetTagName = token.name;
            this.targetTagId = token.tagId;
        }

    });

    if (namespace && namespace.addConstructor) {
        namespace.addConstructor('TagManagementWidget', TagManagementWidget);
    } else {
        window.TagManagementWidget = TagManagementWidget;
    }
})(window.runtimeNamespace);