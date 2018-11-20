var ChronologicalPostListWidget = new Class({

    Extends: C_FilterWidget,
    Implements: [ LoadMoreSupport ],

    autoRefresher: null,

    dataAttributeInlineInjectedNote: 'data-cnt-note-inline-injected',
    /**
     * Option that defines whether newly creates notes should be inserted automatically at top.
     */
    insertNewNotesAutomatically: true,
    /**
     * Option that defines whether edited notes should be replaced automatically with the new
     * content.
     */
    replaceEditedNotesAutomatically: true,
    // handler for clicks on the note body. Will delegate to any registered noteLinkClickHandler if
    // the target was a link and noteBodyClickHandler
    noteClickEventHandler: undefined,

    noteBodyClickHandlers: [],
    noteLinkClickHandlers: [],

    // contains a mapping from note ID (prefixed with n) to the note meta data extracted from the
    // note container HTML. The value can be the serialized JSON or the actual object. The 
    // deserialization is done when the value is accessed for the first time by getNoteMetaData.
    noteMetaData: undefined,

    // CSS class that marks an element as the container of a note. An entry in the CPL can
    // have more than one note container (e.g. when inline comments are shown) 
    noteContainerMarkerClass: 'control-note-wrapper',
    noteHighlightCssClass: 'cn-selected',

    scrollToTopHandler: null,

    widgetGroup: 'blog',

    observedFilterParams: [ 'startDate', 'endDate', 'targetBlogId', 'blogId', 'userId', 'filter',
            'tagPrefix', 'postTextSearchString', 'propertyFilter', 'searchString',
            'showPostsForMe', 'showFavorites', 'showFollowedItems', 'discussionId', 'noteId',
            'selectedNoteId', 'tagIds', 'minRank' ],

    currentCreateNoteWidgets: [],
    
    // TODO maybe Widget parent class should have the eventEmitter
    // for widget specific events
    eventEmitter: undefined,

    init: function() {
        this.parent();
        this.copyStaticParameter('loadComments');
        this.copyStaticParameter('showHeader');
        this.copyStaticParameter('showFooter');
        this.setFilterParameter('viewMode', 'LIST');
        this.createNoteWidgetClass = this.getStaticParameter('createNoteWidgetClass');
        var param = this.getStaticParameter('createNoteWidgetStaticParams');
        if (param) {
            if (typeof param == 'string') {
                this.createNoteWidgetStaticParams = JSON.decode(param);
            } else {
                this.createNoteWidgetStaticParams = param;
            }
        } else {
            this.createNoteWidgetStaticParams = {};
        }
        this.eventEmitter = new communote.classes.EventEmitter();
        this.autoRefresher = new communote.classes.ChronologicalPostListAutoRefresher(this);
        this.noteActionHandler = new communote.classes.NoteActionHandler(this);
        this.noteInteractionControlRenderer = new communote.classes.NoteInteractionControlRenderer(this, this
                .getStaticParameter('interactionControlRendererOptions'));
        this.initLoadMoreSupport();
        this.scrollToTopHandler = new communote.classes.WidgetScrollToTopHandler(this, {
            selector: '.control-cpl-scroll-top'
        });
    },

    addEventListener: function(eventName, fn, context) {
        this.eventEmitter.on(eventName, fn, context);
    },

    /**
     * Add handler function that will be invoked when a click inside a note wrapper element
     * occurred. The handlers registered here will be called after any noteLinkClickHandler
     * registered with addNoteLinkClickHandler. If one of the noteLinkClickHandler stopped the
     * propagation of the event no handler will be called. Registered handlers are called in the
     * order they were attached.
     * 
     * @param {Function} handler The function to call. The function will be passed the clicked
     *            element, the event and a details object that contains a reference to the widget
     *            instance, the noteId and the noteContainer element. If the function returns false,
     *            utils.eventStop() will be called. Other handlers will still be called for the
     *            current event. To avoid this set event.immediatePropagationStopped to true.
     * @param {String} [selector] If provided it will be used to check the clicked link. If it does
     *            not match the selector the handler is not called.
     * @param {Boolean} checkParents If selector is given this parameter defines whether the
     *            selector will also checked against the parents of the clicked element. If a parent
     *            matches, this element will be passed to the handler function as clicked element.
     * @param {Object} [data] An arbitrary object that if provided will be added to the details.
     */
    addNoteBodyClickHandler: function(handler, selector, checkParents, data) {
        var handlerDef;
        if (handler) {
            handlerDef = {};
            handlerDef.callback = handler;
            if (selector) {
                handlerDef.selector = selector;
                handlerDef.checkParents = checkParents;
            }
            if (data) {
                handlerDef.data = data;
            }
            this.noteBodyClickHandlers.push(handlerDef);
        }
    },

    /**
     * Add handler function that will be invoked when a link inside a note wrapper element got
     * clicked. Registered handlers are called in the order they were attached.
     * 
     * @param {Function} handler The function to call. The function will be passed the clicked link
     *            element, the event and a details object that contains a reference to the widget
     *            instance, the noteId and a reference to the noteContainer. If the function returns
     *            false, utils.eventStop() will be called. Other handlers will still be called for
     *            the current event. To avoid this set event.immediatePropagationStopped to true. To
     *            avoid calling the noteBody handlers stop the propagation of the event with
     *            utils.eventStopPropagation.
     * @param {String} [selector] If provided it will be used to check the clicked link. If it does
     *            not match the selector the handler is not called.
     * @param {Object} [data] An arbitrary object that if provided will be added to the details.
     */
    addNoteLinkClickHandler: function(handler, selector, data) {
        var handlerDef;
        if (handler) {
            handlerDef = {};
            handlerDef.callback = handler;
            if (selector) {
                handlerDef.selector = selector;
            }
            if (data) {
                handlerDef.data = data;
            }
            this.noteLinkClickHandlers.push(handlerDef);
        }
    },

    /**
     * @override
     */
    afterShow: function(initPhase, isDirty) {
        if (initPhase) {
            this.noteInteractionControlRenderer.widgetAfterShow(isDirty);
        } else {
            if (!isDirty) {
                this.resumeLoadMoreSupport();
            }
            this.autoRefresher.widgetAfterShow(isDirty);
            this.noteInteractionControlRenderer.widgetAfterShow(isDirty);
            this.scrollToTopHandler.widgetAfterShow(isDirty);
        }
        this.eventEmitter.emit('widgetShown', {initPhase: initPhase, isDirty: isDirty});
    },

    /**
     * @override
     */
    beforeHide: function(initPhase) {
        if (initPhase) {
            this.noteInteractionControlRenderer.widgetBeforeHide();
        } else {
            this.pauseLoadMoreSupport();
            this.autoRefresher.widgetBeforeHide();
            this.noteInteractionControlRenderer.widgetBeforeHide();
            this.scrollToTopHandler.widgetBeforeHide();
        }
    },

    /**
     * @override
     */
    beforeRemove: function() {
        this.parent();
        this.cleanup();
        this.autoRefresher.widgetBeforeRemove();
        this.disposeLoadMoreSupport();
    },

    beforeRemoveCommentNoteWidget: function(widget) {
        widget.domNode.setStyle('display', 'none');
    },

    beforeRemoveEditNoteWidget: function(widget) {
        widget.domNode.setStyle('display', 'none');
    },

    beforeRemoveRepostNoteWidget: function(widget) {
        widget.domNode.setStyle('display', 'none');
    },

    /**
     * Called before the widget for commenting a note is created.
     * 
     * @param noteContainer {Element} the element wrapping the note data
     * @return {Element} the element that should be the parent node of the widget. If null is
     *         returned the widget will not be created.
     */
    beforeShowCommentNoteWidget: function(noteContainer) {
        return null;
    },

    /**
     * Called before the widget for editing a note is created.
     * 
     * @param noteContainer {Element} the element wrapping the note data
     * @return {Element} the element that should be the parent node of the widget. If null is
     *         returned the widget will not be created.
     */
    beforeShowEditNoteWidget: function(noteContainer) {
        return null;
    },

    /**
     * Called before the widget for reposting a note is created.
     * 
     * @param noteContainer {Element} the element wrapping the note data
     * @return {Element} the element that should be the parent node of the widget. If null is
     *         returned the widget will not be created.
     */
    beforeShowRepostNoteWidget: function(noteContainer) {
        return null;
    },

    /**
     * Free any resources hold by the widget. This method should be called before doing a full
     * refresh or removing the widget.
     */
    cleanup: function() {
        var i, noteContainers;
        var widgets = this.currentCreateNoteWidgets;
        if (widgets) {
            for (i = 0; i < widgets.length; i++) {
                this.widgetController.removeWidgetById(widgets[i]);
            }
            widgets.empty();
        }
        if (this.firstDOMLoadDone) {
            // remove hovercards because mootools leaks memory
            communote.hoverCardManager.detachHoverCards(this.domNode, null, null);
            noteContainers = this.detachNoteClickEvents();
            this.noteInteractionControlRenderer.widgetCleanup(noteContainers);
            this.noteMetaData = undefined;
        }
        this.scrollToTopHandler.widgetCleanup();
    },

    /**
     * <p>
     * Create the descriptor to do a partial refresh of the widget which inserts a comment to a note
     * at a specified position. The descriptor will by default define that the comment should be
     * inserted before the placeholder element returned by #getInsertNewCommentPlaceholder. If
     * #getInsertNewCommentPlaceholder does not return a placeholder the partial refresh will not be
     * executed.
     * </p>
     * <p>
     * The context of the partial refresh will contain the following members:
     * <ul>
     * {String} insertNote A member with value 'comment' to mark the refresh as 'insert a comment'
     * operation
     * </ul>
     * <ul>
     * {Number} noteId The note ID of the comment to insert
     * </ul>
     * <ul>
     * {Number} parentNoteId The note ID of the note the comment is a reply to
     * </ul>
     * </p>
     * 
     * @param {Number} noteId The note ID of the comment to insert
     * @param {Number} parentNoteId The ID of the note the comment is a reply to
     * @param {Number} discussionId The ID of the discussion the comment belongs to
     * @param {Element} placeholder The placeholder element before which the new comment should be
     *            added
     * @return {Object} The refresh descriptor or null
     */
    createInsertNewCommentPartialRefreshDescriptor: function(noteId, parentNoteId, discussionId,
            placeholder) {
        // comments should be always visible so we exclude the filter params
        return {
            includeFilterParams: false,
            additionalParams: {
                noteId: noteId,
                viewMode: 'DISCUSSION_NOTE'
            },
            domNode: placeholder,
            insertMode: 'before',
            context: {
                insertNote: 'comment',
                noteId: noteId,
                parentNoteId: parentNoteId,
                discussionId: discussionId
            }
        };
    },

    createReplaceEditedNotePartialRefreshDescriptor: function(noteId, placeholder) {
        return {
            includeFilterParams: false,
            additionalParams: {
                noteId: noteId,
                viewMode: 'DISCUSSION_NOTE'
            },
            domNode: placeholder,
            insertMode: 'replace',
            context: {
                insertNote: 'edit',
                noteId: noteId,
                // just copy the attribute
                inlineInjected: placeholder.getAttribute(this.dataAttributeInlineInjectedNote) === 'true'
            }
        };
    },

    /**
     * Remove all note click events from widget DOM that were added before.
     */
    detachNoteClickEvents: function() {
        var noteContainers = this.domNode.getElements('.' + this.noteContainerMarkerClass);
        noteContainers.removeEvents('click');
        // remove handler so other components can check whether the note clicks should still be handled
        this.noteClickEventHandler = null;
        return noteContainers;
    },

    /**
     * @override
     */
    filterParametersChanged: function(changedParam) {
        this.resetLoadMoreState();
        this.refresh();
    },

    /**
     * Called when an item like topic, tag or user was followed or unfollowed. If the current view
     * shows the followed items the widget will be refreshed.
     */
    followStatusChanged: function() {
        // refresh if showing followed items
        if (this.filterParamStore.getFilterParameter('showFollowedItems')) {
            // reset paging info in case of paging
            this.resetLoadMoreState();
            this.refresh();
        }
    },

    /**
     * Get the placeholder element before which the new comment should be added. The default
     * implementation does return null.
     * 
     * @param {Number} noteId The note ID of the comment to insert
     * @param {Number} parentNoteId The ID of the note the comment is a reply to
     * @param {Element} parentNoteContaainer The note container of the parent note the comment is a
     *            reply to
     * @param {Number} discussionId The ID of the discussion the comment belongs to
     * @return {Element} The placeholder or null if there is none. In case null is returned the
     *         comment will not be added to the DOM.
     */
    getInsertNewCommentPlaceholder: function(noteId, parentNoteId, parentNoteContainer,
            discussionId) {
        // there is no generic way to find that placeholder thus, subclasses need to implement it 
        return null;
    },

    /**
     * @override
     */
    getListeningEvents: function() {
        return this.parent().combine(
                [ 'onUserLogoChanged', 'onUserProfileChanged', 'onTimeZoneChanged', 'onBlogUpdate',
                        'onCurrentUserBlogRoleChanged', 'onNotesChanged', 'onWidgetRemove',
                        'onReloadPostList', 'onItemFollowed', 'onItemUnfollowed',
                        'onNoteFavorStateChanged', 'onNoteLikeStateChanged', 'onDiscussionMoved',
                        'onResponsiveAttributesChanged']);
    },

    /**
     * @return {Function} the lazily initialized handler for clicks on the note container
     */
    getNoteClickEventHandler: function() {
        var self;
        if (!this.noteClickEventHandler) {
            self = this;
            this.noteClickEventHandler = function(event) {
                self.handleNoteClick(this, event);
            }
        }
        return this.noteClickEventHandler;
    },

    /**
     * <p>
     * Returns the element that is the wrapper or container element of a single note in the note
     * list. The container can be retrieved relative to a child element or by the ID of the note. If
     * both parameters are present the faster retrieval should be used. If the note is contained
     * more than once only the first visible container is returned.
     * </p>
     * The default implementation looks for an element with CSS class 'control-note-noteId'.
     * 
     * @param {Element} [childElement] an element that is a child of the note container to return
     * @param {String|Number} [noteId] the ID of the note whose container should be returned
     * @param {Element} [startNode] optional parameter to restrict the search for the note with the
     *            given id to the children of startNode
     * @return {Element} the container element. If neither childElement or noteId is specified
     *         nothing should be returned
     */
    getNoteContainer: function(childElement, noteId, startNode) {
        var elem, i;
        if (childElement) {
            // TODO use optimized parent search of utils!
            elem = document.id(childElement).getParent('.' + this.noteContainerMarkerClass);
        } else if (noteId != null) {
            if (!startNode) {
                startNode = this.domNode;
            }
            elem = startNode.getElements('.control-note-' + noteId);
            if (elem.length > 0) {
                // in case of threaded-view there might be more than 1 results, return the first that is visible
                if (elem.length == 1) {
                    elem = elem[0];
                } else {
                    for (i = 0; i < elem.length; i++) {
                        if (elem[i].isVisible()) {
                            elem = elem[i];
                            break;
                        }
                    }
                }
            }
        }
        return typeOf(elem) === 'element' ? elem : null;
    },

    getNoteContainerByType: function(noteId, inlineInjected) {
        var i, l, inlineInjectedNote;
        var noteContainers = this.getNoteContainers(noteId);
        for (i = 0, l = noteContainers.length; i < l; i++) {
            inlineInjectedNote = noteContainers[i]
                    .getAttribute(this.dataAttributeInlineInjectedNote) === 'true';
            if (inlineInjected === inlineInjectedNote) {
                return noteContainers[i];
            }
        }
        return null;
    },

    /**
     * Returns the elements that are the wrapper or container elements of a single note in the note
     * list. If the note is shown more than once this method will return all containers, otherwise
     * it will return the same as getNoteContainer.
     * 
     * @param {String|Number} [noteId] the ID of the note whose container should be returned
     * @return {Elements} the container elements or an empty array if nothing is found
     */
    getNoteContainers: function(noteId) {
        if (noteId != null) {
            return this.domNode.getElements('.control-note-' + noteId);
        }
        return [];
    },

    /**
     * Get the IDs of the notes which are rendered in the widget. If a filter is defined the noteId
     * is only returned if the filter returns true.
     * 
     * @param {Function} [filter] A function to call before adding the noteId. The function is
     *            passed the noteMetaData object and if provided the filterArgs object. The function
     *            should return true if the noteId should be included in the result.
     * @return {Object} [filterArgs] An argument to be passed to the filter function.
     */
    getNoteIds: function(filter, filterArgs) {
        var metaData, i, noteId;
        var result = [];
        if (this.noteMetaData) {
            for (i in this.noteMetaData) {
                noteId = i.substring(1);
                metaData = this.getNoteMetaData(noteId);
                if (!filter || filter.call(null, metaData, filterArgs)) {
                    result.push(metaData.noteId);
                }
            }
        }
        return result;
    },

    /**
     * Get the IDs of the notes which are rendered in the widget and are part of a discussion.
     * 
     * @param {Number|String} discussionId ID of the discussion
     * @return {Number[]} the IDs of the notes of the discussion or an empty array
     */
    getNoteIdsOfDiscussion: function(discussionId) {
        var filterByDiscussionId = function(metaData) {
            return metaData.discussionId == discussionId;
        };
        return this.getNoteIds(filterByDiscussionId);
    },

    /**
     * Get the meta data that was extracted from a note container rendered in the widget.
     * 
     * @param {Number|String} noteId ID of the note
     * @return {Object} the note meta data or null
     */
    getNoteMetaData: function(noteId) {
        var metaData;
        if (this.noteMetaData) {
            metaData = this.noteMetaData['n' + noteId];
            if (metaData) {
                if (typeof metaData === 'string') {
                    // still a string
                    metaData = JSON.decode(metaData);
                    this.noteMetaData['n' + noteId] = metaData;
                    // enrich with noteId
                    metaData.noteId = parseInt(noteId);
                }
            }
        }
        return metaData;
    },

    /**
     * Get the meta data that was extracted from a note container rendered in the widget for all
     * notes of a discussion.
     * 
     * @param {Number|String} discussionId ID of the discussion
     * @return {Object[]} the note meta data for all notes of the discussion or an empty array
     */
    getNoteMetaDataForDiscussion: function(discussionId) {
        var metaData, i, noteId;
        var result = [];
        if (this.noteMetaData) {
            for (i in this.noteMetaData) {
                noteId = i.substring(1);
                metaData = this.getNoteMetaData(noteId);
                if (metaData.discussionId === discussionId) {
                    result.push(metaData);
                }
            }
        }
        return result;
    },

    getRenderedDiscussionIds: function() {
        var metaData, i, noteId;
        var result = [];
        if (this.noteMetaData) {
            for (i in this.noteMetaData) {
                noteId = i.substring(1);
                metaData = this.getNoteMetaData(noteId);
                result.include(metaData.discussionId);
            }
        }
        return result;
    },

    getReplaceEditedNotePlaceholder: function(noteId) {
        return this.getNoteContainers(noteId);
    },

    handleNoteClick: function(noteElement, event) {
        var linkElement, bodyElement, i, handlerDef, details;
        var linkHandlerCount = this.noteLinkClickHandlers.length;
        var bodyHandlerCount = this.noteBodyClickHandlers.length;
        var utils = communote.utils;
        if (linkHandlerCount || bodyHandlerCount) {
            details = {};
            details.widget = this;
            details.noteId = noteElement.getProperty('data-cnt-note-id');
            details.noteContainer = noteElement;
        }
        linkElement = utils.getClickedLinkElement(event);
        if (linkHandlerCount) {
            if (linkElement) {
                for (i = 0; i < linkHandlerCount; i++) {
                    handlerDef = this.noteLinkClickHandlers[i];
                    if (!handlerDef.selector || linkElement.match(handlerDef.selector)) {
                        details.data = handlerDef.data;
                        if (handlerDef.callback.call(null, linkElement, event, details) === false) {
                            utils.eventStop(event);
                        }
                        if (!this.noteClickEventHandler) {
                            // stop eventhandling if the click handler was removed (e.g. widget remove or refresh)
                            return;
                        }
                        if (event.immediatePropagationStopped) {
                            break;
                        }
                    }
                }
            }
        }
        if (linkElement && linkElement.hasClass('control-entity-link')) {
            utils.invokeLinkHandlerOnClickEvent(event, linkElement);
            if (!this.noteClickEventHandler) {
                return;
            }
        }
        if (!event.immediatePropagationStopped && !utils.isEventPropagationStopped(event)
                && bodyHandlerCount > 0 && event.target.tagName !== 'INPUT'
                && utils.getSelectedText(event.target.ownerDocument).length == 0) {
            for (i = 0; i < bodyHandlerCount; i++) {
                handlerDef = this.noteBodyClickHandlers[i];
                // if there is a selector only call handler if the selector matches
                if (!handlerDef.selector) {
                    bodyElement = event.target;
                } else {
                    if (handlerDef.checkParents) {
                        // stop at the element that received the click
                        bodyElement = utils.getMatchingParentElement(event.target,
                                handlerDef.selector, noteElement);
                    } else if (event.target.match(handlerDef.selector)) {
                        bodyElement = event.target;
                    } else {
                        bodyElement = null;
                    }
                }
                if (bodyElement) {
                    details.data = handlerDef.data;
                    if (handlerDef.callback.call(null, bodyElement, event, details) === false) {
                        utils.eventStop(event);
                    }
                    if (!this.noteClickEventHandler) {
                        return;
                    }
                    if (event.immediatePropagationStopped) {
                        break;
                    }
                }
            }
        }
        // notify autorefresher if clicked somewhere in widget, but ignore clicks in embedded
        // CreateNote widgets (when removing a CreateNote widget with a click on cancel, this
        // click will notify the autorefresher)
        if (this.currentCreateNoteWidgets.length == 0
                || !utils.getMatchingParentElement(event.target, '.'
                        + this.widgetController.widgetDivCssClass, noteElement)) {
            this.autoRefresher.notifyAboutInteraction();
        }
    },

    /**
     * Scrolls the ChronologicalPostList to top, bottom or to a defined element.
     */
    handleNoteListScroll: function(target) {
        var container, containerPos, winScrollY;
        if (!target) {
            // set domNode to scroll to top of widget
            if (this.loadMore.previousPagingOffset != null) {
                target = this.domNode;
                this.loadMore.previousPagingOffset = null;
            }
        }
        if (target) {
            container = document.id(target);
            if (container != null) {
                containerPos = container.getPosition();
                winScrollY = window.getScroll().y;

                if (winScrollY > containerPos.y && target == this.domNode) {
                    window.scrollTo(containerPos.x, containerPos.y);
                } else {
                    container.scrollIntoView();
                }
            }
        }
    },

    /**
     * Highlight a note by adding the noteHighlightCssClass.
     * 
     * @param {Number} noteId The ID of the note to highlight
     * @return {Element} the container of the note the CSS class was added to
     */
    highlightNote: function(noteId) {
        if (noteId != null) {
            return this.highlightNoteContainer(this.getNoteContainer(null, noteId), true);
        }
        return null;
    },

    highlightNoteContainer: function(noteDiv) {
        // might be invisible if CPL is filtered or the note was removed
        if (noteDiv) {
            noteDiv.addClass(this.noteHighlightCssClass);
            return noteDiv;
        }
        return null;
    },

    insertCreateNoteWidget: function(node, newWidgetId, staticParams) {
        var widget;
        if (this.createNoteWidgetStaticParams) {
            staticParams = Object.merge({}, staticParams, this.createNoteWidgetStaticParams);
        }
        widget = widgetController.addWidget(node, this.createNoteWidgetClass, newWidgetId,
                staticParams);
        this.currentCreateNoteWidgets.include(widget.widgetId);
        return widget;
    },

    /**
     * Insert the HTML of a single comment that was created by replying to a note shown in the CPL.
     */
    insertNewComment: function(noteId, parentNoteId, discussionId) {
        var parentNoteContainer, refreshDescriptor, placeholder;
        parentNoteContainer = this.getNoteContainer(null, parentNoteId);
        if (parentNoteContainer) {
            placeholder = this.getInsertNewCommentPlaceholder(noteId, parentNoteId,
                    parentNoteContainer, discussionId);
            if (placeholder) {
                refreshDescriptor = this.createInsertNewCommentPartialRefreshDescriptor(noteId,
                        parentNoteId, discussionId, placeholder);
                this.partialRefresh(refreshDescriptor);
                return true;
            }
        }
        return false;
    },

    insertNewNote: function(noteId) {
        var placeholderElem;
        // only insert the new note if the current filter does not contain the noteId filter
        if (!this.filterParamStore.getFilterParameter('noteId')) {
            placeholderElem = this.domNode.getElement('.control-new-note-placeholder');
            if (placeholderElem) {
                this.partialRefresh({
                    includeFilterParams: true,
                    additionalParams: {
                        noteId: noteId,
                        viewMode: 'LIST_NOTE'
                    },
                    domNode: placeholderElem,
                    insertMode: 'after',
                    context: {
                        insertNote: 'create',
                        noteId: noteId,
                        inlineInjected: false
                    }
                });
            }
        }
    },

    /**
     * Call a note action. If the action is not known nothing will happen.
     * 
     * @param {String} action The name of the action to invoke
     * @param {Object} details An object containing the noteId, a reference to the noteContainer for
     *            which the action was invoked, a reference to the CPL widget instance and a
     *            triggeringEvent field that holds the event that caused the action. The
     *            triggeringEvent field can be missing if there is no associated event.
     * @return {Boolean} whether the action was handled
     */
    invokeNoteAction: function(action, details) {
        return this.noteActionHandler.invokeAction(action, details);
    },

    /**
     * @override Implementation of LoadSupport method which sets the new parameters to get more data
     */
    loadMoreHasMoreData: function(responseMetadata) {
        this.setLoadMoreParameters({
            retrieveOnlyNotesBeforeId: responseMetadata.lastNoteId,
            retrieveOnlyNotesBeforeDate: responseMetadata.lastNoteCreationTimestamp,
            showHeader: false,
            showFooter: false
        });
    },

    onBlogUpdate: function(blogData) {
        // refresh when alias or title changed to correct note headers and permalinks
        if (blogData && (blogData.newTitle || blogData.newAlias)) {
            this.refresh();
        }
    },
    onCurrentUserBlogRoleChanged: function() {
        this.resetLoadMoreState();
        this.refresh();
    },
    onDiscussionMoved: function(details) {
        // TODO could be cleverer if evaluating the details (e.g. in topicSelected context and 
        // selected topic is old topic -> just remove notes)
        this.resetLoadMoreState();
        this.refresh();
    },

    /**
     * Event handler that is called when the current user starts following an item like a tag, user
     * or topic.
     * 
     * @param {Object} params object holding details about the followed item
     */
    onItemFollowed: function(params) {
        this.followStatusChanged();
    },

    /**
     * Event handler that is called when the current user stops following an item like a tag, user
     * or topic.
     * 
     * @param {Object} params object holding details about the followed item
     */
    onItemUnfollowed: function(params) {
        this.followStatusChanged();
    },

    /**
     * Event handler that is invoked when a note was added or removed from the favorites/bookmarks.
     * 
     * @param {Object} details Object containing the noteId and the favorite flag holding the new
     *            state
     */
    onNoteFavorStateChanged: function(details) {
        var metaData;
        if (this.filterParamStore.getFilterParameter('showFavorites')) {
            // TODO when in endless scroll mode the scrolled content will be lost, not nice but is there a workaround?
            this.refresh();
        } else {
            // check if something changed and the note exists in the view
            metaData = this.getNoteMetaData(details.noteId);
            if (metaData && metaData.favorite != details.favorite) {
                metaData.favorite = details.favorite;
                this.noteInteractionControlRenderer.noteStateChanged(details.noteId, 'favorite',
                        details.favorite);
            }
        }
    },

    /**
     * Event handler that is invoked when a note liked or unliked.
     * 
     * @param {Object} details Object containing the noteId and the liked flag holding the new state
     */
    onNoteLikeStateChanged: function(details) {
        var noteContainers, metaData;
        // check if something changed and the note exists in the view
        metaData = this.getNoteMetaData(details.noteId);
        if (metaData && metaData.liked != details.liked) {
            metaData.liked = details.liked;
            if (metaData.liked) {
                metaData.likeCount++;
            } else {
                metaData.likeCount--;
            }
            this.noteInteractionControlRenderer.noteStateChanged(details.noteId, 'liked',
                    details.liked);
        }
    },

    onNotesChanged: function(params) {
        var firstPage, paging;
        if (this.isHidden()) {
            // only notify autorefresher about new comments or notes when hidden, but do no action to avoid
            // unnecessary requests for cases where the widget isn't shown again  
            if (params.action === 'create' || params.action === 'comment') {
                this.autoRefresher.notifyAboutNewNote(params.noteId);
            }
            return;
        }
        paging = this.getLoadMoreMode() == 'paging';
        firstPage = !paging || this.getFilterParameter('offset') == '0';
        // directly insert new notes if we are not in paging mode or when on first page when in paging
        if (params.action == 'create' && this.insertNewNotesAutomatically && firstPage) {
            this.insertNewNote(params.noteId);
            return;
        }
        if (params.action == 'edit' && this.replaceEditedNotesAutomatically) {
            this.replaceEditedNote(params.noteId);
        } else if (params.action == 'comment') {
            this.insertNewComment(params.noteId, params.parentNoteId, params.discussionId);
        } else if (params.action == 'edit' || params.action == 'delete'
                || (paging && this.getFilterParameter('offset') == '0')
                || (!paging && params.action == 'create')) {
            // fallback to old behavior

            // don't refresh if viewing a discussion and creating a note
            if (params.action != 'create'
                    || this.filterParamStore.getFilterParameter('discussionId') == null) {
                // when in paging mode we only want the current page to be refreshed -> don't reset the state
                if (!paging) {
                    this.resetLoadMoreState();
                }
                this.refresh();
            }
        }
    },

    onReloadPostList: function() {
        // TODO when in endless scroll mode the scrolled content will be lost, not nice but is there a workaround?
        this.refresh();
    },
    
    /**
     * Handler for the widget event 'onResponsiveAttributesChanged'.
     * 
     * @param {String[]} Names of the changed attributes
     */
    onResponsiveAttributesChanged: function(changedAttributes) {
        // can ignore simple changes of the viewportWidth attribute because already listening
        // to the resize event 
        if (changedAttributes.length
                && (changedAttributes.length > 1 || changedAttributes[0] !== 'viewportWidth')) {
            // this is butt-ugly: CSS transitions can lead to wrong calculations, so delay the
            // calculation. Could use the transitionend, but this is ugly too... 
            setTimeout(this.noteInteractionControlRenderer.reRenderAdaptivelyOnResize.bind(this.noteInteractionControlRenderer), 750);
        }
    },

    onTimeZoneChanged: function() {
        // TODO when in endless scroll mode the scrolled content will be lost, not nice but is there a workaround?
        this.refresh();
    },

    onUserLogoChanged: function(imagePath) {
        var i;
        var imgElems = this.domNode.getElements('img');
        var userIdParamString = 'userId=' + communote.currentUser.id;
        for (i = 0; i < imgElems.length; i++) {
            if (imgElems[i].src.indexOf(userIdParamString) > 0) {
                imgElems[i].src = imagePath;
            }
        }
    },

    onUserProfileChanged: function() {
        // TODO when in endless scroll mode the scrolled content will be lost, not nice but is there a workaround?
        this.refresh();
    },

    /**
     * Handler for the remove lifecycle event to cleanup before an inner CreateNoteWidget is
     * removed.
     * 
     * @param {Object} details Object with members widgetId that holds the ID and widgetType that
     *            holds the type of the widget to be removed.
     */
    onWidgetRemove: function(details) {
        var oldLength, widget;
        var widgets = this.currentCreateNoteWidgets;
        if (widgets) {
            oldLength = widgets.length;
            if (widgets.erase(details.widgetId).length != oldLength) {
                widget = this.widgetController.getWidget(details.widgetId);
                if (widget.action == 'edit') {
                    this.beforeRemoveEditNoteWidget(widget);
                } else if (widget.action == 'comment') {
                    this.beforeRemoveCommentNoteWidget(widget);
                } else {
                    this.beforeRemoveRepostNoteWidget(widget);
                }
            }
        }
    },

    /**
     * @override
     */
    partialRefreshComplete: function(domNode, context, responseMetadata) {
        this.loadMoreRefreshComplete(context, responseMetadata);
        this.prepareNewNoteContainers(domNode, !!context.inlineInjected);
        communote.hoverCardManager.attachHoverCards(domNode, null, null);
    },

    /**
     * @override
     */
    partialRefreshStart: function(domNode, context) {
        this.loadMoreRefreshStart(context);
    },

    /**
     * Prepare the new note containers by adding required event handlers, extracting the note
     * metaData and notifying the NoteInteractionControlRenderer about the new note.
     * 
     * @param {Element} startNode The new note container or a parent node where the new note
     *            containers should be searched in
     * @param {Boolean} inlineInjectedNotes Whether the note containers represent inline-injected
     *            notes, which are notes that are rendered inline (as part of an inline discussion
     *            or expanded comments) and were not loaded during the first refresh or a loadMore
     *            operation. When true, the note containers will be marked as inlineInjectedNotes
     *            with the help of the dataAttributeInlineInjectedNote data- attribute. Since a note
     *            can occur twice in the stream, this marker can be used to easily decide whether a
     *            note was loaded and injected later, without checking whether the note is a child
     *            of some parent node each time this differentiation is required.
     */
    prepareNewNoteContainers: function(startNode, inlineInjectedNotes) {
        var entries, i, l, clickHandler, noteContainer, noteId;
        if (startNode.hasClass(this.noteContainerMarkerClass)) {
            entries = Array.from(startNode);
        } else {
            entries = startNode.getElements('.' + this.noteContainerMarkerClass);
        }
        clickHandler = this.getNoteClickEventHandler();
        for (i = 0, l = entries.length; i < l; i++) {
            noteContainer = entries[i];
            noteContainer.addEvent('click', clickHandler);
            if (inlineInjectedNotes) {
                noteContainer.setAttribute(this.dataAttributeInlineInjectedNote, 'true');
            }
            noteId = noteContainer.getAttribute('data-cnt-note-id');
            // for performance: save serialized JSON and deserialize lazily on access
            this.noteMetaData['n' + noteId] = noteContainer.getAttribute('data-cnt-note-meta-data');
            this.noteInteractionControlRenderer.prepareNote(noteId, noteContainer);
            this.eventEmitter.emit('noteContainerPrepared', {id: noteId, container: noteContainer});
        }
    },

    /**
     * @override
     */
    refreshComplete: function(responseMetadata) {
        var selectedNoteId, highlightedNoteElem;
        var discussionId = this.filterParamStore.getFilterParameter('discussionId');
        if (discussionId != null) {
            selectedNoteId = this.filterParamStore.getFilterParameter('selectedNoteId');
            highlightedNoteElem = this.highlightNote(selectedNoteId);
        }
        this.handleNoteListScroll(highlightedNoteElem);
        this.autoRefresher.widgetRefreshComplete(responseMetadata);
        this.loadMoreRefreshComplete(null, responseMetadata);
        this.noteInteractionControlRenderer.widgetRefreshComplete(responseMetadata);
        communote.hoverCardManager.attachHoverCards(this.domNode, null, null);
        this.noteMetaData = {};
        this.prepareNewNoteContainers(this.domNode, false);
        this.scrollToTopHandler.widgetRefreshComplete();

        // fire an event to inform other widgets that the user loaded the newest notes for some filter parameters
        E('onNotesLoaded', {
            firstNoteCreationTimestamp: responseMetadata.firstNoteCreationTimestamp,
            lastNoteCreationTimestamp: responseMetadata.lastNoteCreationTimestamp,
            filterParameters: this.filterParamStore.getCurrentFilterParameters()
        });
    },

    /**
     * @override
     */
    refreshStart: function() {
        this.cleanup();
        this.autoRefresher.widgetRefreshStart();
        this.loadMoreRefreshStart(null);
        this.eventEmitter.emit('widgetRefreshing');
    },

    removeEventListener: function(eventName, fn, context) {
        this.eventEmitter.off(eventName, fn, context);
    },
    
    removeNoteMetaData: function(noteIds) {
        var i;
        noteIds = Array.from(noteIds);
        for (i = noteIds.length; i--;) {
            delete this.noteMetaData['n' + noteIds[i]];
        }
    },

    replaceEditedNote: function(noteId) {
        var i, refreshDescriptor, refreshed;
        var placeholders = this.getReplaceEditedNotePlaceholder(noteId);
        if (placeholders) {
            for (i = 0; i < placeholders.length; i++) {
                refreshDescriptor = this.createReplaceEditedNotePartialRefreshDescriptor(noteId,
                        placeholders[i]);
                if (refreshDescriptor) {
                    this.noteInteractionControlRenderer.noteContainerStateChanged(noteId, refreshDescriptor.domNode, 
                            'replace', true);
                    this.partialRefresh(refreshDescriptor);
                    refreshed = true;
                }
            }
        }
        return !!refreshed;
    },

    toggleLikingUsers: function(noteContainer) {
        if (noteContainer) {
            noteContainer.getElement('.cn-like-meta-users').toggleClass('cn-hidden');
        }
    }

});
