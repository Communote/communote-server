/**
 * @class
 * @augments communote.widget.classes.controls.Control
 */
communote.widget.classes.controls.FilterSummary = communote.widget.classes.controls.Control
        .extend(
/** 
 * @lends communote.widget.classes.controls.FilterSummary.prototype 
 */
{
            name: 'FilterSummary',
            shownFilterItems: null,
            /** additional mapping from tag name to DOM id because tags have no IDs yet */
            tagNameToIdMapping: null,
            jQuery: null,
            /** base number for id generation */
            idBase: -1,
            /** offset for id generation */
            idOffset: 0,

            /**
             * @param id
             * @param config
             * @param widget
             */
            constructor: function(id, config, widget) {
                this.base(id, config, widget);
                this.shownFilterItems = {};
                this.tagNameToIdMapping = {};
                this.filterElementIdPrefix = 'cntwFilter_summary_' + this.id + '_';
                this.jQuery = communote.jQuery;
            },
            
            registerListeners: function() {
                this.base();
                this.listenTo('filterChanged', this.widget.channel);
            },

            bindEvents: function() {
                var self = this;
                var domNode = this.getDomNode();
                this.jQuery('.cntwRemoveFilters', domNode).click(function() {
                    // pass null to remove all filter settings
                    self.fireEvent('filterEvent', self.widget.channel, null);
                });
                domNode.hide();
            },

            filterChanged: function(data) {
                // if data is an array remove all filters because this currently the
                // only situation this type of data is created
                if (this.jQuery.isArray(data)) {
                    this.removeAllFilterElements();
                } else {
                    if (data.paramName === 'topicIds' || data.paramName === 'userIds' || data.paramName === 'tagIds') {
                        this.handleIdBasedFilter(data);
                    } else if (data.paramName === 'noteSearchString'
                            || data.paramName === 'tagPrefix'
                            || data.paramName === 'userSearchString') {
                        this.handleTextFilter(data);
                    }
                }
                this.fireEvent('sizeChanged', this.widget.channel);
            },

            /**
             * Create a new element to add to the summary
             * 
             * @param {String} id The ID attribute of the element
             * @param {String} label The text to be set for the filter summary entry
             * @param {jQuery} domNode jQuery instance of this Controls domNode
             * @returns {jQuery Element} the created element
             */
            createFilterElement: function(id, label, domNode) {
                // create in correct document otherwise Chrome isn't working correctly
                var elem = this.jQuery('<li></li>', domNode[0].ownerDocument);
                elem.addClass('cntwItem cntwActive');
                elem.attr('id', id);
                elem.text(label);
                return elem;
            },
            /**
             * Updates an existing element in the summary
             * 
             * @param {String} id The ID attribute of the element
             * @param {String} label The text to be set for the filter summary entry
             * @param {jQuery} domNode jQuery instance of this Controls domNode
             */
            updateFilterElement: function(id, label, domNode) {
                var elem = domNode.find('#' + id);
                elem.text(label);
            },
            /**
             * Append a filter element to the summary and show the summary if hidden.
             * 
             * @param {jQuery} The element selected via jQuery
             * @param {jQuery} domNode jQuery instance of this Controls domNode
             */
            appendFilterElement: function(elem, domNode) {
                var show;
                var self = this;
                var filterList = domNode.find('.cntwFilterList');
                if (filterList.children().length > 0) {
                    filterList.append('<li class="cntwItemSpacer">+</li>');
                } else {
                    show = true;
                }
                filterList.append(elem);
                // add remove action on click
                elem.click(function() {
                    var data, id;
                    id = self.jQuery(this).attr('id');
                    data = self.shownFilterItems[id];
                    self.fireEvent('filterEvent', self.widget.channel, {
                        paramName: data.paramName,
                        value: data.value,
                        label: data.label,
                        added: false
                    });
                });
                if (show) {
                    domNode.show();
                }
            },
            /**
             * Add or update a filter summary element.
             * 
             * @param {String} id The ID attribute of the element
             * @param {Object} data The event data with members paramName, added, value and
             *            optionally label
             */
            addOrUpdateFilterElement: function(id, data) {
                var elem;
                var domNode = this.getDomNode();
                var label = data.label || data.value;
                if (this.shownFilterItems[id]) {
                    this.shownFilterItems[id].value = data.value;
                    this.shownFilterItems[id].label = label;
                    this.updateFilterElement(id, label, domNode);
                } else {
                    // save for event handling
                    this.shownFilterItems[id] = {
                        paramName: data.paramName,
                        value: data.value,
                        label: label
                    };
                    elem = this.createFilterElement(id, label, domNode);
                    this.appendFilterElement(elem, domNode);
                }
            },

            /**
             * Remove a filter element from the summary
             * 
             * @param {String} id The ID attribute of the element
             */
            removeFilterElement: function(id) {
                var children;
                var domNode = this.getDomNode();
                var filterList = domNode.find('.cntwFilterList');
                var elem = filterList.find('#' + id);
                var spacer = elem.next('.cntwItemSpacer');
                elem.remove();
                spacer.remove();
                children = filterList.children();
                if (children.last().hasClass('cntwItemSpacer')) {
                    children.last().remove();
                }
                if (children.length == 0) {
                    domNode.hide();
                }
                delete this.shownFilterItems[id];
            },

            /**
             * Removes all filter elements from the DOM and clears the internal stores.
             */
            removeAllFilterElements: function() {
                var domNode = this.getDomNode();
                this.jQuery('.cntwFilterList', domNode).empty();
                domNode.hide();
                this.shownFilterItems = {};
                this.tagNameToIdMapping = {};
            },

            /**
             * Update or create entry in filter summary for id based filters like topic IDs or author
             * IDs.
             * 
             * @param {Object} data The event data with members paramName, added, value and label
             */
            handleIdBasedFilter: function(data) {
                var id = this.filterElementIdPrefix + data.paramName + data.value;
                if (data.added) {
                    this.addOrUpdateFilterElement(id, data);
                } else {
                    this.removeFilterElement(id);
                }
            },                      
            /**
             * Update or create entry in filter summary for the text search filters.
             * 
             * @param {Object} data The event data with members paramName, added, value and
             *            optionally label
             */
            handleTextFilter: function(data) {
                var id = this.filterElementIdPrefix + data.paramName;
                if (data.added) {
                    this.addOrUpdateFilterElement(id, data);
                } else {
                    this.removeFilterElement(id);
                }
            },

            /**
             * Generate a unique ID (suffix) based on current timestamp and an offset.
             * 
             * @returns {String} the ID
             */
            generateUniqueId: function() {
                var now = (new Date()).getTime();
                if (this.idBase === now) {
                    this.idOffset++;
                } else {
                    this.idOffset = 0;
                    this.idBase = now;
                }
                return now + '_' + this.idOffset;
            },



            getDirectives: function() {
                var self = this;
                var dir = {
                    '.cntwFilterTitle': function() {
                        return self.getLabel('htmlclient.filtersummary.filteredBy');
                    },
                    '.cntwRemoveFilters': function() {
                        return self.getLabel('htmlclient.filtersummary.removeAllFilters');
                    }
                };
                return dir;
            }
        });
