/**
 * Autocompleter with support for suggestions that are grouped into categories.
 * 
 * This autocompleter is a rewrite/extension to the autocompleter created by Harald Kirschner
 * (http://digitarald.de/project/autocompleter/)
 * 
 * @author Communote GmbH
 * @copyright Author
 */

var Autocompleter = new Class({

    Implements: [ Options, Events ],

    options: {
        // if true the autocompleter will be activated automatically after initialization
        // otherwise the activate method must be called explicitly
        activateOnInit: true,

        // if true the input element will be updated when selecting a choice, otherwise not
        updateInputOnSelection: true,
        // if true the input element will be cleared when selecting a choice, otherwise not.
        // This option has higher priority than updateInputOnSelection
        clearInputOnSelection: false,
        // if true the focus will be removed from the input element when selecting a choice, otherwise not
        // If this setting is not overwritten by init options it will be false in multiple mode.
        unfocusInputOnSelection: true,
        // if true the input element will be updated when focusing a choice, 
        // otherwise not. The part of the value that changed will be select.
        updateInputOnFocus: false
    },
    // denotes whether the autocompleter is active (handles keyboard events)
    activated: false,
    // whether the browser can prevent loosing the input focus when clicking somewhere in the suggestions
    blurEventBlockable: false,
    suggestionsShown: false,
    focusedInputValue: null,
    /**
     * @type AutocompleterInputField
     */
    inputField: null,
    /**
     * @type Suggestions
     */
    suggestions: null,

    initialize: function(element, options) {
        // little optimization to speed-up copying of options and reduce the memory
        var suggestionsOptions = options.suggestionsOptions;
        var inputFieldOptions = options.inputFieldOptions;
        var dataSources = options.dataSources;
        delete options.suggestionsOptions;
        delete options.inputFieldOptions;
        delete options.dataSources;
        this.setOptions(options);
        // restore the provided options object
        options.suggestionsOptions = suggestionsOptions;
        options.inputFieldOptions = inputFieldOptions;
        options.dataSources = dataSources;
        this.inputField = new AutocompleterInputField(element, inputFieldOptions);
        this.suggestions = new AutocompleterSuggestions(suggestionsOptions, dataSources);
        if (options.unfocusInputOnSelection == undefined) {
            // TODO a bit hacky
            this.options.unfocusInputOnSelection = !this.inputField.options.multiple;
        }
        // attach callbacks
        this.inputField.setOnClickCallback(this.onInputFieldClick.bind(this));
        this.inputField.setOnKeydownCallback(this.onInputFieldKeydown.bind(this));
        this.inputField.setOnValueChangedCallback(this.prefetch.bind(this));
        this.inputField.setOnFocusChangedCallback(this.onInputFieldFocusChanged.bind(this));
        this.suggestions.setOnChoiceFocusedCallback(this.onChoiceFocused.bind(this));
        this.suggestions.setOnChoiceSelectedCallback(this.onChoiceSelected.bind(this));
        this.suggestions.setOnHideCallback(this.onChoicesHidden.bind(this));
        this.suggestions.setOnShowCallback(this.onChoicesShown.bind(this));
        this.suggestions
                .setDeterminePositionCallback(this.inputField.determinePositionOfSuggestions
                        .bind(this.inputField));
        if (this.options.activateOnInit) {
            this.activate();
        }
        this.blurEventBlockable = !(Browser.name === 'ie' && Browser.version <= 9);
    },

    /**
     * Activate the autocompleter. When the autocompleter is activated it will search for
     * suggestions.
     */
    activate: function() {
        if (!this.activated) {
            this.suggestions.resetQuery();
            this.inputField.activate();
            this.activated = true;
        }
    },
    /**
     * Deactivate the autocompleter. When the autocompleter is deactivated it will not search for
     * suggestions.
     */
    deactivate: function() {
        var i, eventDef;
        if (this.activated) {
            this.inputField.deactivate();
            this.activated = false;
            this.suggestions.hideChoices();
        }
    },
    /**
     * Deactivates the autocompleter and destroys the choices.
     */
    destroy: function() {
        this.deactivate();
        this.suggestions.destroy();
    },

    /**
     * Close the suggestions if shown.
     */
    close: function() {
        this.suggestions.hideChoices();
    },
    /**
     * Resets the internal query cache to issue another query with the current input.
     * 
     * @param {Boolean} clearInput If true the input field will be cleared
     */
    resetQuery: function(clearInput) {
        if (clearInput) {
            this.inputField.clearInput();
        }
        this.suggestions.resetQuery();
    },

    /**
     * @returns {AutocompleterDataSource} the data source instance used by the suggestions of the
     *          autocompleter
     */
    getElement: function() {
        return this.inputField.getElement();
    },

    /**
     * @returns {Boolean} True if the input element, the autocompleter is attached to, is focused
     */
    isInputElementFocused: function() {
        return this.inputField.isFocused();
    },

    onInputFieldKeydown: function(e) {
        if (e.key && !e.shift) {
            switch (e.key) {
            case 'enter':
                if (this.suggestionsShown && this.suggestions.selectFocusedChoice()) {
                    return false;
                } else {
                    // notify others that enter was pressed but not handled
                    this.fireEvent('enterPressed');
                }
                break;
            case 'up':
            case 'down':
                if (this.suggestionsShown) {
                    up = (e.key == 'up');
                    this.suggestions.focusNextChoice(up, true);
                } else {
                    this.prefetch();
                }
                return false;
                /*
                 * case 'tab': this.hideChoices(); break;
                 */
            case 'esc':
                this.suggestions.hideChoices();
                // return false to avoid event propagation, especially useful when 
                // used in a popup that is closed with ESC
                return false;
            }
        }
        return true;
    },

    onInputFieldClick: function() {
        this.prefetch();
        // TODO are there situations where the event should be canceled?
        return true;
    },
    onInputFieldFocusChanged: function(focused) {
        if (!focused) {
            this.suggestions.hideChoices();
            this.fireEvent('onBlur', [ this.inputField.getElement() ]);
        } else {
            this.fireEvent('onFocus', [ this.inputField.getElement() ]);
        }
    },

    onChoiceFocused: function(focusedElem, focusedItem) {
        if (this.options.updateInputOnFocus) {
            this.updateInputValue(focusedItem.inputValue, true, false);
            this.focusedInputValue = focusedItem.inputValue;
        }
        this.fireEvent('onChoiceFocused', [ this.inputField.getElement(), focusedElem,
                focusedItem.token ]);
    },

    onChoiceSelected: function(focusedElem, token, choiceValue) {
        if (this.options.clearInputOnSelection) {
            this.inputField.updateInputValue('', false, true);
        } else if (this.options.updateInputOnSelection) {
            this.inputField.updateInputValue(choiceValue, false, true);
        }
        if (this.options.unfocusInputOnSelection) {
            this.inputField.getElement().blur();
        } else if(!this.blurEventBlockable) {
            // refocus
            this.inputField.getElement().focus();
        }
        this.fireEvent('onChoiceSelected', [ this.inputField.getElement(), focusedElem, token,
                choiceValue ]);
    },

    onChoicesHidden: function(choicesContainer) {
        this.inputField.ignoreCurrentValueChange();
        this.suggestionsShown = false;
        this.fireEvent('onHide', [ this.inputField.getElement(), choicesContainer ]);
    },
    onChoicesShown: function(choicesContainer) {
        this.suggestionsShown = true;
        this.fireEvent('onShow', [ this.inputField.getElement(), choicesContainer ]);
    },

    // TODO better name!
    prefetch: function(query) {
        if (!query) {
            query = this.inputField.getQueryValue();
        }
        if (this.focusedInputValue && this.suggestionsShown) {
            // if visible and the updateInputOnFocus option is set the inputValue of the focused
            // choice will be inserted in the input field but this should not be considered a search
            if (query.trim() == this.focusedInputValue) {
                return;
            } else {
                this.focusedInputValue = null;
            }
        }
        this.suggestions.queryValueChanged(query);
    }
});
