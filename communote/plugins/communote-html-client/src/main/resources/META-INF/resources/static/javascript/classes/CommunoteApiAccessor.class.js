/**
 * CommunoteApiAccessor
 */
communote.widget.classes.CommunoteApiAccessor = communote.Base.extend(
/** @lends communote.widget.classes.CommunoteApiAccessor.prototype */
{

    /** the configuration object */
    configuration: null,

    /** mapping for human readable resources to url parts of the API */
    communoteResourceMapping: {
        topics: 'timelineTopics',
        writableTopics: 'topics',
        tags: 'timelineTags',
        users: 'timelineUsers',
        timeLineNotes: 'timelineNotes',
        notes: 'notes',
        tagSuggestion: 'tagSuggestionLists'
    },

    /** mapping for human readable filter names to filter parameters of the API */
    filterApiMapping: {
        filterHtml: 'filterHtml'
    },

    filterValueSeparatorMapping: {
        noteSearchString: ' '
    },

    /** the registry for all currently running JSON requests that come with a runOnceId */
    runningJsonRequests: null,

    jSessionIdUrlExtension: "",

    /**
     * @constructs CommunoteApiAccessor Class that actually does requests on the Communote REST API.
     *             Do not use it directly, use the ApiController instead.
     *
     * @param {Configuration} configuration the configuration
     */
    constructor: function(configuration) {

        this.configuration = configuration;
        this.runningJsonRequests = {};
        if (configuration.jsessionId && !configuration.usingProxy) {
            this.jSessionIdUrlExtension = ";jsessionid=" + configuration.jsessionId;
        }
    },

    /**
     * Returns the API-name for a given filter parameter (prefixed with f_)
     *
     * @param {String} paramName the filter parameter
     * @returns {String} the API name of the filter parameter
     */
    getFilterParameterName: function(paramName) {
        var finalName = this.filterApiMapping[paramName];
        return finalName || 'f_' + paramName;
    },

    /**
     * @param {String} paramName The name of the filter parameter
     * @returns {String} the string to separate the individual values of a multi-valued filter
     *          parameter (that is one with an array value). The default is a colon.
     */
    getFilterParameterValueSeparator: function(paramName) {
        var separator = this.filterValueSeparatorMapping[paramName];
        return separator || ',';
    },

    /**
     * Translates a simple resource name into an URL part of the REST API
     *
     * @param {String} resource resource in HTML client language (e.g. topics, users)
     * @returns {String} translated resource that becomes part of the request URL (e.g.
     *          timelineTopics, timelineUsers)
     */
    getMappedRessource: function(ressource) {

        return this.communoteResourceMapping[ressource];
    },

    /**
     * Build the base URL (usually beginning with http://) for REST requests
     *
     * @returns base URL
     */
    buildDataBaseUrl: function() {

        var url = this.configuration.dataHost ? this.configuration.dataHost
                : this.configuration.baseHost;
        url += this.configuration.cntPath + this.configuration.dataPath;
        return url;
    },

    /**
     * Build the relative request URL (without leading http://) for the given URL fragment and URL
     * parameters. Does special translations if a proxy is in use (configuration.usingProxy).
     *
     * @param {String} the URL fragment
     * @param {Object} URL parameters
     * @returns {String} the built URL, encoded when proxy in use
     */
    buildRequestUrl: function(urlPart, urlParams) {

        var url, serializedParams = '';

        // build request URL width jsessionid if not usingProxy
        url = urlPart  + this.jSessionIdUrlExtension;

        // serialize params if any
        if (urlParams) {

            // params are encoded implicitly
            serializedParams = communote.jQuery.param(urlParams);
        }

        // append url params only if any
        if (serializedParams.length > 0) {
            // double-encode params if proxy in use
            if (this.configuration.usingProxy) {
                serializedParams = encodeURIComponent('?' + serializedParams);
            } else {
                // prefix params with correct separator
                if (url.indexOf('?') == -1) {
                    serializedParams = '?' + serializedParams;
                } else {
                    serializedParams = '&' + serializedParams;
                }
            }
            // append params to URL
            url += serializedParams;
        }

        return url;
    },

    /**
     * Builds the complete URL for a user image for the given user ID.
     *
     * @param {integer} userId the id of the user
     * @param {size} SMALL|MEDIUM|LARGE|undefined
     * @returns {String} complete URL to the user image
     */
    buildUserImageUrl: function(userId, size) {
        var url, ts;
        size = size || 'MEDIUM';
        url = this.configuration.baseHost + this.configuration.cntPath;
        // workaround that reduces the cache time to an hour, since we do not have the photoLastModification timestamp
        ts = new Date();
        ts.setMinutes(0);
        ts.setSeconds(0);
        ts.setMilliseconds(0);
        // no need to add the doPublicAccess parameter since the public user will already be set in the current session
        url += this.buildRequestUrl('/image/user.do', {
            size: size,
            id: userId,
            t: ts.getTime()
        });
        return url;
    },

    /**
     * Builds the complete URL for the message files
     *
     * @param {String} [lang] language code, should not be provided if messages in user/browser
     *  language should be returned
     * @returns {String} complete URL to the user image
     */
    buildMessageUrl: function(lang) {
        var url = this.configuration.baseHost + this.configuration.cntPath;
        var params = {
                build: pluginBuildTimestamp,
                category: 'html-client'
        };
        if (lang) {
            params.lang = lang;
        }
        url += this.buildRequestUrl('/resources/i18n/messages', params);
        return url;
    },
    
    /**
     * Builds the complete URL for retrieving the API information
     *
     * @returns {String} complete URL to retrieve the API information
     */
    buildApiInformationUrl: function() {
        var url = this.configuration.baseHost + this.configuration.cntPath;
        url += this.buildRequestUrl('/web/rest/information', {
            '_': Date.now()
        });
        return url;
    },

    /**
     * Performs a JSON request that is defined by the given requestDescriptor object. For the
     * structure of the object see ApiController.doApiRequest. Special Cases: * runOnceId given in
     * requestDescriptor: subsequent parallel requests with the same runOnceId will cancel previous
     * requests with the same runOnceId * if no httpMethod is given in the requestDescriptor it
     * defaults to GET * PUT and DELETE requests will be tunneled through a POST request and an
     * additional _method URL parameter
     *
     * @param {Object} requestDescriptor
     * @returns void
     */
    doJsonRequest: function(requestDescriptor) {

        var urlParams = {}, url, callbackContext, jqXHR;

        // check runOnceId if set
        if (requestDescriptor.runOnceId) {
            this.abortJsonRequests(requestDescriptor.runOnceId);
        }
        if (!requestDescriptor.data){
            requestDescriptor.data = {};
        }
        // if language is overridden add lang parameter
        if (this.configuration.lang) {
            requestDescriptor.data.lang = this.configuration.lang;
        }
        
        // default HTTP method to GET
        requestDescriptor.httpMethod = requestDescriptor.httpMethod || 'GET';

        requestDescriptor.contentType = requestDescriptor.contentType || 'application/x-www-form-urlencoded';

        // handle HTTP methods
        if (requestDescriptor.httpMethod == 'GET') {

            urlParams = requestDescriptor.data;
            requestDescriptor.data = {};

        } else if (requestDescriptor.httpMethod != 'POST') {
            // TODO should be part of the POST body but this leads to a '405 Method not allowed' on REST API
            urlParams['_method'] = requestDescriptor.httpMethod;
            requestDescriptor.httpMethod = 'POST';
        }

        if (this.configuration.allowPublicAccess) {
            // set doPublicAccess attribute to true if there is no user, but do not overwrite it
            if (urlParams.doPublicAccess == undefined && !communote.currentUser) {
                urlParams.doPublicAccess = true;
            }
        } else {
            // remove doPublicAccess if not allowed
            delete urlParams.doPublicAccess;
        }
        // build full URL
        url = this.buildDataBaseUrl() + this.buildRequestUrl(requestDescriptor.urlPart, urlParams);

        // build callback context
        callbackContext = {
            self: this,
            runOnceId: requestDescriptor.runOnceId,
            eventChannel: requestDescriptor.eventChannel,
            successHandler: requestDescriptor.successHandler,
            errorHandler: requestDescriptor.errorHandler,
            bind: requestDescriptor.bind || this,
            resource: requestDescriptor.resource
        };

        // perform AJAX request
        jqXHR = communote.jQuery.ajax({

            // request information
            url: url,
            type: requestDescriptor.httpMethod,
            data: requestDescriptor.data,

            // request settings
            async: requestDescriptor.async,
            cache: false,
            dataType: 'json',
            contentType: requestDescriptor.contentType +'; charset=UTF-8',

            // event handler
            success: this.onJsonRequestSuccess,
            error: this.onJsonRequestError,
            context: callbackContext,

            // execute in success and error case
            complete: function() {

                // unmark runOnceId in running requests
                if (this.runOnceId) {

                    delete this.self.runningJsonRequests[this.runOnceId];
                }
            }
        });

        // mark this request as running
        if (requestDescriptor.runOnceId) {

            this.runningJsonRequests[requestDescriptor.runOnceId] = jqXHR;
        }
    },
    
    /**
     * this aborts all previous JsonRequest with the same runOnceId
     * 
     * @param {runOnceId} the runOnceId of the new request
     */
    
    abortJsonRequests: function (runOnceId){ 
        // skip JSON call if this request is already running
        if (typeof this.runningJsonRequests[runOnceId] == 'object') {
            // abort previous XHR
            this.runningJsonRequests[runOnceId].abort();
        }
    },

    /**
     * Event handler for successful JSON requests. Basically calls the successHandler of the
     * previously given requestDescriptor. If data.status is ERROR, the given errorHandler is called
     * instead.
     *
     * @param {Object} data the JSON answer of the request
     * @param {String} textStatus jQuery's textStatus
     * @param {Object} jqXHR jQuery's XHR object
     * @returns void
     */
    onJsonRequestSuccess: function(data, textStatus, jqXHR) {

        // this is here the callbackContext
        var callbackContext = this;

        if (data.status == 'ERROR') {

            this.errorHandler.call(callbackContext.bind, jqXHR, textStatus, null, 'apiError',
                    callbackContext.eventChannel);
        } else {

            // TODO: why changing the data structure here? what about passing the JSON-result directly?
            // rwi says: PURE rendering needs this to iterate over the results. But this shouldn't
            // be done here, because it is control specific and only required for PURE rendering
            var handlerData = {};
            var key = callbackContext.resource || 'data';
            handlerData[key] = data.result;
            handlerData["origResult"] = data;

            this.successHandler.call(callbackContext.bind, handlerData, data.message,
                    callbackContext.eventChannel);
        }
    },

    /**
     * Event handler for erroneous JSON requests. This method basically calles the errorHandler
     * function that was referenced in the requestDescriptor. If the textStatus is abort, nothing
     * happens.
     *
     * @param {Object} jqXHR jQuery's XHR object
     * @param {String} textStatus jQuery's textStatus
     * @param {Object} errorThrown the error object
     * @returns void
     */
    onJsonRequestError: function(jqXHR, textStatus, errorThrown) {

        // do nothing with aborted requests (see linked wiki page to KENMEI-3971)
        if (textStatus == 'abort') {
            return;
        }
        
        // nur im standalone modus machen (if usingProxy == false)
        if (errorThrown == 'Unauthorized' && !this.self.configuration.usingProxy) {
            window.top.location.reload();
            return;
        }

        // readability: "this" is here the callbackContext
        var callbackContext = this;
        this.errorHandler.call(callbackContext.bind, jqXHR, textStatus, errorThrown, 'error',
                callbackContext.eventChannel);
    }

});
