(function(namespace) {
    var Placeholder;
    var nativePlaceholder = null;
    
    function nativePlaceholderSupport() {
        if (nativePlaceholder == null) {
            nativePlaceholder = document.createElement('input').placeholder != undefined;
        }
        return nativePlaceholder;
    }
    
    function destroyAll() {
        var i;
        // nothing to do if native
        if (!nativePlaceholderSupport()) {
            for (i = 0; i < this.length; i++) {
                this[i].destroy();
            }
        }
    }
    
    function getMatching(element) {
        var i;
        for (i = 0; i < this.length; i++) {
            if (this[i].element === element) {
                return this[i];
            }
        }
    }

    Placeholder = new Class({

        overtext: null,
        element: null,

        initialize: function(element, options) {
            var defaultOptions;
            this.element = element;
            if (!nativePlaceholderSupport()) {
                defaultOptions = {
                    positionOptions: {
                        offset: {
                            x: 0,
                            y: 0
                        }
                    },
                    textOverride: this.getText()
                };
                Object.merge(defaultOptions, options);
                this.overtext = new CommunoteOverText(element, defaultOptions);
            }
        },

        destroy: function() {
            if (this.overtext) {
                this.overtext.destroy();
            }
        },
        getText: function() {
            return this.element.getProperty('placeholder');
        },
        refresh: function() {
            if (this.overtext) {
                this.overtext.reposition();
            }
        },
        setText: function(newText) {
            this.element.setProperty('placeholder', newText);
            if (this.overtext && this.overtext.text) {
                this.overtext.text.set('html', newText);
            }
        }
    });
    /**
     * Scan for elements matching the given selector to attach a placeholder.
     * 
     * @param {Element, Elements, String} selector The elements to attach a placeholder to. In case
     *            the value is an string the DOM will be searched for matching elements starting at
     *            the context.
     * @param {Element} [context] The element to start at searching for elements to extend with a
     *            placeholder if selector is a string. If omitted the whole document will be
     *            searched.
     * @param {Object} [options] Options to be passed to the Placeholder constructor to override
     *            defaults
     * @return {Placeholder[]} an extended array of found and initialized placeholders. The array
     *         provides the helper methods destroy() to destroy all placeholders in the array and
     *         getPlaceholder(element) to get the placeholder for the given element if contained.
     */
    Placeholder.attach = function(selector, context, options) {
        var elements, i, type;
        var result = [];
        selector = selector || 'input[type=text]';
        type = typeOf(selector);
        if (type == 'string') {
            context = context || document;
            elements = context.getElements(selector);
        } else if (type == 'element') {
            elements = [selector];
        } else {
            elements = selector;
        }
        for (i = 0; i < elements.length; i++) {
            result.push(new Placeholder(elements[i], options));
        }
        result.getPlaceholder = getMatching;
        result.destroy = destroyAll;
        return result;
    };
    if (namespace && namespace.addConstructor) {
        namespace.addConstructor('Placeholder', Placeholder);
        if (!namespace.utils) {
            namespace.utils = {};
        }
        namespace.utils.attachPlaceholders = Placeholder.attach;
    } else {
        window.Placeholder = Placeholder;
    }
})(window.runtimeNamespace);