/**
 * simple formatting the time (used the UTC-time)
 * 
 * @param {Date}
 *            time - the Date object
 * @returns {String} - Formatted time (00:00)
 */
communote.utils.formatTime = function(time) {
    var timeString, hours, minutes;
    timeString = "";
    hours = time.getUTCHours();
    minutes = time.getUTCMinutes();
    if (hours < 10) {
        timeString += "0";
    }
    timeString += hours + ":";
    if (minutes < 10) {
        timeString += "0";
    }
    timeString += minutes;
    return timeString;
};

/**
 * simple formatting the date (used the UTC-time)
 * 
 * @param {Date}
 *            time - the Date object
 * @returns {String} - Formatted date (dd.mm.yyyy)
 */
communote.utils.formatDate = function(time) {
    var day, month, year;
    year = time.getUTCFullYear();
    month = time.getUTCMonth() + 1;
    month = (month < 10 ? "0" : "") + month;
    day = time.getUTCDay() + 1;
    day = (day < 10 ? "0" : "") + day;
    return day + "." + month + "." + year;
};

/**
 * Encodes XML reserved characters into their entity representation.
 * 
 * @param {String}
 *            text - The String to encode
 * @returns {String} - the encoded string
 */
communote.utils.encodeXml = function(text) {
    var result = text.replace(/&/g, '&amp;');
    result = result.replace(/</g, '&lt;');
    result = result.replace(/>/g, '&gt;');
    // do not use apos because of parser restrictions
    result = result.replace(/'/g, '&#39;');
    return result.replace(/"/g, '&quot;');
};

communote.utils.getUserFullName = function(user, addAlias) {
    if (this.isUserDeleted(user)) {
        return communote.widget.I18nController.getText('htmlclient.userfilter.user.anonymous');
    } else {
        var fullName = user.firstName || "";
		if(fullName.length > 0 && user.lastName){
			fullName += " ";
		}
		fullName += user.lastName || "";	
		if(fullName.length == 0){
			fullName += user.alias;
		}
        else if (addAlias) {
            fullName += ' (' + user.alias + ')';
        }
        return fullName;
    }
};

communote.utils.isUserDeleted = function(user) {
    return user.alias == null;
};

/**
 * @method printError
 * 
 * @param (string)
 *            message
 * @param (object)
 *            event
 * @return (string)
 */
communote.utils.printError = function(message, exception) {
    // print in console
    var nl = "\n";
    var leadingZero = communote.utils.leadingZero;
    var dt = new Date();
    // 'toISOString()' is an alternative, but ie8 and before don't know this
    var date = dt.getUTCFullYear() + "-" + leadingZero(dt.getUTCMonth()) + "-"
            + leadingZero(dt.getUTCDate());
    date += " " + leadingZero(dt.getUTCHours()) + ":" + leadingZero(dt.getUTCMinutes()) + ":"
            + leadingZero(dt.getUTCSeconds());
    var msg = "date:      " + date + nl + "browser:   " + navigator.userAgent + nl + "message:   "
            + message + nl;
    console.log("ERROR" + nl + msg
            + (exception ? ("exception: " + communote.utils.debugObject(exception) + nl) : ""));
    if (exception && exception.message) {
        msg += "exception: " + exception.message + nl;
    }
    return msg;
};

/**
 * @method nl2br replace all "new line" with "<br />"
 * @param (string)
 *            s
 * @return (string)
 */
communote.utils.nl2br = function(s) {
    return s.replace(/\n/g, "<br />");
};

/**
 * @method leadingZero returns a string width leading '0'
 * @param (number|string)
 *            num - number
 * @param (number)
 *            len - optional (default 2), the length of the returned string
 * @return (string)
 */
communote.utils.leadingZero = function(num, len) {
    var s = typeof num;
    if ((s == "number") || (s == "string")) {
        s = "" + num;
        len = len || 2;
        while (s.length < len) {
            s = "0" + s;
        }
    }
    return s;
};

/**
 * @method debugObject iterate over an object and return a string with all members and values
 * @param (object)
 *            param - the printing object
 * @param (string)
 *            offset - leading spaces
 */
communote.utils.debugObject = function(param, offset) {
    offset = offset || "";
    offset += "    ";
    var s = "";
    if (typeof param == "object") {
        for ( var i in param) {
            s += "\n" + offset + i + ": " + communote.utils.debugObject(param[i], offset);
        }
    } else {
        s += param;
    }
    return s;
};

/**
 * Factory function to add a placeholder to an input element. This function will use the placeholder
 * attribute if supported or provide the functionality with the Placeholder class.
 * 
 * @param {jQuery}
 *            inputElem jQuery wrapper for an input element
 * @param {string}
 *            text The placeholder text
 * @param {object}
 *            options Options to be passed to the Placeholder constructor
 * @return {communote.utils.Placeholder} an instance of the placeholder class if the browser does
 *         not support the placeholder attribute, null otherwise
 */
communote.utils.addPlaceholder = function(inputElem, text, options) {
    var placeholder;
    if (this.html5PlaceholderSupport == undefined) {
        this.html5PlaceholderSupport = inputElem[0].placeholder != undefined;
    }
    if (this.html5PlaceholderSupport) {
        inputElem[0].placeholder = text;
    } else {
        placeholder = new communote.utils.Placeholder(inputElem, text, options);
    }
    return placeholder;
};

/**
 * Placeholder for input fields for browsers that do not support the placeholder attribute.
 */
communote.utils.Placeholder = communote.Base.extend({
    text : '',
    element : null,
    cssClass : 'cntwEmpty',
    focused : false,
    enabled : true,
    refreshOnBlur : true,

    constructor : function(inputElem, text, options) {
        var self;
        this.element = inputElem;
        this.text = text;
        if (options.cssClass) {
            this.cssClass = options.cssClass;
        }
        if (options.refreshOnBlur != undefined) {
            this.refreshOnBlur = options.refreshOnBlur;
        }
        self = this;
        this.element.focus(function() {
            self.onFocus();
        });
        this.element.blur(function() {
            self.onBlur();
        });
    },

    onFocus : function() {
        if (!this.focused) {
            this.focused = true;
            if (this.enabled && this.empty) {
                this.element.removeClass(this.cssClass);
                this.element.val('');
            }
        }
    },
    onBlur : function() {
        this.focused = false;
        if (this.refreshOnBlur) {
            this.refresh();
        }
    },
    refresh : function() {
        var value;
        if (this.enabled) {
            if (!this.focused) {
                value = this.element.val();
                if (value.length == 0) {
                    this.empty = true;
                    this.element.addClass(this.cssClass);
                    this.element.val(this.text);
                } else {
                    this.empty = false;
                    this.element.removeClass(this.cssClass);
                }
            }
        }
    },
    enable : function(refresh) {
        if (!this.enabled) {
            this.enabled = true;
            if (refresh) {
                this.refresh();
            }
        }
    },
    disable : function() {
        this.enabled = false;
    }
});

/**
 * returns the text for a given file size without brackets
 */
communote.utils.formatFileSize = function(size) {
    var count = 0, sizeText, calcSize = size;

    while (calcSize >= 1024) {
        count++;
        calcSize = Math.round(calcSize / 1024);
    }
    switch (count) {
    case 1:
        sizeText = calcSize + ' KB';
        break;
    case 2:
        sizeText = calcSize + ' MB';
        break;
    case 3:
        sizeText = calcSize + ' GB';
        break;
    default:
        sizeText = size + ' byte';

    }
    return sizeText;
};

/**
 * autocompleter util functions
 */

communote.utils.autocompleter = {
    /**
     * this selects the last hovered element, when the mouse leaves the menu
     * 
     * @param {}
     *            [input] the input field
     * @param string
     *            [autocompleteType] the type of a autocompleter (imagecomplete or catcomplete)
     * @param function
     *            [getUL] function, which returns the ul-element of the autocompleter
     */
    open : function(input, autocompleteType, getUL) {
        
        var ul, item, autocomplete, menu, $;
        $ = communote.jQuery;
        input.removeClass('ui-autocomplete-loading');
        if (!input.is(':focus')) {
            return false;
        }

        ul = getUL();
        $('li.ui-menu-item', ul).hover(function() {
            item = ul.find('#ui-active-menuitem').parent();
        }, function(event) {
            autocomplete = input.data(autocompleteType);
            menu = autocomplete.menu;
            menu.activate(event, item);
        });
        return false;
    },

    /**
     * handles keydown event for a autocompleter
     * 
     * @param {}
     *            [input] the input field
     */

    blur : function(input) {
        communote.widget.ApiController.abortAutocompleteRequests();
        input.removeClass('ui-autocomplete-loading');
    },

    /**
     * handles keydown for a autocompleter
     * 
     * @param {}
     *            [event] the keydown event
     * @param function
     *            [defaultHandler] the default callback function
     * @param function
     *            [defaultHandler] the enter callback function
     */

    keydown : function(event, defaultHandler, enterHandler) {
        var $ = communote.jQuery;

        var kc = $.ui.keyCode;

        switch (event.keyCode) {
        case kc.ENTER:
            if (enterHandler && (typeof enterHandler == 'function')) {
                enterHandler();
            }
            break;
        case kc.LEFT:
        case kc.RIGHT:
        case kc.UP:
        case kc.DOWN:
        case kc.END:
        case kc.HOME:
        case kc.PAGE_DOWN:
        case kc.PAGE_UP:
        case kc.TAB:
        case kc.ALT:
        case kc.CONTROL:
        case kc.COMMAND:
        case kc.COMMAND_LEFT:
        case kc.COMMAND_RIGHT:
        case kc.WINDOWS:
        case kc.MENU:
        case 16:// Shift
        case 19:// pause-break
        case 20:// caps-lock
        case 92:// WINDOWS second
        case 112:// F1
        case 113:// F2
        case 114:// F3
        case 115:// F4
        case 116:// F5
        case 117:// F6
        case 118:// F7
        case 119:// F8
        case 123:// F9-F12
        case 144:// num-lock
        case 145:// scroll-lock
            break;
        default:
            if (defaultHandler && (typeof defaultHandler == 'function')) {
                defaultHandler();
            }
            break;

        }
    },

    /**
     * handles a changed focus from a autocomplete menu
     * 
     * @param {}
     *            [input] the input field
     * @param {}
     *            [event] the change event
     */

    change : function(input, event) {
        //CNHC-571 fix focus problems
        //TODO analyze impacts        
//        if (event.cancelable === undefined) {
//            input.focus();
//        }
        return false;
    }
};
/**
 * 
 * @param {}
 *            [input] the input field
 */
communote.utils.getCursorPosition = function(input) {
    var field = (input.jquery) ? input[0] : input;
    var range;
    if (field.createTextRange) {
        range = document.selection.createRange().duplicate()
        range.moveEnd('character', field.value.length)
        if (range.text == '')
            return field.value.length
        return field.value.lastIndexOf(range.text)
    } else {
        return field.selectionStart
    }

};

//For IE8 and earlier version.
if (!Date.now) {
  Date.now = function() {
    return new Date().valueOf();
  }
}
