(function(namespace) {
    /**
     * SearchBox widget that supports several search modes which can trigger different events. For
     * each defined search mode an autocompleter can be attached and a placeholder text can be
     * defined. Configuration is done via static parameters. The following parameters exist:
     * 
     * {boolean} [clearInputOnSubmit] Whether the input should be cleared when submitting the
     * search, default is true. If there is no submitEvent for the current mode the input won't be
     * cleared.
     * 
     * {String} searchModes Comma separated listing of search mode names (should not contain
     * whitespaces), this parameter is copied to the request when refreshing the widget. Default FE
     * renders tabs for the search modes in the order of the listing. The tabs will have titles
     * generated from message keys looking like 'blog.filter.searchbox.menu.<modeName>' When
     * defining another FE the mode switching elements need to have a CSS class
     * 'control-search-mode' and one named 'control-search-mode-<modeName>'. The rel attribute of
     * the element can contain the placeholder text for the mode.
     * 
     * {String} [renderSearchModeSwitches] Parameter that is just copied to the request parameters
     * when refreshing the widget. It is intended to control the server side rendering, that is to
     * define whether the search mode switches should be rendered. But this is up to the
     * implementation of the template.
     * 
     * {Object} searchModeOptions JSON object with options for the searchModes. The object is
     * expected to contain a member per search mode which itself is an object with the options for
     * that search mode. The following options are supported:
     * 
     * {String} [searchModeOption.placeholder] The placeholder to set for the mode. If provided the
     * rel attribute (see above) will be ignored.
     * 
     * {String} [searchModeOption.submitEvent] The E2G event to fire when submitting the search for
     * that mode
     * 
     * {String} [searchModeOption.acUrl] URL for constructing the autocompleter for this mode. If
     * not defined the acFactoryFunction will be evaluated. If that option isn't set too, no
     * autocompleter will be created for this mode. The URL is automatically expanded with
     * buildRequestUrl().
     * 
     * {String} [searchModeOption.acPostVar] Name of the post variable that will hold the search
     * input when the autocompleter starts a request. This option is required if acUrl is defined
     * and is ignored if using the acFactoryFunction.
     * 
     * {String} [searchModeOption.acFactoryFunction] Name of a function of the autocompleter factory
     * instance (see option acFactoryInstance) that should be used to create the autocompleter
     * instance.
     * 
     * {Array} [searchModeOption.acFactoryFunctionArgs] Array with additional arguments that should
     * be passed to the factory method. The arguments are added starting at argument position 4.
     * 
     * {String} [searchModeOption.acInject] Name of a method of this class that should be called
     * when the autocompleter wants to inject a choice into the suggestions. If not defined the
     * inject function built in into the autocompleter will be used. This class contains the
     * injectToken method that can be used. For details see the documentation of that method. A
     * custom method has to have the signature as defined by the injectChoiceContentCallback hook
     * (c.f. Autocompleter class)
     * 
     * {Object} [searchModeOption.acPostData] Object with post data to be included when the
     * autocompleter does a request
     * 
     * {String[]} [searchModeOption.acParameterSubset] Array with names of the filter parameters of
     * the filter parameter store of the group to be included in an autocompleter request. If
     * omitted the current parameters will be added completely. If provided but empty none of the
     * current parameters will be added.
     * 
     * {Object} [searchModeOption.acSelectEvent] The E2G event to fire when a suggestion is
     * selected. If defined and clearInputOnSubmit is true the input will be cleared too
     * 
     * {Object} [searchModeOption.acOptions] Object with options to be passed to the autocompleter
     * constructor
     * 
     * {String} [acFactoryInstance] Name of the global variable holding the autocompleterFactory
     * instance. The variable defaults to autocompleterFactory.
     * 
     * {String} [activeSearchMode] Name of a search mode to be used as default search mode. If
     * undefined the first item in searchModes will be the default.
     * 
     * {String} [noSubmitEventCssClass] Name of the CSS class that should be set when a search mode
     * does not define a submit event. The CSS class will be added to the first child element of the
     * widget or the element found by searching for with the selector defined in parameter
     * noSubmitEventCssElementSelector. If not given, no CSS class will be applied.
     * 
     * {String} [noSubmitEventCssElementSelector] Mootools selector to find the element which should
     * get the noSubmitEventCssClass CSS class when switching to a search mode without submitEvent.
     * 
     */
    var SearchBoxWidget = new Class({
        Extends: C_FilterWidget,

        widgetGroup: 'blog',

        modeSwitchElementCssClass: 'control-search-mode',

        noSubmitEventCssClass: null,
        noSubmitEventCssElementSelector: null,

        modeOptions: null,
        currentMode: null,
        defaultMode: null,

        clearInputOnSubmit: true,

        // mapping of search mode names to the defined autocompleter
        autocompleters: undefined,
        // reference to the factory instance to use when creating autocompleter instances
        autocompleterFactory: undefined,

        placeholders: null,

        searchInput: null,

        observedFilterParams: [ 'targetBlogId', 'blogId', 'tagPrefix', 'userId', 'showPostsForMe',
                'postTextSearchString', 'searchString', 'startDate', 'endDate', 'filter', 'tagIds',
                'discussionId', 'noteId', 'propertyFilter' ],

        init: function() {
            var i, modeOpts, modeOptsParam, activeMode, clearInput, modes, factory;
            this.parent();

            activeMode = this.getStaticParameter('activeSearchMode');
            this.copyStaticParameter('contentWrapperCssClass');
            this.copyStaticParameter('searchModes');
            this.copyStaticParameter('renderSearchModeSwitches');
            modes = this.getStaticParameter('searchModes');
            if (modes) {
                // split and trim
                modes = modes.split(',');
            }

            // get the active mode
            if (!activeMode) {
                // first mode found
                activeMode = modes[0];
            }
            modeOpts = {};
            // check for options which is expected to be an object or a serialized JSON object
            modeOptsParam = this.getStaticParameter('searchModeOptions');
            if (typeof modeOptsParam == 'string') {
                modeOptsParam = JSON.decode(modeOptsParam);
            }
            if (modeOptsParam) {
                // get options for the provided modes
                for (i = 0; i < modes.length; i++) {
                    modeOpts[modes[i]] = modeOptsParam[modes[i]];
                }
            }
            this.modeOptions = modeOpts;
            this.defaultMode = activeMode;
            clearInput = this.getStaticParameter('clearInputOnSubmit');
            if (clearInput != undefined && clearInput.toLowerCase() === 'false') {
                this.clearInputOnSubmit = false;
            }
            // send the name of the CSS class we are using for switching the modes so the widget can render it
            this.setFilterParameter('modeSwitchElementCssClass', this.modeSwitchElementCssClass);
            this.autocompleters = {};
            // use default factory instance if there isn't a custom one defined
            factory = this.getStaticParameter('acFactoryInstance') || 'autocompleterFactory';
            this.autocompleterFactory = window[factory];

            this.noSubmitEventCssClass = this.getStaticParameter('noSubmitEventCssClass');
            this.noSubmitEventCssElementSelector = this
                    .getStaticParameter('noSubmitEventCssElementSelector');
        },

        /**
         * Autocompleter selection callback which will trigger the E2G event defined by
         * autocompleter option acSelectEvent. This handler won't be registered if the option is not
         * defined.
         * 
         * @param {Element} inputElem The input element the autocompleter is attached to
         * @param {Element} choiceElem The suggestion element that was selected
         * @param {Object|String} token The suggestion object that belongs to the selected element
         * @param {String} inputValue The value that will be added to the input
         */
        autocompleterChoiceSelected: function(inputElem, choiceElem, token, inputValue) {
            var modeOpts = this.modeOptions[this.currentMode];
            var eventName = modeOpts.acSelectEvent;
            // isn't triggered if there is no event defined
            this.sendFilterGroupEvent(eventName, token);
        },

        /**
         * Callback that is invoked when the autocompleter suggestions are hidden.
         */
        autocompleterChoicesHidden: function() {
            // TODO kinda ugly w.r.t. the generic nature of the widget
            this.searchInput.getParent().removeClass('cn-chooser-input-select');
        },

        /**
         * Callback that is invoked when the autocompleter suggestions are shown.
         */
        autocompleterChoicesShown: function() {
            // TODO kinda ugly w.r.t. the generic nature of the widget
            this.searchInput.getParent().addClass('cn-chooser-input-select');
        },

        /**
         * Event handler for beforeRequestCallback callback of the AutocompleterRequestDataSource
         * called by the DataSource before the request is sent. This callback adds the current
         * filter parameters of the associated FilterParameterStore.
         * 
         * @param {Request} request The prepared request object.
         * @param {Object} data The post data to be sent.
         * @param {String} queryParam The name of the query parameter
         */
        beforeAutocompleterRequest: function(request, data, queryParam) {
            var modeOptions, parameterSubset, i, params;
            modeOptions = this.modeOptions[this.currentMode];
            parameterSubset = modeOptions && modeOptions.acParameterSubset;
            // update the post data with the current filter parameters
            params = this.filterParamStore.getCurrentFilterParameters();
            for (i in params) {
                // do not override existing values
                if (data[i] === undefined
                        && (!parameterSubset || parameterSubset.contains(params[i]))) {
                    data[i] = params[i];
                }
            }
        },

        /**
         * @override
         */
        beforeRemove: function() {
            var i;
            for (i in this.autocompleters) {
                this.autocompleters[i].destroy();
            }
            if (this.placeholders) {
                this.placeholders.destroy();
            }
            this.parent();
        },

        clearInput: function(focusInput) {
            var placeholder;
            this.searchInput.value = '';
            if (focusInput) {
                this.searchInput.focus();
            } else {
                placeholder = this.placeholders
                        && this.placeholders.getPlaceholder(this.searchInput);
                if (placeholder) {
                    placeholder.refresh();
                }
            }
        },

        /**
         * Create an autocompleter for a search mode instance, if necessary.
         * 
         * @param {Object} modeOptions The options for the search mode that where extracted from the
         *            static parameters and defines whether and how an autocompleter should be
         *            created
         * @param {Element} [positionSource] An optional element to position the autocompleter
         *            suggestions.
         * @returns {Autocompleter} the created autocompleter or null if no completer should be
         *          created
         */
        createAutocompleter: function(modeOptions, positionSource) {
            var autocompleter, options, factoryArgs, factoryFunction, i, args, injectFunction;
            if (!modeOptions.acUrl && !modeOptions.acFactoryFunction) {
                return null;
            }
            options = {};
            options.autocompleterOptions = {
                'activateOnInit': false,
                'clearInputOnSelection': ((!!modeOptions.acSelectEvent) && this.clearInputOnSubmit),
                'unfocusInputOnSelection': true,
                'updateInputOnSelection': (!modeOptions.acSelectEvent || !this.clearInputOnSubmit)
            };
            options.inputFieldOptions = {
                'positionSource': positionSource
            };
            if (!modeOptions.acParameterSubset || modeOptions.acParameterSubset.length > 0) {
                options.dataSourceOptions = {
                    'beforeRequestCallback': this.beforeAutocompleterRequest.bind(this)
                };
            }
            injectFunction = modeOptions.acInject && this[modeOptions.acInject];
            if (injectFunction && typeof (injectFunction) === 'function') {
                options['injectChoiceContentCallback'] = injectFunction.bind(this);
            }
            Object.merge(options, modeOptions.acOptions);
            factoryArgs = [];
            factoryArgs.push(this.searchInput);
            factoryArgs.push(options);
            factoryArgs.push(modeOptions.acPostData);
            // create generic autocompleter if a URL is given, otherwise use the factory function
            if (modeOptions.acUrl) {
                factoryFunction = 'createGenericAutocompleter';
                factoryArgs.push(modeOptions.acUrl);
                factoryArgs.push(modeOptions.acPostVar);
            } else {
                factoryFunction = modeOptions.acFactoryFunction;
                // add additional arguments to be passed to the factory function
                args = modeOptions.acFactoryFunctionArgs;
                if (args) {
                    for (i = 0; i < args.length; i++) {
                        factoryArgs.push(args[i]);
                    }
                }
            }
            autocompleter = this.autocompleterFactory[factoryFunction].apply(
                    this.autocompleterFactory, factoryArgs);
            autocompleter.addEvent('show', this.autocompleterChoicesShown.bind(this));
            autocompleter.addEvent('hide', this.autocompleterChoicesHidden.bind(this));
            if (modeOptions.acSelectEvent) {
                autocompleter.addEvent('onChoiceSelected', this.autocompleterChoiceSelected
                        .bind(this));
            }
            return autocompleter;
        },

        createAutocompleters: function() {
            var i, modeOpts, positionSource, autocompleter;
            positionSource = this.domNode.getElement('.control-autocompleter-position-source');
            // create the required autocompleters and store under the mode name
            if (this.modeOptions) {
                for (i in this.modeOptions) {
                    modeOpts = this.modeOptions[i];
                    autocompleter = this.createAutocompleter(modeOpts, positionSource);
                    if (autocompleter) {
                        this.autocompleters[i] = autocompleter;
                    }
                }
            }
        },

        /**
         * @override
         */
        filterParametersChanged: function(changedParams) {
            var searchMode, autocompleter;
            // clear cache of autocompleters
            if (this.autocompleters) {
                for (searchMode in this.autocompleters) {
                    if (this.autocompleters.hasOwnProperty(searchMode)) {
                        autocompleter = this.autocompleters[searchMode];
                        autocompleter.resetQuery(false);
                    }
                }
            }
        },

        /**
         * Implementation for the autocompleter injectChoiceContentCallback hook to set the
         * suggestion content from a JSON object. The JSON member to put in the suggestion content
         * is defined by the search mode option 'acInjectMemberText'. The JSON member to be used as
         * value for updating the input field when the suggestion entry is selected is defined by
         * the search mode option 'acInjectMemberValue'. Both attributes must be contained in the
         * object otherwise it is ignored.
         * 
         * @param {Element} elem The element whose content should be set
         * @param {Object} token A JSON object
         * @param {Object} [metaData] Some meta data, which is ignored
         * @returns {String} The value to update the input or null to let the token be ignored
         */
        injectToken: function(elem, token, metaData) {
            var autocompleter;
            var modeOpts = this.modeOptions[this.currentMode];
            var suggestionText = token[modeOpts.acInjectMemberText];
            var inputValue = token[modeOpts.acInjectMemberValue];
            if (suggestionText && inputValue) {
                autocompleter = this.autocompleters[this.currentMode];
                elem.set('html', autocompleter.markQueryValue(suggestionText));
                return inputValue;
            }
            return null;
        },

        markSearchModeSwitcher: function(mode) {
            var i;
            var modeSwitchers = this.domNode.getElements('.' + this.modeSwitchElementCssClass);
            var modeCss = this.modeSwitchElementCssClass + '-' + mode;
            for (i = 0; i < modeSwitchers.length; i++) {
                if (modeSwitchers[i].hasClass(modeCss)) {
                    modeSwitchers[i].addClass('cn-selected');
                } else {
                    modeSwitchers[i].removeClass('cn-selected');
                }
            }
        },

        /**
         * @override
         */
        refresh: function() {
            // block further refreshs after first
            if (!this.firstDOMLoadDone) {
                this.parent();
            }
        },

        /**
         * @override
         */
        refreshComplete: function(responseMetadata) {
            this.parent(responseMetadata);
            this.searchInput = this.domNode.getElementById(this.widgetId + '-search-input');
            this.searchInput.addEvent('keydown', function(event) {
                var autocompleter;
                if (event.key == "enter") {
                    autocompleter = this.autocompleters[this.currentMode];
                    // check for opened suggestion dropdown
                    if (!autocompleter || !autocompleter.suggestionsShown) {
                        this.submitChanges();
                    }
                    event.stop();
                }
            }.bind(this));

            this.createAutocompleters();
            this.placeholders = communote.utils.attachPlaceholders(this.searchInput);

            this.switchSearchMode(this.currentMode ? this.currentMode : this.defaultMode);
        },

        searchModeClicked: function(elem) {
            var i, autocompleter;
            // hide choices if they are shown
            autocompleter = this.autocompleters[this.currentMode];
            if (autocompleter) {
                autocompleter.close();
            }
            elem = $(elem);
            elem.blur();

            // focus input field 
            this.searchInput.focus();

            var mode = null;
            var cssClasses = elem.getProperty('class').split(' ');
            var modeCssPrefix = this.modeSwitchElementCssClass + '-';
            for (i = 0; i < cssClasses.length; i++) {
                if (cssClasses[i].contains(modeCssPrefix)) {
                    mode = cssClasses[i].substring(modeCssPrefix.length);
                    break;
                }
            }
            if (mode) {
                this.switchSearchMode(mode);
            }
        },

        submitChanges: function() {
            var value = this.searchInput.get('value');
            // fire event of current options
            var modeOpts = this.modeOptions[this.currentMode];
            if (modeOpts) {
                if (modeOpts.submitEvent && value) {
                    this.sendFilterGroupEvent(modeOpts.submitEvent, value);
                    if (this.clearInputOnSubmit) {
                        this.clearInput();
                    }
                }
            }
        },

        /**
         * Deactivate autocompleter of current mode and activate that of the new mode.
         * 
         * @param {boolean} newMode The new mode that will be activated
         */
        switchAutocompleter: function(newMode) {
            var oldAutocompleter = this.autocompleters[this.currentMode];
            var newAutocompleter = this.autocompleters[newMode];
            if (oldAutocompleter) {
                oldAutocompleter.deactivate();
            }
            if (newAutocompleter) {
                newAutocompleter.activate();
            }
        },

        /**
         * Switch the search mode.
         * 
         * @param {String} mode The new mode to switch to
         */
        switchSearchMode: function(mode) {
            var modeOpts = this.modeOptions[mode];
            if (!modeOpts) {
                return;
            }
            this.updatePlaceHolderText(mode);
            this.markSearchModeSwitcher(mode);
            this.switchAutocompleter(mode);
            this.toggleNoSubmitEventCssClass(modeOpts);
            this.currentMode = mode;
        },

        /**
         * Toggle the noSubmitEventCssClass class when the new mode has no submit event or the
         * current one had none. If the noSubmitEventCssClass member is not defined the call is
         * ignored.
         * 
         * @param {Object} modeOpts The mode options of the new mode
         */
        toggleNoSubmitEventCssClass: function(modeOpts) {
            var hadSubmitEvent, elemAction, elem;
            if (this.noSubmitEventCssClass) {
                hadSubmitEvent = this.currentMode
                        && !!this.modeOptions[this.currentMode].submitEvent;
                if (modeOpts.submitEvent) {
                    if (!hadSubmitEvent) {
                        elemAction = 'removeClass';
                    }
                } else {
                    if (hadSubmitEvent == undefined || hadSubmitEvent) {
                        elemAction = 'addClass';
                    }
                }
                if (elemAction) {
                    if (this.noSubmitEventCssElementSelector) {
                        elem = this.domNode.getElement(this.noSubmitEventCssElementSelector);
                    } else {
                        elem = this.domNode.getFirst();
                    }
                    if (elem) {
                        elem[elemAction](this.noSubmitEventCssClass);
                    }
                }
            }
        },

        updatePlaceHolderText: function(mode) {
            var elem, placeholder;
            // get new place holder text from mode options or rel attribute of switch
            var newText = this.modeOptions[mode].placeholder;
            if (newText == undefined) {
                elem = this.domNode.getElement('.' + this.modeSwitchElementCssClass + '-' + mode);
                if (elem) {
                    newText = elem.get('rel');
                }
            }
            if (newText != undefined) {
                placeholder = this.placeholders.getPlaceholder(this.searchInput);
                if (placeholder) {
                    placeholder.setText(newText);
                    placeholder.refresh();
                }
            }
        }
    });
    namespace.addConstructor('SearchBoxWidget', SearchBoxWidget);
})(window.runtimeNamespace);