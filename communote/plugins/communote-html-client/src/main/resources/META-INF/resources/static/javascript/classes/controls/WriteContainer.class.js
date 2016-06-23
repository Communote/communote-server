/**
 * @class
 * @name communote.widget.classes.controls.WriteContainer
 * @augments communote.widget.classes.controls.ContainerControl
 */
communote.widget.classes.controls.WriteContainer = communote.widget.classes.controls.ContainerControl
        .extend(
        /**
         * @lends communote.widget.classes.controls.WriteContainer.prototype
         */
        {
            name : "WriteContainer",
            template : "WriteContainer",
            confirmAction : "create",
            isClickStop : false,
            autocompleteCache : {},
            metadataCache : {},

            /**
             * This is the constructor. Its called the super-class-constructor and it creates a new
             * tag-array.
             */
            constructor : function(id, config, widget) {
                // don't create the widget if there is no authenticated user (public access) 
                if (!communote.currentUser) {
                    throw new communote.widget.classes.ControlDisabledException();
                }
                this.base(id, config, widget);
                this.noteTags = new Array();
                this.tagIdList = new Array();
            },

            /**
             * This method is called, when the control events are bound. Its called the
             * super-class-method and default tags are set.
             */
            bindEvents : function() {
                this.base();
                this.addDefaultTags();
                // hide the input control and view a message, if no writable
                // topic exists
                var elem = communote.jQuery("> .cntwWriteContainer", this.domNode);
                var ctrl = this.getSubControl("name", "TopicSelector");
                if (ctrl && !ctrl.writableTopicsAvailable) {
                    // display error message
                    this.domNode.append("<div class=\"cntwWriteContainer\">"
                            + this.getLabel("htmlclient.writecontainer.nowritabletopics")
                            + "</div>");
                } else {
                    elem.show();
                }

                elem = communote.jQuery(".cntwTagSelector", this.domNode);
                if (this.widget.configuration.edShowTagField) {
                    this.attachAutocompleter(elem);
                    elem.show();
                }
            },

            /**
             * This method adds the given tags from the configuration.
             */
            addDefaultTags : function() {
                var container = this.getDomNode();
                this.createNewTags(this.widget.configuration.edAddDefaultTags, container);
            },

            /**
             * This method creates a new wrapped tag-item with the given name and adds this into the
             * given container.
             * 
             * @param name
             *            for the new Tag
             * @param container
             * 
             */
            getMessageNode : function() {
                return this.getDomNode().find('.cntwWriteContainer');
            },

            /**
             * register the events
             * 
             * @param listener
             *            object
             */
            registerListeners : function() {
                this.listenTo("sendNote");
                this.listenTo("topicSelected");
                // this.listenTo("cancelReply");
                this.listenTo("optionSelected");
                this.listenTo("noteServiceSuccessEvent");
                this.base();
            },

            loadControls : function() {
                var itemOptionList = [];
                var defaultText = "";
                this.controls = [];
                if (this.configData.author && this.configData.author.alias !== null) {
                    if (this.configData.isDirectMessage) {
                        defaultText += "d ";
                    }
                    defaultText += "@" + this.configData.author.alias;
                }
                this.controls.push({
                    type : "WriteField",
                    slot : ".cntwWriteField",
                    data : {
                        defaultText : defaultText
                    }
                });
                if (!this.configData.topicId) {
                    this.controls.push({
                        type : "TopicSelector",
                        slot : ".cntwTopicSelector"
                    });
                }
                // fade out the attachment option in create topic area made by
                // configuration param
                if (this.widget.configuration.edShowUpload) {
                    itemOptionList.push({
                        label : "htmlclient.writecontainer.attachment",
                        css : "cntwAddAttachment",
                        selected : false
                    });
                }
                // fade out the attachment option in create topic area made by
                // configuration param
                if (this.widget.configuration.edShowTag) {
                    itemOptionList.push({
                        label : "htmlclient.writecontainer.tags",
                        css : "cntwAddTags",
                        selected : this.widget.configuration.edShowTagField
                    });
                }
                if (itemOptionList.length > 0) {
                    this.controls.push({
                        type : "OptionList",
                        slot : ".cntwWriteOptionList",
                        data : {
                            pipeList : true,
                            items : itemOptionList
                        }
                    });
                }
                this.defineSendControls();
                // if (this.widget.configuration.edShowTag) {
                this.controls.push({
                    type : "InputField",
                    slot : ".cntwTagSelector",
                    data : {
                        infoTextLabel : "htmlclient.writecontainer.tags.inputfield.placeholder"
                    }
                });
                // }
                // if (this.widget.configuration.edShowUpload) {
                this.controls.push({
                    type : "FileUpload",
                    slot : ".cntwAttachmentSelector",
                    data : {
                        infoTextLabel : "htmlclient.writecontainer.attachment"
                    }
                });
                // }
                this.base();
            },

            /**
             * define the directive for send control
             */
            defineSendControls : function() {
                this.controls.push({
                    type : "SendControls",
                    slot : ".cntwButtonList",
                    data : {
                        pipeList : true,
                        items : [ {
                            label : "htmlclient.common.cancel",
                            css : "cntwCancelNote"
                        }, {
                            label : "htmlclient.common.send",
                            css : "cntwReplyNote"
                        } ]
                    }
                });
            },

            sendNote : function(data) {
                var selectedTopic, unsubmittedInput;
                var $ = communote.jQuery;
                var parent = data.fromControl.domNode.parent();
                // remove all static messages
                $('.cntwInfoBox div', parent).click();
                if (this.configData.topicId != undefined) {
                    data.topicId = this.configData.topicId;
                } else {
                    selectedTopic = this.getSelectedTopic();
                    if (selectedTopic) {
                        data.topicId = selectedTopic.topicId;
                    }
                }
                data.parentNoteId = this.configData.parentNoteId;
                data.isDirectMessage = this.configData.isDirectMessage;

                var fu = this.getSubControl("type", "FileUpload");
                data.attachmentUploadSessionId = fu.getAttachmentUploadSessionId();
                data.attachmentIds = fu.getAttachmentIds();
                unsubmittedInput = $(parent).find('.cntwTagSelector INPUT.ui-autocomplete-input');

                if ((unsubmittedInput.val() != undefined)
                        && !unsubmittedInput.hasClass("cntwEmpty")) {
                    this.createNewTags(unsubmittedInput.val(), parent);
                    unsubmittedInput.val("").blur();
                }
                data.tags = this.noteTags;

                this.startWorking();

                this.fireEvent(this.confirmAction + "Note", this.widget.channel, data);
            },

            getSelectedTopic : function() {
                var topic;
                var selector = this.getSubControl('type', 'TopicSelector');
                if (selector) {
                    topic = selector.getSelectedTopic();
                }
                return topic;
            },

            /**
             * clicking on an item (event routine)
             * 
             * @param {jQ-object}
             *            item
             */
            optionSelected : function(item) {
                if (item.jquery) {
                    var field = this.getSubControl("type", "WriteField");
                    var attachmentNode = this.getSubControl("nodeSelector",
                            ".cntwAttachmentSelector").getDomNode();
                    var tagsNode = this.getSubControl("nodeSelector", ".cntwTagSelector")
                            .getDomNode();
                    if (item.hasClass("cntwCancelNote")) {
                        this.reset();
                    }
                    if (item.hasClass("cntwReplyNote")) {
                        if (!this.isClickStop) {
                            this.isClickStop = true;
                            field.fireEvent("confirmValue", field.channel, field.getValue());
                        }
                    }
                    if (item.hasClass("cntwAddAttachment")) {
                        this.toggleInput(attachmentNode, tagsNode, item.parent());
                    }
                    if (item.hasClass("cntwAddTags")) {
                        if (tagsNode.is(":visible")) {
                            this.detachAutocompleter(tagsNode);
                        } else {
                            this.attachAutocompleter(tagsNode);
                        }
                        this.toggleInput(tagsNode, attachmentNode, item.parent());
                    }
                }
            },

            /**
             * overwrite
             * 
             * @method success if an note is succeed send, reset the clickstop
             */
            success : function(obj) {
                // this.isClickStop = false;
                this.resetClickStop();
                this.base(obj);
            },

            /**
             * overwrite
             * 
             * @method errorValue if an note is not succeed send and an error is fired, reset the
             *         clickstop
             */
            errorValue : function(obj) {
                // this.isClickStop = false;
                this.resetClickStop();
                this.endWorking();
                this.base(obj);
            },

            /**
             * @method resetClickStop delay the enabling of the reply button
             */
            resetClickStop : function() {
                var self = this;
                var t = this.widget.configuration.timeoutForSend || 2000;
                setTimeout(function() {
                    self.isClickStop = false;
                }, t);
            },

            /**
             * set the clicked entry and open the input area
             * 
             * @param {object}
             *            viewNode
             * @param {object}
             *            hideNode
             * @param {object}
             *            parentItem
             */
            toggleInput : function(viewNode, hideNode, parentItem) {
                parentItem.siblings().removeClass("cntwSelected");
                parentItem.removeClass("cntwSelected");
                if (viewNode.is(":visible")) {
                    viewNode.hide();
                } else {
                    hideNode.hide();
                    viewNode.show();
                    parentItem.addClass("cntwSelected");
                }
            },

            /**
             * This method resets the writeContainer to default values.
             * 
             */
            reset : function() {
                var parent;
                var $ = communote.jQuery;
                var field = this.getSubControl("type", "WriteField");
                var selector = this.getSubControl("type", "TopicSelector");
                var tags = this.getSubControl("nodeSelector", ".cntwTagSelector");
                var tagNode = tags.getDomNode();
                var attachment = this.getSubControl("nodeSelector", ".cntwAttachmentSelector");

                this.noteTags = new Array();
                this.tagIdList = new Array();

                field.insertInfoText(true);
                field.setClean();

                if (selector) {
                    selector.noteCreated();
                }
                parent = tagNode.parent();
                $('.cntwTagListContainer *', parent).remove();
                $('.cntwAttachmentListContainer *', parent).remove();
                $('.cntwWriteOptionList LI', parent).removeClass('cntwSelected');
                $('.cntwInfoBox div', parent).click();
                this.detachAutocompleter(tagNode);
                if (this.widget.configuration.edShowTagField) {
                    tagNode.show();
                    $('.cntwWriteOptionList LI .cntwAddTags', parent).parent().addClass(
                            'cntwSelected');
                    $('input', tagNode).val('').blur();
                    this.attachAutocompleter(tagNode);
                } else {
                    tagNode.hide();
                }
                attachment.getDomNode().hide();
                this.addDefaultTags();
            },

            detachAutocompleter : function(node) {
                var input = communote.jQuery(node).find("input");
                input.catcomplete('destroy');
            },

            /**
             * This method adds an categorized autocomplete function for the given field based on
             * rest-api "tagSuggestionList"
             * 
             * @param {object}
             *            node the given input field
             */
            attachAutocompleter : function(node) {
                var container, input, attachToElem, $, self;
                $ = communote.jQuery;

                self = this;
                container = $(node).parent().parent();
                input = $(node).find("input");
                attachToElem = input.parent();

                input.catcomplete({

                    autoFocus : true,
                    position : {
                        of : attachToElem
                    },
                    source : function(request, response) { // CNHC-521
                        self.autocompleteSource(self, request, response, this, input);
                    },

                    /**
                     * handles a tag selection from the autocomplete menu
                     * 
                     * @param {}
                     *            event
                     * @param {}
                     *            ui
                     */

                    select : function(event, ui) {
                        var terms, temp;
                        // explicitly referencing container here is necessary because
                        // otherwise autocomplete would not work in iframe (see modified jQuery UI and KENMEI-3976)
                        var item = ui.item;
                        self.addTag(item, container);

                        terms = input.val();
                        if (item == null) {
                            if (terms.indexOf(",") != -1) {
                                terms = terms.substring(0, terms.lastIndexOf(','));
                                self.createNewTags(terms, container);
                            }
                            terms = '';
                        }
                        if (terms.length > 0) {

                            self.extractTerm(terms, input)

                            var curpos = communote.utils.getCursorPosition(input);

                            var nextCommata = terms.indexOf(',', curpos);
                            if (nextCommata != -1) {
                                temp = terms.substr(0, nextCommata);
                            } else {
                                nextCommata = terms.length;
                                temp = terms;
                            }
                            var beforeCommata = temp.lastIndexOf(',') + 1;
                            terms = communote.jQuery.trim(terms.substr(0, beforeCommata))
                                    + communote.jQuery.trim(terms.substr(nextCommata)).replace(',', '');
                            if (event.originalEvent.originalEvent.type == 'keydown') {
                                event.originalEvent.originalEvent.stopPropagation();
                                // stop keydown event handling
                            }
                        }
                        input.val(terms).focus();
                        event.preventDefault();
                        return false;
                    },

                    /**
                     * handles change event for the completer menu
                     * 
                     */

                    change : function(event, ui) {
                        communote.utils.autocompleter.change(input, event);
                    },

                    /**
                     * this prevent value inserted on focus
                     * 
                     */
                    focus : function() {
                        return false;
                    },

                    /**
                     * handles open event for the completer menu
                     * 
                     */

                    open : function(event, ui) {
                        communote.utils.autocompleter.open(input, 'catcomplete', function() {
                            return input.catcomplete('widget');
                        });

                    }

                });

                /**
                 * handles keydown event for the input field
                 * 
                 */

                input.keydown(function(event) {
                    communote.utils.autocompleter.keydown(event, function() {
                        input.catcomplete("close");
                    }, function() {
                        if (!event.isPropagationStopped()) {
                            // only if event's propagation is running
                            // (stopped when select is previously called)
                            terms = input.val();
                            if (!input.hasClass("cntwEmpty") && (terms != undefined)) {
                                self.createNewTags(terms, container);
                                input.data("catcomplete").term = "";
                                input.val("").blur();
                            }
                        }

                    });
                });

                /**
                 * handles focus lost for the input field
                 * 
                 */

                input.blur(function() {
                    communote.utils.autocompleter.blur(input);
                });
            },

            /**
             * define data source for categorized autocomplete
             * 
             * @param {}
             *            self this
             * @param {}
             *            request
             * @param {}
             *            response
             * @param {}
             *            completer the jQuery autocompleter
             */
            // new feature - activate if needed...
            // maxHeight: 100,
            autocompleteSource : function(self, request, response, completer, input) {
                var selectedTopic, filter, config, topicId;
                config = self.widget.configuration;

                if (config.topicId != undefined) {
                    topicId = config.topicId;
                } else {
                    selectedTopic = self.getSelectedTopic();
                    if (selectedTopic && (selectedTopic.topicId != topicId)) {
                        topicId = selectedTopic.topicId;
                        self.autocompleteCache = {};
                        self.metadataCache = {};
                    }
                }

                completer.metadata = [];

                var term = self.extractTerm(request.term, input);

                if (!term) {
                    response(null);
                    return;
                }
                if (term in self.autocompleteCache) {
                    completer.metadata = self.metadataCache[term];
                    response(self.autocompleteCache[term]);
                    return;
                }

                if (topicId) {
                    filter = {};
                    filter.topicIds = [];
                    filter.topicIds[filter.topicIds.length] = topicId;
                }

                communote.widget.ApiController
                        .getTagSuggestionList(
                                term,
                                function(result, message, channel) {
                                    var suggestionList, tags, tag, i, j, autocompleteTag;
                                    var catcompleteData = [];
                                    var suggestionLists = result.data;
                                    var $ = communote.jQuery;
                                    for (i = 0; i < suggestionLists.length; i++) {
                                        suggestionList = suggestionLists[i];

                                        if ($.inArray(suggestionList.alias,
                                                config.hideSuggestionForAliases) == -1) {
                                            tags = suggestionList.tags;
                                            if (tags == null) {
                                                tags = [];
                                            }

                                            var resultsToShow = tags.length;
                                            var i18nData = {
                                                0 : resultsToShow,
                                                1 : "<strong>" + communote.utils.encodeXml(term)
                                                        + "</strong>"
                                            };
                                            if (tags.length == 20) {
                                                completer.metadata[i] = communote.widget.I18nController
                                                        .getTextFormatted(
                                                                'htmlclient.tags.autocomplete.results.more',
                                                                i18nData);
                                            } else {
                                                completer.metadata[i] = communote.widget.I18nController
                                                        .getTextFormatted(
                                                                'htmlclient.tags.autocomplete.results',
                                                                i18nData);
                                            }

                                            if (tags.length == 0) {
                                                autocompleteTag = {};
                                                autocompleteTag.label = "";
                                                if (($.inArray(suggestionList.alias,
                                                        config.hideSuggestionTitleForAliases) == -1)
                                                        || (suggestionLists.length > 1)) {
                                                    autocompleteTag.category = suggestionList.name;
                                                }
                                                catcompleteData[catcompleteData.length] = autocompleteTag;
                                            } else {
                                                for (j = 0; j < tags.length; j++) {
                                                    tag = tags[j];

                                                    autocompleteTag = {};

                                                    // prepare data for
                                                    // presentation
                                                    if (tag.name != undefined) {
                                                        autocompleteTag.label = tag.name;
                                                    } else {
                                                        autocompleteTag.label = tag.defaultName;
                                                    }
                                                    if (tag.description != undefined) {
                                                        autocompleteTag.context = tag.description;
                                                    }

                                                    if (($.inArray(suggestionList.alias,
                                                            config.hideSuggestionTitleForAliases) == -1)
                                                            || (suggestionLists.length > 1)) {
                                                        autocompleteTag.category = suggestionList.name;
                                                    }

                                                    autocompleteTag.apiTag = tag;
                                                    catcompleteData[catcompleteData.length] = autocompleteTag;
                                                }
                                            }
                                        }
                                    }
                                    // fill cache
                                    self.autocompleteCache[term] = catcompleteData;
                                    self.metadataCache[term] = completer.metadata;

                                    // continue presenting the data
                                    response(catcompleteData);

                                }, this, true, filter, false);

            },

            /**
             * This method splits the specified String object at each commas and returns the object.
             * 
             * @param string
             *            [term] the string object
             * @param {}
             *            [input] the input field
             */
            extractTerm : function(term, input) {
                var curpos, nextCommata, beforeCommata;

                curpos = communote.utils.getCursorPosition(input);
                nextCommata = term.indexOf(',', curpos);
                if (nextCommata != -1) {
                    term = term.substr(0, nextCommata);
                }
                beforeCommata = term.lastIndexOf(',');
                term = communote.jQuery.trim(term.substr(beforeCommata + 1));
                return term;
            },

            /**
             * This method splits the given term at each commas and creates a new wrapped tag object
             * for each substring.
             * 
             * @param {String}
             *            term - the string object
             * @param {HTMLnode}
             *            container - the container, in which the new tags will be added.
             * 
             */
            createNewTags : function(term, container) {
                var name, i, apiItem, item, newTags;

                newTags = term.split(',');
                for (i = 0; i < newTags.length; i++) {
                    name = communote.jQuery.trim(newTags[i]);
                    if (name.length != 0) {
                        apiItem = {};
                        apiItem["defaultName"] = name;
                        apiItem["tagStoreAlias"] = "DefaultNoteTagStore";

                        item = {};
                        item["label"] = name;
                        item["apiTag"] = apiItem;
                        this.addTag(item, container);
                    }
                }
            },

            /**
             * adds a Tag into HTML taglist and to this.noteTags
             * 
             * @param {jQ-object}
             *            item The catcomplete tag object
             * @param {HTMLnode}
             *            container The HTML container (writeContainer or editContainer)
             */
            addTag : function(item, container) {
                var element;
                var $ = communote.jQuery;
                var document = container.ownerDocument || container.context;
                var apiTag = item.apiTag;
                // TODO don't create DOM node ID from tag name because tags can
                // contain characters
                // that are not allowed in ID attributes!
                // Give me an alternative!!
                var id = communote.utils.encodeXml(apiTag.defaultName) + "_" + apiTag.tagStoreAlias;
                var tagList = $('.cntwTagListContainer .cntwTagList', container)[0];
                var noteTags = this.noteTags;
                var idList = this.tagIdList;
                if (!tagList) {
                    $("<li></li>", container).addClass("cntwListLabel").text(
                            this.getLabel('htmlclient.writecontainer.taglistlabel')).appendTo(
                            $("<ul></ul>", container).addClass("cntwTagList").appendTo(
                                    $('.cntwTagListContainer', container)));
                    tagList = $('.cntwTagListContainer .cntwTagList', container)[0];
                }
                if ($.inArray(id, idList) == -1) {
                    idList.push(id);
                    noteTags[noteTags.length] = apiTag;
                    element = $('<li></li>', document).addClass("cntwItem").text(item.label);
                    element.appendTo(tagList);
                    element.click(function() {
                        var storedApiTag, storedId, i;
                        var element = $(this);

                        if (element.siblings().length > 1) {
                            element.remove();
                        } else {
                            element.parent().remove();
                        }

                        for (i = 0; i < noteTags.length; i++) {
                            storedApiTag = noteTags[i];
                            storedId = communote.utils.encodeXml(storedApiTag.defaultName) + "_"
                                    + storedApiTag.tagStoreAlias;
                            if (storedId == id) {
                                noteTags.splice(i, 1);
                                idList.splice($.inArray(id, idList), 1);
                            }
                        }
                    });
                }
            },

            noteServiceSuccessEvent : function(data) {
                this.reset();
                this.endWorking();
            }

        });