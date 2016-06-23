(function() {
    var moveDiscussion = {
        apiAccessor: undefined,
        discussionId: undefined,
        placeholders: undefined,
        srcTopicId: undefined,
        targetTopicId: undefined,
        topicAutocompleter: undefined,
        topicTextboxList: undefined,

        showDialog: function(discussionId, currentTopicId, dialogOptions) {
            var buttons, htmlElement;
            var title = getJSMessage('note.move-discussion.title');
            var html = '<form id="moveDiscussionForm" not_exposed_to_spring_ts_widget="true" class="cn-form-container">';
            html += '<div class="cn-smallline">';
            html += '<label class="cn-label">' + getJSMessage('note.move-discussion.selected');
            html += ' <span class="tooltip-wrapper"><a class="tooltip" rel="'
                    + getJSMessage('note.move-discussion.description') + '">[?]</a></span></label>';
            html += '<div id="moveTargetBlogSearchContainer">';
            html += '<input type="text" id="moveTargetTopicSearchInput" name="moveTargetTopicSearchInput" placeholder="'
                    + getJSMessage('note.move-discussion.selected.none') + '" />';
            html += '</div>';
            html += '</div></form>';

            htmlElement = new Element('div', {
                'id': 'move-discussion-dialog-content',
                'html': html
            });
            init_tips(htmlElement);
            buttons = [];
            buttons.push({
                type: 'cancel'
            });
            buttons.push({
                type: 'ok',
                action: this.moveConfirmed
            });
            this.discussionId = discussionId;
            this.srcTopicId = currentTopicId;
            delete this.targetTopicId;
            showDialog(title, htmlElement, buttons, Object.merge({
                onShowCallback: this.dialogOnShowCallback,
                onCloseCallback: this.cleanup,
                cssClasses: 'cn-move-discussion'
            }, dialogOptions));
        }
    };

    /**
     * Cleanup when the dialog is removed.
     */
    moveDiscussion.cleanup = function() {
        if (this.topicAutocompleter) {
            this.topicAutocompleter.destroy();
            delete this.topicAutocompleter;
        }
        if (this.placeholders) {
            this.placeholders.destroy();
            delete this.placeholders;
        }
        if (this.topicTextboxList) {
            this.topicTextboxList.destroy();
            delete this.topicTextboxList;
        }
    }.bind(moveDiscussion);

    /**
     * Add the auto completer and place to the created dialog.
     * 
     * @param dialogContainer {Element} The element containing the dialog.
     */
    moveDiscussion.dialogOnShowCallback = function(dialogContainer) {
        var constructor;
        var inputField = dialogContainer.getElementById('moveTargetTopicSearchInput');
        var postData = {};
        postData['blogIdsToExclude'] = this.srcTopicId;

        constructor = communote.getConstructor('TextboxList');
        this.topicTextboxList = new constructor(inputField, {
            allowDuplicates: false,
            itemLimit: 1,
            listCssClass: 'cn-border',
            itemLimitReachedAction: 'hide',
            itemLimitReachedCallback: this.topicLimitReached,
            itemRemoveCssClass: 'textboxlist-item-remove cn-icon',
            itemRemoveTitle: getJSMessage('common.tagManagement.remove.tag.tooltip')
        });

        this.topicAutocompleter = autocompleterFactory.createTopicAutocompleter(inputField, {
            suggestionsOptions: {
                zIndex: 1000
            }
        }, postData, false, 'write');
        this.topicAutocompleter.addEvent('onChoiceSelected', this.topicChoiceSelected);
        this.placeholders = communote.utils.attachPlaceholders(inputField);
    }.bind(moveDiscussion);

    moveDiscussion.topicLimitReached = function(reached) {
        if (reached) {
            $$('#dialog-modal.cn-move-discussion input.cn-button.main')
                    .setStyle('display', 'block');
        } else {
            $$('#dialog-modal.cn-move-discussion input.cn-button.main').setStyle('display', 'none');
        }
    };

    /**
     * Autocompleter callback that is invoked when a topic is selected.
     * 
     * @param inputElem Input element containing the selected topic.
     * @param choiceElem The choice.
     * @param blogData Data of the selected topic.
     * @param value value??
     */

    moveDiscussion.topicChoiceSelected = function(inputElem, choiceElem, blogData, value) {
        if (!blogData) {
            return;
        }
        this.topicTextboxList.addItem(blogData.title);
        this.topicAutocompleter.resetQuery(true);
        this.targetTopicId = blogData.id;
    }.bind(moveDiscussion);

    /**
     * Move the discussion after the user selected a topic and confirmed the move.
     */
    moveDiscussion.moveConfirmed = function() {
        this.apiAccessor.doApiRequest('put', 'notes/' + this.discussionId, {
            data: {
                topicId: this.targetTopicId,
                noteId: this.discussionId
            }
        }, function(response) {
            E('onDiscussionMoved', {
                discussionId: this.discussionId,
                oldTopicId: this.srcTopicId,
                newTopicId: this.targetTopicId
            });
            showNotification(NOTIFICATION_BOX_TYPES.success, null, response.message);
        }, function(response) {
            showNotification(NOTIFICATION_BOX_TYPES.error, null, response.message);
        });
        this.cleanup();
    }.bind(moveDiscussion);

    NoteUtils = new Class({

        Implements: Options,

        options: {
            deleteOptions: {
                confirmDeletion: true,
                confirmDialogSize: {
                    width: 275,
                    height: 120
                },
                fireEventsOnSuccess: true,
                showMessageOnSuccess: false
            }
        },

        apiAccessor: null,

        /**
         * Create a new instance.
         * 
         * @param {RestApiAccessor} apiAccessor The RestApiAccessor instance to use for the API
         *            requests
         * @param {Object} options An object with settings that should overwrite the default options
         */
        initialize: function(apiAccessor, options) {
            this.setOptions(options);
            this.apiAccessor = apiAccessor;
            moveDiscussion.apiAccessor = apiAccessor;
        },

        favorUnfavorNote: function(id, favorite, successCallback, errorCallback, options) {
            options = Object.append({}, options);
            options.data = {
                favorite: !!favorite
            };
            this.apiAccessor.doApiRequest('post', 'notes/' + id + '/favorites', options,
                    successCallback
                            || this.defaultFavorNoteSuccessCallback.bind(null, id, favorite),
                    errorCallback || this.defaultErrorCallback);
        },

        createNote: function(noteData, successCallback, errorCallback, options) {
            options = Object.append({}, options);
            options.data = noteData;
            this.apiAccessor.doApiRequest('post', 'notes', options, successCallback, errorCallback);
        },

        /**
         * Create a permanent link for a note.
         * 
         * @param {String|Number} noteId ID of the note
         * @param {String} topicAlias Alias of the topic
         */
        createNotePermalink: function(noteId, topicAlias) {
            return buildRequestUrl('/portal/topics/' + topicAlias + '/notes/' + noteId);
        },

        /**
         * Delete a note identified by its ID.
         * 
         * @param {Number} id The ID of the note to delete
         * @param {Function} [successCallback] A function to be called after the Note was deleted
         *            successfully. The method will be passed the REST API response object
         *            containing status and a message. Additionally this object will be extended
         *            with a result member that holds the Note resource object of the deleted note.
         *            If the callback is not provided the #defaultDeleteNoteSuccessCallback will be
         *            called.
         * @param {Function} [errorCallback] A function to be called if the note deletion failed.
         *            The method will be passed the error object returned by the REST API.
         * @param {Object} [options] Apart from the options supported by #doApiRequest the following
         *            options are allowed. These options are intended to overwrite the same-named
         *            default settings stored in the options.deletionOptions of the instance.
         * @param {Boolean} options.confirmDeletion Whether to show a dialog that asks the user to
         *            confirm the deletion of the note.
         * @param {Object} options.dialogOptions Object holding settings to be passed to the
         *            confirmation dialog. See confirmDialog function for details.
         * @param {Boolean} options.fireEventsOnSuccess Controls whether the default success
         *            callback fires some events to inform other components about the deleted note.
         *            See the #defaultDeleteNoteSuccessCallback method for details.
         * @param {Boolean} options.showMessageOnSuccess Controls whether the default success
         *            callback shows a success message. See the #defaultDeleteNoteSuccessCallback
         *            method for details.
         */
        deleteNote: function(id, successCallback, errorCallback, options) {
            var note, error;
            // merge options with default settings
            options = Object.merge({}, this.options.deleteOptions, options);
            // fetch note synchronously
            this.getNote(id, function(response) {
                note = response.result;
            }, function(errorDetails) {
                error = errorDetails;
            }, {
                async: false
            });
            if (error) {
                if (errorCallback) {
                    errorCallback.call(null, id, error);
                } else {
                    this.defaultErrorCallback(error);
                }
            } else {
                if (options.confirmDeletion) {
                    this.internalDeleteNoteWithConfirmation(note, successCallback, errorCallback,
                            options);
                } else {
                    this.internalDeleteNote(note, successCallback, errorCallback, options);
                }
            }
        },

        /**
         * Default callback that is invoked after a note has been deleted successfully and no custom
         * callback was defined. If specified this method will fire an event to inform the
         * FilterEventListeners about the deleted note and if this event did not result in a change
         * of the filter parameters the onNotesChanged event will be fired to inform all listening
         * widgets. Additionally a feedback message can be shown.
         * 
         * @param {Object} response The response object containing message, status and result (the
         *            note resource that was deleted)
         * @param {Boolean} fireEvents Whether to fire the events
         * @param {Boolean} showMessage Whether to show the feedback message
         */
        defaultDeleteNoteSuccessCallback: function(response, fireEvents, showMessage) {
            var note;
            if (fireEvents) {
                note = response.result;
                // first inform the filter parameter store to handle the cases where someone
                // filtered for the note itself
                if (!E2G("onPostDeleted", null, note.noteId)) {
                    // the filter parameter store did not change, so we inform all widgets with a widget event
                    E("onNotesChanged", {
                        action: 'delete',
                        noteId: note.noteId,
                        discussionId: note.discussionId,
                        deletedNotesCount: note.numberOfComments + 1
                    });
                }
            }
            if (showMessage) {
                showNotification(NOTIFICATION_BOX_TYPES.success, null, response.message);
            }
        },

        /**
         * Default error callback which shows the error message.
         * 
         * @param {Object} errorDetails Object containing the details about an error. The object
         *            should at least contain a message attribute that holds the error message to
         *            display.
         */
        defaultErrorCallback: function(errorDetails) {
            showNotification(NOTIFICATION_BOX_TYPES.error, '', errorDetails.message, {
                duration: ''
            });
        },

        /**
         * Default success handler for the favorNote function which fires a onNoteFavorStateChanged
         * event containing the noteId and the new favorite state.
         */
        defaultFavorNoteSuccessCallback: function(noteId, favorite) {
            E('onNoteFavorStateChanged', {
                noteId: noteId,
                favorite: favorite
            });
        },

        /**
         * Default success handler for the likeNote function which fires a onNoteLikedStateChanged
         * event containing the noteId and the new liked state.
         */
        defaultLikeNoteSuccessCallback: function(noteId, liked) {
            E('onNoteLikeStateChanged', {
                noteId: noteId,
                liked: liked
            });
        },
        /**
         * Delete an attachment which is not yet assigned to a note.
         * 
         * @param {Number} attachmentId The ID of the attachment to delete
         * @param {Function} [successCallback] A function to be called after the attachment was
         *            deleted successfully. The method will be passed the REST API response object
         *            containing status and a message.
         * @param {Function} [errorCallback] A function to be called if the note deletion failed.
         *            The method will be passed the error object returned by the REST API.
         * @param {Object} options An object with additional options to be passed to the
         *            doApiRequest method
         */
        deleteAttachment: function(attachmentId, successCallback, errorCallback, options) {
            this.apiAccessor.doApiRequest('delete', 'attachments/' + attachmentId, options,
                    successCallback, errorCallback);
        },

        exportNote: function(noteId, discussionId, options) {
            var dialogOptions;
            var title = getJSMessage('javascript.dialog.export.title');
            var message = getJSMessage('javascript.dialog.export.message');
            var buttons = [];
            var applicationUrl = communote.server.applicationUrl;
            buttons.push({
                label: getJSMessage('javascript.dialog.export.button.note'),
                action: function() {
                    location.href = insertSessionId(applicationUrl
                            + '/topic/export.do?format=rtf&noteId=' + noteId);
                }
            });
            buttons.push({
                label: getJSMessage('javascript.dialog.export.button.discussion'),
                action: function() {
                    location.href = insertSessionId(applicationUrl
                            + '/topic/export.do?format=rtf&discussionId=' + discussionId);
                }
            });
            dialogOptions = {
                cssClasses: 'cn-export'
            };
            if (options && options.dialogOptions) {
                dialogOptions = Object.merge(dialogOptions, options.dialogOptions);
            }
            showDialog(title, message, buttons, dialogOptions);
        },
        
        getDeleteNoteConfirmationMessage: function(noteToDelete) {
            if (noteToDelete.numberOfComments) {
                return getJSMessage('blogpost.delete.with.comments.confirmation',
                        [ noteToDelete.numberOfComments ]);
            } else {
                return getJSMessage('blogpost.delete.confirmation');
            }
        },
        
        /**
         * Get a REST API Note resource by its ID.
         * 
         * @param {Number} id The ID of the note to retrieve
         * @param {Function} successCallback A function to be called after the Note was loaded
         *            successfully. The function will be passed the API response object containing
         *            the note resource.
         * @param {Function} [errorCallback] A function to be called when the request failed. This
         *            function will be passed the error object returned by the REST API which
         *            usually contains a 'message' field and might contain an 'errors' element with
         *            detailed error descriptions. If callback is not provided there won't be no
         *            error feedback.
         * @param {Object} options An object with additional options to be passed to the
         *            doApiRequest method
         */
        getNote: function(id, successCallback, errorCallback, options) {
            this.apiAccessor.doApiRequest('get', 'notes/' + id, options, successCallback,
                    errorCallback);
        },

        /**
         * Get a REST API TimelineNote resource by its ID.
         * 
         * @param {Number} id The ID of the note to retrieve
         * @param {Function} successCallback A function to be called after the TimelineNote was
         *            loaded successfully. The function will be passed the API response object
         *            containing the TimelineNote resource.
         * @param {Function} [errorCallback] A function to be called when the request failed. This
         *            function will be passed the error object returned by the REST API which
         *            usually contains a 'message' field and might contain an 'errors' element with
         *            detailed error descriptions. If callback is not provided there won't be no
         *            error feedback.
         * @param {Object} options An object with additional options to be passed to the
         *            doApiRequest method
         */
        getTimelineNote: function(id, successCallback, errorCallback, options) {
            this.apiAccessor.doApiRequest('get', 'timelineNotes/' + id, options, successCallback,
                    errorCallback);
        },

        /**
         * Internal helper that shows a dialog that asks the user to confirm the deletion of a note.
         * If user confirms this the note will be deleted.
         * 
         * @param {Note} note A note resource of the note to delete
         * @param {Function} [successCallback] The function to call on successful note deletion
         * @param {Function} [errorCallback] The function to call if note deletion failed
         * @param {Object} options Some options that define the size of the dialog and that control
         *            the behavior of the default success callback
         */
        internalDeleteNoteWithConfirmation: function(note, successCallback, errorCallback, options) {
            var dialogOptions;
            var title = getJSMessage('blogpost.delete.popup.title');
            var confirmMessage = this.getDeleteNoteConfirmationMessage(note);
            if (options) {
                dialogOptions = options.dialogOptions;
            }
            showConfirmDialog(title, confirmMessage, this.internalDeleteNote.bind(this, note,
                    successCallback, errorCallback, options), dialogOptions);
        }.protect(),

        // note: cannot be protected because we pass it to showDialog as callback
        internalDeleteNote: function(note, successCallback, errorCallback, options) {
            errorCallback = errorCallback || this.defaultErrorCallback;
            this.apiAccessor.doApiRequest('DELETE', 'notes/' + note.noteId, options, function(
                    response) {
                // rest API response contains only message and status, so we extend the object with the note
                response.result = note;
                if (successCallback) {
                    successCallback.call(null, response);
                } else {
                    this.defaultDeleteNoteSuccessCallback(response, options.fireEventsOnSuccess,
                            options.showMessageOnSuccess);
                }
            }.bind(this), errorCallback);
        },

        /**
         * Like or dislike a note using the REST API.
         * 
         * @param {Number} id The ID of the note to like or dislike.
         * @param {Boolean} likes Whether to like or dislike the note.
         * @param {Function} [successCallback] A function to be called after the operation
         *            succeeded. The method will be passed the REST API response object.
         * @param {Function} [errorCallback] A function to be called if the operation failed. The
         *            method will be passed the error object returned by the REST API. If not
         *            provided the defaultErrorCallback method will be called which shows the error
         *            message returned from the REST API.
         * @param {Object} options An object with additional options to be passed to the
         *            doApiRequest method
         */
        likeNote: function(id, likes, successCallback, errorCallback, options) {
            options = Object.append({}, options);
            options.data = {
                like: likes
            };
            this.apiAccessor.doApiRequest('post', 'notes/' + id + '/likes', options,
                    successCallback || this.defaultLikeNoteSuccessCallback.bind(this, id, likes),
                    errorCallback || this.defaultErrorCallback);
        },

        listTimelineNotes: function(filterParameters, successCallback, errorCallback, options) {
            options = Object.append({}, options);
            options.data = filterParameters;
            this.apiAccessor.doApiRequest('get', 'timelineNotes', options, successCallback,
                    errorCallback);
        },
        /**
         * Method to move a discussion to another topic.
         * 
         * @param {Number} discussionId ID of the discussion to move.
         * @param {Number} currentTopicId ID of the current topic, the discussion is in.
         * @param {Object} [options] Additional options. Currently only the field dialogOptions
         *            is supported. This field can contain an object with options supported by
         *            showDialog function.
         */
        moveDiscussion: function(discussionId, currentTopicId, options) {
            var dialogOptions;
            if (options) {
                dialogOptions = options.dialogOptions;
            }
            moveDiscussion.showDialog(discussionId, currentTopicId, dialogOptions);
        },

        updateNote: function(id, noteData, successCallback, errorCallback, options) {
            options = Object.append({}, options);
            options.data = noteData;
            this.apiAccessor.doApiRequest('put', 'notes/' + id, options, successCallback,
                    errorCallback);
        }
    });
})();