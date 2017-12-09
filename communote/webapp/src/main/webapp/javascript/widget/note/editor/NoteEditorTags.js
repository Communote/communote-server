(function(window) {
    const communote = window.communote;
    const i18n = communote.i18n;

    /**
     * Helper that adds a tag to the tagStore array if it is not yet contained.
     * 
     * @param {Object} tag The tag to add
     * @param {Object[]} tagStore Array that holds tags which can be persisted tags with tagId or
     *            tags that are not yet persisted
     * @returns {boolean} True if the tag was added, false if it was already contained
     */
    function addTag(tag, tagStore) {
        if (getIndexOfTag(tag, tagStore) === -1) {
            tagStore.push(tag);
            return true;
        }
        return false;
    }

    /**
     * Convert a tag string to a valid tag object containing the minimum required members if it is
     * not already one.
     * 
     * @param {(String|Object)} tag The tag to process
     */
    function convertToTagObject(tag) {
        if (typeof (tag) === 'string') {
            return {
                defaultName: tag
            };
        }
        return tag;
    }

    function extractTagName(tag) {
        return (tag.name == undefined) ? tag.defaultName : tag.name;
    }
    
    /**
     * @returns {Object[]} an array of objects representing the tags the user typed into the tag
     *          input field. The array is empty there is no tag input or no tags were added.
     */
    function extractTagsFromInput(inputElem, tagStringSplitRegEx) {
        var tagString, splitted, i, l;
        var tags = [];
        if (inputElem) {
            tagString = inputElem.value.trim();
            if (tagString.length > 0) {
                splitted = tagString.split(tagStringSplitRegEx);
                for (i = 0, l = splitted.length; i < l; i++) {
                	// skip blank
                    if (splitted[i].length > 0) {
                        tags.push(convertToTagObject(splitted[i]));
                    }
                }
            }
        }
        return tags;
    }

    /**
     * @returns {Object[]} an array of objects representing the tags to attach when saving the note
     */
    function getAllTags(tagHandler, publish) {
        var i, finalTags, uncommittedTags;
        // create shallow copy
        for (i = tagHandler.addedTags.length, finalTags = new Array(i); i--;) {
        	finalTags[i] = tagHandler.addedTags[i];
        }
        // do not save tags from input when doing an autosave and the user is still
        // typing in the tag field, because we would add incomplete tags to the DB
        if (tagHandler.storeUncommittedTags
                && (publish || (tagHandler.tagAutocompleter && !tagHandler.tagAutocompleter
                        .isInputElementFocused()))) {
            uncommittedTags = extractTagsFromInput(tagHandler.getTagInputElement(),
            		tagHandler.tagStringSplitRegEx);
            for (i = 0; i < uncommittedTags.length; i++) {
                addTag(uncommittedTags[i], finalTags);
            }
        }
        return finalTags;
    }
    
    /**
     * Returns the index of a tag within the tagStore array.
     * 
     * @param {Object} tag The tag to find, can be a simple unpersisted tag which just has the
     *            defaultName set or a persisted tag with tagId
     * @param {Object[]} tagStore Array that holds tags which can be persisted tags with tagId or
     *            tags that are not yet persisted
     * @returns {number} the index of the tag or -1 if not contained
     */
    function getIndexOfTag(tag, tagStore) {
        var i, l, addedTag;
        var index = -1;
        // check if tag is just a string or a structured tag with tagId
        var simpleTag = tag.tagId == undefined;
        for (i = 0, l = tagStore.length; i < l; i++) {
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
    }

    /**
     * AutocompleterRequestDataSource callback implementation that sets the current topic before
     * sending the request. Needs to be bound to the component instance.
     */
    function setCurrentTopicBeforeRequestCallback(request, postData, queryParam) {
        postData['blogId'] = this.targetTopicId;
    }
    
    /**
     * Autocompleter callback which is invoked when a suggestion is selected. Needs to be bound to
     * the component instance.
     */
    function tagChoiceSelected(inputElem, choiceElem, token, value) {
        this.addTag(token);
    }
    
    function tagInputEnterPressed() {
    	var tags = extractTagsFromInput(this.getTagInputElement(), this.tagStringSplitRegEx);
        this.addTags(tags);
        this.tagAutocompleter.resetQuery(true);
    }

    /**
     * Create a NoteEditorComponent which allows adding tags to a note.
     * 
     * @param noteEditorWidget The note editor widget
     * @param {String} action The action/mode of the widget
     * @param {String} initialRenderStyle The initial render style the widget was created with.
     * @param {Object} options The settings (staticParameters) the widget was created and
     *            initialized with.
     * 
     * @class
     */
    function TagHandler(noteEditorWidget, action, initialRenderStyle, options) {
        this.widget = noteEditorWidget;
        this.widgetId = noteEditorWidget.widgetId;
        this.tagTextboxList = null;
        this.tagAutocompleter = null;
        // categories of the tag autocompleter. Can be serialized JSON (legacy code, e.g. edit)
        if (typeof options.tagAutocompleterCategories === 'string') {
            this.tagAutocompleterCategories = JSON.parse(options.tagAutocompleterCategories);
        } else {
            this.tagAutocompleterCategories = options.tagAutocompleterCategories;
        }
        if (!this.tagAutocompleterCategories) {
            // create default category definition which uses the default note tagstore
            this.tagAutocompleterCategories = [ {
                id: 'DefaultNoteTagStore',
                provider: 'DefaultNoteTagStore',
                title: ''
            } ];
        }
        // defines whether tags that were just typed into the tag input should be included when
        // submitting the note
        this.storeUncommittedTags = true;
        // RegEx to split the tags in the tag input field
        this.tagStringSplitRegEx = /\s*,\s*/;
        this.canShowTagSelection = initialRenderStyle === 'full';
        this.tagSelectionShown = false;
        this.addedTags = [];
        this.targetTopicId = null;
        this.dirty = false;
        this.modified = false;
        noteEditorWidget.addEventListener('widgetRefreshed', this.onWidgetRefreshed, this);
        noteEditorWidget.addEventListener('renderStyleChanged', this.onRenderStyleChanged, this);
        noteEditorWidget.addEventListener('targetTopicChanged', this.onTargetTopicChanged, this);
        // make sure resources are freed when widget is removed or DOM is refreshed
        noteEditorWidget.addEventListener('widgetRefreshing', this.cleanup, this);
        noteEditorWidget.addEventListener('widgetRemoving', this.cleanup, this);
    }

    /**
     * Add a tag to the addedTags array if it is not yet contained. In case the tag was added
     * tagAdded will be called and the dirty flag will be set.
     * 
     * @param {String|Object} tag The tag to add, can be a simple string tag or a structured tag
     *            object
     */
    TagHandler.prototype.addTag = function(tag) {
        // if the tag is a string build the minimal tag object
        tag = convertToTagObject(tag);
        if (addTag(tag, this.addedTags)) {
            this.tagAdded(tag);
            this.dirty = true;
            this.modified = true;
        }
    };

    /**
     * Add a bunch of tags to the addedTags array. Only tags that are not yet contained will be
     * added. For each added tag the tagAdded will be called. If any tag was added the dirty flag
     * will be set.
     * 
     * @param {(String|Object|String[]|Object[])} tags The tag or tags to add. The tag can be a
     *            simple string tag, a structured tag object or an array of these.
     */
    TagHandler.prototype.addTags = function(tags) {
        var i, l;
        if (!tags) {
            return;
        }
        if (Array.isArray(tags)) {
            for (i = 0, l = tags.length; i < l; i++) {
                this.addTag(tags[i]);
            }
        } else {
            this.addTag(tags);
        }
    };
    
    /**
     * Implementation of the NoteEditorComponent method which appends the data of this component.
     * 
     * @param {Object} noteData Object for adding the data to
     * @param {boolean} resetDirtyState Whether to set the internal dirty state to not-dirty.
     */
    TagHandler.prototype.appendNoteData = function(noteData, resetDirtyState) {
        noteData.tags = getAllTags(this, false);
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
    TagHandler.prototype.appendNoteDataForRestRequest = function(noteData, publish,
            resetDirtyState) {
    	noteData.tags = getAllTags(this, publish);
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
    TagHandler.prototype.canPublishNote = function() {
        return true;
    };
    
    /**
     * Cleanup any resources to avoid memory leaks
     * 
     * @protected
     */
    TagHandler.prototype.cleanup = function() {
    	if (this.tagAutocompleter) {
    		this.tagAutocompleter.destroy();
    	}
    	if (this.tagTextboxList) {
    		this.tagTextboxList.destroy();
    	}
    };

    /**
     * Returns the tag input element.
     * 
     * @return {Element} the element if it exists
     * 
     * @protected
     */
    TagHandler.prototype.getTagInputElement = function() {
        return this.widget.domNode.querySelector('#' + this.widgetId + '-tag-search');
    };

    /**
     * @protected
     */
    TagHandler.prototype.getToggleElement = function() {
        return this.widget.domNode.querySelector('#' + this.widgetId + '-accessory-tag');
    };

    TagHandler.prototype.hideTagSelection = function() {
        var toggleElem, contentElem;
        if (this.tagSelectionShown) {
            toggleElem = this.getToggleElement();
            contentElem = this.widget.domNode.querySelector('.cn-write-note-accessory-tag');
            if (contentElem && toggleElem) {
                toggleElem.style.display = '';
                contentElem.style.display = 'none';
                this.tagSelectionShown = false;
            }
        }
    };
    
    /**
     * Implementation of the NoteEditorComponent method which initializes the data managed by this
     * component.
     * 
     * @param {?Object} noteData Object with details about the note to initialize with.
     */
    TagHandler.prototype.initContent = function(noteData) {
        this.removeTag(null);
        if (noteData) {
            this.addTags(noteData.tags);
        }
        this.showHideTagSelection();
        this.dirty = false;
        this.modified = false;

    };

    /**
     * Implementation of the NoteEditorComponent method which tests whether the data managed by this
     * component is dirty.
     * 
     * @return {boolean} true if tags were removed or added after the dirty state had been reset
     */
    TagHandler.prototype.isDirty = function() {
        return this.dirty;
    };

    /**
     * Implementation of the NoteEditorComponent method which tests whether the data managed by this
     * component has been modified.
     * 
     * @return {boolean} true if tags were removed or added after the content had been initialized
     */
    TagHandler.prototype.isModified = function() {
        return this.modified;
    };
    
    TagHandler.prototype.onRenderStyleChanged = function(changeDescriptor) {
        this.canShowTagSelection = changeDescriptor.newStyle === 'full';
        this.showHideTagSelection();
    };
    
    TagHandler.prototype.onTargetTopicChanged = function(changeDescr) {
        this.targetTopicId = changeDescr.newId;
        // invalidate cache of autocompeter so it restarts a query for the same term
        // after topic (request parameter) changed
        if (this.tagAutocompleter) {
            this.tagAutocompleter.resetQuery(false);
        }
    };

    TagHandler.prototype.onWidgetRefreshed = function() {
        var acOptions, tagAutocompleterMultipleMode, toggleElem;
        var inputElem = this.getTagInputElement();
        this.tagTextboxList = new communote.classes.TextboxList(inputElem, {
            addItemOnChar: ',',
            // kind of hacky but is easiest way to avoid duplicates and showing the tag twice in the list
            autoAddItemCallback: this.addTag.bind(this),
            autoRemoveItemCallback: this.removeTag.bind(this),
            listCssClass: 'cn-border',
            itemRemoveCssClass: 'textboxlist-item-remove cn-icon',
            itemRemoveTitle: i18n.getMessage('blogpost.create.tags.remove.tooltip'),
            parseItemCallback: extractTagName
        });
        // attach autocompleter
        acOptions = this.prepareAutocompleterOptions();
        // no multiple tag autocompletion since the TextboxList is taking care of this
        tagAutocompleterMultipleMode = false;
        this.tagAutocompleter = autocompleterFactory.createTagAutocompleter(inputElem, acOptions,
        		null, 'NOTE', false, tagAutocompleterMultipleMode);
        this.tagAutocompleter.addEvent('onChoiceSelected', tagChoiceSelected.bind(this));
        this.tagAutocompleter.addEvent('enterPressed', tagInputEnterPressed.bind(this));
        toggleElem = this.getToggleElement();
        if (toggleElem) {
            toggleElem.addEventListener('click', this.showTagSelection.bind(this, true));
        }
        this.tagSelectionShown = false;
    };

    /**
     * @protected
     */
    TagHandler.prototype.prepareAutocompleterOptions = function() {
        var positionSource = this.widget.domNode
                .querySelector('.cn-write-note-accessory-tag .cn-border');
        var preparedOptions = {};
        preparedOptions.autocompleterOptions = {};
        if (positionSource) {
            preparedOptions.inputFieldOptions = {
                positionSource: positionSource
            };
        }
        preparedOptions.autocompleterOptions.clearInputOnSelection = true;
        preparedOptions.dataSourceOptions = {};
        preparedOptions.dataSourceOptions.beforeRequestCallback = setCurrentTopicBeforeRequestCallback.bind(this);
        preparedOptions.autocompleterOptions.unfocusInputOnSelection = false;
        preparedOptions.suggestionsOptions = {};
        preparedOptions.suggestionsOptions['categories'] = this.tagAutocompleterCategories;
        return preparedOptions;
    };

    /**
     * Remove one or all tags from the addedTags array. If the array was changed tagRemoved will be
     * called.
     * 
     * @param {(Object|String)} tag The tag to remove or null to remove all tags
     */
    TagHandler.prototype.removeTag = function(tag) {
        var modified, index, inputElem;
        if (tag == null) {
            if (this.addedTags.length > 0) {
                this.addedTags = [];
                modified = true;
            }
            // clear input
            inputElem = this.getTagInputElement();
            if (inputElem) {
            	inputElem.value = '';
            }
        } else {
            tag = convertToTagObject(tag);
            index = getIndexOfTag(tag, this.addedTags);
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
    };

    TagHandler.prototype.showHideTagSelection = function() {
        if (this.canShowTagSelection && this.addedTags.length) {
            this.showTagSelection(false);
        } else {
            this.hideTagSelection();
        }
    };
    
    TagHandler.prototype.showTagSelection = function(focusInput) {
        var toggleElem, contentElem;
        if (!this.tagSelectionShown) {
            toggleElem = this.getToggleElement();
            contentElem = this.widget.domNode.querySelector('.cn-write-note-accessory-tag');
            if (contentElem && toggleElem) {
                toggleElem.style.display = 'none';
                contentElem.style.display = '';
                this.tagSelectionShown = true;
            }
            // resize since it was not visible before and therefore the size is not correct
            this.tagTextboxList.resizeInputToFillAvailableSpace();
            if (focusInput) {
                this.tagTextboxList.focusInput();
            }
        }
    };

    /**
     * Called when a tag was added for instance by selecting it from the autocompleter or submitting
     * the tag input field.
     * 
     * @param {Object} tag The tag that was added
     * 
     * @protected
     */
    TagHandler.prototype.tagAdded = function(tag) {
        this.tagTextboxList.addItem(tag);
    };
    /**
     * @protected
     */
    TagHandler.prototype.tagRemoved = function(tag) {
        if (tag == null) {
            this.tagTextboxList.clearItems();
        } else {
            this.tagTextboxList.removeItem(tag);
        }
    };
    // publish class to allow subclassing or modification by add-ons
    communote.classes.NoteEditorTags = TagHandler;
    // register for all modes
    communote.NoteEditorComponentFactory.register('*', TagHandler);
})(this);