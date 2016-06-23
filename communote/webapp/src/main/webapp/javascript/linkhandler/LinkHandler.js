(function(namespace) {
    var LinkHandler = new Class({
        Implements: Options,

        options: {
            // whether to open links that were not handled by a registered handler in a new window/tab
            openInNewWindow: false,
            // callback function that should be called before passing the application path to the
            // handlers, e.g. for removing a suffix from the path. The function will be invoked
            // with the path and has to return the provided or modified path. 
            preprocessPathCallback: null
        },
        absoluteBaseUrl: undefined,
        relativeBaseUrl: undefined,
        handlers: undefined,

        /**
         * Create a link handler.
         * 
         * @param {String} baseURL The absolute URL which defines the start of an application link. For
         *            sub-paths of this base URL specific handlers can be registered to handle that
         *            application resource.
         * @param {Object} [options] Additional options to override default settings
         */
        initialize: function(baseURL, options) {
            var idx;
            this.setOptions(options);
            this.absoluteBaseUrl = baseURL;
            // strip protocol, host and port
            idx = baseURL.indexOf('://') + 3;
            idx += baseURL.substring(idx).indexOf('/');
            this.relativeBaseUrl = baseURL.substring(idx);
            this.handlers = [];
        },

        /**
         * Register a handler that can handle a certain sub-path of the baseURL.
         */
        registerApplicationLinkHandler: function(handler) {
            this.handlers.push(handler);
        },

        unregisterApplicationLinkHandler: function(handler) {
            this.handlers.erase(handler);
        },

        /**
         * Unregister the first handler that supports the provided application path.
         */
        unregisterApplicationLinkHandlerByPath: function(path) {
            var handler = this.getMatchingHandler(path);
            if (handler) {
                this.handlers.erase(handler);
            }
        },

        extractSubstringAfterChar: function(inputString, character) {
            var remainder, extracted;
            var idx = inputString.indexOf(character);
            if (idx > -1) {
                extracted = inputString.substring(idx + 1);
                remainder = inputString.substring(0, idx);
            } else {
                extracted = '';
                remainder = inputString;
            }
            return {
                extracted: extracted,
                remainder: remainder
            }
        },

        /**
         * Strip baseURL from the provided URL and return the application sub-path or undefined if
         * the URL does not start with the base URL.
         * 
         * @param {String} url The URL without query string or hash, but can contain a
         *            segment
         * @return {String} the application sub-path or undefined
         */
        extractApplicationPath: function(url) {
            var baseUrl, pathString, idx, preprocessCallback;
            // strip sessionid or similar segments, only supporting one segment
            idx = url.indexOf(';');
            if (idx > -1) {
                url = url.substring(0, idx);
            }
            if (url.charAt(0) == '/') {
                baseUrl = this.relativeBaseUrl;
            } else {
                baseUrl = this.absoluteBaseUrl;
            }
            // TODO do anything fancy with https/http?
            if (url.indexOf(baseUrl) == 0) {
                pathString = url.substring(baseUrl.length);
                // strip trailing slash
                if (pathString.charAt(pathString.length - 1) == '/') {
                    pathString = pathString.slice(0, -1);
                }
                preprocessCallback = this.options.preprocessPathCallback;
                if (preprocessCallback) {
                    pathString = preprocessCallback.call(null, pathString);
                }
            }
            return pathString;
        },

        /**
         * Open a link by using one of the registered handlers or navigating to the provided URL if
         * there is no matching handler.
         * 
         * @param {String} linkURL Absolute or relative URL to process. Special path elements like
         *            '..' are not supported.
         * @param {Object} [data] An optional object to provide custom data the targeted handler
         *            might require
         * @return {Boolean} returns always false to allow usage in an onclick handler
         */
        open: function(linkURL, data) {
            var hashString, paramString, pathString, handler;
            var handled = false;
            var result = this.extractSubstringAfterChar(linkURL, '#');
            hashString = result.extracted;
            result = this.extractSubstringAfterChar(result.remainder, '?');
            paramString = result.extracted;
            // strip baseUrl
            pathString = this.extractApplicationPath(result.remainder);
            handler = this.getMatchingHandler(pathString);
            if (handler) {
                handled = handler.open(pathString, paramString, hashString, data);
            }
            if (!handled) {
                // insert the sessionId into the URL to remain logged-in
                linkURL = insertSessionId(linkURL);
                if (this.options.openInNewWindow) {
                    window.open(linkURL, '_blank');
                } else {
                    // seems to be faster than window.open(linkURL, '_self');
                    // TODO find a way to stop all the code running afterwards
                    location.href = linkURL;
                }
            }
            return false;
        },

        getMatchingHandler: function(pathString) {
            var handler, i;
            if (pathString) {
                for (i = 0; i < this.handlers.length; i++) {
                    handler = this.handlers[i];
                    if (handler.supports(pathString)) {
                        return handler;
                    }
                }
            }
            return null;
        },

        /**
         * Process the location of the current page by checking the registered handlers and calling
         * the processLocation method of the first handler that supports the path of the current
         * location. This method should typically be called after the page finished loading to
         * invoke initialization logic.
         */
        processLocation: function() {
            var hashString, paramString, pathString, handler;
            var curLocation = location;
            // strip # and ? 
            if (curLocation.hash) {
                hashString = curLocation.hash.substring(1);
            }
            if (curLocation.search) {
                paramString = curLocation.search.substring(1);
            }
            pathString = this.extractApplicationPath(curLocation.protocol + '//' + curLocation.host
                    + curLocation.pathname);
            handler = this.getMatchingHandler(pathString);
            if (handler) {
                handler.processLocation(pathString, paramString, hashString);
                return true;
            }
            return false;
        }
    });
    if (namespace && namespace.addConstructor) {
        namespace.addConstructor('LinkHandler', LinkHandler);
    } else {
        window.LinkHandler = LinkHandler;
    }
})(window.runtimeNamespace);