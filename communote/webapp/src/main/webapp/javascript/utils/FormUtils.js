(function(namespace) {
    var inputTextTypes = [ 'text', 'password', 'number', 'url', 'email', 'range', 'search', 'date',
            'time', 'datetime', 'month', 'week' ];

    function clearError(elem) {
        var sibling = elem.getPrevious();
        if (sibling && sibling.hasClass('cn-error')) {
            sibling.destroy();
            // assume valid error HTML and thus only check for next if the marker element existed
            sibling = elem.getNext();
            if (sibling && sibling.hasClass('cn-error')) {
                sibling.destroy();
            }
        }
    }

    function getElements(elem, context) {
        if (!elem) {
            elem = 'input[type=text]';
        }
        if (typeof elem == 'string') {
            elem = (context || document).getElements(elem);
        } else if (typeOf(elem) == 'element') {
            elem = Array.from(elem);
        }
        return elem;
    }
    function markUnmarkRequired(elems, required) {
        var i, elem, oldRequired;
        for (i = 0; i < elems.length; i++) {
            elem = elems[i];
            // ignore none text-like inputs
            if (elem.tagName !== 'INPUT' || !inputTextTypes.contains(elem.type)) {
                return;
            }
            // NOTE: simple, fast hack, could also add a validation attribute with more checks (i.e. comma-separated value)
            // or use html5. Mootools validation is also an option, but want no full FE validation.
            oldRequired = elem.getProperty('data-cnt-validation-required') === 'true';
            if (oldRequired != required) {
                clearError(elem);
                if (required) {
                    elem.setProperty('data-cnt-validation-required', 'true');
                } else {
                    elem.removeProperty('data-cnt-validation-required');
                }
            }
        }
    }

    function setError(elem, errorMessage) {
        var sibling = elem.getPrevious();
        if (!sibling || !sibling.hasClass('cn-error')) {
            (new Element('div', {
                'class': 'cn-error cn-hidden'
            })).inject(elem, 'before');
        }
        sibling = elem.getNext();
        if (!sibling || !sibling.hasClass('cn-error')) {
            sibling = new Element('div', {
                'class': 'cn-error'
            });
            sibling.set('html',
                    '<span class="cn-error-message"></span><span class="cn-clear"></span>');
            sibling.inject(elem, 'after');
        }
        sibling.getElement('.cn-error-message').set('text', errorMessage);
    }

    function validateRequired(elem) {
        if (elem.tagName === 'INPUT' && inputTextTypes.contains(elem.type)) {
            if (elem.getProperty('data-cnt-validation-required') === 'true') {
                if (!elem.value.length) {
                    setError(elem, getJSMessage('string.validation.empty'));
                    return false;
                }
            }
            clearError(elem);
        }
        return true;
    }

    if (!namespace) {
        namespace = window;
    }
    if (!namespace.utils) {
        namespace.utils = {};
    }
    namespace.utils.formUtils = {

        /**
         * Clear errors from a previous validate.
         * 
         * @param {String|Element|Elements|Element[]} [elem] The elements to clear. If the value is
         *            a string it will be interpreted as a selector that is applied to the sub-tree
         *            of the element provided by the context argument. If omitted all input elements
         *            of type 'text' will be selected within the context.
         * @param {Element} [context] The context to use as start-node when the elem argument is a
         *            string or missing. If omitted document will be used as context.
         */
        clearErrors: function(elem, context) {
            var i;
            var elems = getElements(elem, context);
            if (elems) {
                for (i = 0; i < elems.length; i++) {
                    clearError(elems[i]);
                }
            }
        },

        /**
         * Mark an input element or an array of input elements as required by adding the data-*
         * attribute data-cnt-validation-required with value 'true'. After marking an element that
         * way it will be checked during validation (see validate function).
         * 
         * @param {String|Element|Elements|Element[]} [elem] The elements to mark. Any non-input
         *            elements will be ignored. If the value is a string it will be interpreted as a
         *            selector that is applied to the sub-tree of the element provided by the
         *            context argument. If omitted all input elements of type 'text' will be
         *            selected within the context.
         * @param {Element} [context] The context to use as start-node when the elem argument is a
         *            string or missing. If omitted document will be used as context.
         * @param {Boolean} required True to mark as required, false to remove the required marker
         *            if existing.
         */
        markUnmarkRequired: function(elem, context, required) {
            elem = getElements(elem, context);
            if (elem) {
                markUnmarkRequired(elem, required);
            }
        },

        /**
         * Mark an element, e.g. an input, as erroneous and add the provided errorMessage. The HTML
         * will resemble that of the server-side error handling.
         * 
         * @param {String|Element|Elements|Element[]} [elem] The elements to mark as erroneous. If
         *            the value is a string it will be interpreted as a selector that is applied to
         *            the sub-tree of the element provided by the context argument. If omitted all
         *            input elements of type 'text' will be selected within the context.
         * @param {Element} [context] The context to use as start-node when the elem argument is a
         *            string or missing. If omitted document will be used as context.
         * @param {String} errorMessage The error message to add
         */
        setErrors: function(elem, context, errorMessage) {
            var i;
            var elems = getElements(elem, context);
            if (elems) {
                // no type or tagName checking because the error could be set to an arbitrary
                // container wrapping some inputs
                for (i = 0; i < elems.length; i++) {
                    setError(elems[i], errorMessage);
                }
            }
        },

        /**
         * Validate the values of input elements and show an error if an element is not valid.
         * Currently only checking for missing values of required elements is supported.
         * 
         * @param {String|Element|Elements|Element[]} [elem] The elements to validate. Any non-input
         *            elements will be ignored. If the value is a string it will be interpreted as a
         *            selector that is applied to the sub-tree of the element provided by the
         *            context argument. If omitted all input elements of type 'text' will be
         *            selected within the context.
         * @param {Element} [context] The context to use as start-node when the elem argument is a
         *            string or missing. If omitted document will be used as context.
         * @return {Boolean} True if all elements were valid, false otherwise
         */
        validate: function(elem, context) {
            var elems, i;
            var allValid = true;
            elems = getElements(elem, context);
            if (elems) {
                for (i = 0; i < elems.length; i++) {
                    if (!validateRequired(elems[i])) {
                        allValid = false;
                    }
                }
            }
            return allValid;
        }
    };
})(window.runtimeNamespace);

// TODO add to namespace
/*
 * Password Checker
 */
function updateQualityMeter(passwordElement) {
    var quality = getPasswordStrength(passwordElement);
    setProgressBarValue(quality);
};

// Function taken from Mozilla Code:
// http://lxr.mozilla.org/seamonkey/source/security/manager/pki/resources/content/password.js
function getPasswordStrength(passwordElement) {
    // Here is how we weigh the quality of the password
    // number of characters
    // numbers
    // non-alpha-numeric chars
    // upper and lower case characters

    var pw = passwordElement.value;

    // length of the password
    var pwlength = (pw.length);
    if (pwlength > 5)
        pwlength = 5;

    // use of numbers in the password
    var numnumeric = pw.replace(/[0-9]/g, "");
    var numeric = (pw.length - numnumeric.length);
    if (numeric > 3)
        numeric = 3;

    // use of symbols in the password
    var symbols = pw.replace(/\W/g, "");
    var numsymbols = (pw.length - symbols.length);
    if (numsymbols > 3)
        numsymbols = 3;

    // use of uppercase in the password
    var numupper = pw.replace(/[A-Z]/g, "");
    var upper = (pw.length - numupper.length);
    if (upper > 3)
        upper = 3;

    var pwstrength = ((pwlength * 10) - 20) + (numeric * 10) + (numsymbols * 15) + (upper * 10);

    // make sure we're give a value between 0 and 100
    if (pwstrength < 0) {
        pwstrength = 0;
    }

    if (pwstrength > 100) {
        pwstrength = 100;
    }

    return pwstrength;
}

// progress bar
function setProgressBarValue(value) {
    var _value = (value / 2) - 5;
    if (isNaN(_value) || _value < 0)
        _value = 0;
    var _progressbar = document.getElementById("progressbarArrow");
    _progressbar.style.left = _value + 'px';
};

function cntForm_markChanged(element) {
    element = $(element);
    if (element) {
        element.set('data-cnt-has-changed', true);
    }
}

function cntForm_setValue(element, value) {
    element = $(element);
    if (element) {
        if (!element.get('data-cnt-has-changed')) {
            element.set('value', value);
        }
    }
}
