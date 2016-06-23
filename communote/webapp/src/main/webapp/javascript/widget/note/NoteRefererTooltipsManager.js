(function(namespace, window) {
    var NoteRefererTooltipsManager;
    /**
     * Callback for the noteReferer Tooltips which loads a note identified by its id with the help
     * of the NoteUtils instance.
     */
    function loadDataCallback(id, successCallback, errorCallback) {
        // fetch timeline note and get content in plaintext
        namespace.utils.noteUtils.getTimelineNote(id, successCallback, errorCallback, {
            data: {
                filterHtml: true
            }
        });
    }
    /**
     * Callback for the noteReferer Tooltips which processes a timeline note object fetched by
     * loadDataCallback. This implementation sets the short text as text and adds the creation date
     * to the title.
     * 
     * @param {Object} apiResponse The response object of the TimelineNote REST API resource
     * @return {Object} the object containing the title and text to present in the tooltip
     */
    function parseDataCallback(apiResponse) {
        var timelineNote, text, title, timestamp, dateString;
        timelineNote = apiResponse.result;
        if (timelineNote.shortText) {
            text = timelineNote.shortText + ' [...]';
        } else {
            text = timelineNote.text;
        }
        text = text.replace(/\n/g, '<br>');
        // TODO this is actually wrong for several reasons:
        // - must use TZ offset of creationDate and not now
        // - the value stored in currentUser.timeZoneOffset is not the UTC offset but something else that depends on server timezone
        // Note: subtracting the getTimezoneOffset here since the formatter is not working with UTC
        // members of Date and thus adds the TZ offset of the client
        timestamp = timelineNote.creationDate;
        dateString = localizedDateFormatter.format(new Date(timestamp),
                getJSMessage('javascript.dateformatter.pattern.datetime.long'));
        title = getJSMessage('blog.post.list.comment.refers_to') + ' (' + dateString + '):';
        return {
            title: title,
            text: '<p>' + text + '</p>'
        };
    }
    
    NoteRefererTooltipsManager = function(cplWidget) {
        this.widget = cplWidget;
        this.tips = new namespace.classes.LazyTips({
            className: 'tip-wrap refers-to',
            htmlContent: true,
            loadDataCallback: loadDataCallback,
            parseDataCallback: parseDataCallback,
            errorFieldMessage: 'message',
            loadingFeedbackMessage: getJSMessage('javascript.loading.message'),
            cacheOptions: {}
        });
    };
    /**
     * Scan the child nodes of a given node for elements that should get a tooltip with the
     * note-referer information.
     * 
     * @param {Element} [startNode] The node whose children should be searched. If omitted the
     *            domNode of the widget is used.
     */
    NoteRefererTooltipsManager.prototype.attach = function(startNode) {
        var elems;
        if (!startNode) {
            startNode = this.widget.domNode;
        }
        // TODO optimize by remembering the number of added elements to avoid unnecessary detaches?
        this.tips.attach(startNode.getElements('.tooltip'));
    };
    
    /**
     * Function to be called if a note was modified. The manager will use it to clear caches.
     * 
     * @param {Number} noteId The ID of the modified note
     */
    NoteRefererTooltipsManager.prototype.noteChanged = function(noteId) {
        this.tips.removeFromCache(noteId);
    };
    
    /**
     * Scan the child nodes of a given node for elements from which the note-referer tooltips should
     * be removed.
     * 
     * @param {Element} [startNode] The node whose children should be searched. If omitted the
     *            domNode of the widget is used.
     */
    NoteRefererTooltipsManager.prototype.detach = function(startNode) {
        if (!startNode) {
            startNode = this.widget.domNode;
        }
        this.tips.detach(startNode.getElements('.tooltip'));
    };
    namespace.classes.NoteRefererTooltipsManager = NoteRefererTooltipsManager;
})(this.runtimeNamespace, this);