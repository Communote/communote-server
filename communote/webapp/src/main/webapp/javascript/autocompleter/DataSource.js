/**
 * An AutocompleterDataSource is a component of the autocompleter framework which retrieves
 * suggestions.
 * 
 * The DataSource has to implement the following interface:
 * <ul>
 * <li>handlesCategory: function(categoryId) - returns whether the DataSource handles the category.
 * This method is invoked before queryForExtend is called</li>
 * <li>queryForExtend: function(queryValue, category) {} - start a query to extend the given
 * category</li>
 * <li>queryForUpdate: function(queryValue) {} - start a query to update the handled categories</li>
 * <li>resetCaches: function() {} - reset caches if there are any</li>
 * </ul>
 * 
 * Further it has to include the Events MixIn and should support the following events
 * <ul>
 * <li>queryStarting - should be fired when a query for updates or an extend is started and is
 * expected to take a while. The arguments for the event are a flag that denotes whether it is an
 * update or an extend and the category ID (or an array of IDs) that identifies the targeted
 * categories. If this argument is null all categories are targeted. The Suggestions use this event
 * to show loading feedback.</li>
 * <li>queryComplete - should be fired when a query for an update or an extend completed.</li>
 * <li>queryFailed - should be fired when a query for an update or an extend failed.</li>
 * </ul>
 * 
 */

/**
 * Autocompleter data source that retrieves the suggestions via JSON Ajax requests.
 */
var AutocompleterRequestDataSource = new Class({
    Implements: [ Options, Events ],

    options: {
        // data to be sent to the server with each request
        postData: {},
        // options to be passed to the constructor of the JSON request
        ajaxOptions: {
            method: 'GET',
            noCache: true
        },
        // whether to do a request if the query string is empty
        doRequestIfEmpty: false,
        // number of milliseconds the start of an update query should be delayed. If another query is
        // to be started within that timeout the previous one is overridden and the timeout is reset.
        delay: false,
        // defines whether appending characters to the query value will return the same or a
        // smaller result set. If true and a previous request with a query value that started
        // with the same value had no results will lead to ignoring the query and just return an
        // empty result set.
        narrowingQuery: true,
        // request parameter that holds the query string
        queryParam: 'value',
        // request parameter that holds the category ID of the category to refresh. Won't be included if missing.
        categoryParam: false,
        // request parameter to be set to the number of choices of a category
        // when extending it with more results.
        extendOffsetParam: 'offset',
        // callback function to be invoked when a request completed. This function
        // will be passed the isUpdate flag, the ID of the finished category and the
        // response. The callback should return false if no results were found. It will
        // be run in the context of this DataSource instance and thus should fire the queryComplete event.
        requestCompleteCallback: null,
        // callback function to be invoked before sending a request. This function
        // will be passed the request, the object containing the data to be sent and
        // string containing the name of the query parameter. It will be run in the
        // context of this DataSource instance.
        beforeRequestCallback: null
    },
    boundQueryForUpdate: undefined,
    requestDescriptors: undefined,
    url: undefined,
    timeout: undefined,

    initialize: function(url, categories, options) {
        this.setOptions(options);
        if (this.options.requestCompleteCallback
                && typeof this.options.requestCompleteCallback !== 'function') {
            delete this.options.requestCompleteCallback;
        }
        if (this.options.beforeRequestCallback
                && typeof this.options.beforeRequestCallback !== 'function') {
            delete this.options.beforeRequestCallback;
        }
        // use default category if no special categories are given
        if (!categories) {
            categories = [ {
                id: 'default'
            } ];
        } else {
            categories = Array.from(categories);
        }
        this.url = url;
        this.prepareForUpdate(categories);
        if (this.options.delay > 0) {
            this.boundQueryForUpdate = this.doQueryForUpdate.bind(this);
        }
    },

    /**
     * Creates an object that maps each category ID to a request descriptor which holds a JSON
     * request object, a flag to mark a running request as update or extend and an object that holds
     * some pre configured request data that will be merged with the postData object found in the
     * options when the request is sent. This object will contain the categoryParam if defined.
     */
    prepareRequestsPerCategory: function(categories) {
        var i, requestDescrs, categoryId, categoryParam;
        categoryParam = this.options.categoryParam;
        requestDescrs = {};
        for (i = 0; i < categories.length; i++) {
            categoryId = categories[i].id;
            // save request object and whether it is an update or extend for each category ID
            requestDescrs[categoryId] = this.createRequestDescriptorForCategory(categoryId,
                    categoryParam, categoryId, true);
        }
        return requestDescrs;
    },

    createRequest: function(descriptor) {
        var request = new Request.JSON(Object.merge({
            'url': this.url,
            'link': 'cancel'
        }, this.options.ajaxOptions));
        request.addEvent('onComplete', this.requestComplete.bind(this, descriptor));
        request.addEvent('onFailure', this.requestFailed.bind(this, descriptor));
        return request;
    },

    createRequestDescriptorForCategory: function(categoryId, categoryParam, categoryParamValue,
            isUpdate) {
        var descriptor, request, data;
        descriptor = {};
        // lazy request creation
        descriptor.request = false;
        descriptor.isUpdate = isUpdate;
        descriptor.categoryId = categoryId;
        if (categoryParam) {
            data = {};
            data[categoryParam] = categoryParamValue;
            descriptor.data = data;
        }
        return descriptor;
    },

    doQueryForUpdate: function() {
        var categoryId, descr;
        var queryValue = this.currentQueryValue;
        // send request for all categories
        if (queryValue.length || this.options.doRequestIfEmpty) {
            for (categoryId in this.requestDescriptors) {
                descr = this.requestDescriptors[categoryId];
                if (descr.noResultsForQueryValue && this.options.narrowingQuery
                        && queryValue.indexOf(descr.noResultsForQueryValue) == 0) {
                    this.handleResult(true, categoryId, descr.noResultsResponse);
                } else {
                    descr.currentQueryValue = queryValue;
                    this.sendRequest(descr, true, queryValue, null);
                }
            }
        } else {
            for (categoryId in this.requestDescriptors) {
                // just pass null to let the suggestions clear previous choices
                this.handleResult(true, categoryId, null);
            }
        }
    },
    
    /**
     * @override
     */
    handlesCategory: function(categoryId) {
        return !!this.requestDescriptors[categoryId];
    },

    prepareForUpdate: function(categories) {
        // create requests that will be used when doing a full update and extending a category 
        this.requestDescriptors = this.prepareRequestsPerCategory(categories);
    },
    /**
     * @override
     */
    queryForUpdate: function(queryValue) {
        var delay = this.options.delay;
        this.currentQueryValue = queryValue;
        if (delay > 0) {
            clearTimeout(this.timeout);
            this.timeout = setTimeout(this.boundQueryForUpdate, delay); 
        } else {
            this.doQueryForUpdate();
        }
    },

    /**
     * @override
     */
    queryForExtend: function(queryValue, category) {
        var descr, param, data;
        descr = this.requestDescriptors[category.id];
        if (descr) {
            param = this.options.extendOffsetParam;
            if (param) {
                data = {};
                data[param] = category.choices.length;
            }
            this.sendRequest(descr, false, queryValue, data);
        }
    },

    /**
     * @override
     */
    resetCaches: function() {
        var categoryId, descr;
        for (categoryId in this.requestDescriptors) {
            descr = this.requestDescriptors[categoryId];
            if (descr.noResultsForQueryValue) {
                descr.noResultsForQueryValue = false;
                delete descr.noResultsResponse;
            }
        }
        if (this.timeout) {
            clearTimeout(this.timeout);
        }
    },

    handleResult: function(isUpdate, categoryId, response) {
        var callback = this.options.requestCompleteCallback;
        // call the callback that should handle the response in case there is one
        if (callback) {
            return callback.call(this, isUpdate, categoryId, response);
        } else {
            this.fireEvent('queryComplete', [ isUpdate, categoryId, response ]);
            return response != null;
        }
    },

    /**
     * Called when a request has completed successfully. This method delegates to the
     * requestCompleteCallback if it is defined or if the callback is not defined to the
     * handleResult method.
     * 
     * @param {Object} descriptor The descriptor of the request that succeeded
     * @param {Object} response The JSON object of the response
     */
    requestComplete: function(descriptor, response) {
        var isUpdate = descriptor.isUpdate;
        var categoryId = descriptor.categoryId;
        var resultsFound = this.handleResult(isUpdate, categoryId, response);
        if (isUpdate) {
            // consider null return value as results found
            if (resultsFound !== false) {
                descriptor.noResultsForQueryValue = false;
                delete descriptor.noResultsResponse;
            } else {
                descriptor.noResultsForQueryValue = descriptor.currentQueryValue;
                descriptor.noResultsResponse = response;
            }
        }
    },
    /**
     * Called when a request has failed.
     * 
     * @param {Object} descriptor The descriptor of the request that failed
     */
    requestFailed: function(descriptor) {
        this.fireEvent('queryFailed', [ descriptor.isUpdate, descriptor.categoryId ]);
    },
    /**
     * Sends an request to the server. Before the request is sent the callback beforeRequestCallback
     * is triggered if it is defined and the queryStarting event is fired.
     * 
     * @param {Object} requestDescr The descriptor holding details about the request to send.
     * @param {String} [queryValue] The query string to send to the server. This value will be added
     *            as value of the request parameter defined in the options under the key queryParam.
     * @param {Object} [data] Object with key value pairs to be added as additional request
     *            parameters
     */
    sendRequest: function(requestDescr, isUpdate, queryValue, data) {
        var callback;
        var queryParam = this.options.queryParam;
        var postData = Object.clone(this.options.postData);
        if (requestDescr.data) {
            postData = Object.merge(postData, requestDescr.data);
        }
        if (data) {
            postData = Object.merge(postData, data);
        }
        if (queryValue && queryParam) {
            postData[queryParam] = queryValue;
        }
        callback = this.options.beforeRequestCallback;
        if (callback) {
            callback.call(this, requestDescr.request, postData, queryParam);
        }
        if (!requestDescr.request) {
            requestDescr.request = this.createRequest(requestDescr);
        }
        this.fireEvent('queryStarting', [ requestDescr.isUpdate, requestDescr.categoryId ]);
        requestDescr.isUpdate = isUpdate;
        requestDescr.request.send({
            'data': postData
        });
    }

});

/**
 * An autocompleter DataSource that matches the search term against an ordered list of predefined
 * static strings.
 */
var AutocompleterStaticDataSource = new Class({
    Implements: [ Options, Events ],

    options: {
        extendSupported: true,
        // whether to do a case-sensitive match
        caseSensitive: false,
        // whether to match the search term against the start of or everywhere in the strings
        matchFromStart: true,
        // define a function that filters the search term before doing the actual match. This
        // function needs to accept a string argument (the search term) and must return the
        // filtered search term or null. The latter case is a shortcut to return a 'no results'
        // found directly without doing a search.
        prefilterSearchCallback: null,
        // define a function that should be called before a valid match is actually is returned.
        // This callback allows a context sensitive filtering of matches, somehow similar to the
        // beforeRequestCallback of the RequestDataSource which can add additional parameters to
        // filter the search. The callback function will be passed the search term and the found
        // definition and should return true if the match should be returned or false if not.
        // should return
        approveMatchCallback: null,
        createSuggestionTokenCallback: null,
        maxResults: -1,
        // array of search string definitions
        definitions: null,
        delaySearch: false
    },
    definitions: null,
    // only supporting one category
    categoryId: '',
    searchTimeout: null,
    // will hold the index of the last found suggestion definition of the last search. An extend will
    // continue with the next definition.
    lastFoundDefinitionIdx: 0,

    initialize: function(definitions, categoryId, options) {
        this.setOptions(options);
        this.parseDefinitions(definitions);
        if (typeOf(this.options.prefilterSearchCallback) != 'function') {
            this.options.prefilterSearchCallback = false;
        }
        if (typeOf(this.options.approveMatchCallback) != 'function') {
            this.options.approveMatchCallback = false;
        }
        if (typeOf(this.options.createSuggestionTokenCallback) != 'function') {
            this.options.createSuggestionTokenCallback = false;
        }
        this.categoryId = categoryId;
    },

    parseDefinitions: function(defs) {
        var defs, def, i, caseSensitive;
        this.definitions = [];
        if (!defs) {
            return;
        }
        caseSensitive = this.options.caseSensitive;
        for (i = 0; i < defs.length; i++) {
            def = this.buildDefinition(defs[i]);
            if (def) {
                this.definitions.push(def);
            }
        }
    }.protect(),

    buildDefinition: function(def, caseSensitive) {
        var finalDef, i;
        // ignore definitions without suggestion string
        if (!def.suggestion) {
            return false;
        }
        finalDef = {};
        finalDef.suggestion = def.suggestion;
        finalDef.match = caseSensitive ? def.suggestion : def.suggestion.toLowerCase();
        finalDef.inputValue = def.inputValue || def.suggestion;
        finalDef.matchEmptyString = !!def.matchEmptyString;
        finalDef.additionalMatches = Array.from(def.additionalMatches);
        if (!finalDef.additionalMatches.length) {
            finalDef.additionalMatches = false;
        } else if (!caseSensitive) {
            // lower case for performance
            for (i = 0; i < finalDef.additionalMatches.length; i++) {
                finalDef.additionalMatches[i] = finalDef.additionalMatches[i].toLowerCase();
            }
        }
        // copy arbitrary meta data if available. A createSuggestionTokenCallback could be using it.
        if (def.metaData) {
            finalDef.metaData = def.metaData;
        }
        return finalDef;
    }.protect(),

    getStartOffset: function(isExtend) {
        return isExtend ? this.lastFoundDefinitionIdx + 1 : 0;
    },

    /**
     * Create the token to be returned to the autocompleter/suggestions which will be passed to the
     * user if he selects the suggestion.
     * 
     * @param {Object} definition The suggestion definition that matched the search
     */
    createSuggestionToken: function(definition) {
        if (this.options.createSuggestionTokenCallback) {
            return this.options.createSuggestionTokenCallback.call(null, definition);
        } else {
            return {
                inputValue: definition.inputValue,
                suggestion: definition.suggestion
            };
        }
    },

    defintionMatches: function(definition, queryValue) {
        var idx, match, fromStart, i;
        if (queryValue.length == 0) {
            match = definition.matchEmptyString;
        } else {
            fromStart = this.options.matchFromStart;
            idx = definition.match.indexOf(queryValue);
            match = idx > -1 && (!fromStart || idx == 0);
            if (!match && definition.additionalMatches) {
                for (i = 0; i < definition.additionalMatches.length; i++) {
                    idx = definition.additionalMatches[i].indexOf(queryValue);
                    if (idx > -1 && (!fromStart || idx == 0)) {
                        match = true;
                        break;
                    }
                }
            }
        }
        if (match && this.options.approveMatchCallback) {
            match = !!this.options.approveMatchCallback.call(null, queryValue, definition);
        }
        return match;
    },

    handlesCategory: function(categoryId) {
        return this.categoryId === categoryId;
    },

    search: function(queryValue, isExtend) {
        var i, maxResults, definition, metaData;
        var result = [];
        var limit = false;
        if (this.options.prefilterSearchCallback) {
            queryValue = this.options.prefilterSearchCallback.call(null, queryValue);
        }
        if (queryValue != null) {
            if (!this.options.caseSensitive) {
                queryValue = queryValue.toLowerCase();
            }
            i = this.getStartOffset(isExtend);
            maxResults = this.options.maxResults;
            // fetch one more to get hasMore feedback
            if (maxResults > 0) {
                maxResults++;
            }
            for (i; i < this.definitions.length && !limit; i++) {
                definition = this.definitions[i];
                if (this.defintionMatches(definition, queryValue)) {
                    if (maxResults > 0 && maxResults == result.length) {
                        limit = true;
                    } else {
                        result.push(this.createSuggestionToken(definition));
                        this.lastFoundDefinitionIdx = i;
                    }
                }
            }
        }
        metaData = {
            resultsReturned: result.length,
            moreResults: limit
        };
        this.fireEvent('queryComplete', [ !isExtend, this.categoryId, result, metaData ]);
    },

    /**
     * @override
     */
    queryForUpdate: function(queryValue) {
        this.lastFoundDefinitionIdx = 0;
        if (this.options.delaySearch) {
            clearTimeout(this.searchTimeout);
            this.searchTimeout = this.search.delay(1, this, queryValue);
        } else {
            this.search(queryValue, false);
        }
    },
    /**
     * @override
     */
    queryForExtend: function(queryValue, category) {
        if (this.options.delaySearch) {
            clearTimeout(this.searchTimeout);
            this.searchTimeout = this.search.delay(1, this, [ queryValue, true ]);
        } else {
            this.search(queryValue, true);
        }
    },
    resetCaches: function() {
        // do nothing
    }
});

/**
 * Data Source which echoes the current query value as a suggestion.
 */
var AutocompleterQueryEchoingDataSource = new Class({
    Implements: [ Options, Events ],

    options: {
        // function to be called before rendering the search term. The function is passed the
        // current search string and should return the string to be rendered as suggestion. If not
        // defined the search string will be rendered.
        buildSuggestionCallback: undefined,
        // creating metadata is disabled by default because it is not really useful since the
        // response contains only one result
        createMetadata: false
    },

    initialize: function(categoryId, options) {
        this.setOptions(options);
        this.categoryId = categoryId;
    },

    handlesCategory: function(categoryId) {
        return this.categoryId === categoryId;
    },

    queryForUpdate: function(queryValue) {
        var metaData, result, inputValue;
        if (this.options.buildSuggestionCallback) {
            inputValue = this.options.buildSuggestionCallback.call(null, queryValue);
        } else {
            inputValue = queryValue;
        }
        if (this.options.createMetadata) {
            metaData = {
                resultsReturned: 1,
                moreResults: false
            };
        }
        result = [];
        result.push({
            inputValue: inputValue,
            suggestion: queryValue
        });
        // directly fire the complete event
        this.fireEvent('queryComplete', [ true, this.categoryId, result, metaData ]);
    },
    queryForExtend: function(queryValue, category) {
        // do nothing
    },

    resetCaches: function() {
        // do nothing
    }

});