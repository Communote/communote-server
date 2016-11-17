var CreateNoteWidget = new Class({
    Extends: C_Widget,

    widgetGroup: "blog",

    // the current working mode of the widget (edit, comment or create)
    action: null,
    addedTags: null,
    ajaxLoadingOverlay: null,
    /**
     * Object that maps the source render style to the allowed target render styles (array of
     * styles). If null all transitions are allowed.
     */
    allowedRenderStyleTransitions: null,
    attachments: null,
    autosaveCookie: null,
    // whether an autosave was created
    autosaveCreated: false,
    autosaveDisabled: false,
    // whether an autosave was loaded
    autosaveLoaded: false,
    autosaveJobActiveTypes: [],
    autosaveJobId: null,
    autosaveNoteId: null,
    autosaveRenderDiscardLink: false,
    /**
     * Default timer interval for auto-saves.
     */
    autosaveTimerPeriod: 10000,
    autosaveVersion: 0,
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
    communoteLocalStorage: new CommunoteLocalStorage(),
    defaultTagStoreAlias: 'DefaultNoteTagStore',
    // whether the editor is currently dirty. Will be reset after a successful autosave.
    dirty: false,
    // the NoteTextEditor instance
    editor: null,
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

    resendNotificationProperty: {},

    // css classes to be applied to the create note container (getWriteContainerElement)
    // when the editor only supports richtext
    richTextEditorCssClass: null,
    runningAutosaveRequest: false,
    // defines whether tags that were just typed into the tag input should be stored when sending
    storeUncommittedTags: true,
    /**
     * Supported render styles of the widget. The first is the default.
     */
    supportedRenderStyles: [ 'full', 'minimal', 'simulate' ],
    tagAutocompleter: null,
    /**
     * @type {Object[]} categories of the tag autocompleter, can provided with a static parameter of
     *       same name. If not defined a default category definition will be used
     */
    tagAutocompleterCategories: null,
    // whether the autocompleter should work in multiple mode
    tagAutocompleterMultipleMode: true,
    // RegEx to split the tags in the tag input field
    tagStringSplitRegEx: /\s*,\s*/,
    topicAutocompleter: null,
    topics: null,
    userAutocompleter: null,
    usersToNotify: null,
    // note properties to pass along with every note. Can be set with the same-named static parameter.
    predefinedNoteProperties: null,

    setup: function() {
        this.discardAutosaveUrl = buildRequestUrl('/blog/deletePost.do')
                + '?action=deleteAutosave&noteId=';

        // added items defined by an ordered array of ids (which might be aliases or sth) and the
        // items in the same order
        this.usersToNotify = {
            ids: [],
            items: []
        };
        this.attachments = {
            ids: [],
            items: []
        };
        // need to remember the order of the added topics to easily get the first crosspost topic
        this.topics = {
            ids: [],
            items: []
        };
        this.addedTags = [];
    },

    init: function() {
        var action, targetBlogId, targetBlogTitle, parentPostId, autosaveDisabled, timerPeriod;
        var cancelBehavior, publishSuccessBehavior, tagAutocompleterCategories;
        this.parent();
        this.autosaveRenderDiscardLink = !!this.getStaticParameter('autosaveRenderDiscardLink');
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
        timerPeriod = this.getStaticParameter('draftTimer');
        if (timerPeriod != null) {
            this.autosaveTimerPeriod = timerPeriod * 1000;
        }
        tagAutocompleterCategories = this.getStaticParameter('tagAutocompleterCategories');
        if (tagAutocompleterCategories) {
            // TODO will be in serialized JSON format for create case -> should be changed when switching from comment based params
            if (typeOf(tagAutocompleterCategories) === 'string') {
                tagAutocompleterCategories = JSON.decode(tagAutocompleterCategories);
            }
            this.tagAutocompleterCategories = tagAutocompleterCategories;
        } else {
            // create default category definition which uses the default note tagstore
            this.tagAutocompleterCategories = [ {
                'id': this.defaultTagStoreAlias,
                'provider': this.defaultTagStoreAlias,
                'title': ''
            } ];
        }
        this.predefinedNoteProperties = this.getStaticParameter('predefinedNoteProperties');

        this.initAutosaveCookie();
    },

    /**
     * Adds one or more attachments described by attachment data object.
     *
     * @param {Object|Object[]} attachmentData A description of the attachment including the member
     *            'id', 'fileName', 'mime'.
     */
    addAttachments: function(attachmentData) {
        this.addNewItems(this.attachments, attachmentData, 'id', this.attachmentAdded);
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

    /**
     * Adds a tag to the addedTags array if it is not yet contained. In case the tag was added
     * tagAdded will be called and the dirty flag will be set.
     *
     * @param {String|Object} tag The tag to add, can be a simple string tag or a structured tag
     *            object
     */
    addTag: function(tag) {
        // if the tag is a string build the minimal tag object
        tag = this.convertToTagObject(tag);
        if (this.internalAddTag(tag, this.addedTags)) {
            this.dirty = true;
            this.modified = true;
            this.tagAdded(tag, false);
        }
    },
    /**
     * Adds a bunch of tags to the addedTags array. Only tags that are not yet contained will be
     * added. For each added tag the tagAdded will be called. If any tag was added the dirty flag
     * will be set.
     *
     * @param {String[]|Object[]} tags The tags to add, the array can contain simple string tags or
     *            structured tag objects
     */
    addTags: function(tags) {
        var i, addedTags, tag, lastIndex;
        if (!tags) {
            return;
        }
        addedTags = [];
        for (i = 0; i < tags.length; i++) {
            // if the tag is a string build the minimal tag object
            tag = this.convertToTagObject(tags[i]);
            if (this.internalAddTag(tag, this.addedTags)) {
                addedTags.push(tag);
            }
        }
        // inform FE that tags were added
        lastIndex = addedTags.length - 1;
        if (lastIndex > -1) {
            for (i = 0; i <= lastIndex; i++) {
                this.tagAdded(addedTags[i], i < lastIndex);
            }
            this.dirty = true;
            this.modified = true;
        }
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
     * Adds users for notification.
     *
     * @param {Object|Object[]) users An object containing user data or an array of such objects.
     * @return whether something was added
     */
    addUsers: function(users) {
        var aliases = this.addNewItems(this.usersToNotify, users, 'alias', this.userAdded);
        return (aliases.length > 0);
    },

    /**
     * Called after an attachment was added. Subclasses can use this hook to update the view.
     *
     * @param {Object} attachmentData JSON object describing the attached attachment
     * @param {boolean} moreToCome true if the attachment was added as part of batch update and the
     *            current item is not the last. Can be used to optimize view updates.
     */
    attachmentAdded: function(attachmentData, moreToCome) {
    },

    /**
     * Called after an attachment was removed. Subclasses can use this hook to update the view.
     *
     * @param {String} id identifier of the attached attachment. This parameter is null if all
     *            attachments were removed.
     */
    attachmentRemoved: function(id) {
    },

    attachmentUploadDone: function(uploadId, jsonResponse) {
        // check for error message
        if (jsonResponse.status == 'ERROR') {
            this.attachmentUploadFailed(uploadId, jsonResponse.message);
        } else {
            this.attachmentUploadSucceeded(uploadId, jsonResponse.result);
        }
    },

    attachmentUploadFailed: function(uploadId, errorMessage) {
        // enable send button
        this.getSendButtonElement().removeProperty('disabled');
        if (errorMessage) {
            // hide notifications
            hideNotification();
            // show error occurred
            showNotification(NOTIFICATION_BOX_TYPES.error, '', errorMessage, {
                duration: ''
            });
        }
    },

    attachmentUploadStarted: function(uploadDescriptor) {
        // disable send button
        this.getSendButtonElement().disabled = 'disabled';
    },

    attachmentUploadSucceeded: function(uploadId, attachmentData) {
        // enable send button
        this.getSendButtonElement().removeProperty('disabled');
        attachmentData.uploadId = uploadId;
        this.addAttachments(attachmentData);
    },

    /**
     * Is called periodically to do an autosave of the note if something changed.
     */
    autosaveJob: function() {
        var content, skipOnlineSave;

        if (this.dirty || this.editor.isDirty()) {
            this.updateAutosaveMarker(getJSMessage('blogpost.autosave.saving'), false);
            content = this.editor.getContent();
            this.dirty = false;
            // this.autosaveJobActiveTypes.empty();
            // skip online autosaves if still running
            skipOnlineSave = this.runningAutosaveRequest;
            this.autosaveNoteOffline(content, skipOnlineSave);
            if (!skipOnlineSave) {
                this.autosaveNoteOnline(content);
            }
        }
    },

    autosaveNoteOffline: function(content, skipOnlineAutosave) {
        this.autosaveJobActiveTypes.erase('offline');
        if (this.autosaveCookie == null) {
            return;
        }
        this.autosaveCookie.empty();
        this.storeInAutosaveCookie(content);
        // increase version if previous online autosave is still running and current online
        // autosave is skipped -> offline autosave will be newer than previous online autosave
        if (skipOnlineAutosave) {
            this.autosaveCookie.set('autosaveVersion', this.autosaveVersion + 1);
        } else {
            this.autosaveCookie.set('autosaveVersion', this.autosaveVersion);
        }
        // fails if too large (>4kb)
        if (this.autosaveCookie.save()) {
            this.autosavingDone(false);
        }
    },

    autosaveNoteOnline: function(content) {
        this.autosaveJobActiveTypes.erase('online');
        // won't succeed if empty or no topic is selected
        if (content == null || (content.length == 0)
                || (this.action == 'create' && this.getTargetTopicId() == null)) {
            return;
        }
        this.sendNote(false, content);
    },

    autosaveNoteOnlineComplete: function(response) {
        this.runningAutosaveRequest = false;
        if (response && response.result) {
            this.autosavingDone(true);
            // TODO api returns only noteId and no details like version, can only blindly
            // increment autosave version
            // using REST API 3.0 whose result object is the ID of the autosave
            this.autosaveNoteId = response.result;
            this.autosaveVersion++;
        }
    },

    /**
     * Is called after an autosave has been created.
     *
     * @param {boolean} online true if it the autosave was created online, false if it was stored
     *            offline in a cookie
     */
    autosavingDone: function(online) {
        this.autosaveJobActiveTypes.include(online ? 'online' : 'offline');
        this.updateAutosaveMarker(getJSMessage('blogpost.autosave.saved') + " "
                + localizedDateFormatter.format(new Date()), this.autosaveRenderDiscardLink);
        this.autosaveCreated = true;
    },

    /**
     * @override
     */
    beforeRemove: function() {
        this.stopAutosaveJob();
        // TODO do autosave?
        this.cleanup();
    },

    /**
     * Cancel the edit, update or create operation. The actual behavior of this function is defined
     * by the configurable cancelBehavior.
     *
     * @param Event [event] The event that triggered cancel.
     */
    cancel: function(event) {
        var remove, changeRenderStyle, hasAutosave, modified, postRemoveOperation;
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
            postRemoveOperation = function() {
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
                    this.discardAutosaveWithConfirm(event, postRemoveOperation);
                } else {
                    this.discardAutosave(postRemoveOperation);
                }
            } else {
                // only modified, but can call the same operations
                if (behavior.confirmReset) {
                    showConfirmDialog(getJSMessage('create.note.dialog.discardChanges.title'),
                            getJSMessage('create.note.dialog.discardChanges.question'),
                            this.discardAutosave.bind(this, postRemoveOperation), {
                                triggeringEvent: event
                            });
                } else {
                    this.discardAutosave(postRemoveOperation);
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

    changeDirectMessageMode: function(activate) {
        if (this.isDirectMessage === activate) {
            return;
        }
        if (activate) {
            // activate only possible if crosspost blogs are empty and at least one user was
            // added
            // TODO show an error if not activatable?
            if (this.getCrosspostTopicsCount() == 0 && this.usersToNotify.ids.length > 0) {
                this.isDirectMessage = true;
            }
        } else {
            this.isDirectMessage = false;
        }
        if (this.isDirectMessage === activate) {
            this.directMessageModeChanged();
        }
    },

    /**
     * Defines if already notified user will receive notifcations again
     */
    changeResendNotificationMode: function(activate) {
      this.resendNotificationProperty = {
        "key": "editNote.resendNotification",
        "keyGroup": "com.communote",
        "value": activate
      };
      communoteLocalStorage.setItem("com.communote.editNote.resendNotification", activate);
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
        if (this.userAutocompleter) {
            this.userAutocompleter.destroy();
        }
        if (this.topicAutocompleter) {
            this.topicAutocompleter.destroy();
        }
        if (this.tagAutocompleter) {
            this.tagAutocompleter.destroy();
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
        this.removeTag(null);
        this.removeAttachment(null);
        this.removeUser(null);
        this.editor.resetContent(null);
        this.autosaveNoteId = null;
        this.autosaveVersion = 0;
        this.updateAutosaveMarker(null);
        this.changeDirectMessageMode(false);
        // empty inputs
        elem = this.getTagSearchElement();
        if (elem) {
            elem.value = '';
        }
        elem = this.getTopicSearchElement();
        if (elem) {
            elem.value = '';
        }
        elem = this.getUserSearchElement();
        if (elem) {
            elem.value = '';
        }
        this.dirty = false;
        this.modified = false;
        this.autosaveLoaded = false;
        this.autosaveCreated = false;
        this.noteProperties = null;
    },

    /**
     * Called after the content of the widget has been initialized after a refresh.
     *
     * @param {boolean} fromOnlineAutosave If true, there was an autosave that was loaded from the
     *            server
     * @param {boolean} fromOfflineAutosave If true, there was an autosave that was loaded from the
     *            cookie
     */
    contentInitialized: function(fromOnlineAutosave, fromOfflineAutosave) {
        if (fromOfflineAutosave || fromOnlineAutosave) {
            this.autosaveLoaded = true;
            this.updateAutosaveMarker(getJSMessage('blogpost.autosave.loaded'),
                    this.autosaveRenderDiscardLink);
        }
    },

    convertAutosaveCookieToJson: function() {
        var blogs, tags, autosaveNoteId, i;
        var note = {};
        note.content = this.autosaveCookie.get('content');
        note.attachments = this.autosaveCookie.get('attachs');
        note.usersToNotify = this.autosaveCookie.get('users');
        note.targetBlog = this.autosaveCookie.get('targetBlog');
        note.properties = this.autosaveCookie.get('properties');
        blogs = this.autosaveCookie.get('blogs');
        if (blogs) {
            // TODO remove this compatibility code in a future release (port 3.0)
            // convert old nameId member to alias
            for (i = 0; i < blogs.length; i++) {
                if (blogs[i].nameId) {
                    blogs[i].alias = blogs[i].nameId;
                    delete blogs[i].nameId;
                }
            }
            note.crosspostBlogs = blogs;
        }
        tags = this.autosaveCookie.get('tags');
        // old tags where a string, ignore them
        if (typeOf(tags) === 'array') {
            note.tags = tags;
        }
        note.isAutosave = true;
        // decrement version because init code increments it again and it should not grow when doing
        // an offline autosave because online autosave should be treated as newer
        note.autosaveVersion = this.autosaveCookie.get('autosaveVersion').toInt() - 1;
        autosaveNoteId = this.autosaveCookie.get('autosaveNoteId');
        if (autosaveNoteId) {
            note.autosaveNoteId = autosaveNoteId.toInt();
        }
        return note;
    },

    /**
     * Convert a tag string to a valid tag object containing the minimum required members if it is
     * not already one.
     *
     * @param {String|Object} tag The tag to process
     */
    convertToTagObject: function(tag) {
        if (typeof (tag) === 'string') {
            tag = {
                defaultName: tag
            };
        }
        return tag;
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
    createPostData: function(publish, content) {
        var i, name, properties;
        var data = {};
        data.text = content;
        var writeContainerElem = this.getWriteContainerElement();
        var hiddenInputs = writeContainerElem.getElements('input[type=hidden]');
        for (i = 0; i < hiddenInputs.length; i++) {
            name = hiddenInputs[i].getProperty('name');
            if (name != 'topicId') {
                data[name] = hiddenInputs[i].value;
            }
        }
        data.isHtml = this.editor.supportsHtml();
        data.topicId = this.getTargetTopicId();
        data.noteId = this.getFilterParameter('noteId');
        if (this.action != 'edit') {
            data.parentNoteId = this.parentPostId;
            data.isDirectMessage = this.isDirectMessage;
        }
        data.tags = this.createTagsPostData(publish);
        data.usersToNotify = this.usersToNotify.ids;
        data.crossPostTopicAliases = this.getCrosspostTopics(true);
        data.attachmentIds = this.attachments.ids;
        data.publish = publish === true ? true : false;
        data.autosaveNoteId = this.autosaveNoteId;
        data.noteVersion = this.autosaveVersion;

        if(this.action === 'edit') {
          if (this.noteProperties === null) {
            this.noteProperties = [];
          }
          this.noteProperties.push(this.resendNotificationProperty);
        }

        properties = this.createPropertiesPostData(publish);
        if (properties) {
            data.properties = properties;
        }
        return data;
    },
    /**
     * @return {Object[]} array with all note properties or null if no properties were set
     */
    createPropertiesPostData: function(publish) {
        var properties;
        if (this.noteProperties) {
            properties = this.noteProperties.slice(0);
        } else {
            properties = [];
        }
        if (this.predefinedNoteProperties) {
            if (typeOf(this.predefinedNoteProperties) === 'array') {
                communote.utils.propertyUtils.mergeProperties(properties,
                        this.predefinedNoteProperties);
            } else {
                communote.utils.propertyUtils.mergeProperty(properties,
                        this.predefinedNoteProperties);
            }
        }
        if (properties.length == 0) {
            properties = null;
        }
        return properties;
    },

    /**
     * @returns {Array} an array of objects representing the tags to attach when saving the note
     */
    createTagsPostData: function(publish) {
        var finalTags, uncommittedTags, i;
        finalTags = this.addedTags.clone();
        // do not save tags from input when doing an autosave and the user is still
        // typing in the tag field, because we would add incomplete tags to the DB
        if (this.storeUncommittedTags
                && (publish || (this.tagAutocompleter && !this.tagAutocompleter
                        .isInputElementFocused()))) {
            uncommittedTags = this.extractTagsFromInput();
            for (i = 0; i < uncommittedTags.length; i++) {
                this.internalAddTag(uncommittedTags[i], finalTags);
            }
        }
        return finalTags;
    },

    /**
     * Called to delete the offline autosave cookie, for instance if the user discarded the autosave
     * or the note was published. Will even be called if there is no cookie. There won't be a cookie
     * if autosave feature or the cookies are disabled.
     */
    deleteAutosaveCookie: function() {
        if (this.autosaveCookie) {
            Cookie.dispose(this.autosaveCookie.key);
            this.autosaveCookie.empty();
        }
    },

    /**
     * Called by changeDirectMessageMode if the new mode could be set.
     */
    directMessageModeChanged: function() {
    },

    /**
     * Discard the autosave if there is one.
     *
     * @param {Function} [postRemoveOperation] A function to run after successfully removing the
     *            autosave. If null the editor will be reset. If no operation should be executed
     *            pass false.
     */
    discardAutosave: function(postRemoveOperation) {
        var url, request, i, utils;
        // default post remove operation: reset editor
        if (postRemoveOperation == null) {
            postRemoveOperation = this.resetToNoAutosaveState.bind(this);
        }
        if (this.autosaveDisabled) {
            if (postRemoveOperation) {
                postRemoveOperation.call();
            }
            return;
        }
        // TODO block if there are running uploads?
        this.stopAutosaveJob();
        this.deleteAutosaveCookie();
        if (this.autosaveNoteId != null) {
            url = this.discardAutosaveUrl + this.autosaveNoteId;
            request = new Request.JSON({
                url: url,
                method: 'get',
                noCache: true
            });
            request.addEvent('complete', this.discardAutosaveOnlineComplete.bind(this,
                    postRemoveOperation));
            request.send();
        } else {
            utils = noteUtils;
            for (i = 0; i < this.attachments.ids.length; i++) {
                // not really interested in the server response as the job
                // will clean-up the attachments that could not be deleted
                utils.deleteAttachment(this.attachments.ids[i]);
            }
            if (postRemoveOperation) {
                postRemoveOperation.call();
            }
        }
    },

    discardAutosaveOnlineComplete: function(postRemoveOperation, jsonObj) {
        if (jsonObj && jsonObj.status == 'ERROR') {
            this.updateAutosaveMarker(jsonObj.message, false);
        } else if (postRemoveOperation) {
            postRemoveOperation.call();
        }
    },

    discardAutosaveWithConfirm: function(event, postRemoveOperation) {
        var title = getJSMessage('create.note.autosave.discard.title');
        var message = getJSMessage('create.note.autosave.discard.question');
        showConfirmDialog(title, message, this.discardAutosave.bind(this, postRemoveOperation), {
            triggeringEvent: event
        });
    },

    /**
     * @returns {String[]} an array of tag strings the user typed into the tag input field. The
     *          array is empty there is no tag input or no tags were added
     */
    extractTagsFromInput: function() {
        var tagString, splitted, i;
        var tags = [];
        var field = this.getTagSearchElement();
        if (field) {
            tagString = field.value.trim();
            if (tagString.length > 0) {
                splitted = tagString.split(this.tagStringSplitRegEx);
                // remove blank
                for (i = 0; i < splitted.length; i++) {
                    if (splitted[i].length > 0) {
                        tags.push(this.convertToTagObject(splitted[i]));
                    }
                }
            }
        }
        return tags;
    },

    getAttachmentSearchElement: function() {
        return this.domNode.getElement('input[type=file]');
    },

    /**
     * Returns the element to be updated with autosave status data.
     */
    getAutosaveMarkerElement: function() {
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

    /**
     * Returns the index of a tag within the tagStore array.
     *
     * @param {Object} tag The tag to find, can be a simple unpersisted tag which just has the
     *            defaultName set or a persisted tag with tagId
     * @param {Object[]} tagStore Array that holds tags which can be persisted tags with tagId or
     *            tags that are not yet persisted
     * @returns {Number} the index of the tag or -1 if not contained
     */
    getIndexOfTag: function(tag, tagStore) {
        var i, index, simpleTag, addedTag;
        index = -1;
        // check if tag is just a string or a structured tag with tagId
        simpleTag = tag.tagId == undefined;
        for (i = 0; i < tagStore.length; i++) {
            addedTag = tagStore[i];
            if (simpleTag) {
                // compare by value: equal if default name matches the input and tagstore is not
                // set or the defaultTagStore
                if (!addedTag.tagStoreAlias || addedTag.tagStoreAlias === tag.tagStoreAlias) {
                    if (addedTag.defaultName === tag.defaultName) {
                        index = i;
                        break;
                    }
                }
            } else if (addedTag.tagId === tag.tagId) {
                index = i;
                break;
            }
        }
        return index;
    },

    getListeningEvents: function() {
        return [];
    },

    getSendButtonElement: function() {
        return this.domNode.getElementById(this.widgetId + '-send-button');
    },

    /**
     * Returns the element to be used as positionSource element for the tag autocompleter.
     *
     * @return {Element} the element or null if not required
     */
    getTagAutocompleterPositionSource: function() {
        return null;
    },
    /**
     * Returns the tag search element.
     *
     * @return {Element} the element if it exists
     */
    getTagSearchElement: function() {
        return this.domNode.getElementById(this.widgetId + '-tag-search');
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
     * Returns the element to be used as positionSource element for the user autocompleter.
     *
     * @return {Element} the element or null if not required
     */
    getUserAutocompleterPositionSource: function() {
        return null;
    },
    /**
     * Returns the user search element.
     *
     * @return {Element} the element if it exists
     */
    getUserSearchElement: function() {
        return this.domNode.getElementById(this.widgetId + '-user-search');
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
        return this.autosaveLoaded || this.autosaveCreated;
    },

    initAutosaveCookie: function() {
        var cookieName, action;
        if (this.autosaveDisabled) {
            return;
        }
        action = this.action;
        if (this.getFilterParameter('repostNoteId')) {
            action = 'repost';
        }
        // make cookie user-unique
        cookieName = 'autosave_' + action + '_u' + communote.currentUser.id;
        if (action == 'edit') {
            cookieName = cookieName + '_' + this.getFilterParameter('noteId');
        } else if (action == 'comment') {
            cookieName = cookieName + '_' + this.parentPostId;
        } else if (action == 'repost') {
            cookieName = cookieName + '_' + this.getFilterParameter('repostNoteId');
        }
        // cookie is set to be valid for 7 days
        var cookie = new Hash.Cookie(cookieName, {
            duration: 7,
            autoSave: false
        });
        cookie.load();
        this.autosaveCookie = cookie;
    },

    initContent: function(responseMetadata) {
        var targetTopic, offlineAutosaveVersion, onlineAutosave, initObject, autosaveLoaded, content;
        if (this.autosaveCookie) {
            offlineAutosaveVersion = this.autosaveCookie.get('autosaveVersion');
        }
        if (responseMetadata) {
            onlineAutosave = responseMetadata.autosave;
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
        this.initialNote = Object.clone(initObject);
        // check for autosave and if available replace initObject with it to init with autosave
        if (onlineAutosave) {
            autosaveLoaded = true;
            if (offlineAutosaveVersion > onlineAutosave.autosaveVersion) {
                // offline autosave is newer
                initObject = this.convertAutosaveCookieToJson();
                onlineAutosave = false;
            } else {
                initObject = onlineAutosave
            }
        } else if (offlineAutosaveVersion != null) {
            initObject = this.convertAutosaveCookieToJson();
            autosaveLoaded = true;
        }
        // force the target topic of the parent or edited note, or in case of create of the autosave
        if (this.action != 'create' || !autosaveLoaded
                || (this.initialTargetTopic && !this.noTargetTopicChangeIfModifiedOrAutosaved)) {
            initObject.targetBlog = targetTopic;
        }

        this.initContentFromJsonObject(initObject);
        this.contentInitialized(autosaveLoaded && !!onlineAutosave, autosaveLoaded
                && !onlineAutosave);
    },

    initContentFromJsonObject: function(note) {
        var topics;
        this.editor.resetContent(note.content);
        this.addAttachments(note.attachments);
        this.addUsers(note.usersToNotify);
        this.addTags(note.tags);
        if (note.isAutosave) {
            this.autosaveNoteId = note.autosaveNoteId;
            // increment autosave version so that new autosaves are newer
            this.autosaveVersion = note.autosaveVersion + 1;
        }
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
        this.changeDirectMessageMode(note.isDirectMessage);
        this.modified = false;
    },

    /**
     * Helper that adds a tag to the tagStore array if it is not yet contained.
     *
     * @param {Object} tag The tag to add
     * @param {Object[]} tagStore Array that holds tags which can be persisted tags with tagId or
     *            tags that are not yet persisted
     * @returns {Boolean} True if the tag was added, false if it was already contained
     */
    internalAddTag: function(tag, tagStore) {
        if (this.getIndexOfTag(tag, tagStore) === -1) {
            tagStore.push(tag);
            return true;
        }
        return false;
    },

    isModified: function() {
        if (!this.modified) {
            if (this.editor.isDirty()) {
                this.modified = true;
            }
        }
        return this.modified;
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
            this.deleteAutosaveCookie();
            // no need to clean up if removing it anyway
            if (this.publishSuccessBehavior.action != 'remove') {
                this.clearAll();
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
                || (this.attachmentUploader && this.attachmentUploader.hasRunningUploads())) {
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
        if (this.runningAutosaveRequest) {
            // wait for end of autosave
            this.publishNoteWhenAutosaveDone.delay(1000, this);
        } else {
            content = this.editor.getContent();
            this.sendNote(true, content);
        }
    },

    refresh: function() {
        this.cleanup();
        this.parent();
    },

    refreshAttachmentSelection: function() {
        var field = this.getAttachmentSearchElement();
        if (field) {
            this.attachmentUploader = new AjaxFileUpload(field, {
                uploadOnChange: true
            });
            this.attachmentUploader.addEvent('uploadStarting', this.attachmentUploadStarted
                    .bind(this));
            this.attachmentUploader.addEvent('uploadFileNotFound', this.attachmentUploadFailed
                    .bind(this));
            this.attachmentUploader
                    .addEvent('uploadFailed', this.attachmentUploadFailed.bind(this));
            this.attachmentUploader.addEvent('uploadDone', this.attachmentUploadDone.bind(this));
        }
    },

    refreshComplete: function(responseMetadata) {
        var isAutosave, resendNotification;
        isAutosave = (this.autosaveCookie && this.autosaveCookie.get('autosaveVersion') != null)
                || (responseMetadata && responseMetadata.autosave);
        // TODO better name
        this.refreshView(isAutosave);
        this.refreshEditor();

        // attach autocompleters
        if (this.useTopicSelection()) {
            this.refreshTopicSelection(this.getTopicSearchElement());
        }
        if (this.useTagSelection()) {
            this.refreshTagSelection(this.getTagSearchElement());
        }
        if (this.useUserSelection()) {
            this.refreshUserSelection(this.getUserSearchElement());
        }

        if (this.useAttachmentSelection()) {
            this.refreshAttachmentSelection();
        }

        if (this.action === 'edit') {
          // Load the latest setting for the resend notification option via Local Storage
          resendNotification = communoteLocalStorage.getItem("com.communote.editNote.resendNotification");
          if(resendNotification === "true") {
            $(this.widgetId + '-resend-notification').set('checked', (resendNotification === "true"));
          }
          this.changeResendNotificationMode(resendNotification);
        }

        this.initContent(responseMetadata);
        this.startAutosaveJob();
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
     * Attaches a tag autocompleter if a tag search element exists.
     *
     * @param {Element} searchElement The input element to attach the autocompleter to
     */
    refreshTagSelection: function(searchElement) {
        var acOptions;
        if (searchElement) {
            acOptions = this.prepareAutocompleterOptions('getTagAutocompleterPositionSource', true,
                    false, this.setCurrentTopicBeforeRequestCallback.bind(this));
            if (!acOptions.suggestionsOptions) {
                acOptions.suggestionsOptions = {};
            }
            acOptions.suggestionsOptions['categories'] = this.tagAutocompleterCategories;
            this.tagAutocompleter = autocompleterFactory.createTagAutocompleter(searchElement,
                    acOptions, null, 'NOTE', false, this.tagAutocompleterMultipleMode);
            this.tagAutocompleter.addEvent('onChoiceSelected', this.tagChoiceSelected.bind(this));
        }
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

    /**
     * Called after refresh if useUserSelection returned true. Default implementation attaches a
     * user autocompleter to it.
     *
     * @param {Element} searchElement The input element to attach the autocompleter to
     */
    refreshUserSelection: function(searchElement) {
        var acOptions;
        if (searchElement) {
            // do not remove focus when selecting because the user will usually add more than one user
            acOptions = this.prepareAutocompleterOptions('getUserAutocompleterPositionSource',
                    true, false, this.setCurrentTopicBeforeRequestCallback.bind(this));
            acOptions.staticDataSourceOptions = {};
            acOptions.staticDataSourceOptions.approveMatchCallback = this.checkDiscussionContextApproveMatchCallback
                    .bind(this);
            this.userAutocompleter = autocompleterFactory.createMentionAutocompleter(searchElement,
                    acOptions, null, true);
            this.userAutocompleter.addEvent('onChoiceSelected', this.userChoiceSelected.bind(this));
        }
    },

    refreshView: function(autosaveLoaded) {
        this.ajaxLoadingOverlay = this.widgetController.createAjaxLoadingOverlay(this.domNode,
                false);
        this.placeholders = communote.utils.attachPlaceholders(null, this.domNode);
    },

    remove: function(deleteAutosave) {
        if (deleteAutosave) {
            if (this.action != 'create') {
                this.discardAutosave(true);
            }
        }
        this.widgetController.removeWidget(this);
    },

    removeAttachment: function(id) {
        if (this.removeItemFromDataHolder(this.attachments, id)) {
            this.attachmentRemoved(id);
        }
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

    /**
     * Remove one or all tags from the addedTags array. If the array was changed tagRemoved will be
     * called.
     *
     * @param {Object|String} tag The tag to remove or null to remove all tags
     */
    removeTag: function(tag) {
        var modified = false, index;
        if (tag == null) {
            if (this.addedTags.length > 0) {
                this.addedTags.empty();
                modified = true;
            }
        } else {
            tag = this.convertToTagObject(tag);
            index = this.getIndexOfTag(tag, this.addedTags);
            if (index > -1) {
                modified = true;
                this.addedTags.splice(index, 1);
            }
        }
        if (modified) {
            this.dirty = true;
            this.modified = true;
            this.tagRemoved(tag);
        }
        return false;
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

    /**
     * Removes an previously added user from the users to notify.
     *
     * @param {Object} [userData] the object holding the details of the user to remove, if not
     *            specified all users are removed
     */
    removeUser: function(userData) {
        var alias = userData && userData.alias;
        if (alias && this.isDirectMessage && this.usersToNotify.ids.contains(alias)
                && this.usersToNotify.ids.length == 1) {
            // do not allow removing the last user if we are in DM mode
            showNotification(NOTIFICATION_BOX_TYPES.failure, '',
                    getJSMessage('error.blogpost.edit.remove-direct-user'), null);
            return;
        }
        if (this.removeItemFromDataHolder(this.usersToNotify, alias)) {
            this.userRemoved(userData);
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
        if (this.initialNote && Object.getLength(this.initialNote)) {
            // restore original note
            this.initContentFromJsonObject(this.initialNote);
        }
        this.startAutosaveJob();
    },

    /**
     * Called when the title of a topic in the topics member is changed. Default implementation does
     * nothing.
     */
    selectedTopicTitleChanged: function(topicItem) {
    },

    sendNote: function(publish, content) {
        var successCallback, errorCallback;
        var data = this.createPostData(publish, content);
        var options = {};

        if (publish) {
            options.defaultErrorMessage = getJSMessage('error.blogpost.create.failed');
            // can use note published callback for success and error since state is evaluated there
            successCallback = this.notePublished.bind(this, data.topicId);
            errorCallback = successCallback;
        } else {
            this.runningAutosaveRequest = true;
            successCallback = this.autosaveNoteOnlineComplete.bind(this);
            errorCallback = function(resultObj) {
                this.runningAutosaveRequest = false;
                // TODO anything to be done?
            }.bind(this);
        }
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
        if (this.autosaveDisabled || this.autosaveJobId != null) {
            return;
        }
        // disable autosave functionality if interval is to small
        if (this.autosaveTimerPeriod < 1000) {
            return;
        }
        this.dirty = false;
        this.autosaveJobId = this.autosaveJob.periodical(this.autosaveTimerPeriod, this);
    },

    /**
     * Disables the send button and shows an AJAX loading overlay
     */
    startPublishNoteFeedback: function() {
        // disable send button
        this.getSendButtonElement().disabled = 'disabled';
        this.ajaxLoadingOverlay.setStyle('display', '');
    },

    /**
     * Stops the autosave job.
     */
    stopAutosaveJob: function() {
        this.dirty = false;
        if (this.autosaveDisabled) {
            return;
        }
        this.autosaveJobActiveTypes.empty();
        if (this.autosaveJobId != null) {
            clearInterval(this.autosaveJobId);
            this.autosaveJobId = null;
        }
    },

    /**
     * Re-enables the send button and hides the AJAX loading overlay
     */
    stopPublishNoteFeedback: function() {
        this.ajaxLoadingOverlay.setStyle('display', 'none');
        this.getSendButtonElement().removeProperty('disabled');
    },

    storeInAutosaveCookie: function(content) {
        var targetBlog, defaultBlog, tags, crosspostTopics, properties;
        tags = this.createTagsPostData(false);
        if (tags.length > 0) {
            this.autosaveCookie.set('tags', tags);
        }
        if (this.attachments.items.length > 0) {
            this.autosaveCookie.set('attachs', this.attachments.items);
        }
        crosspostTopics = this.getCrosspostTopics(false);
        if (crosspostTopics.length > 0) {
            this.autosaveCookie.set('blogs', crosspostTopics);
        }
        if (this.usersToNotify.items.length > 0) {
            this.autosaveCookie.set('users', this.usersToNotify.items);
        }
        properties = this.createPropertiesPostData(false);
        if (properties) {
            this.autosaveCookie.set('properties', properties);
        }
        this.autosaveCookie.set('content', content);
        this.autosaveCookie.set('isDirectMessage', this.isDirectMessage);
        if (this.autosaveNoteId != null) {
            this.autosaveCookie.set('autosaveNoteId', this.autosaveNoteId);
        }
        // save targetBlog when in create mode to allow restoring it, even if unset
        if (this.action == 'create') {
            this.autosaveCookie.set('targetBlog', this.topics.items[0]);
        }
    },

    /**
     * Called when a tag was added for instance by selecting it from the autocompleter or submitting
     * the tag input field. Subclasses can use this class to update a summary. This class does
     * nothing.
     *
     * @param {Object|String} tag The tag that was added
     * @param {Boolean} moreToCome Whether more tags will be added. This can be used to optimize
     *            summary creation.
     */
    tagAdded: function(tag, moreToCome) {
    },

    tagChoiceSelected: function(inputElem, choiceElem, token, value) {
        this.addTag(token);
    },

    tagRemoved: function(tag) {
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

    updateAutosaveMarker: function(message, renderLink) {
        var htmlData;
        var autosaveMarker = this.getAutosaveMarkerElement();
        if (!autosaveMarker)
            return;
        if (message != null) {
            htmlData = '<span class="cn-note-autosave-message">' + message + '</span>';
            if (renderLink) {
                htmlData += '<a class="cn-icon cn-cancel" href="javascript:;" onclick="widgetController.getWidget(\'';
                htmlData += this.widgetId;
                htmlData += '\').discardAutosaveWithConfirm(event)" title="'
                        + getJSMessage('blogpost.autosave.discard') + '"></a>';
            }
            htmlData += '<span class="cn-clear"><!-- --></span>';

            autosaveMarker.set('html', htmlData);
        } else {
            autosaveMarker.set('html', '');
        }
        return autosaveMarker;
    },

    useAttachmentSelection: function() {
        return true;
    },

    /**
     * Called after a user was added. Subclasses can use this hook to update the view.
     *
     * @param {Object} userData JSON object describing the added user
     * @param {boolean} moreToCome true if the user was added as part of batch update and the
     *            current item is not the last. Can be used to optimize view updates.
     */
    userAdded: function(userData, moreToCome) {
    },

    userChoiceSelected: function(inputElem, choiceElem, token, value) {
        this.addUsers(token);
    },

    /**
     * Called after a user was removed. Subclasses can use this hook to update the view.
     *
     * @param {Object} [userData] the object holding the details of the user that was removed. This
     *            parameter is null if all users were removed.
     */
    userRemoved: function(userData) {
    },

    useTagSelection: function() {
        return true;
    },

    useTopicSelection: function() {
        // topics can only be selected when creating notes
        return this.action == 'create';
    },

    useUserSelection: function() {
        return true;
    },

    /**
     * Can be called to invoke a direct message on the actual CreateNote Widget.
     *
     * @param {Object} userAlias The user alias the direct message should be send to.
     */
    writeDirectMessage: function(userAlias) {
        var content = this.editor.getContent();
        if (content.length > 0 || this.usersToNotify.ids.length > 0
                || this.getCrosspostTopicsCount() == 0) {
            // TODO use showDialog utility function!
            var divContent = '<p style="padding: 5px; text-align: center;">'
                    + getJSMessage('blog.post.dm.overwrite.existing')
                    + '</p><div class="actionbar"><div class="button-gray button-left">'
                    + '<input type="button" name="button" onclick="widgetController.getWidget(\''
                    + this.widgetId
                    + '\').writeDirectMessage_Submit(\''
                    + userAlias
                    + '\');closeDialog();return false;" value="'
                    + getJSMessage('javascript.dialog.button.label.yes')
                    + '"></div><div class="button-gray button-right">'
                    + '<input type="button" name="b2" onclick="closeDialog();return false;" value="'
                    + getJSMessage('javascript.dialog.button.label.no')
                    + '"></div><span class="clear"><!-- ie --></span></div>';

            var questionContainer = new Element('div', {
                'html': divContent,
                'class': 'popupConfirmation form_wrapper'
            });

            TB_show2(getJSMessage('blog.post.dm.overwrite.existing.title'),
                    "TB_inline?width=300&height=200", document, questionContainer);
        } else {
            this.writeDirectMessage_Submit(userAlias);
        }
    },
    /**
     * This method is invoked, when the user want's to overwrite the existing message.
     */
    writeDirectMessage_Submit: function(userAlias) {
        this.clearAll();
        this.editor.setContent('d @' + userAlias + ' ');
        this.updateDirectMessage(true);
        window.scrollTo(0, 0);
    }
});
