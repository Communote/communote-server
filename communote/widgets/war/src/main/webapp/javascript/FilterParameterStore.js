// TODO the Listener logic shouldn't be part of this class because the store does not inform
// the listeners (for good reasons: a single FilterEvent can trigger lots of filter parameter
// changes which should be passed to listeners with one call (performance)). Better place
// would be the EventHandler class.
var C_FilterParameterStore = new Class( /** @lends C_FilterParameterStore.prototype */
{
    listener: [],
    currentParams: {},
    /**
     * Key value pairs of parameters that will not be reset when calling #resetCurrentFilterParams.
     * 
     * @private
     */
    currentUnresetableParams: {},

    filteredUnresetableParams: null,
    closedUnresetableParams: null,

    paramSetStorage: {},
    /**
     * Key value pairs of parameters that will not be reset when calling #resetCurrentFilterParams.
     * These parameters will be set in every paramSetStorage entry.
     * 
     * @private
     */
    globalUnresetableParams: {},
    /**
     * Holds the name/ID of the current filter parameter set.
     * 
     * @type String
     */
    currentParamsId: null,
    /**
     * Holds the name/ID of the filter parameter set that was replaced by a call to
     * #loadCurrentFilterParameters
     * 
     * @type String
     */
    previousParamsId: null,

    /**
     * A collection of parameter names. The associated values are arrays that will not be converted
     * into a string in CSV format when the query string is created.
     * 
     * @private
     */
    paramsNotAsCSV: [],

    // TODO add closedParams stuff: array of param names of unresetable parameters that cannot be extended with other param-values, but can be filtered to a subset
    /**
     * Creates a new store.
     * 
     * @param {String[]} [options.unresetableParams] An object with parameters as a key to value
     *            mapping which should be set as unresetable parameters.
     * @param {String[]} [options.filterParams] An object with parameters as a key to value mapping
     *            which should be set as initial filter parameters.
     * @param {String[]} [options.paramsNotAsCSV] A collection of names of parameters whose values
     *            are arrays that should not be rendered in CSV format when the query string is
     *            created.
     * @param {String[]} [options.closedUnresetableParams] A collection of parameter names of the
     *            unresetableParams parameters which should not be extendable, that is,
     *            appendFilterParameterValue or setFilterParameter have no effect if the parameter
     *            name is not in this collection and the value is not among the values of the
     *            unrestable parameter.
     * @param {String[]} [options.filteredUnresetableParams] A collection of parameter names of the
     *            unrestableParams parameters which should be filtered by parameters set via
     *            setFilterParameter or appendFilterParameterValue. So if for example param1 is an
     *            unresetable parameter with value [1,3] and setFilterParameter('param1', 1) is
     *            called an invocation of getCurrentFilterParameters will return {param1: 1}.
     * @constructs
     * @class A class that stores parameters in the form of key value pairs and provides methods to
     *        add, update and remove these parameters easily. These methods should usually only be
     *        called by a {@link C_FilterEventHandler} otherwise the change notification mechanism
     *        isn't working correctly.
     */
    initialize: function(options) {
        var filterParams, paramName;
        if (options) {
            if (options.paramsNotAsCSV) {
                this.paramsNotAsCSV = options.paramsNotAsCSV;
            }
            if (options.closedUnresetableParams) {
                this.closedUnresetableParams = options.closedUnresetableParams;
            }
            if (options.filteredUnresetableParams) {
                this.filteredUnresetableParams = options.filteredUnresetableParams;
            }
            this.currentUnresetableParams = this
                    ._createUnresetableParams(options.unresetableParams);
            filterParams = options.filterParams;
            if (filterParams) {
                for (paramName in filterParams) {
                    if (filterParams.hasOwnProperty(paramName)) {
                        this.setFilterParameter(paramName, filterParams[paramName]);
                    }
                }
            }
        }
        if (!this.filteredUnresetableParams) {
            this.filteredUnresetableParams = [];
        }
        if (!this.closedUnresetableParams) {
            this.closedUnresetableParams = [];
        }
    },

    /**
     * Attaches a listener to be informed about changed parameters.
     * 
     * @param {C_FilterParamListener} listener The listener to add.
     */
    attachListener: function(listener) {
        if (listener.getObservedFilterParameters) {
            if (!this.listener.contains(listener)) {
                this.listener.push(listener);
            }
        }
    },

    /**
     * Removes a listener.
     * 
     * @param {C_FilterParamListener} listener The listener to remove.
     */
    removeListener: function(listener) {
        if (listener != null)
            this.listener.erase(listener);
    },

    /**
     * Appends a filter parameter value. In case the filter parameter already exists and is not an
     * array it will be converted into an array. In case it is already an array the value will be
     * appended if it is not yet contained. In case the value is already contained the remove
     * parameter defines whether it will be removed.
     * 
     * @param {String} paramName The name of the parameter for which a value should be appended.
     * @param {String|Number|Array} value The value to append. In case the value is an array all
     *            elements of the array will be appended if not already contained (type and case
     *            sensitive).
     * @param {boolean} remove Whether an existing value is to be removed or left unmodified.
     * @return {boolean} True if the parameter was changed, false otherwise.
     */
    appendFilterParameterValue: function(paramName, value, remove) {
        var valIsArray, curVal, changed, newVal, i, oldLen;
        if (value == null) {
            return false;
        }
        valIsArray = typeOf(value) === 'array';
        if ((valIsArray || typeOf(value) === 'string') && value.length === 0) {
            return false;
        }
        if (!this.parameterCanBeSet(paramName, value, valIsArray)) {
            return false;
        }
        if (this.currentParams[paramName] == null) {
            if (valIsArray) {
                this.currentParams[paramName] = [];
                this.currentParams[paramName].append(value);
            } else {
                this.currentParams[paramName] = [ value ];
            }
            return true;
        } else {
            curVal = this.currentParams[paramName];
            changed = true;
            if (typeOf(curVal) !== 'array') {
                if (!valIsArray && curVal !== value) {
                    this.currentParams[paramName] = [ curVal, value ];
                } else if (valIsArray) {
                    if (remove) {
                        newVal = ([]).combine(value);
                        this.currentParams[paramName] = newVal;
                        // remove current value if contained
                        if (newVal.length === newVal.erase(curVal).length) {
                            // not contained -> prepend current value
                            newVal.unshift(curVal);
                        } else {
                            if (!newVal.length) {
                                // remove if it got empty
                                delete this.currentParams[paramName];
                            }
                        }
                    } else {
                        newVal = ([ curVal ]).combine(value);
                        this.currentParams[paramName] = newVal;
                        changed = newVal.length != 1;
                    }
                } else {
                    if (remove) {
                        delete this.currentParams[paramName];
                    } else {
                        changed = false;
                    }
                }
            } else {
                if (valIsArray) {
                    if (remove) {
                        // intersect both arrays
                        for (i = 0; i < value.length; i++) {
                            oldLen = curVal.length;
                            curVal.erase(value[i]);
                            if (oldLen == curVal.length) {
                                curVal.push(value[i]);
                            }
                        }
                        if (!curVal.length) {
                            // remove empty array
                            delete this.currentParams[paramName];
                        }
                    } else {
                        oldLen = curVal.length;
                        curVal.combine(value);
                        changed = oldLen != curVal.length;
                    }
                } else {
                    oldLen = curVal.length;
                    if (remove) {
                        curVal.erase(value);
                        if (oldLen === curVal.length) {
                            curVal.push(value);
                        } else if (oldLen === 1) {
                            // remove empty array
                            delete this.currentParams[paramName];
                        }
                    } else {
                        changed = !curVal.contains(value);
                        if (changed) {
                            curVal.push(value);
                        }
                    }
                }
            }
            return changed;
        }
    },

    /**
     * Returns the value of a filter parameter.
     * 
     * @param {String} paramName The key/name of the parameter.
     * @param {Boolean} [excludeUnresetableParams] If true the unresetable parameter value will be
     *            ignored
     * @return {String|boolean|Number|String[]|undefined} The value of the parameter or undefined.
     * 
     */
    getFilterParameter: function(paramName, excludeUnresetableParams) {
        var unresetableValue;
        var value = this.currentParams[paramName];
        if (!excludeUnresetableParams
                && (value == null || this.filteredUnresetableParams.indexOf(paramName) != -1)) {
            unresetableValue = this.currentUnresetableParams[paramName];
        }
        // merge and unlink an array value
        return this._mergeValues(value, unresetableValue);
    },

    /**
     * Returns the number of values of a filter parameter.
     * 
     * @param {String} paramName The key/name of the parameter.
     * @return {number} The number of values of the parameter. The result is null if the parameter
     *         is not stored.
     */
    getFilterParameterValueCount: function(paramName) {
        var curVal = this.getFilterParameter([ paramName ]);
        var paramType = typeOf(curVal);
        var len = 0;
        if (paramType === 'array') {
            len = curVal.length;
        } else if (paramType === 'string') {
            len = 1;
        }
        return len;
    },

    /**
     * Returns the value of an unresetable filter parameter.
     * 
     * @param {String} paramName The key/name of the parameter.
     * @return {String|boolean|Number|String[]|undefined} The value of the parameter or undefined.
     * 
     */
    getUnresetableFilterParameter: function(paramName) {
        var unresetableValue = this.currentUnresetableParams[paramName];
        // unlink an array value
        return this._mergeValues(null, unresetableValue);
    },
    /**
     * Return whether there are currently any filter parameters set.
     * 
     * @param {Boolean} excludeUnresetableParams If true only the resetable parameters will be
     *            checked
     * @return {Boolean} true if there are currently filter parameters
     */
    hasFilterParameters: function(excludeUnresetableParams) {
        // since there are no null, '' or [] values, we can just check for the keys
        var hasParams = Object.getLength(this.currentParams) !== 0;
        if (!hasParams && !excludeUnresetableParams) {
            return Object.getLength(this.currentUnresetableParams) !== 0;
        }
        return hasParams;
    },

    parameterCanBeSet: function(paramName, value, valueIsArray) {
        var unresetableValueIsArray;
        var unresetableValue = this.currentUnresetableParams[paramName];
        // check whether there is a closed, unresetable parameter with name paramName
        if (unresetableValue != null && this.closedUnresetableParams.indexOf(paramName) != -1) {
            // value can only be set if it is the unresetable value or is contained in the value unresetable
            if (valueIsArray == null) {
                valueIsArray = typeOf(value) === 'array';
            }
            unresetableValueIsArray = typeOf(unresetableValue) === 'array';
            if (valueIsArray) {
                if (unresetableValueIsArray) {
                    return this._compareArrays(unresetableValue, value);
                }
                return value.length === 1 && value[0] === unresetableValue;
            } else {
                if (unresetableValueIsArray) {
                    return unresetableValue.indexOf(value) != -1;
                }
                return value === unresetableValue;
            }
        }
        return true;
    },

    /**
     * Sets the value of a parameter and returns whether the parameter value changed. The parameter
     * will be stored in a way that a call to #resetCurrentFilterParameters will not unset it.
     * 
     * @param {String} paramName The name of the parameter to set.
     * @param {String|Number|Array} value The value to set.
     * @return {boolean} true if the parameter was changed or replaced, false otherwise
     */
    setUnresetableFilterParameter: function(paramName, value) {
        // TODO currentParams can now be inconsistent to the closedUnresetableParams! 
        return this._setParameter(paramName, value, this.currentUnresetableParams);
    },

    /**
     * Remove an unresetable parameter if it is set.
     * 
     * @param {String} paramName Name of the unresetable parameter to remove
     * @return {Boolean} true if the parameter was removed, false otherwise
     */
    removeUnresetableFilterParameter: function(paramName) {
        // TODO currentParams can now be inconsistent to the closedUnresetableParams!
        if (this.currentUnresetableParams[paramName]) {
            delete this.currentUnresetableParams[paramName];
            return true;
        }
        return false;
    },

    /**
     * Similar to {@link #setUnresetableFilterParameter} but also sets the parameter for all stored
     * parameter sets. If a new parameter set is created it will have the unresetable parameter too.
     * 
     * @param {String} paramName The name of the parameter to set.
     * @param {String|Number|Array} value The value to set.
     */
    setGlobalUnresetableFilterParameter: function(paramName, value) {
        var i;
        this.setUnresetableFilterParameter(paramName, value);
        for (i in this.paramSetStorage) {
            if (this.paramSetStorage.hasOwnProperty(i)) {
                this.setUnresetableFilterParameterInStoredParamSet(i, paramName, value);
            }
        }
        this.globalUnresetableParams[paramName] = value;
    },

    /**
     * Sets the value of a parameter and returns whether the parameter value changed.
     * 
     * @param {String} paramName The name of the parameter to set.
     * @param {String|number|Array} value The value to set.
     * @return {boolean} true if the parameter was changed or replaced, false otherwise
     */
    setFilterParameter: function(paramName, value) {
        if (this.parameterCanBeSet(paramName, value, null)) {
            return this._setParameter(paramName, value, this.currentParams);
        }
        return false;
    },

    /**
     * Set the parameter if the value is truthy or remove it if it is falsy.
     * 
     * @param {String} paramName The name of the parameter to set or unset
     * @param {String|Boolean} [value] The value of the parameter
     * @return {Boolean} true if the parameter changed, false otherwise
     */
    toggleFilterParameter: function(paramName, value) {
        if (value) {
            return this._setParameter(paramName, value, this.currentParams);
        } else {
            return this.unsetFilterParameter(paramName, null);
        }
    },

    /**
     * Unsets a filter parameter or removes a value from the parameter if its type is array. This
     * method does not change the {@link #currentUnresetableParams}.
     * 
     * @param {String} paramName The name of the parameter to modify.
     * @param {String|number} [value] The value to remove. If provided then the parameter must
     *            contain the value if it is an array or must be set to the value if it is not an
     *            array, otherwise the parameter is not changed. In case this argument is not
     *            provided the parameter is unset.
     * @param {boolean} True if the parameter was modified, false otherwise.
     */
    unsetFilterParameter: function(paramName, value) {
        var idx;
        var curVal = this.currentParams[paramName];
        if (curVal == null) {
            return false;
        }
        if (value == null) {
            delete this.currentParams[paramName];
            return true;
        }
        if (typeOf(curVal) === 'array') {
            idx = curVal.indexOf(value);
            if (idx != -1) {
                if (curVal.length == 1) {
                    delete this.currentParams[paramName]
                } else {
                    curVal.splice(idx, 1);
                }
                return true;
            }
        } else if (curVal == value) {
            delete this.currentParams[paramName];
            return true;
        }
        return false;
    },

    /**
     * Resets the current filter parameters and, if supplied, sets some new parameters. The changed
     * parameters are returned. The {@link #currentUnresetableParams} are not modified.
     * 
     * @param {Object} [newParams] The new parameters to be set as a key-value mapping.
     * @return {String[]} the keys of the changed parameters
     */
    resetCurrentFilterParameters: function(newParams) {
        var i;
        var oldParams = this.currentParams;
        this.currentParams = {};
        if (newParams) {
            for (i in newParams) {
                if (!newParams.hasOwnProperty(i)) {
                    continue;
                }
                this.setFilterParameter(i, newParams[i]);
            }
        }
        return this._getChangedParameters(oldParams, this.currentParams);
    },

    /**
     * Completely replace the current filter and unresetable parameters.
     * 
     * @param {Object} [newFilterParams] The new filter parameters to be set as a key-value mapping.
     * @param {Object} [newUnresetableParams] The new unresetable parameters to be set as a
     *            key-value mapping.
     * @return {String[]} the keys of the changed parameters
     */
    replaceCurrentParameters: function(newFilterParams, newUnresetableParams) {
        var changedParams, unresetableParams;
        unresetableParams = this._createUnresetableParams(newUnresetableParams);
        changedParams = this
                ._getChangedParameters(this.currentUnresetableParams, unresetableParams);
        this.currentUnresetableParams = unresetableParams;
        changedParams.combine(this.resetCurrentFilterParameters(newFilterParams));
        return changedParams;
    },

    /**
     * Returns the current filter parameters. Parameters with array values will be converted into a
     * string with comma separated values if they were not excluded by the paramsNotAsCSV option.
     * 
     * @param {String[]} [paramNames] The names of the parameters to include in the result. If not
     *            defined all parameters will be included. If defined paramNamesToExclude will be
     *            ignored.
     * @param {String[]} [paramNamesToExclude] The names of the parameters to exclude from the
     *            result. If not defined all parameters will be included.
     * @return {Object} The (unlinked) current filter parameters.
     */
    getCurrentFilterParameters: function(paramNames, paramNamesToExclude) {
        return this._getCurrentParams(paramNames, paramNamesToExclude, true);
    },

    /**
     * Returns the current unresetable filter parameters. Parameters with array values will be
     * converted into a string with comma separated values if they were not excluded by the
     * paramsNotAsCSV option.
     * 
     * @param {String[]} [paramNames] The names of the parameters to include in the result. If not
     *            defined all parameters will be included. If defined paramNamesToExclude will be
     *            ignored.
     * @param {String[]} [paramNamesToExclude] The names of the parameters to exclude from the
     *            result. If not defined all parameters will be included.
     * @return {Object} The (unlinked) current unresetable filter parameters.
     */
    getCurrentUnrestableFilterParameters: function(paramNames, paramNamesToExclude) {
        var i, paramName;
        var result = {};
        var unresetableParams = this.currentUnresetableParams;
        if (paramNames) {
            for (i = 0; i < paramNames.length; i++) {
                paramName = paramNames[i];
                if (unresetableParams[paramName]) {
                    result[paramName] = this._getUnlinkedParameterValue(paramName,
                            unresetableParams[paramName]);
                }
            }
        } else {
            for (paramName in unresetableParams) {
                if (!paramNamesToExclude || !paramNamesToExclude.contains(paramName)) {
                    result[paramName] = this._getUnlinkedParameterValue(paramName,
                            unresetableParams[paramName]);
                }
            }
        }
        return result;
    },

    /**
     * Creates a query string of the current filter parameters. The filter parameter values are URI
     * encoded.
     * 
     * @param {String[]} [paramNames] The names of the parameters to include in the query string. If
     *            not defined all parameters will be included. If defined paramNamesToExclude will
     *            be ignored.
     * @param {String[]} [paramNamesToExclude] The names of the parameters to exclude when building
     *            the query string. If not defined all parameters will be included.
     * @return {String} The query string of the filter parameters (e.g. userId=10&blogIds=12)
     */
    createQueryString: function(paramNames, paramNamesToExclude) {
        var params = this._getCurrentParams(paramNames, paramNamesToExclude, true);
        var queryString = Hash.toQueryString(params);
        // the function adds empty arrays which will lead to unwanted leading '&'
        if (queryString.charAt(0) == '&') {
            queryString = queryString.substring(1);
        }
        return queryString;
    },

    /**
     * Informs all listeners of the group about changed parameters. A listener will only be informed
     * about the subset of the changed parameters it defines as being observed.
     * 
     * @param {String[]} changedParams A collection of names of parameters whose values changed.
     * @param {FilterParameterListener[]} [listenerSubset] Optional subset of listener to only
     *            inform those
     */
    informListener: function(changedParams, listenerSubset) {
        var i, j, cp, listeners, listener, observedParams;
        if (!changedParams || !(changedParams.length > 0)) {
            return;
        }
        listeners = this.listener;
        for (i = 0; i < listeners.length; i++) {
            listener = listeners[i];
            if (listenerSubset && !listenerSubset.contains(listener)) {
                continue;
            }
            if (listener.getObservedFilterParameters) {
                observedParams = listener.getObservedFilterParameters();
                if (observedParams && observedParams.length > 0) {
                    // check whether listener is observing all parameters. Only supported if '*' is
                    // the first observed parameter 
                    if (observedParams[0] == '*') {
                        listener.filterParametersChanged(changedParams);
                    } else {
                        // get changed parameters relevant to listener
                        cp = [];
                        for (j = 0; j < changedParams.length; j++) {
                            if (observedParams.contains(changedParams[j]))
                                cp[cp.length] = changedParams[j];
                        }
                        if (cp.length > 0) {
                            listener.filterParametersChanged(cp);
                        }
                    }
                }
            }
        }
    },

    /**
     * Creates a copy of the current filter parameters for later use.
     * 
     * @param {String} id An identifier for retrieving/switching to the stored parameters. In case
     *            there is already a stored parameter set for this ID it will be replaced. The ID
     *            can be null which will result in using the #currentParamsId
     * @param {boolean} setId Whether to store the provided ID as #currentParamsId
     */
    saveCurrentFilterParameters: function(id, setId) {
        if (id == null) {
            id = this.previousParamsId;
        }
        this.paramSetStorage[id] = {
            filterParams: this.currentParams,
            unresetableParams: this.currentUnresetableParams
        };
        if (setId) {
            this.currentParamsId = id;
        }
    },

    /**
     * @param {String} id Identifier of the parameters to load.
     * @param {boolean} returnChangedParams If true the names of the changed filter parameters will
     *            be returned
     * @param {Object} [initParams] Additional parameters (names to value mappings) to be set as
     *            unresetable filter parameters when the parameter set to be loaded does not yet
     *            exist.
     */
    loadCurrentFilterParameters: function(id, returnChangedParams, initParams) {
        this.previousParamsId = this.currentParamsId;
        this.currentParamsId = id;
        var oldFilterParams = this.currentParams;
        var oldUnresetableFilterParams = this.currentUnresetableParams;
        if (this.paramSetStorage[id]) {
            this.currentParams = this.paramSetStorage[id].filterParams;
            this.currentUnresetableParams = this.paramSetStorage[id].unresetableParams;
        } else {
            this.currentParams = {};
            this.currentUnresetableParams = this._createUnresetableParams(initParams);
        }
        if (returnChangedParams) {
            var changedParams = this._getChangedParameters(oldFilterParams, this.currentParams);
            changedParams.combine(this._getChangedParameters(oldUnresetableFilterParams,
                    this.currentUnresetableParams));
            return changedParams;
        }
    },
    _createUnresetableParams: function(initParams) {
        var i;
        var unresetableParams = {};
        if (initParams && typeOf(initParams) === 'object') {
            for (i in initParams) {
                if (initParams.hasOwnProperty(i)) {
                    unresetableParams[i] = initParams[i];
                }
            }
        }
        // apply the global unresetable parameters
        for (i in this.globalUnresetableParams) {
            if (this.globalUnresetableParams.hasOwnProperty(i)) {
                unresetableParams[i] = this.globalUnresetableParams[i];
            }
        }
        return unresetableParams;
    },
    /**
     * Sets the value of a unresetable parameter of one of the stored parameter sets. If there is no
     * stored parameter set that has the provided id a new one will be stored under that id.
     * 
     * @param {String} id Identifier of the stored parameter set to modify.
     * @param {String} paramName The name of the parameter to modify.
     * @param {String|number|Array} value The value to set.
     */
    setUnresetableFilterParameterInStoredParamSet: function(id, paramName, value) {
        var storedParamSet = this.paramSetStorage[id];
        if (!storedParamSet) {
            storedParamSet = {
                filterParams: {},
                unresetableParams: this._createUnresetableParams()
            };
            this.paramSetStorage[id] = storedParamSet;
        }
        this._setParameter(paramName, value, storedParamSet.unresetableParams);
    },

    /**
     * Sets the value of a parameter of one of the stored parameter sets. If there is no stored
     * parameter set that has the provided id a new one will be stored under that id.
     * 
     * @param {String} id Identifier of the stored parameter set to modify.
     * @param {String} paramName The name of the parameter to modify.
     * @param {String|number|Array} value The value to set.
     */
    setFilterParameterInStoredParamSet: function(id, paramName, value) {
        var storedParamSet = this.paramSetStorage[id];
        if (!storedParamSet) {
            storedParamSet = {
                filterParams: {},
                unresetableParams: this._createUnresetableParams()
            };
            this.paramSetStorage[id] = storedParamSet;
        }
        this._setParameter(paramName, value, storedParamSet.filterParams);
    },

    /**
     * Same as getFilterParameter but returns the value of a filter parameter of one of the stored
     * parameter sets. If there is no matching stored parameter set, undefined is returned.
     * 
     * @param {String} id Identifier of the stored parameter set to check
     * @param {String} paramName The key/name of the parameter.
     * @param {Boolean} [excludeUnresetableParams] If true this will not return the fixed
     *            parameters, if there are no others.
     * @return {String|boolean|number|String[]|undefined} The value of the parameter or undefined.
     * 
     */
    getFilterParameterOfStoredParamSet: function(id, paramName, excludeUnresetableParams) {
        var unresetableValue, value;
        var storedParamSet = this.paramSetStorage[id];
        if (!storedParamSet) {
            return undefined;
        }
        value = storedParamSet.filterParams[paramName];
        if (!excludeUnresetableParams) {
            unresetableValue = storedParamSet.unresetableParams[paramName];
        }
        // merge and unlink an array value
        return this._mergeValues(value, unresetableValue);
    },

    /**
     * Resets the filter parameters of one of the stored parameter sets and, if supplied, sets some
     * new parameters. If there is no stored parameter set that has the provided id a new one will
     * be stored under that id.
     * 
     * @param {Object} [newParams] The new parameters as a key-value mapping to be set.
     */
    resetFilterParametersInStoredParamSet: function(id, newParams) {
        var i;
        var storedParamSet = this.paramSetStorage[id];
        if (!storedParamSet) {
            storedParamSet = {
                filterParams: {},
                unresetableParams: this._createUnresetableParams()
            };
            this.paramSetStorage[id] = storedParamSet;
        } else {
            storedParamSet.filterParams = {};
        }
        if (newParams) {
            for (i in newParams) {
                if (!newParams.hasOwnProperty(i))
                    continue;
                this._setParameter(i, newParams[i], storedParamSet.filterParams);
            }
        }
    },

    /**
     * Merges two values into an array. In case the resulting array has only one value and none of
     * the inputs was an array the result will be the single value.
     * 
     * @param {Array|String|Number} val1 value to be merged with val2
     * @param {Array|String|Number} val2 value to be merged with val1
     * @return {Array|String|Number} the merged result
     */
    _mergeValues: function(val1, val2) {
        var result, flatten = true;
        // if null/undefined return the other and in case of array unlink 
        if (val1 == null || val2 == null) {
            if (val1 == null) {
                result = val2;
            }
            if (val2 == null) {
                result = val1;
            }
            if (typeOf(result) === 'array') {
                // unlink
                result = Array.clone(result);
            }
            return result;
        }
        if (typeOf(val1) !== 'array') {
            val1 = [ val1 ];
        } else {
            val1 = Array.clone(val1);
            flatten = false;
        }
        if (typeOf(val2) !== 'array') {
            val2 = [ val2 ];
        } else {
            flatten = false;
        }
        result = val1.combine(val2);
        if (flatten && result.length == 1) {
            return result[0];
        }
        return result;
    },

    /**
     * Returns the (unlinked) current filter parameters including the unresetable parameters. Normal
     * and unresetable filter parameters with the same name will be merged.
     * 
     * @param {Array} [paramNames] an array of parameter names to consider. If unspecified or empty
     *            all parameters will be considered. If set paramNamesToExclude will not be
     *            evaluated.
     * @param {Array} [paramNamesToExclude] an array of parameter names to exclude. If unspecified
     *            or empty all parameters will be considered.
     * @param {Boolean} convertToCSV whether to convert arrays to a comma separated string when the
     *            parameter name is not contained in the paramsNotAsCSV member
     * @return {Object} the (unlinked) filter parameters
     */
    _getCurrentParams: function(paramNames, paramNamesToExclude, convertToCSV) {
        var i, val, paramName, unresetableValue, result = {};
        var curParams = this.currentParams;
        var curUnresetParams = this.currentUnresetableParams;
        if (paramNames && paramNames.length > 0) {
            for (i = 0; i < paramNames.length; i++) {
                paramName = paramNames[i];
                if (curParams[paramName] != null
                        && this.filteredUnresetableParams.indexOf(paramName) != -1) {
                    // if the parameter should filter, the unresetable value can be ignored
                    unresetableValue = null;
                } else {
                    unresetableValue = curUnresetParams[paramName];
                }
                val = this._mergeValues(curParams[paramName], unresetableValue);
                if (val != null) {
                    result[paramName] = val;
                }
            }
        } else {
            // evaluate all parameters and merge if necessary
            for (i in curParams) {
                if (curParams[i] != null
                        && (!paramNamesToExclude || !paramNamesToExclude.contains(i))) {
                    result[i] = this._mergeValues(null, curParams[i]);
                }
            }
            for (i in curUnresetParams) {
                if (!paramNamesToExclude || !paramNamesToExclude.contains(i)) {
                    if (result[i] == null || this.filteredUnresetableParams.indexOf(i) === -1) {
                        // use only current param if parameter is among the filtering parameters
                        val = this._mergeValues(result[i], curUnresetParams[i]);
                        if (val != null) {
                            result[i] = val;
                        }
                    }
                }
            }
        }
        if (convertToCSV) {
            for (paramName in result) {
                if (typeOf(result[paramName]) === 'array'
                        && !this.paramsNotAsCSV.contains(paramName)) {
                    result[paramName] = result[paramName].join(',');
                }
            }
        }
        return result;
    },

    /**
     * Sets the value of a parameter and returns whether the parameter value changed.
     * 
     * @param {String} paramName The name of the parameter to set.
     * @param {String|number|Array} value The value to set. Null, empty arrays or empty strings are
     *            ignored.
     * @param {Object} paramsHolder One of the managed parameter holding objects that should be
     *            modified.
     * @return {boolean} true if the parameter was changed or replaced, false otherwise
     */
    _setParameter: function(paramName, value, paramsHolder) {
        var oldType, newType, changed;
        if (value == undefined) {
            return false;
        }
        newType = typeOf(value);
        if ((newType === 'array' || newType === 'string') && value.length === 0) {
            return false;
        }
        if (paramsHolder[paramName] == undefined) {
            // TODO clone in case of array?
            paramsHolder[paramName] = value;
            return true;
        }
        oldType = typeOf(paramsHolder[paramName]);
        changed = oldType != newType;
        if (!changed) {
            if (oldType === 'array') {
                changed = !this._compareArrays(paramsHolder[paramName], value);
            } else {
                changed = paramsHolder[paramName] != value;
            }
        }
        if (changed) {
            // TODO clone in case of array?
            paramsHolder[paramName] = value;
        }
        return changed;
    },

    /**
     * Private function to compare two filter parameter maps. The values of the parameters must be
     * Strings, (mootools) Arrays or primitive numbers.
     * 
     * @param {Object} a An object with members that are the parameter names and have the parameter
     *            value as value.
     * @param {Object} b An object with members that are the parameter names and have the parameter
     *            value as value.
     * @return {String[]} the names of the changed parameters
     */
    _getChangedParameters: function(a, b) {
        var paramName, changed;
        var changedParams = [];
        if (a == b) {
            return changedParams;
        }
        for (paramName in a) {
            if (!a.hasOwnProperty(paramName))
                continue;
            changed = false;
            if (!b[paramName] || typeOf(b[paramName]) != typeOf(a[paramName])) {
                changed = true;
            } else if (typeOf(a[paramName]) === 'array') {
                changed = !this._compareArrays(a[paramName], b[paramName]);
            } else {
                if (a[paramName] !== b[paramName])
                    changed = true;
            }
            if (changed)
                changedParams.push(paramName);
        }
        // check b for paramNames not in a
        for (paramName in b) {
            if (!b.hasOwnProperty(paramName))
                continue;
            if (!a[paramName])
                changedParams.push(paramName);
        }
        return changedParams;
    },

    _getUnlinkedParameterValue: function(paramName, value) {
        if (typeOf(value) == 'array') {
            if (this.paramsNotAsCSV.contains(paramName)) {
                value = Array.clone(value);
            } else {
                value = value.join(',');
            }
        }
        return value;
    },
    /**
     * Performs a set like compare of two arrays (i.e. position is ignored) and returns whether they
     * have the same content. The operation is case and type sensitive and is only useful for arrays
     * containing simple types.
     * 
     * @param {Array} a One of the arrays to compare.
     * @param {Array} b One of the arrays to compare.
     * @return {boolean} whether the content is the same
     */
    _compareArrays: function(a, b) {
        var i;
        var equal = a.length == b.length;
        for (i = 0; equal && i < a.length; i++) {
            if (!b.contains(a[i])) {
                equal = false;
            }
        }
        return equal;
    }
});
