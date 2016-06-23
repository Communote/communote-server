(function(namespace) {
    var EditTopicStructureWidget = new Class({
        Extends: C_FormWidget,
        widgetGroup: 'blog',
        currentChildTopics: null,
        placeholders: null,
        providedChildTopics: null,
        topicTextboxList: null,
        topicAutocompleter: null,
        wasSubmitFailure: false,
        
        init: function() {
            this.parent();
            this.copyStaticParameter('blogId');
        },
        
        /**
         * @override
         */
        beforeRemove: function() {
            this.cleanup();
        },
        
        cleanup: function() {
            if (this.topicAutocompleter) {
                this.topicAutocompleter.destroy();
                this.topicAutocompleter = null;
            }
            // cleanup placeholders before textbox list since textbox's cleanup code might
            // modify elements belonging to the placeholder of the textbox input
            if (this.placeholders) {
                this.placeholders.destroy();
                this.placeholders = null;
            }
            if (this.topicTextboxList) {
                this.topicTextboxList.destroy();
                this.topicTextboxList = null;
            }
        },
        compareTopics: function(topic1, topic2) {
            return topic1.id == topic2.id;
        },
        extractTopicName: function(topicData) {
            return topicData.title;
        },
        
        /**
         * @override
         */
        onSubmitFailure: function() {
            var i;
            // re-add the current child topics
            for (i = 0; i < this.currentChildTopics.length; i++) {
                this.topicTextboxList.addItem(this.currentChildTopics[i]);
            }
            this.wasSubmitFailue = true;
        },
        
        /**
         * Attach the autocompleter and the textbox list to the search input element.
         */
        prepareSearchField: function() {
            var searchElem, constructor, acOptions, postData;
            searchElem = this.domNode.getElementById(this.widgetId + '-subtopics-search');
            if (!searchElem) {
                return;
            }
            constructor = communote.getConstructor('TextboxList');
            this.topicTextboxList = new constructor(searchElem, {
                allowDuplicates: false,
                compareItemCallback: this.compareTopics,
                listCssClass: 'cn-border',
                itemRemoveCssClass: 'textboxlist-item-remove cn-icon',
                itemLimitReachedAction: 'disable',
                itemRemoveTitle: getJSMessage('widget.editTopicStructure.subtopics.remove.tooltip'),
                parseItemCallback: this.extractTopicName
            });
            acOptions = {};
            acOptions.autocompleterOptions = {};
            acOptions.autocompleterOptions.clearInputOnSelection = true;
            acOptions.autocompleterOptions.unfocusInputOnSelection = false;
            acOptions.inputFieldOptions = {
                    positionSource: searchElem.getParent('.cn-border')
            };
            postData = {};
            // exclude current topic
            postData.blogIdsToExclude = this.getFilterParameter('blogId');
            postData.showOnlyNonToplevelTopics = true;
            this.topicAutocompleter = autocompleterFactory.createTopicAutocompleter(searchElem,
                    acOptions, postData, false, 'read');
            this.topicAutocompleter.addEvent('onChoiceSelected', this.topicChoiceSelected.bind(this));
        },
        
        /**
         * @override
         */
        refreshComplete: function(responseMetadata) {
            var i, childTopic, searchElem;
            searchAndShowRoarNotification(this.domNode);
            this.prepareSearchField();
            this.parent(responseMetadata);
            this.providedChildTopics = [];
            this.currentChildTopics = null;
            
            if (responseMetadata.childTopics) {
                for (i = 0; i < responseMetadata.childTopics.length; i++) {
                    childTopic = responseMetadata.childTopics[i];
                    // save the currently assigned child topics
                    this.providedChildTopics.push(childTopic.id);
                    // add to textboxlist if the submit didn't fails
                    if (!this.wasSubmitFailue) {
                        this.topicTextboxList.addItem(childTopic);
                    }
                }
            }
            this.wasSubmitFailue = false;
            this.submitting = false;
            this.placeholders = namespace.utils.attachPlaceholders(null, this.domNode);
        },
        
        /**
         * @override
         */
        refreshStart: function() {
            this.parent();
            this.cleanup();
        },
        
        saveChanges: function() {
            var widgetForm, toplevelCheckbox, curItems, curTopicIds, i, diff;
            if (this.submitting) {
                return false;
            }
            this.submitting = true;
            widgetForm = this.getWidgetForm();
            toplevelCheckbox = widgetForm.getElementById(this.widgetId + '-toplevel-toggle');
            if (toplevelCheckbox) {
                widgetForm.getElement('input[name=\'toplevelTopic\']').set('value', toplevelCheckbox.get('checked'));
            }
            if (this.topicTextboxList) {
                curTopicIds = [];
                curItems = this.topicTextboxList.getItems();
                // save currently selected items in case of an error to be able to restore them
                this.currentChildTopics = curItems;
                for (i = 0; i < curItems.length; i++) {
                    curTopicIds.push(curItems[i].id);
                }
                diff = namespace.utils.createDiff(this.providedChildTopics, curTopicIds);
                widgetForm.getElement('input[name=\'childTopicIdsToRemove\']').set('value', diff.removed.join(','));
                widgetForm.getElement('input[name=\'childTopicIdsToAdd\']').set('value', diff.added.join(','));
            }
            this.onFormSubmit(widgetForm);
        },
        
        topicChoiceSelected: function(inputElem, choiceElem, token, value) {
            this.topicTextboxList.addItem(token);
        }
    });

    if (namespace && namespace.addConstructor) {
        namespace.addConstructor('EditTopicStructureWidget', EditTopicStructureWidget);
    } else {
        window.EditTopicStructureWidget = EditTopicStructureWidget;
    }
})(window.runtimeNamespace);