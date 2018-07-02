(function(window) {
    const communote = window.communote;
    const i18n = communote.i18n;

    function addUser(handler, userData) {
        var idx = handler.userAliases.indexOf(userData.alias);
        if (idx < 0) {
            handler.userAliases.push(userData.alias);
            handler.userData.push(userData);
            return true;
        }
        return false;
    }
    
    function appendResendNotificationProperty(noteData, property) {
        if (property) {
            if (!noteData.properties) {
                noteData.properties = [];
            }
            communote.utils.propertyUtils.mergeProperty(noteData.properties, property);
        }
    }

    /**
     * Implementation of the 'approveMatchCallback' callback of the AutocompleterStaticDataSource
     * that excludes the
     * 
     * @@discussion suggestion when not used in a discussion context.
     */
    function checkDiscussionContext(query, staticSearchDefinition) {
        if (staticSearchDefinition.inputValue !== '@discussion') {
            return true;
        }
        // @@discussion should only be matched when in a discussion context: ignore create/repost
        //  and editing a note which is not part of a discussion with more than one note
        if (this.action === 'create'
                || this.action === 'repost'
                || (this.action === 'edit' && this.widget.initialNote.numberOfDiscussionNotes === 1)) {
            return false;
        }
        return true;
    }

    function delegateCheckboxClick(delegateFunction, event) {
        delegateFunction.call(this, !!event.currentTarget.checked);
    }

    function enableDisableDmCheckbox(handler) {
        var disable, checkboxElem;
        // disable the checkbox if crosspost topics were added or no user is selected; Disable if editing a
        // DM because converting to non DM is not allowed.
        if (handler.topicCount > 0 || handler.userAliases.length === 0
                || handler.isDirectMessage && handler.action === 'edit') {
            disable = true;
        } else {
            disable = false;
        }
        if (disable != handler.dmCheckboxDisabled) {
            handler.dmCheckboxDisabled = disable;
            checkboxElem = handler.getDirectMessageCheckboxElement();
            checkboxElem.disabled = disable;
        }
    }

    function extractUserName(userData) {
        return userData.longName;
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
    function userChoiceSelected(inputElem, choiceElem, token, value) {
        this.addUsers(token);
    }

    /**
     * Create a NoteEditorComponent which allows adding mentions to a note.
     * 
     * @param noteEditorWidget The note editor widget
     * @param {String} action The action/mode of the widget
     * @param {String} initialRenderStyle The initial render style the widget was created with.
     * @param {Object} options The settings (staticParameters) the widget was created and
     *            initialized with.
     * 
     * @class
     */
    function MentionHandler(noteEditorWidget, action, initialRenderStyle, options) {
        this.widget = noteEditorWidget;
        this.widgetId = noteEditorWidget.widgetId;
        this.action = action;
        this.mentionsContainerElem = null;
        this.userTextboxList = null;
        this.userAutocompleter = null;
        this.canShowMentionSelection = initialRenderStyle === 'full';
        this.mentionSelectionShown = false;
        // lookup for the aliases
        this.userAliases = [];
        // user data with all details. The alias of each entry can be found in userAliases at the same index.
        this.userData = [];
        this.targetTopicId = null;
        this.dirty = false;
        this.modified = false;
        this.isDirectMessage = false;
        this.dmCheckboxDisabled = true;
        this.resendNotificationProperty = null;
        this.topicCount = 0;
        noteEditorWidget.addEventListener('widgetRefreshed', this.onWidgetRefreshed, this);
        noteEditorWidget.addEventListener('renderStyleChanged', this.onRenderStyleChanged, this);
        noteEditorWidget.addEventListener('targetTopicChanged', this.onTargetTopicChanged, this);
        noteEditorWidget.addEventListener('topicAdded', this.onTopicAdded, this);
        noteEditorWidget.addEventListener('topicRemoved', this.onTopicRemoved, this);
        noteEditorWidget.addEventListener('widgetRefreshing', this.cleanup, this);
        noteEditorWidget.addEventListener('widgetRemoving', this.cleanup, this);
    }

    /**
     * Adds users for notification.
     * 
     * @param {Object|Object[]) users An object containing user data or an array of such objects.
     * @return whether something was added
     */
    MentionHandler.prototype.addUsers = function(users) {
        var i, l, addedUserData;
        if (!users) {
            return;
        }
        if (Array.isArray(users)) {
            addedUserData = [];
            for (i = 0, l = users.length; i < l; i++) {
                // update FE as soon as we know which were actually new
                if (addUser(this, users[i])) {
                    addedUserData.push(users[i]);
                }
            }
            for (i = 0, l = addedUserData.length; i < l; i++) {
                this.userAdded(addedUserData[i], i < l - 1);
            }
        } else {
            if (addUser(this, users)) {
                this.userAdded(users, false);
            }
        }
    };
    /**
     * Implementation of the NoteEditorComponent method which appends the data of this component.
     * 
     * @param {Object} noteData Object for adding the data to
     * @param {boolean} resetDirtyState Whether to set the internal dirty state to not-dirty.
     */
    MentionHandler.prototype.appendNoteData = function(noteData, resetDirtyState) {
        noteData.usersToNotify = this.userData;
        noteData.isDirectMessage = this.isDirectMessage;
        if (this.action === 'edit') {
            appendResendNotificationProperty(noteData, this.resendNotificationProperty);
        }
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
    MentionHandler.prototype.appendNoteDataForRestRequest = function(noteData, publish,
            resetDirtyState) {
        noteData.usersToNotify = this.userAliases;
        if (this.action !== 'edit') {
            // TODO does REST API complain if DM flag is sent when editing a note?
            noteData.isDirectMessage = this.isDirectMessage;
        } else {
            appendResendNotificationProperty(noteData, this.resendNotificationProperty); 
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
    MentionHandler.prototype.canPublishNote = function() {
        return true;
    };

    /**
     * @protected
     */
    MentionHandler.prototype.changeDirectMessageMode = function(activate) {
        if (this.isDirectMessage === activate) {
            return;
        }
        if (activate) {
            // activate only possible if crosspost blogs are empty
            // and at least one user was added
            // TODO show an error if not activatable?
            if (this.topicCount < 1 && this.userAliases.length > 0) {
                this.isDirectMessage = true;
            }
        } else {
            this.isDirectMessage = false;
        }
        if (this.isDirectMessage === activate) {
            this.directMessageModeChanged();
        }
    };

    /**
     * Define if already notified user should receive notifications again
     * 
     * @protected
     */
    MentionHandler.prototype.changeResendNotificationMode = function(activate) {
        this.resendNotificationProperty = {
            'key': 'editNote.resendNotification',
            'keyGroup': 'com.communote',
            'value': activate
        };
        communoteLocalStorage.setItem('com.communote.editNote.resendNotification', activate);
    };

    /**
     * Cleanup any resources to avoid memory leaks
     * 
     * @protected
     */
    MentionHandler.prototype.cleanup = function() {
        if (this.userAutocompleter) {
            this.userAutocompleter.destroy();
        }
        if (this.userTextboxList) {
            this.userTextboxList.destroy();
        }
    };
    
    /**
     * Called by changeDirectMessageMode if the new mode could be set.
     * 
     * @protected
     */
    MentionHandler.prototype.directMessageModeChanged = function() {
        // update view, if necessary (e.g. reset after submit)
        var checkboxElem = this.getDirectMessageCheckboxElement();
        if (checkboxElem.checked != this.isDirectMessage) {
            checkboxElem.checked = this.isDirectMessage;
        }
        // fire event via widget to notify other components
        this.widget.emitEvent('directMessageModeChanged', this.isDirectMessage);
    };

    /**
     * @protected
     */
    MentionHandler.prototype.getDirectMessageCheckboxElement = function() {
        return this.mentionsContainerElem.querySelector('#' + this.widgetId + '-direct-message');
    };

    /**
     * @protected
     */
    MentionHandler.prototype.getToggleElement = function() {
        return this.widget.domNode.querySelector('#' + this.widgetId + '-accessory-user');
    };

    MentionHandler.prototype.getUnconfirmedInputWarning = function() {
        var userInputElem = this.getUserInputElement();
        if (userInputElem && userInputElem.value.trim().length) {
        	return {
        		message: i18n.getMessage('blogpost.create.submit.confirm.unsaved.user'),
        		inputName: i18n.getMessage('widget.createNote.mentions.unconfirmed.input.inputName')
        	};
        }
    };

    /**
     * @protected
     */
    MentionHandler.prototype.getUserInputElement = function() {
        return this.mentionsContainerElem.querySelector('#' + this.widgetId + '-user-search');
    };
    
    MentionHandler.prototype.hideMentionSelection = function() {
        var toggleElem;
        if (this.mentionSelectionShown) {
            toggleElem = this.getToggleElement();
            if (this.mentionsContainerElem && toggleElem) {
                toggleElem.style.display = '';
                this.mentionsContainerElem.style.display = 'none';
                this.mentionSelectionShown = false;
            }
        }
    };

    /**
     * Implementation of the NoteEditorComponent method which initializes the data managed by this
     * component.
     * 
     * @param {?Object} noteData Object with details about the note to initialize with.
     */
    MentionHandler.prototype.initContent = function(noteData) {
        var dmWrapperElem;
        this.changeDirectMessageMode(false);
        this.removeUser(null);
        if (noteData) {
            this.addUsers(noteData.usersToNotify);
            this.changeDirectMessageMode(noteData.isDirectMessage);
        }
        if (this.action === 'edit' && !this.isDirectMessage) {
            // hide DM feature when editing a normal note
            dmWrapperElem = document.getElementById(this.widgetId + '-direct-message-wrapper');
            if (dmWrapperElem) {
                dmWrapperElem.style.display = 'none';
            }
        } else {
            // detach from current thread of execution because enabling/disabling the DM checkbox
            // can depend on other components (crosspost topics) and there is no guaranteed order
            // in which initContent is called
            setTimeout(enableDisableDmCheckbox.bind(null, this), 1);
        }
        this.showHideMentionSelection();
        this.dirty = false;
        this.modified = false;
    };

    /**
     * Implementation of the NoteEditorComponent method which tests whether the data managed by this
     * component is dirty.
     * 
     * @return {boolean} true if mentions were removed or added after the dirty state had been reset
     */
    MentionHandler.prototype.isDirty = function() {
        return this.dirty;
    };

    /**
     * Implementation of the NoteEditorComponent method which tests whether the data managed by this
     * component has been modified.
     * 
     * @return {boolean} true if mentions were removed or added after the content had been initialized
     */
    MentionHandler.prototype.isModified = function() {
        return this.modified;
    };

    MentionHandler.prototype.onRenderStyleChanged = function(changeDescriptor) {
        this.canShowMentionSelection = changeDescriptor.newStyle === 'full';
        this.showHideMentionSelection();
    };
    
    MentionHandler.prototype.onTargetTopicChanged = function(newTargetTopic) {
        this.targetTopicId = newTargetTopic && newTargetTopic.id;
        // invalidate cache of autocompeter so it restarts a query for the same term
        // after topic (request parameter) changed
        if (this.userAutocompleter) {
            this.userAutocompleter.resetQuery(false);
        }
    };

    MentionHandler.prototype.onTopicAdded = function(topicData) {
    	this.topicCount++;
    	enableDisableDmCheckbox(this);
    };
    MentionHandler.prototype.onTopicRemoved = function(topicData) {
    	if (topicData) {
    		this.topicCount--;
    	} else {
    		// all removed
    		this.topicCount = 0;
    	}
    	enableDisableDmCheckbox(this);
    };
    
    /**
     * @protected
     */
    MentionHandler.prototype.onWidgetRefreshed = function() {
        var inputElem, toggleElem, acOptions, checkboxElem, resendNotification;
        this.mentionsContainerElem = this.widget.domNode
                .querySelector('.cn-write-note-accessory-user');
        inputElem = this.getUserInputElement();
        this.userTextboxList = new communote.classes.TextboxList(inputElem, {
            autoRemoveItemCallback: this.removeUser.bind(this),
            listCssClass: 'cn-border',
            itemRemoveCssClass: 'textboxlist-item-remove cn-icon',
            itemRemoveTitle: i18n.getMessage('blogpost.create.users.remove.tooltip'),
            parseItemCallback: extractUserName
        });
        acOptions = this.prepareAutocompleterOptions();
        this.userAutocompleter = autocompleterFactory.createMentionAutocompleter(inputElem,
                acOptions, null, true);
        this.userAutocompleter.addEvent('onChoiceSelected', userChoiceSelected.bind(this));
        toggleElem = this.getToggleElement();
        if (toggleElem) {
            toggleElem.addEventListener('click', this.showMentionSelection.bind(this, true));
        }
        this.mentionSelectionShown = false;
        checkboxElem = this.getDirectMessageCheckboxElement();
        checkboxElem.addEventListener('click', delegateCheckboxClick.bind(this,
                this.changeDirectMessageMode));
        if (this.action === 'edit') {
            checkboxElem = this.mentionsContainerElem.querySelector('#' + this.widgetId
                    + '-resend-notification');
            checkboxElem.addEventListener('click', delegateCheckboxClick.bind(this,
                    this.changeResendNotificationMode));
            // Load the latest setting for the resend notification option via Local Storage
            resendNotification = communoteLocalStorage
                    .getItem('com.communote.editNote.resendNotification') === 'true';
            if (resendNotification) {
                checkboxElem.checked = true;
            }
            this.changeResendNotificationMode(resendNotification);
        }
    };

    /**
     * @protected
     */
    MentionHandler.prototype.prepareAutocompleterOptions = function() {
        var positionSource = this.mentionsContainerElem.querySelector('.cn-border');
        var preparedOptions = {};
        preparedOptions.autocompleterOptions = {};
        if (positionSource) {
            preparedOptions.inputFieldOptions = {
                positionSource: positionSource
            };
        }
        preparedOptions.autocompleterOptions.clearInputOnSelection = true;
        preparedOptions.dataSourceOptions = {};
        preparedOptions.dataSourceOptions.beforeRequestCallback = setCurrentTopicBeforeRequestCallback
                .bind(this);
        // do not remove focus when selecting because the user will usually add more than one user
        preparedOptions.autocompleterOptions.unfocusInputOnSelection = false;
        preparedOptions.staticDataSourceOptions = {};
        preparedOptions.staticDataSourceOptions.approveMatchCallback = checkDiscussionContext
                .bind(this);
        return preparedOptions;
    };

    /**
     * Remove a previously added user from the users to notify.
     * 
     * @param {Object} [userData] the object holding the details of the user to remove, if not
     *            specified all users are removed
     */
    MentionHandler.prototype.removeUser = function(userData) {
        var idx, inputElem;
        var alias = userData && userData.alias;
        if (alias && this.isDirectMessage && this.userAliases.length === 1
                && this.userAliases[0] === alias) {
            // do not allow removing the last user if we are in DM mode
            showNotification(NOTIFICATION_BOX_TYPES.failure, '', i18n
                    .getMessage('error.blogpost.edit.remove-direct-user'), null);
            return;
        }
        if (userData == null) {
            if (this.userAliases.length) {
                this.userAliases = [];
                this.userData = [];
                this.userRemoved(null);
            }
            // clear input
            inputElem = this.getUserInputElement();
            if (inputElem) {
                inputElem.value = '';
            }
        } else {
            idx = this.userAliases.indexOf(alias);
            if (idx > -1) {
                this.userAliases.splice(idx, 1);
                this.userData.splice(idx, 1);
                this.userRemoved(userData);
            }
        }
    };

    MentionHandler.prototype.showHideMentionSelection = function() {
        if (this.canShowMentionSelection && this.userAliases.length) {
            this.showMentionSelection(false);
        } else {
            this.hideMentionSelection();
        }
    };
    
    MentionHandler.prototype.showMentionSelection = function(focusInput) {
        var toggleElem, contentElem;
        if (!this.mentionSelectionShown) {
            toggleElem = this.getToggleElement();
            if (this.mentionsContainerElem && toggleElem) {
                toggleElem.style.display = 'none';
                this.mentionsContainerElem.style.display = '';
                this.mentionSelectionShown = true;
            }
            // resize since it was not visible before and therefore the size is not correct
            this.userTextboxList.resizeInputToFillAvailableSpace();
            if (focusInput) {
                this.userTextboxList.focusInput();
            }
        }
    };

    /**
     * Called after a user was added.
     * 
     * @param {Object} userData JSON object describing the added user
     * @param {boolean} moreToCome true if the user was added as part of batch update and the
     *            current item is not the last. Can be used to optimize view updates.
     * 
     * @protected
     */
    MentionHandler.prototype.userAdded = function(userData, moreToCome) {
        this.userTextboxList.addItem(userData);
        if (!moreToCome) {
            enableDisableDmCheckbox(this);
        }
        this.dirty = true;
        this.modified = true;
    };

    MentionHandler.prototype.userRemoved = function(userData) {
        if (!userData) {
            this.userTextboxList.clearItems();
        } else {
            this.userTextboxList.removeItem(userData);
        }
        enableDisableDmCheckbox(this);
        this.dirty = true;
        this.modified = true;
    };

    // publish class to allow subclassing or modification by plugins
    communote.classes.NoteEditorMentions = MentionHandler;
    // register for all modes
    communote.NoteEditorComponentFactory.register('*', MentionHandler);
})(this);