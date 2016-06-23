(function(namespace) {
    var ApplicationLinkHandler = new Class({

        Implements: Options,

        options: {
            // mapping from parameter name as provided in the URL to the name to be used in the filter
            // parameter object
            filterParameterMapping: {},
            // array of parameter names to include when parsing the parameter string. If undefined
            // every parameter is included otherwise only the listed parameters are included.
            filterParametersToInclude: undefined,
            // defines the parameter names whose values should be interpreted as an array in CSV notation
            multiValueFilterParameters: []
        },
        applicationPath: undefined,
        prefixMatch: false,

        /**
         * @param {String} applicationPath The sub path this handler supports. If the string ends
         *            with a star '*' the supports method will test whether the provided path starts
         *            with the applicationPath and has at least one more character. Otherwise the
         *            supports method will test for equality.
         * @constructs
         * @class Abstract application link handler whose supports method matches the provided path
         *        against a configurable path. The subclasses of this class can be registered to the
         *        LinkHandler.
         */
        initialize: function(applicationPath, options) {
            this.setOptions(options);
            if (applicationPath.charAt(applicationPath.length - 1) == '*') {
                this.prefixMatch = true;
                applicationPath = applicationPath.slice(0, -1);
            }
            this.applicationPath = applicationPath;
        },

        supports: function(path) {
            if (this.prefixMatch) {
                if (path.indexOf(this.applicationPath) == 0
                        && path.length > this.applicationPath.length) {
                    return true;
                }
                return false;
            } else {
                return (path == this.applicationPath);
            }
        },

        open: function(path, paramString, hashString, data) {
        },

        processLocation: function(path, paramString, hashString) {
        },

        getObjectFromQueryString: function(paramString) {
            var key, i, l, vaule, mappings, multiValueParams, cleanedResult;
            var includes = this.options.filterParametersToInclude;
            if (!includes || includes.length) {
                result = communote.utils.getObjectFromQueryString(paramString, 'array');
                // post-process
                mappings = this.options.filterParameterMapping;
                for (i in mappings) {
                    if (mappings.hasOwnProperty(i) && result[i] != undefined) {
                        if (!result[mappings[i]]) {
                            result[mappings[i]] = result[i];
                        }
                        delete result[i];
                    }
                }
                if (includes) {
                    // keep only those to include
                    cleanedResults = {};
                    for (i = 0, l = includes.length; i < l; i++) {
                        key = includes[i];
                        if (key in result) {
                            cleanedResult[key] = result[key];
                        }
                    }
                    result = cleanedResult;
                }
                multiValueParams = this.options.multiValueFilterParameters;
                for (i = 0, l = multiValueParams.length; i < l; i++) {
                    key = multiValueParams[i];
                    value = result[key];
                    if (typeof value === 'string') {
                        result[key] = value.split(',');
                    }
                }
            } else {
                result = {};
            }
            return result;
        }
    });
    if (namespace && namespace.addConstructor) {
        namespace.addConstructor('ApplicationLinkHandler', ApplicationLinkHandler);
    } else {
        window.ApplicationLinkHandler = ApplicationLinkHandler;
    }
})(window.runtimeNamespace);