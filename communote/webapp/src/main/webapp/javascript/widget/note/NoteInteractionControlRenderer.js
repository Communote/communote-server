(function(namespace, window) {
    var classNamespace;

    function getHorizontalOffsetValue(element, type) {
        var leftOffset, rightOffset, computedStyle;
        // cannot use mootools Element.getComputedStyle as it might return non-pixel values for IE < 9
        if (window.getComputedStyle) {
            computedStyle = window.getComputedStyle(element);
            leftOffset = Math.round(parseFloat(computedStyle.getPropertyValue(type + '-left')));
            rightOffset = Math.round(parseFloat(computedStyle.getPropertyValue(type + '-right')));
        } else {
            if (element.currentStyle) {
                leftOffset = element.currentStyle[type + 'Left'];
                if (leftOffset == '0') {
                    leftOffset = 0;
                } else if (typeof leftOffset === 'string'
                        && leftOffset.indexOf('px') === leftOffset.length - 2) {
                    leftOffset = Math.round(parseFloat(leftOffset));
                } else {
                    // no way to calculate margins correctly 
                    // note: using element position (e.g. get current pos, set margin to 0,
                    // get pos again and calculate difference) does not work since margin might
                    // cause a line-wrap
                    throw "Offset calculation not supported";
                }
                rightOffset = element.currentStyle[type + 'Right'];
                if (rightOffset == '0') {
                    rightOffset = 0;
                } else if (typeof rightOffset === 'string'
                        && rightOffset.indexOf('px') === rightOffset.length - 2) {
                    rightOffset = Math.round(parseFloat(rightOffset));
                } else {
                    throw "Offset calculation not supported";
                }
            } else {
                // no idea how to calculate it, but ie 7 should have currentStyle
                throw "Offset calculation not supported";
            }
        }
        return leftOffset + rightOffset;
    }
    function mouseEnterDelayed(rendererInstance, event) {
        if (rendererInstance.hideOnHoverTimeout
                && event.target === rendererInstance.lastUnhoveredElem) {
            window.clearTimeout(rendererInstance.hideOnHoverTimeout);
            rendererInstance.hideOnHoverTimeout = null;
        }
        if (rendererInstance.showOnHoverTimeout) {
            window.clearTimeout(rendererInstance.showOnHoverTimeout);
        }
        rendererInstance.showOnHoverTimeout = window.setTimeout(rendererInstance.showActionsOnHover
                .bind(rendererInstance, event), rendererInstance.options.showOnHoverDelay);
    }

    function mouseLeave(rendererInstance, event) {
        if (rendererInstance.showOnHoverTimeout) {
            window.clearTimeout(rendererInstance.showOnHoverTimeout);
            rendererInstance.showOnHoverTimeout = null;
            // workaround for chrome bug: if a child of the element with the mouseleave handler
            // is hovered and that child styles changes to display:none, a leave and enter event
            // sequence is triggered which causes flicker
            rendererInstance.lastUnhoveredElem = event.target;
            rendererInstance.hideOnHoverTimeout = window.setTimeout(
                    rendererInstance.hideActionsOnHover.bind(rendererInstance, event), 1);
        }
    }

    function noteActionLinkHandler(linkElem, event, details) {
        var action = linkElem.getAttribute('data-cnt-note-action-name');
        if (action) {
            // add event to the details
            details.triggeringEvent = event;
            if (details.widget.invokeNoteAction(action, details)) {
                // if it was handled prevent default action
                communote.utils.eventPreventDefault(event);
            }
        }
    }

    function noteMetaInfoLinkHandler(linkElem, event, details) {
        var handled, metaData;
        if (linkElem.hasClass('cn-note-number-of-likes')) {
            handled = true;
            details.widget.toggleLikingUsers(details.noteContainer);
        } else if (linkElem.hasClass('cn-note-number-of-comments')) {
            handled = true;
            metaData = details.widget.getNoteMetaData(details.noteId);
            details.widget.toggleInlineDiscussion(details.noteId, metaData.discussionId,
                    details.noteContainer);
        }
        if (handled) {
            communote.utils.eventPreventDefault(event);
        }
    }

    function permalinkHtmlBuilder(noteId, action, title, label) {
        var metaData = this.widget.getNoteMetaData(noteId);
        var result = {
            href: communote.utils.noteUtils.createNotePermalink(noteId, metaData.topicAlias)
        };
        if (namespace.configuration.openLinksInNewWindow) {
            result.target = '_blank';
        }
        return result;
    }

    classNamespace = ((namespace && namespace.classes) || window);

    /**
     * Constructor of a component of the ChronologicalPostList that renders the controls to interact
     * with the notes. The added controls are the note actions like reply or edit and the meta
     * informations whether the note is a favorite or the number of likers.
     * 
     * @param {ChronologicalPostList} widget The widget instance
     * @param {Object} [options] Object with options to override the defaultOptions
     */
    classNamespace.NoteInteractionControlRenderer = function(widget, options) {
        this.widget = widget;
        this.widgetHidden = true;
        widget.addNoteLinkClickHandler(noteActionLinkHandler, '.cn-note-action');
        widget.addNoteLinkClickHandler(noteMetaInfoLinkHandler, '.cn-note-meta-info');
        this.controls = {};
        // holds all controls that were expanded and thus shown for optimizing the re-render on resize
        this.shownControls = [];
        this.options = Object.merge({}, this.defaultOptions, options);
        if (Modernizr.touch) {
            this.options.showOnHover = false;
        } else {
            this.showOnHoverTimeout = null;
            this.boundMouseEnterHandler = mouseEnterDelayed.bind(null, this);
            this.boundMouseLeaveHandler = mouseLeave.bind(null, this);
            this.hideOnHoverTimeout = null;
            this.lastUnhoveredElem = null;
        }
        this.precalculationsDone = false;
        this.currentWidgetWidth = -1;
        if (this.options.optimizeTouch && Modernizr.touch) {
            this.optimizeTouch = true;
            this.renderIconsThresholdWidth = this.options.renderIconsForTouchThresholdWidth;
            this.boundShowTouchMoreMenu = this.showTouchMoreMenu.bind(this);
            widget.addNoteLinkClickHandler(this.boundShowTouchMoreMenu, '.cn-touch-more-menu');
        } else {
            this.optimizeTouch = false;
            this.renderIconsThresholdWidth = this.options.renderIconsForMouseThresholdWidth;
        }
        // ignore older IEs that do not have getComputedStyle, see getHorizontalOffsetValue above for details
        // NOTE: could implement alternative variant if old IEs are important (see TK)
        this.adaptToWidth = this.options.adaptToWidth
                && !!(window.getComputedStyle || this.options.widthAdaptionInOlderIEs);
        if (this.adaptToWidth) {
            this.singleLineLayout = false;
            this.metaInfoWrapperMargin = 0;
            this.actionContainerOffsets = 0;
            this.boundResizeEventHandler = this.onResizeChangeHandler.bind(this);
        }
    };

    /**
     * Default options which can be overridden by passing an options object to the constructor
     */
    classNamespace.NoteInteractionControlRenderer.prototype.defaultOptions = {
        // selector for the element that is filled with the action elements
        actionsContainerSelector: '.cn-note-actions-wrapper',
        // whether to respect the available space when rendering actions. All visibleActions that do
        // not fit into the available space will be added to the more-menu.
        adaptToWidth: true,
        // whether widths and/or margins of action elements (or wrapper container/meta-infos) can
        // change on resize of the browser window because of media-queries. If true cached values
        // will be recalculated on resize. To reduce the number of recalculations the option 
        // mediaQueryChangeThresholds can be used.
        considerMediaQueryChanges: true,
        /**
         * Mapping from action name to function which should be called when creating the HTML for
         * that action. The function is called with "this" set to the instance of the renderer and
         * is passed the noteId, the action, the title (can be blank) and the label. The function is
         * expected to return an object which can contain an html member that holds the complete
         * HTML or an href member which will be inserted by the default renderer as href instead of
         * the default javascript:; In case the href member is set, a target member can be set too. 
         */
        customActionHtmlBuilder: {
            permalink: permalinkHtmlBuilder
        },
        // can be an array of width thresholds that should trigger a recalculation if
        // the width of the widget was changed in a way that one of the thresholds was hit.
        mediaQueryChangeThresholds: false,
        // selector for the element that is filled with the meta-info
        metaInfoContainerSelector: '.cn-note-meta-info-wrapper',
        // whether to optimize for touch devices
        optimizeTouch: true,
        // if the user has a touch device and the widget width has or exceeds this value the actions
        // are rendered as if the user had a device with mouse input. If negative the touch version
        // will always be rendered. If 0 the mouse version will always be rendered.
        renderMouseActionsForTouchThresholdWidth: 400,
        // if the widget width has or exceeds this value the icons of the actions are rendered. If
        // negative the icons are never rendered. If 0 the icons are always rendered.
        renderIconsForMouseThresholdWidth: 0,
        // same as renderIconsForMouseThresholdWidth but is only used if optimizeTouch is set and
        // the device is a touch device
        renderIconsForTouchThresholdWidth: 400,
        // whether the actions should be shown if the noteContainer is hovered with the mouse
        // pointer. When the pointer leaves the container the actions are hidden.
        showOnHover: true,
        // delay in ms after which the actions should be shown. If the pointer leaves the
        // container before the timeout is reached the actions won't be shown.
        showOnHoverDelay: 200,
        /**
         * Names of actions to be rendered directly if there is enough space. Any action not listed
         * here will be added to the menu.
         */
        visibleActions: [ 'comment', 'like', 'favor' ],
        // try to adapt to the available width even if the browser does not support 
        // getComputedStyle. Depending on the CSS definitions this can fail (e.g. if relative
        // margins or paddings are used).
        widthAdaptionInOlderIEs: false
    };

    /**
     * Calculate the widths and margins the action elements and the more menu require and store the
     * results in internal members.
     */
    classNamespace.NoteInteractionControlRenderer.prototype.calculateActionElementWidths = function(
            noteId, actionsContainer) {
        var parentNode, childNodes, i, j, l, actionElem, action, margin;
        var actions = this.options.visibleActions;
        this.actionElementsWidths = {};
        this.actionElementsMargins = [];
        // calculate the width of all visible actions and the more menu and save widths, but avoid
        // flickering by hiding the content and forcing a fixed height of the parent
        parentNode = document.id(actionsContainer.parentNode);
        parentNode.setStyle('overflow', 'hidden').setStyle('height', parentNode.clientHeight);
        actionsContainer.setStyle('visibility', 'hidden');
        if (this.optimizeTouch) {
            this.renderActionsForTouch(noteId, actions, actions.length, actionsContainer, true);
        } else {
            this.renderActionsForMouse(noteId, actions, actions.length, actionsContainer, true);
        }
        childNodes = actionsContainer.children;
        for (j = 0, i = 0, l = childNodes.length; i < l; i++) {
            actionElem = childNodes[i];
            if (actionElem.nodeType !== 1) {
                // older IEs return comment nodes
                continue;
            }
            // assume there are as many elements as actions plus the more menu
            action = actions[j];
            if (action) {
                this.actionElementsWidths[action] = actionElem.offsetWidth;
                // assume all action-elements have the same margin, but first and last one can have other margins.
                if (this.actionElementsMargins.length < 2) {
                    this.actionElementsMargins.push(getHorizontalOffsetValue(actionElem, 'margin'));
                }
            } else {
                // hit the menu
                margin = getHorizontalOffsetValue(actionElem, 'margin');
                this.moreMenuWidth = actionElem.offsetWidth + margin;
                this.actionElementsMargins.push(margin);
                break;
            }
            j++;
        }
        // normalize margins array
        while (this.actionElementsMargins.length < 3) {
            this.actionElementsMargins.push(margin);
        }
        // clear actions again
        actionsContainer.empty();
        // remove element styles
        actionsContainer.setStyle('visibility', '');
        parentNode.setStyle('height', '').setStyle('overflow', '');
    };

    /**
     * This method sets the member actionsContainerFixedWidth to the width of the actionsContainer.
     * If the actionsContainer is not floated it occupies the available space. In this case the
     * member will receive that width excluding padding. Otherwise the member is set to 0.
     * 
     * To work correctly this method should be passed an empty actionsContainer, when called for the
     * first time. For subsequent calls it is not required to call it with an empty container if we
     * assume that mediaQueries do not change the style of the container (like floating it).
     * 
     * @param {Element} actionsContainer The container that will be filled with the actions
     */
    classNamespace.NoteInteractionControlRenderer.prototype.calculateActionsContainerFixedWidth = function(
            actionsContainer) {
        this.actionsContainerFixedWidth = actionsContainer.clientWidth;
        if (this.actionsContainerFixedWidth) {
            // Subtract an additional pixel because clientWidth can be rounded.
            this.actionsContainerFixedWidth -= 1 + getHorizontalOffsetValue(actionsContainer,
                    'padding');
        }
    };

    /**
     * Create the HTML for an action. If the options provide a customActionHtmlBuilder for this
     * action this function will be used.
     * 
     * @param {String|Number} noteId ID of the note
     * @param {String} action Name of the action to render
     * @param {Boolean} addIcon Whether to render the icon (marker element)
     * @return {String} the HTML of the action
     */
    classNamespace.NoteInteractionControlRenderer.prototype.createActionHTML = function(noteId,
            action, addIcon) {
        var html, htmlBuilderResult, href, target;
        var i18n = namespace.i18n;
        var title = i18n.getMessage(
                'widget.chronologicalPostList.note.action.' + action + '.title', null, '');
        var label = i18n
                .getMessage('widget.chronologicalPostList.note.action.' + action + '.label');
        if (this.options.customActionHtmlBuilder[action]) {
            htmlBuilderResult = this.options.customActionHtmlBuilder[action].call(this, noteId,
                    action, title, label, addIcon);
            if (htmlBuilderResult.html) {
                return htmlBuilderResult.html;
            }
            href = htmlBuilderResult.href;
            if (htmlBuilderResult.target) {
                target = htmlBuilderResult.target;
            }
        }
        if (title) {
            title = ' title="' + title + '"'
        }
        if (href) {
            href = ' href="' + href + '"';
            if (target) {
                href += ' target="' + target + '"';
            }
        } else {
            href = ' href="javascript:;"';
        }
        html = '<a class="cn-note-action cn-note-action-' + action
                + '" data-cnt-note-action-name="' + action + '"' + href + title + '>';
        if (addIcon) {
            html += '<span class="cn-icon"><!-- --></span>'
        }
        html += '<span class="cn-icon-label">' + label + '</span></a>';
        return html;
    };

    classNamespace.NoteInteractionControlRenderer.prototype.createFavoriteMetaInfoHTML = function() {
        return '<span class="cn-note-meta-info cn-note-favorite-marker" title="'
                + communote.i18n.getMessage('widget.chronologicalPostList.favoriteMarker.title')
                + '"><span class="cn-icon"><!-- --></span></span>';
    };
    classNamespace.NoteInteractionControlRenderer.prototype.createLikeCountMetaInfoHTML = function(
            count) {
        return '<a class="cn-note-meta-info cn-note-number-of-likes" href="javascript:;">'
                + '<span class="cn-icon"><!-- --></span><span class="cn-icon-label">' + count
                + '</span></a>';
    };

    classNamespace.NoteInteractionControlRenderer.prototype.createNoteCountMetaInfoHTML = function(
            count) {
        // TODO css class name is misleading
        return '<a class="cn-note-meta-info cn-note-number-of-comments" href="javascript:;">'
                + '<span class="cn-icon"><!-- --></span><span class="cn-icon-label">' + count
                + '</span></a>';
    };

    classNamespace.NoteInteractionControlRenderer.prototype.getActionsContainerOffsets = function(
            actionsContainer) {
        var parentOffset;
        if (this.actionContainerOffsets == undefined) {
            // offset of parent to children: only padding relevant
            parentOffset = getHorizontalOffsetValue(actionsContainer.parentNode, 'padding');
            // margins and paddings of actionWrapper also reduce the available space
            this.actionContainerOffsets = parentOffset
                    + getHorizontalOffsetValue(actionsContainer, 'margin');
            this.actionContainerOffsets += getHorizontalOffsetValue(actionsContainer, 'padding');
        }
        return this.actionContainerOffsets;
    };

    /**
     * Get the number of pixels the actionsContainer can occupy without wrapping the line.
     */
    classNamespace.NoteInteractionControlRenderer.prototype.getAvailableSpace = function(
            noteContainer, actionsContainer) {
        var metaWidth, availableSpace;
        if (this.actionsContainerFixedWidth) {
            if (this.actionsContainerFixedWidth < 0) {
                this.calculateActionsContainerFixedWidth(actionsContainer);
            }
            return this.actionsContainerFixedWidth;
        }
        // pessimistic: subtract 1px because clientWidth is the rounded value
        availableSpace = actionsContainer.parentNode.clientWidth
                - this.getActionsContainerOffsets(actionsContainer) - 1;
        if (this.singleLineLayout) {
            // TODO optimize: remember whether meta-infos has width? 
            // subtract space of meta-infos container. If it has no width, ignore it
            metaWidth = noteContainer.getElement(this.options.metaInfoContainerSelector).offsetWidth;
            if (metaWidth) {
                // pessimistic: subtract 1px because offsetWidth is the rounded value
                availableSpace = availableSpace - metaWidth - this.metaInfoWrapperMargin - 1;
            }
        }
        return availableSpace;
    };

    /**
     * Get the object holding the details of the control of a note.
     * 
     * @param {Number|String} [noteId] The ID of the note. If omitted the ID is extracted from the
     *            noteContainer.
     * @param {Element} [noteContainer] The element wrapping a note. Can be omitted if noteId is
     *            set.
     * @return {Object} the control details
     */
    classNamespace.NoteInteractionControlRenderer.prototype.getControl = function(noteId,
            noteContainer) {
        if (!noteId) {
            noteId = noteContainer.getAttribute('data-cnt-note-id');
        }
        return this.controls['n' + noteId];
    };

    /**
     * Get the state object of a control of a note. This method should be used if it is not sure
     * whether the note is an inlineInjected note or not
     * 
     * @param {Object} control The control object as returned by getControl method
     * @param {Element} noteContainer The element wrapping a note.
     * @return {Object} the object holding the details of the state of the control
     */
    classNamespace.NoteInteractionControlRenderer.prototype.getControlState = function(control,
            noteContainer) {
        if (noteContainer.getAttribute(this.widget.dataAttributeInlineInjectedNote) === 'true') {
            return control.inlineInjectedState;
        }
        return control.state;
    };

    /**
     * Get the index of the first action that is not a visible action and thus should be rendered in
     * the more menu.
     * 
     * @param {String[]} actions Array containing the names of all actions to be rendered for a note
     * @return {Number} the index of the first action to add to the more menu. If all actions are
     *         visible actions the length of the actions array is returned.
     */
    classNamespace.NoteInteractionControlRenderer.prototype.getIndexOfFirstMenuAction = function(
            actions) {
        var i, l;
        var visibleActions = this.options.visibleActions;
        for (i = 0, l = actions.length; i < l; i++) {
            if (visibleActions.indexOf(actions[i]) === -1) {
                // it is not visible and therefore a menu action
                break;
            }
        }
        return i;
    };

    /**
     * Returns the index of the first action that should be added to the more menu. This will take
     * the provided available space and the space required by each action into account. So if a
     * visible action would not fit into the available space its index would be returned.
     * 
     * @param {String[]} actions Array containing the names of all actions to be rendered for a note
     * @param {Number} menuActionIndex The index of the first action within the actions array that
     *            is not a visible action. This is the index returned by getIndexOfFirstMenuAction.
     * @param {Number} availableSpace The space the visible actions can use in pixels
     * @param {Number} the index of the first action to add to the more menu. If there is enough
     *            space for all visible actions, the menuActionIndex will be returned.
     */
    classNamespace.NoteInteractionControlRenderer.prototype.getIndexOfFirstRenderedMenuAction = function(
            actions, menuActionIndex, availableSpace) {
        var action, requiredSpace, renderedMenuActionIdx;
        var i = renderedMenuActionIdx = 0;
        var onlyVisibleActions = actions.length === menuActionIndex;
        var consumedSpace = 0;
        // check the visible actions if they have enough space
        while (i < menuActionIndex) {
            action = actions[i];
            if (i + 1 === menuActionIndex) {
                // last visible action
                requiredSpace = this.getRequiredSpace(action, i == 0, onlyVisibleActions);
                if (!onlyVisibleActions) {
                    requiredSpace += this.moreMenuWidth;
                }
                if (availableSpace >= consumedSpace + requiredSpace) {
                    renderedMenuActionIdx = menuActionIndex;
                }
            } else {
                requiredSpace = this.getRequiredSpace(action, i == 0, false);
                if (availableSpace < consumedSpace + requiredSpace) {
                    // action does not have enough space and should be added to the more menu
                    break;
                } else {
                    // if there is enough space to show the more menu the next action could be
                    // added to the menu if it does not fit anymore.
                    if (availableSpace >= consumedSpace + requiredSpace + this.moreMenuWidth) {
                        renderedMenuActionIdx = i + 1;
                    } else if (!onlyVisibleActions) {
                        // we can stop here because there is not enough space for the more menu
                        // and the more menu has to be rendered. It will be rendered at the last
                        // saved index. If we have only visible actions we cannot stop because the
                        // remaining actions might need less space than the more menu.
                        break;
                    }
                    consumedSpace += requiredSpace;
                }
            }
            i++;
        }
        return renderedMenuActionIdx;
    };

    /**
     * Get the space a visible action occupies. This includes the width of the element and the left
     * and right margins.
     * 
     * @param {String} action The name of the visible action
     * @param {Boolean} isFirst Whether the action is the first action
     * @param {Boolean} isLast Whether the action is the last action
     * @return {Number} the required horizontal space in pixels
     */
    classNamespace.NoteInteractionControlRenderer.prototype.getRequiredSpace = function(action,
            isFirst, isLast) {
        var requiredSpace = this.actionElementsWidths[action];
        if (isLast) {
            requiredSpace += this.actionElementsMargins[2];
        } else if (isFirst) {
            requiredSpace += this.actionElementsMargins[0];
        } else {
            requiredSpace += this.actionElementsMargins[1];
        }
        return requiredSpace;
    };

    classNamespace.NoteInteractionControlRenderer.prototype.hideActions = function(control,
            controlState, noteContainer) {
        var actionsContainer;
        if (controlState.shown && (!this.options.showOnHover || !controlState.containerHovered)
                && !controlState.containerExpanded) {
            if (!noteContainer) {
                noteContainer = this.widget.getNoteContainerByType(control.noteId,
                        controlState == control.inlineInjectedState);
            }
            actionsContainer = noteContainer.getElement(this.options.actionsContainerSelector);
            actionsContainer.setStyle('display', 'none');
            controlState.shown = false;
        }
    };

    classNamespace.NoteInteractionControlRenderer.prototype.hideActionsOnHover = function(event,
            noteDetails) {
        var noteContainer, control, controlState, inlineInjected;
        if (event) {
            noteContainer = event.target;
            control = this.getControl(null, noteContainer);
            controlState = this.getControlState(control, noteContainer);
            inlineInjected = controlState === control.inlineInjectedState;
        } else {
            control = this.getControl(noteDetails.noteId, null);
            if (control) {
                controlState = noteDetails.inlineInjected ? control.inlineInjectedState : control.state;
            }
            if (!control || !controlState) {
                // note might have been removed
                if (noteDetails == this.lastHoveredNote) {
                    this.lastHoveredNote = null;
                }
                return;
            }
            inlineInjected = noteDetails.inlineInjected;
        }
        controlState.containerHovered = false;
        this.hideOnHoverTimeout = null;
        this.lastUnhoveredElem = null;
        if (this.lastHoveredNote && this.lastHoveredNote.noteId === control.noteId
                && this.lastHoveredNote.inlineInjected === inlineInjected) {
            this.lastHoveredNote = null;
        }
        this.hideActions(control, controlState, noteContainer);
    };

    classNamespace.NoteInteractionControlRenderer.prototype.inlineInjectedNoteContainersStateChanged = function(
            noteIds, stateName, newValue) {
        var i, l, control, noteContainer;
        noteIds = Array.from(noteIds);
        for (i = 0, l = noteIds.length; i < l; i++) {
            control = this.getControl(noteIds[i], null);
            if (control) {
                this.updateNoteContainerState(control, control.inlineInjectedState, stateName,
                        newValue, null);
            }
        }
    };

    classNamespace.NoteInteractionControlRenderer.prototype.normalNoteContainersStateChanged = function(
            noteIds, stateName, newValue) {
        var i, l, control, noteContainer;
        noteIds = Array.from(noteIds);
        for (i = 0, l = noteIds.length; i < l; i++) {
            control = this.getControl(noteIds[i], null);
            if (control) {
                this.updateNoteContainerState(control, control.state, stateName, newValue, null);
            }
        }
    };

    classNamespace.NoteInteractionControlRenderer.prototype.noteContainersStateChanged = function(
            noteContainers, stateName, newValue) {
        var i, l;
        for (i = 0, l = noteContainers.length; i < l; i++) {
            this.noteContainerStateChanged(null, noteContainers[i], stateName, newValue);
        }
    };

    classNamespace.NoteInteractionControlRenderer.prototype.noteContainerStateChanged = function(
            noteId, noteContainer, stateName, newValue) {
        var control = this.getControl(noteId, noteContainer);
        var controlState = this.getControlState(control, noteContainer);
        this.updateNoteContainerState(control, controlState, stateName, newValue, noteContainer);
    };

    classNamespace.NoteInteractionControlRenderer.prototype.noteStateChanged = function(noteId,
            stateName, newValue) {
        var classesToAdd, classesToRemove, textToUpdate, metaData;
        var skipInline = false;
        var control = this.getControl(noteId, null);
        // TODO any special handling if the widget is hidden? Could collect the changes per
        // noteId and apply them as soon as the widget is shown again?
        if (!control) {
            return;
        }
        classesToAdd = [];
        classesToRemove = [];
        if (stateName === 'favorite') {
            if (newValue) {
                classesToAdd.push('cn-note-is-favorite');
            } else {
                classesToRemove.push('cn-note-is-favorite')
            }
        } else if (stateName === 'liked') {
            metaData = this.widget.getNoteMetaData(noteId);
            if (newValue) {
                classesToAdd.push('cn-note-is-liked');
                if (metaData.likeCount === 1) {
                    classesToAdd.push('cn-note-has-likes');
                }
            } else {
                classesToRemove.push('cn-note-is-liked')
                if (metaData.likeCount === 0) {
                    classesToRemove.push('cn-note-has-likes');
                }
            }
            if (metaData.likeCount > 0) {
                textToUpdate = {
                    selector: '.cn-note-number-of-likes .cn-icon-label',
                    text: metaData.likeCount
                };
            }
        } else if (stateName === 'commentCount') {
            // is only called for classic
            skipInline = true;
            if (newValue) {
                textToUpdate = {
                    selector: '.cn-note-number-of-comments .cn-icon-label',
                    text: newValue + 1
                // total number of notes in discussion
                };
                classesToAdd.push('cn-note-has-comments');
            } else {
                classesToRemove.push('cn-note-has-comments');
            }
        }
        this.updateNoteContainers(noteId, classesToAdd, classesToRemove, textToUpdate, skipInline);
    };

    /**
     * Function to be called after the browser window or widget was resized. If the rendering does not adapt
     * to the width this method should not be called.
     */
    classNamespace.NoteInteractionControlRenderer.prototype.onResizeChangeHandler = function() {
        var newWidth, thresholds, recalculate, i, l;
        if (!this.widgetHidden) {
            newWidth = this.widget.domNode.clientWidth;
            if (newWidth != this.currentWidgetWidth) {
                if (this.actionsContainerFixedWidth != 0) {
                    // invalidate fixed width, assume that mediaQueries do not change the styles
                    // of the acionsContainer in a way that leads to loosing the "fixed-width ability"
                    this.actionsContainerFixedWidth = -1;
                }
                if (this.precalculationsDone && this.options.considerMediaQueryChanges
                        && window.matchMedia) {
                    // browser supports media queries, thus cached values for widths and margins
                    // might have become invalid
                    thresholds = this.options.mediaQueryChangeThresholds;
                    if (!thresholds || thresholds.length === 0) {
                        recalculate = true;
                    } else {
                        for (i = 0, l = thresholds.length; i < l; i++) {
                            if ((width <= thresholds[i]) !== (this.currentWidgetWidth <= threshold[i])) {
                                recalculate = true;
                                break;
                            }
                        }
                    }
                    if (recalculate) {
                        // invalidate precalculations
                        this.precalculationsDone = false;
                    }
                }
                this.currentWidgetWidth = newWidth;
                // update shown, rendered controls
                this.reRenderShownControls();
            }
        }
    };

    /**
     * If the rendered menu should adapt to the width of the widget this method will calculate and
     * cache the required spaces of the action elements.
     * 
     * @param {String|Number} noteId The ID of the note
     * @param {Element} noteContainer The element wrapping the note
     * @param {Element} [actionsContainer] The element to render the actions to
     * @param {Element} [metaInfoContainer] The element containing the meta infos
     */
    classNamespace.NoteInteractionControlRenderer.prototype.precalculate = function(noteId,
            noteContainer, actionsContainer, metaInfoContainer) {
        if (!this.adaptToWidth) {
            this.precalculationsDone = true;
            return;
        }
        if (!actionsContainer) {
            actionsContainer = noteContainer.getElement(this.options.actionsContainerSelector);
        }
        actionsContainer.setStyle('display', '');
        // if the empty container has a width the content is not floated which means it
        //occupies the available space
        this.calculateActionsContainerFixedWidth(actionsContainer);
        this.calculateActionElementWidths(noteId, actionsContainer);
        if (!this.actionsContainerFixedWidth) {
            // precalculate offsets of action container by calling the lazy getter
            this.getActionsContainerOffsets(actionsContainer);
            // if actions and meta infos are on the same line, width and margins of meta 
            // infos must be considered too
            if (!metaInfoContainer) {
                metaInfoContainer = noteContainer
                        .getElement(this.options.metaInfoContainerSelector);
            }
            if (actionsContainer.getPosition().y === metaInfoContainer.getPosition().y) {
                this.singleLineLayout = true;
                this.metaInfoWrapperMargin = getHorizontalOffsetValue(metaInfoContainer, 'margin');
            }
        }
        this.precalculationsDone = true;
    };

    classNamespace.NoteInteractionControlRenderer.prototype.prepareNote = function(noteId,
            noteContainer) {
        var stateId, metaInfoContainer, oldState;
        // a note might be contained several times in the CPL (e.g. inline discussions), 
        // thus save the state for every note container
        var control = this.controls['n' + noteId];
        var state = {
            shown: false,
            rendered: false,
            containerShown: true
        };
        if (!control) {
            control = this.controls['n' + noteId] = {};
            // save noteId for back-reference
            control.noteId = noteId;
            // actions are the same for each instance of a note
            control.actions = noteContainer.getAttribute('data-cnt-note-actions');
        }
        // add or replace the state. 
        if (noteContainer.getAttribute(this.widget.dataAttributeInlineInjectedNote) === 'true') {
            oldState = control.inlineInjectedState;
            control.inlineInjectedState = state;
        } else {
            oldState = control.state;
            control.state = state;
        }
        metaInfoContainer = this.renderMetaInfo(control, state, noteContainer);
        if (!this.precalculationsDone) {
            try {
                this.precalculate(noteId, noteContainer, null, metaInfoContainer);
            } catch (e) {
                // precalculation failed, probably due to getHorizontalMargins throwing an exception
                this.adaptToWidth = false;
                // TODO remove resize handler!
            }
        }
        // if the container was marked for replacement restore the old state
        if (oldState && oldState.containerReplace) {
            state.containerShown = oldState.containerShown;
            if (oldState.containerExpanded) {
                this.updateNoteContainerState(control, state, 'expanded', true, noteContainer);
            }
        }
        if (this.options.showOnHover) {
            noteContainer.addEvent('mouseenter', this.boundMouseEnterHandler);
            noteContainer.addEvent('mouseleave', this.boundMouseLeaveHandler);
        }
    };

    classNamespace.NoteInteractionControlRenderer.prototype.renderActions = function(control,
            controlState, noteContainer) {
        var menuActionIndex, availableSpace;
        var actionsContainer = noteContainer.getElement(this.options.actionsContainerSelector);
        if (!this.precalculationsDone) {
            if (controlState.rendered) {
                actionsContainer.empty();
            }
            this.precalculate(control.noteId, noteContainer, actionsContainer, null);
            controlState.dirty = false;
            controlState.rendered = false;
        }
        if (!controlState.rendered) {
            if (typeof control.actions === 'string') {
                control.actions = control.actions.split(',');
                control.indexOfMenuAction = this.getIndexOfFirstMenuAction(control.actions);
            }
            if (this.adaptToWidth) {
                availableSpace = this.getAvailableSpace(noteContainer, actionsContainer);
                menuActionIndex = this.getIndexOfFirstRenderedMenuAction(control.actions,
                        control.indexOfMenuAction, availableSpace);
            } else {
                menuActionIndex = control.indexOfMenuAction;
            }
            // save the index of rendered menu action in the state
            controlState.indexOfMenuAction = menuActionIndex;
            if (this.optimizeTouch) {
                this.renderActionsForTouch(control.noteId, control.actions, menuActionIndex,
                        actionsContainer, false);
            } else {
                this.renderActionsForMouse(control.noteId, control.actions, menuActionIndex,
                        actionsContainer, false);
            }
            controlState.rendered = true;
            // save the width at which the control was rendered. Changes in width are treated as dirty.
            controlState.widgetWith = this.currentWidgetWidth;
        } else if (this.adaptToWidth
                && (controlState.dirty || controlState.widgetWith != this.currentWidgetWidth)) {
            // if all visible actions are already visble and the width increased, there is nothing
            // todo, but only if there are no mediaQueries which could lead to reduce the available
            // space even if the width of the widget grew
            if (controlState.dirty
                    || control.indexOfMenuAction !== controlState.indexOfMenuAction
                    || (this.options.considerMediaQueryChanges && this.currentWidgetWidth != controlState.widgetWith)
                    || (!this.options.considerMediaQueryChanges && this.currentWidgetWidth < controlState.widgetWith)) {
                availableSpace = this.getAvailableSpace(noteContainer, actionsContainer);
                menuActionIndex = this.getIndexOfFirstRenderedMenuAction(control.actions,
                        control.indexOfMenuAction, availableSpace);
                if (menuActionIndex != controlState.indexOfMenuAction) {
                    // rerender if the index of the first menu action changed
                    controlState.indexOfMenuAction = menuActionIndex;
                    if (this.optimizeTouch) {
                        this.renderActionsForTouch(control.noteId, control.actions,
                                menuActionIndex, actionsContainer, false);
                    } else {
                        this.renderActionsForMouse(control.noteId, control.actions,
                                menuActionIndex, actionsContainer, false);
                    }
                }
                controlState.widgetWith = this.currentWidgetWidth;
            }
            controlState.dirty = false;
        }
        return actionsContainer;
    };

    /**
     * Render the actions optimized for mouse-based interaction.
     * 
     * @param {String|Number} noteId ID of the for which the actions should be rendered
     * @param {String[]} actions The names of the actions to render
     * @param {Number} menuActionIndex The index of the first action within actions that should be
     *            added to the more-menu. Actions with a higher index will also be added to the
     *            more-menu.
     * @param {Element} actionsContainer The container to render the actions into. The html of that
     *            container will be replaced.
     * @param {Boolean} renderEmptyMenu Whether to render the 'more-menu' element even if there are
     *            no actions inside the menu.
     */
    classNamespace.NoteInteractionControlRenderer.prototype.renderActionsForMouse = function(
            noteId, actions, menuActionIndex, actionsContainer, renderEmptyMenu) {
        var i, l, action, actionHtml;
        var html = '';
        var moreHtml = '';
        var renderIcons = this.renderIconsThresholdWidth >= 0
                && this.currentWidgetWidth >= this.renderIconsThresholdWidth;
        for (i = 0, l = actions.length; i < l; i++) {
            action = actions[i];
            actionHtml = this.createActionHTML(noteId, action, renderIcons);
            if (i < menuActionIndex) {
                html += actionHtml;
            } else {
                moreHtml += '<li>' + actionHtml + '</li>';
            }
        }
        if (renderEmptyMenu || moreHtml) {
            html += '<ul class="cn-menu" aria-haspopup="true"><li><a href="javascript:;" class="cn-more-actions">'
                    + '<span class="cn-icon-label">'
                    + getJSMessage('common.more')
                    + '</span><span class="cn-icon cn-arrow"><!-- --></span></a></li><li><ul class="cn-menu-list">'
                    + moreHtml + '</ul></li></ul>';
        }
        actionsContainer.set('html', html);
    };

    classNamespace.NoteInteractionControlRenderer.prototype.renderActionsForTouch = function(
            noteId, actions, menuActionIndex, actionsContainer, renderEmptyMenu) {
        var i, html, renderIcons;
        var threshold = this.options.renderMouseActionsForTouchThresholdWidth;
        if (threshold >= 0 && this.currentWidgetWidth >= threshold) {
            this.renderActionsForMouse(noteId, actions, menuActionIndex, actionsContainer,
                    renderEmptyMenu);
        } else {
            html = '';
            renderIcons = this.currentWidgetWidth >= this.renderIconsThresholdWidth;
            for (i = 0; i < menuActionIndex; i++) {
                html += this.createActionHTML(noteId, actions[i], renderIcons);
            }
            if (renderEmptyMenu || actions.length > menuActionIndex) {
                html += '<a href="javascript:;" class="cn-touch-more-menu"><span class="cn-icon"><!-- --></span></a>';
            }
            actionsContainer.set('html', html);
        }
    };

    classNamespace.NoteInteractionControlRenderer.prototype.renderMetaInfo = function(control,
            controlState, noteContainer) {
        var count, noteMetaData;
        // TODO how could we allow plugins to render more or other meta-infos?
        // render all meta-infos and let the CSS classes added on the noteContainer decide whether to show them
        var html = this.createFavoriteMetaInfoHTML();
        if (noteContainer.hasClass('cn-note-has-likes')) {
            noteMetaData = this.widget.getNoteMetaData(control.noteId);
            count = noteMetaData.likeCount;
        } else {
            // short-cut that avoid decoding the meta-data if not required
            count = 0;
        }
        html += this.createLikeCountMetaInfoHTML(count);
        // comment count element is only rendered if viewType is CLASSIC
        if (this.widget.getCurrentViewType() === 'CLASSIC') {
            if (noteContainer.hasClass('cn-note-has-comments')) {
                noteMetaData = noteMetaData || this.widget.getNoteMetaData(control.noteId);
                // show the number of all notes in the discussion
                count = noteMetaData.discussionCommentCount + 1;
            } else {
                count = 1;
            }
            html += this.createNoteCountMetaInfoHTML(count);
        }
        return noteContainer.getElement(this.options.metaInfoContainerSelector).set('html', html);
    };

    /**
     * Re-render the actions if the control is rendered, shown and dirty.
     * 
     * @param {Object} control A control definition
     * @param {Object} [controlState] The state of the normal or inlineInjected version of the
     *            control. If omitted nothing will happen.
     * @param {Element} [noteContainer] The container of the note. If missing the container will be
     *            retrieved from the widget.
     */
    classNamespace.NoteInteractionControlRenderer.prototype.reRenderActions = function(control,
            controlState, noteContainer) {
        if (controlState) {
            if (controlState.containerShown && controlState.rendered && controlState.shown
                    && (controlState.dirty || controlState.widgetWith != this.currentWidgetWidth)) {
                if (!noteContainer) {
                    noteContainer = this.widget.getNoteContainerByType(control.noteId,
                            controlState == control.inlineInjectedState);
                }
                this.renderActions(control, controlState, noteContainer);
            }
        }
    };
    
    classNamespace.NoteInteractionControlRenderer.prototype.reRenderAdaptivelyOnResize = function() {
        if (this.adaptToWidth) {
            this.onResizeChangeHandler();
        }
    };

    /**
     * Re-render the shown controls if they are dirty.
     */
    classNamespace.NoteInteractionControlRenderer.prototype.reRenderShownControls = function() {
        var i, l, control;
        for (i = 0, l = this.shownControls.length; i < l; i++) {
            control = this.getControl(this.shownControls[i], null);
            // reRender will check for dirtyness
            this.reRenderActions(control, control.state, null);
            this.reRenderActions(control, control.inlineInjectedState, null);
        }
    };

    /**
     * Show the actions if not already shown. In case the actions haven't been rendered yet they
     * will be rendered. It is assumed that the container of the control is shown (containerShown
     * flag of control state is true).
     * 
     * @param {Object} control A control definition
     * @param {Object} [controlState] The state of the normal or inlineInjected version of the
     *            control. If omitted nothing will happen.
     * @param {Element} [noteContainer] The container of the note. If missing the container will be
     *            retrieved from the widget.
     */
    classNamespace.NoteInteractionControlRenderer.prototype.showActions = function(control,
            controlState, noteContainer) {
        var actionsContainer;
        if (!controlState.shown) {
            if (!noteContainer) {
                noteContainer = this.widget.getNoteContainerByType(control.noteId,
                        controlState == control.inlineInjectedState);
            }
            actionsContainer = this.renderActions(control, controlState, noteContainer);
            actionsContainer.setStyle('display', '');
            controlState.shown = true;
        }
    };

    classNamespace.NoteInteractionControlRenderer.prototype.showActionsOnHover = function(event) {
        var noteContainer = event.target;
        var control = this.getControl(null, noteContainer);
        var controlState = this.getControlState(control, noteContainer);
        var inlineInjected = controlState === control.inlineInjectedState;
        // sometimes mouseleave event is not triggered by browser. Clear hover flag of last hovered note manually.
        if (this.lastHoveredNote
                && (control.noteId != this.lastHoveredNote.noteId || inlineInjected != this.lastHoveredNote.inlineInjected)) {
            this.hideActionsOnHover(null, this.lastHoveredNote);
        }
        this.lastHoveredNote = {};
        this.lastHoveredNote.noteId = control.noteId;
        this.lastHoveredNote.inlineInjected = inlineInjected;
        controlState.containerHovered = true;
        this.showActions(control, controlState, noteContainer);
    };

    /**
     * NoteLinkClickHandler that is invoked when the touch-optimized more-menu is clicked.
     */
    classNamespace.NoteInteractionControlRenderer.prototype.showTouchMoreMenu = function(linkElem,
            event, details) {
        var i, actions, wrapper;
        var control = this.getControl(details.noteId, null);
        var controlState = this.getControlState(control, details.noteContainer);
        var html = '';
        actions = control.actions;
        for (i = controlState.indexOfMenuAction; i < actions.length; i++) {
            html += '<div class="cn-touch-button">'
                    + this.createActionHTML(details.noteId, actions[i], false) + '</div>';
        }
        wrapper = new Element('div', {
            'class': 'cn-touch-more-actions',
            'html': html
        });
        wrapper.addEvent('click', function(e) {
            var linkElem = communote.utils.getClickedLinkElement(e, true);
            if (linkElem) {
                closeDialog(false);
                noteActionLinkHandler(linkElem, e, details);
            }
        });
        showDialog(communote.i18n.getMessage('widget.chronologicalPostList.note.action.more.popup.title'), wrapper, false, {
            windowCssClasses: 'cn-touch-actions-popup',
            triggeringEvent: event
        });
    };

    classNamespace.NoteInteractionControlRenderer.prototype.updateNoteContainers = function(noteId,
            classesToAdd, classesToRemove, textToUpdate, skipInlineInjected) {
        var noteContainers, i, j, l, noteCount, container, control, controlState;
        if (classesToAdd.length || classesToRemove.length || textToUpdate) {
            noteContainers = this.widget.getNoteContainers(noteId);
            for (i = 0, noteCount = noteContainers.length; i < noteCount; i++) {
                container = noteContainers[i];
                if (skipInlineInjected
                        && container.getAttribute(this.widget.dataAttributeInlineInjectedNote) === 'true') {
                    continue;
                }
                for (j = 0, l = classesToAdd.length; j < l; j++) {
                    container.addClass(classesToAdd[j]);
                }
                for (j = 0, l = classesToRemove.length; j < l; j++) {
                    container.removeClass(classesToRemove[j]);
                }
                if (textToUpdate) {
                    container.getElement(textToUpdate.selector).set('text', textToUpdate.text);
                }
                if (this.adaptToWidth) {
                    // check if actions need to be re-rendered
                    control = this.getControl(noteId, container);
                    controlState = this.getControlState(control, container);
                    if (controlState.rendered) {
                        controlState.dirty = true;
                        // only re-render if the widget is not hidden
                        if (!this.widgetHidden) {
                            this.reRenderActions(control, controlState, container);
                        }
                    }
                }
            }
        }
    };

    classNamespace.NoteInteractionControlRenderer.prototype.updateNoteContainerState = function(
            control, controlState, stateName, newValue, noteContainer) {
        var otherState;
        if (stateName === 'deleted') {
            if (controlState) {
                if (controlState == control.state) {
                    delete control.state;
                } else {
                    delete control.inlineInjectedState;
                }
            }
            // remove control if there are no more states
            if (!control.state && !control.inlineInjectedState) {
                delete this.controls['n' + control.noteId];
                this.shownControls.erase(control.noteId);
            }
        } else if (controlState) {
            if (stateName === 'shown') {
                if (newValue === false) {
                    controlState.containerShown = false;
                } else {
                    controlState.containerShown = true;
                    if (controlState.containerExpanded && !controlState.shown) {
                        // if the container was expanded while it was not shown expand it now
                        this.showActions(control, controlState, noteContainer);
                        this.shownControls.include(control.noteId);
                    } else {
                        this.reRenderActions(control, controlState, noteContainer);
                    }
                }
            } else if (stateName === 'expanded') {
                controlState.containerExpanded = newValue;
                if (newValue) {
                    if (controlState.containerShown) {
                        this.showActions(control, controlState, noteContainer);
                        this.shownControls.include(control.noteId);
                    }
                } else {
                    this.hideActions(control, controlState, noteContainer);
                    otherState = controlState == control.state ? control.inlineInjectedState
                            : control.state;
                    if (!otherState || !otherState.containerExpanded) {
                        this.shownControls.erase(control.noteId);
                    }
                }
            } else if (stateName === 'replace') {
                controlState.containerReplace = newValue;
            }
        }
    };

    classNamespace.NoteInteractionControlRenderer.prototype.widgetAfterShow = function(isDirty) {
        var oldWidth;
        this.widgetHidden = false;
        if (!isDirty && this.adaptToWidth) {
            // widget is not going to be refreshed
            oldWidth = this.currentWidgetWidth;
            // assume we missed a resize, if so all shown controls will be re-rendered
            this.onResizeChangeHandler();
            if (oldWidth == this.currentWidgetWidth) {
                // missed no resize. Have to rerender the shown controls which became dirty while
                // the widget was hidden.
                this.reRenderShownControls();
            }
        }
    };

    classNamespace.NoteInteractionControlRenderer.prototype.widgetBeforeHide = function() {
        this.widgetHidden = true;
    };

    /**
     * Callback that should be invoked before the widget is removed or refreshed.
     */
    classNamespace.NoteInteractionControlRenderer.prototype.widgetCleanup = function(noteContainers) {
        if (this.options.showOnHover) {
            noteContainers.removeEvents('mouseenter');
            noteContainers.removeEvents('mouseleave');
            window.clearTimeout(this.showOnHoverTimeout);
            window.clearTimeout(this.hideOnHoverTimeout);
        }
        this.controls = {};
        this.shownControls = [];
        if (this.boundResizeEventHandler) {
            communote.utils.removeDebouncedResizeEventHandler(this.boundResizeEventHandler);
        }
    };
    classNamespace.NoteInteractionControlRenderer.prototype.widgetRefreshComplete = function(
            responseMetadata) {
        if (this.adaptToWidth) {
            this.onResizeChangeHandler();
            communote.utils.addDebouncedResizeEventHandler(this.boundResizeEventHandler);
        } else if (this.currentWidgetWidth == -1) {
            this.currentWidgetWidth = this.widget.domNode.clientWidth;
        }
    };

})(this.runtimeNamespace, this);