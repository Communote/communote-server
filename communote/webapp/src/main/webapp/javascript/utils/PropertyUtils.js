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
        /**
         * Merge a property or an array of properties into an array of properties. Convenience
         * method which delegates to #mergeProperties or #mergeProperty if propertiesToMerge is an
         * array or a single property.
         * 
         * @param {Property[]} properties The array of properties to merge into
         * @param {(Property[]|Property)} propertiesToMerge The property or array of properties to merge
         * @return {Property[]} the updated array of properties
         */
        merge: function(properties, propertiesToMerge) {
        	if (Array.isArray(propertiesToMerge)) {
        		return window.communote.utils.propertyUtils.mergeProperties(properties, propertiesToMerge);
        	} else {
        		return window.communote.utils.propertyUtils.mergeProperty(properties, propertiesToMerge);
        	}
        },
        /**
         * Merge all properties of an array into another array of properties. If the latter already
         * contains a property from the former (w.r.t. group and key) the value will be replaced. All
         * properties which are not yet contained will be added.
         * 
         * @param {Property[]} properties The array of properties to merge into
         * @param {Property[]} propertiesToMerge The array of properties to merge
         * @return {Property[]} the updated array of properties
         */
        mergeProperties: function(properties, propertiesToMerge) {
            var i, l;
            var utils = window.communote.utils.propertyUtils;
            for (i = 0, l = propertiesToMerge.length; i < l; i++) {
                utils.mergeProperty(properties, propertiesToMerge[i]);
            }
            return properties;
        },
        /**
         * Merge a property into an array of properties. If the property with the key group and key
         * already exists in the array its value will be overwritten with that of the property to
         * merge. If the property is not yet contained it will be (cloned and) appended.
         * 
         * @param {Property[]} properties The array of properties to merge into
         * @param {Property} propertyToMerge The property to merge
         * @return {Property[]} the updated array of properties
         */
        mergeProperty: function(properties, propertyToMerge) {
            var i, l, property, clonedProperty;
            for (i = 0, l = properties.length; i < l; i++) {
                property = properties[i];
                if (property.keyGroup === propertyToMerge.keyGroup && property.key === propertyToMerge.key) {
                    property.value = propertyToMerge.value;
                    return properties;
                }
            }
            clonedProperty = {};
            clonedProperty.keyGroup = propertyToMerge.keyGroup;
            clonedProperty.key = propertyToMerge.key;
            clonedProperty.value = propertyToMerge.value;
            properties.push(clonedProperty);
            return properties;
        }
    };
})(this);