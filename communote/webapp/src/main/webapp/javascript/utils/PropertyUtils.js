(function(window) {
    if (!window.communote) {
        window.communote = {};
    }
    if (!window.communote.utils) {
        window.communote.utils = {};
    }
    /**
     * Utilities to work with Communote property resources.
     */
    window.communote.utils.propertyUtils = {
        /**
         * Test whether the given array contains a property described by the fields.
         * 
         * @param {Property[]} properties The array of Property resources to search for a match
         * @param {String} keyGroup The group of the key of the property
         * @param {String} [key] The key of the property. If omitted the property is considered to
         *            be contained if it has the given keyGroup.
         * @param {String} [value] The key of the property. If omitted the property is considered to
         *            be contained if it has the given keyGroup and key.
         */
        containsProperty: function(properties, keyGroup, key, value) {
            var i, l, utils;
            if (properties && keyGroup != undefined) {
                utils = window.communote.utils.propertyUtils;
                for (i = 0, l = properties.length; i < l; i++) {
                    if (utils.matches(properties[i], keyGroup, key, value)) {
                        return true;
                    }
                }
            }
            return false;
        },
        /**
         * Test whether the given property matches the fields. Can be used to test for partial
         * matches, e.g. only keyGroup or keyGroup and key.
         * 
         * @param {Property} property The Property resource to match
         * @param {String} keyGroup The group of the key of the property
         * @param {String} [key] The key of the property. If omitted the property matches if it has
         *            the given keyGroup.
         * @param {String} [value] The key of the property. If omitted the property matches if it
         *            has the given keyGroup and key.
         */
        matches: function(property, keyGroup, key, value) {
            if (property.keyGroup === keyGroup) {
                if (key != undefined) {
                    if (property.key === key) {
                        if (value != undefined) {
                            if (property.value === value) {
                                return true;
                            }
                        } else {
                            return true;
                        }
                    }
                } else {
                    return true;
                }
            }
            return false;
        },
        mergeProperties: function(properties, propertiesToMerge) {
            var i, l;
            var utils = window.communote.utils.propertyUtils;
            for (i = 0, l = propertiesToMerge.length; i < l; i++) {
                utils.mergeProperty(properties, propertiesToMerge[i]);
            }
            return properties;
        },
        mergeProperty: function(properties, propertyToInject) {
            var i, l, property;
            for (i = 0, l = properties.length; i < l; i++) {
                property = properties[i];
                if (property.keyGroup === propertyToInject.keyGroup && property.key === propertyToInject.key) {
                    property.value = propertyToInject.value;
                    return properties;
                }
            }
            properties.push(propertyToInject);
            return properties;
        }
    };
})(this);