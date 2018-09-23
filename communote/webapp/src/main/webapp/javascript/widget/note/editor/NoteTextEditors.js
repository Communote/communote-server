/**
 * Base class for editors to edit or create note texts. This base class doesn't really do anything.
 */
// TODO should we use Options mixin?
// TODO add support for charCode based keyboardShortcuts?
NoteTextEditor = new Class({
    autoresizeMinHeight: 0,
    kbdShortcuts: null,
    noteEditorPrefsKeyPrefix: 'noteEditorPrefs.',

    initialize: function(formElem, options) {
        var shortcuts, i;
        var minHeight = 0;
        var minHeightMin = 70;
        var minHeightMax = 140;
        if (formElem) {
            this.refresh(formElem);
        }
        if (options) {
            shortcuts = options.keyboardShortcuts;
            if (shortcuts) {
                if (typeOf(shortcuts) === 'array') {
                    shortcuts.forEach(this.addKeyboardShortcut, this);
                } else {
                    this.addKeyboardShortcut(shortcuts);
                }
            }
            if (options.autoresizeMinHeight > 0) {
                minHeight = options.autoresizeMinHeight;
            }
            if (options.autoresizeMinHeightUpperLimit > 0) {
                maxHeight = options.autoresizeMinHeightUpperLimit;
            }
            if (options.autoresizeMinHeightLowerLimit > 0) {
                minHeight = options.autoresizeMinHeightLowerLimit;
            }
        }
        minHeight = Math.max(minHeight, minHeightMin);
        this.autoresizeMinHeight = Math.min(minHeight, minHeightMax);
    },

    addKeyboardShortcut: function(shortcutDescr) {
        var requiredAttributes;
        // descriptor must at least have a callback and a keyCode
        if (!shortcutDescr.callback || !shortcutDescr.keyCode > 0) {
            return;
        }
        requiredAttributes = [ 'keyCode' ];
        if (shortcutDescr.ctrlKey != null) {
            requiredAttributes.push('ctrlKey');
        }
        if (shortcutDescr.altKey != null) {
            requiredAttributes.push('altKey');
        }
        if (shortcutDescr.metaKey != null) {
            requiredAttributes.push('metaKey');
        }
        if (shortcutDescr.shiftKey != null) {
            requiredAttributes.push('shiftKey');
        }
        shortcutDescr.requiredAttributes = requiredAttributes;
        if (!this.kbdShortcuts) {
            this.kbdShortcuts = [];
        }
        this.kbdShortcuts.push(shortcutDescr);
        this.keyboardShortcutAdded();
    },

    removeKeyboardShortcutByKeycode: function(keyCode) {
        var i, found;
        shortcuts = this.kbdShortcuts;
        if (shortcuts && shortcuts.length) {
            while (true) {
                found = false;
                for (i = 0; i < shortcuts.length; i++) {
                    if (shortcuts[i].keyCode == keyCode) {
                        found = true;
                        break;
                    }
                }
                if (found) {
                    shortcuts.splice(i, 1);
                    this.keyboardShortcutRemoved();
                } else {
                    break;
                }
            }
        }
    },

    /**
     * Evaluate the passed keyboard event and invoke the callback of the first matching registered
     * keyboard shortcut. This method should be called when the editor caught a key event.
     * 
     * @param {KeyboardEvent} kbdEvent The DOM keyboard event to handle
     * @param {*} metadata - Optional NoteTextEditor specific additional metadata which is forwarded to the callback
     */
    callKeyboardShortcut: function(kbdEvent, metadata) {
        var i, shortcutDescr;
        for (i = 0; i < this.kbdShortcuts.length; i++) {
            shortcutDescr = this.kbdShortcuts[i];
            if (this.attributesMatch(kbdEvent, shortcutDescr, shortcutDescr.requiredAttributes)) {
                shortcutDescr.callback.call(null, metadata);
                if (shortcutDescr.cancelEvent) {
                    kbdEvent.cancelBubble = true;
                    kbdEvent.returnValue = false;
                    if (kbdEvent.stopPropagation) {
                        kbdEvent.stopPropagation();
                    }
                    if (kbdEvent.preventDefault) {
                        kbdEvent.preventDefault();
                    }
                    return false;
                } else {
                    return true;
                }
            }
        }
        return true;
    },

    attributesMatch: function(o1, o2, attributes) {
        var i, attr;
        var match = true;
        for (i = 0; i < attributes.length; i++) {
            attr = attributes[i];
            if (o1[attr] !== o2[attr]) {
                match = false;
                break;
            }
        }
        return match;
    },

    /**
     * Will be called when a keyboard shortcut was successfully added. The number of the currently
     * available shortcuts can be easily determined by reading the length of the #kbdShortcuts
     * array. Subclasses can use this method to add a keydown/keypress listener as soon as shortcuts
     * are available.
     */
    keyboardShortcutAdded: function() {
    },

    /**
     * Will be called when a keyboard shortcut was removed. The number of the currently available
     * shortcuts can be easily determined by reading the length of the #kbdShortcuts array.
     * Subclasses can use this method to remove a keydown/keypress listener as soon as all shortcuts
     * have been removed.
     */
    keyboardShortcutRemoved: function() {
    },

    getPreference: function(key) {
        return communoteLocalStorage.getItem(this.noteEditorPrefsKeyPrefix + key);
    },
    getPreferenceAsBoolean: function(key) {
        return (this.getPreference(key) === 'true');
    },
    setPreference: function(key, value) {
        var prefs, oldValue;
        if (typeof value == 'boolean') {
            oldValue = this.getPreferenceAsBoolean(key);
        } else {
            oldValue = this.getPreference(key);
        }
        if (oldValue == value) {
            return false;
        }
        communoteLocalStorage.setItem(this.noteEditorPrefsKeyPrefix + key, value);
        return true;
    },

    /**
     * @returns {boolean} true if the editor supports editing of HTML, false otherwise
     */
    supportsHtml: function() {
        return false;
    },

    /**
     * Focus the editor.
     */
    focus: function() {

    },

    /**
     * @return {Autocompleter|AutocompleterSuggestions} an attached autocompleter or suggestions
     *         object that suggests users or null if no such autocompleter is attached. If not null
     *         is returned the object must provide access to the resetQuery method.
     */
    getUserAutocompleter: function() {
        return null;
    },

    /**
     * @return {Autocompleter|AutocompleterSuggestions} an attached autocompleter or suggestions
     *         object that suggests tags or null if no such autocompleter is attached. If not null
     *         is returned the object must provide access to the resetQuery method.
     */
    getTagAutocompleter: function() {
        return null;
    },

    /**
     * Called after every refresh of the hosting widget
     * 
     * @param {Element} formElem The form element the editor is attached to. The element might have
     *            changed after construction or last invocation of this method.
     * @param {Function} [refreshCompleteCallback] Function to call as soon as the refresh is
     *            complete and the editor usable. This editor instance is passed to the callback.
     */
    refresh: function(formElem, refreshCompleteCallback) {
    },

    /**
     * Called before the hosting widget is refreshed or removed to allow freeing resources
     */
    cleanup: function() {
    },
    
    /**
     * @return {Element} the element the note text is typed into
     */
    getInputElement: function() {
        return null;
    },

    /**
     * @returns {String} the content of the editor
     */
    getContent: function() {
        return '';
    },

    /**
     * Sets the content of the editor to the provided content. A call to isDirty might return true.
     * 
     * @param [{String}] newContent The content to set, if null the content will be cleared
     */
    setContent: function(newContent) {
    },

    /**
     * Sets the content of the editor to the provided plaintext content. A call to isDirty might
     * return true.<br />
     * The default implementation just calls {@link #setContent} as it does not support HTML.
     * Implementations supporting HTML can do some special pre-processing before inserting the text.
     * 
     * @param [{String}] newContent The content to set, if null the content will be cleared
     */
    setContentFromPlaintext: function(newContent) {
        this.setContent(newContent);
    },

    /**
     * Resets the content of the editor to the provided content and sets the dirty-state to false
     * 
     * @param [{String}] newContent The content to set, if null the content will be cleared
     */
    resetContent: function(newContent) {
    },

    /**
     * Resets the content of the editor to the provided plaintext content and sets the dirty-state
     * to false<br />
     * The default implementation just calls {@link #resetContent} as it does not support HTML.
     * Implementations supporting HTML can do some special pre-processing before inserting the text.
     * 
     * @param [{String}] newContent The content to set, if null the content will be cleared
     */
    resetContentFromPlaintext: function(newContent) {
        this.resetContent(newContent);
    },

    isDirty: function() {
        return false;
    },

    /**
     * Remove keyboard focus from the editor if it has the focus.
     */
    unFocus: function() {

    }
});

(function() {
    var orgTinyMCESettins, settingsDialogContentElem;

    function getSettingsDialogContent(checkBoxes) {
        var html, i;
        if (!settingsDialogContentElem) {
            settingsDialogContentElem = new Element('p', {
                'class': 'cn-form-container'
            });
            html = '<p>' + getJSMessage('javascript.dialog.note.editor.preferences.intro') + '</p>';
            for (i = 0; i < checkBoxes.length; i++) {
                html += '<div class="checkbox cn-line"><input type="checkbox" id="'
                        + checkBoxes[i].id;
                html += '"' + checkBoxes[i].id + '><label for="' + checkBoxes[i].id
                        + '" class="cn-label">';
                html += getJSMessage(checkBoxes[i].msgKey);
                html += '</label></input></div>'
            }
            settingsDialogContentElem.set('html', html);
        }
        for (i = 0; i < checkBoxes.length; i++) {
            settingsDialogContentElem.getElementById(checkBoxes[i].id).set('checked',
                    !!checkBoxes[i].checked);
        }
        return settingsDialogContentElem;
    }
    
    function executeTinyMceCommand(commandName, tinyMceInstance) {
        tinyMceInstance.execCommand(commandName, false, null);
    }

    /**
     * Note text editor using TinyMCE.
     */
    NoteTextTinyMceEditor = new Class({
        Extends: NoteTextEditor,

        tinyMCEInstanceId: null,
        // if instanceId is not null but inCreation is true the editor is not fully usable yet
        tinyMCEInCreation: null,
        // whether remove was called while still in creation
        removeCalled: false,
        formElem: null,
        refreshCompleteCallback: null,
        storeEditorSize: true,
        keydownListenerAttached: false,
        fullscreenActivated: false,
        autoresizeActivated: false,
        userSuggestions: null,
        tagSuggestions: null,
        tagAutocompleterOptions: null,
        userAutocompleterOptions: null,

        /**
         * @override
         */
        initialize: function(formElem, options) {
            initializeTinyMce();
            if (options) {
                this.storeEditorSize = tinyMCE.settings.theme_advanced_resizing
                        && !!options.storeEditorSize;
                this.tagAutocompleterOptions = options.tagAutocompleterOptions;
                this.userAutocompleterOptions = options.userAutocompleterOptions;
            }
            this.parent(formElem, options);
            this.autoresizeMaxHeight = 300;
        },

        /**
         * @override
         */
        keyboardShortcutAdded: function() {
            this.attachKeyEventListener(tinymce.getInstanceById(this.tinyMCEInstanceId));
        },

        /**
         * @override
         */
        keyboardShortcutRemoved: function() {
            if (!this.kbdShortcuts || this.kbdShortcuts.length == 0) {
                this.detachKeyEventListener(tinymce.getInstanceById(this.tinyMCEInstanceId));
            }
        },

        attachKeyEventListener: function(tinyMCEInstance) {
            if (!this.keydownListenerAttached && tinyMCEInstance && !this.tinyMCEInCreation) {
                if (this.kbdShortcuts && this.kbdShortcuts.length > 0) {
                    tinyMCEInstance.onKeyDown.addToTop(this.onTinyMCEKeyDown, this);
                    this.keydownListenerAttached = true;
                }
            }
        },

        detachKeyEventListener: function(tinyMCEInstance) {
            if (this.keydownListenerAttached) {
                tinyMCEInstance.onKeyDown.remove(this.onTinyMCEKeyDown);
                this.keydownListenerAttached = false;
            }
        },

        attachFullscreenListener: function(tinyMCEInstance) {
            tinyMCEInstance.onExecCommand.add(function(ed, cmd, ui, val) {
                if (cmd === 'mceFullScreen') {
                    // in case fullscreen is closed, the calling editor instance will be the fullscreen editor
                    this.fullscreenToggled(ed.id !== 'mce_fullscreen');
                }
            }, this);
        },

        /**
         * Callback to be invoked when the fullscreen mode of the editor was activated or
         * deactivated. When fullscreen mode is activated a new tinymce editor is created.
         * 
         * @param {Boolean} activated True if the editor is now in fullscreen, false otherwise
         */
        fullscreenToggled: function(activated) {
            var tinyMCEInstance;
            if (activated) {
                this.fullscreenActivated = true;
                tinyMCEInstance = tinymce.getInstanceById('mce_fullscreen');
                this.attachFullscreenListener(tinyMCEInstance);
                // add ESC key to close fullscreen. Note: not adding callback via addKeyboardShortcut since
                // it would add a listener to the top (conflict with autocomplete) and we would have to
                // remove it when fullscreen is closed
                tinyMCEInstance.onKeyDown.add(function(editor, event) {
                    if (event.keyCode === 27) {
                        editor.execCommand('mceFullScreen');
                        return false;
                    }
                    return true;
                });
                // register keyDown handler for keyboard shortcuts in full screen editor instance, but only
                // if non-fullscreen editor has this listener attached because this is only the case if
                // there are shortcuts
                if (this.keydownListenerAttached) {
                    tinyMCEInstance.onKeyDown.addToTop(this.onTinyMCEKeyDown, this);
                }
            } else {
                this.fullscreenActivated = false;
            }
        },

        /**
         * Handler for the keydown event of the TinyMCE instance.
         * 
         * @param {tinymce.Editor} editor The editor instance that received the event
         * @param {Event} event The keyboard event
         */
        onTinyMCEKeyDown: function(editor, event) {
            return this.callKeyboardShortcut(event, editor);
        },

        /**
         * ondeactivate event handler for older IE which ensures that the caret is removed from the
         * iFrame when it looses the focus.
         */
        fixIeIFrameDeactivate: function() {
            var newActive = window.event && window.event.toElement;
            // manually set focus to window when the new active element is changed to a input button,
            // otherwise caret wouldn't be removed from the iFrame although all ways to determine
            // whether the iFrame has the focus (blur, focus event handler, hasFocus method on
            // document, activeElement) say that it does not have the focus
            if (newActive && newActive.tagName == 'INPUT' && newActive.type == 'button') {
                window.focus();
            }
        },

        focus: function() {
            var tinyMceEditor;
            if (!this.tinyMCEInstanceId) {
                return;
            }
            if (this.tinyMCEInCreation) {
                this.focusCalled = true;
                return;
            }
            tinyMceEditor = this.getTinyMCEInstance();
            tinyMceEditor.focus();
            // if the editor is empty explicitly move caret to start by selecting all and
            // collapsing to start because it is placed at end (after bogus BR) in firefox
            if (tinyMceEditor.getContent().length == 0) {
                tinyMceEditor.selection.select(tinyMceEditor.getBody(), true);
                tinyMceEditor.selection.collapse(true);
                // need to focus again otherwise IE9 is unfocused
                tinyMceEditor.focus();
            }
        },

        /**
         * @return {tinymce.Editor} The current editor instance, which will be the fullscreen editor
         *         if in fullscreen or the normal editor otherwise
         */
        getTinyMCEInstance: function() {
            return tinymce.getInstanceById(this.fullscreenActivated ? 'mce_fullscreen'
                    : this.tinyMCEInstanceId);
        },

        /**
         * @override
         */
        supportsHtml: function() {
            return true;
        },

        /**
         * @override
         */
        getUserAutocompleter: function() {
            return this.userSuggestions;
        },

        /**
         * @override
         */
        getTagAutocompleter: function() {
            return this.tagSuggestions;
        },

        /**
         * @override
         */
        refresh: function(formElem, refreshCompleteCallback) {
            var tinyMCEInstance;
            this.formElem = formElem;
            this.refreshCompleteCallback = refreshCompleteCallback;
            if (!this.tinyMCEInstanceId) {
                this.tinyMCEInstanceId = formElem.getElement('textarea').id;
            }
            tinyMCEInstance = this.showOrCreateTinyMCE();
            // force non-dirty tinyMCE
            tinyMCEInstance.isNotDirty = true;
            // IE 11 focuses automatically, so explicitly unfocus
            if (Browser.name === 'ie' && Browser.version > 10) {
                this.unFocus();
            }
        },
        
        refreshComplete: function() {
            if (this.refreshCompleteCallback) {
                this.refreshCompleteCallback(this);
                this.refreshCompleteCallback = null;
            }
        },

        /**
         * @override
         */
        cleanup: function() {
            this.parent();
            this.removeTinyMCE();
            this.formElem = null;
        },
        
        /**
         * @override
         */
        getInputElement: function() {
            var tinyMCEInstance = this.getTinyMCEInstance();
            if (tinyMCEInstance) {
                if (!this.tinyMCEInCreation) {
                    // body is the content editable and therefore the input element
                    return tinyMCEInstance.getBody();
                }
            }
            return null;
        },

        /**
         * @override
         */
        getContent: function() {
            var editorElem;
            var content = '';
            var tinyMCEInstance = this.getTinyMCEInstance();
            if (tinyMCEInstance) {
                if (this.tinyMCEInCreation) {
                    editorElem = tinyMCEInstance.getElement();
                    if (editorElem) {
                        content = editorElem.value;
                    }
                } else {
                    content = tinyMCEInstance.save();
                    // force none dirty
                    tinyMCEInstance.isNotDirty = true;
                }
            }
            return content;
        },
        /**
         * @override
         */
        setContent: function(newContent) {
            this.internalSetContent(newContent, false);
        },

        setContentFromPlaintext: function(newContent) {
            this.setContent(this.convertPlaintextToHtml(newContent));
        },

        resetContent: function(newContent) {
            this.internalSetContent(newContent, true);
        },

        resetContentFromPlaintext: function(newContent) {
            this.resetContent(this.convertPlaintextToHtml(newContent));
        },

        isDirty: function() {
            var tinyMCEInstance = this.getTinyMCEInstance();
            if (!tinyMCEInstance || this.tinyMCEInCreation) {
                return false;
            }
            return tinyMCEInstance.isDirty();
        },

        convertPlaintextToHtml: function(plaintext) {
            var html;
            if (!plaintext) {
                return plaintext;
            }
            html = escapeXML(plaintext);
            // convert line breaks the way tinyMCE does it while hitting enter:
            // 1. normalize
            html = html.replace(/\r\n/g, '\n');
            // 2. convert every line of text terminated by \n into P tag
            html = html.replace(/([^\n]+)\n/g, '<p>$1</p>');
            // 3. substitute remaining \n into P tag with embedded BR (empty P tags are not rendered)
            html = html.replace(/\n/g, '<p><br/></p>');
            return html;
        },
        /**
         * Set the content of the editor
         * 
         * @param {String} [newContent] the new content to set, if undefined the content will be
         *            cleared
         * @param {boolean} resetDirtyFlag If true, the changed content will not lead to a dirty
         *            editor, that is isDirty will return false.
         */
        internalSetContent: function(newContent, resetDirtyFlag) {
            var tinyMCEInstance;
            if (!newContent) {
                newContent = '';
            }
            if (this.formElem) {
                this.formElem.getElement('textarea').value = newContent;
            }
            // while inCreation only the content of the textarea is set because editor might not be ready yet
            if (this.tinyMCEInstanceId && !this.tinyMCEInCreation) {
                tinyMCEInstance = this.getTinyMCEInstance();
                if (tinymce.isWebKit || tinymce.isGecko) {
                    // webkit or gecko cannot handle a whitespace at the end of a block -> add a BR after it
                    if (newContent.length && newContent.lastIndexOf(' ') === newContent.length - 1) {
                        newContent = newContent + '<br />';
                    }
                } else {
                    // IE9 renders 2 lines in edit mode when P tag with embedded BR is found. 
                    // When removing BR TinyMCE adds one automatically, but not when a comment is contained.
                    if (tinymce.isIE9) {
                        newContent = newContent.replace(/<p><br\/><\/p>/g, '<p><!-- --></p>');
                    }
                }
                if (!tinyMCEInstance.setContent(newContent)) {
                    // does return nothing if the content is cleared. In that case it does not
                    // fire the onSetContent event which is bad for plugins like autoresize -> fire manually
                    tinyMCEInstance.onSetContent.dispatch(tinyMCEInstance, {
                        set: true,
                        content: '',
                        format: 'html'
                    });
                }
                if (resetDirtyFlag) {
                    tinyMCEInstance.isNotDirty = true;
                }
                tinyMCEInstance.undoManager.clear();
            }
        },

        createUserSuggestionDefiniton: function() {
            var def, suggestions, acOptions;
            acOptions = autocompleterFactory.prepareMentionAutocompleterOptions(
                    this.userAutocompleterOptions, null, true);
            // insert alias on selection
            acOptions.suggestionsOptions.inputValueAttribute = 'alias';
            acOptions.suggestionsOptions.width = 'auto';
            acOptions.suggestionsOptions.maxWidth = false;
            suggestions = new AutocompleterSuggestions(acOptions.suggestionsOptions, acOptions.dataSources);
            def = {
                triggerChar: '@',
                suggestions: suggestions,
                disabled: this.getPreferenceAsBoolean('userSuggestionDisabled')
            };
            this.userSuggestions = suggestions;
            return def;
        },
        createTagSuggestionDefiniton: function() {
            var def, suggestions, acOptions;
            acOptions = autocompleterFactory.prepareTagAutocompleterOptions(
                    this.tagAutocompleterOptions, null, 'NOTE', false, true);
            acOptions.suggestionsOptions.width = 'auto';
            acOptions.suggestionsOptions.maxWidth = false;
            suggestions = new AutocompleterSuggestions(acOptions.suggestionsOptions, acOptions.dataSources);
            def = {
                triggerChar: '#',
                suggestions: suggestions,
                wrapWhitespace: '"',
                disabled: this.getPreferenceAsBoolean('tagSuggestionDisabled')
            };
            this.tagSuggestions = suggestions;
            return def;
        },

        buildSuggestionToggleSettingDefintion: function(triggerChar, disabled) {
            var def = {
                type: 'toggle',
                label: getJSMessage('javascript.note.editor.setting.suggestions.disable',
                        [ triggerChar ]),
                labelToggledOff: getJSMessage('javascript.note.editor.setting.suggestions.enable',
                        [ triggerChar ]),
                disableInFullscreen: true,
                toggledOff: disabled
            };
            def.clickCallback = function(mceInstance, menuItem, element, toggledOff) {
                mceInstance.execCommand('mceSuggestionToggle', null, {
                    action: toggledOff ? 'disable' : 'enable',
                    triggerChar: triggerChar
                });
            };
            return def;
        },

        buildChangeSettingDefinitions: function() {
            var defs = [];
            // nothing to configure in IE7 and IE8 since suggestions are not supported 
            if (Browser.name !== 'ie' || Browser.version > 8) {
                defs.push(this.buildSuggestionToggleSettingDefintion('@', this
                        .getPreferenceAsBoolean('userSuggestionDisabled')));
                defs.push(this.buildSuggestionToggleSettingDefintion('#', this
                        .getPreferenceAsBoolean('tagSuggestionDisabled')));
                defs.push({
                    type: 'action',
                    label: getJSMessage('javascript.note.editor.setting.advanced'),
                    disableInFullscreen: true,
                    clickCallback: this.showAdvancedEditorSettings.bind(this)
                });
            }
            return defs;
        },
        
        enableTabKeyIndentation: function() {
            // hidden config option to disable tab-key indent/outdent
            if (this.getPreferenceAsBoolean('tabKeyIndentationDisabled')) {
                return;
            }
            this.addKeyboardShortcut({
                callback: executeTinyMceCommand.bind(null, 'Indent'),
                cancelEvent: true,
                keyCode: 9,
                shiftKey: false
            });
            this.addKeyboardShortcut({
                callback: executeTinyMceCommand.bind(null, 'Outdent'),
                cancelEvent: true,
                keyCode: 9,
                shiftKey: true
            });
        },

        saveAdvancedSettings: function(settingsDialogElem) {
            var checkBoxElem;
            checkBoxElem = settingsDialogContentElem
                    .getElementById('globalEditorSettingSuggestUsers');
            this.setPreference('userSuggestionDisabled', !checkBoxElem.get('checked'));
            checkBoxElem = settingsDialogContentElem
                    .getElementById('globalEditorSettingSuggestTags');
            this.setPreference('tagSuggestionDisabled', !checkBoxElem.get('checked'));
            checkBoxElem = settingsDialogContentElem
                    .getElementById('globalEditorSettingAutoresize');
            this.setPreference('autoresizeDisabled', !checkBoxElem.get('checked'));
        },

        showAdvancedEditorSettings: function() {
            var elem, checkBoxes = [];
            checkBoxes.push({
                id: 'globalEditorSettingSuggestUsers',
                msgKey: 'javascript.dialog.note.editor.preferences.suggest.users',
                checked: !this.getPreferenceAsBoolean('userSuggestionDisabled')
            });
            checkBoxes.push({
                id: 'globalEditorSettingSuggestTags',
                msgKey: 'javascript.dialog.note.editor.preferences.suggest.tags',
                checked: !this.getPreferenceAsBoolean('tagSuggestionDisabled')
            });
            checkBoxes.push({
                id: 'globalEditorSettingAutoresize',
                msgKey: 'javascript.dialog.note.editor.preferences.autoresize',
                checked: !this.getPreferenceAsBoolean('autoresizeDisabled')
            });
            elem = getSettingsDialogContent(checkBoxes);
            showDialog(getJSMessage('javascript.dialog.note.editor.preferences.title'), elem, [ {
                type: 'ok',
                action: this.saveAdvancedSettings.bind(this, elem)
            }, {
                type: 'cancel'
            } ], {
                cssClasses: 'cn-note-editor-advanced-settings'
            });
        },

        showOrCreateTinyMCE: function() {
            var settings, autoresize;
            var tinyMCEInstance = tinymce.getInstanceById(this.tinyMCEInstanceId);
            if (!tinyMCEInstance) {
                // get global settings, cannot reuse tinymce.settings since tiny changes it with
                // every created editor instance -> save original settings and reuse those
                if (!orgTinyMCESettins) {
                    orgTinyMCESettins = Object.clone(tinyMCE.settings);
                    settings = tinyMCE.settings;
                } else {
                    settings = Object.clone(orgTinyMCESettins);
                }
                settings.theme_advanced_resizing_use_cookie = this.storeEditorSize;
                // autoresize does not work in ie7 and ie8 (shows 2 vertical scrollbars)
                if ((Browser.name !== 'ie' || Browser.version > 8)
                        && !this.getPreferenceAsBoolean('autoresizeDisabled')) {
                    settings.plugins = settings.plugins + ',autoresize';
                    settings.theme_advanced_resizing = false;
                    // 25 px padding is little more than the height of a line which reduces flicker when resizing while caret is on last line 
                    settings.autoresize_bottom_margin = 25;
                    settings.autoresize_max_height = this.autoresizeMaxHeight;
                    settings.autoresize_min_height = this.autoresizeMinHeight;
                    autoresize = true;
                }
                settings.changesettings_options.settingDefinitions = this
                        .buildChangeSettingDefinitions();
                settings.suggestions_options.suggestionDefinitions = [];
                settings.suggestions_options.suggestionDefinitions.push(this
                        .createUserSuggestionDefiniton());
                settings.suggestions_options.suggestionDefinitions.push(this
                        .createTagSuggestionDefiniton());
                tinyMCEInstance = new tinymce.Editor(this.tinyMCEInstanceId, settings);
                this.tinyMCEInCreation = true;
                // attach callback which shows the editor when rendering and init is done
                tinyMCEInstance.onInit.add(this.onTinyMCEInit, this);
                this.enableTabKeyIndentation();
                tinyMCEInstance.render();
                if (autoresize) {
                    // add special marker class for custom styling when autoresize is enabled
                    document.id(tinyMCEInstance.getContainer()).addClass('mceAutoresizeEnabled');
                    this.autoresizeActivated = true;
                }
            } else {
                this.showTinyMCE(tinyMCEInstance);
                this.refreshComplete();
            }
            return tinyMCEInstance;
        },

        showTinyMCE: function(tinyMCEInstance) {
            if (!this.tinyMCEInCreation) {
                if (tinyMCEInstance.isHidden()) {
                    tinyMCEInstance.show();
                }
                this.tinyMCEShown = true;
            }
        },

        onTinyMCEInit: function(tinyMCEInstance) {
            var iframe;
            if (this.tinyMCEInCreation) {
                this.tinyMCEInCreation = false;
                if (this.removeCalled) {
                    this.removeTinyMCE();
                } else {
                    // remove the init event listener, but detached from the current call
                    // because this is the callback of the init event and we would modify
                    // the listeners collection the tinymce is iterating through
                    setTimeout(function() {
                        tinyMCEInstance.onInit.remove(this.onTinyMCEInit);
                    }.bind(this), 0);
                    this.attachKeyEventListener(tinyMCEInstance);
                    this.attachFullscreenListener(tinyMCEInstance);
                    if (Browser.name === 'ie' && Browser.version < 8) {
                        iframe = document.id(tinyMCEInstance.getContentAreaContainer()).getElement(
                                'iframe');
                        iframe.ondeactivate = this.fixIeIFrameDeactivate;
                    }
                    this.showTinyMCE(tinyMCEInstance);
                    if (this.focusCalled) {
                        this.focus();
                        this.focusCalled = false;
                    }
                    this.refreshComplete();
                }
            }
            return true;
        },

        removeTinyMCE: function() {
            var tinyMCEInstance, textareaElem, parentElem, oldHeight, oldVisibility, oldDisplay;
            if (!this.tinyMCEInstanceId) {
                return;
            }
            if (this.tinyMCEInCreation) {
                this.removeCalled = true;
                return;
            }
            this.refreshCompleteCallback = null;
            tinyMCEInstance = tinyMCE.get(this.tinyMCEInstanceId);
            if (tinyMCEInstance) {
                // TODO maybe it is enough to just unfocus the editor?
                if (Browser.name === 'ie') {
                    // in IE tinyMCE (probably its iframe) must not have display 'none' style
                    // when removing it, otherwise the caret gets stuck blinking somewhere in the DOM
                    // and selection with mouse isn't working anymore. To solve this we check whether
                    // any of the parent elements of the tinyMCE has display 'none' style and if so
                    // we remove this style temporarily.
                    textareaElem = tinyMCEInstance.getElement();
                    textareaElem = document.id(textareaElem);
                    parentElem = textareaElem.getParent();
                    while (parentElem && parentElem.getStyle('display') != 'none') {
                        parentElem = parentElem.getParent();
                    }
                    if (parentElem) {
                        // found the not-displayed element: show it but avoid flashing of FE by
                        // hiding element and setting 0 pix height
                        // To restore the styles, remember the actual style values of the element and
                        // not the calculated ones getStyle would return
                        oldHeight = parentElem.style.height;
                        oldDisplay = parentElem.style.display;
                        oldVisibility = parentElem.style.visibility;
                        parentElem.setStyle('height', '0px');
                        parentElem.setStyle('visibility', 'hidden');
                        parentElem.setStyle('display', 'block');
                    }
                }
                this.detachKeyEventListener(tinyMCEInstance);
                tinyMCEInstance.remove();
                if (parentElem) {
                    parentElem.setStyle('display', oldDisplay);
                    parentElem.setStyle('visibility', oldVisibility);
                    parentElem.setStyle('height', oldHeight);
                }
                this.tinyMCEShown = false;
            }
            this.removeCalled = false;
        },
        /**
         * @override
         */
        unFocus: function() {
            var tinyMCEInstance, wrapper, iFrame;
            if (!this.tinyMCEInstanceId) {
                return;
            }
            if (this.tinyMCEInCreation) {
                this.focusCalled = false;
                return;
            }
            tinyMCEInstance = tinyMCE.get(this.tinyMCEInstanceId);
            if (tinyMCEInstance) {
                wrapper = tinyMCEInstance.getContentAreaContainer();
                iFrame = wrapper && document.id(wrapper).getElement('iframe');
                if (iFrame && document.activeElement == iFrame) {
                    if (Browser.name === 'firefox' || Browser.platform === 'ios') {
                        iFrame.blur();
                    } else {
                        window.focus();
                        if (Browser.name === 'ie' && Browser.version < 8) {
                            document.documentElement.focus();
                        }
                    }
                }
            }
        }

    });
})();
/**
 * Note text editor that uses a simple textarea and thus only supports creation and editing of plain
 * text notes. This editor can be configured to grow automatically with its content.
 */
NoteTextTextareaEditor = new Class({
    Extends: NoteTextEditor,

    textarea: null,
    useExpander: false,
    focusOnRefresh: false,
    expanderOptions: null,
    expander: null,
    previousContent: '',
    boundKeydownHandler: null,

    initialize: function(formElem, options) {
        if (options) {
            this.useExpander = !!options.useExpander;
            this.expanderOptions = options.expanderOptions;
            this.focusOnRefresh = !!options.focusOnRefresh;
        }
        this.parent(formElem, options);
        if (this.expanderOptions) {
            this.expanderOptions.minHeight = this.autoresizeMinHeight;
        }
    },

    /**
     * @override
     */
    keyboardShortcutAdded: function() {
        this.attachKeyEventListener();
    },

    /**
     * @override
     */
    keyboardShortcutRemoved: function() {
        if (!this.kbdShortcuts || this.kbdShortcuts.length == 0) {
            this.detachKeyEventListener();
        }
    },

    attachKeyEventListener: function() {
        var elem, handler;
        if (!this.boundKeydownHandler && this.kbdShortcuts && this.kbdShortcuts.length) {
            elem = this.textarea;
            // use DOM events and not mootools wrappers for performance reasons
            // Note: using keydown instead of keypress because the shortcuts only contain a keycode.
            // If we add charCode support we would have to add an additional keypress listener. 
            if (elem) {
                handler = this.callKeyboardShortcut.bind(this);
                if (elem.addEventListener) {
                    elem.addEventListener('keydown', handler, false);
                    this.boundKeydownHandler = handler;
                } else if (elem.attachEvent) {
                    // old IEs
                    elem.attachEvent('onkeydown', handler);
                    this.boundKeydownHandler = handler;
                }
            }
        }
    },

    detachKeyEventListener: function() {
        var elem;
        if (this.boundKeydownHandler) {
            elem = this.textarea;
            // use DOM events and not mootools wrappers for performance reasons
            if (elem) {
                if (elem.removeEventListener) {
                    elem.removeEventListener('keydown', this.boundKeydownHandler, false);
                } else if (elem.detachEvent) {
                    elem.detachEvent('onkeydown', this.boundKeydownHandler);
                }
            }
            this.boundKeydownHandler = null;
        }
    },

    /**
     * @override
     */
    refresh: function(formElem, refreshCompleteCallback) {
        this.textarea = formElem.getElement('textarea');
        // just in case in case the textarea is hidden
        if (this.textarea.getStyle('visibility') == 'hidden') {
            this.textarea.setStyle('visibility', '');
        }
        // add key press listener if shortcuts were added while not yet refreshed
        this.attachKeyEventListener();
        if (this.useExpander) {
            this.expander = new ExpandingTextarea(this.textarea, this.expanderOptions);
            this.expander.startAutoExpand();
        }
        if (this.focusOnRefresh) {
            this.textarea.focus();
        }
        if (refreshCompleteCallback) {
            refreshCompleteCallback(this);
        }
    },

    /**
     * @override
     */
    cleanup: function() {
        this.parent();
        this.detachKeyEventListener();
        if (this.expander) {
            this.expander.stopAutoExpand();
            this.expander = null;
        }
    },
    /**
     * @override
     */
    focus: function() {
        if (this.textarea) {
            this.textarea.focus();
        }
    },
    /**
     * @override
     */
    getInputElement: function() {
        return this.textarea;
    },
    
    /**
     * @override
     */
    getContent: function() {
        return this.textarea.value;
    },
    /**
     * @override
     */
    setContent: function(newContent) {
        this.internalSetContent(newContent, false);
    },

    /**
     * @override
     */
    resetContent: function(content) {
        this.internalSetContent(content, true);
    },
    /**
     * @override
     */
    isDirty: function() {
        var curContent = this.getContent();
        var dirty = curContent != this.previousContent;
        this.previousContent = curContent;
        return dirty;
    },
    /**
     * Set the content of the editor
     * 
     * @param {String} [newContent] the new content to set, if undefined the content will be cleared
     * @param {boolean} resetDirtyFlag If true, the changed content will not lead to a dirty editor,
     *            that is isDirty will return false.
     */
    internalSetContent: function(newContent, resetDirtyFlag) {
        if (!newContent) {
            newContent = '';
        }
        this.textarea.value = newContent;
        if (this.expander) {
            this.expander.checkExpand();
        }
        if (resetDirtyFlag) {
            this.previousContent = newContent;
        }
    },
    /**
     * @override
     */
    unFocus: function() {
        if (document.activeElement == this.textarea) {
            this.textarea.blur();
        }
    }
});
/**
 * Static factory to create note editor instances
 */
NoteTextEditorFactory = {
    // lazily initialized flag that states whether the browser supports rich text editors
    richTextSupport: null,
    // RegEx of user agents that should use a plain text editor although they support
    // contentEditable
    iDeviceUaRegEx: /ipad|iphone/i,
    androidUaRegEx: /android/i,
    defaultRichTextEditorClass: 'NoteTextTinyMceEditor',
    defaultPlainTextEditorClass: 'NoteTextTextareaEditor',

    /**
     * @returns {boolean} true if the browser supports contentEditable and the UA is not to be
     *          excluded
     */
    richTextSupported: function() {
        var supported, ua;
        if (this.richTextSupport == null) {
            supported = 'contentEditable' in document.body;
            if (supported) {
                ua = navigator.userAgent;
                if (this.androidUaRegEx.test(ua)) {
                    supported = false;
                } else if (this.iDeviceUaRegEx.test(ua)) {
                    // iOS-5 and newer claim to support contentEditable but it is awfully buggy. 
                    // The most annoying issue is that the soft keyboard is not closed when tapping 
                    // somewhere in DOM and the caret appears at this position. Moreover the focus
                    // is sometimes partially lost when scrolling (the caret is still in iframe
                    // but typing will not enter any characters).
                    // Thus, disabled for now.
                    supported = false;
                }
            }
            this.richTextSupport = supported;
        }
        return this.richTextSupport;
    },

    /**
     * Creates a rich text editor if the browser supports it. Otherwise a plain text editor is
     * created.
     * 
     * @param {Element} formElem The element to pass to the constructor of the editor
     * @param {Object} [options] Additional options to pass to the constructor of the editor
     * @returns {NoteTextEditor} an editor instance
     */
    createSupportedEditor: function(formElem, options) {
        var editorClass = this.getSupportedEditorType();
        return new editorClass(formElem, options);
    },

    /**
     * Return the class of rich text editor if the browser supports it. Otherwise the class of a
     * plain text editor is returned.
     * 
     * @return {Class} the class of the supported editor
     */
    getSupportedEditorType: function() {
        if (this.richTextSupported()) {
            return window[this.defaultRichTextEditorClass];
        }
        return window[this.defaultPlainTextEditorClass];
    },

    /**
     * Creates a plain text editor.
     * 
     * @param {Element} formElem The element to pass to the constructor of the editor
     * @param {Object} [options] Additional options to pass to the constructor of the editor
     * @returns {NoteTextEditor} an editor instance
     */
    createPlainTextEditor: function(formElem, options) {
        return new window[this.defaultPlainTextEditorClass](formElem, options);
    }
};