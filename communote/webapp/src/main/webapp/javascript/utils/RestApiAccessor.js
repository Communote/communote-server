/**
 * Helper class to access the Communote REST API.
 */
RestApiAccessor = new Class({

    Implements: Options,

    options: {
        // can be a string that contains the baseUrl. If omitted the URL will be created
        // like so: '/web/rest/' + options.restApiVersion + '/'.
        baseUrl: false,
        restApiVersion: '3.0',
        defaultErrorMessage: 'An error occured.',
        disableBasicAuthParameter: 'noAuthenticationChallenge'
    },

    baseUrl: null,

    reusableRequests: null,

    /**
     * Create a new instance.
     * 
     * @param {Object} options An object with settings that should overwrite the default options
     */
    initialize: function(options) {
        this.setOptions(options);
        if (typeof this.options.baseUrl === 'string') {
            this.baseUrl = this.options.baseUrl;
        } else {
            this.baseUrl = '/web/rest/' + this.options.restApiVersion + '/';
        }
        this.reusableRequests = {};
    },

    buildRequestUrl: function(resource, apiVersion) {
        // avoid basic authentication
        return buildRequestUrl(this.getResourceUrl(resource, apiVersion)) + '?'
                + this.options.disableBasicAuthParameter + '=true';
    }.protect(),

    doApiRequest: function(method, resource, options, successCallback, errorCallback) {
        var request, headers, defaultErrorMessage;
        var requestOptions = {};
        requestOptions.url = this.buildRequestUrl(resource, options && options.apiVersion);
        if (options) {
            requestOptions.async = options.async != null ? options.async : true;
            requestOptions.data = options.data;
            headers = options.headers;
            defaultErrorMessage = options.defaultErrorMessage;
        }
        requestOptions.noCache = true;
        if (!method) {
            requestOptions.method = 'get';
        } else {
            method = method.toLowerCase();
            // cannot use Mootools.Request emulation option as server is expecting '_method'
            // in GET params and not post body
            if (method != 'post' && method != 'get') {
                requestOptions.method = 'post';
                requestOptions.url += '&_method=' + method;
            } else {
                requestOptions.method = method;
            }
            if (method == 'post' || method == 'put') {
                // send content as JSON
                requestOptions.headers = Object.merge({
                    'Content-Type': 'application/json; charset=utf-8'
                }, headers);
                if (requestOptions.headers['Content-Type'].indexOf('application/json') > -1) {
                    requestOptions.data = JSON.encode(requestOptions.data);
                    requestOptions.urlEncoded = false;
                } else if (requestOptions.headers['Content-Type'].indexOf('application/x-www-form-urlencoded') > -1) {
                    // mootools adds the header again, so remove it
                    delete requestOptions.headers['Content-Type'];
                }
            } else if (headers) {
                requestOptions.headers = Object.append({}, headers);
            }
        }
        defaultErrorMessage = defaultErrorMessage || this.options.defaultErrorMessage;
        request = this.getOrCreateRequest(requestOptions);
        request.addEvent('complete', this.apiRequestComplete.bind(this, successCallback,
                errorCallback, defaultErrorMessage, request));
        request.send();
    },

    /**
     * Create a request based on the provided options. If there is already a asynchronous GET
     * request for the same URL and without parameters this request will be returned to avoid
     * unnecessary requests.
     * 
     * @param {Object} requestOptions The options for the request
     * @return {Request} The created or existing request
     */
    getOrCreateRequest: function(requestOptions) {
        var request;
        var reusable = requestOptions.method == 'get' && requestOptions.async !== false
                && (!requestOptions.data || Object.keys(requestOptions.data).length == 0);
        if (reusable) {
            // return reusable request or create new save for reuse
            request = this.reusableRequests[requestOptions.url];
            if (!request) {
                requestOptions.cntReusable = true;
                request = new Request.JSON(requestOptions);
                this.reusableRequests[requestOptions.url] = request;
            }
        } else {
            request = new Request.JSON(requestOptions);
        }
        return request;
    },

    apiRequestComplete: function(successCallback, errorCallback, defaultErrorMessage, request,
            response) {
        var errorDetails;
        // in case it was a reusable request remove the cached request since it is done
        if (request.options && request.options.cntReusable) {
            delete this.reusableRequests[request.options.url];
            request.options.cntReusable = false;
        }
        // request is only successful if response contains a status that is not ERROR (WARNING is considered success)
        // exception is information resource whose response has result wrapper or status flag
        if (request.status == 200 && response && (response.status && response.status != 'ERROR') || 
                (response && response.status == undefined)) {
            if (successCallback) {
                successCallback.call(null, response);
            }
        } else if (errorCallback) {
            if (response && typeof response == 'object') {
                errorDetails = response;
            } else {
                if (request.xhr && request.xhr.responseText) {
                    try {
                        // parse responseText which might not always be JSON
                        errorDetails = JSON.decode(request.xhr.responseText);
                    } catch (e) {
                        // response is not JSON, fallback will be used
                    }
                }
                if (!errorDetails) {
                    // create a fallback error object to have a consistent API
                    errorDetails = {
                        status: 'ERROR',
                        message: defaultErrorMessage
                    };
                }
            }
            // augment error object with HTTP status code
            errorDetails.httpStatusCode = request.status;
            errorCallback.call(null, errorDetails);
        }
    },

    getResourceUrl: function(resource, apiVersion) {
        var baseUrl;
        if (apiVersion == undefined) {
            baseUrl = this.baseUrl;
        } else if (apiVersion === false) {
            baseUrl = '/web/rest/';
        } else {
            baseUrl = '/web/rest/' + apiVersion + '/';
        }
        return baseUrl + resource;
    }
});