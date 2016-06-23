(function(namespace, window) {

    function getPixelStyleValue(elem, styleName) {
        var value = elem.getComputedStyle(styleName);
        // ie<9 will return non pixel values if the element has a relative value (%, em, ...) or something like medium or auto 
        // TODO returning 0 in that case is usually not correct, but is there a solution?
        if (value && value.slice(-2) == 'px') {
            // can be a float
            return Math.round(parseFloat(value));
        }
        return 0;
    }
    /**
     * Calculate the offsets to the width of the element. The offsets will include padding, margin
     * and border if required by the box-model.
     */
    function calculateElementWidthOffset(elem) {
        var value = 0;
        var boxSizing = elem.getComputedStyle('box-sizing') || 'content-box';

        if (boxSizing !== 'border-box') {
            if (boxSizing !== 'padding-box') {
                value += getPixelStyleValue(elem, 'padding-left');
                value += getPixelStyleValue(elem, 'padding-right');
            }
            value += getPixelStyleValue(elem, 'border-left-width');
            value += getPixelStyleValue(elem, 'border-right-width');
        }
        value += getPixelStyleValue(elem, 'margin-left');
        value += getPixelStyleValue(elem, 'margin-right');
        return value;
    }

    var TextboxList = new Class({
        Implements: [ Options, Events ],

        options: {
            // character that when typed will lead to adding the content before the current caret
            // position to the list. The itemAdded event will be triggered with the item as payload.
            addItemOnChar: false,
            // whether to add the content of the input as item when the enter key is pressed. When
            // setting this option the value must be 'all' or 'caret' to add the whole content or
            // the content up to the current caret postion to the list
            addItemOnEnter: false,
            // whether duplicate items can be added to the list. If false an item that is already
            // in the list won't be added again. To test for existence the compareItemCallback
            // will be invoked if defined otherwise a simple equals comparison will be done (
            // suitable for string items)
            allowDuplicates: true,
            // function to be called when addItemOnChar or addItemOnEnter is defined to process
            // the string extracted from the input. The callback can work in 2 ways. The first is
            // to convert the string into one or an array of items and return them. These items will
            // be added to the list then. The other way is to call addItem directly after converting
            // the string and return false or null. Therefore the callback is passed the extracted
            // string and run with 'this' set to this instance. The extracted string will always be
            // removed from the input.
            autoAddItemCallback: false,
            // callback that if defined is called before removing an item through keyboard or mouse
            // interaction. If the callback which is invoked with the item to remove returns false
            // the remove is canceled. The callback is called with 'this' set to this instance which
            // allows the callback to remove the item manually via the removeItem method.
            autoRemoveItemCallback: false,
            // function that if defined will be called when allowDuplicates option is set to false.
            // The function will be passed an already existing item and the item to be added and
            // should return true if the items are equal, otherwise false.
            compareItemCallback: false,
            inputCssClass: false,
            itemCssClass: 'textboxlist-classic-look',
            // the number of items that can be added. If the limit is reached the itemLimitReachedAction
            // will be put into action. Values less than 0 are treated as no limitation. The limit
            // can be changed after creation with help of the setLimit method.
            itemLimit: -1,
            // the action to undertake when the itemLimit is reached. Can be false to do nothing,
            // the string 'hide' set the visibility of the input LI to hidden or the string 'disable'
            // to disable the input. Any other string will be interpreted as CSS class that should
            // be applied to the textboxList element.
            itemLimitReachedAction: false,
            // function to be called when the itemLimit is reached or had been reached before but 
            // now isn't anymore. The callback will be passed a boolean denoting whether the limit
            // was reached and is called with 'this' set to this instance.
            itemLimitReachedCallback: false,
            itemRemoveCssClass: 'textboxlist-item-remove',
            itemRemoveTitle: 'remove',
            itemRemoveLabel: '',
            listCssClass: 'textboxlist-classic-look',
            // min width in pixels, if there is not enough space after adding an item the input will
            // get max available size to occupy next line
            minWidth: '40',
            // whether to resize the input if the browser window was resized.
            observeWindowResize: true,
            // callback that if defined is called before adding an item. The callback will be passed
            // the item and should return a string or an element to be added to the list as
            // representation of the item. This callback is required if the items are not strings.
            parseItemCallback: false,
            // number of pixels to add to the width of the input when resizing it to fill the available
            // space. Should usually be set to 1 to compensate rounding issues when using a layout
            // where width styles use the percentage notation. This is necessary because it is not
            // possible to discover in every browser (IE) that rounding occurred. 
            widthOffset: 1
        },

        items: null,
        itemCount: 0,
        element: null,
        textboxList: null,
        listItemFocused: false,
        itemIdBase: '',
        count: 0,
        boundKeydownHandler: null,
        boundKeyupAndClickHandler: null,
        boundKeypressHandler: null,
        inputWidthOffset: 0,
        itemLimit: -1,
        itemLimitReached: false,
        boundResizeHandler: null,

        initialize: function(element, options) {
            var addOnEnter, utils;
            this.element = document.id(element);
            this.setOptions(options);
            addOnEnter = this.options.addItemOnEnter;
            if (addOnEnter && addOnEnter != 'all' && addOnEnter != 'caret') {
                throw 'Unsupported value for option addItemOnEnter. Value must be false, \'all\' or \'caret\'';
            }
            this.items = {};
            this.itemIdBase = 'textboxlist_item_' + String.uniqueID();
            this.textboxList = this.createTextboxList();
            this.resizeInputToFillAvailableSpace();
            if (this.options.observeWindowResize) {
                // TODO add alternative that does not depend on communote.utils?
                utils = communote && communote.utils;
                if (utils && utils.addDebouncedResizeEventHandler) {
                    this.boundResizeHandler = this.resizeInputToFillAvailableSpace.bind(this);
                    utils.addDebouncedResizeEventHandler(this.boundResizeHandler);
                }
            }
            this.setLimit(this.options.itemLimit);
        },

        /**
         * Add an item without firing an event.
         */
        addItem: function(item) {
            this.internalAddItem(item, false);
        },

        acceptInputValue: function(upToCaret) {
            var caretPos, text, itemText, handled;
            var elem = this.element;
            if (upToCaret) {
                caretPos = this.getCaretPosition();
                if (caretPos) {
                    if (caretPos.start == 0 && caretPos.end == 0) {
                        return false;
                    }
                    handled = true;
                    itemText = elem.value;
                    // replace content with cleaned remainder
                    text = itemText.substring(caretPos.end).trim();
                    // text to add is content from 0 to start
                    itemText = itemText.substring(0, caretPos.start).trim();
                }
            } else {
                // check if focused
                if (elem.ownerDocument.activeElement === elem) {
                    handled = true;
                    itemText = elem.value;
                    text = '';
                }
            }
            if (handled) {
                this.internalAddItemsFromString(itemText);
                elem.value = text;
                this.focusInputAndMoveCaretToStart();
                return true;
            }
            return false;
        },

        /**
         * Sum up padding, margins and borders of input and its surrounding LI and safe result in
         * inputWidthOffset member.
         * 
         * @param {Element} inputElem The input element
         * @param {Element} liElem The surrounding LI element
         */
        calculateInputWidthOffset: function(inputElem, liElem) {
            var value = calculateElementWidthOffset(inputElem);
            value += calculateElementWidthOffset(liElem);
            this.inputWidthOffset = value + this.options.widthOffset;
        },

        clearItems: function() {
            var i;
            var itemElems = this.textboxList.getElements('.textboxlist-item');
            for (i = 0; i < itemElems.length; i++) {
                this.internalRemoveListItem(itemElems[i], false, false);
            }
            this.resizeInputToFillAvailableSpace();
        },

        createListItem: function(content) {
            var contentElem, removeElem;
            var elem = new Element('li', {
                'class': 'textboxlist-item'
            });
            elem.set('id', this.itemIdBase + (++this.count));
            if (this.options.itemCssClass) {
                elem.addClass(this.options.itemCssClass);
            }
            elem.addEvent('click', this.onListItemClicked.bind(this));
            if (typeOf(content) == 'element') {
                // TODO wrap in a span?
                contentElem = content;
            } else {
                contentElem = new Element('span', {
                    text: content
                });
            }
            contentElem.addClass('textboxlist-item-content');
            elem.grab(contentElem);
            removeElem = new Element('a', {
                'href': 'javascript:;',
                'title': this.options.itemRemoveTitle,
                'class': this.options.itemRemoveCssClass
            });
            if (this.options.itemRemoveLabel) {
                removeElem.set('text', this.options.itemRemoveLabel);
            }
            removeElem.addEvent('click', this.onRemoveElementClicked.bind(this));
            elem.grab(removeElem);
            return elem;
        },

        createTextboxList: function() {
            var ulElem, liElem;
            ulElem = new Element('ul', {
                'class': 'textboxlist'
            });
            if (this.options.listCssClass) {
                ulElem.addClass(this.options.listCssClass);
            }
            liElem = new Element('li', {
                'class': 'textboxlist-input'
            });
            if (this.options.inputCssClass) {
                liElem.addClass(this.options.inputCssClass);
            }
            ulElem.grab(liElem);
            ulElem.inject(this.element, 'before');
            liElem.grab(this.element);
            this.boundKeydownHandler = this.onInputKeydown.bind(this);
            this.element.addEvent('keydown', this.boundKeydownHandler);
            this.boundKeyupAndClickHandler = this.onInputKeyupOrClicked.bind(this);
            this.element.addEvent('keyup', this.boundKeyupAndClickHandler);
            this.element.addEvent('click', this.boundKeyupAndClickHandler);
            if (this.options.addItemOnChar) {
                this.boundKeypressHandler = this.onInputKeypress.bind(this);
                this.element.addEvent('keypress', this.boundKeypressHandler);
            }
            this.calculateInputWidthOffset(this.element, liElem);
            return ulElem;
        },

        destroy: function() {
            var elem = this.element;
            elem.removeEvent('keydown', this.boundKeydownHandler);
            elem.removeEvent('keyup', this.boundKeyupAndClickHandler);
            elem.removeEvent('click', this.boundKeyupAndClickHandler);
            if (this.boundKeypressHandler) {
                elem.removeEvent('keypress', this.boundKeypressHandler);
            }
            elem.inject(this.textboxList, 'before');
            this.textboxList.destroy();
            this.items = null;
            // TODO only works if the input had no width element style applied
            this.element.setStyle('width', '');
            if (this.boundResizeHandler) {
                communote.utils.removeDebouncedResizeEventHandler(this.boundResizeHandler);
                this.boundResizeHandler = null;
            }
        },

        extractItemContent: function(item) {
            if (this.options.parseItemCallback) {
                return this.options.parseItemCallback.call(null, item);
            } else {
                return item;
            }
        },

        /**
         * Focus the underlying input element of the TextboxList.
         */
        focusInput: function() {
            this.element.focus();
        },

        /**
         * Focus input element and move caret to position 0.
         */
        focusInputAndMoveCaretToStart: function() {
            var range;
            var elem = this.element;
            if (elem.setSelectionRange) {
                elem.focus();
                elem.setSelectionRange(0, 0);
            } else {
                // old IE
                range = elem.ownerDocument.selection.createRange();
                range.move('character', -elem.value.length);
                range.select();
            }
        },

        focusListItem: function(newElem, oldElem) {
            if (oldElem) {
                oldElem.removeClass('textboxlist-item-focused');
            }
            if (newElem) {
                newElem.addClass('textboxlist-item-focused');
                this.listItemFocused = true;
            } else {
                this.listItemFocused = false;
            }
        },

        focusNextListItem: function() {
            var focusedElem = this.getFocusedListItem();
            if (focusedElem) {
                this.focusListItem(focusedElem.getNext('.textboxlist-item'), focusedElem);
            }
        },

        focusPreviousListItem: function() {
            var prevElem;
            var focusedElem = this.getFocusedListItem();
            if (focusedElem) {
                prevElem = focusedElem.getPrevious('.textboxlist-item');
                if (prevElem) {
                    // only remove focus if there is a previous
                    this.focusListItem(prevElem, focusedElem);
                }
            } else {
                prevElem = this.textboxList.getElements('.textboxlist-item');
                if (prevElem.length > 0) {
                    this.focusListItem(prevElem[prevElem.length - 1], null);
                }
            }
        },

        getCaretPosition: function() {
            var range, dup, start, end;
            var elem = this.element;
            if (elem.ownerDocument.activeElement === elem) {
                if (elem.selectionStart !== undefined) {
                    start = elem.selectionStart;
                    end = elem.selectionEnd;
                } else {
                    // old IE
                    range = elem.ownerDocument.selection.createRange();
                    dup = range.duplicate();
                    // get start by moving the start of the duplicate as far left as possible and
                    // recording the amount of characters moved 
                    start = 0 - dup.moveStart('character', -elem.value.length);
                    end = start + range.text.length;
                }
                return {
                    start: start,
                    end: end
                };
            }
            return null;
        },

        getFocusedListItem: function() {
            return this.textboxList.getElement('.textboxlist-item-focused');
        },
        /**
         * @return {Element} return the input element of the TextboxList
         */
        getInputElement: function() {
            return this.element;
        },
        /**
         * Return the ID of an existing item or null if it does not exist.
         * 
         * @param {Object} item The item to get the ID of
         * @return {String} the ID of the item or null if it does not exist
         */
        getItemId: function(item) {
            var existingItem, itemId, existing;
            var callback = this.options.compareItemCallback;
            for (itemId in this.items) {
                if (this.items.hasOwnProperty(itemId)) {
                    existingItem = this.items[itemId];
                    if (callback) {
                        existing = callback.call(null, existingItem, item)
                    } else {
                        existing = (existingItem === item);
                    }
                    if (existing) {
                        return itemId;
                    }
                }
            }
            return null;
        },

        /**
         * @return {Object[]} the items that were added to the textboxlist
         */
        getItems: function() {
            var elemId;
            var result = [];
            for (elemId in this.items) {
                result.push(this.items[elemId]);
            }
            return result;
        },

        handleItemLimit: function() {
            var reached, action;
            reached = (this.itemLimit > -1 && this.itemLimit <= this.itemCount);
            if (this.itemLimitReached != reached) {
                this.itemLimitReached = reached;
                action = this.options.itemLimitReachedAction;
                if (reached) {
                    if (action == 'hide') {
                        this.element.getParent().setStyle('visibility', 'hidden');
                    } else if (action == 'disable') {
                        this.element.setProperty('disabled', 'disabled');
                    } else if (action != false) {
                        this.textboxList.addClass(action);
                    }
                } else {
                    if (action == 'hide') {
                        this.element.getParent().setStyle('visibility', '');
                    } else if (action == 'disable') {
                        this.element.removeProperty('disabled');
                    } else if (action != false) {
                        this.textboxList.removeClass(action);
                    }
                }
                if (this.options.itemLimitReachedCallback) {
                    this.options.itemLimitReachedCallback.call(this, reached);
                }
            }
        },

        internalAddItem: function(item, fireEvent) {
            var content, itemElem;
            if (!this.options.allowDuplicates && this.getItemId(item)) {
                return;
            }
            if (this.itemLimitReached) {
                return;
            }
            content = this.extractItemContent(item);
            itemElem = this.createListItem(content);
            this.items[itemElem.id] = item;
            this.itemCount++;
            this.handleItemLimit();
            itemElem.inject(this.textboxList.getElement('li.textboxlist-input'), 'before');
            this.resizeInputToFillAvailableSpace();
            if (fireEvent) {
                this.fireEvent('itemAdded', item);
            }
        },

        internalAddItemsFromString: function(itemString) {
            var callback, items, i;
            if (itemString.length) {
                callback = this.options.autoAddItemCallback;
                if (callback) {
                    items = callback.call(this, itemString);
                    if (items) {
                        if (typeOf(items) == 'array') {
                            for (i = 0; i < items.length; i++) {
                                this.internalAddItem(items[i], true);
                            }
                        } else {
                            this.internalAddItem(items, true);
                        }
                    }
                } else {
                    this.internalAddItem(itemString, true);
                }
            }
        },

        internalRemoveListItem: function(itemElem, resize, fireEvent) {
            var item;
            if (itemElem) {
                item = this.items[itemElem.id];
                if (this.listItemFocused && itemElem.hasClass('textboxlist-item-focused')) {
                    this.listItemFocused = false;
                }
                delete this.items[itemElem.id];
                itemElem.destroy();
                this.itemCount--;
                this.handleItemLimit();
                if (resize) {
                    this.resizeInputToFillAvailableSpace();
                }
                if (fireEvent) {
                    this.fireEvent('itemRemoved', item);
                }
            }
        },

        /**
         * @return true iff the input element is focused, the caret is at position 0 and the
         *         selection is collapsed
         */
        isCaretAtStart: function() {
            var caretPos = this.getCaretPosition();
            if (caretPos) {
                return (caretPos.start === 0 && caretPos.end === 0);
            }
            return false;
        },

        onInputKeydown: function(event) {
            var atStart;
            var listItemFocused = this.listItemFocused;
            var key = event.key;
            var eventContinuation = true;
            if (key == 'left' || key == 'right' || key == 'backspace' || key == 'delete') {
                atStart = this.isCaretAtStart();
                if (atStart) {
                    if (listItemFocused) {
                        eventContinuation = false;
                        if (key == 'backspace' || key == 'delete') {
                            this.removeFocusedListItem();
                        } else if (key == 'left') {
                            this.focusPreviousListItem();
                        } else {
                            this.focusNextListItem();
                        }
                    } else {
                        if (key == 'backspace' || key == 'left') {
                            eventContinuation = false;
                            this.focusPreviousListItem();
                        }
                    }
                }
            } else if (key == 'enter' && this.options.addItemOnEnter) {
                this.acceptInputValue(this.options.addItemOnEnter == 'caret');
            }
            return eventContinuation;
        },

        onInputKeypress: function(event) {
            var character = String.fromCharCode(event.code);
            if (character === this.options.addItemOnChar) {
                if (this.acceptInputValue(true)) {
                    return false;
                }
            }
            return true;
        },
        onInputKeyupOrClicked: function(event) {
            // remove focus if caret is not at start anymore
            if (this.listItemFocused && !this.isCaretAtStart()) {
                this.focusListItem(null, this.getFocusedListItem());
            }
        },

        onListItemClicked: function(event) {
            var elem = document.id(event.target);
            if (elem.tagName != 'LI') {
                elem = elem.getParent('.textboxlist-item');
            }
            this.focusListItem(elem, this.getFocusedListItem());
            this.focusInputAndMoveCaretToStart();
        },

        onRemoveElementClicked: function(event) {
            var removeElem = document.id(event.target);
            this.removeListItem(removeElem.getParent('li'));
            this.focusInputAndMoveCaretToStart();
            return false;
        },

        refreshContentOfItem: function(item) {
            var content, elem;
            var itemId = this.getItemId(item);
            if (itemId) {
                elem = this.textboxList.getElementById(itemId);
                contentElem = elem.getFirst();
                content = this.extractItemContent(item);
                if (typeOf(content) == 'element') {
                    // TODO wrap in a span?
                    content.addClass('textboxlist-item-content');
                    content.replaces(contentElem);
                } else {
                    contentElem.set('text', content);
                }
                this.resizeInputToFillAvailableSpace();
            }
        },

        removeFocusedListItem: function() {
            this.removeListItem(this.getFocusedListItem());
        },

        /**
         * Remove a previously added item without firing an event.
         */
        removeItem: function(item) {
            var elem, id;
            for (id in this.items) {
                if (this.items[id] === item) {
                    elem = this.textboxList.getElementById(id);
                    this.internalRemoveListItem(elem, true, false);
                }
            }
        },

        removeListItem: function(itemElem) {
            var item;
            if (itemElem) {
                item = this.items[itemElem.id];
                if (this.options.autoRemoveItemCallback) {
                    if (!this.options.autoRemoveItemCallback.call(this, item)) {
                        return;
                    }
                }
                this.internalRemoveListItem(itemElem, true, true);
            }
        },

        resizeInputToFillAvailableSpace: function() {
            var maxAvailable, liElems, offset, itemLiElem;
            var textboxList = this.textboxList;
            var textboxListWidth = textboxList.getSize().x;

            // if element is not visible there will be no width and we can stop
            if (textboxListWidth <= 0) {
                return;
            }
            liElems = textboxList.getElements('li');
            if (liElems.length > 1) {
                // use X position of last item and include its margin on the right
                itemLiElem = liElems[liElems.length - 2];
                offset = itemLiElem.getCoordinates(this.textboxList).right;
                // coordinates do not respect the left border of the UL (bug or css standard?), so add it manually
                offset += getPixelStyleValue(textboxList, 'border-left-width');
                offset += getPixelStyleValue(itemLiElem, 'margin-right');
                offset += getPixelStyleValue(textboxList, 'padding-right');
                offset += getPixelStyleValue(textboxList, 'border-right-width');
                maxAvailable = textboxListWidth - offset - this.inputWidthOffset;
                if (maxAvailable < this.options.minWidth) {
                    maxAvailable = false
                }
            }
            if (!maxAvailable) {
                // no item added yet or maxAvailable was smaller than minWidth -> calculate full available width
                offset = 0 + getPixelStyleValue(textboxList, 'padding-left');
                offset += getPixelStyleValue(textboxList, 'padding-right');
                offset += getPixelStyleValue(textboxList, 'border-left-width');
                offset += getPixelStyleValue(textboxList, 'border-right-width');
                maxAvailable = textboxListWidth - offset - this.inputWidthOffset;
            }
            this.element.setStyle('width', maxAvailable);
        },

        /**
         * Change the number of items that can be added. If the limit is reached the input will be
         * hidden and adding more items will not be possible. Values less than 0 are treated as no
         * limitation. If the current number of items exceeds the new limit, items won't be removed!
         * 
         * @param {Number} itemLimit The new limit to be set.
         */
        setLimit: function(itemLimit) {
            if (itemLimit >= 0) {
                this.itemLimit = itemLimit;
            } else {
                this.itemLimit = -1;
            }
            this.handleItemLimit();
        }

    });
    if (namespace && namespace.addConstructor) {
        namespace.addConstructor('TextboxList', TextboxList);
    } else {
        window.TextboxList = TextboxList;
    }
})(this.runtimeNamespace, this);