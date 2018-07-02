(function() {
    const communote = window.communote;
    const i18n = communote.i18n;

    function addTopic(handler, topicData) {
        var idx;
        if (handler.targetTopic && handler.targetTopic.id == topicData.id) {
            return false;
        }
        idx = handler.topicIds.indexOf(topicData.id);
        if (idx < 0) {
            // assert the topic has an alias since it is required for crossposting
            if (!topicData.alias) {
                throw 'Alias of crosspost topic is missing';
            }
            handler.topicIds.push(topicData.id);
            handler.topicData.push(topicData);
            return true;
        }
        return false;
    }
    
    function extractTopicName(topicData) {
        return topicData.title;
    }
    
    /**
     * Autocompleter callback which is invoked when a suggestion is selected. Needs to be bound to
     * the component instance.
     */
    function topicChoiceSelected(inputElem, choiceElem, token, value) {
        this.addTopics(token);
    }
    
    /**
     * Create a NoteEditorComponent which allows adding crosspost topics to a note. The component
     * will also visualize the current target topic and allows setting another target topic on the
     * widget. However, it won't extract the initial target topic or observe the filter parameter
     * store for target topic changes or add the target topic in the note data when autosaving or
     * publishing the note
     * 
     * @param noteEditorWidget The note editor widget
     * @param {String} action The action/mode of the widget
     * @param {String} initialRenderStyle The initial render style the widget was created with.
     * @param {Object} options The settings (staticParameters) the widget was created and
     *            initialized with.
     * 
     * @class
     */
    function TopicHandler(noteEditorWidget, action, initialRenderStyle, options) {
        this.widget = noteEditorWidget;
        this.widgetId = noteEditorWidget.widgetId;
        this.topicTextboxList = null;
        this.topicAutocompleter = null;
        this.topicIds = [];
        this.topicData = [];
        this.targetTopic = null;
        this.dirty = false;
        this.modified = false;
        this.isDirectMessage = false;
        noteEditorWidget.addEventListener('widgetRefreshed', this.onWidgetRefreshed, this);
        noteEditorWidget.addEventListener('renderStyleChanged', this.onRenderStyleChanged, this);
        noteEditorWidget.addEventListener('userInterfaceShown', this.onUserInterfaceShown, this);
        noteEditorWidget.addEventListener('directMessageModeChanged', this.onDirectMessageModeChanged, this);
        noteEditorWidget.addEventListener('targetTopicChanged', this.onTargetTopicChanged, this);
        noteEditorWidget.addEventListener('targetTopicReset', this.onTargetTopicReset, this);
        noteEditorWidget.addEventListener('targetTopicTitleChanged', this.onTargetTopicTitleChanged, this);
        // make sure resources are freed when widget is removed or DOM is refreshed
        noteEditorWidget.addEventListener('widgetRefreshing', this.cleanup, this);
        noteEditorWidget.addEventListener('widgetRemoving', this.cleanup, this);
    }

    TopicHandler.prototype.addTopics = function(topics) {
        var crosspostTopics, i, l;
        if (!topics) {
            return;
        }
        if (Array.isArray(topics)) {
            if (this.targetTopic == null) {
                this.widget.setTargetTopic(topics[0]);
                crosspostTopics = topics.slice(1);
            } else {
                crosspostTopics = topics;
            }
        } else {
            if (this.targetTopic == null) {
                this.widget.setTargetTopic(topics);
            } else {
                crosspostTopics = topics
            }
        }
        if (!crosspostTopics || this.isDirectMessage) {
            // TODO show an error message that adding crosspost topics is not allowed in DM mode?
            return;
        }
        if (Array.isArray(crosspostTopics)) {
            for (i = 0, l = crosspostTopics.length; i < l; i++) {
                if (addTopic(this, crosspostTopics[i])) {
                    this.topicAdded(crosspostTopics[i]);
                }
            }
        } else {
            if (addTopic(this, crosspostTopics)) {
                this.topicAdded(crosspostTopics);
            }
        }
    };
    
    /**
     * Implementation of the NoteEditorComponent method which appends the data of this component.
     * 
     * @param {Object} noteData Object for adding the data to
     * @param {boolean} resetDirtyState Whether to set the internal dirty state to not-dirty.
     */
    TopicHandler.prototype.appendNoteData = function(noteData, resetDirtyState) {
        noteData.crosspostTopics = this.topicData;
        if (resetDirtyState) {
            this.dirty = false;
        }
    };

    /**
     * Implementation of the NoteEditorComponent method which appends the data of this component in
     * the format which is understood by the REST API.
     * 
     * @param {Object} noteData Object for adding the data to
     * @param {boolean} resetDirtyState Whether to set the internal dirty state to not-dirty.
     */
    TopicHandler.prototype.appendNoteDataForRestRequest = function(noteData, publish,
            resetDirtyState) {
        var i;
        var l = this.topicData.length;
        if (l) {
            noteData.crossPostTopicAliases = [];
            for (i = 0; i < l; i++) {
                noteData.crossPostTopicAliases.push(this.topicData[i].alias);
            }
        }
        if (resetDirtyState) {
            this.dirty = false;
        }
    };

    /**
     * Implementation of the NoteEditorComponent method which tests whether the note can be
     * published.
     * 
     * @return {boolean} always true
     */
    TopicHandler.prototype.canPublishNote = function() {
        return true;
    };

    /**
     * Cleanup any resources to avoid memory leaks
     * 
     * @protected
     */
    TopicHandler.prototype.cleanup = function() {
        if (this.topicAutocompleter) {
            this.topicAutocompleter.destroy();
        }
        if (this.topicTextboxList) {
            this.topicTextboxList.destroy();
        }
    };

    /**
     * @protected
     */
    TopicHandler.prototype.getTopicInputElement = function() {
        return this.widget.domNode.querySelector('#' + this.widgetId + '-topic-search');
    };

    TopicHandler.prototype.getUnconfirmedInputWarning = function() {
        var topicInputElem = this.getTopicInputElement();
        if (topicInputElem && topicInputElem.value.trim().length) {
            return {
                message: i18n.getMessage('blogpost.create.submit.confirm.unsaved.blog'),
                inputName: i18n.getMessage('widget.createNote.topics.unconfirmed.input.inputName')
            };
        }
    };

    /**
     * Implementation of the NoteEditorComponent method which initializes the data managed by this
     * component.
     * 
     * @param {?Object} noteData Object with details about the note to initialize with.
     */
    TopicHandler.prototype.initContent = function(noteData) {
        this.removeTopic(null);
        if (noteData) {
            // no crosspost topics when targetTopic is not set
            if (this.targetTopic && noteData.crosspostBlogs) {
                this.addTopics(noteData.crosspostBlogs);
            }
        }
        this.dirty = false;
        this.modified = false;
    };

    /**
     * Implementation of the NoteEditorComponent method which tests whether the data managed by this
     * component is dirty.
     * 
     * @return {boolean} true if topics were removed or added after the dirty state had been reset
     */
    TopicHandler.prototype.isDirty = function() {
        return this.dirty;
    };

    /**
     * Implementation of the NoteEditorComponent method which tests whether the data managed by this
     * component has been modified.
     * 
     * @return {boolean} true if topics were removed or added after the content had been
     *         initialized
     */
    TopicHandler.prototype.isModified = function() {
        return this.modified;
    };

    TopicHandler.prototype.onDirectMessageModeChanged = function(active) {
        this.isDirectMessage = active;
        this.topicTextboxList.setLimit(this.isDirectMessage ? 1 : -1);
    };
    
    TopicHandler.prototype.onRenderStyleChanged = function(changeDescriptor) {
        if (changeDescriptor.oldStyle === 'simulate' && this.topicTextboxList) {
            // might have been resized while it was hidden which leads to wrong positioning
            this.topicTextboxList.resizeInputToFillAvailableSpace();
        }
    };
    
    TopicHandler.prototype.onTargetTopicChanged = function(newTargetTopic) {
        var idx, i, l;
        if (!newTargetTopic) {
            if (this.targetTopic) {
                this.removeTopic(this.targetTopic);
            }
            return;
        }
        // ignore if already up-to-date (e.g. if this instance triggered the targetTopic change or targetTopicReset fired before)
        if (this.targetTopic && this.targetTopic.id == newTargetTopic.id) {
            return;
        }
        idx = this.topicIds.indexOf(newTargetTopic.id);
        if (idx != -1) {
            this.topicRemoved(this.topicData[idx], true);
            this.topicIds.splice(i, 1);
            this.topicData.splice(i, 1);
        }
        // set new placeholder if there was no targetTopic
        if (!this.targetTopic) {
            this.refreshPlaceholder('blogpost.create.topics.crosspost.hint');
        }
        this.targetTopic = newTargetTopic;
        if (this.topicTextboxList) {
            // rebuild
            this.topicTextboxList.clearItems();
            this.topicTextboxList.addItem(newTargetTopic);
            for (i = 0, l = this.topicData.length; i < l; i++) {
                this.topicTextboxList.addItem(this.topicData[i]);
            }
        }
    };
    
    TopicHandler.prototype.onTargetTopicReset = function(newTargetTopic) {
        // remove all crosspost topics and set new target topic. Preserve current dirty and
        // modified states.
        var oldDirty, oldModified;
        if (this.topicIds.length) {
            oldDirty = this.dirty;
            oldModified = this.modified;
            this.topicData = [];
            this.topicIds = [];
            this.topicRemoved(null, true);
            this.dirty = oldDirty;
            this.modified = oldModified;
        } else if (this.targetTopic) {
            this.removeTopicFromTextboxList(null);
        }
        // set new placeholder if there was no targetTopic
        if (!this.targetTopic && newTargetTopic) {
            this.refreshPlaceholder('blogpost.create.topics.crosspost.hint');
        }
        this.targetTopic = newTargetTopic;
        if (newTargetTopic && this.topicTextboxList) {
            this.topicTextboxList.addItem(newTargetTopic);
        }
    };

    TopicHandler.prototype.onTargetTopicTitleChanged = function(newTitle) {
        if (this.targetTopic) {
            this.targetTopic.title = newTitle;
            if (this.topicTextboxList) {
                this.topicTextboxList.refreshContentOfItem(this.targetTopic);
            }
        }
    };
    TopicHandler.prototype.onUserInterfaceShown = function() {
        if (this.topicTextboxList) {
            // might have changed while invisible which leads to wrong positioning
            this.topicTextboxList.resizeInputToFillAvailableSpace();
        }
    };
    
    /**
     * @protected
     */
    TopicHandler.prototype.onWidgetRefreshed = function() {
        var acOptions;
        var inputElem = this.getTopicInputElement();
        this.topicTextboxList = new communote.classes.TextboxList(inputElem, {
            autoRemoveItemCallback: this.removeTopic.bind(this),
            allowDuplicates: false,
            listCssClass: 'cn-border',
            itemRemoveCssClass: 'textboxlist-item-remove cn-icon',
            itemLimitReachedAction: 'disable',
            itemRemoveTitle: i18n.getMessage('blogpost.create.blogs.remove.tooltip'),
            parseItemCallback: extractTopicName
        });
        if (this.targetTopic != null) {
            this.topicTextboxList.addItem(this.targetTopic);
        }
        acOptions = this.prepareAutocompleterOptions();
        this.topicAutocompleter = autocompleterFactory.createTopicAutocompleter(inputElem,
                acOptions, null, false, 'write');
        this.topicAutocompleter.addEvent('onChoiceSelected', topicChoiceSelected.bind(this));
    };

    /**
     * @protected
     */
    TopicHandler.prototype.prepareAutocompleterOptions = function() {
        var positionSource = this.widget.domNode.querySelector('.cn-write-note-accessory-topic .cn-border');
        var preparedOptions = {};
        preparedOptions.autocompleterOptions = {};
        if (positionSource) {
            preparedOptions.inputFieldOptions = {
                positionSource: positionSource
            };
        }
        preparedOptions.autocompleterOptions.clearInputOnSelection = true;
        preparedOptions.autocompleterOptions.unfocusInputOnSelection = false;
        return preparedOptions;
    };
    
    TopicHandler.prototype.refreshPlaceholder = function(msgKey) {
        var inputElem = this.getTopicInputElement();
        if (inputElem) {
            inputElem.placeholder = i18n.getMessage(msgKey);
        }
    };
    
    TopicHandler.prototype.removeTopic = function(topicData) {
        var i, idx, newTargetTopic, inputElem;
        if (!topicData) {
            // since textbox list contains target topic and crosspost topics remove latter individually
            for (i = this.topicData.length - 1; i >= 0; i--) {
                this.topicRemoved(this.topicData[i], true);
            }
            this.topicData = [];
            this.topicIds = [];
            // clear input
            inputElem = this.getTopicInputElement();
            if (inputElem) {
                inputElem.value = '';
            }
        } else {
            if (this.targetTopic && this.targetTopic.id == topicData.id) {
                // target topic was removed. Use first crosspost target as new target topic
                if (this.topicData.length) {
                    // just remove topic from textbox list without marking dirty or sending event
                    this.removeTopicFromTextboxList(this.targetTopic);
                    newTargetTopic = this.topicData.shift();
                    this.topicIds.shift();
                    this.targetTopic = newTargetTopic;
                    // notify about crosspost topic removal, but don't remove from textbox list as
                    // it is the new target topic
                    this.topicRemoved(newTargetTopic, false);
                } else {
                    this.targetTopic = null;
                    this.removeTopicFromTextboxList(null);
                    this.refreshPlaceholder('blogpost.create.topics.hint');
                }
                this.widget.setTargetTopic(newTargetTopic);
            } else {
                idx = this.topicIds.indexOf(topicData.id);
                if (idx > -1) {
                    this.topicIds.splice(idx, 1);
                    this.topicData.splice(idx, 1);
                    this.topicRemoved(topicData, true);
                }
            }
        }
    };
    
    TopicHandler.prototype.removeTopicFromTextboxList = function(topicData) {
        if (this.topicTextboxList) {
            if (!topicData) {
                this.topicTextboxList.clearItems();
            } else {
                this.topicTextboxList.removeItem(topicData);
            }
        }
    };
    
    TopicHandler.prototype.topicAdded = function(topicData) {
        if (this.topicTextboxList) {
            this.topicTextboxList.addItem(topicData);
        }
        this.dirty = true;
        this.modified = true;
        this.widget.emitEvent('topicAdded', topicData);
    };
    TopicHandler.prototype.topicRemoved = function(topicData, updateView) {
        if (updateView) {
            this.removeTopicFromTextboxList(topicData);
        }
        this.dirty = true;
        this.modified = true;
        this.widget.emitEvent('topicRemoved', topicData);
    };
    // publish class to allow subclassing or modification by plugins
    communote.classes.NoteEditorTopics = TopicHandler;
    // register for modes where new notes are created
    communote.NoteEditorComponentFactory.register(['create', 'repost'], TopicHandler);
})();