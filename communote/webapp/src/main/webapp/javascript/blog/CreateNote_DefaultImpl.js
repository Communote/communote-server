CreateNoteDefaultWidget = new Class({
    Extends: CreateNoteWidget,
    Implements: C_FilterParameterListener,

    observedParams: [ 'targetBlogId' ],

    filterGroup: null,
    filterParameterStore: null,

    plainTextEditorCssClass: 'cn-write-note-plaintext',
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
        if (this.action === 'create') {
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
        if (this.action === 'create') {
            this.editor.focus();
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
        this.eventEmitter.emit('targetTopicChanged', this.targetTopic);
        // invalidate caches of autocompeters to let them restart a query for the same term
        // after topic (request parameter) changed
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

    /**
     * @override
     */
    refreshView: function(autosaveLoaded) {
        this.parent(autosaveLoaded);
        if (autosaveLoaded && this.renderStyle !== 'full') {
            this.setRenderStyle('full');
        }
    },

    /**
     * @override
     */
    getWriteContainerElement: function() {
        return this.domNode.getElement('.cn-write-note-editor');
    },


    showOrHideUserInterfaceForTopic: function(topicId) {
        var result = this.topicWriteAccessEvaluator.checkWriteAccess(topicId, this.initialTargetTopic);
        if (result.writeAccess === true) {
            // make sure the editor is shown, especially if it was hidden before
            this.showUserInterface();
        } else {
            // if the user has write access to one of the subtopics show the interface but remove the topic
            if (result.subtopicWriteAccess === true) {
                this.setTargetTopic(null);
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
            this.eventEmitter.emit('userInterfaceHidden');
        }
    },
    showUserInterface: function() {
        if (this.userInterfaceHidden) {
            this.domNode.getElement('.cn-write-note-no-editor').addClass('cn-hidden');
            this.domNode.getElement('.cn-write-note').removeClass('cn-hidden');
            this.userInterfaceHidden = false;
            this.eventEmitter.emit('userInterfaceShown');
        }
    },

    /**
     * @override
     */
    renderStyleChanged: function(oldStyle, newStyle) {
        var oldEditor, content;
        this.eventEmitter.emit('renderStyleChanged', {
            oldStyle: oldStyle,
            newStyle: newStyle
        });
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
        } else if (newStyle === 'simulate' && this.editor) {
            this.editor.unFocus();
        }
    }
});
