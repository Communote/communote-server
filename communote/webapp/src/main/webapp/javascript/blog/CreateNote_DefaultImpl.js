CreateNoteDefaultWidget = new Class({
    Extends: CreateNoteWidget,
    Implements: C_FilterParameterListener,

    observedParams: [ 'targetBlogId' ],

    filterGroup: null,
    filterParameterStore: null,

    plainTextEditorCssClass: 'cn-write-note-plaintext',
    contentUsersContainer: null,
    contentAttachmentsContainer: null,
    // no multiple tag autocompletion since the TextboxList is taking care of this
    tagAutocompleterMultipleMode: false,
    userInterfaceHidden: false,

    init: function() {
        var renderStyle, filterGroupId, filterGroup, targetBlogId;
        this.parent();
        if (this.renderStyle !== 'full' && !this.editor.supportsHtml()) {
            // check whether the editor in full render style would support HTML because
            // the editor will switch to full if there is an autosave
            renderStyle = this.renderStyle;
            this.renderStyle = 'full';
            this.setFilterParameter('plaintextOnly', !this.createEditor().supportsHtml());
            this.renderStyle = renderStyle;
        }
        if (this.action == 'create') {
            // only observe targetBlogId changes when in create mode
            filterGroupId = this.staticParams.filterWidgetGroupId;
            filterGroup = filterGroupId && communote.filterGroupRepo[filterGroupId];
            if (filterGroup) {
                filterGroup.addMember(this);
                this.filterParameterStore = filterGroup.getParameterStore();
                this.filterGroup = filterGroup;
                // check for targetBlogId can be part of the widget settings or
                // set in the filter store as initial parameter, but give setting preference
                if (!this.initialTargetTopic) {
                    this.setInitialTargetTopic(this.filterParameterStore
                            .getFilterParameter('targetBlogId'), null);
                }
            }
        }
        if (Browser.name === 'ie') {
            this.setFilterParameter('customUpload', true);
        }
        this.topicWriteAccessEvaluator = new communote.classes.TopicWriteAccessEvaluator(this.blogUtils);
    },
    
    activateFullEditor: function() {
        this.setRenderStyle('full');
        if (this.action == 'create') {
            this.editor.focus();
        }
    },

    getListeningEvents: function() {
        return this.parent().combine([ 'onUserLogoChanged' ]);
    },

    onUserLogoChanged: function(imagePath) {
        // clear autocompleter cache
        if (this.userAutocompleter) {
            this.userAutocompleter.resetQuery(false);
        }
    },

    getObservedFilterParameters: function() {
        return this.observedParams;
    },

    filterParametersChanged: function(changedParams) {
        // we are only interested in changes of the 'targetBlogId'
        this.targetBlogIdChanged();
    },

    targetBlogIdChanged: function() {
        // update the initialTargetTopic so it will be set the next time an autosave is discarded or note was sent
        this.setInitialTargetTopic(this.filterParameterStore.getFilterParameter('targetBlogId'),
                null);
        // update the target topic, but not if modified or there is an autosave, also do nothing
        // if not yet refreshed since the responseMetadata might contain autosave data
        // TODO also check whether content is empty?
        if (this.firstDOMLoadDone) {
            if (!this.noTargetTopicChangeIfModifiedOrAutosaved) {
                // CNT pre 3 behavior: change topic even if modified but not if the default topic
                // fallback would be set
                if (this.initialTargetTopic || !this.hasAutosave()) {
                    this.resetTargetTopic();
                } else {
                    // if there is an autosave and user switched to a context where there is no
                    // topic force an access check as if there would be no autosave by passing null
                    // and not the topic of the autosave. This will ensure the editor is shown
                    // and we have same behavior as when the widget is refreshed under same conditions.
                    this.showOrHideUserInterfaceForTopic(null);
                }
            } else if (!this.isModified() && !this.hasAutosave()) {
                this.resetTargetTopic();
            }
        }
    },

    /**
     * @override
     */
    targetTopicChanged: function() {
        var newId = this.getTargetTopicId();
        // don't check for write access here while still initializing because not all information
        // might be available yet and it is done after init anyway
        if (!this.initializingAfterRefresh) {
            this.showOrHideUserInterfaceForTopic(newId);
        }
        // invalidate caches of autocompeters to let them restart a query for the same term
        // after topic (request parameter) changed
        this.resetAutocompleterCache(this.tagAutocompleter);
        this.resetAutocompleterCache(this.userAutocompleter);
        this.resetAutocompleterCache(this.editor.getUserAutocompleter());
        this.resetAutocompleterCache(this.editor.getTagAutocompleter());
    },

    /**
     * Reset the cache of an autocompleter.
     * 
     * @param {Autocompleter} autocompleter The autocompleter whose cache should be reset. If null
     *            nothing will happen.
     */
    resetAutocompleterCache: function(autocompleter) {
        if (autocompleter) {
            autocompleter.resetQuery(false);
        }
    },

    /**
     * @override
     */
    createEditor: function() {
        var minimal, options;
        minimal = this.renderStyle === 'minimal';
        options = {
            autoresizeMinHeight: Math.floor(46200 / this.domNode.clientWidth),
            useExpander: true,
            focusOnRefresh: this.action === 'comment',
            expanderOptions: {
                additionalLines: 0,
                minLines: minimal ? 1 : 0
            },
            keyboardShortcuts: {
                ctrlKey: true,
                keyCode: 13,
                callback: this.publishNote.bind(this),
                cancelEvent: true
            },
            storeEditorSize: this.action === 'create'
        };
        if (minimal) {
            return NoteTextEditorFactory.createPlainTextEditor(null, options);
        } else {
            options.tagAutocompleterOptions = this.prepareAutocompleterOptions(null, false, false,
                    this.setCurrentTopicBeforeRequestCallback.bind(this));
            options.userAutocompleterOptions = this.prepareAutocompleterOptions(null, false, false,
                    this.setCurrentTopicBeforeRequestCallback.bind(this));
            options.userAutocompleterOptions.staticDataSourceOptions = {};
            options.userAutocompleterOptions.staticDataSourceOptions.approveMatchCallback = this.checkDiscussionContextApproveMatchCallback
                    .bind(this);
            return NoteTextEditorFactory.createSupportedEditor(null, options);
        }
    },
    
    /**
     * @override
     */
    loadTargetTopicInfoErrorCallback: function(topicId, response) {
        this.parent(topicId, response);
        if (this.initialTargetTopic && this.initialTargetTopic.id == topicId && this.initialTargetTopic.notFound) {
            // do not know which request (refresh, topic-info GET or topic-rights) is faster -> 
            // don't not hide the interface while still loading, the topic-rights check callback will do it
            if (this.firstDOMLoadDone) {
                this.hideUserInterface(response.message);
            } else {
                this.initialTargetTopic.notFoundErrorMessage = response.message;
            }
        }
    },

    /**
     * @override
     */
    beforeRemove: function() {
        this.parent();
        if (this.filterGroup) {
            this.filterGroup.removeMember(this);
        }
    },

    /**
     * @override
     */
    cleanup: function() {
        this.parent();
        if (this.tagTextboxList) {
            this.tagTextboxList.destroy();
        }
        if (this.topicTextboxList) {
            this.topicTextboxList.destroy();
        }
        if (this.userTextboxList) {
            this.userTextboxList.destroy();
        }
    },

    /**
     * @override
     */
    refreshComplete: function(responseMetadata) {
        // set a marker flag to be able to react depending on the current working context (pre & post init)  
        this.initializingAfterRefresh = true;
        this.parent(responseMetadata);
        // check access rights but not if an autosave is loaded as we might get stuck. The latter can
        // only happen if the topic isn't overridden by the current initial topic (override option
        // false or no topic set and default topic fallback would take effect)
        if (this.action == 'create'
                && (!this.hasAutosave() || this.initialTargetTopic
                        && !this.noTargetTopicChangeIfModifiedOrAutosaved)) {
            this.showOrHideUserInterfaceForTopic(this.getTargetTopicId());
        }
        this.showHideAccessories();
        this.initializingAfterRefresh = false;
    },

    /**
     * @override
     */
    refreshEditor: function() {
        // if using plain text editor add 'cn-border' class to textarea wrapper to
        // get correct look
        if (this.editor.supportsHtml()) {
            this.domNode.getElement('.cn-write-note-editor-fields').removeClass('cn-border');
        } else {
            this.domNode.getElement('.cn-write-note-editor-fields').addClass('cn-border');
        }
        this.parent();
    },

    clearAll: function() {
        this.parent();
        this.showHideAccessories();
    },

    /**
     * @override
     */
    refreshView: function(autosaveLoaded) {
        this.parent(autosaveLoaded);
        if (autosaveLoaded && this.renderStyle !== 'full') {
            this.setRenderStyle('full');
        }
        this.contentUsersContainer = this.domNode.getElement('.cn-write-note-accessory-user');
        this.contentAttachmentsContainer = this.domNode
                .getElement('.cn-write-note-accessory-attachment');
        if (this.useTopicSelection()) {
            this.contentTopicsContainer = this.domNode.getElement('.cn-write-note-accessory-topic');
        }
        this.contentInfoContainer = this.domNode.getElement('.cn-container-note');
    },

    /**
     * @override
     */
    getWriteContainerElement: function() {
        return this.domNode.getElement('.cn-write-note-editor');
    },

    contentInitialized: function(fromAutosave) {
        var dmWrapperElem;
        this.parent(fromAutosave);
        if (this.action == 'edit' && !this.isDirectMessage) {
            dmWrapperElem = this.domNode.getElementById(this.widgetId + '-direct-message-wrapper');
            if (dmWrapperElem) {
                dmWrapperElem.setStyle('display', 'none');
            }
        } else {
            this.enableDisableDmCheckbox();
        }
    },

    /**
     * @override
     */
    refreshTopicSelection: function(searchElement) {
        var constructor = communote.getConstructor('TextboxList');
        this.topicTextboxList = new constructor(searchElement, {
            autoRemoveItemCallback: this.removeTopic.bind(this),
            allowDuplicates: false,
            listCssClass: 'cn-border',
            itemRemoveCssClass: 'textboxlist-item-remove cn-icon',
            itemLimitReachedAction: 'disable',
            itemRemoveTitle: getJSMessage('blogpost.create.blogs.remove.tooltip'),
            parseItemCallback: this.extractTopicName
        });
        this.parent(searchElement);
        // refresh the placeholder since the textboxlist modifies the style of the input
        this.refreshPlaceholder(searchElement, false);
    },

    /**
     * @override
     */
    getTopicAutocompleterPositionSource: function() {
        return this.contentTopicsContainer.getElement('.cn-border');
    },

    /**
     * @override
     */
    refreshUserSelection: function(searchElement) {
        var constructor = communote.getConstructor('TextboxList');
        this.userTextboxList = new constructor(searchElement, {
            autoRemoveItemCallback: this.removeUser.bind(this),
            listCssClass: 'cn-border',
            itemRemoveCssClass: 'textboxlist-item-remove cn-icon',
            itemRemoveTitle: getJSMessage('blogpost.create.users.remove.tooltip'),
            parseItemCallback: this.extractUserName
        });
        this.parent(searchElement);
    },

    /**
     * @override
     */
    getUserAutocompleterPositionSource: function() {
        return this.contentUsersContainer.getElement('.cn-border');
    },

    /**
     * @override
     */
    getUserSearchElement: function() {
        return this.contentUsersContainer.getElementById(this.widgetId + '-user-search');
    },

    /**
     * @override
     */
    refreshTagSelection: function(searchElement) {
        var constructor = communote.getConstructor('TextboxList');
        this.tagTextboxList = new constructor(searchElement, {
            addItemOnChar: ',',
            // kind of hacky but is easiest way to avoid duplicates and showing the tag twice in the list
            autoAddItemCallback: this.addTag.bind(this),
            autoRemoveItemCallback: this.removeTag.bind(this),
            listCssClass: 'cn-border',
            itemRemoveCssClass: 'textboxlist-item-remove cn-icon',
            itemRemoveTitle: getJSMessage('blogpost.create.tags.remove.tooltip'),
            parseItemCallback: this.extractTagName
        });
        this.parent(searchElement);
        if (this.tagAutocompleter) {
            this.tagAutocompleter.addEvent('enterPressed', this.submitTags.bind(this));
        }
    },

    /**
     * @override
     */
    getTagAutocompleterPositionSource: function() {
        return this.domNode.getElement('.cn-write-note-accessory-tag .cn-border');
    },

    submitTags: function() {
        // triggered by onsubmit of a surrounding form, bit ugly but only possibility because we cannot
        // block event handlers, thus another key event handler would also be triggered when a choice in
        // the tag suggestion is selected  
        var tags = this.extractTagsFromInput();
        this.addTags(tags);
        this.tagAutocompleter.resetQuery(true);
    },

    attachmentUploadStarted: function(uploadDescriptor) {
        this.parent(uploadDescriptor);
        this.appendAttachmentUploadItem(uploadDescriptor.uploadId, uploadDescriptor.fileName);
    },

    attachmentUploadSucceeded: function(uploadId, attachmentData) {
        this.parent(uploadId, attachmentData);
        // empty input field (setting value of input to '' won't work in IE)
        this.contentAttachmentsContainer.getElement('form').reset();
    },

    attachmentUploadFailed: function(uploadId, message) {
        this.parent(uploadId, message);
        this.removeAttachmentOrUploadItem('-upload-' + uploadId);
    },

    showAccessory: function(name, focusInput) {
        var textboxList, placeholder, inputElem;
        var toggleElem = this.domNode.getElementById(this.widgetId + '-accessory-' + name);
        var contentElem = this.domNode.getElement('.cn-write-note-accessory-' + name);
        if (contentElem && toggleElem) {
            toggleElem.setStyle('display', 'none');
            contentElem.setStyle('display', '');
            textboxList = this[name + 'TextboxList'];
            if (textboxList) {
                // resize since it was not visible before and therefore the size is not correct
                textboxList.resizeInputToFillAvailableSpace();
                if (focusInput) {
                    textboxList.focusInput();
                }
            }
            // reposition the placeholder
            inputElem = contentElem.getElement('input[type=text]');
            if (inputElem) {
                placeholder = this.placeholders.getPlaceholder(inputElem);
                if (placeholder) {
                    placeholder.refresh();
                }
            }
        }
    },

    hideAccessory: function(name) {
        var toggleElem = this.domNode.getElementById(this.widgetId + '-accessory-' + name);
        var contentElem = this.domNode.getElement('.cn-write-note-accessory-' + name);
        if (contentElem && toggleElem) {
            toggleElem.setStyle('display', '');
            contentElem.setStyle('display', 'none');
        }
    },

    /**
     * Shows the accessories if they have content otherwise hide them.
     */
    showHideAccessories: function() {
        if (this.usersToNotify.ids.length) {
            this.showAccessory('user', false);
        } else {
            this.hideAccessory('user');
        }
        if (this.addedTags.length) {
            this.showAccessory('tag', false);
        } else {
            this.hideAccessory('tag');
        }
        if (this.attachments.ids.length) {
            this.showAccessory('attachment', false);
        } else {
            this.hideAccessory('attachment');
        }
    },

    showOrHideUserInterfaceForTopic: function(topicId) {
        var result = this.topicWriteAccessEvaluator.checkWriteAccess(topicId, this.initialTargetTopic);
        if (result.writeAccess === true) {
            // make sure the editor is shown, especially if it was hidden before
            this.showUserInterface();
        } else {
            // if the user has write access to one of the subtopics show the interface but remove the topic
            if (result.subtopicWriteAccess === true) {
                this.removeTopic(this.topics.items[0]);
                this.showUserInterface();
            } else {
                this.hideUserInterface(result.message || communote.i18n.getMessage('blogpost.create.no.writable.blog.selected'));
            }
        }
    },

    hideUserInterface: function(errorMsg) {
        var errorContainer = this.domNode.getElement('.cn-write-note-no-editor-content');
        errorContainer.set('text', errorMsg);
        if (!this.userInterfaceHidden) {
            this.domNode.getElement('.cn-write-note').addClass('cn-hidden');
            this.domNode.getElement('.cn-write-note-no-editor').removeClass('cn-hidden');
            this.userInterfaceHidden = true;
        }
    },
    showUserInterface: function() {
        if (this.userInterfaceHidden) {
            this.domNode.getElement('.cn-write-note-no-editor').addClass('cn-hidden');
            this.domNode.getElement('.cn-write-note').removeClass('cn-hidden');
            this.userInterfaceHidden = false;
            if (this.topicTextboxList) {
                // might have changed while invisible which leads to wrong positioning, so reposition it now
                this.topicTextboxList.resizeInputToFillAvailableSpace();
            }
        }
    },

    /**
     * @override
     */
    renderStyleChanged: function(oldStyle, newStyle) {
        var oldEditor, content;
        // ignore any style changes before the editor is set (e.g. during init)
        if (newStyle === 'full' && this.editor) {
            if (!instanceOf(this.editor, NoteTextEditorFactory.getSupportedEditorType())) {
                oldEditor = this.editor;
                this.editor = this.createEditor();
                if (this.firstDOMLoadDone) {
                    content = oldEditor.getContent();
                    oldEditor.resetContent(null);
                    this.refreshEditor();
                    if (!oldEditor.supportsHtml()) {
                        this.editor.resetContentFromPlaintext(content);
                    } else {
                        this.editor.resetContent(content);
                    }
                }
                oldEditor.cleanup();
            }
            if (oldStyle === 'simulate' && this.topicTextboxList) {
                // might have been resized while it was hidden which leads to wrong positioning
                this.topicTextboxList.resizeInputToFillAvailableSpace();
                this.refreshPlaceholder(this.topicTextboxList.getInputElement(), false);
            }
            this.showHideAccessories();
        } else if (newStyle === 'simulate' && this.editor) {
            this.editor.unFocus();
        }
    },

    /**
     * @override
     */
    attachmentAdded: function(attachmentData, moreToCome) {
        this.appendAttachmentItem(attachmentData);
    },

    /**
     * @override
     */
    attachmentRemoved: function(id) {
        this.removeAttachmentOrUploadItem(id ? '-attachment-' + id : null);
    },

    /**
     * @override
     */
    selectedTopicTitleChanged: function(topicItem) {
        if (this.topicTextboxList) {
            this.topicTextboxList.refreshContentOfItem(topicItem);
        }
    },

    /**
     * @override
     */
    tagAdded: function(tag) {
        this.parent();
        this.tagTextboxList.addItem(tag);
    },
    /**
     * @override
     */
    tagRemoved: function(tag) {
        this.parent();
        if (tag == null) {
            this.tagTextboxList.clearItems();
        } else {
            this.tagTextboxList.removeItem(tag);
        }
        this.refreshPlaceholder(this.getTagSearchElement(), false);
    },

    /**
     * @override
     */
    topicAdded: function(topicData, moreToCome) {
        if (this.topicTextboxList) {
            this.topicTextboxList.addItem(topicData);
        }
        if (!moreToCome) {
            this.enableDisableDmCheckbox();
        }
        if (this.getCrosspostTopicsCount() == 0) {
            this.refreshPlaceholder(this.getTopicSearchElement(),
                    'blogpost.create.topics.crosspost.hint');
        } else if (!moreToCome) {
            this.refreshPlaceholder(this.getTopicSearchElement(), false);
        }
    },

    /**
     * @override
     */
    topicRemoved: function(topicData) {
        if (!topicData) {
            this.topicTextboxList.clearItems();
        } else {
            this.topicTextboxList.removeItem(topicData);
        }
        this.enableDisableDmCheckbox();
        if (!this.getTargetTopicId()) {
            this.refreshPlaceholder(this.getTopicSearchElement(), 'blogpost.create.topics.hint');
        } else {
            this.refreshPlaceholder(this.getTopicSearchElement(), false);
        }
    },

    /**
     * Refresh a placeholder by repositioning it in case it is not a native one and optionally
     * updating the placeholder text.
     * 
     * @param {Element} inputElem The input element whose placeholder should be refreshed
     * @param {String} [msgKey] Message key of the new text
     */
    refreshPlaceholder: function(inputElem, msgKey) {
        var placeholder = this.placeholders.getPlaceholder(inputElem);
        if (placeholder) {
            if (msgKey) {
                placeholder.setText(getJSMessage(msgKey));
            }
            placeholder.refresh();
        }
    },

    /**
     * @override
     */
    userAdded: function(userData, moreToCome) {
        this.userTextboxList.addItem(userData);
        if (!moreToCome) {
            this.enableDisableDmCheckbox();
        }
    },

    /**
     * @override
     */
    userRemoved: function(userData) {
        if (!userData) {
            this.userTextboxList.clearItems();
        } else {
            this.userTextboxList.removeItem(userData);
        }
        this.enableDisableDmCheckbox();
        this.refreshPlaceholder(this.getUserSearchElement(), false);
    },

    appendAttachmentUploadItem: function(uploadId, fileName) {
        var newUploadItem, html, elem;
        var wrapper = this.contentAttachmentsContainer.getElementById(this.widgetId
                + '-summary-attachments');
        if (wrapper.hasClass('cn-hidden')) {
            wrapper.removeClass('cn-hidden');
        }
        newUploadItem = new Element('div', {
            'id': this.widgetId + '-upload-' + uploadId,
            'class' : 'cn-upload-process'
        });
        html = '<span class="cn-attachment-filename"></span>';
        newUploadItem.set('html', html);
        elem = newUploadItem.getElement('.cn-attachment-filename');
        elem.set('text', getJSMessage('blogpost.create.attachments.uploading', [ fileName ]));
        elem.setProperty('title', fileName);
        wrapper.grab(newUploadItem);
    },

    appendAttachmentItem: function(attachmentData) {
        var elem, newAttachmentElem, html, uploadElem;
        var wrapper = this.contentAttachmentsContainer.getElementById(this.widgetId
                + '-summary-attachments');
        if (wrapper.hasClass('cn-hidden')) {
            wrapper.removeClass('cn-hidden');
        }
        if (attachmentData.uploadId) {
            uploadElem = wrapper.getElementById(this.widgetId + '-upload-'
                    + attachmentData.uploadId);
        }
        newAttachmentElem = new Element('div', {
            'id': this.widgetId + '-attachment-' + attachmentData.id
        });
        html = '<span class="cn-attachment-filename"><span></span><a href="" target="_blank"></a></span>'
                + '<span class="cn-attachment-filesize"></span>'
                + '<span class="cn-attachment-remove"><a href="javascript:;" class="cn-icon" title="'
                + getJSMessage('blogpost.create.attachments.remove.tooltip')
                + '">&nbsp;</a></span>';
        newAttachmentElem.set('html', html);
        elem = newAttachmentElem.getElement('.cn-attachment-filename span');
        elem.set('text', attachmentData.fileName);
        elem.setProperty('title', attachmentData.fileName);
        elem = newAttachmentElem.getElement('a');
        elem.set('text', attachmentData.fileName);
        elem.setProperty('title', attachmentData.fileName);
        elem.setProperty('href', buildRequestUrl('/portal/files/' + attachmentData.id + '/'
                + attachmentData.fileName));
        elem = newAttachmentElem.getElement('.cn-attachment-filesize');
        elem.set('text', attachmentData.size);
        elem = newAttachmentElem.getElement('.cn-attachment-remove a');
        elem.addEvent('click', this.removeAttachment.bind(this, attachmentData.id));
        if (uploadElem) {
            newAttachmentElem.replaces(uploadElem);
        } else {
            wrapper.grab(newAttachmentElem);
        }
    },

    removeAttachmentOrUploadItem: function(elemIdSuffix) {
        var elem, checkHide;
        var wrapper = this.contentAttachmentsContainer.getElementById(this.widgetId
                + '-summary-attachments');
        if (elemIdSuffix) {
            elem = wrapper.getElementById(this.widgetId + elemIdSuffix);
            if (elem) {
                elem.destroy();
                checkHide = true;
            }
        } else {
            // destroy all
            wrapper.getChildren().destroy();
            checkHide = true;
        }
        if (checkHide && wrapper.getChildren().length == 0) {
            wrapper.addClass('cn-hidden');
        }
    },

    extractTopicName: function(topicData) {
        return topicData.title;
    },
    extractUserName: function(userData) {
        return userData.longName;
    },
    extractTagName: function(tag) {
        return tagName = (tag.name == undefined) ? tag.defaultName : tag.name;
    },
    enableDisableDmCheckbox: function() {
        var checkBox, checkBoxLabel;
        checkBox = document.id(this.widgetId + '-direct-message');
        if (checkBox) {
            checkBoxLabel = document.id(this.widgetId + '-direct-message-label');
            // disable the checkbox if more than one blog was selected or no user was added
            if (this.getCrosspostTopicsCount() > 0 || this.usersToNotify.ids.length == 0 || this.isDirectMessage && this.action == 'edit') {
                checkBox.disabled = true;
                checkBoxLabel.addClass('font-gray');
            } else {
                checkBox.disabled = false;
                checkBoxLabel.removeClass('font-gray');
            }
        }
    },

    directMessageModeChanged: function() {
        var wrapperElem;
        var checkBox = this.domNode.getElementById(this.widgetId + '-direct-message');
        if (this.topicTextboxList) {
            this.topicTextboxList.setLimit(this.isDirectMessage ? 1 : -1);
        }
        if (checkBox) {
            checkBox.checked = this.isDirectMessage;
        }
    }
});
