var CreateNoteWidget = new Class({
    Extends: C_Widget,

    widgetGroup: "blog",

    // the current working mode of the widget (edit, comment or create)
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
    // whether the editor is currently dirty. Will be reset after a successful autosave.
    dirty: false,
    // the NoteTextEditor instance
    editor: null,
    eventEmitter: null,
    /**
     * JSON object holding initial note data which will be restored when resetting the note. This
     * data is provided with the response metadata when refreshing the widget. In the edit case this
     * will be the note to edit. Additionally the content can be provided with a static parameter to
     * overwrite the content of the initialNote. In case there is an autosave it will override the
     * initial note when rendering the content.
     */
    initialNote: null,
    /**
     * A topic extracted from settings during initialization, can be null. Will only be set when in
     * create mode and is used when resetting the editor.
     */
    initialTargetTopic: null,
    /**
     * whether a direct message should be created
     */
    isDirectMessage: false,
    // if the note was modified in some way. Will be reset with clearAll after publishing or reverting
    modified: false,
    noTargetTopicChangeIfModifiedOrAutosaved: false,
    // an array of note property objects to be included when sending the note
    noteProperties: null,
    parentPostId: null,
    placeholders: null,
    // css classes to be applied to the create note container (getWriteContainerElement)
    // when the editor only supports plain text
    plainTextEditorCssClass: null,
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

    topicAutocompleter: null,
    topics: null,
    // note properties to pass along with every note. Can be set with the same-named static parameter.
    predefinedNoteProperties: null,

    setup: function() {
        // need to remember the order of the added topics to easily get the first crosspost topic
        this.topics = {
            ids: [],
            items: []
        };
    },

    init: function() {
        var action, targetBlogId, targetBlogTitle, parentPostId, autosaveDisabled;
        var cancelBehavior, publishSuccessBehavior;
        this.parent();
        this.eventEmitter = new communote.classes.EventEmitter();
        this.noTargetTopicChangeIfModifiedOrAutosaved = !!this
                .getStaticParameter('noTargetTopicChangeIfModifiedOrAutosaved');
        this.blogUtils = communote.utils.topicUtils;
        action = this.getStaticParameter('action') || 'create';
        this.setFilterParameter('action', action);
        this.action = action;

        if (action == 'create') {
            targetBlogId = this.getStaticParameter('targetBlogId');
            targetBlogTitle = this.getStaticParameter('targetBlogTitle');
            this.setInitialTargetTopic(targetBlogId, targetBlogTitle);
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
            this.setFilterParameter('plaintextOnly', true);
        }
        
        this.copyStaticParameter('repostNoteId');
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
        this.addEventListener('directMessageModeChanged', this.onDirectMessageModeChanged, this);
    },
    
    addEventListener: function(eventName, fn, context) {
        this.eventEmitter.on(eventName, fn, context);
    },
    
    /**
     * Add the items from items that are not yet in the dataHolder. Will set the dirty flag if
     * something changed.
     *
     * @param {Object} dataHolder the dataHolder to be extended
     * @param {Array|Object} items an array of JSON objects or single JSON object to evaluate
     * @param {String} idString the name of the attribute of the item object of which the value is
     *            used as key in the dataHolder
     * @param {Function} itemAddedCallback The function to call for each added item
     * @return array with IDs of the items that were added. Can be empty.
     */
    addNewItems: function(dataHolder, items, idString, itemAddedCallback) {
        var itemsToAdd, idsToAdd, length, i, id;
        if (!items) {
            return [];
        }
        itemsToAdd = [];
        idsToAdd = [];
        // first collect new items to be able to add them one by one and inform whether there are more to come
        if (typeOf(items) == 'array') {
            length = items.length;
            for (i = 0; i < length; i++) {
                id = items[i][idString];
                if (!dataHolder.ids.contains(id)) {
                    itemsToAdd.push(items[i]);
                    idsToAdd.push(id);
                }
            }
        } else {
            id = items[idString];
            if (!dataHolder.ids.contains(id)) {
                itemsToAdd.push(items);
                idsToAdd.push(id);
            }
        }
        length = itemsToAdd.length;
        for (i = 0; i < length; i++) {
            dataHolder.ids.push(idsToAdd[i]);
            dataHolder.items.push(itemsToAdd[i]);
            itemAddedCallback.call(this, itemsToAdd[i], i < length - 1);
        }
        if (length > 0) {
            this.dirty = true;
            this.modified = true;
        }
        return idsToAdd;
    },

    addTopics: function(topics) {
        var targetTopicId, ids, newTargetTopic, i;
        if (!topics) {
            return;
        }
        targetTopicId = this.getTargetTopicId();
        topics = Array.from(topics);
        if (targetTopicId == undefined) {
            newTargetTopic = topics.shift();
            this.addNewItems(this.topics, newTargetTopic, 'id', this.topicAdded);
            this.targetTopicChanged();
        }
        if (this.isDirectMessage) {
            // TODO show an error message that adding crosspost blogs is not allowed in DM mode
            return false;
        }
        // assert that all topics have an alias since it is required for crossposting
        for (i = 0; i < topics.length; i++) {
            if (!topics[i].alias) {
                throw 'Alias of crosspost topic is missing';
            }
        }
        ids = this.addNewItems(this.topics, topics, 'id', this.topicAdded);
        return (ids.length > 0);
    },

    /**
     * @override
     */
    beforeRemove: function() {
        this.stopAutosaveJob();
        // TODO do autosave?
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
        var resetOperation, hasOnlineAutosave;
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
                resetOperation = function() {
                    this.stopAutosaveJob();
                    postRemoveOperation();
                }.bind(this);
                if (behavior.confirmReset) {
                    showConfirmDialog(getJSMessage('create.note.dialog.discardChanges.title'),
                            getJSMessage('create.note.dialog.discardChanges.question'),
                            resetOperation, {
                                triggeringEvent: event
                            });
                } else {
                    resetOperation();
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
        if (this.action == 'create'
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
        // remove attached autocompleter
        if (this.topicAutocompleter) {
            this.topicAutocompleter.destroy();
        }
        if (this.placeholders) {
            this.placeholders.destroy();
        }
    },

    clearAll: function() {
        var elem;
        if (this.action == 'create') {
            this.resetTargetTopic();
        } else {
            this.removeTopic(null);
        }
        this.editor.resetContent(null);
        // empty inputs
        elem = this.getTopicSearchElement();
        if (elem) {
            elem.value = '';
        }
        this.dirty = false;
        this.modified = false;
        this.noteProperties = null;
    },

    /**
     * Called after the content of the widget has been initialized after a refresh.
     *
     * @param {boolean} fromAutosave If true, the widget was initialized with an autosave
     */
    contentInitialized: function(fromAutosave) {

    },

    /**
     * Create the NoteTextEditor to be used and return it.
     *
     * @return {NoteTextEditor} the editor
     */
    createEditor: function() {
        // create the dummy editor
        return new NoteTextEditor();
    },

    /**
     * Creates an object that holds all the information to be submitted when sending the note.
     *
     * @param {boolean} publish whether the note will be published or stored as an autosave
     * @param {String} content the content of the note
     * @return {Object} an object with all information for storing the note
     */
    // TODO rename getNoteDataForPost
    createPostData: function(publish, content) {
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
        data.crossPostTopicAliases = this.getCrosspostTopics(true);
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
        // check for autosave and if available replace initObject with it to init with autosave
        if (this.autosaveHandler) {
            autosave = this.autosaveHandler.load(responseMetadata);
            if (autosave) {
                autosaveLoaded = true;
                initObject = autosave.noteData;
            }
        }
        // force the target topic of the parent or edited note, or in case of create of the autosave
        if (this.action != 'create' || !autosaveLoaded
                || (this.initialTargetTopic && !this.noTargetTopicChangeIfModifiedOrAutosaved)) {
            initObject.targetBlog = targetTopic;
        }
        return {
            initObject: initObject,
            initialNote: initialNote,
            isAutosave: autosaveLoaded
        };
    },

    getCrosspostTopicsCount: function() {
        var count = this.topics.ids.length;
        if (count > 0) {
            // first topic is the target topic
            count--;
        }
        return count;
    },

    getCrosspostTopics: function(aliases) {
        var i, topics, result;
        // first topic is the target topic
        if (!aliases) {
            return this.topics.items.slice(1);
        }
        topics = this.topics.items;
        result = [];
        for (i = 1; i < topics.length; i++) {
            result.push(topics[i].alias);
        }
        return result;
    },

    getListeningEvents: function() {
        return [];
    },
    
    getNoteData: function(resetDirtyFlag) {
        var data = {};
        data.crosspostTopics = this.getCrosspostTopics(false);
        data.content = this.editor.getContent();
        data.targetTopic = this.topics.items[0];
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
        return this.topics.ids[0];
    },

    /**
     * Returns the element to be used as positionSource element for the topic autocompleter.
     *
     * @return {Element} the element or null if not required
     */
    getTopicAutocompleterPositionSource: function() {
        return null;
    },
    /**
     * Returns the topic search element.
     *
     * @return {Element} the element if it exists
     */
    getTopicSearchElement: function() {
        return this.domNode.getElementById(this.widgetId + '-topic-search');
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
     * This method looks for a FORM element by default.
     */
    getWriteContainerElement: function() {
        return this.domNode.getElement('form');
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

    initAutosaveHandler: function() {
        var action, noteId, options, autosaveTimeout;
        if (this.autosaveDisabled) {
            return;
        }
        action = this.action;
        if (this.getFilterParameter('repostNoteId')) {
            action = 'repost';
        }
        if (action == 'edit') {
            noteId = this.getFilterParameter('noteId');
        } else if (action == 'comment') {
            noteId = this.parentPostId;
        } else if (action == 'repost') {
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
        var topics;
        this.components.initContent(note);
        // TODO hack while migrating to components
        if (!note) {
            return;
        }
        this.editor.resetContent(note.content);
        // no crosspost topics when targetBlog is not set or replying
        if (note.targetBlog) {
            topics = [ note.targetBlog ];
            if (this.action != 'comment' && note.crosspostBlogs) {
                topics.append(note.crosspostBlogs);
            }
            this.addTopics(topics);
        }
        if (note.properties) {
            this.noteProperties = note.properties;
        }
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
                // if the topic is currently selected notify subclasses
                if (this.topics.ids.length > 0 && this.topics.items[0] == this.initialTargetTopic) {
                    this.selectedTopicTitleChanged(this.initialTargetTopic);
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
            // inform widgets and also provide the action
            if (noteId) {
                noteChangedDescr = {
                    action: this.action,
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
                this.clearAll();
                // TODO clearAll shouldn't be necessary when correctly implementing initContent
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
     * Prepares the options for an autocompleter.
     *
     * @param {String} positionSourceGetter Name of a local getter function that returns the
     *            position source element for the autocompleter
     * @param {Boolean} emptyOnSelection True if the autocompleter should clear the input when a
     *            suggestion was selected
     * @param {Boolean} unfocusOnSelection True if the autocompleter should remove the focus from
     *            the input when a suggestion was selected
     * @param {Function} beforeRequestCallback A callback function to be passed to the DataSource
     * @returns {Object} the prepared options object
     */
    prepareAutocompleterOptions: function(positionSourceGetter, emptyOnSelection,
            unfocusOnSelection, beforeRequestCallback) {
        var positionSource;
        var preparedOptions = {};
        preparedOptions.autocompleterOptions = {};
        if (positionSourceGetter) {
            positionSource = this[positionSourceGetter]();
            if (positionSource) {
                preparedOptions.inputFieldOptions = {
                    positionSource: positionSource
                };
            }
        }
        if (emptyOnSelection) {
            preparedOptions.autocompleterOptions.clearInputOnSelection = true;
        }
        if (beforeRequestCallback) {
            preparedOptions.dataSourceOptions = {};
            preparedOptions.dataSourceOptions.beforeRequestCallback = beforeRequestCallback;
        }
        preparedOptions.autocompleterOptions.unfocusInputOnSelection = unfocusOnSelection;
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
        var initData = this.extractInitData(responseMetadata);
        this.initialNote = initData.initialNote;

        // TODO better name
        this.refreshView(initData.isAutosave);
        this.refreshEditor();
        this.eventEmitter.emit('widgetRefreshed');

        // attach autocompleters
        if (this.useTopicSelection()) {
            this.refreshTopicSelection(this.getTopicSearchElement());
        }
        
        this.initContent(initData.initObject);
        // TODO rename to contentInitializedAfterRefresh to make clear it is not called with every initContent call?
        this.contentInitialized(initData.isAutosave);
        // TODO maybe let autosaveHandler.editorInitialized start job 
        if (this.autosaveHandler) {
            this.autosaveHandler.editorInitialized();
            this.startAutosaveJob();
        }
        init_tips(this.domNode);
    },

    refreshEditor: function() {
        var writeContainerElem, classToAdd, classToRemove;
        // apply some CSS classes before refreshing the actual editor component as it might
        // work on the resulting styles (like expanding textarea)
        writeContainerElem = this.getWriteContainerElement();
        if (this.editor.supportsHtml()) {
            classToAdd = this.richTextEditorCssClass;
            classToRemove = this.plainTextEditorCssClass;
        } else {
            classToAdd = this.plainTextEditorCssClass;
            classToRemove = this.richTextEditorCssClass;
        }
        if (classToAdd) {
            writeContainerElem.addClass(classToAdd);
        }
        if (classToRemove) {
            writeContainerElem.removeClass(classToRemove);
        }

        this.editor.refresh(writeContainerElem);
    },

    /**
     * Called after refresh if useTopicSelection returned true. Default implementation attaches a
     * topic autocompleter to the input field.
     *
     * @param {Element} searchElement The input element to attach the autocompleter to
     */
    refreshTopicSelection: function(searchElement) {
        var acOptions;
        if (searchElement) {
            acOptions = this.prepareAutocompleterOptions('getTopicAutocompleterPositionSource',
                    true, false);
            this.topicAutocompleter = autocompleterFactory.createTopicAutocompleter(searchElement,
                    acOptions, null, false, 'write');
            this.topicAutocompleter.addEvent('onChoiceSelected', this.topicChoiceSelected
                    .bind(this));
        }
    },

    refreshView: function(autosaveLoaded) {
        this.ajaxLoadingOverlay = this.widgetController.createAjaxLoadingOverlay(this.domNode,
                false);
        this.placeholders = communote.utils.attachPlaceholders(null, this.domNode);
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

    /**
     * Removes the item from the data holder
     *
     * @param {Object} dataHolder The data holder to update
     * @param {String} id identifier of the item to remove, if null remove all items
     * @return {boolean} whether the dataHolder was changed
     */
    removeItemFromDataHolder: function(dataHolder, id) {
        var idx;
        var changed = false;
        if (id != null) {
            idx = dataHolder.ids.indexOf(id);
            if (idx > -1) {
                dataHolder.ids.splice(idx, 1);
                dataHolder.items.splice(idx, 1);
                changed = true;
            }
        } else {
            // clear if it is not empty yet
            if (dataHolder.ids.length) {
                dataHolder.ids = [];
                dataHolder.items = [];
                changed = true;
            }
        }
        if (changed) {
            this.dirty = true;
            this.modified = true;
        }
        return changed;
    },

    removeTopic: function(topicData) {
        var crosspostTopics, i, oldTargetTopicId, oldDirty;
        var id = topicData && topicData.id;
        var createAction = (this.action == 'create');
        if (id != undefined) {
            // assert the first topic (target topic) is not removed when not in create mode
            if (!createAction && this.getTargetTopicId() == id) {
                return;
            }
        } else if (!createAction) {
            // when not in create mode only remove the crosspost topics
            crosspostTopics = this.getCrosspostTopics(false);
            for (i = 0; i < crosspostTopics.length; i++) {
                topicData = crosspostTopics[i];
                this.removeItemFromDataHolder(this.topics, topicData.id);
                this.topicRemoved(topicData);
            }
            return;
        }
        oldTargetTopicId = this.getTargetTopicId();
        oldDirty = this.dirty;
        if (this.removeItemFromDataHolder(this.topics, id)) {
            // do not create an autosave if no topic is selected
            if (createAction && this.topics.items.length == 0) {
                this.dirty = oldDirty;
            }
            this.topicRemoved(topicData);
            if (oldTargetTopicId != this.getTargetTopicId()) {
                this.targetTopicChanged();
            }
        }
    },

    renderStyleChanged: function(oldStyle, newStyle) {
    },

    /**
     * Helper which replaces the target topic in 'create' mode with the topic returned by
     * getTargetTopicForCreate. In contrast to calling removeTopic(null) and addTopics(newTopic)
     * this method will invoke targetTopicChanged only once, which is more suitable and performant
     * when the targetTopicChanged needs to update the view. Also the dirty and modified flags won't
     * change.
     */
    resetTargetTopic: function() {
        var oldDirty, oldModified;
        if (this.action == 'create') {
            // remember old flags and restore them
            oldDirty = this.dirty;
            oldModified = this.modified;
            // remove all
            this.removeItemFromDataHolder(this.topics, null);
            this.topicRemoved(null);
            this.addNewItems(this.topics, this.getTargetTopicForCreate(), 'id', this.topicAdded);
            this.dirty = oldDirty;
            this.modified = oldModified;
            this.targetTopicChanged();
        }
    },

    /**
     * Reset the editor to the initialNote. Should only be called if there is no autosave or the
     * autosave was removed.
     */
    resetToNoAutosaveState: function() {
        this.clearAll();
//        if (this.initialNote && Object.getLength(this.initialNote)) {
            // restore original note
            this.initContent(this.initialNote);
//        }
        this.startAutosaveJob();
    },

    /**
     * Called when the title of a topic in the topics member is changed. Default implementation does
     * nothing.
     */
    selectedTopicTitleChanged: function(topicItem) {
    },

    /**
     * Send the note.
     * @param {String} content The text/HTML content of the note
     * @param {boolean} [ignoreUncommittedOptions] if false a popup will warn the user when he
     *            publishes a note and there are uncommitted inputs in the blog and user input
     *            fields. If true there will be no warning.
     */
    sendNote: function(content, ignoreUncommittedOptions) {
        var warningMsg, topicInput, uncommittedBlog;
        var cancelFunction, buttons, successCallback, errorCallback, data, options;
        if (!ignoreUncommittedOptions) {
            warningMsg = this.components.getUnconfirmedInputWarning();
            if (!warningMsg) {
                topicInput = this.getTopicSearchElement();
                uncommittedBlog = topicInput && topicInput.value.trim().length;
                if (uncommittedBlog) {
                    warningMsg = getJSMessage('blogpost.create.submit.confirm.unsaved.blog');
                }
            }
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
        
        data = this.createPostData(true, content);
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

    /**
     * AutocompleterRequestDataSource callback implementation that sets the current topic to be
     * ignored before sending the request.
     */
    setIgnoreCurrentTopicBeforeRequestCallback: function(request, postData, queryParam) {
        postData['blogIdsToExclude'] = this.getTargetTopicId();
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
     * Method that is invoked when the target topic was added, removed or replaced. This method is
     * called after topicAdded and topicRemoved.
     */
    targetTopicChanged: function() {
    },

    /**
     * Method that is invoked when a topic was added.
     *
     * @param {Object} topicData The object describing the topic that was added.
     * @param {boolean} moreToCome Whether this add is only the first of a sequence of adds.
     */
    topicAdded: function(topicData, moreToCome) {
    },

    topicChoiceSelected: function(inputElem, choiceElem, token, value) {
        this.addTopics(token);
    },

    /**
     * Method that is invoked when one or more topics were removed.
     *
     * @param {Object} topicData The object describing the topic that was removed. Will be null if
     *            all topics were removed.
     */
    topicRemoved: function(topicData) {
    },

    useTopicSelection: function() {
        // topics can only be selected when creating notes
        return this.action == 'create';
    }
});
