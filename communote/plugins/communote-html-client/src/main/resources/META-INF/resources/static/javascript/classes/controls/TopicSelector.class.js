/**
 * Control for selecting a topic to create a note. The control provides a search field with search
 * suggestion and a drop down showing a predefined set of topics.
 * @class
 * @augments communote.widget.classes.controls.Control
 */
communote.widget.classes.controls.TopicSelector = communote.widget.classes.controls.Control.extend(
/** 
 * @lends communote.widget.classes.controls.TopicSelector.prototype
 */	
{
    name: 'TopicSelector',
    // configurable array of aliases of topics to show
    predefinedTopicAliases: null,
    // configurable alias of a topic to select as default topic
    preselectedTopicAlias: null,
    writableTopicsAvailable: true,
    //template: 'no template',
    autocompleteMaxTopicCount: 50,
    autocompleteMaxHeight: 200,
    autocompleter: null,
    // currently selected topic
    selectedTopic: null,
    // default topic which should be taken if no other is selected
    defaultTopic: null,
    // reference to the input element
    inputElement: null,
    mostUsedTopicsTitle: null,
    placeholderText: null,

    /**
     * @param id
     * @param config
     * @param widget
     */
    constructor: function(id, config, widget) {
        this.base(id, config, widget);
        var widgetConfig = this.widget.configuration;
        if (widgetConfig.edShowTopicChooser === false) {
            this.noRender = true;
        }
        if (widgetConfig.edTopicList) {
            this.predefinedTopicAliases = widgetConfig.edTopicList;
        }
        if (widgetConfig.edPreselectedTopic) {
            this.preselectedTopicAlias = widgetConfig.edPreselectedTopic;
        }
        this.mostUsedTopicsTitle = this.getLabel('htmlclient.topicselector.mostused.title');
        this.placeholderText = this.getLabel('htmlclient.topicselector.placeholder');
    },

    createTopicMenuDropdown: function() {
        var self;
        var domNode = this.getDomNode();
        this.topicMenu = domNode.find('.cntwSelector').overlaymenu({
            itemLabelAttribute: 'title',
            appendTo: 'div.cntwSelector',
            openCallback: function(eventData) {
                self.toggleTopicMenuSelectorArrow(eventData && eventData.target, true);
            },
            closeCallback: function(eventData) {
                // eventData won't be null if callback is triggered by a 
                // click on the dropDown arrow (see click handler definition below)
                self.toggleTopicMenuSelectorArrow(eventData && eventData.target, false);
            },
            selectionCallback: function(topic) {
                self.selectTopic(topic);
            },
            loadData: this.loadTopicMenuEntries,
            loadDataBind: this
        }).data('overlaymenu');
        self = this;
        domNode.find('.cntwDropDownArrow').click(function(event) {
            self.topicMenu.toggle(event);
        });
    },

    /**
     * Callback to change style of the topic menu drop down arrow when the menu is opened/closed.
     * 
     * @param {Element} [elem] The element of drop down.
     * @param {boolean} open True if the menu is open, false otherwise.
     */
    toggleTopicMenuSelectorArrow: function(elem, open) {
        if (elem) {
            elem = communote.jQuery(elem);
        } else {
            elem = this.getDomNode().find('.cntwDropDownArrow');
        }
        if (open) {
            elem.removeClass('cntwSliderOpen');
            elem.addClass('cntwSliderClose');
        } else {
            elem.removeClass('cntwSliderClose');
            elem.addClass('cntwSliderOpen');
        }
    },

    /**
     * Callback that is called by topicMenu when it was marked dirty
     */
    loadTopicMenuEntries: function() {
        if (this.predefinedWritableTopics) {
            // if there is a predefined topic list we expect it will never
            // change, so we just pass it to the topicMenu
            this.topicMenu.dataLoaded(this.predefinedWritableTopics);
        } else {
            this.controller.ApiController.getTopics(null, 'MOST_USED', 0, function(data) {
                this.topicMenu.dataLoaded({
                    title: this.mostUsedTopicsTitle,
                    entries: data.data
                });
            }, this);
        }
    },

    /**
     * @override
     */
    bindEvents: function() {
        var self = this;
        if (!this.writableTopicsAvailable || this.noRender) {
            // stop here because editor will not be visible
            return;
        }
        // local reference to input
        this.inputElement = this.getDomNode().find('input');
        this.createTopicMenuDropdown();
        // use same element for positioning as the drop down is using 
        this.autocompleter = this.attachAutocompleter(this.inputElement, this.topicMenu.element);
        this.updateInputField();
        this.placeholder = communote.utils.addPlaceholder(this.inputElement, this.placeholderText, 
                {refreshOnBlur: false});
        // add blur handler which updates the input field with the selected topic
        // this blur handler must be added after adding the place for correct order of events
        this.inputElement.blur(function() {
            // only update if autocompleter is not open because click on scrollbar of
            // suggestions also creates blur event
            if (!self.autocompleterOpen) {
                self.updateInputField();
            }
            return true;
        });
        if (this.placeholder) {
            this.placeholder.refresh();
        }
    },

    /**
     * Attach the autocompleter to the input element.
     * 
     * @param {jQuery} inputElem The input jQuery element to enhance with the autocompleter.
     * @param {jQuery} attachToElem The jQuery element to use for positioning and sizing the suggestions.
     * @return the autocompleter widget
     */
    attachAutocompleter: function(inputElem, attachToElem) {
        var self = this;
        var cache = {};
        var acElem = inputElem.catcomplete({
            maxHeight: this.autocompleteMaxHeight,
            // special class needed for different formatting of the
            // category headers
            cssClass: 'category-annotation',
            autoFocus: true,
            position: {
                of: attachToElem
            },
            source: function(request, response) {
                // check cache
                var completer = this;
                var term = request.term;
                if (term in cache) {
                    response(cache[term]);
                    return;
                }

                communote.widget.ApiController.searchWritableTopics(term,
                        self.autocompleteMaxTopicCount, self.predefinedTopicAliases,

                        // prepare search results function
                        function(result, message, channel) {
                            var catcompleteData, i, data;
                            var moreResults = result.origResult.metaData.moreElementsAvailable;

                            completer.metadata = [];
                            
                            var resultsToShow = result.data.length;
                            var i18nData = {
                                0: resultsToShow,
                                1: "<strong>" + communote.utils.encodeXml(term) + "</strong>"
                            };

                            if (moreResults) {
                                completer.metadata[0] = communote.widget.I18nController.getTextFormatted(
                                        'htmlclient.topicselector.autocomplete.results.more',
                                        i18nData);
                            } else {
                                completer.metadata[0] = communote.widget.I18nController.getTextFormatted(
                                        'htmlclient.topicselector.autocomplete.results', i18nData);
                            }

                            // prepare data for presentation
                            catcompleteData = [];
                            
                            if(resultsToShow == 0){
                                data = {};
                                data.label = "";                            
                                catcompleteData[catcompleteData.length] = data;
                            }
                            
                            for (i = 0; i < resultsToShow; i++) {
                                data = result.data[i];
                                data.label = data.title;
                                data.value = data.title;

                                catcompleteData[catcompleteData.length] = data;
                            }

                            // fill cache
                            cache[term] = catcompleteData;

                            // continue presenting the data
                            response(catcompleteData);

                        }, this, true);

            }, // end source

            select: function(event, ui) {
                self.selectTopic(ui.item);
                return true;
            },
            open: function() {
                // close topic menu if open
                self.topicMenu.close();
                
                self.autocompleterOpen = true;
                // disable the placeholder when the autocompleter is open to avoid unintended
                // updates of the input
                if (self.placeholder) {
                    self.placeholder.disable();
                }
            }, 
            close: function() {
                var activeElement;
                self.autocompleterOpen = false;
                if (self.placeholder) {
                    self.placeholder.enable(false);
                }
                // update the input field, but only if not focused because Esc can close suggestions
                activeElement = self.inputElement[0].ownerDocument.activeElement;
                if (activeElement !== self.inputElement[0]) {
                    self.updateInputField();
                }
            }
        }).blur(function(){       
            communote.widget.ApiController.abortAutocompleteRequests();
            communote.jQuery(this).removeClass('ui-autocomplete-loading');   
        });
        return acElem.data('catcomplete');
    },

    updateInputField: function() {
        var title;
        if (this.selectedTopic) {
            title = this.selectedTopic.title;
        } else if (this.defaultTopic) {
            title = this.defaultTopic.title;
        } else {
            title = '';
            // reset autocompleter so that it is consistent with input
            this.autocompleter.term = '';
        }
        if (this.inputElement.val() != title) {
            this.inputElement.val(title);
        }
        if (this.placeholder) {
            this.placeholder.refresh();
        }
    },
    /**
     * @override
     */
    render: function() {
        if (this.noRender) {
            // call load data to eval the preselected topic
            this.loadData();
        } else {
            this.base();
        }
    },
    /**
     * @override
     */
    loadData: function() {
        // must check whether there are writable topics
        if (this.predefinedTopicAliases) {
            this.controller.ApiController.getTopics(this.predefinedTopicAliases, 'WRITE', 0,
                    this.loadDataComplete, this);
        } else {
            // in case there is no configured topic list that should be displayed 
            // in selector, check if there is at least one writable topic
            this.controller.ApiController.getTopics(null, 'WRITE', 1, this.loadDataComplete, this, false);
        }
    },
    /**
     * Callback for loadData
     * 
     * @param {object} data The response object, whose 'data' member will hold the topics
     */
    loadDataComplete: function(data) {
        var i, topic;
        var topics = data.data;
        var length = topics.length;
        var callRenderingDone = true;
        if (length == 0) {
            // save that there are no writable topics
            this.writableTopicsAvailable = false;
        } else {
            // save the found topics if a topicList is configured for later use
            if (this.predefinedTopicAliases) {
                this.predefinedWritableTopics = topics;
            }
            // check for the configured preselectTopic
            if (this.preselectedTopicAlias) {
                // in case of a configured topicList the preselected topic must be contained
                if (this.predefinedTopicAliases) {
                    // check server result, because it contains the writable
                    for (i = 0; i < length; i++) {
                        topic = topics[i];
                        if (topic.alias === this.preselectedTopicAlias) {
                            this.defaultTopic = topic;
                            break;
                        }
                    }
                } else {
                    // the found writable topic could be the preselected one
                    topic = topics[0];
                    if (topic.alias === this.preselectedTopicAlias) {
                        this.defaultTopic = topic;
                    } else {
                        // cannot continue yet
                        callRenderingDone = false;
                        // make another server request to check if the topic is writable
                        this.controller.ApiController.getTopics(this.preselectedTopicAlias, 'WRITE',
                                1, function(data) {
                                    if (data.data.length == 1) {
                                        this.defaultTopic = data.data[0];
                                    }
                                    // now that all information is available call rendering done
                                    this.renderingDone();
                                }, this);
                    }
                }
            }
        }
        if (callRenderingDone) {
            this.renderingDone();
        }
    },

    /**
     * Returns whether there are writable topics. This method won't return a reliable result until
     * the first renderingDone event was sent.
     * 
     * @return {boolean} true if there are writable topics for the current user, false otherwise. In
     *         case the configuration option edTopicList is defined, there has to be at least one
     *         writable topic in that list
     */
    isWritableTopicsAvailable: function() {
        return this.writableTopicsAvailable;
    },

    /**
     * return the currently selected topic
     * 
     * @return {object} The topic data object or null if no topic is selected
     */
    getSelectedTopic: function() {
        if (this.selectedTopic) {
            return this.selectedTopic;
        } else {
            return this.defaultTopic;
        }
    },

    /**
     * Set the currently selected topic. The input field will be updated accordingly.
     * 
     * @param {object} topic The object with details about the topic to set
     */
    selectTopic: function(topic) {
        this.selectedTopic = topic;
        this.updateInputField();
    },
    /**
     * Should be invoked when a note was created.
     */
    noteCreated: function() {
        // might not be defined if for example not rendered
        if (this.topicMenu) {
            // when showing most used topics mark the menu entries as dirty
            this.topicMenu.markDirty();
        }
    }
});
