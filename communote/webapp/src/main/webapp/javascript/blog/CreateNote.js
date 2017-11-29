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
    defaultTagStoreAlias: 'DefaultNoteTagStore',
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

    resendNotificationProperty: {},

    // css classes to be applied to the create note container (getWriteContainerElement)
    // when the editor only supports richtext
    richTextEditorCssClass: null,
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

        // added items defined by an ordered array of ids (which might be aliases or sth) and the
        // items in the same order
        this.usersToNotify = {
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
        var action, targetBlogId, targetBlogTitle, parentPostId, autosaveDisabled;
        var cancelBehavior, publishSuccessBehavior, tagAutocompleterCategories;
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

        this.initAutosaveHandler();
        this.components = new communote.classes.NoteEditorComponentManager(this, action,
                this.renderStyle, this.getAllStaticParameters());
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
     * Define if already notified user should receive notifications again
     */
    changeResendNotificationMode: function(activate) {
      this.resendNotificationProperty = {
        'key': 'editNote.resendNotification',
        'keyGroup': 'com.communote',
        'value': activate
      };
      communoteLocalStorage.setItem('com.communote.editNote.resendNotification', activate);
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
        this.removeUser(null);
        this.editor.resetContent(null);
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
    // TODO rename getNoteDataForPost
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
        this.components.appendNoteDataForRestRequest(data, publish, false);
        data.publish = publish === true ? true : false;
        if (this.autosaveHandler) {
            data.autosaveNoteId = this.autosaveHandler.getNoteId();
            data.noteVersion = this.autosaveHandler.getVersion();
        }

        if(this.action === 'edit') {
          if (this.noteProperties === null) {
            this.noteProperties = [];
          }
          this.noteProperties.push(this.resendNotificationProperty);
        }

        properties = this.createPropertiesPostData();
        if (properties) {
            data.properties = properties;
        }
        return data;
    },
    /**
     * @return {Object[]} array with all note properties or null if no properties were set
     */
    createPropertiesPostData: function() {
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
     * Called by changeDirectMessageMode if the new mode could be set.
     */
    directMessageModeChanged: function() {
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
    
    getNoteData: function(resetDirtyFlag) {
        var properties;
        var data = {};
        data.tags = this.createTagsPostData(false);
        data.crosspostTopics = this.getCrosspostTopics(false);
        data.usersToNotify = this.usersToNotify.items;
        data.content = this.editor.getContent();
        data.isDirectMessage = this.isDirectMessage;
        data.targetTopic = this.topics.items[0];
        data.isHtml = this.editor.supportsHtml();
        data.noteId = this.getFilterParameter('noteId');
        if (this.action != 'edit') {
            data.parentNoteId = this.parentPostId;
        }

        if(this.action === 'edit') {
          if (this.noteProperties === null) {
            this.noteProperties = [];
          }
          this.noteProperties.push(this.resendNotificationProperty);
        }

        properties = this.createPropertiesPostData();
        if (properties) {
            data.properties = properties;
        }
        this.components.appendNoteData(data, resetDirtyFlag);
        if (resetDirtyFlag) {
            this.dirty = false;
        }
        return data;
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
        this.addUsers(note.usersToNotify);
        this.addTags(note.tags);
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
        this.parent();
    },

    refreshComplete: function(responseMetadata) {
        var resendNotification;
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
        if (this.useTagSelection()) {
            this.refreshTagSelection(this.getTagSearchElement());
        }
        if (this.useUserSelection()) {
            this.refreshUserSelection(this.getUserSearchElement());
        }

        if (this.action === 'edit') {
          // Load the latest setting for the resend notification option via Local Storage
          resendNotification = communoteLocalStorage.getItem('com.communote.editNote.resendNotification');
          if(resendNotification === 'true') {
            $(this.widgetId + '-resend-notification').set('checked', true);
          }
          this.changeResendNotificationMode(resendNotification);
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
        var userInput, topicInput, uncommittedUser, uncommittedBlog, msgKey;
        var cancelFunction, buttons, successCallback, errorCallback, data, options;
        if (!ignoreUncommittedOptions) {
            userInput = this.getUserSearchElement();
            topicInput = this.getTopicSearchElement();
            uncommittedUser = userInput && userInput.value.trim().length;
            uncommittedBlog = topicInput && topicInput.value.trim().length;
            if (uncommittedUser || uncommittedBlog) {
                msgKey = 'blogpost.create.submit.confirm.unsaved.';
                if (uncommittedUser) {
                    msgKey += 'user';
                }
                if (uncommittedBlog) {
                    msgKey += 'blog';
                }
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
                showDialog(this.getSendButtonElement().value, getJSMessage(msgKey), buttons, {
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
