// TODO rename to NoteEditorWidget and move to widget/note/editor
var CreateNoteWidget = new Class({
    Extends: C_Widget,

    widgetGroup: "blog",

    // the current working mode of the widget (edit, comment, repost or create)
    action: null,
    ajaxLoadingOverlay: null,
    /**
     * Object that maps the source render style to the allowed target render styles (array of
     * styles). If null all transitions are allowed.
     */
    allowedRenderStyleTransitions: null,
    autosaveDisabled: false,
    // local reference to the blogUtils for defaultBlog handling
    blogUtils: null,
    // defines what should happen when cancel method is called. These defaults are intended
    // for create mode and will differ slightly for edit and comment but can also be overridden
    // by configuration.
    cancelBehavior: {
        // whether to discard the autosave and reset the editor. If there is no autosave the
        // editor will only be reset if modified.
        discardAutosave: true,
        // whether to show a dialog to let the user confirm the removal of the autosave, if there
        // is one
        confirmDiscard: true,
        // whether to show a dialog to let the user confirm the reset, if there are modifications
        // but no autosave
        confirmReset: true,
        // defines what to do after handling the autosave/modification. Can be 'remove' to remove
        // the editor, false to do nothing or renderStyle to set another renderStyle. In case of
        // the latter actionOptions must hold the name of the new style.
        action: false,
        // can hold additional details to run the action. The allowed values depend on the action
        actionOptions: undefined
    },
    // ComponentManager with all components for the current working mode/action
    components: null,
    // whether the editor is currently dirty. Will be reset after a successful autosave or submit.
    dirty: false,
    // the NoteTextEditor instance
    editor: null,
    // emitter for local events, mainly for notifying the components
    eventEmitter: null,
    /**
     * JSON object holding initial note data which will be restored when resetting the note. This
     * data is provided with the response metadata when refreshing the widget. In the edit case this
     * will be the note to edit. Additionally the content can be provided with a static parameter to
     * overwrite the content of the initialNote. In case there is an autosave, the autosave has
     * precedence over the initial note when initializing the widget content after a refresh.
     */
    initialNote: null,
    /**
     * A topic extracted from settings during initialization, can be null. Will only be set when in
     * create mode and is used when resetting the editor.
     */
    initialTargetTopic: null,
    targetTopic: null,
    // if the note was modified in some way. Will be reset with initContent after publishing or reverting
    modified: false,
    noTargetTopicChangeIfModifiedOrAutosaved: false,
    // an array of note property objects to be included when sending the note
    noteProperties: null,
    parentPostId: null,
    // css classes to be applied to the create note container (getWriteContainerElement)
    // when the editor only supports plain text
    plainTextEditorCssClass: 'cn-write-note-plaintext',
    // note properties to pass along with every note. Can be set with the same-named static parameter.
    predefinedNoteProperties: null,
    // defines what should happen after a note was published successfully and the editor was
    // cleared. If the editor is in comment or edit mode the action will default to 'remove'.
    publishSuccessBehavior: {
        // can be 'remove' to remove the editor, false to do nothing or renderStyle to set another
        // renderStyle. In case of the latter actionOptions must hold the name of the new style.
        action: false,
        actionOptions: undefined
    },
    /**
     * The render style of the widget
     */
    renderStyle: null,
    renderStyleCssClassPrefix: 'cn-write-note-render-style-',

    // css classes to be applied to the create note container (getWriteContainerElement)
    // when the editor only supports richtext
    richTextEditorCssClass: null,
    /**
     * Supported render styles of the widget. The first is the default.
     */
    supportedRenderStyles: [ 'full', 'minimal', 'simulate' ],
    targetBlogIdChangeTracker: null,
    topicWriteAccessEvaluator: null,
    userInterfaceHidden: false,


    init: function() {
        var action, targetBlogId, targetBlogTitle, parentPostId, autosaveDisabled;
        var filterGroupId, cancelBehavior, publishSuccessBehavior, renderStyle;
        this.parent();
        this.eventEmitter = new communote.classes.EventEmitter();
        this.noTargetTopicChangeIfModifiedOrAutosaved = !!this
                .getStaticParameter('noTargetTopicChangeIfModifiedOrAutosaved');
        this.blogUtils = communote.utils.topicUtils;
        action = this.getStaticParameter('action') || 'create';
        this.setFilterParameter('action', action);
        this.action = action;

        if (action === 'create') {
            targetBlogId = this.getStaticParameter('targetBlogId');
            targetBlogTitle = this.getStaticParameter('targetBlogTitle');
            this.setInitialTargetTopic(targetBlogId, targetBlogTitle);
            // observe targetBlogId changes when in create mode and a repo is configured
            filterGroupId = this.staticParams.filterWidgetGroupId;
            if (filterGroupId && communote.filterGroupRepo[filterGroupId]) {
                this.targetBlogIdChangeTracker = new communote.classes.FilterParameterChangedHandler(
                        filterGroupId, 'targetBlogId', this.onTargetBlogIdChanged.bind(this));
                // check for targetBlogId which can be part of the widget settings (see above) or
                // set in the filter store as initial parameter, but give setting preference
                if (!this.initialTargetTopic) {
                    this.setInitialTargetTopic(this.targetBlogIdChangeTracker
                            .getCurrentValue(), null);
                }
            }
        } else {
            // set some other defaults for the cancelBehavior, mainly for backwards compatibility
            this.cancelBehavior.confirmDiscard = false;
            this.cancelBehavior.confirmReset = false;
            this.cancelBehavior.action = 'remove';
            this.publishSuccessBehavior.action = 'remove';
        }
        cancelBehavior = this.getStaticParameter('cancelBehavior');
        if (cancelBehavior) {
            if (typeof cancelBehavior == 'string') {
                cancelBehavior = JSON.decode(cancelBehavior);
            }
            Object.merge(this.cancelBehavior, cancelBehavior);
        }
        publishSuccessBehavior = this.getStaticParameter('publishSuccessBehavior');
        if (publishSuccessBehavior) {
            if (typeof publishSuccessBehavior == 'string') {
                publishSuccessBehavior = JSON.decode(publishSuccessBehavior);
            }
            Object.merge(this.publishSuccessBehavior, publishSuccessBehavior);
        }
        
        this.setRenderStyle(this.getStaticParameter('renderStyle'));
        this.editor = this.createEditor();
        if (!this.editor.supportsHtml()) {
            if (this.renderStyle !== 'full') {
                // check whether the editor in full render style would support HTML because the
                // editor will switch to full if there is an autosave.
                renderStyle = this.renderStyle;
                this.renderStyle = 'full';
                this.setFilterParameter('plaintextOnly', !this.createEditor().supportsHtml());
                this.renderStyle = renderStyle;
            } else {
                this.setFilterParameter('plaintextOnly', true);
            }
        }
        
        this.copyStaticParameter('repostNoteId');
        if (this.getFilterParameter('repostNoteId')) {
            // change action to repost (pure client-side feature)
            this.action = 'repost';
        }
        this.copyStaticParameter('noteId');
        parentPostId = this.getStaticParameter('parentPostId');
        if (parentPostId) {
            this.parentPostId = parentPostId;
            this.setFilterParameter('parentPostId', parentPostId);
        }

        this.copyStaticParameter('authorNotification');
        this.copyStaticParameter('inheritTags');
        this.copyStaticParameter('copyAttachments');

        autosaveDisabled = this.getStaticParameter('autosaveDisabled');
        if (autosaveDisabled === 'true' || autosaveDisabled === true) {
            this.autosaveDisabled = true;
            // might have been set to true by subclass overriding the default or via static
            // parameter
            this.setFilterParameter('autosaveDisabled', true);
        }
        this.predefinedNoteProperties = this.getStaticParameter('predefinedNoteProperties');

        this.initAutosaveHandler();
        this.components = new communote.classes.NoteEditorComponentManager(this, action,
                this.renderStyle, this.getAllStaticParameters());
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
    
    addEventListener: function(eventName, fn, context) {
        this.eventEmitter.on(eventName, fn, context);
    },

    /**
     * @override
     */
    beforeRemove: function() {
        this.stopAutosaveJob();
        // TODO do autosave?
        if (this.targetBlogIdChangeTracker) {
            this.targetBlogIdChangeTracker.destroy();
        }
        this.cleanup();
        this.eventEmitter.emit('widgetRemoving');
    },

    /**
     * Cancel the edit, update or create operation. The actual behavior of this function is defined
     * by the configurable cancelBehavior.
     *
     * @param Event [event] The event that triggered cancel.
     */
    cancel: function(event) {
        var remove, changeRenderStyle, hasAutosave, modified, postRemoveOperation;
        var hasOnlineAutosave;
        var behavior = this.cancelBehavior;
        if (behavior.action) {
            remove = behavior.action === 'remove';
            changeRenderStyle = behavior.action === 'renderStyle'
                    && this.supportedRenderStyles.contains(behavior.actionOptions);
            // fallback to no action
            if (!remove && !changeRenderStyle) {
                behavior.action = false;
            }
        }
        hasAutosave = this.hasAutosave();
        modified = this.isModified();
        if (behavior.discardAutosave && (hasAutosave || modified)) {
            hasOnlineAutosave = this.autosaveHandler && this.autosaveHandler.hasOnlineAutosave();
            postRemoveOperation = function() {
                this.eventEmitter.emit('noteDiscarded', hasOnlineAutosave);
                if (!remove) {
                    this.resetToNoAutosaveState();
                    if (changeRenderStyle) {
                        this.setRenderStyle(behavior.actionOptions);
                    }
                } else {
                    this.widgetController.removeWidget(this);
                }
            }.bind(this);
            if (hasAutosave) {
                if (behavior.confirmDiscard || behavior.confirmReset) {
                    this.autosaveHandler.discardWithConfirm(event, postRemoveOperation);
                } else {
                    this.autosaveHandler.discard(postRemoveOperation);
                }
            } else {
                // only modified. Can call the same operations but have to stop the autosave job before
                this.stopAutosaveJob();
                if (behavior.confirmReset) {
                    showConfirmDialog(getJSMessage('create.note.dialog.discardChanges.title'),
                            getJSMessage('create.note.dialog.discardChanges.question'),
                            postRemoveOperation, {
                                triggeringEvent: event,
                                onCloseCallback: this.startAutosaveJob.bind(this)
                            });
                } else {
                    postRemoveOperation();
                }
            }
        } else {
            // just run the action, if any
            if (remove) {
                this.widgetController.removeWidget(this);
            } else if (changeRenderStyle) {
                this.setRenderStyle(behavior.actionOptions);
            }
        }
    },

    /**
     * Implementation of the 'approveMatchCallback' callback of the AutocompleterStaticDataSource
     * that excludes the
     *
     * @@discussion suggestion when not used in a discussion context.
     */
    checkDiscussionContextApproveMatchCallback: function(query, staticSearchDefinition) {
        if (staticSearchDefinition.inputValue != '@discussion') {
            return true;
        }
        // @@discussion should only be matched when in a discussion context: create note or edit
        // a note that is not part of a discussion with more than one note
        if (this.action === 'create' || this.action === 'repost'
                || (this.action == 'edit' && this.initialNote.numberOfDiscussionNotes == 1)) {
            return false;
        }
        return true;
    },

    /**
     * Cleanup any resources to avoid memory leaks
     */
    cleanup: function() {
        this.editor.cleanup();
    },

    /**
     * Called after the content of the widget has been initialized after a refresh.
     *
     * @param {boolean} fromAutosave If true, the widget was initialized with an autosave
     */
    contentInitializedAfterRefresh: function(fromAutosave) {
    },

    /**
     * Create the NoteTextEditor to be used and return it.
     *
     * @return {NoteTextEditor} the editor
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
            options.tagAutocompleterOptions = this.prepareAutocompleterOptions(
                    this.setCurrentTopicBeforeRequestCallback.bind(this));
            options.userAutocompleterOptions = this.prepareAutocompleterOptions(
                    this.setCurrentTopicBeforeRequestCallback.bind(this));
            options.userAutocompleterOptions.staticDataSourceOptions = {};
            options.userAutocompleterOptions.staticDataSourceOptions.approveMatchCallback = this.checkDiscussionContextApproveMatchCallback
                    .bind(this);
            return NoteTextEditorFactory.createSupportedEditor(null, options);
        }
    },

    /**
     * Emit an event and notify all NoteEditorComponents or any other listener which registered for the
     * event with addEventListener.
     * 
     * @param {String} eventName The name of the event to emit.
     * @param {*} [eventData] Any data to pass to the listener.
     */
    emitEvent: function(eventName, eventData) {
    	this.eventEmitter.emit(eventName, eventData);
    },
    extractInitData: function(responseMetadata) {
        var targetTopic, autosave, initObject, initialNote, autosaveLoaded, content;
        if (responseMetadata) {
            initObject = responseMetadata.initialNote;
        }
        if (initObject) {
            targetTopic = initObject.targetBlog;
        } else {
            targetTopic = this.getTargetTopicForCreate();
            initObject = {};
        }
        // use provided content
        content = this.getStaticParameter('content');
        if (content) {
            content = content.replace(/<br\s*\/?>/ig, '\n');
            initObject.content = unescapeXML(content);
        }
        // save initial note object as copy
        initialNote = Object.clone(initObject);
        // force invocation of resetTargetTopic when initializing with initialNote and no topic is
        // set in 'create' mode 
        if (!initialNote.targetBlog && this.action === 'create') {
            initialNote.forceResetTargetTopic = true;
        }
        // check for autosave and if available replace initObject with it to init with autosave
        if (this.autosaveHandler) {
            autosave = this.autosaveHandler.load(responseMetadata);
            if (autosave) {
                autosaveLoaded = true;
                initObject = autosave.noteData;
            }
        }
        // force the target topic of the parent or edited note, or in case of create of the autosave
        if (this.action !== 'create' || !autosaveLoaded
                || (this.initialTargetTopic && !this.noTargetTopicChangeIfModifiedOrAutosaved)) {
            initObject.targetBlog = targetTopic;
        }
        return {
            initObject: initObject,
            initialNote: initialNote,
            isAutosave: autosaveLoaded
        };
    },

    getListeningEvents: function() {
        return [];
    },
    
    getNoteData: function(resetDirtyFlag) {
        var data = {};
        data.content = this.editor.getContent();
        data.targetTopic = this.targetTopic;
        data.isHtml = this.editor.supportsHtml();
        data.noteId = this.getFilterParameter('noteId');
        if (this.action != 'edit') {
            data.parentNoteId = this.parentPostId;
        }

        data.properties = [];
        // add properties extracted from init object. Components can overwrite them.
        if (this.noteProperties) {
        	communote.utils.propertyUtils.mergeProperties(data.properties, this.noteProperties);
        }
        this.components.appendNoteData(data, resetDirtyFlag);

        // add predefined properties and let them overwrite properties added by components
        if (this.predefinedNoteProperties) {
        	communote.utils.propertyUtils.merge(data.properties, this.predefinedNoteProperties); 
        }
        if (data.properties.length === 0) {
            delete data.properties;
        }
        if (resetDirtyFlag) {
            this.dirty = false;
        }
        return data;
    },
    /**
     * Creates an object that holds all the information to be submitted when sending the note via
     * REST API.
     *
     * @param {boolean} publish whether the note will be published or stored as an autosave
     * @param {String} content the content of the note
     * @return {Object} an object with all information for storing the note
     */
    getNoteDataForRestRequest: function(publish, content) {
        var i, name;
        var data = {};
        var writeContainerElem = this.getWriteContainerElement();
        var hiddenInputs = writeContainerElem.getElements('input[type=hidden]');
        for (i = 0; i < hiddenInputs.length; i++) {
            name = hiddenInputs[i].getProperty('name');
            if (name != 'topicId') {
                data[name] = hiddenInputs[i].value;
            }
        }
        data.properties = [];
        // add properties extracted from init object. Components can overwrite them.
        if (this.noteProperties) {
            communote.utils.propertyUtils.mergeProperties(data.properties, this.noteProperties);
        }
        data.text = content;
        data.isHtml = this.editor.supportsHtml();
        data.topicId = this.getTargetTopicId();
        data.noteId = this.getFilterParameter('noteId');
        if (this.action != 'edit') {
            data.parentNoteId = this.parentPostId;
        }
        this.components.appendNoteDataForRestRequest(data, publish, false);
        data.publish = publish === true ? true : false;
        if (this.autosaveHandler) {
            data.autosaveNoteId = this.autosaveHandler.getNoteId();
            data.noteVersion = this.autosaveHandler.getVersion();
        }
        // add predefined properties and let them overwrite properties added by components
        if (this.predefinedNoteProperties) {
            communote.utils.propertyUtils.merge(data.properties, this.predefinedNoteProperties); 
        }
        if (data.properties.length === 0) {
            delete data.properties;
        }
        return data;
    },

    getSendButtonElement: function() {
        return this.domNode.getElementById(this.widgetId + '-send-button');
    },

    getTargetTopicForCreate: function() {
        var topic, defaultTopic;
        // check for initialTargetTopic, which is the one provided as static parameter or if not defined set defaultBlogId
        if (this.initialTargetTopic) {
            topic = this.initialTargetTopic;
        } else if (this.blogUtils.isDefaultBlogEnabled()) {
            defaultTopic = this.blogUtils.getDefaultBlog();
            // TODO is cloning really necessary
            topic = {
                id: defaultTopic.id,
                title: defaultTopic.title,
                alias: defaultTopic.alias
            };
        }
        return topic;
    },

    getTargetTopicId: function() {
        return this.targetTopic && this.targetTopic.id;
    },

    /**
     * Return the element that wraps the body of the widget including textarea and additional
     * controls like the attachment upload. This element will receive render style CSS class
     * constructed from the #renderStyleCssClassPrefix and the current render style. The default
     * imlementation looks for child node with class 'control-write-note-body-wrapper'.
     *
     * @return {Element} the wrapper element or null if the widget does not need such a wrapper
     */
    getWrapperElement: function() {
        return this.domNode.getElement('.control-write-note-body-wrapper');
    },

    /**
     * Return the element that contains the input element for the post data and some (optional)
     * additional hidden inputs that provide data to be sent to the server when sending the note.
     */
    getWriteContainerElement: function() {
        return this.domNode.getElement('.cn-write-note-editor');
    },

    /**
     * @return {Boolean} whether there is an autosave, no matter if it was loaded or created later
     *         on
     */
    hasAutosave: function() {
        if (this.autosaveHandler) {
            return this.autosaveHandler.hasAutosave();
        }
        return false;
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

    initAutosaveHandler: function() {
        var action, noteId, options, autosaveTimeout;
        if (this.autosaveDisabled) {
            return;
        }
        action = this.action;
        if (action === 'edit') {
            noteId = this.getFilterParameter('noteId');
        } else if (action === 'comment') {
            noteId = this.parentPostId;
        } else if (action === 'repost') {
            noteId = this.getFilterParameter('repostNoteId');
        }
        options = this.getStaticParameter('autosaveOptions') || {};
        if (!options.defaultDiscardCompleteCallback) {
            options.defaultDiscardCompleteCallback = this.resetToNoAutosaveState.bind(this);
        }
        // prefer new option
        if (!options.autosaveTimeout) {
            // check old option (value is in seconds)
            autosaveTimeout = this.getStaticParameter('draftTimer');
            if (autosaveTimeout != null) {
                options.autosaveTimeout = autosaveTimeout * 1000;
            }
        }
        this.autosaveHandler = new communote.classes.NoteEditorAutosaveHandler(this, action, noteId,
                options);
    },

    initContent: function(note) {
        if (!note) {
            // clearAll
            this.resetTargetTopic();
            this.components.initContent(null);
            this.editor.resetContent(null);
            this.noteProperties = null;
        } else {
            if (note.forceResetTargetTopic) {
                this.resetTargetTopic();
            } else {
                this.setTargetTopic(note.targetBlog);
            }
            this.components.initContent(note);
            this.editor.resetContent(note.content);
            this.noteProperties = note.properties;
        }
        this.dirty = false;
        this.modified = false;
    },

    isDirty: function() {
        return this.dirty || this.editor.isDirty() || this.components.isDirty();
    },
    
    isModified: function() {
        if (!this.modified) {
            if (this.editor.isDirty()) {
                this.modified = true;
            }
        }
        return this.modified || this.components.isModified();
    },

    /**
     * Callback that is invoked when the title of the target topic got loaded via REST API and that
     * request resulted in an error.
     */
    loadTargetTopicInfoErrorCallback: function(topicId, response) {
        // check if still the same topic
        if (this.initialTargetTopic && this.initialTargetTopic.id == topicId) {
            delete this.initialTargetTopic.loadingTitleAsync;
            delete this.initialTargetTopic.loadingTitle;
            if (response.httpStatusCode == 404) {
                this.initialTargetTopic.notFound = true;
                // don't know which request (refresh, topic-info GET or topic-rights) is faster -> 
                // don't hide the interface while still loading, the topic-rights check callback will do it
                if (this.firstDOMLoadDone) {
                    this.hideUserInterface(response.message);
                } else {
                    this.initialTargetTopic.notFoundErrorMessage = response.message;
                }
            } else if (response.httpStatusCode == 403) {
                this.initialTargetTopic.noReadAccess = true;
            } else {
                this.blogUtils.defaultErrorCallback(response);
            }
        }
    },

    /**
     * Callback that is invoked when the title of the target topic got loaded via REST API.
     */
    loadTargetTopicInfoSuccessCallback: function(topicInfo) {
        // check if still the same topic
        if (this.initialTargetTopic && this.initialTargetTopic.id == topicInfo.topicId) {
            this.initialTargetTopic.title = topicInfo.title;
            if (this.initialTargetTopic.loadingTitleAsync) {
                delete this.initialTargetTopic.loadingTitleAsync;
                delete this.initialTargetTopic.loadingTitle;
                // if the topic is current targetTopic notify subclasses
                if (this.targetTopic == this.initialTargetTopic) {
                    this.targetTopicTitleChanged(this.initialTargetTopic);
                }
            }
        }
    },

    notePublished: function(topicId, resultObj) {
        var noteChangedDescr, noteId;
        this.stopPublishNoteFeedback();
        if (resultObj.status == 'ERROR') {
            this.showErrorMessage(resultObj);
            this.startAutosaveJob();
        } else {
            // noteUtils are using REST API version 3.0 which just returns the noteId
            noteId = resultObj.result;
            if (resultObj.status == 'WARNING') {
                // show warning message
                showNotification(NOTIFICATION_BOX_TYPES.warning, '', resultObj.message);
            } else if (resultObj.status == 'OK') {
                // show success message
                showNotification(NOTIFICATION_BOX_TYPES.success, '', resultObj.message);
            }
            // inform widgets and also provide the action, but treat repost like create
            if (noteId) {
                noteChangedDescr = {
                    action: this.action === 'repost' ? 'create' : this.action,
                    noteId: noteId,
                    topicId: topicId
                };
                if (this.action == 'comment') {
                    noteChangedDescr.parentNoteId = this.parentPostId;
                    noteChangedDescr.discussionId = this.getStaticParameter('discussionId');
                }
                E('onNotesChanged', noteChangedDescr);
            }
            // TODO fire internal event?
            if (this.autosaveHandler) {
                this.autosaveHandler.notePublished();
            }
            // no need to clean up if removing it anyway
            if (this.publishSuccessBehavior.action != 'remove') {
                this.initContent(null);
                this.startAutosaveJob();
                if (this.publishSuccessBehavior.action == 'renderStyle') {
                    this.setRenderStyle(this.publishSuccessBehavior.actionOptions);
                }
            } else {
                this.widgetController.removeWidget(this);
            }
        }
    },
    
    /**
     * Callback of the targetBlogIdChangeTracker which observes the filter parameter store for
     * changes of the parameter targetBlogId.
     */
    onTargetBlogIdChanged: function(oldTopicId, newTopicId) {
        // update the initialTargetTopic so it will be set the next time an autosave is discarded or note was sent
        this.setInitialTargetTopic(newTopicId, null);
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
     * Prepares the options for an autocompleter.
     *
     * @param {Function} [beforeRequestCallback] A callback function to be passed to the DataSource
     * @returns {Object} the prepared options object
     */
    prepareAutocompleterOptions: function(beforeRequestCallback) {
        var preparedOptions = {};
        preparedOptions.autocompleterOptions = {};
        if (beforeRequestCallback) {
            preparedOptions.dataSourceOptions = {};
            preparedOptions.dataSourceOptions.beforeRequestCallback = beforeRequestCallback;
        }
        return preparedOptions;
    },

    publishNote: function() {
        // do nothing if there are still running uploads
        if (this.getSendButtonElement().disabled
                || !this.components.canPublishNote()) {
            // TODO show an error message?
            return false;
        }
        // stop autosaves
        this.stopAutosaveJob();
        this.startPublishNoteFeedback();
        this.publishNoteWhenAutosaveDone();
    },

    publishNoteWhenAutosaveDone: function() {
        var content;
        // TODO maybe have a more generic solution like letting the autosaveHandler disable publishing temporarily?
        if (this.autosaveHandler && this.autosaveHandler.isAutosaveInProgress()) {
            // wait for end of autosave
            this.publishNoteWhenAutosaveDone.delay(1000, this);
        } else {
            content = this.editor.getContent();
            this.sendNote(content);
        }
    },

    refresh: function() {
        this.cleanup();
        this.eventEmitter.emit('widgetRefreshing');
        this.parent();
    },

    refreshComplete: function(responseMetadata) {
        var initData;
        // set a marker flag to be able to react depending on the current working context (pre & post init)  
        this.initializingAfterRefresh = true;
        initData = this.extractInitData(responseMetadata);
        this.initialNote = initData.initialNote;

        // TODO better name
        this.refreshView(initData.isAutosave);
        this.refreshEditor();
        this.eventEmitter.emit('widgetRefreshed');
        
        this.initContent(initData.initObject);
        this.contentInitializedAfterRefresh(initData.isAutosave);
        // TODO maybe let autosaveHandler.editorInitialized start job 
        if (this.autosaveHandler) {
            this.autosaveHandler.editorInitialized();
            this.startAutosaveJob();
        }
        init_tips(this.domNode);
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

    refreshEditor: function() {
        var writeContainerElem, classToAdd, classToRemove;
        // apply some CSS classes before refreshing the actual editor component as it might
        // work on the resulting styles (like expanding textarea)
        writeContainerElem = this.getWriteContainerElement();
        if (this.editor.supportsHtml()) {
            classToAdd = this.richTextEditorCssClass;
            classToRemove = this.plainTextEditorCssClass;
            this.domNode.getElement('.cn-write-note-editor-fields').removeClass('cn-border');
        } else {
            classToAdd = this.plainTextEditorCssClass;
            classToRemove = this.richTextEditorCssClass;
            // if using plain text editor add 'cn-border' class to textarea wrapper to
            // get correct look
            this.domNode.getElement('.cn-write-note-editor-fields').addClass('cn-border');
        }
        if (classToAdd) {
            writeContainerElem.addClass(classToAdd);
        }
        if (classToRemove) {
            writeContainerElem.removeClass(classToRemove);
        }

        this.editor.refresh(writeContainerElem);
    },

    refreshView: function(autosaveLoaded) {
        this.ajaxLoadingOverlay = this.widgetController.createAjaxLoadingOverlay(this.domNode,
                false);
        if (autosaveLoaded && this.renderStyle !== 'full') {
            this.setRenderStyle('full');
        }
    },
    
    remove: function(deleteAutosave) {
        var hasOnlineAutosave;
        if (deleteAutosave) {
            if (this.action != 'create') {
                hasOnlineAutosave = this.autosaveHandler && this.autosaveHandler.hasOnlineAutosave();
                this.eventEmitter.emit('noteDiscarded', hasOnlineAutosave);
                if (this.autosaveHandler) {
                    this.autosaveHandler.discard(false);
                }
            }
        }
        this.widgetController.removeWidget(this);
    },
    
    removeEventListener: function(eventName, fn, context) {
        this.eventEmitter.off(eventName, fn, context);
    },

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
     * Helper which replaces the target topic in 'create' mode with the topic returned by
     * getTargetTopicForCreate.
     */
    resetTargetTopic: function() {
        if (this.action === 'create') {
            this.targetTopic = this.getTargetTopicForCreate();
            this.eventEmitter.emit('targetTopicReset', this.targetTopic);
            this.targetTopicChanged();
        }
    },

    /**
     * Reset the editor to the initialNote. Should only be called if there is no autosave or the
     * autosave was removed.
     */
    resetToNoAutosaveState: function() {
        // restore original note
        this.initContent(this.initialNote);
        this.startAutosaveJob();
    },

    /**
     * Send the note.
     * @param {String} content The text/HTML content of the note
     * @param {boolean} [ignoreUncommittedOptions] if false a popup will warn the user when he
     *            publishes a note and there are uncommitted inputs in the blog and user input
     *            fields. If true there will be no warning.
     */
    sendNote: function(content, ignoreUncommittedOptions) {
        var warningMsg, cancelFunction, buttons;
        var successCallback, errorCallback, data, options;
        if (!ignoreUncommittedOptions) {
            warningMsg = this.components.getUnconfirmedInputWarning();
            if (warningMsg) {
                cancelFunction = function() {
                    this.stopPublishNoteFeedback();
                    this.startAutosaveJob();
                }.bind(this);
                buttons = [];
                buttons.push({
                    type: 'yes',
                    action: this.sendNote.bind(this, content, true)
                });
                buttons.push({
                    type: 'no',
                    action: cancelFunction
                });
                showDialog(this.getSendButtonElement().value, warningMsg, buttons, {
                    onCloseCallback: cancelFunction,
                    width: 300
                });
                return;
            }
        }
        
        data = this.getNoteDataForRestRequest(true, content);
        options = {};

        options.defaultErrorMessage = getJSMessage('error.blogpost.create.failed');
        // can use note published callback for success and error since state is evaluated there
        successCallback = this.notePublished.bind(this, data.topicId);
        errorCallback = successCallback;
        if (this.action == 'edit') {
            noteUtils.updateNote(data.noteId, data, successCallback, errorCallback, options);
        } else {
            noteUtils.createNote(data, successCallback, errorCallback, options);
        }
    },

    /**
     * AutocompleterRequestDataSource callback implementation that sets the current topic before
     * sending the request.
     */
    setCurrentTopicBeforeRequestCallback: function(request, postData, queryParam) {
        postData['blogId'] = this.getTargetTopicId();
    },

    setInitialTargetTopic: function(id, title) {
        var topicData, topic;
        if (id != undefined) {
            topic = {};
            topic.id = id;
            this.initialTargetTopic = topic;
            if (!title) {
                topic.title = ' ';
                topic.loadingTitle = true;
                if (this.blogUtils.getTopicInfo(id, this.loadTargetTopicInfoSuccessCallback
                        .bind(this), this.loadTargetTopicInfoErrorCallback.bind(this, id))) {
                    delete topic.loadingTitle;
                } else {
                    topic.loadingTitleAsync = true;
                }
            } else {
                topic.title = title;
            }
        } else {
            this.initialTargetTopic = undefined;
        }
    },

    setRenderStyle: function(newRenderStyle) {
        var oldStyle, elem, transitions;
        if (!this.supportedRenderStyles.contains(newRenderStyle)) {
            // take the default render style if the provided is not supported
            newRenderStyle = this.supportedRenderStyles[0];
        }
        // check if change is allowed
        transitions = this.allowedRenderStyleTransitions;
        if (newRenderStyle == this.renderStyle
                || transitions
                && (transitions[this.renderStyle] && !transitions[this.renderStyle]
                        .contains(newRenderStyle))) {
            return;
        }
        oldStyle = this.renderStyle;
        this.renderStyle = newRenderStyle;
        this.setFilterParameter('renderStyle', newRenderStyle);
        // set the new style css class to the wrapper element if it exists
        elem = this.getWrapperElement();
        if (elem && this.renderStyleCssClassPrefix) {
            elem.removeClass(this.renderStyleCssClassPrefix + oldStyle);
            elem.addClass(this.renderStyleCssClassPrefix + newRenderStyle);
        }
        this.renderStyleChanged(oldStyle, newRenderStyle);
    },
    
    setTargetTopic: function(newTargetTopic) {
        // only allow changing of target topic when in a mode where notes are created
        if (!this.targetTopic || this.action === 'create' || this.action === 'repost') {
            // ignore if nothing changed
            if ((!newTargetTopic && this.targetTopic) || 
                    (newTargetTopic && newTargetTopic.id != this.getTargetTopicId())) {
                this.targetTopic = newTargetTopic;
                this.targetTopicChanged();
                this.dirty = true;
                this.modified = true;
            }
        }
    },

    showErrorMessage: function(errObj) {
        var message;
        if (errObj.errors && typeOf(errObj.errors) == 'array' && errObj.errors.length > 0) {
            // just take 1st error
            message = errObj.errors[0].message;
        } else {
            message = errObj.message;
        }
        hideNotification();
        showNotification(NOTIFICATION_BOX_TYPES.error, '', message, {
            duration: ''
        });
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

    showUserInterface: function() {
        if (this.userInterfaceHidden) {
            this.domNode.getElement('.cn-write-note-no-editor').addClass('cn-hidden');
            this.domNode.getElement('.cn-write-note').removeClass('cn-hidden');
            this.userInterfaceHidden = false;
            this.eventEmitter.emit('userInterfaceShown');
        }
    },
    
    /**
     * Starts the autosave draft job.
     */
    startAutosaveJob: function() {
        if (this.autosaveHandler) {
            // TODO necessary to set dirty flag here?
            this.dirty = false;
            this.autosaveHandler.startAutomaticSave();
        }
    },

    /**
     * Disables the send button and shows an AJAX loading overlay
     */
    startPublishNoteFeedback: function() {
        // disable send button
        this.getSendButtonElement().disabled = true;
        this.ajaxLoadingOverlay.setStyle('display', '');
    },

    /**
     * Stops the autosave job.
     */
    stopAutosaveJob: function() {
        // TODO necessary to set dirty flag here?
        this.dirty = false;
        if (this.autosaveHandler) {
            this.autosaveHandler.stopAutomaticSave();
        }
    },

    /**
     * Re-enables the send button and hides the AJAX loading overlay
     */
    stopPublishNoteFeedback: function() {
        this.ajaxLoadingOverlay.setStyle('display', 'none');
        this.getSendButtonElement().disabled = false;
    },

    /**
     * Method that is invoked when the target topic was added, removed or replaced.
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
     * Called when the title of the target topic changed.
     */
    targetTopicTitleChanged: function(topicData) {
        this.eventEmitter.emit('targetTopicTitleChanged', topicData.title);
    }
});
