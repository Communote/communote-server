/**
 * @class
 * @augments communote.widget.classes.controls.InputField
 */
communote.widget.classes.controls.SearchField = communote.widget.classes.controls.InputField
        .extend(
/** 
 * @lends communote.widget.classes.controls.SearchField.prototype
 */              	
{
            filterType: false,           
            registerListeners: function() {
                // TODO this is a strange dependency. Maybe search field should be a control
                // consisting of optionslist and input field.
                this.listenTo('optionSelected', this.parent.channel);
                this.base();
            },
            
            /**
             * this switchs the input between the filter options
             * 
             * @param {} selection - the selector object
             */

            optionSelected: function(selection) {
                // TODO option list is inflexible: add a value attribute that is
                // passed to this handler.

                this.detachAutoCompleters();
                
                var type = selection.attr('class');
                switch (type) {
                case 'cntwTags':
                    this.filterType = 'tagPrefix';
                    this.attachTagAutoCompleter();
                    break;
                case 'cntwText':
                    this.filterType = 'noteSearchString';
                    break;
                case 'cntwUserText':
                    this.filterType = 'userSearchString';
                    this.attachUserAutoCompleter();
                    break;
                default:
                    // ignore unsupported values
                    break;
                }
                
            },
            
            /**
             * this starts filtering if textsearch is selected
             * 
             * @param string value - the search term
             */

            confirmValue: function(value) {
                if (!this.filterType || (this.filterType == 'tagPrefix') || (this.filterType =='userSearchString')) {
		            return;
			    }
                this.setValue('');
                this.fireEvent('filterEvent', this.widget.channel, {
                    paramName: this.filterType,
                    value: value,
                    added: true
                });
            },
            
            /**
             * this removes the autocompleters and resets the inputfield
             */

            detachAutoCompleters: function() {
                var input = this.getInput();
                input.catcomplete('disable');
                input.catcomplete('destroy');
                input.imagecomplete('disable');
                input.imagecomplete('destroy');
                
                //reset inputField
                input.val('');
                input.focus();
            },
            
            /**
             * this attachs the categorized autocompleter for the tag autocompletion
             */

            attachTagAutoCompleter: function() {
                var input, attachToElem, self;
                   
                self = this;         
                input = this.getInput();

                attachToElem = input.parent().parent();
                input.catcomplete({

                    autoFocus: true,
                    position: {
                        of: attachToElem
                    },
                    
                    source: function(request, response){ //CNHC-521
                        self.tagsSource(self,request, response, this);
                    }, 

                    /**
                     * handles a user selection from the autocomplete menu
                     * 
                     * @param {} event
                     * @param {} ui
                     */

                    select: function(event, ui) {
                        var item = ui.item;
                        eventData = {};
                        eventData.paramName = 'tagIds';
                        eventData.value = item.apiTag.tagId;
                        eventData.label = item.label;
                        eventData.added = true;
                        self.fireEvent('filterEvent', self.widget.channel, eventData);
                        
                        input.val("").focus();
                        event.preventDefault();
                        return false;
                    },

                    /**
                     * this prevent value inserted on focus
                     * 
                     */
                    
                    focus: function() {
                        return false;
                    },
                    
                    /**
                     * this selects the last hovered element, when the mouse leaves
                     * the menu
                     * 
                     */


                    open: function(event, ui) {
                        communote.utils.autocompleter.open(input,'catcomplete', function(){
                            return input.catcomplete('widget');
                        });
                    },
                    
                    /**
                     * handles change event for the completer menu
                     *
                     */
                    
                    change: function(event, ui){
                        communote.utils.autocompleter.change(input, event);
                    }

                });
                
                /**
                 * handles keydown for the input field
                 *
                 * @param {} event
                 */
                input.keydown(function(event) {
                    communote.utils.autocompleter.keydown(event,function(){
                        input.catcomplete("close");
                    });
                });                

                /**
                 * handles focus lost for the input field
                 *
                 */
                        
                input.blur(function(){
                    communote.utils.autocompleter.blur(input);
                });
            
            },
            /**
             * this attachs the imaged autocompleter for the user autocompletion
             */
            attachUserAutoCompleter: function() {
                var input, attachToElem, self;                
                self = this;
         
                input = this.getInput();               
                attachToElem = input.parent().parent();

                input.imagecomplete({

                    autoFocus: true,
                    position: {
                        of: attachToElem
                    },
                    // special class needed for different formatting of the
                    // category headers
                    cssClass: 'category-annotation',
                    
                    source: function(request, response){//CNHC-521
                        self.authorSource(self, request, response, this);
                    },

                    /**
                     * handles a user selection from the autocomplete menu
                     * 
                     * @param {} event
                     * @param {} ui
                     */

                    select: function(event, ui) {
                        var item = ui.item;
                        eventData = {};
                        eventData.paramName = 'userIds';
                        eventData.value = item.user.userId;
                        eventData.label = item.label;
                        eventData.added = true;
                        self.fireEvent('filterEvent', self.widget.channel, eventData);
                        
                        input.val("").focus();
                        event.preventDefault();
                        return false;
                    },

                    /**
                     * this prevent value inserted on focus
                     * 
                     */
                    focus: function() {
                        return false;
                    }, 
                    
                    /**
                     * handles open event for the completer menu
                     *
                     */

                    open: function(event, ui) {
                        communote.utils.autocompleter.open(input,'imagecomplete', function(){
                            return input.imagecomplete('widget');
                        });
                    }, 
                    
                    /**
                     * handles change event for the completer menu
                     *
                     */
                    
                    change: function(event, ui){
                        communote.utils.autocompleter.change(input, event);
                    }
                });
                
                /**
                 * handles keydown event for the input field
                 *
                 */

                input.keydown(function(event) {
                    communote.utils.autocompleter.keydown(event,function(){
                        input.imagecomplete("close");
                    });
                });
                
                /**
                 * handles focus lost for the input field
                 *
                 */
                
                input.blur(function(){
                    communote.utils.autocompleter.blur(input);
                });
            },

            /**
             * define data source for categorized and imaged autocomplete
             * 
             * @param {} self 
             *              this
             * @param {} request
             * @param {} response
             * @param {} completer
             *              the jQuery autocompleter
             */
            // new feature - activate if needed...
            // maxHeight: 100,
            authorSource: function(self, request, response, completer) {
                var term = request.term;
                communote.widget.ApiController.getTimelineUserList(term,

                    // prepare search results function
                    function(result, message, channel) {

                        var catcompleteData, i, data, user;

                        completer.metadata = [];

                        var resultsToShow = result.data.length;
                        var moreResults = result.origResult.metaData.moreElementsAvailable;
                        var i18nData = {
                            0: resultsToShow,
                            1: "<strong>" + communote.utils.encodeXml(term) + "</strong>"
                        };

                        if (moreResults) {
                            completer.metadata[0] = communote.widget.I18nController.getTextFormatted(
                                    'htmlclient.userfilter.autocomplete.results.more',
                                    i18nData);
                        } else {
                            completer.metadata[0] = communote.widget.I18nController.getTextFormatted(
                                    'htmlclient.userfilter.autocomplete.results', i18nData);
                        }

                        // prepare data for presentation
                        catcompleteData = [];
                        if(resultsToShow == 0){
                            data = {};
                            data.label = "";                            
                            catcompleteData[catcompleteData.length] = data;
                        }
                        for (i = 0; i < resultsToShow; i++) {
                            data = {};
                            user = result.data[i];
                            
                            data.label = communote.utils.getUserFullName(user,true);
                            data.value = user.alias;
                            data.user = user;

                            catcompleteData[catcompleteData.length] = data;
                        }
                        // continue presenting the data
                        response(catcompleteData);

                    }, this, true, self.widget.getCurrentFilterParameters(), true, 20);

            },

            /**
             * define data source for categorized autocomplete
             * 
             * @param {} self 
             *              this
             * @param {} request
             * @param {} response
             * @param {} completer
             *              the jQuery autocompleter
             */
            // new feature - activate if needed...
            // maxHeight: 100,
            tagsSource: function(self, request, response, completer) {
                var term, $, config;
                term = request.term;
                $ = communote.jQuery;
                config = self.widget.configuration;    
                communote.widget.ApiController.getTagSuggestionList(term,

                    // prepare search results function
                    function(result, message, channel) { 
                        var suggestionList, suggestionLists, tags, tag, i, j, autocompleteTag, catcompleteData;
                        catcompleteData = [];
                        suggestionLists = result.data; 
                        completer.metadata = [];
                        
                        for (i = 0; i < suggestionLists.length; i++) {
                            suggestionList = suggestionLists[i];
                            if($.inArray(suggestionList.alias, config.hideSuggestionForAliases) == -1){
                                tags = suggestionList.tags;
                                if (tags == null) {
                                    tags = [];
                                }

                                var resultsToShow = tags.length;
                                var i18nData = {
                                        0: resultsToShow,
                                        1: "<strong>" + communote.utils.encodeXml(term) + "</strong>"
                                    };
                                if (tags.length == 20) {
                                    completer.metadata[i] = communote.widget.I18nController.getTextFormatted(
                                            'htmlclient.tagfilter.autocomplete.results.more',
                                            i18nData);
                                } else {
                                    completer.metadata[i] = communote.widget.I18nController.getTextFormatted(
                                            'htmlclient.tagfilter.autocomplete.results', i18nData);
                                }
                                
                                if(tags.length == 0){
                                    autocompleteTag = {};
                                    autocompleteTag.label = "";
                                    if(($.inArray(suggestionList.alias, config.hideSuggestionTitleForAliases) == -1) 
                                            || (suggestionLists.length>1)){
                                        autocompleteTag.category = suggestionList.name;
                                    }
                                    catcompleteData[catcompleteData.length] = autocompleteTag;
                                }
                                
                                for (j = 0; j < tags.length; j++) {
                                    tag = tags[j];
                                    autocompleteTag = {};
                                    // prepare data for presentation
                                    if (tag.name != undefined) {
                                        autocompleteTag.label = tag.name;
                                    } else {
                                        autocompleteTag.label = tag.defaultName;
                                    }
                                    if (tag.description != undefined) {
                                        autocompleteTag.context = tag.description;
                                    }

                                    if(($.inArray(suggestionList.alias, config.hideSuggestionTitleForAliases) == -1) 
                                            || (suggestionLists.length >1)){
                                        autocompleteTag.category = suggestionList.name;
                                    }

                                    autocompleteTag.apiTag = tag;
                                    catcompleteData[catcompleteData.length] = autocompleteTag;
                                }
                            }
                        }
                        // continue presenting the data
                        response(catcompleteData);

                    },this, true, self.widget.getCurrentFilterParameters(), true, true, 20);
            },
            /**
             * This method overwrites the renderDone method from InputField. It appends the search icon next to the input field
             */
            renderingDone: function() {
                var $ = communote.jQuery;
				$(this.getInput()).parent().addClass("cntwMagnifier");
                this.base();
            }

        });
