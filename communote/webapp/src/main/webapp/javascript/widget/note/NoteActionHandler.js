(function(namespace, window) {
    var classNamespace = ((namespace && namespace.classes) || window);

    classNamespace.NoteActionHandler = function(widget) {
        this.widget = widget;
        this.actionHandlers = {};
    };

    /**
     * Add a handler that should be invoked for a given note action.
     * 
     * @param {String} action The name of the action to handle. If there is already a handler for
     *            that action, the existing one will be replaced. If the action refers to a built-in
     *            action the built-in action will be temporarily replaced, that is, when removing
     *            the handler the built-in handler is called again.
     * @param {Function} callback The function that should handle the action
     */
    classNamespace.NoteActionHandler.prototype.addActionHandler = function(action, callback) {
        if (action && callback) {
            this.actionHandlers[action] = callback;
        }
    };

    classNamespace.NoteActionHandler.prototype.commentNote = function(details) {
        var staticParams;
        var parentNoteId = details.noteId;
        var newWidgetId = 'CommentNote_' + parentNoteId + '_' + new Date().getTime();
        // TODO this logic should probably be in the widget
        var node = this.widget.beforeShowCommentNoteWidget(details.noteContainer);
        var metaData = this.widget.getNoteMetaData(details.noteId);
        if (node) {
            staticParams = {
                parentPostId: parentNoteId,
                discussionId: metaData.discussionId,
                action: 'comment',
                targetBlogId: metaData.topicId,
                widgetListenerGroupId: 'post_comment_group'
            };
            this.widget.insertCreateNoteWidget(node, newWidgetId, staticParams);
        }
    };

    classNamespace.NoteActionHandler.prototype.deleteNote = function(details) {
        communote.utils.noteUtils.deleteNote(details.noteId, null, null, {
            dialogOptions: {
                triggeringEvent: details.triggeringEvent
            }
        });
    };

    classNamespace.NoteActionHandler.prototype.editNote = function(details) {
        var staticParams;
        var newWidgetId = 'EditNote_' + details.noteId + '_' + new Date().getTime();
        var node = this.widget.beforeShowEditNoteWidget(details.noteContainer);
        var metaData = this.widget.getNoteMetaData(details.noteId);
        if (node) {
            staticParams = {
                noteId: details.noteId,
                action: 'edit',
                targetBlogId: metaData.topicId,
                widgetListenerGroupId: 'post_edit_group',
                focusTextarea: true
            };
            this.widget.insertCreateNoteWidget(node, newWidgetId, staticParams);
        }
    };

    /**
     * Built-in action to export a note or its discussion.
     * 
     * @param {Object} details Object containing the noteId, note content wrapper and the widget
     *            instance
     */
    classNamespace.NoteActionHandler.prototype.exportNote = function(details) {
        var metaData = this.widget.getNoteMetaData(details.noteId);
        communote.utils.noteUtils.exportNote(details.noteId, metaData.discussionId, {
            dialogOptions: {
                triggeringEvent: details.triggeringEvent
            }
        });
    };

    /**
     * Built-in action to add a note to or remove it from the favorite notes of the current user.
     * 
     * @param {Object} details Object containing the noteId, note content wrapper and the widget
     *            instance
     */
    classNamespace.NoteActionHandler.prototype.favorNote = function(details) {
        var metaData = this.widget.getNoteMetaData(details.noteId);
        // toggle favor state
        communote.utils.noteUtils.favorUnfavorNote(details.noteId, !metaData.favorite);
    };

    /**
     * Invoke a built-in handler if it exists. The built-in handler is expected to be named like
     * action + 'Note' (e.g. editNote) and is passed the details object. If there is no such handler
     * the call is ignored.
     */
    classNamespace.NoteActionHandler.prototype.invokeBuiltInAction = function(action, details) {
        var handler = this[action + 'Note'];
        if (handler) {
            handler.call(this, details);
            return true;
        }
        return false;
    };
    /**
     * Invoke a note action handler that was registered with addActionHandler or a built-in handler.
     * 
     * @param {String} action The name of the action for which a registered or built-in handler
     *            should be invoked
     * @param {Object} details An object containing the noteId, a reference to the noteContainer for
     *            which the action was invoked, a reference to the CPL widget instance and a
     *            triggeringEvent field that holds the event that caused the action. The
     *            triggeringEvent field can be missing if there is no associated event.
     * @return {Boolean} whether the action was handled
     */
    classNamespace.NoteActionHandler.prototype.invokeAction = function(action, details) {
        var handler = this.actionHandlers[action];
        if (handler) {
            handler.call(null, details);
            return true;
        } else {
            return this.invokeBuiltInAction(action, details);
        }
    };
    classNamespace.NoteActionHandler.prototype.likeNote = function(details) {
        var metaData = this.widget.getNoteMetaData(details.noteId);
        communote.utils.noteUtils.likeNote(details.noteId, !metaData.liked);
    };
    classNamespace.NoteActionHandler.prototype.moveNote = function(details) {
        var metaData = this.widget.getNoteMetaData(details.noteId);
        communote.utils.noteUtils.moveDiscussion(metaData.discussionId, metaData.topicId, {
            dialogOptions: {
                triggeringEvent: details.triggeringEvent
            }
        });
    };
    /**
     * Remove a handler that was added via addActionHandler.
     * 
     * @param {String} action The name of the action the handler to remove handles
     */
    classNamespace.NoteActionHandler.prototype.removeActionHandler = function(action) {
        delete this.actionHandlers[action];
    };

    classNamespace.NoteActionHandler.prototype.repostNote = function(details) {
        var staticParams;
        var newWidgetId = 'RepostNote_' + details.noteId + '_' + new Date().getTime();
        var node = this.widget.beforeShowRepostNoteWidget(details.noteContainer);
        if (node) {
            // reposting is like creating a new note with initial content
            staticParams = {
                action: 'create',
                cancelBehavior: {
                    confirmDiscard: false,
                    confirmReset: false,
                    action: 'remove'
                },
                inheritTags: true,
                publishSuccessBehavior: {
                    action: 'remove'
                },
                repostNoteId: details.noteId
            };
            this.widget.insertCreateNoteWidget(node, newWidgetId, staticParams);
        }
    };

})(this.runtimeNamespace, this);