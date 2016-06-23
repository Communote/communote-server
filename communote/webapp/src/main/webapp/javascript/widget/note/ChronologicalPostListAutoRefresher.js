(function(namespace, window) {
    var classNamespace = ((namespace && namespace.classes) || window);

    /**
     * Helper class to get AutoRefresher logic out of the widget for better maintainability.
     * 
     * @param {ChronologicalPostListWidget} widget The widget instance
     */
    classNamespace.ChronologicalPostListAutoRefresher = function(widget) {
        this.widget = widget;
        this.newNotesAvailable = false;
        this.missedNoteIds = null;
        if (!widget.getStaticParameter('disableAutorefresh')) {
            this.enable();
        }
    };

    /**
     * This method will be called from the autorefresher to publish new results.
     * 
     * @param object The result.
     */
    classNamespace.ChronologicalPostListAutoRefresher.prototype.autoRefresherCallback = function(
            object) {
        var reminderElem;
        if (object.result.count > 0) {
            // TODO this should probably a (showNewMessagesReminder) method of the widget
            reminderElem = this.widget.domNode.getElements('.new-messages-reminder');
            if (reminderElem) {
                reminderElem.setStyle('display', 'block');
            }
            this.autoRefresher.stop();
            this.newNotesAvailable = true;
            this.originalDocumentTitle = document.title;
            this.modifiedDocumentTitle = getJSMessage('blog.post.list.autorefresh.title') + ' - '
                    + this.originalDocumentTitle;
            document.title = this.modifiedDocumentTitle;
        } else if (this.autoRefresher.getAdditionalParameter('noteIds')) {
            // did a check for new notes that were created by the current user while the current
            // CPL instance was hidden and found nothing -> return to checking for the other notes.
            this.autoRefresher.stop();
            this.autoRefresher.setAdditionalParameter('userIdsToIgnore', communote.currentUser.id);
            this.autoRefresher.setAdditionalParameter('noteIds', null);
            this.missedNoteIds = null;
            this.autoRefresher.start(true, true);
        }
    };
    
    /**
     * Disable the AutoRefresher.
     */
    classNamespace.ChronologicalPostListAutoRefresher.prototype.disable = function() {
        if (this.autoRefresher) {
            this.autoRefresher.stop();
            this.autoRefresher = null;
        }
    };
    
    /**
     * Enable the AutoRefresher but don't start it yet.
     */
    classNamespace.ChronologicalPostListAutoRefresher.prototype.enable = function() {
        if (!this.autoRefresher) {
            this.autoRefresher = new AutoRefresher({
                url: '/web/v1.3.1/posts-count.json',
                functionToCall: this.autoRefresherCallback.bind(this)
            });
            this.autoRefresherStartTimestamp = (new Date()).getTime();
            // force CLASSIC view type when checking for new notes
            this.autoRefresher.setAdditionalParameter('selectedViewType', 'CLASSIC');
        }
    };

    /**
     * @return {Boolean} whether the AutoRefresher has discovered new notes
     */
    classNamespace.ChronologicalPostListAutoRefresher.prototype.hasNewNotes = function() {
        return this.newNotesAvailable;
    };

    /**
     * Inform the AutoRefresher about user activity to increase the polling interval again after it
     * was decreased over time.
     */
    classNamespace.ChronologicalPostListAutoRefresher.prototype.notifyAboutInteraction = function() {
        if (this.autoRefresher && !this.newNotesAvailable && this.autoRefresher.isStarted()) {
            this.autoRefresher.restart();
        }
    };

    /**
     * Inform the AutoRefresher that the current user created a new note or comment. This method is
     * only called while the widget is hidden.
     */
    classNamespace.ChronologicalPostListAutoRefresher.prototype.notifyAboutNewNote = function(noteId) {
        // just record the note, because the widget is hidden and the AutoRefresher is not running.
        if (!this.missedNoteIds) {
            this.missedNoteIds = [];
        }
        this.missedNoteIds.push(noteId);
    };

    classNamespace.ChronologicalPostListAutoRefresher.prototype.start = function(doImmediateCheck) {
        // don't start AutoRefresher if we already know that there are new notes or the widget is
        // hidden. Latter can occur if widget is hidden before refreshComplete is called
        if (this.autoRefresher && !this.newNotesAvailable && !this.widget.isHidden()) {
            this.autoRefresher.setFilterParameters(this.widget.getRefreshParameters());
            this.autoRefresher
                    .setAdditionalParameter('startDate', this.autoRefresherStartTimestamp);
            // always reset the timer because the user interacted with the notes
            this.autoRefresher.start(doImmediateCheck, true);
        }
    };

    /**
     * Function to be called from the afterShow method of the widget.
     */
    classNamespace.ChronologicalPostListAutoRefresher.prototype.widgetAfterShow = function(isDirty) {
        if (this.modifiedDocumentTitle) {
            document.title = this.modifiedDocumentTitle;
        }
        // start AutoRefresher if necessary
        if (!isDirty && this.autoRefresher && !this.newNotesAvailable) {
            if (this.missedNoteIds && this.missedNoteIds.length) {
                // first check for any missed notes matching the current filter which were created
                // by the current user while the widget was hidden
                this.autoRefresher.setAdditionalParameter('userIdsToIgnore', null);
                this.autoRefresher.setAdditionalParameter('noteIds', this.missedNoteIds.join(','));
            } else {
                this.autoRefresher.setAdditionalParameter('userIdsToIgnore',
                        communote.currentUser.id);
                this.autoRefresher.setAdditionalParameter('noteIds', null);
            }
            this.start(true);
        }
    };
    /**
     * Function to be called from the beforeHide method of the widget.
     */
    classNamespace.ChronologicalPostListAutoRefresher.prototype.widgetBeforeHide = function() {
        if (this.autoRefresher) {
            this.autoRefresher.stop();
        }
        if (this.originalDocumentTitle) {
            document.title = this.originalDocumentTitle;
        }
    };
    /**
     * Function to be called when the widget is doing some cleanup, especially when it is going to
     * do a full refresh or is removed.
     */
    classNamespace.ChronologicalPostListAutoRefresher.prototype.widgetBeforeRemove = function() {
        // do the same as if the widget is hidden
        this.widgetBeforeHide();
    };
    /**
     * Function to be called from the refreshComplete method of the widget.
     */
    classNamespace.ChronologicalPostListAutoRefresher.prototype.widgetRefreshComplete = function(
            responseMetadata) {
        // clear missed notes as content is up-to-date
        this.missedNoteIds = null;
        if (this.autoRefresher) {
            if (responseMetadata.firstNoteCreationTimestamp != null) {
                timestamp = responseMetadata.firstNoteCreationTimestamp;
                if (!isNaN(timestamp)) {
                    this.autoRefresherStartTimestamp = timestamp;
                }
            }
            // ignore the notes of the current user
            // Note: must do it here and not after a note or comment was created because the
            // autoRefresher might have already been run in the time between the note creation and the
            // onNotesChanged event and would have found the new note.
            this.autoRefresher.setAdditionalParameter('userIdsToIgnore', communote.currentUser.id);
            // remove the noteIds filter as we are up to date
            this.autoRefresher.setAdditionalParameter('noteIds', null);
            this.newNotesAvailable = false;
            // restore the original title
            if (this.originalDocumentTitle) {
                // do not change title if widget was hidden between refreshStart and refreshComplete 
                if (!this.widget.isHidden()) {
                    document.title = this.originalDocumentTitle;
                }
                this.originalDocumentTitle = null;
                this.modifiedDocumentTitle = null;
            }
            // are up-to-date no immediate start required
            this.start(false)
        }
    };

    classNamespace.ChronologicalPostListAutoRefresher.prototype.widgetRefreshStart = function() {
        // use current time as start timestamp for the AutoRefresher when the refresh does not
        // return any notes
        this.autoRefresherStartTimestamp = (new Date()).getTime();
        if (this.autoRefresher) {
            this.autoRefresher.stop();
        }
    };

})(this.runtimeNamespace, this);