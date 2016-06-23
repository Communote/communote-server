var AutocompleterInputField = new Class({
    Implements: [ Options ],
    options: {
        delay: 400,
        multiple: false,
        // provide an element to position the choices next to this element and not to the input element 
        positionSource: null,
        separator: ', ',
        separatorSplit: /\s*[,;]\s*/
    },
    activated: false,
    element: null,
    // RegEx for fixing buggy split method in older IEs
    ieSeparatorSplit: null,
    onKeydownCallback: null,
    onClickCallback: null,
    onValueChangedCallback: null,
    onFocusChangedCallback: null,
    // stores the events attached to the input element
    inputEvents: null,
    offsetParent: null,
    positionSource: null,
    observer: null,

    initialize: function(element, options) {
        this.setOptions(options);
        element = document.id(element);
        this.observer = new Observer(element, this.onValueChanged.bind(this), Object.merge({}, {
            'delay': this.options.delay
        }, this.options.observerOptions));
        if (this.options.positionSource) {
            this.positionSource = document.id(this.options.positionSource);
        } else {
            this.positionSource = element;
        }
        if (this.options.multiple) {
            if (!this.options.separator.test(this.options.separatorSplit)) {
                this.options.separatorSplit = this.options.separator;
            }
            // split function in IE < 9 is not working correctly (strips empty strings)
            if (this.options.separatorSplit && 'a,'.split(/,/).length == 1) {
                if (typeOf(this.options.separatorSplit) === 'string') {
                    this.ieSeparatorSplit = new RegExp(this.options.separatorSplit, 'g');
                } else {
                    this.ieSeparatorSplit = new RegExp(this.options.separatorSplit.source, 'g');
                }
            }
        }
        element.setProperty('autocomplete', 'off');
        this.element = element;
        this.prepareInputEvents();
    },

    /**
     * Prepare and save the event handlers that should be added to the input element. This allows
     * adding and removing them when activating and deactivating the autocompleter.
     */
    prepareInputEvents: function() {
        if (this.inputEvents) {
            return;
        }
        this.inputEvents = [];
        this.inputEvents.push({
            'name': 'keydown',
            'handler': this.onKeydown.bind(this)
        });
        this.inputEvents.push({
            'name': 'click',
            'handler': this.onClick.bind(this)
        });
        // need some custom event handling for IE due to loosing focus when clicking scrollbars
        if (Browser.name === 'ie' && Browser.version <= 9) {
            this.inputEvents.push({
                'name': 'focus',
                'handler': function(event) {
                    if (this.refocusOnBlur) {
                        this.refocusOnBlur = false;
                    } else {
                        this.toggleInputElementFocus.delay(100, this, true);
                    }
                }.bind(this)
            });
            this.inputEvents.push({
                'name': 'blur',
                'handler': function(event) {
                    if (this.refocusOnBlur) {
                        this.element.focus();
                    } else {
                        this.toggleInputElementFocus.delay(100, this, false);
                    }
                }.bind(this)
            });
        } else {
            this.inputEvents.push({
                'name': 'focus',
                'handler': function(event) {
                    this.toggleInputElementFocus.delay(100, this, true);
                }.bind(this)
            });
            this.inputEvents.push({
                'name': 'blur',
                'handler': function(event) {
                    this.toggleInputElementFocus.delay(100, this, false);
                }.bind(this)
            });
        }
    },

    toggleInputElementFocus: function(focused) {
        if (this.focused === focused) {
            return;
        }
        this.focused = focused;
        if (this.onFocusChangedCallback) {
            this.onFocusChangedCallback.call(null, focused);
        }
    },

    isFocused: function() {
        return this.focused;
    },

    onKeydown: function(e) {
        if (!this.activated || !this.onKeydownCallback) {
            return true;
        }
        return this.onKeydownCallback.call(null, e);
    },

    onClick: function(e) {
        if (!this.activated || !this.onClickCallback) {
            return true;
        }
        return this.onClickCallback.call(null, e);
    },

    onValueChanged: function(value) {
        if (this.onValueChangedCallback) {
            this.onValueChangedCallback.call(null, this.getQueryValue());
        }
    },

    getElement: function() {
        return this.element;
    },

    getPositionSource: function() {
        return this.positionSource;
    },

    /**
     * Determine the position at which the suggestions should be shown.
     * 
     * @param {Boolean} relative True if the position should be calculated relatively to the offset
     *            parent of the element the suggestions should be positioned to. This parameter will
     *            be true if the suggestions container is a sibling of that element or is positioned
     *            fixed.
     * @return {Object} an object containing the top and left pixel-coordinates for positioning the
     *         suggestions. These coordinates are expected to be stored in the members 'top' and
     *         'left'. Additionally a 'width' member can be included to align the width of the
     *         suggestions with the element at which the suggestions will be shown. However the
     *         suggestions instance might ignore this member if configured so. Finally a
     *         'rightOffset' value can be contained which holds the number of pixels between the
     *         right edge of the positioning source and its offset parent.
     */
    determinePositionOfSuggestions: function(relative) {
        var coords, offsetParentRight;
        if (!this.focused) {
            return null;
        }
        if (relative && !this.offsetParent) {
            this.offsetParent = this.positionSource.getOffsetParent();
        }
        if (this.offsetParent) {
            offsetParentRight = this.offsetParent.getSize().x;
        } else {
            offsetParentRight = document.getSize().x;
        }
        coords = this.positionSource.getCoordinates(this.offsetParent);
        return {
            left: coords.left,
            rightOffset: offsetParentRight - coords.right,
            top: coords.bottom,
            width: coords.width
        };
    },

    /**
     * Updates the input field the autocompleter is attached to.
     * 
     * @param {String} newValue The new value to set. This method handles the multiple suggestion
     *            mode automatically and inserts the new value at the correct position.
     * @param {boolean} markChange If true the characters that are modified or added when exchanging
     *            the current with new value will be selected in the input field. This leads to a
     *            type-ahead like behavior. This parameter is ignored if cleanup is set to true.
     * @param {boolean} cleanup If true and multiple suggestion mode is enabled the input will be
     *            cleaned by rejoining the parts with the value of options.separator.
     */
    updateInputValue: function(newValue, markChange, cleanup) {
        var finalValue, oldValue, start, end, split, tokens, sep;
        var multiple = this.options.multiple;
        if (multiple) {
            split = this.options.separatorSplit;
            finalValue = this.element.value;
            oldValue = finalValue.substr(this.queryIndex).split(split, 1)[0];
            finalValue = finalValue.substr(0, this.queryIndex) + newValue
                    + finalValue.substr(this.queryIndex + oldValue.length);
            // remove white space sequences
            if (cleanup) {
                tokens = finalValue.split(split).filter(function(entry) {
                    return this.test(entry);
                }, /[^\s,]+/);
                sep = this.options.separator;
                if (tokens.length > 0) {
                    finalValue = tokens.join(sep) + sep;
                } else {
                    finalValue = '';
                }
            }
        } else {
            oldValue = this.element.value;
            finalValue = newValue;
        }
        this.observer.setValue(finalValue);
        // when doing a cleanup don't mark change
        // TODO do we need to collapse selection or this done automatically by changing input value?
        if (markChange && !cleanup) {
            start = oldValue.length;
            end = newValue.length;
            if (newValue.substr(0, start).toLowerCase() != oldValue.toLowerCase()) {
                start = 0;
            }
            if (multiple) {
                start += this.queryIndex;
                end += this.queryIndex;
            }
            this.selectRange(start, end);
        }
    },
    /**
     * Clear the input field.
     */
    clearInput: function() {
        this.observer.setValue('');
        this.queryIndex = 0;
    },

    ignoreCurrentValueChange: function() {
        this.observer.clear();
    },

    getQueryValue: function() {
        var value, queryValue, values, index, toIndex, last;
        value = this.element.value;
        queryValue = value.trim();
        if (this.options.multiple) {
            values = this.splitAtSeparator(value);
            index = this.getSelectedRange().start;
            toIndex = this.splitAtSeparator(value.substr(0, index));
            last = toIndex.length - 1;
            index -= toIndex[last].length;
            queryValue = values[last];
            this.queryIndex = index;
            // in IE the query can end up undefined in multiple mode (split leads to empty array)
            if (!queryValue) {
                queryValue = '';
            }
        }
        return queryValue;
    },

    activate: function() {
        var i, eventDef;
        if (!this.activated) {
            this.observer.resume();
            // add events to input
            for (i = 0; i < this.inputEvents.length; i++) {
                eventDef = this.inputEvents[i];
                this.element.addEvent(eventDef.name, eventDef.handler);
            }
            this.activated = true;
        }
    },

    deactivate: function() {
        var i, eventDef;
        if (this.activated) {
            this.observer.pause();
            // remove events from input
            for (i = 0; i < this.inputEvents.length; i++) {
                eventDef = this.inputEvents[i];
                this.element.removeEvent(eventDef.name, eventDef.handler);
            }
            this.activated = false;
        }
    },

    santinizeCallback: function(callback) {
        if (typeof callback === 'function') {
            return callback;
        }
        return null;
    }.protect(),

    setOnKeydownCallback: function(callback) {
        this.onKeydownCallback = this.santinizeCallback(callback);
    },

    setOnClickCallback: function(callback) {
        this.onClickCallback = this.santinizeCallback(callback);
    },

    setOnValueChangedCallback: function(callback) {
        this.onValueChangedCallback = this.santinizeCallback(callback);
    },
    setOnFocusChangedCallback: function(callback) {
        this.onFocusChangedCallback = this.santinizeCallback(callback);
    },

    /**
     * @return {Object} an object with members 'start' and 'end' that represent the start and end
     *         character positions of the text selection of the element.
     */
    getSelectedRange: function() {
        var pos, range, dup, value, offset;
        var elem = this.element;
        if (elem.selectionStart !== undefined) {
            pos = {
                start: elem.selectionStart,
                end: elem.selectionEnd
            };
        } else {
            pos = {
                start: 0,
                end: 0
            };
            range = elem.getDocument().selection.createRange();
            if (!range || range.parentElement() != elem) {
                return pos;
            }
            dup = range.duplicate();
            if (elem.type == 'text') {
                pos.start = 0 - dup.moveStart('character', -100000);
                pos.end = pos.start + range.text.length;
            } else {
                value = elem.value;
                offset = value.length - value.match(/[\n\r]*$/)[0].length;
                dup.moveToElementText(elem);
                dup.setEndPoint('StartToEnd', range);
                pos.end = offset - dup.text.length;
                dup.setEndPoint('StartToStart', range);
                pos.start = offset - dup.text.length;
            }
        }
        return pos;
    },

    /**
     * Select the text from character position 'start' to 'end' of the input element.
     * 
     * @param {Number} start The character position to start the selection
     * @param {Number} end The character position to end the selection
     */
    selectRange: function(start, end) {
        var diff, range;
        var elem = this.element;
        if (elem.setSelectionRange) {
            elem.focus();
            elem.setSelectionRange(start, end);
        } else {
            diff = elem.value.substr(start, end - start).replace(/\r/g, '').length;
            start = elem.value.substr(0, start).replace(/\r/g, '').length;
            range = elem.createTextRange();
            range.collapse(true);
            range.moveEnd('character', start + diff);
            range.moveStart('character', start);
            range.select();
        }
    },

    splitAtSeparator: function(value) {
        var result, lastIndex, regEx, match;
        // IEs < 9 strip empty strings from result, so we split manually
        if (this.ieSeparatorSplit) {
            result = [];
            lastIndex = 0;
            regEx = this.ieSeparatorSplit;
            match = regEx.exec(value);
            while (match != null) {
                result.push(value.substring(lastIndex, match.index));
                // calculate lastIndex manually because regEx.lastIndex is not cross-browser
                lastIndex = match.index + match[0].length;
                match = regEx.exec(value);
            }
            result.push(value.substring(lastIndex));
        } else {
            result = value.split(this.options.separatorSplit);
        }
        return result;
    }
});