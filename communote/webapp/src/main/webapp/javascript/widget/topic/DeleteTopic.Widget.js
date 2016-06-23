(function(namespace) {
    var DeleteTopicWidget = new Class({
        Extends: C_Widget,
        widgetGroup: 'blog',

        deleteSuccessUrl: null,
        placeholders: null,
        targetTopicAlias: null,
        topicAutocompleter: null,
        topicId: null,
        topicTextboxList: null,

        init: function() {
            this.parent();
            this.copyStaticParameter('blogId');
            this.topicId = this.getFilterParameter('blogId');
            this.deleteSuccessUrl = this.getStaticParameter('deleteSuccessUrl') || '/portal/topics';
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
            if (this.placeholders) {
                this.placeholders.destroy();
                this.placeholders = null;
            }
            if (this.topicTextboxList) {
                this.topicTextboxList.destroy();
                this.topicTextboxList = null;
            }
        },

        /**
         * @override
         */
        refreshComplete: function(responseMetadata) {
            var constructor, textboxListInput;
            var postData = {};

            postData['blogIdsToExclude'] = this.getFilterParameter('blogId');

            textboxListInput = this.domNode.getElementById(this.widgetId + '_topic-textbox-list');

            constructor = communote.getConstructor('TextboxList');
            this.topicTextboxList = new constructor(textboxListInput, {
                allowDuplicates: false,
                itemLimit: 1,
                listCssClass: 'cn-border',
                itemLimitReachedAction: 'hide',
                itemRemoveCssClass: 'textboxlist-item-remove cn-icon',
                itemRemoveTitle: getJSMessage('common.tagManagement.remove.tag.tooltip')
            });

            this.topicTextboxList.addEvent('itemRemoved', function() {
                delete this.targetTopicAlias;
            }.bind(this));

            this.topicAutocompleter = autocompleterFactory.createTopicAutocompleter(
                    textboxListInput, {
                        inputFieldOptions: {
                            positionSource: textboxListInput.getParent('.cn-border')
                        }
                    }, postData, false, 'write');

            this.topicAutocompleter.addEvent('onChoiceSelected', this.topicChoiceSelected
                    .bind(this));
            this.placeholders = communote.utils.attachPlaceholders(textboxListInput);

            this.parent(responseMetadata);
        },

        /**
         * @override
         */
        refreshStart: function() {
            this.parent();
            this.cleanup();
        },

        topicChoiceSelected: function(inputElem, choiceElem, token, value) {
            this.topicTextboxList.addItem(token.title);
            this.topicAutocompleter.resetQuery(true);
            this.targetTopicAlias = token.alias;
        },

        deleteTopicButtonClicked: function() {
            if (this.targetTopicAlias == null) {
                showConfirmDialog(getJSMessage('blog.delete.confirmation.info'),
                        getJSMessage('blog.delete.selected.none'), this.deleteTopic.bind(this));
            } else {
                this.deleteTopic();
            }
        },

        deleteTopic: function() {
            var url = buildRequestUrl("/blog/default/deleteBlog.do");
            var request = new Request.JSON({
                url: url,
                method: 'post',
                data: 'blogId=' + this.topicId + '&newBlogAlias=' + this.targetTopicAlias
            });
            request.addEvent('complete', function(response, xml) {
                var failed = request.getHeader('X-APPLICATION-RESULT') == 'ERROR';
                this.stopLoadingFeedback();
                if (failed) {
                    showNotification(NOTIFICATION_BOX_TYPES.failure, '', response.errorMessage,
                            null);
                } else {
                    showNotification(NOTIFICATION_BOX_TYPES.success, '', getJSMessage(
                            'blog.management.delete.success', []), null);

                    // reload current page and re-initialize all widgets (controller should redirect if
                    // location is blog permalink and blog no longer exists)
                    location.href = buildRequestUrl(this.deleteSuccessUrl);
                }
            }.bind(this));
            this.startLoadingFeedback();
            request.send();
        }
    });

    if (namespace && namespace.addConstructor) {
        namespace.addConstructor('DeleteTopicWidget', DeleteTopicWidget);
    } else {
        window.DeleteTopicWidget = DeleteTopicWidget;
    }
})(window.runtimeNamespace);