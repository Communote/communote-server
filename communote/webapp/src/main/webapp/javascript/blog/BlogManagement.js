var BlogManagementWidget = new Class({

    Extends: C_FormWidget,

    widgetGroup: "blog",

    /**
     * whether used in manage or create mode
     */
    createMode: false,

    // URL to open when a topic was successfully created. The URL can contain the strings
    // ALIAS_PLACEHOLDER and ID_PLACEHOLDER which will be replaced with the alias and ID of the
    // current topic respectively
    createSuccessUrl: null,
    // URL to open when a topic was successfully updated. The URL can contain the strings
    // ALIAS_PLACEHOLDER and ID_PLACEHOLDER which will be replaced with the alias and ID of the
    // current topic respectively
    updateSuccessUrl: null,
    cancelUrl: null,
    // remove widget on cancel or only send onBlogManagementCanceled event. Will be ignored if
    // cancelUrl is defined
    removeOnCancel: false,
    // remove widget on successful update or only send onBlogManagementUpdateDone event. Will be ignored if 
    // updateSuccessUrl is set
    removeOnUpdateSuccess: false,

    oldTitle: null,
    oldDescr: null,
    oldAllCanRead: null,
    oldAllCanWrite: null,
    blogAutocompleter: null,
    tagTextboxList: null,

    init: function() {
        var createMode, param;
        this.parent();
        createMode = this.getStaticParameter('createMode');
        if (createMode != null && (createMode === true || createMode == 'true')) {
            this.createMode = true;
            this.copyStaticParameter('parentTopicId');
            this.copyStaticParameter('toplevelTopic');
            this.createSuccessUrl = this.getStaticParameter('createSuccessUrl')
                    || '/portal/topic-edit?blogId=ID_PLACEHOLDER';
        } else {
            this.createMode = false;
            this.copyStaticParameter('blogId');
            this.updateSuccessUrl = this.getStaticParameter('updateSuccessUrl');
            if (!this.updateSuccessUrl) {
                this.removeOnUpdateSuccess = this.getStaticParameter('removeOnUpdateSuccess') === true;
            }
        }
        this.copyStaticParameter('showCancelButton', true);
        this.cancelUrl = this.getStaticParameter('cancelUrl');
        if (!this.cancelUrl) {
            this.removeOnCancel = this.getStaticParameter('removeOnCancel') === true;
        }
    },

    getWidgetListenerGroupId: function() {
        return "blog_moving_posts";
    },

    onBlogAliasKeyUp: function(newAlias) {
        var emailTxtField = this.domNode.getElement('div[name=emailText]');
        if (emailTxtField) {
            var mailToLink = emailTxtField.getElement("a");
            var emailSuffix = this.domNode.getElement('input[name=emailSuffix]').value;
            var newEmailAddress = '?';
            if (newAlias.length > 0) {
                newEmailAddress = newAlias + emailSuffix;
                var newEmailAddressDisplay = newEmailAddress;
                // shorten to 30 chars
                if (newEmailAddress.length > 75) {
                    newEmailAddressDisplay = newEmailAddress.slice(0, 72) + '...';
                }
                if (!mailToLink) {
                    emailTxtField.set('html', '');
                    mailToLink = new Element('a');
                    mailToLink.inject(emailTxtField);
                }
                mailToLink.setProperty('href', 'mailto:' + newEmailAddress);
                mailToLink.setProperty('title', newEmailAddress);
                mailToLink.set('html', newEmailAddressDisplay);
                emailTxtField.removeProperty('title');

            } else {
                if (mailToLink) {
                    mailToLink.dispose();
                }
                emailTxtField.title = getJSMessage('blog.create.email.undefined.tooltip');
                emailTxtField.innerHTML = newEmailAddress;
            }
        }
    },

    /**
     * @override
     */
    beforeRemove: function() {
        if (this.blogAutocompleter) {
            this.blogAutocompleter.destroy();
            this.blogAutocompleter = null;
        }
        if (this.tagAutocompleter) {
            this.tagAutocompleter.destroy();
            this.tagAutocompleter = null;
        }
        if (this.tagTextboxList) {
            this.tagTextboxList.destroy();
        }
    },

    refreshComplete: function(responseMetadata) {
        var tagInput, autocompleterOptions, suggestionsOptions, textboxListInput, tagListHiddenInput, constructor, tagList, i;
        this.parent(responseMetadata);
        if (this.oldTitle == null) {
            this.oldTitle = this.domNode.getElement('input[name=title]').value;
            this.oldDescr = this.domNode.getElement('textarea').innerHTML;
            this.oldAlias = this.getCurrentAlias();
            this.oldTags = this.domNode.getElement('input[name=tags]').value;
        }
        init_tips(this.widgetId);
        // free resources before re-attaching
        if (this.tagAutocompleter) {
            this.tagAutocompleter.destroy();
        }
        
        textboxListInput = this.domNode.getElementById(this.widgetId + '_tag-textbox-list');
        tagListHiddenInput = this.domNode.getElementById('tags');

        constructor = communote.getConstructor('TextboxList');
        this.tagTextboxList = new constructor(textboxListInput, {
            addItemOnChar: ',',
            allowDuplicates: false,
            // kind of hacky but is easiest way to avoid duplicates and showing the tag twice in the list
            listCssClass: 'cn-border',
            itemRemoveCssClass: 'textboxlist-item-remove cn-icon',
            itemRemoveTitle: getJSMessage('common.tagManagement.remove.tag.tooltip')
        });
        
        if (tagListHiddenInput.value != "") {
            tagList = tagListHiddenInput.value.split(',');
            if (tagList.length > 0) {
                for (i = 0; i < tagList.length; i++) {
                    this.tagTextboxList.addItem(tagList[i].trim());
                }
            }
        }
        
        tagInput = this.domNode.getElement('input[name='+this.widgetId + '_tag-textbox-list]');
        suggestionsOptions = {};
        suggestionsOptions.categories = [];
        suggestionsOptions.categories.push({
            id: 'DefaultBlogTagStore',
            title: '',
            provider: 'DefaultBlogTagStore'
        });
        autocompleterOptions = {
            suggestionsOptions: suggestionsOptions
        };

        this.tagAutocompleter = autocompleterFactory.createTagAutocompleter(tagInput,
                autocompleterOptions, null, 'BLOG', true, true);
        tagInput.addEvent('keypress', this.suppressFormSubmit);
        
        if (this.tagAutocompleter) {
            this.tagAutocompleter.addEvent('enterPressed', this.submitTags.bind(this, tagInput));
            this.tagAutocompleter.addEvent('onChoiceSelected', this.tagChoiceSelected.bind(this));
        }
    },
    
    submitTags: function(tagInput) {
        var i, itemsCount, tagList;
        if(tagInput.value != "") {
            itemsCount = tagInput.value.indexOf(',');
            if(itemsCount === -1) {
                this.tagTextboxList.addItem(tagInput.value);
            } else {
                tagList = tagInput.value.trim().split(',');
                for (i = 0; i < tagList.length; i++) {
                    if(tagList[i] != "") {
                        this.tagTextboxList.addItem(tagList[i]);
                    }
                }
            }
            this.tagAutocompleter.resetQuery(true);
        }
    },
    
    tagChoiceSelected: function(inputElem, choiceElem, token, value) {
        this.tagTextboxList.addItem(token.name);
        this.tagAutocompleter.resetQuery(true);
    },
    
    /**
     * overwrite
     * 
     * @method refresh taglist before submit
     */
    onFormSubmit: function(params) {
        this.domNode.getElementById('tags').value = this.tagTextboxList.getItems().toString();
        this.parent(params);
    },
    
    /**
     * overwrite
     * 
     * @method refresh taglist before submit
     */
    onFormSubmitButtonClick: function(objSubmit) {
        this.domNode.getElementById('tags').value = this.tagTextboxList.getItems().toString();
        this.parent(objSubmit);
    },

    attachBlogAutocompleter: function(domNode) {
        var inputField = domNode.getElementById('moveTargetBlogSearchInput');
        var postData = {};
        // reset the autocompleter if there is already one
        if (this.blogAutocompleter) {
            this.blogAutocompleter.destroy();
            this.blogAutocompleter = null;
        }
        postData['blogIdsToExclude'] = this.getFilterParameter('blogId');
        this.blogAutocompleter = autocompleterFactory.createTopicAutocompleter(inputField, {
            suggestionsOptions: {
                zIndex: 1000
            }
        }, postData, false, 'write');
        this.blogAutocompleter.addEvent('onChoiceSelected', function(inputElem, choiceElem, token,
                value) {
            this.doAddBlog(domNode, token);
        }.bind(this));
        communote.utils.attachPlaceholders(inputField);
    },

    /**
     * Overridden to inform other widgets. Sends the onBlogUpdate event when the blog has changed in
     * title and/or description. The event parameters are the blog id, the newTitle (only if
     * changed) and descriptionChanged (boolean).
     */
    onSubmitSuccess: function() {
        var url;
        var newTitle = this.domNode.getElement('input[name=title]').value;
        var newAlias = this.getCurrentAlias();
        var newDescr = this.domNode.getElement('textarea').innerHTML;
        var newTags = this.domNode.getElement('input[name=tags]').value;
        var blogId = this.getFilterParameter('blogId');
        
        if (this.createMode) {
            url = this.replacePlaceholders(this.createSuccessUrl, blogId, newAlias);
            location.href = buildRequestUrl(url);
        } else {
            searchAndShowRoarNotification(this.domNode);
            if (newAlias != this.oldAlias) {
                blogDataChanged = true;
            } else {
                newAlias = null;
            }
            if (newTitle != this.oldTitle) {
                this.widgetController.getDataStore().put({
                    type: 'blog',
                    key: blogId,
                    title: newTitle
                });
                blogDataChanged = true;
            } else {
                newTitle = null;
            }
            if (newAlias || newTitle || this.oldDescr != newDescr || this.oldTags != newTags) {
                E2('onBlogUpdate', null, {
                    id: blogId,
                    newTitle: newTitle,
                    newAlias: newAlias,
                    descriptionChanged: this.oldDescr != newDescr,
                    tagsChanged: this.oldTags != newTags
                });
                // TODO cache invalidation should happen automatically because of the event, but
                // currently not possible
                if (window.blogUtils) {
                    blogUtils.entityChanged(blogId);
                }
            }
            if (this.updateSuccessUrl) {
                url = this.replacePlaceholders(this.updateSuccessUrl, blogId, alias);
                location.href = buildRequestUrl(url);
            } else {
                E('onBlogManagementUpdateDone', {
                    widgetId: this.widgetId,
                    blogId: blogId
                });
                if (this.removeOnUpdateSuccess) {
                    this.widgetController.removeWidgetById(this.widgetId);
                }
            }
        }
    },

    onSubmitFailure: function() {
        // look for the error container and show a message notification if there is an error
        searchAndShowRoarNotification(this.domNode);
    },

    getCurrentAlias: function() {
        return this.domNode.getElement('input[name=nameIdentifier]').value;
    },

    getCurrentBlogId: function() {
        return this.domNode.getElement('input[name=blogId]').value;
    },

    /**
     * Replace placeholder for alias and ID with current values in the provided URL.
     */
    replacePlaceholders: function(url, blogId, alias) {
        if (!blogId) {
            blogId = this.getCurrentBlogId();
        }
        if (!alias) {
            alias = this.getCurrentAlias();
        }
        url = url.replace('ID_PLACEHOLDER', blogId);
        return url.replace('ALIAS_PLACEHOLDER', alias);
    },

    cancelEdit: function() {
        var url;
        if (this.cancelUrl) {
            url = this.replacePlaceholders(this.cancelUrl, null, null);
            location.href = buildRequestUrl(url);
        } else {
            this.widgetController.sendEvent('onBlogManagementCanceled', null, {
                widgetId: this.widgetId,
                blogId: this.getCurrentBlogId()
            });
            if (this.removeOnCancel) {
                this.widgetController.removeWidgetById(this.widgetId);
            }
        }
    },

    suppressFormSubmit: function(event) {
        if (event.key == 'enter') {
            value = this.value.trim();
            if (value.length > 0 && !value.match(/,$/)) {
                this.value = this.value + ', ';
            }
            event.preventDefault();
        }
    }
});
