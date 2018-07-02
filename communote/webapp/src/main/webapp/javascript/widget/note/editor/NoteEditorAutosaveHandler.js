(function(window) {
    var communote = window.communote;
    var utils = communote.utils;
    var i18n = communote.i18n;

    function convertAutosaveCookieToJson(autosaveCookie) {
        var blogs, tags, autosaveNoteId, i;
        var note = {};
        note.content = autosaveCookie.get('content');
        note.attachments = autosaveCookie.get('attachs');
        note.usersToNotify = autosaveCookie.get('users');
        note.targetBlog = autosaveCookie.get('targetBlog');
        note.properties = autosaveCookie.get('properties');
        blogs = autosaveCookie.get('blogs');
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
        tags = autosaveCookie.get('tags');
        // old tags where a string, ignore them
        if (typeOf(tags) === 'array') {
            note.tags = tags;
        }
        note.isAutosave = true;
        // decrement version because init code increments it again and it should not grow when doing
        // an offline autosave because online autosave should be treated as newer
        note.autosaveVersion = autosaveCookie.get('autosaveVersion').toInt() - 1;
        autosaveNoteId = autosaveCookie.get('autosaveNoteId');
        if (autosaveNoteId) {
            note.autosaveNoteId = autosaveNoteId.toInt();
        }
        return note;
    }

    /**
     * Called to delete the offline autosave cookie, for instance if the user discarded the autosave
     * or the note was published. Will even be called if there is no cookie. There won't be a cookie
     * if autosave feature or the cookies are disabled.
     */
    function deleteAutosaveCookie(cookie) {
        if (cookie) {
            Cookie.dispose(cookie.key);
            cookie.empty();
        }
    }

    /**
     * Callback to invoke after the request which deletes the online autosave completed. The
     * autosave handler instance needs to be bound.
     * 
     * @param {Function} [discardCompleteCallback] callback to invoke
     * @param {Object} jsonObj JSON response returned by the request
     */
    function discardOnlineAutosaveComplete(discardCompleteCallback, jsonObj) {
        if (jsonObj && jsonObj.status == 'ERROR') {
            this.updateFeedbackMessage(jsonObj.message, false);
        } else if (typeof discardCompleteCallback === 'function') {
            this.resetState();
            discardCompleteCallback.call();
        }
    }

    function initAutosaveCookie(action, noteId) {
        // make cookie user-unique
        var cookieName = 'autosave_' + action + '_u' + communote.currentUser.id;
        if (noteId) {
            cookieName = cookieName + '_' + noteId;
        }
        // cookie is set to be valid for 7 days
        var cookie = new Hash.Cookie(cookieName, {
            duration: 7,
            autoSave: false
        });
        cookie.load();
        return cookie;
    }

    function onlineAutosaveComplete(response) {
        this.runningAutosaveRequest = false;
        if (response && response.result) {
            this.saveComplete(true);
            // TODO api returns only noteId and no details like version, can only blindly
            // increment autosave version
            // using REST API 3.0 whose result object is the ID of the autosave
            this.autosaveNoteId = response.result;
            this.autosaveVersion++;
        }
    }

    function onlineAutosaveFailed() {
        // TODO anything else to do here?
        this.runningAutosaveRequest = false;
    }

    function saveIfDirty() {
        if (this.widget.isDirty()) {
            this.save();
        }
    }

    function storeInAutosaveCookie(cookie, noteData, action) {
        cookie.empty();
        if (noteData.tags.length > 0) {
            cookie.set('tags', noteData.tags);
        }
        if (noteData.attachments.length > 0) {
            cookie.set('attachs', noteData.attachments);
        }
        if (noteData.crosspostTopics && noteData.crosspostTopics.length > 0) {
            cookie.set('blogs', noteData.crosspostTopics);
        }
        if (noteData.usersToNotify.length > 0) {
            cookie.set('users', noteData.usersToNotify);
        }
        if (noteData.properties) {
            cookie.set('properties', noteData.properties);
        }
        cookie.set('autosaveVersion', noteData.noteVersion);
        cookie.set('content', noteData.content);
        cookie.set('isDirectMessage', noteData.isDirectMessage);
        if (noteData.autosaveNoteId != null) {
            cookie.set('autosaveNoteId', noteData.autosaveNoteId);
        }
        // save targetBlog when in create mode to allow restoring it, even if unset
        if (action === 'create' || action === 'repost') {
            cookie.set('targetBlog', noteData.targetTopic);
        }
        // fails if too large (>4kb)
        return cookie.save();
    }

    /**
     * Creates the autosave handler.
     * 
     * @class
     */
    function AutosaveHandler(noteEditorWidget, action, noteId, options) {
        options = options || {};
        this.widget = noteEditorWidget;
        this.action = action;
        this.autosaveCookie = initAutosaveCookie(action, noteId);
        this.autosaveVersion = 0;
        this.autosaveNoteId = null;
        this.autosaveCreated = false;
        this.autosaveLoaded = false;
        this.runningAutosaveRequest = false;
        // default timeout: 10s
        this.autosaveTimeout = options.autosaveTimeout || 10000;
        this.autosaveJobId = null;
        this.boundOnlineAutosaveErrorCallback = onlineAutosaveFailed.bind(this);
        this.boundOnlineAutosaveSuccessCallback = onlineAutosaveComplete.bind(this);
        this.feedbackElementSelector = options.feedbackElementSelector || '.cn-write-note-status';
        this.renderDiscardLink = options.renderDiscardLink || false;
        this.defaultDiscardCompleteCallback = options.defaultDiscardCompleteCallback;
        this.discardAutosaveUrl = buildRequestUrl('/blog/deletePost.do')
                + '?action=deleteAutosave&noteId=';
    };

    /**
     * Discard the autosave if one was loaded or created. Stops the automatic saving.
     * 
     * @param {Function} [discardCompleteCallback] A function to run after successfully removing the
     *            autosave. If null or undefined, the configured defaultDiscardCompleteCallback is
     *            invoked. If no operation should be executed pass false.
     */
    AutosaveHandler.prototype.discard = function(discardCompleteCallback) {
        var url, request, i;
        // default post remove operation: reset editor
        if (discardCompleteCallback == null) {
            discardCompleteCallback = this.defaultDiscardCompleteCallback;
        }
        // TODO block if there are running uploads?
        this.stopAutomaticSave();
        deleteAutosaveCookie(this.autosaveCookie);
        // TODO fire autosaveDiscarded event (with flag denoting whether online) before calling completeCallback? 
        if (this.autosaveNoteId != null) {
            url = this.discardAutosaveUrl + this.autosaveNoteId;
            request = new Request.JSON({
                url: url,
                method: 'get', // TODO use POST!
                noCache: true
            });
            request.addEvent('complete', discardOnlineAutosaveComplete.bind(this,
                    discardCompleteCallback));
            request.send();
        } else {
            this.resetState();
            if (typeof discardCompleteCallback === 'function') {
                discardCompleteCallback.call();
            }
        }
    };

    /**
     * Shows a confirm message dialog asking the user whether he want's to discard the autosave. If
     * the user confirms the deletion #discard(discardCompleteCallback) will be invoked.
     * 
     * @param {Event} event The DOM event which triggered the invocation of this method
     * @param {Function} [discardCompleteCallback] A function to run after successfully removing the
     *            autosave. See #discard(discardCompleteCallback) for details.
     */
    AutosaveHandler.prototype.discardWithConfirm = function(event, discardCompleteCallback) {
        var title = i18n.getMessage('create.note.autosave.discard.title');
        var message = i18n.getMessage('create.note.autosave.discard.question');
        this.stopAutomaticSave();
        showConfirmDialog(title, message, this.discard.bind(this, discardCompleteCallback), {
            triggeringEvent: event,
            onCloseCallback: this.startAutomaticSave.bind(this)
        });
    };

    AutosaveHandler.prototype.editorInitialized = function() {
        if (this.autosaveLoaded) {
            this.updateFeedbackMessage(i18n.getMessage('blogpost.autosave.loaded'),
                    this.renderDiscardLink);
        }
    };

    AutosaveHandler.prototype.getNoteId = function() {
        return this.autosaveNoteId;
    };

    AutosaveHandler.prototype.getVersion = function() {
        return this.autosaveVersion;
    };

    /**
     * @return {boolean} whether there is an autosave, no matter if it was loaded or created later
     *         on. The autosave can be stored online and/or offline.
     */
    AutosaveHandler.prototype.hasAutosave = function() {
        return this.autosaveLoaded || this.autosaveCreated;
    };

    /**
     * @return {boolean} whether there is an online autosave, no matter if it was loaded or created
     *         later on
     */
    AutosaveHandler.prototype.hasOnlineAutosave = function() {
        return this.autosaveNoteId != null;
    };

    AutosaveHandler.prototype.isAutosaveInProgress = function() {
        return this.runningAutosaveRequest;
    };

    /**
     * Loads an autosave from an offline storage or extracts an online autoasave from the provided
     * responseMetadata. If there is an online and offline autosave the newer is returned.
     * 
     * @param {?Object} responseMetadata Object that was returned from the server when the widget
     *            was refreshed
     * @param {?Object} responseMetadata.autosave Object holding the details of the autosave
     * @return {Object} Object with object member noteData holding the details of the autosave and
     *         boolean member online which denotes whether the autosave is an online autosave. If no
     *         autosave was found, null is returend.
     */
    AutosaveHandler.prototype.load = function(responseMetadata) {
        var loadedAutosave, isOnlineAutosave;
        var offlineAutosaveVersion = this.autosaveCookie.get('autosaveVersion');
        var onlineAutosave = responseMetadata && responseMetadata.autosave;
        if (onlineAutosave) {
            // use offline autosave only if it is newer
            if (offlineAutosaveVersion > onlineAutosave.autosaveVersion) {
                loadedAutosave = convertAutosaveCookieToJson(this.autosaveCookie);
            } else {
                loadedAutosave = onlineAutosave
                isOnlineAutosave = true;
            }
        } else if (offlineAutosaveVersion != null) {
            loadedAutosave = convertAutosaveCookieToJson(this.autosaveCookie);
        }
        if (loadedAutosave) {
            // increment autosave version so that next saved draft is newer
            this.autosaveVersion = loadedAutosave.autosaveVersion + 1;
            this.autosaveNoteId = loadedAutosave.autosaveNoteId;
            this.autosaveLoaded = true;
            return {
                noteData: loadedAutosave,
                online: !!isOnlineAutosave
            };
        }
        return null;
    };

    AutosaveHandler.prototype.notePublished = function() {
        deleteAutosaveCookie(this.autosaveCookie);
        this.resetState();
    };

    /**
     * Reset the internal state
     * 
     * @private
     */
    AutosaveHandler.prototype.resetState = function() {
        this.autosaveVersion = 0;
        this.autosaveNoteId = null;
        this.autosaveCreated = false;
        this.autosaveLoaded = false;
        this.updateFeedbackMessage(null);
    };

    AutosaveHandler.prototype.save = function() {
        var noteData;
        var skipOnlineSave = this.runningAutosaveRequest;
        this.updateFeedbackMessage(i18n.getMessage('blogpost.autosave.saving'), false);
        noteData = this.widget.getNoteData(true);
        noteData.autosaveNoteId = this.autosaveNoteId;
        noteData.noteVersion = this.autosaveVersion;
        if (skipOnlineSave) {
            // increase version if previous online autosave is still running and current online
            // autosave is skipped -> offline autosave will be newer than previous online autosave
            noteData.noteVersion++;
        }
        if (storeInAutosaveCookie(this.autosaveCookie, noteData, this.action)) {
            this.saveComplete(false);
        }
        if (!skipOnlineSave) {
            // won't succeed if empty or no topic is selected
            if (noteData.content == null
                    || (noteData.content.length === 0)
                    || ((this.action === 'create' || this.action === 'repost') && noteData.targetTopic == null)) {
                return;
            }
            // only necessary for online autosaves because they are async
            this.runningAutosaveRequest = true;
            noteData = this.widget.getNoteDataForRestRequest(false, noteData.content);
            if (this.action === 'edit') {
                utils.noteUtils.updateNote(noteData.noteId, noteData,
                        this.boundOnlineAutosaveSuccessCallback,
                        this.boundOnlineAutosaveErrorCallback);
            } else {
                utils.noteUtils.createNote(noteData, this.boundOnlineAutosaveSuccessCallback,
                        this.boundOnlineAutosaveErrorCallback);
            }
        }
    };

    /**
     * Is called after an autosave has been created.
     * 
     * @param {boolean} onlineAutosave True if the autosave was created online, false if it was
     *            stored offline.
     * 
     * @private
     */
    AutosaveHandler.prototype.saveComplete = function(onlineAutosave) {
        this.updateFeedbackMessage(i18n.getMessage('blogpost.autosave.saved') + " "
                + localizedDateFormatter.format(new Date()), this.renderDiscardLink);
        this.autosaveCreated = true;
        // TODO fire event to notify widget and any interested listener
    };
    /**
     * Starts the autosave draft job.
     */
    AutosaveHandler.prototype.startAutomaticSave = function() {
        if (this.autosaveJobId != null) {
            // already running
            return;
        }
        // disable autosave functionality if interval is too small
        if (this.autosaveTimeout < 1000) {
            return;
        }
        this.autosaveJobId = setInterval(saveIfDirty.bind(this), this.autosaveTimeout);
    };

    /**
     * Stops the autosave job.
     */
    AutosaveHandler.prototype.stopAutomaticSave = function() {
        if (this.autosaveJobId != null) {
            clearInterval(this.autosaveJobId);
            this.autosaveJobId = null;
        }
    };

    /**
     * Update the feedback element with the given message. The message is injected into the DOM
     * subtree of the widget into an element which can be selected with configurable
     * feedbackElementSelector. If there is no such element this method does nothing.
     * 
     * @param {?string} message The message to show. If null the current message is removed.
     * @param {boolean} renderDiscardLink Whether to render an action link for discarding the
     *            autosave.
     * 
     * @private
     */
    AutosaveHandler.prototype.updateFeedbackMessage = function(message, renderDiscardLink) {
        var htmlData;
        var feedbackElement = this.widget.domNode.getElement(this.feedbackElementSelector);
        if (!feedbackElement) {
            return;
        }
        if (message) {
            htmlData = '<span class="cn-note-autosave-message">' + message + '</span>';
            if (renderDiscardLink) {
                htmlData += '<a class="cn-icon cn-cancel" href="javascript:;" title="';
                htmlData += i18n.getMessage('blogpost.autosave.discard') + '"></a>';
            }
            htmlData += '<span class="cn-clear"><!-- --></span>';

            feedbackElement.set('html', htmlData);
            if (renderDiscardLink) {
                feedbackElement.getElement('a.cn-cancel').addEvent('click',
                        this.discardWithConfirm.bind(this));
            }
        } else {
            feedbackElement.set('html', '');
        }
    };

    communote.classes.NoteEditorAutosaveHandler = AutosaveHandler;
})(this);