/**
 * A store for filter parameters.
 */
communote.widget.classes.FilterParameterStore = communote.Base.extend(
/** @lends communote.widget.classes.FilterParameterStore.prototype */	
{
    predefinedExtensibleParams: null,
    predefinedClosedParams: null,
    // merge of the two predefined parameter sets
    predefinedParams: null,
    unresetableParms: null,
    selectedParams: null,
    effectiveParams: null,
    equivalentParamNames: null,
    jQuery: null,

    /**
     * @constructs
     * Creates a new FilterParameterStore. The two objects passed as arguments are expected to be
     * disjoint.
     * 
     * @param {Object} [predefinedExtensibleParams] An object holding a key value mapping from
     *            filter parameter name to parameter value. The parameters are expected to be
     *            strings, primitive numbers or arrays of strings or primitive numbers. The
     *            parameters of this object are included in the result of
     *            #getCurrentFilterParameters but not when calling #getFilterParameter.
     * @param {Object} [predefinedClosedParams] An object holding a key value mapping from filter
     *            parameter name to parameter value. The parameters are expected to be strings,
     *            primitive numbers or arrays of strings or primitive numbers. The parameters of
     *            this object are included in the result of #getCurrentFilterParameters but not when
     *            calling #getFilterParameter. When calling one of the methods that set or append
     *            filter parameters this object is checked whether it contains the parameter name
     *            and when it does, the add operation is canceled if the value is not in this
     *            object.
     * @param {String[][]} [equivalentParams] An array of pairs of filter parameter names. The 2
     *            parts of a pair will be treated as equivalent filters with respect to the filter
     *            effect, that is they filter the same aspect (e.g. topicId and topicAlias both filter
     *            topics). This argument is only relevant for filter parameters defined in
     *            predefinedClosedParams.
     */
    constructor: function(predefinedExtensibleParams, predefinedClosedParams, equivalentParams) {
        var i, namePair;
        // local reference to jQuery
        this.jQuery = communote.jQuery;
        this.unresetableParms = {};
        this.selectedParams = {};
        this.equivalentParamNames = {};
        if (predefinedExtensibleParams) {
            this.predefinedExtensibleParams = this
                    .mergeParameters(null, predefinedExtensibleParams);
        }
        if (predefinedClosedParams) {
            this.predefinedClosedParams = this.mergeParameters(null, predefinedClosedParams);
            // equivalent parameters are only relevant when there are closed parameters
            if (equivalentParams) {
                for (i = 0; i < equivalentParams.length; i++) {
                    // mark param name that is not in closed params as equivalent of the
                    // contained name
                    namePair = equivalentParams[i];
                    if ((predefinedClosedParams[namePair[0]] != null)
                            && (predefinedClosedParams[namePair[1]] == null)) {
                        this.equivalentParamNames[namePair[0]] = namePair[1];
                    } else if ((predefinedClosedParams[namePair[1]] != null)
                            && (predefinedClosedParams[namePair[0]] == null)) {
                        this.equivalentParamNames[namePair[1]] = namePair[0];
                    }
                }
            }
        }
        this.predefinedParams = this.mergeParameters(this.predefinedExtensibleParams,
                this.predefinedClosedParams);
    },

    /**
     * Appends a filter parameter value. In case the filter parameter already exists and is not an
     * array it will be converted into an array. In case it is already an array the value will be
     * appended if it is not yet contained. In case the value is already contained the remove
     * parameter defines whether it will be removed. If paramName refers to one of the
     * #predefinedClosedParams the value will only be processed if it is not in that map.
     * 
     * @param {String} paramName The name of the parameter for which a value should be appended
     * @param {String|number} value The value to append.
     * @param {boolean} remove Whether an existing value is to be removed or left unmodified. This
     *            method uses strict equality when checking for the contained value.
     * @returns {boolean} True if the parameter was changed, false otherwise.
     */
    appendFilterParameter: function(paramName, value, remove) {
        var curVal, changed, oldLen;
        if (value == null) {
            return false;
        }
        // do not allow extending the closed parameters if defined
        if (this.definedAndNotContained(this.predefinedClosedParams, paramName, value)) {
            return false;
        }
        changed = true;
        if (this.selectedParams[paramName] == null) {
            this.selectedParams[paramName] = [ value ];
        } else {
            curVal = this.selectedParams[paramName];
            if (!this.jQuery.isArray(curVal)) {
                if (curVal !== value) {
                    this.selectedParams[paramName] = [ curVal, value ];
                } else {
                    if (remove) {
                        delete this.selectedParams[paramName];
                    } else {
                        changed = false;
                    }
                }
            } else {
                oldLen = curVal.length;
                if (remove) {
                    if (oldLen == this.eraseFromArray(value, curVal).length) {
                        curVal.push(value);
                    }
                } else {
                    if (this.jQuery.inArray(value, curVal) === -1) {
                        curVal.push(value);
                    } else {
                        changed = false;
                    }
                }
            }
        }
        if (changed) {
            this.effectiveParams = null;
        }
        return changed;
    },

    /**
     * Sets the value of a parameter and returns whether the parameter value changed. If paramName
     * refers to one of the #predefinedClosedParams the value will only be processed if it is not in
     * that map.
     * 
     * @param {String} paramName The name of the parameter to set.
     * @param {String|number|Array} value The value to set.
     * @returns {boolean} true if the parameter was changed or replaced, false otherwise
     */
    setFilterParameter: function(paramName, value) {
        // do not allow extending the closed parameters
        if (this.definedAndNotContained(this.predefinedClosedParams, paramName, value)) {
            return false;
        }
        if (this.internalSetParameter(this.selectedParams, paramName, value)) {
            // reset the effective params to force rebuilding it
            this.effectiveParams = null;
            return true;
        }
        return false;
    },

    /**
     * Unsets a filter parameter or removes a value from the parameter if its type is array. This
     * method does not change the unresetable parameters.
     * 
     * @param {String} paramName The name of the parameter to modify.
     * @param {String|number} [value] The value to remove. If provided then the parameter must
     *            contain the value if it is an array or must be set to the value if it is not an
     *            array, otherwise the parameter is not changed. In case this argument is not
     *            provided the parameter is unset.
     * @returns {boolean} True if the parameter was modified, false otherwise.
     */
    unsetFilterParameter: function(paramName, value) {
        var oldLen, newLen, changed;
        // no need to check for the closed params because it is checked when adding values
        var curVal = this.selectedParams[paramName];
        if (curVal == null) {
            return false;
        }
        changed = false;
        if (value == null) {
            delete this.selectedParams[paramName];
            changed = true;
        } else if (this.jQuery.isArray(curVal)) {
            oldLen = curVal.length;
            newLen = this.eraseFromArray(value, curVal).length;
            if (newLen != oldLen) {
                changed = true;
                // remove empty arrays
                if (newLen == 0) {
                    delete this.selectedParams[paramName];
                }
            }
        } else if (curVal == value) {
            delete this.selectedParams[paramName];
            changed = true;
        }
        if (changed) {
            this.effectiveParams = null;
        }
        return changed;
    },

    /**
     * Resets the current filter parameters and, if supplied, sets some new parameters. The changed
     * parameter names are returned. The unresetable parameters are not modified.
     * 
     * @param {Object} [newParams] The new parameters as a key-value mapping to be set.
     * @returns {String[]} the keys of the changed parameters
     */
    resetCurrentFilterParameters: function(newParams) {
        var i, oldParams, oldEffectiveParams, changedParams;
        oldParams = this.selectedParams;
        oldEffectiveParams = this.effectiveParams;
        this.selectedParams = {};
        if (newParams) {
            for (i in newParams) {
                if (!newParams.hasOwnProperty(i)) {
                    continue;
                }
                this.setFilterParameter(i, newParams[i]);
            }
        }
        changedParams = this.compareParameters(oldParams, this.selectedParams);
        if (changedParams.length == 0) {
            this.effectiveParams = oldEffectiveParams;
        } else {
            this.effectiveParams = null;
        }
        
        if(oldEffectiveParams.propertyFilter){
            this.selectedParams.propertyFilter = oldEffectiveParams.propertyFilter;
        }
        
        return changedParams;
    },

    /**
     * Returns the value of the filter parameter. This method will look in the parameters added by
     * the set or append methods. This includes the unresetable filter parameters.
     * 
     * @param {String} paramName The name of the parameter
     * @returns {String, number, Array} the unlinked value of the filter parameter, or null if not
     *          contained
     */
    getFilterParameter: function(paramName) {
        var value = this.selectedParams[paramName];
        // TODO always check unresetable params and then merge?
        if (value == null) {
            value = this.unresetableParms[paramName];
        }
        if (this.jQuery.isArray(value)) {
            value = this.jQuery.merge([], value);
        }
        return value;
    },

    /**
     * Returns the current filter parameters which is the merged result of selected, unresetable and
     * predefined parameters. This method will return a new object if the parameters have changed
     * since the last call.
     * 
     * @returns {Object} The (unlinked) current filter parameters.
     */
    getCurrentFilterParameters: function() {
        var params, name, closedParams, equivalentName;
        if (!this.effectiveParams) {
            params = this.mergeParameters(null, this.predefinedParams);
            // if one of the closed preselected parameters is also in the selected params
            // we have to remove it, because the selected parameters should be interpreted
            // as a subset of the closed parameters. When doing this check we must also
            // consider the parameter names that lead to an equivalent filter effect as
            // the parameter of the closed parameters.
            closedParams = this.predefinedClosedParams;
            if (params && closedParams) {
                for (name in closedParams) {
                    equivalentName = this.equivalentParamNames[name];
                    if ((this.selectedParams[name] != null)
                            || (equivalentName && this.selectedParams[equivalentName])) {
                        delete params[name];
                    }
                }
            }
            params = this.mergeParameters(params, this.unresetableParms);
            params = this.mergeParameters(params, this.selectedParams);
            if (!params) {
                params = {};
            }
            this.effectiveParams = params;
        }
        return this.effectiveParams;
    },

    /**
     * Directly queries the predefined parameters for a given parameter.
     * 
     * @param {String} paramName The name of the parameter to retrieve
     * @returns {String|number|Array} the value of the parameter or undefined
     */
    getPredefinedFilterParameter: function(paramName) {
        var value;
        if (this.predefinedParams) {
            value = this.predefinedParams[paramName];
        }
        return value;
    },
    
    /**
     * Directly queries the unresetable parameters for a given parameter.
     * 
     * @param {String} paramName The name of the parameter to retrieve
     * @returns {String|number|Array} the value of the parameter or undefined
     */
    getUnresetableFilterParameter: function(paramName) {
        var value;
        if (this.unresetableParms) {
            value = this.unresetableParms[paramName];
        }
        return value;
    },

    /**
     * Sets the value of an unresetable parameter and returns whether the parameter value changed.
     * Unresetable in that context means that the parameter will be stored in a way that a call to
     * #resetCurrentFilterParameters will not unset it.
     * 
     * @param {String} paramName The name of the parameter to set.
     * @param {String|number|Array} value The value to set.
     * @returns {boolean} true if the parameter was changed or replaced, false otherwise
     */
    setUnresetableFilterParameter: function(paramName, value) {
        // do not allow extending the closed parameters
        if (this.definedAndNotContained(this.predefinedClosedParams, paramName, value)) {
            return false;
        }
        if (this.internalSetParameter(this.unresetableParms, paramName, value)) {
            // reset the effective params to force rebuilding it
            this.effectiveParams = null;
            return true;
        }
        return false;
    },

    /**
     * Removes an unresetable filter parameter.
     * 
     * @param {String} paramName The name of the parameter to remove.
     * @returns {boolean} true if the parameter was removed, false otherwise
     */
    unsetUnresetableFilterParameter: function(paramName) {
        // no need to check for the closed params because it is checked when adding values
        if (this.unresetableParms[paramName] == null) {
            return false;
        } else {
            delete this.unresetableParms[paramName];
            this.effectiveParams = null;
            return true;
        }
    },

    /**
     * Internal helper to set the value of a parameter and return whether the parameter value
     * changed.
     * 
     * @param {Object} paramsHolder One of the managed parameter holding objects that should be
     *            modified.
     * @param {String} paramName The name of the parameter to set.
     * @param {String|number|Array} value The value to set.
     * @returns {boolean} true if the parameter was changed or replaced, false otherwise
     */
    internalSetParameter: function(paramsHolder, paramName, value) {
        var oldType, newType, changed;
        if ((value == null) || (paramsHolder == null)) {
            return false;
        }
        newType = this.jQuery.type(value);
        if (paramsHolder[paramName] == null) {
            if (newType === 'array') {
                // clone array
                paramsHolder[paramName] = this.jQuery.merge([], value);
            } else {
                paramsHolder[paramName] = value;
            }
            return true;
        }
        oldType = this.jQuery.type(paramsHolder[paramName]);
        changed = oldType != newType;
        if (!changed) {
            if (oldType === 'array') {
                changed = !this.compareArrays(paramsHolder[paramName], value);
            } else {
                changed = paramsHolder[paramName] != value;
            }
        }
        if (changed) {
            if (newType === 'array') {
                paramsHolder[paramName] = this.jQuery.merge([], value);
            } else {
                paramsHolder[paramName] = value;
            }
        }
        return changed;
    },

    /**
     * Tests that the passed parameter holder does not contain the value of a given parameter.
     * 
     * @param {Object} paramsHolder One of the internal parameter storing objects.
     * @param {String} paramName The name of the parameter to test
     * @param {String|number} value The value to check for being contained
     * @returns {boolean} True if the parameter holder exists, contains the given parameter and its
     *          value is not the provided value (strictly equality) or does not contains the
     *          provided value, if the parameter value is an array. In every other case false is
     *          returned
     */
    definedAndNotContained: function(paramsHolder, paramName, value) {
        var curVal, jQuery, result;
        result = false;
        if (paramsHolder) {
            curVal = paramsHolder[paramName];
            if (curVal != undefined) {
                jQuery = this.jQuery;
                if (jQuery.isArray(curVal)) {
                    result = jQuery.inArray(value, curVal) == -1;
                } else {
                    result = curVal !== value;
                }
            }
        }
        return result;
    },

    /**
     * Helper to merge two of the internal parameter holders into a new (unlinked) object. Values of
     * parameters that are in both objects will be joined into arrays that do not contain duplicate
     * values. This method expects the values of the parameters to be strings, primitive numbers or
     * arrays of strings or primitive numbers.
     * 
     * @param {Object} a The first parameter holding object or null. This object will not be
     *            modified.
     * @param {Object} b The second parameter holding object or null. This object will not be
     *            modified.
     * @returns {Object} the merged result or null if both arguments were null
     */
    mergeParameters: function(a, b) {
        var params, paramName, jQuery, firstValue, secondValue;
        var firstValueIsArray, secondValueIsArray;
        jQuery = this.jQuery;
        if ((a == null) || (b == null)) {
            if (a == b) {
                return null;
            }
            params = jQuery.extend(true, {}, a || b);
        } else {
            // clone the first object
            params = this.jQuery.extend(true, {}, a);
            // process all members of second
            for (paramName in b) {
                secondValue = b[paramName];
                secondValueIsArray = jQuery.isArray(secondValue);
                if (a[paramName] == null) {
                    if (secondValueIsArray) {
                        params[paramName] = jQuery.merge([], secondValue);
                    } else {
                        params[paramName] = secondValue;
                    }
                } else {
                    // merge the values
                    firstValue = params[paramName];
                    firstValueIsArray = jQuery.isArray(firstValue);
                    if (firstValueIsArray) {
                        if (secondValueIsArray) {
                            this.mergeArrays(firstValue, secondValue);
                        } else {
                            // only add if not contained
                            if (jQuery.inArray(secondValue, firstValue) === -1) {
                                firstValue.push(secondValue);
                            }
                        }
                    } else {
                        if (secondValueIsArray) {
                            // clone array and add firstValue if not contained
                            params[paramName] = jQuery.merge([], secondValue);
                            if (jQuery.inArray(firstValue, secondValue) === -1) {
                                // doesn't preserve order, but shouldn't matter
                                params[paramName].push(firstValue);
                            }
                        } else if (firstValue !== secondValue) {
                            params[paramName] = [ firstValue, secondValue ];
                        }
                    }
                }
            }
        }
        return params;
    },

    /**
     * Private function to compare two filter parameter maps. The values of the parameters must be
     * Strings, arrays or primitive numbers.
     * 
     * @param {Object} a A key value mapping from parameter name to parameter value
     * @param {Object} b A key value mapping from parameter name to parameter value
     * @returns {String[]} the names of the changed parameters
     */
    compareParameters: function(a, b) {
        var changedParams, aValue, bValue, aType, bType, paramName, changed;
        changedParams = [];
        if (a == b) {
            return changedParams;
        }
        for (paramName in a) {
            if (!a.hasOwnProperty(paramName)) {
                continue;
            }
            changed = false;
            bValue = b[paramName];
            aValue = a[paramName];
            if (bValue) {
                aType = this.jQuery.type(aValue);
                bType = this.jQuery.type(bValue);
                if (bType != aType) {
                    changed = true;
                } else if (aType === 'array') {
                    changed = !this.compareArrays(aValue, bValue);
                } else if (aValue !== bValue) {
                    changed = true;
                }
            } else {
                changed = true;
            }
        }
        if (changed) {
            changedParams.push(paramName);
        }
        // check b for paramNames not in a
        for (paramName in b) {
            if (!b.hasOwnProperty(paramName)) {
                continue;
            }
            if (!a[paramName]) {
                changedParams.push(paramName);
            }
        }
        return changedParams;
    },

    /**
     * Merges values from second into first array if not already contained. Works only for strings
     * and primitive numbers and is based on strict equality.
     * 
     * @param {Array} a The array to fill with values of b
     * @param {Array} b The array to merge into a
     * @returns {Array} the modified array a
     */
    mergeArrays: function(a, b) {
        var i, value;
        var jQuery = this.jQuery;
        for (i = 0; i < b.length; i++) {
            value = b[i];
            if (jQuery.inArray(value, a) === -1) {
                a.push(value);
            }
        }
        return a;
    },

    /**
     * Performs a set like compare of two arrays (i.e. position is ignored) and returns whether they
     * have the same content. The operation is case and type sensitive and is only useful for arrays
     * containing primitive types.
     * 
     * @param {Array} a One of the arrays to compare.
     * @param {Array} b One of the arrays to compare.
     * @returns {boolean} whether the content is the same
     */
    compareArrays: function(a, b) {
        var i;
        var jQuery = this.jQuery;
        var aLength = a.length;
        var equal = aLength == b.length;
        for (i = 0; equal && (i < aLength); i++) {
            if (jQuery.inArray(a[i], b) === -1) {
                equal = false;
            }
        }
        return equal;
    },

    /**
     * Erase a value from an array. If the value is not contained the array won't be modified. This
     * method uses strict equality when checking for the contained value.
     * 
     * @param {String,number} value the value to remove
     * @param {Array} arr The array from which the value should be deleted
     * @returns {Array} The array passed to the method
     */
    eraseFromArray: function(value, arr) {
        var idx = this.jQuery.inArray(value, arr);
        if (idx != -1) {
            arr.splice(idx, 1);
        }
        return arr;
    }
});