/**
 * Abstract control for filtering notes. A filter shows a set of elements that will fire a
 * 'filterEvent' when clicked. This event usually leads to a modification of the shared filter
 * parameters and a refresh of the controls that show filterable data.
 * @class
 * @name communote.widget.classes.controls.Filter
 * @augments communote.widget.classes.controls.ContainerControl
 */
communote.widget.classes.controls.Filter = communote.widget.classes.controls.ContainerControl.extend(
/** 
 * @lends communote.widget.classes.controls.Filter.prototype
 */
{
    /**
     * The name of the attribute of an item in the JSON response that holds a label for the filter
     * element. Subclasses must overwrite it.
     *
     * @type {String}
     */
    labelAttribute: null, 
    /**
     * The name of the attribute of an item in the JSON response that represents the value of the
     * filter parameter. Subclasses must overwrite it.
     *
     * @type {String}
     */
    filterParameterValueAttribute: null,
    /**
     * The name of the filter parameter this filter is modifying. Subclasses must overwrite it.
     *
     * @type {String}
     */
    filterParameterName: null,
    /**
     * Prefix for filter element ID generation. Will be set in constructor.
     *
     * @type {String}
     */
    filterElementIdPrefix: null,
    /**
     * Prefix for filter element ID generation
     *
     * @type {String}
     */
    filterElementNodeIdPrefix: null,
    pureDirective: null,
    /**
     * Whether to highlight the currently selected values of the filterParameterName when
     * rendering.
     *
     * @type {boolean}
     */
    highlightSelectedValues: true,
    /**
     * Whether to XML encode the label attribute before rendering. Subclasses could set this member
     * to false if the label is not user controllable data. This will give a small speed-up when
     * rendering.
     *
     * @type {boolean}
     */
    encodeLabelValue: true,
    /**
     * Number of the elements shown by this filter.
     */
    numberShownFilterElements: 0,
    /**
     * local reference to utils
     */
    utils: null,

    /**
     * @param id
     * @param config
     * @param widget
     */
    constructor: function(id, config, widget) {
        this.base(id, config, widget);
        this.filterElementIdPrefix = 'cntwFilter_' + this.id + '_';
        this.pureDirective = this.createPureDirective();
        this.utils = communote.utils;
    },

    /**
     * overwritten
     */
    registerListeners: function() {
        this.base();
        this.listenTo('filterChanged', this.widget.channel);
    },

    /**
     * overwritten
     */
    bindEvents: function() {
        this.base();
        var $ = communote.jQuery;
        var self = this;
        var list = $('.cntwFilterList', this.getDomNode());
        // use event delegation to reduce the amount of listeners

        list.delegate('li', 'click', function() {
            var eventData, elem, item;
            elem = $(this);
            item = self.currentFilterItems[elem.attr('id')];
            eventData = {};
            eventData.paramName = self.filterParameterName;
            eventData.value = item.value;
            eventData.label = item.label;
            eventData.added = !elem.hasClass('cntwActive');
            self.fireEvent('filterEvent', self.widget.channel, eventData);
            return false;
        });
    },

    /**
     * Called when a filter parameter has changed. This handler will reRender the control.
     *
     * @param {Object|Array} data Object describing the changed filter parameter
     * @see communote.widget.classes.FilterEventHandler
     */
    filterChanged: function(data) {
        // just render new filtered data
        this.reRender();
    },

    /**
     * @override
     */
    reRender: function() {
        // reset the element counter when doing a refresh
        this.numberShownFilterElements = 0;
        this.controls = [];
        this.base();
    },

    /**
     * @override
     */
    includeFilterParameters: function() {
        return true;
    },

    /**
     * overwritten
     */
    errorValue: function(errorObj) {
        var domNode, filter;
        var $ = communote.jQuery;
        errorObj.inline = true;
        this.base(errorObj);
        domNode = this.getDomNode();
        filter = $('.cntwFilter', domNode);
        filter.hide();
    },

    /**
     * Builds a string to be used as ID attribute of a DOM element that represents a filter item.
     * Default implementation just appends the index to the filterElementIdPrefix member.
     *
     * @param {Object} item An entry in the JSON response for the resource of the filter
     */
    createFilterElementId: function(item, index) {
        return this.filterElementIdPrefix + this.numberShownFilterElements;
    },

    /**
     * overwritten
     */
    parseData: function(data) {
        var items, i, item, selectedValues;

        this.currentFilterItems = {};
        items = data[this.resource];
        // don't fetch selected values if not needed
        if (this.highlightSelectedValues) {
            selectedValues = this.widget.getFilterParameterStore().getFilterParameter(
                    this.filterParameterName);
        }
        for (i = 0; i < items.length; i++) {
            this.numberShownFilterElements++;
            item = items[i];
            this.parseDataProcessItem(data, item, selectedValues);
        }
        if (this.titleKey) {
            data.filterTitle = this.getLabel(this.titleKey);
        }
    },

    /**
     * Called by parse data for every item in the result set. Subclasses can use this method to
     * augment the item with additional data.
     *
     * @param {Object} data The complete object passed to parseData
     * @param {Object} item Entry in the result array
     * @param {Array|string} selectedValues The currently selected filter values for the
     *            filterParameterName of this class. Can be null if no if no value is selected or
     *            highlightSelectedValues is set to false.
     */
    parseDataProcessItem: function(data, item, selectedValues) {
        var labelAttribute = this.labelAttribute;
        var $ = communote.jQuery;
        var value = item[this.filterParameterValueAttribute];
        // build an id property
        item.id = this.createFilterElementId(item);
        // add css class for highlighting
        if (selectedValues) {
            if ($.isArray(selectedValues)) {
                if ($.inArray(value.toString(), selectedValues) > -1) {
                    // need space because we use PURE append operator (+)
                    item.highlightClass = ' cntwActive';
                }
            } else if (selectedValues == value) {
                item.highlightClass = ' cntwActive';
            }
        }
        // save data for event handling
        this.currentFilterItems[item.id] = {
            label: item[this.labelAttribute],
            value: value
        };
        // encode reserved XML to avoid XSS
        if (this.encodeLabelValue) {
            item[labelAttribute] = this.utils.encodeXml(item[labelAttribute]);
        }
    },

    /**
     * overwritten
     */
    insertValues: function(data, skipRenderingDone) {
        this.base(data, skipRenderingDone);
        var list;
        if (data[this.resource].length <= 0) {
            list = communote.jQuery('.cntwFilterList', this.getDomNode());
            list.hide();
            list.after('<p class="cntwNoFilter">' + this.getLabel('htmlclient.filter.noResultForFilterRequest') + '</p>');
        }
    },

    /**
     * Creates a directive to be passed to the PURE rendering engine.
     *
     * @returns {Object} a directive
     */
    createPureDirective: function() {
        var itemSelector = '.cntwFilterList .cntwItem';
        var dir = {
            '.cntwFilterTitle': 'filterTitle'
        };
        var entryDir = {
            '.': 'entry.' + this.labelAttribute,
            '.@id': 'entry.id'
        };
        dir[itemSelector] = {};
        if (this.highlightSelectedValues) {
            entryDir['.@class+'] = 'entry.highlightClass';
        }
        dir[itemSelector]['entry<-' + this.resource] = entryDir;
        return dir;
    },

    /**
     * @override
     */
    getDirectives: function() {
        return this.pureDirective;
    },

    /**
     * @method  getFullHeight
     * @param   (jQ-object) element
     * @param   (integer) numberOfLines
     * @param   (integer) height - optional, height in Pixel
     *
     */
    getFullHeight: function(element, numberOfLines, height) {
        if (typeof height !== "number") {
            var lineHeight = parseInt(element.css("lineHeight"))
                            || (parseInt(element.css("font-size")) || 0) * 1.33
                            || 12;
            height = numberOfLines * lineHeight;
        }
        height += (parseInt(element.css("padding-top")) || 0 )
                        + (parseInt(element.css("padding-bottom")) || 0 ) ;
        height += (parseInt(element.css("margin-top")) || 0 )
                        + (parseInt(element.css("margin-bottom")) || 0 ) ;
        height += (parseInt(element.css("border-top-width")) || 0 )
                        + (parseInt(element.css("border-bottom-width")) || 0 ) ;
    return height;
    }

});
