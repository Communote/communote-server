/**
 * 
 * @author rwi
 * @copyright none yet defined
 */

(function() {

    var pluginDef = {
        ed: null,
        editorFrameId: null,
        // tag-names of elements that create linebreaks ordered by frequency of appearance
        // TODO contains only the most important tags that can have text children, could make it configurable to add tr for instance?
        // TODO add UL and OL or does tinymce take care of it?
        linebreakingTags: [ 'P', 'LI', 'DIV', 'BODY' ],
        // default delimiters ordered by frequency of occurrence
        defaultDelimiters: [ ' ', '\u00A0', ',', '(', ';', '[' ],
        textProperty: null,
        suggestionDefinitions: null,
        currentSuggestionDefinition: null,
        currentQueryValue: null,
        suggestionsFoundLog: null,
        fireValueChangedTimeout: null,
        fireValueChangedDelay: 200,
        checkBlurInterval: null,
        // delay to wait between checks for blur while the suggestions are shown
        hideSuggestionsOnBlurDelay: 1000,
        boundScrollListener: null,
        // will be true if at least on registered suggestion is enabled
        suggestionsEnabled: false,
        // whether the input of a trigger character interrupts the current suggestion-mode and
        // starts a new suggestion-mode. The canceling and restart will only occur if the string
        // before the trigger char contains one of the delimiters.
        triggerCharInterruptsSuggestion: true,

        /**
         * Initializes the plugin, this will be executed after the plugin has been created. This
         * call is done before the editor instance has finished it's initialization so use the
         * onInit event of the editor instance to intercept that event.
         * 
         * @param {tinymce.Editor} ed Editor instance that the plugin is initialized in.
         * @param {string} url Absolute URL to where the plugin is located.
         */
        init: function(ed, url) {
            var options, tinymceNs, resourceUrlBase, resourceUrlParams;
            options = ed.getParam('suggestions_options');
            tinymceNs = tinymce;
            // check required settings and supported browsers (no old IEs since using JavaScript
            // 1.8.5 like bind). No support for fullscreen yet (because of positioning at edges
            // and datasource config)
            if (!options || ed.id === 'mce_fullscreen' || tinymceNs.isIE6 || tinymceNs.isIE7
                    || tinymceNs.isIE8) {
                return;
            }
            resourceUrlBase = (options.resourceUrlBase || url) + '/suggestions';
            if (options.resourceUrlParams) {
                resourceUrlParams = options.resourceUrlParams;
                if (resourceUrlParams.charAt(0) !== '?') {
                    resourceUrlParams = '?' + resourceUrlParams;
                }
            } else {
                resourceUrlParams = '';
            }
            // include default CSS definitions for suggestions and marker
            if (options.includeCss) {
                tinymce.DOM.loadCSS(resourceUrlBase + '/css/suggestions.css' + resourceUrlParams);
                ed.onInit.add(function(ed) {
                    // add css to iframe after editor init
                    ed.dom
                            .loadCSS(resourceUrlBase + '/css/editor_internal.css'
                                    + resourceUrlParams);
                });
            }
            this.ed = ed;
            this.delimiters = [ ' ', '\u00A0', ',', '.', ';' ];
            // IE has innerText, FF has textContent, others have both (but textContent gives better results)
            this.textProperty = (document.createElement('div').textContent == null) ? 'innerText'
                    : 'textContent';
            if (options.fireValueChangedDelay) {
                this.fireValueChangedDelay = options.fireValueChangedDelay;
            }
            if (options.triggerCharInterruptsSuggestion === false) {
                this.triggerCharInterruptsSuggestion = false;
            }
            this.registerToggleCommand();
            this.extractSuggestionDefinitions(options);
            ed.onRemove.add(this.onEditorRemoved, this);
            ed.onKeyDown.add(this.onKeydown, this);
            ed.onKeyPress.add(this.onKeypress, this);
            ed.onKeyUp.add(this.onKeyup, this);
            ed.onMouseUp.add(this.onMouseup, this);
            if (tinymce.isIE9) {
                ed.onContextMenu.add(this.checkCaretInsideSuggestionWrapper, this);
            }
            ed.onPaste.add(this.onPaste, this);
            ed.onBeforeExecCommand.add(this.onBeforeCommand, this);
            ed.onGetContent.add(function(ed, details) {
                var clearedContent;
                if (this.currentSuggestionDefinition && details.format == 'html') {
                    clearedContent = details.content.replace(
                            /<span class="suggestion-search">([^<]*)<\/span>/g, '$1');
                    details.content = clearedContent.replace(
                            /<span class="suggestion-wrapper">([^<]*)<\/span>/g, '$1');
                }
            }, this);
        },

        /**
         * Register the command to disable/enable the individual suggestions.
         */
        registerToggleCommand: function() {
            this.ed.addCommand('mceSuggestionToggle', function(ui, details) {
                var enable, toggle, triggerChar, def, triggerCode, oneEnabled;
                function toggleDefinition(def, toggle, enable, self) {
                    if (toggle) {
                        def.enabled = !def.enabled;
                    } else {
                        def.enabled = enable;
                    }
                    if (!def.enabled && def == self.currentSuggestionDefinition) {
                        self.destroySuggestionWrapper();
                    }
                }
                if (details && details.action) {
                    oneEnabled = false;
                    toggle = details.action == 'toggle';
                    if (!toggle) {
                        if (details.action == 'enable') {
                            enable = true;
                        } else if (details.action == 'disable') {
                            enable = false;
                        } else {
                            return;
                        }
                    }
                    triggerChar = details.triggerChar;
                    for (triggerCode in this.suggestionDefinitions) {
                        if (this.suggestionDefinitions.hasOwnProperty(triggerCode)) {
                            def = this.suggestionDefinitions[triggerCode];
                            if (!triggerChar || def.triggerChar == triggerChar) {
                                toggleDefinition(def, toggle, enable, this);
                            }
                            oneEnabled = oneEnabled || def.enabled;
                        }
                    }
                    this.suggestionsEnabled = oneEnabled;
                }
            }, this);
        },

        /**
         * Extract and validate the suggestion definitions provided in the options.
         */
        extractSuggestionDefinitions: function(options) {
            var definitions, i, def;
            definitions = options.suggestionDefinitions;
            // definitions must be object or array of defintions
            if (definitions && tinymce.is(definitions, 'object')) {
                if (!tinymce.is(definitions, 'array')) {
                    this.addSuggestionDefinition(definitions);
                } else {
                    for (i = 0; i < definitions.length; i++) {
                        this.addSuggestionDefinition(definitions[i]);
                    }
                }
            }
        },

        addSuggestionDefinition: function(definition) {
            var def, wrapWhitespace;
            // ignore definitions with missing required members
            if (definition.triggerChar && definition.suggestions) {
                if (!this.suggestionDefinitions) {
                    this.suggestionDefinitions = {};
                }
                def = tinymce.extend({
                    prependTriggerChar: true
                }, definition);
                if (!tinymce.is(def.delimiters, 'array')) {
                    def.delimiters = this.defaultDelimiters;
                }
                wrapWhitespace = definition.wrapWhitespace;
                if (wrapWhitespace && wrapWhitespace.length > 0) {
                    def.wrapWhitespace = [];
                    if (tinymce.is(wrapWhitespace, 'array')) {
                        def.wrapWhitespace.push(wrapWhitespace[0]);
                        def.wrapWhitespace.push(wrapWhitespace.length > 1 ? wrapWhitespace[1]
                                : wrapWhitespace[0]);
                    } else {
                        def.wrapWhitespace[0] = def.wrapWhitespace[1] = wrapWhitespace.charAt(0);
                    }
                }
                // check for option that defines whether additional characters appended to query
                // narrow the result set. This true by default, but could for instance be set to
                // false if some special characters cause a split of the string and an OR query
                // with the split parts on the server.
                def.narrowingQuery = definition.narrowingQuery;
                if (def.narrowingQuery !== false) {
                    def.narrowingQuery = true;
                }
                // check for option to destroy suggestions if suggestions had no results for n
                // searches. A value > 1 is only possible for narrowing queries since we check
                // whether the current search term starts with the string for which no results
                //were found.                
                def.destroyAfterNoResults = definition.destroyAfterNoResults;
                if (def.destroyAfterNoResults == null) {
                    def.destroyAfterNoResults = 2;
                }
                if (def.destroyAfterNoResults > 1 && !def.narrowingQuery) {
                    def.destroyAfterNoResults = 0;
                }
                def.destroyAfterNoResultsOnEnter = definition.destroyAfterNoResultsOnEnter;
                if (def.destroyAfterNoResultsOnEnter !== false) {
                    def.destroyAfterNoResultsOnEnter = true;
                }
                // enabled by default
                def.enabled = !definition.disabled;
                // TODO reuse bound functions in all suggestions
                def.suggestions.setDeterminePositionCallback(this.determinePositionOfSuggestions
                        .bind(this));
                def.suggestions.setOnChoiceSelectedCallback(this.onSuggestionSelected.bind(this));
                def.suggestions.setOnHideCallback(this.onSuggestionsHidden.bind(this));
                def.suggestions.setOnShowCallback(this.onSuggestionsShown.bind(this));
                def.suggestions.setOnChoicesFoundCallback(this.onSuggestionsFound.bind(this));
                // use string value of trigger char code as key
                this.suggestionDefinitions[String(def.triggerChar.charCodeAt(0))] = def;
                // enabled if at least one is enabled
                this.suggestionsEnabled = this.suggestionsEnabled || def.enabled;
            }
        },

        onEditorRemoved: function(ed) {
            var i;
            clearTimeout(this.fireValueChangedTimeout);
            clearInterval(this.checkBlurInterval);
            for (i in this.suggestionDefinitions) {
                if (this.suggestionDefinitions.hasOwnProperty(i)) {
                    this.suggestionDefinitions[i].suggestions.destroy();
                }
            }
        },

        onKeydown: function(ed, e) {
            var keyCode, curDef, log, currentInput, destroy;
            curDef = this.currentSuggestionDefinition;
            if (!this.suggestionsEnabled || !curDef) {
                return true;
            }
            keyCode = e.keyCode;
            if (keyCode === 27) {
                // escape key
                this.destroySuggestionWrapper();
                // cancel event to avoid propagation which will close the full-screen in safari on
                // mac, also solves a bug in opera where kbd focus is lost after removing the
                // suggestions wrapper with ESC and selection was collapsed
                return tinymce.dom.Event.cancel(e);
            } else if (keyCode === 38 || keyCode === 40) {
                // up and  down arrow keys
                if (this.suggestionsShown) {
                    curDef.suggestions.focusNextChoice(keyCode === 38, true);
                    return tinymce.dom.Event.cancel(e);
                } else if (keyCode === 40) {
                    // open suggestions on down arrow, e.g. after blur
                    this.search();
                    return tinymce.dom.Event.cancel(e);
                }
            } else if (keyCode === 13) {
                // block enter key and select focused suggestion if there is any
                if (this.suggestionsShown) {
                    if (curDef.destroyAfterNoResultsOnEnter) {
                        log = this.suggestionsFoundLog;
                        if (log && log.noResultsCount > 0) {
                            currentInput = this.getTrimmedSearchValue();
                            if (curDef.narrowingQuery) {
                                destroy = currentInput.indexOf(log.queryValue) == 0;
                            } else {
                                destroy = currentInput == log.queryValue;
                            }
                        }
                    }
                    if (destroy) {
                        this.destroySuggestionWrapper(null);
                    } else {
                        curDef.suggestions.selectFocusedChoice();
                    }
                }
                return tinymce.dom.Event.cancel(e);
            } else if (keyCode === 8) {
                // backspace: check if caret is after trigger char and if so destroy as backspace
                //would remove the trigger char
                if (this.isCaretBehindTrigger()) {
                    this.destroySuggestionWrapper();
                }
            }
            return true;
        },

        isCaretBehindTrigger: function() {
            var range, startNode, text, previousSiblings;
            var domUtils = this.ed.dom;
            range = this.ed.selection.getRng();
            startNode = range.startContainer;
            if (startNode.nodeType === 3) {
                startNode = startNode.parentNode;
            }
            // assume we are inside wrapper or search SPAN, not checking for BOLD and stuff 
            if (!domUtils.hasClass(startNode, 'suggestion-wrapper')) {
                // caret is in search SPAN. startContainer might be element node if the element
                // has no children yet (opera)
                if (range.startContainer.nodeType === 3) {
                    // check text left of caret, take zero-width-space into account
                    text = range.startContainer.nodeValue.substring(0, range.startOffset);
                    if (text.length > 0 && this.stripZeroWidthWhiteSpaces(text).length > 0) {
                        return false;
                    } else {
                        // text is empty but there might be other none empty text node siblings
                        if (range.startContainer.previousSibling) {
                            previousSiblings = this.findPreviousTextNodeSiblings(
                                    range.startContainer, startNode);
                            if (this.stripZeroWidthWhiteSpaces(previousSiblings.aggregatedText).length > 0) {
                                return false;
                            }
                        }
                    }
                }
            }
            return true;
        },

        onKeypress: function(ed, e) {
            var curElem, searchElem, textChild, charCode, charValue;
            if (!this.suggestionsEnabled) {
                return true;
            }
            charCode = e.charCode;
            // handle all keypress events that have a charCode but ignore them if the control key is
            // pressed because of the special meaning (e.g. ctrl-v) they might have. Moreover
            // browsers (especially opera) modify the charCode if ctrl key is down.
            // Also check for alt-key since altgr will set ctrl and alt key in Chrome, Opera and IE to true.
            if (charCode && (!e.ctrlKey || e.altKey)) {
                curElem = ed.selection.getNode();
                if (this.currentSuggestionDefinition
                        && ed.dom.hasClass(curElem, 'suggestion-wrapper')) {
                    charValue = String.fromCharCode(charCode);
                    // TODO check if caret is at position 1?
                    // insert charcode at beginning of searchElem span and move caret after
                    // this character. Fixes chrome behavior and is required when caret was moved with cursor keys 
                    searchElem = ed.dom.select('span', curElem)[0];
                    if (searchElem) {
                        // set content via textProperty, because it will automatically create a text child.
                        // Reading that property will also return an empty string if there is no text node
                        // and also fixes some strange Chrome issues (clearing linebreaks resulting from invisible BRs, whoever adds them)
                        searchElem[this.textProperty] = charValue + searchElem[this.textProperty];
                        textChild = searchElem.firstChild;
                    } else {
                        // create another span which will receive the search term
                        // in opera it is not possible to set caret in a textchild which only
                        // contains a space: insert a nbsp instead
                        if (tinymce.isOpera && charValue === ' ') {
                            charValue = '&nbsp;';
                        }

                        ed.selection.setContent('<span class="suggestion-search">' + charValue
                                + '</span>');
                        searchElem = ed.dom.select('.suggestion-search', curElem)[0];
                        textChild = searchElem.firstChild;
                    }
                    // must set caret on text node otherwise the caret is set to the end
                    ed.selection.setCursorLocation(textChild, 1);
                    return tinymce.dom.Event.cancel(e);
                } else if (this.handleTriggerChar(charCode, curElem)) {
                    this.search('');
                    return tinymce.dom.Event.cancel(e);
                }
            }
            return true;
        },

        onKeyup: function(ed, e) {
            var keyCode = e.keyCode, curDef, curElem, searchTerm, caretPosDetails;
            curDef = this.currentSuggestionDefinition;
            // TODO ignore meta-keys for speed-up
            // ignore if disabled or we are currently not inside a suggestions wrapper, also
            // ignore function keys and ESC key which is handled in keydown
            if (!this.suggestionsEnabled || !curDef || keyCode == 27
                    || (keyCode >= 112 && keyCode <= 123)) {
                return true;
            }
            // keys 33 to 40 are page-up, page-down, end, home and arrow keys
            // TODO what about DEL (46) and BACKSPACE (8)?
            if (keyCode < 33 || keyCode > 40) {
                // another key was pressed and content might have changed (note: no charCode in keyUp)
                curElem = ed.selection.getNode();
                if (!curElem) {
                    curDef.suggestions.hideChoices();
                } else if (curElem.className === 'suggestion-search') {
                    searchTerm = curElem[this.textProperty];
                    this.checkValueChanged(searchTerm);
                } else if (ed.dom.hasClass(curElem, 'suggestion-wrapper')) {
                    searchTerm = curElem[this.textProperty];
                    searchTerm = searchTerm.substring(searchTerm.indexOf(curDef.triggerChar) + 1);
                    this.checkValueChanged(searchTerm);
                } else {
                    // after backspace or del the search span might be empty (rare since there is
                    // usually a br) empty and than the caret is placed after the wrapper -> replace
                    // in wrapper
                    if (keyCode == 8 || keyCode == 46) {
                        curElem = ed.dom.select('.suggestion-wrapper')[0];
                        if (curElem && curElem[this.textProperty]) {
                            ed.selection.setCursorLocation(curElem.firstChild, 1);
                            // just assume the search is empty and break
                            this.checkValueChanged('');
                            return true;
                        }
                    }
                    this.destroySuggestionWrapper();
                }
            } else {
                // check if still inside suggestion wrapper
                caretPosDetails = this.caretInsideSuggestionWrapper();
                if (!caretPosDetails.inside) {
                    this.destroySuggestionWrapper(caretPosDetails);
                }
            }
            return true;
        },

        /**
         * Callback that is invoked when the content of the tinymce editor receives a mouse up
         * event.
         * 
         * @param {tinymce.Editor} ed The editor instance
         * @param {Event} event The W3C DOM event
         */
        onMouseup: function(ed, event) {
            var caretPosDetails;
            // not just checking for left button because right moves caret too
            if (this.currentSuggestionDefinition) {
                clearInterval(this.checkBlurInterval);
                // doesn't work in IE9, after right button up the caret moved visually but
                // caretInside function determines it is still inside -> ignore right button
                // in IE 9 and handle context menu
                if (!tinymce.isIE9 || event.button != 2) {
                    this.checkCaretInsideSuggestionWrapper();
                }
            }
        },

        onPaste: function(ed, event) {
            // don't handle the paste event if the paste plugin is used because the content would be
            // inserted before suggestion wrapper content after destroying it. In case of the paste
            // plugin the handler for the mceInsertContent command will handle the paste plugin.
            if (!ed.plugins.paste && this.currentSuggestionDefinition) {
                // always destroy the suggestions because there is no way (?) to inspect the content
                // to be pasted and therefore it's not possible to check for linebreaks or HTML 
                // content which would corrupt the wrapper SPANs
                this.destroySuggestionWrapper(null);
            }
        },

        onBeforeCommand: function(ed, cmd, ui, val) {
            // check for mceInsertContent which is for example triggered during paste
            if (cmd == 'mceInsertContent') {
                // if value contains HTML markup destroy the suggestions 
                //if (this.currentSuggestionDefinition && val && val.match(/[<>]/)) {

                // for now always cancel suggestions because would have to handle the case when
                // caret is in wrapper and not search element
                if (this.currentSuggestionDefinition) {
                    this.destroySuggestionWrapper(null);
                }
            }
        },

        onScroll: function() {
            var wrapperElem, ed, bottom, viewPort;
            var def = this.currentSuggestionDefinition;
            if (def) {
                if (this.suggestionsShown) {
                    def.suggestions.repositionChoices();
                }
                // TODO this is kind of expensive -> better debounce?
                // if bottom of wrapper is outside of viewport and suggestions are shown, hide them, otherwise show
                ed = this.ed;
                wrapperElem = ed.dom.select('.suggestion-wrapper')[0];
                if (wrapperElem) {
                    bottom = wrapperElem.getBoundingClientRect().bottom;
                    viewPort = ed.dom.getViewPort(ed.getWin());
                    if (this.suggestionsShown) {
                        if (viewPort.y > bottom || viewPort.h < bottom) {
                            def.suggestions.hideChoices();
                        }
                    } else {
                        if (viewPort.y < bottom && viewPort.h > bottom) {
                            // trigger a search which will show choices automatically if 
                            // possible (minLength and similar conditions met)
                            this.search(null);
                        }
                    }
                }
            }
        },

        /**
         * Check if the editor is in suggestion mode and if so, whether the caret is still in the
         * suggestion wrapper. If it is not inside the wrapper the suggestion mode is canceled.
         */
        checkCaretInsideSuggestionWrapper: function() {
            var caretPosDetails;
            if (this.currentSuggestionDefinition) {
                caretPosDetails = this.caretInsideSuggestionWrapper();
                if (!caretPosDetails.inside) {
                    this.destroySuggestionWrapper(caretPosDetails);
                }
            }
        },

        caretInsideSuggestionWrapper: function() {
            var mceSelection, startInside, startAtOffsetZero, endInside, endAtOffsetZero;
            var range, node, collapsed;
            mceSelection = this.ed.selection;
            node = mceSelection.getStart();
            startInside = this.nodeInsideSuggestionWrapper(node);
            // handle offset 0 inside wrapper SPAN as outside since next input would be outside
            startAtOffsetZero = false;
            if (startInside && node.tagName === 'SPAN'
                    && this.ed.dom.hasClass(node, 'suggestion-wrapper')) {
                range = mceSelection.getRng();
                if (range.startOffset === 0) {
                    startInside = false;
                    startAtOffsetZero = true;
                }
            }
            collapsed = mceSelection.isCollapsed();
            if (startInside && !collapsed) {
                endAtOffsetZero = false;
                node = mceSelection.getEnd();
                endInside = this.nodeInsideSuggestionWrapper(node);
                // TODO probably not necessary because should always be left of end -> check browsers
                if (endInside && node.tagName === 'SPAN'
                        && this.ed.dom.hasClass(node, 'suggestion-wrapper')) {
                    if (!range) {
                        range = mceSelection.getRng();
                    }
                    if (range.endOffset === 0) {
                        endInside = false;
                        endAtOffsetZero = true;
                    }
                }
            } else if (collapsed) {
                endInside = startInside;
                endAtOffsetZero = startAtOffsetZero;
            }
            return {
                inside: startInside && endInside,
                startInside: startInside,
                startAtOffsetZero: startAtOffsetZero,
                endInside: endInside,
                endAtOffsetZero: endAtOffsetZero,
                collapsed: collapsed
            };
        },

        nodeInsideSuggestionWrapper: function(node) {
            var tagName;
            var domUtils = this.ed.dom;
            while (true) {
                if (!node) {
                    return false;
                }
                if (node.className === 'suggestion-search'
                        || domUtils.hasClass(node, 'suggestion-wrapper')) {
                    return true;
                }
                tagName = node.tagName;
                // break on some tag names that are not children of the suggestion
                if (tagName === 'P' || tagName === 'LI' || tagName === 'DIV' || tagName === 'BODY') {
                    break;
                }
                node = node.parentNode;
            }
            return false;
        },

        /**
         * Remove all zero-width white spaces from the end of the text and return the cleaned
         * version.
         * 
         * @param {String} text The text to process
         * @return {String} The cleaned text
         */
        stripZeroWidthWhiteSpaceFromEnd: function(text) {
            var i, idx = -1;
            for (i = text.length - 1; i >= 0; i--) {
                // check for zero-width whitespace and zero-width nbsp
                if (text[i] != '\u200B' && text[i] != '\uFEFF') {
                    idx = i + 1;
                    break;
                }
            }
            if (idx > 0) {
                if (idx != text.length) {
                    text = text.substring(0, idx);
                }
                return text;
            }
            // no none zero-width whitespace char found
            return '';
        },

        /**
         * Remove all zero-width white spaces from the text and return the cleaned version.
         * 
         * @param {String} text The text to process
         * @return {String} The cleaned text
         */
        stripZeroWidthWhiteSpaces: function(text) {
            return text.replace(/[\uFEFF\u200B]/g, '');
        },

        getTextOfPreviousNonEmptyNode: function(node) {
            var textContent, sibling;
            // stop at linebreak producing nodes
            if (!node || (node.nodeType === 1 && this.linebreakingTags.indexOf(node.tagName) != -1)) {
                return '';
            }
            sibling = node.previousSibling;
            if (sibling) {
                // stop at BRs
                // TODO special handling for mce_bogus BRs?
                if (sibling.tagName === 'BR') {
                    textContent = '';
                } else {
                    textContent = this.stripZeroWidthWhiteSpaceFromEnd(sibling[this.textProperty]);
                    if (textContent.length === 0) {
                        textContent = this.getTextOfPreviousNonEmptyNode(sibling);
                    }
                }
            } else {
                // current node has no sibling, check text content of previous sibling of parent
                textContent = this.getTextOfPreviousNonEmptyNode(node.parentNode);
            }
            return textContent;
        },

        /**
         * Find the first child node that is a text node. If the provided node is already a text
         * node it will be returned.
         * 
         * @param {Node} node The node whose children should be checked. If this node is an element
         *            node and its startOffset child node is not a text node the first child will be
         *            checked, and so on.
         * @param {Integer} startOffset The (zero-based) number identifying the child node to check.
         * @return {Node} the found text node or the first element node that has no text child
         */
        findTextChild: function(node, startOffset) {
            if (node.nodeType === 3 || node.childNodes.length === 0) {
                return node;
            } else {
                // set 0 as new startOffset
                return this.findTextChild(node.childNodes[startOffset], 0);
            }
        },

        /**
         * Test whether the editor is in suggestion-mode and whether the mode can be interrupted by
         * typing a trigger character of another or the same suggestion-mode.
         * 
         * @param {Node} curElem The currently focused element node
         * @return {Boolean} False if the editor is not in suggestion-mode or the the current
         *         suggestion-mode can be interrupted by a trigger char. The latter is possible if
         *         the search input does not start with a whitespace wrapper character and the
         *         option triggerCharInterruptsSuggestion is set or the definition has a narrowing
         *         query which did not return a result for a search value the current one starts
         *         with.
         */
        inNotInterruptableSuggestionMode: function(curElem) {
            var interruptable, log, content, startsWithWrapper;
            var curDef = this.currentSuggestionDefinition;
            if (curDef) {
                interruptable = false;
                if (curElem.className === 'suggestion-search') {
                    log = this.suggestionsFoundLog;
                    // get trimmed content if necessary
                    if (curDef.wrapWhitespace
                            || (log && log.noResultsCount && curDef.destroyAfterNoResults)) {
                        content = curElem[this.textProperty];
                        content = this.stripZeroWidthWhiteSpaces(content).trim();
                    }
                    if (curDef.wrapWhitespace) {
                        startsWithWrapper = content.charAt(0) == curDef.wrapWhitespace[0];
                    }
                    // in case it is a narrowing query and current input starts with a previous
                    // query which did not return a result, suggestion mode can be interrupted
                    interruptable = !startsWithWrapper
                            && (this.triggerCharInterruptsSuggestion || log && content
                                    && content.indexOf(log.queryValue) == 0);
                }
                return !interruptable;
            }
            return false;
        },

        /**
         * Check whether the provided charCode is one of the registered triggers. If it is one, test
         * whether the position of the caret is at the start of a line or right behind one of the
         * delimiters defined by the suggestion-definition for that trigger. In case this condition
         * holds a special wrapper SPAN element that contains the trigger character is injected and
         * the autocompleter mode is activated.
         * 
         * @param {Integer} charCode Character code to test
         * @param {Node} curElem The currently focused element node.
         * @return {Boolean} true if the charCode is a trigger, the delimiter/line start condition
         *         is met and the suggestion wrapper was injected. False otherwise.
         */
        handleTriggerChar: function(charCode, curElem) {
            var ed, mceSelection, range, startContainer, startOffset, wrapperElem;
            var textContent, lastChar, win;
            var definition = this.suggestionDefinitions[String(charCode)];
            if (!definition || !definition.enabled
                    || this.inNotInterruptableSuggestionMode(curElem)) {
                // not handling if not a trigger character or suggestion disbaled
                return false;
            }
            ed = this.ed;
            mceSelection = ed.selection;
            range = mceSelection.getRng();
            // in Firefox the startContainer of the range is usually the parent element of the 
            // currently focused text node if that text node only contains the empty string 
            startContainer = this.findTextChild(range.startContainer, range.startOffset);
            if (startContainer != range.startContainer) {
                // startOffset is start of the text node found by findTextChild if the passed
                // container was an element node
                startOffset = 0;
            } else {
                startOffset = range.startOffset;
            }
            // TODO check for delimiter on right side of current position and if so are there other
            // delimiters? Also would need to handle the case of an not-collapsed selection
            if (startOffset == 0) {
                textContent = this.getTextOfPreviousNonEmptyNode(startContainer);
            } else {
                textContent = startContainer[this.textProperty].substring(0, startOffset);
                textContent = this.stripZeroWidthWhiteSpaceFromEnd(textContent);
                if (textContent.length === 0) {
                    textContent = this.getTextOfPreviousNonEmptyNode(startContainer);
                }
            }
            // if there is no text on the line completion can be started
            if (textContent.length !== 0) {
                lastChar = textContent[textContent.length - 1];
                if (definition.delimiters.indexOf(lastChar) == -1) {
                    // preceding text is not empty and does not contain a separator character
                    // so don't activate suggestions and return that event wasn't handled
                    return false;
                }
            }
            // cancel current suggestion mode if there is one
            if (this.currentSuggestionDefinition) {
                this.destroySuggestionWrapper();
                curElem = mceSelection.getNode();
            }
            if (!mceSelection.isCollapsed()) {
                // search whole DOM since curElem might be replaced when setting the content
                curElem = null;
            }
            mceSelection.setContent('<span class="suggestion-wrapper">'
                    + String.fromCharCode(charCode) + '</span>');
            wrapperElem = ed.dom.select('span.suggestion-wrapper', curElem)[0];
            mceSelection.setCursorLocation(wrapperElem.firstChild, 1);
            this.currentSuggestionDefinition = definition;
            this.suggestionsFoundLog = null;
            win = ed.getWin();
            if (this.boundScrollListener) {
                win.removeEventListener('scroll', this.boundScrollListener);
            } else {
                this.boundScrollListener = this.onScroll.bind(this);
                win.addEventListener('scroll', this.boundScrollListener, false);
            }
            return true;
        },

        checkValueChanged: function(value) {
            var self, untrimmedValue, curDef, log;
            untrimmedValue = this.stripZeroWidthWhiteSpaces(value);
            value = untrimmedValue.trim();
            // when typing a whitespace after trigger collapse
            if (value.charAt(0) != untrimmedValue.charAt(0)) {
                this.destroySuggestionWrapper();
                return;
            }
            curDef = this.currentSuggestionDefinition;
            if (curDef.wrapWhitespace) {
                // strip whitespace wrapping char
                if (value.charAt(0) == curDef.wrapWhitespace[0]) {
                    value = value.substring(1);
                    // if the last char is the closing wrapping character collapse
                    if (value.charAt(value.length - 1) == curDef.wrapWhitespace[1]) {
                        this.destroySuggestionWrapper();
                        return;
                    }
                }
            }
            log = this.suggestionsFoundLog;
            if (log && log.noResultsCount > 0 && curDef.destroyAfterNoResults > 1) {
                // since it is a narrowing search we can close the suggestions if 
                // current query starts with last query that returned no results and has
                // at least destroyAfterNoResults more chars
                if (value.length >= log.queryValue.length + curDef.destroyAfterNoResults
                        && value.indexOf(log.queryValue) == 0) {
                    this.destroySuggestionWrapper();
                    return;
                }
            }
            if (value != this.currentQueryValue) {
                if (this.suggestionsShown) {
                    // since the value changed the word might have moved on another line
                    this.currentSuggestionDefinition.suggestions.repositionChoices();
                }
                clearTimeout(this.fireValueChangedTimeout);
                self = this;
                this.fireValueChangedTimeout = setTimeout(function() {
                    self.search(value);
                }, this.fireValueChangedDelay);
            }
        },

        search: function(queryValue) {
            var curDef = this.currentSuggestionDefinition;
            if (!curDef) {
                return;
            }
            if (queryValue == null) {
                queryValue = this.getTrimmedSearchValue();
            }
            this.currentQueryValue = queryValue;
            curDef.suggestions.queryValueChanged(queryValue);
        },

        getTrimmedSearchValue: function() {
            var searchElem, value;
            var curDef = this.currentSuggestionDefinition;
            if (!curDef) {
                return null;
            }
            searchElem = this.ed.dom.select('.suggestion-search');
            if (searchElem.length === 0) {
                // only wrapper with trigger char exists -> empty string
                return '';
            }
            value = searchElem[0][this.textProperty];
            value = this.stripZeroWidthWhiteSpaces(value).trim();
            if (curDef.wrapWhitespace) {
                // strip whitespace wrapping char
                if (value.charAt(0) == curDef.wrapWhitespace[0]) {
                    value = value.substring(1);
                }
            }
            return value;
        },

        onSuggestionsShown: function() {
            this.suggestionsShown = true;
            this.checkBlurInterval = setInterval(function() {
                var def = this.currentSuggestionDefinition;
                var activeElement;
                if (!this.editorFrameId) {
                    // TODO is this the correct way to get ID of IFrame/editable element of the editor?
                    // note: cannot get ID in init method because editor isn't rendered yet, so 
                    // resolving ID lazily
                    this.editorFrameId = this.ed.getContentAreaContainer().firstChild.id;
                }
                activeElement = document.activeElement;
                // TODO is there a better way to check whether the editor is still focused?
                // sometimes the activeElement is null in IE9, no idea why
                if (def && activeElement && activeElement.id !== this.editorFrameId) {
                    def.suggestions.hideChoices();
                }
            }.bind(this), this.hideSuggestionsOnBlurDelay);
        },
        onSuggestionsHidden: function() {
            this.suggestionsShown = false;
            clearInterval(this.checkBlurInterval);
            clearTimeout(this.fireValueChangedTimeout);
        },
        onSuggestionSelected: function(choicesElem, token, suggestionValue) {
            var suggestionWrapper, mceSelection, def, prefix, suffix;
            suggestionWrapper = this.ed.dom.select('span.suggestion-wrapper');
            if (suggestionWrapper.length === 0) {
                return;
            }
            suggestionWrapper = suggestionWrapper[0];
            mceSelection = this.ed.selection;
            mceSelection.select(suggestionWrapper);
            def = this.currentSuggestionDefinition;
            prefix = def.prependTriggerChar ? def.triggerChar : '';
            if (def.wrapWhitespace && /\s/.test(suggestionValue)) {
                prefix += def.wrapWhitespace[0];
                suffix = def.wrapWhitespace[1];
            } else {
                suffix = '';
            }
            mceSelection.setContent(prefix + suggestionValue + suffix);
            this.currentSuggestionDefinition = null;
        },

        onSuggestionsFound: function(queryValue, choices) {
            var curDef, log, currentInput, match, newCount;
            curDef = this.currentSuggestionDefinition;
            if (!curDef || choices.length > 0) {
                this.suggestionsFoundLog = null;
                return;
            }
            log = this.suggestionsFoundLog;
            if (!log) {
                log = {
                    noResultsCount: 0,
                    queryValue: null
                };
                this.suggestionsFoundLog = log;
            }
            currentInput = this.getTrimmedSearchValue();
            // test if current input was changed in meantime
            if (curDef.narrowingQuery) {
                // check whether input starts with query
                match = currentInput.indexOf(queryValue) === 0;
            } else {
                match = currentInput == queryValue;
            }
            if (!match) {
                // input was changed in a way that makes the event irrelevant
                this.suggestionsFoundLog = null;
            } else {
                newCount = 1;
                if (log.noResultsCount > 0 && curDef.narrowingQuery) {
                    // ignore queries with same string (fast typing: char key followed by backspace)
                    if (queryValue == log.queryValue) {
                        newCount = log.noResultsCount;
                    } else if (queryValue.indexOf(log.queryValue) == 0) {
                        // can only consider as new no-results event if new query starts with previous
                        newCount = log.noResultsCount + 1;
                    }
                }
                log.noResultsCount = newCount;
                // only save query value on first miss to be able to check following queries
                // against that miss when trying to find the number of additionally typed chars
                // after a miss
                if (newCount == 1) {
                    log.queryValue = queryValue;
                }
            }
            // destroying the suggestions when no-results event occured feels strange since no
            // direct user action is involved. Thus, we only do it if it should stop on first 
            // no-results event. The other cases are handled in checkValueChanged which is
            // triggered by keyup.
            if (curDef.destroyAfterNoResults == 1) {
                this.destroySuggestionWrapper(null);
            }
        },

        determinePositionOfSuggestions: function(relative) {
            var ed, domUtils, editableContainer, rect, anchorNode, anchorNodeRect;
            ed = this.ed;
            domUtils = ed.dom;
            anchorNode = domUtils.select('span.suggestion-wrapper');
            if (!anchorNode || anchorNode.length === 0)
                return null;
            anchorNode = anchorNode[0];
            // get position of IFrame/editable content element
            editableContainer = ed.getContentAreaContainer().firstChild;
            rect = tinymce.DOM.getPos(editableContainer);
            rect.width = editableContainer.offsetWidth;
            // note: not using domUtils.getRect because it does not include the viewport offset in
            // firefox; Additionally we only need the bottom of the returned rect.
            anchorNodeRect = anchorNode.getBoundingClientRect();
            return {
                left: rect.x + anchorNodeRect.left,
                top: rect.y + anchorNodeRect.bottom,
                // define width and right edge relative to right edge of editor (iFrame window)
                width: rect.width - anchorNodeRect.left,
                rightOffset: editableContainer.ownerDocument.documentElement.clientWidth - rect.x - rect.width
            };
        },

        destroySuggestionWrapper: function(caretPosDetails) {
            var suggestionWrapper, bookmark, newContent, range, textSiblings;
            var newTextNode, newStartNode, newStartOffset, newEndNode, newEndOffset;
            var domUtils = this.ed.dom;
            var mceSelection = this.ed.selection;
            clearTimeout(this.fireValueChangedTimeout);
            clearInterval(this.checkBlurInterval);
            if (this.boundScrollListener) {
                this.ed.getWin().removeEventListener('scroll', this.boundScrollListener);
                this.boundScrollListener = null;
            }
            this.currentSuggestionDefinition.suggestions.hideChoices();
            suggestionWrapper = domUtils.select('span.suggestion-wrapper');
            if (suggestionWrapper.length === 0) {
                // reset just in case
                this.currentSuggestionDefinition = null;
                this.currentQueryValue = null;
                return;
            }
            if (!caretPosDetails) {
                caretPosDetails = this.caretInsideSuggestionWrapper();
            }
            suggestionWrapper = suggestionWrapper[0];
            // Note: alternative solution would be creating a bookmark, copying the innerHTML of the
            // search-element and moving to it after replacing the wrapper element with that innerHTML.
            // But this does set the caret after the inserted content if it was at offset 0 of the
            // wrapper element. So we going the long way by calculating the position of the caret
            // inside the wrapper and restoring it after replacing the wrapper with a single text
            // node.
            newContent = suggestionWrapper[this.textProperty];
            newContent = this.stripZeroWidthWhiteSpaces(newContent);
            // create a new textNode to replace the search element
            newTextNode = suggestionWrapper.ownerDocument.createTextNode(newContent);
            if (caretPosDetails.inside || caretPosDetails.startAtOffsetZero
                    || caretPosDetails.endAtOffsetZero) {
                range = mceSelection.getRng();
                newStartNode = range.startContainer;
                newStartOffset = range.startOffset;
                if (caretPosDetails.startInside) {
                    // IE sometimes returns the span element as start and end container -> take child
                    // of offset (which might be null if offset is 1 and only 1 child, but doesn't matter)
                    // and set offset to 0 because it is the start of that child
                    if (newStartNode.nodeType == 1) {
                        newStartNode = newStartNode.childNodes[newStartOffset];
                        newStartOffset = 0;
                    }
                    // the search span might contain many text nodes, thus calculate the actual
                    // startOffset as if all text children where joined into one
                    textSiblings = this.findPreviousTextNodeSiblings(newStartNode,
                            suggestionWrapper);
                    newStartNode = newTextNode;
                    newStartOffset = newStartOffset + textSiblings.aggregatedText.length;
                } else {
                    if (caretPosDetails.startAtOffsetZero) {
                        newStartNode = newTextNode;
                        newStartOffset = 0;
                    }
                }
                if (caretPosDetails.collapsed) {
                    newEndNode = newStartNode;
                    newEndOffset = newStartOffset;
                } else {
                    // restore the end of the selection
                    if (caretPosDetails.startInside && range.endContainer == range.startContainer) {
                        newEndNode = newStartNode;
                        newEndOffset = range.endOffset + textSiblings.aggregatedText.length;
                    } else {
                        if (caretPosDetails.endInside == undefined) {
                            caretPosDetails.endInside = this
                                    .nodeInsideSuggestionWrapper(mceSelection.getEnd());
                        }
                        if (caretPosDetails.endInside) {
                            textSiblings = this.findPreviousTextNodeSiblings(range.endContainer,
                                    suggestionWrapper);
                            newEndNode = newTextNode;
                            newEndOffset = range.endOffset + textSiblings.aggregatedText.length;
                        } else {
                            newEndNode = range.endContainer;
                            newEndOffset = range.endOffset;
                        }
                    }
                }
            }
            suggestionWrapper.parentNode.replaceChild(newTextNode, suggestionWrapper);
            // move caret if it was inside the wrapper element
            if (newStartNode) {
                range = domUtils.createRng();
                range.setStart(newStartNode, newStartOffset);
                range.setEnd(newEndNode, newEndOffset);
                mceSelection.setRng(range);
            }

            this.currentSuggestionDefinition = null;
            this.currentQueryValue = null;
        },

        findPreviousTextNodeSiblings: function(textNode, parentNode) {
            var i, node, innerResult;
            var textNodeFound = false;
            var siblings = [];
            var aggregatedText = '';
            // start at parentNode in depth-first style and collect all text child nodes until textNode is reached
            for (i = 0; i < parentNode.childNodes.length; i++) {
                node = parentNode.childNodes[i];
                if (node.nodeType === 3) {
                    if (node == textNode) {
                        textNodeFound = true;
                        break;
                    } else {
                        siblings.push(node);
                        aggregatedText += node.nodeValue;
                    }
                } else {
                    innerResult = this.findPreviousTextNodeSiblings(textNode, node);
                    siblings = siblings.concat(innerResult.siblings);
                    aggregatedText += innerResult.aggregatedText;
                    if (innerResult.textNodeFound) {
                        textNodeFound = true;
                        break;
                    }
                }
            }
            return {
                textNodeFound: textNodeFound,
                siblings: siblings,
                aggregatedText: aggregatedText
            };
        },

        /**
         * Creates control instances based in the incomming name. This method is normally not needed
         * since the addButton method of the tinymce.Editor class is a more easy way of adding
         * buttons but you sometimes need to create more complex controls like listboxes, split
         * buttons etc then this method can be used to create those.
         * 
         * @param {String} n Name of the control to create.
         * @param {tinymce.ControlManager} cm Control manager to use inorder to create new control.
         * @return {tinymce.ui.Control} New control instance or null if no control was created.
         */
        createControl: function(n, cm) {
            return null;
        },

        /**
         * Returns information about the plugin as a name/value array. The current keys are
         * longname, author, authorurl, infourl and version.
         * 
         * @return {Object} Name/value array containing information about the plugin.
         */
        getInfo: function() {
            return {
                longname: 'Suggestion plugin',
                author: 'rwi',
                authorurl: '',
                infourl: '',
                version: "0.9"
            };
        }
    };
    // use tool for external plugins which handles delayed registration after tinymce is loaded
    if (window.tinymceExternalPluginInitializer) {
        tinymceExternalPluginInitializer.add('tinymce.plugins.SuggestionsPlugin', 'suggestions',
                pluginDef);
    } else {
        tinymce.create('tinymce.plugins.SuggestionsPlugin', pluginDef);
        // Register plugin
        tinymce.PluginManager.add('suggestions', tinymce.plugins.SuggestionsPlugin);
    }
})();