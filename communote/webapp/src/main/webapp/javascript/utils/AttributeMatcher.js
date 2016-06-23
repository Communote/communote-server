(function(namespace, window) {
    var classesNamespace = ((namespace && namespace.classes) || window);

    function updateAttribute(attributes, name, value) {
        if (attributes[name] != value) {
            attributes[name] = value;
            return true;
        }
        return false;
    }

    /**
     * Create a component that manages a set of attributes and provides advanced methods for testing
     * whether one of the attributes matches a condition. If a value of one of the attributes is
     * modified a callback function can be called.
     * 
     * @param {Object} [initialAttributes] Object with keys and values representing the initial
     *            state of the attributes to manage
     * @param {Function} [changeCallback] A function to call if an attribute is added or modified.
     *            The callback will be passed an array of the names of the changed attributes
     * @class
     */
    classesNamespace.AttributeMatcher = function(initialAttributes, changeCallback) {
        this.attributes = Object.merge({}, initialAttributes);
        this.changeCallback = changeCallback;
    };

    /**
     * Return the value of an attribute.
     * 
     * @param {String} name The name of the attribute
     * @return {Boolean|String|Number} the value or undefined if the attribute is not set
     */
    classesNamespace.AttributeMatcher.prototype.getValue = function(name) {
        return this.attributes[name];
    };

    /**
     * Return true if there is an attribute with the given name that matches the value exactly or if
     * the provided value is an array has a value that is in the array. If the value is not an
     * array, the method works like matchesExactly.
     * 
     * @param {String} name The name of the attribute to test
     * @param {String|Number|Boolean|Array} value The value to test
     * @return {Boolean} if there is a matching attribute
     */
    classesNamespace.AttributeMatcher.prototype.matches = function(name, value) {
        var attributeValue = this.attributes[name];
        if (attributeValue) {
            if (typeOf(value) === 'array') {
                return (value.indexOf(attributeValue) > -1);
            } else {
                // same as matchesExactly
                return value === attributeValue;
            }
        }
        return false;
    };

    /**
     * Return true if all attributes match, that is the matches method returns true for each of the
     * name-value-pairs contained in attributes.
     * 
     * @param {Object[]} attributes Array of objects with members 'name' and 'value'. The members
     *            have the same meaning as the equally named parameters of the matches method.
     * @return true if all attributes match
     */
    classesNamespace.AttributeMatcher.prototype.matchesAll = function(attributes) {
        var i, l;
        if (!attributes) {
            return false;
        }
        for (i = 0, l = attributes.length; i < l; i++) {
            // stop if on returns false
            if (!this.matches(attributes[i].name, attributes[i].value)) {
                return false;
            }
        }

        return true;
    };

    /**
     * Return true if at least one attribute matches, that is the matches method returns true for
     * one of the name-value-pairs contained in attributes.
     * 
     * @param {Object[]} attributes Array of objects with members 'name' and 'value'. The members
     *            have the same meaning as the equally named parameters of the matches method.
     * @return true if all attributes match
     */
    classesNamespace.AttributeMatcher.prototype.matchesAny = function(attributes) {
        var i, l;
        if (attributes) {
            for (i = 0, l = attributes.length; i < l; i++) {
                // stop if on returns true
                if (this.matches(attributes[i].name, attributes[i].value)) {
                    return true;
                }
            }
        }
        return false;
    };

    /**
     * Return true if the condition matches.
     * 
     * @param {Object} condition The condition to test. If the condition has the members 'name' and
     *            'value' the matches method will be invoked. If the condition has the members
     *            'attributes' and 'matchAny' or 'matchAll' the methods matchesAny or matchesAll
     *            will be called respectively. If the condition has the member 'negate' with value
     *            true, the result of the called method will be negated before returning.
     * @return {Boolean} true if the condition matches
     */
    classesNamespace.AttributeMatcher.prototype.matchesCondition = function(condition) {
        var result;
        // simplest form: single attribute defined by name and value
        if (condition.name) {
            result = this.matches(condition.name, condition.value);
        } else {
            // array of attribute objects with name and value
            if (condition.matchAny) {
                result = this.matchesAny(condition.attributes);
            } else {
                result = this.matchesAll(condition.attributes);
            }
        }
        if (condition.negate) {
            return !result;
        }
        return result;
    };

    /**
     * Return whether there is an attribute with the given name that has exactly the provided value.
     * 
     * @param {String} name The name of the attribute to test
     * @param {String|Number|Boolean} value The value to test
     * @return {Boolean} true if the value matches
     */
    classesNamespace.AttributeMatcher.prototype.matchesExactly = function(name, value) {
        return this.attributes[name] === value;
    };

    /**
     * Update an attribute and call the changeCallback if the value changed. The changeCallback will
     * be passed an array containing the name. If the attribute does not exist it will be added and
     * the changeCallback is invoked.
     * 
     * @param {String} name The name of the attribute to update or add
     * @param {String|Number|Boolean} value The value to set
     */
    classesNamespace.AttributeMatcher.prototype.updateAttribute = function(name, value) {
        if (updateAttribute(this.attributes, name, value)) {
            if (this.changeCallback) {
                this.changeCallback.call(null, [ name ]);
            }
        }
    };

    /**
     * Update a collection of attributes and call the changeCallback if any value changed. The
     * changeCallback will be passed an array containing the names of all changed attributes. If an
     * attribute does not exist it will be added and also passed to the changeCallback.
     * 
     * @param {Object[]} attributes Array of objects containing the members 'name' and 'value'
     */
    classesNamespace.AttributeMatcher.prototype.updateAttributes = function(attributes) {
        var i, l, changed;
        if (attributes) {
            changed = [];
            for (i = 0, l = attributes.length; i < l; i++) {
                if (updateAttribute(this.attributes, attributes[i].name, attributes[i].value)) {
                    changed.push(attributes[i].name);
                }
            }
            if (changed.length && this.changeCallback) {
                this.changeCallback.call(null, changed);
            }
        }
    };

})(this.runtimeNamespace, this);