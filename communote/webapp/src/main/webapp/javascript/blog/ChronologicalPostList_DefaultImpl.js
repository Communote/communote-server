var ChronologicalPostListDefaultWidget = new Class({
    Extends: ChronologicalPostListWidget,

    // holds details about shown inline discussions
    inlineDiscussions: {},
    inlineComments: {},

    inlineDiscussionMarkerClass: 'control-inline-discussion',
    inlineCommentsMarkerClass: 'control-inline-comments',
    noteExpandedCssClass: 'cn-expanded',
    inlineDiscussionShownCssClass: 'discussion-shown',
    // marker CSS class that identifies the default placeholder for adding new comments
    insertCommentPlaceholderMarkerClass: 'control-new-comment-placeholder',
    // marker CSS class that identifies the placeholder for adding new comments to the threaded view
    insertCommentToThreadedPlaceholderCssClass: 'control-new-comment-to-thread-placeholder',
    // LazyTips instance that is used to show a tool tip containing the short content of a note, a comment refers to
    noteRefererTooltips: null,

    // holds all CSS classes that are added via JavaScript and should be kept when editing a note
    noteWrapperCssClassesToKeepWhenEditing: [],

    // the ID identifying the preferences to use by this instance when loading settings like the viewType from
    // the local storage. This ID is something like the identifier of a profile.
    preferenceId: null,
    /**
     * The currently selected view type
     */
    selectedViewType: null,

    init: function() {
        var predefinedViewType;
        this.parent();
        this.noteWrapperCssClassesToKeepWhenEditing.push(this.noteExpandedCssClass);
        this.noteWrapperCssClassesToKeepWhenEditing.push(this.inlineDiscussionShownCssClass);
        this.noteWrapperCssClassesToKeepWhenEditing.push(this.noteHighlightCssClass);
        predefinedViewType = this.getStaticParameter('predefinedViewType');
        if (!predefinedViewType
                || (predefinedViewType !== 'CLASSIC' && predefinedViewType !== 'COMMENT')) {
            predefinedViewType = null;
            // ignore preferenceId if a viewType is forced to not override user preferences
            this.preferenceId = this.getStaticParameter('preferenceId');
        }
        this.changeViewType(predefinedViewType);
        this.noteRefererTooltips = new communote.classes.NoteRefererTooltipsManager(this);
        this.addNoteCollapseExpandClickHandlers();
        if (!this.getStaticParameter('filterClickHandlersDisabled')) {
            this.addNoteFilterClickHandlers();
        }
    },

    addNoteCollapseExpandClickHandlers: function() {
        this.addNoteLinkClickHandler(function(linkElement, event, metaData) {
            if (linkElement.hasClass('control-collapse-expand-note')) {
                this.collapseOrExpandNote(metaData.noteId, metaData.noteContainer, null);
            }
            // avoid expand/collapse by bodyClick handler on link clicks
            event.cplNoExpand = true;
        }.bind(this), null, null);
        this.addNoteBodyClickHandler(function(element, event, metaData) {
            if (!event.cplNoExpand) {
                if (element.hasClass('cn-click-area')) {
                    this.collapseOrExpandNote(metaData.noteId, metaData.noteContainer, element
                            .hasClass('close') ? 'collapse' : 'expand');
                } else {
                    // generic body click -> only expand
                    this.collapseOrExpandNote(metaData.noteId, metaData.noteContainer, 'expand');
                }
            }
        }.bind(this), null, false, null);
    },

    addNoteFilterClickHandlers: function() {
        this.addNoteLinkClickHandler(function(linkElement, event, metaData) {
            var utils = communote.utils;
            var entityDetails = utils.getCommunoteEntityDetailsFromElement(linkElement);
            if (!entityDetails) {
                return;
            }
            if (entityDetails.noteId) {
                this.sendFilterGroupEvent('onShowNote', {
                    noteId: entityDetails.noteId,
                    blogId: entityDetails.topicId
                }, [ {
                    type: 'blog',
                    key: entityDetails.topicId,
                    title: entityDetails.topicTitle
                }, {
                    type: 'note',
                    key: entityDetails.noteId,
                    title: entityDetails.noteTitle
                } ]);
            } else if (entityDetails.userId) {
                this.sendFilterGroupEvent('onUserSelected', entityDetails.userId, {
                    type: 'user',
                    key: entityDetails.userId,
                    shortName: entityDetails.shortName,
                    longName: entityDetails.longName
                });
            } else if (entityDetails.tagId) {
                this.sendFilterGroupEvent('onTagIdClick', [ entityDetails.tagId,
                        entityDetails.tagName ]);
            } else {
                // ignore (no topic filter yet)
                return;
            }
            utils.eventPreventDefault(event);
        }.bind(this), '.control-filter-entity-link', null);
    },

    /**
     * @override
     */
    // TODO memory leak when there are create note widgets in the discussions??
    onNotesChanged: function(params) {
        var discussionId, discussionDetails, noteIds, i, l, discussionContainer, noteContainers, j;
        var parentContainer, utils;
        discussionId = params.discussionId;
        discussionDetails = this.inlineDiscussions[discussionId];
        // when deleting notes, directly remove elements from DOM
        if (params.action == 'delete') {
            // if the discussion is removed, cleanup
            if (params.noteId == discussionId) {
                this.domNode.getElements('.control-discussion-' + discussionId).destroy();
                delete this.inlineDiscussions[discussionId];
                // TODO remove inline comments
                noteIds = this.getNoteIdsOfDiscussion(discussionId);
            } else {
                // get all notes whose discussionPath contains the noteId. This is the note itself and all replies.
                noteIds = this.getNoteIds(function(noteMetaData, replyMatchRe) {
                    return replyMatchRe.test(noteMetaData.discussionPath);
                }, new RegExp(' ' + params.noteId + ' | ' + params.noteId + '$'));
                utils = communote.utils;
                discussionContainer = discussionDetails
                        && this.domNode.getElementById(discussionDetails.containerId);
                for (i = 0, l = noteIds.length; i < l; i++) {
                    // remove inside inline discussion first
                    if (discussionContainer) {
                        discussionContainer.getElements('.control-note-' + noteIds[i]).destroy();
                    }
                    noteContainers = this.getNoteContainers(noteIds[i]);
                    if (this.selectedViewType === 'CLASSIC') {
                        // must remove the control wrapper if the note is not a direct-reply
                        for (j = 0; j < noteContainers.length; j++) {
                            if (!utils.getMatchingParentElement(noteContainers[j], '.'
                                    + this.inlineCommentsMarkerClass, this.domNode)) {
                                parentContainer = utils.getMatchingParentElement(noteContainers[j],
                                        '.control-discussion-wrapper', this.domNode);
                            }
                            if (parentContainer) {
                                parentContainer.destroy();
                            } else {
                                noteContainers[j].destroy();
                            }
                        }
                    } else {
                        noteContainers.destroy();
                    }
                }
                this.updateCommentCountElements(discussionId, -params.deletedNotesCount);
                // remove from inline discussions if the discussion container was removed
                if (discussionDetails
                        && !this.domNode.getElementById(discussionDetails.containerId)) {
                    // in case the inline discussion was shown the notes of the discussion must be made visible again
                    if (discussionDetails.status === 'shown') {
                        this.toggleDiscussionNodesVisibility(this.domNode, null, discussionId,
                                false);
                    }
                    delete this.inlineDiscussions[discussionId];
                }
            }
            this.removeNoteMetaData(noteIds);
            this.noteInteractionControlRenderer.inlineInjectedNoteContainersStateChanged(noteIds,
                    'deleted', true);
            this.noteInteractionControlRenderer.normalNoteContainersStateChanged(noteIds,
                    'deleted', true);
            this.hideDateNodes(this.domNode);
            return;
        }
        this.parent(params);
    },

    /**
     * Update the element that shows the number of comments or the total number of notes in the
     * discussion in the different view types.
     * 
     * @param {String|Number} discussionId The ID of the discussion for which the number of comments
     *            changed
     * @param {Number} commentCountDiff Positive or negative number representing the amount of
     *            comments that were added or removed. If null the new comment count will be set to
     *            0.
     */
    updateCommentCountElements: function(discussionId, commentCountDiff) {
        var i, l, newValue, commentCount, discussionWrapper, commentCountElem, newText, elem;
        var noteContainers;
        var noteMetaData = this.getNoteMetaDataForDiscussion(discussionId);
        for (i = 0, l = noteMetaData.length; i < l; i++) {
            noteMetaData[i].discussionCommentCount += commentCountDiff;
            if (this.selectedViewType === 'CLASSIC') {
                this.noteInteractionControlRenderer.noteStateChanged(noteMetaData[i].noteId,
                        'commentCount', noteMetaData[i].discussionCommentCount);
            } else {
                commentCount = noteMetaData[i].discussionCommentCount;
            }
        }
        if (this.selectedViewType === 'COMMENT') {
            discussionWrapper = this.domNode.getElement('.control-discussion-' + discussionId);
            commentCountElem = discussionWrapper.getElement('.control-number-of-comments');
            if (commentCountElem) {
                newText = communote.i18n.getMessage('blog.post.list.comment.show_more.singular.'
                        + (commentCount == 1), [ commentCount ]);
                commentCountElem.getElement('.cn-icon-label').set('text', newText);
                // hide when item only has one (or no) comment left
                if (commentCount == 0) {
                    commentCountElem.addClass('cn-hidden');
                } else {
                    commentCountElem.removeClass('cn-hidden');
                }
            }
            if (commentCount == 0) {
                discussionWrapper.removeClass('cn-note-has-inline-comments');
            } else {
                discussionWrapper.addClass('cn-note-has-inline-comments');
            }
            // some super special behavior when comments were removed with enabled COMMENT view type
            if (!commentCountDiff || commentCountDiff < 0) {
                // if the number of comments matches the number of notes shown in the directly visible
                // comment subset the element that toggles between all comments and the subset is
                // removed (because there is nothing to toggle), the container holding the subset is
                // made visible and the container holding  all comments is cleared.
                if (commentCount == discussionWrapper
                        .getElements('.cn-inline-comments-subset .control-note-wrapper').length) {
                    this.hideComments(discussionId);
                    elem = discussionWrapper.getElement('.cn-load-more-comments');
                    if (elem) {
                        // will only exist if there were additional comments when the CPL was refreshed that are now deleted
                        elem.destroy();
                    }
                    elem = discussionWrapper
                            .getElement('.control-inline-comments-load-more-placeholder');
                    // only exists if there were comments when the CPL was refreshed
                    if (elem) {
                        // avoid memory leaks
                        noteContainers = elem.getElements('.' + this.noteContainerMarkerClass);
                        noteContainers.destroy();
                        // notify interaction control renderer
                        this.noteInteractionControlRenderer.noteContainersStateChanged(
                                noteContainers, 'deleted', true);
                        elem.empty();
                    }
                    // delete from inlineComments if contained because it is not needed anymore
                    delete this.inlineComments[discussionId];
                }
            }
        }
    },

    /**
     * @override
     */
    beforeRemoveEditNoteWidget: function(widget) {
        var noteContainer = this.getNoteContainer(widget.domNode);
        this.changeActionClass(noteContainer, true, true);
        this.parent(widget);
    },
    /**
     * @override
     */
    beforeRemoveCommentNoteWidget: function(widget) {
        // check if it is a fast-reply
        var wrapperContainer = widget.domNode.getParent('.control-fast-reply');
        if (!wrapperContainer) {
            wrapperContainer = this.getNoteContainer(widget.domNode);
        }
        this.changeActionClass(wrapperContainer, false, true);
    },
    /**
     * @override
     */
    beforeRemoveRepostNoteWidget: function(widget) {
        // treat like comment
        this.beforeRemoveCommentNoteWidget(widget);
    },

    /**
     * @override
     */
    beforeShowCommentNoteWidget: function(noteContainer) {
        this.changeActionClass(noteContainer, false, false);
        return noteContainer.getElement('.cn-content-comment');
    },

    /**
     * @override
     */
    beforeShowEditNoteWidget: function(noteContainer) {
        this.changeActionClass(noteContainer, true, false);
        return noteContainer.getElement('.cn-content-edit');
    },

    /**
     * @override
     */
    beforeShowRepostNoteWidget: function(noteContainer) {
        // treat like comment
        return this.beforeShowCommentNoteWidget(noteContainer);
    },

    changeActionClass: function(noteContainer, edit, toView) {
        var oldClass, newClass;
        var actionClass = edit ? 'cn-note-render-mode-edit' : 'cn-note-render-mode-comment';
        if (toView) {
            oldClass = actionClass;
            newClass = 'cn-note-render-mode-view';
        } else {
            oldClass = 'cn-note-render-mode-view';
            newClass = actionClass;
        }
        noteContainer.removeClass(oldClass);
        noteContainer.addClass(newClass);
    },

    fastReply: function(linkNode, parentNoteId, full) {
        var staticParams, widget, newWidgetId, placeholderElem, fastReplyContainer, noteContainer, node;
        var utils = communote.utils;
        var noteMetaData = this.getNoteMetaData(parentNoteId);
        if (!noteMetaData) {
            // it is expected that the note to reply to is visible
            return;
        }
        if (linkNode) {
            linkNode = document.id(linkNode);
            fastReplyContainer = utils.getMatchingParentElement(linkNode, '.control-fast-reply',
                    this.domNode);
            if (!fastReplyContainer) {
                noteContainer = this.getNoteContainer(linkNode, parentNoteId);
            }
        } else {
            noteContainer = this.getNoteContainer(null, parentNoteId);
            fastReplyContainer = utils.getMatchingParentElement(noteContainer,
                    '.control-discussion-wrapper').getElement('.control-fast-reply');
        }
        if (!fastReplyContainer || !fastReplyContainer.isVisible()) {
            // trigger a normal comment
            this.invokeNoteAction('comment', {
                noteId: parentNoteId,
                noteContainer: noteContainer
            });
        } else {
            // reduce flickering while loading the widget by cloning the placeholder and injecting it into the widget
            // wrapper div before refreshing to give the widget the correct height and simulate a refresh of the content
            node = fastReplyContainer.getElement('.cn-content-comment');
            if (node) {
                newWidgetId = 'CommentNote_' + parentNoteId + '_' + new Date().getTime();
                staticParams = {
                    parentPostId: parentNoteId,
                    discussionId: parentNoteId,
                    action: 'comment',
                    renderStyle: full ? 'full' : 'minimal',
                    targetBlogId: noteMetaData.topicId,
                    widgetListenerGroupId: 'post_comment_group',
                    refreshOnInitialization: 'false'
                };
                widget = this.insertCreateNoteWidget(node, newWidgetId, staticParams);
                placeholderElem = fastReplyContainer.getElement(
                        '.cn-list-fast-reply-placeholder-wrapper').clone();
                widget.domNode.grab(placeholderElem);
                widget.show();
                this.changeActionClass(fastReplyContainer, false, false);
            }
        }
    },

    cleanup: function() {
        this.parent();
        if (this.firstDOMLoadDone) {
            this.noteRefererTooltips.detach();
        }
    },

    /**
     * @override
     */
    refreshStart: function() {
        // clear the stored inline discussions
        this.inlineDiscussions = {};
        this.parent();
    },

    /**
     * @override
     */
    refreshComplete: function(responseMetadata) {
        this.parent(responseMetadata);
        this.containsNoNotes = this.domNode.getElement('.control-empty-list') != null;
        this.noteRefererTooltips.attach();
        this.showInlineImages(this.domNode);
        this.inlineComments = {};
    },

    /**
     * Collapse or expand a note by removing/adding the #noteExpandedCssClass CSS class from/to the
     * container element of the note.
     * 
     * @param {Element} noteContainer The note container
     * @param {String} [action] If provided it can have the value 'collapse' to collapse the note
     *            and 'expand' to expand the note. If omitted the state of the note is toggled.
     */
    collapseOrExpandNote: function(noteId, noteContainer, action) {
        if (noteContainer.hasClass(this.noteExpandedCssClass)) {
            if (action !== 'expand') {
                noteContainer.removeClass(this.noteExpandedCssClass);
                this.noteInteractionControlRenderer.noteContainerStateChanged(noteId,
                        noteContainer, 'expanded', false);
            }
        } else {
            if (action !== 'collapse') {
                noteContainer.addClass(this.noteExpandedCssClass);
                this.noteInteractionControlRenderer.noteContainerStateChanged(noteId,
                        noteContainer, 'expanded', true);
            }
        }
    },

    showInlineImages: function(startNode) {
        if (!window.Mediabox) {
            return;
        }
        // scan notes for inline pictures
        Mediabox.scanPage(startNode);
    },

    /**
     * Method to load and show the missing comments of the comments view.
     * 
     * @param {String|Number} discussionId comments of the discussion to load.
     */
    showComments: function(discussionId) {
        var noteIds, inlineCommentsExisted;
        var discussionDetails = this.inlineComments[discussionId];
        var discussionWrapper = this.domNode.getElement('.control-discussion-' + discussionId);
        if (!discussionDetails) {
            discussionDetails = {
                discussionId: discussionId,
                selectedViewType: 'COMMENT',
                status: null
            };
            this.inlineComments[discussionId] = discussionDetails;
            this.refreshDiscussion(discussionWrapper
                    .getElement('.control-inline-comments-load-more-placeholder'),
                    discussionDetails);
        } else {
            inlineCommentsExisted = true;
        }
        discussionWrapper.getElement('.' + this.inlineCommentsMarkerClass).removeClass(
                'cn-inline-comments-subset-shown');
        if (discussionDetails.status === 'hidden') {
            discussionDetails.status = 'shown';
        }
        // notify interaction control renderer. Ignore root note because it is always shown.
        noteIds = this.getNoteIdsOfDiscussion(discussionId).erase(parseInt(discussionId));
        this.noteInteractionControlRenderer.normalNoteContainersStateChanged(noteIds, 'shown',
                false);
        if (inlineCommentsExisted) {
            this.noteInteractionControlRenderer.inlineInjectedNoteContainersStateChanged(noteIds,
                    'shown', true);
        }
    },
    /**
     * Method to hide the shown comments of the comments view.
     * 
     * @param {String|Number} discussionId Id of the discussion to hide.
     */
    hideComments: function(discussionId) {
        var discussionWrapper, noteIds;
        var discussionDetails = this.inlineComments[discussionId];
        if (discussionDetails) {
            discussionWrapper = this.domNode.getElement('.control-discussion-' + discussionId);
            discussionWrapper.getElement('.' + this.inlineCommentsMarkerClass).addClass(
                    'cn-inline-comments-subset-shown');
            discussionDetails.status = 'hidden';
            noteIds = this.getNoteIdsOfDiscussion(discussionId).erase(parseInt(discussionId));
            this.noteInteractionControlRenderer.inlineInjectedNoteContainersStateChanged(noteIds,
                    'shown', false);
            this.noteInteractionControlRenderer.normalNoteContainersStateChanged(noteIds, 'shown',
                    true);
        }
    },

    /**
     * @override
     */
    createInsertNewCommentPartialRefreshDescriptor: function(noteId, parentNoteId, discussionId,
            placeholder) {
        var inlineInjected;
        var refreshDescr = this.parent(noteId, parentNoteId, discussionId, placeholder);
        // if threaded we need to replace the placeholder and set the view type to THREAD
        if (placeholder.hasClass(this.insertCommentToThreadedPlaceholderCssClass)) {
            refreshDescr.additionalParams.selectedViewType = 'THREAD';
            refreshDescr.insertMode = 'replace';
            // inlineInjected because it is rendered into the inline discussion
            inlineInjected = true;
        } else {
            // in some cases like inline discussion not yet shown the comment is rendered in
            // COMMENT style even in CLASSIC view type
            refreshDescr.additionalParams.selectedViewType = 'COMMENT';
            // only inlineInjected if the comment is inserted (injected) into an inlineComments or 
            // in CLASSIC view type because injected replies are removed when discussion is opened
            if (this.selectedViewType === 'CLASSIC' || this.inlineComments[discussionId]) {
                inlineInjected = true;
            }
        }
        refreshDescr.context.inlineInjected = inlineInjected;
        return refreshDescr;
    },

    /**
     * @override
     */
    getInsertNewCommentPlaceholder: function(noteId, parentNoteId, parentNoteContainer,
            discussionId) {
        var placeholder, parentMetaData, noteMetaData, discussionContainer, noteContainers, i, prevSibling;
        var discussionWrapper, discussionDetails;
        discussionId = String(discussionId);
        discussionDetails = this.inlineDiscussions[discussionId];
        // in case there is an inlineDiscussion for the discussionId and it is shown, refresh
        // the inlineDiscussion
        if (discussionDetails && discussionDetails.status == 'shown') {
            // within the threaded view of a discussion comments must be added after the
            // last comment whose discussion depth is > 'depth of parent note' and whose
            // discussionPath starts like that of the parent
            parentMetaData = this.getNoteMetaData(parentNoteId);
            discussionContainer = this.domNode.getElementById(discussionDetails.containerId);
            noteContainers = discussionContainer.getElements('.' + this.noteContainerMarkerClass);
            for (i = noteContainers.length; i--;) {
                // get last note container with ID that matches the conditions above
                noteId = noteContainers[i].getAttribute('data-cnt-note-id');
                noteMetaData = this.getNoteMetaData(noteId);
                if (noteMetaData.discussionDepth > parentMetaData.discussionDepth
                        && noteMetaData.discussionPath.indexOf(parentMetaData.discussionPath) === 0) {
                    prevSibling = noteContainers[i];
                    break;
                }
            }
            if (!prevSibling) {
                // there is no note with the discussion depth so insert the note after the parentNoteContainer.
                // The parent note might be more than once in the DOM, so ensure the one in the threaded view is used
                prevSibling = this.getNoteContainer(null, parentNoteId, discussionContainer);
            }
            // create temporary placeholder right after prevSibling
            placeholder = new Element('div', {
                'class': this.insertCommentToThreadedPlaceholderCssClass
            });
            placeholder.inject(prevSibling, 'after');
        } else {
            discussionWrapper = parentNoteContainer.getParent('.control-discussion-wrapper');
            // special case comments view: there are 2 containers which are holding the comments.
            // one contains all and the other a subset (2 newest or those that matched the filters)
            // When container with all comments has already been loaded we use its placeholder otherwise
            // the other.
            if (this.selectedViewType == 'COMMENT' && !this.inlineComments[discussionId]) {
                placeholder = discussionWrapper
                        .getElement('.control-new-comment-to-subset-placeholder');
            }
            // if the discussionDetails exist the inlineDiscussion is hidden, thus mark it dirty to
            // force refresh on next open.
            if (discussionDetails) {
                discussionDetails.dirty = true;
            }
            // in case the note has no comments or the comments have already been loaded or we are
            // in classic view just look for the insertCommentPlaceholderMarkerClass
            if (!placeholder) {
                placeholder = discussionWrapper.getElement('.'
                        + this.insertCommentPlaceholderMarkerClass);
            }
        }
        return placeholder;
    },

    /**
     * @override
     */
    createReplaceEditedNotePartialRefreshDescriptor: function(noteId, placeholder) {
        var viewType;
        var refreshDescriptor = this.parent(noteId, placeholder);
        // find the correct viewType for the note
        if (placeholder.getParent('.' + this.inlineCommentsMarkerClass)) {
            viewType = 'COMMENT';
        } else if (placeholder.getParent('.' + this.inlineDiscussionMarkerClass)) {
            viewType = 'THREAD';
        } else {
            viewType = this.selectedViewType;
            refreshDescriptor.additionalParams.viewMode = 'LIST_NOTE';
            refreshDescriptor.additionalParams.renderOnlyNote = true;
        }
        refreshDescriptor.additionalParams.selectedViewType = viewType;
        return refreshDescriptor;
    },

    /**
     * Shows or hides an inline discussion.
     * 
     * @param {String|Number} [noteId] the ID of the note for which the inline discussion should be
     *            shown. Can be null if the discussion is already known to the widget, which will
     *            result in taking the old triggerNoteId as new trigger.
     * @param {String|Number} discussionId the ID of the discussion to be shown
     */
    toggleInlineDiscussion: function(noteId, discussionId, noteContainer) {
        var discussionContainer, highlight, discussionWrapper, inlineNoteElems, discussionDetails;
        // create discussion details object if it does not exist
        discussionDetails = this.inlineDiscussions[discussionId];
        if (!discussionDetails) {
            if (noteId == null) {
                throw 'NoteId must be provided when the discussion does not exist!';
            }
            discussionDetails = {
                triggerNoteId: noteId,
                discussionId: discussionId,
                selectedViewType: 'THREAD',
                dirty: false,
                status: null
            };
            this.inlineDiscussions[discussionId] = discussionDetails;
        }
        if (discussionDetails.status !== 'shown') {
            // there might be some inlineComments that were added by replying to any note of the discussion
            // these should be removed before showing the inline discussion
            inlineNoteElems = this.domNode.getElements('.control-discussion-' + discussionId + ' .'
                    + this.inlineCommentsMarkerClass + ' .' + this.noteContainerMarkerClass);
            // remove that class that creates the separator
            inlineNoteElems.getParent('.control-discussion-wrapper').removeClass(
                    'cn-note-has-inline-comments');
            inlineNoteElems.destroy();
            // TODO notify interactionControlRenderer!
        }
        if (noteId == null || discussionDetails.triggerNoteId == noteId) {
            // differentiate the different states, do nothing if the discussion is loading
            if (discussionDetails.status === 'shown') {
                this.hideDiscussion(discussionDetails);
            } else if (discussionDetails.status === 'hidden'
                    || discussionDetails.status !== 'loading') {
                this.showDiscussion(discussionDetails);
            }
        } else {
            // clicked on link of another note of the discussion
            if (discussionDetails.status === 'hidden') {
                // move the DOM node of the discussion to the triggerNoteId
                discussionContainer = this.domNode.getElementById(discussionDetails.containerId);
                discussionContainer.dispose();
                discussionWrapper = noteContainer.getParent('.control-discussion-wrapper');
                discussionWrapper.grab(discussionContainer);
                discussionDetails.triggerNoteId = noteId;
                highlight = !discussionDetails.dirty;
                this.showDiscussion(discussionDetails);
                if (highlight) {
                    // remove old highlight and set new one
                    discussionContainer.getElement('.' + this.noteHighlightCssClass).removeClass(
                            this.noteHighlightCssClass);
                    this.highlightNoteContainer(this.getNoteContainer(null, noteId,
                            discussionContainer), false);
                }
            }
        }
    },

    /**
     * Toggle the visibility of the notes of an inline discussion. If the inline discussion was
     * shown the notes belonging to the discussion will be hidden. The note whose HTML contains the
     * inline discussion will not be hidden. If the inline discussion was hidden, the notes of the
     * discussion will be made visible again.
     * 
     * @param {Element} startNode The node to start looking for note elements to be hidden or shown
     * @param {Element} inlineDiscussionContainer The wrapper element containing the inline
     *            discussion. Can be null, usually only when the inline discussion was removed due
     *            to removing the note that hosted the inline discussion.
     * @param {Number} discussionId The ID of the discussion
     * @param {Boolean} shown True if the inline discussion was shown by the user, false otherwise.
     */
    toggleDiscussionNodesVisibility: function(startNode, inlineDiscussionContainer, discussionId,
            shown) {
        var discussionNodes, parentNode, discussionEntrySelector, i, noteContainer, noteContainerSelector;
        var modified = false;
        var action = shown ? 'addClass' : 'removeClass';
        discussionEntrySelector = '.control-discussion-' + discussionId;
        noteContainerSelector = '.' + this.noteContainerMarkerClass;
        if (inlineDiscussionContainer) {
            parentNode = inlineDiscussionContainer.getParent(discussionEntrySelector);
        }
        discussionNodes = startNode.getElements(discussionEntrySelector);
        for (i = 0; i < discussionNodes.length; i++) {
            if (discussionNodes[i] != parentNode) {
                discussionNodes[i][action]('cn-hidden');
                // notify control renderer that the discussion nodes are visible/hidden
                noteContainer = discussionNodes[i].getElement(noteContainerSelector);
                this.noteInteractionControlRenderer.noteContainerStateChanged(null, noteContainer,
                        'shown', !shown);
                modified = true;
            } else {
                parentNode[action]('cn-list-blog-entry-discussion-shown');
            }
        }
        if (modified) {
            if (shown) {
                this.hideDateNodes(startNode);
            } else {
                this.showDateNodes(startNode);
            }
        }
    },
    showDateNodes: function(startNode) {
        var i, dateNode, elem, visibleNoteFound;
        var dateNodes = startNode.getElements('.cn-list-date.cn-hidden');
        for (i = 0; i < dateNodes.length; i++) {
            dateNode = dateNodes[i];
            visibleNoteFound = false;
            // check next siblings that aren't date nodes
            elem = dateNode.getNext();
            while (elem && !elem.hasClass('cn-list-date')) {
                if (!elem.hasClass('cn-hidden')) {
                    // if visible is found stop
                    visibleNoteFound = true;
                    break;
                }
                elem = elem.getNext();
            }
            if (visibleNoteFound) {
                dateNode.removeClass('cn-hidden');
            }
        }
    },
    hideDateNodes: function(startNode) {
        var i, dateNode, elem, visibleNoteFound;
        var dateNodes = startNode.getElements('.cn-list-date');
        for (i = 0; i < dateNodes.length; i++) {
            dateNode = dateNodes[i];
            // if already hidden ignore it
            if (dateNode.hasClass('cn-hidden')) {
                continue;
            }
            visibleNoteFound = false;
            // check next siblings that aren't date nodes
            elem = dateNode.getNext();
            while (elem && !elem.hasClass('cn-list-date')) {
                if (!elem.hasClass('cn-hidden')) {
                    // if visible is found stop
                    visibleNoteFound = true;
                    break;
                }
                elem = elem.getNext();
            }
            if (!visibleNoteFound) {
                dateNode.addClass('cn-hidden');
            }
        }
    },

    markDiscussionTriggerNote: function(inlineDiscussionShown, discussionDetails,
            discussionContainer) {
        var noteContainer;
        if (inlineDiscussionShown) {
            noteContainer = this.getNoteContainer(null, discussionDetails.triggerNoteId,
                    discussionContainer.getParent());
            noteContainer.addClass(this.inlineDiscussionShownCssClass);
        } else {
            noteContainer = discussionContainer.getParent().getElement(
                    '.' + this.inlineDiscussionShownCssClass);
            noteContainer.removeClass(this.inlineDiscussionShownCssClass);
        }
    },

    /**
     * Show an inline discussion for the given discussion details. The discussion will be loaded
     * from the server if it wasn't loaded yet or is dirty.
     * 
     * @param {Object} discussionDetails member of the inlineDiscussions object containing the
     *            details of the discussion to be shown
     */
    showDiscussion: function(discussionDetails) {
        var discussionNoteIds;
        var container = this.getDiscussionContainer(discussionDetails, true);
        if (container) {
            container.setStyle('display', '');
            discussionDetails.status = 'shown';
            // refresh if dirty, will change the status to loading
            if (discussionDetails.dirty) {
                this.refreshDiscussion(container, discussionDetails);
            }
            // TODO - note filter is not working anymore!
            this.toggleDiscussionNodesVisibility(this.domNode, container,
                    discussionDetails.discussionId, true);
            // notify interaction control renderer
            discussionNoteIds = this.getNoteIdsOfDiscussion(discussionDetails.discussionId);
            this.noteInteractionControlRenderer.inlineInjectedNoteContainersStateChanged(
                    discussionNoteIds, 'shown', true);
            this.markDiscussionTriggerNote(true, discussionDetails, container);
            this.widgetController.sendEvent('onInlineDiscussionToggled', null, {
                discussionId: discussionDetails.discussionId,
                show: true
            });
        }
    },

    /**
     * Hide a shown inline discussion.
     * 
     * @param {Object} discussionDetails member of the inlineDiscussions object containing the
     *            details of the discussion to be hidden
     */
    hideDiscussion: function(discussionDetails) {
        var discussionNoteIds;
        var container = this.getDiscussionContainer(discussionDetails, false);
        if (container) {
            container.setStyle('display', 'none');
            discussionDetails.status = 'hidden';
            // remove the create note widgets so that the inline discussion DOM node can be
            // relocated when the discussion is opened from another triggerNoteId 
            this.removeWidgetsWithinDiscussion(container, true);
            this.toggleDiscussionNodesVisibility(this.domNode, container,
                    discussionDetails.discussionId, false);
            // notify interaction control renderer
            discussionNoteIds = this.getNoteIdsOfDiscussion(discussionDetails.discussionId);
            this.noteInteractionControlRenderer.inlineInjectedNoteContainersStateChanged(
                    discussionNoteIds, 'shown', false);
            this.markDiscussionTriggerNote(false, discussionDetails, container);
            this.widgetController.sendEvent('onInlineDiscussionToggled', null, {
                discussionId: discussionDetails.discussionId,
                show: false
            });
        }
    },

    /**
     * Get the container element of an inline discussion.
     * 
     * @param {Object} discussionDetails member of the inlineDiscussions object containing the
     *            details of the discussion for which the container should be returned
     * @param {boolean} create when true the container will be created if it does not exist
     */
    getDiscussionContainer: function(discussionDetails, create) {
        var discussionContainer, elem;
        if (discussionDetails.containerId) {
            discussionContainer = this.domNode.getElementById(discussionDetails.containerId);
        } else if (create) {
            // create container if it doesn't exist
            elem = this.getNoteContainer(null, discussionDetails.triggerNoteId);
            if (!elem) {
                return;
            }
            discussionContainer = elem.getNext('.' + this.inlineDiscussionMarkerClass);
            if (!discussionContainer) {
                discussionContainer = new Element('div', {
                    'class': this.inlineDiscussionMarkerClass + ' cn-inline-discussion'
                });
                discussionContainer.id = 'inlineDiscussion_' + discussionDetails.discussionId + '_'
                        + new Date().getTime();
                elem.getParent('.control-discussion-wrapper').grab(discussionContainer);
            }
            discussionDetails.containerId = discussionContainer.id;
            // mark discussion details as dirty to force a refresh
            discussionDetails.dirty = true;
        }
        return discussionContainer;
    },

    /**
     * Refreshes an inline discussion by reloading it from the server. While the refresh is running
     * the status of the associated inlineDiscussions entry will be 'loading'.
     * 
     * @param {Element} discussionContainer the container element of the inline discussion
     * @param {Object} discussionDetails the member of the inlineDiscussions for the discussion to
     *            be refreshed
     */
    refreshDiscussion: function(discussionContainer, discussionDetails) {
        discussionDetails.status = 'loading';
        this.partialRefresh({
            includeFilterParams: false,
            additionalParams: {
                discussionId: discussionDetails.discussionId,
                selectedNoteId: discussionDetails.triggerNoteId,
                viewMode: 'DISCUSSION',
                selectedViewType: discussionDetails.selectedViewType
            },
            domNode: discussionContainer,
            context: {
                discussionId: discussionDetails.discussionId,
                noteId: discussionDetails.triggerNoteId,
                selectedViewType: discussionDetails.selectedViewType,
                inlineInjected: true
            }
        });
    },

    /**
     * Removes the CreateNote widgets opened inside the inline discussion.
     * 
     * @param {Element} discussionContainer the container element of the discussion
     * @param {boolean} cleanup whether to cleanup the HTML of the note containers by calling the
     *            onBeforeXXRemove handlers. If the inner HTML of the inline discussion is removed,
     *            this parameter can be set to false to get a small speed improvement.
     */
    removeWidgetsWithinDiscussion: function(discussionContainer, cleanup) {
        var widgetNodes, i, widgetId;
        if (this.currentCreateNoteWidgets.length > 0) {
            widgetNodes = discussionContainer.getElements('.'
                    + this.widgetController.widgetDivCssClass);
            for (i = 0; i < widgetNodes.length; i++) {
                widgetId = widgetNodes[i].id;
                if (!cleanup) {
                    this.currentCreateNoteWidgets.erase(widgetId);
                }
                this.widgetController.removeWidgetById(widgetId);
            }
        }
    },

    /**
     * @override
     */
    partialRefreshStart: function(domNode, context) {
        if (context.selectedViewType == 'THREAD') {
            this.removeWidgetsWithinDiscussion(domNode, false);
        }
        this.parent(domNode, context);
    },

    /**
     * @override
     */
    partialRefreshComplete: function(domNode, context, responseMetadata) {
        var discussionDetails;
        // when the partial refresh did not replace the placeholders content (insert mode, e.g. loadMore)
        // domNode will be an Elements instance
        if (typeOf(domNode) == 'elements') {
            // expect that the elements of the notes are wrapped by the first element that
            // was added to the DOM, with one exception: the create-new-note case in CLASSIC
            // viewType might contain the date separator as first element
            if (context.insertNote == 'create' && this.selectedViewType === 'CLASSIC'
                    && domNode.length > 1) {
                domNode = domNode[1];
            } else {
                domNode = domNode[0];
            }
            // the new note might have been added after a date separator which is currently hidden because
            // of shown inline discussions or deletion of notes. For performance reasons only search for
            // hidden date separators within the parent container of the added node.
            this.showDateNodes(domNode.getParent());
        }
        this.parent(domNode, context, responseMetadata);
        if (context.loadMore) {
            this.hideShownNotes(domNode);
        } else if (context.insertNote == 'create') {
            // if the list was completely empty we might need to change the hint text at the bottom
            if (this.containsNoNotes) {
                this.domNode.getElements('.control-empty-list').dispose();
                this.domNode.getElements('.control-loadmore-placeholder').setStyle('display', '');
                this.containsNoNotes = false;
            }
        } else if (context.insertNote == 'edit') {
            // invalidate the old cached refers-to tooltip
            this.noteRefererTooltips.noteChanged(context.noteId);
        }
        this.noteRefererTooltips.attach(domNode);
        this.showInlineImages(domNode);
        if (context.discussionId && !context.insertNote) {
            discussionDetails = context.selectedViewType == 'THREAD' ? this.inlineDiscussions[context.discussionId]
                    : this.inlineComments[context.discussionId];
            discussionDetails.status = 'shown';
            discussionDetails.dirty = false;
            this
                    .highlightNoteContainer(this.getNoteContainer(null, context.noteId, domNode),
                            false);
        }
    },

    /**
     * @override
     */
    partialRefreshBeforeRender: function(descr, responseWrapper, responseMetadata) {
        var i, cssClass, dateSeparatorElem, childElems, utils, parentNode;
        if (descr.context.insertNote == 'create' && this.selectedViewType === 'CLASSIC') {
            // response for create in CLASSIC view type contains 2 elements where first is date separator
            // and the 2nd the note wrapper
            // if response contains a date separator whose value matches the first separator in CPL,
            // remove the separator from the response and add the remainder after this existing date separator
            dateSeparatorElem = descr.domNode.getNext('.cn-list-date');
            childElems = responseWrapper.getChildren();
            // check if the date separator is contained in the response
            if (dateSeparatorElem && childElems.length > 1
                    && childElems[0].get('text') === dateSeparatorElem.get('text')) {
                childElems[0].dispose();
                descr.domNode = dateSeparatorElem;
            }
        } else if (descr.context.insertNote == 'edit' && responseWrapper.getChildren().length == 1) {
            // copy CSS classes that should be kept to the new note before inserting
            for (i = 0; i < this.noteWrapperCssClassesToKeepWhenEditing.length; i++) {
                cssClass = this.noteWrapperCssClassesToKeepWhenEditing[i];
                if (descr.domNode.hasClass(cssClass)) {
                    responseWrapper.getChildren().addClass(cssClass);
                }
            }
        } else if (descr.context.insertNote == 'comment') {
            if (this.selectedViewType === 'CLASSIC') {
                // kinda ugly workaround for another special behavior (which cannot be achieved with CSS):
                // if in classic and the comment was added while inline discussion is closed, the
                // discussion wrapper needs to get a special class for correct rendering (note: this
                // class isn't removed correctly)
                utils = communote.utils;
                parentNode = utils.getMatchingParentElement(descr.domNode, '.'
                        + this.inlineCommentsMarkerClass, this.domNode);
                if (parentNode) {
                    parentNode = utils.getMatchingParentElement(descr.domNode,
                            '.control-discussion-wrapper', this.domNode);
                    parentNode.addClass('cn-note-has-inline-comments');
                }
            }
            // if a comment was successfully added increment the discussion counters by 1.
            // Do this before rendering the new comment to avoid updating this note.
            this.updateCommentCountElements(descr.context.discussionId, 1);
        } else if (descr.context.loadMore) {
            // remove duplicate discussions before rendering to avoid the overhead of adding event handlers and stuff
            this.removeDuplicateDiscussions(responseWrapper);
        }
    },

    /**
     * Hide any note element that belongs to an inline discussion which is currently shown. This
     * method won't do anything if the current view type is not CLASSIC.
     * 
     * @param {Element} startNode The node whose children should be checked for the note containers.
     */
    hideShownNotes: function(startNode) {
        var i, details, discussionContainer;
        if (this.selectedViewType === 'CLASSIC') {
            // hide any note in the response that belongs to a shown inline discussion
            for (i in this.inlineDiscussions) {
                if (this.inlineDiscussions.hasOwnProperty(i)) {
                    details = this.inlineDiscussions[i];
                    if (details.status === 'shown' || details.status === 'loading') {
                        discussionContainer = this.getDiscussionContainer(details, false);
                        this.toggleDiscussionNodesVisibility(startNode, discussionContainer,
                                details.discussionId, true);
                    }
                }
            }
        }
    },

    /**
     * Remove any note that is already contained in the discussion view (COMMENT). These kind of
     * duplications can occur if a comment is deleted and the discussion's lastNoteCreationDate is
     * decreased in a way that the discussion moves into the next result set.
     * 
     * @param {Element} startNode The node whose children should be checked.
     */
    removeDuplicateDiscussions: function(startNode) {
        var discussionContainers, discussionIds, i, l, discussionId;
        if (this.selectedViewType === 'COMMENT') {
            discussionContainers = startNode.getElements('.control-discussion-wrapper');
            discussionIds = this.getRenderedDiscussionIds();
            for (i = 0, l = discussionContainers.length; i < l; i++) {
                // take discussion ID from discussion-path data attribute as these wrappers are the root notes 
                discussionId = discussionContainers[i].getProperty('data-discussion-path');
                if (discussionIds.contains(discussionId)) {
                    // dispose is enough because this method is called from beforeRender
                    discussionContainers[i].dispose();
                }
            }
        }
    },

    /**
     * @return the currently selected view type
     */
    getCurrentViewType: function() {
        return this.selectedViewType;
    },

    /**
     * Get the view type for the current preference ID. In case there is none defined the default
     * from the global ui preferences will be returned.
     * 
     * @return {String} the view type
     */
    getPreferedViewType: function() {
        var viewType;
        // load persisted viewType for current preference ID
        if (this.preferenceId) {
            viewType = communoteLocalStorage.getItem(this.preferenceId + '-selectedViewType');
        }
        if (!viewType) {
            // check global preferences for viewType
            viewType = communote.currentUser.preferences.uiPreferences.viewType;
        }
        return viewType;
    },

    /**
     * Change the view type to the provided type and save this view type as default for the current
     * preference ID.
     * 
     * @param {String} [viewType] The view type to set. If omitted the view type of the current
     *            preference ID or the global preferences will be set.
     */
    changeViewType: function(viewType) {
        if (viewType) {
            // persist view type for current preference ID
            if (this.preferenceId) {
                communoteLocalStorage.setItem(this.preferenceId + '-selectedViewType', viewType);
            }
        } else {
            viewType = this.getPreferedViewType();
        }
        this.selectedViewType = viewType;
        this.setFilterParameter('selectedViewType', viewType);
        // only refresh if there was a refresh before to avoid double refreshs
        if (this.firstDOMLoadDone) {
            this.resetLoadMoreState();
            this.refresh();
        }
    }
});
